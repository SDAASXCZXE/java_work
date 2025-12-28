package ui;

import model.Attendance;
import model.Student;
import service.AttendanceService;
import service.StudentService;
import service.impl.AttendanceServiceImpl;
import service.impl.StudentServiceImpl;
import util.RefreshCenter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.List;

/**
 * 考勤管理面板 - 物理删除与实时统计完整版
 */
public class AttendancePanel extends JPanel {
    private JTable attendanceTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> buildingFilter;
    private JComboBox<String> statusFilter;

    private AttendanceService attendanceService = new AttendanceServiceImpl();
    private StudentService studentService = new StudentServiceImpl();

    private JLabel lblDueCount, lblReturnedCount, lblLateCount, lblAbsentCount, lblLeaveCount, lblCurrentIn;

    private List<Attendance> currentDataList = new ArrayList<>();
    // statsDate == null 表示实时模式（使用 LocalDate.now()）；否则使用指定的查询日期
    private LocalDate statsDate = null;
    private javax.swing.Timer statsTimer;

    public AttendancePanel() {
        initUI();
        loadDataFromDB(); // 初始加载
        startStatsTimer(); // 启动定时任务
        // 注册学生或宿舍变更时的刷新回调，确保考勤视图与学生/宿舍信息同步
        RefreshCenter.register("students-updated", this::onExternalDataUpdated);
        RefreshCenter.register("rooms-updated", this::onExternalDataUpdated);
    }

    // 当学生或宿舍数据在其他面板变更时调用，异步刷新考勤数据
    private void onExternalDataUpdated() {
        // 使用 SwingUtilities.invokeLater 简单安排到 EDT
        javax.swing.SwingUtilities.invokeLater(this::loadDataFromDB);
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
        toolBar.setBorder(BorderFactory.createTitledBorder("考勤管理操作"));

        String[] buttons = {"手动登记", "删除记录", "刷新"};
        for (String text : buttons) {
            JButton button = new JButton(text);
            button.setFocusPainted(false);
            button.addActionListener(this::handleButtonClick);
            toolBar.add(button);
        }

        toolBar.add(Box.createHorizontalStrut(20));
        toolBar.add(new JLabel("楼栋:"));
        buildingFilter = new JComboBox<>(new String[]{"全部", "A栋", "B栋", "C栋", "D栋"});
        buildingFilter.addActionListener(e -> filterAttendance());
        toolBar.add(buildingFilter);

        toolBar.add(new JLabel("状态:"));
        statusFilter = new JComboBox<>(new String[]{"全部", "正常", "晚归", "未归", "请假"});
        statusFilter.addActionListener(e -> filterAttendance());
        toolBar.add(statusFilter);

        // --- 新增查询功能（按日期 / 按学号） ---
        toolBar.add(Box.createHorizontalStrut(10));

        toolBar.add(new JLabel("按日期查询:"));
        SpinnerDateModel queryDateModel = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        JSpinner queryDateSpinner = new JSpinner(queryDateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(queryDateSpinner, "yyyy-MM-dd");
        queryDateSpinner.setEditor(dateEditor);
        toolBar.add(queryDateSpinner);
        JButton btnDateQuery = new JButton("查询日期");
        btnDateQuery.addActionListener(e -> {
            Date d = (Date) queryDateSpinner.getValue();
            LocalDate ld = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            searchByDate(ld);
        });
        toolBar.add(btnDateQuery);

        toolBar.add(Box.createHorizontalStrut(10));
        toolBar.add(new JLabel("学号:"));
        JTextField snoField = new JTextField(8);
        toolBar.add(snoField);
        JButton btnSnoQuery = new JButton("查询学号");
        btnSnoQuery.addActionListener(e -> {
            String sno = snoField.getText().trim();
            if (sno.isEmpty()) { JOptionPane.showMessageDialog(this, "请输入学号"); return; }
            searchByStudent(sno);
        });
        toolBar.add(btnSnoQuery);
        // --- 查询功能结束 ---

        return toolBar;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] columns = {"ID", "学号", "姓名", "宿舍号", "日期", "归寝时间", "状态", "备注", "登记来源"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        attendanceTable = new JTable(tableModel);
        attendanceTable.setRowHeight(25);
        // 彻底隐藏第一列 ID，用于删除操作
        attendanceTable.getColumnModel().getColumn(0).setMinWidth(0);
        attendanceTable.getColumnModel().getColumn(0).setMaxWidth(0);
        attendanceTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        panel.add(new JScrollPane(attendanceTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("实时统计"));
        panel.setPreferredSize(new Dimension(200, 0));

        lblDueCount = new JLabel("应归人数: 0");
        lblReturnedCount = new JLabel("已归人数: 0");
        lblLateCount = new JLabel("晚归人数: 0");
        lblAbsentCount = new JLabel("未归人数: 0");
        lblLeaveCount = new JLabel("请假人数: 0");
        lblCurrentIn = new JLabel("当前在楼: 0");

        Font f = new Font("微软雅黑", Font.BOLD, 13);
        for (JLabel l : new JLabel[]{lblDueCount, lblReturnedCount, lblLateCount, lblAbsentCount, lblLeaveCount, lblCurrentIn}) {
            l.setFont(f);
            l.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
            panel.add(l);
        }
        return panel;
    }

    private void handleButtonClick(ActionEvent e) {
        String cmd = e.getActionCommand();
        if ("手动登记".equals(cmd)) manualRegistration();
        else if ("删除记录".equals(cmd)) deleteAttendanceRecord();
        else if ("刷新".equals(cmd)) loadDataFromDB();
    }

    /**
     * 优化：SwingWorker 异步线程加载，防止数据库查询时 UI 假死
     */
    private void loadDataFromDB() {
        new SwingWorker<List<Attendance>, Void>() {
            @Override
            protected List<Attendance> doInBackground() {
                return attendanceService.listAll();
            }
            @Override
            protected void done() {
                try {
                    currentDataList = get();
                    renderTable(currentDataList);
                    // 刷新为实时模式：清除任何之前的日期筛选
                    statsDate = null;
                    updateStats(); // 刷新表格后同步刷新统计（实时）
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    private void renderTable(List<Attendance> list) {
        tableModel.setRowCount(0);
        Map<String, String> nameMap = new HashMap<>();
        studentService.listStudents().forEach(s -> nameMap.put(s.getSno(), s.getName()));

        for (Attendance a : list) {
            tableModel.addRow(new Object[]{
                    a.getId(),
                    a.getStudentId(),
                    nameMap.getOrDefault(a.getStudentId(), "未知"),
                    a.getBuilding() + a.getRoomNumber(),
                    a.getAttendanceDate(),
                    a.getAttendanceTime(),
                    a.getStatus().getDescription(),
                    a.getRemarks().orElse(""),
                    "系统录入"
            });
        }
    }

    /**
     * 优化：统计逻辑，实时拉取学生总数并重新计算
     */
    private void updateStats() {
        List<Student> students = studentService.listStudents();
        // 使用当前日期进行统计（实时）
        LocalDate today = LocalDate.now();
        List<Attendance> records = attendanceService.listByDate(today);

        // 计算基于当天最新记录的统计
        StatsResult r = computeStatsForDate(students, records, today);

        lblDueCount.setText("应归人数: " + r.totalStudents);
        lblReturnedCount.setText("已归人数: " + r.returned);
        lblLateCount.setText("晚归人数: " + r.late);
        lblLeaveCount.setText("请假人数: " + r.leave);
        lblAbsentCount.setText("未归人数: " + r.absent);
        lblCurrentIn.setText("当前在楼: " + r.currentIn);
    }

    /**
     * 根据当前显示的记录刷新统计（用于查询结果）
     */
    private void updateStatsFromList(List<Attendance> records) {
        List<Student> students = studentService.listStudents();
        LocalDate dateToUse = statsDate == null ? LocalDate.now() : statsDate;
        // 只使用目标日期的记录进行统计（records 可能是跨日期的集合，如按学号查询）
        List<Attendance> filtered = new ArrayList<>();
        if (records != null) {
            for (Attendance a : records) {
                if (a == null) continue;
                if (a.getAttendanceDate() != null && a.getAttendanceDate().equals(dateToUse)) filtered.add(a);
            }
        }
        // 计算统计结果（若查询的是今天，则使用实时判断晚归；若是过去日期则以记录中的状态为准）
        StatsResult r = computeStatsForDate(students, filtered, dateToUse);
        lblDueCount.setText("应归人数: " + r.totalStudents);
        lblReturnedCount.setText("已归人数: " + r.returned);
        lblLateCount.setText("晚归人数: " + r.late);
        lblLeaveCount.setText("请假人数: " + r.leave);
        lblAbsentCount.setText("未归人数: " + r.absent);
        lblCurrentIn.setText("当前在楼: " + r.currentIn);
    }

    /**
     * 按日期查询（异步）
     */
    private void searchByDate(LocalDate date) {
        new SwingWorker<List<Attendance>, Void>() {
            @Override
            protected List<Attendance> doInBackground() {
                return attendanceService.listByDate(date);
            }
            @Override
            protected void done() {
                try {
                    currentDataList = get();
                    renderTable(currentDataList);
                    updateStatsFromList(currentDataList);
                    // 设置为按指定日期显示统计（非实时）
                    statsDate = date;
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    /**
     * 按学号查询（异步）
     */
    private void searchByStudent(String studentId) {
        new SwingWorker<List<Attendance>, Void>() {
            @Override
            protected List<Attendance> doInBackground() {
                return attendanceService.listByStudent(studentId);
            }
            @Override
            protected void done() {
                try {
                    currentDataList = get();
                    renderTable(currentDataList);
                    updateStatsFromList(currentDataList);
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    /**
     * 优化：补全手动登记。自动查询学生信息，自动判定晚归
     */
    private void manualRegistration() {
        // 弹出对话框，支持输入学号、选择日期/时间、方向与状态，并可填写备注
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "手动登记考勤", true);
        dialog.setSize(420, 360);
        dialog.setLocationRelativeTo(this);
        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));

        JTextField snoField = new JTextField();
        JLabel nameLabel = new JLabel("(未加载)");
        JLabel dormLabel = new JLabel("(未加载)");
        JLabel buildingLabel = new JLabel("(未加载)");

        panel.add(new JLabel("学号:")); panel.add(snoField);
        JButton loadBtn = new JButton("加载学生");
        panel.add(loadBtn); panel.add(new JLabel(""));
        panel.add(new JLabel("姓名:")); panel.add(nameLabel);
        panel.add(new JLabel("宿舍号:")); panel.add(dormLabel);
        panel.add(new JLabel("楼栋:")); panel.add(buildingLabel);

        panel.add(new JLabel("日期:"));
        SpinnerDateModel dateModel = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        JSpinner dateSpinner = new JSpinner(dateModel);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        panel.add(dateSpinner);

        panel.add(new JLabel("时间:"));
        SpinnerDateModel timeModel = new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE);
        JSpinner timeSpinner = new JSpinner(timeModel);
        timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "HH:mm:ss"));
        panel.add(timeSpinner);

        panel.add(new JLabel("方向:"));
        JComboBox<Attendance.AttendanceDirection> dirCombo = new JComboBox<>(Attendance.AttendanceDirection.values());
        dirCombo.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Attendance.AttendanceDirection) setText(((Attendance.AttendanceDirection) value).getDescription());
                return this;
            }
        });
        panel.add(dirCombo);

        panel.add(new JLabel("状态:"));
        JComboBox<Attendance.AttendanceStatus> statusCombo = new JComboBox<>(Attendance.AttendanceStatus.values());
        statusCombo.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Attendance.AttendanceStatus) setText(((Attendance.AttendanceStatus) value).getDescription());
                return this;
            }
        });
        panel.add(statusCombo);

        panel.add(new JLabel("备注:"));
        JTextField remarksField = new JTextField();
        panel.add(remarksField);

        dialog.add(panel, BorderLayout.CENTER);

        // 加载学生按钮行为
        loadBtn.addActionListener(ev -> {
            String sno = snoField.getText().trim();
            if (sno.isEmpty()) { JOptionPane.showMessageDialog(dialog, "请输入学号后点击加载学生。", "提示", JOptionPane.WARNING_MESSAGE); return; }
            Student s = studentService.getStudentBySno(sno);
            if (s == null) {
                JOptionPane.showMessageDialog(dialog, "数据库中未找到该学号！", "错误", JOptionPane.ERROR_MESSAGE);
                nameLabel.setText("(未找到)"); dormLabel.setText("(未找到)"); buildingLabel.setText("(未找到)");
            } else {
                nameLabel.setText(s.getName());
                dormLabel.setText(s.getRoomNumber() == null ? "" : s.getRoomNumber());
                buildingLabel.setText(s.getBuilding() == null ? "" : s.getBuilding());
            }
        });

        // 底部按钮
        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton okBtn = new JButton("保存");
        JButton cancelBtn = new JButton("取消");
        btnP.add(okBtn); btnP.add(cancelBtn);
        dialog.add(btnP, BorderLayout.SOUTH);

        // 默认状态根据时间设置（22:30 后为晚归）
        okBtn.addActionListener(ev -> {
            String sno = snoField.getText().trim();
            if (sno.isEmpty()) { JOptionPane.showMessageDialog(dialog, "学号不能为空！", "错误", JOptionPane.ERROR_MESSAGE); return; }
            Student s = studentService.getStudentBySno(sno);
            if (s == null) { JOptionPane.showMessageDialog(dialog, "数据库中未找到该学号！", "错误", JOptionPane.ERROR_MESSAGE); return; }

            Date dDate = (Date) dateSpinner.getValue();
            Date dTime = (Date) timeSpinner.getValue();
            LocalDate date = dDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalTime time = dTime.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();

            Attendance.AttendanceDirection dir = (Attendance.AttendanceDirection) dirCombo.getSelectedItem();
            Attendance.AttendanceStatus status = (Attendance.AttendanceStatus) statusCombo.getSelectedItem();

            // 如果用户未主动选择状态（保留默认选择），我们仍然自动判断晚归
            // 但因为用户可以选择，我们尊重用户选择
            if (status == null) {
                status = time.isAfter(LocalTime.of(22, 30)) ? Attendance.AttendanceStatus.LATE : Attendance.AttendanceStatus.NORMAL;
            }

            Attendance a = new Attendance(Attendance.generateId(), sno, s.getRoomNumber(), s.getBuilding(), date, time, dir, status);
            a.setRemarks(remarksField.getText().trim());

            if (attendanceService.add(a)) {
                JOptionPane.showMessageDialog(dialog, "手动登记成功！状态：" + a.getStatus().getDescription());
                dialog.dispose();
                loadDataFromDB();
            } else {
                JOptionPane.showMessageDialog(dialog, "登记失败，请检查数据库连接或数据完整性。", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(ev -> dialog.dispose());

        // 当打开对话框时，将状态默认设为基于当前时间判断
        // 并初始化时间选择器为当前时间
        timeModel.setValue(new Date());
        statusCombo.setSelectedItem(LocalTime.now().isAfter(LocalTime.of(22, 30)) ? Attendance.AttendanceStatus.LATE : Attendance.AttendanceStatus.NORMAL);

        dialog.setVisible(true);
    }

    private void deleteAttendanceRecord() {
        int row = attendanceTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先在表格中选择一行记录");
            return;
        }

        String id = tableModel.getValueAt(row, 0).toString();
        String name = tableModel.getValueAt(row, 2).toString();

        if (JOptionPane.showConfirmDialog(this, "确定从数据库永久删除 [" + name + "] 的这条记录？",
                "物理删除确认", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (attendanceService.removeById(id)) {
                loadDataFromDB();
            }
        }
    }

    private void startStatsTimer() {
        // 每30秒自动执行一次刷新，保持统计数据最新
        statsTimer = new javax.swing.Timer(30000, e -> loadDataFromDB());
        statsTimer.start();
    }

    private void filterAttendance() {
        String building = (String) buildingFilter.getSelectedItem();
        String status = (String) statusFilter.getSelectedItem();
        javax.swing.table.TableRowSorter<DefaultTableModel> sorter = new javax.swing.table.TableRowSorter<>(tableModel);
        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                boolean bMatch = "全部".equals(building) || entry.getStringValue(3).contains(building.replace("栋",""));
                boolean sMatch = "全部".equals(status) || entry.getStringValue(6).equals(status);
                return bMatch && sMatch;
            }
        });
        attendanceTable.setRowSorter(sorter);
    }

    // ---------- 新增：统计计算方法 ----------
    private static class StatsResult {
        int totalStudents;
        int returned; // 已归（今日最后记录为 IN）
        int late;     // 晚归（今日最后 IN 且时间晚于宵禁时间）
        int leave;    // 请假
        int absent;   // 未归（没有记录或最后为 OUT）
        int currentIn; // 当前在楼（与 returned 相同语义）
    }

    /**
     * 计算指定日期的统计数据：按每个学生当天的最新记录判断状态。
     * 若 date 为今天，则会使用当前时间作为实时参考；若为过去日期，则以记录中的状态为准。
     */
    private StatsResult computeStatsForDate(List<Student> students, List<Attendance> records, LocalDate date) {
        StatsResult res = new StatsResult();
        res.totalStudents = students == null ? 0 : students.size();
        if (res.totalStudents == 0) return res;

        // 宵禁时间：22:30
        LocalTime curfew = LocalTime.of(22, 30);
        boolean isToday = LocalDate.now().equals(date);
        LocalTime nowTime = LocalTime.now();

        // 建立 studentId -> 最新记录 映射（按 attendanceTime 比较）
        Map<String, Attendance> latestMap = new HashMap<>();
        if (records != null) {
            for (Attendance a : records) {
                if (a == null || a.getStudentId() == null) continue;
                String sid = a.getStudentId();
                Attendance prev = latestMap.get(sid);
                if (prev == null) latestMap.put(sid, a);
                else {
                    // 比较时间，取时间更晚的那条记录
                    LocalTime t1 = prev.getAttendanceTime();
                    LocalTime t2 = a.getAttendanceTime();
                    if (t1 == null && t2 != null) latestMap.put(sid, a);
                    else if (t1 != null && t2 != null && t2.isAfter(t1)) latestMap.put(sid, a);
                }
            }
        }

        int returned = 0, late = 0, leave = 0, absent = 0;

        for (Student s : students) {
            if (s == null || s.getSno() == null) continue;
            String sid = s.getSno();
            Attendance la = latestMap.get(sid);

            if (la == null) {
                // 无记录:
                if (isToday) {
                    // 若还未到宵禁时间，不计入未归/晚归；否则视为未归
                    if (nowTime.isAfter(curfew)) absent++;
                } else {
                    // 过去日期且无记录 -> 未归
                    absent++;
                }
                continue;
            }

            // 有记录且为请假
            if (la.getStatus() == Attendance.AttendanceStatus.LEAVE) {
                leave++;
                continue;
            }

            // 若最后记录方向为 IN，则视为已归（并判断是否晚归）
            if (la.getDirection() == Attendance.AttendanceDirection.IN) {
                returned++;
                // 晚归判断：优先使用记录中标记的状态；否则当日且入寝时间晚于宵禁则认为晚归
                if (la.getStatus() == Attendance.AttendanceStatus.LATE) {
                    late++;
                } else if (isToday && la.getAttendanceTime() != null && la.getAttendanceTime().isAfter(curfew)) {
                    late++;
                }
            } else {
                // 最后记录为 OUT：当天且未到宵禁，不算未归；超过宵禁或过去日期则视为未归
                if (isToday) {
                    if (nowTime.isAfter(curfew)) absent++;
                } else {
                    absent++;
                }
            }
        }

        res.returned = returned;
        res.late = late;
        res.leave = leave;
        res.absent = Math.max(0, absent);
        res.currentIn = returned; // 当前在楼与返回数一致（以最新 IN 记录为准）
        return res;
    }
}
