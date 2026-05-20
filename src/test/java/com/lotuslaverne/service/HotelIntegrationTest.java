package com.lotuslaverne.service;

import com.lotuslaverne.util.ConnectDB;
import com.lotuslaverne.util.SessionContext;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration test toàn luồng: DatPhong → CheckIn → Checkout → HuyDat.
 * Tất cả DAO được mock — không cần DB thật.
 */
class HotelIntegrationTest {

    // ────────────────────────────────────────────────────────────────────────────
    // Helpers
    // ────────────────────────────────────────────────────────────────────────────

    private Connection mockCon() throws Exception {
        Connection con = mock(Connection.class);
        doNothing().when(con).setAutoCommit(anyBoolean());
        doNothing().when(con).commit();
        doNothing().when(con).rollback();
        return con;
    }

    private PreparedStatement pstReturning(ResultSet rs) throws Exception {
        PreparedStatement p = mock(PreparedStatement.class);
        when(p.executeQuery()).thenReturn(rs);
        when(p.executeUpdate()).thenReturn(1);
        return p;
    }

    private ResultSet rsWithInt(int val) throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(val);
        return rs;
    }

    private ResultSet rsWithDouble(double val) throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true);
        when(rs.getDouble(1)).thenReturn(val);
        return rs;
    }

    private ResultSet rsEmpty() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(false);
        return rs;
    }

    private ResultSet rsString(String... cols) throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, false);
        for (int i = 0; i < cols.length; i++)
            when(rs.getString(i + 1)).thenReturn(cols[i]);
        when(rs.getString(anyString())).thenReturn(cols.length > 0 ? cols[0] : null);
        return rs;
    }

    // ────────────────────────────────────────────────────────────────────────────
    // LUỒNG 1 — Happy path
    // ────────────────────────────────────────────────────────────────────────────

    /** DatPhong thành công khi phòng trống và giá tồn tại. */
    @Test
    void luong1_datPhong_phongTrong_taoPhieuThanhCong() throws Exception {
        // Chuỗi pst: [donGia query] [overlap check] [INSERT PDP] [INSERT ChiTiet] [INSERT PT]
        ResultSet rsDonGia  = rsWithDouble(500_000);
        ResultSet rsOverlap = rsWithInt(0); // phòng trống

        PreparedStatement pstDonGia  = pstReturning(rsDonGia);
        PreparedStatement pstOverlap = pstReturning(rsOverlap);
        PreparedStatement pstInsert  = mock(PreparedStatement.class);
        when(pstInsert.executeUpdate()).thenReturn(1);

        Connection con = mockCon();
        when(con.prepareStatement(anyString()))
            .thenReturn(pstDonGia)
            .thenReturn(pstOverlap)
            .thenReturn(pstInsert) // INSERT PDP
            .thenReturn(pstInsert) // INSERT ChiTiet
            .thenReturn(pstInsert); // INSERT PT

        ConnectDB db = mock(ConnectDB.class);
        when(db.getConnection()).thenReturn(con);

        SessionContext session = mock(SessionContext.class);
        when(session.getMaNhanVien()).thenReturn("NV001");

        try (MockedStatic<ConnectDB> mDb = mockStatic(ConnectDB.class);
             MockedStatic<SessionContext> mSc = mockStatic(SessionContext.class)) {
            mDb.when(ConnectDB::getInstance).thenReturn(db);
            mSc.when(SessionContext::getInstance).thenReturn(session);

            String maPDP = new DatPhongService().datPhong(
                "PDP_TEST", "KH001", 2, "P101",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                "", "TrucTiep", "PT001", "TienMat", null);

            assertEquals("PDP_TEST", maPDP);
        }
    }

    /** CheckIn thành công khi phiếu DaDat. */
    @Test
    void luong1_checkIn_phieuDaDat_chuyenDaCheckIn() throws Exception {
        ResultSet rsStatus = rsString("DaDat");

        PreparedStatement pstStatus = pstReturning(rsStatus);
        PreparedStatement pstUpdate = mock(PreparedStatement.class);
        when(pstUpdate.executeUpdate()).thenReturn(1);

        Connection con = mockCon();
        when(con.prepareStatement(anyString()))
            .thenReturn(pstStatus)
            .thenReturn(pstUpdate)  // UPDATE PhieuDatPhong
            .thenReturn(pstUpdate); // UPDATE Phong

        ConnectDB db = mock(ConnectDB.class);
        when(db.getConnection()).thenReturn(con);

        try (MockedStatic<ConnectDB> mDb = mockStatic(ConnectDB.class)) {
            mDb.when(ConnectDB::getInstance).thenReturn(db);
            assertDoesNotThrow(() -> new CheckInService().checkIn("PDP_TEST"));
        }
    }

    /** tienThanhToan = tienPhong + tienDV - tienCoc - khuyenMai, không âm. */
    @Test
    void luong1_checkout_tinhDungTienThanhToan() {
        // 3 đêm × 500k = 1.5M, DV 200k, cọc 750k, KM 0
        double result = CheckoutService.tinhTienThanhToan(1_500_000, 200_000, 750_000, 0);
        assertEquals(950_000, result, 0.01);
    }

    // ────────────────────────────────────────────────────────────────────────────
    // LUỒNG 2 — Hủy phòng
    // ────────────────────────────────────────────────────────────────────────────

    /** Hủy trước hơn 7 ngày → hoàn 100% cọc. */
    @Test
    void luong2_huyTruoc7Ngay_hoan100Phan() {
        double tienHoan = HuyDatService.tinhTienHoan(500_000, 10);
        assertEquals(500_000, tienHoan, 0.01);
    }

    /** Hủy trong khoảng 3–7 ngày → hoàn 50% cọc. */
    @Test
    void luong2_huyTrong3Den7Ngay_hoan50Phan() {
        double tienHoan = HuyDatService.tinhTienHoan(500_000, 5);
        assertEquals(250_000, tienHoan, 0.01);
    }

    /** Hủy trong 3 ngày (no-show) → hoàn 0%. */
    @Test
    void luong2_huyTrong3Ngay_hoan0Phan() {
        double tienHoan = HuyDatService.tinhTienHoan(500_000, 1);
        assertEquals(0, tienHoan, 0.01);
    }

    // ────────────────────────────────────────────────────────────────────────────
    // LUỒNG 3 — Edge cases
    // ────────────────────────────────────────────────────────────────────────────

    /** Đặt phòng trùng lịch → throw IllegalStateException. */
    @Test
    void luong3_datPhongTrungLich_throwException() throws Exception {
        ResultSet rsDonGia  = rsWithDouble(500_000);
        ResultSet rsOverlap = rsWithInt(1); // có overlap!

        PreparedStatement pstDonGia  = pstReturning(rsDonGia);
        PreparedStatement pstOverlap = pstReturning(rsOverlap);

        Connection con = mockCon();
        when(con.prepareStatement(anyString()))
            .thenReturn(pstDonGia)
            .thenReturn(pstOverlap);

        ConnectDB db = mock(ConnectDB.class);
        when(db.getConnection()).thenReturn(con);

        SessionContext session = mock(SessionContext.class);
        when(session.getMaNhanVien()).thenReturn("NV001");

        try (MockedStatic<ConnectDB> mDb = mockStatic(ConnectDB.class);
             MockedStatic<SessionContext> mSc = mockStatic(SessionContext.class)) {
            mDb.when(ConnectDB::getInstance).thenReturn(db);
            mSc.when(SessionContext::getInstance).thenReturn(session);

            assertThrows(IllegalStateException.class, () ->
                new DatPhongService().datPhong(
                    "PDP_X", "KH001", 2, "P101",
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                    "", "TrucTiep", null, null, null));
        }
    }

    /** Checkout khi phiếu chưa check-in → throw IllegalStateException. */
    @Test
    void luong3_checkoutChuaCheckIn_throwException() throws Exception {
        ResultSet rsStatus = mock(ResultSet.class);
        when(rsStatus.next()).thenReturn(true);
        when(rsStatus.getString(1)).thenReturn("P101");
        when(rsStatus.getLong(2)).thenReturn(2L);
        when(rsStatus.getString(3)).thenReturn("DaDat"); // không phải DaCheckIn!
        when(rsStatus.getDouble(4)).thenReturn(0.0);

        PreparedStatement pstStatus = pstReturning(rsStatus);
        Connection con = mockCon();
        when(con.prepareStatement(anyString())).thenReturn(pstStatus);

        ConnectDB db = mock(ConnectDB.class);
        when(db.getConnection()).thenReturn(con);

        try (MockedStatic<ConnectDB> mDb = mockStatic(ConnectDB.class)) {
            mDb.when(ConnectDB::getInstance).thenReturn(db);
            assertThrows(IllegalStateException.class, () ->
                new CheckoutService().checkout("PDP_CHUA_CI", "NV001", "TienMat", ""));
        }
    }

    /** Áp mã KM hết hạn → throw IllegalArgumentException. */
    @Test
    void luong3_maKMHetHan_throwException() throws Exception {
        ResultSet rsDonGia = rsWithDouble(500_000);
        ResultSet rsKM     = rsWithInt(0); // count = 0 → hết hạn

        PreparedStatement pstDonGia = pstReturning(rsDonGia);
        PreparedStatement pstKM     = pstReturning(rsKM);

        Connection con = mockCon();
        when(con.prepareStatement(anyString()))
            .thenReturn(pstDonGia)
            .thenReturn(pstKM);

        ConnectDB db = mock(ConnectDB.class);
        when(db.getConnection()).thenReturn(con);

        SessionContext session = mock(SessionContext.class);
        when(session.getMaNhanVien()).thenReturn("NV001");

        try (MockedStatic<ConnectDB> mDb = mockStatic(ConnectDB.class);
             MockedStatic<SessionContext> mSc = mockStatic(SessionContext.class)) {
            mDb.when(ConnectDB::getInstance).thenReturn(db);
            mSc.when(SessionContext::getInstance).thenReturn(session);

            assertThrows(IllegalArgumentException.class, () ->
                new DatPhongService().datPhong(
                    "PDP_KM", "KH001", 2, "P101",
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                    "", "TrucTiep", null, null, "KM_HET_HAN"));
        }
    }

    /** cọc > tổng tiền → tienThanhToan = 0, không âm. */
    @Test
    void luong3_cocLonHonTong_tienThanhToanKhongAm() {
        double result = CheckoutService.tinhTienThanhToan(500_000, 0, 800_000, 0);
        assertEquals(0, result, 0.01);
    }
}
