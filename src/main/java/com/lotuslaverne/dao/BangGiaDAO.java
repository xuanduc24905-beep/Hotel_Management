package com.lotuslaverne.dao;

import com.lotuslaverne.entity.BangGia;
import com.lotuslaverne.util.ConnectDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BangGiaDAO {

    // ─────────────────── READ ───────────────────

    public List<BangGia> getAll() {
        return query("SELECT * FROM BangGia ORDER BY maLoaiPhong, kyGia, loaiThue");
    }

    public List<BangGia> getAllSorted(String orderByColumn, boolean ascending) {
        String dir = ascending ? "ASC" : "DESC";
        String col;
        switch (orderByColumn) {
            case "maBangGia":   col = "maBangGia";   break;
            case "maLoaiPhong": col = "maLoaiPhong"; break;
            case "loaiThue":    col = "loaiThue";    break;
            case "kyGia":       col = "kyGia";       break;
            case "donGia":      col = "donGia";      break;
            case "ngayBatDau":  col = "ngayBatDau";  break;
            case "ngayKetThuc": col = "ngayKetThuc"; break;
            default:            col = "maBangGia";   break;
        }
        return query("SELECT * FROM BangGia ORDER BY " + col + " " + dir);
    }

    public double getGiaHienTai(String maLoaiPhong, String loaiThue) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return 0;
        try {
            PreparedStatement pst = con.prepareStatement(
                "SELECT donGia FROM BangGia WHERE maLoaiPhong=? AND loaiThue=? AND GETDATE() BETWEEN ngayBatDau AND ngayKetThuc");
            pst.setString(1, maLoaiPhong);
            pst.setString(2, loaiThue);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getDouble("donGia");
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    public double getGiaHienTai(String maLoaiPhong, String loaiThue, String kyGia) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return 0;
        try {
            PreparedStatement pst = con.prepareStatement(
                "SELECT donGia FROM BangGia WHERE maLoaiPhong=? AND loaiThue=? AND kyGia=? AND GETDATE() BETWEEN ngayBatDau AND ngayKetThuc");
            pst.setString(1, maLoaiPhong);
            pst.setString(2, loaiThue);
            pst.setString(3, kyGia);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getDouble("donGia");
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    public List<BangGia> getByKyGia(String kyGia) {
        Connection con = ConnectDB.getInstance().getConnection();
        List<BangGia> list = new ArrayList<>();
        if (con == null) return list;
        try {
            PreparedStatement pst = con.prepareStatement(
                "SELECT * FROM BangGia WHERE kyGia=? ORDER BY maLoaiPhong, loaiThue");
            pst.setString(1, kyGia);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<BangGia> getByLoaiPhongAndKyGia(String maLoaiPhong, String kyGia) {
        Connection con = ConnectDB.getInstance().getConnection();
        List<BangGia> list = new ArrayList<>();
        if (con == null) return list;
        try {
            PreparedStatement pst = con.prepareStatement(
                "SELECT * FROM BangGia WHERE maLoaiPhong=? AND kyGia=? ORDER BY loaiThue");
            pst.setString(1, maLoaiPhong);
            pst.setString(2, kyGia);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<BangGia> getByLoaiPhong(String maLoaiPhong) {
        Connection con = ConnectDB.getInstance().getConnection();
        List<BangGia> list = new ArrayList<>();
        if (con == null) return list;
        try {
            PreparedStatement pst = con.prepareStatement(
                "SELECT * FROM BangGia WHERE maLoaiPhong=? ORDER BY kyGia, loaiThue");
            pst.setString(1, maLoaiPhong);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ─────────────────── CREATE / UPDATE / DELETE ───────────────────

    public boolean them(BangGia bg) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            PreparedStatement pst = con.prepareStatement(
                "INSERT INTO BangGia (maBangGia,maLoaiPhong,loaiThue,kyGia,donGia,ngayBatDau,ngayKetThuc) VALUES (?,?,?,?,?,?,?)");
            pst.setString(1, bg.getMaBangGia());
            pst.setString(2, bg.getMaLoaiPhong());
            pst.setString(3, bg.getLoaiThue());
            pst.setString(4, bg.getKyGia());
            pst.setDouble(5, bg.getDonGia());
            pst.setDate(6, bg.getNgayBatDau());
            pst.setDate(7, bg.getNgayKetThuc());
            return pst.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); throw new RuntimeException(e.getMessage(), e); }
    }

    public boolean sua(BangGia bg) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            PreparedStatement pst = con.prepareStatement(
                "UPDATE BangGia SET maLoaiPhong=?,loaiThue=?,kyGia=?,donGia=?,ngayBatDau=?,ngayKetThuc=? WHERE maBangGia=?");
            pst.setString(1, bg.getMaLoaiPhong());
            pst.setString(2, bg.getLoaiThue());
            pst.setString(3, bg.getKyGia());
            pst.setDouble(4, bg.getDonGia());
            pst.setDate(5, bg.getNgayBatDau());
            pst.setDate(6, bg.getNgayKetThuc());
            pst.setString(7, bg.getMaBangGia());
            return pst.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); throw new RuntimeException(e.getMessage(), e); }
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

    // ─────────────────── HELPER ───────────────────

    private List<BangGia> query(String sql) {
        List<BangGia> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;
        try {
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    private BangGia mapRow(ResultSet rs) throws SQLException {
        return new BangGia(
            rs.getString("maBangGia"), rs.getString("maLoaiPhong"),
            rs.getString("loaiThue"), rs.getString("kyGia"),
            rs.getDouble("donGia"),
            rs.getDate("ngayBatDau"), rs.getDate("ngayKetThuc"));
    }
}
