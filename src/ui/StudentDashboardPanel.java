package ui;

import model.Student;
import javax.swing.*;
import java.awt.*;

public class StudentDashboardPanel extends JPanel {
    private Student student;
    private JTabbedPane tabbedPane;

    public StudentDashboardPanel(Student student) {
        this.student = student;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // 顶部欢迎栏
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(new Color(70, 130, 180));
        JLabel welcomeLbl = new JLabel("欢迎您, " + student.getName() + " (" + student.getSno() + ")");
        welcomeLbl.setForeground(Color.WHITE);
        welcomeLbl.setFont(new Font("微软雅黑", Font.BOLD, 16));
        header.add(welcomeLbl);
        add(header, BorderLayout.NORTH);

        // 初始化功能页签
        tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
        tabbedPane.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        tabbedPane.addTab("个人资料", new StudentInfoPanel(student));
        tabbedPane.addTab("故障报修", new StudentRepairPanel(student));
       // tabbedPane.addTab("考勤明细", new StudentAttendancePanel(student));
        tabbedPane.addTab("请假申请", new StudentHolidayPanel(student));
       // tabbedPane.addTab("调宿申请", new StudentRoomChangePanel(student));

        add(tabbedPane, BorderLayout.CENTER);
    }
}