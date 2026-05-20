package com.lotuslaverne.service;

import com.lotuslaverne.util.ConnectDB;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HuyDatServiceTest {

    // Case 1: huỷ trước 7 ngày → hoàn 100% cọc
    @Test
    void hoanDayDuKhiHuyTruoc7Ngay() {
        assertEquals(500_000, HuyDatService.tinhTienHoan(500_000, 10), 0.01);
    }

    // Case 2: huỷ trong 3–7 ngày → hoàn 50% cọc
    @Test
    void hoan50PhanTramKhiHuyTrong3Den7Ngay() {
        assertEquals(250_000, HuyDatService.tinhTienHoan(500_000, 5), 0.01);
    }

    // Case 3: huỷ trong 3 ngày → 0
    @Test
    void khongHoanKhiHuyTrong3Ngay() {
        assertEquals(0, HuyDatService.tinhTienHoan(500_000, 2), 0.01);
    }

    // Case 4: phiếu đã checkout → throw IllegalStateException
    @Test
    void throwKhiPhieuKhongHopLe() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true);
        when(rs.getString(1)).thenReturn("DaCheckOut");
        when(rs.getDate(2)).thenReturn(Date.valueOf("2026-12-31"));

        PreparedStatement pst = mock(PreparedStatement.class);
        when(pst.executeQuery()).thenReturn(rs);

        Connection con = mock(Connection.class);
        when(con.prepareStatement(anyString())).thenReturn(pst);

        ConnectDB db = mock(ConnectDB.class);
        when(db.getConnection()).thenReturn(con);

        try (MockedStatic<ConnectDB> mocked = mockStatic(ConnectDB.class)) {
            mocked.when(ConnectDB::getInstance).thenReturn(db);
            assertThrows(IllegalStateException.class,
                    () -> new HuyDatService().huyDat("PDP_TEST", "NV001"));
        }
    }
}
