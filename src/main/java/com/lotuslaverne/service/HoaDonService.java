package com.lotuslaverne.service;

import com.lotuslaverne.dao.HoaDonDAO;

public class HoaDonService {

    /**
     * Lấy toàn bộ dữ liệu chi tiết hóa đơn để xuất PDF.
     * Trả về Object[] theo format của HoaDonDAO.getChiTietForPdf(), hoặc null nếu không tìm thấy.
     */
    public Object[] layChiTietHoaDon(String maHoaDon) {
        return new HoaDonDAO().getChiTietForPdf(maHoaDon);
    }

    /**
     * Dịch mã phương thức thanh toán sang tên hiển thị tiếng Việt.
     * Mã không tồn tại → trả về mã gốc.
     */
    public String dichPhuongThuc(String ma) {
        if (ma == null) return "";
        return switch (ma) {
            case "TienMat"     -> "Tiền Mặt";
            case "ChuyenKhoan" -> "Chuyển Khoản";
            default            -> ma;
        };
    }
}
