package com.lotuslaverne.service;

import com.lotuslaverne.util.ConnectDB;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Service tập trung toàn bộ SQL truy vấn báo cáo — View chỉ nhận data và render.
 */
public class BaoCaoService {

    /**
     * Trả về mảng [doanhThu, doanhThuTruoc, tongPhieu, soHuy, luotKhach, avgStay].
     */
    public double[] getStatCards(LocalDate from, LocalDate to) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return new double[6];

        Date sqlFrom = Date.valueOf(from);
        Date sqlTo   = Date.valueOf(to);

        double doanhThu = querySum(con,
            "SELECT ISNULL(SUM(tienThanhToan),0) FROM HoaDon "
            + "WHERE CAST(ngayLap AS DATE) BETWEEN ? AND ?",
            sqlFrom, sqlTo);

        long kDays = ChronoUnit.DAYS.between(from, to) + 1;
        double doanhThuTruoc = querySum(con,
            "SELECT ISNULL(SUM(tienThanhToan),0) FROM HoaDon "
            + "WHERE CAST(ngayLap AS DATE) BETWEEN ? AND ?",
            Date.valueOf(from.minusDays(kDays)), Date.valueOf(from.minusDays(1)));

        double tongPhieu = querySum(con,
            "SELECT COUNT(*) FROM PhieuDatPhong "
            + "WHERE CAST(thoiGianNhanDuKien AS DATE) BETWEEN ? AND ?",
            sqlFrom, sqlTo);

        double soHuy = querySum(con,
            "SELECT COUNT(*) FROM PhieuDatPhong "
            + "WHERE trangThai=N'HuyDat' AND CAST(thoiGianNhanDuKien AS DATE) BETWEEN ? AND ?",
            sqlFrom, sqlTo);

        double luotKhach = querySum(con,
            "SELECT COUNT(DISTINCT maKhachHang) FROM PhieuDatPhong "
            + "WHERE trangThai IN (N'DaCheckIn',N'DaCheckOut') "
            + "AND CAST(thoiGianNhanThucTe AS DATE) BETWEEN ? AND ?",
            sqlFrom, sqlTo);

        double avgStay = querySum(con,
            "SELECT ISNULL(AVG(CAST(DATEDIFF(day, thoiGianNhanThucTe, "
            + "ISNULL(thoiGianTraThucTe, GETDATE())) AS FLOAT)), 0) "
            + "FROM PhieuDatPhong "
            + "WHERE trangThai IN (N'DaCheckIn',N'DaCheckOut') "
            + "AND CAST(thoiGianNhanThucTe AS DATE) BETWEEN ? AND ?",
            sqlFrom, sqlTo);

        return new double[]{doanhThu, doanhThuTruoc, tongPhieu, soHuy, luotKhach, avgStay};
    }

    /**
     * Doanh thu từng ngày (≤31 ngày). Key = "dd/MM".
     */
    public Map<String, Double> getRevenueByDay(LocalDate from, LocalDate to) {
        Map<String, Double> result = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
        for (long i = 0; i <= ChronoUnit.DAYS.between(from, to); i++)
            result.put(from.plusDays(i).format(fmt), 0.0);

        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return result;

        String sql = "SELECT CAST(ngayLap AS DATE), ISNULL(SUM(tienThanhToan),0) "
                   + "FROM HoaDon "
                   + "WHERE CAST(ngayLap AS DATE) BETWEEN ? AND ? "
                   + "GROUP BY CAST(ngayLap AS DATE) ORDER BY 1";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setDate(1, Date.valueOf(from));
            pst.setDate(2, Date.valueOf(to));
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next())
                    result.put(rs.getDate(1).toLocalDate().format(fmt), rs.getDouble(2));
            }
        } catch (Exception ignored) {}
        return result;
    }

    /**
     * Doanh thu từng tháng (>31 ngày). Key = "MM/yyyy".
     */
    public Map<String, Double> getRevenueByMonth(LocalDate from, LocalDate to) {
        Map<String, Double> result = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/yyyy");
        LocalDate cur = from.withDayOfMonth(1);
        while (!cur.isAfter(to)) { result.put(cur.format(fmt), 0.0); cur = cur.plusMonths(1); }

        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return result;

        String sql = "SELECT YEAR(ngayLap), MONTH(ngayLap), ISNULL(SUM(tienThanhToan),0) "
                   + "FROM HoaDon "
                   + "WHERE CAST(ngayLap AS DATE) BETWEEN ? AND ? "
                   + "GROUP BY YEAR(ngayLap), MONTH(ngayLap) ORDER BY 1, 2";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setDate(1, Date.valueOf(from));
            pst.setDate(2, Date.valueOf(to));
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next())
                    result.put(String.format("%02d/%04d", rs.getInt(2), rs.getInt(1)), rs.getDouble(3));
            }
        } catch (Exception ignored) {}
        return result;
    }

    /**
     * Top 5 loại phòng được đặt nhiều nhất. Key = tenLoaiPhong, Value = count.
     */
    public Map<String, Integer> getTopLoaiPhong(LocalDate from, LocalDate to) {
        Map<String, Integer> result = new LinkedHashMap<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return result;

        String sql = "SELECT TOP 5 lp.tenLoaiPhong, COUNT(*) "
                   + "FROM ChiTietPhieuDatPhong ct "
                   + "JOIN Phong p ON p.maPhong = ct.maPhong "
                   + "JOIN LoaiPhong lp ON lp.maLoaiPhong = p.maLoaiPhong "
                   + "JOIN PhieuDatPhong pdp ON pdp.maPhieuDatPhong = ct.maPhieuDatPhong "
                   + "WHERE CAST(pdp.thoiGianNhanDuKien AS DATE) BETWEEN ? AND ? "
                   + "GROUP BY lp.tenLoaiPhong ORDER BY 2 DESC";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setDate(1, Date.valueOf(from));
            pst.setDate(2, Date.valueOf(to));
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) result.put(rs.getString(1), rs.getInt(2));
            }
        } catch (Exception ignored) {}
        return result;
    }

    /**
     * Doanh thu theo hình thức thanh toán. Key = phuongThucThanhToan, Value = tổng.
     */
    public Map<String, Double> getRevenueByPayment(LocalDate from, LocalDate to) {
        Map<String, Double> result = new LinkedHashMap<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return result;

        String sql = "SELECT ISNULL(phuongThucThanhToan, N'Khác'), ISNULL(SUM(tienThanhToan),0) "
                   + "FROM HoaDon "
                   + "WHERE CAST(ngayLap AS DATE) BETWEEN ? AND ? "
                   + "GROUP BY phuongThucThanhToan ORDER BY 2 DESC";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setDate(1, Date.valueOf(from));
            pst.setDate(2, Date.valueOf(to));
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) result.put(rs.getString(1), rs.getDouble(2));
            }
        } catch (Exception ignored) {}
        return result;
    }

    // ── helper ──────────────────────────────────────────────────────────────

    private double querySum(Connection con, String sql, Date from, Date to) {
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setDate(1, from);
            pst.setDate(2, to);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (Exception ignored) {}
        return 0;
    }
}
