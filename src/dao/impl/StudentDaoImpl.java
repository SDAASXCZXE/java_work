package dao.impl;

import dao.StudentDao;
import model.Student;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 学生 DAO 实现类（JDBC 实现）
 */
public class StudentDaoImpl implements StudentDao {

    // 判断 ResultSet 中是否包含指定列名（忽略大小写）
    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int cols = md.getColumnCount();
        for (int i = 1; i <= cols; i++) {
            if (columnName.equalsIgnoreCase(md.getColumnName(i))) return true;
        }
        return false;
    }

    // 查询所有学生
    @Override
    public List<Student> findAll() {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM student";

        try (
                Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                Student s = new Student();
                s.setSno(rs.getString("sno"));
                s.setName(rs.getString("name"));
                s.setGender(rs.getString("gender"));
                s.setCollege(rs.getString("college"));
                s.setMajor(rs.getString("major"));
                s.setGrade(rs.getString("grade"));
                s.setClazz(rs.getString("class"));
                s.setPhone(rs.getString("phone"));
                java.sql.Date inDate = null;
                try { inDate = rs.getDate("in_date"); } catch (Exception ignored) {}
                if (inDate != null) s.setInDate(inDate.toLocalDate());

                // 可选列：building, room_number, bed_number
                try {
                    if (hasColumn(rs, "building")) s.setBuilding(rs.getString("building"));
                } catch (Exception ignored) {}
                try {
                    if (hasColumn(rs, "room_number")) s.setRoomNumber(rs.getString("room_number"));
                } catch (Exception ignored) {}
                try {
                    if (hasColumn(rs, "bed_number")) s.setBedNumber(rs.getInt("bed_number"));
                } catch (Exception ignored) {}

                list.add(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 添加学生
    @Override
    public boolean addStudent(Student s) {
        String sql = "INSERT INTO student " +
                "(sno, name, gender, college, major, grade, class, phone, in_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) return false;
            ps = conn.prepareStatement(sql);
            ps.setString(1, s.getSno());
            ps.setString(2, s.getName());
            ps.setString(3, s.getGender());
            ps.setString(4, s.getCollege());
            ps.setString(5, s.getMajor());
            ps.setString(6, s.getGrade());
            ps.setString(7, s.getClazz());
            ps.setString(8, s.getPhone());
            if (s.getInDate() != null) ps.setDate(9, java.sql.Date.valueOf(s.getInDate()));
            else ps.setDate(9, java.sql.Date.valueOf(java.time.LocalDate.now()));

            int c = ps.executeUpdate();
            return c > 0;
        } catch (SQLException ex) {
            // 如果是约束违规（例如学号唯一冲突），SQLState 通常以 '23' 开头
            String sqlState = ex.getSQLState();
            if (sqlState != null && sqlState.startsWith("23")) {
                // 违反约束（重复键/唯一索引），静默处理返回 false
                return false;
            }
            // 其它 SQL 错误，打印堆栈以便调试
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

    // 根据 id 删除学生
    @Override
    public boolean deleteStudent(String sno) {
        String sql = "DELETE FROM student WHERE sno = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) return false;
            pstmt.setString(1, sno);
            int rows = pstmt.executeUpdate();

            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 更新学生信息
    @Override
    public boolean updateStudent(Student s) {
        // 修改SQL：使用实际的字段名 dorm_no 和 bed_no
        String sql = "UPDATE student SET name=?, gender=?, college=?, major=?, grade=?, class=?, phone=?, in_date=?, dorm_no=?, bed_no=? WHERE sno=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (conn == null) return false;

            // 设置参数（注意：只有11个参数了，不是12个）
            ps.setString(1, s.getName());
            ps.setString(2, s.getGender());
            ps.setString(3, s.getCollege());
            ps.setString(4, s.getMajor());
            ps.setString(5, s.getGrade());
            ps.setString(6, s.getClazz());
            ps.setString(7, s.getPhone());

            // in_date 处理：数据库中为varchar，需要转换为字符串
            if (s.getInDate() != null) {
                ps.setString(8, s.getInDate().toString());  // 改为setString
            } else {
                ps.setString(8, java.time.LocalDate.now().toString());
            }

            // 关键修改：构建 dorm_no 字符串（例如 "A栋101"）
            String dormNo = "";
            if (s.getBuilding() != null && !s.getBuilding().isEmpty() &&
                    s.getRoomNumber() != null && !s.getRoomNumber().isEmpty()) {
                dormNo = s.getBuilding() + "栋" + s.getRoomNumber();
            }
            ps.setString(9, dormNo);  // dorm_no

            // bed_no 处理：数据库中为varchar(50)，可能是字符串
            String bedNo = "";
            if (s.getBedNumber() > 0) {
                bedNo = String.valueOf(s.getBedNumber());
            }
            ps.setString(10, bedNo);  // bed_no

            ps.setString(11, s.getSno());  // WHERE 条件

            int c = ps.executeUpdate();
            return c > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 新增：检查指定学号是否已存在
    @Override
    public boolean existsBySno(String sno) {
        String sql = "SELECT COUNT(1) FROM student WHERE sno = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sno);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
