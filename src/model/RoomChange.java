package model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * 宿舍调整申请实体类
 * 表示学生的宿舍调整请求及处理结果
 */
public class RoomChange {
    private String id;               // 申请ID，如 "C20241222143000"
    private String studentId;        // 申请学生学号
    private String oldBuilding;      // 原楼栋
    private String oldRoomNumber;    // 原宿舍号
    private String newBuilding;      // 新楼栋
    private String newRoomNumber;    // 新宿舍号
    private String reason;           // 申请原因
    private LocalDateTime applyTime; // 申请时间
    private LocalDateTime updateTime;// 更新时间
    private RoomChangeStatus status; // 审批状态
    private String approver;         // 审批人（管理员/宿管）
    private LocalDateTime approveTime;// 审批时间
    private String approveComment;   // 审批意见
    private boolean deleted;         // 逻辑删除标志

    // 宿舍调整状态枚举
    public enum RoomChangeStatus {
        PENDING("待审批", "pending"),
        APPROVED("已批准", "approved"),
        REJECTED("已拒绝", "rejected"),
        CANCELED("已取消", "canceled"),
        EXECUTED("已执行", "executed");

        private final String description;
        private final String code;

        RoomChangeStatus(String description, String code) {
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
    public RoomChange(String id, String studentId, String oldBuilding, String oldRoomNumber,
                     String newBuilding, String newRoomNumber, String reason) {
        this.id = id;
        this.studentId = studentId;
        this.oldBuilding = oldBuilding;
        this.oldRoomNumber = oldRoomNumber;
        this.newBuilding = newBuilding;
        this.newRoomNumber = newRoomNumber;
        this.reason = reason;
        this.applyTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.status = RoomChangeStatus.PENDING;
        this.deleted = false;
    }

    // Getter方法
    public String getId() {
        return id;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getOldBuilding() {
        return oldBuilding;
    }

    public String getOldRoomNumber() {
        return oldRoomNumber;
    }

    public String getNewBuilding() {
        return newBuilding;
    }

    public String getNewRoomNumber() {
        return newRoomNumber;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getApplyTime() {
        return applyTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public RoomChangeStatus getStatus() {
        return status;
    }

    public Optional<String> getApprover() {
        return Optional.ofNullable(approver);
    }

    public Optional<LocalDateTime> getApproveTime() {
        return Optional.ofNullable(approveTime);
    }

    public Optional<String> getApproveComment() {
        return Optional.ofNullable(approveComment);
    }

    public boolean isDeleted() {
        return deleted;
    }

    // Setter方法
    public void setNewBuilding(String newBuilding) {
        this.newBuilding = newBuilding;
        this.updateTime = LocalDateTime.now();
    }

    public void setNewRoomNumber(String newRoomNumber) {
        this.newRoomNumber = newRoomNumber;
        this.updateTime = LocalDateTime.now();
    }

    public void setReason(String reason) {
        this.reason = reason;
        this.updateTime = LocalDateTime.now();
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
        this.updateTime = LocalDateTime.now();
    }

    // 状态转换方法
    public void approve(String approver, String comment) {
        this.status = RoomChangeStatus.APPROVED;
        this.approver = approver;
        this.approveTime = LocalDateTime.now();
        this.approveComment = comment;
        this.updateTime = LocalDateTime.now();
    }

    public void reject(String approver, String comment) {
        this.status = RoomChangeStatus.REJECTED;
        this.approver = approver;
        this.approveTime = LocalDateTime.now();
        this.approveComment = comment;
        this.updateTime = LocalDateTime.now();
    }

    public void cancel() {
        this.status = RoomChangeStatus.CANCELED;
        this.updateTime = LocalDateTime.now();
    }

    public void execute() {
        this.status = RoomChangeStatus.EXECUTED;
        this.updateTime = LocalDateTime.now();
    }

    // 辅助方法
    public boolean isPending() {
        return this.status == RoomChangeStatus.PENDING;
    }

    public boolean isApproved() {
        return this.status == RoomChangeStatus.APPROVED;
    }

    public boolean isRejected() {
        return this.status == RoomChangeStatus.REJECTED;
    }

    public boolean isCanceled() {
        return this.status == RoomChangeStatus.CANCELED;
    }

    public boolean isExecuted() {
        return this.status == RoomChangeStatus.EXECUTED;
    }

    // 工厂方法
    public static String generateId() {
        String timestamp = LocalDateTime.now().toString().replaceAll("[-:T]", "").substring(0, 14);
        return "C" + timestamp;
    }

    public static RoomChange createNewApplication(String studentId, String oldBuilding, String oldRoomNumber,
                                                 String newBuilding, String newRoomNumber, String reason) {
        String id = generateId();
        return new RoomChange(id, studentId, oldBuilding, oldRoomNumber, newBuilding, newRoomNumber, reason);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoomChange roomChange = (RoomChange) o;
        return Objects.equals(id, roomChange.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "RoomChange{" +
                "id='" + id + '\'' +
                ", studentId='" + studentId + '\'' +
                ", oldBuilding='" + oldBuilding + '\'' +
                ", oldRoomNumber='" + oldRoomNumber + '\'' +
                ", newBuilding='" + newBuilding + '\'' +
                ", newRoomNumber='" + newRoomNumber + '\'' +
                ", applyTime=" + applyTime +
                ", status=" + status.getDescription() +
                '}';
    }
}