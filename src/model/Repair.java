package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * 宿舍报修实体类
 * 表示宿舍维修请求的详细信息
 */
public class Repair {
    private String id;               // 报修ID，如 "R20241222143000"
    private String studentId;        // 报修学生学号
    private String roomNumber;       // 宿舍号
    private String building;         // 楼栋
    private LocalDateTime submitTime;// 提交时间
    private LocalDateTime updateTime;// 更新时间
    private RepairType repairType;   // 报修类型
    private String description;      // 报修描述
    private String imageUrls;        // 报修图片URL，逗号分隔
    private BigDecimal progress;     // 维修进度（0-100）
    private BigDecimal evaluation;   // 维修评价（0-5星）
    private BigDecimal fee;          // 维修费用
    private String repairman;        // 维修人员
    private String adminNo;          // 处理的管理员学号
    private RepairStatus status;     // 报修状态
    private String remarks;          // 备注
    private boolean deleted;         // 逻辑删除标志

    // 报修类型枚举
    public enum RepairType {
        WATER_ELEC("水电设施", "water_elec"),
        FURNITURE("家具", "furniture"),
        NETWORK("网络", "network"),
        HVAC("空调", "hvac"),
        OTHER("其他", "other");

        private final String description;
        private final String code;

        RepairType(String description, String code) {
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

    // 报修状态枚举
    public enum RepairStatus {
        PENDING("待处理", "pending"),
        PROCESSING("处理中", "processing"),
        FINISHED("已完成", "finished"),
        CANCELED("已取消", "canceled"),
        REJECTED("已拒绝", "rejected");

        private final String description;
        private final String code;

        RepairStatus(String description, String code) {
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
    public Repair(String id, String studentId, String roomNumber, String building,
                 RepairType repairType, String description, String imageUrls) {
        this.id = id;
        this.studentId = studentId;
        this.roomNumber = roomNumber;
        this.building = building;
        this.repairType = repairType;
        this.description = description;
        this.imageUrls = imageUrls;
        this.submitTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.progress = BigDecimal.ZERO;
        this.status = RepairStatus.PENDING;
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

    public LocalDateTime getSubmitTime() {
        return submitTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public RepairType getRepairType() {
        return repairType;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrls() {
        return imageUrls;
    }

    public BigDecimal getProgress() {
        return progress;
    }

    public Optional<BigDecimal> getEvaluation() {
        return Optional.ofNullable(evaluation);
    }

    public Optional<BigDecimal> getFee() {
        return Optional.ofNullable(fee);
    }

    public Optional<String> getRepairman() {
        return Optional.ofNullable(repairman);
    }

    public Optional<String> getAdminNo() {
        return Optional.ofNullable(adminNo);
    }

    public RepairStatus getStatus() {
        return status;
    }

    public Optional<String> getRemarks() {
        return Optional.ofNullable(remarks);
    }

    public boolean isDeleted() {
        return deleted;
    }

    // Setter方法
    public void setImageUrls(String imageUrls) {
        this.imageUrls = imageUrls;
        this.updateTime = LocalDateTime.now();
    }

    public void setEvaluation(BigDecimal evaluation) {
        this.evaluation = evaluation;
        this.updateTime = LocalDateTime.now();
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
        this.updateTime = LocalDateTime.now();
    }

    public void setRepairman(String repairman) {
        this.repairman = repairman;
        this.updateTime = LocalDateTime.now();
    }

    public void setAdminNo(String adminNo) {
        this.adminNo = adminNo;
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
    public void updateProgress(int progressPercent) {
        progressPercent = Math.max(0, Math.min(100, progressPercent));
        this.progress = BigDecimal.valueOf(progressPercent);
        this.updateTime = LocalDateTime.now();

        if (progressPercent == 100) {
            this.status = RepairStatus.FINISHED;
        } else if (progressPercent > 0 && progressPercent < 100) {
            this.status = RepairStatus.PROCESSING;
        }
    }

    public void cancel() {
        this.status = RepairStatus.CANCELED;
        this.updateTime = LocalDateTime.now();
    }

    public void reject() {
        this.status = RepairStatus.REJECTED;
        this.updateTime = LocalDateTime.now();
    }

    public void evaluate(int star) {
        star = Math.max(0, Math.min(5, star));
        this.evaluation = BigDecimal.valueOf(star);
        this.updateTime = LocalDateTime.now();
    }

    // 辅助方法
    public boolean isFinished() {
        return this.status == RepairStatus.FINISHED;
    }

    public boolean isProcessing() {
        return this.status == RepairStatus.PROCESSING;
    }

    public boolean isPending() {
        return this.status == RepairStatus.PENDING;
    }

    // 工厂方法
    public static String generateId() {
        String timestamp = LocalDateTime.now().toString().replaceAll("[-:T]", "").substring(0, 14);
        return "R" + timestamp;
    }

    public static Repair createNewRepair(String studentId, String roomNumber, String building,
                                        RepairType repairType, String description, String imageUrls) {
        String id = generateId();
        return new Repair(id, studentId, roomNumber, building, repairType, description, imageUrls);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Repair repair = (Repair) o;
        return Objects.equals(id, repair.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Repair{" +
                "id='" + id + '\'' +
                ", studentId='" + studentId + '\'' +
                ", roomNumber='" + roomNumber + '\'' +
                ", building='" + building + '\'' +
                ", submitTime=" + submitTime +
                ", repairType=" + repairType.getDescription() +
                ", status=" + status.getDescription() +
                ", progress=" + progress + "%" +
                '}';
    }
}