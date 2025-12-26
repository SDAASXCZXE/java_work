package dao.impl;

import dao.AttendanceDao;
import model.Attendance;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 考勤 DAO 实现 - 物理删除版
 */
public class AttendanceDaoImpl implements AttendanceDao {

    /**
     * 将结果集映射为对象 - 使用提供的 8 参数构造方法
     */
    private Attendance mapResultSet(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String studentId = rs.getString("student_id");
        String roomNumber = rs.getString("room_number");
        String building = rs.getString("building");

        java.sql.Date sqlDate = rs.getDate("attendance_date");
        LocalDate date = (sqlDate != null) ? sqlDate.toLocalDate() : LocalDate.now();

        java.sql.Time sqlTime = rs.getTime("attendance_time");
        LocalTime time = (sqlTime != null) ? sqlTime.toLocalTime() : LocalTime.MIDNIGHT;

        String directionStr = rs.getString("direction");
        String statusStr = rs.getString("status");

        // 转换方向枚举
        Attendance.AttendanceDirection direction = "in".equalsIgnoreCase(directionStr) ?
                Attendance.AttendanceDirection.IN : Attendance.AttendanceDirection.OUT;

        // 转换状态枚举
        Attendance.AttendanceStatus status = Attendance.AttendanceStatus.NORMAL;
        if (statusStr != null) {
            status = switch (statusStr.toLowerCase()) {
                case "late" -> Attendance.AttendanceStatus.LATE;
                case "absent" -> Attendance.AttendanceStatus.ABSENT;
                case "leave" -> Attendance.AttendanceStatus.LEAVE;
                case "overdue" -> Attendance.AttendanceStatus.OVERDUE;
                default -> Attendance.AttendanceStatus.NORMAL;
            };
        }

        // 调用你提供的构造函数：(id, studentId, roomNumber, building, date, time, direction, status)
        return new Attendance(id, studentId, roomNumber, building, date, time, direction, status);
    }

    @Override
    public List<Attendance> findAll() {
        List<Attendance> list = new ArrayList<>();
        // 物理删除模式，不需要判断 deleted
        String sql = "SELECT * FROM attendance ORDER BY attendance_date DESC, attendance_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public List<Attendance> findByDate(LocalDate date) {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT * FROM attendance WHERE attendance_date = ? ORDER BY attendance_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public List<Attendance> findByStudent(String studentId) {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT * FROM attendance WHERE student_id = ? ORDER BY attendance_date DESC, attendance_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public boolean insert(Attendance attendance) {
        // 即使表中有 deleted 字段，物理删除模式下插入时设为 0
        String sql = "INSERT INTO attendance (id, student_id, room_number, building, attendance_date, " +
                "attendance_time, direction, status, create_time, update_time, deleted) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,0)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, attendance.getId());
            ps.setString(2, attendance.getStudentId());
            ps.setString(3, attendance.getRoomNumber());
            ps.setString(4, attendance.getBuilding());
            ps.setDate(5, java.sql.Date.valueOf(attendance.getAttendanceDate()));
            ps.setTime(6, java.sql.Time.valueOf(attendance.getAttendanceTime()));
            ps.setString(7, attendance.getDirection().getCode());
            ps.setString(8, attendance.getStatus().getCode());
            ps.setTimestamp(9, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(10, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean deleteById(String id) {
        // 彻底物理删除记录
        String sql = "DELETE FROM attendance WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
}