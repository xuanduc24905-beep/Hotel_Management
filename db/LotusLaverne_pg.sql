-- PostgreSQL schema cho LotusLaverne Hotel Management
-- Chạy bằng psql: psql -U postgres -d quanlykikhachsan -f LotusLaverne_pg.sql

-- ============================================================
-- PHẦN 1: TABLES
-- ============================================================

CREATE TABLE LoaiPhong (
    maLoaiPhong  varchar(10)  NOT NULL,
    tenLoaiPhong varchar(100) NOT NULL,
    CONSTRAINT PK_LoaiPhong PRIMARY KEY (maLoaiPhong)
);

CREATE TABLE BangGia (
    maBangGia   varchar(10) NOT NULL,
    maLoaiPhong varchar(10) NOT NULL,
    loaiThue    varchar(10) NOT NULL,
    donGia      double precision NOT NULL,
    ngayBatDau  date NOT NULL,
    ngayKetThuc date NOT NULL,
    CONSTRAINT PK_BangGia PRIMARY KEY (maBangGia),
    CONSTRAINT CHK_BangGia_DonGia  CHECK (donGia > 0),
    CONSTRAINT CHK_BangGia_LoaiThue CHECK (loaiThue IN ('QuaDem','TheoNgay','TheoGio')),
    CONSTRAINT CHK_BangGia_Ngay    CHECK (ngayKetThuc > ngayBatDau),
    CONSTRAINT FK_BangGia_LoaiPhong FOREIGN KEY (maLoaiPhong) REFERENCES LoaiPhong(maLoaiPhong)
);

CREATE TABLE Phong (
    maPhong     varchar(10) NOT NULL,
    tenPhong    varchar(50) NOT NULL,
    maLoaiPhong varchar(10) NOT NULL,
    trangThai   varchar(20) NOT NULL DEFAULT 'PhongTrong',
    CONSTRAINT PK_Phong PRIMARY KEY (maPhong),
    CONSTRAINT CHK_Phong_TrangThai CHECK (trangThai IN ('PhongTrong','PhongCanDon','PhongDangSuDung','PhongDat')),
    CONSTRAINT FK_Phong_LoaiPhong  FOREIGN KEY (maLoaiPhong) REFERENCES LoaiPhong(maLoaiPhong)
);

CREATE TABLE KhachHang (
    maKH        varchar(10)  NOT NULL,
    hoTenKH     varchar(100) NOT NULL,
    cccd        varchar(20)  NOT NULL,
    soDienThoai varchar(15)  NOT NULL,
    gioiTinh    boolean      NOT NULL,
    ngaySinh    timestamp    NULL,
    diaChi      varchar(200) NULL,
    quocTich    varchar(50)  NULL DEFAULT 'Việt Nam',
    CONSTRAINT PK_KhachHang  PRIMARY KEY (maKH),
    CONSTRAINT UQ_KhachHang_CCCD UNIQUE (cccd)
);

CREATE TABLE KhuyenMai (
    maKhuyenMai      varchar(10)  NOT NULL,
    tenKhuyenMai     varchar(100) NOT NULL,
    ngayApDung       timestamp    NOT NULL,
    ngayKetThuc      timestamp    NOT NULL,
    phanTramGiam     double precision NOT NULL,
    dieuKienApDung   varchar(255) NULL,
    CONSTRAINT PK_KhuyenMai          PRIMARY KEY (maKhuyenMai),
    CONSTRAINT CHK_KhuyenMai_Ngay    CHECK (ngayKetThuc > ngayApDung),
    CONSTRAINT CHK_KhuyenMai_PhanTram CHECK (phanTramGiam > 0 AND phanTramGiam <= 100)
);

CREATE TABLE NhanVien (
    maNhanVien          varchar(10)  NOT NULL,
    hoTenNhanVien       varchar(100) NOT NULL,
    cccd                varchar(20)  NOT NULL,
    ngaySinh            timestamp    NULL,
    ngayBatDauLam       timestamp    NULL,
    ngayKetThucHopDong  timestamp    NULL,
    soDienThoai         varchar(15)  NULL,
    diaChi              varchar(200) NULL,
    email               varchar(100) NULL,
    vaiTro              varchar(10)  NOT NULL,
    CONSTRAINT PK_NhanVien      PRIMARY KEY (maNhanVien),
    CONSTRAINT UQ_NhanVien_CCCD UNIQUE (cccd),
    CONSTRAINT CHK_NhanVien_VaiTro CHECK (vaiTro IN ('LeTan','QuanLy'))
);

CREATE TABLE TaiKhoan (
    maTaiKhoan  varchar(10)  NOT NULL,
    maNhanVien  varchar(10)  NOT NULL,
    vaiTro      varchar(10)  NOT NULL,
    tenDangNhap varchar(50)  NOT NULL,
    matKhau     varchar(255) NOT NULL,
    CONSTRAINT PK_TaiKhoan             PRIMARY KEY (maTaiKhoan),
    CONSTRAINT UQ_TaiKhoan_NhanVien    UNIQUE (maNhanVien),
    CONSTRAINT UQ_TaiKhoan_TenDangNhap UNIQUE (tenDangNhap),
    CONSTRAINT CHK_TaiKhoan_VaiTro     CHECK (vaiTro IN ('LeTan','QuanLy')),
    CONSTRAINT FK_TaiKhoan_NhanVien    FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien)
);

CREATE TABLE PhieuDatPhong (
    maPhieuDatPhong     varchar(10) NOT NULL,
    ngayDat             timestamp   NOT NULL DEFAULT NOW(),
    maKhachHang         varchar(10) NOT NULL,
    maNhanVien          varchar(10) NOT NULL,
    soNguoi             int         NOT NULL,
    thoiGianNhanDuKien  timestamp   NOT NULL,
    thoiGianNhanThucTe  timestamp   NULL,
    thoiGianTraDuKien   timestamp   NOT NULL,
    thoiGianTraThucTe   timestamp   NULL,
    ghiChu              varchar(500) NULL,
    CONSTRAINT PK_PhieuDatPhong  PRIMARY KEY (maPhieuDatPhong),
    CONSTRAINT CHK_PDP_Ngay      CHECK (thoiGianTraDuKien > thoiGianNhanDuKien),
    CONSTRAINT CHK_PDP_SoNguoi   CHECK (soNguoi > 0),
    CONSTRAINT FK_PDP_KhachHang  FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKH),
    CONSTRAINT FK_PDP_NhanVien   FOREIGN KEY (maNhanVien)  REFERENCES NhanVien(maNhanVien)
);

CREATE TABLE PhuThu (
    maPhuThu    varchar(10)    NOT NULL,
    tenPhuThu   varchar(100)   NOT NULL,
    loaiPhuThu  varchar(15)    NOT NULL,
    donGia      decimal(18,2)  NOT NULL,
    ghiChu      varchar(255)   NULL,
    CONSTRAINT PK_PhuThu       PRIMARY KEY (maPhuThu),
    CONSTRAINT CHK_PhuThu_DonGia CHECK (donGia > 0),
    CONSTRAINT CHK_PhuThu_Loai   CHECK (loaiPhuThu IN ('HuHongDoDac','RaMuon','NhanMuon'))
);

CREATE TABLE ChiTietPhieuDatPhong (
    maPhieuDatPhong varchar(10)   NOT NULL,
    maPhong         varchar(10)   NOT NULL,
    donGia          decimal(18,2) NOT NULL,
    maPhuThu        varchar(10)   NULL,
    CONSTRAINT PK_ChiTietPhieuDatPhong PRIMARY KEY (maPhieuDatPhong, maPhong),
    CONSTRAINT CHK_CTPDP_DonGia        CHECK (donGia > 0),
    CONSTRAINT FK_CTPDP_PhieuDatPhong  FOREIGN KEY (maPhieuDatPhong) REFERENCES PhieuDatPhong(maPhieuDatPhong),
    CONSTRAINT FK_CTPDP_Phong          FOREIGN KEY (maPhong)         REFERENCES Phong(maPhong),
    CONSTRAINT FK_CTPDP_PhuThu         FOREIGN KEY (maPhuThu)        REFERENCES PhuThu(maPhuThu)
);

CREATE TABLE DichVu (
    maDichVu        varchar(10)  NOT NULL,
    tenDichVu       varchar(100) NOT NULL,
    donGia          double precision NOT NULL,
    soLuongTonKho   int          NOT NULL DEFAULT 0,
    loaiDichVu      varchar(10)  NOT NULL,
    CONSTRAINT PK_DichVu      PRIMARY KEY (maDichVu),
    CONSTRAINT CHK_DichVu_DonGia CHECK (donGia > 0),
    CONSTRAINT CHK_DichVu_Loai   CHECK (loaiDichVu IN ('TienIch','DoAn','DoUong'))
);

CREATE TABLE ChiTietDichVu (
    maDichVu        varchar(10)  NOT NULL,
    maPhieuDatPhong varchar(10)  NOT NULL,
    soLuong         int          NOT NULL,
    thoiDiemSuDung  timestamp    NOT NULL DEFAULT NOW(),
    ghiChu          varchar(255) NULL,
    CONSTRAINT PK_ChiTietDichVu  PRIMARY KEY (maDichVu, maPhieuDatPhong),
    CONSTRAINT CHK_CTDV_SoLuong  CHECK (soLuong > 0),
    CONSTRAINT FK_CTDV_DichVu    FOREIGN KEY (maDichVu)        REFERENCES DichVu(maDichVu),
    CONSTRAINT FK_CTDV_PDP       FOREIGN KEY (maPhieuDatPhong) REFERENCES PhieuDatPhong(maPhieuDatPhong)
);

CREATE TABLE HoaDon (
    maHoaDon            varchar(10)   NOT NULL,
    ngayLap             timestamp     NOT NULL DEFAULT NOW(),
    maNhanVienLap       varchar(10)   NOT NULL,
    maPhieuDatPhong     varchar(10)   NOT NULL,
    ngayThanhToan       timestamp     NULL,
    tienKhuyenMai       decimal(18,2) NOT NULL DEFAULT 0,
    tienThanhToan       decimal(18,2) NOT NULL,
    phuongThucThanhToan varchar(15)   NOT NULL,
    ghiChu              varchar(500)  NULL,
    CONSTRAINT PK_HoaDon             PRIMARY KEY (maHoaDon),
    CONSTRAINT CHK_HoaDon_PhuongThuc CHECK (phuongThucThanhToan IN ('ChuyenKhoan','TienMat')),
    CONSTRAINT CHK_HoaDon_TienTT     CHECK (tienThanhToan >= 0),
    CONSTRAINT FK_HoaDon_NhanVien    FOREIGN KEY (maNhanVienLap)   REFERENCES NhanVien(maNhanVien),
    CONSTRAINT FK_HoaDon_PDP         FOREIGN KEY (maPhieuDatPhong) REFERENCES PhieuDatPhong(maPhieuDatPhong)
);

CREATE TABLE ChiTietHoaDon (
    maChiTiet   SERIAL        NOT NULL,
    maHoaDon    varchar(10)   NOT NULL,
    loaiTien    varchar(15)   NOT NULL,
    maKhuyenMai varchar(10)   NULL,
    moTa        varchar(255)  NULL,
    donGia      decimal(18,2) NOT NULL,
    thanhTien   decimal(18,2) NOT NULL,
    soLuong     int           NOT NULL DEFAULT 1,
    CONSTRAINT PK_ChiTietHoaDon  PRIMARY KEY (maChiTiet),
    CONSTRAINT CHK_CTHD_LoaiTien CHECK (loaiTien IN ('TienPhuThu','TienDichVu','TienPhong','TienCoc')),
    CONSTRAINT CHK_CTHD_SoLuong  CHECK (soLuong > 0),
    CONSTRAINT FK_CTHD_HoaDon    FOREIGN KEY (maHoaDon)    REFERENCES HoaDon(maHoaDon),
    CONSTRAINT FK_CTHD_KhuyenMai FOREIGN KEY (maKhuyenMai) REFERENCES KhuyenMai(maKhuyenMai)
);

CREATE TABLE PhieuThu (
    maPhieuThu          varchar(10)   NOT NULL,
    maHoaDon            varchar(10)   NULL,
    maNhanVienLap       varchar(10)   NOT NULL,
    maPhieuDatPhong     varchar(10)   NOT NULL,
    soTienCoc           decimal(18,2) NOT NULL DEFAULT 0,
    ngayThu             timestamp     NOT NULL DEFAULT NOW(),
    phuongThucThanhToan varchar(15)   NOT NULL,
    ghiChu              varchar(500)  NULL,
    CONSTRAINT PK_PhieuThu             PRIMARY KEY (maPhieuThu),
    CONSTRAINT CHK_PhieuThu_PhuongThuc CHECK (phuongThucThanhToan IN ('ChuyenKhoan','TienMat')),
    CONSTRAINT FK_PhieuThu_HoaDon      FOREIGN KEY (maHoaDon)        REFERENCES HoaDon(maHoaDon),
    CONSTRAINT FK_PhieuThu_NhanVien    FOREIGN KEY (maNhanVienLap)   REFERENCES NhanVien(maNhanVien),
    CONSTRAINT FK_PhieuThu_PDP         FOREIGN KEY (maPhieuDatPhong) REFERENCES PhieuDatPhong(maPhieuDatPhong)
);

-- ============================================================
-- PHẦN 2: INDEXES
-- ============================================================

CREATE INDEX IDX_KhachHang_SoDienThoai    ON KhachHang(soDienThoai);
CREATE INDEX IDX_PhieuDatPhong_MaKhachHang ON PhieuDatPhong(maKhachHang);
CREATE INDEX IDX_PhieuDatPhong_ThoiGian    ON PhieuDatPhong(thoiGianNhanDuKien, thoiGianTraDuKien);
CREATE INDEX IDX_ChiTietPDP_MaPhong        ON ChiTietPhieuDatPhong(maPhong);
CREATE INDEX IDX_HoaDon_NgayThanhToan      ON HoaDon(ngayThanhToan);

-- ============================================================
-- PHẦN 3: VIEWS
-- ============================================================

CREATE OR REPLACE VIEW VW_DanhSachPhong AS
SELECT
    p.maPhong,
    p.tenPhong,
    lp.tenLoaiPhong,
    p.trangThai,
    bg.donGia    AS giaQuaDem,
    bg.loaiThue
FROM Phong p
JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong
LEFT JOIN BangGia bg ON lp.maLoaiPhong = bg.maLoaiPhong
    AND bg.loaiThue = 'QuaDem'
    AND CURRENT_DATE BETWEEN bg.ngayBatDau AND bg.ngayKetThuc;

CREATE OR REPLACE VIEW VW_DoanhThuTheoThang AS
SELECT
    EXTRACT(YEAR  FROM h.ngayThanhToan)::int AS Nam,
    EXTRACT(MONTH FROM h.ngayThanhToan)::int AS Thang,
    COUNT(h.maHoaDon)                         AS SoHoaDon,
    SUM(h.tienThanhToan)                      AS TongDoanhThu,
    SUM(h.tienKhuyenMai)                      AS TongGiamGia
FROM HoaDon h
WHERE h.ngayThanhToan IS NOT NULL
GROUP BY EXTRACT(YEAR FROM h.ngayThanhToan), EXTRACT(MONTH FROM h.ngayThanhToan);

CREATE OR REPLACE VIEW VW_PhongDatNhieuNhat AS
SELECT
    ct.maPhong,
    p.tenPhong,
    lp.tenLoaiPhong,
    COUNT(*) AS SoLuotDat
FROM ChiTietPhieuDatPhong ct
JOIN Phong p     ON ct.maPhong     = p.maPhong
JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong
GROUP BY ct.maPhong, p.tenPhong, lp.tenLoaiPhong;

CREATE OR REPLACE VIEW VW_DichVuSuDungNhieuNhat AS
SELECT
    dv.maDichVu,
    dv.tenDichVu,
    dv.loaiDichVu,
    SUM(ct.soLuong)                    AS TongSoLuong,
    COUNT(DISTINCT ct.maPhieuDatPhong) AS SoLanSuDung
FROM ChiTietDichVu ct
JOIN DichVu dv ON ct.maDichVu = dv.maDichVu
GROUP BY dv.maDichVu, dv.tenDichVu, dv.loaiDichVu;

-- ============================================================
-- PHẦN 4: FUNCTIONS (thay thế Stored Procedures)
-- ============================================================

CREATE OR REPLACE FUNCTION SP_CheckIn(p_maPhieuDatPhong varchar(10))
RETURNS void AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM PhieuDatPhong WHERE maPhieuDatPhong = p_maPhieuDatPhong) THEN
        RAISE EXCEPTION 'Phiếu đặt phòng không tồn tại.';
    END IF;

    IF EXISTS (
        SELECT 1 FROM PhieuDatPhong
        WHERE maPhieuDatPhong = p_maPhieuDatPhong
          AND thoiGianNhanThucTe IS NOT NULL
    ) THEN
        RAISE EXCEPTION 'Phiếu này đã check-in rồi.';
    END IF;

    UPDATE PhieuDatPhong
    SET thoiGianNhanThucTe = NOW()
    WHERE maPhieuDatPhong = p_maPhieuDatPhong;

    UPDATE Phong SET trangThai = 'PhongDangSuDung'
    WHERE maPhong IN (
        SELECT maPhong FROM ChiTietPhieuDatPhong
        WHERE maPhieuDatPhong = p_maPhieuDatPhong
    );
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION SP_DoiPhong(
    p_maPhieuDatPhong varchar(10),
    p_maPhongMoi      varchar(10),
    p_donGiaMoi       decimal(18,2)
) RETURNS void AS $$
DECLARE
    v_maPhongCu      varchar(10);
    v_thoiGianNhan   timestamp;
    v_thoiGianTra    timestamp;
BEGIN
    SELECT maPhong INTO v_maPhongCu
    FROM ChiTietPhieuDatPhong
    WHERE maPhieuDatPhong = p_maPhieuDatPhong;

    IF NOT EXISTS (
        SELECT 1 FROM Phong WHERE maPhong = p_maPhongMoi AND trangThai = 'PhongTrong'
    ) THEN
        RAISE EXCEPTION 'Phòng mới không trống.';
    END IF;

    SELECT thoiGianNhanDuKien, thoiGianTraDuKien
    INTO v_thoiGianNhan, v_thoiGianTra
    FROM PhieuDatPhong WHERE maPhieuDatPhong = p_maPhieuDatPhong;

    IF EXISTS (
        SELECT 1 FROM ChiTietPhieuDatPhong ct
        JOIN PhieuDatPhong p ON ct.maPhieuDatPhong = p.maPhieuDatPhong
        WHERE ct.maPhong = p_maPhongMoi
          AND ct.maPhieuDatPhong <> p_maPhieuDatPhong
          AND p.thoiGianNhanDuKien < v_thoiGianTra
          AND p.thoiGianTraDuKien  > v_thoiGianNhan
    ) THEN
        RAISE EXCEPTION 'Phòng mới đã có lịch đặt trùng thời gian.';
    END IF;

    UPDATE ChiTietPhieuDatPhong
    SET maPhong = p_maPhongMoi, donGia = p_donGiaMoi
    WHERE maPhieuDatPhong = p_maPhieuDatPhong;

    UPDATE Phong SET trangThai = 'PhongCanDon'      WHERE maPhong = v_maPhongCu;
    UPDATE Phong SET trangThai = 'PhongDangSuDung'  WHERE maPhong = p_maPhongMoi;
END;
$$ LANGUAGE plpgsql;
