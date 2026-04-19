package com.lotuslaverne.dao;

import com.lotuslaverne.util.ConnectDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ThongKeDAO {

    public double layDoanhThuHomNay() {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return 0.0;
        String sql = "SELECT SUM(tienThanhToan) AS total FROM HoaDon WHERE CAST(ngayLap as DATE) = CAST(GETDATE() as DATE)";
        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) return rs.getDouble("total");
        } catch (Exception e) {
            System.err.println("Chưa có HD hoặc DB lỗi: " + e.getMessage());
        }
        return 0.0;
    }

    public int demSoPhongTheoTrangThai(String trangThai) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return 0;
        String sql = "SELECT COUNT(*) AS sl FROM Phong WHERE trangThai = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, trangThai);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt("sl");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int demTongNhanSu() {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return 0;
        String sql = "SELECT COUNT(*) AS sl FROM NhanVien WHERE vaiTro != 'NghiViec'";
        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) return rs.getInt("sl");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
