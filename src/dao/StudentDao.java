package dao;

import model.Student;
import java.util.List;

/**
 * 学生数据访问层接口
 */
public interface StudentDao {
    /** 查询所有学生 */
    List<Student> findAll();

    /** 插入学生，成功返回 true */
    boolean addStudent(Student student);

    /** 根据学号删除学生，成功返回 true */
    boolean deleteStudent(String sno);

    /** 检查学号是否存在 */
    boolean existsBySno(String sno);
}
