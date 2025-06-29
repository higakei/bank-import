package com.bank

import akka.actor.ActorSystem
import akka.stream.{Materializer, SystemMaterializer}
import com.google.inject.{Guice, Injector}
import com.bank.infrastructures.db.NamedDatabaseConfigProvider
import com.bank.infrastructures.ws.BankCodeJpModule
import com.typesafe.config.ConfigFactory
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import play.api.libs.ws.ahc.{AhcWSClient, StandaloneAhcWSClient}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

trait BankImportApp {

  def withApp[T](block: Injector => ExecutionContext => T): T =
    withWsClient { (system, ws) =>
      withDbConfigProvider { dbConfigProvider =>
        val app = BankImportApplication(
          system = system,
          ws = ws,
          configuration = Configuration(ConfigFactory.load()),
          dbConfigProvider = Some(dbConfigProvider)
        )
        val injector = createInjector(app = app)
        block(injector)(system.dispatcher)
      }
    }

  def withoutDB[T](block: Injector => ExecutionContext => T): T = {
    withWsClient { (system, ws) =>
      val app = BankImportApplication(
        system = system,
        ws = ws,
        configuration = Configuration(ConfigFactory.load()),
        dbConfigProvider = None
      )
      val injector = createInjector(app = app)
      block(injector)(system.dispatcher)
    }
  }

  private def createInjector(app: BankImportApplication): Injector =
    Guice.createInjector(new BankImportModule(app = app), new BankCodeJpModule)

  private def withActorSystem[T](block: ActorSystem => T): T = {
    val system = ActorSystem()
    try {
      block(system)
    } finally {
      Await.result(system.terminate(), Duration.Inf)
    }
  }

  private def withWsClient[T](block: (ActorSystem, WSClient) => T): T = {
    withActorSystem { system =>
      val materializer: Materializer = SystemMaterializer(system).materializer
      val ws                         = new AhcWSClient(StandaloneAhcWSClient()(materializer))
      try {
        block(system, ws)
      } finally {
        ws.close()
      }
    }
  }

  private def withDbConfigProvider[T](block: DatabaseConfigProvider => T): T = {
    val dbConfigProvider = NamedDatabaseConfigProvider("default")
    try {
      block(dbConfigProvider)
    } finally {
      dbConfigProvider.get.db.close()
    }
  }
}

case class BankImportApplication(
    system: ActorSystem,
    ws: WSClient,
    configuration: Configuration,
    dbConfigProvider: Option[DatabaseConfigProvider]
)

class BankImportModule(app: BankImportApplication) extends ScalaModule {

  override def configure(): Unit = {
    bind[ActorSystem].toInstance(app.system)
    bind[ExecutionContext].toInstance(app.system.dispatcher)
    bind[WSClient].toInstance(app.ws)
    bind[Configuration].toInstance(app.configuration)
    app.dbConfigProvider.foreach(bind[DatabaseConfigProvider].toInstance(_))
  }
}
