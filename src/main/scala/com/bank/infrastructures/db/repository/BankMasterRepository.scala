package com.bank.infrastructures.db.repository

import com.bank.domains.entity.{BankMasterEntity, BankResponse}
import com.bank.infrastructures.db.BankMapper
import com.bank.infrastructures.db.dto.Tables.{BankBranchMaster, BankMaster}
import com.bank.utilities.TimestampSupport
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import java.time.OffsetDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class BankMasterRepository @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile]
    with BankMapper
    with TimestampSupport {
  import profile.api._

  def createTable(): DBIO[Unit] =
    BankMaster.schema.create

  def dropTableIfExists(): DBIO[Unit] =
    BankMaster.schema.dropIfExists

  def register(banks: Seq[BankResponse], version: OffsetDateTime): DBIO[Option[Int]] =
    BankMaster ++= banks.map(toInsertRow(_, version))

  def bankCodesNotInBankMaster: DBIO[Seq[String]] = {
    BankMaster.filterNot(bank => BankBranchMaster.filter(_.bankCode === bank.code).exists).map(_.code).result
  }

  def update(entity: BankMasterEntity): DBIO[Int] = {
    val onUpdate = entity.copy(updatedAt = now())
    BankMaster.filter(_.id === entity.id).update(toRow(entity = onUpdate))
  }

  def list(earlierThanVersion: Option[OffsetDateTime] = None): DBIO[Seq[BankMasterEntity]] =
    BankMaster.filterOpt(earlierThanVersion)(_.version < _).result.map(_.map(toEntity))

  def delete(code: Seq[String]): DBIO[Unit] =
    for {
      _ <- BankBranchMaster.filter(_.bankCode inSet code).delete
      _ <- BankMaster.filter(_.code inSet code).delete
    } yield ()

  /** 支店が最新バージョンではない金融機関コード一覧
    * <pre>
    * - 金融機関のバージョンより支店のバージョンが古い金融機関
    * - 支店が登録されていない金融機関
    * </pre>
    * @return 金融機関コード一覧
    */
  def bankCodesHasEarlierVersionBranches: DBIO[Seq[String]] = {
    val query    = BankBranchMaster.groupBy(_.bankCode).map { case (bankCode, t) => (bankCode, t.map(_.version).min) }
    val leftJoin = BankMaster.joinLeft(query).on { case (bank, (bankCode, _)) => bank.code === bankCode }
    val filter = leftJoin.filter { case (bank, branch) =>
      branch.flatMap { case (_, version) => version }.map(_ < bank.version).getOrElse(true)
    }

    filter.map { case (bank, _) => bank.code }.result
  }
}
