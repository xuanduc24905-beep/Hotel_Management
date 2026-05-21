# PHỎNG VẤN NGHIỆP VỤ — HỆ THỐNG QUẢN LÝ KHÁCH SẠN
**Tên hệ thống:** Lotus Laverne Hotel Management  
**Phiên bản:** v3.0  
**Ngôn ngữ:** Java (JavaFX) + SQL Server  
**Ngày lập:** 21/05/2026  

---

## 1. Tổng Quan Hệ Thống

### Mục tiêu nghiệp vụ
Hệ thống được xây dựng nhằm quản lý hoạt động vận hành khách sạn hằng ngày, bao gồm đặt phòng, nhận phòng, trả phòng, thanh toán, quản lý nhân viên, dịch vụ, thiết bị và phân quyền tài khoản.

### Q: Khách sạn hiện có những bộ phận nào sử dụng hệ thống?
**A:** Hệ thống phục vụ 2 bộ phận chính:
- **Quản lý (Management):** Toàn quyền trên tất cả chức năng.
- **Lễ tân (Reception):** Phụ trách đặt phòng, check-in, check-out, hóa đơn và xem danh mục.

### Q: Những vai trò nào được phép đăng nhập vào hệ thống?
**A:** Hệ thống hỗ trợ 2 vai trò:
- `QuanLy` — Quản lý
- `LeTan` — Lễ tân

> Vai trò được lưu trong bảng `TaiKhoan.vaiTro` và `NhanVien.vaiTro` ở database, không cho phép người dùng tự chọn khi đăng nhập.

### Q: Mỗi vai trò được phép thao tác những chức năng nào?

| Chức năng | Quản Lý | Lễ Tân |
|-----------|:-------:|:------:|
| Quản lý phòng | ✅ Toàn quyền | ✅ Xem + Đặt/Check-in/Check-out |
| Quản lý thiết bị | ✅ Toàn quyền | 👁 Chỉ xem |
| Quản lý dịch vụ | ✅ Toàn quyền | 👁 Chỉ xem |
| Bảng giá phòng | ✅ Toàn quyền | 👁 Chỉ xem |
| Khuyến mãi | ✅ Toàn quyền | 👁 Chỉ xem |
| Hóa đơn | ✅ Toàn quyền | ✅ Xem + Tạo khi checkout |
| Quản lý nhân viên | ✅ Toàn quyền | ❌ Không có |
| Tài khoản hệ thống | ✅ Toàn quyền | ✅ Chỉ đổi MK bản thân |
| Cài đặt hệ thống | ✅ Toàn quyền | ✅ Chỉ xem |
| Báo cáo / Thống kê | ✅ Toàn quyền | ✅ Xem |

### Q: Quy trình làm việc hiện tại?
**A:** Hệ thống thay thế hoàn toàn quy trình thủ công (giấy/Excel). Toàn bộ nghiệp vụ từ đặt phòng → check-in → dịch vụ → check-out → hóa đơn được xử lý trong phần mềm.

### Q: Hệ thống cần ưu tiên nghiệp vụ nào nhất?
**A:** Theo thứ tự ưu tiên:
1. Đặt phòng / Check-in / Check-out
2. Thanh toán và hóa đơn
3. Quản lý phòng
4. Quản lý nhân viên & phân quyền
5. Báo cáo thống kê

### Q: Dữ liệu có cần sao lưu định kỳ không?
**A:** Có. Hệ thống cấu hình sao lưu tự động **hàng ngày lúc 02:00**. Cài đặt này được quản lý trong màn hình `Cài Đặt → Hệ Thống → Tự Động Sao Lưu`.

### Q: Có cần ghi nhận lịch sử thao tác không?
**A:** Hệ thống có cài đặt **Nhật Ký Truy Cập** (bật/tắt) trong `Cài Đặt → Bảo Mật`. Mặc định: Bật.

### Q: Khách sạn có yêu cầu phân quyền chặt chẽ không?
**A:** Có. Phân quyền được kiểm tra thông qua `SessionContext.getVaiTro()` ở tầng View. Lễ tân bị ẩn toàn bộ nút thêm/sửa/xóa trên các màn hình danh mục.

### Q: Hệ thống có cần xuất file PDF, Excel, HTML không?
**A:** Có. Màn hình Hóa Đơn hỗ trợ:
- **Xuất PDF** — dùng để in hóa đơn giao khách.
- **Xuất HTML** — dùng để lưu trữ hoặc gửi email.

---

## 2. Màn Hình Quản Lý Thiết Bị

### Q: Thiết bị được quản lý theo từng phòng hay tổng số?
**A:** Quản lý **tổng số lượng toàn khách sạn** theo danh mục. Không gắn thiết bị cụ thể với phòng cụ thể trong phiên bản hiện tại.

### Q: Mỗi thiết bị lưu những thông tin nào?
**A:**

| Trường | Kiểu | Bắt buộc |
|--------|------|:--------:|
| Mã thiết bị | `VARCHAR` | ✅ |
| Tên thiết bị | `VARCHAR` | ✅ |
| Loại thiết bị | `VARCHAR` | ✅ |
| Số lượng | `INT` | ✅ |
| Đơn giá | `DECIMAL` | ✅ |
| Trạng thái | `VARCHAR` | ✅ |

### Q: Mã thiết bị do người dùng nhập hay hệ thống tự sinh?
**A:** **Người dùng nhập thủ công**. Hệ thống kiểm tra trùng mã khi thêm mới.

### Q: Loại thiết bị gồm những nhóm nào?
**A:** Điện tử, Điện lạnh, Nội thất, Vệ sinh, Khác.

### Q: Trạng thái thiết bị gồm những giá trị nào?
**A:** Tốt, Hư hỏng, Đang sửa chữa, Ngừng sử dụng.

### Q: Khi thiết bị bị hỏng có cần ghi nhận ngày hỏng không?
**A:** Phiên bản hiện tại **chưa hỗ trợ**. Chỉ cập nhật trạng thái. Có thể phát triển ở phiên bản sau.

### Q: Thiết bị có cần gắn với phòng cụ thể không?
**A:** **Chưa hỗ trợ** trong phiên bản hiện tại.

### Q: Nếu xóa thiết bị đã dùng trong phòng thì xử lý thế nào?
**A:** Hệ thống kiểm tra ràng buộc FK trong DB trước khi xóa. Nếu có dữ liệu liên quan, hiển thị thông báo lỗi và từ chối xóa.

### Q: Có cần tìm kiếm thiết bị không?
**A:** Có. Hỗ trợ tìm kiếm real-time theo tên, mã TB qua thanh search ở toolbar.

### Q: Khi sửa số lượng có kiểm tra âm không?
**A:** Có. Hệ thống validate số lượng phải ≥ 0 trước khi lưu.

### Q: Đơn giá dùng để làm gì?
**A:** Quản lý tài sản và tính giá trị bồi thường khi thiết bị bị hỏng/mất.

### Q: Có xuất danh sách thiết bị ra Excel/PDF không?
**A:** Chưa hỗ trợ ở phiên bản hiện tại. Có thể bổ sung sau.

### Q: Ai được phép thêm, sửa, xóa thiết bị?
**A:** **Chỉ Quản lý**. Lễ tân chỉ được xem danh sách.

---

## 3. Màn Hình Quản Lý Dịch Vụ

### Q: Khách sạn cung cấp những nhóm dịch vụ nào?
**A:** Ăn uống, Giặt ủi, Thuê xe, Dịch vụ phòng, Khác.

### Q: Mã dịch vụ được tự sinh hay người dùng nhập?
**A:** **Người dùng nhập thủ công**. Hệ thống kiểm tra trùng mã.

### Q: Mỗi dịch vụ lưu những thông tin nào?
**A:**

| Trường | Kiểu | Bắt buộc |
|--------|------|:--------:|
| Mã dịch vụ | `VARCHAR` | ✅ |
| Tên dịch vụ | `VARCHAR` | ✅ |
| Loại dịch vụ | `VARCHAR` | ✅ |
| Đơn giá | `DECIMAL` | ✅ |
| Trạng thái | `VARCHAR` | ✅ |

### Q: Trạng thái dịch vụ gồm những giá trị nào?
**A:** Đang kinh doanh, Ngừng kinh doanh.

### Q: Khi dịch vụ ngừng kinh doanh, hóa đơn cũ có hiển thị không?
**A:** Có. Hóa đơn cũ vẫn lưu đầy đủ. Chỉ ngăn tạo mới giao dịch với dịch vụ đó.

### Q: Có cho phép xóa dịch vụ đã phát sinh trong hóa đơn không?
**A:** **Không**. Hệ thống kiểm tra FK và từ chối xóa nếu còn giao dịch liên quan.

### Q: Đơn giá dịch vụ có thay đổi theo thời gian không?
**A:** Có. Quản lý có thể sửa đơn giá. Hóa đơn cũ **không bị ảnh hưởng** (đã chốt giá tại thời điểm phát sinh).

### Q: Dịch vụ có cần quản lý tồn kho không?
**A:** **Chưa hỗ trợ** trong phiên bản hiện tại.

### Q: Có cần tìm kiếm dịch vụ không?
**A:** Có. Tìm kiếm real-time theo tên, mã dịch vụ.

### Q: Ai được phép thêm, sửa, ngừng kinh doanh dịch vụ?
**A:** **Chỉ Quản lý**. Lễ tân chỉ xem.

### Q: Khi khách sử dụng dịch vụ, hệ thống cộng tiền vào đâu?
**A:** Cộng thẳng vào **phiếu đặt phòng** (không tạo hóa đơn riêng). Tiền dịch vụ được tổng hợp vào hóa đơn khi checkout.

---

## 4. Màn Hình Bảng Giá Phòng

### Q: Khách sạn có những loại phòng nào?
**A:** Standard, Deluxe, Superior, Suite (load động từ bảng `LoaiPhong` trong DB).

### Q: Khách sạn có những loại thuê nào?
**A:** `QuaDem` (Qua đêm), `TheoNgay` (Theo ngày), `TheoGio` (Theo giờ).

### Q: Giá phòng tính theo kỳ giá nào?
**A:** `NgayThuong`, `CuoiTuan`, `LeTet`, `CaoDiem`.

### Q: Một loại phòng có thể có nhiều bảng giá khác nhau không?
**A:** Có. Mỗi tổ hợp (Loại phòng + Loại thuê + Kỳ giá) là một bảng giá riêng biệt.

### Q: Mã bảng giá được tự sinh hay nhập thủ công?
**A:** **Nhập thủ công** bởi quản lý.

### Q: Đơn giá có ngày bắt đầu và ngày kết thúc hiệu lực không?
**A:** Có. Bắt buộc nhập `ngayBatDau` và `ngayKetThuc`. Ngày kết thúc phải sau ngày bắt đầu.

### Q: Nếu có nhiều bảng giá cùng loại phòng, cùng loại thuê, cùng thời gian thì xử lý ra sao?
**A:** Hệ thống không tự phát hiện xung đột. Quản lý chịu trách nhiệm đảm bảo không trùng. Tính năng kiểm tra xung đột có thể bổ sung sau.

### Q: Khi đặt phòng, hệ thống chọn bảng giá dựa trên tiêu chí nào?
**A:** Dựa trên: Loại phòng + Loại thuê + Kỳ giá tương ứng ngày nhận phòng.

### Q: Khuyến mãi được áp dụng trực tiếp trên bảng giá hay qua màn hình riêng?
**A:** Thông qua **màn hình Khuyến Mãi riêng**. Hệ thống tự động tính `% Giảm KM` và `Giá Sau KM` trên bảng giá hiển thị (dựa trên KM đang hiệu lực theo ngày hiện tại).

### Q: Có cho phép sửa giá sau khi bảng giá đã dùng trong phiếu đặt phòng không?
**A:** Cho phép sửa. Hóa đơn cũ không bị ảnh hưởng.

### Q: Nếu xóa bảng giá đã phát sinh giao dịch thì có cho phép không?
**A:** **Không cho phép**. Hệ thống trả về lỗi DB.

### Q: Hệ thống có cần sắp xếp và lọc bảng giá không?
**A:** Có. Hỗ trợ:
- **Lọc:** Kỳ giá, Loại phòng, Loại thuê.
- **Sắp xếp:** Theo mã BG, loại phòng, loại thuê, kỳ giá, đơn giá, ngày bắt đầu, ngày kết thúc (tăng/giảm dần).

### Q: Trường hợp khách ở qua nhiều ngày (thường + cuối tuần) tính giá thế nào?
**A:** Hiện tại áp dụng **một mức giá duy nhất** theo loại phòng + loại thuê + kỳ giá khi đặt. Tính năng chia nhỏ từng ngày có thể phát triển ở phiên bản sau.

### Q: Trường hợp Lễ/Tết trùng cuối tuần thì ưu tiên kỳ giá nào?
**A:** **Ưu tiên LeTet** (giá Lễ/Tết thường cao hơn). Quản lý cần đảm bảo cấu hình đúng kỳ giá cho ngày đó.

---

## 5. Màn Hình Quản Lý Hóa Đơn

### Q: Hóa đơn được tạo tự động hay thủ công?
**A:** **Tự động** khi nhân viên thực hiện thao tác Check-out cho khách.

### Q: Mỗi hóa đơn lưu những thông tin nào?
**A:**

| Trường | Mô tả |
|--------|-------|
| Mã hóa đơn | Tự sinh theo quy tắc `HD + timestamp` |
| Ngày lập | Ngày giờ thực hiện checkout |
| Nhân viên lập | Tên đăng nhập từ `SessionContext` |
| Mã phiếu đặt phòng | Liên kết với `PhieuDatPhong` |
| Ngày thanh toán | = Ngày lập |
| Tiền khuyến mãi | % giảm từ KM đang hiệu lực |
| Thành tiền | Tổng tiền phòng + dịch vụ – khuyến mãi |
| Phương thức thanh toán | Tiền mặt / Chuyển khoản / Thẻ / Ví điện tử |
| Ghi chú | Tùy chọn |

### Q: Một phiếu đặt phòng có thể có nhiều hóa đơn không?
**A:** **Không**. Mỗi phiếu đặt phòng có đúng **1 hóa đơn** khi checkout.

### Q: Hóa đơn gồm những khoản tiền nào?
**A:** Tiền phòng, Tiền dịch vụ, Giảm giá khuyến mãi, Tổng thành tiền.

### Q: Phương thức thanh toán gồm những loại nào?
**A:** Tiền mặt, Chuyển khoản, Thẻ, Ví điện tử.

### Q: Có cần lưu trạng thái hóa đơn không?
**A:** Có. Trạng thái: **Đã thanh toán**, **Chưa thanh toán**, **Đã hủy**.

### Q: Khi hóa đơn đã thanh toán, có cho phép sửa hoặc xóa không?
**A:** **Không cho phép** sửa/xóa hóa đơn đã thanh toán. Chỉ Quản lý mới có quyền xem đầy đủ lịch sử.

### Q: Có cần in hóa đơn trực tiếp từ hệ thống không?
**A:** Có. Hỗ trợ **xuất PDF** (in hóa đơn giao khách) và **xuất HTML** (lưu trữ/email).

### Q: Mẫu hóa đơn cần có những thông tin nào?
**A:** Thông tin khách sạn (từ `CaiDatView`), thông tin khách, thông tin phòng, chi tiết dịch vụ, tổng tiền, phương thức TT, nhân viên lập.

### Q: Có cần tìm kiếm và lọc hóa đơn không?
**A:** Có. Tìm kiếm theo: mã hóa đơn, mã phiếu, phương thức TT, ngày lập. Lọc theo khoảng thời gian.

### Q: Tổng tiền hiển thị phía trên là tổng toàn bộ hay theo kết quả tìm kiếm?
**A:** Tổng theo **kết quả đang hiển thị** (sau khi lọc/tìm kiếm).

### Q: Ai được phép xem lịch sử hóa đơn?
**A:** Cả Quản lý và Lễ tân đều có thể xem. Chỉ Quản lý mới có thể xóa.

---

## 6. Màn Hình Quản Lý Nhân Viên

### Q: Khách sạn có những loại nhân viên nào?
**A:** Hệ thống hiện hỗ trợ 2 vai trò: **Quản lý** và **Lễ tân**. (Buồng phòng, Kế toán, Bảo vệ, Kỹ thuật chưa được phân vai riêng trong phiên bản hiện tại.)

### Q: Mỗi nhân viên lưu những thông tin nào?
**A:**

| Trường | Kiểu | Bắt buộc |
|--------|------|:--------:|
| Mã nhân viên | `VARCHAR` (tự sinh UUID 4 ký tự) | ✅ |
| Họ tên | `VARCHAR` | ✅ |
| Số điện thoại | `VARCHAR` | ✅ |
| Số CCCD | `VARCHAR(12)` | ❌ (không bắt buộc) |
| Email | `VARCHAR` | ❌ (không bắt buộc) |
| Vai trò | `VARCHAR` (QuanLy/LeTan) | ✅ |
| Ca làm việc | `VARCHAR` | ❌ (mặc định: Sáng) |
| Ngày vào làm | `DATE` | ❌ |
| Trạng thái | `VARCHAR` | ✅ (mặc định: Đang Làm) |

### Q: Mã nhân viên do hệ thống tự sinh hay người dùng nhập?
**A:** **Tự sinh** theo quy tắc `NV` + 4 ký tự UUID ngẫu nhiên (VD: `NVA3F8`).

### Q: Số điện thoại có bắt buộc duy nhất không?
**A:** Có. DB có ràng buộc UNIQUE trên SĐT. Hệ thống báo lỗi nếu trùng.

### Q: CCCD có bắt buộc duy nhất không?
**A:** Có (nếu nhập). Dùng để xác minh danh tính trong chức năng "Quên mật khẩu".

### Q: Vai trò nhân viên có ảnh hưởng đến quyền đăng nhập không?
**A:** Có. Vai trò nhân viên phải **khớp** với vai trò tài khoản khi tạo tài khoản mới (hệ thống kiểm tra và cảnh báo nếu không khớp).

### Q: Ca làm việc gồm những ca nào?
**A:** Sáng (`Sang`), Chiều (`Chieu`), Đêm (`Dem`), Hành Chính (`HanhChinh`).

### Q: Một nhân viên có thể làm nhiều ca không?
**A:** **Không**. Mỗi nhân viên có 1 ca cố định.

### Q: Trạng thái nhân viên gồm những loại nào?
**A:** Đang Làm, Nghỉ Việc, Tạm Nghỉ. (Hiện tại UI mặc định hiển thị "Đang Làm", cần cập nhật DB để thay đổi.)

### Q: Nếu nhân viên nghỉ việc thì tài khoản có bị khóa không?
**A:** **Chưa tự động**. Quản lý cần xóa tài khoản thủ công trong màn hình Tài Khoản.

### Q: Có cho phép xóa nhân viên đã từng lập hóa đơn không?
**A:** **Không**. Hệ thống kiểm tra FK và từ chối xóa với thông báo: *"Không thể xóa nếu còn dữ liệu liên quan (phiếu đặt phòng, hóa đơn)."*

### Q: Ai được phép thêm, sửa, xóa nhân viên?
**A:** **Chỉ Quản lý**.

### Q: Có cần tìm kiếm nhân viên không?
**A:** Có. Tìm kiếm real-time theo tên, vai trò, số điện thoại trên thanh search.

---

## 7. Màn Hình Thêm Nhân Viên

### Q: Những trường nào là bắt buộc?
**A:** Họ và tên, Số điện thoại, Vai trò. Các trường còn lại (CCCD, Email, Ca làm) là tùy chọn.

### Q: Họ và tên có giới hạn độ dài không?
**A:** Không giới hạn cứng trong UI, phụ thuộc vào giới hạn `VARCHAR` trong DB.

### Q: Số điện thoại cần kiểm tra định dạng như thế nào?
**A:** Hiện tại chỉ kiểm tra **không được trống**. Chưa validate định dạng số điện thoại (10-11 số).

### Q: CCCD cần đúng 12 số không?
**A:** Chưa validate định dạng trong UI. Hệ thống chấp nhận mọi chuỗi ký tự.

### Q: Email có cần kiểm tra định dạng không?
**A:** Chưa validate. Cho phép bỏ trống.

### Q: Khi thêm nhân viên, hệ thống có tự động tạo tài khoản không?
**A:** **Không tự động**. Quản lý phải vào màn hình `Tài Khoản → Tạo Tài Khoản` để tạo riêng.

### Q: Vai trò mặc định khi thêm nhân viên là gì?
**A:** **Lễ Tân** (mặc định trong ComboBox).

### Q: Ca làm việc mặc định là ca nào?
**A:** **Sáng**.

### Q: Sau khi thêm thành công, hệ thống xử lý thế nào?
**A:** Hiển thị dialog thông báo thành công với mã NV được sinh, sau đó **xóa trắng form** để nhập nhân viên tiếp theo.

---

## 8. Màn Hình Tài Khoản Hệ Thống

### Q: Mỗi nhân viên có bắt buộc phải có tài khoản không?
**A:** **Không bắt buộc**. Nhân viên buồng phòng hoặc kỹ thuật có thể không cần tài khoản.

### Q: Một nhân viên có thể có nhiều tài khoản không?
**A:** **Không**. Hệ thống kiểm tra và từ chối nếu nhân viên đã có tài khoản.

### Q: Một tài khoản có thể gắn với nhiều nhân viên không?
**A:** **Không**. Quan hệ 1-1 giữa nhân viên và tài khoản.

### Q: Mã tài khoản được tự sinh hay nhập thủ công?
**A:** Tùy chọn. Nếu để trống → tự sinh theo `TK + (timestamp % 100000)`. Người dùng có thể nhập thủ công.

### Q: Tên đăng nhập có bắt buộc duy nhất không?
**A:** Có. DB có ràng buộc UNIQUE. Hệ thống báo lỗi nếu trùng.

### Q: Quy tắc đặt mật khẩu là gì?
**A:** Tối thiểu **6 ký tự**. Không yêu cầu ký tự đặc biệt, chữ hoa, số riêng.

### Q: Vai trò tài khoản gồm những loại nào?
**A:** `QuanLy`, `LeTan`. Quản lý được tạo cả 2 loại. Lễ tân chỉ được tạo tài khoản `LeTan`.

### Q: Vai trò tài khoản khác gì với vai trò nhân viên?
**A:** Phải **khớp nhau**. Hệ thống kiểm tra và cảnh báo nếu cố tạo tài khoản `QuanLy` cho nhân viên `LeTan`.

### Q: Có cần chức năng khóa tài khoản không?
**A:** Chưa hỗ trợ khóa. Hiện tại chỉ có **xóa tài khoản**.

### Q: Có hiển thị tài khoản đang đăng nhập không?
**A:** Có. Trong danh sách tài khoản và trong màn hình Đổi Mật Khẩu, tài khoản đang đăng nhập được đánh dấu **★ đang đăng nhập**.

### Q: Có cho phép xóa tài khoản đang đăng nhập không?
**A:** **Không**. Hệ thống từ chối với thông báo: *"Không thể xóa tài khoản đang đăng nhập!"*

### Q: Có cần lưu lịch sử đăng nhập không?
**A:** Chưa hỗ trợ chi tiết. Có cài đặt "Nhật Ký Truy Cập" nhưng chưa hiển thị log trong UI.

### Q: Có cần giới hạn số lần nhập sai mật khẩu không?
**A:** **Chưa hỗ trợ** trong phiên bản hiện tại.

### Q: Có cần chức năng quên mật khẩu không?
**A:** Có. Được tích hợp ngay tại màn hình đăng nhập. Xác minh bằng **tên đăng nhập + CCCD** của nhân viên, sau đó cho phép đặt mật khẩu mới.

---

## 9. Màn Hình Tạo Tài Khoản

### Q: Khi tạo tài khoản, có bắt buộc phải chọn nhân viên không?
**A:** Có. Trường **Mã nhân viên là bắt buộc (*)**.

### Q: Danh sách nhân viên hiển thị gồm tất cả hay chỉ nhân viên chưa có tài khoản?
**A:** Hiển thị **tất cả nhân viên**. Hệ thống kiểm tra và báo lỗi nếu nhân viên đã có tài khoản sau khi submit.

### Q: Tên đăng nhập có được phép trùng không?
**A:** **Không**. Báo lỗi: *"Mã TK hoặc tên đăng nhập đã tồn tại!"*

### Q: Mật khẩu ban đầu do quản lý nhập hay hệ thống tự tạo?
**A:** **Quản lý nhập thủ công**. Có nút 👁 để xem/ẩn mật khẩu khi nhập.

### Q: Có cần bắt buộc nhân viên đổi mật khẩu lần đầu đăng nhập không?
**A:** **Chưa hỗ trợ** trong phiên bản hiện tại.

### Q: Vai trò tài khoản được chọn thủ công hay lấy theo vai trò nhân viên?
**A:** **Chọn thủ công** từ ComboBox. Hệ thống **tự động kiểm tra** và cảnh báo nếu không khớp vai trò nhân viên.

### Q: Sau khi tạo tài khoản, hệ thống thông báo gì?
**A:** Hiển thị dialog: *"Tạo tài khoản thành công! Mã TK: [mã]"*. Form được xóa trắng, danh sách tài khoản tự refresh.

### Q: Có cần gửi email thông tin tài khoản không?
**A:** **Chưa hỗ trợ** trong phiên bản hiện tại.

---

## 10. Màn Hình Đổi Mật Khẩu

### Q: Ai được phép đổi mật khẩu?
**A:**
- **Người dùng tự đổi:** Chọn tài khoản của mình trong ComboBox.
- **Quản lý đổi cho nhân viên:** Chọn bất kỳ tài khoản nào trong danh sách.

### Q: Khi đổi mật khẩu, có bắt buộc nhập mật khẩu hiện tại không?
**A:** **Có**. Bắt buộc xác minh mật khẩu hiện tại trước khi đổi.

### Q: Mật khẩu mới cần đáp ứng điều kiện gì?
**A:** Tối thiểu **6 ký tự**.

### Q: Mật khẩu xác nhận có bắt buộc trùng không?
**A:** **Có**. Hệ thống kiểm tra và báo lỗi nếu không khớp.

### Q: Có biểu tượng xem/ẩn mật khẩu không?
**A:** Có. Cả 3 trường (mật khẩu hiện tại, mới, xác nhận) đều có nút 👁 toggle.

### Q: Sau khi đổi mật khẩu thành công, người dùng có bị đăng xuất không?
**A:** **Không tự động đăng xuất**. Hệ thống chỉ hiển thị thông báo thành công.

### Q: Có cần lưu ngày đổi mật khẩu gần nhất không?
**A:** **Chưa hỗ trợ** trong phiên bản hiện tại.

### Q: Có bắt buộc đổi mật khẩu định kỳ không?
**A:** Có cài đặt trong `Cài Đặt → Bảo Mật → Đổi Mật Khẩu Định Kỳ` (mặc định: 90 ngày). Hiện tại là cấu hình UI, chưa tự động enforce.

---

## 11. Màn Hình Cài Đặt

### Q: Thông tin khách sạn gồm những dữ liệu nào?
**A:**

| Key | Mặc định |
|-----|----------|
| Tên khách sạn | Lotus Laverne Hotel |
| Địa chỉ | 123 Nguyễn Huệ, Q.1, TP.HCM |
| Điện thoại | (028) 3822 1234 |
| Email | info@lotuslaverne.vn |
| Website | www.lotuslaverne.vn |
| Số phòng | 30 phòng |

### Q: Những thông tin này có được in trên hóa đơn không?
**A:** Có. Thông tin khách sạn được đọc từ `caidat.properties` và hiển thị trên mẫu hóa đơn PDF/HTML.

### Q: Ai được phép chỉnh sửa thông tin khách sạn?
**A:** Hiện tại **không có phân quyền riêng** cho màn hình Cài Đặt. Cần bổ sung kiểm tra `canEdit()` nếu cần.

### Q: Khi thay đổi thông tin khách sạn, hóa đơn cũ có cập nhật theo không?
**A:** **Không**. Hóa đơn cũ đã chốt tại thời điểm in. Thông tin mới chỉ áp dụng cho hóa đơn được tạo sau khi lưu.

### Q: Số phòng được nhập thủ công hay tự tính?
**A:** **Nhập thủ công** (chuỗi text, VD: "30 phòng"). Không tự tính từ danh sách phòng.

### Q: Hệ thống cần thông báo những sự kiện nào?
**A:**

| Thông báo | Mặc định |
|-----------|:--------:|
| Nhận phòng mới | ✅ Bật |
| Trả phòng | ✅ Bật |
| Thanh toán | ✅ Bật |
| Cảnh báo hệ thống | ❌ Tắt |
| Báo cáo hàng ngày | ✅ Bật |
| Email tổng kết tuần | ❌ Tắt |

### Q: Thông báo hiển thị trong app, gửi email hay cả hai?
**A:** Hiện tại là **cấu hình UI** (bật/tắt toggle). Chức năng gửi email thực tế chưa được tích hợp.

### Q: Cài đặt bảo mật gồm những mục nào?

| Cài đặt | Mặc định |
|---------|:--------:|
| Xác thực 2 bước | Tắt |
| Thời gian hết phiên | 30 phút |
| Nhật ký truy cập | Bật |
| Mã hoá dữ liệu | Bật |
| Đổi mật khẩu định kỳ | 90 ngày |

### Q: Nếu người dùng không hoạt động 60 phút thì tự đăng xuất không?
**A:** Cài đặt thời gian hết phiên mặc định là **30 phút**. Tính năng tự đăng xuất chưa được implement trong phiên bản hiện tại.

### Q: Cài đặt hệ thống gồm những mục nào?

| Cài đặt | Giá trị |
|---------|---------|
| Phiên bản | v1.0.0 |
| Ngôn ngữ | Tiếng Việt |
| Múi giờ | GMT+7 (Hà Nội) |
| Định dạng ngày | dd/MM/yyyy |
| Đơn vị tiền tệ | VND (₫) |
| Tự động sao lưu | Hàng ngày 02:00 |

### Q: Cài đặt có hỗ trợ tiếng Anh, ngoại tệ, múi giờ khác không?
**A:** Hiện tại là cấu hình text (chỉnh sửa thủ công). Chưa có logic tự động đổi ngôn ngữ UI hay convert tiền tệ.

### Q: Dữ liệu cài đặt được lưu ở đâu?
**A:** Lưu trong file `caidat.properties` tại thư mục làm việc của ứng dụng (cùng cấp với file JAR).

### Q: Có cần phục hồi dữ liệu từ bản sao lưu không?
**A:** Cần bổ sung tính năng này. Hiện tại chỉ có cấu hình lịch sao lưu, chưa có giao diện phục hồi.

---

## Phụ Lục — Thông Tin Kỹ Thuật

### Stack công nghệ
- **Frontend:** JavaFX (FXML + CSS inline)
- **Backend:** Java (3-layer: Entity → DAO → View)
- **Database:** SQL Server (JDBC)
- **Build:** Maven / Gradle
- **Bảo mật mật khẩu:** Hash (PasswordUtil)

### Giá trị vai trò trong DB
| Hiển thị | Giá trị DB |
|----------|-----------|
| Quản Lý | `QuanLy` |
| Lễ Tân | `LeTan` |

### Phân quyền kỹ thuật
```java
// Kiểm tra trong mỗi View
private boolean canEdit() {
    String role = SessionContext.getInstance().getVaiTro();
    return "QuanLy".equalsIgnoreCase(role) || "Admin".equalsIgnoreCase(role);
}
```

### SessionContext được khởi tạo ở đâu?
```java
// LoginView.java — sau khi xác thực thành công
SessionContext.getInstance().init(
    tk.getMaNhanVien(),   // Mã nhân viên
    username,             // Tên đăng nhập
    tk.getVaiTro()        // "QuanLy" hoặc "LeTan"
);
```

---

*Tài liệu này được tạo tự động từ source code thực tế của dự án Lotus Laverne Hotel Management.*  
*Cập nhật lần cuối: 21/05/2026*
