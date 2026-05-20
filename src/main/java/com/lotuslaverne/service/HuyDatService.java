package com.lotuslaverne.service;

import com.lotuslaverne.util.ConnectDB;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class HuyDatService {

    public double huyDat(String maPhieuDatPhong, String maNhanVien) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) throw new IllegalStateException("Không kết nối được database");

        // 1. Validate trạng thái + lấy ngày nhận dự kiến
        String trangThai;
        LocalDate ngayNhan;
        try (PreparedStatement pst = con.prepareStatement(
                "SELECT trangThai, CAST(thoiGianNhanDuKien AS DATE)"
                + " FROM PhieuDatPhong WHERE maPhieuDatPhong = ?")) {
            pst.setString(1, maPhieuDatPhong);
            ResultSet rs = pst.executeQuery();
            if (!rs.next())
                throw new IllegalArgumentException("Không tìm thấy phiếu: " + maPhieuDatPhong);
            trangThai = rs.getString(1);
            Date d = rs.getDate(2);
            ngayNhan = d != null ? d.toLocalDate() : LocalDate.now();
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi đọc phiếu: " + e.getMessage(), e);
        }

        if (!"DaDat".equals(trangThai) && !"ChoThanhToan".equals(trangThai))
            throw new IllegalStateException("Chỉ huỷ được phiếu ở trạng thái DaDat hoặc ChoThanhToan");

        // 2. Lấy tiền cọc đã thu
        double tienCoc;
        try (PreparedStatement pst = con.prepareStatement(
                "SELECT ISNULL(SUM(soTienCoc), 0) FROM PhieuThu WHERE maPhieuDatPhong = ?")) {
            pst.setString(1, maPhieuDatPhong);
            ResultSet rs = pst.executeQuery();
            tienCoc = rs.next() ? rs.getDouble(1) : 0;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi đọc tiền cọc: " + e.getMessage(), e);
        }

        // 3. Tính % hoàn theo BUSINESS_RULES Section 3
        long soNgay = ChronoUnit.DAYS.between(LocalDate.now(), ngayNhan);
        double tienHoan = tinhTienHoan(tienCoc, soNgay);

        // 4. Transaction: UPDATE phiếu + INSERT PhieuHoan nếu có hoàn
        try {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement pst = con.prepareStatement(
                        "UPDATE PhieuDatPhong SET trangThai = N'HuyDat' WHERE maPhieuDatPhong = ?")) {
                    pst.setString(1, maPhieuDatPhong);
                    pst.executeUpdate();
                }

                if (tienHoan > 0) {
                    String maPH = "PH" + java.util.UUID.randomUUID()
                            .toString().replace("-", "").substring(0, 8).toUpperCase();
                    try (PreparedStatement pst = con.prepareStatement(
                            "INSERT INTO PhieuHoan"
                            + " (maPhieuHoan, maPhieuDatPhong, soTienHoan, ngayHoan, maNhanVien)"
                            + " VALUES (?, ?, ?, GETDATE(), ?)")) {
                        pst.setString(1, maPH);
                        pst.setString(2, maPhieuDatPhong);
                        pst.setDouble(3, tienHoan);
                        pst.setString(4, maNhanVien);
                        pst.executeUpdate();
                    }
                }

                con.commit();
            } catch (Exception e) {
                try { con.rollback(); } catch (Exception ignored) {}
                throw e;
            } finally {
                try { con.setAutoCommit(true); } catch (Exception ignored) {}
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Huỷ đặt phòng thất bại: " + e.getMessage(), e);
        }

        return tienHoan;
    }

    // Package-private: unit test tính toán không cần mock DB (Section 3)
    static double tinhTienHoan(double tienCoc, long soNgayTruocNhan) {
        if (soNgayTruocNhan > 7) return tienCoc;
        if (soNgayTruocNhan >= 3) return Math.round(tienCoc * 0.5);
        return 0;
    }
}
