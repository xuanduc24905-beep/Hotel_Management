package com.lotuslaverne.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
    private static ConnectDB instance;
    private Connection connection;
    private long lastFailTime = -1;
    private static final long RETRY_INTERVAL_MS = 30_000;

    private final String url = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyKhachSan;encrypt=true;trustServerCertificate=true;loginTimeout=3;";
    private final String user = "sa";
    private final String password = "sapassword"; // THAY MẬT KHẨU SA Ở ĐÂY

    private ConnectDB() {
        tryConnect(true);
    }

    public static ConnectDB getInstance() {
        if (instance == null) {
            instance = new ConnectDB();
        }
        return instance;
    }

    public Connection getConnection() {
        if (connection != null) {
            try { if (!connection.isClosed()) return connection; }
            catch (SQLException ignored) {}
        }
        long now = System.currentTimeMillis();
        if (lastFailTime > 0 && (now - lastFailTime) < RETRY_INTERVAL_MS) {
            return null;
        }
        tryConnect(false);
        return connection;
    }

    private void tryConnect(boolean verbose) {
        try {
            connection = DriverManager.getConnection(url, user, password);
            lastFailTime = -1;
            System.out.println("Kết nối CSDL LotusLaverne thành công!");
        } catch (SQLException e) {
            lastFailTime = System.currentTimeMillis();
            connection = null;
            if (verbose) System.err.println("Chạy offline (không có DB): " + e.getMessage());
        }
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
