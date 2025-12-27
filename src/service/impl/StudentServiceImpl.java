package service.impl;

import dao.StudentDao;
import dao.impl.StudentDaoImpl;
import model.Student;
import service.StudentService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
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
    public boolean updateStudent(Student student) {
        try {
            return studentDao.updateStudent(student);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean existsBySno(String sno) {
        return studentDao.existsBySno(sno);
    }

    @Override
    public Student getStudentBySno(String sno) {
        try {
            return studentDao.findBySno(sno);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean importFromCsv(File file) {
        if (file == null || !file.exists()) return false;
        int success = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                // 跳过表头（第一行可能包含列名）
                if (first) { first = false; if (line.toLowerCase().contains("sno") || line.contains("学号")) continue; }
                // 简单 CSV 解析（逗号分隔），若需支持引号或逗号转义请增强
                String[] cols = line.split(",");
                // 期待字段：sno,name,gender,college,major,grade,class,phone
                if (cols.length < 2) continue;
                Student s = new Student();
                s.setSno(cols[0].trim());
                s.setName(cols.length > 1 ? cols[1].trim() : "");
                s.setGender(cols.length > 2 ? cols[2].trim() : "");
                s.setCollege(cols.length > 3 ? cols[3].trim() : "");
                s.setMajor(cols.length > 4 ? cols[4].trim() : "");
                s.setGrade(cols.length > 5 ? cols[5].trim() : "");
                s.setClazz(cols.length > 6 ? cols[6].trim() : "");
                s.setPhone(cols.length > 7 ? cols[7].trim() : "");
                if (studentDao.addStudent(s)) success++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success > 0;
    }

}
