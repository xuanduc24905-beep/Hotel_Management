package com.lotuslaverne.dao;

import com.lotuslaverne.entity.HoaDon;
import com.lotuslaverne.util.ConnectDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HoaDonDAO {

    public boolean taoHoaDon(HoaDon hd) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            PreparedStatement pst = con.prepareStatement(
                "INSERT INTO HoaDon (maHoaDon,ngayLap,maNhanVienLap,maPhieuDatPhong,ngayThanhToan,tienKhuyenMai,tienThanhToan,phuongThucThanhToan,ghiChu) VALUES (?,GETDATE(),?,?,GETDATE(),?,?,?,?)");
            pst.setString(1, hd.getMaHoaDon());
            pst.setString(2, hd.getMaNhanVienLap());
            pst.setString(3, hd.getMaPhieuDatPhong());
            pst.setDouble(4, hd.getTienKhuyenMai());
            pst.setDouble(5, hd.getTienThanhToan());
            pst.setString(6, hd.getPhuongThucThanhToan());
            pst.setString(7, hd.getGhiChu());
            return pst.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public List<HoaDon> getAll() {
        List<HoaDon> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;
        try {
            PreparedStatement pst = con.prepareStatement("SELECT * FROM HoaDon ORDER BY ngayLap DESC");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<HoaDon> timKiem(String keyword) {
        List<HoaDon> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;
        try {
            PreparedStatement pst = con.prepareStatement(
                "SELECT * FROM HoaDon WHERE maHoaDon LIKE ? OR maPhieuDatPhong LIKE ? OR maNhanVienLap LIKE ? ORDER BY ngayLap DESC");
            String like = "%" + keyword + "%";
            pst.setString(1, like); pst.setString(2, like); pst.setString(3, like);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    private HoaDon mapRow(ResultSet rs) throws SQLException {
        HoaDon hd = new HoaDon();
        hd.setMaHoaDon(rs.getString("maHoaDon"));
        hd.setNgayLap(rs.getTimestamp("ngayLap"));
        hd.setMaNhanVienLap(rs.getString("maNhanVienLap"));
        hd.setMaPhieuDatPhong(rs.getString("maPhieuDatPhong"));
        hd.setNgayThanhToan(rs.getTimestamp("ngayThanhToan"));
        hd.setTienKhuyenMai(rs.getDouble("tienKhuyenMai"));
        hd.setTienThanhToan(rs.getDouble("tienThanhToan"));
        hd.setPhuongThucThanhToan(rs.getString("phuongThucThanhToan"));
        hd.setGhiChu(rs.getString("ghiChu"));
        return hd;
    }
}
