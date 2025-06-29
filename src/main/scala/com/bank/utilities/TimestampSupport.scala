package com.bank.utilities

import java.time.OffsetDateTime
import scala.math.pow

trait TimestampSupport {

  /** タイムスタンプの切り捨て（
    * @param timestamp タイムスタンプ
    * @param digits ナノ秒の桁数(9桁まで)
    * @return タイムスタンプ
    */
  private def floorTimestamp(timestamp: OffsetDateTime, digits: Int): OffsetDateTime = {
    // ナノ秒は9桁まで
    val exponent = 9 - digits match {
      case s if s < 0 => 0
      case s          => s
    }

    val base = pow(10, exponent)
    val nano = (timestamp.getNano.toDouble / base).toInt * base
    timestamp.withNano(nano.toInt)
  }

  def now(digits: Int = 0): OffsetDateTime =
    floorTimestamp(timestamp = OffsetDateTime.now(), digits = digits)
}

object TimestampHelper extends TimestampSupport
