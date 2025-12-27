package service;

import model.Repair;

/**
 * 报修业务接口（学生端、管理员端共用）
 */
public interface RepairService {
    /** 列出某个学生的所有报修记录（按时间倒序） */
    java.util.List<Repair> listByStudent(String studentId);

    /** 添加新报修 */
    boolean addRepair(Repair repair);

    /** 物理删除一条报修记录 */
    boolean deleteById(String id);
}
