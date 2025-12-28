import ui.LoginFrame;

import javax.swing.*;

import util.StudentRegistrationSync;

/**
 * 应用程序主启动类
 */
public class App {
    public static void main(String[] args) {
        // 设置Swing外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 启动后台同步守护线程：将 student 表中的学号同步到前端用户文件（若用户不存在）
        StudentRegistrationSync sync = new StudentRegistrationSync(30); // 每 30 秒同步一次
        Thread t = new Thread(sync, "StudentRegistrationSync");
        t.setDaemon(true);
        t.start();

        // 启动登录界面
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });

    }
}
