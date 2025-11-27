
package ui;

import model.Student;
import service.StudentService;
import service.impl.StudentServiceImpl;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * 学生管理界面
 */
public class StudentPanel extends JPanel {

    private StudentService service = new StudentServiceImpl();
    private JTextArea area;

    public StudentPanel() {
        setLayout(new BorderLayout());

        JButton load = new JButton("加载学生信息");
        area = new JTextArea();

        load.addActionListener(e -> loadStudents());

        add(load, BorderLayout.NORTH);
        add(new JScrollPane(area), BorderLayout.CENTER);
    }

    private void loadStudents() {
        List<Student> list = service.listStudents();
        area.setText("");

        for (Student s : list) {
            area.append(s.getId() + " - " + s.getName() + " - " + s.getGender() +
                    " - 宿舍:" + s.getRoomId() + "\n");
        }
    }
}
