package ui;

import uimodel.User;
import uimodel.UserType;
import model.Student; // 引入 Student 实体类
import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 主窗体：负责根据登录角色分发功能面板
 */
public class MainFrame extends JFrame {
    private JTabbedPane tabs;
    private boolean isExiting = false;
    private User currentUser;
    private JLabel statusTimeLabel;

    public MainFrame(User user) {
        this.currentUser = user;
        initLookAndFeel();
        initUI();
        setupListeners();
        startClock(); // 启动状态栏时钟
    }

    private void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void initUI() {
        setTitle("学生宿舍管理系统 V2.0");
        setSize(1280, 800); // 略微扩大默认尺寸
        setMinimumSize(new Dimension(1000, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        setLayout(new BorderLayout());

        // 1. 顶部菜单栏
        setJMenuBar(createMenuBar());

        // 2. 主选项卡面板
        tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        addFunctionTabs();
        add(tabs, BorderLayout.CENTER);

        // 3. 底部状态栏
        add(createStatusBar(), BorderLayout.SOUTH);

        setVisible(true);
    }

    /**
     * 核心逻辑：根据用户身份加载对应的面板
     */
    private void addFunctionTabs() {
        UserType userType = currentUser.getUserType();

        if (userType == UserType.STUDENT) {
            // --- 学生视角 ---
            // 将 User 对象封装为 Student 实体对象传入
            Student student = new Student();
            student.setSno(currentUser.getStudentId() != null ? currentUser.getStudentId() : "N/A");
            student.setName(currentUser.getName() != null ? currentUser.getName() : currentUser.getUsername());

            tabs.addTab("个人工作台", new StudentDashboardPanel(student));

        } else {
            // --- 管理员/宿管视角 ---
            // 使用之前定义的管理端面板
            tabs.addTab("学生档案管理", new StudentPanel());
            tabs.addTab("宿舍资源管理", new RoomPanel());
            tabs.addTab("日常考勤监控", new AttendancePanel());
            tabs.addTab("外来人员登记", new VisitorPanel());

            // 如果是超级管理员，可以增加系统配置页
            if (userType == UserType.ADMIN) {
                // tabs.addTab("系统配置", new ConfigPanel());
            }
        }
    }

    /**
     * 创建底部状态栏
     */
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
        statusBar.setPreferredSize(new Dimension(this.getWidth(), 30));

        String roleName = currentUser.getUserType() == UserType.STUDENT ? "学生" : "管理员";
        JLabel userLabel = new JLabel(" 当前登录： " + currentUser.getUsername() + " [" + roleName + "]  ");
        userLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        statusTimeLabel = new JLabel();
        statusTimeLabel.setFont(new Font("Consolas", Font.PLAIN, 13));

        statusBar.add(userLabel, BorderLayout.WEST);
        statusBar.add(statusTimeLabel, BorderLayout.EAST);

        return statusBar;
    }

    private void startClock() {
        Timer timer = new Timer(1000, e -> {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss  ");
            statusTimeLabel.setText(dtf.format(LocalDateTime.now()));
        });
        timer.start();
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // 文件菜单
        JMenu fileMenu = new JMenu("系统(S)");
        fileMenu.setMnemonic('S');

        JMenuItem logoutItem = new JMenuItem("注销登录");
        JMenuItem exitItem = new JMenuItem("退出系统");

        logoutItem.addActionListener(e -> {
            dispose();
            // 此处应调回 LoginFrame
            // new LoginFrame().setVisible(true);
        });
        exitItem.addActionListener(e -> confirmAndExit());

        fileMenu.add(new JMenuItem("数据备份"));
        fileMenu.addSeparator();
        fileMenu.add(logoutItem);
        fileMenu.add(exitItem);

        // 帮助菜单
        JMenu helpMenu = new JMenu("帮助(H)");
        helpMenu.setMnemonic('H');
        JMenuItem helpItem = new JMenuItem("使用手册");
        JMenuItem aboutItem = new JMenuItem("关于");

        helpItem.addActionListener(e -> showHelp());
        aboutItem.addActionListener(e -> showAbout());

        helpMenu.add(helpItem);
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(Box.createHorizontalGlue()); // 将帮助推到最右侧
        menuBar.add(helpMenu);

        return menuBar;
    }

    /**
     * 确认退出逻辑
     */
    private void confirmAndExit() {
        int confirm = JOptionPane.showConfirmDialog(
                this, "确定要退出学生宿舍管理系统吗？", "确认退出",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            isExiting = true;
            System.exit(0);
        }
    }

    private void setupListeners() {
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                confirmAndExit();
            }
        });
    }

    // --- 以下是原有的辅助方法，已保留并优化 ---

    private void showHelp() {
        // ... 原有代码逻辑 ...
        JOptionPane.showMessageDialog(this, "请查阅项目根目录下的 Help.pdf 或联系管理员。");
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(this,
                "学生宿舍管理系统 V2.0\n基于 Java Swing + JDBC 构建\n\n© 2024 开发小组 版权所有",
                "关于", JOptionPane.INFORMATION_MESSAGE);
    }
}