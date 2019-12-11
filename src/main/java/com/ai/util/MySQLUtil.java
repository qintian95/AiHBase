package com.ai.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author tq
 * @date 2019/12/10 14:21
 */
public class MySQLUtil {
    private static Connection mConnect;
    static {
        try {
            System.out.println("init---");
            Class.forName("com.mysql.jdbc.Driver");
            mConnect=DriverManager.getConnection("jdbc:mysql://172.16.18.28:3306/judicial_documents?useSSL=false", "guest01", "12345678a");
        } catch (ClassNotFoundException | SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public static Connection getConnection() {
        return mConnect;

    }
    public static void  close() {
        try {
            mConnect.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
