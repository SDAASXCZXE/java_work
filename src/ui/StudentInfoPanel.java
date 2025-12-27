package ui;

import model.Student;
import util.DBUtil;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 学生个人信息显示面板
 * 修复点：增加实时数据库查询逻辑，确保楼号和房号正确展示
 */
public class StudentInfoPanel extends JPanel {
    private Student student;
    private JPanel contentPanel;

    public StudentInfoPanel(Student student) {
        this.student = student;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        // 使用网格布局展示信息
        contentPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        contentPanel.setBorder(BorderFactory.createTitledBorder("个人资料明细"));

        refreshDataAndUI(); // 初始化时加载数据
        add(contentPanel, BorderLayout.NORTH);
    }

    /**
     * 核心修复方法：从数据库获取最新信息并刷新 UI
     */
    public void refreshDataAndUI() {
        if (student == null || student.getSno() == null) {
            showEmptyMessage();
            return;
        }

        // 1. 从数据库读取最新数据
        // 注意：根据你的 StudentDaoImpl，宿舍号对应 dorm_no，楼号对应 building
        String sql = "SELECT name, gender, class, phone, dorm_no FROM student WHERE sno = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, student.getSno());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // 更新对象属性
                    student.setName(rs.getString("name"));
                    student.setGender(rs.getString("gender"));
                    student.setClazz(rs.getString("class"));
                    student.setPhone(rs.getString("phone"));
                    student.setRoomNumber(rs.getString("dorm_no")); // 修复：设置宿舍号
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("StudentInfoPanel 数据加载失败: " + e.getMessage());
        }

        // 2. 重建 UI 内容
        contentPanel.removeAll();

        addInfoRow("学号:", student.getSno());
        addInfoRow("姓名:", student.getName());
        addInfoRow("班级:", student.getClazz());

        String dormInfo = formatValue(student.getRoomNumber());
        addInfoRow("宿舍位置:", dormInfo.trim().isEmpty() ? "未分配" : dormInfo);

        addInfoRow("联系电话:", student.getPhone());
        addInfoRow("性别:", student.getGender());

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /**
     * 辅助方法：添加一行信息
     */
    private void addInfoRow(String label, String value) {
        JLabel lblName = new JLabel(label);
        lblName.setFont(new Font("微软雅黑", Font.BOLD, 13));

        JLabel lblValue = new JLabel(formatValue(value));
        lblValue.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        lblValue.setForeground(Color.BLUE); // 突出显示数据内容

        contentPanel.add(lblName);
        contentPanel.add(lblValue);
    }

    private void showEmptyMessage() {
        contentPanel.removeAll();
        contentPanel.add(new JLabel("未登录或无学生信息"));
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private String formatValue(String val) {
        return (val == null || val.trim().isEmpty() || val.equalsIgnoreCase("null")) ? "" : val;
    }

    // 外部调用接口
    public void updateStudent(Student updated) {
        this.student = updated;
        refreshDataAndUI();
    }
}