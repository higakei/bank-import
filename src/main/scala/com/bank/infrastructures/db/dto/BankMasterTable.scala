package com.bank.infrastructures.db.dto

// AUTO-GENERATED Slick data model for table BankMaster
trait BankMasterTable {

  self: Tables =>

  import profile.api._
  import java.time._
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** Entity class storing rows of table BankMaster
    *  @param id Database column id SqlType(INT), AutoInc, PrimaryKey
    *  @param code Database column code SqlType(VARCHAR), Length(4,true)
    *  @param name Database column name SqlType(TEXT)
    *  @param halfWidthKana Database column half_width_kana SqlType(TEXT)
    *  @param fullWidthKana Database column full_width_Kana SqlType(TEXT)
    *  @param hiragana Database column hiragana SqlType(TEXT)
    *  @param businessTypeCode Database column business_type_code SqlType(VARCHAR), Length(5,true)
    *  @param businessType Database column business_type SqlType(TEXT)
    *  @param version Database column version SqlType(TIMESTAMP)
    *  @param createdAt Database column created_at SqlType(TIMESTAMP)
    *  @param updatedAt Database column updated_at SqlType(TIMESTAMP)
    */
  case class BankMasterRow(
      id: Int,
      code: String,
      name: String,
      halfWidthKana: String,
      fullWidthKana: String,
      hiragana: String,
      businessTypeCode: String,
      businessType: String,
      version: OffsetDateTime,
      createdAt: OffsetDateTime = OffsetDateTime.now().withNano(0),
      updatedAt: OffsetDateTime = OffsetDateTime.now().withNano(0)
  )

  /** GetResult implicit for fetching BankMasterRow objects using plain SQL queries */
  implicit def GetResultBankMasterRow(implicit e0: GR[Int], e1: GR[String], e2: GR[OffsetDateTime]): GR[BankMasterRow] =
    GR { prs =>
      import prs._
      BankMasterRow.tupled(
        (
          <<[Int],
          <<[String],
          <<[String],
          <<[String],
          <<[String],
          <<[String],
          <<[String],
          <<[String],
          <<[OffsetDateTime],
          <<[OffsetDateTime],
          <<[OffsetDateTime]
        )
      )
    }

  /** Table description of table bank_master. Objects of this class serve as prototypes for rows in queries. */
  class BankMaster(_tableTag: Tag) extends profile.api.Table[BankMasterRow](_tableTag, "bank_master") {
    def * = (
      id,
      code,
      name,
      halfWidthKana,
      fullWidthKana,
      hiragana,
      businessTypeCode,
      businessType,
      version,
      createdAt,
      updatedAt
    ) <> (BankMasterRow.tupled, BankMasterRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (
      (
        Rep.Some(id),
        Rep.Some(code),
        Rep.Some(name),
        Rep.Some(halfWidthKana),
        Rep.Some(fullWidthKana),
        Rep.Some(hiragana),
        Rep.Some(businessTypeCode),
        Rep.Some(businessType),
        Rep.Some(version),
        Rep.Some(createdAt),
        Rep.Some(updatedAt)
      )
    ).shaped.<>(
      { r =>
        import r._;
        _1.map(_ =>
          BankMasterRow.tupled(
            (_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get)
          )
        )
      },
      (_: Any) => throw new Exception("Inserting into ? projection not supported.")
    )

    /** Database column id SqlType(INT), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)

    /** Database column code SqlType(VARCHAR), Length(4,true) */
    val code: Rep[String] = column[String]("code", O.Length(4, varying = true))

    /** Database column name SqlType(TEXT) */
    val name: Rep[String] = column[String]("name")

    /** Database column half_width_kana SqlType(TEXT) */
    val halfWidthKana: Rep[String] = column[String]("half_width_kana")

    /** Database column full_width_Kana SqlType(TEXT) */
    val fullWidthKana: Rep[String] = column[String]("full_width_Kana")

    /** Database column hiragana SqlType(TEXT) */
    val hiragana: Rep[String] = column[String]("hiragana")

    /** Database column business_type_code SqlType(VARCHAR), Length(5,true) */
    val businessTypeCode: Rep[String] = column[String]("business_type_code", O.Length(5, varying = true))

    /** Database column business_type SqlType(TEXT) */
    val businessType: Rep[String] = column[String]("business_type")

    /** Database column version SqlType(TIMESTAMP) */
    val version: Rep[OffsetDateTime] = column[OffsetDateTime]("version")

    /** Database column created_at SqlType(TIMESTAMP) */
    val createdAt: Rep[OffsetDateTime] = column[OffsetDateTime]("created_at")

    /** Database column updated_at SqlType(TIMESTAMP) */
    val updatedAt: Rep[OffsetDateTime] = column[OffsetDateTime]("updated_at")

    /** Index over (version) (database name idx_bank_master_version) */
    val index1 = index("idx_bank_master_version", version)

    /** Uniqueness Index over (code) (database name uk_bank_master) */
    val index2 = index("uk_bank_master", code, unique = true)
  }

  /** Collection-like TableQuery object for table BankMaster */
  lazy val BankMaster = new TableQuery(tag => new BankMaster(tag))
}
