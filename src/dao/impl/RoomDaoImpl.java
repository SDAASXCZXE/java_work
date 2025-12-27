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
                String room_number = rs.getString("room_number");
                String building = rs.getString("building");
                String room_type = rs.getString("room_type");
                int total_beds = rs.getInt("total_beds");
                int occupied = rs.getInt("occupied");
                int available_beds = rs.getInt("available_beds");
                String monitor = rs.getString("monitor");
                String phone = rs.getString("phone");
                int hygiene_score = rs.getInt("hygiene_score");
                String status = rs.getString("status");
                String remarks = rs.getString("remarks");

                // 直接使用数据库中的字符串表示
                Room r = new Room(room_number, building, room_type, total_beds, occupied, available_beds, monitor, phone, hygiene_score, status, remarks);
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
            ps.setString(1, room.getRoomNumber());
            ps.setString(2, room.getBuilding());
            // 直接写入字符串
            ps.setString(3, room.getRoomType() == null ? null : room.getRoomType());
            ps.setInt(4, room.getTotalBeds());
            ps.setInt(5, room.getOccupied());
            ps.setInt(6, room.getAvailableBeds());
            ps.setString(7, room.getMonitor());
            ps.setString(8, room.getPhone());
            ps.setInt(9, room.getHygieneScore());
            ps.setString(10, room.getStatus() == null ? null : room.getStatus());
            ps.setString(11, room.getRemarks());

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

            String sql = "UPDATE room SET room_type=?, total_beds=?, occupied=?, " +
                    "available_beds=?, monitor=?, phone=?, hygiene_score=?, status=?, remarks=? " +
                    "WHERE room_number=? AND building=?";

            ps = conn.prepareStatement(sql);
            //ps.setString(1, room.getBuilding()); // building 不应该被修改，或者应该作为条件
            // 直接写入字符串
            ps.setString(1, room.getRoomType() == null ? null : room.getRoomType());
            ps.setInt(2, room.getTotalBeds());
            ps.setInt(3, room.getOccupied());
            ps.setInt(4, room.getAvailableBeds());
            ps.setString(5, room.getMonitor());
            ps.setString(6, room.getPhone());
            ps.setInt(7, room.getHygieneScore());
            ps.setString(8, room.getStatus() == null ? null : room.getStatus());
            ps.setString(9, room.getRemarks());
            ps.setString(10, room.getRoomNumber());
            ps.setString(11, room.getBuilding()); // 增加 building 作为条件

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

    // 实现接口方法：按楼栋+房间号删除
    @Override
    public boolean deleteByBuildingAndRoom(String building, String roomNumber) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) return false;
            String sql = "DELETE FROM room WHERE building = ? AND room_number = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, building);
            ps.setString(2, roomNumber);
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

    // 兼容旧代码：如果调用方只传 roomNumber，我们将删除所有楼栋中该房号（不推荐）
    public boolean deleteByRoomNumber(String roomNumber) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) return false;
            String sql = "DELETE FROM room WHERE room_number = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, roomNumber);
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
            ps.setString(3, status);
            ps.setString(4, roomNumber);
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

    // 修复后的方法，支持联合查询
    public boolean updateOccupancy(String building, String roomNumber, int occupied, int available, String status) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) return false;
            String sql = "UPDATE room SET occupied = ?, available_beds = ?, status = ? WHERE building = ? AND room_number = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, occupied);
            ps.setInt(2, available);
            ps.setString(3, status);
            ps.setString(4, building);
            ps.setString(5, roomNumber);
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
    public boolean existsByRoomNumber(String roomNumber) {
        String sql = "SELECT COUNT(1) FROM room WHERE room_number = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 新增：联合检查方法
    public boolean existsByBuildingAndRoom(String building, String roomNumber) {
        String sql = "SELECT COUNT(1) FROM room WHERE building = ? AND room_number = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, building);
            ps.setString(2, roomNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}

