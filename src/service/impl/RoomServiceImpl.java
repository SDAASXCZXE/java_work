package service.impl;

import dao.RoomDao;
import dao.impl.RoomDaoImpl;
import model.Room;
import service.RoomService;

import java.util.List;

/**
 * RoomService 的实现，作为 GUI 与 DAO 之间的中间层
 */
public class RoomServiceImpl implements RoomService {

    private RoomDao dao = new RoomDaoImpl();

    @Override
    public List<Room> findAll() {
        return dao.findAll();
    }

    @Override
    public boolean add(Room room) {
        return dao.insert(room);
    }

    @Override
    public boolean update(Room room) {
        return dao.update(room);
    }

    @Override
    public boolean deleteByRoomNumber(String roomNumber) {
        return dao.deleteByRoomNumber(roomNumber);
    }

    @Override
    public boolean updateOccupancy(String roomNumber, int occupied, int available, String status) {
        return dao.updateOccupancy(roomNumber, occupied, available, status);
    }

    // 新增：检查房间是否存在，UI 使用以避免重复添加
    public boolean existsByRoomNumber(String roomNumber) {
        return dao.existsByRoomNumber(roomNumber);
    }
}
