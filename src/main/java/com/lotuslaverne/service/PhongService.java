package com.lotuslaverne.service;

import com.lotuslaverne.dao.PhongDAO;
import com.lotuslaverne.entity.Phong;
import com.lotuslaverne.util.ConnectDB;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class PhongService {

    private static final Set<String> TRANG_THAI_HOP_LE =
        Set.of("PhongTrong", "DangSuDung", "PhongCanDon", "BaoDuong");

    /**
     * Xác nhận chuyển khoản thành công: cập nhật trangThai phiếu từ 'ChoThanhToan' → 'DaDat'.
     */
    public boolean xacNhanChuyenKhoan(String maPhieuDatPhong) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try (PreparedStatement pst = con.prepareStatement(
                "UPDATE PhieuDatPhong SET trangThai=N'DaDat' WHERE maPhieuDatPhong=?")) {
            pst.setString(1, maPhieuDatPhong);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cập nhật trạng thái phòng — chỉ chấp nhận: PhongTrong, DangSuDung, PhongCanDon, BaoDuong.
     * @throws IllegalArgumentException nếu trangThai không hợp lệ
     */
    public boolean capNhatTrangThai(String maPhong, String trangThai) {
        if (!TRANG_THAI_HOP_LE.contains(trangThai))
            throw new IllegalArgumentException("Trạng thái không hợp lệ: " + trangThai
                + ". Chỉ chấp nhận: " + TRANG_THAI_HOP_LE);
        return new PhongDAO().capNhatTrangThai(maPhong, trangThai);
    }

    /**
     * Thêm phòng mới — sinh maPhong tự động nếu để trống.
     */
    public boolean themPhong(Phong phong) {
        if (phong.getMaPhong() == null || phong.getMaPhong().isBlank()) {
            phong.setMaPhong("P" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        }
        return new PhongDAO().themPhong(phong);
    }

    /**
     * Danh sách phòng theo trạng thái DB. Truyền null hoặc "" để lấy tất cả.
     */
    public List<Phong> loadPhongTheo(String trangThai) {
        List<Phong> all = new PhongDAO().getAll();
        if (trangThai == null || trangThai.isBlank()) return all;
        return all.stream()
            .filter(p -> trangThai.equals(p.getTrangThai()))
            .collect(Collectors.toList());
    }

    /**
     * Danh sách phòng trống trong khoảng ngày (dựa trên overlap lịch đặt).
     * Dùng cho tab tìm phòng theo ngày.
     */
    public List<String[]> loadPhongTheoNgay(LocalDate ngayVao, LocalDate ngayTra) {
        return new PhongDAO().searchAvailable(null, ngayVao, ngayTra,
                false, false, false, false, false, false);
    }
}
