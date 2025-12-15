package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * 宿舍管理面板
 */
public class RoomPanel extends JPanel {
    private JTable roomTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> buildingFilter;
    private JComboBox<String> statusFilter;

    // 添加统计标签的引用
    private JLabel totalRoomsLabel;
    private JLabel totalBedsLabel;
    private JLabel occupiedBedsLabel;
    private JLabel availableBedsLabel;
    private JLabel occupancyRateLabel;
    private JLabel underRepairLabel;

    public RoomPanel() {
        initUI();
        loadSampleData();
        // 初始化后立即更新统计数据
        updateStatistics();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(createToolBar(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createSidePanel(), BorderLayout.EAST);
    }

    /**
     * 创建工具栏
     */
    private JPanel createToolBar() {
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        toolBar.setBorder(BorderFactory.createTitledBorder("宿舍管理"));

        String[] buttons = {"新增宿舍", "编辑信息", "删除宿舍", "入住登记", "退宿处理", "导出数据"};

        for (String text : buttons) {
            JButton button = new JButton(text);
            button.setBackground(new Color(70, 130, 180));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.addActionListener(this::handleButtonClick);
            toolBar.add(button);
        }

        toolBar.add(Box.createHorizontalStrut(20));
        toolBar.add(new JLabel("宿舍楼:"));
        buildingFilter = new JComboBox<>(new String[]{"全部", "A栋", "B栋", "C栋", "D栋", "E栋"});
        buildingFilter.addActionListener(e -> filterTable());
        toolBar.add(buildingFilter);

        toolBar.add(new JLabel("状态:"));
        statusFilter = new JComboBox<>(new String[]{"全部", "已住满", "有空位", "维修中", "空置"});
        statusFilter.addActionListener(e -> filterTable());
        toolBar.add(statusFilter);

        toolBar.add(Box.createHorizontalStrut(20));
        toolBar.add(new JLabel("搜索:"));
        searchField = new JTextField(12);
        toolBar.add(searchField);

        JButton searchButton = new JButton("搜索");
        searchButton.addActionListener(e -> searchRooms());
        toolBar.add(searchButton);

        return toolBar;
    }

    /**
     * 创建表格面板
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = {"宿舍号", "宿舍楼", "房间类型", "床位总数", "已住人数", "空余床位",
                "宿舍长", "联系电话", "卫生评分", "状态", "备注"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        roomTable = new JTable(tableModel);
        roomTable.setRowHeight(25);
        roomTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        roomTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        roomTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        roomTable.getColumnModel().getColumn(1).setPreferredWidth(60);
        roomTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        roomTable.getColumnModel().getColumn(3).setPreferredWidth(70);
        roomTable.getColumnModel().getColumn(4).setPreferredWidth(70);

        JScrollPane scrollPane = new JScrollPane(roomTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("宿舍列表"));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    /**
     * 创建侧边信息面板
     */
    private JPanel createSidePanel() {
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBorder(BorderFactory.createTitledBorder("宿舍信息统计"));
        sidePanel.setPreferredSize(new Dimension(300, 0));

        // 统计信息面板
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new GridLayout(6, 2, 5, 5));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 创建统计标签
        totalRoomsLabel = createStatLabel("总宿舍数:", "0间");
        totalBedsLabel = createStatLabel("总床位数:", "0个");
        occupiedBedsLabel = createStatLabel("已住床位数:", "0个");
        availableBedsLabel = createStatLabel("空余床位数:", "0个");
        occupancyRateLabel = createStatLabel("入住率:", "0%");
        underRepairLabel = createStatLabel("维修中房间:", "0间");

        // 添加到面板
        statsPanel.add(new JLabel("总宿舍数:"));
        statsPanel.add(totalRoomsLabel);

        statsPanel.add(new JLabel("总床位数:"));
        statsPanel.add(totalBedsLabel);

        statsPanel.add(new JLabel("已住床位数:"));
        statsPanel.add(occupiedBedsLabel);

        statsPanel.add(new JLabel("空余床位数:"));
        statsPanel.add(availableBedsLabel);

        statsPanel.add(new JLabel("入住率:"));
        statsPanel.add(occupancyRateLabel);

        statsPanel.add(new JLabel("维修中房间:"));
        statsPanel.add(underRepairLabel);

        sidePanel.add(statsPanel);

        sidePanel.add(Box.createVerticalStrut(20));

        // 快速操作
        JPanel quickActions = new JPanel();
        quickActions.setLayout(new BoxLayout(quickActions, BoxLayout.Y_AXIS));
        quickActions.setBorder(BorderFactory.createTitledBorder("快速操作"));

        String[] actions = {"查看空余宿舍", "显示使用率图表"};

        for (String action : actions) {
            JButton button = new JButton(action);
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.setMaximumSize(new Dimension(250, 30));
            button.addActionListener(e -> {
                if (action.equals("查看空余宿舍")) {
                    showAvailableRooms();
                } else if (action.equals("显示使用率图表")) {
                    showRoomUsageChart();
                }
            });
            quickActions.add(button);
            quickActions.add(Box.createVerticalStrut(5));
        }

        sidePanel.add(quickActions);
        return sidePanel;
    }

    /**
     * 创建统计标签
     */
    private JLabel createStatLabel(String title, String value) {
        JLabel label = new JLabel(value);
        label.setFont(new Font("微软雅黑", Font.BOLD, 14));
        label.setForeground(new Color(70, 130, 180));
        return label;
    }

    /**
     * 更新统计信息
     */
    private void updateStatistics() {
        int totalRooms = tableModel.getRowCount();
        int totalBeds = 0;
        int occupiedBeds = 0;
        int underRepair = 0;

        for (int i = 0; i < totalRooms; i++) {
            try {
                int beds = Integer.parseInt(tableModel.getValueAt(i, 3).toString());
                int occupied = Integer.parseInt(tableModel.getValueAt(i, 4).toString());
                String status = tableModel.getValueAt(i, 9).toString();

                totalBeds += beds;
                occupiedBeds += occupied;

                if ("维修中".equals(status)) {
                    underRepair++;
                }
            } catch (NumberFormatException e) {
                // 跳过无效数据
                continue;
            }
        }

        int availableBeds = totalBeds - occupiedBeds;
        double occupancyRate = totalBeds > 0 ? (occupiedBeds * 100.0 / totalBeds) : 0;

        // 更新标签显示
        if (totalRoomsLabel != null) {
            totalRoomsLabel.setText(totalRooms + "间");
        }
        if (totalBedsLabel != null) {
            totalBedsLabel.setText(totalBeds + "个");
        }
        if (occupiedBedsLabel != null) {
            occupiedBedsLabel.setText(occupiedBeds + "个");
        }
        if (availableBedsLabel != null) {
            availableBedsLabel.setText(availableBeds + "个");
        }
        if (occupancyRateLabel != null) {
            occupancyRateLabel.setText(String.format("%.1f%%", occupancyRate));
        }
        if (underRepairLabel != null) {
            underRepairLabel.setText(underRepair + "间");
        }
    }

    /**
     * 加载示例数据
     */
    private void loadSampleData() {
        Object[][] sampleData = {
                {"101", "A栋", "四人间", "4", "4", "0", "张三", "13800138001", "95", "已住满", ""},
                {"102", "A栋", "四人间", "4", "3", "1", "李四", "13800138002", "88", "有空位", ""},
                {"103", "A栋", "六人间", "6", "6", "0", "王五", "13800138003", "90", "已住满", ""},
                {"201", "B栋", "四人间", "4", "4", "0", "赵六", "13800138004", "92", "已住满", ""},
                {"202", "B栋", "四人间", "4", "2", "2", "钱七", "13800138005", "85", "有空位", "新装修"},
                {"203", "B栋", "二人间", "2", "2", "0", "孙八", "13800138006", "96", "已住满", ""},
                {"301", "C栋", "四人间", "4", "0", "4", "", "", "0", "空置", "待分配"},
                {"302", "C栋", "四人间", "4", "0", "4", "", "", "0", "维修中", "空调维修"},
                {"401", "D栋", "六人间", "6", "5", "1", "周九", "13800138007", "87", "有空位", ""},
                {"402", "D栋", "六人间", "6", "6", "0", "吴十", "13800138008", "91", "已住满", ""},
        };

        for (Object[] row : sampleData) {
            tableModel.addRow(row);
        }

        // 加载数据后更新统计
        updateStatistics();
    }

    /**
     * 处理按钮点击
     */
    private void handleButtonClick(ActionEvent e) {
        String command = ((JButton) e.getSource()).getText();

        switch (command) {
            case "新增宿舍":
                addRoom();
                break;
            case "编辑信息":
                editRoom();
                break;
            case "删除宿舍":
                deleteRoom();
                break;
            case "入住登记":
                checkIn();
                break;
            case "退宿处理":
                checkOut();
                break;
            case "导出数据":
                exportRoomData();
                break;
        }
    }

    /**
     * 新增宿舍
     */
    private void addRoom() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "新增宿舍", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] labels = {"宿舍楼:", "房间号:", "房间类型:", "床位总数:", "宿舍长:",
                "联系电话:", "卫生评分:", "备注:"};
        JComponent[] fields = new JComponent[labels.length];

        for (int i = 0; i < labels.length; i++) {
            formPanel.add(new JLabel(labels[i]));

            switch (i) {
                case 0:
                    fields[i] = new JComboBox<>(new String[]{"A栋", "B栋", "C栋", "D栋", "E栋"});
                    break;
                case 2:
                    fields[i] = new JComboBox<>(new String[]{"二人间", "四人间", "六人间", "八人间"});
                    break;
                case 3:
                    JSpinner spinner = new JSpinner(new SpinnerNumberModel(4, 1, 8, 1));
                    fields[i] = spinner;
                    break;
                case 6:
                    JSpinner scoreSpinner = new JSpinner(new SpinnerNumberModel(80, 0, 100, 1));
                    fields[i] = scoreSpinner;
                    break;
                default:
                    fields[i] = new JTextField();
                    break;
            }

            formPanel.add((Component) fields[i]);
        }

        dialog.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        saveButton.addActionListener(e -> {
            String building = (String) ((JComboBox) fields[0]).getSelectedItem();
            String roomNumber = ((JTextField) fields[1]).getText().trim();
            String roomType = (String) ((JComboBox) fields[2]).getSelectedItem();
            int totalBeds = (Integer) ((JSpinner) fields[3]).getValue();
            String monitor = ((JTextField) fields[4]).getText().trim();
            String phone = ((JTextField) fields[5]).getText().trim();
            int score = (Integer) ((JSpinner) fields[6]).getValue();
            String remark = ((JTextField) fields[7]).getText().trim();

            if (roomNumber.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "房间号不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Object[] newRow = {
                    roomNumber, building, roomType, totalBeds,
                    0, totalBeds, monitor, phone, score, "空置", remark
            };
            tableModel.addRow(newRow);

            // 更新统计信息
            updateStatistics();

            JOptionPane.showMessageDialog(dialog, "宿舍添加成功！");
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * 编辑宿舍信息
     */
    private void editRoom() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要编辑的宿舍！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "编辑宿舍信息", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 450);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridLayout(9, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] labels = {"宿舍楼:", "房间号:", "房间类型:", "床位总数:", "已住人数:",
                "宿舍长:", "联系电话:", "卫生评分:", "状态:"};
        JComponent[] fields = new JComponent[labels.length];

        for (int i = 0; i < labels.length; i++) {
            formPanel.add(new JLabel(labels[i]));

            String currentValue = "";
            if (selectedRow < tableModel.getRowCount()) {
                switch (i) {
                    case 0: currentValue = tableModel.getValueAt(selectedRow, 1).toString(); break;
                    case 1: currentValue = tableModel.getValueAt(selectedRow, 0).toString(); break;
                    case 2: currentValue = tableModel.getValueAt(selectedRow, 2).toString(); break;
                    case 3: currentValue = tableModel.getValueAt(selectedRow, 3).toString(); break;
                    case 4: currentValue = tableModel.getValueAt(selectedRow, 4).toString(); break;
                    case 5: currentValue = tableModel.getValueAt(selectedRow, 6).toString(); break;
                    case 6: currentValue = tableModel.getValueAt(selectedRow, 7).toString(); break;
                    case 7: currentValue = tableModel.getValueAt(selectedRow, 8).toString(); break;
                    case 8: currentValue = tableModel.getValueAt(selectedRow, 9).toString(); break;
                }
            }

            switch (i) {
                case 0:
                    JComboBox<String> buildingCombo = new JComboBox<>(new String[]{"A栋", "B栋", "C栋", "D栋", "E栋"});
                    buildingCombo.setSelectedItem(currentValue);
                    fields[i] = buildingCombo;
                    break;
                case 2:
                    JComboBox<String> typeCombo = new JComboBox<>(new String[]{"二人间", "四人间", "六人间", "八人间"});
                    typeCombo.setSelectedItem(currentValue);
                    fields[i] = typeCombo;
                    break;
                case 3:
                    JSpinner totalSpinner = new JSpinner(new SpinnerNumberModel(
                            Integer.parseInt(currentValue), 1, 8, 1));
                    fields[i] = totalSpinner;
                    break;
                case 4:
                    JSpinner occupiedSpinner = new JSpinner(new SpinnerNumberModel(
                            Integer.parseInt(currentValue), 0, 8, 1));
                    fields[i] = occupiedSpinner;
                    break;
                case 7:
                    JSpinner scoreSpinner = new JSpinner(new SpinnerNumberModel(
                            Integer.parseInt(currentValue), 0, 100, 1));
                    fields[i] = scoreSpinner;
                    break;
                case 8:
                    JComboBox<String> statusCombo = new JComboBox<>(new String[]{"已住满", "有空位", "维修中", "空置"});
                    statusCombo.setSelectedItem(currentValue);
                    fields[i] = statusCombo;
                    break;
                default:
                    JTextField textField = new JTextField(currentValue);
                    if (i == 1) textField.setEditable(false); // 房间号不可编辑
                    fields[i] = textField;
                    break;
            }
            formPanel.add((Component) fields[i]);
        }

        dialog.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        saveButton.addActionListener(e -> {
            int totalBeds = (Integer) ((JSpinner) fields[3]).getValue();
            int occupied = (Integer) ((JSpinner) fields[4]).getValue();
            int available = totalBeds - occupied;

            // 更新表格数据
            tableModel.setValueAt(((JComboBox) fields[0]).getSelectedItem(), selectedRow, 1);
            tableModel.setValueAt(((JComboBox) fields[2]).getSelectedItem(), selectedRow, 2);
            tableModel.setValueAt(totalBeds, selectedRow, 3);
            tableModel.setValueAt(occupied, selectedRow, 4);
            tableModel.setValueAt(available, selectedRow, 5);
            tableModel.setValueAt(((JTextField) fields[5]).getText(), selectedRow, 6);
            tableModel.setValueAt(((JTextField) fields[6]).getText(), selectedRow, 7);
            tableModel.setValueAt(((JSpinner) fields[7]).getValue(), selectedRow, 8);
            tableModel.setValueAt(((JComboBox) fields[8]).getSelectedItem(), selectedRow, 9);

            // 更新统计信息
            updateStatistics();

            JOptionPane.showMessageDialog(dialog, "宿舍信息修改成功！");
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * 删除宿舍
     */
    private void deleteRoom() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的宿舍！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int occupied = Integer.parseInt(tableModel.getValueAt(selectedRow, 4).toString());
        if (occupied > 0) {
            JOptionPane.showMessageDialog(this,
                    "该宿舍仍有学生居住，无法删除！",
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String roomNumber = tableModel.getValueAt(selectedRow, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要删除宿舍 [" + roomNumber + "] 吗？",
                "确认删除",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            tableModel.removeRow(selectedRow);

            // 更新统计信息
            updateStatistics();

            JOptionPane.showMessageDialog(this, "删除成功！");
        }
    }

    /**
     * 入住登记
     */
    private void checkIn() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要登记的宿舍！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int availableBeds = Integer.parseInt(tableModel.getValueAt(selectedRow, 5).toString());
        if (availableBeds <= 0) {
            JOptionPane.showMessageDialog(this, "该宿舍已住满，无法入住！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "入住登记", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        formPanel.add(new JLabel("入住人数:"));
        JSpinner checkinSpinner = new JSpinner(new SpinnerNumberModel(1, 1, availableBeds, 1));
        formPanel.add(checkinSpinner);

        formPanel.add(new JLabel("备注:"));
        JTextField remarkField = new JTextField();
        formPanel.add(remarkField);

        dialog.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton confirmButton = new JButton("确认入住");
        JButton cancelButton = new JButton("取消");

        confirmButton.addActionListener(e -> {
            int checkinCount = (Integer) checkinSpinner.getValue();

            // 更新宿舍信息
            int currentOccupied = Integer.parseInt(tableModel.getValueAt(selectedRow, 4).toString());
            int currentAvailable = Integer.parseInt(tableModel.getValueAt(selectedRow, 5).toString());

            tableModel.setValueAt(currentOccupied + checkinCount, selectedRow, 4);
            tableModel.setValueAt(currentAvailable - checkinCount, selectedRow, 5);

            // 如果住满了，更新状态
            if (currentAvailable - checkinCount == 0) {
                tableModel.setValueAt("已住满", selectedRow, 9);
            } else {
                tableModel.setValueAt("有空位", selectedRow, 9);
            }

            // 更新统计信息
            updateStatistics();

            JOptionPane.showMessageDialog(dialog, "入住登记成功！\n入住人数: " + checkinCount);
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * 退宿处理
     */
    private void checkOut() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要退宿的宿舍！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int occupied = Integer.parseInt(tableModel.getValueAt(selectedRow, 4).toString());
        if (occupied <= 0) {
            JOptionPane.showMessageDialog(this, "该宿舍没有学生居住！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "退宿处理", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        formPanel.add(new JLabel("退宿人数:"));
        JSpinner checkoutSpinner = new JSpinner(new SpinnerNumberModel(1, 1, occupied, 1));
        formPanel.add(checkoutSpinner);

        formPanel.add(new JLabel("退宿原因:"));
        JTextField reasonField = new JTextField();
        formPanel.add(reasonField);

        dialog.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton confirmButton = new JButton("确认退宿");
        JButton cancelButton = new JButton("取消");

        confirmButton.addActionListener(e -> {
            int checkoutCount = (Integer) checkoutSpinner.getValue();
            String reason = reasonField.getText().trim();

            // 更新宿舍信息
            int currentOccupied = Integer.parseInt(tableModel.getValueAt(selectedRow, 4).toString());
            int totalBeds = Integer.parseInt(tableModel.getValueAt(selectedRow, 3).toString());

            tableModel.setValueAt(currentOccupied - checkoutCount, selectedRow, 4);
            tableModel.setValueAt(totalBeds - (currentOccupied - checkoutCount), selectedRow, 5);

            // 更新状态
            if (currentOccupied - checkoutCount == 0) {
                tableModel.setValueAt("空置", selectedRow, 9);
            } else {
                tableModel.setValueAt("有空位", selectedRow, 9);
            }

            // 更新统计信息
            updateStatistics();

            JOptionPane.showMessageDialog(dialog,
                    "退宿处理成功！\n退宿人数: " + checkoutCount +
                            (reason.isEmpty() ? "" : "\n退宿原因: " + reason));
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * 导出宿舍数据
     */
    private void exportRoomData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("导出宿舍数据");
        fileChooser.setSelectedFile(new java.io.File("宿舍数据_" +
                new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".xlsx"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            String filePath = file.getAbsolutePath();

            if (!filePath.toLowerCase().endsWith(".xlsx")) {
                filePath += ".xlsx";
            }

            // 模拟导出过程
            String finalFilePath = filePath;
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this,
                                "宿舍数据导出成功！\n文件路径: " + finalFilePath + "\n导出记录数: " + tableModel.getRowCount(),
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
     * 过滤表格
     */
    private void filterTable() {
        String building = (String) buildingFilter.getSelectedItem();
        String status = (String) statusFilter.getSelectedItem();

        javax.swing.RowFilter<DefaultTableModel, Object> filter = new javax.swing.RowFilter<DefaultTableModel, Object>() {
            public boolean include(javax.swing.RowFilter.Entry<? extends DefaultTableModel, ? extends Object> entry) {
                boolean buildingMatch = "全部".equals(building) || entry.getStringValue(1).equals(building);
                boolean statusMatch = "全部".equals(status) || entry.getStringValue(9).equals(status);
                return buildingMatch && statusMatch;
            }
        };

        javax.swing.table.TableRowSorter<DefaultTableModel> sorter =
                new javax.swing.table.TableRowSorter<>(tableModel);
        sorter.setRowFilter(filter);
        roomTable.setRowSorter(sorter);
    }

    /**
     * 搜索宿舍
     */
    private void searchRooms() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入搜索关键词！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        roomTable.clearSelection();

        boolean found = false;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                Object value = tableModel.getValueAt(i, j);
                if (value != null && value.toString().toLowerCase().contains(keyword.toLowerCase())) {
                    roomTable.setRowSelectionInterval(i, i);
                    roomTable.scrollRectToVisible(roomTable.getCellRect(i, 0, true));
                    found = true;
                    break;
                }
            }
            if (found) break;
        }

        if (!found) {
            JOptionPane.showMessageDialog(this, "未找到匹配的宿舍！", "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * 显示空余宿舍
     */
    private void showAvailableRooms() {
        StringBuilder availableRooms = new StringBuilder("空余宿舍列表：\n\n");
        int count = 0;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            int availableBeds = Integer.parseInt(tableModel.getValueAt(i, 5).toString());
            if (availableBeds > 0) {
                availableRooms.append(tableModel.getValueAt(i, 0)).append(" (")
                        .append(tableModel.getValueAt(i, 1)).append(") - ")
                        .append("空余床位: ").append(availableBeds)
                        .append(" - ").append(tableModel.getValueAt(i, 2))
                        .append("\n");
                count++;
            }
        }

        if (count == 0) {
            availableRooms.append("暂无空余宿舍");
        }

        JTextArea textArea = new JTextArea(availableRooms.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        JOptionPane.showMessageDialog(this, scrollPane, "空余宿舍 (" + count + "间)", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 显示宿舍使用率图表
     */
    private void showRoomUsageChart() {
        JDialog chartDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "宿舍使用率统计图表", true);
        chartDialog.setSize(600, 500);
        chartDialog.setLocationRelativeTo(this);
        chartDialog.setLayout(new BorderLayout());

        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 计算统计数据
                java.util.Map<String, Integer> buildingStats = new java.util.HashMap<>();
                java.util.Map<String, Integer> buildingTotal = new java.util.HashMap<>();

                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String building = tableModel.getValueAt(i, 1).toString();
                    int totalBeds = Integer.parseInt(tableModel.getValueAt(i, 3).toString());
                    int occupied = Integer.parseInt(tableModel.getValueAt(i, 4).toString());

                    buildingStats.put(building, buildingStats.getOrDefault(building, 0) + occupied);
                    buildingTotal.put(building, buildingTotal.getOrDefault(building, 0) + totalBeds);
                }

                // 绘制图表
                String[] buildings = buildingStats.keySet().toArray(new String[0]);
                int barWidth = 40;
                int spacing = 30;
                int startX = 100;
                int startY = 100;
                int chartHeight = 300;

                // 标题
                g2d.setFont(new Font("微软雅黑", Font.BOLD, 18));
                g2d.drawString("各宿舍楼使用率统计", 200, 40);

                // 坐标轴
                g2d.drawLine(startX, startY, startX, startY + chartHeight);
                g2d.drawLine(startX, startY + chartHeight, startX + (barWidth + spacing) * buildings.length, startY + chartHeight);

                // Y轴标签
                g2d.setFont(new Font("微软雅黑", Font.PLAIN, 12));
                for (int i = 0; i <= 10; i++) {
                    int y = startY + chartHeight - (i * chartHeight / 10);
                    g2d.drawString(i * 10 + "%", startX - 30, y + 4);
                    g2d.drawLine(startX - 5, y, startX, y);
                }

                // 柱状图
                for (int i = 0; i < buildings.length; i++) {
                    int total = buildingTotal.get(buildings[i]);
                    int occupied = buildingStats.get(buildings[i]);
                    double rate = total > 0 ? (occupied * 100.0 / total) : 0;

                    int barX = startX + i * (barWidth + spacing);
                    int barHeight = (int) (chartHeight * rate / 100.0);

                    // 柱子
                    g2d.setColor(new Color(70, 130, 180));
                    g2d.fillRect(barX, startY + chartHeight - barHeight, barWidth, barHeight);

                    // 边框
                    g2d.setColor(Color.BLACK);
                    g2d.drawRect(barX, startY + chartHeight - barHeight, barWidth, barHeight);

                    // 标注
                    g2d.drawString(buildings[i], barX + barWidth/2 - 10, startY + chartHeight + 20);
                    g2d.drawString(String.format("%.1f%%", rate), barX + barWidth/2 - 15, startY + chartHeight - barHeight - 5);

                    // 详细数据
                    g2d.drawString(occupied + "/" + total, barX + barWidth/2 - 15, startY + chartHeight - barHeight/2 + 5);
                }

                // 图例
                g2d.setColor(new Color(70, 130, 180));
                g2d.fillRect(startX, 450, 20, 20);
                g2d.setColor(Color.BLACK);
                g2d.drawString("使用率", startX + 30, 465);

                // 统计摘要
                g2d.setFont(new Font("微软雅黑", Font.BOLD, 14));
                g2d.drawString("统计摘要:", startX + 200, 450);
                g2d.setFont(new Font("微软雅黑", Font.PLAIN, 12));
                g2d.drawString("总宿舍数: " + tableModel.getRowCount() + "间", startX + 200, 475);

                int totalBeds = 0;
                int totalOccupied = 0;
                for (String building : buildings) {
                    totalBeds += buildingTotal.get(building);
                    totalOccupied += buildingStats.get(building);
                }
                g2d.drawString("总床位数: " + totalBeds + "个", startX + 200, 495);
                g2d.drawString("已住人数: " + totalOccupied + "人", startX + 350, 475);
                g2d.drawString("平均入住率: " + String.format("%.1f%%", totalBeds > 0 ? (totalOccupied * 100.0 / totalBeds) : 0), startX + 350, 495);
            }
        };

        chartDialog.add(chartPanel, BorderLayout.CENTER);
        chartDialog.setVisible(true);
    }
}