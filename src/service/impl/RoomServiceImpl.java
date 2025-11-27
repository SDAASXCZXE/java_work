
package service.impl;

import dao.RoomDao;
import dao.impl.RoomDaoImpl;
import model.Room;
import service.RoomService;

import java.util.List;

public class RoomServiceImpl implements RoomService {

    private RoomDao dao = new RoomDaoImpl();

    public List<Room> findAll() {
        return dao.findAll();
    }
}
