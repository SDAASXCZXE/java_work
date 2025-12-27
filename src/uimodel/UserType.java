/*
 * 文件：UserType.java
 * 说明：定义前端使用的用户类型（管理员、学生等）。
 * 注意：仅添加注释，不修改枚举定义等业务代码。
 */

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