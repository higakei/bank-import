CREATE TABLE `bank_master` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(4) NOT NULL COMMENT '金融機関コード',
  `name` TEXT NOT NULL COMMENT '金融機関名',
  `half_width_kana` TEXT NOT NULL COMMENT '半角カタカナ',
  `full_width_Kana` TEXT NOT NULL COMMENT '全角カタカナ',
  `hiragana` TEXT NOT NULL COMMENT 'ひらがな',
  `business_type_code` VARCHAR(5) NOT NULL COMMENT '金融機関種別コード',
  `business_type` TEXT NOT NULL COMMENT '金融機関種別',
  `version` TIMESTAMP NOT NULL COMMENT '金融機関データベースのバージョン',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bank_master` (`code`),
  INDEX `idx_bank_master_version` (`version`)
) COMMENT = '金融機関';
