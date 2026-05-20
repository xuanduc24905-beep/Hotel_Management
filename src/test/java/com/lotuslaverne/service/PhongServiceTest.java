package com.lotuslaverne.service;

import com.lotuslaverne.dao.PhongDAO;
import com.lotuslaverne.entity.Phong;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PhongServiceTest {

    private final PhongService svc = new PhongService();

    // ── capNhatTrangThai ────────────────────────────────────────────────

    @Test
    void capNhatTrangThai_hopleLe_pass() {
        try (MockedConstruction<PhongDAO> mc = mockConstruction(PhongDAO.class,
                (m, ctx) -> when(m.capNhatTrangThai("P101", "PhongTrong")).thenReturn(true))) {
            assertTrue(svc.capNhatTrangThai("P101", "PhongTrong"));
        }
    }

    @Test
    void capNhatTrangThai_DangSuDung_pass() {
        try (MockedConstruction<PhongDAO> mc = mockConstruction(PhongDAO.class,
                (m, ctx) -> when(m.capNhatTrangThai("P101", "DangSuDung")).thenReturn(true))) {
            assertTrue(svc.capNhatTrangThai("P101", "DangSuDung"));
        }
    }

    @Test
    void capNhatTrangThai_PhongCanDon_pass() {
        try (MockedConstruction<PhongDAO> mc = mockConstruction(PhongDAO.class,
                (m, ctx) -> when(m.capNhatTrangThai("P101", "PhongCanDon")).thenReturn(true))) {
            assertTrue(svc.capNhatTrangThai("P101", "PhongCanDon"));
        }
    }

    @Test
    void capNhatTrangThai_BaoDuong_pass() {
        try (MockedConstruction<PhongDAO> mc = mockConstruction(PhongDAO.class,
                (m, ctx) -> when(m.capNhatTrangThai("P101", "BaoDuong")).thenReturn(true))) {
            assertTrue(svc.capNhatTrangThai("P101", "BaoDuong"));
        }
    }

    @Test
    void capNhatTrangThai_khongHopLe_throwIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
            () -> svc.capNhatTrangThai("P101", "DaHuy"));
    }

    @Test
    void capNhatTrangThai_BaoTri_khongHopLe_throwIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
            () -> svc.capNhatTrangThai("P101", "BaoTri"));
    }

    @Test
    void capNhatTrangThai_PhongDat_khongHopLe_throwIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
            () -> svc.capNhatTrangThai("P101", "PhongDat"));
    }

    // ── themPhong ───────────────────────────────────────────────────────

    @Test
    void themPhong_sinhMaTuDong_khiMaRong() {
        Phong phong = new Phong();
        phong.setMaPhong("");
        phong.setTenPhong("Phòng Test");

        try (MockedConstruction<PhongDAO> mc = mockConstruction(PhongDAO.class,
                (m, ctx) -> when(m.themPhong(any())).thenReturn(true))) {
            assertTrue(svc.themPhong(phong));
            assertNotNull(phong.getMaPhong());
            assertFalse(phong.getMaPhong().isBlank());
        }
    }

    @Test
    void themPhong_giuMaCoSan_khiMaKhongRong() {
        Phong phong = new Phong();
        phong.setMaPhong("P999");
        phong.setTenPhong("Phòng Test 999");

        try (MockedConstruction<PhongDAO> mc = mockConstruction(PhongDAO.class,
                (m, ctx) -> when(m.themPhong(any())).thenReturn(true))) {
            assertTrue(svc.themPhong(phong));
            assertEquals("P999", phong.getMaPhong());
        }
    }

    // ── loadPhongTheo ───────────────────────────────────────────────────

    @Test
    void loadPhongTheo_filterTheoTrangThai() {
        Phong p1 = new Phong(); p1.setMaPhong("P101"); p1.setTrangThai("PhongTrong");
        Phong p2 = new Phong(); p2.setMaPhong("P102"); p2.setTrangThai("DangSuDung");
        try (MockedConstruction<PhongDAO> mc = mockConstruction(PhongDAO.class,
                (m, ctx) -> when(m.getAll()).thenReturn(List.of(p1, p2)))) {
            List<Phong> result = svc.loadPhongTheo("PhongTrong");
            assertEquals(1, result.size());
            assertEquals("P101", result.get(0).getMaPhong());
        }
    }

    @Test
    void loadPhongTheo_nullTraVeTatCa() {
        Phong p1 = new Phong(); p1.setMaPhong("P101"); p1.setTrangThai("PhongTrong");
        Phong p2 = new Phong(); p2.setMaPhong("P102"); p2.setTrangThai("DangSuDung");
        try (MockedConstruction<PhongDAO> mc = mockConstruction(PhongDAO.class,
                (m, ctx) -> when(m.getAll()).thenReturn(List.of(p1, p2)))) {
            List<Phong> result = svc.loadPhongTheo(null);
            assertEquals(2, result.size());
        }
    }

    // ── loadPhongTheoNgay ───────────────────────────────────────────────

    @Test
    void loadPhongTheoNgay_happyPath() {
        List<String[]> data = List.<String[]>of(new String[]{"P101", "101", "Standard", "500000", "WiFi"});
        try (MockedConstruction<PhongDAO> mc = mockConstruction(PhongDAO.class,
                (m, ctx) -> when(m.searchAvailable(any(), any(), any(),
                        anyBoolean(), anyBoolean(), anyBoolean(),
                        anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(data))) {
            List<String[]> result = svc.loadPhongTheoNgay(LocalDate.now(), LocalDate.now().plusDays(2));
            assertEquals(1, result.size());
        }
    }
}
