package com.bank.domains.entity

import com.bank.domains.entity.BankCodeJpApiResponse.OkResponse
import play.api.libs.json.{JsValue, Json, OWrites, Reads}

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

object BankCodeJp {

  case class BankBranch(
      code: String,
      bankCode: String,
      name: String,
      halfWidthKana: String,
      fullWidthKana: String,
      hiragana: String,
      version: OffsetDateTime
  )

  object BankBranch {
    def apply(response: BankBranchesResponse): Seq[BankBranch] =
      response.branches.map { branch =>
        BankBranch(
          code = branch.code,
          bankCode = response.bank.code,
          name = branch.name,
          halfWidthKana = branch.halfWidthKana,
          fullWidthKana = branch.fullWidthKana,
          hiragana = branch.hiragana,
          version = response.versionTime
        )
      }
  }
}

object BankCodeJpApiResponse {
  private val DateTimePattern                   = "yyyy-MM-dd'T'HH:mm:ssZ"
  lazy val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(DateTimePattern)

  trait OkResponse[T]
  case class ErrorResponse(url: String, status: Int, reason: JsValue)
}

case class BanksOkResponse(url: String, status: Int, response: BanksResponse) extends OkResponse[BanksResponse]
case class BankBranchesOkResponse(url: String, status: Int, response: BankBranchesResponse)
    extends OkResponse[BankBranchesResponse]

case class BanksResponse(
    size: Int,
    limit: Int,
    hasNext: Boolean,
    nextCursor: Option[String],
    hasPrev: Boolean,
    prevCursor: Option[String],
    version: String,
    banks: Seq[BankResponse]
) {
  def versionTime: OffsetDateTime =
    OffsetDateTime.parse(version, BankCodeJpApiResponse.dateTimeFormatter)
}

object BanksResponse {
  implicit val banksResponseWrites: OWrites[BanksResponse] = Json.writes[BanksResponse]
  implicit val banksResponseReads: Reads[BanksResponse]    = Json.reads[BanksResponse]
}

case class BankResponse(
    code: String,
    name: String,
    halfWidthKana: String,
    fullWidthKana: String,
    hiragana: String,
    businessTypeCode: String,
    businessType: String
)

object BankResponse {
  implicit val bankResponseWrites: OWrites[BankResponse] = Json.writes[BankResponse]
  implicit val bankResponseReads: Reads[BankResponse]    = Json.reads[BankResponse]
}

case class BankCodeJpApiException(url: String, status: Int, reason: JsValue) extends Exception {
  override def getMessage: String = Json.prettyPrint(reason)
}

object BankCodeJpApiException {
  import BankCodeJpApiResponse._
  def apply(response: ErrorResponse): BankCodeJpApiException =
    BankCodeJpApiException(url = response.url, status = response.status, reason = response.reason)
}

case class BankBranchesResponse(
    bank: BankResponse,
    branches: Seq[BankBranchResponse],
    size: Int,
    limit: Int,
    hasNext: Boolean,
    nextCursor: Option[String],
    hasPrev: Boolean,
    prevCursor: Option[String],
    version: String
) {
  def versionTime: OffsetDateTime =
    OffsetDateTime.parse(version, BankCodeJpApiResponse.dateTimeFormatter)
}

object BankBranchesResponse {
  implicit val bankBranchesResponseRead: Reads[BankBranchesResponse]    = Json.reads[BankBranchesResponse]
  implicit val bankBranchesResponseWrite: OWrites[BankBranchesResponse] = Json.writes[BankBranchesResponse]
}

case class BankBranchResponse(
    code: String,
    name: String,
    halfWidthKana: String,
    fullWidthKana: String,
    hiragana: String
)

object BankBranchResponse {
  implicit val bankBranchResponseRead: Reads[BankBranchResponse]     = Json.reads[BankBranchResponse]
  implicit val bankBranchResponseWrites: OWrites[BankBranchResponse] = Json.writes[BankBranchResponse]
}
