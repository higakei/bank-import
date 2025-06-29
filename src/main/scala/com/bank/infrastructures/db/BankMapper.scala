package com.bank.infrastructures.db

import com.bank.domains.entity.BankCodeJp.BankBranch
import com.bank.domains.entity.{BankBranchMasterEntity, BankMasterEntity, BankResponse}
import com.bank.infrastructures.db.dto.Tables.{BankBranchMasterRow, BankMasterRow}

import java.time.OffsetDateTime

trait BankMapper {

  def toInsertRow(response: BankResponse, version: OffsetDateTime): BankMasterRow =
    BankMasterRow(
      id = 0,
      code = response.code,
      name = response.name,
      halfWidthKana = response.halfWidthKana,
      fullWidthKana = response.fullWidthKana,
      hiragana = response.hiragana,
      businessTypeCode = response.businessTypeCode,
      businessType = response.businessType,
      version = version
    )

  def toInsertRow(bankCodeJp: BankBranch): BankBranchMasterRow =
    BankBranchMasterRow(
      id = 0,
      code = bankCodeJp.code,
      bankCode = bankCodeJp.bankCode,
      name = bankCodeJp.name,
      halfWidthKana = bankCodeJp.halfWidthKana,
      fullWidthKana = bankCodeJp.fullWidthKana,
      hiragana = bankCodeJp.hiragana,
      version = bankCodeJp.version
    )

  def toEntity(row: BankMasterRow): BankMasterEntity =
    BankMasterEntity(
      id = row.id,
      bank = toResponse(row = row),
      version = row.version,
      createdAt = row.createdAt,
      updatedAt = row.updatedAt
    )

  private def toResponse(row: BankMasterRow): BankResponse =
    BankResponse(
      code = row.code,
      name = row.name,
      halfWidthKana = row.halfWidthKana,
      fullWidthKana = row.fullWidthKana,
      hiragana = row.hiragana,
      businessTypeCode = row.businessTypeCode,
      businessType = row.businessType
    )

  def toRow(entity: BankMasterEntity): BankMasterRow =
    BankMasterRow(
      id = entity.id,
      code = entity.code,
      name = entity.bank.name,
      halfWidthKana = entity.bank.halfWidthKana,
      fullWidthKana = entity.bank.fullWidthKana,
      hiragana = entity.bank.hiragana,
      businessTypeCode = entity.bank.businessTypeCode,
      businessType = entity.bank.businessType,
      version = entity.version,
      createdAt = entity.createdAt,
      updatedAt = entity.updatedAt
    )

  def toEntity(row: BankBranchMasterRow): BankBranchMasterEntity =
    BankBranchMasterEntity(
      id = row.id,
      branch = toBankBranch(row = row),
      createdAt = row.createdAt,
      updatedAt = row.updatedAt
    )

  private def toBankBranch(row: BankBranchMasterRow): BankBranch =
    BankBranch(
      code = row.code,
      bankCode = row.bankCode,
      name = row.name,
      halfWidthKana = row.halfWidthKana,
      fullWidthKana = row.fullWidthKana,
      hiragana = row.hiragana,
      version = row.version
    )

  def toRow(entity: BankBranchMasterEntity): BankBranchMasterRow =
    BankBranchMasterRow(
      id = entity.id,
      code = entity.branch.code,
      bankCode = entity.branch.bankCode,
      name = entity.branch.name,
      halfWidthKana = entity.branch.halfWidthKana,
      fullWidthKana = entity.branch.fullWidthKana,
      hiragana = entity.branch.hiragana,
      version = entity.branch.version,
      createdAt = entity.createdAt,
      updatedAt = entity.updatedAt
    )
}
