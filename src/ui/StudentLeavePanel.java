/*
 * 文件：StudentLeavePanel.java
 * 说明：学生端请假申请面板（病假/事假），包含提交、查看和删除操作。
 * 注意：仅添加注释，不修改业务代码。
 */

package ui;

import model.LeaveRequest;
import model.Student;
import service.LeaveService;
import service.impl.LeaveServiceImpl;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Calendar;

/**
 * 学生端 - 请假申请面板（病事假）
 */
public class StudentLeavePanel extends JPanel {
    private Student student;
    private JTable table;
    private DefaultTableModel model;
    private final LeaveService leaveService = new LeaveServiceImpl();

    public StudentLeavePanel(Student student) {
        this.student = student;
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("提交请假申请");
        addBtn.setBackground(new Color(70,130,180));
        addBtn.setForeground(Color.WHITE);
        JButton refreshBtn = new JButton("刷新列表");
        JButton deleteBtn = new JButton("删除请假记录");
        deleteBtn.setBackground(new Color(200,50,50));
        deleteBtn.setForeground(Color.WHITE);

        addBtn.addActionListener(e -> showAddLeaveDialog());
        refreshBtn.addActionListener(e -> loadData());
        deleteBtn.addActionListener(e -> handleDelete());

        tools.add(addBtn);
        tools.add(refreshBtn);
        tools.add(deleteBtn);
        add(tools, BorderLayout.NORTH);

        String[] cols = {"ID","类型","起始日期","截止日期","提交时间","状态"};
        model = new DefaultTableModel(cols,0) {
            @Override public boolean isCellEditable(int row,int column){return false;}
        };
        table = new JTable(model);
        table.setRowHeight(28);
        table.getTableHeader().setReorderingAllowed(false);

        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int column){
                JLabel l = (JLabel) super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
                l.setHorizontalAlignment(JLabel.CENTER);
                String s = value == null ? "" : value.toString();
                if ("pending".equalsIgnoreCase(s) || "待审核".equals(s)) l.setForeground(Color.RED);
                else if ("approved".equalsIgnoreCase(s) || "已批准".equals(s)) l.setForeground(Color.BLUE);
                else if ("completed".equalsIgnoreCase(s) || "已完成".equals(s)) l.setForeground(new Color(0,150,0));
                return l;
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void showAddLeaveDialog(){
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "提交请假申请", true);
        d.setSize(480,420);
        d.setLocationRelativeTo(this);
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx=0; gbc.gridy=0; p.add(new JLabel("请选择类型:"),gbc);
        gbc.gridx=1; String[] types = {"病假","事假"}; JComboBox<String> typeBox = new JComboBox<>(types); p.add(typeBox,gbc);

        // 使用 JSpinner 支持日期+时间选择（格式 yyyy-MM-dd HH:mm）
        gbc.gridx=0; gbc.gridy=1; p.add(new JLabel("起始日期/时间:"),gbc);
        gbc.gridx=1;
        SpinnerDateModel startModel = new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE);
        JSpinner startSpinner = new JSpinner(startModel);
        JSpinner.DateEditor startEditor = new JSpinner.DateEditor(startSpinner, "yyyy-MM-dd HH:mm");
        startSpinner.setEditor(startEditor);
        p.add(startSpinner,gbc);

        gbc.gridx=0; gbc.gridy=2; p.add(new JLabel("截止日期/时间:"),gbc);
        gbc.gridx=1;
        SpinnerDateModel endModel = new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE);
        JSpinner endSpinner = new JSpinner(endModel);
        JSpinner.DateEditor endEditor = new JSpinner.DateEditor(endSpinner, "yyyy-MM-dd HH:mm");
        endSpinner.setEditor(endEditor);
        p.add(endSpinner,gbc);

        gbc.gridx=0; gbc.gridy=3; p.add(new JLabel("事由:"),gbc);
        gbc.gridx=1; JTextField reasonField = new JTextField(20); p.add(reasonField,gbc);

        gbc.gridx=0; gbc.gridy=4; p.add(new JLabel("联系人:"),gbc);
        gbc.gridx=1; JTextField contactField = new JTextField(20); p.add(contactField,gbc);

        gbc.gridx=0; gbc.gridy=5; p.add(new JLabel("联系电话:"),gbc);
        gbc.gridx=1; JTextField phoneField = new JTextField(20); p.add(phoneField,gbc);

        JButton submit = new JButton("提交");
        submit.addActionListener(e -> {
            String type = (String) typeBox.getSelectedItem();
            Date startDateObj = (Date) startSpinner.getValue();
            Date endDateObj = (Date) endSpinner.getValue();
            if (startDateObj == null || endDateObj == null) { JOptionPane.showMessageDialog(d,"起始/结束日期不能为空"); return; }

            LocalDate start = startDateObj.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate end = endDateObj.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            if (end.isBefore(start)) { JOptionPane.showMessageDialog(d,"截止日期不能早于起始日期"); return; }

            // 构建 LeaveRequest 对象
            LeaveRequest lr = new LeaveRequest(LeaveRequest.generateId(), student.getSno(), student.getRoomNumber(), student.getBuilding(), type, start, end);
            lr.setReason(reasonField.getText().trim());
            lr.setContactPerson(contactField.getText().trim());
            lr.setContactPhone(phoneField.getText().trim());

            boolean ok = leaveService.addLeave(lr);
            if (ok) {
                JOptionPane.showMessageDialog(d,"请假申请提交成功");
                d.dispose();
                loadData();
            } else {
                JOptionPane.showMessageDialog(d,"提交失败，请稍后重试","错误",JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx=0; gbc.gridy=6; gbc.gridwidth=2; p.add(submit,gbc);
        d.add(p);
        d.setVisible(true);
    }

    private void loadData(){
        model.setRowCount(0);
        if (student==null || student.getSno()==null) return;
        List<LeaveRequest> list = leaveService.listByStudent(student.getSno());
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (LeaveRequest l : list){
            String id = l.getId();
            String type = l.getLeaveType() != null ? l.getLeaveType() : "";
            String start = l.getStartDate() != null ? l.getStartDate().toString() : "";
            String end = l.getEndDate() != null ? l.getEndDate().toString() : "";
            String time = ""; try{ if (l.getApplyTime()!=null) time = l.getApplyTime().format(dtf);}catch(Exception ignored){}
            String status = l.getStatus() != null ? l.getStatus() : "pending";
            model.addRow(new Object[]{id,type,start,end,time,status});
        }
    }

    private void handleDelete(){
        int row = table.getSelectedRow(); if (row==-1){ JOptionPane.showMessageDialog(this,"请选择一条记录"); return; }
        String id = model.getValueAt(row,0).toString();
        if (JOptionPane.showConfirmDialog(this,"确认永久删除请假记录 " + id + " ?","删除确认",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){
            boolean ok = leaveService.deleteById(id);
            if (ok){ JOptionPane.showMessageDialog(this,"删除成功"); loadData(); } else { JOptionPane.showMessageDialog(this,"删除失败","错误",JOptionPane.ERROR_MESSAGE); }
        }
    }
}

