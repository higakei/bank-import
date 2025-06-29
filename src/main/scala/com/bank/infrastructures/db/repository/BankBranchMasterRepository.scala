package com.bank.infrastructures.db.repository

import com.bank.domains.entity.{BankBranchMasterEntity, BankCodeJp}
import com.bank.infrastructures.db.BankMapper
import com.bank.infrastructures.db.dto.Tables.BankBranchMaster
import com.bank.utilities.TimestampSupport
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import java.time.OffsetDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class BankBranchMasterRepository @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile]
    with BankMapper
    with TimestampSupport {
  import profile.api._

  def createIfNotExists(): DBIO[Unit] = {
    val tableName  = BankBranchMaster.baseTableRow.tableName
    val showTables = sql"show tables like $tableName;"
    val ifExists   = showTables.as[String].headOption
    ifExists.flatMap {
      case Some(_) => DBIO.successful(())
      case None    => BankBranchMaster.schema.create
    }
  }

  def register(bankBranches: Seq[BankCodeJp.BankBranch]): DBIO[Option[Int]] =
    BankBranchMaster ++= bankBranches.map(toInsertRow)

  def ofBank(bankCode: String, earlierThanVersion: Option[OffsetDateTime] = None): DBIO[Seq[BankBranchMasterEntity]] = {
    val query = BankBranchMaster.filter(_.bankCode === bankCode).filterOpt(earlierThanVersion)(_.version < _)
    query.result.map(_.map(toEntity))
  }

  def update(entity: BankBranchMasterEntity): DBIO[Int] = {
    val onUpdate = entity.copy(updatedAt = now())
    BankBranchMaster.filter(_.id === entity.id).update(toRow(entity = onUpdate))
  }

  def delete(id: Int): DBIO[Int] =
    BankBranchMaster.filter(_.id === id).delete
}
