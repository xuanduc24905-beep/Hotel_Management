# Sổ Tay Phát Triển — Lotus Laverne Hotel Management

> Ghi chú hàng ngày trong quá trình làm đồ án.
> Mỗi ngày thêm 1 mục mới lên **trên cùng**.

---

## 20/05/2026

**Implement Rule 12–15 (nghiệp vụ mới)**

- Thêm **Walk-in check-in** (`WalkInService`): khách không đặt trước, nhân viên nhập thông tin tại quầy, hệ thống tự tạo phiếu + thu cọc 50% + check-in ngay trong 1 transaction. Nút Walk-in màu cam trên màn hình Check-in.
- Thêm **Sửa / Xoá dịch vụ phòng** (`DichVuPhongView`): cho phép chỉnh số lượng hoặc xoá dịch vụ khi phiếu chưa checkout. Sau khi checkout thì khoá hoàn toàn.
- Thêm **log thay đổi dịch vụ** (`ChiTietDichVuLog`): mỗi lần sửa/xoá đều ghi mã NV + thời gian + giá trị cũ/mới. Cần chạy `db/V2_add_dichvu_log.sql` để tạo bảng.
- Cập nhật `BUSINESS_RULES.md` + `README.md` với rule 12–15.
- 63/63 tests vẫn xanh sau khi thêm.

**File mới:** `WalkInService.java`, `db/V2_add_dichvu_log.sql`  
**File sửa:** `CheckInView`, `DichVuPhongView`, `DichVuDAO`, `PhieuDatPhongDAO`, `PhongDAO`

---

## 19/05/2026

**Refactor toàn bộ Service layer + fix BUSINESS_RULES violations**

- Tách biệt hoàn toàn business logic khỏi View và DAO — toàn bộ nằm trong Service layer.
- Fix `CheckInView`: bỏ dữ liệu demo cứng, gọi `CheckInService` thay `PhieuDatPhongDAO.checkIn()`.
- Fix `PhieuThuView`: tự động tính cọc 50% từ BangGia, guard chống thu 2 lần, hiển thị tên khách.
- Fix `DoiPhongView`: ghi nhận bù trừ chênh lệch phòng vào `ghiChu` phiếu.
- Fix `ThanhToanView`: bỏ fallback giá hardcode 200k, bỏ `"NV001"` hardcode.
- Xoá debug log trong `KhachView`.
- Viết 63 unit test với Mockito — bao phủ: overlap ngày, tính tiền, hoàn cọc, validate trạng thái, KM.

---

## 07/05/2026

**Fix 9 bug lớn + gộp SQL + seed data đầy đủ**

- Xoá màn hình `ThanhToanView` trùng với `CheckoutView`.
- Thêm `SessionContext` singleton — không còn hardcode `"NV001"` ở bất kỳ đâu.
- Fix N+1 query ở `CheckInView` (JOIN sẵn thay vì query từng khách).
- Fix entity `PhieuDatPhong` thiếu field `trangThai`, `hinhThucDat`.
- Fix SQL sai tên cột `ngayBatDau` → `ngayApDung` ở khuyến mãi.
- Fix tìm phòng trống dùng `'Trong'` → `'PhongTrong'` + filter tiện nghi hoạt động.
- Fix `CheckoutView`: thêm transaction, INSERT `ChiTietHoaDon`, trừ cọc đúng.
- Fix sinh mã KH dùng UUID thay `millis % 100000`.
- Gộp 3 file SQL thành 1, seed 14 khách + 11 phiếu + 3 hóa đơn mẫu.

---

## Cách thêm mục mới

```
## DD/MM/YYYY

**Tiêu đề ngắn gọn**

- Việc 1
- Việc 2
- Gặp vấn đề gì, fix như thế nào

**File thay đổi chính:** abc.java, xyz.java
```
