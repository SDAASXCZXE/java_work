package service.impl;

import dao.HolidayDao;
import dao.impl.HolidayDaoImpl;
import model.Holiday;
import service.HolidayService;

import java.util.List;

/**
 * 假期 Service 实现
 */
public class HolidayServiceImpl implements HolidayService {

    private HolidayDao dao = new HolidayDaoImpl();

    @Override
    public List<Holiday> listAll() {
        return dao.findAll();
    }

    @Override
    public List<Holiday> listByStudent(String studentId) {
        return dao.findByStudentId(studentId);
    }

    @Override
    public List<Holiday> listByStatus(Holiday.HolidayStatus status) {
        return dao.findByStatus(status);
    }

    @Override
    public boolean add(Holiday holiday) {
        return dao.insert(holiday);
    }

    @Override
    public boolean update(Holiday holiday) {
        return dao.update(holiday);
    }

    @Override
    public boolean removeById(String id) {
        return dao.deleteById(id);
    }
}

