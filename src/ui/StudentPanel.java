package ui;

import java.time.LocalDate;
import java.sql.*;
import service.*;
import model.Student;
import service.impl.StudentServiceImpl;
import util.DBUtil;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Date;

  // 用于数据库中的日期类型



/**
 * 学生管理面板
 */
public class StudentPanel extends JPanel {
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JLabel countLabel; // 添加成员变量

    public StudentPanel() {
        initUI();
        loadStudentsFromDB();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 顶部工具栏
        add(createToolBar(), BorderLayout.NORTH);

        // 中间表格
        add(createTablePanel(), BorderLayout.CENTER);

        // 底部信息栏
        add(createInfoPanel(), BorderLayout.SOUTH);
    }

    /**
     * 创建工具栏
     */
    private JPanel createToolBar() {
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        toolBar.setBorder(BorderFactory.createTitledBorder("学生管理"));

        // 操作按钮
        String[] buttons = {"新增", "编辑", "删除", "分配宿舍", "查看详情", "导出数据"};

        for (String text : buttons) {
            JButton button = new JButton(text);
            button.setBackground(new Color(70, 130, 180));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.addActionListener(this::handleButtonClick);
            toolBar.add(button);
        }

        // 搜索功能
        toolBar.add(Box.createHorizontalStrut(20));
        toolBar.add(new JLabel("搜索:"));
        searchField = new JTextField(15);
        toolBar.add(searchField);

        JButton searchButton = new JButton("搜索");
        searchButton.addActionListener(e -> searchStudents());
        toolBar.add(searchButton);

        JButton resetButton = new JButton("重置");
        resetButton.addActionListener(e -> resetSearch());
        toolBar.add(resetButton);

        return toolBar;
    }

    /**
     * 创建表格面板
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // 创建表格模型
        String[] columns = {"学号", "姓名", "性别", "学院", "专业", "年级", "班级", "宿舍号", "床位号", "联系电话", "入住日期"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 表格不可直接编辑
            }
        };

        studentTable = new JTable(tableModel);
        studentTable.setRowHeight(25);
        studentTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        studentTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        // 设置列宽
        studentTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // 学号
        studentTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // 姓名
        studentTable.getColumnModel().getColumn(2).setPreferredWidth(50);  // 性别
        studentTable.getColumnModel().getColumn(3).setPreferredWidth(100); // 学院
        studentTable.getColumnModel().getColumn(4).setPreferredWidth(100); // 专业

        // 添加滚动条
        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("学生列表"));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 创建信息面板
     */
    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBorder(BorderFactory.createEtchedBorder());

        // 直接创建countLabel，避免通过索引访问
        countLabel = new JLabel("学生总数: 0");
        JLabel selectedLabel = new JLabel("已选中: 0");

        // 监听表格选择变化
        studentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedLabel.setText("已选中: " + studentTable.getSelectedRowCount());
            }
        });

        infoPanel.add(countLabel);
        infoPanel.add(Box.createHorizontalStrut(20));
        infoPanel.add(selectedLabel);

        return infoPanel;
    }

    /**
     * 更新学生总数 - 修复的方法
     */
    private void updateStudentCount() {
        if (countLabel != null) {
            countLabel.setText("学生总数: " + tableModel.getRowCount());
        }
    }

    /**
     * 从数据库中加载 表示显示功能
     */
    private void loadStudentsFromDB() {
        tableModel.setRowCount(0);

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM student");

            while (rs.next()) {
                Object[] row = {
                        rs.getString("sno"),
                        rs.getString("name"),
                        rs.getString("gender"),
                        rs.getString("college"),
                        rs.getString("major"),
                        rs.getString("grade"),
                        rs.getString("class"),
                        rs.getString("dorm_no"),
                        rs.getString("bed_no"),
                        rs.getString("phone"),
                        rs.getDate("in_date")
                };
                tableModel.addRow(row);
            }

            updateStudentCount();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            DBUtil.close(conn);
        }
    }

    /**
     * 处理按钮点击
     */
    private void handleButtonClick(ActionEvent e) {
        String command = ((JButton) e.getSource()).getText();

        switch (command) {
            case "新增":
                addStudent();
                break;
            case "编辑":
                editStudent();
                break;
            case "删除":
                deleteStudent();
                break;
            case "分配宿舍":
                assignDormitory();
                break;
           // case "查看详情":
             //   viewDetails();
            //    break;
           // case "导出数据":
             //   exportData();
               // break;
        }
    }

    /**
     * 添加学生
     */
    private void addStudent() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "添加学生", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridLayout(11, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] labels = {"学号:", "姓名:", "性别:", "学院:", "专业:", "年级:", "班级:", "联系电话:", "紧急联系人:", "紧急联系电话:"};
        JTextField[] fields = new JTextField[labels.length];

        for (int i = 0; i < labels.length; i++) {
            formPanel.add(new JLabel(labels[i]));
            fields[i] = new JTextField();
            formPanel.add(fields[i]);
        }

        dialog.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        // 保存按钮事件监听器 - 放在正确的位置
        saveButton.addActionListener(e -> {
            // 验证必填字段
            if (fields[0].getText().trim().isEmpty() || fields[1].getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "学号和姓名不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 创建学生对象并设置属性
            Student student = new Student();
            student.setSno(fields[0].getText().trim());
            student.setName(fields[1].getText().trim());
            student.setGender(fields[2].getText().trim());
            student.setCollege(fields[3].getText().trim());
            student.setMajor(fields[4].getText().trim());
            student.setGrade(fields[5].getText().trim());
            student.setClazz(fields[6].getText().trim());
            student.setPhone(fields[7].getText().trim());
            // 注意：紧急联系人相关字段可能需要在Student模型中添加
            student.setInDate(LocalDate.now());

            // 调用业务层添加学生
            StudentService studentService = new StudentServiceImpl();

            // 新增：在添加前检查学号是否已存在
            try {
                if (studentService.existsBySno(student.getSno())) {
                    JOptionPane.showMessageDialog(dialog, "该学号已存在，请检查输入。", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                studentService.addStudent(student);
                JOptionPane.showMessageDialog(dialog, "学生添加成功！");
                dialog.dispose(); // 关闭对话框
                loadStudentsFromDB(); // 刷新表格数据
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "添加学生失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 取消按钮事件监听器
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // 显示对话框
        dialog.setVisible(true);
    }
    /**
     * 删除学生（走 Service → DAO → 数据库）
     */
    private void deleteStudent() {
        // 1. 获取选中行
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "请先选择要删除的学生！",
                    "提示",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // 2. 获取学生学号（第0列）
        String studentSno = tableModel.getValueAt(selectedRow, 0).toString();

        // 3. 获取学生姓名（第1列）
        String studentName = tableModel.getValueAt(selectedRow, 1).toString();

        // 4. 删除确认
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "确定要删除学生 [" + studentName + "] 吗？",
                "确认删除",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // 5. 调用 Service，真正删除数据库数据
                StudentService studentService = new StudentServiceImpl();
                studentService.deleteStudent(studentSno);

                // 6. 重新加载数据库数据到 JTable
                loadStudentsFromDB();

                // 7. 更新学生数量
                updateStudentCount();

                JOptionPane.showMessageDialog(this, "删除成功！");
            } catch (Exception e) {
                e.printStackTrace();  // 调试用
                JOptionPane.showMessageDialog(
                        this,
                        "删除失败: " + e.getMessage(),
                        "删除失败",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    /**
     * 编辑学生
     */
    private void editStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要编辑的学生！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 获取选中的学生信息
        String studentId = tableModel.getValueAt(selectedRow, 0).toString();
        String name = tableModel.getValueAt(selectedRow, 1).toString();

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "编辑学生信息", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridLayout(10, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] labels = {"学号:", "姓名:", "性别:", "学院:", "专业:", "年级:", "班级:", "联系电话:", "宿舍楼:", "房间号:"};
        JComponent[] fields = new JComponent[labels.length];

        for (int i = 0; i < labels.length; i++) {
            formPanel.add(new JLabel(labels[i]));

            String currentValue = "";
            if (selectedRow < tableModel.getRowCount()) {
                switch (i) {
                    case 0: currentValue = tableModel.getValueAt(selectedRow, 0).toString(); break;
                    case 1: currentValue = tableModel.getValueAt(selectedRow, 1).toString(); break;
                    case 2: currentValue = tableModel.getValueAt(selectedRow, 2).toString(); break;
                    case 3: currentValue = tableModel.getValueAt(selectedRow, 3).toString(); break;
                    case 4: currentValue = tableModel.getValueAt(selectedRow, 4).toString(); break;
                    case 5: currentValue = tableModel.getValueAt(selectedRow, 5).toString(); break;
                    case 6: currentValue = tableModel.getValueAt(selectedRow, 6).toString(); break;
                    case 7: currentValue = tableModel.getValueAt(selectedRow, 9).toString(); break;
                    case 8:
                        String dorm = tableModel.getValueAt(selectedRow, 7).toString();
                        currentValue = dorm.length() > 1 ? dorm.substring(0, 1) : "A";
                        break;
                    case 9:
                        String room = tableModel.getValueAt(selectedRow, 7).toString();
                        currentValue = room.length() > 1 ? room.substring(1) : "101";
                        break;
                }
            }

            if (i == 2) { // 性别
                JComboBox<String> genderCombo = new JComboBox<>(new String[]{"男", "女"});
                genderCombo.setSelectedItem(currentValue);
                fields[i] = genderCombo;
            } else if (i == 8) { // 宿舍楼
                JComboBox<String> buildingCombo = new JComboBox<>(new String[]{"A栋", "B栋", "C栋", "D栋"});
                buildingCombo.setSelectedItem(currentValue + "栋");
                fields[i] = buildingCombo;
            } else {
                JTextField textField = new JTextField(currentValue);
                if (i == 0) textField.setEditable(false); // 学号不可编辑
                fields[i] = textField;
            }
            formPanel.add((Component) fields[i]);
        }

        dialog.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        saveButton.addActionListener(e -> {
            // 更新表格数据
            for (int i = 0; i < fields.length; i++) {
                if (i == 2) {
                    tableModel.setValueAt(((JComboBox) fields[i]).getSelectedItem(), selectedRow, i);
                } else if (i == 8 || i == 9) {
                    // 处理宿舍信息
                    if (i == 8) {
                        String building = ((String) ((JComboBox) fields[8]).getSelectedItem()).replace("栋", "");
                        String room = ((JTextField) fields[9]).getText().trim();
                        tableModel.setValueAt(building + room, selectedRow, 7);
                    }
                } else if (i == 7) {
                    tableModel.setValueAt(((JTextField) fields[i]).getText(), selectedRow, 9);
                } else if (i != 0) {
                    tableModel.setValueAt(((JTextField) fields[i]).getText(), selectedRow, i);
                }
            }

            JOptionPane.showMessageDialog(dialog, "学生信息修改成功！");
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * 分配宿舍
     */
    private void assignDormitory() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要分配宿舍的学生！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String studentName = tableModel.getValueAt(selectedRow, 1).toString();

        // 宿舍分配对话框
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "为 " + studentName + " 分配宿舍", true);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));
        dialog.setSize(300, 250);
        dialog.setLocationRelativeTo(this);

        dialog.add(new JLabel("宿舍楼:"));
        JComboBox<String> buildingCombo = new JComboBox<>(new String[]{"A栋", "B栋", "C栋", "D栋"});
        dialog.add(buildingCombo);

        dialog.add(new JLabel("房间号:"));
        JTextField roomField = new JTextField();
        dialog.add(roomField);

        dialog.add(new JLabel("床位号:"));
        JComboBox<String> bedCombo = new JComboBox<>(new String[]{"1号", "2号", "3号", "4号"});
        dialog.add(bedCombo);

        dialog.add(new JLabel("备注:"));
        JTextField remarkField = new JTextField();
        dialog.add(remarkField);

        JButton assignButton = new JButton("分配");
        JButton cancelButton = new JButton("取消");

        assignButton.addActionListener(e -> {
            String building = ((String) buildingCombo.getSelectedItem()).replace("栋", "");
            String room = roomField.getText().trim();
            String bed = (String) bedCombo.getSelectedItem();

            if (room.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请输入房间号！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            tableModel.setValueAt(building + room, selectedRow, 7);
            tableModel.setValueAt(bed, selectedRow, 8);

            JOptionPane.showMessageDialog(dialog, "宿舍分配成功！");
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(assignButton);
        dialog.add(cancelButton);

        dialog.setVisible(true);
    }

    /**
     * 导出学生数据
     */
    private void exportStudentData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("导出学生数据");
        fileChooser.setSelectedFile(new java.io.File("学生数据_" +
                new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".xlsx"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            String filePath = file.getAbsolutePath();

            if (!filePath.toLowerCase().endsWith(".xlsx")) {
                filePath += ".xlsx";
            }

            // 模拟导出过程
            String finalFilePath = filePath;
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this,
                                "学生数据导出成功！\n文件路径: " + finalFilePath + "\n导出记录数: " + tableModel.getRowCount(),
                                "导出成功",
                                JOptionPane.INFORMATION_MESSAGE);
                    });
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this,
                                "导出失败: " + e.getMessage(),
                                "错误",
                                JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();
        }
    }

    /**
     * 搜索学生
     */
    private void searchStudents() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入搜索关键词！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 清除之前的高亮
        studentTable.clearSelection();

        boolean found = false;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            boolean rowMatch = false;
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                Object value = tableModel.getValueAt(i, j);
                if (value != null && value.toString().toLowerCase().contains(keyword.toLowerCase())) {
                    rowMatch = true;
                    found = true;
                    break;
                }
            }

            if (rowMatch) {
                studentTable.addRowSelectionInterval(i, i);
                studentTable.scrollRectToVisible(studentTable.getCellRect(i, 0, true));
                break; // 只选中第一个匹配项
            }
        }

        if (!found) {
            JOptionPane.showMessageDialog(this, "未找到匹配的学生！", "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * 重置搜索
     */
    private void resetSearch() {
        searchField.setText("");
        studentTable.clearSelection();
    }
}

