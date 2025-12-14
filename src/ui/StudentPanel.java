package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

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
        //loadSampleData();
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
     * 加载示例数据

    private void loadSampleData() {
        // 示例数据
        Object[][] sampleData = {
                {"20230001", "张三", "男", "计算机学院", "软件工程", "2023", "1班", "A101", "1号", "13800138001", "2023-09-01"},
                {"20230002", "李四", "女", "文学院", "汉语言文学", "2023", "2班", "B202", "3号", "13800138002", "2023-09-01"},
                {"20230003", "王五", "男", "经济学院", "金融学", "2023", "1班", "C303", "2号", "13800138003", "2023-09-01"},
                {"20230004", "赵六", "女", "法学院", "法学", "2023", "3班", "A102", "4号", "13800138004", "2023-09-01"},
                {"20230005", "钱七", "男", "医学院", "临床医学", "2023", "2班", "B201", "1号", "13800138005", "2023-09-01"},
                {"20230006", "孙八", "女", "艺术学院", "音乐表演", "2023", "1班", "C304", "3号", "13800138006", "2023-09-01"},
                {"20230007", "周九", "男", "工程学院", "土木工程", "2023", "4班", "A103", "2号", "13800138007", "2023-09-01"},
                {"20230008", "吴十", "女", "管理学院", "工商管理", "2023", "2班", "B203", "4号", "13800138008", "2023-09-01"},
        };

        for (Object[] row : sampleData) {
            tableModel.addRow(row);
        }

        updateStudentCount();
    }


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
            case "查看详情":
                viewDetails();
                break;
            case "导出数据":
                exportData();
                break;
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

        // 创建表单面板
        JPanel formPanel = new JPanel(new GridLayout(10, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] labels = {"学号:", "姓名:", "性别:", "学院:", "专业:", "年级:", "班级:", "联系电话:", "紧急联系人:", "紧急联系电话:"};
        JTextField[] fields = new JTextField[labels.length];

        for (int i = 0; i < labels.length; i++) {
            formPanel.add(new JLabel(labels[i]));
            fields[i] = new JTextField();
            formPanel.add(fields[i]);
        }

        dialog.add(formPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        saveButton.addActionListener(e -> {
            // 验证必填字段
            if (fields[0].getText().trim().isEmpty() || fields[1].getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "学号和姓名不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 添加到表格
            Object[] newRow = {
                    fields[0].getText().trim(),
                    fields[1].getText().trim(),
                    "男", // 默认值
                    fields[3].getText().trim(),
                    fields[4].getText().trim(),
                    fields[5].getText().trim(),
                    fields[6].getText().trim(),
                    "", "", // 宿舍号和床位号
                    fields[7].getText().trim(),
                    new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date())
            };
            tableModel.addRow(newRow);

            updateStudentCount();
            JOptionPane.showMessageDialog(dialog, "学生添加成功！");
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
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

        JOptionPane.showMessageDialog(this,
                "编辑功能开发中，当前选择学生: " + tableModel.getValueAt(selectedRow, 1),
                "提示",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 删除学生
     */
    private void deleteStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的学生！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String studentName = tableModel.getValueAt(selectedRow, 1).toString();
        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要删除学生 [" + studentName + "] 吗？",
                "确认删除",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            tableModel.removeRow(selectedRow);
            updateStudentCount();
            JOptionPane.showMessageDialog(this, "删除成功！");
        }
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
        dialog.setLayout(new GridLayout(4, 2, 10, 10));
        dialog.setSize(300, 200);
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

        JButton assignButton = new JButton("分配");
        JButton cancelButton = new JButton("取消");

        assignButton.addActionListener(e -> {
            String building = (String) buildingCombo.getSelectedItem();
            String room = roomField.getText().trim();
            String bed = (String) bedCombo.getSelectedItem();

            if (room.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请输入房间号！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 更新表格数据
            tableModel.setValueAt(building + room, selectedRow, 7); // 宿舍号
            tableModel.setValueAt(bed, selectedRow, 8); // 床位号

            JOptionPane.showMessageDialog(dialog, "宿舍分配成功！");
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(assignButton);
        dialog.add(cancelButton);

        dialog.setVisible(true);
    }

    /**
     * 查看详情
     */
    private void viewDetails() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要查看的学生！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StringBuilder details = new StringBuilder();
        details.append("学生详细信息\n");
        details.append("================\n\n");

        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            details.append(tableModel.getColumnName(i)).append(": ")
                    .append(tableModel.getValueAt(selectedRow, i)).append("\n");
        }

        JTextArea textArea = new JTextArea(details.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        JOptionPane.showMessageDialog(this, scrollPane, "学生详情", JOptionPane.INFORMATION_MESSAGE);
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

        // 简单的搜索逻辑
        boolean found = false;
        for (int i = 0; i < studentTable.getRowCount(); i++) {
            boolean rowMatch = false;
            for (int j = 0; j < studentTable.getColumnCount(); j++) {
                Object value = studentTable.getValueAt(i, j);
                if (value != null && value.toString().toLowerCase().contains(keyword.toLowerCase())) {
                    rowMatch = true;
                    found = true;
                    break;
                }
            }

            if (rowMatch) {
                studentTable.setRowSelectionInterval(i, i);
                studentTable.scrollRectToVisible(studentTable.getCellRect(i, 0, true));
                break;
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

    /**
     * 导出数据
     */
    private void exportData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("导出学生数据");
        fileChooser.setSelectedFile(new java.io.File("学生数据.xls"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            // 实际开发中这里应该实现导出逻辑
            JOptionPane.showMessageDialog(this,
                    "数据导出功能开发中\n文件路径: " + file.getAbsolutePath(),
                    "提示",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
}