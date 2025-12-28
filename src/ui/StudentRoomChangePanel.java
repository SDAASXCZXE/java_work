package ui;

import model.RoomChange;
import model.Student;
import service.RoomChangeService;
import service.impl.RoomChangeServiceImpl;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 学生端 - 退宿/换宿申请面板
 */
public class StudentRoomChangePanel extends JPanel {
    private Student student;
    private JTable table;
    private DefaultTableModel model;
    private final RoomChangeService rcService = new RoomChangeServiceImpl();

    public StudentRoomChangePanel(Student student) {
        this.student = student;
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("提交退宿/换宿申请");
        addBtn.setBackground(new Color(70,130,180));
        addBtn.setForeground(Color.WHITE);
        JButton refreshBtn = new JButton("刷新列表");
        JButton deleteBtn = new JButton("删除申请");
        deleteBtn.setBackground(new Color(200,50,50));
        deleteBtn.setForeground(Color.WHITE);

        addBtn.addActionListener(e -> showAddDialog());
        refreshBtn.addActionListener(e -> loadData());
        deleteBtn.addActionListener(e -> handleDelete());

        tools.add(addBtn);
        tools.add(refreshBtn);
        tools.add(deleteBtn);
        add(tools, BorderLayout.NORTH);

        String[] cols = {"ID","旧宿舍","新宿舍","原因","申请时间","状态"};
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
                if ("pending".equalsIgnoreCase(s) || "待审批".equals(s)) l.setForeground(Color.RED);
                else if ("approved".equalsIgnoreCase(s) || "已批准".equals(s)) l.setForeground(Color.BLUE);
                else if ("executed".equalsIgnoreCase(s) || "已执行".equals(s)) l.setForeground(new Color(0,150,0));
                return l;
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void showAddDialog(){
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "提交退宿/换宿申请", true);
        d.setSize(520,300);
        d.setLocationRelativeTo(this);
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx=0; gbc.gridy=0; p.add(new JLabel("当前楼栋/宿舍:"),gbc);
        gbc.gridx=1; p.add(new JLabel(student.getBuilding() + " / " + student.getRoomNumber()),gbc);

        gbc.gridx=0; gbc.gridy=1; p.add(new JLabel("目标楼栋:"),gbc);
        gbc.gridx=1; JTextField newBuildingField = new JTextField(20); p.add(newBuildingField,gbc);

        gbc.gridx=0; gbc.gridy=2; p.add(new JLabel("目标宿舍号:"),gbc);
        gbc.gridx=1; JTextField newRoomField = new JTextField(20); p.add(newRoomField,gbc);

        gbc.gridx=0; gbc.gridy=3; p.add(new JLabel("申请原因:"),gbc);
        gbc.gridx=1; JTextField reasonField = new JTextField(30); p.add(reasonField,gbc);

        JButton submit = new JButton("提交申请");
        submit.addActionListener(e -> {
            String nb = newBuildingField.getText().trim();
            String nr = newRoomField.getText().trim();
            String reason = reasonField.getText().trim();
            if (nb.isEmpty() || nr.isEmpty() || reason.isEmpty()) { JOptionPane.showMessageDialog(d, "请填写目标楼栋、宿舍号和申请原因"); return; }

            RoomChange rc = RoomChange.createNewApplication(student.getSno(), student.getBuilding(), student.getRoomNumber(), nb, nr, reason);
            boolean ok = rcService.addRoomChange(rc);
            if (ok) { JOptionPane.showMessageDialog(d, "申请提交成功"); d.dispose(); loadData(); } else { JOptionPane.showMessageDialog(d, "提交失败","错误", JOptionPane.ERROR_MESSAGE); }
        });

        gbc.gridx=0; gbc.gridy=4; gbc.gridwidth=2; p.add(submit,gbc);
        d.add(p);
        d.setVisible(true);
    }

    private void loadData(){
        model.setRowCount(0);
        if (student==null || student.getSno()==null) return;
        List<RoomChange> list = rcService.listByStudent(student.getSno());
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (RoomChange rc : list){
            String id = rc.getId();
            String oldDorm = rc.getOldBuilding() + " / " + rc.getOldRoomNumber();
            String newDorm = rc.getNewBuilding() + " / " + rc.getNewRoomNumber();
            String time = ""; try { if (rc.getApplyTime() != null) time = rc.getApplyTime().format(dtf);} catch (Exception ignored) {}
            String status = rc.getStatus() != null ? rc.getStatus().getDescription() : "pending";
            model.addRow(new Object[]{id, oldDorm, newDorm, rc.getReason(), time, status});
        }
    }

    private void handleDelete(){
        int row = table.getSelectedRow(); if (row==-1){ JOptionPane.showMessageDialog(this,"请选择一条记录"); return; }
        String id = model.getValueAt(row,0).toString();
        if (JOptionPane.showConfirmDialog(this,"确认永久删除申请 " + id + " ?","删除确认",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_NO_OPTION){
            boolean ok = rcService.deleteById(id);
            if (ok){ JOptionPane.showMessageDialog(this,"删除成功"); loadData(); } else { JOptionPane.showMessageDialog(this,"删除失败","错误",JOptionPane.ERROR_MESSAGE); }
        }
    }

    // 外部调用：更新 student 对象并刷新数据
    public void updateStudent(Student updated) {
        if (updated == null) return;
        this.student = updated;
        loadData();
    }
}
