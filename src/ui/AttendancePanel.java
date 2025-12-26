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
import java.util.ArrayList;

import model.Attendance;
import model.Student;
import service.AttendanceService;
import service.impl.AttendanceServiceImpl;
import service.StudentService;
import service.impl.StudentServiceImpl;

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
    private LocalDate statsDate = LocalDate.now();
    private javax.swing.Timer statsTimer;

    public AttendancePanel() {
        initUI();
        loadDataFromDB(); // 初始加载
        startStatsTimer(); // 启动定时任务
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
                    updateStats(); // 刷新表格后同步刷新统计
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
        List<Attendance> records = attendanceService.listByDate(statsDate);

        int due = students.size(); // 实时获取当前学生总数
        int ret = 0, lat = 0, lea = 0;

        for (Attendance a : records) {
            if (a.isEntry()) ret++;
            if (a.getStatus() == Attendance.AttendanceStatus.LATE) lat++;
            if (a.getStatus() == Attendance.AttendanceStatus.LEAVE) lea++;
        }

        lblDueCount.setText("应归人数: " + due);
        lblReturnedCount.setText("已归人数: " + ret);
        lblLateCount.setText("晚归人数: " + lat);
        lblLeaveCount.setText("请假人数: " + lea);
        lblAbsentCount.setText("未归人数: " + (due - ret - lea));
        lblCurrentIn.setText("当前在楼: " + ret);
    }

    /**
     * 优化：补全手动登记。自动查询学生信息，自动判定晚归
     */
    private void manualRegistration() {
        String sno = JOptionPane.showInputDialog(this, "请输入需要登记的学号:");
        if (sno == null || sno.trim().isEmpty()) return;

        Student s = studentService.getStudentBySno(sno);
        if (s == null) {
            JOptionPane.showMessageDialog(this, "数据库中未找到该学号！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 构造新对象，使用自动生成的 ID (或者你在构造方法里处理)
        Attendance a = new Attendance(sno, s.getRoomNumber(), s.getBuilding());
        a.setAttendanceDate(LocalDate.now());
        a.setAttendanceTime(LocalTime.now());
        // 逻辑：22:30 以后登记算作晚归
        a.setStatus(LocalTime.now().isAfter(LocalTime.of(22, 30)) ?
                Attendance.AttendanceStatus.LATE : Attendance.AttendanceStatus.NORMAL);

        if (attendanceService.add(a)) {
            JOptionPane.showMessageDialog(this, "手动登记成功！状态：" + a.getStatus().getDescription());
            loadDataFromDB();
        }
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
}
