-- holiday 表建表脚本
CREATE TABLE IF NOT EXISTS `holiday` (
  `id` VARCHAR(64) NOT NULL,
  `student_id` VARCHAR(64) DEFAULT NULL,
  `room_number` VARCHAR(64) DEFAULT NULL,
  `building` VARCHAR(64) DEFAULT NULL,
  `start_date` DATE DEFAULT NULL,
  `end_date` DATE DEFAULT NULL,
  `actual_back_date` DATE DEFAULT NULL,
  `destination` VARCHAR(256) DEFAULT NULL,
  `contact_person` VARCHAR(128) DEFAULT NULL,
  `contact_phone` VARCHAR(64) DEFAULT NULL,
  `apply_time` DATETIME DEFAULT NULL,
  `update_time` DATETIME DEFAULT NULL,
  `status` VARCHAR(32) DEFAULT NULL,
  `remarks` TEXT,
  `deleted` TINYINT(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  INDEX `idx_student` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

