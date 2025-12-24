package dao;

import model.Holiday;
import java.util.List;

/**
 * 假期登记 DAO 接口
 */
public interface HolidayDao {
    /** 查询所有未删除的假期登记 */
    List<Holiday> findAll();

    /** 按学生学号查询假期登记 */
    List<Holiday> findByStudentId(String studentId);

    /** 按状态查询（如 PENDING/APPROVED 等） */
    List<Holiday> findByStatus(Holiday.HolidayStatus status);

    /** 插入一条登记 */
    boolean insert(Holiday holiday);

    /** 更新一条登记（主要用于状态/实际返校日期等） */
    boolean update(Holiday holiday);

    /** 根据 id 逻辑删除 */
    boolean deleteById(String id);
}

