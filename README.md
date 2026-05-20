# Lotus Laverne Hotel Management

Phần mềm quản lý khách sạn xây dựng bằng **JavaFX + SQL Server**, phục vụ nghiệp vụ lễ tân, buồng phòng và quản trị nội bộ.

---

## Công nghệ sử dụng

| Thành phần | Phiên bản |
|---|---|
| Java | 17 |
| JavaFX | 17.0.13 |
| SQL Server | 2019+ (localhost:1433) |
| Maven | 3.x |
| iTextPDF | 5.5.13 |
| jBCrypt | 0.4 |
| JUnit 5 + Mockito | Test suite |

---

## Cài đặt & chạy

### Yêu cầu

- JDK 17+
- Maven 3.6+
- SQL Server 2019+ đang chạy ở `localhost:1433`

### 1. Clone repo

```bash
git clone https://github.com/xuanduc24905-beep/Hotel_Management.git
cd Hotel_Management
```

### 2. Tạo database

Tạo database tên `QuanLyKhachSan` trên SQL Server, sau đó chạy file schema:

```bash
sqlcmd -S localhost -U sa -P <password> -d QuanLyKhachSan -i db/LotusLaverne_utf8.sql
```

### 3. Cấu hình kết nối

Mở [ConnectDB.java](src/main/java/com/lotuslaverne/util/ConnectDB.java) và chỉnh `user` / `password` cho đúng SQL Server account.

### 4. Seed dữ liệu mẫu

Chạy `SeedAdmin.java` một lần để tạo tài khoản admin mặc định.

### 5. Chạy ứng dụng

```bash
mvn clean javafx:run
```

### 6. Chạy test

```bash
mvn test
```

**63 tests, 0 failures** — bao phủ: overlap ngày, tính tiền, hoàn cọc, validate trạng thái, KM.

---

## Kiến trúc

```
src/main/java/com/lotuslaverne/
├── application/      Main, SeedAdmin
├── dao/              JDBC — chỉ thực hiện query, không chứa business logic
├── entity/           POJO thuần
├── fx/
│   ├── views/        Màn hình JavaFX — chỉ gọi Service, không chứa SQL
│   └── ...           LoginView, MainLayout, UiUtils
├── service/          Business logic layer (tách biệt hoàn toàn)
└── util/             ConnectDB, SessionContext, PdfExporter
```

### Nguyên tắc phân tầng

| Tầng | Trách nhiệm | Không được làm |
|---|---|---|
| **View** | Hiển thị, sự kiện UI, gọi Service | Chứa SQL, tính toán nghiệp vụ |
| **Service** | Business logic, validate, transaction | Trực tiếp render UI |
| **DAO** | CRUD với DB, PreparedStatement | Chứa logic tính toán |
| **Entity** | Dữ liệu thuần | Logic |

---

## Nghiệp vụ chi tiết

### 1. Luồng chính

```
Đặt phòng → Thu cọc → Check-in → Dùng dịch vụ → Checkout → Xuất hóa đơn
```

---

### 2. Đặt phòng (`DatPhongService`)

**Rule kiểm tra phòng trống:**  
Dùng overlap ngày — KHÔNG dùng trạng thái phòng:

```sql
NOT (thoiGianTraDuKien <= ngayVaoMoi OR thoiGianNhanDuKien >= ngayTraMoi)
```

**Bước thực hiện (1 transaction):**

1. Validate ngày hợp lệ (ngày trả > ngày vào)
2. Kiểm tra phòng không trùng lịch
3. Validate mã KM nếu có (`ngayApDung ≤ TODAY ≤ ngayKetThuc`)
4. Lấy giá từ `BangGia` theo kỳ hiệu lực — KHÔNG hardcode
5. `INSERT PhieuDatPhong` + `INSERT ChiTietPhieuDatPhong` (cùng transaction)
6. `INSERT PhieuThu` cọc 50% (cùng transaction)

**Cọc = 50% tổng tiền phòng**, tính ngay lúc đặt.  
1 phiếu chỉ được áp 1 mã KM, áp vào tiền phòng.

---

### 3. Huỷ đặt phòng (`HuyDatService`)

| Thời điểm huỷ so với ngày nhận | Hoàn cọc |
|---|---|
| Trước 7 ngày | 100% |
| 3 – 7 ngày | 50% |
| Trong 3 ngày hoặc no-show | 0% |

Cập nhật `trangThai → HuyDat`, ghi nhận số tiền hoàn (nếu có).

---

### 4. Check-in (`CheckInService`)

- Chỉ được check-in khi phiếu ở trạng thái `DaDat`
- Ghi `thoiGianNhanThucTe = NOW()`
- Cập nhật `Phong.trangThai → DangSuDung`
- Validate + 2 UPDATE nằm trong 1 transaction

---

### 5. Dịch vụ phát sinh

- Ghi nhận ngay khi khách sử dụng, không đợi checkout
- Mỗi dịch vụ → `INSERT ChiTietDichVu` với `maPhieuDatPhong` tương ứng
- Cộng dần vào hóa đơn cuối

---

### 6. Giá phòng

- Loại thuê: `QuaDem` (tính theo ngày)
- Lấy từ `BangGia` theo: `GETDATE() BETWEEN ngayBatDau AND ngayKetThuc`
- Giá lock tại thời điểm đặt, lưu vào `ChiTietPhieuDatPhong.donGia`
- Không thay đổi dù bảng giá cập nhật sau
- Nếu không có giá hiệu lực → **báo lỗi**, không dùng giá fallback hardcode

---

### 7. Checkout (`CheckoutService`)

Chỉ được checkout khi phiếu ở trạng thái `DaCheckIn`.

**Công thức:**
```
tienThanhToan = tienPhong + tienDichVu - tienCoc   (min = 0)
```

**5 bước trong 1 transaction:**

| Bước | Hành động |
|---|---|
| 1 | `UPDATE PhieuDatPhong → DaCheckOut` + `thoiGianTraThucTe` |
| 2 | `UPDATE Phong → PhongCanDon` (không phải PhongTrong!) |
| 3 | `INSERT HoaDon` (dùng `HoaDon.taoTuCheckout()`) |
| 4 | `INSERT ChiTietHoaDon`: tiền phòng, từng dịch vụ, tiền cọc (âm) |
| 5 | `UPDATE PhieuThu SET maHoaDon` (liên kết cọc vào hóa đơn) |

`HoaDon` **chỉ sinh từ checkout()** — không tạo thủ công.

---

### 8. Hóa đơn

- `maHoaDon` sinh tự động: `"HD"` + UUID 8 ký tự
- Không có chức năng tạo/sửa/xóa thủ công
- Xuất PDF ngay sau checkout thành công

---

### 9. Đổi phòng (`DoiPhongService`)

1. Validate phiếu ở trạng thái `DaCheckIn`
2. Kiểm tra phòng mới không trùng lịch (overlap ngày)
3. Lấy giá mới từ `BangGia`
4. Gọi `SP_DoiPhong(maPDP, maPhongMoi, giaMoi)`
5. Tính chênh lệch = `(giaMoi − giaCu) × số đêm còn lại`
6. Ghi bù trừ vào `PhieuDatPhong.ghiChu` để cashier thấy khi checkout
7. Trả về `tongBuTru` (dương = thu thêm, âm = hoàn lại, 0 = ngang)

---

### 10. Thu cọc (`PhieuThuService`)

- Tự động tính 50% = `donGia × soNgay × 50%`
- Guard chống thu 2 lần: kiểm tra `PhieuThu` trước khi tạo
- `maNhanVien` lấy từ `SessionContext` — không hardcode

---

### 11. Dashboard & Báo cáo

| Chỉ số | Nguồn |
|---|---|
| Doanh thu hôm nay | `SUM(tienThanhToan)` từ `HoaDon WHERE ngayThanhToan = TODAY` |
| Số phòng trống | `COUNT WHERE trangThai = 'PhongTrong'` |
| Tỉ lệ lấp đầy | `phòng đang dùng / tổng phòng × 100` |
| Khách đang lưu trú | `COUNT phiếu DaCheckIn` |
| Phòng cần dọn | `trangThai = 'PhongCanDon'` |

Báo cáo so sánh kỳ hiện tại vs kỳ trước cùng độ dài (7 ngày so 7 ngày trước, v.v.).

---

### 12. Vòng đời trạng thái phòng

```
PhongTrong → PhongDat → DangSuDung → PhongCanDon → PhongTrong
                ↓
             HuyDat (khi huỷ đặt)
```

Phòng sau checkout về `PhongCanDon`, không về `PhongTrong` ngay — đảm bảo quy trình housekeeping được kiểm soát trước khi nhận khách mới.

---

### 13. Vòng đời trạng thái phiếu đặt phòng

```
DaDat → DaCheckIn → DaCheckOut
  ↓
HuyDat
```

---

## Ràng buộc kiến trúc

- Business logic chỉ nằm trong **Service layer**, không trong View hoặc DAO
- Mọi write operation nhiều bảng phải dùng **transaction**
- Không hardcode `maNhanVien` — luôn lấy từ `SessionContext.getInstance().getMaNhanVien()`
- Không hardcode giá — luôn lấy từ `BangGia`
- Nếu không có giá hiệu lực → báo lỗi, không fallback
- Toàn bộ query dùng `PreparedStatement` — tránh SQL Injection

---

## Màn hình chức năng

| Màn hình | Mô tả |
|---|---|
| **Dashboard** | Thống kê tổng quan: doanh thu, phòng trống, lấp đầy, cảnh báo |
| **Quản lý phòng** | Card grid + bảng, tab Phòng Cần Dọn, cập nhật housekeeping |
| **Đặt phòng** | Wizard 3 bước, lịch Gantt 7 ngày, lọc tiện nghi, áp KM |
| **Check-in** | Danh sách phiếu DaDat, xác nhận nhận phòng |
| **Dịch vụ phòng** | Ghi nhận mini-bar, room service, giặt ủi theo phiếu |
| **Đổi phòng** | Tra cứu theo SĐT, tính bù trừ chênh lệch giá |
| **Checkout** | Tính tiền (phòng + DV − cọc − KM), xuất hóa đơn PDF |
| **Khách hàng** | CRUD, lịch sử đặt phòng |
| **Phiếu thu** | Quản lý cọc, danh sách phiếu thu |
| **Hóa đơn** | Xem lịch sử hóa đơn (chỉ đọc) |
| **Khuyến mãi** | Quản lý mã KM, hiệu lực theo ngày |
| **Nhân viên** | Quản lý hồ sơ, chấm công |
| **Báo cáo** | Doanh thu theo ngày/tháng, so sánh kỳ, phương thức thanh toán |
| **Cài đặt** | Thông tin khách sạn, bảng giá, tài khoản |

---

## Test suite

```
src/test/java/com/lotuslaverne/service/
├── DatPhongServiceTest.java         # Validate đặt phòng, overlap ngày
├── DatPhongServiceQueryTest.java    # SQL overlap query 8 trường hợp
├── CheckInServiceTest.java          # Validate trạng thái DaDat
├── CheckoutServiceTest.java         # Transaction, tính tiền, trừ cọc
├── HuyDatServiceTest.java           # 3 mức hoàn cọc
├── DoiPhongServiceTest.java         # Validate DaCheckIn, phòng trùng lịch
├── PhieuThuServiceTest.java         # Guard thu 2 lần, tính 50%
├── HoaDonServiceTest.java           # Factory method, không tạo thủ công
├── PhongServiceTest.java            # CRUD phòng, vòng đời trạng thái
└── HotelIntegrationTest.java        # Integration: đặt → check-in → checkout
```

---

## Nhóm phát triển

Đồ án môn **Phát triển Ứng dụng** — Lotus Laverne Hotel Management System.
