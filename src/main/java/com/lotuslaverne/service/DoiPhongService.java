package com.lotuslaverne.service;

import com.lotuslaverne.util.ConnectDB;

import java.sql.*;
import java.time.LocalDateTime;

public class DoiPhongService {

    // O-1: bỏ giaMoi khỏi param, Service tự query BangGia
    // O-3: validate trạng thái DaCheckIn trước khi đổi
    // O-4: kiểm tra phòng mới có trống không
    // Trả về tongBuTru = (giaMoi - giaCu) × soDemConLai (dương=thu thêm, âm=hoàn lại, 0=ngang)
    public double doiPhong(String maPhieuDatPhong, String maPhongMoi) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) throw new IllegalStateException("Không kết nối được database");

        // O-3: Validate trạng thái + lấy khoảng ngày của phiếu
        String trangThai;
        Timestamp tNhan, tTra;
        try (PreparedStatement pst = con.prepareStatement(
                "SELECT trangThai, thoiGianNhanDuKien, thoiGianTraDuKien"
                + " FROM PhieuDatPhong WHERE maPhieuDatPhong = ?")) {
            pst.setString(1, maPhieuDatPhong);
            ResultSet rs = pst.executeQuery();
            if (!rs.next())
                throw new IllegalArgumentException("Không tìm thấy phiếu: " + maPhieuDatPhong);
            trangThai = rs.getString(1);
            tNhan     = rs.getTimestamp(2);
            tTra      = rs.getTimestamp(3);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi đọc phiếu: " + e.getMessage(), e);
        }

        if (!"DaCheckIn".equals(trangThai))
            throw new IllegalStateException("Chỉ đổi phòng được khi phiếu ở trạng thái DaCheckIn");

        // O-4: Kiểm tra phòng mới có trống trong khoảng ngày đó không
        try (PreparedStatement chk = con.prepareStatement(
                "SELECT COUNT(*) FROM ChiTietPhieuDatPhong ct"
                + " JOIN PhieuDatPhong pdp ON pdp.maPhieuDatPhong = ct.maPhieuDatPhong"
                + " WHERE ct.maPhong = ?"
                + " AND pdp.maPhieuDatPhong <> ?"
                + " AND pdp.trangThai NOT IN (N'HuyDat', N'DaCheckOut')"
                + " AND NOT (pdp.thoiGianTraDuKien <= ? OR pdp.thoiGianNhanDuKien >= ?)")) {
            chk.setString(1, maPhongMoi);
            chk.setString(2, maPhieuDatPhong);
            chk.setTimestamp(3, tNhan);
            chk.setTimestamp(4, tTra);
            ResultSet rs = chk.executeQuery();
            if (rs.next() && rs.getInt(1) > 0)
                throw new IllegalStateException("Phòng " + maPhongMoi + " đã có lịch đặt trùng ngày");
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi kiểm tra phòng mới: " + e.getMessage(), e);
        }

        // O-1: Lấy donGia phòng mới từ BangGia
        double giaMoi;
        try (PreparedStatement pst = con.prepareStatement(
                "SELECT bg.donGia FROM Phong p"
                + " JOIN BangGia bg ON bg.maLoaiPhong = p.maLoaiPhong"
                + "   AND bg.loaiThue = 'QuaDem'"
                + "   AND GETDATE() BETWEEN bg.ngayBatDau AND bg.ngayKetThuc"
                + " WHERE p.maPhong = ?")) {
            pst.setString(1, maPhongMoi);
            ResultSet rs = pst.executeQuery();
            if (!rs.next() || rs.getDouble(1) <= 0)
                throw new IllegalStateException("Không có giá hiệu lực cho phòng: " + maPhongMoi);
            giaMoi = rs.getDouble(1);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi đọc giá phòng mới: " + e.getMessage(), e);
        }

        // Lấy giá phòng cũ + số đêm còn lại để tính bù trừ
        String maPhongCu = null;
        try (PreparedStatement pst = con.prepareStatement(
                "SELECT maPhong FROM ChiTietPhieuDatPhong WHERE maPhieuDatPhong = ?")) {
            pst.setString(1, maPhieuDatPhong);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) maPhongCu = rs.getString(1);
        } catch (Exception ignored) {}

        double giaCu = 0;
        if (maPhongCu != null) {
            try (PreparedStatement pst = con.prepareStatement(
                    "SELECT bg.donGia FROM Phong p"
                    + " JOIN BangGia bg ON bg.maLoaiPhong = p.maLoaiPhong"
                    + "   AND bg.loaiThue = 'QuaDem'"
                    + "   AND GETDATE() BETWEEN bg.ngayBatDau AND bg.ngayKetThuc"
                    + " WHERE p.maPhong = ?")) {
                pst.setString(1, maPhongCu);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) giaCu = rs.getDouble(1);
            } catch (Exception ignored) {}
        }

        long soDemConLai = 1;
        if (tTra != null) {
            long msConLai = tTra.getTime() - System.currentTimeMillis();
            soDemConLai = Math.max(1, msConLai / (24L * 3_600_000));
        }
        double tongBuTru = (giaMoi - giaCu) * soDemConLai;

        // Gọi SP_DoiPhong với giá đã tính (O-2 giữ nguyên — logic trong SP)
        try (CallableStatement cs = con.prepareCall("{call SP_DoiPhong(?,?,?)}")) {
            cs.setString(1, maPhieuDatPhong);
            cs.setString(2, maPhongMoi);
            cs.setDouble(3, giaMoi);
            cs.execute();
        } catch (Exception e) {
            throw new RuntimeException("Đổi phòng thất bại: " + e.getMessage(), e);
        }

        // Ghi nhận bù trừ vào ghiChu phiếu để cashier thấy khi checkout
        if (tongBuTru != 0 && maPhongCu != null) {
            String note = String.format(" | Bù trừ đổi phòng %s→%s: %s%.0f VNĐ (%d đêm)",
                    maPhongCu, maPhongMoi,
                    tongBuTru > 0 ? "Thu thêm +" : "Hoàn lại -",
                    Math.abs(tongBuTru), soDemConLai);
            try (PreparedStatement upd = con.prepareStatement(
                    "UPDATE PhieuDatPhong SET ghiChu = ISNULL(ghiChu,'') + ? WHERE maPhieuDatPhong = ?")) {
                upd.setString(1, note);
                upd.setString(2, maPhieuDatPhong);
                upd.executeUpdate();
            } catch (Exception ignored) {}
        }

        return tongBuTru;
    }

    public Object[] traCuuBySdt(String sdt) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return null;
        String sql = "SELECT TOP 1 kh.hoTenKH, kh.cmnd, "
                + "pdp.maPhieuDatPhong, pdp.thoiGianNhanThucTe, pdp.thoiGianTraDuKien, "
                + "ct.maPhong, lp.tenLoaiPhong, ISNULL(bg.donGia, 0) AS donGia "
                + "FROM KhachHang kh "
                + "JOIN PhieuDatPhong pdp ON pdp.maKhachHang = kh.maKH "
                + "JOIN ChiTietPhieuDatPhong ct ON ct.maPhieuDatPhong = pdp.maPhieuDatPhong "
                + "JOIN Phong p ON p.maPhong = ct.maPhong "
                + "JOIN LoaiPhong lp ON lp.maLoaiPhong = p.maLoaiPhong "
                + "LEFT JOIN BangGia bg ON bg.maLoaiPhong = p.maLoaiPhong "
                + "   AND bg.loaiThue = 'QuaDem' "
                + "   AND GETDATE() BETWEEN bg.ngayBatDau AND bg.ngayKetThuc "
                + "WHERE kh.soDienThoai = ? "
                + "  AND pdp.thoiGianNhanThucTe IS NOT NULL "
                + "  AND pdp.thoiGianTraThucTe IS NULL "
                + "ORDER BY pdp.thoiGianNhanThucTe DESC";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, sdt);
            try (ResultSet rs = pst.executeQuery()) {
                if (!rs.next()) return null;
                Timestamp tsNhan = rs.getTimestamp("thoiGianNhanThucTe");
                Timestamp tsTra  = rs.getTimestamp("thoiGianTraDuKien");
                return new Object[]{
                    rs.getString("hoTenKH"),
                    rs.getString("cmnd"),
                    rs.getString("maPhieuDatPhong"),
                    tsNhan != null ? tsNhan.toLocalDateTime() : null,
                    tsTra  != null ? tsTra.toLocalDateTime()  : null,
                    rs.getString("maPhong"),
                    rs.getString("tenLoaiPhong"),
                    rs.getDouble("donGia")
                };
            }
        } catch (Exception e) {
            throw new RuntimeException("Tra cứu thất bại: " + e.getMessage(), e);
        }
    }
}
