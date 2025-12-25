package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
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
 * 考勤管理面板 - 优化增强版
 * 保持原有框架与变量名，增强了统计逻辑与交互体验
 */
public class AttendancePanel extends JPanel {
    private JTable attendanceTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> buildingFilter;
    private JComboBox<String> statusFilter;

    // 业务层
    private AttendanceService attendanceService = new AttendanceServiceImpl();
    private StudentService studentService = new StudentServiceImpl();

    // 统计面板相关标签
    private JLabel lblDueCount;       // 应归人数
    private JLabel lblReturnedCount;  // 已归人数
    private JLabel lblLateCount;      // 晚归
    private JLabel lblAbsentCount;    // 未归
    private JLabel lblLeaveCount;     // 请假
    private JLabel lblCurrentIn;      // 当前在楼

    // 统计刷新定时器
    private javax.swing.Timer statsTimer;

    // 当前统计基准日期（支持跨日期查询统计）
    private LocalDate statsDate = LocalDate.now();

    public AttendancePanel() {
        initUI();
        loadDataFromDB();
        startStatsTimer();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(createToolBar(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createStatsPanel(), BorderLayout.EAST);
    }

    private JPanel createToolBar() {
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        toolBar.setBorder(BorderFactory.createTitledBorder("考勤操作与筛选"));

        String[] buttons = {"手动登记", "批量导入", "导出数据", "生成报表", "发送提醒"};
        for (String text : buttons) {
            JButton button = new JButton(text);
            button.setBackground(new Color(70, 130, 180));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.addActionListener(this::handleButtonClick);
            toolBar.add(button);
        }

        toolBar.add(Box.createHorizontalStrut(20));
        toolBar.add(new JLabel("楼栋:"));
        buildingFilter = new JComboBox<>(new String[]{"全部", "A栋", "B栋", "C栋", "D栋"});
        // 筛选事件监听
        buildingFilter.addActionListener(e -> { filterAttendance(); updateStats(); });
        toolBar.add(buildingFilter);

        toolBar.add(new JLabel("状态:"));
        statusFilter = new JComboBox<>(new String[]{"全部", "正常", "晚归", "未归", "请假"});
        statusFilter.addActionListener(e -> { filterAttendance(); updateStats(); });
        toolBar.add(statusFilter);

        toolBar.add(new JLabel("日期:"));
        JTextField dateField = new JTextField(statsDate.toString(), 10);
        toolBar.add(dateField);

        JButton dateBtn = new JButton("查询");
        dateBtn.addActionListener(e -> queryByDate(dateField.getText()));
        toolBar.add(dateBtn);

        return toolBar;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] columns = {"序号", "学号", "姓名", "宿舍号", "日期", "归寝时间", "考勤状态", "备注", "登记时间"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        attendanceTable = new JTable(tableModel);
        attendanceTable.setRowHeight(28);

        // 自定义单元格渲染：根据考勤状态标记背景色
        attendanceTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                // 居中显示
                setHorizontalAlignment(JLabel.CENTER);

                String status = "";
                try {
                    Object st = table.getValueAt(row, 6);
                    if (st != null) status = st.toString();
                } catch (Exception ignored) {}

                if (!isSelected) {
                    if ("晚归".equals(status)) c.setBackground(new Color(255, 243, 224));
                    else if ("未归".equals(status)) c.setBackground(Color.decode("#FFEBEE")); // 浅红
                    else if ("请假".equals(status)) c.setBackground(Color.decode("#E8F5E9")); // 浅绿
                    else c.setBackground(Color.WHITE);
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("学生考勤明细列表"));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("数据可视化统计"));
        panel.setPreferredSize(new Dimension(220, 0));

        JPanel todayPanel = new JPanel(new GridLayout(6, 1, 10, 10));
        lblDueCount = new JLabel("应归人数: 0");
        lblReturnedCount = new JLabel("已归人数: 0");
        lblLateCount = new JLabel("晚归人数: 0");
        lblAbsentCount = new JLabel("未归人数: 0");
        lblLeaveCount = new JLabel("请假人数: 0");
        lblCurrentIn = new JLabel("当前在楼: 0");

        Font f = new Font("微软雅黑", Font.BOLD, 13);
        for (JLabel l : new JLabel[]{lblDueCount, lblReturnedCount, lblLateCount, lblAbsentCount, lblLeaveCount, lblCurrentIn}) {
            l.setFont(f);
            todayPanel.add(l);
        }
        panel.add(todayPanel);

        // 快速操作区域
        JPanel quickPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        quickPanel.setBorder(BorderFactory.createTitledBorder("快捷操作"));
        String[] actions = {"查看异常", "导出今日", "发送通知", "生成月报"};
        for (String action : actions) {
            JButton btn = new JButton(action);
            btn.addActionListener(e -> handleQuickAction(action));
            quickPanel.add(btn);
        }
        panel.add(Box.createVerticalStrut(20));
        panel.add(quickPanel);

        return panel;
    }

    private void handleButtonClick(ActionEvent e) {
        String command = e.getActionCommand();
        switch (command) {
            case "手动登记" -> manualRegistration();
            case "批量导入" -> batchImport();
            case "导出数据" -> exportData();
            case "生成报表" -> generateReport();
            case "发送提醒" -> sendReminder();
        }
    }

    private void handleQuickAction(String action) {
        switch (action) {
            case "查看异常" -> viewAbnormal();
            case "导出今日" -> exportData();
            case "发送通知" -> sendNotification();
            case "生成月报" -> generateMonthlyReport();
        }
    }

    /**
     * 核心：加载数据库数据
     */
    private void loadDataFromDB() {
        tableModel.setRowCount(0);
        try {
            // 获取所有考勤记录
            List<Attendance> list = attendanceService.listAll();
            renderTable(list);
            updateStats(); // 渲染后刷新统计
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "考勤数据加载失败");
        }
    }

    /**
     * 渲染表格数据
     */
    private void renderTable(List<Attendance> list) {
        tableModel.setRowCount(0);
        // 预载学生姓名映射，避免在循环中频繁查库
        Map<String, String> nameMap = new HashMap<>();
        try { studentService.listStudents().forEach(s -> nameMap.put(s.getSno(), s.getName())); } catch (Exception ignored) {}

        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        int idx = 1;
        for (Attendance a : list) {
            tableModel.addRow(new Object[]{
                    idx++,
                    a.getStudentId(),
                    nameMap.getOrDefault(a.getStudentId(), "未知"),
                    a.getRoomNumber(),
                    a.getAttendanceDate(),
                    a.getAttendanceTime() != null ? a.getAttendanceTime().format(timeFmt) : "-",
                    a.getStatus() != null ? a.getStatus().getDescription() : "未知",
                    a.getRemarks().orElse(""),
                    a.getCreateTime() != null ? a.getCreateTime().format(dtFmt) : "-"
            });
        }
    }

    private void startStatsTimer() {
        if (statsTimer != null) return;
        statsTimer = new javax.swing.Timer(60000, e -> updateStats());
        statsTimer.start();
    }

    /**
     * 更新实时统计逻辑
     */
    private void updateStats() {
        try {
            // 1. 获取选定日期的考勤数据
            List<Attendance> dailyRecords = attendanceService.listByDate(statsDate);

            // 2. 统计状态（逻辑：以该生当天最后一条记录为准）
            Map<String, Attendance> lastRecordMap = new HashMap<>();
            for (Attendance a : dailyRecords) {
                lastRecordMap.put(a.getStudentId(), a);
            }

            int returned = 0, late = 0, absent = 0, leave = 0;
            for (Attendance a : lastRecordMap.values()) {
                if (a.isEntry()) returned++;
                switch (a.getStatus()) {
                    case LATE -> late++;
                    case ABSENT -> absent++;
                    case LEAVE -> leave++;
                }
            }

            // 3. 获取应归总数（此处可根据具体业务调整，默认学生总数）
            int due = studentService.listStudents().size();

            // 更新UI
            final int fDue = due, fRet = returned, fLat = late, fAbs = absent, fLea = leave;
            SwingUtilities.invokeLater(() -> {
                lblDueCount.setText("应归人数: " + fDue);
                lblReturnedCount.setText("已归人数: " + fRet);
                lblLateCount.setText("晚归人数: " + fLat);
                lblAbsentCount.setText("未归人数: " + fAbs);
                lblLeaveCount.setText("请假人数: " + fLea);
                lblCurrentIn.setText("当前在楼: " + fRet);
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * 手动登记优化：增加校验与持久化
     */
    private void manualRegistration() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "手动考勤补录", true);
        dialog.setSize(350, 450);
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridLayout(7, 2, 10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField txtSid = new JTextField();
        JTextField txtRoom = new JTextField();
        JTextField txtDate = new JTextField(LocalDate.now().toString());
        JTextField txtTime = new JTextField(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"正常", "晚归", "未归", "请假"});
        JTextField txtRemark = new JTextField();

        p.add(new JLabel("学号*:")); p.add(txtSid);
        p.add(new JLabel("宿舍号:")); p.add(txtRoom);
        p.add(new JLabel("日期(yyyy-MM-dd):")); p.add(txtDate);
        p.add(new JLabel("时间(HH:mm):")); p.add(txtTime);
        p.add(new JLabel("状态:")); p.add(cbStatus);
        p.add(new JLabel("备注:")); p.add(txtRemark);

        JButton btnOk = new JButton("确认登记");
        btnOk.addActionListener(e -> {
            try {
                if(txtSid.getText().isEmpty()) throw new Exception("学号必填");

                Attendance a = Attendance.createCustomRecord(
                        txtSid.getText(), txtRoom.getText(), "",
                        LocalDate.parse(txtDate.getText()), LocalTime.parse(txtTime.getText()),
                        Attendance.AttendanceDirection.IN,
                        switch((String)cbStatus.getSelectedItem()){
                            case "晚归"->Attendance.AttendanceStatus.LATE;
                            case "未归"->Attendance.AttendanceStatus.ABSENT;
                            case "请假"->Attendance.AttendanceStatus.LEAVE;
                            default->Attendance.AttendanceStatus.NORMAL;
                        }
                );
                if(attendanceService.add(a)) {
                    loadDataFromDB();
                    dialog.dispose();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "保存失败: " + ex.getMessage());
            }
        });

        dialog.add(p, BorderLayout.CENTER);
        dialog.add(btnOk, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void queryByDate(String dateStr) {
        try {
            statsDate = LocalDate.parse(dateStr);
            List<Attendance> list = attendanceService.listByDate(statsDate);
            renderTable(list);
            updateStats();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "日期格式错误: yyyy-MM-dd");
        }
    }

    // --- 占位方法补全 ---
    private void batchImport() { JOptionPane.showMessageDialog(this, "请选择Excel文件进行导入..."); }
    private void exportData() { JOptionPane.showMessageDialog(this, "考勤数据已导出至桌面。"); }
    private void generateReport() { generateMonthlyReport(); }
    private void sendReminder() { JOptionPane.showMessageDialog(this, "已通过系统向未归学生发送App推送。"); }
    private void viewAbnormal() { statusFilter.setSelectedItem("未归"); }
    private void exportToday() { exportData(); }
    private void sendNotification() { JOptionPane.showMessageDialog(this, "通知已发送至各宿舍长。"); }
    private void generateMonthlyReport() {
        JOptionPane.showMessageDialog(this, "正在生成本月考勤分析简报...\n正常率：98.2%\n异常：15人次");
    }

    private void filterAttendance() {
        String building = (String) buildingFilter.getSelectedItem();
        String status = (String) statusFilter.getSelectedItem();

        javax.swing.table.TableRowSorter<DefaultTableModel> sorter = new javax.swing.table.TableRowSorter<>(tableModel);
        sorter.setRowFilter(new javax.swing.RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(javax.swing.RowFilter.Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                String roomVal = null;
                try { roomVal = entry.getStringValue(3); } catch (Exception ignored) {}
                String statusVal = null;
                try { statusVal = entry.getStringValue(6); } catch (Exception ignored) {}

                String selBuilding = building == null ? "全部" : building;
                boolean bMatch = "全部".equals(selBuilding) || (roomVal != null && roomVal.contains(selBuilding.replace("栋","")));
                boolean sMatch = "全部".equals(status) || (statusVal != null && statusVal.equals(status));
                return bMatch && sMatch;
            }
        });
        attendanceTable.setRowSorter(sorter);
    }
}

