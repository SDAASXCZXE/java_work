package dao;

import model.Holiday;

import java.util.List;

public interface HolidayDao {
    /** 按学生学号查询请假记录（按申请时间倒序） */
    List<Holiday> findByStudentId(String studentId);

    /** 添加请假记录 */
    boolean addHoliday(Holiday h);

    /** 物理删除请假记录 */
    boolean deleteById(String id);
}

