package com.lotuslaverne.service;

import com.lotuslaverne.dao.PhieuThuDAO;
import com.lotuslaverne.util.SessionContext;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PhieuThuServiceTest {

    private final PhieuThuService svc = new PhieuThuService();

    // ── thuCoc ──────────────────────────────────────────────────────────

    @Test
    void thuCoc_lanDau_success() {
        SessionContext session = mock(SessionContext.class);
        when(session.getMaNhanVien()).thenReturn("NV001");

        try (MockedConstruction<PhieuThuDAO> mc = mockConstruction(PhieuThuDAO.class, (m, ctx) -> {
                 when(m.daDuocThuCoc("PDP001")).thenReturn(false);
                 when(m.generateMaPT()).thenReturn("PT_TEST_01");
                 when(m.taoPhieuThu("PT_TEST_01", "PDP001", "NV001", 500_000, "TienMat", "Cọc")).thenReturn(true);
             });
             MockedStatic<SessionContext> mockedSc = mockStatic(SessionContext.class)) {
            mockedSc.when(SessionContext::getInstance).thenReturn(session);

            String maPT = svc.thuCoc("PDP001", 500_000, "TienMat", "Cọc");
            assertEquals("PT_TEST_01", maPT);
        }
    }

    @Test
    void thuCoc_lanHai_throwIllegalState() {
        try (MockedConstruction<PhieuThuDAO> mc = mockConstruction(PhieuThuDAO.class,
                (m, ctx) -> when(m.daDuocThuCoc("PDP001")).thenReturn(true))) {
            assertThrows(IllegalStateException.class,
                () -> svc.thuCoc("PDP001", 500_000, "TienMat", ""));
        }
    }

    // ── loadPhieuChuaCheckIn ────────────────────────────────────────────

    @Test
    void loadPhieuChuaCheckIn_happyPath() {
        List<String> data = List.of("PDP001", "PDP002");
        try (MockedConstruction<PhieuThuDAO> mc = mockConstruction(PhieuThuDAO.class,
                (m, ctx) -> when(m.loadPhieuChuaCheckIn()).thenReturn(data))) {
            List<String> result = svc.loadPhieuChuaCheckIn();
            assertEquals(2, result.size());
            assertEquals("PDP001", result.get(0));
        }
    }

    @Test
    void loadPhieuChuaCheckIn_emptyResult() {
        try (MockedConstruction<PhieuThuDAO> mc = mockConstruction(PhieuThuDAO.class,
                (m, ctx) -> when(m.loadPhieuChuaCheckIn()).thenReturn(List.of()))) {
            assertTrue(svc.loadPhieuChuaCheckIn().isEmpty());
        }
    }
}
