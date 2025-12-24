package service;

import model.Holiday;

import java.util.List;

/**
 * 假期业务接口
 */
public interface HolidayService {
    List<Holiday> listAll();

    List<Holiday> listByStudent(String studentId);

    List<Holiday> listByStatus(Holiday.HolidayStatus status);

    boolean add(Holiday holiday);

    boolean update(Holiday holiday);

    boolean removeById(String id);
}

