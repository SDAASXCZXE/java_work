package model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Visitor {
    private String id;
    private String name;
    private String idType;
    private String idNumber;
    private String phone;
    private String visitRoom;
    private String targetStudent;
    private String reason;
    private LocalDateTime arriveTime;
    private LocalDateTime leaveTime;
    private LocalDateTime createTime;
    private String remarks;
    private boolean deleted; // 逻辑删除标志

    public static String generateId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    // Getter & Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIdType() { return idType; }
    public void setIdType(String idType) { this.idType = idType; }
    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getVisitRoom() { return visitRoom; }
    public void setVisitRoom(String visitRoom) { this.visitRoom = visitRoom; }
    public String getTargetStudent() { return targetStudent; }
    public void setTargetStudent(String targetStudent) { this.targetStudent = targetStudent; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getArriveTime() { return arriveTime; }
    public void setArriveTime(LocalDateTime arriveTime) { this.arriveTime = arriveTime; }
    public Optional<LocalDateTime> getLeaveTime() { return Optional.ofNullable(leaveTime); }
    public void setLeaveTime(LocalDateTime leaveTime) { this.leaveTime = leaveTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}