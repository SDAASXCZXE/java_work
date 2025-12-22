package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 宿舍报修单（支持图片 URL、进度、评价）
 *
 * @param rid          报修单号 R+yyyyMMdd+序号
 * @param roomNum      宿舍号
 * @param studentSno   报修人学号
 * @param problemType  问题类型
 * @param description  问题描述
 * @param picUrls      图片地址（逗号分隔）
 * @param status       处理状态
 * @param progress     进度 0-100
 * @param createTime   创建时间
 * @param finishTime   完成时间（可空）
 * @param evaluate     评价（0-5星，可空）
 */
public record Repair(
        String rid,
        String roomNum,
        String studentSno,
        ProblemType problemType,
        String description,
        String picUrls,
        Status status,
        byte progress,
        LocalDateTime createTime,
        LocalDateTime finishTime,
        BigDecimal evaluate) {

    public enum ProblemType { WATER_ELEC, FURNITURE, NETWORK, HVAC, OTHER }
    public enum Status { PENDING, PROCESSING, FINISHED, CANCELED }

    /* 新建报修 */
    public static Repair create(String roomNum,
                                String studentSno,
                                ProblemType type,
                                String desc,
                                String pics) {
        String rid = "R" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
                + String.format("%04d", (int) (Math.random() * 9999));
        return new Repair(rid, roomNum, studentSno, type, desc, pics,
                Status.PENDING, (byte) 0, LocalDateTime.now(), null, null);
    }

    /* 更新进度 */
    public Repair progress(byte percent) {
        return new Repair(rid, roomNum, studentSno, problemType, description, picUrls,
                percent == 100 ? Status.FINISHED : Status.PROCESSING,
                percent, createTime, percent == 100 ? LocalDateTime.now() : null, evaluate);
    }
}
