package com.lotuslaverne.service;

import com.lotuslaverne.entity.HoaDon;
import com.lotuslaverne.util.ConnectDB;

import java.sql.*;

public class CheckoutService {

    public HoaDon checkout(String maPhieuDatPhong, String maNhanVien,
                           String phuongThuc, String ghiChu) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) throw new IllegalStateException("Không kết nối được database");

        try {
            // 1. Validate trạng thái DaCheckIn + lấy maPhong + phanTramGiam KM
            String maPhong;
            long soNgay;
            double phanTramGiam;
            try (PreparedStatement pst = con.prepareStatement(
                    "SELECT ct.maPhong,"
                    + " DATEDIFF(day, ISNULL(pdp.thoiGianNhanThucTe, pdp.thoiGianNhanDuKien), GETDATE()),"
                    + " pdp.trangThai,"
                    + " ISNULL(km.phanTramGiam, 0)"
                    + " FROM PhieuDatPhong pdp"
                    + " JOIN ChiTietPhieuDatPhong ct ON ct.maPhieuDatPhong = pdp.maPhieuDatPhong"
                    + " LEFT JOIN KhuyenMai km ON km.maKhuyenMai = pdp.maKhuyenMai"
                    + " WHERE pdp.maPhieuDatPhong = ?")) {
                pst.setString(1, maPhieuDatPhong);
                ResultSet rs = pst.executeQuery();
                if (!rs.next())
                    throw new IllegalArgumentException(
                            "Không tìm thấy phiếu đặt phòng: " + maPhieuDatPhong);
                if (!"DaCheckIn".equals(rs.getString(3)))
                    throw new IllegalStateException("Phiếu chưa check-in hoặc đã checkout");
                maPhong     = rs.getString(1);
                soNgay      = Math.max(1, rs.getLong(2));
                phanTramGiam = rs.getDouble(4);
            }

            // 2. Giá đã lock tại thời điểm đặt — lấy từ ChiTietPhieuDatPhong
            double donGia;
            try (PreparedStatement pst = con.prepareStatement(
                    "SELECT donGia FROM ChiTietPhieuDatPhong WHERE maPhieuDatPhong = ? AND maPhong = ?")) {
                pst.setString(1, maPhieuDatPhong);
                pst.setString(2, maPhong);
                ResultSet rs = pst.executeQuery();
                if (!rs.next() || rs.getDouble(1) <= 0)
                    throw new IllegalStateException("Không tìm thấy giá phòng");
                donGia = rs.getDouble(1);
            }

            // 3. Tổng tiền dịch vụ
            double tienDV;
            try (PreparedStatement pst = con.prepareStatement(
                    "SELECT ISNULL(SUM(ctdv.soLuong * dv.donGia), 0)"
                    + " FROM ChiTietDichVu ctdv JOIN DichVu dv ON dv.maDichVu = ctdv.maDichVu"
                    + " WHERE ctdv.maPhieuDatPhong = ?")) {
                pst.setString(1, maPhieuDatPhong);
                ResultSet rs = pst.executeQuery();
                tienDV = rs.next() ? rs.getDouble(1) : 0;
            }

            // 4. Tiền cọc đã thu
            double tienCoc;
            try (PreparedStatement pst = con.prepareStatement(
                    "SELECT ISNULL(SUM(soTienCoc), 0) FROM PhieuThu WHERE maPhieuDatPhong = ?")) {
                pst.setString(1, maPhieuDatPhong);
                ResultSet rs = pst.executeQuery();
                tienCoc = rs.next() ? rs.getDouble(1) : 0;
            }

            double tienPhong      = donGia * soNgay;
            double tienKhuyenMai  = Math.round(tienPhong * phanTramGiam / 100.0);
            double tienThanhToan  = tinhTienThanhToan(tienPhong, tienDV, tienCoc, tienKhuyenMai);

            // Tạo HoaDon object trước transaction — lấy maHD từ đây dùng xuyên suốt các INSERT
            HoaDon hd = HoaDon.taoTuCheckout(
                    maPhieuDatPhong, maNhanVien, tienKhuyenMai, tienThanhToan,
                    phuongThuc, ghiChu != null ? ghiChu : "");
            String maHD = hd.getMaHoaDon();

            // 5. Transaction — toàn bộ write trong 1 atomic block
            con.setAutoCommit(false);
            try {
                try (PreparedStatement pst = con.prepareStatement(
                        "UPDATE PhieuDatPhong SET trangThai = N'DaCheckOut',"
                        + " thoiGianTraThucTe = GETDATE() WHERE maPhieuDatPhong = ?")) {
                    pst.setString(1, maPhieuDatPhong);
                    pst.executeUpdate();
                }

                try (PreparedStatement pst = con.prepareStatement(
                        "UPDATE Phong SET trangThai = N'PhongCanDon'"
                        + " WHERE maPhong IN"
                        + " (SELECT maPhong FROM ChiTietPhieuDatPhong WHERE maPhieuDatPhong = ?)")) {
                    pst.setString(1, maPhieuDatPhong);
                    pst.executeUpdate();
                }

                try (PreparedStatement pst = con.prepareStatement(
                        "INSERT INTO HoaDon"
                        + " (maHoaDon, ngayLap, maNhanVienLap, maPhieuDatPhong,"
                        + "  ngayThanhToan, tienKhuyenMai, tienThanhToan, phuongThucThanhToan, ghiChu)"
                        + " VALUES (?, GETDATE(), ?, ?, GETDATE(), ?, ?, ?, ?)")) {
                    pst.setString(1, maHD);
                    pst.setString(2, maNhanVien);
                    pst.setString(3, maPhieuDatPhong);
                    pst.setDouble(4, tienKhuyenMai);
                    pst.setDouble(5, tienThanhToan);
                    pst.setString(6, phuongThuc);
                    pst.setString(7, hd.getGhiChu());
                    pst.executeUpdate();
                }

                if (tienPhong > 0) {
                    try (PreparedStatement pst = con.prepareStatement(
                            "INSERT INTO ChiTietHoaDon"
                            + " (maHoaDon, loaiTien, moTa, donGia, soLuong, thanhTien)"
                            + " VALUES (?, N'TienPhong', ?, ?, ?, ?)")) {
                        pst.setString(1, maHD);
                        pst.setString(2, "Tiền phòng (" + soNgay + " đêm)");
                        pst.setDouble(3, donGia);
                        pst.setInt   (4, (int) soNgay);
                        pst.setDouble(5, tienPhong);
                        pst.executeUpdate();
                    }
                }

                try (PreparedStatement qry = con.prepareStatement(
                        "SELECT dv.tenDichVu, ctdv.soLuong, dv.donGia, (ctdv.soLuong * dv.donGia)"
                        + " FROM ChiTietDichVu ctdv JOIN DichVu dv ON dv.maDichVu = ctdv.maDichVu"
                        + " WHERE ctdv.maPhieuDatPhong = ?")) {
                    qry.setString(1, maPhieuDatPhong);
                    ResultSet rs = qry.executeQuery();
                    while (rs.next()) {
                        try (PreparedStatement ins = con.prepareStatement(
                                "INSERT INTO ChiTietHoaDon"
                                + " (maHoaDon, loaiTien, moTa, donGia, soLuong, thanhTien)"
                                + " VALUES (?, N'TienDichVu', ?, ?, ?, ?)")) {
                            ins.setString(1, maHD);
                            ins.setString(2, rs.getString(1));
                            ins.setDouble(3, rs.getDouble(3));
                            ins.setInt   (4, rs.getInt(2));
                            ins.setDouble(5, rs.getDouble(4));
                            ins.executeUpdate();
                        }
                    }
                }

                if (tienCoc > 0) {
                    try (PreparedStatement pst = con.prepareStatement(
                            "INSERT INTO ChiTietHoaDon"
                            + " (maHoaDon, loaiTien, moTa, donGia, soLuong, thanhTien)"
                            + " VALUES (?, N'TienCoc', N'Tiền cọc đã thu', ?, 1, ?)")) {
                        pst.setString(1, maHD);
                        pst.setDouble(2, -tienCoc);
                        pst.setDouble(3, -tienCoc);
                        pst.executeUpdate();
                    }
                }

                try (PreparedStatement pst = con.prepareStatement(
                        "UPDATE PhieuThu SET maHoaDon = ?"
                        + " WHERE maPhieuDatPhong = ? AND maHoaDon IS NULL")) {
                    pst.setString(1, maHD);
                    pst.setString(2, maPhieuDatPhong);
                    pst.executeUpdate();
                }

                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }

            return hd;

        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Checkout thất bại: " + e.getMessage(), e);
        }
    }

    // Package-private: unit test tính toán không cần mock DB
    static double tinhTienThanhToan(double tienPhong, double tienDV, double tienCoc, double tienKhuyenMai) {
        return Math.max(0, tienPhong + tienDV - tienCoc - tienKhuyenMai);
    }
}
