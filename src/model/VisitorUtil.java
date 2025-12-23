package model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 访客工具类
 */
public class VisitorUtil {

    /**
     * 生成访客ID
     */
    public static String generateVisitorId() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return "V" + sdf.format(new Date());
    }

    /**
     * 验证证件号码格式
     */
    public static boolean validateIdNumber(String idType, String idNumber) {
        if (idNumber == null || idNumber.trim().isEmpty()) {
            return false;
        }

        switch (idType) {
            case "身份证":
                return validateChineseID(idNumber);
            case "护照":
                return validatePassport(idNumber);
            case "学生证":
                return validateStudentCard(idNumber);
            default:
                return idNumber.length() >= 3;
        }
    }

    /**
     * 验证身份证号码
     */
    private static boolean validateChineseID(String idNumber) {
        // 简单的身份证验证
        return idNumber.matches("\\d{17}[0-9Xx]") || idNumber.matches("\\d{15}");
    }

    /**
     * 验证护照号码
     */
    private static boolean validatePassport(String idNumber) {
        // 护照格式：E1234567 或 G12345678
        return idNumber.matches("[A-Za-z]\\d{7,8}");
    }

    /**
     * 验证学生证号码
     */
    private static boolean validateStudentCard(String idNumber) {
        // 学生证通常是纯数字
        return idNumber.matches("\\d{6,10}");
    }

    /**
     * 验证电话号码
     */
    public static boolean validatePhone(String phone) {
        if (phone == null) return false;
        return phone.matches("1[3-9]\\d{9}") || phone.matches("\\d{3,4}-\\d{7,8}");
    }

    /**
     * 格式化时间显示
     */
    public static String formatDateTime(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(date);
    }

    /**
     * 解析时间字符串
     */
    public static Date parseDateTime(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            return sdf.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 计算访问时长（格式化显示）
     */
    public static String formatDuration(long minutes) {
        if (minutes < 60) {
            return minutes + "分钟";
        } else {
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;
            return hours + "小时" + (remainingMinutes > 0 ? remainingMinutes + "分钟" : "");
        }
    }
}