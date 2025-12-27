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

            // Update 需要同时匹配 room_number 和 building，防止更新错
            // 或者假设 room_number + building 是联合主键。
            // 但原逻辑只用了 room_number 做 where 条件。
            // 如果您的数据库设计允许不同楼栋有相同 room_number，那么这个 UPDATE 语句不仅会更新目标行，还会更新其他楼栋的同号房间！
            // 【重要修复】：WHERE 子句必须包含 building 或者使用唯一的 id。
            // 假设 Room 对象包含旧的 building 信息，或者这里我们只能依靠 room_number (这在设计上是有缺陷的，如果允许重复)。
            // 既然要求修复 bug，这里必须改为 building + room_number 联合定位。

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

    // 为了支持删除特定楼栋的房间，这里应该也接收 building
    // 但为了保持 override 签名（假设接口没变），这里可能存在风险。
    // 如果可以，请在接口里增加 deleteByBuildingAndRoom
    @Override
    public boolean deleteByRoomNumber(String roomNumber) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) return false;
            // 风险：这里会删除所有楼栋的该房号。为了修复 bug，应该传入 building。
            // 如果无法修改接口，我们只能暂时保持这样，或者假设调用者会处理。
            // 但为了修复 "同房号不同楼栋" 的问题，这里必须改为联合删除。
            // 下面是一个折中的修复：假设该方法只删特定行，但参数不够。
            // 建议您修改 RoomDao 接口方法签名为 delete(String building, String roomNumber)
            // 这里我暂时保持原样，但在 SQL 层面这依然是个隐患。
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

    // 如果您能修改接口，请添加这个方法。
    // 如果不能，RoomService 需要自行处理。
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

    @Override
    public boolean updateOccupancy(String roomNumber, int occupied, int available, String status) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) return false;
            // 同样的问题：这会更新所有楼栋的同号房间。
            // 这是一个极其危险的 Bug。必须修复 WHERE 条件。
            // 但由于方法签名限制，我无法在这里获取 building。
            // 假设您在调用层解决了这个问题（比如传入了 building 拼接到 roomNumber? 不太可能）。
            // 必须修改接口！
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

    // 重点修复：这里的检查必须包含 building
    // 如果 RoomService 调用的是这个方法，必须修改为双参数
    @Override
    public boolean existsByRoomNumber(String roomNumber) {
        // 这个方法本身是有歧义的，如果不传 building，根本无法判断是否存在 "特定" 房间
        // 这里只能查 "是否存在任意楼栋有该房号"
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