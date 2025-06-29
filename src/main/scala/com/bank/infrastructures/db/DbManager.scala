package com.bank.infrastructures.db

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.basic.{BasicProfile, DatabaseConfig}
import slick.jdbc.JdbcProfile
import slick.sql.SqlAction

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class DbManager @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends JdbcDao

trait JdbcDao extends HasDatabaseConfigProvider[JdbcProfile] {
  protected val dbConfigProvider: DatabaseConfigProvider
  import profile.api._

  def run[T](action: DBIO[T]): Future[T] =
    run(transactionally = false, action = action)

  def run[T](transactionally: Boolean, action: DBIO[T]): Future[T] =
    db.run(if (transactionally) action.transactionally else action)

  def run[T](transactionally: Boolean)(block: => DBIO[T]): Future[T] =
    run(transactionally = transactionally, action = block)

  def sql[R, S <: NoStream, E <: Effect](action: SqlAction[R, S, E]): Seq[String] =
    action.statements.toSeq

  def close(): Unit = db.close()
}

object NamedDatabaseConfigProvider {
  def apply(dbName: String): DatabaseConfigProvider = new DatabaseConfigProvider {
    val dbConfig = DatabaseConfig.forConfig[JdbcProfile](s"slick.dbs.$dbName")
    def get[P <: BasicProfile]: DatabaseConfig[P] = {
      dbConfig.asInstanceOf[DatabaseConfig[P]]
    }
  }
}
