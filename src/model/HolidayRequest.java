package model;

import java.time.LocalDate;

/**
 * 学生假期离校/返校申请
 *
 * @param hid        主键（数据库自增）
 * @param sno        学号
 * @param type       登记类型
 * @param leaveDate  离校日期
 * @param backDate   返校日期（可空）
 * @param destination目的地
 * @param emergency  紧急联系人
 * @param emergencyPhone 紧急电话
 * @param status     审批状态
 */
public record HolidayRequest(
        Long hid,
        String sno,
        Type type,
        LocalDate leaveDate,
        LocalDate backDate,
        String destination,
        String emergency,
        String emergencyPhone,
        Status status) {

    public enum Type { LEAVE, BACK }
    public enum Status { PENDING, APPROVED, REJECTED }

    /* 新建离校申请 */
    public static HolidayRequest leave(String sno,
                                       LocalDate leave,
                                       LocalDate back,
                                       String dest,
                                       String contact,
                                       String phone) {
        return new HolidayRequest(null, sno, Type.LEAVE, leave, back, dest, contact, phone, Status.PENDING);
    }
}
