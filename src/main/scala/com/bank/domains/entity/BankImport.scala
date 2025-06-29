package com.bank.domains.entity

import com.bank.domains.entity.BankCodeJp.BankBranch

import java.time.OffsetDateTime

case class SyncBankBranchResult(
    bankCode: String,
    hasNoBranch: Boolean,
    updates: Int,
    inserts: Seq[BankBranch],
    deletes: Seq[BankBranchMasterEntity],
    deleted: Boolean
) {
  def print: String =
    if (hasNoBranch) "支店なし" else s"更新：$updates, 登録：${inserts.size}, 削除：${deletes.size}"
}

case class SyncBankBranchResults(
    target: Seq[String],
    completed: Seq[SyncBankBranchResult] = Nil,
    hasNoBranch: Seq[String] = Nil,
    banksNotFound: Seq[String] = Nil,
    limitExceeded: Boolean = false,
    failure: Option[String] = None
) { self =>
  lazy val inserts: Seq[BankBranch]             = completed.flatMap(_.inserts)
  lazy val deletes: Seq[BankBranchMasterEntity] = completed.flatMap(_.deletes)

  def addCompleted(result: SyncBankBranchResult): SyncBankBranchResults =
    if (result.hasNoBranch) {
      self.copy(hasNoBranch = hasNoBranch :+ result.bankCode)
    } else {
      self.copy(completed = completed :+ result)
    }

  def notSynced: Int =
    target.size - completed.size - banksNotFound.size
}

case class BankMasterEntity(
    id: Int,
    bank: BankResponse,
    version: OffsetDateTime,
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime
) {
  def code: String = bank.code
  def name: String = bank.name
}

case class BankBranchMasterEntity(
    id: Int,
    branch: BankBranch,
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime
) {
  def code: String            = branch.code
  def version: OffsetDateTime = branch.version
  def name: String            = branch.name
  def bankCode: String        = branch.bankCode
}
