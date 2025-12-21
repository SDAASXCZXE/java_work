package model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 学生每日归寝打卡记录
 *
 * @param seq        主键（数据库自增）
 * @param sno        学号
 * @param date       日期
 * @param backTime   归寝时间
 * @param status     考勤状态
 * @param remark     备注（晚归原因/请假单号等）
 */
public record Attendance(
        Long seq,
        String sno,
        LocalDate date,
        LocalTime backTime,
        Status status,
        String remark) {

    public enum Status {
        NORMAL("正常"), LATE("晚归"), ABSENT("未归"), LEAVE("请假");
        private final String desc;
        Status(String desc) { this.desc = desc; }
        public String getDesc() { return desc; }
    }

    /* 工厂：正常刷卡 */
    public static Attendance normal(String sno, LocalTime backTime) {
        return new Attendance(null, sno, LocalDate.now(), backTime, Status.NORMAL, null);
    }

    /* 工厂：晚归 */
    public static Attendance late(String sno, LocalTime backTime, String why) {
        return new Attendance(null, sno, LocalDate.now(), backTime, Status.LATE, why);
    }
}
