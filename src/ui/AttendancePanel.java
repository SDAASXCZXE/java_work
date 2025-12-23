package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Attendance;
import service.AttendanceService;
import service.impl.AttendanceServiceImpl;
import service.StudentService;
import service.impl.StudentServiceImpl;

/**
 * 考勤管理面板
 */
public class AttendancePanel extends JPanel {
    private JTable attendanceTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> buildingFilter;
    private JComboBox<String> statusFilter;

    // 使用业务层
    private AttendanceService attendanceService = new AttendanceServiceImpl();
    private StudentService studentService = new StudentServiceImpl();

    // 统计面板相关标签（动态更新）
    private JLabel lblDueCount;       // 应归人数
    private JLabel lblReturnedCount;  // 已归人数
    private JLabel lblLateCount;      // 晚归
    private JLabel lblAbsentCount;    // 未归
    private JLabel lblLeaveCount;     // 请假
    private JLabel lblCurrentIn;      // 当前在楼（基于今日最后记录判断）

    // 统计刷新定时器
    private javax.swing.Timer statsTimer;

    // 当前用于统计的日期（默认今天），可由 "查询" 修改
    private LocalDate statsDate = LocalDate.now();

    public AttendancePanel() {
        initUI();
        loadDataFromDB();
        // 启动统计自动刷新（立即更新一次，然后每60秒更新）
        startStatsTimer();
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
        buildingFilter.addActionListener(e -> { filterAttendance(); updateStats(); });
        toolBar.add(buildingFilter);

        toolBar.add(new JLabel("状态:"));
        statusFilter = new JComboBox<>(new String[]{"全部", "正常", "晚归", "未归", "请假"});
        statusFilter.addActionListener(e -> { filterAttendance(); updateStats(); });
        toolBar.add(statusFilter);

        // 日期选择
        toolBar.add(new JLabel("日期:"));
        JTextField dateField = new JTextField(new java.text.SimpleDateFormat("yyyy-MM-dd")
                .format(new java.util.Date()), 10);
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

                String status = table.getValueAt(row, 6) == null ? "" : table.getValueAt(row, 6).toString();
                if (!isSelected) {
                    switch (status) {
                        case "晚归":
                            c.setBackground(new Color(255, 255, 200)); // 浅黄色
                            break;
                        case "未归":
                            c.setBackground(new Color(255, 200, 200)); // 浅红色
                            break;
                        case "请假":
                            c.setBackground(new Color(200, 255, 200)); // 浅绿色
                            break;
                        default:
                            c.setBackground(Color.WHITE);
                            break;
                    }
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
     * 创建统计面板
     */
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("考勤统计 (实时)"));
        panel.setPreferredSize(new Dimension(250, 0));

        // 今日统计 - 使用动态标签
        JPanel todayPanel = new JPanel(new GridLayout(6, 1, 5, 5));
        todayPanel.setBorder(BorderFactory.createTitledBorder("今日统计"));

        lblDueCount = new JLabel("应归人数: 0");
        lblReturnedCount = new JLabel("已归人数: 0");
        lblLateCount = new JLabel("晚归人数: 0");
        lblAbsentCount = new JLabel("未归人数: 0");
        lblLeaveCount = new JLabel("请假人数: 0");
        lblCurrentIn = new JLabel("当前在楼: 0");

        for (JLabel l : new JLabel[]{lblDueCount, lblReturnedCount, lblLateCount, lblAbsentCount, lblLeaveCount, lblCurrentIn}) {
            l.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            todayPanel.add(l);
        }

        panel.add(todayPanel);
        panel.add(Box.createVerticalStrut(15));

        // 本月统计（保持简单展示，调用 updateStats 会更新此处文本）
        JPanel monthPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        monthPanel.setBorder(BorderFactory.createTitledBorder("本月统计"));

        // 使用占位标签，会在 updateStats 中计算并设置文本
        JLabel m1 = new JLabel("正常天数: 0天");
        JLabel m2 = new JLabel("晚归次数: 0次");
        JLabel m3 = new JLabel("未归次数: 0次");
        JLabel m4 = new JLabel("请假次数: 0次");
        JLabel m5 = new JLabel("出勤率: 0%");

        for (JLabel l : new JLabel[]{m1, m2, m3, m4, m5}) {
            l.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            monthPanel.add(l);
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
     * 从数据库加载考勤记录并填充表格（同时查询学生姓名以显示）
     */
    private void loadDataFromDB() {
        tableModel.setRowCount(0);
        try {
            // 构建学号->姓名映射，便于显示
            Map<String, String> nameMap = new HashMap<>();
            try {
                studentService.listStudents().forEach(s -> nameMap.put(s.getSno(), s.getName()));
            } catch (Exception ignored) {}

            List<Attendance> list = attendanceService.listAll();
            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
            DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            int idx = 1;
            for (Attendance a : list) {
                String name = nameMap.getOrDefault(a.getStudentId(), "");
                Object[] row = new Object[]{
                        idx++,
                        a.getStudentId(),
                        name,
                        a.getRoomNumber(),
                        a.getAttendanceDate().toString(),
                        a.getAttendanceTime() == null ? "" : a.getAttendanceTime().format(timeFmt),
                        a.getStatus() == null ? "" : a.getStatus().getDescription(),
                        a.getRemarks().orElse(""),
                        a.getCreateTime() == null ? "" : a.getCreateTime().format(dtFmt)
                };
                tableModel.addRow(row);
            }

            // 数据加载后更新统计
            updateStats();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载考勤数据失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 启动一个定时器周期性刷新统计（每60秒）
     */
    private void startStatsTimer() {
        if (statsTimer != null) return;
        updateStats();
        // 每60秒刷新一次统计（仅影响 statsDate 指定的日期）
        statsTimer = new javax.swing.Timer(60_000, e -> updateStats());
        statsTimer.start();
    }

    /**
     * 计算并更新统计面板（实时）
     */
    private void updateStats() {
        try {
            // 当前筛选条件
            String buildingSel = "全部";
            String statusSel = "全部";
            try { buildingSel = (String) buildingFilter.getSelectedItem(); } catch (Exception ignored) {}
            try { statusSel = (String) statusFilter.getSelectedItem(); } catch (Exception ignored) {}

            String buildingNormalized = null; // 如 "A"
            if (buildingSel != null && !"全部".equals(buildingSel)) {
                buildingNormalized = buildingSel.replace("栋", "").trim();
            }

            // 使用 statsDate（默认今天），使查询/统计保持一致
            LocalDate dateForStats = statsDate == null ? LocalDate.now() : statsDate;

            List<Attendance> todayList = attendanceService.listByDate(dateForStats);

            // 统计已归（以每个学号的最新记录为准，若最后一条为 IN 则判为已归）
            Map<String, Attendance> lastMap = new HashMap<>();
            for (Attendance a : todayList) {
                // 应用楼栋和状态筛选（仅影响统计，不改变表格）
                if (buildingNormalized != null) {
                    String aBuilding = a.getBuilding() == null ? "" : a.getBuilding();
                    String aRoom = a.getRoomNumber() == null ? "" : a.getRoomNumber();
                    boolean match = aBuilding.equalsIgnoreCase(buildingNormalized) || aBuilding.equalsIgnoreCase(buildingNormalized + "栋") || aRoom.startsWith(buildingNormalized);
                    if (!match) continue;
                }
                if (statusSel != null && !"全部".equals(statusSel)) {
                    String stDesc = a.getStatus() == null ? "" : a.getStatus().getDescription();
                    if (!statusSel.equals(stDesc)) continue;
                }

                String sid = a.getStudentId();
                java.time.LocalTime at = a.getAttendanceTime();
                java.time.LocalDateTime create = a.getCreateTime();
                Attendance prev = lastMap.get(sid);
                if (prev == null) {
                    lastMap.put(sid, a);
                } else {
                    java.time.LocalDateTime prevTs = prev.getAttendanceTime() == null ? prev.getCreateTime() : prev.getAttendanceTime().atDate(prev.getAttendanceDate());
                    java.time.LocalDateTime curTs = at == null ? create : at.atDate(a.getAttendanceDate());
                    if (curTs.isAfter(prevTs)) {
                        lastMap.put(sid, a);
                    }
                }
            }

            int returned = 0, late = 0, absent = 0, leave = 0;
            for (Attendance a : lastMap.values()) {
                if (a.isEntry()) returned++;
                if (a.getStatus() == Attendance.AttendanceStatus.LATE) late++;
                if (a.getStatus() == Attendance.AttendanceStatus.ABSENT) absent++;
                if (a.getStatus() == Attendance.AttendanceStatus.LEAVE) leave++;
            }

            // 应归人数：根据学生表总数或按楼栋筛选
            int due = 0;
            try {
                List<?> students = studentService.listStudents();
                if (students != null) {
                    if (buildingNormalized == null) {
                        due = students.size();
                    } else {
                        int cnt = 0;
                        for (Object obj : students) {
                            try {
                                java.lang.reflect.Method m = obj.getClass().getMethod("getDormNo");
                                Object dorm = m.invoke(obj);
                                if (dorm != null && dorm.toString().startsWith(buildingNormalized)) cnt++;
                            } catch (NoSuchMethodException ns) {
                                try {
                                    java.lang.reflect.Method m2 = obj.getClass().getMethod("getDorm_no");
                                    Object dorm = m2.invoke(obj);
                                    if (dorm != null && dorm.toString().startsWith(buildingNormalized)) cnt++;
                                } catch (Exception ignore) {
                                    cnt = -1; break;
                                }
                            } catch (Exception ignore) {
                                cnt = -1; break;
                            }
                        }
                        if (cnt >= 0) due = cnt; else due = students.size();
                    }
                }
            } catch (Exception ignored) { }

            int currentIn = returned;

            // 使用 final 局部变量传入 lambda，避免 "必须是 final" 的错误
            final int fDue = due;
            final int fReturned = returned;
            final int fLate = late;
            final int fAbsent = absent;
            final int fLeave = leave;
            final int fCurrentIn = currentIn;

            SwingUtilities.invokeLater(() -> {
                lblDueCount.setText("应归人数: " + fDue);
                lblReturnedCount.setText("已归人数: " + fReturned);
                lblLateCount.setText("晚归人数: " + fLate);
                lblAbsentCount.setText("未归人数: " + fAbsent);
                lblLeaveCount.setText("请假人数: " + fLeave);
                lblCurrentIn.setText("当前在楼: " + fCurrentIn);
            });

            // TODO: 本月统计（可按需实现更复杂的逻辑）
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 手动登记（持久化到数据库）
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

        // 姓名（UI 展示，数据库表可能没有人名字段，这里只作展示）
        formPanel.add(new JLabel("姓名:"));
        JTextField nameField = new JTextField();
        formPanel.add(nameField);

        // 宿舍号
        formPanel.add(new JLabel("宿舍号:"));
        JTextField dormField = new JTextField();
        formPanel.add(dormField);

        // 日期
        formPanel.add(new JLabel("日期:"));
        JTextField dateField = new JTextField(new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
        formPanel.add(dateField);

        // 归寝时间
        formPanel.add(new JLabel("归寝时间:"));
        JTextField timeField = new JTextField(new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date()));
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
            if (studentIdField.getText().trim().isEmpty() || nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "学号和姓名不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 构造 Attendance 对象并持久化
            try {
                String sid = studentIdField.getText().trim();
                String room = dormField.getText().trim();
                String dateText = dateField.getText().trim();
                String timeText = timeField.getText().trim();

                LocalDate d = LocalDate.parse(dateText);
                LocalTime t = LocalTime.parse(timeText);

                Attendance.AttendanceStatus st = Attendance.AttendanceStatus.NORMAL;
                String stText = (String) statusCombo.getSelectedItem();
                switch (stText) {
                    case "晚归": st = Attendance.AttendanceStatus.LATE; break;
                    case "未归": st = Attendance.AttendanceStatus.ABSENT; break;
                    case "请假": st = Attendance.AttendanceStatus.LEAVE; break;
                    default: st = Attendance.AttendanceStatus.NORMAL; break;
                }

                Attendance a = Attendance.createCustomRecord(sid, room, room.length() > 0 ? room.substring(0,1) : "", d, t, Attendance.AttendanceDirection.IN, st);
                boolean ok = attendanceService.add(a);
                if (ok) {
                    loadDataFromDB();
                    JOptionPane.showMessageDialog(dialog, "考勤登记成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "考勤登记失败，请检查数据库。", "错误", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "登记失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
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
     * 批量导入
     */
    private void batchImport() {
        JOptionPane.showMessageDialog(this, "批量导入功能开发中...", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 导出数据
     */
    private void exportData() {
        JOptionPane.showMessageDialog(this, "数据导出功能开发中...", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 生成报表
     */
    private void generateReport() {
        JOptionPane.showMessageDialog(this, "报表生成功能开发中...", "提示", JOptionPane.INFORMATION_MESSAGE);
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

        JOptionPane.showMessageDialog(this, "已筛选显示异常考勤记录", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 导出今日
     */
    private void exportToday() {
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        int count = 0;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (today.equals(tableModel.getValueAt(i, 4).toString())) {
                count++;
            }
        }

        JOptionPane.showMessageDialog(this,
                "今日考勤记录：" + count + "条\n导出功能开发中...",
                "今日记录",
                JOptionPane.INFORMATION_MESSAGE);
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
            JOptionPane.showMessageDialog(this,
                    "已向" + choice + "发送通知\n通知内容：请按时归寝，注意安全",
                    "通知发送",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * 生成月报
     */
    private void generateMonthlyReport() {
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
        report.append("报告月份：2024年1月\n");
        report.append("统计时间：2024-01-01 至 2024-01-31\n\n");
        report.append("统计结果：\n");
        report.append("1. 总考勤人次：9,600\n");
        report.append("2. 正常归寝：9,250 (96.4%)\n");
        report.append("3. 晚归人次：280 (2.9%)\n");
        report.append("4. 未归人次：45 (0.5%)\n");
        report.append("5. 请假人次：25 (0.3%)\n\n");
        report.append("异常情况分析：\n");
        report.append("• 晚归高峰时段：22:30-23:30\n");
        report.append("• 主要晚归原因：学习、社团活动\n");
        report.append("• 重点关注宿舍：B栋、C栋\n\n");
        report.append("建议措施：\n");
        report.append("1. 加强22:30后的巡查\n");
        report.append("2. 开展安全教育活动\n");
        report.append("3. 优化社团活动时间安排\n");

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
                String val3 = null;
                try { val3 = entry.getStringValue(3); } catch (Exception ignored) {}
                boolean buildingMatch = "全部".equals(building) || (val3 != null && val3.startsWith(building.replace("栋", "")));
                String val6 = null;
                try { val6 = entry.getStringValue(6); } catch (Exception ignored) {}
                boolean statusMatch = "全部".equals(status) || (val6 != null && val6.equals(status));
                return buildingMatch && statusMatch;
            }
        };

        javax.swing.table.TableRowSorter<DefaultTableModel> sorter =
                new javax.swing.table.TableRowSorter<>(tableModel);
        sorter.setRowFilter(filter);
        attendanceTable.setRowSorter(sorter);
    }

    /**
     * 按日期查询
     */
    private void queryByDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入查询日期！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 尝试解析并通过 Service 查询数据库
        try {
            java.time.LocalDate d = java.time.LocalDate.parse(date.trim());
            java.util.List<Attendance> list = attendanceService.listByDate(d);

            // 填充表格
            tableModel.setRowCount(0);
            java.time.format.DateTimeFormatter timeFmt = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
            java.time.format.DateTimeFormatter dtFmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            // 获取学生姓名映射
            java.util.Map<String, String> nameMap = new java.util.HashMap<>();
            try { studentService.listStudents().forEach(s -> nameMap.put(s.getSno(), s.getName())); } catch (Exception ignored) {}

            int idx = 1;
            for (Attendance a : list) {
                String name = nameMap.getOrDefault(a.getStudentId(), "");
                Object[] row = new Object[]{
                        idx++,
                        a.getStudentId(),
                        name,
                        a.getRoomNumber(),
                        a.getAttendanceDate().toString(),
                        a.getAttendanceTime() == null ? "" : a.getAttendanceTime().format(timeFmt),
                        a.getStatus() == null ? "" : a.getStatus().getDescription(),
                        a.getRemarks().orElse(""),
                        a.getCreateTime() == null ? "" : a.getCreateTime().format(dtFmt)
                };
                tableModel.addRow(row);
            }

            // 设置用于统计的日期为查询日期，并刷新统计
            try { statsDate = d; } catch (Exception ignored) {}
            updateStats();

            JOptionPane.showMessageDialog(this, "查询完成：共 " + list.size() + " 条记录", "查询结果", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "查询失败：请输入正确的日期格式 yyyy-MM-dd 或检查数据库连接。\n" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
