package model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 考勤工具类
 */
public class AttendanceUtil {

    /**
     * 解析日期字符串
     */
    public static Date parseDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 解析时间字符串
     */
    public static Date parseTime(String timeStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            return sdf.parse(timeStr);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 解析日期时间字符串
     */
    public static Date parseDateTime(String dateTimeStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            return sdf.parse(dateTimeStr);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 格式化日期
     */
    public static String formatDate(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    /**
     * 格式化时间
     */
    public static String formatTime(Date time) {
        if (time == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(time);
    }

    /**
     * 格式化日期时间
     */
    public static String formatDateTime(Date dateTime) {
        if (dateTime == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(dateTime);
    }

    /**
     * 根据时间判断考勤状态
     */
    public static AttendanceStatus getStatusByTime(Date checkInTime) {
        if (checkInTime == null) {
            return AttendanceStatus.ABSENT;
        }

        // 获取小时数
        SimpleDateFormat sdf = new SimpleDateFormat("HH");
        int hour = Integer.parseInt(sdf.format(checkInTime));

        // 判断状态
        if (hour >= 22 && hour < 24) { // 22:00-23:59 为晚归
            return AttendanceStatus.LATE;
        } else if (hour >= 0 && hour < 6) { // 00:00-05:59 为晚归
            return AttendanceStatus.LATE;
        } else if (hour >= 6 && hour < 22) { // 06:00-21:59 为正常
            return AttendanceStatus.NORMAL;
        } else {
            return AttendanceStatus.NORMAL;
        }
    }

    /**
     * 验证学号格式
     */
    public static boolean validateStudentId(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            return false;
        }
        // 学号通常是8-10位数字
        return studentId.matches("\\d{8,10}");
    }

    /**
     * 验证宿舍号格式
     */
    public static boolean validateDormitory(String dormitory) {
        if (dormitory == null || dormitory.trim().isEmpty()) {
            return false;
        }
        // 宿舍号格式如：A101, B202, C303
        return dormitory.matches("[A-Za-z]\\d{3}");
    }

    /**
     * 获取状态对应的颜色
     */
    public static java.awt.Color getStatusColor(AttendanceStatus status) {
        switch (status) {
            case NORMAL:
                return new java.awt.Color(200, 255, 200); // 浅绿色
            case LATE:
                return new java.awt.Color(255, 255, 200); // 浅黄色
            case ABSENT:
                return new java.awt.Color(255, 200, 200); // 浅红色
            case LEAVE:
                return new java.awt.Color(200, 220, 255); // 浅蓝色
            default:
                return java.awt.Color.WHITE;
        }
    }

    /**
     * 获取当前时间字符串
     */
    public static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    /**
     * 计算时间段内的天数
     */
    public static int getDaysBetween(Date startDate, Date endDate) {
        long diff = endDate.getTime() - startDate.getTime();
        return (int) (diff / (1000 * 60 * 60 * 24));
    }
}