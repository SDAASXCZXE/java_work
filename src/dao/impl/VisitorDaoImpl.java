package dao.impl;

import dao.VisitorDao;
import model.Visitor;
import util.DBUtil;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VisitorDaoImpl implements VisitorDao {
    @Override
    public List<Visitor> findAll() {
        List<Visitor> list = new ArrayList<>();
        // 关键：只查询没有被逻辑删除的数据
        String sql = "SELECT * FROM visitor WHERE deleted = 0 ORDER BY arrive_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Visitor v = new Visitor();
                v.setId(rs.getString("id"));
                v.setName(rs.getString("name"));
                v.setIdType(rs.getString("id_type"));
                v.setIdNumber(rs.getString("id_number"));
                v.setPhone(rs.getString("phone"));
                v.setVisitRoom(rs.getString("visit_room"));
                v.setTargetStudent(rs.getString("target_student"));
                v.setReason(rs.getString("reason"));
                v.setArriveTime(rs.getTimestamp("arrive_time").toLocalDateTime());
                Timestamp lt = rs.getTimestamp("leave_time");
                if (lt != null) v.setLeaveTime(lt.toLocalDateTime());
                v.setRemarks(rs.getString("remarks"));
                list.add(v);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public boolean insert(Visitor v) {
        String sql = "INSERT INTO visitor (id, name, id_type, id_number, phone, visit_room, target_student, reason, arrive_time, leave_time, create_time, deleted) VALUES (?,?,?,?,?,?,?,?,?,?,?,0)";
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
            ps.setTimestamp(9, Timestamp.valueOf(v.getArriveTime()));
            ps.setTimestamp(10, v.getLeaveTime().map(Timestamp::valueOf).orElse(null));
            ps.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean deleteById(String id) {
        // 逻辑删除 SQL
        String sql = "UPDATE visitor SET deleted = 1 WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
}