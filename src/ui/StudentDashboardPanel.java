package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * 学生个人面板 - 学生登录后可见
 */
public class StudentDashboardPanel extends JPanel {
    private JTable repairTable;
    private DefaultTableModel repairTableModel;
    private JTable attendanceTable;
    private DefaultTableModel attendanceTableModel;

    // 当前登录学生信息
    private String studentId;
    private String studentName;
    private String dormitory;

    @SuppressWarnings("FieldMayBeFinal")
    private service.HolidayService holidayService = new service.impl.HolidayServiceImpl(); // 假期服务实例

    public StudentDashboardPanel(String studentId, String studentName, String dormitory) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.dormitory = dormitory;

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

        JLabel welcomeLabel = new JLabel("欢迎您，" + studentName + "同学！");
        welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);

        JLabel detailLabel = new JLabel("学号：" + studentId + " | 宿舍：" + dormitory);
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
        statsPanel.add(new JLabel("已提交报修：0  |  处理中：0  |  已完成：0"));

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
        String[] types = {"请选择", "申请换宿", "申请退宿", "申请调整床位"};
        JComboBox<String> typeCombo = new JComboBox<>(types);
        panel.add(typeCombo, gbc);

        // 当前宿舍
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("当前宿舍："), gbc);

        gbc.gridx = 1;
        JTextField currentDormField = new JTextField(dormitory);
        currentDormField.setEditable(false);
        panel.add(currentDormField, gbc);

        // 目标宿舍（换宿时使用）
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("目标宿舍："), gbc);

        gbc.gridx = 1;
        JTextField targetDormField = new JTextField();
        targetDormField.setEnabled(false);
        panel.add(targetDormField, gbc);

        // 申请原因
        gbc.gridx = 0;
        gbc.gridy = 3;
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
            targetDormField.setEnabled("申请换宿".equals(selected));
        });

        // 按钮区域
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton submitBtn = new JButton("提交申请");
        submitBtn.setBackground(new Color(70, 130, 180));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.addActionListener(e -> submitTransferApplication(typeCombo, targetDormField, reasonArea));

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
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"离校登记", "返校登记"});
        formPanel.add(typeCombo);

        // 离校时间
        formPanel.add(new JLabel("离校时间："));
        JTextField leaveDateField = new JTextField();
        formPanel.add(leaveDateField);

        // 预计返校时间
        formPanel.add(new JLabel("返校时间："));
        JTextField returnDateField = new JTextField();
        formPanel.add(returnDateField);

        // 目的地
        formPanel.add(new JLabel("目的地："));
        JTextField destinationField = new JTextField();
        formPanel.add(destinationField);

        // 紧急联系人
        formPanel.add(new JLabel("紧急联系人："));
        JTextField emergencyContactField = new JTextField();
        formPanel.add(emergencyContactField);

        // 联系电话
        formPanel.add(new JLabel("联系电话："));
        JTextField emergencyPhoneField = new JTextField();
        formPanel.add(emergencyPhoneField);

        panel.add(formPanel, BorderLayout.NORTH);

        // 按钮区域
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton registerBtn = new JButton("提交登记");
        registerBtn.setBackground(new Color(46, 139, 87));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.addActionListener(e -> submitHolidayRegistration(
                typeCombo, leaveDateField, returnDateField,
                destinationField, emergencyContactField, emergencyPhoneField));

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

        String[] labels = {"学号：", "姓名：", "性别：", "学院：", "专业：",
                "班级：", "宿舍：", "床位：", "联系电话：", "入住日期："};

        String[] values = {studentId, studentName, "男", "计算机学院", "软件工程",
                "1班", dormitory, "1号床", "13800138001", "2023-09-01"};

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
        Timer timer = new Timer(1000, e -> {
            timeLabel.setText(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(new java.util.Date()));
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
     * 新建报修
     */
    private void createNewRepair() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "新建报修", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 报修类型
        formPanel.add(new JLabel("问题类型："));
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{
                "水电问题", "家具损坏", "电器故障", "门窗问题", "卫生问题", "其他"
        });
        formPanel.add(typeCombo);

        // 宿舍信息
        formPanel.add(new JLabel("宿舍号："));
        JTextField dormField = new JTextField(dormitory);
        dormField.setEditable(false);
        formPanel.add(dormField);

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
            String type = (String) typeCombo.getSelectedItem();
            String description = descriptionArea.getText().trim();

            if (description.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请填写问题描述！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 添加到表格
            Object[] newRow = {
                    "R" + System.currentTimeMillis(),
                    new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date()),
                    type,
                    description,
                    "待处理",
                    "0%"
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
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
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
     * 跟踪维修进度
     */
    private void trackRepairProgress() {
        int selectedRow = repairTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要跟踪的报修记录！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String repairId = repairTableModel.getValueAt(selectedRow, 0).toString();
        String status = repairTableModel.getValueAt(selectedRow, 4).toString();

        JOptionPane.showMessageDialog(this,
                "报修单号：" + repairId + "\n当前状态：" + status + "\n\n维修进度跟踪中...",
                "维修进度",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 提交调宿申请
     */
    private void submitTransferApplication(JComboBox<String> typeCombo, JTextField targetField, JTextArea reasonArea) {
        String type = (String) typeCombo.getSelectedItem();
        String reason = reasonArea.getText().trim();

        if ("请选择".equals(type)) {
            JOptionPane.showMessageDialog(this, "请选择申请类型！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (reason.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写申请原因！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if ("申请换宿".equals(type) && targetField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写目标宿舍！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String message = String.format("申请类型：%s\n当前宿舍：%s\n目标宿舍：%s\n申请原因：%s\n\n确认提交申请？",
                type, dormitory, targetField.getText(), reason);

        int confirm = JOptionPane.showConfirmDialog(this, message, "确认提交", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this, "申请已提交，等待审核！", "成功", JOptionPane.INFORMATION_MESSAGE);
            typeCombo.setSelectedIndex(0);
            targetField.setText("");
            reasonArea.setText("");
        }
    }

    /**
     * 查看调宿记录
     */
    private void viewTransferRecords() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "调宿申请记录", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);

        String[] columns = {"申请时间", "申请类型", "当前宿舍", "目标宿舍", "申请原因", "审核状态"};
        Object[][] data = {
                {"2024-01-15", "申请换宿", "A101", "B202", "宿舍环境问题", "审核通过"},
                {"2024-01-10", "申请退宿", "A101", "", "毕业离校", "待审核"}
        };

        JTable table = new JTable(data, columns);
        JScrollPane scrollPane = new JScrollPane(table);

        dialog.add(scrollPane);
        dialog.setVisible(true);
    }

    /**
     * 提交假期登记（现在将调用 HolidayService 将数据写入数据库）
     */
    private void submitHolidayRegistration(JComboBox<String> typeCombo, JTextField leaveField,
                                           JTextField returnField, JTextField destinationField,
                                           JTextField contactField, JTextField phoneField) {
        String type = (String) typeCombo.getSelectedItem();
        String leaveDate = leaveField.getText().trim();
        String returnDate = returnField.getText().trim();
        String destination = destinationField.getText().trim();
        String contact = contactField.getText().trim();
        String phone = phoneField.getText().trim();

        if (leaveDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写离校时间！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if ("离校登记".equals(type) && returnDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写预计返校时间！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if ("离校登记".equals(type) && destination.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写目的地！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 解析日期，允许用户输入 yyyy-MM-dd 格式
        java.time.LocalDate leaveLocal = null;
        java.time.LocalDate returnLocal = null;
        try {
            leaveLocal = java.time.LocalDate.parse(leaveDate);
            if (!returnDate.isEmpty()) returnLocal = java.time.LocalDate.parse(returnDate);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "日期格式错误，请使用 yyyy-MM-dd 格式", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 生成 ID 并构造 Holiday 对象
        String id = model.Holiday.generateId();
        model.Holiday.HolidayType htype = "返校登记".equals(type) ? model.Holiday.HolidayType.BACK : model.Holiday.HolidayType.LEAVE;
        model.Holiday holiday = new model.Holiday(id, this.studentId, this.dormitory, "", htype, leaveLocal, returnLocal);
        // 补充其它可选字段
        if (!destination.isEmpty()) holiday.setDestination(destination);
        if (!contact.isEmpty()) holiday.setContactPerson(contact);
        if (!phone.isEmpty()) holiday.setContactPhone(phone);

        boolean ok = holidayService.add(holiday);
        if (ok) {
            JOptionPane.showMessageDialog(this, "假期登记已提交并保存到数据库！", "成功", JOptionPane.INFORMATION_MESSAGE);

            // 清空表单
            leaveField.setText("");
            returnField.setText("");
            destinationField.setText("");
            contactField.setText("");
            phoneField.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "保存假期登记时发生错误，请检查数据库连接或日志。", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 查看假期记录（从数据库读取）
     */
    private void viewHolidayRecords() {
        java.util.List<model.Holiday> records = holidayService.listByStudent(this.studentId);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "假期登记记录", true);
        dialog.setSize(800, 400);
        dialog.setLocationRelativeTo(this);

        String[] columns = {"登记时间", "登记类型", "离校时间", "返校时间", "目的地", "状态"};
        javax.swing.table.DefaultTableModel tableModel = new javax.swing.table.DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        for (model.Holiday h : records) {
            String regTime = h.getRegisterTime() == null ? "" : h.getRegisterTime().toString();
            String typeStr = h.getHolidayType().getDescription();
            String leave = h.getLeaveDate() == null ? "" : h.getLeaveDate().toString();
            String planned = h.getPlannedBackDate() == null ? "" : h.getPlannedBackDate().toString();
            String dest = h.getDestination().orElse("");
            String status = h.getStatus().getDescription();
            Object[] row = { regTime, typeStr, leave, planned, dest, status };
            tableModel.addRow(row);
        }

        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        dialog.add(scrollPane);
        dialog.setVisible(true);
    }

    /**
     * 显示一键报修对话框
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
            Object[] newRow = {
                    "Q" + System.currentTimeMillis(),
                    new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date()),
                    selected,
                    "一键报修：" + selected,
                    "待处理",
                    "0%"
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
        // 加载报修示例数据
        Object[][] repairData = {
                {"R20240115001", "2024-01-15 14:30", "水电问题", "厕所水管漏水", "处理中", "50%"},
                {"R20240110002", "2024-01-10 09:15", "电器故障", "空调不制冷", "已完成", "100%"},
                {"R20240105003", "2024-01-05 16:45", "家具损坏", "床板断裂", "待处理", "0%"}
        };

        for (Object[] row : repairData) {
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
    }
}

