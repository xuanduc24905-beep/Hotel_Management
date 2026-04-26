package com.lotuslaverne.dao;

import com.lotuslaverne.entity.KhachHang;
import com.lotuslaverne.util.ConnectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class KhachHangDAO {

    public List<KhachHang> getAll() {
        List<KhachHang> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;
        try {
            PreparedStatement pst = con.prepareStatement(
                "SELECT maKH,hoTenKH,soDienThoai,cmnd,gioiTinh,ngaySinh,diaChi,quocTich "
                + "FROM KhachHang ORDER BY hoTenKH");
            ResultSet rs = pst.executeQuery();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            while (rs.next()) {
                KhachHang kh = new KhachHang();
                kh.setMaKH       (rs.getString("maKH"));
                kh.setHoTenKH    (rs.getString("hoTenKH"));
                kh.setSoDienThoai(rs.getString("soDienThoai"));
                kh.setCmnd       (rs.getString("cmnd"));
                kh.setGioiTinh   (rs.getBoolean("gioiTinh"));
                if (rs.getDate("ngaySinh") != null)
                    kh.setNgaySinh(sdf.format(rs.getDate("ngaySinh")));
                kh.setDiaChi  (rs.getString("diaChi"));
                kh.setQuocTich(rs.getString("quocTich") != null ? rs.getString("quocTich") : "Việt Nam");
                // email không có trong bảng KhachHang schema hiện tại — để trống
                list.add(kh);
            }
        } catch (Exception e) {
            System.err.println("Load Khách Hàng lỗi: " + e.getMessage());
        }
        return list;
    }

    /** Tự động sinh mã KH nếu null */
    public boolean themKhachHang(KhachHang kh) {
        if (kh.getMaKH() == null || kh.getMaKH().trim().isEmpty()) {
            kh.setMaKH("KH" + (System.currentTimeMillis() % 100000));
        }
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            PreparedStatement pst = con.prepareStatement(
                "INSERT INTO KhachHang (maKH,hoTenKH,cmnd,soDienThoai,gioiTinh,diaChi,quocTich) "
                + "VALUES (?,?,?,?,?,?,?)");
            pst.setString (1, kh.getMaKH());
            pst.setString (2, kh.getHoTenKH());
            pst.setString (3, kh.getCmnd() != null ? kh.getCmnd() : "");
            pst.setString (4, kh.getSoDienThoai());
            pst.setBoolean(5, kh.isGioiTinh());
            pst.setString (6, kh.getDiaChi());
            pst.setString (7, kh.getQuocTich() != null ? kh.getQuocTich() : "Việt Nam");
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Cập nhật thông tin liên lạc đầy đủ */
    public boolean suaKhachHang(KhachHang kh) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            PreparedStatement pst = con.prepareStatement(
                "UPDATE KhachHang SET hoTenKH=?,soDienThoai=?,cmnd=?,gioiTinh=?,diaChi=?,quocTich=? "
                + "WHERE maKH=?");
            pst.setString (1, kh.getHoTenKH());
            pst.setString (2, kh.getSoDienThoai());
            pst.setString (3, kh.getCmnd() != null ? kh.getCmnd() : "");
            pst.setBoolean(4, kh.isGioiTinh());
            pst.setString (5, kh.getDiaChi());
            pst.setString (6, kh.getQuocTich() != null ? kh.getQuocTich() : "Việt Nam");
            pst.setString (7, kh.getMaKH());
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
