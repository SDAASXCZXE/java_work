package ui;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;

/**
 * 学生宿舍管理系统主界面
 */
public class MainFrame extends JFrame {

    private JTabbedPane tabs;
    private StatusBar statusBar;

    public MainFrame() {
        initLookAndFeel();
        initUI();
        setupListeners();
    }

    /**
     * 初始化界面外观
     */
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

    /**
     * 初始化用户界面
     */
    private void initUI() {
        // 设置窗口属性
        setTitle("学生宿舍管理系统 V2.0");
        setSize(1200, 750);
        setMinimumSize(new Dimension(1000, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setIconImage(createAppIcon());

        // 创建布局
        setLayout(new BorderLayout());

        // 添加菜单栏
        add(createMenuBar(), BorderLayout.NORTH);

        // 创建主选项卡面板
        tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        // 添加功能面板
        addFunctionTabs();

        // 添加选项卡面板到主窗口
        add(tabs, BorderLayout.CENTER);

        // 添加状态栏
        statusBar = new StatusBar();
        add(statusBar, BorderLayout.SOUTH);

        // 显示窗口
        setVisible(true);
    }

    /**
     * 创建应用图标
     */
    private Image createAppIcon() {
        // 创建简单的图形作为备选
        Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
        if (image != null) return image;

        // 创建默认图标
        java.awt.image.BufferedImage defaultIcon = new java.awt.image.BufferedImage(32, 32, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = defaultIcon.createGraphics();
        g2d.setColor(new Color(70, 130, 180));
        g2d.fillRect(0, 0, 32, 32);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("宿", 8, 24);
        g2d.dispose();
        return defaultIcon;
    }

    /**
     * 创建菜单栏
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(70, 130, 180));

        // 文件菜单
        JMenu fileMenu = new JMenu("文件");
        fileMenu.setForeground(Color.WHITE);
        fileMenu.setMnemonic('F');

        JMenuItem newItem = new JMenuItem("新建");
        JMenuItem openItem = new JMenuItem("打开");
        JMenuItem saveItem = new JMenuItem("保存");
        JMenuItem exitItem = new JMenuItem("退出");

        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // 编辑菜单
        JMenu editMenu = new JMenu("编辑");
        editMenu.setForeground(Color.WHITE);
        editMenu.setMnemonic('E');

        JMenuItem cutItem = new JMenuItem("剪切");
        JMenuItem copyItem = new JMenuItem("复制");
        JMenuItem pasteItem = new JMenuItem("粘贴");

        editMenu.add(cutItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);

        // 视图菜单
        JMenu viewMenu = new JMenu("视图");
        viewMenu.setForeground(Color.WHITE);
        viewMenu.setMnemonic('V');

        JCheckBoxMenuItem statusBarItem = new JCheckBoxMenuItem("显示状态栏", true);
        statusBarItem.addActionListener(e -> {
            statusBar.setVisible(statusBarItem.isSelected());
        });

        viewMenu.add(statusBarItem);

        // 工具菜单
        JMenu toolMenu = new JMenu("工具");
        toolMenu.setForeground(Color.WHITE);
        toolMenu.setMnemonic('T');

        JMenuItem backupItem = new JMenuItem("数据备份");
        JMenuItem restoreItem = new JMenuItem("数据恢复");

        toolMenu.add(backupItem);
        toolMenu.add(restoreItem);

        // 帮助菜单
        JMenu helpMenu = new JMenu("帮助");
        helpMenu.setForeground(Color.WHITE);
        helpMenu.setMnemonic('H');

        JMenuItem helpItem = new JMenuItem("帮助文档");
        JMenuItem aboutItem = new JMenuItem("关于系统");

        helpMenu.add(helpItem);
        helpMenu.addSeparator();
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(toolMenu);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(helpMenu);

        return menuBar;
    }

    /**
     * 添加功能选项卡 - 使用文字代替图标
     */
    private void addFunctionTabs() {
        // 使用纯文字标题，避免图标显示问题
        tabs.addTab("系统概览", createOverviewPanel());
        tabs.addTab("学生管理", new StudentPanel());
        tabs.addTab("宿舍管理", new RoomPanel());
        tabs.addTab("床位分配", createBedAssignmentPanel());
        tabs.addTab("费用管理", createFeeManagementPanel());
        tabs.addTab("设备管理", createEquipmentPanel());
        tabs.addTab("访客登记", createVisitorPanel());
        tabs.addTab("报修管理", createRepairPanel());
        tabs.addTab("统计报表", createReportPanel());
        tabs.addTab("系统设置", createSettingsPanel());
    }

    /**
     * 设置事件监听器
     */
    private void setupListeners() {
        // 窗口监听器
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                        MainFrame.this,
                        "确定要退出系统吗？",
                        "确认退出",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    dispose();
                    System.exit(0);
                } else {
                    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
                }
            }
        });

        // 选项卡变化监听器
        tabs.addChangeListener(e -> {
            int selectedIndex = tabs.getSelectedIndex();
            if (selectedIndex != -1) {
                String tabName = tabs.getTitleAt(selectedIndex);
                statusBar.updateStatus("当前页面: " + tabName);
            }
        });
    }

    /**
     * 状态栏类
     */
    private class StatusBar extends JPanel {
        private JLabel statusLabel;
        private JLabel userLabel;
        private JLabel timeLabel;

        public StatusBar() {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEtchedBorder());
            setBackground(new Color(240, 240, 240));

            // 状态信息
            statusLabel = new JLabel("就绪");
            statusLabel.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));

            // 用户信息
            userLabel = new JLabel("用户: 管理员");
            userLabel.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));

            // 时间显示
            timeLabel = new JLabel();
            updateTime();
            timeLabel.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));

            // 启动时间更新线程
            new Timer(1000, e -> updateTime()).start();

            add(statusLabel, BorderLayout.WEST);
            add(userLabel, BorderLayout.CENTER);
            add(timeLabel, BorderLayout.EAST);
        }

        public void updateStatus(String message) {
            statusLabel.setText(message);
        }

        private void updateTime() {
            timeLabel.setText(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
        }
    }

    // 以下方法创建其他功能面板（简化版）
    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        // 欢迎标题
        JLabel titleLabel = new JLabel("欢迎使用学生宿舍管理系统");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 28));
        titleLabel.setForeground(new Color(70, 130, 180));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 系统信息
        JTextArea infoArea = new JTextArea();
        infoArea.setText("系统功能：\n" +
                "1. 学生信息管理\n" +
                "2. 宿舍分配管理\n" +
                "3. 费用缴纳管理\n" +
                "4. 设备资产管理\n" +
                "5. 访客登记管理\n" +
                "6. 报修处理管理\n" +
                "7. 统计报表分析\n" +
                "8. 系统设置维护");
        infoArea.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        infoArea.setEditable(false);
        infoArea.setBackground(new Color(240, 245, 250));
        infoArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        content.add(titleLabel);
        content.add(Box.createVerticalStrut(30));
        content.add(infoArea);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBedAssignmentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("床位分配管理 - 功能开发中", SwingConstants.CENTER), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFeeManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("费用管理 - 功能开发中", SwingConstants.CENTER), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createEquipmentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("设备管理 - 功能开发中", SwingConstants.CENTER), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createVisitorPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("访客登记 - 功能开发中", SwingConstants.CENTER), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRepairPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("报修管理 - 功能开发中", SwingConstants.CENTER), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("统计报表 - 功能开发中", SwingConstants.CENTER), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("系统设置 - 功能开发中", SwingConstants.CENTER), BorderLayout.CENTER);
        return panel;
    }

    /**
     * 主方法
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new MainFrame();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "系统启动失败: " + e.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}