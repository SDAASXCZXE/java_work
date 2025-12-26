package dao;

import model.Repair;
import java.util.List;

/**
 * 报修数据访问接口
 */
public interface RepairDao {
    /** 按学生学号查询报修记录（按提交时间倒序） */
    List<Repair> findByStudentId(String studentId);

    /** 新增一条报修记录 */
    boolean addRepair(Repair repair);

    /** 物理删除一条报修记录 */
    boolean deleteById(String id);
}
