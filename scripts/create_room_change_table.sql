-- 表：room_change
-- 用于存储退宿/换宿申请记录

CREATE TABLE IF NOT EXISTS `room_change` (
  `id` VARCHAR(32) NOT NULL PRIMARY KEY,
  `student_id` VARCHAR(64) NOT NULL,
  `old_building` VARCHAR(64),
  `old_room_number` VARCHAR(64),
  `new_building` VARCHAR(64),
  `new_room_number` VARCHAR(64),
  `reason` TEXT,
  `status` VARCHAR(32) DEFAULT 'pending',
  `apply_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `approver` VARCHAR(64),
  `approve_time` TIMESTAMP NULL,
  `approve_comment` TEXT,
  `deleted` TINYINT(1) DEFAULT 0,
  INDEX `idx_rc_student_id` (`student_id`),
  INDEX `idx_rc_apply_time` (`apply_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

