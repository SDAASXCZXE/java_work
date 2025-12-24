package service.impl;

import dao.AttendanceDao;
import dao.impl.AttendanceDaoImpl;
import model.Attendance;
import service.AttendanceService;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤 Service 实现，作为 UI 与 DAO 之间的中间层
 */
public class AttendanceServiceImpl implements AttendanceService {

    private AttendanceDao dao = new AttendanceDaoImpl();

    @Override
    public List<Attendance> listAll() {
        return dao.findAll();
    }

    @Override
    public List<Attendance> listByDate(LocalDate date) {
        return dao.findByDate(date);
    }

    @Override
    public List<Attendance> listByStudent(String studentId) {
        return dao.findByStudent(studentId);
    }

    @Override
    public boolean add(Attendance attendance) {
        return dao.insert(attendance);
    }

    @Override
    public boolean removeById(String id) {
        return dao.deleteById(id);
    }
}
