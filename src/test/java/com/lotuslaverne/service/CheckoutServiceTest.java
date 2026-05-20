package com.lotuslaverne.service;

import com.lotuslaverne.util.ConnectDB;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CheckoutServiceTest {

    // Case 1: tiền phòng + dịch vụ - cọc = đúng
    @Test
    void tinhDungTongTien() {
        double tienPhong = 1_000_000;
        double tienDV    =   200_000;
        double tienCoc   =   300_000;
        assertEquals(900_000, CheckoutService.tinhTienThanhToan(tienPhong, tienDV, tienCoc, 0), 0.01);
    }

    // Case 2: cọc lớn hơn tổng → kết quả không được âm
    @Test
    void truDungCocKhongAmSo() {
        double tienPhong = 300_000;
        double tienDV    =       0;
        double tienCoc   = 500_000;
        assertEquals(0, CheckoutService.tinhTienThanhToan(tienPhong, tienDV, tienCoc, 0), 0.01);
    }

    // Case 4: có khuyến mãi 10% → trừ vào tiền phòng
    @Test
    void tinhDungKhiCoKhuyenMai() {
        double tienPhong     = 1_000_000;
        double tienDV        =   200_000;
        double tienCoc       =   300_000;
        double tienKhuyenMai =   100_000; // 10% of tienPhong
        assertEquals(800_000, CheckoutService.tinhTienThanhToan(tienPhong, tienDV, tienCoc, tienKhuyenMai), 0.01);
    }

    // Case 3: phiếu không tồn tại → throw IllegalArgumentException
    @Test
    void throwKhiPhieuKhongTonTai() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(false);

        PreparedStatement pst = mock(PreparedStatement.class);
        when(pst.executeQuery()).thenReturn(rs);

        Connection con = mock(Connection.class);
        when(con.prepareStatement(anyString())).thenReturn(pst);

        ConnectDB db = mock(ConnectDB.class);
        when(db.getConnection()).thenReturn(con);

        try (MockedStatic<ConnectDB> mocked = mockStatic(ConnectDB.class)) {
            mocked.when(ConnectDB::getInstance).thenReturn(db);
            CheckoutService svc = new CheckoutService();
            assertThrows(IllegalArgumentException.class,
                    () -> svc.checkout("PDP_KHONG_TON_TAI", "NV001", "TienMat", ""));
        }
    }
}
