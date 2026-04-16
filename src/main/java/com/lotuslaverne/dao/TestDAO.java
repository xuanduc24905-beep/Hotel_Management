package com.lotuslaverne.dao;

import com.lotuslaverne.entity.KhachHang;
import com.lotuslaverne.entity.Phong;

public class TestDAO {
    public static void main(String[] args) {
        System.out.println("Testing KhachHangDAO.getAll()...");
        KhachHangDAO dao = new KhachHangDAO();
        try {
            System.out.println("Size: " + dao.getAll().size());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        System.out.println("Testing KhachHangDAO.themKhachHang()...");
        boolean res = dao.themKhachHang(new KhachHang("TEST01", "Test Name", "090909090", "123456789"));
        System.out.println("Result: " + res);
        System.out.println("Size after: " + dao.getAll().size());
        
        PhongDAO pdao = new PhongDAO();
        System.out.println("Phong size: " + pdao.getAll().size());
    }
}
