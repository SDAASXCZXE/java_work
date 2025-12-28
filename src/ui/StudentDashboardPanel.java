/*
 * 文件：StudentDashboardPanel.java
 * 说明：学生端主面板，作为学生登录后的仪表盘，包含多个功能页签（个人资料、报修、请假、假期、退宿/换宿等）。
 * 注意：仅添加文件级注释，不修改现有逻辑代码。
 */

package ui;

import model.Student;

import javax.swing.*;
import java.awt.*;

import service.impl.StudentServiceImpl;
import util.RefreshCenter;

public class StudentDashboardPanel extends JPanel {
    private Student student;
    private JTabbedPane tabbedPane;
    private JLabel welcomeLbl; // 提升为字段，便于更新欢迎文字

    public StudentDashboardPanel(Student student) {
        this.student = student;
        initUI();
        // 注册全局学生更新通知，当管理员端更新学生信息时会触发
        RefreshCenter.register("students-updated", this::onStudentsUpdated);
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // 顶部欢迎栏
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(new Color(70, 130, 180));
        welcomeLbl = new JLabel("欢迎您, " + student.getName() + " (" + student.getSno() + ")");
        welcomeLbl.setForeground(Color.WHITE);
        welcomeLbl.setFont(new Font("微软雅黑", Font.BOLD, 16));
        header.add(welcomeLbl);
        add(header, BorderLayout.NORTH);

        // 初始化功能页签
        tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
        tabbedPane.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        tabbedPane.addTab("个人资料", new StudentInfoPanel(student));
        tabbedPane.addTab("故障报修", new StudentRepairPanel(student));
        tabbedPane.addTab("请假登记", new StudentLeavePanel(student));
        tabbedPane.addTab("假期登记", new StudentHolidayPanel(student));
        // 新增：退宿/换宿申请页面（学生端）
        tabbedPane.addTab("退宿/换宿", new StudentRoomChangePanel(student));

        add(tabbedPane, BorderLayout.CENTER);
    }

    // 当检测到学生数据已更新时，重新从数据库拉取该学生最新信息并刷新子面板
    private void onStudentsUpdated() {
        // 使用新线程避免阻塞 EDT，然后在 EDT 中更新组件
        new Thread(() -> {
            try {
                StudentServiceImpl svc = new StudentServiceImpl();
                Student fresh = svc.getStudentBySno(student.getSno());
                if (fresh == null) return;
                this.student = fresh;
                SwingUtilities.invokeLater(() -> {
                    // 更新欢迎栏
                    welcomeLbl.setText("欢迎您, " + student.getName() + " (" + student.getSno() + ")");
                    // 更新各个子面板（如果实现了 updateStudent 方法则调用）
                    for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                        Component c = tabbedPane.getComponentAt(i);
                        if (c instanceof ui.StudentInfoPanel) ((ui.StudentInfoPanel) c).updateStudent(student);
                        else if (c instanceof ui.StudentRepairPanel) ((ui.StudentRepairPanel) c).updateStudent(student);
                        else if (c instanceof ui.StudentLeavePanel) ((ui.StudentLeavePanel) c).updateStudent(student);
                        else if (c instanceof ui.StudentHolidayPanel) ((ui.StudentHolidayPanel) c).updateStudent(student);
                        else if (c instanceof ui.StudentRoomChangePanel) ((ui.StudentRoomChangePanel) c).updateStudent(student);
                    }
                });
            } catch (Exception ignored) {}
        }).start();
    }
}