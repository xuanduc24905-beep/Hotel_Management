package com.lotuslaverne.dao;

import com.lotuslaverne.entity.Phong;
import com.lotuslaverne.util.ConnectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PhongDAO {

    /** Lấy tất cả phòng kèm tiện nghi, sức chứa, mô tả */
    public List<Phong> getAll() {
        List<Phong> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;
        try {
            PreparedStatement pst = con.prepareStatement("SELECT * FROM Phong ORDER BY maPhong");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            System.err.println("Load Phòng lỗi: " + e.getMessage());
        }
        return list;
    }

    /** Lọc phòng có chứa tiện nghi cụ thể (VD: "WiFi") */
    public List<Phong> timTheoTienNghi(String tienNghi) {
        List<Phong> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;
        try {
            PreparedStatement pst = con.prepareStatement(
                "SELECT * FROM Phong WHERE tienNghi LIKE ? ORDER BY maPhong");
            pst.setString(1, "%" + tienNghi + "%");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Thêm phòng mới với đầy đủ thông tin */
    public boolean themPhong(Phong p) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            PreparedStatement pst = con.prepareStatement(
                "INSERT INTO Phong (maPhong,tenPhong,maLoaiPhong,trangThai,tienNghi,soNguoiToiDa,moTa) "
                + "VALUES (?,?,?,?,?,?,?)");
            pst.setString(1, p.getMaPhong());
            pst.setString(2, p.getTenPhong());
            pst.setString(3, p.getMaLoaiPhong());
            pst.setString(4, p.getTrangThai() != null ? p.getTrangThai() : "PhongTrong");
            pst.setString(5, p.getTienNghi());
            pst.setInt   (6, p.getSoNguoiToiDa() > 0 ? p.getSoNguoiToiDa() : 2);
            pst.setString(7, p.getMoTa());
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Cập nhật toàn bộ thông tin phòng (trừ trangThai) */
    public boolean capNhatPhong(Phong p) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            PreparedStatement pst = con.prepareStatement(
                "UPDATE Phong SET tenPhong=?,maLoaiPhong=?,trangThai=?,"
                + "tienNghi=?,soNguoiToiDa=?,moTa=? WHERE maPhong=?");
            pst.setString(1, p.getTenPhong());
            pst.setString(2, p.getMaLoaiPhong());
            pst.setString(3, p.getTrangThai());
            pst.setString(4, p.getTienNghi());
            pst.setInt   (5, p.getSoNguoiToiDa());
            pst.setString(6, p.getMoTa());
            pst.setString(7, p.getMaPhong());
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Chỉ cập nhật trạng thái phòng */
    public boolean capNhatTrangThai(String maPhong, String trangThaiMoi) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            PreparedStatement pst = con.prepareStatement(
                "UPDATE Phong SET trangThai=? WHERE maPhong=?");
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
        if (con == null) return false;
        try {
            PreparedStatement pst = con.prepareStatement(
                "DELETE FROM Phong WHERE maPhong=?");
            pst.setString(1, maPhong);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Phong mapRow(ResultSet rs) throws Exception {
        Phong p = new Phong();
        p.setMaPhong     (rs.getString("maPhong"));
        p.setTenPhong    (rs.getString("tenPhong"));
        p.setMaLoaiPhong (rs.getString("maLoaiPhong"));
        p.setTrangThai   (rs.getString("trangThai"));
        // Đọc an toàn — nếu cột chưa tồn tại (DB chưa migrate) thì bỏ qua
        try { p.setTienNghi   (rs.getString("tienNghi")); } catch (Exception ignored) {}
        try { p.setSoNguoiToiDa(rs.getInt("soNguoiToiDa")); } catch (Exception ignored) {}
        try { p.setMoTa       (rs.getString("moTa")); } catch (Exception ignored) {}
        return p;
    }
}
