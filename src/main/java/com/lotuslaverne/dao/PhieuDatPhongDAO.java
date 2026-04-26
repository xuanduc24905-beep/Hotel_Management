package com.lotuslaverne.dao;

import com.lotuslaverne.entity.PhieuDatPhong;
import com.lotuslaverne.util.ConnectDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PhieuDatPhongDAO {

    public boolean lapPhieuDat(PhieuDatPhong pdp) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            // Dùng SP_TaoPhieuDatPhong để kiểm tra phòng trống + tránh trùng lịch
            // Fallback: INSERT thẳng (dùng khi gọi từ dialog nhanh)
            PreparedStatement pst = con.prepareStatement(
                "INSERT INTO PhieuDatPhong "
                + "(maPhieuDatPhong,ngayDat,maKhachHang,maNhanVien,soNguoi,"
                + "thoiGianNhanDuKien,thoiGianTraDuKien,hinhThucDat,trangThai,ghiChu) "
                + "VALUES (?,GETDATE(),?,?,?,?,?,N'TrucTiep',N'DaDat',?)");
            pst.setString(1, pdp.getMaPhieuDatPhong());
            pst.setString(2, pdp.getMaKhachHang());
            pst.setString(3, pdp.getMaNhanVien());
            pst.setInt   (4, pdp.getSoNguoi());
            pst.setTimestamp(5, pdp.getThoiGianNhanDuKien());
            pst.setTimestamp(6, pdp.getThoiGianTraDuKien());
            pst.setString(7, pdp.getGhiChu());
            return pst.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /** Gọi Stored Procedure SP_CheckIn → cập nhật thoiGianNhanThucTe + trangThai='DaCheckIn' + Phong='PhongDat' */
    public boolean checkIn(String maPhieuDatPhong) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            CallableStatement cs = con.prepareCall("{call SP_CheckIn(?)}");
            cs.setString(1, maPhieuDatPhong);
            cs.execute();
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /** Cập nhật trangThai phiếu sang DaCheckOut + thời gian trả thực tế */
    public boolean checkOut(String maPhieuDatPhong) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            PreparedStatement pst = con.prepareStatement(
                "UPDATE PhieuDatPhong SET trangThai=N'DaCheckOut', thoiGianTraThucTe=GETDATE() "
                + "WHERE maPhieuDatPhong=?");
            pst.setString(1, maPhieuDatPhong);
            return pst.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public List<PhieuDatPhong> getAll() {
        List<PhieuDatPhong> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;
        try {
            PreparedStatement pst = con.prepareStatement(
                "SELECT * FROM PhieuDatPhong ORDER BY ngayDat DESC");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    /** Lấy các phiếu chờ check-in (trangThai = 'DaDat') */
    public List<PhieuDatPhong> getChuaCheckIn() {
        List<PhieuDatPhong> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;
        try {
            PreparedStatement pst = con.prepareStatement(
                "SELECT * FROM PhieuDatPhong WHERE trangThai = N'DaDat' "
                + "ORDER BY thoiGianNhanDuKien ASC");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    /** Lấy các phiếu đang lưu trú (trangThai = 'DaCheckIn') */
    public List<PhieuDatPhong> getDangSuDung() {
        List<PhieuDatPhong> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;
        try {
            PreparedStatement pst = con.prepareStatement(
                "SELECT * FROM PhieuDatPhong WHERE trangThai = N'DaCheckIn' "
                + "ORDER BY thoiGianNhanThucTe DESC");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    private PhieuDatPhong mapRow(ResultSet rs) throws SQLException {
        PhieuDatPhong p = new PhieuDatPhong();
        p.setMaPhieuDatPhong(rs.getString("maPhieuDatPhong"));
        p.setNgayDat(rs.getTimestamp("ngayDat"));
        p.setMaKhachHang(rs.getString("maKhachHang"));
        p.setMaNhanVien(rs.getString("maNhanVien"));
        p.setSoNguoi(rs.getInt("soNguoi"));
        p.setThoiGianNhanDuKien(rs.getTimestamp("thoiGianNhanDuKien"));
        p.setThoiGianNhanThucTe(rs.getTimestamp("thoiGianNhanThucTe"));
        p.setThoiGianTraDuKien(rs.getTimestamp("thoiGianTraDuKien"));
        p.setThoiGianTraThucTe(rs.getTimestamp("thoiGianTraThucTe"));
        p.setGhiChu(rs.getString("ghiChu"));
        return p;
    }
}
