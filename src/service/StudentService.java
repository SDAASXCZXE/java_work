package service;

import model.Student;
import java.util.List;

public interface StudentService {
    List<Student> listStudents();
    void addStudent(Student student);
    void deleteStudent(String id);
}