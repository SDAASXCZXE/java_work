package service;

import model.Holiday;

public interface HolidayService {
    java.util.List<Holiday> listByStudent(String studentId);
    boolean addHoliday(Holiday h);
    boolean deleteById(String id);
}

