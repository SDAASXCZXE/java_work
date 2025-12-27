package dao;

import model.Visitor;

import java.util.List;

public interface VisitorDao {
    List<Visitor> findAll();
    boolean insert(Visitor v);
    boolean deleteById(String id); // 逻辑删除
}

