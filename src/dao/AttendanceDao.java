package dao;

import model.Attendance;
import java.time.LocalDate;
import java.util.List;

public interface AttendanceDao {
    /** 查询所有非删除的考勤记录 */
    List<Attendance> findAll();

    /** 按日期查询考勤记录 */
    List<Attendance> findByDate(LocalDate date);

    /** 插入一条考勤记录 */
    boolean insert(Attendance attendance);

    /** 根据 id 逻辑删除 */
    boolean deleteById(String id);
}

