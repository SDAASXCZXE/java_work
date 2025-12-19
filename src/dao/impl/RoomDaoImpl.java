package dao.impl;

import dao.RoomDao;
import model.Room;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * RoomDao 的 JDBC 实现，负责与 `room` 表交互。
 */
public class RoomDaoImpl implements RoomDao {
    @Override
    public List<Room> findAll() {
        List<Room> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) return list;
            String sql = "SELECT * FROM room";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                String roomNo = safeGetString(rs, "room_number", "room_no", "dorm_no", "roomNumber");
                String building = safeGetString(rs, "building", "building_name");
                String type = safeGetString(rs, "room_type", "type");
                int totalBeds = safeGetInt(rs, "total_beds", "beds");
                int occupied = safeGetInt(rs, "occupied", "used");
                int available = safeGetInt(rs, "available_beds", "available");
                String monitor = safeGetString(rs, "monitor", "dorm_leader");
                String phone = safeGetString(rs, "phone", "contact");
                int hygiene = safeGetInt(rs, "hygiene_score", "score");
                String status = safeGetString(rs, "status", "state");
                String remarks = safeGetString(rs, "remarks", "remark");

                // 转换类型/状态为枚举，容错：支持短码（S/D/Q/6）、英文名、或中文描述
                Room.RoomType roomType = mapDbValueToRoomType(type);
                Room.RoomStatus roomStatus = mapDbValueToRoomStatus(status);

                if (available == Integer.MIN_VALUE) {
                    if (totalBeds != Integer.MIN_VALUE && occupied != Integer.MIN_VALUE) {
                        available = totalBeds - occupied;
                    } else {
                        available = 0;
                    }
                }
                if (totalBeds == Integer.MIN_VALUE) totalBeds = 0;
                if (occupied == Integer.MIN_VALUE) occupied = 0;
                if (hygiene == Integer.MIN_VALUE) hygiene = 0;

                // 构造 Room（参数顺序与实体类构造器一致）
                Room r = new Room(roomNo, building, roomType, totalBeds, occupied, available, monitor, phone, hygiene, roomStatus, remarks);
                list.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
            DBUtil.close(conn);
        }
        return list;
    }

    @Override
    public boolean insert(Room room) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) return false;
            String sql = "INSERT INTO room (room_number, building, room_type, total_beds, occupied, available_beds, monitor, phone, hygiene_score, status, remarks) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, truncate(room.getRoom_number(), 64));
            ps.setString(2, truncate(room.getBuilding(), 32));
            // 写入短码或受控字符串以避免长度/类型不匹配
            ps.setString(3, mapRoomTypeToDb(room.getRoom_type()));
            ps.setInt(4, room.getTotal_beds());
            ps.setInt(5, room.getOccupied());
            ps.setInt(6, room.getAvailable_beds());
            ps.setString(7, truncate(room.getMonitor().orElse(null), 64));
            ps.setString(8, truncate(room.getPhone().orElse(null), 32));
            ps.setInt(9, room.getHygiene_score());
            ps.setString(10, mapStatusToDb(room.getStatus()));
            ps.setString(11, truncate(room.getRemarks().orElse(null), 512));
            int c = ps.executeUpdate();
            return c > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
            DBUtil.close(conn);
        }
    }

    @Override
    public boolean update(Room room) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) return false;
            String sql = "UPDATE room SET building=?, room_type=?, total_beds=?, occupied=?, available_beds=?, monitor=?, phone=?, hygiene_score=?, status=?, remarks=? WHERE room_number=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, truncate(room.getBuilding(), 32));
            ps.setString(2, mapRoomTypeToDb(room.getRoom_type()));
            ps.setInt(3, room.getTotal_beds());
            ps.setInt(4, room.getOccupied());
            ps.setInt(5, room.getAvailable_beds());
            ps.setString(6, truncate(room.getMonitor().orElse(null), 64));
            ps.setString(7, truncate(room.getPhone().orElse(null), 32));
            ps.setInt(8, room.getHygiene_score());
            ps.setString(9, mapStatusToDb(room.getStatus()));
            ps.setString(10, truncate(room.getRemarks().orElse(null), 512));
            ps.setString(11, truncate(room.getRoom_number(), 64));
            int c = ps.executeUpdate();
            return c > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
            DBUtil.close(conn);
        }
    }

    @Override
    public boolean deleteByRoomNumber(String roomNumber) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) return false;
            String sql = "DELETE FROM room WHERE room_number = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, truncate(roomNumber, 64));
            int c = ps.executeUpdate();
            return c > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
            DBUtil.close(conn);
        }
    }

    @Override
    public boolean updateOccupancy(String roomNumber, int occupied, int available, String status) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) return false;
            String sql = "UPDATE room SET occupied = ?, available_beds = ?, status = ? WHERE room_number = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, occupied);
            ps.setInt(2, available);
            ps.setString(3, mapStatusToDbString(status));
            ps.setString(4, truncate(roomNumber, 64));
            int c = ps.executeUpdate();
            return c > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
            DBUtil.close(conn);
        }
    }

    // 辅助从 ResultSet 获取 String/Int（按多个候选列名）
    private String safeGetString(ResultSet rs, String... names) {
        for (String name : names) {
            try {
                String v = rs.getString(name);
                if (v != null) return v;
            } catch (Exception ignored) {}
        }
        try { String v = rs.getString(1); return v; } catch (Exception ignored) {}
        return "";
    }

    private int safeGetInt(ResultSet rs, String... names) {
        for (String name : names) {
            try {
                int v = rs.getInt(name);
                if (!rs.wasNull()) return v;
            } catch (Exception ignored) {}
        }
        return Integer.MIN_VALUE;
    }

    // 将 RoomType 转换为数据库友好的短码（可根据实际表结构调整）
    private String mapRoomTypeToDb(Room.RoomType rt) {
        if (rt == null) return null;
        switch (rt) {
            case SINGLE: return "S";    // single
            case DOUBLE: return "D";    // double
            case QUAD: return "Q";      // quad/four
            case SIX: return "6";       // six
            default: return "Q"; // 默认写入四人间短码以保持安全兼容
        }
    }

    // 将数据库中的 room_type 值映射回 Room.RoomType（支持短码、英文、中文）
    private Room.RoomType mapDbValueToRoomType(String v) {
        if (v == null) return Room.RoomType.QUAD;
        v = v.trim();
        if (v.isEmpty()) return Room.RoomType.QUAD;
        String up = v.toUpperCase();
        if (up.equals("S") || up.contains("SINGLE") || v.contains("单")) return Room.RoomType.SINGLE;
        if (up.equals("D") || up.contains("DOUBLE") || v.contains("二")) return Room.RoomType.DOUBLE;
        if (up.equals("Q") || up.contains("QUAD") || up.contains("FOUR") || v.contains("四")) return Room.RoomType.QUAD;
        if (up.equals("6") || up.contains("SIX") || v.contains("六")) return Room.RoomType.SIX;
        // Fallback: try to parse digits
        try {
            int num = Integer.parseInt(v);
            if (num == 1) return Room.RoomType.SINGLE;
            if (num == 2) return Room.RoomType.DOUBLE;
            if (num == 4) return Room.RoomType.QUAD;
            if (num == 6) return Room.RoomType.SIX;
        } catch (Exception ignored) {}
        return Room.RoomType.QUAD;
    }

    // 将数据库中 status 值映射回 Room.RoomStatus（支持中文、英文、短码）
    private Room.RoomStatus mapDbValueToRoomStatus(String v) {
        if (v == null) return Room.RoomStatus.AVAILABLE;
        v = v.trim();
        if (v.isEmpty()) return Room.RoomStatus.AVAILABLE;
        String up = v.toUpperCase();
        if (up.equals("FULL") || up.contains("FULL") || v.contains("已住满") || v.contains("满")) return Room.RoomStatus.FULL;
        if (up.equals("VACANT") || up.contains("VACANT") || v.contains("空置")) return Room.RoomStatus.VACANT;
        if (up.equals("AVAILABLE") || up.contains("AVAILABLE") || v.contains("有空位") || v.contains("空位")) return Room.RoomStatus.AVAILABLE;
        // Fallback: if contains words indicating fullness
        try {
            //尝试数字判断（若保存为数字）
            int num = Integer.parseInt(v);
            if (num <= 0) return Room.RoomStatus.VACANT;
        } catch (Exception ignored) {}
        return Room.RoomStatus.AVAILABLE;
    }

    // 将 RoomStatus 转为数据库友好短码（或规范字符串），根据你的表的期望调整
    private String mapStatusToDb(Room.RoomStatus rs) {
        if (rs == null) return null;
        switch (rs) {
            case FULL: return "FULL";
            case AVAILABLE: return "AVAILABLE";
            case VACANT: return "VACANT";
            default: return rs.name();
        }
    }

    // 当从外部直接提供 status 文本（如 updateOccupancy 调用时），映射为 DB 期望值
    private String mapStatusToDbString(String statusText) {
        if (statusText == null) return null;
        String t = statusText.trim();
        if (t.contains("已住满") || t.equalsIgnoreCase("FULL") || t.contains("满")) return "FULL";
        if (t.contains("空置") || t.equalsIgnoreCase("VACANT")) return "VACANT";
        if (t.contains("有空位") || t.equalsIgnoreCase("AVAILABLE")) return "AVAILABLE";
        return t.length() <= 32 ? t : t.substring(0, 32);
    }

    // 辅助：截断字符串以避免写入过长导致数据库报错
    private String truncate(String s, int maxLen) {
        if (s == null) return null;
        s = s.trim();
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen);
    }
}
