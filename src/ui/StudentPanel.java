package ui;

import java.time.LocalDate;
import service.*;
import model.Student;
import model.Room;
import service.impl.StudentServiceImpl;
import service.impl.RoomServiceImpl;
import util.RefreshCenter;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * 学生管理面板 - 统一分配版
 * 功能：将编辑与分配宿舍合并，支持基本信息、宿舍号、床位号的一站式管理
 */
public class StudentPanel extends JPanel {
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JLabel countLabel;

    public StudentPanel() {
        initUI();
        loadStudentsFromDB();
        // 注册刷新监听
        RefreshCenter.register("students-updated", this::loadStudentsFromDB);
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(createToolBar(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createInfoPanel(), BorderLayout.SOUTH);
    }

    private JPanel createToolBar() {
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        toolBar.setBorder(BorderFactory.createTitledBorder("学生管理"));

        // 合并后的按钮组
        String[] buttons = {"新增", "分配宿舍", "删除"};
        for (String text : buttons) {
            JButton button = new JButton(text);
            button.setBackground(new Color(70, 130, 180));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.addActionListener(this::handleButtonClick);
            toolBar.add(button);
        }

        toolBar.add(Box.createHorizontalStrut(20));
        toolBar.add(new JLabel("快速搜索:"));
        searchField = new JTextField(15);
        toolBar.add(searchField);
        JButton searchBtn = new JButton("查询");
        searchBtn.addActionListener(e -> searchStudents());
        toolBar.add(searchBtn);

        return toolBar;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] columns = {"学号", "姓名", "性别", "学院", "专业", "年级", "班级", "宿舍号", "床位号", "联系电话", "入住日期"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        studentTable = new JTable(tableModel);
        studentTable.setRowHeight(25);
        studentTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        panel.add(new JScrollPane(studentTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        countLabel = new JLabel("学生总数: 0");
        infoPanel.add(countLabel);
        return infoPanel;
    }

    private void loadStudentsFromDB() {
        new Thread(() -> {
            List<Student> list = new StudentServiceImpl().listStudents();
            SwingUtilities.invokeLater(() -> {
                tableModel.setRowCount(0);
                for (Student s : list) {
                    tableModel.addRow(new Object[]{
                            s.getSno(), s.getName(), s.getGender(), s.getCollege(),
                            s.getMajor(), s.getGrade(), s.getClazz(),
                            getSafeStr(s.getRoomNumber()), getSafeStr(s.getBedNumber()),
                            s.getPhone(), s.getInDate()
                    });
                }
                countLabel.setText("学生总数: " + tableModel.getRowCount());
            });
        }).start();
    }

    private void handleButtonClick(ActionEvent e) {
        String cmd = ((JButton) e.getSource()).getText();
        if (cmd.equals("分配宿舍")) assignDormitoryAndEdit();
        else if (cmd.equals("删除")) deleteStudent();
        else if (cmd.equals("新增")) addStudent();
    }

    /**
     * 【统一功能】：分配宿舍 + 编辑信息 + 分配床位
     */
    private void assignDormitoryAndEdit() {
        int row = studentTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先在列表中选中一名学生！");
            return;
        }

        // 获取当前行数据
        String sno = getTableValue(row, 0);
        String name = getTableValue(row, 1);
        String originalDorm = getTableValue(row, 7);
        String originalBed = getTableValue(row, 8);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "分配宿舍与信息编辑", true);
        dialog.setSize(420, 580);
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridLayout(11, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 基本信息组件
        JTextField txtName = new JTextField(name);
        JTextField txtPhone = new JTextField(getTableValue(row, 9));

        // 宿舍分配组件
        String bInit = "A栋", rInit = "";
        if (originalDorm.contains("栋")) {
            int idx = originalDorm.indexOf("栋");
            bInit = originalDorm.substring(0, idx + 1);
            rInit = originalDorm.substring(idx + 1);
        }
        JComboBox<String> cbBuilding = new JComboBox<>(new String[]{"A栋", "B栋", "C栋", "D栋"});
        cbBuilding.setSelectedItem(bInit);
        JTextField roomField = new JTextField(rInit);

        // --- 新增：床位分配组件 ---
        JTextField bedField = new JTextField(originalBed);

        JLabel roomStatusLabel = new JLabel(" ");
        roomStatusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        formPanel.add(new JLabel("学号:")); formPanel.add(new JLabel(sno));
        formPanel.add(new JLabel("姓名:")); formPanel.add(txtName);
        formPanel.add(new JLabel("联系电话:")); formPanel.add(txtPhone);
        formPanel.add(new JLabel(" ")); formPanel.add(new JLabel(" ")); // 分隔线感
        formPanel.add(new JLabel("分配楼栋:")); formPanel.add(cbBuilding);
        formPanel.add(new JLabel("房间号:")); formPanel.add(roomField);
        formPanel.add(new JLabel("床位号:")); formPanel.add(bedField);

        // --- 实时响应逻辑：DocumentListener ---
        Runnable checkRoomExist = () -> {
            String b = cbBuilding.getSelectedItem().toString();
            String r = roomField.getText().trim();
            if (r.isEmpty()) { roomStatusLabel.setText(""); return; }

            new Thread(() -> {
                Room found = new RoomServiceImpl().findAll().stream()
                        .filter(rm -> rm.getBuilding().equals(b) && rm.getRoomNumber().equals(r))
                        .findFirst().orElse(null);
                SwingUtilities.invokeLater(() -> {
                    if (found == null) {
                        roomStatusLabel.setText("❌ 宿舍不存在");
                        roomStatusLabel.setForeground(Color.RED);
                    } else {
                        String detail = found.getAvailableBeds() <= 0 ? "已满" : "余" + found.getAvailableBeds();
                        roomStatusLabel.setText("✅ " + found.getStatus() + " (" + detail + ")");
                        roomStatusLabel.setForeground(new Color(0, 128, 0));
                    }
                });
            }).start();
        };

        // 添加实时监听
        roomField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { checkRoomExist.run(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { checkRoomExist.run(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { checkRoomExist.run(); }
        });
        cbBuilding.addActionListener(e -> checkRoomExist.run());

        JButton saveBtn = new JButton("提交分配并保存");
        saveBtn.addActionListener(e -> {
            new Thread(() -> {
                try {
                    String newDorm = roomField.getText().trim().isEmpty() ? "" : cbBuilding.getSelectedItem() + roomField.getText().trim();
                    String newBed = bedField.getText().trim();

                    Student updated = new Student();
                    updated.setSno(sno);
                    updated.setName(txtName.getText().trim());
                    updated.setPhone(txtPhone.getText().trim());
                    updated.setRoomNumber(newDorm);
                    updated.setBedNumber(newBed); // 设置床位

                    RoomService roomService = new RoomServiceImpl();

                    // 宿舍/床位变动逻辑
                    if (!originalDorm.equals(newDorm)) {
                        // 迁出原宿舍
                        if (!originalDorm.isEmpty()) processRoomChange(roomService, originalDorm, -1);
                        // 迁入新宿舍
                        if (!newDorm.isEmpty()) {
                            if (!processRoomChange(roomService, newDorm, 1)) {
                                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(dialog, "分配失败：目标宿舍已满！"));
                                return;
                            }
                        }
                    }

                    if (new StudentServiceImpl().updateStudent(updated)) {
                        SwingUtilities.invokeLater(() -> {
                            dialog.dispose();
                            loadStudentsFromDB();
                            RefreshCenter.notify("students-updated");
                            RefreshCenter.notify("rooms-updated");
                            JOptionPane.showMessageDialog(this, "分配成功！");
                        });
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }).start();
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(roomStatusLabel, BorderLayout.NORTH);
        bottomPanel.add(saveBtn, BorderLayout.CENTER);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * 核心逻辑：自动更新人数并根据剩余床位切换“住满/有空位”
     */
    private boolean processRoomChange(RoomService service, String dormStr, int change) {
        int idx = dormStr.indexOf("栋");
        if (idx == -1) return false;
        String b = dormStr.substring(0, idx + 1);
        String r = dormStr.substring(idx + 1);

        Room room = service.findAll().stream()
                .filter(rm -> rm.getBuilding().equals(b) && rm.getRoomNumber().equals(r))
                .findFirst().orElse(null);

        if (room == null) return false;
        if (change > 0 && room.getAvailableBeds() <= 0) return false;

        int newOcc = Math.max(0, room.getOccupied() + change);
        int newAvail = room.getTotalBeds() - newOcc;
        // 自动计算状态
        String newStatus = (newAvail <= 0) ? "已住满" : "有空位";

        return service.updateOccupancy(r, newOcc, newAvail, newStatus);
    }

    private void deleteStudent() {
        int row = studentTable.getSelectedRow();
        if (row == -1) return;
        String sno = getTableValue(row, 0);
        String dorm = getTableValue(row, 7);
        if (JOptionPane.showConfirmDialog(this, "确定删除该学生吗？宿舍人数将自动释放。") == JOptionPane.YES_OPTION) {
            new Thread(() -> {
                if (!dorm.isEmpty()) processRoomChange(new RoomServiceImpl(), dorm, -1);
                if (new StudentServiceImpl().deleteStudent(sno)) SwingUtilities.invokeLater(this::loadStudentsFromDB);
            }).start();
        }
    }

    private void addStudent() {
        JOptionPane.showMessageDialog(this, "请使用系统注册功能新增学生。");
    }

    private String getTableValue(int r, int c) {
        Object o = tableModel.getValueAt(r, c);
        return o == null ? "" : o.toString();
    }

    private String getSafeStr(Object o) {
        return o == null ? "" : o.toString();
    }

    private void searchStudents() {
        String kw = searchField.getText().trim().toLowerCase();
        if (kw.isEmpty()) return;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (getTableValue(i,0).toLowerCase().contains(kw) || getTableValue(i,1).toLowerCase().contains(kw)) {
                studentTable.setRowSelectionInterval(i, i);
                studentTable.scrollRectToVisible(studentTable.getCellRect(i, 0, true));
                return;
            }
        }
    }
}