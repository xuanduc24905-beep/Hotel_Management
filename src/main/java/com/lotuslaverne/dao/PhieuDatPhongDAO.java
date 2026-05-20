package com.lotuslaverne.dao;

import com.lotuslaverne.entity.PhieuDatPhong;
import com.lotuslaverne.util.ConnectDB;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PhieuDatPhongDAO {

    /**
     * Trả về true nếu phòng không có lịch đặt nào giao với [ngayVao, ngayTra).
     * Business rule: cho phép cùng ngày — khách cũ trả 12h, khách mới vào 14h.
     * Overlap: NOT (thoiGianTraDuKien <= ngayVao OR thoiGianNhanDuKien >= ngayTra)
     */
    public boolean isPhongTrong(String maPhong, LocalDate ngayVao, LocalDate ngayTra) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return true;
        try (PreparedStatement pst = con.prepareStatement(
                "SELECT COUNT(*) FROM ChiTietPhieuDatPhong ct"
                + " JOIN PhieuDatPhong pdp ON pdp.maPhieuDatPhong = ct.maPhieuDatPhong"
                + " WHERE ct.maPhong = ?"
                + " AND pdp.trangThai NOT IN (N'HuyDat', N'DaCheckOut')"
                + " AND NOT (pdp.thoiGianTraDuKien <= ? OR pdp.thoiGianNhanDuKien >= ?)")) {
            pst.setString(1, maPhong);
            pst.setTimestamp(2, Timestamp.valueOf(ngayVao.atStartOfDay()));
            pst.setTimestamp(3, Timestamp.valueOf(ngayTra.atStartOfDay()));
            ResultSet rs = pst.executeQuery();
            return rs.next() && rs.getInt(1) == 0;
        } catch (Exception e) { e.printStackTrace(); return true; }
    }

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

    /**
     * Lấy phiếu chờ check-in JOIN sẵn KhachHang + ChiTietPhieuDatPhong — tránh N+1 query.
     * Mỗi Object[]: {maPhieuDatPhong, hoTenKH, maNhanVien, soNguoi, thoiGianNhanDuKien, thoiGianTraDuKien, ghiChu, maPhong}
     */
    public List<Object[]> getChuaCheckInJoined() {
        List<Object[]> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;
        try {
            PreparedStatement pst = con.prepareStatement(
                "SELECT pdp.maPhieuDatPhong, kh.hoTenKH, pdp.maNhanVien, pdp.soNguoi,"
                + " pdp.thoiGianNhanDuKien, pdp.thoiGianTraDuKien, pdp.ghiChu,"
                + " ISNULL(ct.maPhong,'')"
                + " FROM PhieuDatPhong pdp"
                + " JOIN KhachHang kh ON kh.maKH = pdp.maKhachHang"
                + " LEFT JOIN ChiTietPhieuDatPhong ct ON ct.maPhieuDatPhong = pdp.maPhieuDatPhong"
                + " WHERE pdp.trangThai = N'DaDat'"
                + " ORDER BY pdp.thoiGianNhanDuKien ASC");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) list.add(new Object[]{
                rs.getString(1), rs.getString(2), rs.getString(3),
                rs.getInt(4), rs.getTimestamp(5), rs.getTimestamp(6), rs.getString(7), rs.getString(8)
            });
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Load danh sách đặt phòng cho calendar gantt chart.
     * Lọc theo khoảng [from, to), loại bỏ trạng thái DaCheckOut và HuyDat.
     * Mỗi Object[]: {maPhieuDatPhong, maPhong, hoTenKH, ngayNhan(str), ngayTra(str), trangThai}.
     */
    public List<String[]> loadBookingsForCalendar(java.time.LocalDate from, java.time.LocalDate to) {
        List<String[]> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;
        String sql = "SELECT pdp.maPhieuDatPhong,ct.maPhong,kh.hoTenKH,"
                   + "pdp.thoiGianNhanDuKien,pdp.thoiGianTraDuKien,pdp.trangThai "
                   + "FROM PhieuDatPhong pdp "
                   + "JOIN ChiTietPhieuDatPhong ct ON ct.maPhieuDatPhong=pdp.maPhieuDatPhong "
                   + "JOIN KhachHang kh ON kh.maKH=pdp.maKhachHang "
                   + "WHERE pdp.trangThai NOT IN ('DaCheckOut','HuyDat') "
                   + "AND pdp.thoiGianNhanDuKien<? AND pdp.thoiGianTraDuKien>?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setTimestamp(1, Timestamp.valueOf(to.atStartOfDay()));
            pst.setTimestamp(2, Timestamp.valueOf(from.atStartOfDay()));
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) list.add(new String[]{
                    rs.getString(1), rs.getString(2), rs.getString(3),
                    rs.getTimestamp(4) != null ? rs.getTimestamp(4).toString().substring(0, 10) : "",
                    rs.getTimestamp(5) != null ? rs.getTimestamp(5).toString().substring(0, 10) : "",
                    rs.getString(6)
                });
            }
        } catch (Exception ignored) {}
        return list;
    }

    /**
     * Load lịch sử đặt phòng cho bảng hiển thị (TOP 100, order by ngày nhận DESC).
     * Mỗi Object[]: {maPhieuDatPhong, hoTenKH, maPhong, ngayNhan(str), ngayTra(str), trangThai, ghiChu}.
     */
    public List<Object[]> loadHistoryForView() {
        List<Object[]> result = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return result;
        String sql = "SELECT TOP 100 pdp.maPhieuDatPhong, kh.hoTenKH, "
                   + "ct.maPhong, "
                   + "CONVERT(varchar,pdp.thoiGianNhanDuKien,103) AS ngayNhan, "
                   + "CONVERT(varchar,pdp.thoiGianTraDuKien,103) AS ngayTra, "
                   + "pdp.trangThai, pdp.ghiChu "
                   + "FROM PhieuDatPhong pdp "
                   + "LEFT JOIN KhachHang kh ON kh.maKH = pdp.maKhachHang "
                   + "LEFT JOIN ChiTietPhieuDatPhong ct ON ct.maPhieuDatPhong = pdp.maPhieuDatPhong "
                   + "ORDER BY pdp.thoiGianNhanDuKien DESC";
        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                result.add(new Object[]{
                    rs.getString(1),
                    rs.getString(2) != null ? rs.getString(2) : "—",
                    rs.getString(3) != null ? rs.getString(3) : "—",
                    rs.getString(4) != null ? rs.getString(4) : "—",
                    rs.getString(5) != null ? rs.getString(5) : "—",
                    rs.getString(6) != null ? rs.getString(6) : "—",
                    rs.getString(7) != null ? rs.getString(7) : ""
                });
            }
        } catch (Exception ignored) {}
        return result;
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

    /** Lấy trangThai của phiếu. Trả về "" nếu không tìm thấy. */
    public String getTrangThai(String maPDP) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return "";
        try (PreparedStatement pst = con.prepareStatement(
                "SELECT trangThai FROM PhieuDatPhong WHERE maPhieuDatPhong=?")) {
            pst.setString(1, maPDP);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        } catch (Exception ignored) {}
        return "";
    }

    /** Lấy maPhong từ ChiTietPhieuDatPhong theo maPDP. Trả về "" nếu không tìm thấy. */
    public String getMaPhong(String maPDP) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return "";
        try (PreparedStatement pst = con.prepareStatement(
                "SELECT maPhong FROM ChiTietPhieuDatPhong WHERE maPhieuDatPhong=?")) {
            pst.setString(1, maPDP);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getString("maPhong");
            }
        } catch (Exception ignored) {}
        return "";
    }

    /**
     * Lấy thông tin cần thiết để xuất PDF hóa đơn.
     * Trả về Object[]{tenKH, tenPhong, tenLoaiPhong, tsNhan(Timestamp), tsTra(Timestamp), donGia(double)}.
     * Trả về null nếu không tìm thấy.
     */
    public Object[] getInfoForPdf(String maPDP) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return null;
        String sql = "SELECT kh.hoTenKH, p.tenPhong, lp.tenLoaiPhong, "
                + "pdp.thoiGianNhanThucTe, pdp.thoiGianNhanDuKien, pdp.thoiGianTraThucTe, ct.donGia "
                + "FROM PhieuDatPhong pdp "
                + "JOIN KhachHang kh ON pdp.maKhachHang = kh.maKH "
                + "JOIN ChiTietPhieuDatPhong ct ON pdp.maPhieuDatPhong = ct.maPhieuDatPhong "
                + "JOIN Phong p ON ct.maPhong = p.maPhong "
                + "JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong "
                + "WHERE pdp.maPhieuDatPhong = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maPDP);
            try (ResultSet rs = pst.executeQuery()) {
                if (!rs.next()) return null;
                java.sql.Timestamp tsNhan = rs.getTimestamp("thoiGianNhanThucTe");
                if (tsNhan == null) tsNhan = rs.getTimestamp("thoiGianNhanDuKien");
                return new Object[]{
                    rs.getString("hoTenKH"),
                    rs.getString("tenPhong"),
                    rs.getString("tenLoaiPhong"),
                    tsNhan,
                    rs.getTimestamp("thoiGianTraThucTe"),
                    rs.getDouble("donGia")
                };
            }
        } catch (Exception ignored) {}
        return null;
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
        p.setTrangThai(rs.getString("trangThai"));
        p.setHinhThucDat(rs.getString("hinhThucDat"));
        p.setGhiChu(rs.getString("ghiChu"));
        return p;
    }
}
