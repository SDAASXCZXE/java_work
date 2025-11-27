
package service;

import model.Student;
import java.util.List;

public interface StudentService {
    List<Student> listStudents();
    boolean addStudent(Student s);
    boolean deleteStudent(int id);
}
