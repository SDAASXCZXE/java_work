package dao.impl;

import dao.HolidayDao;
import model.Holiday;
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
public class HolidayDaoImpl implements HolidayDao {

    @Override
    public List<Holiday> findByStudentId(String studentId) {
        List<Holiday> list = new ArrayList<>();
        String sql = "SELECT * FROM holiday WHERE student_id = ? AND deleted = 0 ORDER BY apply_time DESC";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("id");
                    String sid = rs.getString("student_id");
                    String roomNo = rs.getString("room_number");
                    String building = rs.getString("building");

                    LocalDate start = null, end = null;
                    try { java.sql.Date d1 = rs.getDate("start_date"); if (d1 != null) start = d1.toLocalDate(); } catch (Exception ignored) {}
                    try { java.sql.Date d2 = rs.getDate("end_date"); if (d2 != null) end = d2.toLocalDate(); } catch (Exception ignored) {}

                    LocalDateTime registerTime = null;
                    LocalDateTime updateTime = null;
                    try { Timestamp t = rs.getTimestamp("apply_time"); if (t != null) registerTime = t.toLocalDateTime(); } catch (Exception ignored) {}
                    try { Timestamp t2 = rs.getTimestamp("update_time"); if (t2 != null) updateTime = t2.toLocalDateTime(); } catch (Exception ignored) {}

                    // status 字段（字符串 code 或 name）
                    String statusStr = null;
                    try { statusStr = rs.getString("status"); } catch (Exception ignored) {}

                    // remarks
                    String remarks = null;
                    try { remarks = rs.getString("remarks"); } catch (Exception ignored) {}

                    // destination / contact
                    String dest = null, contact = null, contactPhone = null;
                    try { dest = rs.getString("destination"); } catch (Exception ignored) {}
                    try { contact = rs.getString("contact_person"); } catch (Exception ignored) {}
                    try { contactPhone = rs.getString("contact_phone"); } catch (Exception ignored) {}

                    // 默认假设为离校类型（若需支持类型字段可在表中扩展）
                    Holiday.HolidayType ht = Holiday.HolidayType.LEAVE;
                    Holiday h = new Holiday(id, sid, roomNo, building, ht, start, end);

                    // 反射设置 registerTime/updateTime
                    try {
                        java.lang.reflect.Field f = Holiday.class.getDeclaredField("registerTime");
                        f.setAccessible(true);
                        if (registerTime != null) f.set(h, registerTime);
                    } catch (Exception ignored) {}
                    try {
                        java.lang.reflect.Field f = Holiday.class.getDeclaredField("updateTime");
                        f.setAccessible(true);
                        if (updateTime != null) f.set(h, updateTime);
                    } catch (Exception ignored) {}

                    // 设置 status 枚举
                    if (statusStr != null) {
                        try {
                            java.lang.reflect.Field f = Holiday.class.getDeclaredField("status");
                            f.setAccessible(true);
                            for (Holiday.HolidayStatus hs : Holiday.HolidayStatus.values()) {
                                if (hs.getCode().equalsIgnoreCase(statusStr) || hs.name().equalsIgnoreCase(statusStr)) { f.set(h, hs); break; }
                            }
                        } catch (Exception ignored) {}
                    }

                    // 设置可选字段通过 setter
                    try { if (dest != null) h.setDestination(dest); } catch (Exception ignored) {}
                    try { if (contact != null) h.setContactPerson(contact); } catch (Exception ignored) {}
                    try { if (contactPhone != null) h.setContactPhone(contactPhone); } catch (Exception ignored) {}
                    try { if (remarks != null) h.setRemarks(remarks); } catch (Exception ignored) {}

                    list.add(h);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean addHoliday(Holiday h) {
        String sql = "INSERT INTO holiday (id, student_id, room_number, building, start_date, end_date, actual_back_date, destination, contact_person, contact_phone, status, apply_time, update_time, remarks, deleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, h.getId());
            ps.setString(2, h.getStudentId());
            ps.setString(3, h.getRoomNumber());
            ps.setString(4, h.getBuilding());
            ps.setDate(5, h.getLeaveDate() != null ? java.sql.Date.valueOf(h.getLeaveDate()) : null);
            ps.setDate(6, h.getPlannedBackDate() != null ? java.sql.Date.valueOf(h.getPlannedBackDate()) : null);
            ps.setDate(7, h.getActualBackDate().isPresent() ? java.sql.Date.valueOf(h.getActualBackDate().get()) : null);
            ps.setString(8, h.getDestination().orElse(null));
            ps.setString(9, h.getContactPerson().orElse(null));
            ps.setString(10, h.getContactPhone().orElse(null));
            ps.setString(11, h.getStatus() != null ? h.getStatus().getCode() : Holiday.HolidayStatus.PENDING.getCode());

            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            ps.setTimestamp(12, Timestamp.valueOf(now));
            ps.setTimestamp(13, Timestamp.valueOf(now));
            ps.setString(14, h.getRemarks().orElse(null));
            ps.setInt(15, h.isDeleted() ? 1 : 0);

            int c = ps.executeUpdate();
            return c > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteById(String id) {
        String sql = "DELETE FROM holiday WHERE id = ?";
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

