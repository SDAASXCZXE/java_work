package model;

import java.time.LocalDate;
import java.util.List;

/**
 * 面向学生的“我的考勤”聚合视图（只读）
 *
 * @param sno           学号
 * @param monthNormal   本月正常天数
 * @param monthLate     本月晚归次数
 * @param monthAbsent   本月未归次数
 * @param totalNormal   累计正常天数
 * @param totalLate     累计晚归次数
 * @param totalAbsent   累计未归次数
 * @param records       最近 30 条明细
 */
public record MyAttendance(
        String sno,
        long monthNormal,
        long monthLate,
        long monthAbsent,
        long totalNormal,
        long totalLate,
        long totalAbsent,
        List<Attendance> records) {

    /* 工厂：根据原始明细计算统计值 */
    public static MyAttendance of(String sno, List<Attendance> list) {
        LocalDate today = LocalDate.now();
        long mn = list.stream().filter(a -> a.date().getMonth() == today.getMonth() && a.status() == Attendance.Status.NORMAL).count();
        long ml = list.stream().filter(a -> a.date().getMonth() == today.getMonth() && a.status() == Attendance.Status.LATE).count();
        long ma = list.stream().filter(a -> a.date().getMonth() == today.getMonth() && a.status() == Attendance.Status.ABSENT).count();

        long tn = list.stream().filter(a -> a.status() == Attendance.Status.NORMAL).count();
        long tl = list.stream().filter(a -> a.status() == Attendance.Status.LATE).count();
        long ta = list.stream().filter(a -> a.status() == Attendance.Status.ABSENT).count();

        return new MyAttendance(sno, mn, ml, ma, tn, tl, ta, list);
    }
}
