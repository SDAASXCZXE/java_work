package dao.impl;

import dao.AttendanceDao;
import model.Attendance;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 考勤 DAO 的 JDBC 实现
 */
public class AttendanceDaoImpl implements AttendanceDao {
    @Override
    public List<Attendance> findAll() {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT * FROM attendance WHERE deleted = 0 ORDER BY create_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString("id");
                String studentId = rs.getString("student_id");
                String roomNumber = rs.getString("room_number");
                String building = rs.getString("building");
                java.sql.Date sqlDate = rs.getDate("attendance_date");
                LocalDate date = sqlDate == null ? LocalDate.now() : sqlDate.toLocalDate();
                java.sql.Time sqlTime = rs.getTime("attendance_time");
                LocalTime time = sqlTime == null ? LocalTime.MIDNIGHT : sqlTime.toLocalTime();
                String direction = rs.getString("direction");
                String status = rs.getString("status");

                Attendance.AttendanceDirection dir = "in".equalsIgnoreCase(direction) ? Attendance.AttendanceDirection.IN : Attendance.AttendanceDirection.OUT;
                Attendance.AttendanceStatus st = Attendance.AttendanceStatus.NORMAL;
                if ("late".equalsIgnoreCase(status)) st = Attendance.AttendanceStatus.LATE;
                else if ("absent".equalsIgnoreCase(status)) st = Attendance.AttendanceStatus.ABSENT;
                else if ("leave".equalsIgnoreCase(status)) st = Attendance.AttendanceStatus.LEAVE;
                else if ("overdue".equalsIgnoreCase(status)) st = Attendance.AttendanceStatus.OVERDUE;

                // 使用数据库的 id 构造对象，避免生成新的 id 覆盖
                Attendance a = new Attendance(id, studentId, roomNumber, building, date, time, dir, st);
                list.add(a);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Attendance> findByDate(LocalDate date) {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT * FROM attendance WHERE deleted = 0 AND attendance_date = ? ORDER BY create_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("id");
                    String studentId = rs.getString("student_id");
                    String roomNumber = rs.getString("room_number");
                    String building = rs.getString("building");
                    java.sql.Date sqlDate = rs.getDate("attendance_date");
                    LocalDate d = sqlDate == null ? date : sqlDate.toLocalDate();
                    java.sql.Time sqlTime = rs.getTime("attendance_time");
                    LocalTime time = sqlTime == null ? LocalTime.MIDNIGHT : sqlTime.toLocalTime();
                    String direction = rs.getString("direction");
                    String status = rs.getString("status");

                    Attendance.AttendanceDirection dir = "in".equalsIgnoreCase(direction) ? Attendance.AttendanceDirection.IN : Attendance.AttendanceDirection.OUT;
                    Attendance.AttendanceStatus st = Attendance.AttendanceStatus.NORMAL;
                    if ("late".equalsIgnoreCase(status)) st = Attendance.AttendanceStatus.LATE;
                    else if ("absent".equalsIgnoreCase(status)) st = Attendance.AttendanceStatus.ABSENT;
                    else if ("leave".equalsIgnoreCase(status)) st = Attendance.AttendanceStatus.LEAVE;
                    else if ("overdue".equalsIgnoreCase(status)) st = Attendance.AttendanceStatus.OVERDUE;

                    Attendance a = new Attendance(id, studentId, roomNumber, building, d, time, dir, st);
                    list.add(a);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Attendance> findByStudent(String studentId) {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT * FROM attendance WHERE deleted = 0 AND student_id = ? ORDER BY attendance_date DESC, create_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("id");
                    String sid = rs.getString("student_id");
                    String roomNumber = rs.getString("room_number");
                    String building = rs.getString("building");
                    java.sql.Date sqlDate = rs.getDate("attendance_date");
                    LocalDate d = sqlDate == null ? LocalDate.now() : sqlDate.toLocalDate();
                    java.sql.Time sqlTime = rs.getTime("attendance_time");
                    LocalTime time = sqlTime == null ? LocalTime.MIDNIGHT : sqlTime.toLocalTime();
                    String direction = rs.getString("direction");
                    String status = rs.getString("status");

                    Attendance.AttendanceDirection dir = "in".equalsIgnoreCase(direction) ? Attendance.AttendanceDirection.IN : Attendance.AttendanceDirection.OUT;
                    Attendance.AttendanceStatus st = Attendance.AttendanceStatus.NORMAL;
                    if ("late".equalsIgnoreCase(status)) st = Attendance.AttendanceStatus.LATE;
                    else if ("absent".equalsIgnoreCase(status)) st = Attendance.AttendanceStatus.ABSENT;
                    else if ("leave".equalsIgnoreCase(status)) st = Attendance.AttendanceStatus.LEAVE;
                    else if ("overdue".equalsIgnoreCase(status)) st = Attendance.AttendanceStatus.OVERDUE;

                    Attendance a = new Attendance(id, sid, roomNumber, building, d, time, dir, st);
                    list.add(a);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean insert(Attendance attendance) {
        String sql = "INSERT INTO attendance (id, student_id, room_number, building, attendance_date, attendance_time, direction, status, create_time, update_time, device_id, remarks, deleted) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,0)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, attendance.getId());
            ps.setString(2, attendance.getStudentId());
            ps.setString(3, attendance.getRoomNumber());
            ps.setString(4, attendance.getBuilding());

            // 处理可能为 null 的日期/时间
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

            int c = ps.executeUpdate();
            return c > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteById(String id) {
        String sql = "UPDATE attendance SET deleted = 1 WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            int c = ps.executeUpdate();
            return c > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
