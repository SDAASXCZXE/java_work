/*
 * 文件：UserManager.java
 * 说明：前端用户管理器，提供当前登录用户的管理（单例式使用）。
 * 注意：仅添加注释，不修改业务逻辑。
 */

package uimodel;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 用户管理器 - 管理用户数据
 */

public class UserManager {
    private static final String USER_DATA_FILE = "users.dat";
    private List<User> users;
    private static UserManager instance;
    private static User currentUser; // 当前登录用户

    private UserManager() {
        users = new ArrayList<>();
        loadUsers();
        if (users.isEmpty()) {
            initializeDefaultUsers();
        }
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
        // 添加默认管理员账户
        users.add(new User("admin", "admin123", UserType.ADMIN,
                null, "系统管理员", "13800000000", "admin@dorm.com"));

        // 添加默认学生账户
        users.add(new User("student", "student123", UserType.STUDENT,
                "20230001", "张三", "13822222222", "student@school.com"));

        // 保存到文件
        saveUsers();
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
        if (userType == UserType.STUDENT && studentId != null && !studentId.trim().isEmpty()) {
            for (User user : users) {
                if (user.getUserType() == UserType.STUDENT &&
                        studentId.equals(user.getStudentId())) {
                    return false;
                }
            }
        }

        // 创建新用户
        User newUser = new User(username, password, userType, studentId, name, phone, email);
        users.add(newUser);

        // 保存到文件
        return saveUsers();
    }

    /**
     * 删除指定用户名的用户（用于回滚）
     */
    public synchronized boolean removeUserByUsername(String username) {
        boolean removed = false;
        Iterator<User> it = users.iterator();
        while (it.hasNext()) {
            User u = it.next();
            if (u.getUsername().equals(username)) {
                it.remove();
                removed = true;
                break;
            }
        }
        if (removed) return saveUsers();
        return false;
    }

    /**
     * 检查指定学号是否已在用户列表中存在（学生账号）
     */
    public synchronized boolean existsStudentId(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) return false;
        for (User u : users) {
            if (u.getUserType() == UserType.STUDENT && studentId.equals(u.getStudentId())) return true;
        }
        return false;
    }

    /**
     * 加载用户数据
     */
    @SuppressWarnings("unchecked")
    private void loadUsers() {
        File file = new File(USER_DATA_FILE);

        if (file.exists() && file.length() > 0) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                Object obj = ois.readObject();
                if (obj instanceof List) {
                    List<User> loadedUsers = (List<User>) obj;
                    if (loadedUsers != null) {
                        users.clear();
                        users.addAll(loadedUsers);
                    }
                }
            } catch (Exception e) {
                users = new ArrayList<>();
            }
        } else {
            users = new ArrayList<>();
        }
    }

    /**
     * 保存用户数据
     */
    private boolean saveUsers() {
        File file = new File(USER_DATA_FILE);
        File parentDir = file.getParentFile();

        // 确保目录存在
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(users);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 用户登录验证
     */
    public User login(String username, String password, UserType userType) {
        for (User user : users) {
            if (user.getUsername().equals(username) &&
                    user.getPassword().equals(password) &&
                    user.getUserType() == userType) {
                currentUser = user; // 设置当前登录用户
                return user;
            }
        }
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
     * 获取所有用户列表（用于外部访问）
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    /**
     * 获取当前登录用户
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * 设置当前登录用户
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }
}

