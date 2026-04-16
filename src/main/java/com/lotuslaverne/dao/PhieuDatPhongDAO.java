package com.lotuslaverne.dao;

import com.lotuslaverne.entity.PhieuDatPhong;
import com.lotuslaverne.util.ConnectDB;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class PhieuDatPhongDAO {
    
    public boolean lapPhieuDat(PhieuDatPhong pdp) {
        Connection con = ConnectDB.getInstance().getConnection();
        try {
            String sql = "INSERT INTO PhieuDatPhong (maPhieuDatPhong, maNhanVien, maKH, ngayLapPhieu, soNguoi) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, pdp.getMaPhieuDatPhong());
            pst.setString(2, pdp.getMaNhanVien());
            pst.setString(3, pdp.getMaKhachHang());
            pst.setTimestamp(4, pdp.getNgayLapPhieu());
            pst.setInt(5, pdp.getSoNguoi());
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
