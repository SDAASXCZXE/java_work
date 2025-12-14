package dao.impl;

import dao.StudentDao;
import model.Student;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
        String sql = "select * from student";

        try (
                Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                Student s = new Student();
                s.setId(rs.getInt("id"));
                s.setName(rs.getString("name"));
             //   s.setAge(rs.getInt("age"));
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
        String sql = "insert into student(name, age) values (?, ?)";

        try (
                Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, s.getName());
          //  ps.setInt(2, s.getAge());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 根据 id 删除学生
    @Override
    public boolean deleteStudent(int id) {
        String sql = "delete from student where id = ?";

        try (
                Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
