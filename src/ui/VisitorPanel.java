/*
 * 文件：VisitorPanel.java
 * 说明：访客管理面板（学生/管理员均可查看），包含访客登记、列表显示和删除功能。
 * 注意：仅添加注释，不改动业务逻辑代码。
 */

package ui;

import model.Visitor;
import service.VisitorService;
import service.impl.VisitorServiceImpl;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 访客记录管理面板 - 完整功能版
 */
public class VisitorPanel extends JPanel {
    private JTable visitorTable;
    private DefaultTableModel tableModel;
    private VisitorService visitorService = new VisitorServiceImpl();
    private DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // 数据缓存，用于本地快速筛选和统计，确保与数据库同步
    private List<Visitor> allVisitorsCache = new ArrayList<>();

    // 统计标签
    private JLabel lblToday, lblWeek, lblMonth, lblCurrent;

    public VisitorPanel() {
        initUI();
        loadVisitorsFromDB();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(createToolBar(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createStatsPanel(), BorderLayout.SOUTH);
    }

    private JPanel createToolBar() {
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        toolBar.setBorder(BorderFactory.createTitledBorder("访客记录管理"));

        String[] buttons = {"访客登记", "编辑记录", "删除记录", "查找访客", "导出记录", "刷新数据"};
        for (String text : buttons) {
            JButton button = new JButton(text);
            button.setBackground(new Color(70, 130, 180));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.addActionListener(this::handleButtonClick);
            toolBar.add(button);
        }

        toolBar.add(Box.createHorizontalStrut(20));
        toolBar.add(new JLabel("快速筛选:"));
        JComboBox<String> filterCombo = new JComboBox<>(new String[]{"全部", "今日", "本周", "本月"});
        filterCombo.addActionListener(e -> filterRecords((String) filterCombo.getSelectedItem()));
        toolBar.add(filterCombo);

        return toolBar;
    }

    private JPanel createTablePanel() {
        String[] columns = {"ID", "姓名", "来访时间", "离开时间", "宿舍", "被访学生", "事由", "证件类型", "证件号码", "电话", "备注"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        visitorTable = new JTable(tableModel);
        visitorTable.setRowHeight(28);
        visitorTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(visitorTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("访客记录列表"));
        return new JPanel(new BorderLayout()) {{
            add(scrollPane);
        }};
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 8));
        panel.setBorder(BorderFactory.createEtchedBorder());
        panel.setBackground(Color.WHITE);

        lblToday = new JLabel("今日访客: 0人");
        lblWeek = new JLabel("本周访客: 0人");
        lblMonth = new JLabel("本月访客: 0人");
        lblCurrent = new JLabel("当前在楼: 0人");

        Font f = new Font("微软雅黑", Font.BOLD, 12);
        for (JLabel lbl : new JLabel[]{lblToday, lblWeek, lblMonth, lblCurrent}) {
            lbl.setFont(f);
            lbl.setForeground(new Color(50, 50, 50));
            panel.add(lbl);
        }
        return panel;
    }

    private void handleButtonClick(ActionEvent e) {
        String command = e.getActionCommand();
        switch (command) {
            case "访客登记" -> showVisitorDialog(null);
            case "编辑记录" -> editVisitor();
            case "删除记录" -> deleteVisitor();
            case "查找访客" -> searchVisitor();
            case "导出记录" -> exportRecords();
            case "刷新数据" -> loadVisitorsFromDB();
        }
    }

    /**
     * 加载数据库数据：核心是更新本地全量缓存
     */
    private void loadVisitorsFromDB() {
        // 从 service 获取所有 deleted=0 的访客
        allVisitorsCache = visitorService.listAll();
        updateTable(allVisitorsCache);
        updateStatistics();
    }

    /**
     * 逻辑删除：数据库变位 + 界面移除 + 缓存同步
     */
    private void deleteVisitor() {
        int r = visitorTable.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "请选择要删除的记录。");
            return;
        }
        String id = (String) tableModel.getValueAt(r, 0);
        String name = (String) tableModel.getValueAt(r, 1);

        if (JOptionPane.showConfirmDialog(this, "确定删除访客 [" + name + "] 的记录吗？", "确认删除", JOptionPane.YES_NO_OPTION) == 0) {
            // 1. 数据库逻辑删除 (deleted = 1)
            if (visitorService.removeById(id)) {
                // 2. 重新加载数据库以刷新缓存和表格（最稳健的做法）
                loadVisitorsFromDB();
                JOptionPane.showMessageDialog(this, "记录已成功移除。");
            } else {
                JOptionPane.showMessageDialog(this, "操作失败，请检查数据库连接。");
            }
        }
    }

    private void editVisitor() {
        int r = visitorTable.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "请选择要编辑的记录。");
            return;
        }
        String id = tableModel.getValueAt(r, 0).toString();
        Visitor target = allVisitorsCache.stream()
                .filter(v -> v.getId().equals(id))
                .findFirst().orElse(null);

        if (target != null) showVisitorDialog(target);
    }

    private void showVisitorDialog(Visitor existingVisitor) {
        boolean isEdit = existingVisitor != null;
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), isEdit ? "编辑访客" : "访客登记", true);
        dialog.setSize(500, 480);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtName = new JTextField(isEdit ? existingVisitor.getName() : "", 15);
        JComboBox<String> cbIdType = new JComboBox<>(new String[]{"身份证", "护照", "学生证", "其他"});
        if (isEdit) cbIdType.setSelectedItem(existingVisitor.getIdType());

        JTextField txtIdNum = new JTextField(isEdit ? existingVisitor.getIdNumber() : "");
        JTextField txtPhone = new JTextField(isEdit ? existingVisitor.getPhone() : "");
        JTextField txtRoom = new JTextField(isEdit ? existingVisitor.getVisitRoom() : "");
        JTextField txtTarget = new JTextField(isEdit ? existingVisitor.getTargetStudent() : "");
        JTextArea taReason = new JTextArea(isEdit ? existingVisitor.getReason() : "", 3, 20);
        JTextField txtLeave = new JTextField(isEdit ? existingVisitor.getLeaveTime().map(fmt::format).orElse("") : "");

        String[] labels = {"访客姓名*", "证件类型", "证件号码*", "联系电话*", "访问宿舍", "被访学生", "来访事由", "离开时间"};
        JComponent[] comps = {txtName, cbIdType, txtIdNum, txtPhone, txtRoom, txtTarget, new JScrollPane(taReason), txtLeave};

        for (int i = 0; i < labels.length; i++) {
            g.gridx = 0; g.gridy = i; g.weightx = 0; mainPanel.add(new JLabel(labels[i]), g);
            g.gridx = 1; g.weightx = 1.0; mainPanel.add(comps[i], g);
        }

        JButton btnSave = new JButton("保存");
        btnSave.addActionListener(ev -> {
            Visitor v = isEdit ? existingVisitor : new Visitor();
            if (!isEdit) {
                v.setId(Visitor.generateId());
                v.setArriveTime(LocalDateTime.now());
            }
            v.setName(txtName.getText().trim());
            v.setIdType((String) cbIdType.getSelectedItem());
            v.setIdNumber(txtIdNum.getText().trim());
            v.setPhone(txtPhone.getText().trim());
            v.setVisitRoom(txtRoom.getText().trim());
            v.setTargetStudent(txtTarget.getText().trim());
            v.setReason(taReason.getText().trim());

            String leaveStr = txtLeave.getText().trim();
            if (!leaveStr.isEmpty()) {
                try { v.setLeaveTime(LocalDateTime.parse(leaveStr, fmt)); } catch (Exception ex) {}
            }

            boolean ok = isEdit ? visitorService.updateVisitor(v) : visitorService.addVisitor(v);
            if (ok) {
                loadVisitorsFromDB();
                dialog.dispose();
            }
        });

        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.add(btnSave, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void searchVisitor() {
        String kw = JOptionPane.showInputDialog(this, "请输入访客姓名或证件号码:");
        if (kw == null || kw.isBlank()) return;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String name = tableModel.getValueAt(i, 1).toString();
            String idNum = tableModel.getValueAt(i, 8).toString();
            if (name.contains(kw) || idNum.contains(kw)) {
                visitorTable.setRowSelectionInterval(i, i);
                visitorTable.scrollRectToVisible(visitorTable.getCellRect(i, 0, true));
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "未找到匹配的访客记录。");
    }

    private void filterRecords(String f) {
        if (allVisitorsCache == null) return;
        LocalDate today = LocalDate.now();
        List<Visitor> filtered = allVisitorsCache.stream().filter(v -> {
            if ("全部".equals(f)) return true;
            LocalDate arrive = v.getArriveTime().toLocalDate();
            return switch (f) {
                case "今日" -> arrive.isEqual(today);
                case "本周" -> arrive.isAfter(today.minusWeeks(1));
                case "本月" -> arrive.isAfter(today.minusMonths(1));
                default -> true;
            };
        }).collect(Collectors.toList());
        updateTable(filtered);
    }

    private void updateTable(List<Visitor> list) {
        tableModel.setRowCount(0);
        for (Visitor v : list) {
            tableModel.addRow(new Object[]{
                    v.getId(), v.getName(), fmt.format(v.getArriveTime()),
                    v.getLeaveTime().map(fmt::format).orElse("在楼中"),
                    v.getVisitRoom(), v.getTargetStudent(), v.getReason(),
                    v.getIdType(), v.getIdNumber(), v.getPhone(), v.getRemarks()
            });
        }
    }

    private void updateStatistics() {
        if (allVisitorsCache == null) return;
        LocalDate today = LocalDate.now();
        long t = allVisitorsCache.stream().filter(v -> v.getArriveTime().toLocalDate().isEqual(today)).count();
        long w = allVisitorsCache.stream().filter(v -> v.getArriveTime().toLocalDate().isAfter(today.minusWeeks(1))).count();
        long m = allVisitorsCache.stream().filter(v -> v.getArriveTime().toLocalDate().isAfter(today.minusMonths(1))).count();
        long c = allVisitorsCache.stream().filter(v -> v.getLeaveTime().isEmpty()).count();

        lblToday.setText("今日访客: " + t + "人");
        lblWeek.setText("本周访客: " + w + "人");
        lblMonth.setText("本月访客: " + m + "人");
        lblCurrent.setText("当前在楼: " + c + "人");
    }

    private void exportRecords() {
        JOptionPane.showMessageDialog(this, "导出 Excel 功能正在集成...");
    }
}

