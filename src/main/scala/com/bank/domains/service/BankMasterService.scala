package com.bank.domains.service

import com.bank.domains.entity.BankCodeJp.BankBranch
import com.bank.domains.entity._
import com.bank.infrastructures.db.{DBIOHelper, DbManager}
import com.bank.infrastructures.db.repository.{BankBranchMasterRepository, BankMasterRepository}
import com.bank.infrastructures.ws.BankCodeJpAPI
import play.api.Logging
import play.api.http.Status
import slick.dbio.DBIO

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class BankMasterService @Inject() (
    api: BankCodeJpAPI,
    dbManager: DbManager,
    bankMasterRepository: BankMasterRepository,
    bankBranchMasterRepository: BankBranchMasterRepository
)(implicit ec: ExecutionContext)
    extends Logging
    with Status {

  def importBanks(limit: Int = api.maxLimit): Future[BanksResponse] =
    for {
      _        <- dbManager.run(DBIO.seq(bankMasterRepository.dropTableIfExists(), bankMasterRepository.createTable()))
      response <- getBanks(limit = limit)
      _ <- dbManager.run(transactionally = true) {
        bankMasterRepository.register(banks = response.banks, version = response.versionTime)
      }
    } yield response

  def importBankBranches(): Future[Seq[BankBranch]] = {
    for {
      _         <- dbManager.run(bankBranchMasterRepository.createIfNotExists())
      bankCodes <- dbManager.run(bankMasterRepository.bankCodesNotInBankMaster)
      _ = logger.info(s"not imported bank: ${bankCodes.size}")
      bankBranches <- getBankBranches(bankCodes = bankCodes)
      _ <- dbManager.run(transactionally = true) {
        bankBranchMasterRepository.register(bankBranches = bankBranches)
      }
    } yield bankBranches
  }

  def syncBanks(delete: Boolean = false): Future[(Int, Int, Seq[BankMasterEntity])] = {
    def getUpsertBanks(
        response: BanksResponse,
        imported: Seq[BankMasterEntity]
    ): (Seq[BankMasterEntity], Seq[BankResponse]) = {
      val initial: (Seq[BankMasterEntity], Seq[BankResponse]) = (Nil, Nil)
      val version                                             = response.versionTime
      response.banks.foldRight(initial) { case (bank, (updates, inserts)) =>
        imported.find(_.code == bank.code) match {
          case Some(exists) if exists.version.isBefore(version) =>
            val update = exists.copy(bank = bank, version = version)
            (updates :+ update, inserts)
          case Some(_) => (updates, inserts)
          case None    => (updates, inserts :+ bank)
        }
      }
    }

    getBanks(limit = api.maxLimit).flatMap { response =>
      dbManager.run(transactionally = true) {
        for {
          imported <- bankMasterRepository.list()
          (updates, inserts) = getUpsertBanks(response = response, imported = imported)
          _       <- bankMasterRepository.register(banks = inserts, version = response.versionTime)
          _       <- DBIO.seq(updates.map(bankMasterRepository.update): _*)
          deletes <- bankMasterRepository.list(earlierThanVersion = Some(response.versionTime))
          _ <- DBIOHelper.If(delete) {
            bankMasterRepository.delete(code = deletes.map(_.code))
          }
        } yield (updates.size, inserts.size, deletes)
      }
    }
  }

  def syncBankBranches(
      bankCode: String,
      delete: Boolean = false
  ): Future[SyncBankBranchResult] = {
    def getUpsertBankBranches(response: BankBranchesResponse, imported: Seq[BankBranchMasterEntity]) = {
      val initial: (Seq[BankBranchMasterEntity], Seq[BankBranch]) = (Nil, Nil)
      BankBranch(response).foldRight(initial) { case (branch, (updates, inserts)) =>
        imported.find(_.code == branch.code) match {
          case Some(exists) if exists.version.isBefore(branch.version) =>
            (updates :+ exists.copy(branch = branch), inserts)
          case Some(_) => (updates, inserts)
          case None    => (updates, inserts :+ branch)
        }
      }
    }

    getBankBranches(bankCode = bankCode, limit = api.maxLimit, cursor = None).flatMap { response =>
      dbManager.run(transactionally = true) {
        for {
          imported <- bankBranchMasterRepository.ofBank(bankCode = bankCode)
          (updates, inserts) = getUpsertBankBranches(response = response, imported = imported)
          _ <- bankBranchMasterRepository.register(bankBranches = inserts)
          _ <- DBIO.seq(updates.map(bankBranchMasterRepository.update): _*)
          deletes <- bankBranchMasterRepository.ofBank(
            bankCode = bankCode,
            earlierThanVersion = Some(response.versionTime)
          )
          _ <- DBIOHelper.If(delete) {
            DBIO.seq(deletes.map(branch => bankBranchMasterRepository.delete(branch.id)): _*)
          }
        } yield SyncBankBranchResult(
          bankCode = bankCode,
          hasNoBranch = (response.size == 0) && imported.isEmpty,
          updates = updates.size,
          inserts = inserts,
          deletes = deletes,
          deleted = delete
        )
      }
    }
  }

  def syncBankBranches(): Future[SyncBankBranchResults] = {
    def sync(bankCodes: Seq[String]): Future[SyncBankBranchResults] = {
      val iterator = bankCodes.iterator

      def sync(results: SyncBankBranchResults): Future[SyncBankBranchResults] = {
        if (iterator.hasNext) {
          val bankCode = iterator.next()
          Thread.sleep(api.requestInterval) // リクエストレート制限
          syncBankBranches(bankCode = bankCode).transformWith {
            case Success(result) =>
              logger.info(s"bankCode($bankCode): ${result.print}")
              sync(results = results.addCompleted(result = result))
            case Failure(e: BankCodeJpApiException) if e.status == NOT_FOUND =>
              logger.warn(s"bankCode($bankCode): ${e.reason}")
              sync(results = results.copy(banksNotFound = results.banksNotFound :+ bankCode))
            case Failure(e: BankCodeJpApiException) if e.status == TOO_MANY_REQUESTS =>
              logger.warn(s"bankCode($bankCode): ${e.reason}")
              Future.successful(results.copy(limitExceeded = true))
            case Failure(e) =>
              logger.error(s"bankCode($bankCode) failed", e)
              Future.successful(results.copy(failure = Some(bankCode)))
          }
        } else {
          Future.successful(results)
        }
      }

      sync(results = SyncBankBranchResults(target = bankCodes))
    }

    for {
      bankCodes <- dbManager.run(bankMasterRepository.bankCodesHasEarlierVersionBranches)
      _ = logger.info(s"target banks: ${bankCodes.size}")
      results <- sync(bankCodes = bankCodes)
    } yield results
  }

  def getBanks(limit: Int, cursor: Option[String] = None): Future[BanksResponse] =
    api.getBanks(limit = limit, cursor = cursor).flatMap {
      case Right(value)   => Future.successful(value.response)
      case Left(response) => Future.failed(BankCodeJpApiException(response))
    }

  /** 指定した金融機関の支店一覧を取得する（最大2000件まで）
    * @param bankCode　金融機関コード
    * @param limit 取得する件数
    * @param cursor フェッチカーソル
    * @return 支店一覧
    */
  def getBankBranches(
      bankCode: String,
      limit: Int,
      cursor: Option[String]
  ): Future[BankBranchesResponse] = {
    api.getBankBranches(bankCode = bankCode, limit = limit, cursor = cursor).flatMap {
      case Right(response) =>
        logger.debug(s"url=${response.url}, status=${response.status}, size=${response.response.size}")
        Future.successful(response.response)
      case Left(response) =>
        logger.debug(s"url=${response.url}, status=${response.status}, reason=${response.reason}")
        Future.failed(BankCodeJpApiException(response))
    }
  }

  /** 指定した金融機関の支店を全件取得する
    * @param bankCode 金融機関コード
    * @return 支店一覧（API毎の配列）
    */
  def getBankBranches(bankCode: String): Future[Seq[BankBranch]] = {
    val log: PartialFunction[Throwable, Unit] = {
      case e: BankCodeJpApiException if e.status == TOO_MANY_REQUESTS || e.status == NOT_FOUND =>
        logger.warn(s"bankCode($bankCode): ${e.reason}")
      case e => logger.error(s"bankCode($bankCode): failed", e)
    }

    getAllBankBranches(bankCode = bankCode).flatMap { results =>
      val responses: Future[Seq[BankBranch]] = Future.successful(Nil)
      results.foldLeft(responses) { (responses, result) =>
        responses.flatMap(responses =>
          result match {
            case Right(response) =>
              logger.info(s"bankCode($bankCode): completed(${response.size})")
              Future.successful(responses ++ BankBranch(response))
            case Left(e) =>
              log(e)
              Future.failed(e)
          }
        )
      }
    }
  }

  /** 複数の金融機関の支店を取得する
    * <pre>
    * - APIエラーになるまでの再帰処理
    * - 1日のリクエスト回数上限を超えた場合、エラーにせずそれまで取得した支店一覧を返す
    * - APIレスポンスが存在しない金融機関コード(404)の場合はスキップして処理を継続する（旧バージョンでは存在する金融機関コード）
    * </pre>
    * @param bankCodes 金融機関リスト
    * @return 支店一覧
    */
  def getBankBranches(bankCodes: Seq[String]): Future[Seq[BankBranch]] = {
    val iterator = bankCodes.iterator

    def getBranches(result: Either[Throwable, Seq[BankBranch]]): Future[Either[Throwable, Seq[BankBranch]]] = {
      result match {
        case _ if !iterator.hasNext => Future.successful(result)
        case Right(branches) =>
          Thread.sleep(api.requestInterval) // リクエストレート制限
          getBankBranches(bankCode = iterator.next()).transformWith {
            case Success(ofBankCode)                                         => getBranches(result = Right(branches ++ ofBankCode))
            case Failure(e: BankCodeJpApiException) if e.status == NOT_FOUND => getBranches(result = Right(branches))
            case Failure(e: BankCodeJpApiException) if e.status == TOO_MANY_REQUESTS =>
              Future.successful(Right(branches))
            case Failure(e) => Future.successful(Left(e))
          }
        case Left(_) => Future.successful(result)
      }
    }

    getBranches(result = Right(Nil)).flatMap {
      case Right(branches) => Future.successful(branches)
      case Left(e)         => Future.failed(e)
    }
  }

  /** 指定した金融機関の全支店を取得する
    * <pre>
    * - カーソルを使って複数のAPIに分けて全支店を取得
    * - hasNext=falseまたはAPIエラーになるまでの再帰処理
    * - APIエラーになるまでの結果を保持するため`Seq[Either[_]]`を返す
    * </pre>
    * @param bankCode 金融機関コード
    * @return APIの実行結果リスト
    */
  private def getAllBankBranches(bankCode: String): Future[Seq[Either[Throwable, BankBranchesResponse]]] = {
    def getAllBankBranches(
        bankCode: String,
        limit: Int,
        cursor: Option[String],
        results: Seq[Either[Throwable, BankBranchesResponse]]
    ): Future[Seq[Either[Throwable, BankBranchesResponse]]] = {
      Thread.sleep(api.requestInterval) // リクエストレート制限
      getBankBranches(bankCode = bankCode, limit = limit, cursor = cursor).transformWith {
        case Success(response) if response.hasNext =>
          val latest = results :+ Right(response)
          getAllBankBranches(bankCode = bankCode, limit = limit, cursor = response.nextCursor, results = latest)
        case Success(response) => Future.successful(results :+ Right(response))
        case Failure(e)        => Future.successful(results :+ Left(e))
      }
    }

    getAllBankBranches(bankCode = bankCode, limit = api.maxLimit, cursor = None, results = Nil)
  }
}
