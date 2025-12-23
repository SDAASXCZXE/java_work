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

                // Convert string values to enums. 先尝试按枚举名解析（英文），若失败再按中文描述解析（兼容旧数据）
                Room.RoomType roomType = null;
                if (room_type != null) {
                    try {
                        roomType = Room.RoomType.valueOf(room_type.toUpperCase());
                    } catch (Exception ex) {
                        try {
                            roomType = Room.RoomType.fromDescription(room_type);
                        } catch (Exception ex2) {
                            ex2.printStackTrace();
                            roomType = Room.RoomType.QUAD;
                        }
                    }
                } else {
                    roomType = Room.RoomType.QUAD;
                }

                Room.RoomStatus roomStatus = null;
                if (status != null) {
                    try {
                        roomStatus = Room.RoomStatus.valueOf(status.toUpperCase());
                    } catch (Exception ex) {
                        try {
                            roomStatus = Room.RoomStatus.fromDescription(status);
                        } catch (Exception ex2) {
                            ex2.printStackTrace();
                            roomStatus = Room.RoomStatus.VACANT;
                        }
                    }
                } else {
                    roomStatus = Room.RoomStatus.VACANT;
                }

                Room r = new Room(room_number, building, roomType, total_beds, occupied, available_beds, monitor, phone, hygiene_score, roomStatus, remarks);
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
            // 写入时使用枚举名（英文），避免向 ENUM 类型字段写入中文描述导致截断/警告
            ps.setString(3, room.getRoomType() == null ? null : room.getRoomType().name());
            ps.setInt(4, room.getTotalBeds());
            ps.setInt(5, room.getOccupied());
            ps.setInt(6, room.getAvailableBeds());
            ps.setString(7, room.getMonitor());
            ps.setString(8, room.getPhone());
            ps.setInt(9, room.getHygieneScore());
            ps.setString(10, room.getStatus() == null ? null : room.getStatus().name());
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

            String sql = "UPDATE room SET building=?, room_type=?, total_beds=?, occupied=?, " +
                    "available_beds=?, monitor=?, phone=?, hygiene_score=?, status=?, remarks=? " +
                    "WHERE room_number=?";

            ps = conn.prepareStatement(sql);
            ps.setString(1, room.getBuilding());
            // 写入枚举名
            ps.setString(2, room.getRoomType() == null ? null : room.getRoomType().name());
            ps.setInt(3, room.getTotalBeds());
            ps.setInt(4, room.getOccupied());
            ps.setInt(5, room.getAvailableBeds());
            ps.setString(6, room.getMonitor());
            ps.setString(7, room.getPhone());
            ps.setInt(8, room.getHygieneScore());
            ps.setString(9, room.getStatus() == null ? null : room.getStatus().name());
            ps.setString(10, room.getRemarks());
            ps.setString(11, room.getRoomNumber());

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
            // status 由 RoomPanel 传入中文描述或英文名；优先写入英文名以兼容 ENUM
            String statusToWrite = status;
            if (statusToWrite != null) {
                // 若传入是中文描述，尝试转换为枚举名
                try {
                    Room.RoomStatus rs = Room.RoomStatus.fromDescription(statusToWrite);
                    statusToWrite = rs.name();
                } catch (Exception ignored) {
                    // 不是中文描述，则假设已经是枚举名或其他可写格式
                }
            }
            ps.setString(3, statusToWrite);
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
}
