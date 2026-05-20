package com.lotuslaverne.dao;

import com.lotuslaverne.entity.Phong;
import com.lotuslaverne.util.ConnectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PhongDAO {

    /** Lấy tất cả phòng kèm tiện nghi, sức chứa, mô tả */
    public List<Phong> getAll() {
        List<Phong> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;
        try {
            PreparedStatement pst = con.prepareStatement("SELECT * FROM Phong ORDER BY maPhong");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            System.err.println("Load Phòng lỗi: " + e.getMessage());
        }
        return list;
    }

    /**
     * Lấy tất cả phòng cho calendar gantt (chỉ cần maPhong và tenPhong).
     * Trả về List của String[] {maPhong, tenPhong}.
     */
    public List<String[]> loadAllForCalendar() {
        List<String[]> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;
        try (PreparedStatement pst = con.prepareStatement(
                "SELECT maPhong, ISNULL(tenPhong, maPhong) FROM Phong ORDER BY maPhong");
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) list.add(new String[]{rs.getString(1), rs.getString(2)});
        } catch (Exception ignored) {}
        return list;
    }

    /**
     * Tìm phòng trống theo tiêu chí tìm kiếm.
     * Trả về List của String[] {maPhong, tenPhong, tenLoaiPhong, donGia(formatted), tienNghi}.
     */
    public List<String[]> searchAvailable(String loai, LocalDate nd, LocalDate nt,
                                          boolean wifi, boolean tv, boolean dh,
                                          boolean bt, boolean bc, boolean mb) {
        List<String[]> result = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return result;

        StringBuilder sql = new StringBuilder(
            "SELECT p.maPhong, ISNULL(p.tenPhong,p.maPhong), lp.tenLoaiPhong, "
            + "ISNULL(bg.donGia,0), ISNULL(p.tienNghi,'') "
            + "FROM Phong p "
            + "LEFT JOIN LoaiPhong lp ON lp.maLoaiPhong=p.maLoaiPhong "
            + "LEFT JOIN BangGia bg ON bg.maLoaiPhong=p.maLoaiPhong "
            + "  AND bg.loaiThue=N'QuaDem' AND GETDATE() BETWEEN bg.ngayBatDau AND bg.ngayKetThuc "
            + "WHERE p.trangThai=N'PhongTrong' "
            + "AND p.maPhong NOT IN ("
            + "  SELECT ct.maPhong FROM ChiTietPhieuDatPhong ct "
            + "  JOIN PhieuDatPhong pdp ON pdp.maPhieuDatPhong=ct.maPhieuDatPhong "
            + "  WHERE pdp.trangThai NOT IN (N'DaCheckOut',N'HuyDat') "
            + "  AND pdp.thoiGianNhanDuKien<? AND pdp.thoiGianTraDuKien>?) ");
        if (loai != null && !loai.equals("Tất cả")) sql.append("AND lp.tenLoaiPhong=? ");
        if (wifi) sql.append("AND p.tienNghi LIKE N'%WiFi%' ");
        if (tv)   sql.append("AND p.tienNghi LIKE N'%TV%' ");
        if (dh)   sql.append("AND p.tienNghi LIKE N'%Điều Hòa%' ");
        if (bt)   sql.append("AND p.tienNghi LIKE N'%Bồn Tắm%' ");
        if (bc)   sql.append("AND p.tienNghi LIKE N'%Ban Công%' ");
        if (mb)   sql.append("AND p.tienNghi LIKE N'%Mini Bar%' ");
        sql.append("ORDER BY bg.donGia ASC");

        try (PreparedStatement pst = con.prepareStatement(sql.toString())) {
            pst.setTimestamp(1, java.sql.Timestamp.valueOf(nt.atStartOfDay()));
            pst.setTimestamp(2, java.sql.Timestamp.valueOf(nd.atStartOfDay()));
            if (loai != null && !loai.equals("Tất cả")) pst.setString(3, loai);
            try (ResultSet rs = pst.executeQuery()) {
                java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
                while (rs.next()) {
                    double gia = rs.getDouble(4);
                    result.add(new String[]{
                        rs.getString(1), rs.getString(2), rs.getString(3),
                        df.format(gia), rs.getString(5) != null ? rs.getString(5) : ""
                    });
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    /** Lọc phòng có chứa tiện nghi cụ thể (VD: "WiFi") */
    public List<Phong> timTheoTienNghi(String tienNghi) {
        List<Phong> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;
        try {
            PreparedStatement pst = con.prepareStatement(
                "SELECT * FROM Phong WHERE tienNghi LIKE ? ORDER BY maPhong");
            pst.setString(1, "%" + tienNghi + "%");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Thêm phòng mới với đầy đủ thông tin */
    public boolean themPhong(Phong p) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            PreparedStatement pst = con.prepareStatement(
                "INSERT INTO Phong (maPhong,tenPhong,maLoaiPhong,trangThai,tienNghi,soNguoiToiDa,moTa) "
                + "VALUES (?,?,?,?,?,?,?)");
            pst.setString(1, p.getMaPhong());
            pst.setString(2, p.getTenPhong());
            pst.setString(3, p.getMaLoaiPhong());
            pst.setString(4, p.getTrangThai() != null ? p.getTrangThai() : "PhongTrong");
            pst.setString(5, p.getTienNghi());
            pst.setInt   (6, p.getSoNguoiToiDa() > 0 ? p.getSoNguoiToiDa() : 2);
            pst.setString(7, p.getMoTa());
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Cập nhật toàn bộ thông tin phòng (trừ trangThai) */
    public boolean capNhatPhong(Phong p) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            PreparedStatement pst = con.prepareStatement(
                "UPDATE Phong SET tenPhong=?,maLoaiPhong=?,trangThai=?,"
                + "tienNghi=?,soNguoiToiDa=?,moTa=? WHERE maPhong=?");
            pst.setString(1, p.getTenPhong());
            pst.setString(2, p.getMaLoaiPhong());
            pst.setString(3, p.getTrangThai());
            pst.setString(4, p.getTienNghi());
            pst.setInt   (5, p.getSoNguoiToiDa());
            pst.setString(6, p.getMoTa());
            pst.setString(7, p.getMaPhong());
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Lấy danh sách phòng đang PhongTrong (dùng cho walk-in dialog). */
    public List<Phong> getPhongTrong() {
        List<Phong> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;
        try (PreparedStatement pst = con.prepareStatement(
                "SELECT * FROM Phong WHERE trangThai=N'PhongTrong' ORDER BY maPhong")) {
            ResultSet rs = pst.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception ignored) {}
        return list;
    }

    /** Chỉ cập nhật trạng thái phòng */
    public boolean capNhatTrangThai(String maPhong, String trangThaiMoi) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            PreparedStatement pst = con.prepareStatement(
                "UPDATE Phong SET trangThai=? WHERE maPhong=?");
            pst.setString(1, trangThaiMoi);
            pst.setString(2, maPhong);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean xoaPhong(String maPhong) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            PreparedStatement pst = con.prepareStatement(
                "DELETE FROM Phong WHERE maPhong=?");
            pst.setString(1, maPhong);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Phong mapRow(ResultSet rs) throws Exception {
        Phong p = new Phong();
        p.setMaPhong     (rs.getString("maPhong"));
        p.setTenPhong    (rs.getString("tenPhong"));
        p.setMaLoaiPhong (rs.getString("maLoaiPhong"));
        p.setTrangThai   (rs.getString("trangThai"));
        // Đọc an toàn — nếu cột chưa tồn tại (DB chưa migrate) thì bỏ qua
        try { p.setTienNghi   (rs.getString("tienNghi")); } catch (Exception ignored) {}
        try { p.setSoNguoiToiDa(rs.getInt("soNguoiToiDa")); } catch (Exception ignored) {}
        try { p.setMoTa       (rs.getString("moTa")); } catch (Exception ignored) {}
        return p;
    }
}
