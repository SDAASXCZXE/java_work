package dao.impl;

import dao.LeaveDao;
import model.LeaveRequest;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 请假 DAO JDBC 实现
 */
public class LeaveDaoImpl implements LeaveDao {

    @Override
    public List<LeaveRequest> findByStudentId(String studentId) {
        List<LeaveRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM leave_request WHERE student_id = ? AND deleted = 0 ORDER BY apply_time DESC";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("id");
                    String sid = rs.getString("student_id");
                    String roomNo = rs.getString("room_number");
                    String building = rs.getString("building");
                    String leaveType = rs.getString("leave_type");

                    LocalDate start = null, end = null;
                    try { java.sql.Date d1 = rs.getDate("start_date"); if (d1 != null) start = d1.toLocalDate(); } catch (Exception ignored) {}
                    try { java.sql.Date d2 = rs.getDate("end_date"); if (d2 != null) end = d2.toLocalDate(); } catch (Exception ignored) {}

                    LocalDateTime applyTime = null;
                    LocalDateTime updateTime = null;
                    try { Timestamp t = rs.getTimestamp("apply_time"); if (t != null) applyTime = t.toLocalDateTime(); } catch (Exception ignored) {}
                    try { Timestamp t2 = rs.getTimestamp("update_time"); if (t2 != null) updateTime = t2.toLocalDateTime(); } catch (Exception ignored) {}

                    String status = null;
                    try { status = rs.getString("status"); } catch (Exception ignored) {}

                    String reason = null, contact = null, contactPhone = null, remarks = null;
                    try { reason = rs.getString("reason"); } catch (Exception ignored) {}
                    try { contact = rs.getString("contact_person"); } catch (Exception ignored) {}
                    try { contactPhone = rs.getString("contact_phone"); } catch (Exception ignored) {}
                    try { remarks = rs.getString("remarks"); } catch (Exception ignored) {}

                    LeaveRequest lr = new LeaveRequest(id, sid, roomNo, building, leaveType, start, end);
                    try { java.lang.reflect.Field f = LeaveRequest.class.getDeclaredField("applyTime"); f.setAccessible(true); if (applyTime != null) f.set(lr, applyTime);} catch (Exception ignored) {}
                    try { java.lang.reflect.Field f = LeaveRequest.class.getDeclaredField("updateTime"); f.setAccessible(true); if (updateTime != null) f.set(lr, updateTime);} catch (Exception ignored) {}
                    try { if (status != null) { java.lang.reflect.Field f = LeaveRequest.class.getDeclaredField("status"); f.setAccessible(true); f.set(lr, status); } } catch (Exception ignored) {}
                    try { if (reason != null) lr.setReason(reason); } catch (Exception ignored) {}
                    try { if (contact != null) lr.setContactPerson(contact); } catch (Exception ignored) {}
                    try { if (contactPhone != null) lr.setContactPhone(contactPhone); } catch (Exception ignored) {}
                    try { if (remarks != null) lr.setRemarks(remarks); } catch (Exception ignored) {}

                    list.add(lr);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean addLeave(LeaveRequest l) {
        String sql = "INSERT INTO leave_request (id, student_id, room_number, building, leave_type, start_date, end_date, reason, contact_person, contact_phone, status, apply_time, update_time, remarks, deleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, l.getId());
            ps.setString(2, l.getStudentId());
            ps.setString(3, l.getRoomNumber());
            ps.setString(4, l.getBuilding());
            ps.setString(5, l.getLeaveType());
            ps.setDate(6, l.getStartDate() != null ? java.sql.Date.valueOf(l.getStartDate()) : null);
            ps.setDate(7, l.getEndDate() != null ? java.sql.Date.valueOf(l.getEndDate()) : null);
            ps.setString(8, l.getReason().orElse(null));
            ps.setString(9, l.getContactPerson().orElse(null));
            ps.setString(10, l.getContactPhone().orElse(null));
            ps.setString(11, l.getStatus());

            LocalDateTime now = LocalDateTime.now();
            ps.setTimestamp(12, Timestamp.valueOf(now));
            ps.setTimestamp(13, Timestamp.valueOf(now));
            ps.setString(14, l.getRemarks().orElse(null));
            ps.setInt(15, l.isDeleted() ? 1 : 0);

            int c = ps.executeUpdate();
            return c > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteById(String id) {
        String sql = "DELETE FROM leave_request WHERE id = ?";
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

