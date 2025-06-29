package com.bank.infrastructures.db

import slick.jdbc.MySQLProfile

import java.sql.{PreparedStatement, ResultSet, Timestamp}
import java.time.{OffsetDateTime, ZoneId}

trait CustomizedMySQLProfile extends MySQLProfile {

  override val columnTypes: JdbcTypes = new JdbcTypes {
    override val offsetDateTimeType: OffsetDateTimeJdbcType = new OffsetDateTimeJdbcType {
      override def sqlType: Int = {
        java.sql.Types.TIMESTAMP
      }

      override def setValue(v: OffsetDateTime, p: PreparedStatement, idx: Int): Unit = {
        p.setTimestamp(idx, if (v == null) null else Timestamp.from(v.toInstant))
      }

      override def getValue(r: ResultSet, idx: Int): OffsetDateTime = {
        r.getTimestamp(idx) match {
          case null => null
          case ts   => OffsetDateTime.ofInstant(ts.toInstant, ZoneId.systemDefault())
        }
      }

      override def updateValue(v: OffsetDateTime, r: ResultSet, idx: Int): Unit = {
        r.updateTimestamp(idx, if (v == null) null else Timestamp.from(v.toInstant))
      }
    }
  }

}

object CustomizedMySQLProfile extends CustomizedMySQLProfile
