package service;

import model.RoomChange;

public interface RoomChangeService {
    java.util.List<RoomChange> listByStudent(String studentId);
    boolean addRoomChange(RoomChange rc);
    boolean deleteById(String id);
}

