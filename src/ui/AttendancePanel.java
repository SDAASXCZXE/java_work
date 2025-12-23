package ui;

import model.Attendance;
import model.AttendanceManager;
import model.AttendanceStatus;
import model.AttendanceUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 考勤管理面板
 */
public class AttendancePanel extends JPanel {
    private JTable attendanceTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> buildingFilter;
    private JComboBox<String> statusFilter;
    private AttendanceManager attendanceManager;

    // 统计标签
    private JLabel todayTotalLabel;
    private JLabel todayNormalLabel;
    private JLabel todayLateLabel;
    private JLabel todayAbsentLabel;
    private JLabel todayLeaveLabel;
    private JLabel monthTotalLabel;
    private JLabel monthLateLabel;
    private JLabel monthAbsentLabel;
    private JLabel monthLeaveLabel;
    private JLabel monthRateLabel;

    public AttendancePanel() {
        attendanceManager = AttendanceManager.getInstance();
        initUI();
        loadAttendances();
        updateStatistics();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 顶部工具栏
        add(createToolBar(), BorderLayout.NORTH);

        // 中间表格
        add(createTablePanel(), BorderLayout.CENTER);

        // 右侧统计面板
        add(createStatsPanel(), BorderLayout.EAST);
    }

    /**
     * 创建工具栏
     */
    private JPanel createToolBar() {
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        toolBar.setBorder(BorderFactory.createTitledBorder("考勤管理"));

        String[] buttons = {"手动登记", "批量导入", "导出数据", "生成报表", "发送提醒"};

        for (String text : buttons) {
            JButton button = new JButton(text);
            button.setBackground(new Color(70, 130, 180));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.addActionListener(this::handleButtonClick);
            toolBar.add(button);
        }

        // 筛选功能
        toolBar.add(Box.createHorizontalStrut(20));
        toolBar.add(new JLabel("宿舍楼:"));
        buildingFilter = new JComboBox<>(new String[]{"全部", "A栋", "B栋", "C栋", "D栋"});
        buildingFilter.addActionListener(e -> filterAttendance());
        toolBar.add(buildingFilter);

        toolBar.add(new JLabel("状态:"));
        statusFilter = new JComboBox<>(new String[]{"全部", "正常", "晚归", "未归", "请假"});
        statusFilter.addActionListener(e -> filterAttendance());
        toolBar.add(statusFilter);

        // 日期选择
        toolBar.add(new JLabel("日期:"));
        JTextField dateField = new JTextField(AttendanceUtil.formatDate(new Date()), 10);
        toolBar.add(dateField);

        JButton dateBtn = new JButton("查询");
        dateBtn.addActionListener(e -> queryByDate(dateField.getText()));
        toolBar.add(dateBtn);

        return toolBar;
    }

    /**
     * 创建表格面板
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = {"序号", "学号", "姓名", "宿舍号", "日期", "归寝时间",
                "考勤状态", "备注", "登记时间"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        attendanceTable = new JTable(tableModel);
        attendanceTable.setRowHeight(25);
        attendanceTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        attendanceTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        // 设置行颜色
        attendanceTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                String statusStr = table.getValueAt(row, 6).toString();
                AttendanceStatus status = getAttendanceStatusFromString(statusStr);

                if (!isSelected) {
                    c.setBackground(AttendanceUtil.getStatusColor(status));
                }

                return c;
            }
        });

        // 设置列宽
        attendanceTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        attendanceTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        attendanceTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        attendanceTable.getColumnModel().getColumn(3).setPreferredWidth(70);
        attendanceTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        attendanceTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        attendanceTable.getColumnModel().getColumn(6).setPreferredWidth(70);

        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("考勤记录"));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    /**
     * 字符串转考勤状态枚举
     */
    private AttendanceStatus getAttendanceStatusFromString(String statusStr) {
        switch (statusStr) {
            case "正常":
                return AttendanceStatus.NORMAL;
            case "晚归":
                return AttendanceStatus.LATE;
            case "未归":
                return AttendanceStatus.ABSENT;
            case "请假":
                return AttendanceStatus.LEAVE;
            default:
                return AttendanceStatus.NORMAL;
        }
    }
    /**
     * 创建统计面板
     */
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("考勤统计"));
        panel.setPreferredSize(new Dimension(250, 0));

        // 今日统计
        JPanel todayPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        todayPanel.setBorder(BorderFactory.createTitledBorder("今日统计"));

        String[] todayLabels = {"应归人数:", "已归人数:", "晚归人数:", "未归人数:", "请假人数:"};

        // 初始化标签
        todayTotalLabel = new JLabel("320");
        todayNormalLabel = new JLabel("315");
        todayLateLabel = new JLabel("3");
        todayAbsentLabel = new JLabel("2");
        todayLeaveLabel = new JLabel("5");

        JLabel[] todayValueLabels = {todayTotalLabel, todayNormalLabel, todayLateLabel, todayAbsentLabel, todayLeaveLabel};

        for (int i = 0; i < todayLabels.length; i++) {
            JLabel label = new JLabel(todayLabels[i]);
            label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            todayPanel.add(label);

            todayValueLabels[i].setFont(new Font("微软雅黑", Font.BOLD, 12));
            todayValueLabels[i].setForeground(new Color(70, 130, 180));
            todayPanel.add(todayValueLabels[i]);
        }

        panel.add(todayPanel);
        panel.add(Box.createVerticalStrut(15));

        // 本月统计
        JPanel monthPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        monthPanel.setBorder(BorderFactory.createTitledBorder("本月统计"));

        String[] monthLabels = {"正常天数:", "晚归次数:", "未归次数:", "请假次数:", "出勤率:"};

        // 初始化标签
        monthTotalLabel = new JLabel("18天");
        monthLateLabel = new JLabel("45次");
        monthAbsentLabel = new JLabel("8次");
        monthLeaveLabel = new JLabel("67次");
        monthRateLabel = new JLabel("96.2%");

        JLabel[] monthValueLabels = {monthTotalLabel, monthLateLabel, monthAbsentLabel, monthLeaveLabel, monthRateLabel};

        for (int i = 0; i < monthLabels.length; i++) {
            JLabel label = new JLabel(monthLabels[i]);
            label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            monthPanel.add(label);

            monthValueLabels[i].setFont(new Font("微软雅黑", Font.BOLD, 12));
            monthValueLabels[i].setForeground(new Color(70, 130, 180));
            monthPanel.add(monthValueLabels[i]);
        }

        panel.add(monthPanel);
        panel.add(Box.createVerticalStrut(15));

        // 快速操作
        JPanel quickPanel = new JPanel();
        quickPanel.setLayout(new BoxLayout(quickPanel, BoxLayout.Y_AXIS));
        quickPanel.setBorder(BorderFactory.createTitledBorder("快速操作"));

        String[] actions = {"查看异常", "导出今日", "发送通知", "生成月报"};

        for (String action : actions) {
            JButton button = new JButton(action);
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.setMaximumSize(new Dimension(200, 30));
            button.addActionListener(e -> handleQuickAction(action));
            quickPanel.add(button);
            quickPanel.add(Box.createVerticalStrut(5));
        }

        panel.add(quickPanel);

        return panel;
    }

    /**
     * 处理按钮点击
     */
    private void handleButtonClick(ActionEvent e) {
        String command = ((JButton) e.getSource()).getText();

        switch (command) {
            case "手动登记":
                manualRegistration();
                break;
            case "批量导入":
                batchImport();
                break;
            case "导出数据":
                exportData();
                break;
            case "生成报表":
                generateReport();
                break;
            case "发送提醒":
                sendReminder();
                break;
        }
    }

    /**
     * 处理快速操作
     */
    private void handleQuickAction(String action) {
        switch (action) {
            case "查看异常":
                viewAbnormal();
                break;
            case "导出今日":
                exportToday();
                break;
            case "发送通知":
                sendNotification();
                break;
            case "生成月报":
                generateMonthlyReport();
                break;
        }
    }

    /**
     * 手动登记
     */
    private void manualRegistration() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "手动考勤登记", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 学号
        formPanel.add(new JLabel("学号:"));
        JTextField studentIdField = new JTextField();
        formPanel.add(studentIdField);

        // 姓名
        formPanel.add(new JLabel("姓名:"));
        JTextField nameField = new JTextField();
        formPanel.add(nameField);

        // 宿舍号
        formPanel.add(new JLabel("宿舍号:"));
        JTextField dormField = new JTextField();
        formPanel.add(dormField);

        // 日期
        formPanel.add(new JLabel("日期:"));
        JTextField dateField = new JTextField(AttendanceUtil.formatDate(new Date()));
        formPanel.add(dateField);

        // 归寝时间
        formPanel.add(new JLabel("归寝时间:"));
        JTextField timeField = new JTextField(AttendanceUtil.formatTime(new Date()));
        formPanel.add(timeField);

        // 考勤状态
        formPanel.add(new JLabel("考勤状态:"));
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"正常", "晚归", "未归", "请假"});
        formPanel.add(statusCombo);

        // 备注
        formPanel.add(new JLabel("备注:"));
        JTextField remarkField = new JTextField();
        formPanel.add(remarkField);

        dialog.add(formPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton registerBtn = new JButton("登记");
        registerBtn.setBackground(new Color(70, 130, 180));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.addActionListener(e -> {
            String studentId = studentIdField.getText().trim();
            String studentName = nameField.getText().trim();
            String dormitory = dormField.getText().trim();
            String dateStr = dateField.getText().trim();
            String timeStr = timeField.getText().trim();
            String statusStr = (String) statusCombo.getSelectedItem();
            String remark = remarkField.getText().trim();

            // 验证输入
            if (studentId.isEmpty() || studentName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "学号和姓名不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!AttendanceUtil.validateStudentId(studentId)) {
                JOptionPane.showMessageDialog(dialog, "学号格式不正确！应为8-10位数字", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!AttendanceUtil.validateDormitory(dormitory)) {
                JOptionPane.showMessageDialog(dialog, "宿舍号格式不正确！应为字母+3位数字，如A101", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 解析日期时间
            Date attendanceDate = AttendanceUtil.parseDate(dateStr);
            Date checkInTime = AttendanceUtil.parseTime(timeStr);

            if (attendanceDate == null) {
                JOptionPane.showMessageDialog(dialog, "日期格式不正确！应为yyyy-MM-dd", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            AttendanceStatus status = getAttendanceStatusFromString(statusStr);

            // 如果未设置时间且不是未归状态，则使用当前时间
            if (checkInTime == null && status != AttendanceStatus.ABSENT) {
                checkInTime = new Date();
            }

            // 如果状态为"自动判断"，根据时间判断状态
            if ("自动判断".equals(statusStr)) {
                status = AttendanceUtil.getStatusByTime(checkInTime);
            }

            // 创建Attendance对象
            Attendance attendance = new Attendance();
            attendance.setStudentId(studentId);
            attendance.setStudentName(studentName);
            attendance.setDormitory(dormitory);
            attendance.setAttendanceDate(attendanceDate);
            attendance.setCheckInTime(checkInTime);
            attendance.setStatus(status);
            attendance.setRemark(remark);
            attendance.setRecordTime(new Date());

            // 保存考勤记录
            boolean success = attendanceManager.addAttendance(attendance);

            if (success) {
                // 添加到表格
                addAttendanceToTable(attendance);

                // 更新统计信息
                updateStatistics();

                JOptionPane.showMessageDialog(dialog, "考勤登记成功！", "成功", JOptionPane.INFORMATION_MESSAGE);

                // 清空表单
                studentIdField.setText("");
                nameField.setText("");
                dormField.setText("");
                remarkField.setText("");
            } else {
                JOptionPane.showMessageDialog(dialog, "考勤登记失败！", "错误", JOptionPane.ERROR_MESSAGE);
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
     * 将考勤记录添加到表格
     */
    private void addAttendanceToTable(Attendance attendance) {
        Object[] row = new Object[9];
        row[0] = attendance.getId();
        row[1] = attendance.getStudentId();
        row[2] = attendance.getStudentName();
        row[3] = attendance.getDormitory();
        row[4] = AttendanceUtil.formatDate(attendance.getAttendanceDate());
        row[5] = AttendanceUtil.formatTime(attendance.getCheckInTime());
        row[6] = attendance.getStatus().getDescription();
        row[7] = attendance.getRemark();
        row[8] = AttendanceUtil.formatDateTime(attendance.getRecordTime());

        tableModel.addRow(row);
    }

    /**
     * 批量导入
     */
    private void batchImport() {
        JOptionPane.showMessageDialog(this, "批量导入功能开发中...", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 导出数据
     */
    private void exportData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("导出考勤数据");
        fileChooser.setSelectedFile(new java.io.File("考勤数据_" +
                new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".csv"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();

            // 模拟导出过程
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this,
                                "考勤数据导出成功！\n文件路径: " + file.getAbsolutePath() + "\n导出记录数: " + tableModel.getRowCount(),
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
     * 生成报表
     */
    private void generateReport() {
        // 生成考勤报表
        List<Attendance> allAttendances = attendanceManager.getAllAttendances();

        if (allAttendances.isEmpty()) {
            JOptionPane.showMessageDialog(this, "暂无考勤数据！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 生成报表内容
        StringBuilder report = new StringBuilder();
        report.append("考勤统计报表\n");
        report.append("========================\n\n");
        report.append("统计时间: ").append(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n\n");

        // 今日统计
        Map<String, Integer> todayStats = attendanceManager.getTodayStatistics();
        report.append("今日考勤统计:\n");
        report.append("• 总记录数: ").append(todayStats.get("total")).append("\n");
        report.append("• 正常归寝: ").append(todayStats.get("normal")).append("\n");
        report.append("• 晚归人数: ").append(todayStats.get("late")).append("\n");
        report.append("• 未归人数: ").append(todayStats.get("absent")).append("\n");
        report.append("• 请假人数: ").append(todayStats.get("leave")).append("\n\n");

        // 本月统计
        Map<String, Object> monthStats = attendanceManager.getMonthStatistics();
        report.append("本月考勤统计:\n");
        report.append("• 总记录数: ").append(monthStats.get("total")).append("\n");
        report.append("• 正常次数: ").append(monthStats.get("normal")).append("\n");
        report.append("• 晚归次数: ").append(monthStats.get("late")).append("\n");
        report.append("• 未归次数: ").append(monthStats.get("absent")).append("\n");
        report.append("• 请假次数: ").append(monthStats.get("leave")).append("\n");
        report.append("• 出勤率: ").append(monthStats.get("attendanceRate")).append("\n\n");

        // 异常分析
        List<Attendance> abnormalAttendances = attendanceManager.getAbnormalAttendances();
        if (!abnormalAttendances.isEmpty()) {
            report.append("异常情况分析:\n");
            report.append("• 异常总数: ").append(abnormalAttendances.size()).append("\n");
            report.append("• 晚归高峰时段: ").append(attendanceManager.getLatePeakTime()).append("\n");
            report.append("• 重点关注宿舍: ").append(String.join(", ", attendanceManager.getFocusDormitories())).append("\n");
        }

        // 显示报表
        JTextArea reportArea = new JTextArea(report.toString());
        reportArea.setEditable(false);
        reportArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(reportArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));

        JOptionPane.showMessageDialog(this, scrollPane, "考勤统计报表", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 发送提醒
     */
    private void sendReminder() {
        int[] selectedRows = attendanceTable.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "请选择要发送提醒的记录！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StringBuilder message = new StringBuilder();
        message.append("将向以下学生发送提醒：\n\n");

        for (int row : selectedRows) {
            String name = tableModel.getValueAt(row, 2).toString();
            String status = tableModel.getValueAt(row, 6).toString();
            message.append(name).append(" (").append(status).append(")\n");
        }

        message.append("\n确认发送提醒？");

        int confirm = JOptionPane.showConfirmDialog(this, message.toString(),
                "发送提醒确认", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this, "提醒已发送！", "成功", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * 查看异常
     */
    private void viewAbnormal() {
        List<Attendance> abnormalAttendances = attendanceManager.getAbnormalAttendances();

        if (abnormalAttendances.isEmpty()) {
            JOptionPane.showMessageDialog(this, "暂无异常考勤记录！", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 筛选显示异常记录
        javax.swing.RowFilter<DefaultTableModel, Object> filter = new javax.swing.RowFilter<DefaultTableModel, Object>() {
            public boolean include(javax.swing.RowFilter.Entry<? extends DefaultTableModel, ? extends Object> entry) {
                String status = entry.getStringValue(6);
                return "晚归".equals(status) || "未归".equals(status);
            }
        };

        javax.swing.table.TableRowSorter<DefaultTableModel> sorter =
                new javax.swing.table.TableRowSorter<>(tableModel);
        sorter.setRowFilter(filter);
        attendanceTable.setRowSorter(sorter);

        JOptionPane.showMessageDialog(this,
                "已筛选显示 " + abnormalAttendances.size() + " 条异常考勤记录",
                "异常考勤",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 导出今日
     */
    private void exportToday() {
        List<Attendance> todayAttendances = attendanceManager.getTodayAttendances();

        if (todayAttendances.isEmpty()) {
            JOptionPane.showMessageDialog(this, "今日暂无考勤记录！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("导出今日考勤");
        fileChooser.setSelectedFile(new java.io.File("今日考勤_" +
                new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date()) + ".csv"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();

            JOptionPane.showMessageDialog(this,
                    "今日考勤记录：" + todayAttendances.size() + "条\n" +
                            "文件路径: " + file.getAbsolutePath(),
                    "今日记录",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * 发送通知
     */
    private void sendNotification() {
        String[] options = {"晚归学生", "未归学生", "全体学生"};
        String choice = (String) JOptionPane.showInputDialog(this,
                "选择通知对象：", "发送通知",
                JOptionPane.QUESTION_MESSAGE, null,
                options, options[0]);

        if (choice != null) {
            // 获取对应状态的学生
            List<Attendance> targetAttendances;
            switch (choice) {
                case "晚归学生":
                    targetAttendances = attendanceManager.getAttendancesByStatus(AttendanceStatus.LATE);
                    break;
                case "未归学生":
                    targetAttendances = attendanceManager.getAttendancesByStatus(AttendanceStatus.ABSENT);
                    break;
                default:
                    targetAttendances = attendanceManager.getTodayAttendances();
                    break;
            }

            int count = targetAttendances.size();
            JOptionPane.showMessageDialog(this,
                    "已向" + choice + "发送通知（共" + count + "人）\n通知内容：请按时归寝，注意安全",
                    "通知发送",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * 生成月报
     */
    private void generateMonthlyReport() {
        Map<String, Object> monthStats = attendanceManager.getMonthStatistics();

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "月度考勤报告", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JTextArea reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        // 生成报告内容
        StringBuilder report = new StringBuilder();
        report.append("             月度考勤报告\n");
        report.append("================================\n\n");
        report.append("报告月份：").append(new java.text.SimpleDateFormat("yyyy年MM月").format(new Date())).append("\n");
        report.append("统计时间：").append(AttendanceUtil.formatDateTime(new Date())).append("\n\n");
        report.append("统计结果：\n");
        report.append("1. 总考勤人次：").append(monthStats.get("total")).append("\n");
        report.append("2. 正常归寝：").append(monthStats.get("normal")).append(" (").append(monthStats.get("attendanceRate")).append(")\n");
        report.append("3. 晚归人次：").append(monthStats.get("late")).append("\n");
        report.append("4. 未归人次：").append(monthStats.get("absent")).append("\n");
        report.append("5. 请假人次：").append(monthStats.get("leave")).append("\n\n");

        // 异常情况分析
        report.append("异常情况分析：\n");
        report.append("• 晚归高峰时段：").append(attendanceManager.getLatePeakTime()).append("\n");

        List<String> focusDorms = attendanceManager.getFocusDormitories();
        if (!focusDorms.isEmpty()) {
            report.append("• 重点关注宿舍：").append(String.join(", ", focusDorms)).append("\n");
        }

        report.append("• 主要晚归原因：学习、社团活动、外出实习\n\n");

        report.append("建议措施：\n");
        report.append("1. 加强22:30后的巡查\n");
        report.append("2. 开展安全教育活动\n");
        report.append("3. 优化社团活动时间安排\n");
        report.append("4. 建立家长联系机制\n");

        reportArea.setText(report.toString());

        JScrollPane scrollPane = new JScrollPane(reportArea);
        dialog.add(scrollPane);
        dialog.setVisible(true);
    }

    /**
     * 筛选考勤记录
     */
    private void filterAttendance() {
        String building = (String) buildingFilter.getSelectedItem();
        String status = (String) statusFilter.getSelectedItem();

        javax.swing.RowFilter<DefaultTableModel, Object> filter = new javax.swing.RowFilter<DefaultTableModel, Object>() {
            public boolean include(javax.swing.RowFilter.Entry<? extends DefaultTableModel, ? extends Object> entry) {
                boolean buildingMatch = "全部".equals(building) ||
                        entry.getStringValue(3).startsWith(building.replace("栋", ""));
                boolean statusMatch = "全部".equals(status) ||
                        entry.getStringValue(6).equals(status);
                return buildingMatch && statusMatch;
            }
        };

        javax.swing.table.TableRowSorter<DefaultTableModel> sorter =
                new javax.swing.table.TableRowSorter<>(tableModel);
        sorter.setRowFilter(filter);
        attendanceTable.setRowSorter(sorter);

        // 显示筛选结果
        int filteredCount = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (sorter.getViewRowCount() > i) {
                filteredCount++;
            }
        }

        JOptionPane.showMessageDialog(this,
                "已筛选出 " + filteredCount + " 条记录",
                "筛选结果",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 按日期查询
     */
    private void queryByDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入查询日期！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        javax.swing.RowFilter<DefaultTableModel, Object> filter = new javax.swing.RowFilter<DefaultTableModel, Object>() {
            public boolean include(javax.swing.RowFilter.Entry<? extends DefaultTableModel, ? extends Object> entry) {
                return entry.getStringValue(4).equals(date.trim());
            }
        };

        javax.swing.table.TableRowSorter<DefaultTableModel> sorter =
                new javax.swing.table.TableRowSorter<>(tableModel);
        sorter.setRowFilter(filter);
        attendanceTable.setRowSorter(sorter);

        int count = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (date.trim().equals(tableModel.getValueAt(i, 4).toString())) {
                count++;
            }
        }

        JOptionPane.showMessageDialog(this,
                "日期 " + date + " 的考勤记录：" + count + "条",
                "查询结果",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 加载考勤数据
     */
    private void loadAttendances() {
        List<Attendance> attendances = attendanceManager.getAllAttendances();

        if (attendances.isEmpty()) {
            // 如果没有数据，加载示例数据
            loadSampleData();
        } else {
            // 加载真实数据
            for (Attendance attendance : attendances) {
                addAttendanceToTable(attendance);
            }
        }
    }

    /**
     * 更新统计信息
     */
    private void updateStatistics() {
        // 更新今日统计
        Map<String, Integer> todayStats = attendanceManager.getTodayStatistics();
        todayTotalLabel.setText(String.valueOf(todayStats.get("total")));
        todayNormalLabel.setText(String.valueOf(todayStats.get("normal")));
        todayLateLabel.setText(String.valueOf(todayStats.get("late")));
        todayAbsentLabel.setText(String.valueOf(todayStats.get("absent")));
        todayLeaveLabel.setText(String.valueOf(todayStats.get("leave")));

        // 更新本月统计
        Map<String, Object> monthStats = attendanceManager.getMonthStatistics();
        monthTotalLabel.setText(monthStats.get("normal") + "天");
        monthLateLabel.setText(monthStats.get("late") + "次");
        monthAbsentLabel.setText(monthStats.get("absent") + "次");
        monthLeaveLabel.setText(monthStats.get("leave") + "次");
        monthRateLabel.setText((String) monthStats.get("attendanceRate"));
    }

    /**
     * 加载示例数据（仅在没有真实数据时使用）
     */
    private void loadSampleData() {
        try {
            // 创建示例考勤数据
            Attendance[] sampleAttendances = {
                    new Attendance("20230001", "张三", "A101",
                            AttendanceUtil.parseDate("2024-01-15"),
                            AttendanceUtil.parseTime("22:15"),
                            AttendanceStatus.NORMAL, ""),
                    new Attendance("20230002", "李四", "B202",
                            AttendanceUtil.parseDate("2024-01-15"),
                            AttendanceUtil.parseTime("23:45"),
                            AttendanceStatus.LATE, "社团活动"),
                    new Attendance("20230003", "王五", "C303",
                            AttendanceUtil.parseDate("2024-01-15"),
                            null,
                            AttendanceStatus.ABSENT, "请假回家"),
                    new Attendance("20230004", "赵六", "A102",
                            AttendanceUtil.parseDate("2024-01-15"),
                            AttendanceUtil.parseTime("22:30"),
                            AttendanceStatus.NORMAL, ""),
                    new Attendance("20230005", "钱七", "B201",
                            AttendanceUtil.parseDate("2024-01-15"),
                            AttendanceUtil.parseTime("23:30"),
                            AttendanceStatus.LATE, "图书馆学习"),
                    new Attendance("20230006", "孙八", "C304",
                            AttendanceUtil.parseDate("2024-01-15"),
                            AttendanceUtil.parseTime("22:00"),
                            AttendanceStatus.NORMAL, ""),
                    new Attendance("20230007", "周九", "A103",
                            AttendanceUtil.parseDate("2024-01-15"),
                            null,
                            AttendanceStatus.ABSENT, "实习未归"),
                    new Attendance("20230008", "吴十", "B203",
                            AttendanceUtil.parseDate("2024-01-15"),
                            AttendanceUtil.parseTime("22:45"),
                            AttendanceStatus.NORMAL, ""),
                    new Attendance("20230009", "郑十一", "C305",
                            AttendanceUtil.parseDate("2024-01-15"),
                            AttendanceUtil.parseTime("22:20"),
                            AttendanceStatus.NORMAL, ""),
                    new Attendance("20230010", "王十二", "A104",
                            AttendanceUtil.parseDate("2024-01-15"),
                            null,
                            AttendanceStatus.LEAVE, "生病请假")
            };

            for (Attendance attendance : sampleAttendances) {
                attendanceManager.addAttendance(attendance);
                addAttendanceToTable(attendance);
            }

            // 更新统计信息
            updateStatistics();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}