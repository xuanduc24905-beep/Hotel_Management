package com.lotuslaverne.service;

import com.lotuslaverne.dao.PhieuThuDAO;
import com.lotuslaverne.util.SessionContext;

import java.util.List;

public class PhieuThuService {

    /**
     * Thu tiền cọc cho một phiếu đặt phòng.
     * - Sinh mã PT tự động trong Service.
     * - maNhanVien lấy từ SessionContext (không hardcode).
     * @throws IllegalStateException nếu phiếu đã được thu cọc.
     */
    public String thuCoc(String maPhieuDatPhong, double soTienCoc,
                         String phuongThuc, String ghiChu) {
        PhieuThuDAO dao = new PhieuThuDAO();
        if (dao.daDuocThuCoc(maPhieuDatPhong))
            throw new IllegalStateException(
                "Phiếu đặt phòng " + maPhieuDatPhong + " đã được thu cọc trước đó!");

        String maPT = dao.generateMaPT();
        String maNV = SessionContext.getInstance().getMaNhanVien();
        boolean ok  = dao.taoPhieuThu(maPT, maPhieuDatPhong, maNV, soTienCoc, phuongThuc, ghiChu);
        if (!ok) throw new RuntimeException("Không tạo được phiếu thu cọc, kiểm tra kết nối DB.");
        return maPT;
    }

    /** Danh sách mã phiếu đặt phòng chưa checkout (DaDat hoặc DaCheckIn). */
    public List<String> loadPhieuChuaCheckIn() {
        return new PhieuThuDAO().loadPhieuChuaCheckIn();
    }
}
