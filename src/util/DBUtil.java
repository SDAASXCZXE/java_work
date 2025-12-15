
package util;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * 数据库工具类
 * 预留 JDBC 实现
 */
public class DBUtil {

    private static final String URL = "jdbc:mysql://localhost:3306/my_dorm?useSSL=false&serverTimezone=UTC&characterEncoding=utf8";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() {
        try {
             Class.forName("com.mysql.cj.jdbc.Driver");
             return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static void close(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
