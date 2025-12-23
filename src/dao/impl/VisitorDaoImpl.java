package dao.impl;

import dao.VisitorDao;
import model.Visitor;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Visitor DAO JDBC 实现
 */
public class VisitorDaoImpl implements VisitorDao {
    @Override
    public List<Visitor> findAll() {
        List<Visitor> list = new ArrayList<>();
        String sql = "SELECT * FROM visitor WHERE deleted = 0 ORDER BY create_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                String idType = rs.getString("id_type");
                String idNumber = rs.getString("id_number");
                String phone = rs.getString("phone");
                String visitRoom = rs.getString("visit_room");
                String targetStudent = rs.getString("target_student");
                String reason = rs.getString("reason");
                Timestamp arriveTs = rs.getTimestamp("arrive_time");
                Timestamp leaveTs = rs.getTimestamp("leave_time");
                Timestamp createTs = rs.getTimestamp("create_time");
                Timestamp updateTs = rs.getTimestamp("update_time");
                String remarks = rs.getString("remarks");

                Visitor v = new Visitor();
                v.setId(id);
                v.setName(name);
                v.setIdType(idType);
                v.setIdNumber(idNumber);
                v.setPhone(phone);
                v.setVisitRoom(visitRoom);
                v.setTargetStudent(targetStudent);
                v.setReason(reason);
                if (arriveTs != null) v.setArriveTime(arriveTs.toLocalDateTime());
                if (leaveTs != null) v.setLeaveTime(leaveTs.toLocalDateTime());
                if (createTs != null) v.setCreateTime(createTs.toLocalDateTime());
                if (updateTs != null) v.setUpdateTime(updateTs.toLocalDateTime());
                v.setRemarks(remarks);
                v.setDeleted(false);
                list.add(v);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean insert(Visitor v) {
        String sql = "INSERT INTO visitor (id, name, id_type, id_number, phone, visit_room, target_student, reason, arrive_time, leave_time, create_time, update_time, remarks, deleted) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,0)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, v.getId());
            ps.setString(2, v.getName());
            ps.setString(3, v.getIdType());
            ps.setString(4, v.getIdNumber());
            ps.setString(5, v.getPhone());
            ps.setString(6, v.getVisitRoom());
            ps.setString(7, v.getTargetStudent());
            ps.setString(8, v.getReason());
            ps.setTimestamp(9, v.getArriveTime() == null ? Timestamp.valueOf(LocalDateTime.now()) : Timestamp.valueOf(v.getArriveTime()));
            ps.setTimestamp(10, v.getLeaveTime().isPresent() ? Timestamp.valueOf(v.getLeaveTime().get()) : null);
            ps.setTimestamp(11, v.getCreateTime() == null ? Timestamp.valueOf(LocalDateTime.now()) : Timestamp.valueOf(v.getCreateTime()));
            ps.setTimestamp(12, v.getUpdateTime() == null ? Timestamp.valueOf(LocalDateTime.now()) : Timestamp.valueOf(v.getUpdateTime()));
            ps.setString(13, v.getRemarks());

            int c = ps.executeUpdate();
            return c > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteById(String id) {
        String sql = "UPDATE visitor SET deleted = 1 WHERE id = ?";
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

