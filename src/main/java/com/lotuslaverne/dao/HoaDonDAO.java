package com.lotuslaverne.dao;

import com.lotuslaverne.entity.HoaDon;
import com.lotuslaverne.util.ConnectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class HoaDonDAO {

    public boolean taoHoaDon(HoaDon hd) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            String sql = "INSERT INTO HoaDon (maHoaDon, maNhanVien, maKH, ngayLap, tongTien, ghiChu) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, hd.getMaHoaDon());
            pst.setString(2, hd.getMaNhanVien());
            pst.setString(3, hd.getMaKhachHang());
            pst.setTimestamp(4, hd.getNgayLap());
            pst.setDouble(5, hd.getTongTien());
            pst.setString(6, hd.getGhiChu());
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
