package model;

import java.io.Serializable;
import java.util.Date;

/**
 * 考勤实体类
 */
public class Attendance implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;               // 序号
    private String studentId;     // 学号
    private String studentName;   // 姓名
    private String dormitory;     // 宿舍号
    private Date attendanceDate;  // 考勤日期
    private Date checkInTime;     // 归寝时间
    private AttendanceStatus status; // 考勤状态
    private String remark;        // 备注
    private Date recordTime;      // 登记时间

    // 构造函数
    public Attendance() {
        this.recordTime = new Date();
    }

    public Attendance(String studentId, String studentName, String dormitory,
                      Date attendanceDate, Date checkInTime, AttendanceStatus status,
                      String remark) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.dormitory = dormitory;
        this.attendanceDate = attendanceDate;
        this.checkInTime = checkInTime;
        this.status = status;
        this.remark = remark;
        this.recordTime = new Date();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getDormitory() {
        return dormitory;
    }

    public void setDormitory(String dormitory) {
        this.dormitory = dormitory;
    }

    public Date getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(Date attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public Date getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(Date checkInTime) {
        this.checkInTime = checkInTime;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(Date recordTime) {
        this.recordTime = recordTime;
    }

    /**
     * 判断是否为异常考勤
     */
    public boolean isAbnormal() {
        return status == AttendanceStatus.LATE ||
                status == AttendanceStatus.ABSENT;
    }

    /**
     * 判断是否为今日考勤
     */
    public boolean isToday() {
        if (attendanceDate == null) return false;

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(new Date());
        String attendanceDay = sdf.format(attendanceDate);

        return today.equals(attendanceDay);
    }

    /**
     * 获取归寝时间的小时数
     */
    public int getCheckInHour() {
        if (checkInTime == null) return 0;

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(checkInTime);
        return cal.get(java.util.Calendar.HOUR_OF_DAY);
    }

    /**
     * 获取宿舍楼信息
     */
    public String getBuilding() {
        if (dormitory == null || dormitory.isEmpty()) {
            return "";
        }
        // 提取宿舍楼信息，如"A101"中的"A"
        return dormitory.substring(0, 1);
    }

    @Override
    public String toString() {
        return "Attendance{" +
                "id=" + id +
                ", studentId='" + studentId + '\'' +
                ", studentName='" + studentName + '\'' +
                ", dormitory='" + dormitory + '\'' +
                ", status=" + status +
                ", date=" + attendanceDate +
                '}';
    }
}

/**
 * 颜色枚举
 */
enum Color {
    GREEN, YELLOW, RED, BLUE;
}