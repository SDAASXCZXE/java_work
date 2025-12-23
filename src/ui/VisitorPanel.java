package ui;

// 此文件由自动化脚本修改以刷新索引

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import model.Visitor;
import service.VisitorService;
import service.impl.VisitorServiceImpl;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

/**
 * 访客记录管理面板
 */
public class VisitorPanel extends JPanel {
    private JTable visitorTable;
    private DefaultTableModel tableModel;
    private VisitorService visitorService = new VisitorServiceImpl();
    private DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public VisitorPanel() {
        initUI();
        loadVisitorsFromDB(); // 从数据库加载记录
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 顶部工具栏
        add(createToolBar(), BorderLayout.NORTH);

        // 中间表格
        add(createTablePanel(), BorderLayout.CENTER);

        // 底部统计信息
        add(createStatsPanel(), BorderLayout.SOUTH);
    }

    /**
     * 创建工具栏
     */
    private JPanel createToolBar() {
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        toolBar.setBorder(BorderFactory.createTitledBorder("访客记录管理"));

        String[] buttons = {"访客登记", "编辑记录", "删除记录", "查找访客", "导出记录"};

        for (String text : buttons) {
            JButton button = new JButton(text);
            button.setBackground(new Color(70, 130, 180));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.addActionListener(this::handleButtonClick);
            toolBar.add(button);
        }

        // 快速筛选
        toolBar.add(Box.createHorizontalStrut(20));
        toolBar.add(new JLabel("筛选:"));

        JComboBox<String> filterCombo = new JComboBox<>(new String[]{"全部", "今日", "本周", "本月"});
        filterCombo.addActionListener(e -> filterRecords((String)filterCombo.getSelectedItem()));
        toolBar.add(filterCombo);

        return toolBar;
    }

    /**
     * 创建表格面板
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = {"访客ID", "访客姓名", "来访时间", "离开时间", "访问宿舍", "被访学生",
                "来访事由", "证件类型", "证件号码", "联系电话", "备注"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        visitorTable = new JTable(tableModel);
        visitorTable.setRowHeight(25);
        visitorTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        visitorTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        // 设置列宽
        visitorTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        visitorTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        visitorTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        visitorTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        visitorTable.getColumnModel().getColumn(4).setPreferredWidth(80);

        JScrollPane scrollPane = new JScrollPane(visitorTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("访客记录列表"));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    /**
     * 创建统计面板
     */
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        panel.setBorder(BorderFactory.createEtchedBorder());

        String[] stats = {"今日访客: 0人", "本周访客: 0人", "本月访客: 0人", "当前在楼: 0人"};

        for (String stat : stats) {
            JLabel label = new JLabel(stat);
            label.setFont(new Font("微软雅黑", Font.BOLD, 12));
            label.setForeground(new Color(70, 130, 180));
            panel.add(label);
        }

        return panel;
    }

    /**
     * 处理按钮点击
     */
    private void handleButtonClick(ActionEvent e) {
        String command = ((JButton) e.getSource()).getText();

        switch (command) {
            case "访客登记":
                registerVisitor();
                break;
            case "编辑记录":
                editVisitor();
                break;
            case "删除记录":
                deleteVisitor();
                break;
            case "查找访客":
                searchVisitor();
                break;
            case "导出记录":
                exportRecords();
                break;
        }
    }

    /**
     * 使用数据库保存访客记录
     */
    private void registerVisitor() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "访客登记", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(480, 420);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // 字段：与数据库列顺序无关，但界面友好
        String[] labels = {"访客姓名:", "证件类型:", "证件号码:", "联系电话:", "访问宿舍:", "被访学生:", "来访事由:", "预计离开(可空):"};
        JComponent[] fields = new JComponent[labels.length];

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0;
            formPanel.add(new JLabel(labels[i]), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            if (i == 1) {
                fields[i] = new JComboBox<>(new String[]{"身份证", "护照", "学生证", "其他"});
            } else if (i == 6) {
                JTextArea ta = new JTextArea(3, 20);
                fields[i] = new JScrollPane(ta);
            } else {
                fields[i] = new JTextField();
            }
            formPanel.add(fields[i], gbc);
        }

        dialog.add(formPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        JButton saveBtn = new JButton("保存");
        JButton cancelBtn = new JButton("取消");

        saveBtn.addActionListener(ev -> {
            // 必填检查（姓名、证件类型、证件号码、联系电话）
            String name = ((JTextField) fields[0]).getText().trim();
            Object idTypeObj = fields[1] instanceof JComboBox ? ((JComboBox<?>) fields[1]).getSelectedItem() : null;
            String idType = idTypeObj == null ? "" : idTypeObj.toString();
            String idNumber = ((JTextField) fields[2]).getText().trim();
            String phone = ((JTextField) fields[3]).getText().trim();

            if (name.isEmpty() || idType.isEmpty() || idNumber.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "姓名、证件类型、证件号码和联系电话为必填项。", "输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Visitor v = new Visitor();
            v.setId(Visitor.generateId());
            v.setName(name);
            v.setIdType(idType);
            v.setIdNumber(idNumber);
            v.setPhone(phone);
            v.setVisitRoom(((JTextField) fields[4]).getText().trim());
            v.setTargetStudent(((JTextField) fields[5]).getText().trim());
            if (fields[6] instanceof JScrollPane) {
                JTextArea ta = (JTextArea) ((JScrollPane) fields[6]).getViewport().getView();
                v.setReason(ta.getText().trim());
            } else {
                v.setReason("");
            }
            // 解析预计离开时间（可为空）
            String leaveStr = ((JTextField) fields[7]).getText().trim();
            try {
                if (!leaveStr.isEmpty()) {
                    v.setLeaveTime(LocalDateTime.parse(leaveStr, fmt));
                }
            } catch (Exception ex) {
                // 忽略解析错误，用户可以后续修改
            }
            v.setArriveTime(LocalDateTime.now());
            v.setRemarks("");

            boolean ok = visitorService.addVisitor(v);
            if (ok) {
                Object[] row = new Object[]{v.getId(), v.getName(), v.getArriveTime() == null ? "" : fmt.format(v.getArriveTime()),
                        v.getLeaveTime().map(fmt::format).orElse(""), v.getVisitRoom(), v.getTargetStudent(), v.getReason(), v.getIdType(), v.getIdNumber(), v.getPhone(), v.getRemarks()};
                tableModel.insertRow(0, row);
                JOptionPane.showMessageDialog(dialog, "保存成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "保存失败，请检查数据库连接或输入。", "失败", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(ev -> dialog.dispose());

        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    /**
     * 编辑访客记录
     */
    private void editVisitor() {
        int r = visitorTable.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "请选择要编辑的记录。", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = tableModel.getValueAt(r, 0).toString();
        JOptionPane.showMessageDialog(this, "编辑功能尚未实现（记录ID=" + id + ")", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 删除访客记录
     */
    private void deleteVisitor() {
        int r = visitorTable.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "请选择要删除的记录。", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = tableModel.getValueAt(r, 0).toString();
        String name = tableModel.getValueAt(r, 1).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "确定删除访客记录 [" + name + "] 吗？", "确认", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = visitorService.removeById(id);
            if (ok) {
                tableModel.removeRow(r);
                JOptionPane.showMessageDialog(this, "删除成功");
            } else {
                JOptionPane.showMessageDialog(this, "删除失败，请检查数据库连接。", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 查找访客
     */
    private void searchVisitor() {
        String kw = JOptionPane.showInputDialog(this, "请输入访客姓名或证件号：", "查找", JOptionPane.QUESTION_MESSAGE);
        if (kw == null || kw.trim().isEmpty()) return;
        kw = kw.trim().toLowerCase();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String name = String.valueOf(tableModel.getValueAt(i, 1));
            String idNum = String.valueOf(tableModel.getValueAt(i, 8));
            if ((name != null && name.toLowerCase().contains(kw)) || (idNum != null && idNum.toLowerCase().contains(kw))) {
                visitorTable.setRowSelectionInterval(i, i);
                visitorTable.scrollRectToVisible(visitorTable.getCellRect(i, 0, true));
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "未找到匹配记录。", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 导出记录
     */
    private void exportRecords() {
        JOptionPane.showMessageDialog(this, "导出功能开发中...", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 筛选记录
     */
    private void filterRecords(String f) {
        JOptionPane.showMessageDialog(this, "筛选：" + f + "（功能开发中）", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 从数据库加载访客记录
     */
    private void loadVisitorsFromDB() {
        tableModel.setRowCount(0);
        try {
            for (Visitor v : visitorService.listAll()) {
                Object[] row = new Object[]{v.getId(), v.getName(), v.getArriveTime() == null ? "" : fmt.format(v.getArriveTime()),
                        v.getLeaveTime().map(fmt::format).orElse(""), v.getVisitRoom(), v.getTargetStudent(), v.getReason(), v.getIdType(), v.getIdNumber(), v.getPhone(), v.getRemarks()};
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载访客记录失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}