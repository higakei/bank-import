CREATE TABLE `bank_branch_master` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(4) NOT NULL COMMENT '支店コード',
  `bank_code` VARCHAR(4) NOT NULL COMMENT '金融機関コード',
  `name` TEXT NOT NULL COMMENT '支店名',
  `half_width_kana` TEXT NOT NULL COMMENT '半角カタカナ',
  `full_width_Kana` TEXT NOT NULL COMMENT '全角カタカナ',
  `hiragana` TEXT NOT NULL COMMENT 'ひらがな',
  `version` TIMESTAMP NOT NULL COMMENT '金融機関データベースのバージョン',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bank_branch_master` (`bank_code`, `code`),
  INDEX `idx_bank_branch_master_version` (`version`),
  CONSTRAINT `fk_bank_branch_master_bank_new` FOREIGN KEY (`bank_code`) REFERENCES `bank_master` (`code`)
) COMMENT = '支店';
