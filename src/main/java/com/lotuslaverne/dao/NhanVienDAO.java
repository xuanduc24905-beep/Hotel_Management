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
                NhanVien nv = new NhanVien(
                    rs.getString("maNhanVien"),
                    rs.getString("tenNhanVien"),
                    rs.getString("soDienThoai"),
                    rs.getString("vaiTro")
                );
                try { nv.setCaLamViec(rs.getString("caLamViec")); } catch (Exception ignored) {}
                try { nv.setCccd(rs.getString("cccd")); } catch (Exception ignored) {}
                try { nv.setEmail(rs.getString("email")); } catch (Exception ignored) {}
                try { nv.setDiaChi(rs.getString("diaChi")); } catch (Exception ignored) {}
                try { nv.setNgaySinh(rs.getDate("ngaySinh")); } catch (Exception ignored) {}
                try { nv.setNgayBatDauLam(rs.getDate("ngayBatDauLam")); } catch (Exception ignored) {}
                try { nv.setNgayKetThucHopDong(rs.getDate("ngayKetThucHopDong")); } catch (Exception ignored) {}
                list.add(nv);
            }
        } catch (Exception e) {
            System.err.println("Load Nhân Viên lỗi: " + e.getMessage());
        }
        return list;
    }

    public boolean themNhanVien(NhanVien nv) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            String sql = "INSERT INTO NhanVien (maNhanVien, tenNhanVien, soDienThoai, cccd, email, diaChi, vaiTro, caLamViec, ngayBatDauLam) "
                       + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, GETDATE())";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, nv.getMaNhanVien());
            pst.setString(2, nv.getTenNhanVien());
            pst.setString(3, nv.getSoDienThoai());
            // cccd: nếu trống thì set NULL (tránh UNIQUE constraint conflict khi nhiều dòng null)
            String cccd = nv.getCccd();
            if (cccd != null && cccd.trim().isEmpty()) cccd = null;
            pst.setString(4, cccd);
            // email
            String email = nv.getEmail();
            if (email != null && email.trim().isEmpty()) email = null;
            pst.setString(5, email);
            // diaChi
            String diaChi = nv.getDiaChi();
            if (diaChi != null && diaChi.trim().isEmpty()) diaChi = null;
            pst.setString(6, diaChi);
            pst.setString(7, nv.getVaiTro());
            pst.setString(8, nv.getCaLamViec());
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Thêm nhân viên lỗi: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi thêm nhân viên: " + e.getMessage(), e);
        }
    }

    public boolean suaNhanVien(NhanVien nv) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            String sql = "UPDATE NhanVien SET tenNhanVien=?, soDienThoai=?, cccd=?, email=?, diaChi=?, vaiTro=?, caLamViec=? WHERE maNhanVien=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, nv.getTenNhanVien());
            pst.setString(2, nv.getSoDienThoai());
            String cccd = nv.getCccd();
            if (cccd != null && cccd.trim().isEmpty()) cccd = null;
            pst.setString(3, cccd);
            String email = nv.getEmail();
            if (email != null && email.trim().isEmpty()) email = null;
            pst.setString(4, email);
            String diaChi = nv.getDiaChi();
            if (diaChi != null && diaChi.trim().isEmpty()) diaChi = null;
            pst.setString(5, diaChi);
            pst.setString(6, nv.getVaiTro());
            pst.setString(7, nv.getCaLamViec());
            pst.setString(8, nv.getMaNhanVien());
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Sửa nhân viên lỗi: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi sửa nhân viên: " + e.getMessage(), e);
        }
    }

    public boolean xoaNhanVien(String maNhanVien) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            // Kiểm tra ràng buộc FK: TaiKhoan, PhieuDatPhong, HoaDon
            String checkSQL = "SELECT COUNT(*) FROM TaiKhoan WHERE maNhanVien = ?";
            try (PreparedStatement chk = con.prepareStatement(checkSQL)) {
                chk.setString(1, maNhanVien);
                try (ResultSet rs = chk.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.err.println("Không thể xóa: nhân viên còn tài khoản liên kết.");
                        return false;
                    }
                }
            }
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
