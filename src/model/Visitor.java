package model;

import java.io.Serializable;
import java.util.Date;

/**
 * 访客实体类
 */
public class Visitor implements Serializable {
    private static final long serialVersionUID = 1L;

    private String visitorId;      // 访客ID
    private String visitorName;    // 访客姓名
    private Date visitTime;        // 来访时间
    private Date leaveTime;        // 离开时间
    private String dormitory;      // 访问宿舍
    private String visitedStudent; // 被访学生
    private String purpose;        // 来访事由
    private String idType;         // 证件类型
    private String idNumber;       // 证件号码
    private String phone;          // 联系电话
    private String remark;         // 备注
    private Date createTime;       // 登记时间

    // 构造函数
    public Visitor() {
        this.createTime = new Date();
    }

    public Visitor(String visitorId, String visitorName, Date visitTime, Date leaveTime,
                   String dormitory, String visitedStudent, String purpose, String idType,
                   String idNumber, String phone, String remark) {
        this.visitorId = visitorId;
        this.visitorName = visitorName;
        this.visitTime = visitTime;
        this.leaveTime = leaveTime;
        this.dormitory = dormitory;
        this.visitedStudent = visitedStudent;
        this.purpose = purpose;
        this.idType = idType;
        this.idNumber = idNumber;
        this.phone = phone;
        this.remark = remark;
        this.createTime = new Date();
    }

    // Getters and Setters
    public String getVisitorId() {
        return visitorId;
    }

    public void setVisitorId(String visitorId) {
        this.visitorId = visitorId;
    }

    public String getVisitorName() {
        return visitorName;
    }

    public void setVisitorName(String visitorName) {
        this.visitorName = visitorName;
    }

    public Date getVisitTime() {
        return visitTime;
    }

    public void setVisitTime(Date visitTime) {
        this.visitTime = visitTime;
    }

    public Date getLeaveTime() {
        return leaveTime;
    }

    public void setLeaveTime(Date leaveTime) {
        this.leaveTime = leaveTime;
    }

    public String getDormitory() {
        return dormitory;
    }

    public void setDormitory(String dormitory) {
        this.dormitory = dormitory;
    }

    public String getVisitedStudent() {
        return visitedStudent;
    }

    public void setVisitedStudent(String visitedStudent) {
        this.visitedStudent = visitedStudent;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 获取证件类型枚举
     */
    public IDType getIdTypeEnum() {
        try {
            return IDType.valueOf(idType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return IDType.OTHER;
        }
    }

    /**
     * 检查访客是否仍在楼内
     */
    public boolean isStillInBuilding() {
        if (leaveTime == null) {
            return true;
        }
        return new Date().before(leaveTime);
    }

    /**
     * 获取访问时长（分钟）
     */
    public long getVisitDuration() {
        if (visitTime == null || leaveTime == null) {
            return 0;
        }
        long diff = leaveTime.getTime() - visitTime.getTime();
        return diff / (60 * 1000); // 转换为分钟
    }

    @Override
    public String toString() {
        return "Visitor{" +
                "visitorId='" + visitorId + '\'' +
                ", visitorName='" + visitorName + '\'' +
                ", visitTime=" + visitTime +
                ", dormitory='" + dormitory + '\'' +
                ", visitedStudent='" + visitedStudent + '\'' +
                ", purpose='" + purpose + '\'' +
                '}';
    }

    /**
     * 验证访客信息是否完整
     */
    public boolean isValid() {
        return visitorName != null && !visitorName.trim().isEmpty() &&
                idType != null && !idType.trim().isEmpty() &&
                idNumber != null && !idNumber.trim().isEmpty() &&
                phone != null && !phone.trim().isEmpty() &&
                purpose != null && !purpose.trim().isEmpty();
    }
}

/**
 * 证件类型枚举
 */
enum IDType {
    ID_CARD("身份证"),
    PASSPORT("护照"),
    STUDENT_CARD("学生证"),
    OTHER("其他");

    private final String description;

    IDType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}