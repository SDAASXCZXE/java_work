package service;

import model.Attendance;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤业务层接口
 */
public interface AttendanceService {
    /** 列出所有考勤记录（未删除） */
    List<Attendance> listAll();

    /** 按日期列出考勤记录 */
    List<Attendance> listByDate(LocalDate date);

    /** 按学号列出个人考勤记录 */
    List<Attendance> listByStudent(String studentId);

    /** 新增一条考勤记录 */
    boolean add(Attendance attendance);

    /** 根据 id 逻辑删除一条记录 */
    boolean removeById(String id);
}
