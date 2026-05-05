package com.lotuslaverne.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
    private static ConnectDB instance;
    private Connection connection;

    // Cấu hình chuỗi kết nối dựa trên thiết lập máy cá nhân
    // Người dùng cần đổi localhost, port, hoặc user/password nếu có
    private final String url = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyKhachSan;encrypt=true;trustServerCertificate=true;";
    private final String user = "sa";
    private final String password = "123456"; // THAY BẬT SQL SERVER SA PASSWORD Ở ĐÂY

    private ConnectDB() {
        try {
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Kết nối CSDL LotusLaverne thành công!");
        } catch (SQLException e) {
            System.err.println("Chạy offline (không có DB): " + e.getMessage());
            connection = null;
        }
    }

    public static ConnectDB getInstance() {
        if (instance == null) {
            instance = new ConnectDB();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
            }
        } catch (SQLException e) {
            System.err.println("Offline mode: " + e.getMessage());
            connection = null;
        }
        return connection;
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
