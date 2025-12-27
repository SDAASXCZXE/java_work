package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * 假期登记实体类
 * 表示学生的假期离校和返校登记信息
 */
public class Holiday {
    private String id;               // 登记ID，如 "H20241222143000"
    private String studentId;        // 登记学生学号
    private String roomNumber;       // 宿舍号
    private String building;         // 楼栋
    private HolidayType holidayType; // 假期类型（离校/返校）
    private LocalDate leaveDate;     // 离校日期
    private LocalDate plannedBackDate;// 计划返校日期
    private LocalDate actualBackDate;// 实际返校日期
    private String destination;      // 目的地
    private String contactPerson;    // 联系人
    private String contactPhone;     // 联系电话
    private LocalDateTime registerTime;// 登记时间
    private LocalDateTime updateTime;// 更新时间
    private HolidayStatus status;    // 状态
    private String remarks;          // 备注
    private boolean deleted;         // 逻辑删除标志

    // 假期类型枚举
    public enum HolidayType {
        LEAVE("离校", "leave"),
        BACK("返校", "back");

        private final String description;
        private final String code;

        HolidayType(String description, String code) {
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

    // 假期状态枚举
    public enum HolidayStatus {
        PENDING("待审核", "pending"),
        APPROVED("已批准", "approved"),
        REJECTED("已拒绝", "rejected"),
        COMPLETED("已完成", "completed"),
        OVERDUE("已逾期", "overdue");

        private final String description;
        private final String code;

        HolidayStatus(String description, String code) {
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
    public Holiday(String id, String studentId, String roomNumber, String building,
                  HolidayType holidayType, LocalDate leaveDate, LocalDate plannedBackDate) {
        this.id = id;
        this.studentId = studentId;
        this.roomNumber = roomNumber;
        this.building = building;
        this.holidayType = holidayType;
        this.leaveDate = leaveDate;
        this.plannedBackDate = plannedBackDate;
        this.registerTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.status = HolidayStatus.PENDING;
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

    public HolidayType getHolidayType() {
        return holidayType;
    }

    public LocalDate getLeaveDate() {
        return leaveDate;
    }

    public LocalDate getPlannedBackDate() {
        return plannedBackDate;
    }

    public Optional<LocalDate> getActualBackDate() {
        return Optional.ofNullable(actualBackDate);
    }

    public Optional<String> getDestination() {
        return Optional.ofNullable(destination);
    }

    public Optional<String> getContactPerson() {
        return Optional.ofNullable(contactPerson);
    }

    public Optional<String> getContactPhone() {
        return Optional.ofNullable(contactPhone);
    }

    public LocalDateTime getRegisterTime() {
        return registerTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public HolidayStatus getStatus() {
        return status;
    }

    public Optional<String> getRemarks() {
        return Optional.ofNullable(remarks);
    }

    public boolean isDeleted() {
        return deleted;
    }

    // Setter方法
    public void setActualBackDate(LocalDate actualBackDate) {
        this.actualBackDate = actualBackDate;
        this.updateTime = LocalDateTime.now();
        if (actualBackDate != null) {
            this.status = HolidayStatus.COMPLETED;
        }
    }

    public void setDestination(String destination) {
        this.destination = destination;
        this.updateTime = LocalDateTime.now();
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
        this.updateTime = LocalDateTime.now();
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
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
    public void approve() {
        this.status = HolidayStatus.APPROVED;
        this.updateTime = LocalDateTime.now();
    }

    public void reject() {
        this.status = HolidayStatus.REJECTED;
        this.updateTime = LocalDateTime.now();
    }

    public void markAsOverdue() {
        this.status = HolidayStatus.OVERDUE;
        this.updateTime = LocalDateTime.now();
    }

    public void complete(LocalDate actualBackDate) {
        this.actualBackDate = actualBackDate;
        this.status = HolidayStatus.COMPLETED;
        this.updateTime = LocalDateTime.now();
    }

    // 辅助方法
    public boolean isPending() {
        return this.status == HolidayStatus.PENDING;
    }


    public static String generateId() {
        String timestamp = LocalDateTime.now().toString().replaceAll("[-:T]", "").substring(0, 14);
        return "H" + timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Holiday holiday = (Holiday) o;
        return Objects.equals(id, holiday.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Holiday{" +
                "id='" + id + '\'' +
                ", studentId='" + studentId + '\'' +
                ", roomNumber='" + roomNumber + '\'' +
                ", building='" + building + '\'' +
                ", holidayType=" + holidayType.getDescription() +
                ", leaveDate=" + leaveDate +
                ", plannedBackDate=" + plannedBackDate +
                ", status=" + status.getDescription() +
                '}';
    }
}