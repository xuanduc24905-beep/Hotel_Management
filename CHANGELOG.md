# CHANGELOG — Lotus Laverne Hotel Management

> Cập nhật: 07/05/2026

---

## [v3.0] — 07/05/2026

### Tổng quan

Đợt cải tiến lớn trên toàn bộ codebase: sửa 9 bug nghiệp vụ, xóa màn hình trùng lặp, refactor luồng dữ liệu, gộp 3 file SQL thành 1, và bổ sung bộ seed data đầy đủ phản ánh tình trạng khách sạn đang hoạt động.

---

## 1. Xóa màn hình Thanh Toán trùng lặp

**Vấn đề:** Hệ thống có 2 màn hình checkout chạy song song — `ThanhToanView.java` (màn hình cũ) và `CheckoutView.java` (màn hình chính xác). Nav bar có mục "Thanh Toán" riêng dẫn đến `ThanhToanView`, gây nhầm lẫn và phân mảnh logic nghiệp vụ.

**Các file thay đổi:**

- **`src/main/java/com/lotuslaverne/fx/MainLayout.java`**
  - Xóa `NavEntry("💳", "Thanh Toán", "thanhtoan", false)` khỏi mảng `ALL_NAV`
  - Cập nhật `SECTION_BREAKS` (chỉ số dịch -1 cho BaoCao và NhanSu)
  - Xóa `case "thanhtoan"` khỏi `navigateToView()`

- **`src/main/java/com/lotuslaverne/fx/views/PhongView.java`**
  - Đổi `navigateToView("thanhtoan", maPDP)` → `navigateToView("checkout", null)` ở nút hành động phòng "Đang Thuê"
  - Xóa biến `maPDP` không còn dùng và method `layMaPDPDangActive()`

- **`src/main/java/com/lotuslaverne/fx/views/ThanhToanView.java`** — **XÓA HOÀN TOÀN**

---

## 2. SessionContext — Thay thế maNhanVien hardcoded

**Vấn đề:** `DatPhongView` dùng chuỗi `"NV001"` hardcoded khi tạo phiếu đặt phòng. Nhân viên nào đăng nhập cũng đều tạo phiếu đứng tên NV001.

**File mới:** `src/main/java/com/lotuslaverne/util/SessionContext.java`

```java
public class SessionContext {
    private static final SessionContext INSTANCE = new SessionContext();
    private String maNhanVien = "NV001"; // fallback an toàn

    public static SessionContext getInstance() { return INSTANCE; }
    public void init(String maNhanVien, String tenDangNhap, String vaiTro) { ... }
    public String getMaNhanVien() { return maNhanVien; }
}
```

**File sửa:**

- **`src/main/java/com/lotuslaverne/fx/LoginView.java`**  
  Sau khi xác thực thành công, gọi `SessionContext.getInstance().init(tk.getMaNhanVien(), username, resolvedRole)` để lưu thông tin nhân viên đang đăng nhập.

- **`src/main/java/com/lotuslaverne/fx/views/DatPhongView.java`**  
  Thay `"NV001"` → `SessionContext.getInstance().getMaNhanVien()` tại điểm tạo phiếu đặt phòng.

---

## 3. Fix N+1 query trong CheckInView

**Vấn đề:** `CheckInView.loadData()` gọi `getChuaCheckIn()` để lấy danh sách phiếu, sau đó với mỗi phiếu lại gọi thêm `layTenKhach(maKhachHang)` — một query riêng cho từng khách. Với 100 phiếu chờ check-in → 101 round-trip DB.

**File sửa:** `src/main/java/com/lotuslaverne/dao/PhieuDatPhongDAO.java`

Thêm method `getChuaCheckInJoined()` trả về `List<Object[]>` với JOIN sẵn `KhachHang`:

```java
"SELECT pdp.maPhieuDatPhong, kh.hoTenKH, pdp.maNhanVien, pdp.soNguoi,"
+ " pdp.thoiGianNhanDuKien, pdp.thoiGianTraDuKien, pdp.ghiChu"
+ " FROM PhieuDatPhong pdp"
+ " JOIN KhachHang kh ON kh.maKH = pdp.maKhachHang"
+ " WHERE pdp.trangThai = N'DaDat'"
+ " ORDER BY pdp.thoiGianNhanDuKien ASC"
```

**File sửa:** `src/main/java/com/lotuslaverne/fx/views/CheckInView.java`

- Xóa method `layTenKhach()`
- Viết lại `loadData()` dùng `getChuaCheckInJoined()` (1 query duy nhất)
- Xóa các import thừa: `ConnectDB`, `Connection`, `PreparedStatement`, `ResultSet`, `PhieuDatPhong` entity

---

## 4. Fix entity PhieuDatPhong thiếu field

**Vấn đề:** `PhieuDatPhong.java` không có field `trangThai` và `hinhThucDat`, khiến `mapRow()` trong DAO throw `NoSuchMethodException` khi gọi setter.

**File sửa:** `src/main/java/com/lotuslaverne/entity/PhieuDatPhong.java`

Thêm:
```java
private String trangThai;
private String hinhThucDat;
// + getter/setter tương ứng
```

**File sửa:** `src/main/java/com/lotuslaverne/dao/PhieuDatPhongDAO.java` — `mapRow()`

```java
p.setTrangThai(rs.getString("trangThai"));
p.setHinhThucDat(rs.getString("hinhThucDat"));
```

---

## 5. Fix DatPhongView — KhuyenMai dùng sai tên cột

**Vấn đề:** `lookupKhuyenMai()` dùng tên cột `ngayBatDau` (không tồn tại) và lọc `trangThai='HoatDong'` (không có trong schema). Query luôn trả `null` → không áp dụng được khuyến mãi nào.

**File sửa:** `src/main/java/com/lotuslaverne/fx/views/DatPhongView.java`

```java
// Trước (sai)
"WHERE maKhuyenMai=? AND CAST(GETDATE() AS DATE) BETWEEN ngayBatDau AND ngayKetThuc"
+ " AND trangThai='HoatDong'"

// Sau (đúng theo schema)
"WHERE maKhuyenMai=? AND CAST(GETDATE() AS DATE) BETWEEN ngayApDung AND ngayKetThuc"
```

---

## 6. Fix DatPhongView — tìm phòng trống sai trạng thái

**Vấn đề:** `searchAvailableRooms()` lọc `trangThai='Trong'` (không khớp với enum trong DB). Không có phòng nào trả về dù DB có phòng trống `PhongTrong`.

**File sửa:** `src/main/java/com/lotuslaverne/fx/views/DatPhongView.java`

```java
// Trước
"WHERE trangThai='Trong'"

// Sau
"WHERE trangThai = N'PhongTrong'"
```

Ngoài ra, 6 checkbox tiện nghi (WiFi, TV, Điều Hòa, Bồn Tắm, Ban Công, Mini Bar) đã được thu thập ở UI nhưng không đưa vào câu SQL. Đã thêm điều kiện `LIKE` động cho từng tiện nghi được chọn:

```java
if (cbWifi.isSelected())    sql += " AND tienNghi LIKE N'%WiFi%'";
if (cbTV.isSelected())      sql += " AND tienNghi LIKE N'%TV%'";
if (cbDieuHoa.isSelected()) sql += " AND tienNghi LIKE N'%Điều Hòa%'";
if (cbBonTam.isSelected())  sql += " AND tienNghi LIKE N'%Bồn Tắm%'";
if (cbBanCong.isSelected()) sql += " AND tienNghi LIKE N'%Ban Công%'";
if (cbMiniBar.isSelected()) sql += " AND tienNghi LIKE N'%Mini Bar%'";
```

Sau khi đặt phòng thành công, cập nhật trạng thái phòng ngay lập tức:

```java
"UPDATE Phong SET trangThai = N'PhongDat' WHERE maPhong = ?"
```

---

## 7. Fix CheckoutView — transaction + ChiTietHoaDon + trừ cọc

**Vấn đề (3 bug liên quan):**

1. **Không có transaction**: nếu INSERT `HoaDon` thành công nhưng UPDATE `Phong` fail → dữ liệu inconsistent (hóa đơn tồn tại nhưng phòng vẫn `PhongDat`).
2. **`ChiTietHoaDon` luôn rỗng**: code cũ tạo `HoaDon` nhưng không INSERT dòng chi tiết nào.
3. **Tiền cọc không được trừ**: `tienThanhToan` tính bằng tổng tiền phòng + dịch vụ mà không trừ đặt cọc đã thu từ `PhieuThu`.

**File sửa:** `src/main/java/com/lotuslaverne/fx/views/CheckoutView.java`

Thêm các field instance để truyền dữ liệu từ double-click handler sang `doCheckout()`:
```java
private double tienPhongFinal, tienDVFinal;
private int    soNgayFinal;
```

Viết lại `doCheckout()` với transaction đầy đủ:

```
1. Load soTienCoc từ PhieuThu (SUM theo maPhieuDatPhong)
2. setAutoCommit(false)
3. UPDATE PhieuDatPhong → DaCheckOut + thoiGianTraThucTe = GETDATE()
4. UPDATE Phong → PhongCanDon
5. INSERT HoaDon (tienThanhToan = tienPhong + tienDV - soTienCoc)
6. INSERT ChiTietHoaDon — dòng TienPhong
7. INSERT ChiTietHoaDon — từng dòng TienDichVu (JOIN ChiTietDichVu × DichVu)
8. INSERT ChiTietHoaDon — dòng TienCoc âm nếu soTienCoc > 0
9. UPDATE PhieuThu SET maHoaDon để liên kết
10. commit() / rollback() on exception
```

---

## 8. Fix KhachHangDAO — sinh mã KH có thể trùng

**Vấn đề:** Mã KH được sinh bằng `System.currentTimeMillis() % 100000` — tối đa 100.000 giá trị, xác suất va chạm cao khi thao tác nhanh hoặc dữ liệu lớn.

**File sửa:** `src/main/java/com/lotuslaverne/dao/KhachHangDAO.java`

```java
// Trước
"KH" + (System.currentTimeMillis() % 100000)

// Sau
"KH" + UUID.randomUUID().toString().replace("-","").substring(0,8).toUpperCase()
// → VD: "KH3FA2C1D8" — 16^8 ≈ 4.3 tỷ khả năng
```

---

## 9. Gộp SQL thành 1 file + seed data đầy đủ

### 9.1 Gộp file

| Trước | Sau |
|-------|-----|
| `db/LotusLaverne_utf8.sql` (schema v2.0) | `db/LotusLaverne_utf8.sql` (schema v3.0 — all-in-one) |
| `db/patch_add_kyGia.sql` | **XÓA** (đã merge vào file chính) |
| `db/update_thietbi.sql` | **XÓA** (đã merge vào file chính) |

Cả hai patch đã được tích hợp sẵn vào schema v3.0:
- Cột `kyGia` trong `BangGia` với `CHECK (kyGia IN ('NgayThuong','CuoiTuan','LeTet','CaoDiem'))`
- Bảng `ThietBi` với 6 bản ghi thiết bị mẫu

### 9.2 Seed data mới

**Khách hàng:** 3 → 14 (+11)

| Nhóm | ID | Ghi chú |
|------|----|---------|
| Gốc | KH001–KH003 | Giữ nguyên |
| Đang ở | KH004–KH009 | 6 khách hiện đang check-in |
| Chờ check-in | KH010–KH011 | 2 khách sẽ đến ngày 08–09/05 |
| Lịch sử | KH012–KH014 | 3 khách đã checkout; KH014 là khách nước ngoài (Pháp) |

**Trạng thái phòng hiện tại (7/05/2026):**

| Phòng | Loại | Trạng thái | Khách |
|-------|------|------------|-------|
| P101 | Standard | `PhongDat` | Nguyễn Thị Lan (PDP001) |
| P102 | Standard | `PhongDat` | Lý Văn Quân (PDP006) |
| P103 | Standard | `PhongTrong` | — |
| P201 | Deluxe | `PhongDat` | Đặng Văn Minh (PDP002) |
| P202 | Deluxe | `PhongDat` | Bùi Thị Ngọc (PDP003) |
| P301 | Superior | `PhongDat` | Trịnh Quang Hải (PDP004) |
| P302 | Superior | `PhongTrong` | — |
| P401 | Suite | `PhongDat` | Ngô Thị Phương (PDP005) |

**Phiếu đặt phòng:** 11 phiếu

| Trạng thái | Số lượng | Phiếu |
|------------|----------|-------|
| `DaCheckIn` | 6 | PDP001–PDP006 |
| `DaDat` | 2 | PDP007–PDP008 |
| `DaCheckOut` | 3 | PDP009–PDP011 |

**Dịch vụ phát sinh:** 21 dòng `ChiTietDichVu` trải đều các loại (ăn, uống, tiện ích) trên cả khách đang ở lẫn lịch sử.

**Hóa đơn (3 hóa đơn cho khách đã checkout):**

| Hóa đơn | Phiếu | Tiền phòng | Tiền DV | Đặt cọc | **Thanh toán** |
|---------|-------|-----------|--------|--------|--------------|
| HD001 | PDP009 | 2.000.000 | — | 500.000 | **1.500.000** |
| HD002 | PDP010 | 3.200.000 | 135.000 | 800.000 | **2.535.000** |
| HD003 | PDP011 | 12.000.000 | 565.000 | 2.000.000 | **10.565.000** |

**Phiếu thu đặt cọc:** 9 phiếu (6 đang treo, 3 đã liên kết hóa đơn)

---

## Tổng kết thay đổi

| # | Loại | File | Mức độ |
|---|------|------|--------|
| 1 | Xóa màn hình trùng lặp | `ThanhToanView.java`, `MainLayout.java`, `PhongView.java` | High |
| 2 | SessionContext singleton | `SessionContext.java` (mới), `LoginView.java`, `DatPhongView.java` | High |
| 3 | Fix N+1 query | `PhieuDatPhongDAO.java`, `CheckInView.java` | Medium |
| 4 | Fix entity thiếu field | `PhieuDatPhong.java`, `PhieuDatPhongDAO.java` | High |
| 5 | Fix SQL cột sai (KhuyenMai) | `DatPhongView.java` | High |
| 6 | Fix trangThai tìm phòng + filter tiện nghi | `DatPhongView.java` | High |
| 7 | Fix transaction + CTHD + trừ cọc | `CheckoutView.java` | Critical |
| 8 | Fix UUID mã KH | `KhachHangDAO.java` | Medium |
| 9 | Gộp SQL + seed data đầy đủ | `db/LotusLaverne_utf8.sql` | Medium |
