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
    public boolean deleteByBuildingAndRoom(String building, String roomNumber) {
        try {
            // 直接调用 DAO 中的实现
            java.lang.reflect.Method m = dao.getClass().getMethod("deleteByBuildingAndRoom", String.class, String.class);
            Object res = m.invoke(dao, building, roomNumber);
            return res instanceof Boolean && (Boolean) res;
        } catch (NoSuchMethodException nsme) {
            // 兼容：如果 DAO 没有该方法，退回到按 roomNumber 删除（不推荐）
            try {
                java.lang.reflect.Method m2 = dao.getClass().getMethod("deleteByRoomNumber", String.class);
                Object res = m2.invoke(dao, roomNumber);
                return res instanceof Boolean && (Boolean) res;
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateOccupancy(String roomNumber, int occupied, int available, String status) {
        return dao.updateOccupancy(roomNumber, occupied, available, status);
    }

    @Override
    public boolean existsByRoomNumber(String roomNumber) {
        return dao.existsByRoomNumber(roomNumber);
    }

    @Override
    public boolean updateOccupancy(String building, String roomNumber, int occupied, int available, String status) {
        try {
            java.lang.reflect.Method m = dao.getClass().getMethod("updateOccupancy", String.class, String.class, int.class, int.class, String.class);
            Object res = m.invoke(dao, building, roomNumber, occupied, available, status);
            return res instanceof Boolean && (Boolean) res;
        } catch (NoSuchMethodException nsme) {
            return dao.updateOccupancy(roomNumber, occupied, available, status);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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
