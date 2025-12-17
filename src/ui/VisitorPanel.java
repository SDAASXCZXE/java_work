package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * 访客记录管理面板
 */
public class VisitorPanel extends JPanel {
    private JTable visitorTable;
    private DefaultTableModel tableModel;

    public VisitorPanel() {
        initUI();
        loadSampleData();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 顶部工具栏
        add(createToolBar(), BorderLayout.NORTH);

        // 中间表格
        add(createTablePanel(), BorderLayout.CENTER);

        // 底部统计信息
        add(createStatsPanel(), BorderLayout.SOUTH);
    }

    /**
     * 创建工具栏
     */
    private JPanel createToolBar() {
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        toolBar.setBorder(BorderFactory.createTitledBorder("访客记录管理"));

        String[] buttons = {"访客登记", "编辑记录", "删除记录", "查找访客", "导出记录"};

        for (String text : buttons) {
            JButton button = new JButton(text);
            button.setBackground(new Color(70, 130, 180));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.addActionListener(this::handleButtonClick);
            toolBar.add(button);
        }

        // 快速筛选
        toolBar.add(Box.createHorizontalStrut(20));
        toolBar.add(new JLabel("筛选:"));

        JComboBox<String> filterCombo = new JComboBox<>(new String[]{"全部", "今日", "本周", "本月"});
        filterCombo.addActionListener(e -> filterRecords((String)filterCombo.getSelectedItem()));
        toolBar.add(filterCombo);

        return toolBar;
    }

    /**
     * 创建表格面板
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = {"访客ID", "访客姓名", "来访时间", "离开时间", "访问宿舍", "被访学生",
                "来访事由", "证件类型", "证件号码", "联系电话", "备注"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        visitorTable = new JTable(tableModel);
        visitorTable.setRowHeight(25);
        visitorTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        visitorTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        // 设置列宽
        visitorTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        visitorTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        visitorTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        visitorTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        visitorTable.getColumnModel().getColumn(4).setPreferredWidth(80);

        JScrollPane scrollPane = new JScrollPane(visitorTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("访客记录列表"));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    /**
     * 创建统计面板
     */
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        panel.setBorder(BorderFactory.createEtchedBorder());

        String[] stats = {"今日访客: 5人", "本周访客: 23人", "本月访客: 89人", "当前在楼: 3人"};

        for (String stat : stats) {
            JLabel label = new JLabel(stat);
            label.setFont(new Font("微软雅黑", Font.BOLD, 12));
            label.setForeground(new Color(70, 130, 180));
            panel.add(label);
        }

        return panel;
    }

    /**
     * 处理按钮点击
     */
    private void handleButtonClick(ActionEvent e) {
        String command = ((JButton) e.getSource()).getText();

        switch (command) {
            case "访客登记":
                registerVisitor();
                break;
            case "编辑记录":
                editVisitor();
                break;
            case "删除记录":
                deleteVisitor();
                break;
            case "查找访客":
                searchVisitor();
                break;
            case "导出记录":
                exportRecords();
                break;
        }
    }

    /**
     * 访客登记
     */
    private void registerVisitor() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "访客登记", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        String[] labels = {"访客姓名:", "证件类型:", "证件号码:", "联系电话:",
                "访问宿舍:", "被访学生:", "来访事由:", "预计离开:"};

        JComponent[] fields = new JComponent[labels.length];

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0;
            formPanel.add(new JLabel(labels[i]), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;

            if (i == 1) { // 证件类型
                fields[i] = new JComboBox<>(new String[]{"身份证", "护照", "学生证", "其他"});
            } else if (i == 7) { // 预计离开时间
                fields[i] = new JTextField(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm")
                        .format(new java.util.Date()));
            } else if (i == 6) { // 来访事由
                JTextArea textArea = new JTextArea(3, 20);
                fields[i] = new JScrollPane(textArea);
            } else {
                fields[i] = new JTextField();
            }

            formPanel.add(fields[i], gbc);
        }

        dialog.add(formPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton registerBtn = new JButton("登记访客");
        registerBtn.setBackground(new Color(70, 130, 180));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.addActionListener(e -> {
            // 验证输入
            for (int i = 0; i < fields.length; i++) {
                if (i == 6) continue; // 跳过事由字段

                String value = "";
                if (fields[i] instanceof JTextField) {
                    value = ((JTextField) fields[i]).getText().trim();
                } else if (fields[i] instanceof JComboBox) {
                    value = ((JComboBox) fields[i]).getSelectedItem().toString();
                }

                if (value.isEmpty() && i != 4 && i != 5) { // 访问宿舍和被访学生可选
                    JOptionPane.showMessageDialog(dialog, labels[i] + "不能为空！",
                            "输入错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // 添加到表格
            Object[] newRow = new Object[labels.length + 3];
            newRow[0] = "V" + System.currentTimeMillis(); // 访客ID
            newRow[1] = ((JTextField) fields[0]).getText().trim(); // 姓名
            newRow[2] = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm")
                    .format(new java.util.Date()); // 来访时间
            newRow[3] = ((JTextField) fields[7]).getText().trim(); // 离开时间

            for (int i = 1; i < fields.length; i++) {
                if (i == 6) {
                    // 处理事由字段
                    JScrollPane scrollPane = (JScrollPane) fields[i];
                    JTextArea textArea = (JTextArea) scrollPane.getViewport().getView();
                    newRow[i + 3] = textArea.getText().trim();
                } else if (fields[i] instanceof JTextField) {
                    newRow[i + 3] = ((JTextField) fields[i]).getText().trim();
                } else if (fields[i] instanceof JComboBox) {
                    newRow[i + 3] = ((JComboBox) fields[i]).getSelectedItem().toString();
                }
            }

            newRow[11] = ""; // 备注

            tableModel.addRow(newRow);

            JOptionPane.showMessageDialog(dialog, "访客登记成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });

        JButton cancelBtn = new JButton("取消");
        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(registerBtn);
        buttonPanel.add(cancelBtn);

        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * 编辑访客记录
     */
    private void editVisitor() {
        int selectedRow = visitorTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要编辑的访客记录！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
                "编辑功能开发中\n当前选择记录ID: " + tableModel.getValueAt(selectedRow, 0),
                "提示",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 删除访客记录
     */
    private void deleteVisitor() {
        int selectedRow = visitorTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要删除的访客记录！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String visitorName = tableModel.getValueAt(selectedRow, 1).toString();
        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要删除访客 [" + visitorName + "] 的记录吗？",
                "确认删除",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            tableModel.removeRow(selectedRow);
            JOptionPane.showMessageDialog(this, "删除成功！");
        }
    }

    /**
     * 查找访客
     */
    private void searchVisitor() {
        String keyword = JOptionPane.showInputDialog(this, "请输入访客姓名或证件号码:", "查找访客", JOptionPane.QUESTION_MESSAGE);

        if (keyword != null && !keyword.trim().isEmpty()) {
            keyword = keyword.trim().toLowerCase();

            boolean found = false;
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String name = tableModel.getValueAt(i, 1).toString().toLowerCase();
                String id = tableModel.getValueAt(i, 8).toString().toLowerCase();

                if (name.contains(keyword) || id.contains(keyword)) {
                    visitorTable.setRowSelectionInterval(i, i);
                    visitorTable.scrollRectToVisible(visitorTable.getCellRect(i, 0, true));
                    found = true;
                    break;
                }
            }

            if (!found) {
                JOptionPane.showMessageDialog(this, "未找到匹配的访客记录！", "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    /**
     * 导出记录
     */
    private void exportRecords() {
        JOptionPane.showMessageDialog(this, "导出功能开发中...", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 筛选记录
     */
    private void filterRecords(String filter) {
        // 实现筛选逻辑
        JOptionPane.showMessageDialog(this, "筛选功能开发中: " + filter, "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 加载示例数据
     */
    private void loadSampleData() {
        Object[][] sampleData = {
                {"V20240115001", "张伟", "2024-01-15 14:30", "2024-01-15 16:30", "A101", "张三",
                        "家长探望", "身份证", "110101199001011234", "13800138001", ""},
                {"V20240114002", "李明", "2024-01-14 10:15", "2024-01-14 11:45", "B202", "李四",
                        "同学聚会", "学生证", "20230001", "13800138002", ""},
                {"V20240113003", "王芳", "2024-01-13 16:20", "2024-01-13 18:00", "C303", "王五",
                        "物品交接", "身份证", "310101199202022345", "13800138003", ""},
                {"V20240112004", "赵强", "2024-01-12 09:30", "2024-01-12 10:15", "A102", "赵六",
                        "工作访问", "工作证", "WZ2024001", "13800138004", "公司员工"},
                {"V20240111005", "刘洋", "2024-01-11 19:45", "2024-01-11 21:30", "D404", "刘七",
                        "朋友来访", "身份证", "440101199303033456", "13800138005", ""}
        };

        for (Object[] row : sampleData) {
            tableModel.addRow(row);
        }
    }
}