package com.lotuslaverne.service;

import com.lotuslaverne.util.ConnectDB;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DoiPhongServiceTest {

    // Case 1: Tìm thấy khách đang ở → trả về đúng 8 trường với đúng kiểu
    @Test
    void traVeDuLieuKhiTimThayKhach() throws Exception {
        Timestamp tsNhan = Timestamp.valueOf(LocalDateTime.of(2026, 5, 18, 14, 0));
        Timestamp tsTra  = Timestamp.valueOf(LocalDateTime.of(2026, 5, 25, 12, 0));

        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true);
        when(rs.getString("hoTenKH")).thenReturn("Nguyen Van A");
        when(rs.getString("cmnd")).thenReturn("001234567890");
        when(rs.getString("maPhieuDatPhong")).thenReturn("PDP001");
        when(rs.getTimestamp("thoiGianNhanThucTe")).thenReturn(tsNhan);
        when(rs.getTimestamp("thoiGianTraDuKien")).thenReturn(tsTra);
        when(rs.getString("maPhong")).thenReturn("P101");
        when(rs.getString("tenLoaiPhong")).thenReturn("Deluxe");
        when(rs.getDouble("donGia")).thenReturn(500_000.0);

        PreparedStatement pst = mock(PreparedStatement.class);
        when(pst.executeQuery()).thenReturn(rs);
        Connection con = mock(Connection.class);
        when(con.prepareStatement(anyString())).thenReturn(pst);
        ConnectDB db = mock(ConnectDB.class);
        when(db.getConnection()).thenReturn(con);

        try (MockedStatic<ConnectDB> mocked = mockStatic(ConnectDB.class)) {
            mocked.when(ConnectDB::getInstance).thenReturn(db);
            Object[] result = new DoiPhongService().traCuuBySdt("0901234567");
            assertNotNull(result);
            assertEquals("Nguyen Van A",           result[0]);
            assertEquals("001234567890",           result[1]);
            assertEquals("PDP001",                 result[2]);
            assertEquals(tsNhan.toLocalDateTime(), result[3]);
            assertEquals(tsTra.toLocalDateTime(),  result[4]);
            assertEquals("P101",                   result[5]);
            assertEquals("Deluxe",                 result[6]);
            assertEquals(500_000.0, (double) result[7], 0.01);
        }
    }

    // Case 2: SĐT không tồn tại → trả về null
    @Test
    void traVeNullKhiSdtKhongTonTai() throws Exception {
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
            assertNull(new DoiPhongService().traCuuBySdt("0999999999"));
        }
    }

    // Case 3: SQL encode điều kiện "đang ở" (thoiGianNhanThucTe IS NOT NULL AND thoiGianTraThucTe IS NULL)
    @Test
    void sqlEncodeDieuKienLoaiKhachDaCheckout() throws Exception {
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
            new DoiPhongService().traCuuBySdt("0901234567");
            verify(con).prepareStatement(argThat(sql ->
                    sql.contains("thoiGianNhanThucTe IS NOT NULL") &&
                    sql.contains("thoiGianTraThucTe IS NULL")));
        }
    }

    // Case 4 (O-3): phiếu không ở DaCheckIn → throw IllegalStateException
    @Test
    void throwKhiPhieuChuaCheckIn() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true);
        when(rs.getString(1)).thenReturn("DaDat");
        when(rs.getTimestamp(2)).thenReturn(Timestamp.valueOf(LocalDateTime.now().plusDays(1)));
        when(rs.getTimestamp(3)).thenReturn(Timestamp.valueOf(LocalDateTime.now().plusDays(3)));

        PreparedStatement pst = mock(PreparedStatement.class);
        when(pst.executeQuery()).thenReturn(rs);
        Connection con = mock(Connection.class);
        when(con.prepareStatement(anyString())).thenReturn(pst);
        ConnectDB db = mock(ConnectDB.class);
        when(db.getConnection()).thenReturn(con);

        try (MockedStatic<ConnectDB> mocked = mockStatic(ConnectDB.class)) {
            mocked.when(ConnectDB::getInstance).thenReturn(db);
            assertThrows(IllegalStateException.class,
                    () -> new DoiPhongService().doiPhong("PDP001", "P102"));
        }
    }

    // Case 5 (O-4): phòng mới đã có lịch trùng → throw IllegalStateException
    @Test
    void throwKhiPhongMoiTrungLich() throws Exception {
        // Query 1 — trangThai: DaCheckIn
        ResultSet rsPhieu = mock(ResultSet.class);
        when(rsPhieu.next()).thenReturn(true);
        when(rsPhieu.getString(1)).thenReturn("DaCheckIn");
        when(rsPhieu.getTimestamp(2)).thenReturn(Timestamp.valueOf(LocalDateTime.now().plusDays(1)));
        when(rsPhieu.getTimestamp(3)).thenReturn(Timestamp.valueOf(LocalDateTime.now().plusDays(3)));

        // Query 2 — overlap check: 1 conflict
        ResultSet rsOverlap = mock(ResultSet.class);
        when(rsOverlap.next()).thenReturn(true);
        when(rsOverlap.getInt(1)).thenReturn(1);

        PreparedStatement pst1 = mock(PreparedStatement.class);
        PreparedStatement pst2 = mock(PreparedStatement.class);
        when(pst1.executeQuery()).thenReturn(rsPhieu);
        when(pst2.executeQuery()).thenReturn(rsOverlap);

        Connection con = mock(Connection.class);
        when(con.prepareStatement(anyString())).thenReturn(pst1).thenReturn(pst2);
        ConnectDB db = mock(ConnectDB.class);
        when(db.getConnection()).thenReturn(con);

        try (MockedStatic<ConnectDB> mocked = mockStatic(ConnectDB.class)) {
            mocked.when(ConnectDB::getInstance).thenReturn(db);
            assertThrows(IllegalStateException.class,
                    () -> new DoiPhongService().doiPhong("PDP001", "P102"));
        }
    }
}
