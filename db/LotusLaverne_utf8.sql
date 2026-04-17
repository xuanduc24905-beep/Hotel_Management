USE [master]
GO
/****** Object:  Database [QuanLyKhachSan]    Script Date: 4/6/2026 9:54:42 PM ******/
CREATE DATABASE [QuanLyKhachSan]
 CONTAINMENT = NONE
 ON  PRIMARY 
( NAME = N'QuanLyKhachSan', FILENAME = N'C:\Program Files\Microsoft SQL Server\MSSQL17.MSSQLSERVER\MSSQL\DATA\QuanLyKhachSan.mdf' , SIZE = 8192KB , MAXSIZE = UNLIMITED, FILEGROWTH = 65536KB )
 LOG ON 
( NAME = N'QuanLyKhachSan_log', FILENAME = N'C:\Program Files\Microsoft SQL Server\MSSQL17.MSSQLSERVER\MSSQL\DATA\QuanLyKhachSan_log.ldf' , SIZE = 8192KB , MAXSIZE = 2048GB , FILEGROWTH = 65536KB )
 WITH CATALOG_COLLATION = DATABASE_DEFAULT, LEDGER = OFF
GO
ALTER DATABASE [QuanLyKhachSan] SET COMPATIBILITY_LEVEL = 170
GO
IF (1 = FULLTEXTSERVICEPROPERTY('IsFullTextInstalled'))
begin
EXEC [QuanLyKhachSan].[dbo].[sp_fulltext_database] @action = 'enable'
end
GO
ALTER DATABASE [QuanLyKhachSan] SET ANSI_NULL_DEFAULT OFF 
GO
ALTER DATABASE [QuanLyKhachSan] SET ANSI_NULLS OFF 
GO
ALTER DATABASE [QuanLyKhachSan] SET ANSI_PADDING OFF 
GO
ALTER DATABASE [QuanLyKhachSan] SET ANSI_WARNINGS OFF 
GO
ALTER DATABASE [QuanLyKhachSan] SET ARITHABORT OFF 
GO
ALTER DATABASE [QuanLyKhachSan] SET AUTO_CLOSE OFF 
GO
ALTER DATABASE [QuanLyKhachSan] SET AUTO_SHRINK OFF 
GO
ALTER DATABASE [QuanLyKhachSan] SET AUTO_UPDATE_STATISTICS ON 
GO
ALTER DATABASE [QuanLyKhachSan] SET CURSOR_CLOSE_ON_COMMIT OFF 
GO
ALTER DATABASE [QuanLyKhachSan] SET CURSOR_DEFAULT  GLOBAL 
GO
ALTER DATABASE [QuanLyKhachSan] SET CONCAT_NULL_YIELDS_NULL OFF 
GO
ALTER DATABASE [QuanLyKhachSan] SET NUMERIC_ROUNDABORT OFF 
GO
ALTER DATABASE [QuanLyKhachSan] SET QUOTED_IDENTIFIER OFF 
GO
ALTER DATABASE [QuanLyKhachSan] SET RECURSIVE_TRIGGERS OFF 
GO
ALTER DATABASE [QuanLyKhachSan] SET  ENABLE_BROKER 
GO
ALTER DATABASE [QuanLyKhachSan] SET AUTO_UPDATE_STATISTICS_ASYNC OFF 
GO
ALTER DATABASE [QuanLyKhachSan] SET DATE_CORRELATION_OPTIMIZATION OFF 
GO
ALTER DATABASE [QuanLyKhachSan] SET TRUSTWORTHY OFF 
GO
ALTER DATABASE [QuanLyKhachSan] SET ALLOW_SNAPSHOT_ISOLATION OFF 
GO
ALTER DATABASE [QuanLyKhachSan] SET PARAMETERIZATION SIMPLE 
GO
ALTER DATABASE [QuanLyKhachSan] SET READ_COMMITTED_SNAPSHOT OFF 
GO
ALTER DATABASE [QuanLyKhachSan] SET HONOR_BROKER_PRIORITY OFF 
GO
ALTER DATABASE [QuanLyKhachSan] SET RECOVERY FULL 
GO
ALTER DATABASE [QuanLyKhachSan] SET  MULTI_USER 
GO
ALTER DATABASE [QuanLyKhachSan] SET PAGE_VERIFY CHECKSUM  
GO
ALTER DATABASE [QuanLyKhachSan] SET DB_CHAINING OFF 
GO
ALTER DATABASE [QuanLyKhachSan] SET FILESTREAM( NON_TRANSACTED_ACCESS = OFF ) 
GO
ALTER DATABASE [QuanLyKhachSan] SET TARGET_RECOVERY_TIME = 60 SECONDS 
GO
ALTER DATABASE [QuanLyKhachSan] SET DELAYED_DURABILITY = DISABLED 
GO
ALTER DATABASE [QuanLyKhachSan] SET OPTIMIZED_LOCKING = OFF 
GO
ALTER DATABASE [QuanLyKhachSan] SET ACCELERATED_DATABASE_RECOVERY = OFF  
GO
EXEC sys.sp_db_vardecimal_storage_format N'QuanLyKhachSan', N'ON'
GO
ALTER DATABASE [QuanLyKhachSan] SET QUERY_STORE = ON
GO
ALTER DATABASE [QuanLyKhachSan] SET QUERY_STORE (OPERATION_MODE = READ_WRITE, CLEANUP_POLICY = (STALE_QUERY_THRESHOLD_DAYS = 30), DATA_FLUSH_INTERVAL_SECONDS = 900, INTERVAL_LENGTH_MINUTES = 60, MAX_STORAGE_SIZE_MB = 1000, QUERY_CAPTURE_MODE = AUTO, SIZE_BASED_CLEANUP_MODE = AUTO, MAX_PLANS_PER_QUERY = 200, WAIT_STATS_CAPTURE_MODE = ON)
GO
USE [QuanLyKhachSan]
GO
/****** Object:  Table [dbo].[LoaiPhong]    Script Date: 4/6/2026 9:54:43 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[LoaiPhong](
	[maLoaiPhong] [nvarchar](10) NOT NULL,
	[tenLoaiPhong] [nvarchar](100) NOT NULL,
 CONSTRAINT [PK_LoaiPhong] PRIMARY KEY CLUSTERED 
(
	[maLoaiPhong] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[BangGia]    Script Date: 4/6/2026 9:54:43 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[BangGia](
	[maBangGia] [nvarchar](10) NOT NULL,
	[maLoaiPhong] [nvarchar](10) NOT NULL,
	[loaiThue] [nvarchar](10) NOT NULL,
	[donGia] [float] NOT NULL,
	[ngayBatDau] [date] NOT NULL,
	[ngayKetThuc] [date] NOT NULL,
 CONSTRAINT [PK_BangGia] PRIMARY KEY CLUSTERED 
(
	[maBangGia] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Phong]    Script Date: 4/6/2026 9:54:43 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Phong](
	[maPhong] [nvarchar](10) NOT NULL,
	[tenPhong] [nvarchar](50) NOT NULL,
	[maLoaiPhong] [nvarchar](10) NOT NULL,
	[trangThai] [nvarchar](20) NOT NULL,
 CONSTRAINT [PK_Phong] PRIMARY KEY CLUSTERED 
(
	[maPhong] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  View [dbo].[VW_DanhSachPhong]    Script Date: 4/6/2026 9:54:43 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- ============================================================
-- PHẦN 5: VIEWS - BÁO CÁO / THỐNG KÊ
-- ============================================================

-- View: Danh sách phòng + loại + giá hiện tại
CREATE   VIEW [dbo].[VW_DanhSachPhong] AS
SELECT
    p.maPhong,
    p.tenPhong,
    lp.tenLoaiPhong,
    p.trangThai,
    bg.donGia       AS giaQuaDem,
    bg.loaiThue
FROM Phong p
JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong
LEFT JOIN BangGia bg ON lp.maLoaiPhong = bg.maLoaiPhong
    AND bg.loaiThue = N'QuaDem'
    AND GETDATE() BETWEEN bg.ngayBatDau AND bg.ngayKetThuc;
GO
/****** Object:  Table [dbo].[HoaDon]    Script Date: 4/6/2026 9:54:43 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[HoaDon](
	[maHoaDon] [nvarchar](10) NOT NULL,
	[ngayLap] [datetime] NOT NULL,
	[maNhanVienLap] [nvarchar](10) NOT NULL,
	[maPhieuDatPhong] [nvarchar](10) NOT NULL,
	[ngayThanhToan] [datetime] NULL,
	[tienKhuyenMai] [decimal](18, 2) NOT NULL,
	[tienThanhToan] [decimal](18, 2) NOT NULL,
	[phuongThucThanhToan] [nvarchar](15) NOT NULL,
	[ghiChu] [nvarchar](500) NULL,
 CONSTRAINT [PK_HoaDon] PRIMARY KEY CLUSTERED 
(
	[maHoaDon] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  View [dbo].[VW_DoanhThuTheoThang]    Script Date: 4/6/2026 9:54:43 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- View: Doanh thu theo tháng
CREATE   VIEW [dbo].[VW_DoanhThuTheoThang] AS
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
/****** Object:  Table [dbo].[ChiTietPhieuDatPhong]    Script Date: 4/6/2026 9:54:43 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ChiTietPhieuDatPhong](
	[maPhieuDatPhong] [nvarchar](10) NOT NULL,
	[maPhong] [nvarchar](10) NOT NULL,
	[donGia] [decimal](18, 2) NOT NULL,
	[maPhuThu] [nvarchar](10) NULL,
 CONSTRAINT [PK_ChiTietPhieuDatPhong] PRIMARY KEY CLUSTERED 
(
	[maPhieuDatPhong] ASC,
	[maPhong] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  View [dbo].[VW_PhongDatNhieuNhat]    Script Date: 4/6/2026 9:54:43 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- View: Phòng được đặt nhiều nhất
CREATE   VIEW [dbo].[VW_PhongDatNhieuNhat] AS
SELECT
    ct.maPhong,
    p.tenPhong,
    lp.tenLoaiPhong,
    COUNT(*) AS SoLuotDat
FROM ChiTietPhieuDatPhong ct
JOIN Phong p ON ct.maPhong = p.maPhong
JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong
GROUP BY ct.maPhong, p.tenPhong, lp.tenLoaiPhong;
GO
/****** Object:  Table [dbo].[DichVu]    Script Date: 4/6/2026 9:54:43 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[DichVu](
	[maDichVu] [nvarchar](10) NOT NULL,
	[tenDichVu] [nvarchar](100) NOT NULL,
	[donGia] [float] NOT NULL,
	[soLuongTonKho] [int] NOT NULL,
	[loaiDichVu] [nvarchar](10) NOT NULL,
 CONSTRAINT [PK_DichVu] PRIMARY KEY CLUSTERED 
(
	[maDichVu] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[ChiTietDichVu]    Script Date: 4/6/2026 9:54:43 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ChiTietDichVu](
	[maDichVu] [nvarchar](10) NOT NULL,
	[maPhieuDatPhong] [nvarchar](10) NOT NULL,
	[soLuong] [int] NOT NULL,
	[thoiDiemSuDung] [datetime] NOT NULL,
	[ghiChu] [nvarchar](255) NULL,
 CONSTRAINT [PK_ChiTietDichVu] PRIMARY KEY CLUSTERED 
(
	[maDichVu] ASC,
	[maPhieuDatPhong] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  View [dbo].[VW_DichVuSuDungNhieuNhat]    Script Date: 4/6/2026 9:54:43 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- View: Dịch vụ sử dụng nhiều nhất
CREATE   VIEW [dbo].[VW_DichVuSuDungNhieuNhat] AS
SELECT
    dv.maDichVu,
    dv.tenDichVu,
    dv.loaiDichVu,
    SUM(ct.soLuong)                     AS TongSoLuong,
    COUNT(DISTINCT ct.maPhieuDatPhong)  AS SoLanSuDung
FROM ChiTietDichVu ct
JOIN DichVu dv ON ct.maDichVu = dv.maDichVu
GROUP BY dv.maDichVu, dv.tenDichVu, dv.loaiDichVu;
GO
/****** Object:  Table [dbo].[ChiTietHoaDon]    Script Date: 4/6/2026 9:54:43 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ChiTietHoaDon](
	[maChiTiet] [int] IDENTITY(1,1) NOT NULL,
	[maHoaDon] [nvarchar](10) NOT NULL,
	[loaiTien] [nvarchar](15) NOT NULL,
	[maKhuyenMai] [nvarchar](10) NULL,
	[moTa] [nvarchar](255) NULL,
	[donGia] [decimal](18, 2) NOT NULL,
	[thanhTien] [decimal](18, 2) NOT NULL,
	[soLuong] [int] NOT NULL,
 CONSTRAINT [PK_ChiTietHoaDon] PRIMARY KEY CLUSTERED 
(
	[maChiTiet] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[KhachHang]    Script Date: 4/6/2026 9:54:43 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[KhachHang](
	[maKH] [nvarchar](10) NOT NULL,
	[hoTenKH] [nvarchar](100) NOT NULL,
	[cccd] [nvarchar](20) NOT NULL,
	[soDienThoai] [nvarchar](15) NOT NULL,
	[gioiTinh] [bit] NOT NULL,
	[ngaySinh] [datetime] NULL,
	[diaChi] [nvarchar](200) NULL,
	[quocTich] [nvarchar](50) NULL,
 CONSTRAINT [PK_KhachHang] PRIMARY KEY CLUSTERED 
(
	[maKH] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_KhachHang_CCCD] UNIQUE NONCLUSTERED 
(
	[cccd] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[KhuyenMai]    Script Date: 4/6/2026 9:54:43 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[KhuyenMai](
	[maKhuyenMai] [nvarchar](10) NOT NULL,
	[tenKhuyenMai] [nvarchar](100) NOT NULL,
	[ngayApDung] [datetime] NOT NULL,
	[ngayKetThuc] [datetime] NOT NULL,
	[phanTramGiam] [float] NOT NULL,
	[dieuKienApDung] [nvarchar](255) NULL,
 CONSTRAINT [PK_KhuyenMai] PRIMARY KEY CLUSTERED 
(
	[maKhuyenMai] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[NhanVien]    Script Date: 4/6/2026 9:54:43 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[NhanVien](
	[maNhanVien] [nvarchar](10) NOT NULL,
	[hoTenNhanVien] [nvarchar](100) NOT NULL,
	[cccd] [nvarchar](20) NOT NULL,
	[ngaySinh] [datetime] NULL,
	[ngayBatDauLam] [datetime] NULL,
	[ngayKetThucHopDong] [datetime] NULL,
	[soDienThoai] [nvarchar](15) NULL,
	[diaChi] [nvarchar](200) NULL,
	[email] [nvarchar](100) NULL,
	[vaiTro] [nvarchar](10) NOT NULL,
 CONSTRAINT [PK_NhanVien] PRIMARY KEY CLUSTERED 
(
	[maNhanVien] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_NhanVien_CCCD] UNIQUE NONCLUSTERED 
(
	[cccd] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[PhieuDatPhong]    Script Date: 4/6/2026 9:54:43 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[PhieuDatPhong](
	[maPhieuDatPhong] [nvarchar](10) NOT NULL,
	[ngayDat] [datetime] NOT NULL,
	[maKhachHang] [nvarchar](10) NOT NULL,
	[maNhanVien] [nvarchar](10) NOT NULL,
	[soNguoi] [int] NOT NULL,
	[thoiGianNhanDuKien] [datetime] NOT NULL,
	[thoiGianNhanThucTe] [datetime] NULL,
	[thoiGianTraDuKien] [datetime] NOT NULL,
	[thoiGianTraThucTe] [datetime] NULL,
	[ghiChu] [nvarchar](500) NULL,
 CONSTRAINT [PK_PhieuDatPhong] PRIMARY KEY CLUSTERED 
(
	[maPhieuDatPhong] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[PhieuThu]    Script Date: 4/6/2026 9:54:43 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[PhieuThu](
	[maPhieuThu] [nvarchar](10) NOT NULL,
	[maHoaDon] [nvarchar](10) NULL,
	[maNhanVienLap] [nvarchar](10) NOT NULL,
	[maPhieuDatPhong] [nvarchar](10) NOT NULL,
	[soTienCoc] [decimal](18, 2) NOT NULL,
	[ngayThu] [datetime] NOT NULL,
	[phuongThucThanhToan] [nvarchar](15) NOT NULL,
	[ghiChu] [nvarchar](500) NULL,
 CONSTRAINT [PK_PhieuThu] PRIMARY KEY CLUSTERED 
(
	[maPhieuThu] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[PhuThu]    Script Date: 4/6/2026 9:54:43 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[PhuThu](
	[maPhuThu] [nvarchar](10) NOT NULL,
	[tenPhuThu] [nvarchar](100) NOT NULL,
	[loaiPhuThu] [nvarchar](15) NOT NULL,
	[donGia] [decimal](18, 2) NOT NULL,
	[ghiChu] [nvarchar](255) NULL,
 CONSTRAINT [PK_PhuThu] PRIMARY KEY CLUSTERED 
(
	[maPhuThu] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[TaiKhoan]    Script Date: 4/6/2026 9:54:43 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[TaiKhoan](
	[maTaiKhoan] [nvarchar](10) NOT NULL,
	[maNhanVien] [nvarchar](10) NOT NULL,
	[vaiTro] [nvarchar](10) NOT NULL,
	[tenDangNhap] [nvarchar](50) NOT NULL,
	[matKhau] [nvarchar](255) NOT NULL,
 CONSTRAINT [PK_TaiKhoan] PRIMARY KEY CLUSTERED 
(
	[maTaiKhoan] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_TaiKhoan_NhanVien] UNIQUE NONCLUSTERED 
(
	[maNhanVien] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_TaiKhoan_TenDangNhap] UNIQUE NONCLUSTERED 
(
	[tenDangNhap] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
ALTER TABLE [dbo].[ChiTietDichVu] ADD  DEFAULT (getdate()) FOR [thoiDiemSuDung]
GO
ALTER TABLE [dbo].[ChiTietHoaDon] ADD  DEFAULT ((1)) FOR [soLuong]
GO
ALTER TABLE [dbo].[DichVu] ADD  DEFAULT ((0)) FOR [soLuongTonKho]
GO
ALTER TABLE [dbo].[HoaDon] ADD  DEFAULT (getdate()) FOR [ngayLap]
GO
ALTER TABLE [dbo].[HoaDon] ADD  DEFAULT ((0)) FOR [tienKhuyenMai]
GO
ALTER TABLE [dbo].[KhachHang] ADD  DEFAULT (N'Việt Nam') FOR [quocTich]
GO
ALTER TABLE [dbo].[PhieuDatPhong] ADD  DEFAULT (getdate()) FOR [ngayDat]
GO
ALTER TABLE [dbo].[PhieuThu] ADD  DEFAULT ((0)) FOR [soTienCoc]
GO
ALTER TABLE [dbo].[PhieuThu] ADD  DEFAULT (getdate()) FOR [ngayThu]
GO
ALTER TABLE [dbo].[Phong] ADD  DEFAULT (N'PhongTrong') FOR [trangThai]
GO
ALTER TABLE [dbo].[BangGia]  WITH CHECK ADD  CONSTRAINT [FK_BangGia_LoaiPhong] FOREIGN KEY([maLoaiPhong])
REFERENCES [dbo].[LoaiPhong] ([maLoaiPhong])
GO
ALTER TABLE [dbo].[BangGia] CHECK CONSTRAINT [FK_BangGia_LoaiPhong]
GO
ALTER TABLE [dbo].[ChiTietDichVu]  WITH CHECK ADD  CONSTRAINT [FK_CTDV_DichVu] FOREIGN KEY([maDichVu])
REFERENCES [dbo].[DichVu] ([maDichVu])
GO
ALTER TABLE [dbo].[ChiTietDichVu] CHECK CONSTRAINT [FK_CTDV_DichVu]
GO
ALTER TABLE [dbo].[ChiTietDichVu]  WITH CHECK ADD  CONSTRAINT [FK_CTDV_PhieuDatPhong] FOREIGN KEY([maPhieuDatPhong])
REFERENCES [dbo].[PhieuDatPhong] ([maPhieuDatPhong])
GO
ALTER TABLE [dbo].[ChiTietDichVu] CHECK CONSTRAINT [FK_CTDV_PhieuDatPhong]
GO
ALTER TABLE [dbo].[ChiTietHoaDon]  WITH CHECK ADD  CONSTRAINT [FK_CTHD_HoaDon] FOREIGN KEY([maHoaDon])
REFERENCES [dbo].[HoaDon] ([maHoaDon])
GO
ALTER TABLE [dbo].[ChiTietHoaDon] CHECK CONSTRAINT [FK_CTHD_HoaDon]
GO
ALTER TABLE [dbo].[ChiTietHoaDon]  WITH CHECK ADD  CONSTRAINT [FK_CTHD_KhuyenMai] FOREIGN KEY([maKhuyenMai])
REFERENCES [dbo].[KhuyenMai] ([maKhuyenMai])
GO
ALTER TABLE [dbo].[ChiTietHoaDon] CHECK CONSTRAINT [FK_CTHD_KhuyenMai]
GO
ALTER TABLE [dbo].[ChiTietPhieuDatPhong]  WITH CHECK ADD  CONSTRAINT [FK_CTPDP_PhieuDatPhong] FOREIGN KEY([maPhieuDatPhong])
REFERENCES [dbo].[PhieuDatPhong] ([maPhieuDatPhong])
GO
ALTER TABLE [dbo].[ChiTietPhieuDatPhong] CHECK CONSTRAINT [FK_CTPDP_PhieuDatPhong]
GO
ALTER TABLE [dbo].[ChiTietPhieuDatPhong]  WITH CHECK ADD  CONSTRAINT [FK_CTPDP_Phong] FOREIGN KEY([maPhong])
REFERENCES [dbo].[Phong] ([maPhong])
GO
ALTER TABLE [dbo].[ChiTietPhieuDatPhong] CHECK CONSTRAINT [FK_CTPDP_Phong]
GO
ALTER TABLE [dbo].[ChiTietPhieuDatPhong]  WITH CHECK ADD  CONSTRAINT [FK_CTPDP_PhuThu] FOREIGN KEY([maPhuThu])
REFERENCES [dbo].[PhuThu] ([maPhuThu])
GO
ALTER TABLE [dbo].[ChiTietPhieuDatPhong] CHECK CONSTRAINT [FK_CTPDP_PhuThu]
GO
ALTER TABLE [dbo].[HoaDon]  WITH CHECK ADD  CONSTRAINT [FK_HoaDon_NhanVien] FOREIGN KEY([maNhanVienLap])
REFERENCES [dbo].[NhanVien] ([maNhanVien])
GO
ALTER TABLE [dbo].[HoaDon] CHECK CONSTRAINT [FK_HoaDon_NhanVien]
GO
ALTER TABLE [dbo].[HoaDon]  WITH CHECK ADD  CONSTRAINT [FK_HoaDon_PhieuDatPhong] FOREIGN KEY([maPhieuDatPhong])
REFERENCES [dbo].[PhieuDatPhong] ([maPhieuDatPhong])
GO
ALTER TABLE [dbo].[HoaDon] CHECK CONSTRAINT [FK_HoaDon_PhieuDatPhong]
GO
ALTER TABLE [dbo].[PhieuDatPhong]  WITH CHECK ADD  CONSTRAINT [FK_PDP_KhachHang] FOREIGN KEY([maKhachHang])
REFERENCES [dbo].[KhachHang] ([maKH])
GO
ALTER TABLE [dbo].[PhieuDatPhong] CHECK CONSTRAINT [FK_PDP_KhachHang]
GO
ALTER TABLE [dbo].[PhieuDatPhong]  WITH CHECK ADD  CONSTRAINT [FK_PDP_NhanVien] FOREIGN KEY([maNhanVien])
REFERENCES [dbo].[NhanVien] ([maNhanVien])
GO
ALTER TABLE [dbo].[PhieuDatPhong] CHECK CONSTRAINT [FK_PDP_NhanVien]
GO
ALTER TABLE [dbo].[PhieuThu]  WITH CHECK ADD  CONSTRAINT [FK_PhieuThu_HoaDon] FOREIGN KEY([maHoaDon])
REFERENCES [dbo].[HoaDon] ([maHoaDon])
GO
ALTER TABLE [dbo].[PhieuThu] CHECK CONSTRAINT [FK_PhieuThu_HoaDon]
GO
ALTER TABLE [dbo].[PhieuThu]  WITH CHECK ADD  CONSTRAINT [FK_PhieuThu_NhanVien] FOREIGN KEY([maNhanVienLap])
REFERENCES [dbo].[NhanVien] ([maNhanVien])
GO
ALTER TABLE [dbo].[PhieuThu] CHECK CONSTRAINT [FK_PhieuThu_NhanVien]
GO
ALTER TABLE [dbo].[PhieuThu]  WITH CHECK ADD  CONSTRAINT [FK_PhieuThu_PhieuDatPhong] FOREIGN KEY([maPhieuDatPhong])
REFERENCES [dbo].[PhieuDatPhong] ([maPhieuDatPhong])
GO
ALTER TABLE [dbo].[PhieuThu] CHECK CONSTRAINT [FK_PhieuThu_PhieuDatPhong]
GO
ALTER TABLE [dbo].[Phong]  WITH CHECK ADD  CONSTRAINT [FK_Phong_LoaiPhong] FOREIGN KEY([maLoaiPhong])
REFERENCES [dbo].[LoaiPhong] ([maLoaiPhong])
GO
ALTER TABLE [dbo].[Phong] CHECK CONSTRAINT [FK_Phong_LoaiPhong]
GO
ALTER TABLE [dbo].[TaiKhoan]  WITH CHECK ADD  CONSTRAINT [FK_TaiKhoan_NhanVien] FOREIGN KEY([maNhanVien])
REFERENCES [dbo].[NhanVien] ([maNhanVien])
GO
ALTER TABLE [dbo].[TaiKhoan] CHECK CONSTRAINT [FK_TaiKhoan_NhanVien]
GO
ALTER TABLE [dbo].[BangGia]  WITH CHECK ADD  CONSTRAINT [CHK_BangGia_DonGia] CHECK  (([donGia]>(0)))
GO
ALTER TABLE [dbo].[BangGia] CHECK CONSTRAINT [CHK_BangGia_DonGia]
GO
ALTER TABLE [dbo].[BangGia]  WITH CHECK ADD  CONSTRAINT [CHK_BangGia_LoaiThue] CHECK  (([loaiThue]=N'QuaDem' OR [loaiThue]=N'TheoNgay' OR [loaiThue]=N'TheoGio'))
GO
ALTER TABLE [dbo].[BangGia] CHECK CONSTRAINT [CHK_BangGia_LoaiThue]
GO
ALTER TABLE [dbo].[ChiTietDichVu]  WITH CHECK ADD  CONSTRAINT [CHK_CTDV_SoLuong] CHECK  (([soLuong]>(0)))
GO
ALTER TABLE [dbo].[ChiTietDichVu] CHECK CONSTRAINT [CHK_CTDV_SoLuong]
GO
ALTER TABLE [dbo].[ChiTietHoaDon]  WITH CHECK ADD  CONSTRAINT [CHK_CTHD_LoaiTien] CHECK  (([loaiTien]=N'TienPhuThu' OR [loaiTien]=N'TienDichVu' OR [loaiTien]=N'TienPhong' OR [loaiTien]=N'TienCoc'))
GO
ALTER TABLE [dbo].[ChiTietHoaDon] CHECK CONSTRAINT [CHK_CTHD_LoaiTien]
GO
ALTER TABLE [dbo].[ChiTietHoaDon]  WITH CHECK ADD  CONSTRAINT [CHK_CTHD_SoLuong] CHECK  (([soLuong]>(0)))
GO
ALTER TABLE [dbo].[ChiTietHoaDon] CHECK CONSTRAINT [CHK_CTHD_SoLuong]
GO
ALTER TABLE [dbo].[ChiTietPhieuDatPhong]  WITH CHECK ADD  CONSTRAINT [CHK_CTPDP_DonGia] CHECK  (([donGia]>(0)))
GO
ALTER TABLE [dbo].[ChiTietPhieuDatPhong] CHECK CONSTRAINT [CHK_CTPDP_DonGia]
GO
ALTER TABLE [dbo].[DichVu]  WITH CHECK ADD  CONSTRAINT [CHK_DichVu_DonGia] CHECK  (([donGia]>(0)))
GO
ALTER TABLE [dbo].[DichVu] CHECK CONSTRAINT [CHK_DichVu_DonGia]
GO
ALTER TABLE [dbo].[DichVu]  WITH CHECK ADD  CONSTRAINT [CHK_DichVu_Loai] CHECK  (([loaiDichVu]=N'TienIch' OR [loaiDichVu]=N'DoAn' OR [loaiDichVu]=N'DoUong'))
GO
ALTER TABLE [dbo].[DichVu] CHECK CONSTRAINT [CHK_DichVu_Loai]
GO
ALTER TABLE [dbo].[HoaDon]  WITH CHECK ADD  CONSTRAINT [CHK_HoaDon_PhuongThuc] CHECK  (([phuongThucThanhToan]=N'ChuyenKhoan' OR [phuongThucThanhToan]=N'TienMat'))
GO
ALTER TABLE [dbo].[HoaDon] CHECK CONSTRAINT [CHK_HoaDon_PhuongThuc]
GO
ALTER TABLE [dbo].[HoaDon]  WITH CHECK ADD  CONSTRAINT [CHK_HoaDon_TienThanhToan] CHECK  (([tienThanhToan]>=(0)))
GO
ALTER TABLE [dbo].[HoaDon] CHECK CONSTRAINT [CHK_HoaDon_TienThanhToan]
GO
ALTER TABLE [dbo].[KhuyenMai]  WITH CHECK ADD  CONSTRAINT [CHK_KhuyenMai_Ngay] CHECK  (([ngayKetThuc]>[ngayApDung]))
GO
ALTER TABLE [dbo].[KhuyenMai] CHECK CONSTRAINT [CHK_KhuyenMai_Ngay]
GO
ALTER TABLE [dbo].[KhuyenMai]  WITH CHECK ADD  CONSTRAINT [CHK_KhuyenMai_PhanTram] CHECK  (([phanTramGiam]>(0) AND [phanTramGiam]<=(100)))
GO
ALTER TABLE [dbo].[KhuyenMai] CHECK CONSTRAINT [CHK_KhuyenMai_PhanTram]
GO
ALTER TABLE [dbo].[NhanVien]  WITH CHECK ADD  CONSTRAINT [CHK_NhanVien_VaiTro] CHECK  (([vaiTro]=N'LeTan' OR [vaiTro]=N'QuanLy'))
GO
ALTER TABLE [dbo].[NhanVien] CHECK CONSTRAINT [CHK_NhanVien_VaiTro]
GO
ALTER TABLE [dbo].[PhieuDatPhong]  WITH CHECK ADD  CONSTRAINT [CHK_PDP_Ngay] CHECK  (([thoiGianTraDuKien]>[thoiGianNhanDuKien]))
GO
ALTER TABLE [dbo].[PhieuDatPhong] CHECK CONSTRAINT [CHK_PDP_Ngay]
GO
ALTER TABLE [dbo].[PhieuDatPhong]  WITH CHECK ADD  CONSTRAINT [CHK_PDP_SoNguoi] CHECK  (([soNguoi]>(0)))
GO
ALTER TABLE [dbo].[PhieuDatPhong] CHECK CONSTRAINT [CHK_PDP_SoNguoi]
GO
ALTER TABLE [dbo].[PhieuThu]  WITH CHECK ADD  CONSTRAINT [CHK_PhieuThu_PhuongThuc] CHECK  (([phuongThucThanhToan]=N'ChuyenKhoan' OR [phuongThucThanhToan]=N'TienMat'))
GO
ALTER TABLE [dbo].[PhieuThu] CHECK CONSTRAINT [CHK_PhieuThu_PhuongThuc]
GO
ALTER TABLE [dbo].[Phong]  WITH CHECK ADD  CONSTRAINT [CHK_Phong_TrangThai] CHECK  (([trangThai]=N'PhongTrong' OR [trangThai]=N'PhongCanDon' OR [trangThai]=N'PhongDangSuDung' OR [trangThai]=N'PhongDat'))
GO
ALTER TABLE [dbo].[Phong] CHECK CONSTRAINT [CHK_Phong_TrangThai]
GO
ALTER TABLE [dbo].[PhuThu]  WITH CHECK ADD  CONSTRAINT [CHK_PhuThu_DonGia] CHECK  (([donGia]>(0)))
GO
ALTER TABLE [dbo].[PhuThu] CHECK CONSTRAINT [CHK_PhuThu_DonGia]
GO
ALTER TABLE [dbo].[PhuThu]  WITH CHECK ADD  CONSTRAINT [CHK_PhuThu_Loai] CHECK  (([loaiPhuThu]=N'HuHongDoDac' OR [loaiPhuThu]=N'RaMuon' OR [loaiPhuThu]=N'NhanMuon'))
GO
ALTER TABLE [dbo].[PhuThu] CHECK CONSTRAINT [CHK_PhuThu_Loai]
GO
ALTER TABLE [dbo].[TaiKhoan]  WITH CHECK ADD  CONSTRAINT [CHK_TaiKhoan_VaiTro] CHECK  (([vaiTro]=N'LeTan' OR [vaiTro]=N'QuanLy'))
GO
ALTER TABLE [dbo].[TaiKhoan] CHECK CONSTRAINT [CHK_TaiKhoan_VaiTro]
GO
ALTER TABLE [dbo].[BangGia]  WITH CHECK ADD  CONSTRAINT [CHK_BangGia_Ngay] CHECK  (([ngayKetThuc]>[ngayBatDau]))
GO
ALTER TABLE [dbo].[BangGia] CHECK CONSTRAINT [CHK_BangGia_Ngay]
GO

-- ============================================================
-- PHẦN 6: INDEXES TỐI ƯU TÌM KIẾM
-- ============================================================
CREATE NONCLUSTERED INDEX [IDX_KhachHang_SoDienThoai]
    ON [dbo].[KhachHang] ([soDienThoai]);
GO
CREATE NONCLUSTERED INDEX [IDX_PhieuDatPhong_MaKhachHang]
    ON [dbo].[PhieuDatPhong] ([maKhachHang]);
GO
CREATE NONCLUSTERED INDEX [IDX_PhieuDatPhong_ThoiGian]
    ON [dbo].[PhieuDatPhong] ([thoiGianNhanDuKien], [thoiGianTraDuKien]);
GO
CREATE NONCLUSTERED INDEX [IDX_ChiTietPDP_MaPhong]
    ON [dbo].[ChiTietPhieuDatPhong] ([maPhong]);
GO
CREATE NONCLUSTERED INDEX [IDX_HoaDon_NgayThanhToan]
    ON [dbo].[HoaDon] ([ngayThanhToan]);
GO
/****** Object:  StoredProcedure [dbo].[SP_CheckIn]    Script Date: 4/6/2026 9:54:43 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- SP2: Check-in
CREATE   PROCEDURE [dbo].[SP_CheckIn]
    @maPhieuDatPhong NVARCHAR(10)
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    BEGIN TRY
        IF NOT EXISTS (SELECT 1 FROM PhieuDatPhong WHERE maPhieuDatPhong = @maPhieuDatPhong)
        BEGIN
            RAISERROR(N'Phiếu đặt phòng không tồn tại.', 16, 1);
            ROLLBACK; RETURN;
        END

        IF EXISTS (
            SELECT 1 FROM PhieuDatPhong
            WHERE maPhieuDatPhong = @maPhieuDatPhong
              AND thoiGianNhanThucTe IS NOT NULL
        )
        BEGIN
            RAISERROR(N'Phiếu này đã check-in rồi.', 16, 1);
            ROLLBACK; RETURN;
        END

        UPDATE PhieuDatPhong
        SET thoiGianNhanThucTe = GETDATE()
        WHERE maPhieuDatPhong = @maPhieuDatPhong;

        UPDATE Phong SET trangThai = N'PhongDangSuDung'
        WHERE maPhong IN (
            SELECT maPhong FROM ChiTietPhieuDatPhong
            WHERE maPhieuDatPhong = @maPhieuDatPhong
        );

        COMMIT;
        PRINT N'Check-in phiếu ' + @maPhieuDatPhong + N' thành công.';
    END TRY
    BEGIN CATCH
        ROLLBACK; THROW;
    END CATCH
END;
GO
/****** Object:  StoredProcedure [dbo].[SP_CheckOut]    Script Date: 4/6/2026 9:54:43 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- SP3: Check-out và tạo hóa đơn
CREATE   PROCEDURE [dbo].[SP_CheckOut]
    @maPhieuDatPhong        NVARCHAR(10),
    @maHoaDon               NVARCHAR(10),
    @maNhanVien             NVARCHAR(10),
    @maKhuyenMai            NVARCHAR(10) = NULL,
    @phuongThucThanhToan    NVARCHAR(15)
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    BEGIN TRY
        DECLARE @tienPhong      DECIMAL(18,2);
        DECLARE @tienDichVu     DECIMAL(18,2);
        DECLARE @tienKhuyenMai  DECIMAL(18,2) = 0;
        DECLARE @phanTramGiam   FLOAT = 0;

        SELECT @tienPhong =
            DATEDIFF(DAY, ISNULL(p.thoiGianNhanThucTe, p.thoiGianNhanDuKien), GETDATE()) * ct.donGia
        FROM PhieuDatPhong p
        JOIN ChiTietPhieuDatPhong ct ON p.maPhieuDatPhong = ct.maPhieuDatPhong
        WHERE p.maPhieuDatPhong = @maPhieuDatPhong;

        SELECT @tienDichVu = ISNULL(SUM(ctdv.soLuong * dv.donGia), 0)
        FROM ChiTietDichVu ctdv
        JOIN DichVu dv ON ctdv.maDichVu = dv.maDichVu
        WHERE ctdv.maPhieuDatPhong = @maPhieuDatPhong;

        IF @maKhuyenMai IS NOT NULL
        BEGIN
            SELECT @phanTramGiam = phanTramGiam
            FROM KhuyenMai
            WHERE maKhuyenMai = @maKhuyenMai
              AND GETDATE() BETWEEN ngayApDung AND ngayKetThuc;

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
        SET thoiGianTraThucTe = GETDATE()
        WHERE maPhieuDatPhong = @maPhieuDatPhong;

        COMMIT;
        PRINT N'Check-out thành công. Tổng tiền: ' + CAST(@tongTien AS NVARCHAR) + N' VNĐ';
    END TRY
    BEGIN CATCH
        ROLLBACK; THROW;
    END CATCH
END;
GO
/****** Object:  StoredProcedure [dbo].[SP_DoiPhong]    Script Date: 4/6/2026 9:54:43 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- SP4: Đổi phòng
CREATE   PROCEDURE [dbo].[SP_DoiPhong]
    @maPhieuDatPhong    NVARCHAR(10),
    @maPhongMoi         NVARCHAR(10),
    @donGiaMoi          DECIMAL(18,2)
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    BEGIN TRY
        DECLARE @maPhongCu NVARCHAR(10);

        SELECT @maPhongCu = maPhong FROM ChiTietPhieuDatPhong
        WHERE maPhieuDatPhong = @maPhieuDatPhong;

        IF NOT EXISTS (
            SELECT 1 FROM Phong
            WHERE maPhong = @maPhongMoi AND trangThai = N'PhongTrong'
        )
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

        UPDATE Phong SET trangThai = N'PhongCanDon'  WHERE maPhong = @maPhongCu;
        UPDATE Phong SET trangThai = N'PhongDangSuDung' WHERE maPhong = @maPhongMoi;

        COMMIT;
        PRINT N'Đổi phòng từ ' + @maPhongCu + N' sang ' + @maPhongMoi + N' thành công.';
    END TRY
    BEGIN CATCH
        ROLLBACK; THROW;
    END CATCH
END;
GO
/****** Object:  StoredProcedure [dbo].[SP_TaoPhieuDatPhong]    Script Date: 4/6/2026 9:54:43 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- ============================================================
-- PHẦN 3: STORED PROCEDURES
-- ============================================================

-- SP1: Tạo phiếu đặt phòng mới
CREATE   PROCEDURE [dbo].[SP_TaoPhieuDatPhong]
    @maPhieuDatPhong    NVARCHAR(10),
    @maKhachHang        NVARCHAR(10),
    @maNhanVien         NVARCHAR(10),
    @soNguoi            INT,
    @maPhong            NVARCHAR(10),
    @thoiGianNhanDuKien DATETIME,
    @thoiGianTraDuKien  DATETIME,
    @donGia             DECIMAL(18,2),
    @ghiChu             NVARCHAR(500) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    BEGIN TRY
        -- Kiểm tra phòng còn trống
        IF NOT EXISTS (
            SELECT 1 FROM Phong
            WHERE maPhong = @maPhong AND trangThai = N'PhongTrong'
        )
        BEGIN
            RAISERROR(N'Phòng %s hiện không trống.', 16, 1, @maPhong);
            ROLLBACK; RETURN;
        END

        -- Kiểm tra trùng lịch
        IF EXISTS (
            SELECT 1 FROM ChiTietPhieuDatPhong ct
            JOIN PhieuDatPhong p ON ct.maPhieuDatPhong = p.maPhieuDatPhong
            WHERE ct.maPhong = @maPhong
              AND p.thoiGianNhanDuKien < @thoiGianTraDuKien
              AND p.thoiGianTraDuKien  > @thoiGianNhanDuKien
        )
        BEGIN
            RAISERROR(N'Phòng %s đã có lịch đặt trùng.', 16, 1, @maPhong);
            ROLLBACK; RETURN;
        END

        -- Tạo phiếu
        INSERT INTO PhieuDatPhong
            (maPhieuDatPhong, ngayDat, maKhachHang, maNhanVien, soNguoi,
             thoiGianNhanDuKien, thoiGianTraDuKien, ghiChu)
        VALUES
            (@maPhieuDatPhong, GETDATE(), @maKhachHang, @maNhanVien, @soNguoi,
             @thoiGianNhanDuKien, @thoiGianTraDuKien, @ghiChu);

        -- Tạo chi tiết
        INSERT INTO ChiTietPhieuDatPhong (maPhieuDatPhong, maPhong, donGia)
        VALUES (@maPhieuDatPhong, @maPhong, @donGia);

        -- Cập nhật trạng thái phòng
        UPDATE Phong SET trangThai = N'PhongDat'
        WHERE maPhong = @maPhong;

        COMMIT;
        PRINT N'Tạo phiếu đặt phòng ' + @maPhieuDatPhong + N' thành công.';
    END TRY
    BEGIN CATCH
        ROLLBACK; THROW;
    END CATCH
END;
GO
USE [master]
GO
ALTER DATABASE [QuanLyKhachSan] SET  READ_WRITE 
GO
