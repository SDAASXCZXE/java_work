
package model;

import java.util.Date;

/**
 * 学生实体类
 */
public class Student {
    private int id; // 学生ID
    private String name; // 学生姓名
    private String gender; // 学生性别
    private int roomId; // 宿舍ID
    private String studentId; // 学号
    private String college; // 学院
    private String major; // 专业
    private String grade; // 年级
    private String clazz; // 班级
    private String bedNumber; // 床位号
    private Date checkInDate; // 入住日期
    private String phoneNumber; // 电话号码
    private String status; // 在校状态

    public Student() {}

    public Student(int id, String name, String gender, int roomId, String studentId, String college, String major, String grade, String clazz, String bedNumber, Date checkInDate, String phoneNumber, String status) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.roomId = roomId;
        this.studentId = studentId;
        this.college = college;
        this.major = major;
        this.grade = grade;
        this.clazz = clazz;
        this.bedNumber = bedNumber;
        this.checkInDate = checkInDate;
        this.phoneNumber = phoneNumber;
        this.status = status;
    }

    // getter和setter方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getBedNumber() {
        return bedNumber;
    }

    public void setBedNumber(String bedNumber) {
        this.bedNumber = bedNumber;
    }

    public Date getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(Date checkInDate) {
        this.checkInDate = checkInDate;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", gender='" + gender + '\'' +
                ", roomId=" + roomId +
                ", studentId='" + studentId + '\'' +
                ", college='" + college + '\'' +
                ", major='" + major + '\'' +
                ", grade='" + grade + '\'' +
                ", clazz='" + clazz + '\'' +
                ", bedNumber='" + bedNumber + '\'' +
                ", checkInDate=" + checkInDate +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
