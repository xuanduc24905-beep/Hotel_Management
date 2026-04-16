package com.lotuslaverne.dao;

import com.lotuslaverne.entity.Phong;
import com.lotuslaverne.util.ConnectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PhongDAO {

    public List<Phong> getAll() {
        List<Phong> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;
        try {
            String sql = "SELECT * FROM Phong";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(new Phong(
                    rs.getString("maPhong"),
                    rs.getString("tenPhong"),
                    rs.getString("maLoaiPhong"),
                    rs.getString("trangThai") // Trống, Đang Thuê, Dơ
                ));
            }
        } catch (Exception e) {
            System.err.println("Load Phòng lỗi: " + e.getMessage());
        }
        return list;
    }

    public boolean themPhong(Phong p) {
        Connection con = ConnectDB.getInstance().getConnection();
        try {
            String sql = "INSERT INTO Phong (maPhong, tenPhong, maLoaiPhong, trangThai) VALUES (?, ?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, p.getMaPhong());
            pst.setString(2, p.getTenPhong());
            pst.setString(3, p.getMaLoaiPhong());
            pst.setString(4, p.getTrangThai());
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean capNhatTrangThai(String maPhong, String trangThaiMoi) {
        Connection con = ConnectDB.getInstance().getConnection();
        try {
            String sql = "UPDATE Phong SET trangThai = ? WHERE maPhong = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, trangThaiMoi);
            pst.setString(2, maPhong);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean xoaPhong(String maPhong) {
        Connection con = ConnectDB.getInstance().getConnection();
        try {
            String sql = "DELETE FROM Phong WHERE maPhong = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, maPhong);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
