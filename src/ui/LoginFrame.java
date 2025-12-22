package ui;

import uimodel.User;
import uimodel.UserType;
import uimodel.UserManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Base64;
import java.util.Properties;

/**
 * 系统登录界面 - 移除未使用的方法
 */
public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<UserType> userTypeCombo;
    private JCheckBox rememberCheckBox;
    private User currentUser;

    private JLabel dateLabel;
    private static final String CONFIG_FILE = "login_config.properties";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER = "remember";
    private static final String KEY_USERTYPE = "usertype";

    // 使用简单的加密密钥
    private static final String ENCRYPTION_KEY = "DormSystemKey2024!";

    public LoginFrame() {
        initUI();
        setupListeners();
        loadRememberedUser();
        updateDate();
    }

    private void initUI() {
        setTitle("学生宿舍管理系统 - 登录");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        // 设置窗口图标
        setIconImage(createIcon());

        // 使用BorderLayout作为主布局
        setLayout(new BorderLayout());

        // =========== 顶部标题区域 ===========
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setPreferredSize(new Dimension(500, 80));
        headerPanel.setLayout(new BorderLayout());

        // 左侧标题
        JLabel titleLabel = new JLabel("  学生宿舍管理系统");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // 右侧日期
        dateLabel = new JLabel();
        dateLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        dateLabel.setForeground(Color.WHITE);
        headerPanel.add(dateLabel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // =========== 中间表单区域 ===========
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridBagLayout());
        centerPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 用户名标签
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel userLabel = new JLabel("用户名:");
        userLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        centerPanel.add(userLabel, gbc);

        // 用户名输入框
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        usernameField = new JTextField(15);
        usernameField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        centerPanel.add(usernameField, gbc);

        // 密码标签
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        JLabel passLabel = new JLabel("密  码:");
        passLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        centerPanel.add(passLabel, gbc);

        // 密码输入框
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        centerPanel.add(passwordField, gbc);

        // 用户类型标签
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        JLabel typeLabel = new JLabel("用户类型:");
        typeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        centerPanel.add(typeLabel, gbc);

        // 用户类型下拉框
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        userTypeCombo = new JComboBox<>(UserType.values());
        userTypeCombo.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        centerPanel.add(userTypeCombo, gbc);

        // 记住密码复选框（跨越两列）
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        rememberCheckBox = new JCheckBox("记住密码");
        rememberCheckBox.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        rememberCheckBox.setBackground(Color.WHITE);
        centerPanel.add(rememberCheckBox, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // =========== 底部按钮区域 ===========
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 15));
        buttonPanel.setBackground(Color.WHITE);

        // 登录按钮
        JButton loginButton = new JButton("登录");
        loginButton.setPreferredSize(new Dimension(100, 35));
        loginButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        loginButton.setBackground(new Color(70, 130, 180));
        loginButton.setForeground(Color.BLACK);
        loginButton.setFocusPainted(false);
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });

        // 注册按钮
        JButton registerButton = new JButton("注册");
        registerButton.setPreferredSize(new Dimension(100, 35));
        registerButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        registerButton.setBackground(new Color(46, 139, 87));
        registerButton.setForeground(Color.BLACK);
        registerButton.setFocusPainted(false);
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRegisterDialog();
            }
        });

        // 重置按钮
        JButton resetButton = new JButton("重置");
        resetButton.setPreferredSize(new Dimension(100, 35));
        resetButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        resetButton.setBackground(new Color(205, 92, 92));
        resetButton.setForeground(Color.BLACK);
        resetButton.setFocusPainted(false);
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetForm();
            }
        });

        // 退出按钮
        JButton exitButton = new JButton("退出");
        exitButton.setPreferredSize(new Dimension(100, 35));
        exitButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        exitButton.setBackground(new Color(105, 105, 105));
        exitButton.setForeground(Color.BLACK);
        exitButton.setFocusPainted(false);
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmExit();
            }
        });

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(exitButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // 添加键盘快捷键
        setupKeyboardShortcuts();
    }

    private Image createIcon() {
        // 创建系统图标
        java.awt.image.BufferedImage icon = new java.awt.image.BufferedImage(64, 64,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = icon.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制蓝色背景
        g2d.setColor(new Color(70, 130, 180));
        g2d.fillRoundRect(0, 0, 64, 64, 15, 15);

        // 绘制宿舍图标
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3));

        // 绘制房子形状
        int[] xPoints = {20, 32, 44};
        int[] yPoints = {30, 15, 30};
        g2d.drawPolygon(xPoints, yPoints, 3); // 屋顶

        g2d.drawRect(25, 30, 15, 20); // 房子主体
        g2d.drawRect(28, 40, 4, 10); // 门

        // 绘制窗户
        g2d.drawOval(34, 35, 5, 5);

        g2d.dispose();
        return icon;
    }

    private void updateDate() {
        // 更新日期显示
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy年MM月dd日 EEEE");
        String currentDate = dateFormat.format(new java.util.Date());
        dateLabel.setText(currentDate);
    }

    private void setupListeners() {
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowActivated(java.awt.event.WindowEvent e) {
                updateDate();
            }

            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                confirmExit();
            }
        });
    }

    private void setupKeyboardShortcuts() {
        // Enter键登录
        getRootPane().registerKeyboardAction(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        login();
                    }
                },
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // ESC键重置
        getRootPane().registerKeyboardAction(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        resetForm();
                    }
                },
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Ctrl+R 注册
        getRootPane().registerKeyboardAction(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        showRegisterDialog();
                    }
                },
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_DOWN_MASK),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    /**
     * 加载记住的用户信息
     */
    private void loadRememberedUser() {
        Properties props = new Properties();
        File configFile = new File(CONFIG_FILE);

        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);

                String savedUsername = props.getProperty(KEY_USERNAME, "");
                String savedPassword = props.getProperty(KEY_PASSWORD, "");
                String savedRemember = props.getProperty(KEY_REMEMBER, "false");
                String savedUserType = props.getProperty(KEY_USERTYPE, "STUDENT");

                if (!savedUsername.isEmpty() && "true".equals(savedRemember)) {
                    // 解密密码
                    String decryptedPassword = decryptPassword(savedPassword);

                    // 设置用户名
                    usernameField.setText(savedUsername);

                    // 设置密码
                    if (!decryptedPassword.isEmpty()) {
                        passwordField.setText(decryptedPassword);
                    }

                    // 设置用户类型
                    try {
                        UserType type = UserType.valueOf(savedUserType);
                        userTypeCombo.setSelectedItem(type);
                    } catch (IllegalArgumentException e) {
                        userTypeCombo.setSelectedIndex(0);
                    }

                    // 选中记住密码复选框
                    rememberCheckBox.setSelected(true);

                    // 焦点移到密码字段
                    passwordField.requestFocus();

                    System.out.println("已加载保存的登录信息");
                }
            } catch (Exception e) {
                System.err.println("加载登录配置失败: " + e.getMessage());
                // 如果配置文件损坏，删除它
                configFile.delete();
            }
        }
    }

    /**
     * 保存记住的用户信息
     */
    private void saveRememberedUser() {
        Properties props = new Properties();
        File configFile = new File(CONFIG_FILE);

        if (rememberCheckBox.isSelected()) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            UserType userType = (UserType) userTypeCombo.getSelectedItem();

            if (!username.isEmpty() && !password.isEmpty()) {
                // 加密密码
                String encryptedPassword = encryptPassword(password);

                props.setProperty(KEY_USERNAME, username);
                props.setProperty(KEY_PASSWORD, encryptedPassword);
                props.setProperty(KEY_USERTYPE, userType.name());
                props.setProperty(KEY_REMEMBER, "true");

                // 添加注释
                props.setProperty("comment", "学生宿舍管理系统 - 登录配置");

                try {
                    // 确保目录存在
                    File parentDir = configFile.getParentFile();
                    if (parentDir != null && !parentDir.exists()) {
                        parentDir.mkdirs();
                    }

                    try (FileOutputStream fos = new FileOutputStream(configFile)) {
                        props.store(fos, "Login Configuration");
                        System.out.println("登录信息已保存");
                    }
                } catch (Exception e) {
                    System.err.println("保存登录配置失败: " + e.getMessage());
                }
            }
        } else {
            // 如果不记住密码，删除配置文件
            if (configFile.exists()) {
                boolean deleted = configFile.delete();
                if (deleted) {
                    System.out.println("已清除保存的登录信息");
                }
            }
        }
    }

    /**
     * 简单的密码加密（Base64 + 简单混淆）
     */
    private String encryptPassword(String password) {
        try {
            // 添加混淆字符串
            String mixed = password + ENCRYPTION_KEY;

            // Base64编码
            String encoded = Base64.getEncoder().encodeToString(mixed.getBytes("UTF-8"));

            // 简单反转
            return new StringBuilder(encoded).reverse().toString();
        } catch (Exception e) {
            System.err.println("密码加密失败: " + e.getMessage());
            return "";
        }
    }

    /**
     * 密码解密
     */
    private String decryptPassword(String encrypted) {
        try {
            if (encrypted == null || encrypted.isEmpty()) {
                return "";
            }

            // 反转回来
            String reversed = new StringBuilder(encrypted).reverse().toString();

            // Base64解码
            byte[] decodedBytes = Base64.getDecoder().decode(reversed);
            String decoded = new String(decodedBytes, "UTF-8");

            // 移除混淆字符串
            if (decoded.endsWith(ENCRYPTION_KEY)) {
                return decoded.substring(0, decoded.length() - ENCRYPTION_KEY.length());
            }

            return "";
        } catch (Exception e) {
            System.err.println("密码解密失败: " + e.getMessage());
            return "";
        }
    }

    /**
     * 登录方法
     */
    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        UserType userType = (UserType) userTypeCombo.getSelectedItem();

        // 验证输入
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名和密码不能为空！",
                    "输入错误", JOptionPane.ERROR_MESSAGE);
            usernameField.requestFocus();
            return;
        }

        // 直接验证
        UserManager userManager = UserManager.getInstance();
        User user = userManager.login(username, password, userType);

        if (user != null) {
            currentUser = user;

            // 保存记住密码设置
            saveRememberedUser();

            JOptionPane.showMessageDialog(this,
                    String.format("登录成功！\n欢迎您，%s！",
                            user.getName() != null ? user.getName() : user.getUsername()),
                    "登录成功",
                    JOptionPane.INFORMATION_MESSAGE);

            // 打开主系统
            openMainSystem();

        } else {
            JOptionPane.showMessageDialog(this,
                    "登录失败！用户名、密码或用户类型错误。",
                    "登录失败",
                    JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
            passwordField.requestFocus();
        }
    }

    /**
     * 显示注册对话框
     */
    private void showRegisterDialog() {
        RegisterDialog registerDialog = new RegisterDialog(this);
        registerDialog.setVisible(true);
    }

    /**
     * 重置表单
     */
    private void resetForm() {
        usernameField.setText("");
        passwordField.setText("");
        userTypeCombo.setSelectedIndex(0);
        rememberCheckBox.setSelected(false);
        usernameField.requestFocus();

        // 清除保存的登录信息
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            configFile.delete();
        }
    }

    /**
     * 确认退出
     */
    private void confirmExit() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要退出学生宿舍管理系统吗？",
                "确认退出",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // 清除内存中的密码
            passwordField.setText("");
            dispose();
            System.exit(0);
        }
    }

    /**
     * 打开主系统
     */
    private void openMainSystem() {
        // 在主线程中创建和显示主界面
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    MainFrame mainFrame = new MainFrame(currentUser);

                    // 根据用户类型设置标题
                    if (currentUser != null) {
                        String userTypeStr = currentUser.getUserType().getDescription();
                        String userName = currentUser.getName() != null ?
                                currentUser.getName() : currentUser.getUsername();
                        mainFrame.setTitle("学生宿舍管理系统 - " + userTypeStr + "(" + userName + ")");
                    }

                    // 显示主窗口
                    mainFrame.setVisible(true);

                    // 关闭登录窗口
                    dispose();

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(LoginFrame.this,
                            "启动主系统失败: " + e.getMessage(),
                            "系统错误",
                            JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        // 设置系统外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 启动登录界面
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            }
        });
    }
}