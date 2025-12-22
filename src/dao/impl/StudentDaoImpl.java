package dao.impl;

import dao.StudentDao;
import model.Student;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 学生 DAO 实现类（JDBC 实现）
 */
public class StudentDaoImpl implements StudentDao {

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
                s.setInDate(rs.getDate("in_date").toLocalDate());
                list.add(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 添加学生
    @Override
    public void addStudent(Student s) throws Exception {
        String sql = "INSERT INTO student " +
                "(sno, name, gender, college, major, grade, class, phone, in_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getSno());
            ps.setString(2, s.getName());
            ps.setString(3, s.getGender());
            ps.setString(4, s.getCollege());
            ps.setString(5, s.getMajor());
            ps.setString(6, s.getGrade());
            ps.setString(7, s.getClazz());
            ps.setString(8, s.getPhone());
            ps.setDate(9, java.sql.Date.valueOf(s.getInDate()));

            ps.executeUpdate();
        }
    }

    // 根据 id 删除学生
    @Override
    public void deleteStudent(String sno) {
        String sql = "DELETE FROM student WHERE sno = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, sno);
            int rows = pstmt.executeUpdate();

            if (rows == 0) {
                throw new RuntimeException("未找到学号为 " + sno + " 的学生");
            }
        } catch (SQLException e) {
            throw new RuntimeException("删除学生失败: " + e.getMessage(), e);
        }
    }
}
