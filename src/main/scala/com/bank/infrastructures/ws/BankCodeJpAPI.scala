package com.bank.infrastructures.ws

import com.bank.domains.entity.BankCodeJpApiResponse.ErrorResponse
import com.bank.domains.entity.{BankBranchesOkResponse, BankBranchesResponse, BanksOkResponse, BanksResponse}
import com.google.inject.Inject
import play.api.Configuration
import play.api.http.{MimeTypes, Status}
import play.api.libs.json.{JsValue, Reads}
import play.api.libs.ws.{WSClient, WSResponse}

import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class BankCodeJpAPI @Inject() (
    ws: WSClient,
    apiClient: BankCodeJpApiClient,
    configuration: Configuration
)(implicit ec: ExecutionContext)
    extends WsSupport {

  lazy val maxLimit: Int          = configuration.get[Int]("bankCodeJp.maxLimit")
  private lazy val apiKey: String = configuration.get[String]("bankCodeJp.apiKey")
  private lazy val perSecond: Int = configuration.get[Int]("bankCodeJp.max.request.per.second")
  lazy val requestInterval: Long  = Math.ceil(1000d / perSecond).toLong

  def getBanks(limit: Int, cursor: Option[String]): Future[Either[ErrorResponse, BanksOkResponse]] = {
    val endpoint = configuration.get[String]("bankCodeJp.endpoints.banks")
    val params   = getQueryParams(apiKey = apiKey, limit = limit, cursor = cursor)
    val url      = addQuery(url = endpoint, params = params)

    for {
      wsResponse <- apiClient.getBanks(ws = ws, url = url)
      response   <- Future.fromTry(parseJson[BanksResponse](response = wsResponse, url = url))
    } yield response match {
      case Right(value) => Right(BanksOkResponse(url = url, status = wsResponse.status, response = value))
      case Left(_)      => Left(ErrorResponse(url = url, status = wsResponse.status, reason = wsResponse.json))
    }
  }

  def getBankBranches(
      bankCode: String,
      limit: Int,
      cursor: Option[String]
  ): Future[Either[ErrorResponse, BankBranchesOkResponse]] = {
    val endpoint = configuration.get[String]("bankCodeJp.endpoints.branches").replaceFirst("\\{bankCode}", bankCode)
    val params   = getQueryParams(apiKey = apiKey, limit = limit, cursor = cursor)
    val url      = addQuery(url = endpoint, params = params)

    for {
      wsResponse <- apiClient.getBankBranches(ws = ws, url = url)
      response   <- Future.fromTry(parseJson[BankBranchesResponse](response = wsResponse, url = url))
    } yield response match {
      case Right(value) => Right(BankBranchesOkResponse(url = url, status = wsResponse.status, response = value))
      case Left(_)      => Left(ErrorResponse(url = url, status = wsResponse.status, reason = wsResponse.json))
    }
  }

  private def getQueryParams(apiKey: String, limit: Int, cursor: Option[String]): Map[String, Option[String]] =
    Map("apiKey" -> Some(apiKey), "limit" -> Some(limit.toString), "cursor" -> cursor)

  private def parseJson[T](response: WSResponse, url: String)(implicit
      rds: Reads[T]
  ): Try[Either[BankCodeJpApiErrorResponse, T]] = {
    val json = Try(response.body[JsValue]).toOption
    response.status match {
      case OK =>
        json.flatMap(_.asOpt[T]) match {
          case Some(value) => Success(Right(value))
          case None        => Failure(ApiParseException(url = url, contentType = response.contentType, body = response.body))
        }
      case status =>
        json.flatMap(_.asOpt[BankCodeJpApiErrorResponse]) match {
          case Some(value) => Success(Left(value))
          case None        => Failure(ApiException(url = url, status = status, body = response.body))
        }
    }
  }
}

trait WsSupport extends Status with MimeTypes {

  private def getQuery(params: Map[String, Option[String]]): String = {
    val queries = params.flatMap {
      case (key, Some(value)) => Some(s"$key=$value")
      case (_, None)          => None
    }
    queries.mkString("&")
  }

  def addQuery(url: String, params: Map[String, Option[String]]): String =
    getQuery(params = params) match {
      case query if query.isEmpty => url
      case query                  => s"$url?$query"
    }
}

case class ApiException(url: String, status: Int, body: String) extends Exception {
  override def getMessage: String = s"url=$url, status=$status, body=$body"
}

case class ApiParseException(url: String, contentType: String, body: String) extends Exception {
  override def getMessage: String = s"url=$url, $contentType=$contentType, body=$body"
}

trait BankCodeJpApiClient {
  def getBanks(ws: WSClient, url: String): Future[WSResponse]        = ws.url(url = url).get()
  def getBankBranches(ws: WSClient, url: String): Future[WSResponse] = ws.url(url = url).get()
}

object BankCodeJpApiClient extends BankCodeJpApiClient
