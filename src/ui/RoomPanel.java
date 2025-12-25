package ui;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

import model.Room;
import service.RoomService;
import service.impl.RoomServiceImpl;
import util.DBUtil;

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
        add(createTablePanel(), BorderLayout.CENTER);
        add(createInfoPanel(), BorderLayout.SOUTH);
    }

    /**
     * 创建顶部工具栏：包含操作按钮、过滤条件与搜索控件
     * @return 工具栏 JPanel
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
                return;
            }

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
            }
            updateRoomCount();
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
        }
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

            // 新增：检查房间号唯一（使用已有的 roomService 实例）
            if (roomService.existsByRoomNumber(roomNumber)) {
                JOptionPane.showMessageDialog(dialog, "房间号已存在，请检查输入。", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean ok = roomService.add(room);
            if (ok) {
                loadRoomsFromDB();
                // 尝试在表格中定位新添加的宿舍并选中
                boolean found = false;
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    Object v = tableModel.getValueAt(i, 0);
                    if (v != null && v.toString().equals(roomNumber)) {
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
                JOptionPane.showMessageDialog(dialog, "宿舍添加失败，请检查数据库连接或重复房间号。", "错误", JOptionPane.ERROR_MESSAGE);
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
        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要删除宿舍 [" + roomNumber + "] 吗？",
                "确认删除",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
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
            boolean ok = roomService.updateOccupancy(roomNumber, newOccupied, newAvailable, statusText);
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

            int currentOccupied = parseIntSafe(safeGet(selectedRow, 4), 0);
            int totalBeds = parseIntSafe(safeGet(selectedRow, 3), 0);

            int newOccupied = Math.max(0, currentOccupied - checkoutCount);
            int newAvailable = Math.max(0, totalBeds - newOccupied);
            String statusText = newOccupied == 0 ? "空置" : "有空位";

            String roomNumber = safeGet(selectedRow, 0);
            boolean ok = roomService.updateOccupancy(roomNumber, newOccupied, newAvailable, statusText);
            if (ok) {
                loadRoomsFromDB();
                JOptionPane.showMessageDialog(dialog,
                        "退宿处理成功！\n退宿人数: " + checkoutCount +
                                (reason.isEmpty() ? "" : "\n退宿原因: " + reason));
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "退宿失败，请检查数据库连接。", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
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

}

