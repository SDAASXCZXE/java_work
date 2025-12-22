package model;

import java.time.LocalDateTime;

/**
 * 访客记录（一次完整的来访闭环）
 *
 * @param vid          访客编号（业务主键 V+yyyyMMdd+序号）
 * @param visitorName  访客姓名
 * @param comeTime     来访时间
 * @param leaveTime    离开时间
 * @param roomNum      被访宿舍号
 * @param studentSno   被访学生学号
 * @param reason       来访事由
 * @param idType       证件类型
 * @param idNo         证件号码
 * @param phone        访客电话
 * @param remark       备注（可空）
 */
public record Visitor(
        String vid,
        String visitorName,
        LocalDateTime comeTime,
        LocalDateTime leaveTime,
        String roomNum,
        String studentSno,
        String reason,
        IdType idType,
        String idNo,
        String phone,
        String remark) {

    public enum IdType { ID_CARD, STUDENT_CARD, WORK_CARD, PASSPORT, OTHER }

    /* 快速工厂：新建登记时调用（离开时间为空） */
    public static Visitor create(String visitorName,
                                 String roomNum,
                                 String studentSno,
                                 String reason,
                                 IdType idType,
                                 String idNo,
                                 String phone,
                                 String remark) {
        String vid = "V" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
                + String.format("%04d", (int) (Math.random() * 9999));
        return new Visitor(vid, visitorName, LocalDateTime.now(), null,
                roomNum, studentSno, reason, idType, idNo, phone, remark);
    }

    /* 离开扫码时补全 */
    public Visitor leave() {
        return new Visitor(vid, visitorName, comeTime, LocalDateTime.now(),
                roomNum, studentSno, reason, idType, idNo, phone, remark);
    }

    /* 是否已离开 */
    public boolean isGone() { return leaveTime != null; }
}
