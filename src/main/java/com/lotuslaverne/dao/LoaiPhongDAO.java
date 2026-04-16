package com.lotuslaverne.dao;

import com.lotuslaverne.entity.LoaiPhong;
import com.lotuslaverne.util.ConnectDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class LoaiPhongDAO {
    public List<LoaiPhong> getAll() {
        List<LoaiPhong> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;
        try {
            String sql = "SELECT * FROM LoaiPhong";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(new LoaiPhong(rs.getString("maLoaiPhong"), rs.getString("tenLoaiPhong"), "DangHoatDong"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
