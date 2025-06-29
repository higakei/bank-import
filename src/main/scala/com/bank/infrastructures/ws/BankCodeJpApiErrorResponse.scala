package com.bank.infrastructures.ws

import com.bank.infrastructures.ws.BankCodeJpApiErrorResponse.ValidationError
import play.api.libs.json.{Json, OWrites, Reads}

// 必須フィールドなし（エラー内容によって異なる）
case class BankCodeJpApiErrorResponse(
    httpStatusCode: Option[Int],
    code: Option[String],
    message: Option[String],
    validationErrors: Option[Seq[ValidationError]]
)

object BankCodeJpApiErrorResponse {
  implicit val bankCodeJpApiErrorResponseReads: Reads[BankCodeJpApiErrorResponse] =
    Json.reads[BankCodeJpApiErrorResponse]
  implicit val bankCodeJpApiErrorResponseWrites: OWrites[BankCodeJpApiErrorResponse] =
    Json.writes[BankCodeJpApiErrorResponse]

  case class ValidationError(message: String, paramName: String, invalidValue: Option[String])
  object ValidationError {
    implicit val validationErrorReads: Reads[ValidationError]    = Json.reads[ValidationError]
    implicit val validationErrorWrites: OWrites[ValidationError] = Json.writes[ValidationError]
  }
}
