package dao;

import model.LeaveRequest;

import java.util.List;

public interface LeaveDao {
    /** 按学生学号查询请假记录（按申请时间倒序） */
    List<LeaveRequest> findByStudentId(String studentId);

    /** 添加请假记录 */
    boolean addLeave(LeaveRequest l);

    /** 物理删除请假记录 */
    boolean deleteById(String id);
}

