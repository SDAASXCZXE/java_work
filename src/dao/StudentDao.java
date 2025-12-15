
package dao;

import model.Student;
import java.util.List;

/**
 * 学生数据访问层接口
 */
public interface StudentDao {
    List<Student> findAll();
    void addStudent(Student student) throws Exception;
    boolean deleteStudent(int id);
}
