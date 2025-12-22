package model;

import java.math.*;
import java.time.*;
import java.util.*;
import uimodel.UserType;

/**
 * 宿舍事件日志实体
 * 用于记录宿舍管理系统中的各种事件
 */
public final class DormEvent {

    /* ===================== 业务域 ===================== */
    public enum BizType {
        VISITOR, FEE, DISCIPLINE, NOTICE, SYSTEM_LOG
    }

    public enum Status {
        PENDING, NORMAL, CANCELED, REJECTED
    }

    public enum IdType { ID_CARD, PASSPORT, STUDENT_CARD, WORK_CARD, OTHER }

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

    /* ---- 费用域 ---- */
    private final Optional<BigDecimal> feeAmount;
    private final Optional<String>  feeItem;

    /* ---- 权限 & 日志 ---- */
    private final int               privilegeMask;  // 位掩码
    private final Optional<String>  logDetail;     // 日志详情

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

        this.feeAmount = Optional.ofNullable(b.feeAmount);
        this.feeItem   = Optional.ofNullable(b.feeItem);

        this.privilegeMask = b.privilegeMask;
        this.logDetail     = Optional.ofNullable(b.logDetail);
    }

    /* ===================== 业务工厂 ===================== */
    public static DormEvent newVisitor(String sno, String building, String room,
                                       String gName, IdType gIdType, String gIdNo, String gPhone,
                                       String reason, LocalDateTime come, LocalDateTime leave) {
        return full(BizType.VISITOR, "V" + timestamp(), sno, building, room)
                .guestName(gName).guestIdType(gIdType).guestIdNo(gIdNo)
                .guestPhone(gPhone).guestReason(reason).guestLeave(leave)
                .status(Status.NORMAL).build();
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

    public DormEvent guestLeaveNow() {
        return toBuilder().guestLeave(LocalDateTime.now()).build();
    }

    /* ===================== 权限工具 ===================== */
    public boolean canSee(String viewerSno, UserType viewerType, String viewerBuilding) {
        if (viewerType == UserType.ADMIN) return true;
        if (viewerType == UserType.DORM_MANAGER)
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
        private String        guestName, guestPhone, guestIdNo, guestReason;
        private IdType        guestIdType;
        private LocalDateTime guestLeave;
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
            this.bizType = e.bizType;
            this.id = e.id;
            this.sno = e.sno;
            this.adminNo = e.adminNo;
            this.roomNum = e.roomNum;
            this.building = e.building;
            this.happenTime = e.happenTime;
            this.status = e.status;
            this.deleted = e.deleted;
            this.guestName = e.guestName.orElse(null);
            this.guestIdType = e.guestIdType.orElse(null);
            this.guestIdNo = e.guestIdNo.orElse(null);
            this.guestPhone = e.guestPhone.orElse(null);
            this.guestReason = e.guestReason.orElse(null);
            this.guestLeave = e.guestLeave.orElse(null);
            this.feeAmount = e.feeAmount.orElse(null);
            this.feeItem = e.feeItem.orElse(null);
            this.privilegeMask = e.privilegeMask;
            this.logDetail = e.logDetail.orElse(null);
        }

        /* 链式 setter 返回 Builder，最后 build() */
        public Builder adminNo(String adminNo) { this.adminNo = adminNo; return this; }
        public Builder status(Status status) { this.status = status; return this; }
        public Builder guestName(String guestName) { this.guestName = guestName; return this; }
        public Builder guestIdType(IdType guestIdType) { this.guestIdType = guestIdType; return this; }
        public Builder guestIdNo(String guestIdNo) { this.guestIdNo = guestIdNo; return this; }
        public Builder guestPhone(String guestPhone) { this.guestPhone = guestPhone; return this; }
        public Builder guestReason(String guestReason) { this.guestReason = guestReason; return this; }
        public Builder guestLeave(LocalDateTime guestLeave) { this.guestLeave = guestLeave; return this; }
        public Builder feeAmount(BigDecimal feeAmount) { this.feeAmount = feeAmount; return this; }
        public Builder feeItem(String feeItem) { this.feeItem = feeItem; return this; }
        public Builder logDetail(String logDetail) { this.logDetail = logDetail; return this; }
        public Builder remark(String remark) { this.logDetail = remark; return this; } // alias for logDetail
        public DormEvent build() { return new DormEvent(this); }
    }
}