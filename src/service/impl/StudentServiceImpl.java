package service.impl;

import dao.StudentDao;
import dao.impl.StudentDaoImpl;
import model.Student;
import service.StudentService;

import java.util.List;

public class StudentServiceImpl implements StudentService {

    private StudentDao studentDao = new StudentDaoImpl(); // 实例化 DAO

    // 获取所有学生
    @Override
    public List<Student> listStudents() {
        return studentDao.findAll();  // 使用正确的变量名
    }

    @Override
    public void addStudent(Student student) {
        try {
            studentDao.addStudent(student);  // 调用 DAO 的添加方法
        } catch (Exception e) {
            // 打印异常堆栈信息，帮助调试
            e.printStackTrace();
            throw new RuntimeException("添加学生失败, 错误信息: " + e.getMessage(), e);
        }
    }

    // 删除学生
        @Override
        public void deleteStudent(String sno) {
            try {
                studentDao.deleteStudent(sno);
            } catch (Exception e) {
                throw new RuntimeException("删除学生失败: " + e.getMessage());
            }
        }

}
