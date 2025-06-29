package com.bank.infrastructures.db.dto

// AUTO-GENERATED Slick data model for table BankBranchMaster
trait BankBranchMasterTable {

  self: Tables =>

  import profile.api._
  import slick.model.ForeignKeyAction
  import java.time._
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** Entity class storing rows of table BankBranchMaster
    *  @param id Database column id SqlType(INT), AutoInc, PrimaryKey
    *  @param code Database column code SqlType(VARCHAR), Length(4,true)
    *  @param bankCode Database column bank_code SqlType(VARCHAR), Length(4,true)
    *  @param name Database column name SqlType(TEXT)
    *  @param halfWidthKana Database column half_width_kana SqlType(TEXT)
    *  @param fullWidthKana Database column full_width_Kana SqlType(TEXT)
    *  @param hiragana Database column hiragana SqlType(TEXT)
    *  @param version Database column version SqlType(TIMESTAMP)
    *  @param createdAt Database column created_at SqlType(TIMESTAMP)
    *  @param updatedAt Database column updated_at SqlType(TIMESTAMP)
    */
  case class BankBranchMasterRow(
      id: Int,
      code: String,
      bankCode: String,
      name: String,
      halfWidthKana: String,
      fullWidthKana: String,
      hiragana: String,
      version: OffsetDateTime,
      createdAt: OffsetDateTime = OffsetDateTime.now().withNano(0),
      updatedAt: OffsetDateTime = OffsetDateTime.now().withNano(0)
  )

  /** GetResult implicit for fetching BankBranchMasterRow objects using plain SQL queries */
  implicit def GetResultBankBranchMasterRow(implicit
      e0: GR[Int],
      e1: GR[String],
      e2: GR[OffsetDateTime]
  ): GR[BankBranchMasterRow] = GR { prs =>
    import prs._
    BankBranchMasterRow.tupled(
      (
        <<[Int],
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

  /** Table description of table bank_branch_master. Objects of this class serve as prototypes for rows in queries. */
  class BankBranchMaster(_tableTag: Tag)
      extends profile.api.Table[BankBranchMasterRow](_tableTag, "bank_branch_master") {
    def * = (
      id,
      code,
      bankCode,
      name,
      halfWidthKana,
      fullWidthKana,
      hiragana,
      version,
      createdAt,
      updatedAt
    ) <> (BankBranchMasterRow.tupled, BankBranchMasterRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (
      (
        Rep.Some(id),
        Rep.Some(code),
        Rep.Some(bankCode),
        Rep.Some(name),
        Rep.Some(halfWidthKana),
        Rep.Some(fullWidthKana),
        Rep.Some(hiragana),
        Rep.Some(version),
        Rep.Some(createdAt),
        Rep.Some(updatedAt)
      )
    ).shaped.<>(
      { r =>
        import r._;
        _1.map(_ =>
          BankBranchMasterRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get))
        )
      },
      (_: Any) => throw new Exception("Inserting into ? projection not supported.")
    )

    /** Database column id SqlType(INT), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)

    /** Database column code SqlType(VARCHAR), Length(4,true) */
    val code: Rep[String] = column[String]("code", O.Length(4, varying = true))

    /** Database column bank_code SqlType(VARCHAR), Length(4,true) */
    val bankCode: Rep[String] = column[String]("bank_code", O.Length(4, varying = true))

    /** Database column name SqlType(TEXT) */
    val name: Rep[String] = column[String]("name")

    /** Database column half_width_kana SqlType(TEXT) */
    val halfWidthKana: Rep[String] = column[String]("half_width_kana")

    /** Database column full_width_Kana SqlType(TEXT) */
    val fullWidthKana: Rep[String] = column[String]("full_width_Kana")

    /** Database column hiragana SqlType(TEXT) */
    val hiragana: Rep[String] = column[String]("hiragana")

    /** Database column version SqlType(TIMESTAMP) */
    val version: Rep[OffsetDateTime] = column[OffsetDateTime]("version")

    /** Database column created_at SqlType(TIMESTAMP) */
    val createdAt: Rep[OffsetDateTime] = column[OffsetDateTime]("created_at")

    /** Database column updated_at SqlType(TIMESTAMP) */
    val updatedAt: Rep[OffsetDateTime] = column[OffsetDateTime]("updated_at")

    /** Foreign key referencing BankMaster (database name fk_bank_branch_master_bank) */
    lazy val bankMasterFk = foreignKey("fk_bank_branch_master_bank", bankCode, BankMaster)(
      r => r.code,
      onUpdate = ForeignKeyAction.Restrict,
      onDelete = ForeignKeyAction.Restrict
    )

    /** Index over (version) (database name idx_bank_branch_master_version) */
    val index1 = index("idx_bank_branch_master_version", version)

    /** Uniqueness Index over (bankCode,code) (database name uk_bank_branch_master) */
    val index2 = index("uk_bank_branch_master", (bankCode, code), unique = true)
  }

  /** Collection-like TableQuery object for table BankBranchMaster */
  lazy val BankBranchMaster = new TableQuery(tag => new BankBranchMaster(tag))
}
