package com.lotuslaverne.dao;

import com.lotuslaverne.entity.TaiKhoan;
import com.lotuslaverne.util.ConnectDB;
import com.lotuslaverne.util.PasswordUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaiKhoanDAO {

    /**
     * Đăng nhập: tìm tài khoản theo username trước, sau đó verify hash.
     * Không so sánh mật khẩu trực tiếp trong SQL để tránh timing-attack và
     * để BCrypt có thể kiểm tra hash đúng cách.
     */
    public TaiKhoan checkLogin(String username, String password) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return null;
        try {
            PreparedStatement pst = con.prepareStatement(
                "SELECT * FROM TaiKhoan WHERE tenDangNhap = ?");
            pst.setString(1, username);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("matKhau");
                if (PasswordUtil.verify(password, storedHash)) {
                    return new TaiKhoan(
                        rs.getString("maTaiKhoan"), rs.getString("maNhanVien"),
                        rs.getString("vaiTro"), rs.getString("tenDangNhap"), storedHash);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    /** Tạo tài khoản mới — mật khẩu được băm trước khi lưu vào DB. */
    public boolean taoTaiKhoan(TaiKhoan tk) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            String hashedPassword = PasswordUtil.hash(tk.getMatKhau());
            PreparedStatement pst = con.prepareStatement(
                "INSERT INTO TaiKhoan (maTaiKhoan, maNhanVien, vaiTro, tenDangNhap, matKhau) VALUES (?,?,?,?,?)");
            pst.setString(1, tk.getMaTaiKhoan());
            pst.setString(2, tk.getMaNhanVien());
            pst.setString(3, tk.getVaiTro());
            pst.setString(4, tk.getTenDangNhap());
            pst.setString(5, hashedPassword);
            return pst.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /**
     * Đổi mật khẩu: xác minh mật khẩu cũ trước (verify hash),
     * sau đó lưu hash mới — không bao giờ lưu plaintext.
     */
    public boolean doiMatKhau(String maTaiKhoan, String matKhauCu, String matKhauMoi) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            // Bước 1: lấy hash hiện tại
            PreparedStatement pstGet = con.prepareStatement(
                "SELECT matKhau FROM TaiKhoan WHERE maTaiKhoan = ?");
            pstGet.setString(1, maTaiKhoan);
            ResultSet rs = pstGet.executeQuery();
            if (!rs.next()) return false;

            String currentHash = rs.getString("matKhau");

            // Bước 2: kiểm tra mật khẩu cũ
            if (!PasswordUtil.verify(matKhauCu, currentHash)) return false;

            // Bước 3: lưu hash mới
            PreparedStatement pstUpdate = con.prepareStatement(
                "UPDATE TaiKhoan SET matKhau = ? WHERE maTaiKhoan = ?");
            pstUpdate.setString(1, PasswordUtil.hash(matKhauMoi));
            pstUpdate.setString(2, maTaiKhoan);
            return pstUpdate.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean suaVaiTro(String maTaiKhoan, String vaiTroMoi) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            PreparedStatement pst = con.prepareStatement(
                "UPDATE TaiKhoan SET vaiTro = ? WHERE maTaiKhoan = ?");
            pst.setString(1, vaiTroMoi);
            pst.setString(2, maTaiKhoan);
            return pst.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean xoa(String maTaiKhoan) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            PreparedStatement pst = con.prepareStatement(
                "DELETE FROM TaiKhoan WHERE maTaiKhoan = ?");
            pst.setString(1, maTaiKhoan);
            return pst.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public List<TaiKhoan> getAll() {
        List<TaiKhoan> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;
        try {
            PreparedStatement pst = con.prepareStatement("SELECT * FROM TaiKhoan");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(new TaiKhoan(
                    rs.getString("maTaiKhoan"), rs.getString("maNhanVien"),
                    rs.getString("vaiTro"), rs.getString("tenDangNhap"), rs.getString("matKhau")));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
}
