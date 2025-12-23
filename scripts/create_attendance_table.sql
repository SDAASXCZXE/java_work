-- 创建 attendance 表的示例 SQL（在测试或备份环境先执行）
CREATE TABLE IF NOT EXISTS attendance (
  id VARCHAR(64) PRIMARY KEY,
  student_id VARCHAR(32) NOT NULL,
  room_number VARCHAR(64),
  building VARCHAR(16),
  attendance_date DATE,
  attendance_time TIME,
  direction VARCHAR(16),
  status VARCHAR(16),
  create_time DATETIME,
  update_time DATETIME,
  device_id VARCHAR(64),
  remarks VARCHAR(255),
  deleted TINYINT(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 索引
CREATE INDEX idx_attendance_date ON attendance(attendance_date);
CREATE INDEX idx_attendance_student ON attendance(student_id);
CREATE INDEX idx_attendance_room ON attendance(room_number);

