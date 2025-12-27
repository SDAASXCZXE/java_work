/*
 * 文件：DBUtil.java
 * 说明：数据库工具类，提供获取 JDBC 连接的方法。
 * 用法：DAO 层使用 DBUtil.getConnection() 获取数据库连接（注意配置 URL/USER/PASSWORD）。
 * 注意：本文件仅添加注释，不修改原有数据库配置信息或逻辑。
 */

package util;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * 数据库工具类
 * 预留 JDBC 实现
 */
public class DBUtil {

    private static final String URL =
            "jdbc:mysql://rm-bp188tau63v6gj419qo.mysql.rds.aliyuncs.com:3306/student"
                    + "?useSSL=false"
                    + "&serverTimezone=Asia/Shanghai"
                    + "&characterEncoding=utf8";
    private static final String USER = "java_user";
    private static final String PASSWORD = "SUNsun123@";

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
