package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;

/**
 * 考勤记录实体类
 * 表示学生的宿舍进出考勤信息
 */
public class Attendance {
    private String id;               // 考勤ID，如 "A20241222143000"
    private String studentId;        // 学生学号
    private String roomNumber;       // 宿舍号
    private String building;         // 楼栋
    private LocalDate attendanceDate;// 考勤日期
    private LocalTime attendanceTime;// 考勤时间
    private AttendanceDirection direction;// 考勤方向（进/出）
    private AttendanceStatus status; // 考勤状态
    private LocalDateTime createTime;// 创建时间
    private LocalDateTime updateTime;// 更新时间
    private String deviceId;         // 考勤设备ID
    private String remarks;          // 备注
    private boolean deleted;         // 逻辑删除标志

    // 考勤方向枚举
    public enum AttendanceDirection {
        IN("进入", "in"),
        OUT("离开", "out");

        private final String description;
        private final String code;

        AttendanceDirection(String description, String code) {
            this.description = description;
            this.code = code;
        }

        public String getDescription() {
            return description;
        }

        public String getCode() {
            return code;
        }
    }

    // 考勤状态枚举
    public enum AttendanceStatus {
        NORMAL("正常", "normal"),
        LATE("晚归", "late"),
        ABSENT("未归", "absent"),
        LEAVE("请假", "leave"),
        OVERDUE("逾期", "overdue");

        private final String description;
        private final String code;

        AttendanceStatus(String description, String code) {
            this.description = description;
            this.code = code;
        }

        public String getDescription() {
            return description;
        }

        public String getCode() {
            return code;
        }
    }

    // 构造函数
    public Attendance(String id, String studentId, String roomNumber, String building,
                     LocalDate attendanceDate, LocalTime attendanceTime, AttendanceDirection direction,
                     AttendanceStatus status) {
        this.id = id;
        this.studentId = studentId;
        this.roomNumber = roomNumber;
        this.building = building;
        this.attendanceDate = attendanceDate;
        this.attendanceTime = attendanceTime;
        this.direction = direction;
        this.status = status;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.deleted = false;
    }

    // 便捷构造：只传学号、宿舍号、楼栋，默认进入（IN）和正常状态
    public Attendance(String studentId, String roomNumber, String building) {
        this.id = generateId();
        this.studentId = studentId;
        this.roomNumber = roomNumber;
        this.building = building;
        this.attendanceDate = LocalDate.now();
        this.attendanceTime = LocalTime.now();
        this.direction = AttendanceDirection.IN;
        this.status = AttendanceStatus.NORMAL;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.deleted = false;
    }

    // Getter方法
    public String getId() {
        return id;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getBuilding() {
        return building;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public LocalTime getAttendanceTime() {
        return attendanceTime;
    }

    public AttendanceDirection getDirection() {
        return direction;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public Optional<String> getRemarks() {
        return Optional.ofNullable(remarks);
    }

    public boolean isDeleted() {
        return deleted;
    }

    // Setter方法
    public void setAttendanceTime(LocalTime attendanceTime) {
        this.attendanceTime = attendanceTime;
        this.updateTime = LocalDateTime.now();
    }

    // 新增：设置考勤日期
    public void setAttendanceDate(LocalDate date) {
        this.attendanceDate = date;
        this.updateTime = LocalDateTime.now();
    }


    public void setStatus(AttendanceStatus status) {
        this.status = status;
        this.updateTime = LocalDateTime.now();
    }



    public void setRemarks(String remarks) {
        this.remarks = remarks;
        this.updateTime = LocalDateTime.now();
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
        this.updateTime = LocalDateTime.now();
    }

    // 状态转换方法
    public void markAsLate() {
        this.status = AttendanceStatus.LATE;
        this.updateTime = LocalDateTime.now();
    }

    // 辅助方法

    public boolean isEntry() {
        return this.direction == AttendanceDirection.IN;
    }

    // 检查是否迟到
    public boolean checkLate(LocalTime curfewTime) {
        if (this.direction == AttendanceDirection.IN && this.attendanceTime != null) {
            if (this.attendanceTime.isAfter(curfewTime)) {
                markAsLate();
                return true;
            }
        }
        return false;
    }

    // 工厂方法
    public static String generateId() {
        String timestamp = LocalDateTime.now().toString().replaceAll("[-:T]", "").substring(0, 14);
        return "A" + timestamp;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attendance attendance = (Attendance) o;
        return Objects.equals(id, attendance.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Attendance{" +
                "id='" + id + '\'' +
                ", studentId='" + studentId + '\'' +
                ", roomNumber='" + roomNumber + '\'' +
                ", building='" + building + '\'' +
                ", attendanceDate=" + attendanceDate +
                ", attendanceTime=" + attendanceTime +
                ", direction=" + direction.getDescription() +
                ", status=" + status.getDescription() +
                '}';
    }
}

