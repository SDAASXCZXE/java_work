package uimodel;

/**
 * 用户类型枚举
 */
public enum UserType {
    ADMIN("管理员"),
    STUDENT("学生");


    private final String description;

    UserType(String description) {
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