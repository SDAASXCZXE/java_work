/*
 * 文件：StudentInfoPanel.java
 * 说明：学生个人信息显示/编辑面板，用于学生查看与修改个人资料（如电话、紧急联系人等）。
 * 注意：仅插入注释，不修改功能实现。
 */

package ui;

import model.Student;

import javax.swing.*;
import java.awt.*;

public class StudentInfoPanel extends JPanel {
    private Student student;

    public StudentInfoPanel(Student student) {
        this.student = student;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10,10));
        JPanel p = new JPanel(new GridLayout(6,2,8,8));
        p.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        p.add(new JLabel("学号:")); p.add(new JLabel(student.getSno()));
        p.add(new JLabel("姓名:")); p.add(new JLabel(student.getName()));
        p.add(new JLabel("班级:")); p.add(new JLabel(student.getClazz()));
        p.add(new JLabel("宿舍:")); p.add(new JLabel(student.getBuilding() + " " + student.getRoomNumber()));
        p.add(new JLabel("联系电话:")); p.add(new JLabel(student.getPhone()));

        add(p, BorderLayout.NORTH);
    }
}