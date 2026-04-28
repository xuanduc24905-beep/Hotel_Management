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

    private String lastError;

    /** Lấy thông báo lỗi cuối cùng (null nếu không có lỗi) */
    public String getLastError() { return lastError; }

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
        if (con == null) {
            lastError = "Không kết nối được CSDL!";
            return false;
        }
        try {
            PreparedStatement pst = con.prepareStatement(
                "INSERT INTO KhachHang (maKH,hoTenKH,cmnd,soDienThoai,gioiTinh,ngaySinh,diaChi,quocTich) "
                + "VALUES (?,?,?,?,?,?,?,?)");
            pst.setString (1, kh.getMaKH());
            pst.setString (2, kh.getHoTenKH());
            pst.setString (3, kh.getCmnd() != null ? kh.getCmnd() : "");
            pst.setString (4, kh.getSoDienThoai());
            pst.setBoolean(5, kh.isGioiTinh());
            // Xử lý ngày sinh: chuyển String -> Date nếu có
            if (kh.getNgaySinh() != null && !kh.getNgaySinh().trim().isEmpty()) {
                try {
                    java.util.Date d = new SimpleDateFormat("dd/MM/yyyy").parse(kh.getNgaySinh());
                    pst.setDate(6, new java.sql.Date(d.getTime()));
                } catch (Exception pe) {
                    pst.setNull(6, java.sql.Types.DATE);
                }
            } else {
                pst.setNull(6, java.sql.Types.DATE);
            }
            pst.setString (7, kh.getDiaChi());
            pst.setString (8, kh.getQuocTich() != null ? kh.getQuocTich() : "Việt Nam");
            lastError = null;
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("UQ_KhachHang_CMND")) {
                lastError = "CCCD/Hộ chiếu đã tồn tại trong hệ thống!";
            } else if (msg != null && msg.contains("UQ_KhachHang_SDT")) {
                lastError = "Số điện thoại đã tồn tại trong hệ thống!";
            } else if (msg != null && msg.contains("PK_KhachHang")) {
                lastError = "Mã khách hàng đã tồn tại!";
            } else {
                lastError = "Lỗi DB: " + msg;
            }
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
