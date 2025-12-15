package ui;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;

/**
 * 学生宿舍管理系统主界面
 */
public class MainFrame extends JFrame {

    private JTabbedPane tabs;

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

        // 显示窗口
        setVisible(true);
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

        JMenuItem newItem = new JMenuItem("新建");
        JMenuItem openItem = new JMenuItem("打开");
        JMenuItem saveItem = new JMenuItem("保存");
        JMenuItem importItem = new JMenuItem("导入数据");
        JMenuItem exportItem = new JMenuItem("导出数据");
        JMenuItem exitItem = new JMenuItem("退出");

        // 添加文件菜单功能
        importItem.addActionListener(e -> importData());
        exportItem.addActionListener(e -> exportData());
        exitItem.addActionListener(e -> exitApplication());

        fileMenu.add(newItem);
        fileMenu.add(openItem);
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
        JMenuItem selectAllItem = new JMenuItem("全选");

        editMenu.add(cutItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);
        editMenu.addSeparator();
        editMenu.add(selectAllItem);

        // 视图菜单
        JMenu viewMenu = new JMenu("视图");
        viewMenu.setForeground(Color.BLACK);
        viewMenu.setMnemonic('V');

        JMenuItem refreshItem = new JMenuItem("刷新");
        JMenuItem zoomInItem = new JMenuItem("放大");
        JMenuItem zoomOutItem = new JMenuItem("缩小");
        JMenuItem resetZoomItem = new JMenuItem("重置缩放");
        JMenuItem showChartItem = new JMenuItem("显示统计图表");

        refreshItem.addActionListener(e -> refreshView());
        zoomInItem.addActionListener(e -> zoomIn());
        zoomOutItem.addActionListener(e -> zoomOut());
        resetZoomItem.addActionListener(e -> resetZoom());
        showChartItem.addActionListener(e -> showStatisticsChart());

        viewMenu.add(refreshItem);
        viewMenu.addSeparator();
        viewMenu.add(zoomInItem);
        viewMenu.add(zoomOutItem);
        viewMenu.add(resetZoomItem);
        viewMenu.addSeparator();
        viewMenu.add(showChartItem);

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
     * 添加功能选项卡
     */
    private void addFunctionTabs() {
        tabs.addTab("学生管理", new StudentPanel());
        tabs.addTab("宿舍管理", new RoomPanel());
    }

    /**
     * 设置事件监听器
     */
    private void setupListeners() {
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                exitApplication();
            }
        });
    }

    /**
     * 文件菜单功能
     */
    private void importData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("导入数据");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Excel文件 (*.xls, *.xlsx)", "xls", "xlsx"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            String filePath = file.getAbsolutePath();

            // 模拟数据导入
            try {
                Thread.sleep(1000); // 模拟导入过程
                JOptionPane.showMessageDialog(this,
                        "数据导入成功！\n文件路径: " + filePath + "\n导入记录数: 128",
                        "导入成功",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "导入失败: " + e.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("导出数据");
        fileChooser.setSelectedFile(new java.io.File("宿舍系统数据_" +
                new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".xlsx"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Excel文件 (*.xlsx)", "xlsx"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();

            // 确保文件扩展名
            String filePath = file.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".xlsx")) {
                filePath += ".xlsx";
            }

            // 模拟数据导出
            try {
                Thread.sleep(1500); // 模拟导出过程
                JOptionPane.showMessageDialog(this,
                        "数据导出成功！\n文件路径: " + filePath + "\n导出记录数: 156",
                        "导出成功",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "导出失败: " + e.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exitApplication() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "确定要退出系统吗？",
                "确认退出",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
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

    private void showStatisticsChart() {
        // 创建图表对话框
        JDialog chartDialog = new JDialog(this, "宿舍使用率统计图表", true);
        chartDialog.setSize(800, 600);
        chartDialog.setLocationRelativeTo(this);
        chartDialog.setLayout(new BorderLayout());

        // 创建图表面板
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 绘制标题
                g2d.setFont(new Font("微软雅黑", Font.BOLD, 18));
                g2d.setColor(Color.BLACK);
                g2d.drawString("宿舍使用率统计", 300, 40);

                // 绘制柱状图
                String[] buildings = {"A栋", "B栋", "C栋", "D栋", "E栋"};
                int[] occupied = {95, 88, 76, 92, 65}; // 已使用百分比
                int[] available = {5, 12, 24, 8, 35};  // 空余百分比

                int barWidth = 60;
                int spacing = 40;
                int startX = 100;
                int startY = 100;
                int chartHeight = 300;

                // 绘制坐标轴
                g2d.drawLine(startX, startY, startX, startY + chartHeight);
                g2d.drawLine(startX, startY + chartHeight, startX + (barWidth + spacing) * buildings.length, startY + chartHeight);

                // 绘制柱状图
                for (int i = 0; i < buildings.length; i++) {
                    int barX = startX + i * (barWidth + spacing);

                    // 已使用部分（蓝色）
                    int usedHeight = (int) (chartHeight * occupied[i] / 100.0);
                    g2d.setColor(new Color(70, 130, 180));
                    g2d.fillRect(barX, startY + chartHeight - usedHeight, barWidth, usedHeight);

                    // 空余部分（绿色）
                    int availHeight = (int) (chartHeight * available[i] / 100.0);
                    g2d.setColor(new Color(144, 238, 144));
                    g2d.fillRect(barX, startY + chartHeight - usedHeight - availHeight, barWidth, availHeight);

                    // 边框
                    g2d.setColor(Color.BLACK);
                    g2d.drawRect(barX, startY + chartHeight - usedHeight - availHeight, barWidth, usedHeight + availHeight);

                    // 标注
                    g2d.setFont(new Font("微软雅黑", Font.PLAIN, 12));
                    g2d.drawString(buildings[i], barX + barWidth/2 - 10, startY + chartHeight + 20);
                    g2d.drawString(occupied[i] + "%", barX + barWidth/2 - 10, startY + chartHeight - usedHeight - 5);
                }

                // 图例
                g2d.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                g2d.setColor(new Color(70, 130, 180));
                g2d.fillRect(100, 450, 20, 20);
                g2d.setColor(Color.BLACK);
                g2d.drawString("已使用", 130, 465);

                g2d.setColor(new Color(144, 238, 144));
                g2d.fillRect(200, 450, 20, 20);
                g2d.setColor(Color.BLACK);
                g2d.drawString("空余", 230, 465);

                // 统计信息
                g2d.setFont(new Font("微软雅黑", Font.BOLD, 14));
                g2d.setColor(Color.BLACK);
                g2d.drawString("统计信息:", 100, 500);
                g2d.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                g2d.drawString("总宿舍数: 320间", 100, 525);
                g2d.drawString("总床位数: 1280个", 100, 550);
                g2d.drawString("已住人数: 1185人", 300, 525);
                g2d.drawString("空余床位: 95个", 300, 550);
                g2d.drawString("平均入住率: 92.6%", 500, 525);
            }
        };

        // 添加导出图表按钮
        JPanel buttonPanel = new JPanel();
        JButton exportChartButton = new JButton("导出图表");
        exportChartButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("导出图表");
            fileChooser.setSelectedFile(new java.io.File("宿舍使用率图表.png"));

            int result = fileChooser.showSaveDialog(chartDialog);
            if (result == JFileChooser.APPROVE_OPTION) {
                java.io.File file = fileChooser.getSelectedFile();
                JOptionPane.showMessageDialog(chartDialog,
                        "图表已保存到: " + file.getAbsolutePath(),
                        "导出成功",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        buttonPanel.add(exportChartButton);

        chartDialog.add(chartPanel, BorderLayout.CENTER);
        chartDialog.add(buttonPanel, BorderLayout.SOUTH);
        chartDialog.setVisible(true);
    }

    /**
     * 帮助菜单功能
     */
    private void showHelp() {
        JTextArea helpText = new JTextArea();
        helpText.setText("学生宿舍管理系统使用说明\n\n" +
                "1. 学生管理\n" +
                "   - 新增：添加新学生\n" +
                "   - 编辑：修改学生信息\n" +
                "   - 删除：删除学生记录\n" +
                "   - 分配宿舍：为学生分配宿舍\n" +
                "   - 搜索：按条件查找学生\n" +
                "   - 重置：清除搜索条件\n\n" +
                "2. 宿舍管理\n" +
                "   - 新增宿舍：添加新宿舍\n" +
                "   - 编辑信息：修改宿舍信息\n" +
                "   - 删除宿舍：删除空宿舍\n" +
                "   - 入住登记：办理学生入住\n" +
                "   - 退宿处理：办理学生退宿\n" +
                "   - 查看空余宿舍：显示可用宿舍列表\n\n" +
                "3. 文件菜单\n" +
                "   - 导入数据：从Excel文件导入数据\n" +
                "   - 导出数据：将数据导出为Excel文件\n\n" +
                "4. 视图菜单\n" +
                "   - 刷新：刷新当前视图\n" +
                "   - 放大/缩小：调整视图大小\n" +
                "   - 显示统计图表：查看宿舍使用率统计\n");
        helpText.setEditable(false);
        helpText.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(helpText);
        scrollPane.setPreferredSize(new Dimension(500, 400));

        JOptionPane.showMessageDialog(this, scrollPane, "帮助文档", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAbout() {
        String aboutText = "学生宿舍管理系统 V2.0\n\n" +
                "版本：2.0.0\n" +
                "功能特点：\n" +
                "• 学生信息管理\n" +
                "• 宿舍分配管理\n" +
                "• 数据导入导出\n" +
                "• 统计图表展示\n" +
                "• 可视化界面操作\n\n" +
                "开发团队：宿舍管理系统开发组\n" +
                "联系电话：138-XXXX-XXXX\n" +
                "邮箱：support@dorm.com\n\n" +
                "© 2024 版权所有";

        JOptionPane.showMessageDialog(this, aboutText, "关于系统", JOptionPane.INFORMATION_MESSAGE);
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