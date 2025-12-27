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
    private JPanel contentPanel;

    public StudentInfoPanel(Student student) {
        this.student = student;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10,10));
        contentPanel = new JPanel(new GridLayout(6,2,8,8));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        rebuildContent();
        add(contentPanel, BorderLayout.NORTH);
    }

    private void rebuildContent() {
        contentPanel.removeAll();
        contentPanel.add(new JLabel("学号:")); contentPanel.add(new JLabel(student.getSno()));
        contentPanel.add(new JLabel("姓名:")); contentPanel.add(new JLabel(student.getName()));
        contentPanel.add(new JLabel("班级:")); contentPanel.add(new JLabel(student.getClazz()));
        contentPanel.add(new JLabel("宿舍:")); contentPanel.add(new JLabel(student.getBuilding() + " " + student.getRoomNumber()));
        contentPanel.add(new JLabel("联系电话:")); contentPanel.add(new JLabel(student.getPhone()));
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // 外部调用：当后台学生信息发生变更时，由 StudentDashboardPanel 调用来更新显示
    public void updateStudent(Student updated) {
        if (updated == null) return;
        this.student = updated;
        rebuildContent();
    }
}