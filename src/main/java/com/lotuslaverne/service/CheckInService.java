package com.lotuslaverne.service;

import com.lotuslaverne.util.ConnectDB;

import java.sql.*;

public class CheckInService {

    public void checkIn(String maPhieuDatPhong) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) throw new IllegalStateException("Không kết nối được database");

        // Validate trạng thái DaDat (Rule 4)
        try (PreparedStatement pst = con.prepareStatement(
                "SELECT trangThai FROM PhieuDatPhong WHERE maPhieuDatPhong = ?")) {
            pst.setString(1, maPhieuDatPhong);
            ResultSet rs = pst.executeQuery();
            if (!rs.next())
                throw new IllegalArgumentException("Không tìm thấy phiếu: " + maPhieuDatPhong);
            if (!"DaDat".equals(rs.getString(1)))
                throw new IllegalStateException("Phiếu không ở trạng thái DaDat");
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi validate check-in: " + e.getMessage(), e);
        }

        // Transaction (Rule 4)
        try {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement pst = con.prepareStatement(
                        "UPDATE PhieuDatPhong"
                        + " SET trangThai = N'DaCheckIn', thoiGianNhanThucTe = GETDATE()"
                        + " WHERE maPhieuDatPhong = ?")) {
                    pst.setString(1, maPhieuDatPhong);
                    pst.executeUpdate();
                }

                try (PreparedStatement pst = con.prepareStatement(
                        "UPDATE Phong SET trangThai = N'DangSuDung'"
                        + " WHERE maPhong IN"
                        + " (SELECT maPhong FROM ChiTietPhieuDatPhong WHERE maPhieuDatPhong = ?)")) {
                    pst.setString(1, maPhieuDatPhong);
                    pst.executeUpdate();
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
            throw new RuntimeException("Check-in thất bại: " + e.getMessage(), e);
        }
    }
}
