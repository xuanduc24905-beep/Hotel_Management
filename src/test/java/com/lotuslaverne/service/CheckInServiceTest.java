package com.lotuslaverne.service;

import com.lotuslaverne.util.ConnectDB;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CheckInServiceTest {

    // Case 1: check-in hợp lệ (DaDat) → không throw, transaction commit
    @Test
    void checkInHopLe() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true);
        when(rs.getString(1)).thenReturn("DaDat");

        PreparedStatement pstValidate = mock(PreparedStatement.class);
        PreparedStatement pstUpdate1  = mock(PreparedStatement.class);
        PreparedStatement pstUpdate2  = mock(PreparedStatement.class);
        when(pstValidate.executeQuery()).thenReturn(rs);

        Connection con = mock(Connection.class);
        when(con.prepareStatement(anyString()))
            .thenReturn(pstValidate)
            .thenReturn(pstUpdate1)
            .thenReturn(pstUpdate2);

        ConnectDB db = mock(ConnectDB.class);
        when(db.getConnection()).thenReturn(con);

        try (MockedStatic<ConnectDB> mocked = mockStatic(ConnectDB.class)) {
            mocked.when(ConnectDB::getInstance).thenReturn(db);
            assertDoesNotThrow(() -> new CheckInService().checkIn("PDP001"));
            verify(con).commit();
        }
    }

    // Case 2: phiếu không tồn tại → throw IllegalArgumentException
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
            assertThrows(IllegalArgumentException.class,
                    () -> new CheckInService().checkIn("PDP_KHONG_TON_TAI"));
        }
    }

    // Case 3: phiếu đã check-in rồi → throw IllegalStateException
    @Test
    void throwKhiPhieuDaCheckIn() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true);
        when(rs.getString(1)).thenReturn("DaCheckIn");

        PreparedStatement pst = mock(PreparedStatement.class);
        when(pst.executeQuery()).thenReturn(rs);

        Connection con = mock(Connection.class);
        when(con.prepareStatement(anyString())).thenReturn(pst);

        ConnectDB db = mock(ConnectDB.class);
        when(db.getConnection()).thenReturn(con);

        try (MockedStatic<ConnectDB> mocked = mockStatic(ConnectDB.class)) {
            mocked.when(ConnectDB::getInstance).thenReturn(db);
            assertThrows(IllegalStateException.class,
                    () -> new CheckInService().checkIn("PDP001"));
        }
    }
}
