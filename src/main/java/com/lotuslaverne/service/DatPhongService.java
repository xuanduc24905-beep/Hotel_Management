package com.lotuslaverne.service;

import com.lotuslaverne.dao.KhachHangDAO;
import com.lotuslaverne.dao.PhieuDatPhongDAO;
import com.lotuslaverne.dao.PhieuThuDAO;
import com.lotuslaverne.dao.PhongDAO;
import com.lotuslaverne.entity.KhachHang;
import com.lotuslaverne.util.ConnectDB;
import com.lotuslaverne.util.SessionContext;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class DatPhongService {

    // D-1: donGia bỏ khỏi param, Service tự query BangGia
    // D-5: bỏ isPending/ChoThanhToan — phiếu luôn bắt đầu ở DaDat (Rule 2)
    // D-6: maNV bỏ khỏi param, lấy từ SessionContext
    public String datPhong(String maPDP, String maKH, int soNguoi,
                           String maPhong,
                           LocalDate ngayNhan, LocalDate ngayTra,
                           String ghiChu, String hinhThucDat,
                           String maPT, String payMethod,
                           String maKhuyenMai) {

        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) throw new IllegalStateException("Không kết nối được database");

        String maNV = SessionContext.getInstance().getMaNhanVien();

        Timestamp tNhan = Timestamp.valueOf(ngayNhan.atStartOfDay());
        Timestamp tTra  = Timestamp.valueOf(ngayTra.atStartOfDay());

        // D-1: Lấy donGia từ BangGia (Rule 6 — giá tại thời điểm đặt)
        double donGia;
        try (PreparedStatement pst = con.prepareStatement(
                "SELECT bg.donGia FROM Phong p"
                + " JOIN BangGia bg ON bg.maLoaiPhong = p.maLoaiPhong"
                + "   AND bg.loaiThue = 'QuaDem'"
                + "   AND GETDATE() BETWEEN bg.ngayBatDau AND bg.ngayKetThuc"
                + " WHERE p.maPhong = ?")) {
            pst.setString(1, maPhong);
            ResultSet rs = pst.executeQuery();
            if (!rs.next() || rs.getDouble(1) <= 0)
                throw new IllegalStateException("Không có giá hiệu lực cho phòng: " + maPhong);
            donGia = rs.getDouble(1);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi đọc giá phòng: " + e.getMessage(), e);
        }

        // D-4: Validate mã KM còn hiệu lực (Rule 2)
        if (maKhuyenMai != null && !maKhuyenMai.isBlank()) {
            try (PreparedStatement pst = con.prepareStatement(
                    "SELECT COUNT(*) FROM KhuyenMai"
                    + " WHERE maKhuyenMai = ? AND GETDATE() BETWEEN ngayApDung AND ngayKetThuc")) {
                pst.setString(1, maKhuyenMai);
                ResultSet rs = pst.executeQuery();
                if (!rs.next() || rs.getInt(1) == 0)
                    throw new IllegalArgumentException("Mã khuyến mãi hết hạn hoặc không tồn tại: " + maKhuyenMai);
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("Lỗi kiểm tra mã KM: " + e.getMessage(), e);
            }
        }

        // Kiểm tra overlap ngày trước khi ghi DB (Rule 2)
        try (PreparedStatement chk = con.prepareStatement(
                "SELECT COUNT(*) FROM ChiTietPhieuDatPhong ct"
                + " JOIN PhieuDatPhong pdp ON pdp.maPhieuDatPhong = ct.maPhieuDatPhong"
                + " WHERE ct.maPhong = ?"
                + " AND pdp.trangThai NOT IN (N'HuyDat', N'DaCheckOut')"
                + " AND NOT (pdp.thoiGianTraDuKien <= ? OR pdp.thoiGianNhanDuKien >= ?)")) {
            chk.setString(1, maPhong);
            chk.setTimestamp(2, tNhan);
            chk.setTimestamp(3, tTra);
            ResultSet rs = chk.executeQuery();
            if (rs.next() && rs.getInt(1) > 0)
                throw new IllegalStateException("Phòng đã có lịch đặt trùng ngày");
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Kiểm tra phòng trống thất bại: " + e.getMessage(), e);
        }

        // Rule 2 — Cọc = 50% tổng tiền phòng
        long soNgay    = Math.max(1L, ChronoUnit.DAYS.between(ngayNhan, ngayTra));
        double tienCoc = Math.round(donGia * soNgay * 0.5);

        try {
            con.setAutoCommit(false);
            try {
                // INSERT PhieuDatPhong (D-5: luôn DaDat)
                try (PreparedStatement pst = con.prepareStatement(
                        "INSERT INTO PhieuDatPhong"
                        + " (maPhieuDatPhong,ngayDat,maKhachHang,maNhanVien,soNguoi,"
                        + "thoiGianNhanDuKien,thoiGianTraDuKien,hinhThucDat,trangThai,ghiChu,maKhuyenMai)"
                        + " VALUES (?,GETDATE(),?,?,?,?,?,?,N'DaDat',?,?)")) {
                    pst.setString(1, maPDP);
                    pst.setString(2, maKH);
                    pst.setString(3, maNV);
                    pst.setInt   (4, soNguoi);
                    pst.setTimestamp(5, tNhan);
                    pst.setTimestamp(6, tTra);
                    pst.setString(7, hinhThucDat != null ? hinhThucDat : "TrucTiep");
                    pst.setString(8, ghiChu);
                    pst.setString(9, maKhuyenMai);
                    pst.executeUpdate();
                }

                // INSERT ChiTietPhieuDatPhong — donGia lock tại thời điểm đặt
                try (PreparedStatement pst = con.prepareStatement(
                        "INSERT INTO ChiTietPhieuDatPhong (maPhieuDatPhong, maPhong, donGia)"
                        + " VALUES (?,?,?)")) {
                    pst.setString(1, maPDP);
                    pst.setString(2, maPhong);
                    pst.setDouble(3, donGia);
                    pst.executeUpdate();
                }

                // INSERT PhieuThu cọc
                if (tienCoc > 0 && maPT != null && !maPT.isBlank()) {
                    new PhieuThuDAO().taoPhieuThuWithCon(con, maPT, maPDP, maNV,
                            tienCoc, payMethod, "Tiền cọc đặt phòng");
                }

                con.commit();
                return maPDP;

            } catch (Exception e) {
                try { con.rollback(); } catch (Exception ignored) {}
                throw e;
            } finally {
                try { con.setAutoCommit(true); } catch (Exception ignored) {}
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Đặt phòng thất bại: " + e.getMessage(), e);
        }
    }

    /** Danh sách phòng (maPhong, tenPhong) để hiển thị lịch calendar. */
    public List<String[]> loadPhongTrong(LocalDate ngayVao, LocalDate ngayTra) {
        return new PhongDAO().loadAllForCalendar();
    }

    /** Lịch đặt phòng trong tuần [from, to) cho gantt chart. */
    public List<String[]> loadLichDat(LocalDate from, LocalDate to) {
        return new PhieuDatPhongDAO().loadBookingsForCalendar(from, to);
    }

    /** Lịch sử đặt phòng TOP limit bản ghi gần nhất. */
    public List<Object[]> loadLichSu(int limit) {
        return new PhieuDatPhongDAO().loadHistoryForView();
    }

    /** Tra cứu khách hàng theo SĐT. Trả về null nếu không tìm thấy. */
    public KhachHang lookupKhach(String sdt) {
        return new KhachHangDAO().findBySdt(sdt);
    }
}
