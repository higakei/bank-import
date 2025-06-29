package com.bank

import com.bank.domains.service.BankMasterService
import play.api.Logging

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

/** 金融機関データインポート
  * <pre>
  * BankCodeJp APIから金融機関データを登録する
  * - sbt "bankImport / runMain com.manabo.bank.ImportBanksApp"
  *
  * ・前提条件
  * - bank_masterにデータがある場合はデータを退避しておくこと（DROP IF EXISTSしているため）
  * </pre>
  */
object ImportBanksApp extends App with BankImportApp with Logging {
  logger.info("import banks start")
  withApp { injector => _: ExecutionContext =>
    val service  = injector.getInstance(classOf[BankMasterService])
    val result   = service.importBanks()
    val response = Await.result(result, Duration.Inf)

    if (response.hasNext) {
      logger.warn(s"import banks not completed, size=${response.size}, nextCursor=${response.nextCursor}")
    } else {
      logger.info(s"import banks completed, size=${response.size}")
    }
  }
}

/** 金融機関データ同期
  * <pre>
  * 金融機関データをBankCodeJpと同期する
  * - sbt "bankImport / runMain com.manabo.bank.SyncBanksApp delete"
  * - deleteパラメータを指定した場合、最新バージョンに存在しない金融機関を削除する
  * - deleteパラメータを指定しない場合、削除しないでリストアップのみ
  * - 更新対象は最新バージョンではない金融機関で、変更がない場合はバージョンのみ更新
  * </pre>
  */
object SyncBanksApp extends App with BankImportApp with Logging {
  val delete = args.contains("delete")
  logger.info("synchronize banks start")
  withApp { injector => _: ExecutionContext =>
    val service = injector.getInstance(classOf[BankMasterService])
    val action  = service.syncBanks(delete = delete)
    val result  = Await.result(action, Duration.Inf)
    logger.info("synchronize banks completed")
    result match {
      case (updates, inserts, deletes) =>
        if (delete) {
          logger.info(s"更新：$updates, 登録：$inserts, 削除：${deletes.size}")
        } else {
          logger.info(s"更新：$updates, 登録：$inserts")
          logger.info(s"削除対象：${deletes.size}（未削除）")
          deletes.foreach(delete => logger.info(s"code=${delete.code}, name=${delete.name}"))
        }
    }
  }
}
