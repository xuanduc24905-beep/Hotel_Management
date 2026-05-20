# Thuyết Trình - Hệ Thống Quản Lý Khách Sạn Lotus Laverne

> **Mục đích:** Tài liệu ôn tập 30 phút, tập trung demo + trả lời câu hỏi  
> **Người phụ trách:** Người 1 — Đặt Phòng & Thanh Toán  
> **Cấu trúc:** Quản lý phòng → Đặt phòng → Check-out / Thanh toán → Báo cáo doanh thu

---

## 1. QUẢN LÝ PHÒNG

### 1.1 Luồng nghiệp vụ

```
Tạo loại phòng (LoaiPhong)
    → Tạo phòng (Phong) gắn với loại
        → Quản lý trạng thái vòng đời phòng:
           PhongTrong → PhongDat → DaCheckIn → PhongCanDon → DangDon → PhongTrong
           (hoặc BaoTri khi cần sửa chữa)
```

### 1.2 Entity - Mô hình dữ liệu

**Mở file khi được hỏi:** [Phong.java](src/main/java/com/lotuslaverne/entity/Phong.java)

Các trường quan trọng:
- `maPhong`, `tenPhong`, `maLoaiPhong`
- `trangThai` — chuỗi enum: `PhongTrong`, `PhongDat`, `PhongCanDon`, `DangDon`, `BaoTri`
- `tienNghi` — lưu dạng chuỗi ghép: `"WiFi,TV,Điều Hòa"` → tìm kiếm bằng LIKE
- `soNguoiToiDa` — dùng để lọc phòng khi đặt

### 1.3 DAO - Truy vấn chính

**Mở file khi được hỏi:** [PhongDAO.java](src/main/java/com/lotuslaverne/dao/PhongDAO.java)

| Phương thức | Dòng | Chức năng |
|---|---|---|
| `getAll()` | ~15 | Lấy tất cả phòng kèm tiện nghi |
| `timTheoTienNghi(String)` | ~32 | Lọc phòng theo tiện nghi cụ thể |
| `capNhatTrangThai(maPhong, trangThai)` | ~93 | Đổi trạng thái phòng (dùng nhiều nhất) |
| `mapRow(ResultSet)` | ~122 | Map an toàn, có fallback nếu cột mới chưa có |

### 1.4 UI - Giao diện

**Mở file khi được hỏi:** [PhongView.java](src/main/java/com/lotuslaverne/fx/views/PhongView.java)

- **Tab 1 (Card Grid):** Mỗi phòng là một card màu theo trạng thái, hiển thị tiện nghi dạng chip
- **Tab 2 (Danh Sách):** Bảng chi tiết, hỗ trợ sắp xếp đa cột
- **Tab 3 (Phòng Cần Dọn):** Bảng lọc riêng `trangThai='PhongCanDon'`, có nút "🧹 Đang Dọn" và "✅ Đã Dọn Xong" cập nhật trạng thái ngay trên bảng
- **Bộ lọc đa tiêu chí:** Loại phòng + Trạng thái + Tiện nghi (dùng Set để chọn nhiều)
- Click card → điều hướng nhanh sang đặt phòng / checkout qua `MainLayout`

### 1.5 Điểm cần nhấn khi thuyết trình

> *"Chúng em thiết kế trạng thái phòng thành vòng đời khép kín — khi khách check-out, phòng tự động chuyển sang 'Cần Dọn' thay vì 'Trống' ngay, đảm bảo quy trình dọn phòng được kiểm soát trước khi nhận khách mới."*

---

## 2. ĐẶT PHÒNG (PhieuDatPhong)

### 2.1 Luồng nghiệp vụ

```
Nhân viên mở DatPhongView
  Step 1: Nhập tiêu chí (ngày nhận, ngày trả, số khách, loại phòng, tiện nghi)
      ↓
  Step 2: Chọn phòng từ danh sách phòng trống phù hợp (hiển thị giá)
      ↓
  Step 3: Nhập thông tin khách + mã khuyến mãi → Xác nhận
      ↓
  Tạo PhieuDatPhong + ChiTietPhieuDatPhong trong 1 TRANSACTION → trạng thái "DaDat"
      ↓
  Hiển thị Phiếu Thu Cọc (50% tổng tiền) + tự INSERT vào bảng PhieuThu
```

### 2.2 Entity chính

**Mở file khi được hỏi:** [PhieuDatPhong.java](src/main/java/com/lotuslaverne/entity/PhieuDatPhong.java)

- `thoiGianNhanDuKien` / `thoiGianNhanThucTe` — phân biệt giờ dự kiến vs thực tế
- `thoiGianTraDuKien` / `thoiGianTraThucTe` — tương tự
- `trangThai`: `DaDat` → `DaCheckIn` → `DaCheckOut` (hoặc `HuyDat`)
- `hinhThucDat`: TrucTiep / QuaDienThoai / OnlineBooking

**Mở file khi được hỏi:** [BangGia.java](src/main/java/com/lotuslaverne/entity/BangGia.java)

- `loaiThue`: `QuaDem` / `QuaGio`
- `kyGia`: `NgayThuong`, `CuoiTuan`, `LeTet`, `CaoDiem` — hỗ trợ giá động theo thời điểm
- `ngayBatDau`, `ngayKetThuc` — hiệu lực theo khoảng thời gian

### 2.3 Logic tìm phòng trống

**Mở file khi được hỏi:** [DatPhongView.java](src/main/java/com/lotuslaverne/fx/views/DatPhongView.java) — dòng ~813

```sql
-- Loại trừ phòng đã được đặt trùng thời gian
SELECT p.maPhong, p.tenPhong, lp.tenLoaiPhong, bg.donGia, p.tienNghi
FROM Phong p
JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong
JOIN BangGia bg ON lp.maLoaiPhong = bg.maLoaiPhong
  AND bg.loaiThue = 'QuaDem'
  AND GETDATE() BETWEEN bg.ngayBatDau AND bg.ngayKetThuc
WHERE p.maPhong NOT IN (
    SELECT ctpdp.maPhong FROM ChiTietPhieuDatPhong ctpdp
    JOIN PhieuDatPhong pdp ON ctpdp.maPhieuDatPhong = pdp.maPhieuDatPhong
    WHERE pdp.trangThai NOT IN ('HuyDat','DaCheckOut')
      AND pdp.thoiGianNhanDuKien < ? AND pdp.thoiGianTraDuKien > ?
)
AND p.soNguoiToiDa >= ?
AND p.tienNghi LIKE '%WiFi%'   -- (lọc tiện nghi động)
```

> *"Truy vấn dùng subquery loại trừ phòng đã bị block theo khoảng thời gian chồng lấp — không chỉ kiểm tra ngày mà kiểm tra cả giờ."*

### 2.4 Hệ thống khuyến mãi

**Mở file khi được hỏi:** [DatPhongView.java](src/main/java/com/lotuslaverne/fx/views/DatPhongView.java) — dòng ~873

- Nhập mã → `lookupKhuyenMai()` tra DB bảng `KhuyenMai`
- Fallback demo codes: `SUMMER10` (10%), `LOTUS20` (20%), `VIP30` (30%)
- Hiển thị trực tiếp: tiền gốc, % giảm, số tiền giảm, tổng cuối

### 2.5 Lịch đặt phòng (Gantt)

**Trong DatPhongView.java — Tab Calendar**

- Hiển thị 7 ngày theo chiều ngang, phòng theo chiều dọc
- Mỗi booking là một thanh màu: Xanh dương = DaDat, Xanh lá = DaCheckIn
- Query JOIN 3 bảng: `PhieuDatPhong → ChiTietPhieuDatPhong → KhachHang`
- Lọc bỏ trạng thái `DaCheckOut`, `HuyDat`

### 2.6 Điểm cần nhấn khi thuyết trình

> *"Giá phòng được lấy động từ bảng BangGia theo loại phòng + loại thuê + kỳ giá tại thời điểm đặt, không hardcode — nên hệ thống linh hoạt xử lý giá cuối tuần hay lễ tết khác ngày thường."*

---

## 3. CHECK-IN

### 3.1 Luồng nghiệp vụ

```
Mở CheckInView → danh sách phiếu trạng thái "DaDat"
    → Chọn phiếu → Bấm "Check In"
        → Gọi SP_CheckIn (stored procedure)
            → trangThai = 'DaCheckIn', thoiGianNhanThucTe = NOW()
            → Phòng chuyển sang 'PhongDat'
```

### 3.2 DAO - Check-in

**Mở file khi được hỏi:** [PhieuDatPhongDAO.java](src/main/java/com/lotuslaverne/dao/PhieuDatPhongDAO.java) — dòng ~34

```java
// Gọi stored procedure để đảm bảo tính nhất quán
public void checkIn(String maPhieuDatPhong) {
    String sql = "EXEC SP_CheckIn ?";
    // SP tự cập nhật cả PhieuDatPhong lẫn Phong trong 1 transaction
}
```

- Dùng **Stored Procedure** thay vì 2 câu UPDATE riêng → đảm bảo atomic

### 3.3 Query tối ưu (tránh N+1)

**Mở file khi được hỏi:** [PhieuDatPhongDAO.java](src/main/java/com/lotuslaverne/dao/PhieuDatPhongDAO.java) — dòng ~75

```java
// getChuaCheckInJoined() - 1 query thay vì query từng phiếu
// Trả về Object[]: {maPhieu, tenKH, maNhanVien, soNguoi, thoiGianNhanDK, ...}
```

> *"Chúng em dùng JOIN query thay vì load từng phiếu rồi lookup khách hàng riêng — tránh vấn đề N+1 queries khi có nhiều phiếu đang chờ."*

---

## 4. CHECK-OUT & THANH TOÁN

### 4.1 Luồng nghiệp vụ

```
Mở CheckoutView → danh sách khách đang ở (trangThai = 'DaCheckIn')
  Step 1: Chọn phòng/khách cần checkout
      ↓
  Step 2: Xem chi tiết hóa đơn
      - Tiền phòng = donGia × số đêm
      - Tiền dịch vụ (nếu có)
      - Tiền cọc đã trả (load từ PhieuThu → trừ thẳng vào tổng)
      ↓
  Step 3: Chọn phương thức thanh toán → Xác nhận
      ↓
  doCheckout() — transaction toàn bộ
```

### 4.2 Tính tiền phòng

**Mở file khi được hỏi:** [CheckoutView.java](src/main/java/com/lotuslaverne/fx/views/CheckoutView.java) — dòng ~507, ~343

```java
// Số đêm tính bằng DATEDIFF từ giờ thực tế nhận phòng
soDem = DATEDIFF(day, thoiGianNhanThucTe, GETDATE())

// Tiền phòng
tienPhong = queryTienPhong(maPhong) * soDem

// Tổng
tongTien = tienPhong + tienDichVu - datCoc
```

- `tienThanhToan = max(0, tongTienFinal - soTienCocDaDat)` — trừ tiền đặt cọc trước

### 4.3 Giao diện thanh toán tiền mặt

**Mở file khi được hỏi:** [CheckoutView.java](src/main/java/com/lotuslaverne/fx/views/CheckoutView.java) — dòng ~185

- **Nút mệnh giá:** 500K, 200K, 100K, 50K, 20K, 10K, 5K, 2K, 1K — click để cộng dồn
- **Gợi ý tiền:** Tự động gợi ý số tiền chẵn gần nhất (làm tròn lên 10K, 50K)
- **Tiền thừa:** Hiển thị ngay sau khi nhập đủ

```java
tienThua = soTienNhap - tongTien  // hiện màu xanh khi >= 0
```

### 4.4 Phương thức thanh toán

4 lựa chọn: **Tiền Mặt** | **Mã QR** | **MOMO** | **Chuyển Khoản**

### 4.5 Transaction Checkout (quan trọng nhất)

**Mở file khi được hỏi:** [CheckoutView.java](src/main/java/com/lotuslaverne/fx/views/CheckoutView.java) — dòng ~591–698

```java
// doCheckout() — toàn bộ trong 1 transaction, ROLLBACK nếu lỗi
conn.setAutoCommit(false);
try {
    // 1. Lấy tiền cọc đã thu
    tienCoc = loadSoTienCoc(maPDP);  // từ PhieuThu

    // 2. Cập nhật PhieuDatPhong → DaCheckOut + giờ trả thực tế
    UPDATE PhieuDatPhong SET trangThai='DaCheckOut', thoiGianTraThucTe=GETDATE()

    // 3. Cập nhật phòng → PhongCanDon (không phải PhongTrong!)
    UPDATE Phong SET trangThai='PhongCanDon'

    // 4. Tạo HoaDon
    INSERT INTO HoaDon(tienThanhToan, phuongThucThanhToan, ...)

    // 5. Tạo ChiTietHoaDon (nhiều dòng)
    INSERT ChiTietHoaDon loaiTien='TienPhong', soLuong=soDem, donGia=...
    INSERT ChiTietHoaDon loaiTien='TienDichVu', ... (mỗi dịch vụ 1 dòng)
    INSERT ChiTietHoaDon loaiTien='TienCoc', thanhTien=-tienCoc  // âm = đã trừ

    // 6. Gắn PhieuThu với HoaDon vừa tạo
    UPDATE PhieuThu SET maHoaDon = maHoaDonMoi

    conn.commit();
} catch (Exception e) {
    conn.rollback();  // hoàn tác toàn bộ nếu bất kỳ bước nào lỗi
}
```

> *"Toàn bộ quá trình checkout nằm trong 1 transaction — nếu bất kỳ bước nào thất bại (ví dụ mất kết nối DB giữa chừng), hệ thống rollback hoàn toàn, không để dữ liệu ở trạng thái dở dang."*

### 4.6 Entity HoaDon

**Mở file khi được hỏi:** [HoaDon.java](src/main/java/com/lotuslaverne/entity/HoaDon.java)

- `tienKhuyenMai` — lưu số tiền đã giảm (audit trail)
- `tienThanhToan` — số tiền thực thu
- `phuongThucThanhToan`
- `ngayLap` vs `ngayThanhToan` — phân biệt ngày tạo hóa đơn và ngày thực thanh toán

---

## 5. BÁO CÁO DOANH THU & THỐNG KÊ

### 5.1 Dashboard tổng quan (màn hình chính)

**Mở file khi được hỏi:** [ThongKeDAO.java](src/main/java/com/lotuslaverne/dao/ThongKeDAO.java)

| Phương thức | Dòng | Số liệu |
|---|---|---|
| `layDoanhThuHomNay()` | ~10 | Tổng doanh thu hôm nay |
| `demSoPhongTheoTrangThai(String)` | ~28 | Số phòng theo từng trạng thái |
| `demKhachDangLuuTru()` | ~58 | Khách đang ở (DISTINCT maKH) |
| `layOccupancyRate()` | ~75 | Tỉ lệ lấp đầy (%) + tổng phòng |

```sql
-- Occupancy rate
SELECT 
    SUM(CASE WHEN trangThai != 'PhongTrong' THEN 1 ELSE 0 END) * 100.0 / COUNT(*) 
    AS occupancy
FROM Phong
```

### 5.2 Màn hình Báo Cáo chi tiết

**Mở file khi được hỏi:** [BaoCaoView.java](src/main/java/com/lotuslaverne/fx/views/BaoCaoView.java)

**Bộ lọc thời gian:**
- Preset nhanh: `7 ngày`, `Tháng này`, `Tháng trước`, `Năm nay`
- Tùy chọn: Date picker tùy ý từ–đến
- `resolvePreset()` — dòng ~131: chuyển tên preset sang khoảng ngày cụ thể

**4 thẻ thống kê chính:**

| Thẻ | Nguồn dữ liệu | Tính như nào |
|---|---|---|
| Doanh Thu Kỳ Này | HoaDon.tienThanhToan | SUM theo ngày trong khoảng |
| So Sánh Kỳ Trước | Cùng câu query, dịch ngược khoảng | % thay đổi = (mới-cũ)/cũ × 100 |
| Tỉ Lệ Hủy Phòng | PhieuDatPhong trangThai='HuyDat' | COUNT(hủy) / COUNT(tổng) × 100 |
| Lượt Khách Check-in | DISTINCT maKH trong khoảng | thoiGianNhanThucTe lọc theo range |

**Biểu đồ & phân tích:**
- `buildChartsRow(from, to)` — xu hướng doanh thu theo ngày / tuần
- `buildPaymentBreakdown(from, to)` — phân bổ phương thức thanh toán (tiền mặt, QR, MOMO...)

### 5.3 Điểm cần nhấn khi thuyết trình

> *"Màn hình báo cáo tự so sánh kỳ hiện tại với kỳ trước có cùng độ dài — nếu xem 7 ngày thì so 7 ngày trước đó, nếu xem cả tháng thì so tháng trước — giúp quản lý thấy ngay xu hướng tăng/giảm."*

---

## 6. KIẾN TRÚC TỔNG QUAN

```
fx/views/          ← JavaFX UI (View + Controller gộp)
    PhongView.java
    DatPhongView.java
    CheckInView.java
    CheckoutView.java
    BaoCaoView.java

entity/            ← POJO thuần (không logic)
    Phong, LoaiPhong, BangGia
    PhieuDatPhong, ChiTietPhieuDatPhong, KhachHang
    HoaDon, ChiTietHoaDon

dao/               ← JDBC trực tiếp, PreparedStatement
    PhongDAO, LoaiPhongDAO
    PhieuDatPhongDAO, KhachHangDAO, BangGiaDAO
    HoaDonDAO, ThongKeDAO

util/
    DBConnection.java   ← Singleton kết nối SQL Server
```

**Bảo mật:** Toàn bộ query dùng `PreparedStatement` — tránh SQL Injection  
**Tính nhất quán:** Checkout dùng transaction + rollback  
**Hiệu năng:** Dùng JOIN query thay vì N+1, index trên maPhieuDatPhong, maPhong

---

## 7. CÂU HỎI THƯỜNG GẶP

**Q: Tại sao sau checkout phòng không về "Trống" ngay?**  
A: Vì cần quy trình dọn phòng — phòng về `PhongCanDon`, sau khi housekeeping xác nhận dọn xong mới chuyển `DangDon` → `PhongTrong`.

**Q: Giá phòng được tính như thế nào?**  
A: Lấy từ bảng `BangGia` theo loại phòng + loại thuê (`QuaDem`/`QuaGio`) + kỳ giá hiệu lực tại thời điểm đặt. Giá lưu vào `ChiTietPhieuDatPhong.donGia` ngay lúc đặt — không thay đổi dù sau này cập nhật bảng giá.

**Q: Khuyến mãi xử lý như thế nào?**  
A: Nhập mã → tra bảng `KhuyenMai` lấy % giảm → tính lại tổng tiền ngay trên UI → khi tạo hóa đơn lưu vào `HoaDon.tienKhuyenMai`.

**Q: Nếu mất điện giữa chừng khi checkout thì sao?**  
A: `doCheckout()` bọc trong transaction — rollback toàn bộ nếu có lỗi. Dữ liệu không bao giờ ở trạng thái một nửa. Ngoài ra hàm trả về `boolean` — nếu false thì UI hiện thông báo lỗi, không hiện "thành công" ảo.

**Q: Tiền cọc được xử lý như thế nào trong toàn bộ luồng?**  
A: Lúc đặt phòng → dialog Phiếu Thu Cọc hiện ra → hệ thống tự INSERT vào bảng `PhieuThu` (50% tổng). Lúc checkout → query `SUM(soTienCoc)` từ `PhieuThu` → hiển thị dòng "Tiền cọc đã trả: -xxx đ" trong hóa đơn → nhân viên chỉ cần thu phần còn lại.

**Q: Tại sao đặt phòng và chi tiết phòng phải nằm trong cùng transaction?**  
A: Nếu INSERT `PhieuDatPhong` thành công nhưng INSERT `ChiTietPhieuDatPhong` thất bại → tồn tại phiếu không biết đặt phòng nào → hệ thống không thể checkout. Transaction đảm bảo cả 2 thành công hoặc cả 2 bị rollback.

**Q: Báo cáo so sánh kỳ trước tính như thế nào?**  
A: Tính khoảng thời gian đang xem (N ngày), rồi query ngược lại N ngày trước đó với cùng điều kiện, tính % thay đổi.

**Q: Tìm phòng trống xử lý trùng ngày như thế nào?**  
A: Dùng subquery loại trừ phòng đã có booking chồng lấp: `thoiGianNhanDuKien < ngayTra AND thoiGianTraDuKien > ngayNhan` — điều kiện này chính xác với mọi kiểu chồng lấp.

---

## 8. FILES CẦN MỞ KHI DEMO

| Tình huống cô hỏi | Mở file này |
|---|---|
| Cấu trúc dữ liệu phòng | [Phong.java](src/main/java/com/lotuslaverne/entity/Phong.java) |
| Giao diện quản lý phòng | [PhongView.java](src/main/java/com/lotuslaverne/fx/views/PhongView.java) |
| Logic tìm phòng trống | [DatPhongView.java](src/main/java/com/lotuslaverne/fx/views/DatPhongView.java) dòng 813 |
| Bảng giá động | [BangGia.java](src/main/java/com/lotuslaverne/entity/BangGia.java) + [BangGiaDAO.java](src/main/java/com/lotuslaverne/dao/BangGiaDAO.java) |
| Transaction checkout | [CheckoutView.java](src/main/java/com/lotuslaverne/fx/views/CheckoutView.java) dòng 591 |
| Hóa đơn | [HoaDon.java](src/main/java/com/lotuslaverne/entity/HoaDon.java) |
| Stored procedure check-in | [PhieuDatPhongDAO.java](src/main/java/com/lotuslaverne/dao/PhieuDatPhongDAO.java) dòng 34 |
| Thống kê dashboard | [ThongKeDAO.java](src/main/java/com/lotuslaverne/dao/ThongKeDAO.java) |
| Báo cáo chi tiết | [BaoCaoView.java](src/main/java/com/lotuslaverne/fx/views/BaoCaoView.java) |
