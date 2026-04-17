package com.lotuslaverne.dao;

import com.lotuslaverne.util.ConnectDB;
import java.sql.*;

public class PhieuThuDAO {

    public boolean taoPhieuThu(String maPhieuThu, String maPhieuDatPhong,
                                String maNhanVienLap, double soTienCoc,
                                String phuongThuc, String ghiChu) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            PreparedStatement pst = con.prepareStatement(
                "INSERT INTO PhieuThu (maPhieuThu,maHoaDon,maNhanVienLap,maPhieuDatPhong,soTienCoc,ngayThu,phuongThucThanhToan,ghiChu) VALUES (?,NULL,?,?,?,GETDATE(),?,?)");
            pst.setString(1, maPhieuThu);
            pst.setString(2, maNhanVienLap);
            pst.setString(3, maPhieuDatPhong);
            pst.setDouble(4, soTienCoc);
            pst.setString(5, phuongThuc);
            pst.setString(6, ghiChu);
            return pst.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
}
