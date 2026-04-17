package com.lotuslaverne.dao;

import com.lotuslaverne.entity.LoaiPhong;
import com.lotuslaverne.util.ConnectDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoaiPhongDAO {

    public List<LoaiPhong> getAll() {
        List<LoaiPhong> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;
        try {
            PreparedStatement pst = con.prepareStatement("SELECT * FROM LoaiPhong");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(new LoaiPhong(rs.getString("maLoaiPhong"), rs.getString("tenLoaiPhong"), "DangHoatDong"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public boolean them(LoaiPhong lp) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            PreparedStatement pst = con.prepareStatement(
                "INSERT INTO LoaiPhong (maLoaiPhong,tenLoaiPhong) VALUES (?,?)");
            pst.setString(1, lp.getMaLoaiPhong());
            pst.setString(2, lp.getTenLoaiPhong());
            return pst.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean sua(LoaiPhong lp) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            PreparedStatement pst = con.prepareStatement(
                "UPDATE LoaiPhong SET tenLoaiPhong=? WHERE maLoaiPhong=?");
            pst.setString(1, lp.getTenLoaiPhong());
            pst.setString(2, lp.getMaLoaiPhong());
            return pst.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean xoa(String maLoaiPhong) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            PreparedStatement pst = con.prepareStatement("DELETE FROM LoaiPhong WHERE maLoaiPhong=?");
            pst.setString(1, maLoaiPhong);
            return pst.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
}
