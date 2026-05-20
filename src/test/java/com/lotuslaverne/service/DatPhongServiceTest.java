package com.lotuslaverne.service;

import com.lotuslaverne.util.ConnectDB;
import com.lotuslaverne.util.SessionContext;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatPhongServiceTest {

    // Helpers để mock Connection trả về donGia và trangThai phòng trống
    private Connection mockConWithDonGia(double donGia, int overlapCount) throws Exception {
        // Resultset 1: BangGia donGia
        ResultSet rsDonGia = mock(ResultSet.class);
        when(rsDonGia.next()).thenReturn(donGia > 0);
        when(rsDonGia.getDouble(1)).thenReturn(donGia);

        // Resultset 2: overlap check
        ResultSet rsOverlap = mock(ResultSet.class);
        when(rsOverlap.next()).thenReturn(true);
        when(rsOverlap.getInt(1)).thenReturn(overlapCount);

        PreparedStatement pstDonGia  = mock(PreparedStatement.class);
        PreparedStatement pstOverlap = mock(PreparedStatement.class);
        when(pstDonGia.executeQuery()).thenReturn(rsDonGia);
        when(pstOverlap.executeQuery()).thenReturn(rsOverlap);

        Connection con = mock(Connection.class);
        // First prepareStatement → donGia query, second → overlap check
        when(con.prepareStatement(anyString()))
            .thenReturn(pstDonGia)
            .thenReturn(pstOverlap);
        return con;
    }

    // Case 1: Phòng đã có lịch trùng ngày → throw IllegalStateException
    @Test
    void throwKhiPhongTrungNgay() throws Exception {
        Connection con = mockConWithDonGia(500_000, 1);
        ConnectDB db = mock(ConnectDB.class);
        when(db.getConnection()).thenReturn(con);

        SessionContext session = mock(SessionContext.class);
        when(session.getMaNhanVien()).thenReturn("NV001");

        try (MockedStatic<ConnectDB> mockedDb = mockStatic(ConnectDB.class);
             MockedStatic<SessionContext> mockedSc = mockStatic(SessionContext.class)) {
            mockedDb.when(ConnectDB::getInstance).thenReturn(db);
            mockedSc.when(SessionContext::getInstance).thenReturn(session);

            assertThrows(IllegalStateException.class, () ->
                new DatPhongService().datPhong(
                    "PDP001", "KH001", 2, "P101",
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                    "", "TrucTiep", null, null, null));
        }
    }

    // Case 2: Không có giá hiệu lực → throw IllegalStateException
    @Test
    void throwKhiKhongCoGiaHieuLuc() throws Exception {
        ResultSet rsEmpty = mock(ResultSet.class);
        when(rsEmpty.next()).thenReturn(false);

        PreparedStatement pst = mock(PreparedStatement.class);
        when(pst.executeQuery()).thenReturn(rsEmpty);

        Connection con = mock(Connection.class);
        when(con.prepareStatement(anyString())).thenReturn(pst);

        ConnectDB db = mock(ConnectDB.class);
        when(db.getConnection()).thenReturn(con);

        SessionContext session = mock(SessionContext.class);
        when(session.getMaNhanVien()).thenReturn("NV001");

        try (MockedStatic<ConnectDB> mockedDb = mockStatic(ConnectDB.class);
             MockedStatic<SessionContext> mockedSc = mockStatic(SessionContext.class)) {
            mockedDb.when(ConnectDB::getInstance).thenReturn(db);
            mockedSc.when(SessionContext::getInstance).thenReturn(session);

            assertThrows(IllegalStateException.class, () ->
                new DatPhongService().datPhong(
                    "PDP001", "KH001", 2, "P101",
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                    "", "TrucTiep", null, null, null));
        }
    }

    // Case 3: Mã KM hết hạn → throw IllegalArgumentException
    @Test
    void throwKhiMaKMHetHan() throws Exception {
        // donGia result
        ResultSet rsDonGia = mock(ResultSet.class);
        when(rsDonGia.next()).thenReturn(true);
        when(rsDonGia.getDouble(1)).thenReturn(500_000.0);

        // KM validation → count = 0 (không hợp lệ)
        ResultSet rsKM = mock(ResultSet.class);
        when(rsKM.next()).thenReturn(true);
        when(rsKM.getInt(1)).thenReturn(0);

        PreparedStatement pstDonGia = mock(PreparedStatement.class);
        PreparedStatement pstKM     = mock(PreparedStatement.class);
        when(pstDonGia.executeQuery()).thenReturn(rsDonGia);
        when(pstKM.executeQuery()).thenReturn(rsKM);

        Connection con = mock(Connection.class);
        when(con.prepareStatement(anyString()))
            .thenReturn(pstDonGia)
            .thenReturn(pstKM);

        ConnectDB db = mock(ConnectDB.class);
        when(db.getConnection()).thenReturn(con);

        SessionContext session = mock(SessionContext.class);
        when(session.getMaNhanVien()).thenReturn("NV001");

        try (MockedStatic<ConnectDB> mockedDb = mockStatic(ConnectDB.class);
             MockedStatic<SessionContext> mockedSc = mockStatic(SessionContext.class)) {
            mockedDb.when(ConnectDB::getInstance).thenReturn(db);
            mockedSc.when(SessionContext::getInstance).thenReturn(session);

            assertThrows(IllegalArgumentException.class, () ->
                new DatPhongService().datPhong(
                    "PDP001", "KH001", 2, "P101",
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                    "", "TrucTiep", null, null, "KM_HET_HAN"));
        }
    }
}
