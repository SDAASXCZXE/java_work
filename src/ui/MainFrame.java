package ui;

import uimodel.User;
import uimodel.UserType;
import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;

public class MainFrame extends JFrame {
    private JTabbedPane tabs;
    private boolean isExiting = false;
    private User currentUser;

    public MainFrame(User user) {
        this.currentUser = user;
        initLookAndFeel();
        initUI();
        setupListeners();
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
        // 设置窗口属性
        setTitle("学生宿舍管理系统 V2.0");
        setSize(1200, 750);
        setMinimumSize(new Dimension(1000, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // 创建布局
        setLayout(new BorderLayout());

        // 添加菜单栏
        add(createMenuBar(), BorderLayout.NORTH);

        // 创建主选项卡面板
        tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        // 添加功能面板（根据用户类型）
        addFunctionTabs();

        // 添加选项卡面板到主窗口
        add(tabs, BorderLayout.CENTER);

        // 显示窗口
        setVisible(true);
    }

    /**
     * 添加功能选项卡 - 根据用户类型显示不同面板
     */
    private void addFunctionTabs() {
        UserType userType = currentUser.getUserType();

        if (userType == UserType.STUDENT) {
            // 学生只能看到个人面板
            String dormitory = "A101"; // 默认宿舍
            if (currentUser.getStudentId() != null && currentUser.getStudentId().length() >= 8) {
                // 使用学号后两位作为宿舍号的一部分
                dormitory = "A" + currentUser.getStudentId().substring(6) + "01";
            }

            String studentName = currentUser.getName() != null ?
                    currentUser.getName() : currentUser.getUsername();
            String studentId = currentUser.getStudentId() != null ?
                    currentUser.getStudentId() : "20230001";

            tabs.addTab("我的宿舍", new StudentDashboardPanel(studentId, studentName, dormitory));

        } else {
            // 管理员和宿舍管理员看到完整系统
            tabs.addTab("学生管理", new StudentPanel());
            tabs.addTab("宿舍管理", new RoomPanel());
            tabs.addTab("访客登记", new VisitorPanel());
            tabs.addTab("考勤管理", new AttendancePanel());

            if (userType == UserType.ADMIN) {
                // 只有系统管理员能看到这些功能
                tabs.addTab("系统设置", createSettingsPanel());
            }
        }
    }

    /**
     * 创建菜单栏
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(240, 240, 240));

        // 文件菜单
        JMenu fileMenu = new JMenu("文件");
        fileMenu.setForeground(Color.BLACK);
        fileMenu.setMnemonic('F');

        JMenuItem saveItem = new JMenuItem("保存");
        JMenuItem importItem = new JMenuItem("导入数据");
        JMenuItem exportItem = new JMenuItem("导出数据");
        JMenuItem exitItem = new JMenuItem("退出");

        // 为退出菜单项添加事件
        exitItem.addActionListener(e -> confirmAndExit());

        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(importItem);
        fileMenu.add(exportItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // 编辑菜单
        JMenu editMenu = new JMenu("编辑");
        editMenu.setForeground(Color.BLACK);
        editMenu.setMnemonic('E');

        JMenuItem cutItem = new JMenuItem("剪切");
        JMenuItem copyItem = new JMenuItem("复制");
        JMenuItem pasteItem = new JMenuItem("粘贴");

        editMenu.add(cutItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);

        // 视图菜单
        JMenu viewMenu = new JMenu("视图");
        viewMenu.setForeground(Color.BLACK);
        viewMenu.setMnemonic('V');

        JMenuItem refreshItem = new JMenuItem("刷新");
        JMenuItem zoomInItem = new JMenuItem("放大");
        JMenuItem zoomOutItem = new JMenuItem("缩小");
        JMenuItem resetZoomItem = new JMenuItem("重置缩放");

        refreshItem.addActionListener(e -> refreshView());
        zoomInItem.addActionListener(e -> zoomIn());
        zoomOutItem.addActionListener(e -> zoomOut());
        resetZoomItem.addActionListener(e -> resetZoom());

        viewMenu.add(refreshItem);
        viewMenu.addSeparator();
        viewMenu.add(zoomInItem);
        viewMenu.add(zoomOutItem);
        viewMenu.add(resetZoomItem);

        // 帮助菜单
        JMenu helpMenu = new JMenu("帮助");
        helpMenu.setForeground(Color.BLACK);
        helpMenu.setMnemonic('H');

        JMenuItem helpItem = new JMenuItem("帮助文档");
        JMenuItem aboutItem = new JMenuItem("关于系统");

        helpItem.addActionListener(e -> showHelp());
        aboutItem.addActionListener(e -> showAbout());

        helpMenu.add(helpItem);
        helpMenu.addSeparator();
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(helpMenu);

        return menuBar;
    }

    /**
     * 设置事件监听器
     */
    private void setupListeners() {
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (!isExiting) {
                    confirmAndExit();
                }
            }
        });
    }

    /**
     * 确认并退出系统
     */
    private void confirmAndExit() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "确定要退出系统吗？",
                "确认退出",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            isExiting = true;

            System.out.println("正在退出系统...");

            Timer timer = new Timer(100, new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    dispose();
                    System.exit(0);
                }
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    /**
     * 视图菜单功能
     */
    private void refreshView() {
        int selectedIndex = tabs.getSelectedIndex();
        if (selectedIndex != -1) {
            Component selectedTab = tabs.getComponentAt(selectedIndex);
            if (selectedTab instanceof JPanel) {
                selectedTab.revalidate();
                selectedTab.repaint();
                JOptionPane.showMessageDialog(this,
                        "视图已刷新",
                        "提示",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void zoomIn() {
        Font currentFont = tabs.getFont();
        float newSize = currentFont.getSize2D() + 1;
        Font newFont = currentFont.deriveFont(newSize);
        tabs.setFont(newFont);

        Component selectedTab = tabs.getComponentAt(tabs.getSelectedIndex());
        if (selectedTab instanceof JPanel) {
            updateAllComponentsFont((JPanel) selectedTab, newFont);
        }
    }

    private void zoomOut() {
        Font currentFont = tabs.getFont();
        float newSize = Math.max(currentFont.getSize2D() - 1, 8);
        Font newFont = currentFont.deriveFont(newSize);
        tabs.setFont(newFont);

        Component selectedTab = tabs.getComponentAt(tabs.getSelectedIndex());
        if (selectedTab instanceof JPanel) {
            updateAllComponentsFont((JPanel) selectedTab, newFont);
        }
    }

    private void resetZoom() {
        Font defaultFont = new Font("微软雅黑", Font.PLAIN, 14);
        tabs.setFont(defaultFont);

        Component selectedTab = tabs.getComponentAt(tabs.getSelectedIndex());
        if (selectedTab instanceof JPanel) {
            updateAllComponentsFont((JPanel) selectedTab, defaultFont);
        }

        JOptionPane.showMessageDialog(this,
                "缩放已重置",
                "提示",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateAllComponentsFont(Container container, Font font) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JComponent) {
                ((JComponent) comp).setFont(font);
            }
            if (comp instanceof Container) {
                updateAllComponentsFont((Container) comp, font);
            }
        }
    }

    /**
     * 创建系统设置面板
     */
    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // 标题
        JLabel titleLabel = new JLabel("系统设置");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 130, 180));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 设置选项
        JPanel settingsPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        settingsPanel.setBorder(BorderFactory.createTitledBorder("设置选项"));
        settingsPanel.setMaximumSize(new Dimension(600, 300));

        String[] settings = {"数据备份路径:", "自动保存间隔:", "默认用户类型:", "系统语言:", "主题颜色:"};
        String[] defaults = {"./backup/", "30分钟", "学生", "中文", "蓝色主题"};

        for (int i = 0; i < settings.length; i++) {
            settingsPanel.add(new JLabel(settings[i]));
            JTextField field = new JTextField(defaults[i]);
            settingsPanel.add(field);
        }

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton saveBtn = new JButton("保存设置");
        saveBtn.setBackground(new Color(70, 130, 180));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "设置已保存！", "提示", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton resetBtn = new JButton("恢复默认");
        resetBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "设置已恢复为默认值！", "提示", JOptionPane.INFORMATION_MESSAGE);
        });

        buttonPanel.add(saveBtn);
        buttonPanel.add(resetBtn);

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(settingsPanel);
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(buttonPanel);

        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }

    /**
     * 帮助菜单功能
     */
    private void showHelp() {
        StringBuilder helpText = new StringBuilder();
        helpText.append("学生宿舍管理系统使用说明\n\n");

        UserType userType = currentUser.getUserType();

        if (userType == UserType.STUDENT) {
            helpText.append("【学生功能说明】\n");
            helpText.append("1. 我的宿舍：查看个人宿舍信息和相关功能\n");
            helpText.append("   • 宿舍报修：提交宿舍维修申请\n");
            helpText.append("   • 换宿申请：申请更换宿舍\n");
            helpText.append("   • 假期登记：登记离校/返校信息\n");
            helpText.append("   • 我的考勤：查看个人考勤记录\n");
            helpText.append("   • 个人信息：查看和更新个人信息\n\n");
            helpText.append("使用提示：\n");
            helpText.append("• 报修时请详细描述问题\n");
            helpText.append("• 假期离校前务必登记\n");
            helpText.append("• 及时查看考勤记录\n");
        } else {
            helpText.append("【管理员功能说明】\n");
            helpText.append("1. 学生管理：管理学生信息\n");
            helpText.append("2. 宿舍管理：管理宿舍信息\n");
            helpText.append("3. 访客登记：管理访客记录\n");
            helpText.append("4. 考勤管理：管理学生考勤\n");
            if (userType == UserType.ADMIN) {
                helpText.append("5. 系统设置：配置系统参数\n\n");
            }
            helpText.append("使用提示：\n");
            helpText.append("• 定期备份重要数据\n");
            helpText.append("• 及时处理学生申请\n");
            helpText.append("• 关注异常考勤情况\n");
        }

        helpText.append("\n技术支持：\n");
        helpText.append("电话：400-123-4567\n");
        helpText.append("邮箱：support@dorm.com\n");

        JTextArea textArea = new JTextArea(helpText.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));

        JOptionPane.showMessageDialog(this, scrollPane, "帮助文档", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAbout() {
        String aboutText = "学生宿舍管理系统 V2.0\n\n" +
                "版本：2.0.0\n" +
                "编译时间：2024年1月\n\n" +
                "功能模块：\n" +
                "• 学生信息管理\n" +
                "• 宿舍分配管理\n" +
                "• 访客登记管理\n" +
                "• 考勤记录管理\n" +
                "• 宿舍报修系统\n\n" +
                "开发团队：宿舍管理系统开发组\n" +
                "© 2024 版权所有";

        JOptionPane.showMessageDialog(this, aboutText, "关于系统", JOptionPane.INFORMATION_MESSAGE);
    }
}