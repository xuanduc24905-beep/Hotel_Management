-- ============================================================
-- LOTUS LAVERNE HOTEL MANAGEMENT SYSTEM
-- Database Schema v2.0
-- SQL Server 2017+  |  Collation: Vietnamese_CI_AS
-- ============================================================

USE [master]
GO

IF EXISTS (SELECT name FROM sys.databases WHERE name = N'QuanLyKhachSan')
    DROP DATABASE [QuanLyKhachSan]
GO

CREATE DATABASE [QuanLyKhachSan]
    COLLATE Vietnamese_CI_AS
GO

USE [QuanLyKhachSan]
GO

-- ============================================================
-- PHẦN 1: BẢNG (thứ tự theo phụ thuộc khóa ngoại)
-- ============================================================

-- 1. Loại Phòng
CREATE TABLE LoaiPhong (
    maLoaiPhong     NVARCHAR(10)   NOT NULL,
    tenLoaiPhong    NVARCHAR(100)  NOT NULL,
    soNguoiToiDa    INT            NOT NULL DEFAULT 2,
    moTa            NVARCHAR(255)  NULL,
    CONSTRAINT PK_LoaiPhong PRIMARY KEY (maLoaiPhong),
    CONSTRAINT CHK_LoaiPhong_SoNguoi CHECK (soNguoiToiDa > 0)
);
GO

-- 2. Loại Dịch Vụ  (bảng mới - v1 thiếu)
CREATE TABLE LoaiDichVu (
    maLoaiDichVu    NVARCHAR(10)   NOT NULL,
    tenLoaiDichVu   NVARCHAR(100)  NOT NULL,
    CONSTRAINT PK_LoaiDichVu PRIMARY KEY (maLoaiDichVu)
);
GO

-- 2.5 Thiết Bị
CREATE TABLE ThietBi (
    maThietBi    NVARCHAR(10)  NOT NULL,
    tenThietBi   NVARCHAR(100) NOT NULL,
    loaiThietBi  NVARCHAR(50)  NOT NULL,
    soLuong      INT           NOT NULL DEFAULT 0,
    donGia       DECIMAL(18,2) NOT NULL DEFAULT 0,
    trangThai    NVARCHAR(20)  NOT NULL DEFAULT N'Tot',
    CONSTRAINT PK_ThietBi PRIMARY KEY (maThietBi),
    CONSTRAINT CHK_ThietBi_TrangThai CHECK (trangThai IN (N'Tot', N'CanBaoTri', N'HuHong'))
);
GO

-- 3. Nhân Viên   (đổi hoTenNhanVien → tenNhanVien để khớp entity Java)
CREATE TABLE NhanVien (
    maNhanVien          NVARCHAR(10)   NOT NULL,
    tenNhanVien         NVARCHAR(100)  NOT NULL,
    soDienThoai         NVARCHAR(15)   NULL,
    cccd                NVARCHAR(20)   NULL,
    ngaySinh            DATE           NULL,
    ngayBatDauLam       DATE           NULL,
    ngayKetThucHopDong  DATE           NULL,
    diaChi              NVARCHAR(200)  NULL,
    email               NVARCHAR(100)  NULL,
    vaiTro              NVARCHAR(10)   NOT NULL,
    caLamViec           NVARCHAR(15)   NULL,
    CONSTRAINT PK_NhanVien       PRIMARY KEY (maNhanVien),
    CONSTRAINT UQ_NhanVien_CCCD  UNIQUE (cccd),
    CONSTRAINT CHK_NhanVien_VaiTro     CHECK (vaiTro IN (N'LeTan', N'QuanLy')),
    CONSTRAINT CHK_NhanVien_CaLamViec  CHECK (caLamViec IS NULL OR caLamViec IN (N'Sang', N'Chieu', N'Dem', N'HanhChinh'))
);
GO

-- 4. Khách Hàng  (đổi cccd → cmnd để khớp entity Java)
CREATE TABLE KhachHang (
    maKH        NVARCHAR(10)   NOT NULL,
    hoTenKH     NVARCHAR(100)  NOT NULL,
    soDienThoai NVARCHAR(15)   NOT NULL,
    cmnd        NVARCHAR(20)   NOT NULL,
    gioiTinh    BIT            NOT NULL DEFAULT 1,
    ngaySinh    DATE           NULL,
    diaChi      NVARCHAR(200)  NULL,
    quocTich    NVARCHAR(50)   NOT NULL DEFAULT N'Việt Nam',
    CONSTRAINT PK_KhachHang       PRIMARY KEY (maKH),
    CONSTRAINT UQ_KhachHang_CMND  UNIQUE (cmnd),
    CONSTRAINT UQ_KhachHang_SDT   UNIQUE (soDienThoai)
);
GO

-- 5. Tài Khoản
CREATE TABLE TaiKhoan (
    maTaiKhoan  NVARCHAR(10)   NOT NULL,
    maNhanVien  NVARCHAR(10)   NOT NULL,
    vaiTro      NVARCHAR(10)   NOT NULL,
    tenDangNhap NVARCHAR(50)   NOT NULL,
    matKhau     NVARCHAR(255)  NOT NULL,
    CONSTRAINT PK_TaiKhoan              PRIMARY KEY (maTaiKhoan),
    CONSTRAINT UQ_TaiKhoan_NhanVien     UNIQUE (maNhanVien),
    CONSTRAINT UQ_TaiKhoan_TenDangNhap  UNIQUE (tenDangNhap),
    CONSTRAINT FK_TaiKhoan_NhanVien     FOREIGN KEY (maNhanVien) REFERENCES NhanVien (maNhanVien),
    CONSTRAINT CHK_TaiKhoan_VaiTro CHECK (vaiTro IN (N'LeTan', N'QuanLy'))
);
GO

-- 6. Khuyến Mãi
CREATE TABLE KhuyenMai (
    maKhuyenMai     NVARCHAR(10)   NOT NULL,
    tenKhuyenMai    NVARCHAR(100)  NOT NULL,
    phanTramGiam    DECIMAL(5,2)   NOT NULL,
    ngayApDung      DATE           NOT NULL,
    ngayKetThuc     DATE           NOT NULL,
    dieuKienApDung  NVARCHAR(255)  NULL,
    CONSTRAINT PK_KhuyenMai         PRIMARY KEY (maKhuyenMai),
    CONSTRAINT CHK_KhuyenMai_PhanTram CHECK (phanTramGiam > 0 AND phanTramGiam <= 100),
    CONSTRAINT CHK_KhuyenMai_Ngay    CHECK (ngayKetThuc > ngayApDung)
);
GO

-- 7. Phòng  (trangThai dùng enum khớp Java: 'PhongTrong','PhongDat','PhongCanDon','DangDon','BaoTri')
CREATE TABLE Phong (
    maPhong      NVARCHAR(10)   NOT NULL,
    tenPhong     NVARCHAR(50)   NOT NULL,
    maLoaiPhong  NVARCHAR(10)   NOT NULL,
    trangThai    NVARCHAR(20)   NOT NULL DEFAULT N'PhongTrong',
    tienNghi     NVARCHAR(500)  NULL,        -- "WiFi,TV,Điều Hòa,Bồn Tắm,..."
    soNguoiToiDa INT            NULL DEFAULT 2,
    moTa         NVARCHAR(1000) NULL,
    CONSTRAINT PK_Phong           PRIMARY KEY (maPhong),
    CONSTRAINT FK_Phong_LoaiPhong FOREIGN KEY (maLoaiPhong) REFERENCES LoaiPhong (maLoaiPhong),
    CONSTRAINT CHK_Phong_TrangThai CHECK (trangThai IN (N'PhongTrong', N'PhongDat', N'PhongCanDon', N'DangDon', N'BaoTri'))
);
GO

-- 8. Bảng Giá
CREATE TABLE BangGia (
    maBangGia   NVARCHAR(10)   NOT NULL,
    maLoaiPhong NVARCHAR(10)   NOT NULL,
    loaiThue    NVARCHAR(10)   NOT NULL,
    donGia      DECIMAL(18,2)  NOT NULL,
    ngayBatDau  DATE           NOT NULL,
    ngayKetThuc DATE           NOT NULL,
    CONSTRAINT PK_BangGia           PRIMARY KEY (maBangGia),
    CONSTRAINT FK_BangGia_LoaiPhong FOREIGN KEY (maLoaiPhong) REFERENCES LoaiPhong (maLoaiPhong),
    CONSTRAINT CHK_BangGia_LoaiThue CHECK (loaiThue IN (N'QuaDem', N'TheoNgay', N'TheoGio')),
    CONSTRAINT CHK_BangGia_DonGia   CHECK (donGia > 0),
    CONSTRAINT CHK_BangGia_Ngay     CHECK (ngayKetThuc > ngayBatDau)
);
GO

-- 9. Dịch Vụ  (maLoaiDichVu là FK → LoaiDichVu; bỏ soLuongTonKho; thêm trangThai)
CREATE TABLE DichVu (
    maDichVu        NVARCHAR(10)   NOT NULL,
    tenDichVu       NVARCHAR(100)  NOT NULL,
    maLoaiDichVu    NVARCHAR(10)   NOT NULL,
    donGia          DECIMAL(18,2)  NOT NULL,
    trangThai       NVARCHAR(20)   NOT NULL DEFAULT N'DangKinhDoanh',
    CONSTRAINT PK_DichVu              PRIMARY KEY (maDichVu),
    CONSTRAINT FK_DichVu_LoaiDichVu   FOREIGN KEY (maLoaiDichVu) REFERENCES LoaiDichVu (maLoaiDichVu),
    CONSTRAINT CHK_DichVu_DonGia      CHECK (donGia > 0),
    CONSTRAINT CHK_DichVu_TrangThai   CHECK (trangThai IN (N'DangKinhDoanh', N'NgungKinhDoanh'))
);
GO

-- 10. Phiếu Đặt Phòng  (thêm hinhThucDat + trangThai)
CREATE TABLE PhieuDatPhong (
    maPhieuDatPhong     NVARCHAR(10)   NOT NULL,
    ngayDat             DATETIME       NOT NULL DEFAULT GETDATE(),
    maKhachHang         NVARCHAR(10)   NOT NULL,
    maNhanVien          NVARCHAR(10)   NOT NULL,
    soNguoi             INT            NOT NULL,
    thoiGianNhanDuKien  DATETIME       NOT NULL,
    thoiGianNhanThucTe  DATETIME       NULL,
    thoiGianTraDuKien   DATETIME       NOT NULL,
    thoiGianTraThucTe   DATETIME       NULL,
    hinhThucDat         NVARCHAR(20)   NOT NULL DEFAULT N'TrucTiep',
    trangThai           NVARCHAR(15)   NOT NULL DEFAULT N'DaDat',
    ghiChu              NVARCHAR(500)  NULL,
    CONSTRAINT PK_PhieuDatPhong      PRIMARY KEY (maPhieuDatPhong),
    CONSTRAINT FK_PDP_KhachHang      FOREIGN KEY (maKhachHang) REFERENCES KhachHang (maKH),
    CONSTRAINT FK_PDP_NhanVien       FOREIGN KEY (maNhanVien)  REFERENCES NhanVien (maNhanVien),
    CONSTRAINT CHK_PDP_SoNguoi       CHECK (soNguoi > 0),
    CONSTRAINT CHK_PDP_Ngay          CHECK (thoiGianTraDuKien > thoiGianNhanDuKien),
    CONSTRAINT CHK_PDP_HinhThuc      CHECK (hinhThucDat IN (N'TrucTiep', N'QuaDienThoai', N'QuaWeb')),
    CONSTRAINT CHK_PDP_TrangThai     CHECK (trangThai IN (N'DaDat', N'DaCheckIn', N'DaCheckOut', N'DaHuy'))
);
GO

-- 11. Chi Tiết Phiếu Đặt Phòng  (bỏ maPhuThu - phụ thu thuộc về hóa đơn)
CREATE TABLE ChiTietPhieuDatPhong (
    maPhieuDatPhong NVARCHAR(10)   NOT NULL,
    maPhong         NVARCHAR(10)   NOT NULL,
    donGia          DECIMAL(18,2)  NOT NULL,
    CONSTRAINT PK_ChiTietPDP        PRIMARY KEY (maPhieuDatPhong, maPhong),
    CONSTRAINT FK_CTPDP_PhieuDat    FOREIGN KEY (maPhieuDatPhong) REFERENCES PhieuDatPhong (maPhieuDatPhong),
    CONSTRAINT FK_CTPDP_Phong       FOREIGN KEY (maPhong)         REFERENCES Phong (maPhong),
    CONSTRAINT CHK_CTPDP_DonGia     CHECK (donGia > 0)
);
GO

-- 12. Chi Tiết Dịch Vụ (dịch vụ phát sinh trong kỳ lưu trú)
CREATE TABLE ChiTietDichVu (
    maDichVu        NVARCHAR(10)   NOT NULL,
    maPhieuDatPhong NVARCHAR(10)   NOT NULL,
    soLuong         INT            NOT NULL,
    thoiDiemSuDung  DATETIME       NOT NULL DEFAULT GETDATE(),
    ghiChu          NVARCHAR(255)  NULL,
    CONSTRAINT PK_ChiTietDichVu     PRIMARY KEY (maDichVu, maPhieuDatPhong),
    CONSTRAINT FK_CTDV_DichVu       FOREIGN KEY (maDichVu)        REFERENCES DichVu (maDichVu),
    CONSTRAINT FK_CTDV_PhieuDat     FOREIGN KEY (maPhieuDatPhong) REFERENCES PhieuDatPhong (maPhieuDatPhong),
    CONSTRAINT CHK_CTDV_SoLuong     CHECK (soLuong > 0)
);
GO

-- 13. Phụ Thu (khoản thu phát sinh ngoài phòng)
CREATE TABLE PhuThu (
    maPhuThu    NVARCHAR(10)   NOT NULL,
    tenPhuThu   NVARCHAR(100)  NOT NULL,
    loaiPhuThu  NVARCHAR(20)   NOT NULL,
    donGia      DECIMAL(18,2)  NOT NULL,
    ghiChu      NVARCHAR(255)  NULL,
    CONSTRAINT PK_PhuThu          PRIMARY KEY (maPhuThu),
    CONSTRAINT CHK_PhuThu_DonGia  CHECK (donGia > 0),
    CONSTRAINT CHK_PhuThu_Loai    CHECK (loaiPhuThu IN (N'HuHongDoDac', N'RaMuon', N'NhanMuon'))
);
GO

-- 14. Hóa Đơn
CREATE TABLE HoaDon (
    maHoaDon            NVARCHAR(10)   NOT NULL,
    ngayLap             DATETIME       NOT NULL DEFAULT GETDATE(),
    maNhanVienLap       NVARCHAR(10)   NOT NULL,
    maPhieuDatPhong     NVARCHAR(10)   NOT NULL,
    ngayThanhToan       DATETIME       NULL,
    tienKhuyenMai       DECIMAL(18,2)  NOT NULL DEFAULT 0,
    tienThanhToan       DECIMAL(18,2)  NOT NULL,
    phuongThucThanhToan NVARCHAR(15)   NOT NULL,
    ghiChu              NVARCHAR(500)  NULL,
    CONSTRAINT PK_HoaDon            PRIMARY KEY (maHoaDon),
    CONSTRAINT FK_HoaDon_NhanVien   FOREIGN KEY (maNhanVienLap)   REFERENCES NhanVien (maNhanVien),
    CONSTRAINT FK_HoaDon_PhieuDat   FOREIGN KEY (maPhieuDatPhong) REFERENCES PhieuDatPhong (maPhieuDatPhong),
    CONSTRAINT CHK_HoaDon_PhuongThuc    CHECK (phuongThucThanhToan IN (N'TienMat', N'ChuyenKhoan')),
    CONSTRAINT CHK_HoaDon_TienThanhToan CHECK (tienThanhToan >= 0)
);
GO

-- 15. Chi Tiết Hóa Đơn
CREATE TABLE ChiTietHoaDon (
    maChiTiet   INT            NOT NULL IDENTITY(1,1),
    maHoaDon    NVARCHAR(10)   NOT NULL,
    loaiTien    NVARCHAR(15)   NOT NULL,
    maKhuyenMai NVARCHAR(10)   NULL,
    moTa        NVARCHAR(255)  NULL,
    donGia      DECIMAL(18,2)  NOT NULL,
    soLuong     INT            NOT NULL DEFAULT 1,
    thanhTien   DECIMAL(18,2)  NOT NULL,
    CONSTRAINT PK_ChiTietHoaDon     PRIMARY KEY (maChiTiet),
    CONSTRAINT FK_CTHD_HoaDon       FOREIGN KEY (maHoaDon)    REFERENCES HoaDon (maHoaDon),
    CONSTRAINT FK_CTHD_KhuyenMai    FOREIGN KEY (maKhuyenMai) REFERENCES KhuyenMai (maKhuyenMai),
    CONSTRAINT CHK_CTHD_LoaiTien    CHECK (loaiTien IN (N'TienPhong', N'TienDichVu', N'TienPhuThu', N'TienCoc')),
    CONSTRAINT CHK_CTHD_SoLuong     CHECK (soLuong > 0)
);
GO

-- 16. Phiếu Thu (đặt cọc)
CREATE TABLE PhieuThu (
    maPhieuThu          NVARCHAR(10)   NOT NULL,
    maHoaDon            NVARCHAR(10)   NULL,
    maNhanVienLap       NVARCHAR(10)   NOT NULL,
    maPhieuDatPhong     NVARCHAR(10)   NOT NULL,
    soTienCoc           DECIMAL(18,2)  NOT NULL DEFAULT 0,
    ngayThu             DATETIME       NOT NULL DEFAULT GETDATE(),
    phuongThucThanhToan NVARCHAR(15)   NOT NULL,
    ghiChu              NVARCHAR(500)  NULL,
    CONSTRAINT PK_PhieuThu              PRIMARY KEY (maPhieuThu),
    CONSTRAINT FK_PhieuThu_HoaDon       FOREIGN KEY (maHoaDon)        REFERENCES HoaDon (maHoaDon),
    CONSTRAINT FK_PhieuThu_NhanVien     FOREIGN KEY (maNhanVienLap)   REFERENCES NhanVien (maNhanVien),
    CONSTRAINT FK_PhieuThu_PhieuDat     FOREIGN KEY (maPhieuDatPhong) REFERENCES PhieuDatPhong (maPhieuDatPhong),
    CONSTRAINT CHK_PhieuThu_PhuongThuc  CHECK (phuongThucThanhToan IN (N'TienMat', N'ChuyenKhoan'))
);
GO

-- ============================================================
-- PHẦN 2: INDEXES
-- ============================================================

CREATE NONCLUSTERED INDEX IDX_KhachHang_SDT        ON KhachHang (soDienThoai);
CREATE NONCLUSTERED INDEX IDX_KhachHang_CMND       ON KhachHang (cmnd);
CREATE NONCLUSTERED INDEX IDX_PDP_KhachHang        ON PhieuDatPhong (maKhachHang);
CREATE NONCLUSTERED INDEX IDX_PDP_ThoiGian         ON PhieuDatPhong (thoiGianNhanDuKien, thoiGianTraDuKien);
CREATE NONCLUSTERED INDEX IDX_PDP_TrangThai        ON PhieuDatPhong (trangThai);
CREATE NONCLUSTERED INDEX IDX_ChiTietPDP_Phong     ON ChiTietPhieuDatPhong (maPhong);
CREATE NONCLUSTERED INDEX IDX_HoaDon_NgayThanhToan ON HoaDon (ngayThanhToan);
CREATE NONCLUSTERED INDEX IDX_DichVu_TrangThai     ON DichVu (trangThai);
GO

-- ============================================================
-- PHẦN 3: VIEWS
-- ============================================================

-- Danh sách phòng + loại + giá qua đêm hiện hành
CREATE VIEW VW_DanhSachPhong AS
SELECT
    p.maPhong,
    p.tenPhong,
    lp.maLoaiPhong,
    lp.tenLoaiPhong,
    lp.soNguoiToiDa,
    p.trangThai,
    bg.donGia AS giaQuaDem
FROM Phong p
JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong
LEFT JOIN BangGia bg ON lp.maLoaiPhong = bg.maLoaiPhong
    AND bg.loaiThue = N'QuaDem'
    AND CAST(GETDATE() AS DATE) BETWEEN bg.ngayBatDau AND bg.ngayKetThuc;
GO

-- Doanh thu theo tháng
CREATE VIEW VW_DoanhThuTheoThang AS
SELECT
    YEAR(h.ngayThanhToan)   AS Nam,
    MONTH(h.ngayThanhToan)  AS Thang,
    COUNT(h.maHoaDon)       AS SoHoaDon,
    SUM(h.tienThanhToan)    AS TongDoanhThu,
    SUM(h.tienKhuyenMai)    AS TongGiamGia
FROM HoaDon h
WHERE h.ngayThanhToan IS NOT NULL
GROUP BY YEAR(h.ngayThanhToan), MONTH(h.ngayThanhToan);
GO

-- Phòng được đặt nhiều nhất
CREATE VIEW VW_PhongDatNhieuNhat AS
SELECT
    ct.maPhong,
    p.tenPhong,
    lp.tenLoaiPhong,
    COUNT(*) AS SoLuotDat
FROM ChiTietPhieuDatPhong ct
JOIN Phong p     ON ct.maPhong      = p.maPhong
JOIN LoaiPhong lp ON p.maLoaiPhong  = lp.maLoaiPhong
GROUP BY ct.maPhong, p.tenPhong, lp.tenLoaiPhong;
GO

-- Dịch vụ được gọi nhiều nhất
CREATE VIEW VW_DichVuSuDungNhieuNhat AS
SELECT
    dv.maDichVu,
    dv.tenDichVu,
    ldv.tenLoaiDichVu,
    SUM(ct.soLuong)                    AS TongSoLuong,
    COUNT(DISTINCT ct.maPhieuDatPhong) AS SoLanSuDung
FROM ChiTietDichVu ct
JOIN DichVu    dv  ON ct.maDichVu     = dv.maDichVu
JOIN LoaiDichVu ldv ON dv.maLoaiDichVu = ldv.maLoaiDichVu
GROUP BY dv.maDichVu, dv.tenDichVu, ldv.tenLoaiDichVu;
GO

-- ============================================================
-- PHẦN 4: STORED PROCEDURES
-- ============================================================

-- SP1: Tạo phiếu đặt phòng
CREATE PROCEDURE SP_TaoPhieuDatPhong
    @maPhieuDatPhong    NVARCHAR(10),
    @maKhachHang        NVARCHAR(10),
    @maNhanVien         NVARCHAR(10),
    @soNguoi            INT,
    @maPhong            NVARCHAR(10),
    @thoiGianNhanDuKien DATETIME,
    @thoiGianTraDuKien  DATETIME,
    @donGia             DECIMAL(18,2),
    @hinhThucDat        NVARCHAR(20)  = N'TrucTiep',
    @ghiChu             NVARCHAR(500) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    BEGIN TRY
        IF NOT EXISTS (SELECT 1 FROM Phong WHERE maPhong = @maPhong AND trangThai = N'PhongTrong')
        BEGIN
            RAISERROR(N'Phòng %s hiện không trống.', 16, 1, @maPhong);
            ROLLBACK; RETURN;
        END

        IF EXISTS (
            SELECT 1 FROM ChiTietPhieuDatPhong ct
            JOIN PhieuDatPhong p ON ct.maPhieuDatPhong = p.maPhieuDatPhong
            WHERE ct.maPhong = @maPhong
              AND p.trangThai NOT IN (N'DaCheckOut', N'DaHuy')
              AND p.thoiGianNhanDuKien < @thoiGianTraDuKien
              AND p.thoiGianTraDuKien  > @thoiGianNhanDuKien
        )
        BEGIN
            RAISERROR(N'Phòng %s đã có lịch đặt trùng thời gian.', 16, 1, @maPhong);
            ROLLBACK; RETURN;
        END

        INSERT INTO PhieuDatPhong
            (maPhieuDatPhong, ngayDat, maKhachHang, maNhanVien, soNguoi,
             thoiGianNhanDuKien, thoiGianTraDuKien, hinhThucDat, trangThai, ghiChu)
        VALUES
            (@maPhieuDatPhong, GETDATE(), @maKhachHang, @maNhanVien, @soNguoi,
             @thoiGianNhanDuKien, @thoiGianTraDuKien, @hinhThucDat, N'DaDat', @ghiChu);

        INSERT INTO ChiTietPhieuDatPhong (maPhieuDatPhong, maPhong, donGia)
        VALUES (@maPhieuDatPhong, @maPhong, @donGia);

        COMMIT;
    END TRY
    BEGIN CATCH ROLLBACK; THROW; END CATCH
END;
GO

-- SP2: Check-in
CREATE PROCEDURE SP_CheckIn
    @maPhieuDatPhong NVARCHAR(10)
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    BEGIN TRY
        IF NOT EXISTS (
            SELECT 1 FROM PhieuDatPhong
            WHERE maPhieuDatPhong = @maPhieuDatPhong AND trangThai = N'DaDat'
        )
        BEGIN
            RAISERROR(N'Phiếu không tồn tại hoặc đã được xử lý.', 16, 1);
            ROLLBACK; RETURN;
        END

        UPDATE PhieuDatPhong
        SET thoiGianNhanThucTe = GETDATE(), trangThai = N'DaCheckIn'
        WHERE maPhieuDatPhong = @maPhieuDatPhong;

        UPDATE Phong SET trangThai = N'PhongDat'
        WHERE maPhong IN (
            SELECT maPhong FROM ChiTietPhieuDatPhong
            WHERE maPhieuDatPhong = @maPhieuDatPhong
        );

        COMMIT;
    END TRY
    BEGIN CATCH ROLLBACK; THROW; END CATCH
END;
GO

-- SP3: Check-out và tạo hóa đơn
CREATE PROCEDURE SP_CheckOut
    @maPhieuDatPhong        NVARCHAR(10),
    @maHoaDon               NVARCHAR(10),
    @maNhanVien             NVARCHAR(10),
    @maKhuyenMai            NVARCHAR(10)  = NULL,
    @phuongThucThanhToan    NVARCHAR(15)
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    BEGIN TRY
        DECLARE @tienPhong      DECIMAL(18,2);
        DECLARE @tienDichVu     DECIMAL(18,2);
        DECLARE @tienKhuyenMai  DECIMAL(18,2) = 0;
        DECLARE @phanTramGiam   DECIMAL(5,2)  = 0;

        SELECT @tienPhong = ISNULL(
            DATEDIFF(DAY,
                ISNULL(p.thoiGianNhanThucTe, p.thoiGianNhanDuKien),
                GETDATE()) * ct.donGia, 0)
        FROM PhieuDatPhong p
        JOIN ChiTietPhieuDatPhong ct ON p.maPhieuDatPhong = ct.maPhieuDatPhong
        WHERE p.maPhieuDatPhong = @maPhieuDatPhong;

        SELECT @tienDichVu = ISNULL(SUM(ctdv.soLuong * dv.donGia), 0)
        FROM ChiTietDichVu ctdv
        JOIN DichVu dv ON ctdv.maDichVu = dv.maDichVu
        WHERE ctdv.maPhieuDatPhong = @maPhieuDatPhong;

        IF @maKhuyenMai IS NOT NULL
        BEGIN
            SELECT @phanTramGiam = ISNULL(phanTramGiam, 0)
            FROM KhuyenMai
            WHERE maKhuyenMai = @maKhuyenMai
              AND CAST(GETDATE() AS DATE) BETWEEN ngayApDung AND ngayKetThuc;
            SET @tienKhuyenMai = (@tienPhong + @tienDichVu) * @phanTramGiam / 100;
        END

        DECLARE @tongTien DECIMAL(18,2) = @tienPhong + @tienDichVu - @tienKhuyenMai;

        INSERT INTO HoaDon
            (maHoaDon, ngayLap, maNhanVienLap, maPhieuDatPhong,
             ngayThanhToan, tienKhuyenMai, tienThanhToan, phuongThucThanhToan)
        VALUES
            (@maHoaDon, GETDATE(), @maNhanVien, @maPhieuDatPhong,
             GETDATE(), @tienKhuyenMai, @tongTien, @phuongThucThanhToan);

        UPDATE Phong SET trangThai = N'PhongCanDon'
        WHERE maPhong IN (
            SELECT maPhong FROM ChiTietPhieuDatPhong
            WHERE maPhieuDatPhong = @maPhieuDatPhong
        );

        UPDATE PhieuDatPhong
        SET thoiGianTraThucTe = GETDATE(), trangThai = N'DaCheckOut'
        WHERE maPhieuDatPhong = @maPhieuDatPhong;

        COMMIT;
    END TRY
    BEGIN CATCH ROLLBACK; THROW; END CATCH
END;
GO

-- SP4: Đổi phòng
CREATE PROCEDURE SP_DoiPhong
    @maPhieuDatPhong NVARCHAR(10),
    @maPhongMoi      NVARCHAR(10),
    @donGiaMoi       DECIMAL(18,2)
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    BEGIN TRY
        DECLARE @maPhongCu NVARCHAR(10);
        SELECT @maPhongCu = maPhong FROM ChiTietPhieuDatPhong
        WHERE maPhieuDatPhong = @maPhieuDatPhong;

        IF NOT EXISTS (SELECT 1 FROM Phong WHERE maPhong = @maPhongMoi AND trangThai = N'PhongTrong')
        BEGIN
            RAISERROR(N'Phòng mới không trống.', 16, 1);
            ROLLBACK; RETURN;
        END

        DECLARE @thoiGianNhan DATETIME, @thoiGianTra DATETIME;
        SELECT @thoiGianNhan = thoiGianNhanDuKien, @thoiGianTra = thoiGianTraDuKien
        FROM PhieuDatPhong WHERE maPhieuDatPhong = @maPhieuDatPhong;

        IF EXISTS (
            SELECT 1 FROM ChiTietPhieuDatPhong ct
            JOIN PhieuDatPhong p ON ct.maPhieuDatPhong = p.maPhieuDatPhong
            WHERE ct.maPhong = @maPhongMoi
              AND ct.maPhieuDatPhong <> @maPhieuDatPhong
              AND p.trangThai NOT IN (N'DaCheckOut', N'DaHuy')
              AND p.thoiGianNhanDuKien < @thoiGianTra
              AND p.thoiGianTraDuKien  > @thoiGianNhan
        )
        BEGIN
            RAISERROR(N'Phòng mới đã có lịch đặt trùng thời gian.', 16, 1);
            ROLLBACK; RETURN;
        END

        UPDATE ChiTietPhieuDatPhong
        SET maPhong = @maPhongMoi, donGia = @donGiaMoi
        WHERE maPhieuDatPhong = @maPhieuDatPhong;

        UPDATE Phong SET trangThai = N'PhongCanDon' WHERE maPhong = @maPhongCu;
        UPDATE Phong SET trangThai = N'PhongDat'    WHERE maPhong = @maPhongMoi;

        COMMIT;
    END TRY
    BEGIN CATCH ROLLBACK; THROW; END CATCH
END;
GO

-- ============================================================
-- PHẦN 5: DỮ LIỆU MẪU
-- ============================================================

INSERT INTO LoaiPhong (maLoaiPhong, tenLoaiPhong, soNguoiToiDa, moTa) VALUES
    ('LP01', N'Standard',  2, N'Phòng tiêu chuẩn, đầy đủ tiện nghi cơ bản'),
    ('LP02', N'Deluxe',    2, N'Phòng cao cấp, view đẹp'),
    ('LP03', N'Superior',  3, N'Phòng gia đình, 1 giường đôi + 1 giường đơn'),
    ('LP04', N'Suite',     4, N'Phòng VIP, phòng khách riêng');

INSERT INTO LoaiDichVu (maLoaiDichVu, tenLoaiDichVu) VALUES
    ('LDV01', N'Đồ Ăn'),
    ('LDV02', N'Đồ Uống'),
    ('LDV03', N'Tiện Ích');

INSERT INTO ThietBi (maThietBi, tenThietBi, loaiThietBi, soLuong, donGia, trangThai) VALUES
    ('TB001', N'Tivi Samsung 43 inch', N'Điện tử', 20, 5000000, N'Tot'),
    ('TB002', N'Điều hòa Panasonic 1HP', N'Điện lạnh', 25, 8000000, N'Tot'),
    ('TB003', N'Tủ lạnh Mini', N'Điện lạnh', 20, 2500000, N'Tot'),
    ('TB004', N'Giường đôi', N'Nội thất', 15, 3000000, N'Tot'),
    ('TB005', N'Tủ quần áo gỗ', N'Nội thất', 15, 2000000, N'Tot'),
    ('TB006', N'Máy sấy tóc Panasonic', N'Điện tử', 30, 400000, N'Tot');

INSERT INTO NhanVien
    (maNhanVien, tenNhanVien, soDienThoai, cccd, ngaySinh, ngayBatDauLam, diaChi, email, vaiTro, caLamViec) VALUES
    ('NV001', N'Nguyễn Văn Anh',  '0912345678', '001099012345', '1990-05-15', '2022-01-01', N'Hà Nội', 'anh.nv@lotus.vn',   'QuanLy', N'HanhChinh'),
    ('NV002', N'Trần Thị Bình',   '0923456789', '001099023456', '1995-08-20', '2023-03-01', N'Hà Nội', 'binh.tt@lotus.vn',  'LeTan',  N'Sang'),
    ('NV003', N'Lê Văn Cường',    '0934567890', '001099034567', '1998-12-10', '2024-01-15', N'Hà Nội', 'cuong.lv@lotus.vn', 'LeTan',  N'Chieu');

INSERT INTO KhachHang
    (maKH, hoTenKH, soDienThoai, cmnd, gioiTinh, ngaySinh, diaChi, quocTich) VALUES
    ('KH001', N'Phạm Minh Đức',  '0945678901', '001099045678', 1, '1988-03-22', N'TP.HCM',  N'Việt Nam'),
    ('KH002', N'Hoàng Thị Em',   '0956789012', '001099056789', 0, '1992-07-14', N'Đà Nẵng', N'Việt Nam'),
    ('KH003', N'Vũ Quốc Phong',  '0967890123', '001099067890', 1, '1985-11-30', N'Hà Nội',  N'Việt Nam');

INSERT INTO TaiKhoan (maTaiKhoan, maNhanVien, vaiTro, tenDangNhap, matKhau) VALUES
    ('TK001', 'NV001', 'QuanLy', 'admin',       '123456'),
    ('TK002', 'NV002', 'LeTan',  'letanthu',    '123456'),
    ('TK003', 'NV003', 'LeTan',  'letancuong',  '123456');

INSERT INTO KhuyenMai
    (maKhuyenMai, tenKhuyenMai, phanTramGiam, ngayApDung, ngayKetThuc, dieuKienApDung) VALUES
    ('KM001', N'Khuyến Mãi Hè',   10.00, '2026-06-01', '2026-08-31', N'Áp dụng mùa hè 2026'),
    ('KM002', N'Giảm Cuối Tuần',   5.00, '2026-01-01', '2026-12-31', N'Thứ 7 và Chủ nhật');

INSERT INTO BangGia (maBangGia, maLoaiPhong, loaiThue, donGia, ngayBatDau, ngayKetThuc) VALUES
    ('BG001', 'LP01', N'QuaDem',   500000, '2026-01-01', '2026-12-31'),
    ('BG002', 'LP01', N'TheoGio',   80000, '2026-01-01', '2026-12-31'),
    ('BG003', 'LP02', N'QuaDem',   800000, '2026-01-01', '2026-12-31'),
    ('BG004', 'LP02', N'TheoGio',  120000, '2026-01-01', '2026-12-31'),
    ('BG005', 'LP03', N'QuaDem',  1000000, '2026-01-01', '2026-12-31'),
    ('BG006', 'LP03', N'TheoGio',  150000, '2026-01-01', '2026-12-31'),
    ('BG007', 'LP04', N'QuaDem',  2000000, '2026-01-01', '2026-12-31'),
    ('BG008', 'LP04', N'TheoGio',  300000, '2026-01-01', '2026-12-31');

INSERT INTO Phong (maPhong, tenPhong, maLoaiPhong, trangThai, tienNghi, soNguoiToiDa, moTa) VALUES
    ('P101', N'Phòng 101',      'LP01', N'PhongTrong', N'WiFi,TV',                        2, N'Phòng tiêu chuẩn tầng 1, view sân vườn'),
    ('P102', N'Phòng 102',      'LP01', N'PhongTrong', N'WiFi,TV,Điều Hòa',               2, N'Phòng tiêu chuẩn tầng 1, điều hòa mới'),
    ('P103', N'Phòng 103',      'LP01', N'PhongTrong', N'WiFi,TV,Điều Hòa',               2, N'Phòng tiêu chuẩn tầng 1, hướng Đông'),
    ('P201', N'Phòng 201',      'LP02', N'PhongTrong', N'WiFi,TV,Điều Hòa,Bồn Tắm',      2, N'Phòng Deluxe tầng 2, view thành phố'),
    ('P202', N'Phòng 202',      'LP02', N'PhongTrong', N'WiFi,TV,Điều Hòa,Ban Công',      2, N'Phòng Deluxe tầng 2, ban công rộng'),
    ('P301', N'Phòng 301',      'LP03', N'PhongTrong', N'WiFi,TV,Điều Hòa,Bồn Tắm',      3, N'Phòng Superior tầng 3, phòng gia đình'),
    ('P302', N'Phòng 302',      'LP03', N'PhongTrong', N'WiFi,TV,Điều Hòa,Ban Công',      3, N'Phòng Superior tầng 3, ban công ngắm cảnh'),
    ('P401', N'Suite Hoàng Gia','LP04', N'PhongTrong', N'WiFi,TV,Điều Hòa,Bồn Tắm,Ban Công,Mini Bar', 4, N'Phòng Suite VIP, phòng khách riêng, view toàn cảnh');

INSERT INTO DichVu (maDichVu, tenDichVu, maLoaiDichVu, donGia, trangThai) VALUES
    ('DV001', N'Nước suối Lavie 500ml',  'LDV02',  15000, N'DangKinhDoanh'),
    ('DV002', N'Cơm chiên dương châu',   'LDV01',  65000, N'DangKinhDoanh'),
    ('DV003', N'Giặt ủi (1 bộ)',         'LDV03',  50000, N'DangKinhDoanh'),
    ('DV004', N'Cà phê sữa đá',          'LDV02',  35000, N'DangKinhDoanh'),
    ('DV005', N'Phở bò tái',             'LDV01',  75000, N'DangKinhDoanh'),
    ('DV006', N'Thuê xe đạp (ngày)',      'LDV03',  80000, N'DangKinhDoanh'),
    ('DV007', N'Bia Tiger 330ml',         'LDV02',  30000, N'DangKinhDoanh');

INSERT INTO PhuThu (maPhuThu, tenPhuThu, loaiPhuThu, donGia, ghiChu) VALUES
    ('PT001', N'Phá hỏng đồ đạc',      'HuHongDoDac', 500000, N'Theo đánh giá thiệt hại'),
    ('PT002', N'Trả phòng muộn',        'RaMuon',       200000, N'Sau 12h trưa'),
    ('PT003', N'Nhận phòng sớm',        'NhanMuon',     150000, N'Trước 14h');

GO

USE [master]
GO
