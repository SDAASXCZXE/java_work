package uimodel;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户管理器 - 管理用户数据
 */
public class UserManager {
    private static final String USER_DATA_FILE = "users.dat";
    private List<User> users;
    private static UserManager instance;

    private UserManager() {
        users = new ArrayList<>();
        loadUsers();
        initializeDefaultUsers();
    }

    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    /**
     * 初始化默认用户
     */
    private void initializeDefaultUsers() {
        if (users.isEmpty()) {
            // 添加默认管理员账户
            users.add(new User("admin", "admin123", UserType.ADMIN,
                    null, "系统管理员", "13800000000", "admin@dorm.com"));

            // 添加默认宿舍管理员账户
            users.add(new User("manager", "manager123", UserType.DORM_MANAGER,
                    null, "宿舍管理员", "13811111111", "manager@dorm.com"));

            // 添加默认学生账户
            users.add(new User("student", "student123", UserType.STUDENT,
                    "20230001", "张三", "13822222222", "student@school.com"));

            saveUsers();
        }
    }

    /**
     * 注册新用户
     */
    public boolean registerUser(String username, String password, UserType userType,
                                String studentId, String name, String phone, String email) {
        // 检查用户名是否已存在
        if (getUserByUsername(username) != null) {
            return false;
        }

        // 检查学号是否已存在（仅对学生用户）
        if (userType == UserType.STUDENT && studentId != null) {
            for (User user : users) {
                if (user.getUserType() == UserType.STUDENT &&
                        studentId.equals(user.getStudentId())) {
                    return false;
                }
            }
        }

        User newUser = new User(username, password, userType, studentId, name, phone, email);
        users.add(newUser);
        return saveUsers();
    }

    /**
     * 用户登录验证
     */
    public User login(String username, String password, UserType userType) {
        System.out.println("尝试登录 - 用户名: " + username + ", 类型: " + userType);

        for (User user : users) {
            System.out.println("检查用户: " + user.getUsername() + ", 类型: " + user.getUserType());

            if (user.getUsername().equals(username) &&
                    user.getPassword().equals(password) &&
                    user.getUserType() == userType) {
                System.out.println("登录成功: " + username);
                return user;
            }
        }

        System.out.println("登录失败: " + username);
        return null;
    }

    /**
     * 根据用户名获取用户
     */
    public User getUserByUsername(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    /**
     * 删除用户
     */
    public boolean deleteUser(String username) {
        User user = getUserByUsername(username);
        if (user != null) {
            users.remove(user);
            return saveUsers();
        }
        return false;
    }

    /**
     * 更新用户信息
     */
    public boolean updateUser(User updatedUser) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUsername().equals(updatedUser.getUsername())) {
                users.set(i, updatedUser);
                return saveUsers();
            }
        }
        return false;
    }

    /**
     * 保存用户数据到文件
     */
    @SuppressWarnings("unchecked")
    private void loadUsers() {
        File file = new File(USER_DATA_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                users = (List<User>) ois.readObject();
            } catch (Exception e) {
                System.err.println("加载用户数据失败: " + e.getMessage());
                users = new ArrayList<>();
            }
        }
    }

    /**
     * 从文件加载用户数据
     */
    private boolean saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USER_DATA_FILE))) {
            oos.writeObject(users);
            return true;
        } catch (Exception e) {
            System.err.println("保存用户数据失败: " + e.getMessage());
            return false;
        }
    }
}