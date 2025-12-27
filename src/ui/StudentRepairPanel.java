package ui;

import model.Repair;
import model.Student;
import service.RepairService;
import service.impl.RepairServiceImpl;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 学生端 - 故障报修面板
 * 实现功能：查看个人报修历史、提交新报修申请（已接入数据库）
 */
public class StudentRepairPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private Student student;

    private final RepairService repairService = new RepairServiceImpl();

    public StudentRepairPanel(Student student) {
        this.student = student;
        initUI();
        loadData(); // 初始化加载数据
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- 1. 顶部工具栏 ---
        JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("提交新报修");
        addBtn.setBackground(new Color(70, 130, 180));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFocusPainted(false);

        JButton refreshBtn = new JButton("刷新列表");
        JButton deleteBtn = new JButton("删除报修");
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setBackground(new Color(200, 50, 50));

        // 绑定“提交新报修”按钮事件
        addBtn.addActionListener(e -> showAddRepairDialog());
        refreshBtn.addActionListener(e -> loadData());
        deleteBtn.addActionListener(e -> handleDeleteAction());

        tools.add(addBtn);
        tools.add(refreshBtn);
        tools.add(deleteBtn);
        add(tools, BorderLayout.NORTH);

        // --- 2. 中间表格区域 ---
        String[] cols = {"报修单号", "设备类型", "内容描述", "宿舍地址", "申请时间", "当前状态"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(30);
        table.getTableHeader().setReorderingAllowed(false);

        // 设置状态列居中对齐及颜色渲染
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(JLabel.CENTER);
                String status = (String) value;
                if ("待处理".equals(status)) label.setForeground(Color.RED);
                else if ("处理中".equals(status)) label.setForeground(Color.BLUE);
                else if ("已完成".equals(status)) label.setForeground(new Color(0, 150, 0));
                return label;
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    // 处理删除动作：物理删除选中的报修记录
    private void handleDeleteAction() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选择一条报修记录");
            return;
        }

        String id = model.getValueAt(row, 0).toString();
        if (JOptionPane.showConfirmDialog(this, "确定要永久删除报修单 " + id + " 吗？", "删除确认", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            boolean ok = repairService.deleteById(id);
            if (ok) {
                JOptionPane.showMessageDialog(this, "删除成功");
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "删除失败，请稍后重试或联系管理员", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 弹出提交报修的对话框（提交后写库）
     */
    private void showAddRepairDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "提交报修申请", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 字段：设备类型
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("设备类型:"), gbc);
        gbc.gridx = 1;
        String[] types = {"灯具照明", "水路管网", "木工家具", "锁具门窗", "网络宽带", "其它"};
        JComboBox<String> typeBox = new JComboBox<>(types);
        panel.add(typeBox, gbc);

        // 字段：内容描述
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("情况描述:"), gbc);
        gbc.gridx = 1;
        JTextArea descArea = new JTextArea(5, 20);
        descArea.setLineWrap(true);
        panel.add(new JScrollPane(descArea), gbc);

        // 提交按钮
        JButton submitBtn = new JButton("立即提交");
        submitBtn.addActionListener(e -> {
            String type = (String) typeBox.getSelectedItem();
            String desc = descArea.getText().trim();

            if (desc.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请填写描述信息");
                return;
            }

            // 构造 Repair 对象并调用 Service 写入数据库
            Repair.RepairType rt = mapType(type);
            Repair r = Repair.createNewRepair(
                    student.getSno(),
                    student.getRoomNumber(),
                    student.getBuilding(),
                    rt,
                    desc,
                    null
            );

            boolean ok = repairService.addRepair(r);
            if (ok) {
                String orderId = r.getId();
                String address = student.getBuilding() + student.getRoomNumber();
                String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                model.insertRow(0, new Object[]{orderId, type, desc, address, time, "待处理"});
                JOptionPane.showMessageDialog(dialog, "报修提交成功！工作人员将尽快上门。");
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "提交失败，请稍后重试或联系管理员。", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(submitBtn, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    /**
     * 从数据库加载当前学生的报修记录，并渲染表格
     */
    private void loadData() {
        model.setRowCount(0);
        if (student == null || student.getSno() == null) return;
        List<Repair> list = repairService.listByStudent(student.getSno());
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (Repair r : list) {
            String id = r.getId();
            String type = r.getRepairType() != null ? r.getRepairType().getDescription() : "其它";
            String desc = r.getDescription();
            String addr = (r.getBuilding() == null ? "" : r.getBuilding()) + (r.getRoomNumber() == null ? "" : r.getRoomNumber());
            String time = "";
            try { if (r.getSubmitTime() != null) time = r.getSubmitTime().format(dtf); } catch (Exception ignored) {}
            String status = r.getStatus() != null ? r.getStatus().getDescription() : "未知";
            model.addRow(new Object[]{id, type, desc, addr, time, status});
        }
    }

    // 将 UI 上的类型文本映射为 Repair.RepairType
    private Repair.RepairType mapType(String text) {
        if (text == null) return Repair.RepairType.OTHER;
        switch (text) {
            case "灯具照明": return Repair.RepairType.WATER_ELEC; // 归类为水电
            case "水路管网": return Repair.RepairType.WATER_ELEC;
            case "木工家具": return Repair.RepairType.FURNITURE;
            case "锁具门窗": return Repair.RepairType.FURNITURE;
            case "网络宽带": return Repair.RepairType.NETWORK;
            default: return Repair.RepairType.OTHER;
        }
    }
}