package service;

import model.LeaveRequest;

public interface LeaveService {
    java.util.List<LeaveRequest> listByStudent(String studentId);
    boolean addLeave(LeaveRequest l);
    boolean deleteById(String id);
}

