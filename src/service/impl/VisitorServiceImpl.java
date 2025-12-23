package service.impl;

import dao.VisitorDao;
import dao.impl.VisitorDaoImpl;
import model.Visitor;
import service.VisitorService;

import java.util.List;

public class VisitorServiceImpl implements VisitorService {
    private VisitorDao dao = new VisitorDaoImpl();

    @Override
    public List<Visitor> listAll() {
        return dao.findAll();
    }

    @Override
    public boolean addVisitor(Visitor v) {
        // 简单校验
        if (v == null) return false;
        if (v.getName() == null || v.getName().trim().isEmpty()) return false;
        if (v.getIdNumber() == null || v.getIdNumber().trim().isEmpty()) return false;
        if (v.getPhone() == null || v.getPhone().trim().isEmpty()) return false;
        if (v.getArriveTime() == null) v.setArriveTime(java.time.LocalDateTime.now());
        return dao.insert(v);
    }

    @Override
    public boolean removeById(String id) {
        return dao.deleteById(id);
    }
}

