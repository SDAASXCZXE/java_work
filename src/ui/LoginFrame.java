package ui;

import uimodel.User;
import uimodel.UserType;
import uimodel.UserManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 系统登录界面 - 根据图片描述重新设计
 */
public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<UserType> userTypeCombo;
    private JCheckBox rememberCheckBox;
    private User currentUser;

    // 新增：日期显示
    private JLabel dateLabel;

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

        // 主面板使用BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 245, 245));

        // =========== 顶部标题区域 ===========
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        headerPanel.setPreferredSize(new Dimension(500, 80));

        // 左侧：系统标题
        JLabel titleLabel = new JLabel("学生宿舍管理系统");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // 右侧：日期显示
        dateLabel = new JLabel();
        dateLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        dateLabel.setForeground(Color.WHITE);
        headerPanel.add(dateLabel, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // =========== 中间登录表单区域 ===========
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(40, 60, 30, 60));
        centerPanel.setBackground(new Color(245, 245, 245));

        // 表单容器
        JPanel formContainer = new JPanel();
        formContainer.setLayout(new GridLayout(5, 2, 15, 15));
        formContainer.setBackground(new Color(245, 245, 245));

        // 用户名行
        JLabel userLabel = new JLabel("用户名:");
        userLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        userLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        usernameField = new JTextField();
        usernameField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        usernameField.setPreferredSize(new Dimension(200, 35));

        // 密码行
        JLabel passLabel = new JLabel("密码:");
        passLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        passLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(200, 35));

        // 用户类型行
        JLabel typeLabel = new JLabel("用户类型:");
        typeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        typeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        userTypeCombo = new JComboBox<>(UserType.values());
        userTypeCombo.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        userTypeCombo.setPreferredSize(new Dimension(200, 35));

        // 记住密码行（放在第4行，第2列）
        rememberCheckBox = new JCheckBox("记住密码");
        rememberCheckBox.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        rememberCheckBox.setBackground(new Color(245, 245, 245));

        // 占位符（为了对齐）
        JLabel placeholder = new JLabel("");

        // 添加组件到表单
        formContainer.add(userLabel);
        formContainer.add(usernameField);
        formContainer.add(passLabel);
        formContainer.add(passwordField);
        formContainer.add(typeLabel);
        formContainer.add(userTypeCombo);
        formContainer.add(placeholder); // 空标签占位
        formContainer.add(rememberCheckBox);

        centerPanel.add(formContainer);
        centerPanel.add(Box.createVerticalStrut(20));

        // =========== 底部按钮区域 ===========
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(245, 245, 245));

        // 登录按钮
        JButton loginButton = new JButton("登录");
        loginButton.setBackground(new Color(70, 130, 180));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        loginButton.setFocusPainted(false);
        loginButton.setPreferredSize(new Dimension(120, 40));
        loginButton.addActionListener(e -> login());

        // 注册按钮
        JButton registerButton = new JButton("注册");
        registerButton.setBackground(new Color(46, 139, 87));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        registerButton.setFocusPainted(false);
        registerButton.setPreferredSize(new Dimension(120, 40));
        registerButton.addActionListener(e -> showRegisterDialog());

        // 重置按钮
        JButton resetButton = new JButton("重置");
        resetButton.setBackground(new Color(205, 92, 92));
        resetButton.setForeground(Color.WHITE);
        resetButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        resetButton.setFocusPainted(false);
        resetButton.setPreferredSize(new Dimension(120, 40));
        resetButton.addActionListener(e -> resetForm());

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        buttonPanel.add(resetButton);

        centerPanel.add(buttonPanel);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // =========== 底部信息区域 ===========
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(new Color(240, 240, 240));
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        // 左侧：版本信息
        JLabel versionLabel = new JLabel("版本: V2.0 | 学生宿舍管理系统");
        versionLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        versionLabel.setForeground(Color.GRAY);
        footerPanel.add(versionLabel, BorderLayout.WEST);

        // 右侧：退出按钮
        JButton exitButton = new JButton("退出系统");
        exitButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        exitButton.setFocusPainted(false);
        exitButton.addActionListener(e -> exitApplication());
        footerPanel.add(exitButton, BorderLayout.EAST);

        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);

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
        // 添加窗口监听器
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowActivated(java.awt.event.WindowEvent e) {
                updateDate();
            }
        });
    }

    private void setupKeyboardShortcuts() {
        // Enter键登录
        getRootPane().registerKeyboardAction(
                e -> login(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // ESC键重置
        getRootPane().registerKeyboardAction(
                e -> resetForm(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Ctrl+R 注册
        getRootPane().registerKeyboardAction(
                e -> showRegisterDialog(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_DOWN_MASK),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private void loadRememberedUser() {
        // 模拟加载记住的用户
        // 实际开发中可以读取配置文件
        try {
            // 检查是否有保存的用户信息
            java.util.Properties props = new java.util.Properties();
            java.io.File configFile = new java.io.File("login_config.properties");

            if (configFile.exists()) {
                props.load(new java.io.FileInputStream(configFile));
                String savedUser = props.getProperty("username", "");
                String savedType = props.getProperty("usertype", "STUDENT");

                if (!savedUser.isEmpty()) {
                    usernameField.setText(savedUser);
                    rememberCheckBox.setSelected(true);

                    // 设置用户类型
                    try {
                        UserType type = UserType.valueOf(savedType);
                        userTypeCombo.setSelectedItem(type);
                    } catch (IllegalArgumentException e) {
                        userTypeCombo.setSelectedIndex(0);
                    }

                    passwordField.requestFocus();
                }
            }
        } catch (Exception e) {
            // 忽略错误，使用默认值
        }
    }

    private void saveRememberedUser() {
        if (rememberCheckBox.isSelected()) {
            try {
                java.util.Properties props = new java.util.Properties();
                props.setProperty("username", usernameField.getText().trim());
                props.setProperty("usertype", ((UserType)userTypeCombo.getSelectedItem()).name());

                props.store(new java.io.FileOutputStream("login_config.properties"),
                        "Login Configuration");
            } catch (Exception e) {
                System.err.println("保存登录配置失败: " + e.getMessage());
            }
        } else {
            // 清除保存的配置
            java.io.File configFile = new java.io.File("login_config.properties");
            if (configFile.exists()) {
                configFile.delete();
            }
        }
    }

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

        // 创建简单的加载提示
        final JOptionPane optionPane = new JOptionPane("正在验证登录信息...",
                JOptionPane.INFORMATION_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                null,
                new Object[]{},
                null);

        final JDialog dialog = optionPane.createDialog(this, "请稍候");
        dialog.setModal(true);

        // 在后台线程进行验证
        new Thread(() -> {
            try {
                Thread.sleep(800); // 模拟验证时间

                UserManager userManager = UserManager.getInstance();
                final User user = userManager.login(username, password, userType);

                SwingUtilities.invokeLater(() -> {
                    dialog.dispose();

                    if (user != null) {
                        currentUser = user;
                        saveRememberedUser();

                        JOptionPane.showMessageDialog(this,
                                String.format("登录成功！\n欢迎您，%s！",
                                        user.getName() != null ? user.getName() : user.getUsername()),
                                "登录成功",
                                JOptionPane.INFORMATION_MESSAGE);

                        openMainSystem();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "登录失败！用户名、密码或用户类型错误。",
                                "登录失败",
                                JOptionPane.ERROR_MESSAGE);
                        passwordField.setText("");
                        passwordField.requestFocus();
                    }
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    dialog.dispose();
                    JOptionPane.showMessageDialog(this,
                            "登录过程出现错误: " + e.getMessage(),
                            "系统错误",
                            JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();

        dialog.setVisible(true);
    }

    private JDialog createLoadingDialog(String message) {
        JDialog dialog = new JDialog(this, "请稍候", true);
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        panel.add(messageLabel, BorderLayout.CENTER);
        panel.add(progressBar, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.pack();

        // 设置dialog关闭时不退出程序
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        return dialog;
    }

    private void openMainSystem() {
        // 直接在主线程中创建和显示主界面
        try {
            // 创建主系统界面
            MainFrame mainFrame = new MainFrame();

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
            JOptionPane.showMessageDialog(this,
                    "启动主系统失败: " + e.getMessage(),
                    "系统错误",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message,
                "错误", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccessDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message,
                title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void showRegisterDialog() {
        RegisterDialog registerDialog = new RegisterDialog(this);
        registerDialog.setVisible(true);

        if (registerDialog.isRegistered()) {
            // 注册成功后，可以自动填充用户名
            // 这里可以添加相关逻辑
        }
    }

    private void resetForm() {
        usernameField.setText("");
        passwordField.setText("");
        userTypeCombo.setSelectedIndex(0);
        rememberCheckBox.setSelected(false);
        usernameField.requestFocus();
    }

    private void exitApplication() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要退出学生宿舍管理系统吗？",
                "确认退出",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }


    public User getCurrentUser() {
        return currentUser;
    }

    public static void main(String[] args) {
        // 设置系统外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // 设置全局字体
            Font font = new Font("微软雅黑", Font.PLAIN, 14);
            java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = UIManager.get(key);
                if (value instanceof Font) {
                    UIManager.put(key, font);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 启动登录界面
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}