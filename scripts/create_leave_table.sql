-- Create table for Leave requests (请假申请)
-- 使用 leave_request 避免 SQL 保留字

CREATE TABLE IF NOT EXISTS `leave_request` (
  `id` VARCHAR(32) NOT NULL PRIMARY KEY,
  `student_id` VARCHAR(64) NOT NULL,
  `room_number` VARCHAR(64),
  `building` VARCHAR(64),
  `leave_type` VARCHAR(32),
  `start_date` DATE,
  `end_date` DATE,
  `reason` TEXT,
  `contact_person` VARCHAR(64),
  `contact_phone` VARCHAR(32),
  `status` VARCHAR(32) DEFAULT 'pending',
  `apply_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `remarks` TEXT,
  `deleted` TINYINT(1) DEFAULT 0,
  INDEX `idx_leave_student_id` (`student_id`),
  INDEX `idx_leave_apply_time` (`apply_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

