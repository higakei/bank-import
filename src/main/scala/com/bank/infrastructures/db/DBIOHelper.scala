package com.bank.infrastructures.db

import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

object DBIOHelper {
  def If[T](cond: Boolean)(action: => DBIO[T])(implicit ec: ExecutionContext): DBIO[Option[T]] =
    if (cond) action.map(Some(_)) else DBIO.successful(None)
}
