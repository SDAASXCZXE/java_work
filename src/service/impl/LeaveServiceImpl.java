package service.impl;

import dao.LeaveDao;
import dao.impl.LeaveDaoImpl;
import model.LeaveRequest;
import service.LeaveService;

import java.util.List;

public class LeaveServiceImpl implements LeaveService {
    private final LeaveDao leaveDao = new LeaveDaoImpl();

    @Override
    public List<LeaveRequest> listByStudent(String studentId) {
        try { return leaveDao.findByStudentId(studentId); } catch (Exception e) { e.printStackTrace(); return java.util.Collections.emptyList(); }
    }

    @Override
    public boolean addLeave(LeaveRequest l) {
        try { return leaveDao.addLeave(l); } catch (Exception e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean deleteById(String id) {
        try { return leaveDao.deleteById(id); } catch (Exception e) { e.printStackTrace(); return false; }
    }
}

