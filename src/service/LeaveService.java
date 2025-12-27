package service;

import model.LeaveRequest;
import java.util.List;

public interface LeaveService {
    java.util.List<LeaveRequest> listByStudent(String studentId);
    boolean addLeave(LeaveRequest l);
    boolean deleteById(String id);
}

