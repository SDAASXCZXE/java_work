package ui;

import model.Student;
import model.Repair;
import model.Holiday;
import model.RoomChange;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 学生个人面板 - 学生登录后可见
 */
public class StudentDashboardPanel extends JPanel {
    private JTable repairTable;
    private DefaultTableModel repairTableModel;
    private JTable attendanceTable;
    private DefaultTableModel attendanceTableModel;

    // 使用Student对象存储学生信息
    private Student student;

    // 数据存储列表
    private List<Repair> repairList = new ArrayList<>();
    private List<Holiday> holidayList = new ArrayList<>();
    private List<RoomChange> roomChangeList = new ArrayList<>();

    // 日期格式化器
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public StudentDashboardPanel(String studentId, String studentName) {
        // 创建Student对象
        this.student = new Student();
        this.student.setSno(studentId);
        this.student.setName(studentName);

        // 设置学生信息（包括正确的入学日期）
        this.student.setGender("男");
        this.student.setCollege("计算机学院");
        this.student.setMajor("软件工程");
        this.student.setClazz("1班");
        this.student.setPhone("13800138001");

        // 正确设置入学日期为LocalDate
        try {
            LocalDate inDate = LocalDate.parse("2023-09-01");
            this.student.setInDate(inDate);
        } catch (DateTimeParseException e) {
            // 如果解析失败，使用当前日期
            this.student.setInDate(LocalDate.now());
            System.err.println("设置入学日期失败，使用当前日期: " + e.getMessage());
        }

        initUI();
        loadSampleData();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 顶部欢迎信息
        add(createWelcomePanel(), BorderLayout.NORTH);

        // 中间主面板（使用选项卡）
        JTabbedPane mainTabs = new JTabbedPane(JTabbedPane.TOP);
        mainTabs.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        // 添加功能选项卡
        mainTabs.addTab("宿舍报修", createRepairPanel());
        mainTabs.addTab("换宿/退宿申请", createTransferPanel());
        mainTabs.addTab("假期登记", createHolidayPanel());
        mainTabs.addTab("我的考勤", createMyAttendancePanel());
        mainTabs.addTab("个人信息", createProfilePanel());

        add(mainTabs, BorderLayout.CENTER);

        // 底部状态栏
        add(createStatusPanel(), BorderLayout.SOUTH);
    }

    /**
     * 创建欢迎面板
     */
    private JPanel createWelcomePanel() {
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setBackground(new Color(70, 130, 180));
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // 左侧欢迎信息
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setBackground(new Color(70, 130, 180));

        JLabel welcomeLabel = new JLabel("欢迎您，" + student.getName() + "同学！");
        welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);

        JLabel detailLabel = new JLabel("学号：" + student.getSno() + " | 学院：" + student.getCollege());
        detailLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        detailLabel.setForeground(Color.WHITE);

        infoPanel.add(welcomeLabel);
        infoPanel.add(detailLabel);

        welcomePanel.add(infoPanel, BorderLayout.WEST);

        // 右侧快速操作
        JPanel quickPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        quickPanel.setBackground(new Color(70, 130, 180));

        String[] quickActions = {"一键报修", "查看公告", "联系宿管"};
        for (String action : quickActions) {
            JButton button = new JButton(action);
            button.setBackground(Color.WHITE);
            button.setForeground(new Color(70, 130, 180));
            button.setFocusPainted(false);
            button.addActionListener(e -> handleQuickAction(action));
            quickPanel.add(button);
        }

        welcomePanel.add(quickPanel, BorderLayout.EAST);

        return welcomePanel;
    }

    /**
     * 创建报修面板
     */
    private JPanel createRepairPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // 顶部工具栏
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolBar.setBorder(BorderFactory.createTitledBorder("宿舍报修管理"));

        JButton newRepairBtn = new JButton("新建报修");
        newRepairBtn.setBackground(new Color(70, 130, 180));
        newRepairBtn.setForeground(Color.WHITE);
        newRepairBtn.addActionListener(e -> createNewRepair());

        JButton uploadImageBtn = new JButton("上传图片");
        uploadImageBtn.addActionListener(e -> uploadRepairImage());

        JButton trackBtn = new JButton("跟踪进度");
        trackBtn.addActionListener(e -> trackRepairProgress());

        toolBar.add(newRepairBtn);
        toolBar.add(uploadImageBtn);
        toolBar.add(trackBtn);

        panel.add(toolBar, BorderLayout.NORTH);

        // 中间表格
        String[] columns = {"报修ID", "报修时间", "问题类型", "问题描述", "状态", "处理进度"};
        repairTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        repairTable = new JTable(repairTableModel);
        repairTable.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(repairTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("我的报修记录"));

        panel.add(scrollPane, BorderLayout.CENTER);

        // 底部统计信息
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statsPanel.add(new JLabel("已提交报修：3  |  处理中：1  |  已完成：1"));

        panel.add(statsPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * 创建换宿/退宿申请面板
     */
    private JPanel createTransferPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("宿舍调整申请"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 申请类型
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("申请类型："), gbc);

        gbc.gridx = 1;
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"请选择", "申请换宿", "申请退宿", "申请调整床位"});
        panel.add(typeCombo, gbc);

        // 当前班级
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("当前班级："), gbc);

        gbc.gridx = 1;
        JTextField currentClassField = new JTextField(student.getClazz());
        currentClassField.setEditable(false);
        panel.add(currentClassField, gbc);

        // 目标宿舍（换宿时使用）
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("目标宿舍楼栋："), gbc);

        gbc.gridx = 1;
        JTextField newBuildingField = new JTextField();
        newBuildingField.setEnabled(false);
        panel.add(newBuildingField, gbc);

        // 目标宿舍号
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("目标宿舍号："), gbc);

        gbc.gridx = 1;
        JTextField newRoomField = new JTextField();
        newRoomField.setEnabled(false);
        panel.add(newRoomField, gbc);

        // 申请原因
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("申请原因："), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        JTextArea reasonArea = new JTextArea(4, 30);
        JScrollPane reasonScroll = new JScrollPane(reasonArea);
        panel.add(reasonScroll, gbc);

        // 类型选择监听
        typeCombo.addActionListener(e -> {
            String selected = (String) typeCombo.getSelectedItem();
            boolean enabled = "申请换宿".equals(selected);
            newBuildingField.setEnabled(enabled);
            newRoomField.setEnabled(enabled);
        });

        // 按钮区域
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton submitBtn = new JButton("提交申请");
        submitBtn.setBackground(new Color(70, 130, 180));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.addActionListener(e -> submitTransferApplication(typeCombo, newBuildingField, newRoomField, reasonArea));

        JButton viewBtn = new JButton("查看申请记录");
        viewBtn.addActionListener(e -> viewTransferRecords());

        buttonPanel.add(submitBtn);
        buttonPanel.add(viewBtn);

        panel.add(buttonPanel, gbc);

        return panel;
    }

    /**
     * 创建假期登记面板
     */
    private JPanel createHolidayPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // 登记表单
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("假期离校/返校登记"));

        // 登记类型
        formPanel.add(new JLabel("登记类型："));
        JComboBox<Holiday.HolidayType> typeCombo = new JComboBox<>(Holiday.HolidayType.values());
        typeCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Holiday.HolidayType) {
                    setText(((Holiday.HolidayType) value).getDescription());
                }
                return this;
            }
        });
        formPanel.add(typeCombo);

        // 离校时间
        formPanel.add(new JLabel("离校时间（yyyy-MM-dd）："));
        JTextField leaveDateField = new JTextField();
        leaveDateField.setToolTipText("请输入格式为 yyyy-MM-dd 的日期，例如：2024-01-15");
        formPanel.add(leaveDateField);

        // 预计返校时间
        formPanel.add(new JLabel("预计返校时间（yyyy-MM-dd）："));
        JTextField plannedBackDateField = new JTextField();
        plannedBackDateField.setToolTipText("请输入格式为 yyyy-MM-dd 的日期，例如：2024-02-25");
        formPanel.add(plannedBackDateField);

        // 目的地
        formPanel.add(new JLabel("目的地："));
        JTextField destinationField = new JTextField();
        formPanel.add(destinationField);

        // 紧急联系人
        formPanel.add(new JLabel("紧急联系人："));
        JTextField contactPersonField = new JTextField();
        formPanel.add(contactPersonField);

        // 联系电话
        formPanel.add(new JLabel("联系电话："));
        JTextField contactPhoneField = new JTextField();
        formPanel.add(contactPhoneField);

        panel.add(formPanel, BorderLayout.NORTH);

        // 按钮区域
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton registerBtn = new JButton("提交登记");
        registerBtn.setBackground(new Color(46, 139, 87));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.addActionListener(e -> submitHolidayRegistration(
                typeCombo, leaveDateField, plannedBackDateField,
                destinationField, contactPersonField, contactPhoneField));

        JButton recordBtn = new JButton("查看登记记录");
        recordBtn.addActionListener(e -> viewHolidayRecords());

        buttonPanel.add(registerBtn);
        buttonPanel.add(recordBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * 创建我的考勤面板
     */
    private JPanel createMyAttendancePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // 考勤统计
        JPanel statsPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        statsPanel.setBorder(BorderFactory.createTitledBorder("考勤统计"));

        String[] statsLabels = {"本月正常", "本月晚归", "本月未归", "累计正常", "累计晚归", "累计未归"};
        String[] statsValues = {"28天", "2次", "0次", "256天", "8次", "1次"};

        for (int i = 0; i < statsLabels.length; i++) {
            JPanel statItem = new JPanel(new FlowLayout(FlowLayout.LEFT));
            statItem.add(new JLabel(statsLabels[i] + ": "));
            JLabel valueLabel = new JLabel(statsValues[i]);
            valueLabel.setForeground(new Color(70, 130, 180));
            valueLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
            statItem.add(valueLabel);
            statsPanel.add(statItem);
        }

        panel.add(statsPanel, BorderLayout.NORTH);

        // 考勤记录表格
        String[] columns = {"日期", "星期", "归寝时间", "状态", "备注"};
        attendanceTableModel = new DefaultTableModel(columns, 0);

        attendanceTable = new JTable(attendanceTableModel);
        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("考勤记录"));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 创建个人信息面板
     */
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 使用Student对象的字段
        String[] labels = {"学号：", "姓名：", "性别：", "学院：", "专业：",
                "班级：", "电话：", "入学日期："};

        // 安全格式化入学日期
        String inDateStr = "未知";
        if (student.getInDate() != null) {
            try {
                inDateStr = student.getInDate().format(DATE_FORMATTER);
            } catch (Exception e) {
                inDateStr = "格式错误";
                System.err.println("格式化入学日期失败: " + e.getMessage());
            }
        }

        String[] values = {
                student.getSno(),
                student.getName(),
                student.getGender(),
                student.getCollege(),
                student.getMajor(),
                student.getClazz(),
                student.getPhone(),
                inDateStr
        };

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0;
            panel.add(new JLabel(labels[i]), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            JTextField field = new JTextField(values[i]);
            field.setEditable(false);
            panel.add(field, gbc);
        }

        return panel;
    }

    /**
     * 创建状态面板
     */
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEtchedBorder());
        panel.setBackground(new Color(240, 240, 240));

        // 左侧状态信息
        JLabel statusLabel = new JLabel("系统状态：正常");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // 右侧时间
        JLabel timeLabel = new JLabel();
        timeLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // 更新时间
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeLabel.setText(DATE_TIME_FORMAT.format(new Date()));
            }
        });
        timer.start();

        panel.add(statusLabel, BorderLayout.WEST);
        panel.add(timeLabel, BorderLayout.EAST);

        return panel;
    }

    /**
     * 处理快速操作
     */
    private void handleQuickAction(String action) {
        switch (action) {
            case "一键报修":
                showQuickRepairDialog();
                break;
            case "查看公告":
                showAnnouncements();
                break;
            case "联系宿管":
                contactDormManager();
                break;
        }
    }

    /**
     * 新建报修（使用Repair类）
     */
    private void createNewRepair() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "新建报修", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 报修类型（使用Repair.RepairType枚举）
        formPanel.add(new JLabel("问题类型："));
        JComboBox<Repair.RepairType> typeCombo = new JComboBox<>(Repair.RepairType.values());
        typeCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Repair.RepairType) {
                    setText(((Repair.RepairType) value).getDescription());
                }
                return this;
            }
        });
        formPanel.add(typeCombo);

        // 学生信息
        formPanel.add(new JLabel("学号："));
        JTextField snoField = new JTextField(student.getSno());
        snoField.setEditable(false);
        formPanel.add(snoField);

        formPanel.add(new JLabel("姓名："));
        JTextField nameField = new JTextField(student.getName());
        nameField.setEditable(false);
        formPanel.add(nameField);

        // 详细描述
        formPanel.add(new JLabel("详细描述："));
        formPanel.add(new JLabel("")); // 占位

        // 多行文本区域
        JTextArea descriptionArea = new JTextArea(5, 20);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);

        dialog.add(formPanel, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton submitBtn = new JButton("提交报修");
        submitBtn.setBackground(new Color(70, 130, 180));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.addActionListener(e -> {
            Repair.RepairType repairType = (Repair.RepairType) typeCombo.getSelectedItem();
            String description = descriptionArea.getText().trim();

            if (description.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请填写问题描述！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 创建Repair对象（假设楼栋和宿舍号从其他信息获取）
            String building = "1号楼"; // 默认值
            String roomNumber = student.getClazz(); // 使用班级作为宿舍号
            Repair repair = Repair.createNewRepair(
                    student.getSno(),
                    roomNumber,
                    building,
                    repairType,
                    description,
                    ""
            );

            // 添加到数据列表
            repairList.add(repair);

            // 添加到表格
            Object[] newRow = {
                    repair.getId(),
                    repair.getSubmitTime().format(DATE_TIME_FORMATTER),
                    repair.getRepairType().getDescription(),
                    repair.getDescription(),
                    repair.getStatus().getDescription(),
                    repair.getProgress() + "%"
            };
            repairTableModel.addRow(newRow);

            JOptionPane.showMessageDialog(dialog, "报修申请已提交！", "成功", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });

        JButton cancelBtn = new JButton("取消");
        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(submitBtn);
        buttonPanel.add(cancelBtn);

        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * 上传报修图片
     */
    private void uploadRepairImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择报修图片");
        fileChooser.setFileFilter(new FileNameExtensionFilter(
                "图片文件", "jpg", "jpeg", "png", "gif"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            JOptionPane.showMessageDialog(this,
                    "图片已选择：" + file.getName() + "\n大小：" + file.length() + " 字节",
                    "图片选择",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * 跟踪维修进度（使用Repair类）
     */
    private void trackRepairProgress() {
        int selectedRow = repairTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要跟踪的报修记录！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String repairId = repairTableModel.getValueAt(selectedRow, 0).toString();

        // 从数据列表中查找对应的Repair对象
        Repair repair = repairList.stream()
                .filter(r -> r.getId().equals(repairId))
                .findFirst()
                .orElse(null);

        if (repair == null) {
            JOptionPane.showMessageDialog(this, "未找到对应的报修记录！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String message = String.format(
                "报修单号：%s\n" +
                        "报修时间：%s\n" +
                        "问题类型：%s\n" +
                        "当前状态：%s\n" +
                        "处理进度：%s%%\n" +
                        "最后更新：%s\n\n" +
                        "维修进度跟踪中...",
                repair.getId(),
                repair.getSubmitTime().format(DATE_TIME_FORMATTER),
                repair.getRepairType().getDescription(),
                repair.getStatus().getDescription(),
                repair.getProgress(),
                repair.getUpdateTime().format(DATE_TIME_FORMATTER)
        );

        JOptionPane.showMessageDialog(this, message, "维修进度", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 提交调宿申请（使用RoomChange类）
     */
    private void submitTransferApplication(JComboBox<String> typeCombo, JTextField newBuildingField,
                                           JTextField newRoomField, JTextArea reasonArea) {
        String type = (String) typeCombo.getSelectedItem();
        String reason = reasonArea.getText().trim();
        String newBuilding = newBuildingField.getText().trim();
        String newRoom = newRoomField.getText().trim();

        if ("请选择".equals(type)) {
            JOptionPane.showMessageDialog(this, "请选择申请类型！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (reason.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写申请原因！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if ("申请换宿".equals(type)) {
            if (newBuilding.isEmpty() || newRoom.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请填写目标宿舍楼栋和宿舍号！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 创建RoomChange对象
            RoomChange roomChange = RoomChange.createNewApplication(
                    student.getSno(),
                    "1号楼", // 原楼栋（默认值）
                    student.getClazz(), // 原宿舍号（用班级代替）
                    newBuilding,
                    newRoom,
                    reason
            );

            // 添加到数据列表
            roomChangeList.add(roomChange);
        }

        String message = String.format("申请类型：%s\n当前班级：%s\n目标宿舍：%s%s%s\n申请原因：%s\n\n确认提交申请？",
                type,
                student.getClazz(),
                "申请换宿".equals(type) ? newBuilding : "",
                "申请换宿".equals(type) && !newRoom.isEmpty() ? "号楼 " : "",
                "申请换宿".equals(type) ? newRoom : "",
                reason);

        int confirm = JOptionPane.showConfirmDialog(this, message, "确认提交", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this, "申请已提交，等待审核！", "成功", JOptionPane.INFORMATION_MESSAGE);
            typeCombo.setSelectedIndex(0);
            newBuildingField.setText("");
            newRoomField.setText("");
            reasonArea.setText("");
        }
    }

    /**
     * 查看调宿记录（使用RoomChange类）
     */
    private void viewTransferRecords() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "调宿申请记录", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);

        String[] columns = {"申请时间", "申请类型", "原宿舍", "目标宿舍", "申请原因", "审核状态"};

        // 创建数据数组
        Object[][] data = new Object[roomChangeList.size()][6];

        for (int i = 0; i < roomChangeList.size(); i++) {
            RoomChange rc = roomChangeList.get(i);
            data[i][0] = rc.getApplyTime().format(DATE_TIME_FORMATTER);
            data[i][1] = "申请换宿"; // 默认类型
            data[i][2] = rc.getOldBuilding() + rc.getOldRoomNumber();
            data[i][3] = rc.getNewBuilding() + rc.getNewRoomNumber();
            data[i][4] = rc.getReason();
            data[i][5] = rc.getStatus().getDescription();
        }

        // 如果没有数据，显示默认数据
        if (roomChangeList.isEmpty()) {
            data = new Object[][] {
                    {"2024-01-15", "申请换宿", "1号楼" + student.getClazz(), "B栋202", "宿舍环境问题", "审核通过"},
                    {"2024-01-10", "申请退宿", "1号楼" + student.getClazz(), "", "毕业离校", "待审核"}
            };
        }

        JTable table = new JTable(data, columns);
        JScrollPane scrollPane = new JScrollPane(table);

        dialog.add(scrollPane);
        dialog.setVisible(true);
    }

    /**
     * 提交假期登记（使用Holiday类）
     */
    private void submitHolidayRegistration(JComboBox<Holiday.HolidayType> typeCombo, JTextField leaveField,
                                           JTextField plannedBackField, JTextField destinationField,
                                           JTextField contactPersonField, JTextField contactPhoneField) {
        Holiday.HolidayType holidayType = (Holiday.HolidayType) typeCombo.getSelectedItem();
        String leaveDateStr = leaveField.getText().trim();
        String plannedBackStr = plannedBackField.getText().trim();
        String destination = destinationField.getText().trim();
        String contactPerson = contactPersonField.getText().trim();
        String contactPhone = contactPhoneField.getText().trim();

        if (leaveDateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写离校时间！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (holidayType == Holiday.HolidayType.LEAVE && plannedBackStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写预计返校时间！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (holidayType == Holiday.HolidayType.LEAVE && destination.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写目的地！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            LocalDate leaveDate = LocalDate.parse(leaveDateStr);
            LocalDate plannedBackDate = plannedBackStr.isEmpty() ? null : LocalDate.parse(plannedBackStr);

            // 创建Holiday对象
            Holiday holiday;
            String building = "1号楼"; // 默认楼栋
            String roomNumber = student.getClazz(); // 使用班级作为宿舍号

            if (holidayType == Holiday.HolidayType.LEAVE) {
                holiday = Holiday.createLeaveRegistration(
                        student.getSno(),
                        roomNumber,
                        building,
                        leaveDate,
                        plannedBackDate
                );
            } else {
                // 返校登记
                holiday = Holiday.createBackRegistration(
                        student.getSno(),
                        roomNumber,
                        building,
                        leaveDate,
                        plannedBackDate,
                        LocalDate.now() // 实际返校日期设为当前日期
                );
            }

            // 设置可选信息
            if (!destination.isEmpty()) holiday.setDestination(destination);
            if (!contactPerson.isEmpty()) holiday.setContactPerson(contactPerson);
            if (!contactPhone.isEmpty()) holiday.setContactPhone(contactPhone);

            // 添加到数据列表
            holidayList.add(holiday);

            String message = String.format("登记类型：%s\n离校时间：%s\n预计返校时间：%s\n目的地：%s\n紧急联系人：%s\n联系电话：%s\n\n确认提交登记？",
                    holidayType.getDescription(), leaveDateStr,
                    plannedBackStr.isEmpty() ? "无" : plannedBackStr,
                    destination, contactPerson, contactPhone);

            int confirm = JOptionPane.showConfirmDialog(this, message, "确认提交", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(this, "假期登记已提交！", "成功", JOptionPane.INFORMATION_MESSAGE);

                // 清空表单
                leaveField.setText("");
                plannedBackField.setText("");
                destinationField.setText("");
                contactPersonField.setText("");
                contactPhoneField.setText("");
            }
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                    "日期格式错误，请使用yyyy-MM-dd格式！例如：2024-01-15",
                    "错误", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "提交失败: " + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 查看假期记录（使用Holiday类）
     */
    private void viewHolidayRecords() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "假期登记记录", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);

        String[] columns = {"登记时间", "登记类型", "离校时间", "返校时间", "目的地", "状态"};

        // 创建数据数组
        Object[][] data = new Object[holidayList.size()][6];

        for (int i = 0; i < holidayList.size(); i++) {
            Holiday holiday = holidayList.get(i);
            data[i][0] = holiday.getRegisterTime().format(DATE_TIME_FORMATTER);
            data[i][1] = holiday.getHolidayType().getDescription();
            data[i][2] = holiday.getLeaveDate().format(DATE_FORMATTER);
            data[i][3] = holiday.getPlannedBackDate() != null ?
                    holiday.getPlannedBackDate().format(DATE_FORMATTER) : "无";
            data[i][4] = holiday.getDestination().orElse("");
            data[i][5] = holiday.getStatus().getDescription();
        }

        // 如果没有数据，显示默认数据
        if (holidayList.isEmpty()) {
            data = new Object[][] {
                    {"2024-01-15 08:30", "离校", "2024-01-15", "2024-02-25", "上海", "已批准"},
                    {"2023-12-30 14:20", "离校", "2023-12-30", "2024-01-05", "北京", "已完成"}
            };
        }

        JTable table = new JTable(data, columns);
        JScrollPane scrollPane = new JScrollPane(table);

        dialog.add(scrollPane);
        dialog.setVisible(true);
    }

    /**
     * 显示一键报修对话框（使用Repair类）
     */
    private void showQuickRepairDialog() {
        String[] problems = {
                "厕所堵塞", "水管漏水", "灯不亮", "空调故障",
                "门窗损坏", "床铺问题", "网络故障", "其他问题"
        };

        String selected = (String) JOptionPane.showInputDialog(this,
                "请选择报修问题：", "一键报修",
                JOptionPane.QUESTION_MESSAGE, null,
                problems, problems[0]);

        if (selected != null) {
            // 根据选择的问题映射到RepairType
            Repair.RepairType repairType;
            switch (selected) {
                case "厕所堵塞":
                case "水管漏水":
                    repairType = Repair.RepairType.WATER_ELEC;
                    break;
                case "家具损坏":
                case "床铺问题":
                    repairType = Repair.RepairType.FURNITURE;
                    break;
                case "网络故障":
                    repairType = Repair.RepairType.NETWORK;
                    break;
                case "空调故障":
                    repairType = Repair.RepairType.HVAC;
                    break;
                default:
                    repairType = Repair.RepairType.OTHER;
            }

            // 创建Repair对象
            String building = "1号楼";
            String roomNumber = student.getClazz();
            Repair repair = Repair.createNewRepair(
                    student.getSno(),
                    roomNumber,
                    building,
                    repairType,
                    "一键报修：" + selected,
                    ""
            );

            // 添加到数据列表
            repairList.add(repair);

            // 添加到表格
            Object[] newRow = {
                    repair.getId(),
                    repair.getSubmitTime().format(DATE_TIME_FORMATTER),
                    repair.getRepairType().getDescription(),
                    repair.getDescription(),
                    repair.getStatus().getDescription(),
                    repair.getProgress() + "%"
            };
            repairTableModel.addRow(newRow);

            JOptionPane.showMessageDialog(this, "一键报修已提交！", "成功", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * 显示公告
     */
    private void showAnnouncements() {
        String announcement = "宿舍管理系统公告\n\n" +
                "1. 寒假离校登记时间为1月20日-2月25日\n" +
                "2. 宿舍楼将于2月26日统一开放\n" +
                "3. 请同学们注意宿舍用电安全\n" +
                "4. 报修服务电话：12345678\n" +
                "5. 紧急情况请联系：13900000000";

        JTextArea textArea = new JTextArea(announcement);
        textArea.setEditable(false);
        textArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        JOptionPane.showMessageDialog(this, scrollPane, "系统公告", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 联系宿管
     */
    private void contactDormManager() {
        String contactInfo = "宿舍管理员联系方式：\n\n" +
                "📞 值班电话：12345678\n" +
                "📱 手机：13900000000\n" +
                "🏢 办公室：学生宿舍1号楼101室\n" +
                "⏰ 工作时间：8:00-20:00\n" +
                "📧 邮箱：dorm@university.edu.cn";

        JOptionPane.showMessageDialog(this, contactInfo, "联系宿管", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 加载示例数据
     */
    private void loadSampleData() {
        try {
            // 加载报修示例数据（使用Repair类）
            Repair repair1 = Repair.createNewRepair(
                    student.getSno(),
                    student.getClazz(),
                    "1号楼",
                    Repair.RepairType.WATER_ELEC,
                    "厕所水管漏水",
                    ""
            );
            repair1.updateProgress(50);
            repairList.add(repair1);

            Repair repair2 = Repair.createNewRepair(
                    student.getSno(),
                    student.getClazz(),
                    "1号楼",
                    Repair.RepairType.HVAC,
                    "空调不制冷",
                    ""
            );
            repair2.updateProgress(100);
            repairList.add(repair2);

            Repair repair3 = Repair.createNewRepair(
                    student.getSno(),
                    student.getClazz(),
                    "1号楼",
                    Repair.RepairType.FURNITURE,
                    "床板断裂",
                    ""
            );
            repairList.add(repair3);

            // 添加到表格
            for (Repair repair : repairList) {
                Object[] row = {
                        repair.getId(),
                        repair.getSubmitTime().format(DATE_TIME_FORMATTER),
                        repair.getRepairType().getDescription(),
                        repair.getDescription(),
                        repair.getStatus().getDescription(),
                        repair.getProgress() + "%"
                };
                repairTableModel.addRow(row);
            }

            // 加载考勤示例数据
            Object[][] attendanceData = {
                    {"2024-01-15", "星期一", "22:15", "正常", ""},
                    {"2024-01-14", "星期日", "23:45", "晚归", "校外活动"},
                    {"2024-01-13", "星期六", "22:30", "正常", ""},
                    {"2024-01-12", "星期五", "22:00", "正常", ""},
                    {"2024-01-11", "星期四", "21:45", "正常", ""}
            };

            for (Object[] row : attendanceData) {
                attendanceTableModel.addRow(row);
            }

            // 加载假期示例数据
            Holiday holiday1 = Holiday.createLeaveRegistration(
                    student.getSno(),
                    student.getClazz(),
                    "1号楼",
                    LocalDate.of(2024, 1, 15),
                    LocalDate.of(2024, 2, 25)
            );
            holiday1.setDestination("上海");
            holiday1.approve();
            holidayList.add(holiday1);

            // 加载调宿示例数据
            RoomChange roomChange1 = RoomChange.createNewApplication(
                    student.getSno(),
                    "1号楼",
                    student.getClazz(),
                    "B栋",
                    "202",
                    "宿舍环境问题"
            );
            roomChange1.approve("admin001", "同意调换");
            roomChangeList.add(roomChange1);

        } catch (Exception e) {
            System.err.println("加载示例数据时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Getter方法
    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    // 获取报修列表（新增方法）
    public List<Repair> getRepairList() {
        return repairList;
    }

    // 获取假期列表（新增方法）
    public List<Holiday> getHolidayList() {
        return holidayList;
    }

    // 获取调宿列表（新增方法）
    public List<RoomChange> getRoomChangeList() {
        return roomChangeList;
    }
}