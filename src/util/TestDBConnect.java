package util;

import java.sql.Connection;

public class TestDBConnect {
    public static void main(String[] args) {
        System.out.println("Testing DB connection using DBUtil.getConnection()...");
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.out.println("DBUtil.getConnection() returned null (unable to connect)");
            } else {
                System.out.println("Connection established: " + conn.getMetaData().getURL());
            }
        } catch (Exception e) {
            System.out.println("Exception while testing DB connection: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (Exception ignored) {}
            }
        }
    }
}

