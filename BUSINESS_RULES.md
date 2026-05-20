# BUSINESS_RULES.md — Hệ thống quản lý khách sạn Lotus Laverne

## 1. Luồng nghiệp vụ chính
Đặt phòng → Thu cọc → Check-in → Dùng dịch vụ → Checkout → Xuất hoá đơn

## 2. Đặt phòng
- Khách đặt qua điện thoại hoặc walk-in, nhân viên tạo phiếu trên hệ thống
- 1 phiếu đặt có thể gồm nhiều phòng
- Kiểm tra phòng trống PHẢI dùng overlap ngày, KHÔNG dùng trạng thái phòng:
  NOT (ngayTra <= ngayVaoMoi OR ngayVao >= ngayTraMoi)
- Cọc = 50% tổng tiền phòng, thu ngay khi tạo phiếu
- Phiếu đặt + PhieuThu cọc phải nằm trong cùng 1 transaction
- 1 phiếu chỉ được áp 1 mã KM, áp vào tiền phòng, nhân viên nhập tay
- Mã KM phải còn hiệu lực tại ngày đặt (kiểm tra ngayApDung/ngayKetThuc)

## 3. Huỷ đặt phòng
- Huỷ trước 7 ngày so với ngày nhận phòng → hoàn 100% cọc
- Huỷ trong khoảng 3-7 ngày → hoàn 50% cọc
- Huỷ trong 3 ngày hoặc no-show → mất toàn bộ cọc
- Cập nhật trạng thái phiếu → HuyDat, ghi nhận số tiền hoàn (nếu có)

## 4. Check-in
- Chỉ được check-in khi phiếu ở trạng thái DaDat
- Ghi nhận thoiGianNhanThucTe = thời điểm thực tế check-in
- Cập nhật trạng thái phòng → DangSuDung

## 5. Dịch vụ phát sinh
- Ghi nhận ngay khi khách sử dụng, không đợi checkout
- Mỗi dịch vụ ghi vào ChiTietDichVu với maPhieuDatPhong tương ứng
- Cộng dần vào hoá đơn cuối

## 6. Giá phòng
- Tính theo ngày (loaiThue = 'QuaDem')
- Lấy từ BangGia theo kỳ hiệu lực: GETDATE() BETWEEN ngayBatDau AND ngayKetThuc
- Nếu không có giá hiệu lực → báo lỗi, KHÔNG dùng giá fallback hardcode
- Giá lấy tại thời điểm đặt, không thay đổi dù bảng giá sau này thay đổi

## 7. Checkout
- Chỉ được checkout khi phiếu ở trạng thái DaCheckIn
- tienThanhToan = tienPhong + tienDichVu - tienCoc (không được âm, min = 0)
- Toàn bộ các bước sau phải trong 1 transaction:
  1. UPDATE PhieuDatPhong → DaCheckOut + thoiGianTraThucTe
  2. UPDATE Phong → PhongCanDon
  3. INSERT HoaDon (dùng HoaDon.taoTuCheckout())
  4. INSERT ChiTietHoaDon: tiền phòng, từng dịch vụ, tiền cọc (âm)
  5. UPDATE PhieuThu SET maHoaDon (link cọc vào hoá đơn)
- HoaDon KHÔNG được tạo thủ công, chỉ sinh từ checkout()

## 8. Hoá đơn
- maHoaDon sinh tự động: "HD" + UUID 8 ký tự
- Không có chức năng tạo/sửa/xoá hoá đơn thủ công
- Xuất PDF sau khi checkout thành công

## 9. Dashboard
- Doanh thu hôm nay = SUM(tienThanhToan) từ HoaDon WHERE ngayThanhToan = TODAY
- Số phòng trống = COUNT WHERE trangThai = 'PhongTrong'
- Tỉ lệ lấp đầy = phòng đang dùng / tổng phòng × 100
- Số khách đang lưu trú = COUNT phiếu DaCheckIn
- Cảnh báo phòng cần dọn = trangThai = 'PhongCanDon'

## 10. Báo cáo
- Doanh thu theo ngày/tháng từ bảng HoaDon
- Top loại phòng theo số lượt đặt
- So sánh doanh thu kỳ này vs kỳ trước cùng kỳ

## 11. Ràng buộc chung
- Tất cả business logic nằm trong Service layer, KHÔNG trong View hoặc DAO
- DAO chỉ thực hiện query, KHÔNG chứa logic tính toán
- View chỉ gọi Service, KHÔNG chứa SQL hoặc business logic
- Mọi write operation phức tạp (nhiều bảng) phải dùng transaction
- KHÔNG hardcode mã nhân viên — luôn lấy từ SessionContext.getInstance().getMaNhanVien()
- KHÔNG hardcode giá — luôn lấy từ BangGia

## 12. Check-in
- Walk-in (không đặt trước): thu tiền đêm đầu + cọc ngay tại quầy trước khi giao phòng
- Có đặt trước: verify thông tin phiếu đặt, thu cọc nếu chưa thu
- Chỉ check-in được khi phiếu ở trạng thái DaDat
- Walk-in tạo PhieuDatPhong + PhieuThu + check-in phải trong 1 transaction (WalkInService)

## 13. Dịch vụ phòng
- Ghi nhận ngay khi khách sử dụng
- Cho phép sửa số lượng / xoá khi phiếu chưa checkout
- Mỗi lần sửa/xoá phải ghi log: maNhanVien, thoiGianSua, giaTriCu, giaTriMoi
- Sau khi checkout → khoá, không cho sửa/xoá

## 14. Đổi phòng
- Chênh lệch giá (phòng mới - phòng cũ) × số đêm còn lại
- Không thu/hoàn ngay — cộng vào tổng tiền lúc checkout (ghi vào ghiChu phiếu)
- Phải kiểm tra phòng mới còn trống (overlap ngày) trước khi đổi
- Chỉ đổi được khi phiếu đang DaCheckIn

## 15. Tiền cọc
- Thu 1 lần, 50% tổng tiền phòng
- Thu tại thời điểm đặt phòng hoặc lúc check-in (walk-in)
- Hoàn/trừ cọc lúc checkout theo rule Section 3
