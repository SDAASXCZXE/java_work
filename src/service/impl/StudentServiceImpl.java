
package service.impl;

import dao.StudentDao;
import dao.impl.StudentDaoImpl;
import model.Student;
import service.StudentService;

import java.util.List;

public class StudentServiceImpl implements StudentService {

    private StudentDao dao = new StudentDaoImpl();

    public List<Student> listStudents() {
        return dao.findAll();
    }

    public boolean addStudent(Student s) {
        return dao.addStudent(s);
    }

    public boolean deleteStudent(int id) {
        return dao.deleteStudent(id);
    }
}
