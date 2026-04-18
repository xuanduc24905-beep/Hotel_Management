package com.lotuslaverne.application;

import com.lotuslaverne.util.ConnectDB;
import com.lotuslaverne.util.PasswordUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SeedAdmin {
    public static void main(String[] args) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) {
            System.out.println("[LỖI] Không kết nối được DB!");
            return;
        }
        System.out.println("[OK] Kết nối DB thành công.");

        try {
            // Lấy toàn bộ tài khoản để xem DB có gì
            ResultSet rs = con.prepareStatement("SELECT maTaiKhoan, tenDangNhap, matKhau FROM TaiKhoan").executeQuery();
            System.out.println("--- Danh sách TaiKhoan hiện có ---");
            boolean found = false;
            while (rs.next()) {
                String ma   = rs.getString("maTaiKhoan");
                String user = rs.getString("tenDangNhap");
                String pw   = rs.getString("matKhau");
                System.out.println("  " + ma + " | " + user + " | " + pw);
                if ("admin".equalsIgnoreCase(user)) {
                    found = true;
                    // Force reset về BCrypt thật của "admin123"
                    String hash = PasswordUtil.hash("admin123");
                    PreparedStatement upd = con.prepareStatement(
                        "UPDATE TaiKhoan SET matKhau=? WHERE tenDangNhap=?");
                    upd.setString(1, hash);
                    upd.setString(2, user);
                    int rows = upd.executeUpdate();
                    System.out.println("[OK] Đã reset mật khẩu 'admin' -> BCrypt(admin123). Rows: " + rows);
                }
            }
            if (!found) System.out.println("[WARN] Không tìm thấy tài khoản 'admin' trong DB.");

        } catch (Exception e) {
            System.out.println("[LỖI] Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
