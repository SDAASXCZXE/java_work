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
 * 假期登记 DAO 的 JDBC 实现
 */
public class HolidayDaoImpl implements HolidayDao {
    @Override
    public List<Holiday> findAll() {
        List<Holiday> list = new ArrayList<>();
        String sql = "SELECT * FROM holiday WHERE deleted = 0 ORDER BY register_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString("id");
                String studentId = rs.getString("student_id");
                String roomNumber = rs.getString("room_number");
                String building = rs.getString("building");
                String type = rs.getString("holiday_type");
                LocalDate leaveDate = rs.getDate("leave_date") == null ? null : rs.getDate("leave_date").toLocalDate();
                LocalDate plannedBack = rs.getDate("planned_back_date") == null ? null : rs.getDate("planned_back_date").toLocalDate();
                LocalDate actualBack = rs.getDate("actual_back_date") == null ? null : rs.getDate("actual_back_date").toLocalDate();
                String destination = rs.getString("destination");
                String contact = rs.getString("contact_person");
                String phone = rs.getString("contact_phone");
                Timestamp regTs = rs.getTimestamp("register_time");
                Timestamp updTs = rs.getTimestamp("update_time");
                String status = rs.getString("status");
                String remarks = rs.getString("remarks");

                Holiday.HolidayType htype = "back".equalsIgnoreCase(type) ? Holiday.HolidayType.BACK : Holiday.HolidayType.LEAVE;
                Holiday.HolidayStatus hstatus = Holiday.HolidayStatus.PENDING;
                if ("approved".equalsIgnoreCase(status)) hstatus = Holiday.HolidayStatus.APPROVED;
                else if ("rejected".equalsIgnoreCase(status)) hstatus = Holiday.HolidayStatus.REJECTED;
                else if ("completed".equalsIgnoreCase(status)) hstatus = Holiday.HolidayStatus.COMPLETED;
                else if ("overdue".equalsIgnoreCase(status)) hstatus = Holiday.HolidayStatus.OVERDUE;

                Holiday h = new Holiday(id, studentId, roomNumber, building, htype, leaveDate, plannedBack);
                if (actualBack != null) h.setActualBackDate(actualBack);
                h.setDestination(destination);
                h.setContactPerson(contact);
                h.setContactPhone(phone);
                h.setRemarks(remarks);
                if (regTs != null) {
                    // try reflect register_time/update_time if needed (Holiday class stores them internally)
                }
                // reflect status and deleted via methods
                switch (hstatus) {
                    case APPROVED:
                        h.approve();
                        break;
                    case REJECTED:
                        h.reject();
                        break;
                    case COMPLETED:
                        h.complete(actualBack);
                        break;
                    case OVERDUE:
                        h.markAsOverdue();
                        break;
                    default:
                        // keep pending
                        break;
                }

                list.add(h);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Holiday> findByStudentId(String studentId) {
        List<Holiday> list = new ArrayList<>();
        String sql = "SELECT * FROM holiday WHERE deleted = 0 AND student_id = ? ORDER BY register_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("id");
                    String roomNumber = rs.getString("room_number");
                    String building = rs.getString("building");
                    String type = rs.getString("holiday_type");
                    LocalDate leaveDate = rs.getDate("leave_date") == null ? null : rs.getDate("leave_date").toLocalDate();
                    LocalDate plannedBack = rs.getDate("planned_back_date") == null ? null : rs.getDate("planned_back_date").toLocalDate();
                    LocalDate actualBack = rs.getDate("actual_back_date") == null ? null : rs.getDate("actual_back_date").toLocalDate();
                    String destination = rs.getString("destination");
                    String contact = rs.getString("contact_person");
                    String phone = rs.getString("contact_phone");
                    String status = rs.getString("status");
                    String remarks = rs.getString("remarks");

                    Holiday.HolidayType htype = "back".equalsIgnoreCase(type) ? Holiday.HolidayType.BACK : Holiday.HolidayType.LEAVE;
                    Holiday.HolidayStatus hstatus = Holiday.HolidayStatus.PENDING;
                    if ("approved".equalsIgnoreCase(status)) hstatus = Holiday.HolidayStatus.APPROVED;
                    else if ("rejected".equalsIgnoreCase(status)) hstatus = Holiday.HolidayStatus.REJECTED;
                    else if ("completed".equalsIgnoreCase(status)) hstatus = Holiday.HolidayStatus.COMPLETED;
                    else if ("overdue".equalsIgnoreCase(status)) hstatus = Holiday.HolidayStatus.OVERDUE;

                    Holiday h = new Holiday(id, studentId, roomNumber, building, htype, leaveDate, plannedBack);
                    if (actualBack != null) h.setActualBackDate(actualBack);
                    h.setDestination(destination);
                    h.setContactPerson(contact);
                    h.setContactPhone(phone);
                    h.setRemarks(remarks);
                    switch (hstatus) {
                        case APPROVED:
                            h.approve();
                            break;
                        case REJECTED:
                            h.reject();
                            break;
                        case COMPLETED:
                            h.complete(actualBack);
                            break;
                        case OVERDUE:
                            h.markAsOverdue();
                            break;
                        default:
                            break;
                    }
                    list.add(h);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Holiday> findByStatus(Holiday.HolidayStatus status) {
        List<Holiday> list = new ArrayList<>();
        String sql = "SELECT * FROM holiday WHERE deleted = 0 AND status = ? ORDER BY register_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.getCode());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("id");
                    String studentId = rs.getString("student_id");
                    String roomNumber = rs.getString("room_number");
                    String building = rs.getString("building");
                    String type = rs.getString("holiday_type");
                    LocalDate leaveDate = rs.getDate("leave_date") == null ? null : rs.getDate("leave_date").toLocalDate();
                    LocalDate plannedBack = rs.getDate("planned_back_date") == null ? null : rs.getDate("planned_back_date").toLocalDate();
                    LocalDate actualBack = rs.getDate("actual_back_date") == null ? null : rs.getDate("actual_back_date").toLocalDate();
                    String destination = rs.getString("destination");
                    String contact = rs.getString("contact_person");
                    String phone = rs.getString("contact_phone");
                    String remarks = rs.getString("remarks");

                    Holiday.HolidayType htype = "back".equalsIgnoreCase(type) ? Holiday.HolidayType.BACK : Holiday.HolidayType.LEAVE;

                    Holiday h = new Holiday(id, studentId, roomNumber, building, htype, leaveDate, plannedBack);
                    if (actualBack != null) h.setActualBackDate(actualBack);
                    h.setDestination(destination);
                    h.setContactPerson(contact);
                    h.setContactPhone(phone);
                    h.setRemarks(remarks);
                    switch (status) {
                        case APPROVED:
                            h.approve();
                            break;
                        case REJECTED:
                            h.reject();
                            break;
                        case COMPLETED:
                            h.complete(actualBack);
                            break;
                        case OVERDUE:
                            h.markAsOverdue();
                            break;
                        default:
                            break;
                    }
                    list.add(h);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean insert(Holiday holiday) {
        String sql = "INSERT INTO holiday (id, student_id, room_number, building, holiday_type, leave_date, planned_back_date, actual_back_date, destination, contact_person, contact_phone, register_time, update_time, status, remarks, deleted) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,0)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, holiday.getId());
            ps.setString(2, holiday.getStudentId());
            ps.setString(3, holiday.getRoomNumber());
            ps.setString(4, holiday.getBuilding());
            ps.setString(5, holiday.getHolidayType().getCode());
            ps.setDate(6, holiday.getLeaveDate() == null ? null : java.sql.Date.valueOf(holiday.getLeaveDate()));
            ps.setDate(7, holiday.getPlannedBackDate() == null ? null : java.sql.Date.valueOf(holiday.getPlannedBackDate()));
            ps.setDate(8, null);
            ps.setString(9, holiday.getDestination().orElse(null));
            ps.setString(10, holiday.getContactPerson().orElse(null));
            ps.setString(11, holiday.getContactPhone().orElse(null));
            ps.setTimestamp(12, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(13, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(14, holiday.getStatus().getCode());
            ps.setString(15, holiday.getRemarks().orElse(null));

            int c = ps.executeUpdate();
            return c > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(Holiday holiday) {
        String sql = "UPDATE holiday SET room_number=?, building=?, holiday_type=?, leave_date=?, planned_back_date=?, actual_back_date=?, destination=?, contact_person=?, contact_phone=?, update_time=?, status=?, remarks=? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, holiday.getRoomNumber());
            ps.setString(2, holiday.getBuilding());
            ps.setString(3, holiday.getHolidayType().getCode());
            ps.setDate(4, holiday.getLeaveDate() == null ? null : java.sql.Date.valueOf(holiday.getLeaveDate()));
            ps.setDate(5, holiday.getPlannedBackDate() == null ? null : java.sql.Date.valueOf(holiday.getPlannedBackDate()));
            ps.setDate(6, holiday.getActualBackDate().orElse(null) == null ? null : java.sql.Date.valueOf(holiday.getActualBackDate().get()));
            ps.setString(7, holiday.getDestination().orElse(null));
            ps.setString(8, holiday.getContactPerson().orElse(null));
            ps.setString(9, holiday.getContactPhone().orElse(null));
            ps.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(11, holiday.getStatus().getCode());
            ps.setString(12, holiday.getRemarks().orElse(null));
            ps.setString(13, holiday.getId());

            int c = ps.executeUpdate();
            return c > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteById(String id) {
        String sql = "UPDATE holiday SET deleted = 1 WHERE id = ?";
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

