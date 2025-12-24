package service;

import model.Student;
import java.util.List;

public interface StudentService {
    List<Student> listStudents();
    boolean addStudent(Student student);
    boolean deleteStudent(String id);
    boolean existsBySno(String sno);
}