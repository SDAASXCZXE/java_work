
package dao;

import model.Student;
import java.util.List;

/**
 * 学生数据访问层接口
 */
public interface StudentDao {
    List<Student> findAll();
    boolean addStudent(Student s);
    boolean deleteStudent(int id);
}
