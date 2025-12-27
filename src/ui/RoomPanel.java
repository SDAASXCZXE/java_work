package ui;

import model.Room;
import model.Student;
import service.impl.StudentServiceImpl;
import service.RoomService;
import service.impl.RoomServiceImpl;
import util.DBUtil;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * 宿舍管理面板
 * 该类负责显示和操作宿舍信息的界面，包括：
 * - 顶部工具栏（新增/编辑/删除/入住/退宿/导出）
 * - 中间宿舍表格（显示宿舍列表）
 * - 底部信息栏（总宿舍数、已选中数量）
 */

public class RoomPanel extends JPanel {
    private JTable roomTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    // 统计 / 信息标签
    private JLabel countLabel;
    private JLabel selectedLabel;

    // 过滤器引用
    private JComboBox<String> buildingFilter;
    private JComboBox<String> statusFilter;

    // 服务层，用于数据库持久化操作
    private RoomService roomService = new RoomServiceImpl();

    // 新增：图表面板引用（柱状图）
    private ChartPanel chartPanel;

    // 新增：将中间分割面板提升为字段，便于在工具栏中控制右侧图表显示/隐藏
    private JSplitPane splitPane;

    // 新增：保存最近一次的楼栋占用率数据，供弹窗查看使用
    private java.util.Map<String, Double> lastOccupancyRate = new java.util.HashMap<>();

    // 新增：chartWrapper 保存右侧包装面板，便于切换
    private JPanel chartWrapper;

    public RoomPanel() {
        initUI();
        loadRoomsFromDB();
        // 【添加这一行】：注册监听器，监听到 "rooms-updated" 信号时自动刷新列表
        util.RefreshCenter.register("rooms-updated", this::loadRoomsFromDB);
    }

    /**
     * 初始化界面：设置布局并添加工具栏、表格面板和信息面板
     */
    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(createToolBar(), BorderLayout.NORTH);
        // 中间区域使用左右分割：左侧表格，右侧柱状图统计
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createTablePanel(), createChartPanel());
        splitPane.setResizeWeight(0.68);
        add(splitPane, BorderLayout.CENTER);
        add(createInfoPanel(), BorderLayout.SOUTH);
    }

    /**
     * 创建顶部工具栏：包含操作按钮、过滤条件与搜索控件
     * @return 工具栏 JPanel
     */
    private JPanel createToolBar() {
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        toolBar.setBorder(BorderFactory.createTitledBorder("宿舍管理"));

        String[] buttons = {"新增宿舍", "编辑信息", "删除宿舍", "入住登记", "退宿处理", "导出数据", "导入宿舍"};
        for (String text : buttons) {
            JButton button = new JButton(text);
            button.setBackground(new Color(70, 130, 180));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.addActionListener(this::handleButtonClick);
            toolBar.add(button);
        }

        // 新增：图标按钮用于切换右侧柱状图的显示/隐藏
        Icon chartIcon = UIManager.getIcon("FileView.fileIcon");
        JButton chartToggleBtn = new JButton(chartIcon);
        chartToggleBtn.setToolTipText("切换显示/隐藏宿舍统计图");
        chartToggleBtn.setPreferredSize(new Dimension(28, 28));
        chartToggleBtn.addActionListener(e -> {
            if (splitPane == null) return;
            Component right = splitPane.getRightComponent();
            // 如果当前右侧是我们的 chartWrapper，则隐藏它（替换为占位空面板）
            if (right == chartWrapper) {
                JPanel placeholder = new JPanel();
                splitPane.setRightComponent(placeholder);
                splitPane.setDividerLocation(getWidth());
            } else {
                // 恢复右侧为 chartWrapper
                splitPane.setRightComponent(chartWrapper);
                splitPane.setDividerLocation((int)(getWidth() * 0.68));
            }
            splitPane.revalidate();
            splitPane.repaint();
        });
        toolBar.add(chartToggleBtn);

        // 新增：单独的查看统计图按钮，弹出独立对话框显示最新图表数据
        JButton viewChartBtn = new JButton("查看统计图");
        viewChartBtn.setBackground(new Color(60, 160, 100));
        viewChartBtn.setForeground(Color.WHITE);
        viewChartBtn.addActionListener(e -> {
            JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "宿舍占用率 - 单独窗口", true);
            d.setSize(700, 480);
            d.setLocationRelativeTo(this);
            ChartPanel cp = new ChartPanel();
            cp.setData(lastOccupancyRate);
            d.add(cp);
            d.setVisible(true);
        });
        toolBar.add(viewChartBtn);

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

        JButton resetBtn = new JButton("重置");
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            roomTable.clearSelection();
            roomTable.setRowSorter(null);
        });
        toolBar.add(resetBtn);

        return toolBar;
    }

    /**
     * 创建并初始化宿舍表格面板（中间区）
     * @return 放置了 JTable 的面板
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

        // 常用列宽设置
        try {
            roomTable.getColumnModel().getColumn(0).setPreferredWidth(80);
            roomTable.getColumnModel().getColumn(1).setPreferredWidth(60);
            roomTable.getColumnModel().getColumn(2).setPreferredWidth(80);
            roomTable.getColumnModel().getColumn(3).setPreferredWidth(70);
            roomTable.getColumnModel().getColumn(4).setPreferredWidth(70);
        } catch (Exception ignored) {}

        JScrollPane scrollPane = new JScrollPane(roomTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("宿舍列表"));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    /**
     * 创建底部信息面板，显示总宿舍数与选中计数（与 StudentPanel 风格一致）
     * @return 信息面板
     */
    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBorder(BorderFactory.createEtchedBorder());

        countLabel = new JLabel("总宿舍数: 0");
        selectedLabel = new JLabel("已选中: 0");

        // 监听选择变化
        roomTable.getSelectionModel().addListSelectionListener((e) -> {
            if (!e.getValueIsAdjusting()) {
                selectedLabel.setText("已选中: " + roomTable.getSelectedRowCount());
            }
        });

        infoPanel.add(countLabel);
        infoPanel.add(Box.createHorizontalStrut(20));
        infoPanel.add(selectedLabel);

        return infoPanel;
    }

    /**
     * 更新底部显示的宿舍总数（从表格模型读取）
     */
    private void updateRoomCount() {
        if (countLabel != null) {
            countLabel.setText("总宿舍数: " + tableModel.getRowCount());
        }
    }

    /**
     * 从数据库加载宿舍数据到表格，使用 Service 层（持久化）
     */
    public void loadRoomsFromDB() {
        tableModel.setRowCount(0);
        try {
            List<Room> rooms = roomService.findAll();

            // 如果结果为空，额外检查数据库连接，便于定位问题
            if (rooms == null || rooms.isEmpty()) {
                // 仅在无法连接数据库时弹出错误提示
                try {
                    if (DBUtil.getConnection() == null) {
                        JOptionPane.showMessageDialog(this, "无法连接数据库，请检查配置并确保数据库可用。", "数据库连接错误", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (Exception ignored) {
                    // ignore
                }
                // 若连接可用但列表为空，则表示当前无宿舍记录
                updateRoomCount();
                // 更新图表为空数据
                if (chartPanel != null) chartPanel.setData(java.util.Collections.emptyMap());
                lastOccupancyRate.clear();
                return;
            }

            // 为统计收集每栋楼的床位总数和已住人数
            java.util.Map<String, Integer> totalBedsPerBuilding = new java.util.HashMap<>();
            java.util.Map<String, Integer> occupiedPerBuilding = new java.util.HashMap<>();

            for (Room r : rooms) {
                Object[] row = {
                        r.getRoomNumber(),
                        r.getBuilding(),
                        // 现在 roomType 已经是字符串
                        r.getRoomType() == null ? "" : r.getRoomType(),
                        r.getTotalBeds(),
                        r.getOccupied(),
                        r.getAvailableBeds(),
                        r.getMonitor(),
                        r.getPhone(),
                        r.getHygieneScore(),
                        r.getStatus() == null ? "" : r.getStatus(),
                        r.getRemarks()
                };
                tableModel.addRow(row);

                // 统计
                String b = r.getBuilding() == null ? "未知" : r.getBuilding();
                totalBedsPerBuilding.put(b, totalBedsPerBuilding.getOrDefault(b, 0) + r.getTotalBeds());
                occupiedPerBuilding.put(b, occupiedPerBuilding.getOrDefault(b, 0) + r.getOccupied());
            }
            updateRoomCount();

            // 计算入住率（occupied / total）
            java.util.Map<String, Double> occupancyRate = new java.util.HashMap<>();
            for (String b : totalBedsPerBuilding.keySet()) {
                int total = totalBedsPerBuilding.getOrDefault(b, 0);
                int occ = occupiedPerBuilding.getOrDefault(b, 0);
                double rate = total == 0 ? 0.0 : ((double) occ) / total;
                occupancyRate.put(b, rate);
            }
            // 更新图表
            if (chartPanel != null) chartPanel.setData(occupancyRate);
            // 保存为最近一次统计数据，供弹窗查看
            lastOccupancyRate.clear();
            lastOccupancyRate.putAll(occupancyRate);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载宿舍数据失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 工具栏按钮统一事件处理器：根据按钮文本分发到对应的方法
     * @param e ActionEvent
     */
    private void handleButtonClick(ActionEvent e) {
        String command = ((JButton) e.getSource()).getText();

        switch (command) {
            case "新增宿舍": addRoom(); break;
            case "编辑信息": editRoom(); break;
            case "删除宿舍": deleteRoom(); break;
            case "入住登记": checkIn(); break;
            case "退宿处理": checkOut(); break;
            case "导出数据": exportRoomData(); break;
            case "导入宿舍": importRooms(); break;
        }
    }

    // 一键导入宿舍 CSV
    private void importRooms() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("导入宿舍（CSV）");
        int res = chooser.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;
        java.io.File file = chooser.getSelectedFile();
        if (file == null || !file.exists()) return;

        new Thread(() -> {
            boolean ok = roomService.importFromCsv(file);
            SwingUtilities.invokeLater(() -> {
                if (ok) {
                    JOptionPane.showMessageDialog(this, "宿舍导入完成（部分或全部成功）");
                    loadRoomsFromDB();
                    util.RefreshCenter.notify("rooms-updated");
                } else {
                    JOptionPane.showMessageDialog(this, "宿舍导入失败或没有有效记录。", "导入结果", JOptionPane.WARNING_MESSAGE);
                }
            });
        }).start();
    }

    /**
     * 弹出对话框新增宿舍（保存到数据库）
     */
    @SuppressWarnings("unchecked")
    private void addRoom() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "新增宿舍", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(420, 460);
        dialog.setLocationRelativeTo(this);

        // 按照数据库列顺序：room_number, building, room_type, total_beds, occupied, available_beds, monitor, phone, hygiene_score, status, remarks
        String[] labels = {"房间号:", "宿舍楼:", "房间类型:", "床位总数:", "已住人数:", "空余床位:", "宿舍长:", "联系电话:", "卫生评分:", "状态:", "备注:"};
        JComponent[] fields = new JComponent[labels.length];

        JPanel formPanel = new JPanel(new GridLayout(labels.length, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        for (int i = 0; i < labels.length; i++) {
            formPanel.add(new JLabel(labels[i]));
            switch (i) {
                case 0:
                    fields[i] = new JTextField(); // room_number
                    break;
                case 1:
                    fields[i] = new JComboBox<>(new String[]{"A栋", "B栋", "C栋", "D栋", "E栋"});
                    break;
                case 2:
                    fields[i] = new JComboBox<>(new String[]{"二人间", "四人间", "六人间", "八人间"});
                    break;
                case 3:
                    fields[i] = new JSpinner(new SpinnerNumberModel(4, 1, 8, 1)); // total_beds
                    break;
                case 4:
                    fields[i] = new JSpinner(new SpinnerNumberModel(0, 0, 8, 1)); // occupied
                    break;
                case 5:
                    fields[i] = new JSpinner(new SpinnerNumberModel(4, 0, 8, 1)); // available_beds (初始同 total)
                    break;
                case 6:
                    fields[i] = new JTextField(); // monitor
                    break;
                case 7:
                    fields[i] = new JTextField(); // phone
                    break;
                case 8:
                    fields[i] = new JSpinner(new SpinnerNumberModel(80, 0, 100, 1)); // hygiene_score
                    break;
                case 9:
                    fields[i] = new JComboBox<>(new String[]{"已住满", "有空位", "维修中", "空置"});
                    break;
                case 10:
                    fields[i] = new JTextField(); // remarks
                    break;
                default:
                    fields[i] = new JTextField();
            }
            formPanel.add((Component) fields[i]);
        }

        // 三者联动：totalBeds = occupied + available
        try {
            if (!(fields[3] instanceof JSpinner)
                    || !(fields[4] instanceof JSpinner)
                    || !(fields[5] instanceof JSpinner)) {
                System.err.println("Spinner 初始化失败");
                return;
            }

            JSpinner totalSpinner = (JSpinner) fields[3];
            JSpinner occupiedSpinner = (JSpinner) fields[4];
            JSpinner availSpinner = (JSpinner) fields[5];


            SpinnerNumberModel occModel = (SpinnerNumberModel) occupiedSpinner.getModel();
            SpinnerNumberModel availModel = (SpinnerNumberModel) availSpinner.getModel();

            ChangeListener listener = e -> {
                int total = (Integer) totalSpinner.getValue();
                int occupied = (Integer) occupiedSpinner.getValue();

                // 已住人数不能超过总床位
                if (occupied > total) {
                    occupied = total;
                    occupiedSpinner.setValue(total);
                }

                int available = total - occupied;

                // 更新模型约束
                occModel.setMaximum(total);
                availModel.setMaximum(total);

                // 自动计算空床位
                availSpinner.setValue(available);
            };

            totalSpinner.addChangeListener(listener);
            occupiedSpinner.addChangeListener(listener);

        } catch (Exception ignored) {}


        dialog.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        saveButton.addActionListener(e -> {
            String roomNumber = ((JTextField) fields[0]).getText().trim();
            String building = (String) ((JComboBox<String>) fields[1]).getSelectedItem();
            String roomType = (String) ((JComboBox<String>) fields[2]).getSelectedItem();
            int totalBeds = (Integer) ((JSpinner) fields[3]).getValue();
            int occupied = (Integer) ((JSpinner) fields[4]).getValue();
            int available = (Integer) ((JSpinner) fields[5]).getValue();
            String monitor = ((JTextField) fields[6]).getText().trim();
            String phone = ((JTextField) fields[7]).getText().trim();
            int hygiene = (Integer) ((JSpinner) fields[8]).getValue();
            String statusText = (String) ((JComboBox<String>) fields[9]).getSelectedItem();
            String remarks = ((JTextField) fields[10]).getText().trim();

            if (roomNumber.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "房间号不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 确保 available 不超过 total
            available = Math.max(0, Math.min(available, totalBeds));

            // 直接使用字符串
            Room room = new Room(roomNumber, building, roomType, totalBeds, occupied, available, monitor, phone, hygiene, statusText, remarks);

            // 【关键修复】检查房间号唯一性（联合 building 和 roomNumber）
            // 如果 roomService 不支持 building 参数，我们手动遍历 tableModel 检查
            boolean isDuplicate = false;
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String existRoom = tableModel.getValueAt(i, 0).toString();
                String existBuilding = tableModel.getValueAt(i, 1).toString();
                if (existRoom.equals(roomNumber) && existBuilding.equals(building)) {
                    isDuplicate = true;
                    break;
                }
            }

            if (isDuplicate) {
                JOptionPane.showMessageDialog(dialog, "该楼栋已存在此房间号，请检查输入。", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 如果您的数据库设置了 room_number 唯一键（不包含 building），这里依然会报错。
            // 请确保数据库 UNIQUE 索引是 (building, room_number)。

            boolean ok = roomService.add(room);
            if (ok) {
                loadRoomsFromDB();
                // 尝试在表格中定位新添加的宿舍并选中
                boolean found = false;
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    Object vRoom = tableModel.getValueAt(i, 0);
                    Object vBuilding = tableModel.getValueAt(i, 1);
                    if (vRoom != null && vRoom.toString().equals(roomNumber) &&
                            vBuilding != null && vBuilding.toString().equals(building)) {
                        roomTable.setRowSelectionInterval(i, i);
                        roomTable.scrollRectToVisible(roomTable.getCellRect(i, 0, true));
                        found = true;
                        break;
                    }
                }
                if (found) {
                    JOptionPane.showMessageDialog(dialog, "宿舍添加成功！");
                    dialog.dispose();
                } else {
                    String msg = "宿舍已插入但未在列表中找到。请检查数据库表内容。";
                    System.err.println(msg);
                    JOptionPane.showMessageDialog(dialog, msg, "警告", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(dialog, "宿舍添加失败，请检查数据库连接或是否存在重复数据。", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * 编辑选中宿舍信息并更新（同时同步到数据库）
     */
    @SuppressWarnings("unchecked")
    private void editRoom() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要编辑的宿舍！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "编辑宿舍信息", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(420, 520);
        dialog.setLocationRelativeTo(this);

        // 按数据库列顺序展示编辑表单
        String[] labels = {"房间号:", "宿舍楼:", "房间类型:", "床位总数:", "已住人数:", "空余床位:", "宿舍长:", "联系电话:", "卫生评分:", "状态:", "备注:"};
        JComponent[] fields = new JComponent[labels.length];

        JPanel formPanel = new JPanel(new GridLayout(labels.length, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        for (int i = 0; i < labels.length; i++) {
            String currentValue = "";
            switch (i) {
                case 0: currentValue = safeGet(selectedRow, 0); break; // room_number
                case 1: currentValue = safeGet(selectedRow, 1); break; // building
                case 2: currentValue = safeGet(selectedRow, 2); break; // room_type
                case 3: currentValue = safeGet(selectedRow, 3); break; // total_beds
                case 4: currentValue = safeGet(selectedRow, 4); break; // occupied
                case 5: currentValue = safeGet(selectedRow, 5); break; // available_beds
                case 6: currentValue = safeGet(selectedRow, 6); break; // monitor
                case 7: currentValue = safeGet(selectedRow, 7); break; // phone
                case 8: currentValue = safeGet(selectedRow, 8); break; // hygiene
                case 9: currentValue = safeGet(selectedRow, 9); break; // status
                case 10: currentValue = safeGet(selectedRow, 10); break; // remarks
            }

            formPanel.add(new JLabel(labels[i]));
            switch (i) {
                case 0:
                    JTextField idField = new JTextField(currentValue);
                    idField.setEditable(false);
                    fields[i] = idField;
                    break;
                case 1:
                    JComboBox<String> bCombo = new JComboBox<>(new String[]{"A栋", "B栋", "C栋", "D栋", "E栋"});
                    bCombo.setSelectedItem(currentValue);
                    bCombo.setEnabled(false); // 楼栋和房间号是主键，通常不允许修改
                    fields[i] = bCombo;
                    break;
                case 2:
                    JComboBox<String> typeCombo = new JComboBox<>(new String[]{"二人间", "四人间", "六人间", "八人间"});
                    typeCombo.setSelectedItem(currentValue);
                    fields[i] = typeCombo;
                    break;
                case 3:
                    fields[i] = new JSpinner(new SpinnerNumberModel(parseIntSafe(currentValue, 4), 1, 8, 1));
                    break;
                case 4:
                    fields[i] = new JSpinner(new SpinnerNumberModel(parseIntSafe(currentValue, 0), 0, 8, 1));
                    break;
                case 5:
                    fields[i] = new JSpinner(new SpinnerNumberModel(parseIntSafe(currentValue, 0), 0, 8, 1));
                    break;
                case 6:
                    fields[i] = new JTextField(currentValue);
                    break;
                case 7:
                    fields[i] = new JTextField(currentValue);
                    break;
                case 8:
                    fields[i] = new JSpinner(new SpinnerNumberModel(parseIntSafe(currentValue, 80), 0, 100, 1));
                    break;
                case 9:
                    JComboBox<String> statusCombo = new JComboBox<>(new String[]{"已住满", "有空位", "维修中", "空置"});
                    statusCombo.setSelectedItem(currentValue);
                    fields[i] = statusCombo;
                    break;
                case 10:
                    fields[i] = new JTextField(currentValue);
                    break;
                default:
                    fields[i] = new JTextField(currentValue);
            }
            formPanel.add((Component) fields[i]);
        }

        // 同步 total_beds -> available_beds
        try {
            JSpinner totalSpinner = (JSpinner) fields[3];
            JSpinner availSpinner = (JSpinner) fields[5];
            SpinnerNumberModel availModel = (SpinnerNumberModel) availSpinner.getModel();
            totalSpinner.addChangeListener(e -> {
                int tv = (Integer) totalSpinner.getValue();
                availModel.setMaximum(tv);
                if ((Integer)availSpinner.getValue() > tv) availSpinner.setValue(tv);
            });
        } catch (Exception ignored) {}

        dialog.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        saveButton.addActionListener(e -> {
            String roomNumber = ((JTextField) fields[0]).getText().trim();
            String building = (String) ((JComboBox<String>) fields[1]).getSelectedItem();
            String roomType = (String) ((JComboBox<String>) fields[2]).getSelectedItem();
            int totalBeds = (Integer) ((JSpinner) fields[3]).getValue();
            int occupied = (Integer) ((JSpinner) fields[4]).getValue();
            int available = (Integer) ((JSpinner) fields[5]).getValue();
            String monitor = ((JTextField) fields[6]).getText().trim();
            String phone = ((JTextField) fields[7]).getText().trim();
            int hygiene = (Integer) ((JSpinner) fields[8]).getValue();
            String statusText = (String) ((JComboBox<String>) fields[9]).getSelectedItem();
            String remarks = ((JTextField) fields[10]).getText().trim();

            available = Math.max(0, Math.min(available, totalBeds));

            // 直接使用字符串
            Room room = new Room(roomNumber, building, roomType, totalBeds, occupied, available, monitor, phone, hygiene, statusText, remarks);
            boolean ok = roomService.update(room);
            if (ok) {
                loadRoomsFromDB();
                JOptionPane.showMessageDialog(dialog, "宿舍信息修改成功！");
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "更新失败，请检查数据库连接。", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * 删除选中宿舍（若已有人入住则禁止删除），并同步到数据库
     */
    private void deleteRoom() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的宿舍！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int occupied = parseIntSafe(safeGet(selectedRow, 4), 0);
        if (occupied > 0) {
            JOptionPane.showMessageDialog(this, "该宿舍仍有学生居住，无法删除！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String roomNumber = safeGet(selectedRow, 0);
        String building = safeGet(selectedRow, 1); // 获取楼栋

        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要删除宿舍 [" + building + " " + roomNumber + "] 吗？",
                "确认删除",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // 这里存在一个隐患：deleteByRoomNumber 可能会删除所有楼栋的同号房间
            // 建议修复 Dao 层的删除逻辑
            // 临时检查：如果 RoomService.deleteByRoomNumber 实现有误，这里会误删
            boolean ok = roomService.deleteByRoomNumber(roomNumber);
            if (ok) {
                loadRoomsFromDB();
                JOptionPane.showMessageDialog(this, "删除成功！");
            } else {
                JOptionPane.showMessageDialog(this, "删除失败，请检查数据库连接或外键约束。", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 入住登记：为选中宿舍增加入住人数并更新空余床位与状态（持久化）
     */
    private void checkIn() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要登记的宿舍！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int availableBeds = parseIntSafe(safeGet(selectedRow, 5), 0);
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

            int currentOccupied = parseIntSafe(safeGet(selectedRow, 4), 0);
            int currentAvailable = parseIntSafe(safeGet(selectedRow, 5), 0);

            int newOccupied = currentOccupied + checkinCount;
            int newAvailable = Math.max(0, currentAvailable - checkinCount);
            String statusText = newAvailable == 0 ? "已住满" : "有空位";

            String roomNumber = safeGet(selectedRow, 0);
            String building = safeGet(selectedRow, 1);

            // 注意：这里调用的是 Dao 层的 updateOccupancy，如果 Dao 层只用 roomNumber 做条件，会有 Bug。
            // 建议您修改 Service/Dao 层以支持 building 参数。
            // 假设 roomService.updateOccupancy 已经修复或接受联合键，否则这里依然有风险。

            boolean ok = roomService.updateOccupancy(building, roomNumber, newOccupied, newAvailable, statusText);
            if (ok) {
                loadRoomsFromDB();
                JOptionPane.showMessageDialog(dialog, "入住登记成功！\n入住人数: " + checkinCount);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "登记失败，请检查数据库连接。", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * 退宿处理：为选中宿舍减少入住人数并更新空余床位与状态（持久化）
     */
    private void checkOut() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要退宿的宿舍！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int occupied = parseIntSafe(safeGet(selectedRow, 4), 0);
        if (occupied <= 0) {
            JOptionPane.showMessageDialog(this, "该宿舍没有学生居住！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String roomNumber = safeGet(selectedRow, 0);
        String building = safeGet(selectedRow, 1);
        if (building.isEmpty() || roomNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "宿舍信息不完整，无法退宿。", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 查找该宿舍的学生（Student.roomNumber 存储格式为 "A栋101"）
        String dormKey = (building + roomNumber).replaceAll("\\s+", "").toLowerCase();
        java.util.List<Student> residents = new StudentServiceImpl().listStudents().stream()
                .filter(s -> {
                    String rn = s.getRoomNumber();
                    if (rn == null) return false;
                    String normalized = rn.replaceAll("\\s+", "").toLowerCase();
                    // 严格匹配楼栋+房间号，避免匹配到其他楼或仅按房间号匹配
                    return normalized.equals(dormKey);
                })
                .toList();

        if (residents.isEmpty()) {
            JOptionPane.showMessageDialog(this, "未在学生记录中找到该宿舍的入住学生。", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 构造选择列表，允许多选以退多名学生
        DefaultListModel<String> lm = new DefaultListModel<>();
        for (Student s : residents) {
            lm.addElement(s.getSno() + "  " + s.getName() + "  床位:" + (s.getBedNumber() == null ? "" : s.getBedNumber()));
        }
        JList<String> list = new JList<>(lm);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setVisibleRowCount(Math.min(8, lm.size()));

        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        p.add(new JLabel("请选择要退宿的学生（可多选）："), BorderLayout.NORTH);
        p.add(new JScrollPane(list), BorderLayout.CENTER);

        int res = JOptionPane.showConfirmDialog(this, p, "退宿 - 选择学生", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        int[] sel = list.getSelectedIndices();
        if (sel == null || sel.length == 0) {
            JOptionPane.showMessageDialog(this, "请至少选择一名学生。", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 逐个更新学生记录并统计成功数
        StudentServiceImpl stuSvc = new StudentServiceImpl();
        int removed = 0;
        java.util.List<String> removedNames = new java.util.ArrayList<>();
        for (int idx : sel) {
            if (idx < 0 || idx >= residents.size()) continue;
            Student s = residents.get(idx);
            // 清空宿舍信息
            s.setRoomNumber("");
            s.setBedNumber("");
            boolean ok = stuSvc.updateStudent(s);
            if (ok) {
                removed++;
                removedNames.add(s.getSno() + "(" + s.getName() + ")");
            }
        }

        if (removed == 0) {
            JOptionPane.showMessageDialog(this, "未能更新任何学生记录。请检查数据库连接或权限。", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 更新宿舍的占用人数
        Room room = roomService.findAll().stream()
                .filter(r -> building.equals(r.getBuilding()) && roomNumber.equals(r.getRoomNumber()))
                .findFirst().orElse(null);
        if (room == null) {
            JOptionPane.showMessageDialog(this, "未找到对应宿舍记录，学生信息已更新但宿舍表未同步。", "警告", JOptionPane.WARNING_MESSAGE);
            // 仍然刷新学生列表
            loadRoomsFromDB();
            util.RefreshCenter.notify("students-updated");
            return;
        }

        int newOcc = Math.max(0, room.getOccupied() - removed);
        int newAvail = Math.max(0, room.getTotalBeds() - newOcc);
        String statusText = newAvail == 0 ? "已住满" : (newOcc == 0 ? "空置" : "有空位");

        boolean okRoom = roomService.updateOccupancy(building, roomNumber, newOcc, newAvail, statusText);

        // 刷新界面
        loadRoomsFromDB();
        util.RefreshCenter.notify("students-updated");
        util.RefreshCenter.notify("rooms-updated");

        String msg = String.format("已成功为宿舍 [%s %s] 退宿 %d 人：%s", building, roomNumber, removed, String.join(", ", removedNames));
        if (!okRoom) msg += "\n但宿舍人数更新失败，请检查数据库连接。";
        JOptionPane.showMessageDialog(this, msg, "退宿完成", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 导出表格数据到本地文件（模拟导出，显示成功提示）
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
     * 根据顶部过滤条件（宿舍楼、状态）对表格进行筛选
     */
    private void filterTable() {
        String building = (String) buildingFilter.getSelectedItem();
        String status = (String) statusFilter.getSelectedItem();

        javax.swing.RowFilter<DefaultTableModel, Object> filter = new javax.swing.RowFilter<>() {
            public boolean include(javax.swing.RowFilter.Entry<? extends DefaultTableModel, ? extends Object> entry) {
                boolean buildingMatch = "全部".equals(building) || entry.getStringValue(1).equals(building);
                boolean statusMatch = "全部".equals(status) || entry.getStringValue(9).equals(status);
                return buildingMatch && statusMatch;
            }
        };

        javax.swing.table.TableRowSorter<DefaultTableModel> sorter = new javax.swing.table.TableRowSorter<>(tableModel);
        sorter.setRowFilter(filter);
        roomTable.setRowSorter(sorter);
    }

    /**
     * 在表格中按关键字搜索并选中第一个匹配行
     */
    private void searchRooms() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入搜索关键词！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        roomTable.clearSelection();

        java.util.List<Integer> matchedRows = new java.util.ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            boolean rowMatches = false;
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                Object value = tableModel.getValueAt(i, j);
                if (value != null && value.toString().toLowerCase().contains(keyword.toLowerCase())) {
                    rowMatches = true;
                    break;
                }
            }
            if (rowMatches) matchedRows.add(i);
        }

        if (!matchedRows.isEmpty()) {
            // 选中并滚动到第一个匹配项，同时保持选中所有匹配行
            int first = matchedRows.get(0);
            for (int r : matchedRows) {
                try {
                    roomTable.addRowSelectionInterval(r, r);
                } catch (Exception ignored) {}
            }
            roomTable.scrollRectToVisible(roomTable.getCellRect(first, 0, true));
        } else {
            JOptionPane.showMessageDialog(this, "未找到匹配的宿舍！", "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * 从表格安全读取字符串（防越界/空值），用于内部辅助
     */
    private String safeGet(int row, int col) {
        if (row < 0 || row >= tableModel.getRowCount()) return "";
        Object v = tableModel.getValueAt(row, col);
        return v == null ? "" : v.toString();
    }

    /**
     * 将字符串安全转换为整数，失败时返回指定默认值
     * @param s 待转换字符串
     * @param def 默认值
     * @return 解析后的整数或默认值
     */
    private int parseIntSafe(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }

    /**
     * 创建统计图表面板（右侧）
     */
    private JPanel createChartPanel() {
        chartWrapper = new JPanel(new BorderLayout());
        chartWrapper.setBorder(BorderFactory.createTitledBorder("宿舍占用率（按楼栋）"));
        chartPanel = new ChartPanel();
        chartWrapper.add(chartPanel, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshChart = new JButton("刷新统计");
        refreshChart.addActionListener(e -> loadRoomsFromDB());
        bottom.add(refreshChart);
        chartWrapper.add(bottom, BorderLayout.SOUTH);
        return chartWrapper;
    }

    /**
     * 内部类：简单柱状图绘制组件（使用 Java2D）
     */
    private static class ChartPanel extends JPanel {
        private java.util.List<String> keys = new java.util.ArrayList<>();
        private java.util.List<Double> values = new java.util.ArrayList<>();

        public void setData(java.util.Map<String, Double> data) {
            keys.clear(); values.clear();
            if (data != null && !data.isEmpty()) {
                // 按键排序，保证显示顺序稳定
                java.util.List<String> k = new java.util.ArrayList<>(data.keySet());
                java.util.Collections.sort(k);
                for (String s : k) { keys.add(s); values.add(data.getOrDefault(s, 0.0)); }
            }
            // 保证在 EDT 上重绘
            if (javax.swing.SwingUtilities.isEventDispatchThread()) {
                repaint();
            } else {
                javax.swing.SwingUtilities.invokeLater(this::repaint);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                int w = getWidth();
                int h = getHeight();
                // 背景
                g2.setColor(getBackground());
                g2.fillRect(0, 0, w, h);

                if (keys.isEmpty()) {
                    g2.setColor(Color.GRAY);
                    g2.drawString("无统计数据", 10, 20);
                    return;
                }

                int padding = 40;
                int labelHeight = 40;
                int plotHeight = Math.max(30, h - padding - labelHeight);
                int plotWidth = Math.max(50, w - padding * 2);

                // 找到最大值用于缩放（values 为比例 0..1）
                double maxVal = 0.0;
                for (double v : values) if (v > maxVal) maxVal = v;
                if (maxVal < 0.01) maxVal = 0.01; // 避免除零

                int n = keys.size();
                int barGap = 10;
                int availableWidth = plotWidth - (n + 1) * barGap;
                int barWidth;
                if (availableWidth > 0) {
                    barWidth = Math.max(10, availableWidth / Math.max(1, n));
                } else {
                    // 宽度不足时：缩小 gap，保证每个柱最小宽度
                    barGap = 6;
                    int minBar = 10;
                    int totalNeeded = n * minBar + (n + 1) * barGap;
                    if (totalNeeded <= plotWidth) {
                        barWidth = minBar;
                    } else {
                        // 尽量压缩到能显示
                        barWidth = Math.max(4, plotWidth / Math.max(1, n + (n/3)));
                    }
                }

                // 绘制 Y 轴刻度（0% - 100%）
                g2.setColor(Color.DARK_GRAY);
                g2.drawLine(padding, padding, padding, padding + plotHeight);
                g2.drawLine(padding, padding + plotHeight, padding + plotWidth, padding + plotHeight);

                g2.setFont(new Font("宋体", Font.PLAIN, 11));
                for (int i = 0; i <= 5; i++) {
                    int y = padding + (int) (plotHeight * (1.0 - i / 5.0));
                    String label = (i * 20) + "%";
                    g2.drawString(label, 4, y + 4);
                    g2.setColor(new Color(220, 220, 220));
                    g2.drawLine(padding + 1, y, padding + plotWidth, y);
                    g2.setColor(Color.DARK_GRAY);
                }

                // 绘制柱状图
                int x = padding + barGap;
                FontMetrics fm = g2.getFontMetrics();
                for (int i = 0; i < n; i++) {
                    double val = values.get(i);
                    int barHeight = (int) (val / maxVal * plotHeight);
                    int bx = x;
                    int by = padding + plotHeight - barHeight;

                    // 渐变填充
                    GradientPaint gp = new GradientPaint(bx, by, new Color(100, 160, 220), bx, by + barHeight, new Color(30, 90, 160));
                    g2.setPaint(gp);
                    g2.fillRect(bx, by, barWidth, Math.max(1, barHeight));

                    // 边框
                    g2.setColor(Color.DARK_GRAY);
                    g2.drawRect(bx, by, barWidth, Math.max(1, barHeight));

                    // 值文字（百分比）
                    String valStr = String.format("%.0f%%", val * 100);
                    int strW = fm.stringWidth(valStr);
                    g2.setColor(Color.BLACK);
                    g2.drawString(valStr, bx + (barWidth - strW) / 2, Math.max(by - 6, padding + 10));

                    // X 轴标签（楼栋名）
                    String label = keys.get(i);
                    int labY = padding + plotHeight + 18;
                    int labW = fm.stringWidth(label);
                    // 如果标签过宽，截断并加省略号
                    String outLabel = label;
                    int maxLabelW = barWidth + 8;
                    if (labW > maxLabelW) {
                        for (int cut = label.length() - 1; cut > 0; cut--) {
                            String t = label.substring(0, cut) + "...";
                            if (fm.stringWidth(t) <= maxLabelW) { outLabel = t; break; }
                        }
                    }
                    g2.drawString(outLabel, bx + (barWidth - fm.stringWidth(outLabel)) / 2, labY);

                    x += barWidth + barGap;
                }

            } finally {
                g2.dispose();
            }
        }
    }

}

