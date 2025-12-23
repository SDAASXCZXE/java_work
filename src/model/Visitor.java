package model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * 访客实体类
 */
public class Visitor {
    private String id;            // 访客ID
    private String name;          // 访客姓名
    private String idType;        // 证件类型（身份证/护照/学生证/...）
    private String idNumber;      // 证件号码
    private String phone;         // 联系电话
    private String visitRoom;     // 被访问宿舍（宿舍号，如 A101）
    private String targetStudent; // 被访学生
    private String reason;        // 来访事由
    private LocalDateTime arriveTime; // 来访时间
    private LocalDateTime leaveTime;  // 离开时间（可为空）
    private LocalDateTime createTime; // 记录创建时间
    private LocalDateTime updateTime; // 记录更新时间
    private String remarks;           // 备注
    private boolean deleted;          // 逻辑删除标志

    public Visitor() {
    }

    public Visitor(String id, String name, String idType, String idNumber, String phone,
                   String visitRoom, String targetStudent, String reason,
                   LocalDateTime arriveTime, LocalDateTime leaveTime) {
        this.id = id;
        this.name = name;
        this.idType = idType;
        this.idNumber = idNumber;
        this.phone = phone;
        this.visitRoom = visitRoom;
        this.targetStudent = targetStudent;
        this.reason = reason;
        this.arriveTime = arriveTime;
        this.leaveTime = leaveTime;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.deleted = false;
    }

    // 工厂方法，基于时间戳生成唯一 ID
    public static String generateId() {
        String ts = java.time.LocalDateTime.now().toString().replaceAll("[-:T\\.]", "");
        if (ts.length() > 14) ts = ts.substring(0, 14);
        return "V" + ts;
    }

    // --- Getter / Setter ---
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getVisitRoom() {
        return visitRoom;
    }

    public void setVisitRoom(String visitRoom) {
        this.visitRoom = visitRoom;
    }

    public String getTargetStudent() {
        return targetStudent;
    }

    public void setTargetStudent(String targetStudent) {
        this.targetStudent = targetStudent;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getArriveTime() {
        return arriveTime;
    }

    public void setArriveTime(LocalDateTime arriveTime) {
        this.arriveTime = arriveTime;
    }

    public Optional<LocalDateTime> getLeaveTime() {
        return Optional.ofNullable(leaveTime);
    }

    public void setLeaveTime(LocalDateTime leaveTime) {
        this.leaveTime = leaveTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Visitor visitor = (Visitor) o;
        return Objects.equals(id, visitor.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Visitor{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", idType='" + idType + '\'' +
                ", idNumber='" + idNumber + '\'' +
                ", phone='" + phone + '\'' +
                ", visitRoom='" + visitRoom + '\'' +
                ", targetStudent='" + targetStudent + '\'' +
                ", arriveTime=" + arriveTime +
                '}';
    }
}

