package com.lotuslaverne.dao;

import com.lotuslaverne.entity.DichVu;
import com.lotuslaverne.util.ConnectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DichVuDAO {
    
    public List<DichVu> getAll() {
        List<DichVu> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;
        try {
            String sql = "SELECT * FROM DichVu";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(new DichVu(
                    rs.getString("maDichVu"),
                    rs.getString("tenDichVu"),
                    rs.getString("loaiDichVu"),
                    rs.getDouble("donGia"),
                    String.valueOf(rs.getInt("soLuongTonKho"))
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean themDichVu(DichVu dv) {
        Connection con = ConnectDB.getInstance().getConnection();
        try {
            String sql = "INSERT INTO DichVu (maDichVu, tenDichVu, loaiDichVu, donGia, soLuongTonKho) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, dv.getMaDichVu());
            pst.setString(2, dv.getTenDichVu());
            pst.setString(3, dv.getMaLoaiDichVu()); 
            pst.setDouble(4, dv.getDonGia());
            pst.setInt(5, 100); // Mặc định số lượng
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean capNhatDichVu(DichVu dv) {
        Connection con = ConnectDB.getInstance().getConnection();
        try {
            String sql = "UPDATE DichVu SET tenDichVu = ?, loaiDichVu = ?, donGia = ? WHERE maDichVu = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, dv.getTenDichVu());
            pst.setString(2, dv.getMaLoaiDichVu());
            pst.setDouble(3, dv.getDonGia());
            pst.setString(4, dv.getMaDichVu());
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean dungBanDichVu(String maDichVu) {
        Connection con = ConnectDB.getInstance().getConnection();
        try {
            String sql = "UPDATE DichVu SET soLuongTonKho = 0 WHERE maDichVu = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, maDichVu);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
