# 金融機関データインポート
[BankcodeJP API](https://bankcode-jp.com/)から金融機関データをインポート機能を実装から金融機関データをインポート機能を実装
## DB
- bank_master
  金融機関テーブル
- bank_branch_master
  支店テーブル
## DI
playと互換性を持たせるため、Guiceを使ってExecutionContext、WSClient、Configuration、DatabaseConfigProviderをDIできるように実装
## Logging
playと互換性を持たせるため、play.api.Loggingを使用
## CodeGenerator
Tablesのcreated_atとupdate_atをTimestampからOffsetDateTimeに変換するために、codegen / CustomSlickSourceCodeGeneratorを下記のように変更をしTablesを生成
- 22行目
  LocalDateTimeからOffsetDateTimeに変更
  TimestampのカラムはTableRowはOffsetDateTimeになる
```scala
    override def rawType: String = model.tpe match {
        case "java.sql.Timestamp" => "OffsetDateTime"
        case "java.sql.Date"      => "LocalDate"
        case "java.sql.Time"      => "LocalTime"
        case _                    => super.rawType
      }
```
- 29行目
  createdAtとupdatedAtのデフォルト値はCURRENT_TIMESTAMPなので以下のようにOffsetDateTimeで初期値を設定する
```scala
      override def default: Option[String] = rawName match {
        case "createdAt" | "updatedAt" => Some("OffsetDateTime.now().withNano(0)")
        case _                         => super.default
      }
```
## MySQLProfile
TimestampからOffsetDateTimeを変更した時、OffsetDateTimeのJDBC のデフォルトは文字列でやり取りしていて、select時パースエラーになる。（ISO 8601以外はエラー、now()などで設定した場合はISO 8601以外のフォーマット）
CustomizedMySQLProfileを作成して、TimestampとOffsetDateTimeの変換を実装し、以下のようにCustomizedMySQLProfileを使うように修正
- application.conf
  slickのprofileをMySQLProfileからCustomizedMySQLProfileに変更
- CustomSlickCodeGen
  41行目のwriteToMultipleFilesの引数にCustomizedMySQLProfileを設定して、Tablesを生成
```scala
  val codegenFuture =
    modelFuture.map(model =>
      new CustomSlickSourceCodeGenerator(model)
        .writeToMultipleFiles("com.bank.infrastructures.db.CustomizedMySQLProfile", outputDir, pkg)
    )
```
## mysql-connector-java
8.0.23からタイムゾーン仕様が変更されて、挙動が変わった
https://github.com/yukihane/hello-java/tree/main/spring/mysql-timezone-example
application.confのJDBC_URLのserverTimezoneが効かなく、`forceConnectionTimeZoneToSession=true`を設定して解決  
https://qiita.com/kazuki43zoo/items/c94c22ff7ae620c461ee
https://dev.mysql.com/doc/relnotes/connector-j/8.0/en/news-8-0-23.html
## BankcodeJP API
- limit
  max limitは2,000件で、今のところ金融機関データと支店データは2,000件を超えることはない（支店APIは金融機関コード指定しなければならない）
- リクエストレート
  支店データは金融機関毎に支店APIを呼び出すため、
  リクエストレートを考慮して、スリープしなければならず、APIアクセスはFutureが返るので、再帰処理で支店データを取得
- リクエスト制限
  有料プランのPRO以外は、１日のリクエスト制限があり、リクエスト制限に達したらデータインポートを終了するよう実装（支店データ）
- バージョン
  金融機関データベースのバージョン
  同期をとる時に利用する
- カーソル
  ページングする時のAPIパラメータ
## APP
- 支店データは前提として金融機関データを取り込んでから実行し（bank_codeがFK）、リクエスト制限があるので複数回実行する
- 同期APPは、既存データはバージョンも更新し、新規データは登録する（バージョンのみ更新する場合もある）
- ImportBanksApp
  金融機関データをインポートする（登録のみ）
- SyncBanksApp
  インポート後の金融機関データをBankcodeJPと同期する
  削除対象データはパラメータで削除するかどうか設定できる
- ImportBankBranchesApp
  支店データインポートする（登録のみ）
- SyncBankBranchesApp
  指定した金融機関の支店データをBankcodeJPと同期する
  削除対象データはパラメータで削除するかどうか設定できる
- SynBranchesApp
  全金融機関の支店データをBankcodeJPと同期する
  削除対象データは削除しない（SyncBankBranchesAppで削除）
## 調査結果
- 支店データのインポートは金融機関単位でAPIを使用するため、無料プランだとリクエスト制限があり複数日かかる（現在金融機関が1,100件あり1日350回までなので4日）が毎年の金融機関データ更新のために利用するには無料プランで問題ない
- BankcodeJP APIはJSONPにも対応し、曖昧検索もできるので、有料プランであれば、フロントからでも利用できます。