package service.impl;

import dao.VisitorDao;
import dao.impl.VisitorDaoImpl;
import model.Visitor;
import service.VisitorService;
import util.DBUtil;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public class VisitorServiceImpl implements VisitorService {
    private VisitorDao dao = new VisitorDaoImpl();

    @Override
    public List<Visitor> listAll() { return dao.findAll(); }

    @Override
    public boolean addVisitor(Visitor v) {
        if (v.getArriveTime() == null) v.setArriveTime(LocalDateTime.now());
        return dao.insert(v);
    }

    @Override
    public boolean removeById(String id) { return dao.deleteById(id); }

    @Override
    public boolean updateVisitor(Visitor v) {
        String sql = "UPDATE visitor SET name=?, id_type=?, id_number=?, phone=?, visit_room=?, target_student=?, reason=?, leave_time=?, remarks=? WHERE id=? AND deleted=0";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, v.getName());
            ps.setString(2, v.getIdType());
            ps.setString(3, v.getIdNumber());
            ps.setString(4, v.getPhone());
            ps.setString(5, v.getVisitRoom());
            ps.setString(6, v.getTargetStudent());
            ps.setString(7, v.getReason());
            ps.setTimestamp(8, v.getLeaveTime().map(Timestamp::valueOf).orElse(null));
            ps.setString(9, v.getRemarks());
            ps.setString(10, v.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
}