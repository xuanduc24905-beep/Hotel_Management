package com.lotuslaverne.dao;

import com.lotuslaverne.entity.KhuyenMai;
import com.lotuslaverne.util.ConnectDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KhuyenMaiDAO {

    public List<KhuyenMai> getAll() {
        List<KhuyenMai> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;
        try {
            PreparedStatement pst = con.prepareStatement("SELECT * FROM KhuyenMai ORDER BY ngayApDung DESC");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<KhuyenMai> layTatCaKhuyenMai() {
        List<KhuyenMai> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;
        try {
            PreparedStatement pst = con.prepareStatement(
                "SELECT * FROM KhuyenMai WHERE ngayKetThuc > GETDATE() AND ngayApDung <= GETDATE()");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public boolean them(KhuyenMai km) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            PreparedStatement pst = con.prepareStatement(
                "INSERT INTO KhuyenMai (maKhuyenMai,tenKhuyenMai,ngayApDung,ngayKetThuc,phanTramGiam,dieuKienApDung) VALUES (?,?,?,?,?,?)");
            pst.setString(1, km.getMaKhuyenMai());
            pst.setString(2, km.getTenKhuyenMai());
            pst.setTimestamp(3, km.getNgayApDung());
            pst.setTimestamp(4, km.getNgayKetThuc());
            pst.setDouble(5, km.getPhanTramGiam());
            pst.setString(6, km.getDieuKienApDung());
            return pst.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean sua(KhuyenMai km) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            PreparedStatement pst = con.prepareStatement(
                "UPDATE KhuyenMai SET tenKhuyenMai=?,ngayApDung=?,ngayKetThuc=?,phanTramGiam=?,dieuKienApDung=? WHERE maKhuyenMai=?");
            pst.setString(1, km.getTenKhuyenMai());
            pst.setTimestamp(2, km.getNgayApDung());
            pst.setTimestamp(3, km.getNgayKetThuc());
            pst.setDouble(4, km.getPhanTramGiam());
            pst.setString(5, km.getDieuKienApDung());
            pst.setString(6, km.getMaKhuyenMai());
            return pst.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean xoa(String maKM) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            PreparedStatement pst = con.prepareStatement("DELETE FROM KhuyenMai WHERE maKhuyenMai=?");
            pst.setString(1, maKM);
            return pst.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    private KhuyenMai mapRow(ResultSet rs) throws SQLException {
        return new KhuyenMai(
            rs.getString("maKhuyenMai"),
            rs.getString("tenKhuyenMai"),
            rs.getTimestamp("ngayApDung"),
            rs.getTimestamp("ngayKetThuc"),
            rs.getDouble("phanTramGiam"),
            rs.getString("dieuKienApDung")
        );
    }
}
