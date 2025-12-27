package dao.impl;

import dao.RoomChangeDao;
import model.RoomChange;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 宿舍调整 DAO JDBC 实现
 */
public class RoomChangeDaoImpl implements RoomChangeDao {

    @Override
    public List<RoomChange> findByStudentId(String studentId) {
        List<RoomChange> list = new ArrayList<>();
        String sql = "SELECT * FROM room_change WHERE student_id = ? AND deleted = 0 ORDER BY apply_time DESC";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("id");
                    String sid = rs.getString("student_id");
                    String oldBuilding = rs.getString("old_building");
                    String oldRoom = rs.getString("old_room_number");
                    String newBuilding = rs.getString("new_building");
                    String newRoom = rs.getString("new_room_number");
                    String reason = rs.getString("reason");

                    LocalDateTime applyTime = null, updateTime = null, approveTime = null;
                    try { Timestamp t = rs.getTimestamp("apply_time"); if (t != null) applyTime = t.toLocalDateTime(); } catch (Exception ignored) {}
                    try { Timestamp t = rs.getTimestamp("update_time"); if (t != null) updateTime = t.toLocalDateTime(); } catch (Exception ignored) {}
                    try { Timestamp t = rs.getTimestamp("approve_time"); if (t != null) approveTime = t.toLocalDateTime(); } catch (Exception ignored) {}

                    String status = rs.getString("status");
                    String approver = rs.getString("approver");
                    String approveComment = rs.getString("approve_comment");

                    RoomChange rc = new RoomChange(id, sid, oldBuilding, oldRoom, newBuilding, newRoom, reason);
                    try { java.lang.reflect.Field f = RoomChange.class.getDeclaredField("applyTime"); f.setAccessible(true); if (applyTime != null) f.set(rc, applyTime);} catch (Exception ignored) {}
                    try { java.lang.reflect.Field f = RoomChange.class.getDeclaredField("updateTime"); f.setAccessible(true); if (updateTime != null) f.set(rc, updateTime);} catch (Exception ignored) {}
                    try { java.lang.reflect.Field f = RoomChange.class.getDeclaredField("approveTime"); f.setAccessible(true); if (approveTime != null) f.set(rc, approveTime);} catch (Exception ignored) {}
                    try { if (status != null) { java.lang.reflect.Field f = RoomChange.class.getDeclaredField("status"); f.setAccessible(true); // try to find matching enum by code or name
                            for (RoomChange.RoomChangeStatus s : RoomChange.RoomChangeStatus.values()) {
                                if (s.getCode().equalsIgnoreCase(status) || s.name().equalsIgnoreCase(status)) { f.set(rc, s); break; }
                            }
                        }
                    } catch (Exception ignored) {}
                    try { if (approver != null) { java.lang.reflect.Field f = RoomChange.class.getDeclaredField("approver"); f.setAccessible(true); f.set(rc, approver);} } catch (Exception ignored) {}
                    try { if (approveComment != null) { java.lang.reflect.Field f = RoomChange.class.getDeclaredField("approveComment"); f.setAccessible(true); f.set(rc, approveComment);} } catch (Exception ignored) {}
                    try { if (reason != null) rc.setReason(reason); } catch (Exception ignored) {}

                    list.add(rc);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean addRoomChange(RoomChange rc) {
        String sql = "INSERT INTO room_change (id, student_id, old_building, old_room_number, new_building, new_room_number, reason, status, apply_time, update_time, approver, approve_time, approve_comment, deleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rc.getId());
            ps.setString(2, rc.getStudentId());
            ps.setString(3, rc.getOldBuilding());
            ps.setString(4, rc.getOldRoomNumber());
            ps.setString(5, rc.getNewBuilding());
            ps.setString(6, rc.getNewRoomNumber());
            ps.setString(7, rc.getReason());
            ps.setString(8, rc.getStatus() != null ? rc.getStatus().getCode() : RoomChange.RoomChangeStatus.PENDING.getCode());

            LocalDateTime now = LocalDateTime.now();
            ps.setTimestamp(9, Timestamp.valueOf(now));
            ps.setTimestamp(10, Timestamp.valueOf(now));
            ps.setString(11, rc.getApprover().orElse(null));
            ps.setTimestamp(12, rc.getApproveTime().isPresent() ? Timestamp.valueOf(rc.getApproveTime().get()) : null);
            ps.setString(13, rc.getApproveComment().orElse(null));
            ps.setInt(14, rc.isDeleted() ? 1 : 0);

            int c = ps.executeUpdate();
            return c > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteById(String id) {
        String sql = "DELETE FROM room_change WHERE id = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            int r = ps.executeUpdate();
            return r > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

