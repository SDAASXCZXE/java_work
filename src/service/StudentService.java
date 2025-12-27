package service;

import model.Student;

import java.util.List;

public interface StudentService {
    List<Student> listStudents();
    boolean addStudent(Student student);
    boolean deleteStudent(String id);
    boolean updateStudent(Student student);
    boolean existsBySno(String sno);
    Student getStudentBySno(String sno);
}