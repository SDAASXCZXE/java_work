
package ui;

import javax.swing.*;
import java.awt.*;

/**
 * 主界面窗口
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("学生宿舍管理系统");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("学生管理", new StudentPanel());
        tabs.addTab("宿舍管理", new RoomPanel());

        add(tabs, BorderLayout.CENTER);
        setVisible(true);
    }
}
