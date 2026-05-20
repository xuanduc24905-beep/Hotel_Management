package com.lotuslaverne.service;

import com.lotuslaverne.dao.HoaDonDAO;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HoaDonServiceTest {

    private final HoaDonService svc = new HoaDonService();

    // ── dichPhuongThuc ──────────────────────────────────────────────────

    @Test
    void dichPhuongThuc_TienMat() {
        assertEquals("Tiền Mặt", svc.dichPhuongThuc("TienMat"));
    }

    @Test
    void dichPhuongThuc_ChuyenKhoan() {
        assertEquals("Chuyển Khoản", svc.dichPhuongThuc("ChuyenKhoan"));
    }

    @Test
    void dichPhuongThuc_maKhacTraVeMaGoc() {
        assertEquals("ThePay", svc.dichPhuongThuc("ThePay"));
    }

    @Test
    void dichPhuongThuc_nullTraVeRong() {
        assertEquals("", svc.dichPhuongThuc(null));
    }

    // ── layChiTietHoaDon ────────────────────────────────────────────────

    @Test
    void layChiTietHoaDon_happyPath() {
        Object[] data = new Object[]{"PDP001", "Nguyen A", "P101", "Phong 101",
                "Standard", "20/05/2026", "22/05/2026", 2L, 500_000.0,
                1_000_000.0, 0.0, 1_000_000.0, "TienMat", null, "NV001", null, 0.0};
        try (MockedConstruction<HoaDonDAO> mc = mockConstruction(HoaDonDAO.class,
                (m, ctx) -> when(m.getChiTietForPdf("HD001")).thenReturn(data))) {
            Object[] result = svc.layChiTietHoaDon("HD001");
            assertNotNull(result);
            assertEquals("PDP001", result[0]);
        }
    }

    @Test
    void layChiTietHoaDon_khongTimThay_traVeNull() {
        try (MockedConstruction<HoaDonDAO> mc = mockConstruction(HoaDonDAO.class,
                (m, ctx) -> when(m.getChiTietForPdf(anyString())).thenReturn(null))) {
            assertNull(svc.layChiTietHoaDon("HDKHONGTON"));
        }
    }
}
