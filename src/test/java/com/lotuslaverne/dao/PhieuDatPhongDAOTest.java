package com.lotuslaverne.dao;

import com.lotuslaverne.util.ConnectDB;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PhieuDatPhongDAOTest {

    private final PhieuDatPhongDAO dao = new PhieuDatPhongDAO();

    private ConnectDB buildMock(int countResult) throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(countResult);

        PreparedStatement pst = mock(PreparedStatement.class);
        when(pst.executeQuery()).thenReturn(rs);

        Connection con = mock(Connection.class);
        when(con.prepareStatement(anyString())).thenReturn(pst);

        ConnectDB db = mock(ConnectDB.class);
        when(db.getConnection()).thenReturn(con);
        return db;
    }

    @Test
    void phongTrong_khiKhongCoPhieuNao() throws Exception {
        // COUNT = 0 → không có phiếu đặt nào → trống
        ConnectDB db = buildMock(0);
        try (MockedStatic<ConnectDB> mocked = mockStatic(ConnectDB.class)) {
            mocked.when(ConnectDB::getInstance).thenReturn(db);
            assertTrue(dao.isPhongTrong("P101",
                    LocalDate.of(2026, 6, 10),
                    LocalDate.of(2026, 6, 13)));
        }
    }

    @Test
    void phongBan_khiDatTrungGiuaKy() throws Exception {
        // COUNT = 1 → có phiếu trùng giữa kỳ → bận
        ConnectDB db = buildMock(1);
        try (MockedStatic<ConnectDB> mocked = mockStatic(ConnectDB.class)) {
            mocked.when(ConnectDB::getInstance).thenReturn(db);
            assertFalse(dao.isPhongTrong("P101",
                    LocalDate.of(2026, 6, 10),
                    LocalDate.of(2026, 6, 13)));
        }
    }

    @Test
    void phongTrong_khachMoiVaoDungNgayKhachCuTra() throws Exception {
        // Business rule: khách trả trước 12h, dọn phòng 14h, khách mới check-in chiều cùng ngày → hợp lệ
        //
        // Khách cũ : vào 7/6, trả 10/6
        // Khách mới: vào 10/6, trả 13/6
        //
        // NOT (thoiGianTraDuKien <= ngayVaoMoi  OR  thoiGianNhanDuKien >= ngayTraMoi)
        // NOT (10/6             <= 10/6          OR  7/6               >= 13/6)
        // NOT (TRUE                              OR  FALSE)
        // NOT (TRUE) = FALSE  → khách cũ không được đếm → COUNT = 0 → trống ✓
        ConnectDB db = buildMock(0);
        try (MockedStatic<ConnectDB> mocked = mockStatic(ConnectDB.class)) {
            mocked.when(ConnectDB::getInstance).thenReturn(db);
            assertTrue(dao.isPhongTrong("P101",
                    LocalDate.of(2026, 6, 10),
                    LocalDate.of(2026, 6, 13)));
        }
    }

    @Test
    void phongBan_khiNgayMoiOverlapRoRang() throws Exception {
        // Khách cũ : vào 7/6, trả 10/6
        // Khách mới: vào 9/6, trả 12/6  ← cố đặt chồng lên
        //
        // NOT (10/6 <= 9/6  OR  7/6 >= 12/6)
        // NOT (FALSE        OR  FALSE)
        // NOT (FALSE) = TRUE  → khách cũ được đếm → COUNT = 1 → bận ✓
        ConnectDB db = buildMock(1);
        try (MockedStatic<ConnectDB> mocked = mockStatic(ConnectDB.class)) {
            mocked.when(ConnectDB::getInstance).thenReturn(db);
            assertFalse(dao.isPhongTrong("P101",
                    LocalDate.of(2026, 6, 9),
                    LocalDate.of(2026, 6, 12)));
        }
    }
}
