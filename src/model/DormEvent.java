package model;

import java.math.*;
import java.time.*;
import java.util.*;
import java.util.function.Function;

/**
 * 宿舍全能事件实体
 * 一行数据 = 任意业务场景的全量信息
 */
public final class DormEvent {

    /* ===================== 业务域 ===================== */
    public enum BizType {
        VISITOR, ATTENDANCE, REPAIR, HOLIDAY, MY_ATT,
        ROOM_CHANGE, FEE, DISCIPLINE, NOTICE, SYSTEM_LOG
    }

    public enum Status {
        PENDING, NORMAL, LATE, ABSENT, LEAVE,
        PROCESSING, FINISHED, CANCELED, REJECTED, OVERDUE
    }

    public enum IdType { ID_CARD, PASSPORT, STUDENT_CARD, WORK_CARD, OTHER }
    public enum RepairType { WATER_ELEC, FURNITURE, NETWORK, HVAC, OTHER }
    public enum HolidayType { LEAVE, BACK }
    public enum AttDirection { IN, OUT }

    /* ===================== 字段 ===================== */
    private final String          id;               // 业务主键
    private final BizType         bizType;          // 业务域
    private final String          sno;              // 学生学号
    private final String          adminNo;          // 操作员（宿管/管理员）
    private final String          roomNum;          // 宿舍号
    private final String          building;         // 楼栋（冗余，方便索引）
    private final LocalDateTime   happenTime;       // 事件发生时间
    private final Status          status;           // 当前状态
    private final boolean         deleted;          // 逻辑删除标志

    /* ---- 访客域 ---- */
    private final Optional<String>  guestName;
    private final Optional<IdType>  guestIdType;
    private final Optional<String>  guestIdNo;
    private final Optional<String>  guestPhone;
    private final Optional<String>  guestReason;
    private final Optional<LocalDateTime> guestLeave;

    /* ---- 考勤域 ---- */
    private final Optional<LocalDate> attDate;
    private final Optional<LocalTime> attTime;
    private final Optional<AttDirection> attDirection;

    /* ---- 报修域 ---- */
    private final Optional<RepairType> repairType;
    private final Optional<String>  repairDesc;
    private final Optional<String>  repairImgs;     // 逗号分隔 URL
    private final Optional<BigDecimal> repairProgress;
    private final Optional<BigDecimal> repairEvaluate; // 0-5 星
    private final Optional<BigDecimal> repairFee;      // 维修费用

    /* ---- 假期域 ---- */
    private final Optional<HolidayType> holidayType;
    private final Optional<LocalDate> holidayLeave;
    private final Optional<LocalDate> holidayBack;
    private final Optional<String>  holidayDest;
    private final Optional<String>  holidayContact;
    private final Optional<String>  holidayPhone;

    /* ---- 费用域 ---- */
    private final Optional<BigDecimal> feeAmount;
    private final Optional<String>  feeItem;

    /* ---- 权限 & 日志 ---- */
    private final int               privilegeMask;  // 位掩码
    private final Optional<String>  logDetail;

    /* ===================== 构造器 ===================== */
    private DormEvent(Builder b) {
        this.id           = b.id;
        this.bizType      = b.bizType;
        this.sno          = b.sno;
        this.adminNo      = b.adminNo;
        this.roomNum      = b.roomNum;
        this.building     = b.building;
        this.happenTime   = b.happenTime;
        this.status       = b.status;
        this.deleted      = b.deleted;

        this.guestName   = Optional.ofNullable(b.guestName);
        this.guestIdType = Optional.ofNullable(b.guestIdType);
        this.guestIdNo   = Optional.ofNullable(b.guestIdNo);
        this.guestPhone  = Optional.ofNullable(b.guestPhone);
        this.guestReason = Optional.ofNullable(b.guestReason);
        this.guestLeave  = Optional.ofNullable(b.guestLeave);

        this.attDate      = Optional.ofNullable(b.attDate);
        this.attTime      = Optional.ofNullable(b.attTime);
        this.attDirection = Optional.ofNullable(b.attDirection);

        this.repairType    = Optional.ofNullable(b.repairType);
        this.repairDesc    = Optional.ofNullable(b.repairDesc);
        this.repairImgs    = Optional.ofNullable(b.repairImgs);
        this.repairProgress= Optional.ofNullable(b.repairProgress);
        this.repairEvaluate= Optional.ofNullable(b.repairEvaluate);
        this.repairFee     = Optional.ofNullable(b.repairFee);

        this.holidayType   = Optional.ofNullable(b.holidayType);
        this.holidayLeave  = Optional.ofNullable(b.holidayLeave);
        this.holidayBack   = Optional.ofNullable(b.holidayBack);
        this.holidayDest   = Optional.ofNullable(b.holidayDest);
        this.holidayContact= Optional.ofNullable(b.holidayContact);
        this.holidayPhone  = Optional.ofNullable(b.holidayPhone);

        this.feeAmount = Optional.ofNullable(b.feeAmount);
        this.feeItem   = Optional.ofNullable(b.feeItem);

        this.privilegeMask = b.privilegeMask;
        this.logDetail     = Optional.ofNullable(b.logDetail);
    }

    /* ===================== 14 个业务工厂 ===================== */
    public static DormEvent newVisitor(String sno, String building, String room,
                                       String gName, IdType gIdType, String gIdNo, String gPhone,
                                       String reason, LocalDateTime come, LocalDateTime leave) {
        return full(BizType.VISITOR, "V" + timestamp(), sno, building, room)
                .guestName(gName).guestIdType(gIdType).guestIdNo(gIdNo)
                .guestPhone(gPhone).guestReason(reason).guestLeave(leave)
                .status(Status.NORMAL).build();
    }

    public static DormEvent newAttendance(String sno, String building, String room,
                                          LocalDate date, LocalTime time, AttDirection dir, Status status) {
        return full(BizType.ATTENDANCE, "A" + date.toString().replace("-", "") + sno, sno, building, room)
                .attDate(date).attTime(time).attDirection(dir).status(status).build();
    }

    public static DormEvent newRepair(String sno, String building, String room,
                                      RepairType type, String desc, String imgUrls) {
        return full(BizType.REPAIR, "R" + timestamp(), sno, building, room)
                .repairType(type).repairDesc(desc).repairImgs(imgUrls)
                .repairProgress(BigDecimal.ZERO).status(Status.PENDING).build();
    }

    public static DormEvent newHoliday(String sno, String building, String room,
                                       HolidayType hType, LocalDate leave, LocalDate back,
                                       String dest, String contact, String phone) {
        return full(BizType.HOLIDAY, "H" + leave.toString().replace("-", "") + sno,
                sno, building, room)
                .holidayType(hType).holidayLeave(leave).holidayBack(back)
                .holidayDest(dest).holidayContact(contact).holidayPhone(phone)
                .status(Status.PENDING).build();
    }

    public static DormEvent myAttSummary(String sno, String building, String room,
                                         long monthN, long monthL, long monthA,
                                         long totalN, long totalL, long totalA) {
        String remark = String.format("月正常=%d|月晚归=%d|月未归=%d|总正常=%d|总晚归=%d|总未归=%d",
                monthN, monthL, monthA, totalN, totalL, totalA);
        return full(BizType.MY_ATT, "M" + YearMonth.now().toString().replace("-", "") + sno,
                sno, building, room).status(Status.NORMAL).remark(remark).build();
    }

    public static DormEvent roomChange(String sno, String oldBuilding, String oldRoom,
                                       String newBuilding, String newRoom, String reason) {
        return full(BizType.ROOM_CHANGE, "C" + timestamp(), sno, oldBuilding, oldRoom)
                .remark("旧=" + oldBuilding + oldRoom + "|新=" + newBuilding + newRoom + "|原因=" + reason)
                .status(Status.PENDING).build();
    }

    public static DormEvent fee(String sno, String building, String room,
                                BigDecimal amount, String item) {
        return full(BizType.FEE, "F" + timestamp(), sno, building, room)
                .feeAmount(amount).feeItem(item).status(Status.NORMAL).build();
    }

    public static DormEvent discipline(String sno, String building, String room,
                                       String detail) {
        return full(BizType.DISCIPLINE, "D" + timestamp(), sno, building, room)
                .remark(detail).status(Status.PENDING).build();
    }

    public static DormEvent notice(String adminNo, String building,
                                   String title, String content) {
        return full(BizType.NOTICE, "N" + timestamp(), null, building, null)
                .adminNo(adminNo).remark(title + "|" + content).status(Status.NORMAL).build();
    }

    public static DormEvent systemLog(String operator, String detail) {
        return full(BizType.SYSTEM_LOG, "L" + timestamp(), null, null, null)
                .adminNo(operator).logDetail(detail).status(Status.NORMAL).build();
    }

    /* ===================== 状态机工具 ===================== */
    public DormEvent nextStatus(Status newStatus) {
        return toBuilder().status(newStatus).build();
    }

    public DormEvent repairProgress(int percent) {
        return toBuilder()
                .repairProgress(BigDecimal.valueOf(percent))
                .status(percent == 100 ? Status.FINISHED : Status.PROCESSING)
                .build();
    }

    public DormEvent repairEvaluate(BigDecimal star) {
        return toBuilder().repairEvaluate(star).build();
    }

    public DormEvent guestLeaveNow() {
        return toBuilder().guestLeave(LocalDateTime.now()).build();
    }

    public DormEvent holidayApprove(LocalDate back) {
        return toBuilder().holidayBack(back).status(Status.NORMAL).build();
    }

    /* ===================== 权限工具 ===================== */
    public boolean canSee(String viewerSno, UserType viewerType, String viewerBuilding) {
        if (viewerType == UserType.ADMIN) return true;
        if (viewerType == UserType.DORM_ADMIN)
            return Objects.equals(building, viewerBuilding);
        return Objects.equals(sno, viewerSno); // 学生只能看自己
    }

    /* ===================== 只读 Getter ===================== */
    public String          getId()           { return id; }
    public BizType         getBizType()      { return bizType; }
    public String          getSno()          { return sno; }
    public String          getAdminNo()      { return adminNo; }
    public String          getRoomNum()      { return roomNum; }
    public String          getBuilding()     { return building; }
    public LocalDateTime   getHappenTime()   { return happenTime; }
    public Status          getStatus()       { return status; }
    public boolean         isDeleted()       { return deleted; }

    public Optional<String>  getGuestName()   { return guestName; }
    public Optional<IdType>  getGuestIdType() { return guestIdType; }
    public Optional<String>  getGuestIdNo()   { return guestIdNo; }
    public Optional<String>  getGuestPhone()  { return guestPhone; }
    public Optional<String>  getGuestReason() { return guestReason; }
    public Optional<LocalDateTime> getGuestLeave() { return guestLeave; }

    public Optional<LocalDate>     getAttDate()      { return attDate; }
    public Optional<LocalTime>     getAttTime()      { return attTime; }
    public Optional<AttDirection>  getAttDirection() { return attDirection; }

    public Optional<RepairType> getRepairType()    { return repairType; }
    public Optional<String>     getRepairDesc()    { return repairDesc; }
    public Optional<String>     getRepairImgs()    { return repairImgs; }
    public Optional<BigDecimal> getRepairProgress(){ return repairProgress; }
    public Optional<BigDecimal> getRepairEvaluate(){ return repairEvaluate; }
    public Optional<BigDecimal> getRepairFee()     { return repairFee; }

    public Optional<HolidayType> getHolidayType()   { return holidayType; }
    public Optional<LocalDate>   getHolidayLeave()  { return holidayLeave; }
    public Optional<LocalDate>   getHolidayBack()   { return holidayBack; }
    public Optional<String>      getHolidayDest()   { return holidayDest; }
    public Optional<String>      getHolidayContact(){ return holidayContact; }
    public Optional<String>      getHolidayPhone()  { return holidayPhone; }

    public Optional<BigDecimal> getFeeAmount() { return feeAmount; }
    public Optional<String>     getFeeItem()   { return feeItem; }

    public int               getPrivilegeMask() { return privilegeMask; }
    public Optional<String>  getLogDetail()     { return logDetail; }

    /* ===================== 内部工具 ===================== */
    private static String timestamp() {
        return LocalDateTime.now().toString().replaceAll("[-:T]", "").substring(0, 14);
    }
    private static Builder full(BizType t, String id, String sno, String building, String room) {
        return new Builder(t, id, sno, building, room);
    }
    public Builder toBuilder() {
        return new Builder(this);
    }

    /* ===================== Builder ===================== */
    public static final class Builder {
        // 省略雷同字段声明，与上文私有字段一一对应
        private final BizType   bizType;
        private final String    id;
        private String sno, adminNo, roomNum, building;
        private LocalDateTime happenTime = LocalDateTime.now();
        private Status        status     = Status.PENDING;
        private boolean       deleted    = false;
        private String        remark;
        private LocalDate     dateOpt, backDate;
        private LocalTime     timeOpt;
        private String        guestName, guestPhone, guestIdNo, guestReason;
        private LocalDateTime guestLeave;
        private AttDirection  attDirection;
        private RepairType    repairType;
        private String        repairDesc, repairImgs;
        private BigDecimal    repairProgress, repairEvaluate, repairFee;
        private HolidayType   holidayType;
        private String        holidayDest, holidayContact, holidayPhone;
        private BigDecimal    feeAmount;
        private String        feeItem;
        private int           privilegeMask = 0;
        private String        logDetail;

        /* 新事件构造 */
        Builder(BizType t, String id, String sno, String building, String room) {
            this.bizType = t; this.id = id; this.sno = sno; this.building = building; this.roomNum = room;
        }
        /* 已有事件修改 */
        Builder(DormEvent e) {
            // 全字段拷贝，省略
        }

        /* 链式 setter 返回 Builder，最后 build() */
        public DormEvent build() { return new DormEvent(this); }
    }
}
