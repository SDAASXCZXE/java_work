package model;

import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

/**
 * 学生实体类
 */
public class Student {
    private String sno;  // 学号
    private String name;  // 姓名
    private String gender;  // 性别
    private String college;  // 学院
    private String major;  // 专业
    private String grade;  // 年级
    private String clazz;  // 班级
    private String phone;  // 电话
    private LocalDate inDate;  // 入学日期
    
    // 宿舍信息
    private String roomNumber;  // 宿舍号
    private String building;  // 楼栋
    private int bedNumber;  // 床位号
    private LocalDate checkInDate;  // 入住日期
    private boolean isMonitor;  // 是否为宿舍长

    // 无参构造器
    public Student() {}

    // 有参构造器
    public Student(String sno, String name, String gender, String college, String major, String grade, String clazz, String phone, LocalDate inDate) {
        this.sno = sno;
        this.name = name;
        this.gender = gender;
        this.college = college;
        this.major = major;
        this.grade = grade;
        this.clazz = clazz;
        this.phone = phone;
        this.inDate = inDate;
    }

    // Getter 和 Setter 方法
    public String getSno() {
        return sno;
    }

    public void setSno(String sno) {
        this.sno = sno;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getInDate() {
        return inDate;
    }

    public void setInDate(LocalDate inDate) {
        this.inDate = inDate;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public int getBedNumber() {
        return bedNumber;
    }

    public void setBedNumber(int bedNumber) {
        this.bedNumber = bedNumber;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public boolean isMonitor() {
        return isMonitor;
    }

    public void setMonitor(boolean monitor) {
        isMonitor = monitor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return Objects.equals(sno, student.sno);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sno);
    }

    @Override
    public String toString() {
        return "Student{" +
                "sno='" + sno + '\'' +
                ", name='" + name + '\'' +
                ", gender='" + gender + '\'' +
                ", college='" + college + '\'' +
                ", major='" + major + '\'' +
                ", grade='" + grade + '\'' +
                ", clazz='" + clazz + '\'' +
                ", phone='" + phone + '\'' +
                ", inDate=" + inDate +
                ", roomNumber='" + roomNumber + '\'' +
                ", building='" + building + '\'' +
                ", bedNumber=" + bedNumber +
                ", checkInDate=" + checkInDate +
                ", isMonitor=" + isMonitor +
                '}';
    }
}
