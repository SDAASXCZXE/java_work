package service;

import model.Visitor;

import java.util.List;

public interface VisitorService {
    /** 查询所有未删除的访客 */
    List<Visitor> listAll();

    /** 登记新访客 */
    boolean addVisitor(Visitor v);

    /** 逻辑删除访客 */
    boolean removeById(String id);

    /** 更新访客信息（如补全离开时间、修改备注） */
    boolean updateVisitor(Visitor v);
}