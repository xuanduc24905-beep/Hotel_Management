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

---

## Tính năng chính

- **Dashboard** — thống kê tổng quan phòng, doanh thu, công suất
- **Đặt phòng** — tạo phiếu đặt, chọn phòng, thu tiền cọc
- **Check-in / Check-out** — xác nhận nhận phòng, thanh toán có trừ cọc, xuất hóa đơn PDF
- **Quản lý phòng** — theo dõi trạng thái phòng, tab Phòng Cần Dọn, cập nhật housekeeping
- **Khách hàng** — tìm kiếm, thêm/sửa hồ sơ khách
- **Nhân viên & tài khoản** — phân quyền, đổi mật khẩu, chấm công
- **Dịch vụ & hóa đơn** — ghi nhận dịch vụ phát sinh, in phiếu thu
- **Báo cáo** — doanh thu theo ngày/tháng, công suất phòng
- **Cài đặt** — thông tin khách sạn, bảo mật, thông báo

---

## Cài đặt & chạy

### Yêu cầu

- JDK 17+
- Maven 3.6+
- SQL Server đang chạy ở `localhost:1433`

### 1. Clone repo

```bash
git clone https://github.com/xuanduc24905-beep/Hotel_Management.git
cd Hotel_Management
```

### 2. Tạo database

Tạo database tên `QuanLyKhachSan` trên SQL Server, sau đó import schema từ file `.sql` trong thư mục `db/` (nếu có).

### 3. Cấu hình kết nối

Mở [ConnectDB.java](src/main/java/com/lotuslaverne/util/ConnectDB.java) và chỉnh `user` / `password` cho đúng SQL Server account của bạn.

### 4. Chạy ứng dụng

```bash
mvn clean javafx:run
```

---

## Cấu trúc project

```
src/main/java/com/lotuslaverne/
├── application/      # Main entry point, SeedAdmin
├── dao/              # Data Access Objects (JDBC)
├── entity/           # Model classes
├── fx/
│   ├── views/        # Các màn hình chức năng
│   └── ...           # LoginView, MainLayout, UiUtils
└── util/             # ConnectDB, SessionContext, PdfExporter, ...
```

---

## Tài khoản mặc định

Chạy `SeedAdmin.java` một lần để tạo tài khoản admin mặc định, sau đó đăng nhập qua màn hình Login.

---

## Nhóm phát triển

Đồ án môn **Phát triển Ứng dụng** — Lotus Laverne Hotel Management System.
