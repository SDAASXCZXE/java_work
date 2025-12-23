package model;

/**
 * 考勤状态枚举
 */
public enum AttendanceStatus {
    NORMAL("正常", Color.GREEN),
    LATE("晚归", Color.YELLOW),
    ABSENT("未归", Color.RED),
    LEAVE("请假", Color.BLUE);

    private final String description;
    private final Color color;

    AttendanceStatus(String description, Color color) {
        this.description = description;
        this.color = color;
    }

    public String getDescription() {
        return description;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public String toString() {
        return description;
    }
}
