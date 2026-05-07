-- ============================================================
-- LOTUS LAVERNE HOTEL MANAGEMENT SYSTEM
-- Database Schema v3.0 (merged + full seed data)
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

-- 2. Loại Dịch Vụ
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

-- 3. Nhân Viên
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

-- 4. Khách Hàng
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

-- 7. Phòng
CREATE TABLE Phong (
    maPhong      NVARCHAR(10)   NOT NULL,
    tenPhong     NVARCHAR(50)   NOT NULL,
    maLoaiPhong  NVARCHAR(10)   NOT NULL,
    trangThai    NVARCHAR(20)   NOT NULL DEFAULT N'PhongTrong',
    tienNghi     NVARCHAR(500)  NULL,
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
    kyGia       NVARCHAR(15)   NOT NULL DEFAULT N'NgayThuong',
    donGia      DECIMAL(18,2)  NOT NULL,
    ngayBatDau  DATE           NOT NULL,
    ngayKetThuc DATE           NOT NULL,
    CONSTRAINT PK_BangGia           PRIMARY KEY (maBangGia),
    CONSTRAINT FK_BangGia_LoaiPhong FOREIGN KEY (maLoaiPhong) REFERENCES LoaiPhong (maLoaiPhong),
    CONSTRAINT CHK_BangGia_LoaiThue CHECK (loaiThue IN (N'QuaDem', N'TheoNgay', N'TheoGio')),
    CONSTRAINT CHK_BangGia_KyGia   CHECK (kyGia IN (N'NgayThuong', N'CuoiTuan', N'LeTet', N'CaoDiem')),
    CONSTRAINT CHK_BangGia_DonGia   CHECK (donGia > 0),
    CONSTRAINT CHK_BangGia_Ngay     CHECK (ngayKetThuc > ngayBatDau)
);
GO

-- 9. Dịch Vụ
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

-- 10. Phiếu Đặt Phòng
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

-- 11. Chi Tiết Phiếu Đặt Phòng
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

-- 12. Chi Tiết Dịch Vụ
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

-- 13. Phụ Thu
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
    AND bg.kyGia = N'NgayThuong'
    AND CAST(GETDATE() AS DATE) BETWEEN bg.ngayBatDau AND bg.ngayKetThuc;
GO

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

-- ── Loại phòng & dịch vụ ──────────────────────────────────
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
    ('TB001', N'Tivi Samsung 43 inch',      N'Điện tử',  20, 5000000, N'Tot'),
    ('TB002', N'Điều hòa Panasonic 1HP',    N'Điện lạnh',25, 8000000, N'Tot'),
    ('TB003', N'Tủ lạnh Mini',              N'Điện lạnh',20, 2500000, N'Tot'),
    ('TB004', N'Giường đôi',                N'Nội thất', 15, 3000000, N'Tot'),
    ('TB005', N'Tủ quần áo gỗ',             N'Nội thất', 15, 2000000, N'Tot'),
    ('TB006', N'Máy sấy tóc Panasonic',     N'Điện tử',  30,  400000, N'Tot');

-- ── Nhân viên ─────────────────────────────────────────────
INSERT INTO NhanVien
    (maNhanVien, tenNhanVien, soDienThoai, cccd, ngaySinh, ngayBatDauLam, diaChi, email, vaiTro, caLamViec) VALUES
    ('NV001', N'Nguyễn Văn Anh',  '0912345678', '001099012345', '1990-05-15', '2022-01-01', N'Hà Nội', 'anh.nv@lotus.vn',   'QuanLy', N'HanhChinh'),
    ('NV002', N'Trần Thị Bình',   '0923456789', '001099023456', '1995-08-20', '2023-03-01', N'Hà Nội', 'binh.tt@lotus.vn',  'LeTan',  N'Sang'),
    ('NV003', N'Lê Văn Cường',    '0934567890', '001099034567', '1998-12-10', '2024-01-15', N'Hà Nội', 'cuong.lv@lotus.vn', 'LeTan',  N'Chieu');

-- ── Khách hàng (14 khách) ─────────────────────────────────
INSERT INTO KhachHang
    (maKH, hoTenKH, soDienThoai, cmnd, gioiTinh, ngaySinh, diaChi, quocTich) VALUES
    -- Khách gốc
    ('KH001', N'Phạm Minh Đức',     '0945678901', '001099045678', 1, '1988-03-22', N'TP.HCM',       N'Việt Nam'),
    ('KH002', N'Hoàng Thị Em',      '0956789012', '001099056789', 0, '1992-07-14', N'Đà Nẵng',      N'Việt Nam'),
    ('KH003', N'Vũ Quốc Phong',     '0967890123', '001099067890', 1, '1985-11-30', N'Hà Nội',       N'Việt Nam'),
    -- Khách đang ở (DaCheckIn)
    ('KH004', N'Nguyễn Thị Lan',    '0978901234', '001099078901', 0, '1990-04-12', N'Hà Nội',       N'Việt Nam'),
    ('KH005', N'Đặng Văn Minh',     '0989012345', '001099089012', 1, '1987-09-08', N'TP.HCM',       N'Việt Nam'),
    ('KH006', N'Bùi Thị Ngọc',      '0990123456', '001099090123', 0, '1993-02-28', N'Đà Lạt',       N'Việt Nam'),
    ('KH007', N'Trịnh Quang Hải',   '0901234567', '001099001234', 1, '1982-06-15', N'Hải Phòng',    N'Việt Nam'),
    ('KH008', N'Ngô Thị Phương',    '0911234560', '001099011234', 0, '1996-10-05', N'Cần Thơ',      N'Việt Nam'),
    ('KH009', N'Lý Văn Quân',       '0922345678', '001099022345', 1, '1991-01-25', N'Huế',          N'Việt Nam'),
    -- Khách chờ check-in (DaDat)
    ('KH010', N'Đinh Thị Rộng',     '0933456789', '001099033456', 0, '1989-07-20', N'Nha Trang',    N'Việt Nam'),
    ('KH011', N'Phan Văn Sơn',      '0944567890', '001099044567', 1, '1984-12-03', N'Vũng Tàu',     N'Việt Nam'),
    -- Khách đã trả phòng (DaCheckOut - lịch sử)
    ('KH012', N'Mai Thị Tuyết',     '0955678901', '001099055678', 0, '1997-03-17', N'Hội An',       N'Việt Nam'),
    ('KH013', N'Cao Văn Uy',        '0966789012', '001099066789', 1, '1980-08-09', N'Hà Nội',       N'Việt Nam'),
    ('KH014', N'Sophie Laurent',    '0977890123', 'FR1234567890', 0, '1988-11-22', N'Paris, Pháp',  N'Pháp');

-- ── Tài khoản ─────────────────────────────────────────────
INSERT INTO TaiKhoan (maTaiKhoan, maNhanVien, vaiTro, tenDangNhap, matKhau) VALUES
    ('TK001', 'NV001', 'QuanLy', 'admin',      '123456'),
    ('TK002', 'NV002', 'LeTan',  'letanthu',   '123456'),
    ('TK003', 'NV003', 'LeTan',  'letancuong', '123456');

-- ── Khuyến mãi ────────────────────────────────────────────
INSERT INTO KhuyenMai
    (maKhuyenMai, tenKhuyenMai, phanTramGiam, ngayApDung, ngayKetThuc, dieuKienApDung) VALUES
    ('KM001', N'Khuyến Mãi Hè',  10.00, '2026-06-01', '2026-08-31', N'Áp dụng mùa hè 2026'),
    ('KM002', N'Giảm Cuối Tuần',  5.00, '2026-01-01', '2026-12-31', N'Thứ 7 và Chủ nhật');

-- ── Bảng giá (32 bản ghi) ─────────────────────────────────
INSERT INTO BangGia (maBangGia, maLoaiPhong, loaiThue, kyGia, donGia, ngayBatDau, ngayKetThuc) VALUES
    -- Standard (LP01)
    ('BG001', 'LP01', N'QuaDem',  N'NgayThuong',  500000, '2026-01-01', '2026-12-31'),
    ('BG002', 'LP01', N'TheoGio', N'NgayThuong',   80000, '2026-01-01', '2026-12-31'),
    ('BG003', 'LP01', N'QuaDem',  N'CuoiTuan',    600000, '2026-01-01', '2026-12-31'),
    ('BG004', 'LP01', N'TheoGio', N'CuoiTuan',    100000, '2026-01-01', '2026-12-31'),
    ('BG005', 'LP01', N'QuaDem',  N'LeTet',       750000, '2026-01-01', '2026-12-31'),
    ('BG006', 'LP01', N'TheoGio', N'LeTet',       120000, '2026-01-01', '2026-12-31'),
    ('BG007', 'LP01', N'QuaDem',  N'CaoDiem',     700000, '2026-01-01', '2026-12-31'),
    ('BG008', 'LP01', N'TheoGio', N'CaoDiem',     110000, '2026-01-01', '2026-12-31'),
    -- Deluxe (LP02)
    ('BG009', 'LP02', N'QuaDem',  N'NgayThuong',  800000, '2026-01-01', '2026-12-31'),
    ('BG010', 'LP02', N'TheoGio', N'NgayThuong',  120000, '2026-01-01', '2026-12-31'),
    ('BG011', 'LP02', N'QuaDem',  N'CuoiTuan',    960000, '2026-01-01', '2026-12-31'),
    ('BG012', 'LP02', N'TheoGio', N'CuoiTuan',    150000, '2026-01-01', '2026-12-31'),
    ('BG013', 'LP02', N'QuaDem',  N'LeTet',      1200000, '2026-01-01', '2026-12-31'),
    ('BG014', 'LP02', N'TheoGio', N'LeTet',       180000, '2026-01-01', '2026-12-31'),
    ('BG015', 'LP02', N'QuaDem',  N'CaoDiem',    1100000, '2026-01-01', '2026-12-31'),
    ('BG016', 'LP02', N'TheoGio', N'CaoDiem',     170000, '2026-01-01', '2026-12-31'),
    -- Superior (LP03)
    ('BG017', 'LP03', N'QuaDem',  N'NgayThuong', 1000000, '2026-01-01', '2026-12-31'),
    ('BG018', 'LP03', N'TheoGio', N'NgayThuong',  150000, '2026-01-01', '2026-12-31'),
    ('BG019', 'LP03', N'QuaDem',  N'CuoiTuan',   1200000, '2026-01-01', '2026-12-31'),
    ('BG020', 'LP03', N'TheoGio', N'CuoiTuan',    180000, '2026-01-01', '2026-12-31'),
    ('BG021', 'LP03', N'QuaDem',  N'LeTet',      1500000, '2026-01-01', '2026-12-31'),
    ('BG022', 'LP03', N'TheoGio', N'LeTet',       220000, '2026-01-01', '2026-12-31'),
    ('BG023', 'LP03', N'QuaDem',  N'CaoDiem',    1400000, '2026-01-01', '2026-12-31'),
    ('BG024', 'LP03', N'TheoGio', N'CaoDiem',     200000, '2026-01-01', '2026-12-31'),
    -- Suite (LP04)
    ('BG025', 'LP04', N'QuaDem',  N'NgayThuong', 2000000, '2026-01-01', '2026-12-31'),
    ('BG026', 'LP04', N'TheoGio', N'NgayThuong',  300000, '2026-01-01', '2026-12-31'),
    ('BG027', 'LP04', N'QuaDem',  N'CuoiTuan',   2400000, '2026-01-01', '2026-12-31'),
    ('BG028', 'LP04', N'TheoGio', N'CuoiTuan',    360000, '2026-01-01', '2026-12-31'),
    ('BG029', 'LP04', N'QuaDem',  N'LeTet',      3000000, '2026-01-01', '2026-12-31'),
    ('BG030', 'LP04', N'TheoGio', N'LeTet',       450000, '2026-01-01', '2026-12-31'),
    ('BG031', 'LP04', N'QuaDem',  N'CaoDiem',    2800000, '2026-01-01', '2026-12-31'),
    ('BG032', 'LP04', N'TheoGio', N'CaoDiem',     420000, '2026-01-01', '2026-12-31');

-- ── Phòng (trạng thái phản ánh hiện trạng thực) ───────────
-- P101, P102, P201, P202, P301, P401 đang có khách (DaCheckIn → PhongDat)
-- P103, P302 còn trống
INSERT INTO Phong (maPhong, tenPhong, maLoaiPhong, trangThai, tienNghi, soNguoiToiDa, moTa) VALUES
    ('P101', N'Phòng 101',       'LP01', N'PhongDat',   N'WiFi,TV',                                   2, N'Phòng tiêu chuẩn tầng 1, view sân vườn'),
    ('P102', N'Phòng 102',       'LP01', N'PhongDat',   N'WiFi,TV,Điều Hòa',                          2, N'Phòng tiêu chuẩn tầng 1, điều hòa mới'),
    ('P103', N'Phòng 103',       'LP01', N'PhongTrong', N'WiFi,TV,Điều Hòa',                          2, N'Phòng tiêu chuẩn tầng 1, hướng Đông'),
    ('P201', N'Phòng 201',       'LP02', N'PhongDat',   N'WiFi,TV,Điều Hòa,Bồn Tắm',                 2, N'Phòng Deluxe tầng 2, view thành phố'),
    ('P202', N'Phòng 202',       'LP02', N'PhongDat',   N'WiFi,TV,Điều Hòa,Ban Công',                 2, N'Phòng Deluxe tầng 2, ban công rộng'),
    ('P301', N'Phòng 301',       'LP03', N'PhongDat',   N'WiFi,TV,Điều Hòa,Bồn Tắm',                 3, N'Phòng Superior tầng 3, phòng gia đình'),
    ('P302', N'Phòng 302',       'LP03', N'PhongTrong', N'WiFi,TV,Điều Hòa,Ban Công',                 3, N'Phòng Superior tầng 3, ban công ngắm cảnh'),
    ('P401', N'Suite Hoàng Gia', 'LP04', N'PhongDat',   N'WiFi,TV,Điều Hòa,Bồn Tắm,Ban Công,Mini Bar', 4, N'Phòng Suite VIP, phòng khách riêng, view toàn cảnh');

-- ── Dịch vụ & phụ thu ─────────────────────────────────────
INSERT INTO DichVu (maDichVu, tenDichVu, maLoaiDichVu, donGia, trangThai) VALUES
    ('DV001', N'Nước suối Lavie 500ml',  'LDV02',  15000, N'DangKinhDoanh'),
    ('DV002', N'Cơm chiên dương châu',   'LDV01',  65000, N'DangKinhDoanh'),
    ('DV003', N'Giặt ủi (1 bộ)',         'LDV03',  50000, N'DangKinhDoanh'),
    ('DV004', N'Cà phê sữa đá',          'LDV02',  35000, N'DangKinhDoanh'),
    ('DV005', N'Phở bò tái',             'LDV01',  75000, N'DangKinhDoanh'),
    ('DV006', N'Thuê xe đạp (ngày)',      'LDV03',  80000, N'DangKinhDoanh'),
    ('DV007', N'Bia Tiger 330ml',         'LDV02',  30000, N'DangKinhDoanh');

INSERT INTO PhuThu (maPhuThu, tenPhuThu, loaiPhuThu, donGia, ghiChu) VALUES
    ('PT001', N'Phá hỏng đồ đạc', 'HuHongDoDac', 500000, N'Theo đánh giá thiệt hại'),
    ('PT002', N'Trả phòng muộn',  'RaMuon',       200000, N'Sau 12h trưa'),
    ('PT003', N'Nhận phòng sớm',  'NhanMuon',     150000, N'Trước 14h');

-- ============================================================
-- PHẦN 6: PHIẾU ĐẶT PHÒNG
-- ============================================================

-- ── Khách đã check-in (đang lưu trú) ─────────────────────
INSERT INTO PhieuDatPhong
    (maPhieuDatPhong, ngayDat, maKhachHang, maNhanVien, soNguoi,
     thoiGianNhanDuKien, thoiGianNhanThucTe, thoiGianTraDuKien,
     hinhThucDat, trangThai, ghiChu)
VALUES
    -- PDP001: Nguyễn Thị Lan – P101 Standard (5 đêm, vào 05/05)
    ('PDP001', '2026-05-03', 'KH004', 'NV002', 1,
     '2026-05-05 14:00', '2026-05-05 14:23', '2026-05-10 12:00',
     N'TrucTiep', N'DaCheckIn', N'Yêu cầu tầng cao'),
    -- PDP002: Đặng Văn Minh – P201 Deluxe (5 đêm, vào 04/05)
    ('PDP002', '2026-05-02', 'KH005', 'NV002', 2,
     '2026-05-04 15:00', '2026-05-04 15:18', '2026-05-09 12:00',
     N'QuaWeb', N'DaCheckIn', N'Phòng nhìn ra thành phố'),
    -- PDP003: Bùi Thị Ngọc – P202 Deluxe (5 đêm, vào 06/05)
    ('PDP003', '2026-05-05', 'KH006', 'NV003', 1,
     '2026-05-06 14:00', '2026-05-06 14:35', '2026-05-11 12:00',
     N'TrucTiep', N'DaCheckIn', NULL),
    -- PDP004: Trịnh Quang Hải – P301 Superior (5 đêm, vào 03/05, trả phòng ngày mai)
    ('PDP004', '2026-05-01', 'KH007', 'NV002', 3,
     '2026-05-03 16:00', '2026-05-03 16:05', '2026-05-08 12:00',
     N'QuaDienThoai', N'DaCheckIn', N'Thêm 1 giường phụ'),
    -- PDP005: Ngô Thị Phương – P401 Suite (6 đêm, vào 06/05)
    ('PDP005', '2026-05-04', 'KH008', 'NV002', 2,
     '2026-05-06 13:00', '2026-05-06 13:42', '2026-05-12 12:00',
     N'QuaWeb', N'DaCheckIn', N'Tuần trăng mật, chuẩn bị hoa & rượu'),
    -- PDP006: Lý Văn Quân – P102 Standard (2 đêm, vào hôm nay 07/05)
    ('PDP006', '2026-05-06', 'KH009', 'NV003', 1,
     '2026-05-07 10:00', '2026-05-07 10:15', '2026-05-09 12:00',
     N'TrucTiep', N'DaCheckIn', NULL);

-- ── Khách chờ check-in ────────────────────────────────────
INSERT INTO PhieuDatPhong
    (maPhieuDatPhong, ngayDat, maKhachHang, maNhanVien, soNguoi,
     thoiGianNhanDuKien, thoiGianTraDuKien,
     hinhThucDat, trangThai, ghiChu)
VALUES
    -- PDP007: Đinh Thị Rộng – P103 Standard (ngày mai 08/05)
    ('PDP007', '2026-05-06', 'KH010', 'NV002', 1,
     '2026-05-08 14:00', '2026-05-12 12:00',
     N'QuaWeb', N'DaDat', NULL),
    -- PDP008: Phan Văn Sơn – P302 Superior (09/05)
    ('PDP008', '2026-05-07', 'KH011', 'NV003', 2,
     '2026-05-09 14:00', '2026-05-14 12:00',
     N'QuaDienThoai', N'DaDat', N'Đề nghị giường phụ');

-- ── Lịch sử đã trả phòng ─────────────────────────────────
INSERT INTO PhieuDatPhong
    (maPhieuDatPhong, ngayDat, maKhachHang, maNhanVien, soNguoi,
     thoiGianNhanDuKien, thoiGianNhanThucTe, thoiGianTraDuKien, thoiGianTraThucTe,
     hinhThucDat, trangThai, ghiChu)
VALUES
    -- PDP009: Mai Thị Tuyết – P101 Standard (01/05 → 05/05, 4 đêm)
    ('PDP009', '2026-04-29', 'KH012', 'NV002', 1,
     '2026-05-01 14:00', '2026-05-01 14:10', '2026-05-05 12:00', '2026-05-05 11:45',
     N'TrucTiep', N'DaCheckOut', NULL),
    -- PDP010: Cao Văn Uy – P201 Deluxe (28/04 → 02/05, 4 đêm)
    ('PDP010', '2026-04-25', 'KH013', 'NV002', 2,
     '2026-04-28 15:00', '2026-04-28 15:20', '2026-05-02 12:00', '2026-05-02 10:30',
     N'QuaWeb', N'DaCheckOut', N'Khách doanh nhân'),
    -- PDP011: Sophie Laurent – P401 Suite (25/04 → 01/05, 6 đêm)
    ('PDP011', '2026-04-20', 'KH014', 'NV001', 2,
     '2026-04-25 13:00', '2026-04-25 13:55', '2026-05-01 12:00', '2026-05-01 12:00',
     N'QuaWeb', N'DaCheckOut', N'Khách nước ngoài, thanh toán thẻ');

-- ============================================================
-- PHẦN 7: CHI TIẾT PHIẾU ĐẶT PHÒNG
-- ============================================================

INSERT INTO ChiTietPhieuDatPhong (maPhieuDatPhong, maPhong, donGia) VALUES
    ('PDP001', 'P101',  500000),   -- Standard NgayThuong
    ('PDP002', 'P201',  800000),   -- Deluxe NgayThuong
    ('PDP003', 'P202',  800000),   -- Deluxe NgayThuong
    ('PDP004', 'P301', 1000000),   -- Superior NgayThuong
    ('PDP005', 'P401', 2000000),   -- Suite NgayThuong
    ('PDP006', 'P102',  500000),   -- Standard NgayThuong
    ('PDP007', 'P103',  500000),   -- Standard NgayThuong
    ('PDP008', 'P302', 1000000),   -- Superior NgayThuong
    ('PDP009', 'P101',  500000),   -- Standard NgayThuong
    ('PDP010', 'P201',  800000),   -- Deluxe NgayThuong
    ('PDP011', 'P401', 2000000);   -- Suite NgayThuong

-- ============================================================
-- PHẦN 8: CHI TIẾT DỊCH VỤ
-- ============================================================

-- Dịch vụ của khách đang ở
INSERT INTO ChiTietDichVu (maDichVu, maPhieuDatPhong, soLuong, thoiDiemSuDung) VALUES
    -- PDP001 - Nguyễn Thị Lan
    ('DV001', 'PDP001', 4, '2026-05-06 08:00'),   -- nước suối x4
    ('DV004', 'PDP001', 2, '2026-05-07 07:30'),   -- cà phê x2
    -- PDP002 - Đặng Văn Minh
    ('DV002', 'PDP002', 2, '2026-05-05 12:30'),   -- cơm chiên x2
    ('DV001', 'PDP002', 6, '2026-05-06 09:00'),   -- nước suối x6
    ('DV003', 'PDP002', 1, '2026-05-06 10:00'),   -- giặt ủi x1
    -- PDP003 - Bùi Thị Ngọc
    ('DV007', 'PDP003', 4, '2026-05-06 20:00'),   -- bia x4
    ('DV005', 'PDP003', 2, '2026-05-07 12:00'),   -- phở x2
    -- PDP004 - Trịnh Quang Hải (trả phòng ngày mai → nhiều dịch vụ)
    ('DV001', 'PDP004', 8, '2026-05-04 08:00'),   -- nước suối x8
    ('DV002', 'PDP004', 4, '2026-05-05 12:00'),   -- cơm chiên x4
    ('DV006', 'PDP004', 2, '2026-05-06 09:00'),   -- thuê xe đạp x2
    ('DV003', 'PDP004', 2, '2026-05-07 10:00'),   -- giặt ủi x2
    -- PDP005 - Ngô Thị Phương (Suite – tuần trăng mật)
    ('DV004', 'PDP005', 4, '2026-05-07 07:00'),   -- cà phê x4
    ('DV005', 'PDP005', 2, '2026-05-07 12:00'),   -- phở x2
    ('DV003', 'PDP005', 2, '2026-05-07 11:00'),   -- giặt ủi x2
    ('DV007', 'PDP005', 6, '2026-05-06 21:00');   -- bia x6

-- Dịch vụ của khách đã trả phòng (cho lịch sử hóa đơn)
INSERT INTO ChiTietDichVu (maDichVu, maPhieuDatPhong, soLuong, thoiDiemSuDung) VALUES
    -- PDP010 - Cao Văn Uy (Deluxe)
    ('DV001', 'PDP010', 2, '2026-04-29 08:00'),   -- nước suối x2
    ('DV004', 'PDP010', 3, '2026-04-30 07:30'),   -- cà phê x3
    -- PDP011 - Sophie Laurent (Suite)
    ('DV003', 'PDP011', 3, '2026-04-27 10:00'),   -- giặt ủi x3
    ('DV007', 'PDP011', 8, '2026-04-28 21:00'),   -- bia x8
    ('DV004', 'PDP011', 5, '2026-04-29 07:00');   -- cà phê x5

-- ============================================================
-- PHẦN 9: HÓA ĐƠN (khách đã trả phòng)
-- ============================================================
-- HD001: PDP009 – Mai Thị Tuyết, 4 đêm Standard = 2.000.000, đặt cọc 500k → thanh toán 1.500.000
INSERT INTO HoaDon
    (maHoaDon, ngayLap, maNhanVienLap, maPhieuDatPhong,
     ngayThanhToan, tienKhuyenMai, tienThanhToan, phuongThucThanhToan, ghiChu)
VALUES
    ('HD001', '2026-05-05 11:45', 'NV002', 'PDP009',
     '2026-05-05 11:45', 0, 1500000, N'TienMat', NULL);

-- HD002: PDP010 – Cao Văn Uy, 4 đêm Deluxe = 3.200.000
--        DV: nước x2 (30k) + cà phê x3 (105k) = 135.000
--        Tổng: 3.335.000 – đặt cọc 800k → thanh toán 2.535.000
INSERT INTO HoaDon
    (maHoaDon, ngayLap, maNhanVienLap, maPhieuDatPhong,
     ngayThanhToan, tienKhuyenMai, tienThanhToan, phuongThucThanhToan, ghiChu)
VALUES
    ('HD002', '2026-05-02 10:30', 'NV002', 'PDP010',
     '2026-05-02 10:30', 0, 2535000, N'ChuyenKhoan', N'Khách doanh nhân, xuất hóa đơn VAT');

-- HD003: PDP011 – Sophie Laurent, 6 đêm Suite = 12.000.000
--        DV: giặt x3 (150k) + bia x8 (240k) + cà phê x5 (175k) = 565.000
--        Tổng: 12.565.000 – đặt cọc 2.000k → thanh toán 10.565.000
INSERT INTO HoaDon
    (maHoaDon, ngayLap, maNhanVienLap, maPhieuDatPhong,
     ngayThanhToan, tienKhuyenMai, tienThanhToan, phuongThucThanhToan, ghiChu)
VALUES
    ('HD003', '2026-05-01 12:00', 'NV001', 'PDP011',
     '2026-05-01 12:00', 0, 10565000, N'ChuyenKhoan', N'Khách nước ngoài');

-- ============================================================
-- PHẦN 10: CHI TIẾT HÓA ĐƠN
-- ============================================================

-- HD001 – PDP009 (Standard 4 đêm)
INSERT INTO ChiTietHoaDon (maHoaDon, loaiTien, moTa, donGia, soLuong, thanhTien) VALUES
    ('HD001', N'TienPhong', N'Phòng 101 – Standard – 4 đêm', 500000, 4, 2000000),
    ('HD001', N'TienCoc',   N'Trừ đặt cọc',                 -500000, 1, -500000);

-- HD002 – PDP010 (Deluxe 4 đêm + dịch vụ)
INSERT INTO ChiTietHoaDon (maHoaDon, loaiTien, moTa, donGia, soLuong, thanhTien) VALUES
    ('HD002', N'TienPhong',  N'Phòng 201 – Deluxe – 4 đêm',  800000, 4, 3200000),
    ('HD002', N'TienDichVu', N'Nước suối x2',                  15000, 2,   30000),
    ('HD002', N'TienDichVu', N'Cà phê sữa đá x3',              35000, 3,  105000),
    ('HD002', N'TienCoc',    N'Trừ đặt cọc',                 -800000, 1, -800000);

-- HD003 – PDP011 (Suite 6 đêm + dịch vụ)
INSERT INTO ChiTietHoaDon (maHoaDon, loaiTien, moTa, donGia, soLuong, thanhTien) VALUES
    ('HD003', N'TienPhong',  N'Suite Hoàng Gia – 6 đêm',    2000000, 6, 12000000),
    ('HD003', N'TienDichVu', N'Giặt ủi x3',                   50000, 3,   150000),
    ('HD003', N'TienDichVu', N'Bia Tiger x8',                  30000, 8,   240000),
    ('HD003', N'TienDichVu', N'Cà phê sữa đá x5',             35000, 5,   175000),
    ('HD003', N'TienCoc',    N'Trừ đặt cọc',                -2000000, 1, -2000000);

-- ============================================================
-- PHẦN 11: PHIẾU THU (đặt cọc)
-- ============================================================

-- Đặt cọc cho khách đang ở (maHoaDon còn NULL)
INSERT INTO PhieuThu
    (maPhieuThu, maHoaDon, maNhanVienLap, maPhieuDatPhong, soTienCoc, ngayThu, phuongThucThanhToan, ghiChu)
VALUES
    ('PTH001', NULL, 'NV002', 'PDP001',  500000, '2026-05-03 09:00', N'TienMat',     N'Cọc đặt phòng'),
    ('PTH002', NULL, 'NV002', 'PDP002',  800000, '2026-05-02 10:30', N'ChuyenKhoan', N'Cọc đặt phòng'),
    ('PTH003', NULL, 'NV003', 'PDP003',  800000, '2026-05-05 14:00', N'TienMat',     N'Cọc đặt phòng'),
    ('PTH004', NULL, 'NV002', 'PDP004', 1000000, '2026-05-01 11:00', N'TienMat',     N'Cọc đặt phòng'),
    ('PTH005', NULL, 'NV002', 'PDP005', 2000000, '2026-05-04 16:00', N'ChuyenKhoan', N'Cọc đặt phòng Suite'),
    ('PTH006', NULL, 'NV003', 'PDP006',  500000, '2026-05-06 15:00', N'TienMat',     N'Cọc đặt phòng');

-- Đặt cọc cho khách đã trả phòng (liên kết với hóa đơn)
INSERT INTO PhieuThu
    (maPhieuThu, maHoaDon, maNhanVienLap, maPhieuDatPhong, soTienCoc, ngayThu, phuongThucThanhToan, ghiChu)
VALUES
    ('PTH007', 'HD001', 'NV002', 'PDP009',  500000, '2026-04-29 09:00', N'TienMat',     N'Cọc đặt phòng'),
    ('PTH008', 'HD002', 'NV002', 'PDP010',  800000, '2026-04-25 11:00', N'ChuyenKhoan', N'Cọc đặt phòng'),
    ('PTH009', 'HD003', 'NV001', 'PDP011', 2000000, '2026-04-20 14:00', N'ChuyenKhoan', N'Cọc đặt phòng Suite');

GO

USE [master]
GO
