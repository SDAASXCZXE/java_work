package uimodel;

/**
 * 用户实体类
 */
public class User {
    private String username;
    private String password;
    private UserType userType;
    private String studentId; // 学生专用
    private String name;
    private String phone;
    private String email;

    public User() {
    }

    public User(String username, String password, UserType userType) {
        this.username = username;
        this.password = password;
        this.userType = userType;
    }

    public User(String username, String password, UserType userType,
                String studentId, String name, String phone, String email) {
        this.username = username;
        this.password = password;
        this.userType = userType;
        this.studentId = studentId;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", userType=" + userType +
                ", name='" + name + '\'' +
                '}';
    }
}