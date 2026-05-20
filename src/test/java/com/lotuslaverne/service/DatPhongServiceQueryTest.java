package com.lotuslaverne.service;

import com.lotuslaverne.dao.KhachHangDAO;
import com.lotuslaverne.dao.PhieuDatPhongDAO;
import com.lotuslaverne.dao.PhongDAO;
import com.lotuslaverne.entity.KhachHang;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatPhongServiceQueryTest {

    private final DatPhongService svc = new DatPhongService();

    // ── loadPhongTrong ──────────────────────────────────────────────────

    @Test
    void loadPhongTrong_happyPath() {
        List<String[]> data = List.of(new String[]{"P101", "101"}, new String[]{"P201", "201"});
        try (MockedConstruction<PhongDAO> mc = mockConstruction(PhongDAO.class,
                (m, ctx) -> when(m.loadAllForCalendar()).thenReturn(data))) {
            List<String[]> result = svc.loadPhongTrong(LocalDate.now(), LocalDate.now().plusDays(7));
            assertEquals(2, result.size());
            assertEquals("P101", result.get(0)[0]);
        }
    }

    @Test
    void loadPhongTrong_emptyResult() {
        try (MockedConstruction<PhongDAO> mc = mockConstruction(PhongDAO.class,
                (m, ctx) -> when(m.loadAllForCalendar()).thenReturn(new ArrayList<>()))) {
            List<String[]> result = svc.loadPhongTrong(LocalDate.now(), LocalDate.now().plusDays(7));
            assertTrue(result.isEmpty());
        }
    }

    // ── loadLichDat ─────────────────────────────────────────────────────

    @Test
    void loadLichDat_happyPath() {
        List<String[]> data = List.<String[]>of(
            new String[]{"PDP001", "P101", "Nguyen A", "2026-05-20", "2026-05-22", "DaDat"});
        try (MockedConstruction<PhieuDatPhongDAO> mc = mockConstruction(PhieuDatPhongDAO.class,
                (m, ctx) -> when(m.loadBookingsForCalendar(any(), any())).thenReturn(data))) {
            List<String[]> result = svc.loadLichDat(LocalDate.now(), LocalDate.now().plusDays(7));
            assertEquals(1, result.size());
            assertEquals("PDP001", result.get(0)[0]);
        }
    }

    @Test
    void loadLichDat_emptyResult() {
        try (MockedConstruction<PhieuDatPhongDAO> mc = mockConstruction(PhieuDatPhongDAO.class,
                (m, ctx) -> when(m.loadBookingsForCalendar(any(), any())).thenReturn(new ArrayList<>()))) {
            List<String[]> result = svc.loadLichDat(LocalDate.now(), LocalDate.now().plusDays(7));
            assertTrue(result.isEmpty());
        }
    }

    // ── loadLichSu ──────────────────────────────────────────────────────

    @Test
    void loadLichSu_happyPath() {
        List<Object[]> data = List.<Object[]>of(
            new Object[]{"PDP001", "Nguyen A", "P101", "20/05/2026", "22/05/2026", "DaDat", ""});
        try (MockedConstruction<PhieuDatPhongDAO> mc = mockConstruction(PhieuDatPhongDAO.class,
                (m, ctx) -> when(m.loadHistoryForView()).thenReturn(data))) {
            List<Object[]> result = svc.loadLichSu(100);
            assertEquals(1, result.size());
            assertEquals("PDP001", result.get(0)[0]);
        }
    }

    @Test
    void loadLichSu_emptyResult() {
        try (MockedConstruction<PhieuDatPhongDAO> mc = mockConstruction(PhieuDatPhongDAO.class,
                (m, ctx) -> when(m.loadHistoryForView()).thenReturn(new ArrayList<>()))) {
            List<Object[]> result = svc.loadLichSu(100);
            assertTrue(result.isEmpty());
        }
    }

    // ── lookupKhach ─────────────────────────────────────────────────────

    @Test
    void lookupKhach_happyPath() {
        KhachHang kh = new KhachHang("KH001", "Nguyen Van A", "0912345678", "123456789");
        try (MockedConstruction<KhachHangDAO> mc = mockConstruction(KhachHangDAO.class,
                (m, ctx) -> when(m.findBySdt("0912345678")).thenReturn(kh))) {
            KhachHang result = svc.lookupKhach("0912345678");
            assertNotNull(result);
            assertEquals("KH001", result.getMaKH());
        }
    }

    @Test
    void lookupKhach_emptyResult() {
        try (MockedConstruction<KhachHangDAO> mc = mockConstruction(KhachHangDAO.class,
                (m, ctx) -> when(m.findBySdt(anyString())).thenReturn(null))) {
            KhachHang result = svc.lookupKhach("0999999999");
            assertNull(result);
        }
    }
}
