/*
 * 文件：StudentPanel.java
 * 说明：学生管理面板（管理员端），用于查看、添加、编辑学生记录等操作。
 * 注意：仅插入注释，不更改业务逻辑。
 */

package ui;

import model.Room;
import model.Student;
import service.RoomService;
import service.impl.RoomServiceImpl;
import service.impl.StudentServiceImpl;
import util.RefreshCenter;

import uimodel.UserManager;
import uimodel.UserType;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学生管理面板 - 统一分配版
 * 修复说明：封装了 addStudent 方法，修正了括号闭合错误，保留所有核心逻辑。
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

        // 增加“编辑”和“导入学生”按钮用于修改学生信息与批量导入
        String[] buttons = {"新增", "编辑", "分配宿舍", "删除", "导入学生"};
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
        else if (cmd.equals("编辑")) editStudent();
        else if (cmd.equals("导入学生")) importStudents();
    }

    // 一键导入学生（CSV）
    private void importStudents() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("导入学生（CSV）");
        int res = chooser.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;
        java.io.File file = chooser.getSelectedFile();
        if (file == null || !file.exists()) return;

        new Thread(() -> {
            boolean ok = new StudentServiceImpl().importFromCsv(file);
            SwingUtilities.invokeLater(() -> {
                if (ok) {
                    JOptionPane.showMessageDialog(this, "学生导入完成（部分或全部成功）");
                    loadStudentsFromDB();
                    RefreshCenter.notify("students-updated");
                } else {
                    JOptionPane.showMessageDialog(this, "学生导入失败或没有有效记录。", "导入结果", JOptionPane.WARNING_MESSAGE);
                }
            });
        }).start();
    }

    /**
     * 【分配宿舍 + 信息编辑】合并版：支持床位号分配与住满检测
     */
    private void assignDormitoryAndEdit() {
        int row = studentTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先在列表中选中一名学生！");
            return;
        }

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

        JTextField txtName = new JTextField(name);
        JTextField txtPhone = new JTextField(getTableValue(row, 9));

        String bInit = "A栋", rInit = "";
        if (originalDorm.contains("栋")) {
            int idx = originalDorm.indexOf("栋");
            bInit = originalDorm.substring(0, idx + 1);
            rInit = originalDorm.substring(idx + 1);
        }
        JComboBox<String> cbBuilding = new JComboBox<>(new String[]{"A栋", "B栋", "C栋", "D栋", "E栋"});
        cbBuilding.setSelectedItem(bInit);
        JTextField roomField = new JTextField(rInit);
        JTextField bedField = new JTextField(originalBed);

        JLabel roomStatusLabel = new JLabel(" ");
        roomStatusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        formPanel.add(new JLabel("学号:")); formPanel.add(new JLabel(sno));
        formPanel.add(new JLabel("姓名:")); formPanel.add(txtName);
        formPanel.add(new JLabel("联系电话:")); formPanel.add(txtPhone);
        formPanel.add(new JLabel(" ")); formPanel.add(new JLabel(" "));
        formPanel.add(new JLabel("分配楼栋:")); formPanel.add(cbBuilding);
        formPanel.add(new JLabel("房间号:")); formPanel.add(roomField);
        formPanel.add(new JLabel("床位号:")); formPanel.add(bedField);

        Runnable checkRoomExist = () -> {
            Object sel = cbBuilding.getSelectedItem();
            if (sel == null) { SwingUtilities.invokeLater(() -> roomStatusLabel.setText("")); return; }
            String b = sel.toString();
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

        roomField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { checkRoomExist.run(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { checkRoomExist.run(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { checkRoomExist.run(); }
        });

        JButton saveBtn = new JButton("提交分配并保存");
        saveBtn.addActionListener(e -> {
            new Thread(() -> {
                try {
                    Object sel = cbBuilding.getSelectedItem();
                    String newDorm = roomField.getText().trim().isEmpty() ? "" : (sel == null ? "" : sel.toString() + roomField.getText().trim());

                    // 从数据库读取完整学生对象，避免覆盖未显示的字段
                    StudentServiceImpl studentService = new StudentServiceImpl();
                    Student existing = studentService.getStudentBySno(sno);
                    if (existing == null) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(dialog, "未找到该学生的完整记录，无法保存。"));
                        return;
                    }

                    // 只修改需要变更的字段
                    existing.setName(txtName.getText().trim());
                    existing.setPhone(txtPhone.getText().trim());
                    existing.setRoomNumber(newDorm);
                    existing.setBedNumber(bedField.getText().trim());

                    RoomService roomService = new RoomServiceImpl();
                    if (!originalDorm.equals(newDorm)) {
                        if (!originalDorm.isEmpty()) processRoomChange(roomService, originalDorm, -1);
                        if (!newDorm.isEmpty()) {
                            if (!processRoomChange(roomService, newDorm, 1)) {
                                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(dialog, "分配失败：目标宿舍已满！"));
                                return;
                            }
                        }
                    }

                    if (studentService.updateStudent(existing)) {
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
     * 【新增学生】：处理基本信息录入
     */
    private void addStudent() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "添加学生", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(450, 550);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridLayout(11, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] labels = {"学号:", "姓名:", "性别:", "学院:", "专业:", "年级:", "班级:", "联系电话:", "紧急联系人:", "紧急联系电话:"};
        JComponent[] fields = new JComponent[labels.length];

        Map<String, String[]> collegeMajorMap = new HashMap<>();
        collegeMajorMap.put("信息工程学院", new String[]{"软件工程", "计算机科学与技术", "人工智能", "数据科学与大数据技术", "网络工程"});
        collegeMajorMap.put("经济管理学院", new String[]{"会计学", "财务管理", "工商管理", "市场营销", "金融学"});
        collegeMajorMap.put("电子信息学院", new String[]{"电子信息工程", "通信工程", "微电子科学与工程"});
        collegeMajorMap.put("机械工程学院", new String[]{"机械设计制造及其自动化", "车辆工程", "工业设计", "智能制造工程", "材料成型及控制工程"});
        collegeMajorMap.put("土木工程学院", new String[]{"土木工程", "建筑环境与能源应用工程", "给排水科学与工程", "工程管理", "道路桥梁与渡河工程"});
        collegeMajorMap.put("外国语学院", new String[]{"英语", "日语", "法语", "德语", "翻译", "商务英语"});
        collegeMajorMap.put("艺术学院", new String[]{"美术学", "音乐学", "舞蹈学", "视觉传达设计", "环境设计", "产品设计"});
        collegeMajorMap.put("理学院", new String[]{"数学与应用数学", "物理学", "化学", "应用化学", "统计学"});
        collegeMajorMap.put("教育学院", new String[]{"教育学", "学前教育", "小学教育", "特殊教育", "教育技术学"});
        collegeMajorMap.put("体育学院", new String[]{"体育教育", "社会体育指导与管理", "运动训练", "武术与民族传统体育"});
        collegeMajorMap.put("电气工程学院", new String[]{"电气工程及其自动化", "自动化", "测控技术与仪器", "机器人工程"});
        collegeMajorMap.put("化工学院", new String[]{"化学工程与工艺", "制药工程", "能源化学工程", "应用化学"});
        collegeMajorMap.put("马克思主义学院", new String[]{"思想政治教育", "马克思主义理论", "科学社会主义"});

        for (int i = 0; i < labels.length; i++) {
            formPanel.add(new JLabel(labels[i]));
            if (labels[i].equals("性别:")) {
                fields[i] = new JComboBox<>(new String[]{"男", "女"});
            } else if (labels[i].equals("学院:")) {
                fields[i] = new JComboBox<>(collegeMajorMap.keySet().toArray(new String[0]));
            } else if (labels[i].equals("专业:")) {
                fields[i] = new JComboBox<String>();
            } else {
                fields[i] = new JTextField();
            }
            formPanel.add(fields[i]);
        }

        JComboBox<String> collegeCombo = (JComboBox<String>) fields[3];
        JComboBox<String> majorCombo = (JComboBox<String>) fields[4];

        collegeCombo.addActionListener(e -> {
            majorCombo.removeAllItems();
            String[] majors = collegeMajorMap.get(collegeCombo.getSelectedItem());
            if (majors != null) for (String m : majors) majorCombo.addItem(m);
        });
        collegeCombo.setSelectedIndex(0);

        JButton saveButton = new JButton("保存");
        saveButton.addActionListener(e -> {
            Student student = new Student();
            student.setSno(((JTextField) fields[0]).getText().trim());
            student.setName(((JTextField) fields[1]).getText().trim());
            if (student.getSno().isEmpty() || student.getName().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "学号和姓名不能为空！");
                return;
            }
            student.setGender((String)((JComboBox)fields[2]).getSelectedItem());
            student.setCollege((String)collegeCombo.getSelectedItem());
            student.setMajor((String)majorCombo.getSelectedItem());
            student.setGrade(((JTextField) fields[5]).getText().trim());
            student.setClazz(((JTextField) fields[6]).getText().trim());
            student.setPhone(((JTextField) fields[7]).getText().trim());
            student.setInDate(LocalDate.now());

            // ---- 新增：在前端用户文件中也创建一个学生账号（学号作为用户名，默认密码 123456） ----
            UserManager um = UserManager.getInstance();
            String username = student.getSno();
            String defaultPassword = "123456";

            // 检查是否已存在相同学号或用户名
            if (um.getUserByUsername(username) != null || um.existsStudentId(student.getSno())) {
                JOptionPane.showMessageDialog(dialog, "无法创建学生账号：用户名或学号已存在，请检查后重试。", "创建账号失败", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean registered = um.registerUser(username, defaultPassword, UserType.STUDENT,
                    student.getSno(), student.getName(), student.getPhone(), "");

            if (!registered) {
                JOptionPane.showMessageDialog(dialog, "为学生创建前端账号失败，请稍后重试。", "失败", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 继续写入学生表；若写入失败则回滚 users.dat 中的用户
            StudentServiceImpl sSvc = new StudentServiceImpl();
            boolean dbOk = false;
            try {
                dbOk = sSvc.addStudent(student);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (!dbOk) {
                // 回滚前端用户
                um.removeUserByUsername(username);
                JOptionPane.showMessageDialog(dialog, "将学生信息保存到数据库失败，已回滚创建的前端账号。", "数据库写入失败", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 成功
            JOptionPane.showMessageDialog(dialog, "学生添加成功！已为该学生创建前端账号：用户名=" + username + " 密码=" + defaultPassword);
            dialog.dispose();
            loadStudentsFromDB();
            RefreshCenter.notify("students-updated");
        });

        JPanel btnPnl = new JPanel();
        btnPnl.add(saveButton);
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(btnPnl, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * 编辑学生信息（不修改学号与入学日期）。
     */
    private void editStudent() {
        int row = studentTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先在列表中选中一名学生！");
            return;
        }

        String sno = getTableValue(row, 0);
        String name = getTableValue(row, 1);
        String gender = getTableValue(row, 2);
        String college = getTableValue(row, 3);
        String major = getTableValue(row, 4);
        String grade = getTableValue(row, 5);
        String clazz = getTableValue(row, 6);
        String phone = getTableValue(row, 9);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "编辑学生信息", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(450, 500);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridLayout(9, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] labels = {"学号:", "姓名:", "性别:", "学院:", "专业:", "年级:", "班级:", "联系电话:"};
        JComponent[] fields = new JComponent[labels.length];

        Map<String, String[]> collegeMajorMap = new HashMap<>();
        collegeMajorMap.put("信息工程学院", new String[]{"软件工程", "计算机科学与技术", "人工智能", "数据科学与大数据技术", "网络工程"});
        collegeMajorMap.put("经济管理学院", new String[]{"会计学", "财务管理", "工商管理", "市场营销", "金融学"});
        collegeMajorMap.put("电子信息学院", new String[]{"电子信息工程", "通信工程", "微电子科学与工程"});
        collegeMajorMap.put("机械工程学院", new String[]{"机械设计制造及其自动化", "车辆工程", "工业设计", "智能制造工程", "材料成型及控制工程"});
        collegeMajorMap.put("土木工程学院", new String[]{"土木工程", "建筑环境与能源应用工程", "给排水科学与工程", "工程管理", "道路桥梁与渡河工程"});
        collegeMajorMap.put("外国语学院", new String[]{"英语", "日语", "法语", "德语", "翻译", "商务英语"});
        collegeMajorMap.put("艺术学院", new String[]{"美术学", "音乐学", "舞蹈学", "视觉传达设计", "环境设计", "产品设计"});
        collegeMajorMap.put("理学院", new String[]{"数学与应用数学", "物理学", "化学", "应用化学", "统计学"});
        collegeMajorMap.put("教育学院", new String[]{"教育学", "学前教育", "小学教育", "特殊教育", "教育技术学"});
        collegeMajorMap.put("体育学院", new String[]{"体育教育", "社会体育指导与管理", "运动训练", "武术与民族传统体育"});
        collegeMajorMap.put("电气工程学院", new String[]{"电气工程及其自动化", "自动化", "测控技术与仪器", "机器人工程"});
        collegeMajorMap.put("化工学院", new String[]{"化学工程与工艺", "制药工程", "能源化学工程", "应用化学"});
        collegeMajorMap.put("马克思主义学院", new String[]{"思想政治教育", "马克思主义理论", "科学社会主义"});


        for (int i = 0; i < labels.length; i++) {
            formPanel.add(new JLabel(labels[i]));
            if (labels[i].equals("性别:")) {
                JComboBox<String> cb = new JComboBox<>(new String[]{"男", "女"});
                cb.setSelectedItem(gender == null || gender.isEmpty() ? "男" : gender);
                fields[i] = cb;
            } else if (labels[i].equals("学院:")) {
                JComboBox<String> cb = new JComboBox<>(collegeMajorMap.keySet().toArray(new String[0]));
                cb.setSelectedItem(college == null || college.isEmpty() ? cb.getItemAt(0) : college);
                fields[i] = cb;
            } else if (labels[i].equals("专业:")) {
                fields[i] = new JComboBox<String>();
            } else if (labels[i].equals("学号:")) {
                JTextField tf = new JTextField(sno);
                tf.setEditable(false);
                fields[i] = tf;
            } else if (labels[i].equals("姓名:")) {
                fields[i] = new JTextField(name);
            } else if (labels[i].equals("年级:")) {
                fields[i] = new JTextField(grade);
            } else if (labels[i].equals("班级:")) {
                fields[i] = new JTextField(clazz);
            } else if (labels[i].equals("联系电话:")) {
                fields[i] = new JTextField(phone);
            } else {
                fields[i] = new JTextField();
            }
            formPanel.add(fields[i]);
        }

        JComboBox<String> collegeCombo = (JComboBox<String>) fields[3];
        JComboBox<String> majorCombo = (JComboBox<String>) fields[4];

        collegeCombo.addActionListener(e -> {
            majorCombo.removeAllItems();
            String[] majors = collegeMajorMap.get(collegeCombo.getSelectedItem());
            if (majors != null) for (String m : majors) majorCombo.addItem(m);
        });
        // 触发一次以填充专业
        collegeCombo.setSelectedItem(college == null || college.isEmpty() ? collegeCombo.getItemAt(0) : college);
        // 选择专业
        if (major != null && !major.isEmpty()) majorCombo.setSelectedItem(major);

        JButton saveButton = new JButton("保存");
        saveButton.addActionListener(e -> {
            new Thread(() -> {
                try {
                    StudentServiceImpl studentService = new StudentServiceImpl();
                    Student existing = studentService.getStudentBySno(sno);
                    if (existing == null) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(dialog, "未能找到该学生记录，无法保存。"));
                        return;
                    }

                    // 只修改表单中的字段，保留其他字段不变
                    existing.setName(((JTextField) fields[1]).getText().trim());
                    existing.setGender((String) ((JComboBox) fields[2]).getSelectedItem());
                    existing.setCollege((String) collegeCombo.getSelectedItem());
                    existing.setMajor((String) majorCombo.getSelectedItem());
                    existing.setGrade(((JTextField) fields[5]).getText().trim());
                    existing.setClazz(((JTextField) fields[6]).getText().trim());
                    existing.setPhone(((JTextField) fields[7]).getText().trim());

                    if (studentService.updateStudent(existing)) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(dialog, "学生信息更新成功！");
                            dialog.dispose();
                            loadStudentsFromDB();
                            RefreshCenter.notify("students-updated");
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(dialog, "更新失败！请检查输入或查看日志。"));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(dialog, "操作失败：" + ex.getMessage()));
                }
            }).start();
        });

        JPanel btnPnl = new JPanel();
        btnPnl.add(saveButton);
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(btnPnl, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

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
        String newStatus = (newAvail <= 0) ? "已住满" : "有空位";
        // 使用楼栋与房间号联合更新，避免影响其他楼栋的同号房间
        return service.updateOccupancy(b, r, newOcc, newAvail, newStatus);
    }

    private void deleteStudent() {
        int row = studentTable.getSelectedRow();
        if (row == -1) return;
        String sno = getTableValue(row, 0);
        String dorm = getTableValue(row, 7);
        if (JOptionPane.showConfirmDialog(this, "确定删除该学生吗？宿舍人数将自动释放。") == JOptionPane.YES_OPTION) {
            new Thread(() -> {
                if (!dorm.isEmpty()) processRoomChange(new RoomServiceImpl(), dorm, -1);
                if (new StudentServiceImpl().deleteStudent(sno)) {
                    SwingUtilities.invokeLater(() -> {
                        loadStudentsFromDB();
                        RefreshCenter.notify("students-updated");
                        RefreshCenter.notify("rooms-updated");
                    });
                }
            }).start();
        }
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