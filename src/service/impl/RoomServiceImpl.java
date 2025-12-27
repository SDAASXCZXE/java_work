package service.impl;

import dao.RoomDao;
import dao.impl.RoomDaoImpl;
import model.Room;
import service.RoomService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

    @Override
    public boolean importFromCsv(File file) {
        if (file == null || !file.exists()) return false;
        int success = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (first) { first = false; if (line.toLowerCase().contains("room_number") || line.contains("房间号")) continue; }
                String[] cols = line.split(",");
                // 期待字段顺序：room_number, building, room_type, total_beds, occupied, available_beds, monitor, phone, hygiene_score, status, remarks
                if (cols.length < 2) continue;
                String roomNumber = cols[0].trim();
                String building = cols.length > 1 ? cols[1].trim() : "";
                String roomType = cols.length > 2 ? cols[2].trim() : "";
                int totalBeds = cols.length > 3 ? parseIntSafe(cols[3].trim(), 4) : 4;
                int occupied = cols.length > 4 ? parseIntSafe(cols[4].trim(), 0) : 0;
                int available = cols.length > 5 ? parseIntSafe(cols[5].trim(), Math.max(0, totalBeds - occupied)) : Math.max(0, totalBeds - occupied);
                String monitor = cols.length > 6 ? cols[6].trim() : "";
                String phone = cols.length > 7 ? cols[7].trim() : "";
                int hygiene = cols.length > 8 ? parseIntSafe(cols[8].trim(), 80) : 80;
                String status = cols.length > 9 ? cols[9].trim() : "有空位";
                String remarks = cols.length > 10 ? cols[10].trim() : "";

                Room r = new Room(roomNumber, building, roomType, totalBeds, occupied, available, monitor, phone, hygiene, status, remarks);
                if (dao.insert(r)) success++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success > 0;
    }

    // 辅助：安全解析整数
    private int parseIntSafe(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }
}
