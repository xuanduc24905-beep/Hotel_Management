package com.lotuslaverne.dao;

import com.lotuslaverne.entity.BangGia;
import com.lotuslaverne.util.ConnectDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BangGiaDAO {

    public List<BangGia> getAll() {
        List<BangGia> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;
        try {
            PreparedStatement pst = con.prepareStatement(
                "SELECT * FROM BangGia ORDER BY maLoaiPhong, loaiThue");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public double getGiaHienTai(String maLoaiPhong, String loaiThue) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return 0;
        try {
            PreparedStatement pst = con.prepareStatement(
                "SELECT donGia FROM BangGia WHERE maLoaiPhong=? AND loaiThue=? AND GETDATE() BETWzEEN ngayBatDau AND ngayKetThuc");
            pst.setString(1, maLoaiPhong);
            pst.setString(2, loaiThue);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getDouble("donGia");
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    public boolean them(BangGia bg) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            PreparedStatement pst = con.prepareStatement(
                "INSERT INTO BangGia (maBangGia,maLoaiPhong,loaiThue,donGia,ngayBatDau,ngayKetThuc) VALUES (?,?,?,?,?,?)");
            pst.setString(1, bg.getMaBangGia());
            pst.setString(2, bg.getMaLoaiPhong());
            pst.setString(3, bg.getLoaiThue());
            pst.setDouble(4, bg.getDonGia());
            pst.setDate(5, bg.getNgayBatDau());
            pst.setDate(6, bg.getNgayKetThuc());
            return pst.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean sua(BangGia bg) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            PreparedStatement pst = con.prepareStatement(
                "UPDATE BangGia SET maLoaiPhong=?,loaiThue=?,donGia=?,ngayBatDau=?,ngayKetThuc=? WHERE maBangGia=?");
            pst.setString(1, bg.getMaLoaiPhong());
            pst.setString(2, bg.getLoaiThue());
            pst.setDouble(3, bg.getDonGia());
            pst.setDate(4, bg.getNgayBatDau());
            pst.setDate(5, bg.getNgayKetThuc());
            pst.setString(6, bg.getMaBangGia());
            return pst.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean xoa(String maBangGia) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            PreparedStatement pst = con.prepareStatement("DELETE FROM BangGia WHERE maBangGia=?");
            pst.setString(1, maBangGia);
            return pst.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    private BangGia mapRow(ResultSet rs) throws SQLException {
        return new BangGia(
            rs.getString("maBangGia"), rs.getString("maLoaiPhong"),
            rs.getString("loaiThue"), rs.getDouble("donGia"),
            rs.getDate("ngayBatDau"), rs.getDate("ngayKetThuc"));
    }
}
