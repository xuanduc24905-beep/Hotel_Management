package com.lotuslaverne.dao;

import com.lotuslaverne.util.ConnectDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ThongKeDAO {

    public double layDoanhThuHomNay() {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return 0.0;
        String sql = "SELECT ISNULL(SUM(tienThanhToan),0) AS total FROM HoaDon "
                   + "WHERE CAST(ngayLap AS DATE) = CAST(GETDATE() AS DATE)";
        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) return rs.getDouble("total");
        } catch (Exception e) {
            System.err.println("layDoanhThuHomNay loi: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Đếm số phòng theo trạng thái DB.
     * Truyền vào đúng giá trị lưu trong DB: "PhongTrong", "PhongDat", "PhongCanDon", "DangDon", "BaoTri"
     */
    public int demSoPhongTheoTrangThai(String trangThaiDB) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return 0;
        String sql = "SELECT COUNT(*) AS sl FROM Phong WHERE trangThai = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, trangThaiDB);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt("sl");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Đếm tổng nhân viên */
    public int demTongNhanSu() {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return 0;
        String sql = "SELECT COUNT(*) AS sl FROM NhanVien";
        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) return rs.getInt("sl");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Đếm số khách đang lưu trú (phiếu đã check-in, chưa check-out) */
    public int demKhachDangLuuTru() {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return 0;
        String sql = "SELECT COUNT(*) AS sl FROM PhieuDatPhong WHERE trangThai = N'DaCheckIn'";
        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) return rs.getInt("sl");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Tính tỉ lệ lấp đầy phòng.
     * @return double[] { phanTramLapDay, tongSoPhong }
     */
    public double[] layOccupancyRate() {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return new double[]{0, 0};
        String sql = "SELECT COUNT(*) AS total, "
                   + "SUM(CASE WHEN trangThai != N'PhongTrong' THEN 1 ELSE 0 END) AS daSuDung "
                   + "FROM Phong";
        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                int total    = rs.getInt("total");
                int daSuDung = rs.getInt("daSuDung");
                double pct   = total == 0 ? 0 : (daSuDung * 100.0 / total);
                return new double[]{pct, total};
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new double[]{0, 0};
    }
}
