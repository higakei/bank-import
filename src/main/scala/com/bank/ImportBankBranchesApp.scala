package com.bank

import com.bank.domains.entity.BankBranchMasterEntity
import com.bank.domains.entity.BankCodeJp.BankBranch
import com.bank.domains.service.BankMasterService
import play.api.Logging

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

/** 支店データインポート
  * <pre>
  * BankCodeJp APIから支店データを登録する
  * - sbt "bankImport / runMain com.manabo.bank.ImportBankBranchesApp"
  * - 無料アカウントでは１日のリクエスト上限があるので、数日に分けてインポートする
  * </pre>
  */
object ImportBankBranchesApp extends App with BankImportApp with Logging {
  logger.info("import bank branches start")
  withApp { injector => _: ExecutionContext =>
    val service  = injector.getInstance(classOf[BankMasterService])
    val action   = service.importBankBranches()
    val branches = Await.result(action, Duration.Inf)
    logger.info(s"import bank branches completed, branches=${branches.size}")
  }
}

/** 支店データ同期（金融機関コード指定）
  * <pre>
  * 指定した金融機関の支店データをBankCodeJpと同期する
  * - sbt "bankImport / runMain com.manabo.bank.SyncBankBranchApp 0001 delete"
  * - 第一パラメータに支店コードを指定する
  * - deleteパラメータを指定した場合、最新バージョンに存在しない支店を削除する
  * - deleteパラメータを指定しない場合、削除しないでリストアップのみ
  * - 更新対象は最新バージョンではない支店で、変更がない場合はバージョンのみ更新
  * </pre>
  */
object SyncBankBranchesApp extends App with BankImportApp with Logging {
  val delete = args.contains("delete")

  private val sync: String => Unit = { bankCode =>
    logger.info(s"sync bank branches start: bankCode=$bankCode")
    withApp { injector => _: ExecutionContext =>
      val service = injector.getInstance(classOf[BankMasterService])
      val action  = service.syncBankBranches(bankCode = bankCode, delete = delete)
      val result  = Await.result(action, Duration.Inf)
      logger.info(s"sync bank branches completed: ${result.print}")

      if (result.inserts.nonEmpty) logger.info("新規登録")
      result.inserts.foreach(branch => logger.info(s"code=${branch.code}, name=${branch.name}"))

      if (result.deletes.nonEmpty && result.deleted) logger.info("削除")
      if (result.deletes.nonEmpty && !result.deleted) logger.info("削除対象")
      result.deletes.foreach(branch => logger.info(s"code=${branch.code}, name=${branch.name}"))
    }
  }

  args.headOption match {
    case Some(bankCode) => sync(bankCode)
    case None           => logger.error(s"not found bank code in ${args.toList}")
  }
}

/** 支店データ同期
  * <pre>
  * 支店データをBankCodeJpと同期する
  * - sbt "bankImport / runMain com.manabo.bank.SynBranchesApp"
  * - 無料アカウントでは１日のリクエスト上限があるので、数日に分けて同期する
  * - 更新対象は最新バージョンではない支店で、変更がない場合はバージョンのみ更新
  * - 削除対象データは確認のため削除しない(金融機関コード指定で削除する)
  * </pre>
  */
object SynBranchesApp extends App with BankImportApp with Logging {
  logger.info(s"sync bank branches start")
  withApp { injector => _: ExecutionContext =>
    val service = injector.getInstance(classOf[BankMasterService])
    val action  = service.syncBankBranches()
    val result  = Await.result(action, Duration.Inf)
    logger.info("sync bank branches end")

    logger.info("同期結果")
    logger.info(s"対象金融機関：${result.target.size}件")
    logger.info(s"同期済：${result.completed.size}件")
    logger.info(s"金融機関なし：${result.banksNotFound.size}件")
    logger.info(s"未処理：${result.notSynced}件")
    result.failure.foreach(bankCode => logger.info(s"${bankCode}でエラーが発生したので同期を中断しました"))
    if (result.limitExceeded) logger.info(s"１日リクエスト回数の上限に達したので同期を中断しました")

    if (result.hasNoBranch.nonEmpty) logger.info("---- 支店なし -----")
    result.hasNoBranch.foreach { bankCode => logger.info(s"bankCode=$bankCode") }

    if (result.banksNotFound.nonEmpty) logger.info("---- 金融機関なし -----")
    result.banksNotFound.foreach { bankCode => logger.info(s"bankCode=$bankCode") }

    if (result.inserts.nonEmpty) logger.info("---- 新規登録 -----")
    result.inserts.foreach { branch: BankBranch =>
      logger.info(s"bankCode=${branch.bankCode}, code=${branch.code}, name=${branch.name}")
    }

    if (result.deletes.nonEmpty) logger.info("---- 削除対象 -----")
    result.deletes.foreach { branch: BankBranchMasterEntity =>
      logger.info(s"bankCode=${branch.bankCode}, code=${branch.code}, name=${branch.name}")
    }
  }
}
