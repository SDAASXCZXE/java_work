package service;

import model.Holiday;
import java.util.List;

public interface HolidayService {
    java.util.List<Holiday> listByStudent(String studentId);
    boolean addHoliday(Holiday h);
    boolean deleteById(String id);
}

