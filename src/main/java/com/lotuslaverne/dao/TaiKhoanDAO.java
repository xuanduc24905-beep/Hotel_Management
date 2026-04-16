package com.lotuslaverne.dao;

import com.lotuslaverne.entity.TaiKhoan;
import com.lotuslaverne.util.ConnectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TaiKhoanDAO {
    
    // Kiểm tra đăng nhập
    public TaiKhoan checkLogin(String username, String password) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return null; // Thêm cảnh báo nếu rớt kết nối CSDL

        TaiKhoan tk = null;
        try {
            // Thực hiện truy vấn trong SQL Server bảng TaiKhoan
            String sql = "SELECT * FROM TaiKhoan WHERE tenDangNhap = ? AND matKhau = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, username);
            pst.setString(2, password);
            
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                tk = new TaiKhoan(
                    rs.getString("maTaiKhoan"),
                    rs.getString("maNhanVien"),
                    rs.getString("vaiTro"),
                    rs.getString("tenDangNhap"),
                    rs.getString("matKhau")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tk;
    }
}
