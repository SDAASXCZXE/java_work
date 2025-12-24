package service.impl;

import dao.StudentDao;
import dao.impl.StudentDaoImpl;
import model.Student;
import service.StudentService;

import java.util.List;

public class StudentServiceImpl implements StudentService {

    private final StudentDao studentDao = new StudentDaoImpl(); // 实例化 DAO

    // 获取所有学生
    @Override
    public List<Student> listStudents() {
        return studentDao.findAll();  // 使用正确的变量名
    }

    @Override
    public boolean addStudent(Student student) {
        try {
            return studentDao.addStudent(student);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 删除学生
    @Override
    public boolean deleteStudent(String sno) {
        try {
            return studentDao.deleteStudent(sno);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean existsBySno(String sno) {
        return studentDao.existsBySno(sno);
    }

}
