package ui;

import model.Visitor;
import model.VisitorManager;
import model.VisitorUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 访客记录管理面板
 */
public class VisitorPanel extends JPanel {
    private JTable visitorTable;
    private DefaultTableModel tableModel;
    private VisitorManager visitorManager;

    public VisitorPanel() {
        visitorManager = VisitorManager.getInstance();
        initUI();
        loadVisitors();
        updateStats();
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

        JComboBox<String> filterCombo = new JComboBox<>(new String[]{"全部", "今日", "本周", "本月", "当前在楼"});
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

        // 使用成员变量保存标签，方便更新
        JLabel todayLabel = new JLabel("今日访客: 0人");
        JLabel weekLabel = new JLabel("本周访客: 0人");
        JLabel monthLabel = new JLabel("本月访客: 0人");
        JLabel currentLabel = new JLabel("当前在楼: 0人");

        JLabel[] statLabels = {todayLabel, weekLabel, monthLabel, currentLabel};

        for (JLabel label : statLabels) {
            label.setFont(new Font("微软雅黑", Font.BOLD, 12));
            label.setForeground(new Color(70, 130, 180));
            panel.add(label);
        }

        return panel;
    }

    /**
     * 更新统计数据
     */
    private void updateStats() {
        // 获取统计面板
        JPanel statsPanel = (JPanel) getComponent(2);

        // 更新统计数据
        int todayCount = visitorManager.countTodayVisitors();
        int weekCount = visitorManager.countThisWeekVisitors();
        int monthCount = visitorManager.countThisMonthVisitors();
        int currentCount = visitorManager.countCurrentVisitors();

        // 更新标签
        Component[] components = statsPanel.getComponents();
        if (components.length >= 4) {
            ((JLabel) components[0]).setText("今日访客: " + todayCount + "人");
            ((JLabel) components[1]).setText("本周访客: " + weekCount + "人");
            ((JLabel) components[2]).setText("本月访客: " + monthCount + "人");
            ((JLabel) components[3]).setText("当前在楼: " + currentCount + "人");
        }
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
                "访问宿舍:", "被访学生:", "来访事由:", "预计离开时间:"};

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
                // 默认设置为2小时后
                Date leaveTime = new Date(System.currentTimeMillis() + 2 * 60 * 60 * 1000);
                fields[i] = new JTextField(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(leaveTime));
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
            String visitorName = ((JTextField) fields[0]).getText().trim();
            String idType = (String) ((JComboBox) fields[1]).getSelectedItem();
            String idNumber = ((JTextField) fields[2]).getText().trim();
            String phone = ((JTextField) fields[3]).getText().trim();
            String dormitory = ((JTextField) fields[4]).getText().trim();
            String visitedStudent = ((JTextField) fields[5]).getText().trim();
            JScrollPane purposeScroll = (JScrollPane) fields[6];
            JTextArea purposeTextArea = (JTextArea) purposeScroll.getViewport().getView();
            String purpose = purposeTextArea.getText().trim();
            String leaveTimeStr = ((JTextField) fields[7]).getText().trim();

            // 验证必填字段
            if (visitorName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "访客姓名不能为空！", "输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (idNumber.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "证件号码不能为空！", "输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!VisitorUtil.validateIdNumber(idType, idNumber)) {
                JOptionPane.showMessageDialog(dialog, "证件号码格式不正确！", "输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (phone.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "联系电话不能为空！", "输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!VisitorUtil.validatePhone(phone)) {
                JOptionPane.showMessageDialog(dialog, "联系电话格式不正确！", "输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (purpose.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "来访事由不能为空！", "输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 解析离开时间
            Date leaveTime = VisitorUtil.parseDateTime(leaveTimeStr);
            if (leaveTime == null) {
                JOptionPane.showMessageDialog(dialog, "离开时间格式不正确！", "输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 创建Visitor对象
            Visitor visitor = new Visitor();
            visitor.setVisitorName(visitorName);
            visitor.setIdType(idType);
            visitor.setIdNumber(idNumber);
            visitor.setPhone(phone);
            visitor.setDormitory(dormitory);
            visitor.setVisitedStudent(visitedStudent);
            visitor.setPurpose(purpose);
            visitor.setVisitTime(new Date());
            visitor.setLeaveTime(leaveTime);

            // 保存访客
            boolean success = visitorManager.registerVisitor(visitor);

            if (success) {
                // 添加到表格
                addVisitorToTable(visitor);

                // 更新统计数据
                updateStats();

                JOptionPane.showMessageDialog(dialog, "访客登记成功！\n访客ID: " + visitor.getVisitorId(),
                        "成功", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "访客登记失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelBtn = new JButton("取消");
        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(registerBtn);
        buttonPanel.add(cancelBtn);

        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * 将访客添加到表格
     */
    private void addVisitorToTable(Visitor visitor) {
        Object[] row = new Object[11];
        row[0] = visitor.getVisitorId();
        row[1] = visitor.getVisitorName();
        row[2] = VisitorUtil.formatDateTime(visitor.getVisitTime());
        row[3] = VisitorUtil.formatDateTime(visitor.getLeaveTime());
        row[4] = visitor.getDormitory();
        row[5] = visitor.getVisitedStudent();
        row[6] = visitor.getPurpose();
        row[7] = visitor.getIdType();
        row[8] = visitor.getIdNumber();
        row[9] = visitor.getPhone();
        row[10] = visitor.getRemark();

        tableModel.addRow(row);
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

        String visitorId = tableModel.getValueAt(selectedRow, 0).toString();
        Visitor visitor = visitorManager.getVisitorById(visitorId);

        if (visitor == null) {
            JOptionPane.showMessageDialog(this, "未找到对应的访客记录！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 创建编辑对话框
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "编辑访客信息", true);
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
                "访问宿舍:", "被访学生:", "来访事由:", "离开时间:", "备注:"};

        JComponent[] fields = new JComponent[labels.length];

        // 填充现有数据
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0;
            formPanel.add(new JLabel(labels[i]), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;

            switch (i) {
                case 0: // 姓名
                    fields[i] = new JTextField(visitor.getVisitorName());
                    break;
                case 1: // 证件类型
                    fields[i] = new JComboBox<>(new String[]{"身份证", "护照", "学生证", "其他"});
                    ((JComboBox) fields[i]).setSelectedItem(visitor.getIdType());
                    break;
                case 2: // 证件号码
                    fields[i] = new JTextField(visitor.getIdNumber());
                    break;
                case 3: // 电话
                    fields[i] = new JTextField(visitor.getPhone());
                    break;
                case 4: // 宿舍
                    fields[i] = new JTextField(visitor.getDormitory());
                    break;
                case 5: // 被访学生
                    fields[i] = new JTextField(visitor.getVisitedStudent());
                    break;
                case 6: // 事由
                    JTextArea purposeArea = new JTextArea(visitor.getPurpose(), 3, 20);
                    fields[i] = new JScrollPane(purposeArea);
                    break;
                case 7: // 离开时间
                    fields[i] = new JTextField(VisitorUtil.formatDateTime(visitor.getLeaveTime()));
                    break;
                case 8: // 备注
                    fields[i] = new JTextField(visitor.getRemark());
                    break;
            }

            formPanel.add(fields[i], gbc);
        }

        dialog.add(formPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton saveBtn = new JButton("保存修改");
        saveBtn.setBackground(new Color(70, 130, 180));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> {
            // 更新访客信息
            visitor.setVisitorName(((JTextField) fields[0]).getText().trim());
            visitor.setIdType((String) ((JComboBox) fields[1]).getSelectedItem());
            visitor.setIdNumber(((JTextField) fields[2]).getText().trim());
            visitor.setPhone(((JTextField) fields[3]).getText().trim());
            visitor.setDormitory(((JTextField) fields[4]).getText().trim());
            visitor.setVisitedStudent(((JTextField) fields[5]).getText().trim());

            JScrollPane purposeScroll = (JScrollPane) fields[6];
            JTextArea purposeTextArea = (JTextArea) purposeScroll.getViewport().getView();
            visitor.setPurpose(purposeTextArea.getText().trim());

            String leaveTimeStr = ((JTextField) fields[7]).getText().trim();
            Date leaveTime = VisitorUtil.parseDateTime(leaveTimeStr);
            if (leaveTime != null) {
                visitor.setLeaveTime(leaveTime);
            }

            visitor.setRemark(((JTextField) fields[8]).getText().trim());

            // 更新访客信息
            boolean success = visitorManager.updateVisitor(visitor);

            if (success) {
                // 更新表格
                updateTableRow(selectedRow, visitor);
                JOptionPane.showMessageDialog(dialog, "访客信息修改成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "修改失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelBtn = new JButton("取消");
        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * 更新表格行
     */
    private void updateTableRow(int rowIndex, Visitor visitor) {
        tableModel.setValueAt(visitor.getVisitorName(), rowIndex, 1);
        tableModel.setValueAt(VisitorUtil.formatDateTime(visitor.getVisitTime()), rowIndex, 2);
        tableModel.setValueAt(VisitorUtil.formatDateTime(visitor.getLeaveTime()), rowIndex, 3);
        tableModel.setValueAt(visitor.getDormitory(), rowIndex, 4);
        tableModel.setValueAt(visitor.getVisitedStudent(), rowIndex, 5);
        tableModel.setValueAt(visitor.getPurpose(), rowIndex, 6);
        tableModel.setValueAt(visitor.getIdType(), rowIndex, 7);
        tableModel.setValueAt(visitor.getIdNumber(), rowIndex, 8);
        tableModel.setValueAt(visitor.getPhone(), rowIndex, 9);
        tableModel.setValueAt(visitor.getRemark(), rowIndex, 10);
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

        String visitorId = tableModel.getValueAt(selectedRow, 0).toString();
        String visitorName = tableModel.getValueAt(selectedRow, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要删除访客 [" + visitorName + "] 的记录吗？",
                "确认删除",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = visitorManager.deleteVisitor(visitorId);
            if (success) {
                tableModel.removeRow(selectedRow);
                updateStats();
                JOptionPane.showMessageDialog(this, "删除成功！");
            } else {
                JOptionPane.showMessageDialog(this, "删除失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 查找访客
     */
    private void searchVisitor() {
        String keyword = JOptionPane.showInputDialog(this, "请输入访客姓名或证件号码:", "查找访客", JOptionPane.QUESTION_MESSAGE);

        if (keyword != null && !keyword.trim().isEmpty()) {
            keyword = keyword.trim();

            // 使用VisitorManager搜索
            List<Visitor> results = visitorManager.searchVisitorByName(keyword);
            if (results.isEmpty()) {
                results = visitorManager.searchVisitorByIdNumber(keyword);
            }

            if (!results.isEmpty()) {
                // 在表格中定位到第一个结果
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String tableVisitorId = tableModel.getValueAt(i, 0).toString();
                    if (tableVisitorId.equals(results.get(0).getVisitorId())) {
                        visitorTable.setRowSelectionInterval(i, i);
                        visitorTable.scrollRectToVisible(visitorTable.getCellRect(i, 0, true));

                        JOptionPane.showMessageDialog(this,
                                "找到 " + results.size() + " 条匹配记录",
                                "查找结果",
                                JOptionPane.INFORMATION_MESSAGE);
                        break;
                    }
                }
            } else {
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
        List<Visitor> filteredVisitors;

        switch (filter) {
            case "今日":
                filteredVisitors = visitorManager.getTodayVisitors();
                break;
            case "本周":
                // 这里可以扩展VisitorManager添加getThisWeekVisitors方法
                filteredVisitors = visitorManager.getAllVisitors();
                break;
            case "本月":
                // 这里可以扩展VisitorManager添加getThisMonthVisitors方法
                filteredVisitors = visitorManager.getAllVisitors();
                break;
            case "当前在楼":
                filteredVisitors = visitorManager.getCurrentVisitors();
                break;
            default: // 全部
                filteredVisitors = visitorManager.getAllVisitors();
                break;
        }

        // 清空表格
        tableModel.setRowCount(0);

        // 重新加载筛选后的数据
        for (Visitor visitor : filteredVisitors) {
            addVisitorToTable(visitor);
        }

        JOptionPane.showMessageDialog(this, "已筛选显示" + filteredVisitors.size() + "条记录", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 加载访客数据
     */
    private void loadVisitors() {
        List<Visitor> visitors = visitorManager.getAllVisitors();

        if (visitors.isEmpty()) {
            // 如果没有数据，加载示例数据
            loadSampleData();
        } else {
            // 加载真实数据
            for (Visitor visitor : visitors) {
                addVisitorToTable(visitor);
            }
        }
    }

    /**
     * 加载示例数据（仅在没有真实数据时使用）
     */
    private void loadSampleData() {
        try {
            // 创建示例访客数据
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            Visitor[] sampleVisitors = {
                    new Visitor("V20240115001", "张伟", sdf.parse("2024-01-15 14:30"),
                            sdf.parse("2024-01-15 16:30"), "A101", "张三",
                            "家长探望", "身份证", "110101199001011234", "13800138001", ""),
                    new Visitor("V20240114002", "李明", sdf.parse("2024-01-14 10:15"),
                            sdf.parse("2024-01-14 11:45"), "B202", "李四",
                            "同学聚会", "学生证", "20230001", "13800138002", ""),
                    new Visitor("V20240113003", "王芳", sdf.parse("2024-01-13 16:20"),
                            sdf.parse("2024-01-13 18:00"), "C303", "王五",
                            "物品交接", "身份证", "310101199202022345", "13800138003", ""),
                    new Visitor("V20240112004", "赵强", sdf.parse("2024-01-12 09:30"),
                            sdf.parse("2024-01-12 10:15"), "A102", "赵六",
                            "工作访问", "工作证", "WZ2024001", "13800138004", "公司员工"),
                    new Visitor("V20240111005", "刘洋", sdf.parse("2024-01-11 19:45"),
                            sdf.parse("2024-01-11 21:30"), "D404", "刘七",
                            "朋友来访", "身份证", "440101199303033456", "13800138005", "")
            };

            for (Visitor visitor : sampleVisitors) {
                visitorManager.registerVisitor(visitor);
                addVisitorToTable(visitor);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}