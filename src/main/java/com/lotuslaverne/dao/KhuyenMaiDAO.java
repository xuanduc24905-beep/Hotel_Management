package com.lotuslaverne.dao;

import com.lotuslaverne.entity.KhuyenMai;
import com.lotuslaverne.util.ConnectDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class KhuyenMaiDAO {
    public List<KhuyenMai> layTatCaKhuyenMai() {
        List<KhuyenMai> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;
        try {
            String sql = "SELECT * FROM KhuyenMai WHERE ngayKetThuc > GETDATE()";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(new KhuyenMai(
                    rs.getString("maKhuyenMai"),
                    rs.getString("tenKhuyenMai"),
                    rs.getDouble("phanTramGiam"),
                    rs.getDouble("soTienGiam"),
                    rs.getTimestamp("ngayKetThuc")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
