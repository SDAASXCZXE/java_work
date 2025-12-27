package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * 请假申请实体类（与 Holiday 区分，主要用于病假/事假等）
 * 类型/状态均以字符串形式存储，便于未来与数据库兼容
 */
public class LeaveRequest {
    private String id;              // L + 时间戳
    private String studentId;       // 学号
    private String roomNumber;      // 宿舍号
    private String building;        // 楼栋
    private String leaveType;       // 请假类型，例如 "病假"/"事假"
    private LocalDate startDate;    // 请假开始日期
    private LocalDate endDate;      // 请假结束日期
    private String reason;          // 事由
    private String contactPerson;   // 联系人
    private String contactPhone;    // 联系电话
    private LocalDateTime applyTime; // 申请时间
    private LocalDateTime updateTime; // 更新时间
    private String status;          // 状态，例如 "pending"/"approved"/"rejected"/"completed"
    private String remarks;         // 备注
    private boolean deleted;        // 逻辑删除标志，默认 false

    public LeaveRequest(String id, String studentId, String roomNumber, String building,
                        String leaveType, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.studentId = studentId;
        this.roomNumber = roomNumber;
        this.building = building;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.applyTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.status = "pending";
        this.deleted = false;
    }

    public static String generateId() {
        String timestamp = LocalDateTime.now().toString().replaceAll("[-:T]", "").substring(0, 14);
        return "L" + timestamp;
    }

    // Getters
    public String getId() { return id; }
    public String getStudentId() { return studentId; }
    public String getRoomNumber() { return roomNumber; }
    public String getBuilding() { return building; }
    public String getLeaveType() { return leaveType; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public Optional<String> getReason() { return Optional.ofNullable(reason); }
    public Optional<String> getContactPerson() { return Optional.ofNullable(contactPerson); }
    public Optional<String> getContactPhone() { return Optional.ofNullable(contactPhone); }
    public LocalDateTime getApplyTime() { return applyTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public Optional<String> getRemarks() { return Optional.ofNullable(remarks); }
    public String getStatus() { return status; }
    public boolean isDeleted() { return deleted; }

    // Setters
    public void setReason(String reason) { this.reason = reason; this.updateTime = LocalDateTime.now(); }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; this.updateTime = LocalDateTime.now(); }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; this.updateTime = LocalDateTime.now(); }
    public void setRemarks(String remarks) { this.remarks = remarks; this.updateTime = LocalDateTime.now(); }
    public void setStatus(String status) { this.status = status; this.updateTime = LocalDateTime.now(); }
    public void setDeleted(boolean deleted) { this.deleted = deleted; this.updateTime = LocalDateTime.now(); }
    public void setApplyTime(LocalDateTime applyTime) { this.applyTime = applyTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LeaveRequest that = (LeaveRequest) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "LeaveRequest{" +
                "id='" + id + '\'' +
                ", studentId='" + studentId + '\'' +
                ", leaveType='" + leaveType + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status='" + status + '\'' +
                '}';
    }
}

