package com.bank.infrastructures.ws

import net.codingwell.scalaguice.ScalaModule

class BankCodeJpModule extends ScalaModule {

  override def configure(): Unit = {
    bind[BankCodeJpApiClient].toInstance(BankCodeJpApiClient)
  }
}
