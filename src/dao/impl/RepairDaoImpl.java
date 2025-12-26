package dao.impl;

import dao.RepairDao;
import model.Repair;
import util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 报修 DAO JDBC 实现
 */
public class RepairDaoImpl implements RepairDao {

    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int cols = md.getColumnCount();
        for (int i = 1; i <= cols; i++) {
            if (columnName.equalsIgnoreCase(md.getColumnName(i))) return true;
        }
        return false;
    }

    @Override
    public List<Repair> findByStudentId(String studentId) {
        List<Repair> list = new ArrayList<>();
        String sql = "SELECT * FROM repair WHERE student_id = ? AND deleted = 0 ORDER BY submit_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("id");
                    String sid = rs.getString("student_id");
                    String roomNo = rs.getString("room_number");
                    String building = rs.getString("building");

                    LocalDateTime submitTime = null;
                    LocalDateTime updateTime = null;
                    try { Timestamp t = rs.getTimestamp("submit_time"); if (t != null) submitTime = t.toLocalDateTime(); } catch (Exception ignored) {}
                    try { Timestamp t2 = rs.getTimestamp("update_time"); if (t2 != null) updateTime = t2.toLocalDateTime(); } catch (Exception ignored) {}

                    // repair_type 存储为字符串 code 或枚举 name
                    String typeStr = null;
                    try { typeStr = rs.getString("repair_type"); } catch (Exception ignored) {}
                    Repair.RepairType type = Repair.RepairType.OTHER;
                    if (typeStr != null) {
                        for (Repair.RepairType rt : Repair.RepairType.values()) {
                            if (rt.getCode().equalsIgnoreCase(typeStr) || rt.name().equalsIgnoreCase(typeStr)) {
                                type = rt; break;
                            }
                        }
                    }

                    String desc = rs.getString("description");
                    String imgs = rs.getString("image_urls");
                    BigDecimal progress = null;
                    try { progress = rs.getBigDecimal("progress"); } catch (Exception ignored) {}

                    // status 字段
                    String statusStr = null;
                    try { statusStr = rs.getString("status"); } catch (Exception ignored) {}
                    Repair.RepairStatus status = Repair.RepairStatus.PENDING;
                    if (statusStr != null) {
                        for (Repair.RepairStatus rsd : Repair.RepairStatus.values()) {
                            if (rsd.getCode().equalsIgnoreCase(statusStr) || rsd.name().equalsIgnoreCase(statusStr)) {
                                status = rsd; break;
                            }
                        }
                    }

                    Repair r = new Repair(id, sid, roomNo, building, type, desc, imgs);
                    // 反射或直接设置可选字段
                    try { if (submitTime != null) {
                        java.lang.reflect.Field f = Repair.class.getDeclaredField("submitTime");
                        f.setAccessible(true);
                        f.set(r, submitTime);
                    } } catch (Exception ignored) {}

                    try { if (updateTime != null) {
                        java.lang.reflect.Field f = Repair.class.getDeclaredField("updateTime");
                        f.setAccessible(true);
                        f.set(r, updateTime);
                    } } catch (Exception ignored) {}

                    try { if (progress != null) {
                        java.lang.reflect.Field f = Repair.class.getDeclaredField("progress");
                        f.setAccessible(true);
                        f.set(r, progress);
                    } } catch (Exception ignored) {}

                    try { if (status != null) {
                        java.lang.reflect.Field f = Repair.class.getDeclaredField("status");
                        f.setAccessible(true);
                        f.set(r, status);
                    } } catch (Exception ignored) {}

                    try { if (hasColumn(rs, "remarks")) {
                        String remarks = rs.getString("remarks");
                        if (remarks != null) {
                            java.lang.reflect.Field f = Repair.class.getDeclaredField("remarks");
                            f.setAccessible(true);
                            f.set(r, remarks);
                        }
                    } } catch (Exception ignored) {}

                    list.add(r);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean addRepair(Repair repair) {
        String sql = "INSERT INTO repair (id, student_id, room_number, building, submit_time, update_time, repair_type, description, image_urls, progress, evaluation, fee, repairman, admin_no, status, remarks, deleted) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) return false;
            ps = conn.prepareStatement(sql);
            ps.setString(1, repair.getId());
            ps.setString(2, repair.getStudentId());
            ps.setString(3, repair.getRoomNumber());
            ps.setString(4, repair.getBuilding());
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            ps.setTimestamp(5, Timestamp.valueOf(now));
            ps.setTimestamp(6, Timestamp.valueOf(now));
            ps.setString(7, repair.getRepairType() != null ? repair.getRepairType().getCode() : Repair.RepairType.OTHER.getCode());
            ps.setString(8, repair.getDescription());
            ps.setString(9, repair.getImageUrls());
            ps.setBigDecimal(10, repair.getProgress() != null ? repair.getProgress() : BigDecimal.ZERO);
            ps.setBigDecimal(11, repair.getEvaluation().orElse(null));
            ps.setBigDecimal(12, repair.getFee().orElse(null));
            ps.setString(13, repair.getRepairman().orElse(null));
            ps.setString(14, repair.getAdminNo().orElse(null));
            ps.setString(15, repair.getStatus() != null ? repair.getStatus().getCode() : Repair.RepairStatus.PENDING.getCode());
            ps.setString(16, repair.getRemarks().orElse(null));
            ps.setInt(17, repair.isDeleted() ? 1 : 0);

            int c = ps.executeUpdate();
            return c > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
            DBUtil.close(conn);
        }
    }

    @Override
    public boolean deleteById(String id) {
        String sql = "DELETE FROM repair WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            int r = ps.executeUpdate();
            return r > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
