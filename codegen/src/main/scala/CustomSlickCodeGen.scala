import com.typesafe.config.ConfigFactory
import slick.jdbc.MySQLProfile
import slick.jdbc.MySQLProfile.api.Database

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object CustomSlickCodeGen extends App {
  val config = ConfigFactory.load()
  ConfigFactory.load()
  val defaultPath = "slick.dbs.default"
  val driver      = config.getString(s"$defaultPath.db.driver")
  val url         = config.getString(s"$defaultPath.db.url")
  val user        = config.getString(s"$defaultPath.db.user")
  val password    = config.getString(s"$defaultPath.db.password")
  val outputDir   = "app/"
  val pkg         = "infrastructures.dto"

  val db = Database.forURL(
    url = this.url,
    driver = this.driver,
    user = this.user,
    password = this.password
  )

  val ignoreTables = Seq("ignore_tables")
  val codegenTargetTables = MySQLProfile.createModel(
    Some(
      MySQLProfile.defaultTables.map(
        _.filter(table => !ignoreTables.contains(table.name.name.toLowerCase))
      )
    )
  )

  val modelFuture = db.run(codegenTargetTables)

  val codegenFuture =
    modelFuture.map(model =>
      new CustomSlickSourceCodeGenerator(model)
        .writeToMultipleFiles("infrastructures.profile.CustomizedMySQLProfile", outputDir, pkg)
    )

  Await.result(codegenFuture, Duration.Inf)
}
