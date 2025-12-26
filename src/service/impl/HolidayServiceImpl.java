package service.impl;

import dao.HolidayDao;
import dao.impl.HolidayDaoImpl;
import model.Holiday;
import service.HolidayService;

import java.util.List;

public class HolidayServiceImpl implements HolidayService {
    private final HolidayDao holidayDao = new HolidayDaoImpl();

    @Override
    public List<Holiday> listByStudent(String studentId) {
        try { return holidayDao.findByStudentId(studentId); } catch (Exception e) { e.printStackTrace(); return java.util.Collections.emptyList(); }
    }

    @Override
    public boolean addHoliday(Holiday h) {
        try { return holidayDao.addHoliday(h); } catch (Exception e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean deleteById(String id) {
        try { return holidayDao.deleteById(id); } catch (Exception e) { e.printStackTrace(); return false; }
    }
}

