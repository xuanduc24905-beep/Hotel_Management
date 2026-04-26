package com.lotuslaverne.dao;

import com.lotuslaverne.entity.ThietBi;
import com.lotuslaverne.util.ConnectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ThietBiDAO {

    public List<ThietBi> getAll() {
        List<ThietBi> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;
        try {
            String sql = "SELECT * FROM ThietBi";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(new ThietBi(
                    rs.getString("maThietBi"),
                    rs.getString("tenThietBi"),
                    rs.getString("loaiThietBi"),
                    rs.getInt("soLuong"),
                    rs.getDouble("donGia"),
                    rs.getString("trangThai")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean themThietBi(ThietBi tb) {
        Connection con = ConnectDB.getInstance().getConnection();
        try {
            String sql = "INSERT INTO ThietBi (maThietBi, tenThietBi, loaiThietBi, soLuong, donGia, trangThai) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, tb.getMaThietBi());
            pst.setString(2, tb.getTenThietBi());
            pst.setString(3, tb.getLoaiThietBi());
            pst.setInt(4, tb.getSoLuong());
            pst.setDouble(5, tb.getDonGia());
            pst.setString(6, tb.getTrangThai());
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean capNhatThietBi(ThietBi tb) {
        Connection con = ConnectDB.getInstance().getConnection();
        try {
            String sql = "UPDATE ThietBi SET tenThietBi = ?, loaiThietBi = ?, soLuong = ?, donGia = ?, trangThai = ? WHERE maThietBi = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, tb.getTenThietBi());
            pst.setString(2, tb.getLoaiThietBi());
            pst.setInt(3, tb.getSoLuong());
            pst.setDouble(4, tb.getDonGia());
            pst.setString(5, tb.getTrangThai());
            pst.setString(6, tb.getMaThietBi());
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean xoaThietBi(String maThietBi) {
        Connection con = ConnectDB.getInstance().getConnection();
        try {
            String sql = "DELETE FROM ThietBi WHERE maThietBi = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, maThietBi);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
