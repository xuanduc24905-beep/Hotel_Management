package com.lotuslaverne.service;

import com.lotuslaverne.util.ConnectDB;
import com.lotuslaverne.util.SessionContext;

import java.sql.*;
import java.time.LocalDate;
import java.util.UUID;

/**
 * WalkInService — Rule 12: walk-in check-in.
 * Toàn bộ luồng trong 1 transaction:
 *   1. Tìm / tạo KhachHang theo SĐT
 *   2. INSERT PhieuDatPhong (DaDat, hinhThucDat = TrucTiep)
 *   3. INSERT ChiTietPhieuDatPhong (lấy giá từ BangGia)
 *   4. INSERT PhieuThu cọc 50%
 *   5. UPDATE PhieuDatPhong → DaCheckIn + thoiGianNhanThucTe = NOW
 *   6. UPDATE Phong → DangSuDung
 *
 * @return maPDP vừa tạo để View hiển thị xác nhận.
 */
public class WalkInService {

    public String walkIn(String hoTenKH, String sdt, String cmnd,
                         String maPhong, LocalDate ngayTra) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) throw new IllegalStateException("Không kết nối được database");

        boolean oldAutoCommit = true;
        try {
            oldAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);

            // 1. Tìm hoặc tạo KhachHang
            String maKH = timHoacTaoKhachHang(con, hoTenKH, sdt, cmnd);

            // 2. Lấy donGia từ BangGia
            double donGia = layDonGia(con, maPhong);
            if (donGia <= 0)
                throw new IllegalStateException("Không có giá hiệu lực cho phòng: " + maPhong);

            // 3. Tính số đêm và cọc
            LocalDate ngayHom = LocalDate.now();
            long soNgay = Math.max(1, ngayHom.until(ngayTra).getDays());
            double tienCoc = 0.5 * donGia * soNgay;

            // 4. Tạo mã phiếu
            String maPDP = "PDP" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
            String maNV  = SessionContext.getInstance().getMaNhanVien();

            // 5. INSERT PhieuDatPhong
            try (PreparedStatement pst = con.prepareStatement(
                    "INSERT INTO PhieuDatPhong "
                    + "(maPhieuDatPhong,ngayDat,maKhachHang,maNhanVien,soNguoi,"
                    + "thoiGianNhanDuKien,thoiGianTraDuKien,hinhThucDat,trangThai,ghiChu) "
                    + "VALUES (?,GETDATE(),?,?,1,GETDATE(),?,N'TrucTiep',N'DaDat',N'Walk-in')")) {
                pst.setString(1, maPDP);
                pst.setString(2, maKH);
                pst.setString(3, maNV);
                pst.setTimestamp(4, Timestamp.valueOf(ngayTra.atStartOfDay()));
                pst.executeUpdate();
            }

            // 6. INSERT ChiTietPhieuDatPhong
            try (PreparedStatement pst = con.prepareStatement(
                    "INSERT INTO ChiTietPhieuDatPhong (maPhieuDatPhong,maPhong,donGia) VALUES (?,?,?)")) {
                pst.setString(1, maPDP);
                pst.setString(2, maPhong);
                pst.setDouble(3, donGia);
                pst.executeUpdate();
            }

            // 7. INSERT PhieuThu cọc
            String maPT = "PT" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
            try (PreparedStatement pst = con.prepareStatement(
                    "INSERT INTO PhieuThu (maPhieuThu,maPhieuDatPhong,maNhanVien,soTienThu,"
                    + "ngayThu,loaiThu,ghiChu) VALUES (?,?,?,?,GETDATE(),N'CocDatPhong',N'Walk-in coc 50%')")) {
                pst.setString(1, maPT);
                pst.setString(2, maPDP);
                pst.setString(3, maNV);
                pst.setDouble(4, tienCoc);
                pst.executeUpdate();
            }

            // 8. Check-in ngay: DaCheckIn + thoiGianNhanThucTe
            try (PreparedStatement pst = con.prepareStatement(
                    "UPDATE PhieuDatPhong SET trangThai=N'DaCheckIn', thoiGianNhanThucTe=GETDATE() "
                    + "WHERE maPhieuDatPhong=?")) {
                pst.setString(1, maPDP);
                pst.executeUpdate();
            }

            // 9. Cập nhật trạng thái phòng
            try (PreparedStatement pst = con.prepareStatement(
                    "UPDATE Phong SET trangThai=N'DangSuDung' WHERE maPhong=?")) {
                pst.setString(1, maPhong);
                pst.executeUpdate();
            }

            con.commit();
            return maPDP;

        } catch (IllegalStateException e) {
            rollback(con);
            throw e;
        } catch (Exception e) {
            rollback(con);
            throw new RuntimeException("Walk-in thất bại: " + e.getMessage(), e);
        } finally {
            try { con.setAutoCommit(oldAutoCommit); } catch (Exception ignored) {}
        }
    }

    private String timHoacTaoKhachHang(Connection con, String hoTen, String sdt, String cmnd)
            throws SQLException {
        try (PreparedStatement chk = con.prepareStatement(
                "SELECT maKH FROM KhachHang WHERE soDienThoai=?")) {
            chk.setString(1, sdt);
            try (ResultSet rs = chk.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        }
        // Tạo mới
        String maKH = "KH" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        try (PreparedStatement ins = con.prepareStatement(
                "INSERT INTO KhachHang (maKH,hoTenKH,soDienThoai,cmnd) VALUES (?,?,?,?)")) {
            ins.setString(1, maKH);
            ins.setString(2, hoTen);
            ins.setString(3, sdt);
            ins.setString(4, cmnd != null ? cmnd : "");
            ins.executeUpdate();
        }
        return maKH;
    }

    private double layDonGia(Connection con, String maPhong) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement(
                "SELECT bg.donGia FROM Phong p"
                + " JOIN BangGia bg ON bg.maLoaiPhong = p.maLoaiPhong"
                + "   AND bg.loaiThue = 'QuaDem'"
                + "   AND GETDATE() BETWEEN bg.ngayBatDau AND bg.ngayKetThuc"
                + " WHERE p.maPhong = ?")) {
            pst.setString(1, maPhong);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return 0;
    }

    private void rollback(Connection con) {
        try { con.rollback(); } catch (Exception ignored) {}
    }
}
