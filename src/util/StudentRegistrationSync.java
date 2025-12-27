package util;

import model.Student;
import service.StudentService;
import service.impl.StudentServiceImpl;
import uimodel.UserManager;
import uimodel.UserType;

import java.util.List;

/**
 * 后台同步线程：根据学生档案表（student）中的数据，自动在前端用户文件中创建对应学生账号（若不存在）
 * 默认账号：用户名 = 学号，密码 = "123456"
 *
 * 说明：该线程为守护线程，可以在应用启动时启动并周期性运行。
 */
public class StudentRegistrationSync implements Runnable {
    private volatile boolean running = true;
    private final int intervalSeconds;

    public StudentRegistrationSync() {
        this(60); // 默认 60 秒一次
    }

    public StudentRegistrationSync(int intervalSeconds) {
        this.intervalSeconds = Math.max(5, intervalSeconds);
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        StudentService studentService = new StudentServiceImpl();
        UserManager userManager = UserManager.getInstance();

        // 首次立即同步一次
        syncOnce(studentService, userManager);

        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(intervalSeconds * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            syncOnce(studentService, userManager);
        }
    }

    private void syncOnce(StudentService studentService, UserManager userManager) {
        try {
            List<Student> students = studentService.listStudents();
            if (students == null || students.isEmpty()) return;

            for (Student s : students) {
                if (s == null) continue;
                String sno = s.getSno();
                if (sno == null || sno.trim().isEmpty()) continue;

                // 若已有用户与该学号关联或用户名已存在，则跳过
                if (userManager.existsStudentId(sno) || userManager.getUserByUsername(sno) != null) continue;

                // 创建前端用户，默认密码为 123456
                try {
                    boolean ok = userManager.registerUser(sno, "123456", UserType.STUDENT, sno, s.getName(), s.getPhone(), "");
                    if (ok) {
                        // 若需要，可以在此处写日志或触发刷新通知
                        util.RefreshCenter.notify("students-updated");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

