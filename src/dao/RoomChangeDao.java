package dao;

import model.RoomChange;
import java.util.List;

public interface RoomChangeDao {
    /** 按学生学号查询宿舍调整申请（按申请时间倒序） */
    List<RoomChange> findByStudentId(String studentId);

    /** 添加宿舍调整申请 */
    boolean addRoomChange(RoomChange rc);

    /** 物理删除申请 */
    boolean deleteById(String id);
}

