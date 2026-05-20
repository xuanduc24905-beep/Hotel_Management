package com.lotuslaverne.dao;

import com.lotuslaverne.entity.HoaDon;
import com.lotuslaverne.util.ConnectDB;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
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

    /**
     * Lấy toàn bộ thông tin cần thiết để xuất PDF cho một hóa đơn.
     * Trả về Object[] gồm:
     * [0] maPDP, [1] tenKH, [2] maPhong, [3] tenPhong, [4] loaiPhong,
     * [5] tgNhan(String), [6] tgTra(String), [7] soNgay(long), [8] donGia(double),
     * [9] tamTinhPhong(double), [10] khuyenMai(double), [11] tongTien(double),
     * [12] phuongThuc(String), [13] ghiChu(String), [14] tenNV(String),
     * [15] dvList(List<Object[]> — {tenDV, soLuong, donGia, thanhTien}),
     * [16] phatSinhDV(double).
     * Trả về null nếu không tìm thấy hoặc lỗi.
     */
    public Object[] getChiTietForPdf(String maHD) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return null;

        String sql = "SELECT hd.maHoaDon, hd.maPhieuDatPhong, hd.tienKhuyenMai, hd.tienThanhToan, "
                + "hd.phuongThucThanhToan, hd.ghiChu, hd.maNhanVienLap, "
                + "kh.hoTenKH, ct.maPhong, p.tenPhong, lp.tenLoaiPhong, ct.donGia, "
                + "pdp.thoiGianNhanThucTe, pdp.thoiGianNhanDuKien, pdp.thoiGianTraThucTe "
                + "FROM HoaDon hd "
                + "JOIN PhieuDatPhong pdp ON hd.maPhieuDatPhong = pdp.maPhieuDatPhong "
                + "JOIN KhachHang kh ON pdp.maKhachHang = kh.maKH "
                + "JOIN ChiTietPhieuDatPhong ct ON pdp.maPhieuDatPhong = ct.maPhieuDatPhong "
                + "JOIN Phong p ON ct.maPhong = p.maPhong "
                + "JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong "
                + "WHERE hd.maHoaDon = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maHD);
            try (ResultSet rs = pst.executeQuery()) {
                if (!rs.next()) return null;

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                String maPDP     = rs.getString("maPhieuDatPhong");
                String tenKH     = rs.getString("hoTenKH");
                String maPhong   = rs.getString("maPhong");
                String tenPhong  = rs.getString("tenPhong");
                String loaiPhong = rs.getString("tenLoaiPhong");
                double donGia    = rs.getDouble("donGia");
                double khuyenMai = rs.getDouble("tienKhuyenMai");
                double tongTien  = rs.getDouble("tienThanhToan");
                String phuongThuc = rs.getString("phuongThucThanhToan");
                String ghiChu    = rs.getString("ghiChu") != null ? rs.getString("ghiChu") : "";
                String tenNV     = rs.getString("maNhanVienLap");

                Timestamp tsNhan = rs.getTimestamp("thoiGianNhanThucTe");
                if (tsNhan == null) tsNhan = rs.getTimestamp("thoiGianNhanDuKien");
                Timestamp tsTra  = rs.getTimestamp("thoiGianTraThucTe");
                String tgNhan = tsNhan != null ? sdf.format(tsNhan) : "N/A";
                String tgTra  = tsTra  != null ? sdf.format(tsTra)  : "N/A";
                long soNgay = 1;
                if (tsNhan != null && tsTra != null) {
                    long ms = tsTra.getTime() - tsNhan.getTime();
                    soNgay = Math.max(1, ms / (24 * 3600000L));
                }
                double tamTinhPhong = donGia * soNgay;

                // Dịch vụ phát sinh
                double phatSinhDV = 0;
                List<Object[]> dvList = new ArrayList<>();
                String sqlDV = "SELECT dv.tenDichVu, ctdv.soLuong, dv.donGia, "
                        + "(ctdv.soLuong * dv.donGia) AS thanhTien "
                        + "FROM ChiTietDichVu ctdv JOIN DichVu dv ON ctdv.maDichVu = dv.maDichVu "
                        + "WHERE ctdv.maPhieuDatPhong = ?";
                try (PreparedStatement pst2 = con.prepareStatement(sqlDV)) {
                    pst2.setString(1, maPDP);
                    try (ResultSet rs2 = pst2.executeQuery()) {
                        DecimalFormat df = new DecimalFormat("#,###");
                        while (rs2.next()) {
                            double tt = rs2.getDouble("thanhTien");
                            phatSinhDV += tt;
                            dvList.add(new Object[]{
                                rs2.getString("tenDichVu"),
                                String.valueOf(rs2.getInt("soLuong")),
                                df.format(rs2.getDouble("donGia")),
                                df.format(tt)
                            });
                        }
                    }
                }

                return new Object[]{
                    maPDP, tenKH, maPhong, tenPhong, loaiPhong,
                    tgNhan, tgTra, soNgay, donGia, tamTinhPhong,
                    khuyenMai, tongTien, phuongThuc, ghiChu, tenNV,
                    dvList, phatSinhDV
                };
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
