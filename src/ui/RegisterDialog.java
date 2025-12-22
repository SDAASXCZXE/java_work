package ui;

import uimodel.User;
import uimodel.UserType;
import uimodel.UserManager;
import javax.swing.*;
import java.awt.*;

/**
 * 用户注册对话框
 */
public class RegisterDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JComboBox<UserType> userTypeCombo;
    private JTextField studentIdField;
    private JTextField nameField;
    private JTextField phoneField;
    private JTextField emailField;

    private boolean registered = false;

    public RegisterDialog(JFrame parent) {
        super(parent, "用户注册", true);
        initUI();
        setupListeners();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setSize(400, 450);
        setLocationRelativeTo(getParent());
        setResizable(false);

        // 主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setLayout(new GridLayout(9, 2, 10, 10));

        // 用户名
        mainPanel.add(new JLabel("用户名*:"));
        usernameField = new JTextField();
        mainPanel.add(usernameField);

        // 密码
        mainPanel.add(new JLabel("密码*:"));
        passwordField = new JPasswordField();
        mainPanel.add(passwordField);

        // 确认密码
        mainPanel.add(new JLabel("确认密码*:"));
        confirmPasswordField = new JPasswordField();
        mainPanel.add(confirmPasswordField);

        // 用户类型
        mainPanel.add(new JLabel("用户类型*:"));
        userTypeCombo = new JComboBox<>(UserType.values());
        userTypeCombo.addActionListener(e -> updateFormFields());
        mainPanel.add(userTypeCombo);

        // 学号（仅学生）
        mainPanel.add(new JLabel("学号:"));
        studentIdField = new JTextField();
        mainPanel.add(studentIdField);

        // 姓名
        mainPanel.add(new JLabel("姓名:"));
        nameField = new JTextField();
        mainPanel.add(nameField);

        // 电话
        mainPanel.add(new JLabel("电话:"));
        phoneField = new JTextField();
        mainPanel.add(phoneField);

        // 邮箱
        mainPanel.add(new JLabel("邮箱:"));
        emailField = new JTextField();
        mainPanel.add(emailField);

        add(mainPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton registerButton = new JButton("注册");
        registerButton.setBackground(new Color(70, 130, 180));
        registerButton.setForeground(Color.BLACK);
        registerButton.setPreferredSize(new Dimension(100, 30));
        registerButton.addActionListener(e -> register());

        JButton cancelButton = new JButton("取消");
        cancelButton.setPreferredSize(new Dimension(100, 30));
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // 初始更新表单字段
        updateFormFields();
    }

    private void updateFormFields() {
        UserType selectedType = (UserType) userTypeCombo.getSelectedItem();
        boolean isStudent = selectedType == UserType.STUDENT;

        // 更新学号字段标签和状态
        Component studentIdLabel = ((JPanel) getContentPane().getComponent(0)).getComponent(8);
        Component studentIdField = ((JPanel) getContentPane().getComponent(0)).getComponent(9);

        if (studentIdLabel instanceof JLabel) {
            ((JLabel) studentIdLabel).setText(isStudent ? "学号*:" : "学号:");
        }

        if (studentIdField instanceof JTextField) {
            ((JTextField) studentIdField).setEnabled(isStudent);
        }
    }

    private void setupListeners() {
        // 按ESC键关闭对话框
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    /**
     * 注册方法
     */
    private void register() {
        // 获取输入数据
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        UserType userType = (UserType) userTypeCombo.getSelectedItem();
        String studentId = studentIdField.getText().trim();
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();


        // 验证输入
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名、密码和确认密码不能为空！",
                    "输入错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "两次输入的密码不一致！",
                    "输入错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "密码长度不能少于6位！",
                    "输入错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (userType == UserType.STUDENT && studentId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "学生账号必须填写学号！",
                    "输入错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 验证学号格式（学生用户）
        if (userType == UserType.STUDENT && !studentId.matches("\\d{8,10}")) {
            JOptionPane.showMessageDialog(this, "学号必须是8-10位数字！",
                    "输入错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 验证电话格式
        if (!phone.isEmpty() && !phone.matches("1[3-9]\\d{9}")) {
            JOptionPane.showMessageDialog(this, "请输入有效的手机号码！",
                    "输入错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 验证邮箱格式
        if (!email.isEmpty() && !email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            JOptionPane.showMessageDialog(this, "请输入有效的邮箱地址！",
                    "输入错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 注册用户
        UserManager userManager = UserManager.getInstance();

        // 执行注册（学号检查已经在registerUser方法中完成）
        boolean success = userManager.registerUser(username, password, userType,
                studentId, name, phone, email);

        if (success) {
            JOptionPane.showMessageDialog(this,
                    "注册成功！\n用户名: " + username +
                            "\n用户类型: " + userType.getDescription(),
                    "注册成功",
                    JOptionPane.INFORMATION_MESSAGE);

            registered = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "注册失败！用户名或学号可能已存在。",
                    "注册失败",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}