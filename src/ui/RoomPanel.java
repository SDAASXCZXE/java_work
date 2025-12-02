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

    public RoomPanel() {
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

        // 右侧信息面板
        add(createSidePanel(), BorderLayout.EAST);
    }

    /**
     * 创建工具栏
     */
    private JPanel createToolBar() {
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        toolBar.setBorder(BorderFactory.createTitledBorder("宿舍管理"));

        // 操作按钮
        String[] buttons = {"新增宿舍", "编辑信息", "删除宿舍", "入住登记", "退宿处理", "检查记录"};

        for (String text : buttons) {
            JButton button = new JButton(text);
            button.setBackground(new Color(70, 130, 180));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.addActionListener(this::handleButtonClick);
            toolBar.add(button);
        }

        // 过滤功能
        toolBar.add(Box.createHorizontalStrut(20));
        toolBar.add(new JLabel("宿舍楼:"));
        buildingFilter = new JComboBox<>(new String[]{"全部", "A栋", "B栋", "C栋", "D栋", "E栋"});
        buildingFilter.addActionListener(e -> filterByBuilding());
        toolBar.add(buildingFilter);

        toolBar.add(new JLabel("状态:"));
        JComboBox<String> statusFilter = new JComboBox<>(new String[]{"全部", "已住满", "有空位", "维修中", "空置"});
        statusFilter.addActionListener(e -> filterByStatus());
        toolBar.add(statusFilter);

        // 搜索功能
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

        // 创建表格模型
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

        // 设置列宽
        roomTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // 宿舍号
        roomTable.getColumnModel().getColumn(1).setPreferredWidth(60);  // 宿舍楼
        roomTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // 房间类型
        roomTable.getColumnModel().getColumn(3).setPreferredWidth(70);  // 床位总数
        roomTable.getColumnModel().getColumn(4).setPreferredWidth(70);  // 已住人数

        // 添加滚动条
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

        // 统计信息
        String[] statsLabels = {
                "总宿舍数:", "总床位数:", "已住床位数:",
                "空余床位数:", "入住率:", "维修中房间:",
                "今日入住:", "今日退宿:", "本月报修:"
        };

        String[] statsValues = {
                "320", "1280", "1185",
                "95", "92.5%", "8",
                "12", "5", "23"
        };

        for (int i = 0; i < statsLabels.length; i++) {
            JPanel statPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            statPanel.add(new JLabel(statsLabels[i]));
            JLabel valueLabel = new JLabel(statsValues[i]);
            valueLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
            valueLabel.setForeground(new Color(70, 130, 180));
            statPanel.add(valueLabel);
            sidePanel.add(statPanel);
        }

        sidePanel.add(Box.createVerticalStrut(20));

        // 快速操作
        JPanel quickActions = new JPanel();
        quickActions.setLayout(new BoxLayout(quickActions, BoxLayout.Y_AXIS));
        quickActions.setBorder(BorderFactory.createTitledBorder("快速操作"));

        String[] actions = {"查看空余宿舍", "生成维修清单", "打印宿舍报表", "导出宿舍数据"};

        for (String action : actions) {
            JButton button = new JButton(action);
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.setMaximumSize(new Dimension(250, 30));
            button.addActionListener(e -> handleQuickAction(action));
            quickActions.add(button);
            quickActions.add(Box.createVerticalStrut(5));
        }

        sidePanel.add(quickActions);

        return sidePanel;
    }

    /**
     * 加载示例数据
     */
    private void loadSampleData() {
        // 示例数据
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
            case "检查记录":
                viewInspection();
                break;
        }
    }

    /**
     * 处理快速操作
     */
    private void handleQuickAction(String action) {
        switch (action) {
            case "查看空余宿舍":
                showAvailableRooms();
                break;
            case "生成维修清单":
                generateRepairList();
                break;
            case "打印宿舍报表":
                printRoomReport();
                break;
            case "导出宿舍数据":
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

        // 创建表单面板
        JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] labels = {"宿舍楼:", "房间号:", "房间类型:", "床位总数:", "宿舍长:",
                "联系电话:", "卫生评分:", "备注:"};
        JComponent[] fields = new JComponent[labels.length];

        for (int i = 0; i < labels.length; i++) {
            formPanel.add(new JLabel(labels[i]));

            switch (i) {
                case 0: // 宿舍楼
                    fields[i] = new JComboBox<>(new String[]{"A栋", "B栋", "C栋", "D栋", "E栋"});
                    break;
                case 2: // 房间类型
                    fields[i] = new JComboBox<>(new String[]{"二人间", "四人间", "六人间", "八人间"});
                    break;
                case 3: // 床位总数
                    JSpinner spinner = new JSpinner(new SpinnerNumberModel(4, 1, 8, 1));
                    fields[i] = spinner;
                    break;
                case 6: // 卫生评分
                    JSlider slider = new JSlider(0, 100, 80);
                    slider.setMajorTickSpacing(20);
                    slider.setMinorTickSpacing(5);
                    slider.setPaintTicks(true);
                    slider.setPaintLabels(true);
                    fields[i] = slider;
                    break;
                default:
                    fields[i] = new JTextField();
                    break;
            }

            formPanel.add((Component) fields[i]);
        }

        dialog.add(formPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        saveButton.addActionListener(e -> {
            // 保存逻辑
            String building = (String) ((JComboBox) fields[0]).getSelectedItem();
            String roomNumber = ((JTextField) fields[1]).getText();
            String roomType = (String) ((JComboBox) fields[2]).getSelectedItem();
            int totalBeds = (Integer) ((JSpinner) fields[3]).getValue();

            if (roomNumber.trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "房间号不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 添加到表格
            Object[] newRow = {
                    roomNumber, building, roomType, totalBeds,
                    0, totalBeds, "", "", 80, "空置", ""
            };
            tableModel.addRow(newRow);

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

        String roomNumber = tableModel.getValueAt(selectedRow, 0).toString();
        JOptionPane.showMessageDialog(this,
                "编辑功能开发中，当前选择宿舍: " + roomNumber,
                "提示",
                JOptionPane.INFORMATION_MESSAGE);
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

        // 检查宿舍是否有人居住
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

        // 检查是否有空余床位
        int availableBeds = Integer.parseInt(tableModel.getValueAt(selectedRow, 5).toString());
        if (availableBeds <= 0) {
            JOptionPane.showMessageDialog(this, "该宿舍已住满，无法入住！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String roomNumber = tableModel.getValueAt(selectedRow, 0).toString();
        JOptionPane.showMessageDialog(this,
                "为宿舍 " + roomNumber + " 办理入住登记\n空余床位: " + availableBeds,
                "入住登记",
                JOptionPane.INFORMATION_MESSAGE);
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

        String roomNumber = tableModel.getValueAt(selectedRow, 0).toString();
        JOptionPane.showMessageDialog(this,
                "为宿舍 " + roomNumber + " 办理退宿处理",
                "退宿处理",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 查看检查记录
     */
    private void viewInspection() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "宿舍检查记录", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        String[] columns = {"检查日期", "宿舍号", "卫生得分", "安全问题", "检查员", "备注"};
        Object[][] data = {
                {"2024-01-15", "A101", "95", "无", "张三", "优秀"},
                {"2024-01-15", "A102", "88", "垃圾未及时清理", "张三", "良好"},
                {"2024-01-15", "A103", "90", "无", "张三", "优秀"},
                {"2024-01-14", "B201", "92", "无", "李四", "优秀"},
                {"2024-01-14", "B202", "85", "物品摆放不整齐", "李四", "良好"},
        };

        JTable inspectionTable = new JTable(data, columns);
        JScrollPane scrollPane = new JScrollPane(inspectionTable);

        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    /**
     * 按宿舍楼过滤
     */
    private void filterByBuilding() {
        String selectedBuilding = (String) buildingFilter.getSelectedItem();
        if ("全部".equals(selectedBuilding)) {
            // 重置过滤
            roomTable.setRowSorter(null);
        } else {
            // 简单过滤逻辑
            javax.swing.RowFilter<DefaultTableModel, Object> filter = new javax.swing.RowFilter<DefaultTableModel, Object>() {
                public boolean include(javax.swing.RowFilter.Entry<? extends DefaultTableModel, ? extends Object> entry) {
                    String building = entry.getStringValue(1); // 第1列是宿舍楼
                    return building.equals(selectedBuilding);
                }
            };

            javax.swing.table.TableRowSorter<DefaultTableModel> sorter =
                    new javax.swing.table.TableRowSorter<>(tableModel);
            sorter.setRowFilter(filter);
            roomTable.setRowSorter(sorter);
        }
    }

    /**
     * 按状态过滤
     */
    private void filterByStatus() {
        // 实现状态过滤逻辑
        JOptionPane.showMessageDialog(this, "状态过滤功能开发中", "提示", JOptionPane.INFORMATION_MESSAGE);
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

        // 简单的搜索逻辑
        boolean found = false;
        for (int i = 0; i < roomTable.getRowCount(); i++) {
            for (int j = 0; j < roomTable.getColumnCount(); j++) {
                Object value = roomTable.getValueAt(i, j);
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
        scrollPane.setPreferredSize(new Dimension(300, 200));

        JOptionPane.showMessageDialog(this, scrollPane, "空余宿舍 (" + count + "间)", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 生成维修清单
     */
    private void generateRepairList() {
        JOptionPane.showMessageDialog(this, "维修清单已生成！", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 打印宿舍报表
     */
    private void printRoomReport() {
        JOptionPane.showMessageDialog(this, "宿舍报表打印中...", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 导出宿舍数据
     */
    private void exportRoomData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("导出宿舍数据");
        fileChooser.setSelectedFile(new java.io.File("宿舍数据.xls"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            JOptionPane.showMessageDialog(this,
                    "数据导出功能开发中\n文件路径: " + file.getAbsolutePath(),
                    "提示",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
}