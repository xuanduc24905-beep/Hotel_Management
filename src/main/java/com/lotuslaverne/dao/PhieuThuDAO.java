package com.lotuslaverne.dao;

import com.lotuslaverne.util.ConnectDB;
import java.sql.*;

public class PhieuThuDAO {

    /** Sinh mã phiếu thu dạng "PT" + 8 ký tự UUID uppercase. */
    public String generateMaPT() {
        return "PT" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    /**
     * Danh sách mã PhieuDatPhong có trangThai IN ('DaDat','DaCheckIn').
     * Mỗi Object[]: {maPhieuDatPhong}.
     */
    public java.util.List<String> loadPhieuChuaCheckIn() {
        java.util.List<String> list = new java.util.ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;
        String sql = "SELECT maPhieuDatPhong FROM PhieuDatPhong "
                   + "WHERE trangThai IN ('DaDat','DaCheckIn') ORDER BY ngayDat DESC";
        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) list.add(rs.getString("maPhieuDatPhong"));
        } catch (Exception ignored) {}
        return list;
    }

    /**
     * Trả về danh sách phòng của phiếu đặt phòng dưới dạng chuỗi ngăn cách bởi ", ".
     * Thử STRING_AGG trước (SQL Server 2017+), fallback về nối thủ công.
     */
    public String getPhongsByPhieu(String maPDP) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return "—";
        // Thử STRING_AGG
        try (PreparedStatement pst = con.prepareStatement(
                "SELECT STRING_AGG(maPhong, ', ') AS phong FROM ChiTietPhieuDatPhong WHERE maPhieuDatPhong=?")) {
            pst.setString(1, maPDP);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    String val = rs.getString("phong");
                    if (val != null) return val;
                }
            }
        } catch (Exception ignored) {}
        // Fallback nối thủ công
        StringBuilder sb = new StringBuilder();
        try (PreparedStatement pst = con.prepareStatement(
                "SELECT maPhong FROM ChiTietPhieuDatPhong WHERE maPhieuDatPhong=?")) {
            pst.setString(1, maPDP);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(rs.getString("maPhong"));
                }
            }
        } catch (Exception ignored) {}
        return sb.length() > 0 ? sb.toString() : "—";
    }

    /**
     * Tải tất cả phiếu thu (hiển thị trong bảng danh sách).
     * Mỗi Object[]: {maPhieuThu, maPhieuDatPhong, soTienCoc (formatted), phuongThucThanhToan, ngayThu, ghiChu}.
     */
    public java.util.List<Object[]> loadAllPhieuThu() {
        java.util.List<Object[]> list = new java.util.ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;
        String sql = "SELECT maPhieuThu, maPhieuDatPhong, soTienCoc, phuongThucThanhToan, ngayThu, ghiChu "
                   + "FROM PhieuThu ORDER BY ngayThu DESC";
        java.text.DecimalFormat money = new java.text.DecimalFormat("#,###");
        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("maPhieuThu"),
                    rs.getString("maPhieuDatPhong"),
                    money.format(rs.getDouble("soTienCoc")) + "đ",
                    rs.getString("phuongThucThanhToan"),
                    rs.getTimestamp("ngayThu") != null
                            ? rs.getTimestamp("ngayThu").toString().substring(0, 16) : "",
                    rs.getString("ghiChu") != null ? rs.getString("ghiChu") : ""
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    /** Kiểm tra phiếu đặt phòng đã có PhieuThu cọc chưa. */
    public boolean daDuocThuCoc(String maPhieuDatPhong) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try (PreparedStatement pst = con.prepareStatement(
                "SELECT COUNT(*) FROM PhieuThu WHERE maPhieuDatPhong=?")) {
            pst.setString(1, maPhieuDatPhong);
            ResultSet rs = pst.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (Exception ignored) { return false; }
    }

    public boolean taoPhieuThu(String maPhieuThu, String maPhieuDatPhong,
                                String maNhanVienLap, double soTienCoc,
                                String phuongThuc, String ghiChu) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            taoPhieuThuWithCon(con, maPhieuThu, maPhieuDatPhong,
                    maNhanVienLap, soTienCoc, phuongThuc, ghiChu);
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public void taoPhieuThuWithCon(Connection con, String maPhieuThu, String maPhieuDatPhong,
            String maNhanVienLap, double soTienCoc, String phuongThuc, String ghiChu)
            throws SQLException {
        try (PreparedStatement pst = con.prepareStatement(
                "INSERT INTO PhieuThu (maPhieuThu,maHoaDon,maNhanVienLap,maPhieuDatPhong,"
                + "soTienCoc,ngayThu,phuongThucThanhToan,ghiChu) VALUES (?,NULL,?,?,?,GETDATE(),?,?)")) {
            pst.setString(1, maPhieuThu);
            pst.setString(2, maNhanVienLap);
            pst.setString(3, maPhieuDatPhong);
            pst.setDouble(4, soTienCoc);
            pst.setString(5, phuongThuc);
            pst.setString(6, ghiChu);
            pst.executeUpdate();
        }
    }
}
