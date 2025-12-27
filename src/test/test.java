package test;

import util.DBUtil;

import java.sql.Connection;

public class test{

    public static void main(String[] args) { //测试数据库是否连接成功
        Connection conn = DBUtil.getConnection();

        if (conn != null) {
            System.out.println("数据库连接成功！");
        } else {
            System.out.println("数据库连接失败！");
        }
    }
}