package com.lotuslaverne.dao;

import com.lotuslaverne.entity.DichVu;
import com.lotuslaverne.util.ConnectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DichVuDAO {

    public List<DichVu> getAll() {
        List<DichVu> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;
        try {
            String sql = "SELECT * FROM DichVu";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(new DichVu(
                    rs.getString("maDichVu"),
                    rs.getString("tenDichVu"),
                    rs.getString("maLoaiDichVu"),
                    rs.getDouble("donGia"),
                    rs.getString("trangThai")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean themDichVu(DichVu dv) {
        Connection con = ConnectDB.getInstance().getConnection();
        try {
            String sql = "INSERT INTO DichVu (maDichVu, tenDichVu, maLoaiDichVu, donGia, trangThai) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, dv.getMaDichVu());
            pst.setString(2, dv.getTenDichVu());
            pst.setString(3, dv.getMaLoaiDichVu());
            pst.setDouble(4, dv.getDonGia());
            pst.setString(5, dv.getTrangThai());
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean capNhatDichVu(DichVu dv) {
        Connection con = ConnectDB.getInstance().getConnection();
        try {
            String sql = "UPDATE DichVu SET tenDichVu = ?, maLoaiDichVu = ?, donGia = ?, trangThai = ? WHERE maDichVu = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, dv.getTenDichVu());
            pst.setString(2, dv.getMaLoaiDichVu());
            pst.setDouble(3, dv.getDonGia());
            pst.setString(4, dv.getTrangThai());
            pst.setString(5, dv.getMaDichVu());
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean dungBanDichVu(String maDichVu) {
        Connection con = ConnectDB.getInstance().getConnection();
        try {
            String sql = "UPDATE DichVu SET trangThai = N'NgungKinhDoanh' WHERE maDichVu = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, maDichVu);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Gắn dịch vụ vào phiếu đặt phòng đang ở */
    public boolean themChiTietDichVu(String maDichVu, String maPhieuDatPhong, int soLuong, String ghiChu) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            // Nếu đã tồn tại thì cộng thêm số lượng
            String checkSql = "SELECT soLuong FROM ChiTietDichVu WHERE maDichVu=? AND maPhieuDatPhong=?";
            PreparedStatement chk = con.prepareStatement(checkSql);
            chk.setString(1, maDichVu); chk.setString(2, maPhieuDatPhong);
            java.sql.ResultSet rs = chk.executeQuery();
            if (rs.next()) {
                int currentQty = rs.getInt("soLuong");
                String upd = "UPDATE ChiTietDichVu SET soLuong=?, ghiChu=? WHERE maDichVu=? AND maPhieuDatPhong=?";
                PreparedStatement pst = con.prepareStatement(upd);
                pst.setInt(1, currentQty + soLuong);
                pst.setString(2, ghiChu);
                pst.setString(3, maDichVu);
                pst.setString(4, maPhieuDatPhong);
                return pst.executeUpdate() > 0;
            } else {
                String ins = "INSERT INTO ChiTietDichVu (maDichVu,maPhieuDatPhong,soLuong,thoiDiemSuDung,ghiChu) VALUES (?,?,?,GETDATE(),?)";
                PreparedStatement pst = con.prepareStatement(ins);
                pst.setString(1, maDichVu);
                pst.setString(2, maPhieuDatPhong);
                pst.setInt(3, soLuong);
                pst.setString(4, ghiChu);
                return pst.executeUpdate() > 0;
            }
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /** Lấy danh sách dịch vụ đã dùng theo phiếu đặt phòng */
    public java.util.List<Object[]> getChiTietDichVuByPhieu(String maPhieuDatPhong) {
        java.util.List<Object[]> list = new java.util.ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;
        String sql = "SELECT dv.maDichVu, dv.tenDichVu, ct.soLuong, dv.donGia, " +
                     "(ct.soLuong * dv.donGia) AS thanhTien, ct.thoiDiemSuDung, ct.ghiChu " +
                     "FROM ChiTietDichVu ct " +
                     "JOIN DichVu dv ON dv.maDichVu = ct.maDichVu " +
                     "WHERE ct.maPhieuDatPhong = ? ORDER BY ct.thoiDiemSuDung DESC";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maPhieuDatPhong);
            java.sql.ResultSet rs = pst.executeQuery();
            java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("maDichVu"),
                    rs.getString("tenDichVu"),
                    rs.getInt("soLuong"),
                    df.format(rs.getDouble("donGia")),
                    df.format(rs.getDouble("thanhTien")),
                    rs.getString("thoiDiemSuDung") != null
                        ? rs.getTimestamp("thoiDiemSuDung").toString().substring(0, 16) : "",
                    rs.getString("ghiChu") != null ? rs.getString("ghiChu") : ""
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
}
