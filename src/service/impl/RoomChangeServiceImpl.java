package service.impl;

import dao.RoomChangeDao;
import dao.impl.RoomChangeDaoImpl;
import model.RoomChange;
import service.RoomChangeService;

import java.util.List;

public class RoomChangeServiceImpl implements RoomChangeService {
    private final RoomChangeDao rcDao = new RoomChangeDaoImpl();

    @Override
    public List<RoomChange> listByStudent(String studentId) {
        try { return rcDao.findByStudentId(studentId); } catch (Exception e) { e.printStackTrace(); return java.util.Collections.emptyList(); }
    }

    @Override
    public boolean addRoomChange(RoomChange rc) {
        try { return rcDao.addRoomChange(rc); } catch (Exception e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean deleteById(String id) {
        try { return rcDao.deleteById(id); } catch (Exception e) { e.printStackTrace(); return false; }
    }
}

