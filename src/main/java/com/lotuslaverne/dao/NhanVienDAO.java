package com.lotuslaverne.dao;

import com.lotuslaverne.entity.NhanVien;
import com.lotuslaverne.util.ConnectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class NhanVienDAO {

    public List<NhanVien> getAll() {
        List<NhanVien> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;
        try {
            String sql = "SELECT * FROM NhanVien";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(new NhanVien(
                    rs.getString("maNhanVien"),
                    rs.getString("hoTenNhanVien"), // Cần khớp với DB
                    rs.getString("soDienThoai"),
                    rs.getString("vaiTro")
                ));
            }
        } catch (Exception e) {
            System.err.println("Load Nhân Viên lỗi (Có thể sai tên cột DB): " + e.getMessage());
        }
        return list;
    }

    public boolean themNhanVien(NhanVien nv) {
        Connection con = ConnectDB.getInstance().getConnection();
        try {
            String sql = "INSERT INTO NhanVien (maNhanVien, hoTenNhanVien, soDienThoai, vaiTro) VALUES (?, ?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, nv.getMaNhanVien());
            pst.setString(2, nv.getTenNhanVien());
            pst.setString(3, nv.getSoDienThoai());
            pst.setString(4, nv.getVaiTro());
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean suaNhanVien(NhanVien nv) {
        Connection con = ConnectDB.getInstance().getConnection();
        try {
            String sql = "UPDATE NhanVien SET hoTenNhanVien = ?, soDienThoai = ?, vaiTro = ? WHERE maNhanVien = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, nv.getTenNhanVien());
            pst.setString(2, nv.getSoDienThoai());
            pst.setString(3, nv.getVaiTro());
            pst.setString(4, nv.getMaNhanVien());
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean xoaNhanVien(String maNhanVien) {
        Connection con = ConnectDB.getInstance().getConnection();
        try {
            String sql = "DELETE FROM NhanVien WHERE maNhanVien = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, maNhanVien);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Không thể xóa do ràng buộc FK, hoặc lỗi DB: " + e.getMessage());
            return false;
        }
    }
}
