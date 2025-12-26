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
    private String roomNumber;  // 宿舍号 带栋
    private String bedNumber;  // 床位号

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

    public String getBedNumber() {
        return bedNumber;
    }

    public void setBedNumber(String bedNumber) {
        this.bedNumber = bedNumber;
    }

    /**
     * 解析并返回楼栋部分，例如："A栋101" 返回 "A栋"；"A-101" 返回 "A"；若无法解析则返回空字符串或原值
     */
    public String getBuilding() {
        if (roomNumber == null || roomNumber.trim().isEmpty()) return "";
        String r = roomNumber.trim();
        // 常见带“栋”的格式，如 "A栋101"
        int idx = r.indexOf('栋');
        if (idx > 0) return r.substring(0, idx + 1);
        // 常见含 '-' 或 '_' 分隔，如 "A-101" 或 "A_101"
        for (char sep : new char[]{'-', '_', ' '}) {
            int p = r.indexOf(sep);
            if (p > 0) return r.substring(0, p);
        }
        // 否则提取前导非数字字符作为楼栋
        int firstDigit = -1;
        for (int i = 0; i < r.length(); i++) {
            if (Character.isDigit(r.charAt(i))) { firstDigit = i; break; }
        }
        if (firstDigit > 0) return r.substring(0, firstDigit);
        // 若无法区分，则返回原 roomNumber（方便调用方识别）
        return r;
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
                ", bedNumber=" + bedNumber +
                '}';
    }
}
