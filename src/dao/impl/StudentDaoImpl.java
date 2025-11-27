
package dao.impl;

import dao.StudentDao;
import model.Student;
import java.util.ArrayList;
import java.util.List;

/**
 * 学生 DAO 实现类（JDBC 待实现）
 */
public class StudentDaoImpl implements StudentDao {

    @Override
    public List<Student> findAll() {
        return new ArrayList<>();
    }

    @Override
    public boolean addStudent(Student s) {
        return false;
    }

    @Override
    public boolean deleteStudent(int id) {
        return false;
    }
}
