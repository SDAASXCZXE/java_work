package ui;

import java.time.LocalDate;
import service.*;
import model.Student;
import service.impl.StudentServiceImpl;
import util.RefreshCenter;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

  // 用于数据库中的日期类型



/**
 * 学生管理面板
 */
public class StudentPanel extends JPanel {
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JLabel countLabel; // 添加成员变量

    public StudentPanel() {
        initUI();
        loadStudentsFromDB();

        // 注册刷新监听器，当注册对话框或其它地方通知 students-updated 时，刷新表格
        RefreshCenter.register("students-updated", this::loadStudentsFromDB);
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 顶部工具栏
        add(createToolBar(), BorderLayout.NORTH);

        // 中间表格
        add(createTablePanel(), BorderLayout.CENTER);

        // 底部信息栏
        add(createInfoPanel(), BorderLayout.SOUTH);
    }

    /**
     * 创建工具栏
     */
    private JPanel createToolBar() {
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        toolBar.setBorder(BorderFactory.createTitledBorder("学生管理"));

        // 操作按钮
        String[] buttons = {"新增", "编辑", "删除", "分配宿舍", "查看详情", "导出数据"};

        for (String text : buttons) {
            JButton button = new JButton(text);
            button.setBackground(new Color(70, 130, 180));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.addActionListener(this::handleButtonClick);
            toolBar.add(button);
        }

        // 搜索功能
        toolBar.add(Box.createHorizontalStrut(20));
        toolBar.add(new JLabel("搜索:"));
        searchField = new JTextField(15);
        toolBar.add(searchField);

        JButton searchButton = new JButton("搜索");
        searchButton.addActionListener(e -> searchStudents());
        toolBar.add(searchButton);

        JButton resetButton = new JButton("重置");
        resetButton.addActionListener(e -> resetSearch());
        toolBar.add(resetButton);

        return toolBar;
    }

    /**
     * 创建表格面板
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // 创建表格模型
        String[] columns = {"学号", "姓名", "性别", "学院", "专业", "年级", "班级", "宿舍号", "床位号", "联系电话", "入住日期"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 表格不可直接编辑
            }
        };

        studentTable = new JTable(tableModel);
        studentTable.setRowHeight(25);
        studentTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        studentTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        // 设置列宽
        studentTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // 学号
        studentTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // 姓名
        studentTable.getColumnModel().getColumn(2).setPreferredWidth(50);  // 性别
        studentTable.getColumnModel().getColumn(3).setPreferredWidth(100); // 学院
        studentTable.getColumnModel().getColumn(4).setPreferredWidth(100); // 专业

        // 添加滚动条
        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("学生列表"));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 创建信息面板
     */
    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBorder(BorderFactory.createEtchedBorder());

        // 直接创建countLabel，避免通过索引访问
        countLabel = new JLabel("学生总数: 0");
        JLabel selectedLabel = new JLabel("已选中: 0");

        // 监听表格选择变化
        studentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedLabel.setText("已选中: " + studentTable.getSelectedRowCount());
            }
        });

        infoPanel.add(countLabel);
        infoPanel.add(Box.createHorizontalStrut(20));
        infoPanel.add(selectedLabel);

        return infoPanel;
    }

    /**
     * 更新学生总数 - 修复的方法
     */
    private void updateStudentCount() {
        if (countLabel != null) {
            countLabel.setText("学生总数: " + tableModel.getRowCount());
        }
    }

    /**
     * 从数据库中加载 表示显示功能（通过 Service 层，保持与 Room/Attendance 一致）
     */
    private void loadStudentsFromDB() {
        tableModel.setRowCount(0);

        try {
            service.StudentService studentService =
                    new service.impl.StudentServiceImpl();
            java.util.List<Student> students = studentService.listStudents();

            /* ================= 回退：直接 JDBC ================= */
            if (students == null || students.isEmpty()) {
                try (java.sql.Connection conn = util.DBUtil.getConnection()) {
                    if (conn == null) {
                        JOptionPane.showMessageDialog(this,
                                "无法连接到数据库，请检查 DBUtil 配置。",
                                "提示", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    try (java.sql.Statement stmt = conn.createStatement();
                         java.sql.ResultSet rs =
                                 stmt.executeQuery("SELECT * FROM student")) {

                        while (rs.next()) {

                            // 宿舍号（building + room_number）
                            String dorm = "";
                            try {
                                String building = rs.getString("building");
                                String roomNo = rs.getString("room_number");
                                if (building != null && roomNo != null) {
                                    dorm = building + roomNo;
                                } else if (roomNo != null) {
                                    dorm = roomNo;
                                }
                            } catch (Exception ignored) {}

                            // 床位号
                            String bed = "";
                            try {
                                int b = rs.getInt("bed_number");
                                if (!rs.wasNull()) bed = String.valueOf(b);
                            } catch (Exception ignored) {}

                            // 入住日期
                            Object inDate = "";
                            try {
                                java.sql.Date d = rs.getDate("in_date");
                                if (d != null) inDate = d;
                            } catch (Exception ignored) {}

                            Object[] row = {
                                    rs.getString("sno"),
                                    rs.getString("name"),
                                    rs.getString("gender"),
                                    rs.getString("college"),
                                    rs.getString("major"),
                                    rs.getString("grade"),
                                    rs.getString("class"),
                                    dorm,
                                    bed,
                                    rs.getString("phone"),
                                    inDate
                            };
                            tableModel.addRow(row);
                        }
                    }

                    updateStudentCount();
                    return;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                            "通过直接 JDBC 回退加载学生失败：" + ex.getMessage(),
                            "错误", JOptionPane.ERROR_MESSAGE);
                }
            }

            /* ================= 正常：Service ================= */
            for (Student s : students) {

                // 宿舍号：Student 里只有 roomNumber
                String dorm = s.getRoomNumber() == null ? "" : s.getRoomNumber();

                // 床位号：Student 里是 String
                String bed = s.getBedNumber() == null ? "" : s.getBedNumber();

                // 入住日期
                Object inDate = s.getInDate() == null
                        ? ""
                        : java.sql.Date.valueOf(s.getInDate());

                Object[] row = {
                        s.getSno(),
                        s.getName(),
                        s.getGender(),
                        s.getCollege(),
                        s.getMajor(),
                        s.getGrade(),
                        s.getClazz(),
                        dorm,
                        bed,
                        s.getPhone(),
                        inDate
                };
                tableModel.addRow(row);
            }

            updateStudentCount();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "加载学生数据失败：" + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }

        /* ================= 空数据提示 ================= */
        if (tableModel.getRowCount() == 0) {
            tableModel.setRowCount(0);
            tableModel.addRow(new Object[]{
                    "", "暂无学生记录或无法连接数据库",
                    "", "", "", "", "", "", "", "", ""
            });
            updateStudentCount();
        }
    }

    /**
     * 处理按钮点击
     */
    private void handleButtonClick(ActionEvent e) {
        String command = ((JButton) e.getSource()).getText();

        switch (command) {
            case "新增":
                addStudent();
                break;
            case "编辑":
                editStudent();
                break;
            case "删除":
                deleteStudent();
                break;
            case "分配宿舍":
                assignDormitory();
                break;
           // case "查看详情":
             //   viewDetails();
            //    break;
           // case "导出数据":
             //   exportData();
               // break;
        }
    }

    /**
     * 添加学生
     */
    private void addStudent() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "添加学生", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridLayout(11, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] labels = {"学号:", "姓名:", "性别:", "学院:", "专业:", "年级:", "班级:", "联系电话:", "紧急联系人:", "紧急联系电话:"};
        JTextField[] fields = new JTextField[labels.length];

        for (int i = 0; i < labels.length; i++) {
            formPanel.add(new JLabel(labels[i]));
            fields[i] = new JTextField();
            formPanel.add(fields[i]);
        }

        dialog.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        // 保存按钮事件监听器 - 放在正确的位置
        saveButton.addActionListener(e -> {
            // 验证必填字段
            if (fields[0].getText().trim().isEmpty() || fields[1].getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "学号和姓名不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 创建学生对象并设置属性
            Student student = new Student();
            student.setSno(fields[0].getText().trim());
            student.setName(fields[1].getText().trim());
            student.setGender(fields[2].getText().trim());
            student.setCollege(fields[3].getText().trim());
            student.setMajor(fields[4].getText().trim());
            student.setGrade(fields[5].getText().trim());
            student.setClazz(fields[6].getText().trim());
            student.setPhone(fields[7].getText().trim());
            // 注意：紧急联系人相关字段可能需要在Student模型中添加
            student.setInDate(LocalDate.now());

            // 调用业务层添加学生
            StudentService studentService = new StudentServiceImpl();

            // 新增：在添加前检查学号是否已存在
            try {
                if (studentService.existsBySno(student.getSno())) {
                    JOptionPane.showMessageDialog(dialog, "该学号已存在，请检查输入。", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                boolean added = studentService.addStudent(student);
                if (added) {
                    JOptionPane.showMessageDialog(dialog, "学生添加成功！");
                    dialog.dispose(); // 关闭对话框
                    loadStudentsFromDB(); // 刷新表格数据
                    // 通知其它组件（若需要）
                    util.RefreshCenter.notify("students-updated");
                } else {
                    JOptionPane.showMessageDialog(dialog, "学生保存到数据库失败，请检查数据库或日志。", "错误", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "添加学生失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 取消按钮事件监听器
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // 显示对话框
        dialog.setVisible(true);
    }
    /**
     * 删除学生（走 Service → DAO → 数据库）
     */
    private void deleteStudent() {
        // 1. 获取选中行
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "请先选择要删除的学生！",
                    "提示",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // 2. 获取学生学号（第0列）
        String studentSno = tableModel.getValueAt(selectedRow, 0).toString();

        // 3. 获取学生姓名（第1列）
        String studentName = tableModel.getValueAt(selectedRow, 1).toString();

        // 4. 删除确认
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "确定要删除学生 [" + studentName + "] 吗？",
                "确认删除",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // 5. 调用 Service，真正删除数据库数据
                StudentService studentService = new StudentServiceImpl();
                boolean deleted = studentService.deleteStudent(studentSno);
                if (deleted) {
                    // 6. 重新加载数据库数据到 JTable
                    loadStudentsFromDB();
                    // 7. 更新学生数量
                    updateStudentCount();
                    // 通知其它组件
                    util.RefreshCenter.notify("students-updated");
                    JOptionPane.showMessageDialog(this, "删除成功！");
                } else {
                    JOptionPane.showMessageDialog(this, "删除学生失败，请检查数据库或日志。", "错误", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();  // 调试用
                JOptionPane.showMessageDialog(
                        this,
                        "删除失败: " + e.getMessage(),
                        "删除失败",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    /**
     * 编辑学生
     */
    private void editStudent() {
        final int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要编辑的学生！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        final String studentSno = tableModel.getValueAt(selectedRow, 0).toString();

        final JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "编辑学生信息", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridLayout(10, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] labels = {"学号:", "姓名:", "性别:", "学院:", "专业:", "年级:", "班级:", "联系电话:", "宿舍楼:", "房间号:"};
        JComponent[] fields = new JComponent[labels.length];

        for (int i = 0; i < labels.length; i++) {
            formPanel.add(new JLabel(labels[i]));

            String currentValue = "";
            if (selectedRow < tableModel.getRowCount()) {
                switch (i) {
                    case 0: currentValue = tableModel.getValueAt(selectedRow, 0).toString(); break;
                    case 1: currentValue = tableModel.getValueAt(selectedRow, 1).toString(); break;
                    case 2: currentValue = tableModel.getValueAt(selectedRow, 2).toString(); break;
                    case 3: currentValue = tableModel.getValueAt(selectedRow, 3).toString(); break;
                    case 4: currentValue = tableModel.getValueAt(selectedRow, 4).toString(); break;
                    case 5: currentValue = tableModel.getValueAt(selectedRow, 5).toString(); break;
                    case 6: currentValue = tableModel.getValueAt(selectedRow, 6).toString(); break;
                    case 7: currentValue = tableModel.getValueAt(selectedRow, 9).toString(); break;
                    case 8:
                        String dorm = tableModel.getValueAt(selectedRow, 7).toString();
                        currentValue = dorm.length() > 1 ? dorm.substring(0, 1) : "A";
                        break;
                    case 9:
                        String room = tableModel.getValueAt(selectedRow, 7).toString();
                        currentValue = room.length() > 1 ? room.substring(1) : "";
                        break;
                }
            }

            if (i == 2) { // 性别
                JComboBox<String> genderCombo = new JComboBox<>(new String[]{"男", "女"});
                genderCombo.setSelectedItem(currentValue);
                fields[i] = genderCombo;
            } else if (i == 8) { // 宿舍楼
                JComboBox<String> buildingCombo = new JComboBox<>(new String[]{"A栋", "B栋", "C栋", "D栋"});
                buildingCombo.setSelectedItem(currentValue + "栋");
                fields[i] = buildingCombo;
            } else {
                JTextField textField = new JTextField(currentValue);
                if (i == 0) textField.setEditable(false); // 学号不可编辑
                fields[i] = textField;
            }
            formPanel.add((Component) fields[i]);
        }

        // 添加一个状态标签，显示宿舍是否存在与可分配床位信息
        JLabel roomStatusLabel = new JLabel("");
        roomStatusLabel.setForeground(new Color(80, 120, 160));

        // 将状态标签放入底部按钮左侧：改为一个容器放置状态与按钮
        dialog.add(formPanel, BorderLayout.CENTER);

        // 创建按钮面板（稍后和状态行一起加入南部）
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        // 绑定实时检测逻辑：当用户更改楼栋或房间号时，检测宿舍是否存在
        final model.Room[] previewRoom = new model.Room[1];
        previewRoom[0] = null;

        // 获取楼栋组件与房间号文本框（fields[8] 与 fields[9]）
        final JComponent buildingComp = fields[8];
        final JTextField roomField = (JTextField) fields[9];

        Runnable checkRoomExist = () -> {
            try {
                String b = "";
                if (buildingComp instanceof JComboBox) {
                    Object sel = ((JComboBox<?>) buildingComp).getSelectedItem();
                    b = sel == null ? "" : sel.toString();
                } else if (buildingComp instanceof JTextField) {
                    b = ((JTextField) buildingComp).getText().trim();
                }
                if (!b.endsWith("栋") && b.length() == 1) b = b + "栋";
                String rn = roomField.getText().trim();
                if (rn.isEmpty()) {
                    previewRoom[0] = null;
                    SwingUtilities.invokeLater(() -> roomStatusLabel.setText(""));
                    return;
                }

                service.RoomService rs = new service.impl.RoomServiceImpl();
                model.Room found = null;
                for (model.Room r : rs.findAll()) {
                    String rb = r.getBuilding() == null ? "" : r.getBuilding();
                    String rr = r.getRoomNumber() == null ? "" : r.getRoomNumber();
                    if (rb.equals(b) && rr.equals(rn)) {
                        found = r;
                        break;
                    }
                }
                if (found != null) {
                    previewRoom[0] = found;
                    if (found.getAvailableBeds() <= 0) {
                        SwingUtilities.invokeLater(() -> roomStatusLabel.setText("该宿舍已满！"));
                    } else {
                        final int availBeds = found.getAvailableBeds();
                        SwingUtilities.invokeLater(() -> roomStatusLabel.setText("宿舍存在，空余床位: " + availBeds));
                    }
                } else {
                    previewRoom[0] = null;
                    SwingUtilities.invokeLater(() -> roomStatusLabel.setText("宿舍不存在"));
                }
            } catch (Exception ex) {
                previewRoom[0] = null;
                SwingUtilities.invokeLater(() -> roomStatusLabel.setText("检查宿舍时发生错误"));
            }
        };

        // 给 roomField 添加文档监听（实时响应输入）
        roomField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { checkRoomExist.run(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { checkRoomExist.run(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { checkRoomExist.run(); }
        });

        // 给楼栋下拉添加监听
        if (buildingComp instanceof JComboBox) {
            ((JComboBox<?>) buildingComp).addActionListener(ae -> checkRoomExist.run());
        } else if (buildingComp instanceof JTextField) {
            ((JTextField) buildingComp).getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { checkRoomExist.run(); }
                @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { checkRoomExist.run(); }
                @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { checkRoomExist.run(); }
            });
        }

        // 将按钮和状态标签放到南部容器
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(roomStatusLabel, BorderLayout.WEST);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        southPanel.add(buttonPanel, BorderLayout.EAST);
        dialog.add(southPanel, BorderLayout.SOUTH);

        // 保存按钮的逻辑
        saveButton.addActionListener(e -> {
            try {
                // 1. 构建要更新的学生对象（从表单读取值）
                model.Student updated = new model.Student();

                // 学号
                String sno = ((JTextField) fields[0]).getText().trim();
                updated.setSno(sno);

                // 基本信息
                updated.setName(((JTextField) fields[1]).getText().trim());

                // 性别
                if (fields[2] instanceof JComboBox) {
                    Object sel = ((JComboBox<?>) fields[2]).getSelectedItem();
                    updated.setGender(sel == null ? "" : sel.toString());
                } else {
                    updated.setGender(((JTextField) fields[2]).getText().trim());
                }

                // 学院专业等信息
                updated.setCollege(((JTextField) fields[3]).getText().trim());
                updated.setMajor(((JTextField) fields[4]).getText().trim());
                updated.setGrade(((JTextField) fields[5]).getText().trim());
                updated.setClazz(((JTextField) fields[6]).getText().trim());
                updated.setPhone(((JTextField) fields[7]).getText().trim());

                // 获取宿舍楼和房间号
                String b = "";
                if (buildingComp instanceof JComboBox) {
                    Object sel = ((JComboBox<?>) buildingComp).getSelectedItem();
                    b = sel == null ? "" : sel.toString();
                } else if (buildingComp instanceof JTextField) {
                    b = ((JTextField) buildingComp).getText().trim();  //*栋
                }
                if (!b.endsWith("栋") && b.length() == 1) b = b + "栋";
                String rn = roomField.getText().trim();  // 房间号
                updated.setRoomNumber(b+rn);
                // 入住日期
                try {
                    Object inDateObj = tableModel.getValueAt(selectedRow, 10);
                    if (inDateObj instanceof java.sql.Date) {
                        updated.setInDate(((java.sql.Date) inDateObj).toLocalDate());
                    }
                } catch (Exception ignored) {}

                // 2. 处理宿舍分配逻辑
                service.RoomService roomService = new service.impl.RoomServiceImpl();

                // 获取原始宿舍信息（如果有的话）
                String originalDorm = tableModel.getValueAt(selectedRow, 7).toString();
                String originalBuilding = "";
                String originalRoomNumber = "";

                if (!originalDorm.isEmpty() && originalDorm.length() > 1) {
                    originalBuilding = originalDorm.substring(0, 1) + "栋";
                    originalRoomNumber = originalDorm.substring(1);
                }

                // 新宿舍信息
                String newBuilding = b;
                String newRoomNumber = rn;

                // 情况1: 没有选择宿舍（清空宿舍分配）
                if (newRoomNumber.isEmpty()) {

                    // 如果原来有宿舍，需要从原宿舍迁出
                    if (!originalRoomNumber.isEmpty()) {

                        model.Room originalRoom = null;
                        for (model.Room r : roomService.findAll()) {
                            if (r.getRoomNumber().equals(originalRoomNumber)
                                    && r.getBuilding().equals(originalBuilding)) {
                                originalRoom = r;
                                break;
                            }
                        }

                        if (originalRoom != null) {
                            int newOccupied = originalRoom.getOccupied() - 1;
                            int newAvailable = originalRoom.getAvailableBeds() + 1;

                            newOccupied = Math.max(0, newOccupied);
                            newAvailable = Math.min(originalRoom.getTotalBeds(), newAvailable);

                            String status = newOccupied >= originalRoom.getTotalBeds()
                                    ? model.Room.RoomStatus.FULL.name()
                                    : model.Room.RoomStatus.AVAILABLE.name();

                            roomService.updateOccupancy(
                                    originalRoomNumber, newOccupied, newAvailable, status);
                        }
                    }

                    // 清空床位号（字符串）
                    updated.setBedNumber("");
                }

                // 情况2: 选择了新宿舍
                else {

                    // 查找目标宿舍
                    model.Room targetRoom = null;
                    for (model.Room r : roomService.findAll()) {
                        if (r.getRoomNumber().equals(newRoomNumber)
                                && r.getBuilding().equals(newBuilding)) {
                            targetRoom = r;
                            break;
                        }
                    }

                    if (targetRoom == null) {
                        JOptionPane.showMessageDialog(dialog,
                                "宿舍 " + newBuilding + newRoomNumber + " 不存在！",
                                "错误", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (targetRoom.getAvailableBeds() <= 0) {
                        JOptionPane.showMessageDialog(dialog,
                                "宿舍 " + newBuilding + newRoomNumber + " 已满！",
                                "错误", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // ===== 核心修复：字符串 ↔ 数字 =====
                    int nextBedInt = targetRoom.getOccupied() + 1;

                    if (nextBedInt > targetRoom.getTotalBeds()) {
                        JOptionPane.showMessageDialog(dialog,
                                "宿舍 " + newBuilding + newRoomNumber + " 床位已满！",
                                "错误", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // 存回 Student：字符串
                    updated.setBedNumber(toStr(nextBedInt));

                    // ===== 是否换宿舍 =====
                    boolean isSameRoom =
                            originalBuilding.equals(newBuilding)
                                    && originalRoomNumber.equals(newRoomNumber);

                    if (!isSameRoom && !originalRoomNumber.isEmpty()) {

                        model.Room originalRoom = null;
                        for (model.Room r : roomService.findAll()) {
                            if (r.getRoomNumber().equals(originalRoomNumber)
                                    && r.getBuilding().equals(originalBuilding)) {
                                originalRoom = r;
                                break;
                            }
                        }

                        if (originalRoom != null) {
                            int newOccupied = originalRoom.getOccupied() - 1;
                            int newAvailable = originalRoom.getAvailableBeds() + 1;

                            newOccupied = Math.max(0, newOccupied);
                            newAvailable = Math.min(originalRoom.getTotalBeds(), newAvailable);

                            String status = newOccupied >= originalRoom.getTotalBeds()
                                    ? model.Room.RoomStatus.FULL.name()
                                    : model.Room.RoomStatus.AVAILABLE.name();

                            roomService.updateOccupancy(
                                    originalRoomNumber, newOccupied, newAvailable, status);
                        }
                    }
                }

                // 3. 保存学生信息
                service.StudentService studentService = new service.impl.StudentServiceImpl();
                boolean studentOk = studentService.updateStudent(updated);
                Student student = new Student();

                if (studentOk) {
                    JOptionPane.showMessageDialog(dialog, "学生信息修改成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadStudentsFromDB();
                    updateStudentCount();
                    util.RefreshCenter.notify("students-updated");
                } else {
                    JOptionPane.showMessageDialog(dialog, "保存学生信息失败！", "错误", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog,
                        "保存学生信息时发生错误：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);

    }

    /**
     * 分配宿舍
     */
    private void assignDormitory() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要分配宿舍的学生！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String studentName = tableModel.getValueAt(selectedRow, 1).toString();

        // 宿舍分配对话框
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "为 " + studentName + " 分配宿舍", true);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));
        dialog.setSize(300, 250);
        dialog.setLocationRelativeTo(this);

        dialog.add(new JLabel("宿舍楼:"));
        JComboBox<String> buildingCombo = new JComboBox<>(new String[]{"A栋", "B栋", "C栋", "D栋"});
        dialog.add(buildingCombo);

        dialog.add(new JLabel("房间号:"));
        JTextField roomField = new JTextField();
        dialog.add(roomField);

        dialog.add(new JLabel("床位号:"));
        JComboBox<String> bedCombo = new JComboBox<>(new String[]{"1号", "2号", "3号", "4号"});
        dialog.add(bedCombo);

        dialog.add(new JLabel("备注:"));
        JTextField remarkField = new JTextField();
        dialog.add(remarkField);

        JButton assignButton = new JButton("分配");
        JButton cancelButton = new JButton("取消");

        assignButton.addActionListener(e -> {
            String building = ((String) buildingCombo.getSelectedItem()).replace("栋", "");
            String room = roomField.getText().trim();
            String bed = (String) bedCombo.getSelectedItem();

            if (room.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请输入房间号！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            tableModel.setValueAt(building + room, selectedRow, 7);
            tableModel.setValueAt(bed, selectedRow, 8);

            JOptionPane.showMessageDialog(dialog, "宿舍分配成功！");
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(assignButton);
        dialog.add(cancelButton);

        dialog.setVisible(true);
    }

    /**
     * 导出学生数据
     */
    private void exportStudentData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("导出学生数据");
        fileChooser.setSelectedFile(new java.io.File("学生数据_" +
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
                                "学生数据导出成功！\n文件路径: " + finalFilePath + "\n导出记录数: " + tableModel.getRowCount(),
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
     * 搜索学生
     */
    private void searchStudents() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入搜索关键词！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 清除之前的高亮
        studentTable.clearSelection();

        boolean found = false;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            boolean rowMatch = false;
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                Object value = tableModel.getValueAt(i, j);
                if (value != null && value.toString().toLowerCase().contains(keyword.toLowerCase())) {
                    rowMatch = true;
                    found = true;
                    break;
                }
            }

            if (rowMatch) {
                studentTable.addRowSelectionInterval(i, i);
                studentTable.scrollRectToVisible(studentTable.getCellRect(i, 0, true));
                break; // 只选中第一个匹配项
            }
        }

        if (!found) {
            JOptionPane.showMessageDialog(this, "未找到匹配的学生！", "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * 重置搜索
     */
    private void resetSearch() {
        searchField.setText("");
        studentTable.clearSelection();
    }
    /**
     * 字符串转 int（安全）
     * 空串 / null / 非数字 → 返回 defaultValue
     */
    private int toInt(String value, int defaultValue) {
        if (value == null) return defaultValue;
        value = value.trim();
        if (value.isEmpty()) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * int 转字符串（统一出口）
     */
    private String toStr(int value) {
        return value <= 0 ? "" : String.valueOf(value);
    }

}

