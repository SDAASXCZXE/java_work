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
 * 考勤 DAO 的 JDBC 实现 - 物理删除优化版
 */
public class AttendanceDaoImpl implements AttendanceDao {

    /**
     * 查询所有记录
     */
    @Override
    public List<Attendance> findAll() {
        List<Attendance> list = new ArrayList<>();
        // 物理删除模式下，不再需要 deleted = 0 条件
        String sql = "SELECT * FROM attendance ORDER BY attendance_date DESC, attendance_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToAttendance(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 按日期查询记录
     */
    @Override
    public List<Attendance> findByDate(LocalDate date) {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT * FROM attendance WHERE attendance_date = ? ORDER BY attendance_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAttendance(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 按学号查询记录
     */
    @Override
    public List<Attendance> findByStudent(String studentId) {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT * FROM attendance WHERE student_id = ? ORDER BY attendance_date DESC, attendance_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAttendance(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 插入新记录
     */
    @Override
    public boolean insert(Attendance attendance) {
        // 虽然执行物理删除，但如果数据库表结构保留了 deleted 字段，插入时设为 0 以防万一
        String sql = "INSERT INTO attendance (id, student_id, room_number, building, attendance_date, " +
                "attendance_time, direction, status, create_time, update_time, device_id, remarks, deleted) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,0)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, attendance.getId());
            ps.setString(2, attendance.getStudentId());
            ps.setString(3, attendance.getRoomNumber());
            ps.setString(4, attendance.getBuilding());

            LocalDate ad = attendance.getAttendanceDate() == null ? LocalDate.now() : attendance.getAttendanceDate();
            LocalTime at = attendance.getAttendanceTime() == null ? LocalTime.now() : attendance.getAttendanceTime();

            ps.setDate(5, java.sql.Date.valueOf(ad));
            ps.setTime(6, java.sql.Time.valueOf(at));
            ps.setString(7, attendance.getDirection().getCode());
            ps.setString(8, attendance.getStatus().getCode());
            ps.setTimestamp(9, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(10, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(11, attendance.getDeviceId().orElse(null));
            ps.setString(12, attendance.getRemarks().orElse(null));

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 彻底物理删除记录
     */
    @Override
    public boolean deleteById(String id) {
        // 修复：添加了缺失的 FROM 关键字
        String sql = "DELETE FROM attendance WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 私有辅助方法：封装结果集映射逻辑，提高复用性
     */
    private Attendance mapResultSetToAttendance(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String studentId = rs.getString("student_id");
        String roomNumber = rs.getString("room_number");
        String building = rs.getString("building");

        java.sql.Date sqlDate = rs.getDate("attendance_date");
        LocalDate date = (sqlDate != null) ? sqlDate.toLocalDate() : LocalDate.now();

        java.sql.Time sqlTime = rs.getTime("attendance_time");
        LocalTime time = (sqlTime != null) ? sqlTime.toLocalTime() : LocalTime.MIDNIGHT;

        String direction = rs.getString("direction");
        String status = rs.getString("status");

        // 转换枚举
        Attendance.AttendanceDirection dir = "out".equalsIgnoreCase(direction) ?
                Attendance.AttendanceDirection.OUT : Attendance.AttendanceDirection.IN;

        Attendance.AttendanceStatus st = Attendance.AttendanceStatus.NORMAL;
        if (status != null) {
            st = switch (status.toLowerCase()) {
                case "late" -> Attendance.AttendanceStatus.LATE;
                case "absent" -> Attendance.AttendanceStatus.ABSENT;
                case "leave" -> Attendance.AttendanceStatus.LEAVE;
                case "overdue" -> Attendance.AttendanceStatus.OVERDUE;
                default -> Attendance.AttendanceStatus.NORMAL;
            };
        }

        Attendance a = new Attendance(id, studentId, roomNumber, building, date, time, dir, st);

        // 补全可选字段
        a.setRemarks(rs.getString("remarks"));
        // 如果实体类有 createTime 字段也可在此 set

        return a;
    }
}