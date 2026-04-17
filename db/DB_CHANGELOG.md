# DB Changelog — LotusLaverne_utf8.sql

## Ngày sửa: 17/04/2026

---

## Fix 1 — `PhieuThu.maHoaDon`: NOT NULL → NULL

**Vị trí:** Bảng `PhieuThu`, dòng định nghĩa cột `maHoaDon`

**Vấn đề:**  
Cột `maHoaDon` khai báo `NOT NULL` nhưng `PhieuThu` là phiếu thu tiền cọc — được tạo tại thời điểm **đặt phòng (UC017)**. Trong khi đó `HoaDon` chỉ được tạo tại thời điểm **check-out (UC024)**. Ràng buộc NOT NULL khiến không thể INSERT `PhieuThu` trước khi có hóa đơn, phá vỡ toàn bộ luồng nghiệp vụ thu cọc.

**Sửa:**
```sql
-- Trước
[maHoaDon] [nvarchar](10) NOT NULL

-- Sau
[maHoaDon] [nvarchar](10) NULL
```

---

## Fix 2 — `SP_CheckOut`: tính tiền phòng khi chưa check-in

**Vị trí:** Stored Procedure `SP_CheckOut`, phần tính `@tienPhong`

**Vấn đề:**  
Công thức tính tiền phòng dùng `thoiGianNhanThucTe` làm mốc bắt đầu. Nếu khách chưa check-in (`thoiGianNhanThucTe = NULL`), `DATEDIFF` trả về `NULL` → `@tienPhong = NULL` → hóa đơn tổng tiền bằng `NULL`, không insert được vào DB do ràng buộc `CHECK (tienThanhToan >= 0)`.

**Sửa:**
```sql
-- Trước
DATEDIFF(DAY, p.thoiGianNhanThucTe, GETDATE()) * ct.donGia

-- Sau
DATEDIFF(DAY, ISNULL(p.thoiGianNhanThucTe, p.thoiGianNhanDuKien), GETDATE()) * ct.donGia
```

---

## Fix 3 — `SP_DoiPhong`: thiếu kiểm tra lịch trùng phòng mới

**Vị trí:** Stored Procedure `SP_DoiPhong`

**Vấn đề:**  
Procedure chỉ kiểm tra `trangThai = 'PhongTrong'` cho phòng mới. Một phòng đang `PhongTrong` vẫn có thể có booking tương lai trong `ChiTietPhieuDatPhong`. Đổi phòng vào trùng lịch đặt trước sẽ gây xung đột dữ liệu, vi phạm yêu cầu phi chức năng "Không cho phép đặt phòng trùng thời gian".

**Sửa:** Thêm kiểm tra conflict thời gian trước khi cập nhật:
```sql
-- Thêm sau khi kiểm tra PhongTrong
DECLARE @thoiGianNhan DATETIME, @thoiGianTra DATETIME;
SELECT @thoiGianNhan = thoiGianNhanDuKien, @thoiGianTra = thoiGianTraDuKien
FROM PhieuDatPhong WHERE maPhieuDatPhong = @maPhieuDatPhong;

IF EXISTS (
    SELECT 1 FROM ChiTietPhieuDatPhong ct
    JOIN PhieuDatPhong p ON ct.maPhieuDatPhong = p.maPhieuDatPhong
    WHERE ct.maPhong = @maPhongMoi
      AND ct.maPhieuDatPhong <> @maPhieuDatPhong
      AND p.thoiGianNhanDuKien < @thoiGianTra
      AND p.thoiGianTraDuKien  > @thoiGianNhan
)
BEGIN
    RAISERROR(N'Phòng mới đã có lịch đặt trùng thời gian.', 16, 1);
    ROLLBACK; RETURN;
END
```

---

## Fix 4 — `BangGia`: thiếu CHECK constraint ngày hợp lệ

**Vị trí:** Constraints của bảng `BangGia`

**Vấn đề:**  
Bảng không có ràng buộc kiểm tra `ngayKetThuc > ngayBatDau`. Có thể INSERT bảng giá với ngày kết thúc trước ngày bắt đầu, dẫn đến view `VW_DanhSachPhong` không trả về giá nào cho phòng đó.

**Sửa:**
```sql
ALTER TABLE [dbo].[BangGia]
    ADD CONSTRAINT [CHK_BangGia_Ngay] CHECK ([ngayKetThuc] > [ngayBatDau]);
```

---

## Thêm mới — Indexes tối ưu tìm kiếm

**Vị trí:** Cuối file, Phần 6 mới

**Lý do:**  
Các cột thường xuyên được dùng trong mệnh đề `WHERE` khi tìm kiếm (theo SĐT khách, theo thời gian đặt phòng, theo phòng) không có index → full table scan mỗi lần truy vấn.

| Index | Bảng | Cột | Dùng cho |
|-------|------|-----|----------|
| `IDX_KhachHang_SoDienThoai` | KhachHang | soDienThoai | Tìm khách theo SĐT (UC015, UC026) |
| `IDX_PhieuDatPhong_MaKhachHang` | PhieuDatPhong | maKhachHang | Xem lịch sử đặt phòng của khách |
| `IDX_PhieuDatPhong_ThoiGian` | PhieuDatPhong | thoiGianNhanDuKien, thoiGianTraDuKien | Kiểm tra trùng lịch (SP_TaoPhieuDatPhong) |
| `IDX_ChiTietPDP_MaPhong` | ChiTietPhieuDatPhong | maPhong | Tìm booking theo phòng (SP_DoiPhong, SP_CheckIn) |
| `IDX_HoaDon_NgayThanhToan` | HoaDon | ngayThanhToan | Thống kê doanh thu theo ngày/tháng (UC027) |

---

## Tóm tắt

| # | Loại | Mức độ | Tác động |
|---|------|--------|----------|
| Fix 1 | Bug — logic nghiệp vụ | Critical | UC017 hoàn toàn không chạy được nếu không sửa |
| Fix 2 | Bug — tính toán sai | High | SP_CheckOut trả NULL nếu khách chưa check-in |
| Fix 3 | Bug — thiếu validation | High | SP_DoiPhong có thể gây xung đột lịch đặt |
| Fix 4 | Bug — thiếu constraint | Medium | Dữ liệu bảng giá có thể không hợp lệ |
| Thêm 5 indexes | Hiệu năng | Medium | Tìm kiếm nhanh hơn trên dữ liệu lớn |
