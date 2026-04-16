package com.lotuslaverne.dao;

import com.lotuslaverne.entity.KhachHang;
import com.lotuslaverne.util.ConnectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class KhachHangDAO {

    public List<KhachHang> getAll() {
        List<KhachHang> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;
        try {
            String sql = "SELECT * FROM KhachHang";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(new KhachHang(
                    rs.getString("maKhachHang"),
                    rs.getString("hoTenKhachHang"),
                    rs.getString("soDienThoai"),
                    rs.getString("soCMND_CCCD")
                ));
            }
        } catch (Exception e) {
            System.err.println("Load Khách hàng lỗi: " + e.getMessage());
        }
        return list;
    }

    public boolean themKhachHang(KhachHang kh) {
        Connection con = ConnectDB.getInstance().getConnection();
        try {
            String sql = "INSERT INTO KhachHang (maKhachHang, hoTenKhachHang, soDienThoai, soCMND_CCCD) VALUES (?, ?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, kh.getMaKH());
            pst.setString(2, kh.getHoTenKH());
            pst.setString(3, kh.getSoDienThoai());
            pst.setString(4, kh.getCmnd());
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean suaKhachHang(KhachHang kh) {
        Connection con = ConnectDB.getInstance().getConnection();
        try {
            String sql = "UPDATE KhachHang SET hoTenKhachHang = ?, soDienThoai = ?, soCMND_CCCD = ? WHERE maKhachHang = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, kh.getHoTenKH());
            pst.setString(2, kh.getSoDienThoai());
            pst.setString(3, kh.getCmnd());
            pst.setString(4, kh.getMaKH());
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
