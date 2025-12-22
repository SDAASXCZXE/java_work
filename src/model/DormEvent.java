package model;

import java.time.*;
import java.util.*;
import java.math.BigDecimal;

/**
 * 宿舍统一事件实体
 * 一张表/一个JSON即可存访客、考勤、保修、假期、我的考勤五类业务
 */
public final class DormEvent {

    /* ===================== 业务类型 ===================== */
    public enum BizType {
        VISITOR,      // 访客登记
        ATTENDANCE,   // 考勤打卡
        REPAIR,       // 宿舍报修
        HOLIDAY,      // 假期登记
        MY_ATT        // 我的考勤（聚合视图）
    }

    /* ===================== 通用状态 ===================== */
    public enum Status {
        PENDING,      // 待处理 / 待审批
        NORMAL,       // 正常 / 已通过
        LATE,         // 晚归
        ABSENT,       // 未归
        LEAVE,        // 请假
        PROCESSING,   // 处理中
        FINISHED,     // 已完成
        CANCELED      // 已取消
    }

    /* ===================== 字段 ===================== */
    private final String          id;           // 业务主键（各业务线前缀不同）
    private final BizType         bizType;      // 业务类型
    private final String          sno;          // 学号（或宿管账号）
    private final String          roomNum;      // 宿舍号
    private final LocalDateTime   happenTime;   // 事件发生时间
    private final Status          status;       // 状态
    private final Optional<String>  remark;      // 备注
    private final Optional<LocalDate> dateOpt;   // 仅日期（考勤/假期用）
    private final Optional<LocalTime> timeOpt;   // 仅时间（考勤用）
    private final Optional<String>  guestName;   // 访客姓名
    private final Optional<String>  guestPhone;  // 访客电话
    private final Optional<String>  guestIdNo;   // 访客证件
    private final Optional<LocalDateTime> leaveTime; // 访客/假期离开时间
    private final Optional<String>  problem;    // 报修问题描述
    private final Optional<BigDecimal> progress; // 报修进度 0-100
    private final Optional<BigDecimal> evaluate; // 报修评价 0-5
    private final Optional<LocalDate> backDate;  // 假期返校日期

    /* ===================== 私有构造 ===================== */
    private DormEvent(Builder b) {
        this.id         = b.id;
        this.bizType    = b.bizType;
        this.sno        = b.sno;
        this.roomNum    = b.roomNum;
        this.happenTime = b.happenTime;
        this.status     = b.status;
        this.remark     = Optional.ofNullable(b.remark);
        this.dateOpt    = Optional.ofNullable(b.dateOpt);
        this.timeOpt    = Optional.ofNullable(b.timeOpt);
        this.guestName  = Optional.ofNullable(b.guestName);
        this.guestPhone = Optional.ofNullable(b.guestPhone);
        this.guestIdNo  = Optional.ofNullable(b.guestIdNo);
        this.leaveTime  = Optional.ofNullable(b.leaveTime);
        this.problem    = Optional.ofNullable(b.problem);
        this.progress   = Optional.ofNullable(b.progress);
        this.evaluate   = Optional.ofNullable(b.evaluate);
        this.backDate   = Optional.ofNullable(b.backDate);
    }

    /* ===================== 5 个语义工厂 ===================== */
    public static DormEvent newVisitor(String sno, String room,
                                       String gName, String gPhone, String gIdNo,
                                       LocalDateTime come, LocalDateTime leave) {
        return new Builder(BizType.VISITOR, "V" + come.toLocalDate().toString().replace("-", "") + UUID.randomUUID().toString().substring(0, 4), sno, room)
                .happenTime(come)
                .guestName(gName).guestPhone(gPhone).guestIdNo(gIdNo)
                .leaveTime(leave)
                .status(Status.NORMAL)
                .build();
    }

    public static DormEvent newAttendance(String sno, String room,
                                          LocalDate date, LocalTime time, Status status) {
        return new Builder(BizType.ATTENDANCE, "A" + date.toString().replace("-", "") + sno, sno, room)
                .dateOpt(date).timeOpt(time).status(status)
                .happenTime(date.atTime(time))
                .build();
    }

    public static DormEvent newRepair(String sno, String room,
                                      String problem, String pics) {
        return new Builder(BizType.REPAIR, "R" + LocalDateTime.now().toString().replaceAll("[-:T]", "").substring(0, 14), sno, room)
                .problem(problem).status(Status.PENDING).progress(BigDecimal.ZERO)
                .happenTime(LocalDateTime.now())
                .build();
    }

    public static DormEvent newHoliday(String sno, String room,
                                       LocalDate leave, LocalDate back,
                                       String dest, String contact, String phone) {
        return new Builder(BizType.HOLIDAY, "H" + leave.toString().replace("-", "") + sno, sno, room)
                .dateOpt(leave).backDate(back)
                .remark(dest + "|" + contact + "|" + phone)
                .status(Status.PENDING)
                .happenTime(leave.atStartOfDay())
                .build();
    }

    public static DormEvent myAttendance(String sno, String room,
                                         long monthNormal, long monthLate, long monthAbsent,
                                         long totalNormal, long totalLate, long totalAbsent) {
        String id = "M" + YearMonth.now().toString().replace("-", "") + sno;
        String remark = String.format("月正常=%d|月晚归=%d|月未归=%d|总正常=%d|总晚归=%d|总未归=%d",
                monthNormal, monthLate, monthAbsent, totalNormal, totalLate, totalAbsent);
        return new Builder(BizType.MY_ATT, id, sno, room)
                .status(Status.NORMAL)
                .remark(remark)
                .happenTime(LocalDateTime.now())
                .build();
    }

    /* ===================== Getter ===================== */
    public String          getId()         { return id; }
    public BizType         getBizType()    { return bizType; }
    public String          getSno()        { return sno; }
    public String          getRoomNum()    { return roomNum; }
    public LocalDateTime   getHappenTime() { return happenTime; }
    public Status          getStatus()     { return status; }
    public Optional<String> getRemark()    { return remark; }
    public Optional<LocalDate> getDateOpt()   { return dateOpt; }
    public Optional<LocalTime> getTimeOpt()   { return timeOpt; }
    public Optional<String> getGuestName() { return guestName; }
    public Optional<String> getGuestPhone() { return guestPhone; }
    public Optional<String> getGuestIdNo() { return guestIdNo; }
    public Optional<LocalDateTime> getLeaveTime() { return leaveTime; }
    public Optional<String> getProblem()   { return problem; }
    public Optional<BigDecimal> getProgress() { return progress; }
    public Optional<BigDecimal> getEvaluate() { return evaluate; }
    public Optional<LocalDate> getBackDate()  { return backDate; }

    /* ===================== Builder ===================== */
    private static final class Builder {
        private final BizType   bizType;
        private final String    id;
        private final String    sno;
        private final String    roomNum;

        private LocalDateTime happenTime = LocalDateTime.now();
        private Status        status     = Status.PENDING;
        private String        remark;
        private LocalDate     dateOpt;
        private LocalTime     timeOpt;
        private String        guestName;
        private String        guestPhone;
        private String        guestIdNo;
        private LocalDateTime leaveTime;
        private String        problem;
        private BigDecimal    progress;
        private BigDecimal    evaluate;
        private LocalDate     backDate;

        Builder(BizType bizType, String id, String sno, String roomNum) {
            this.bizType = bizType;
            this.id      = id;
            this.sno     = sno;
            this.roomNum = roomNum;
        }

        Builder happenTime(LocalDateTime t) { this.happenTime = t; return this; }
        Builder status(Status s)            { this.status = s; return this; }
        Builder remark(String r)            { this.remark = r; return this; }
        Builder dateOpt(LocalDate d)        { this.dateOpt = d; return this; }
        Builder timeOpt(LocalTime t)        { this.timeOpt = t; return this; }
        Builder guestName(String n)         { this.guestName = n; return this; }
        Builder guestPhone(String p)        { this.guestPhone = p; return this; }
        Builder guestIdNo(String id)        { this.guestIdNo = id; return this; }
        Builder leaveTime(LocalDateTime t)  { this.leaveTime = t; return this; }
        Builder problem(String p)           { this.problem = p; return this; }
        Builder progress(BigDecimal p)      { this.progress = p; return this; }
        Builder evaluate(BigDecimal e)      { this.evaluate = e; return this; }
        Builder backDate(LocalDate d)       { this.backDate = d; return this; }

        DormEvent build() { return new DormEvent(this); }
    }

    /* ===================== toString ===================== */
    @Override
    public String toString() {
        return String.format("%s[%s] %s %s %s %s",
                bizType, id, sno, roomNum, status, happenTime);
    }
}
