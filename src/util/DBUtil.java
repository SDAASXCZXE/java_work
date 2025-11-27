
package util;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * 数据库工具类
 * 预留 JDBC 实现
 */
public class DBUtil {

    private static final String URL = "jdbc:mysql://localhost:3306/dormitory?useSSL=false&characterEncoding=utf8";
    private static final String USER = "root";
    private static final String PASSWORD = "123456";

    public static Connection getConnection() {
        try {
            // Class.forName("com.mysql.cj.jdbc.Driver");
            // return DriverManager.getConnection(URL, USER, PASSWORD);
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
