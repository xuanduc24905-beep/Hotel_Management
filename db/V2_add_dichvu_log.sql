-- Migration V2: Bảng log thay đổi dịch vụ phòng (Rule 13)
-- Chạy 1 lần trên database QuanLyKhachSan:
--   sqlcmd -S localhost -U sa -P <password> -d QuanLyKhachSan -i db/V2_add_dichvu_log.sql

IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_NAME = 'ChiTietDichVuLog'
)
BEGIN
    CREATE TABLE ChiTietDichVuLog (
        id            INT IDENTITY(1,1) PRIMARY KEY,
        maDichVu      NVARCHAR(20)  NOT NULL,
        maPhieuDatPhong NVARCHAR(20) NOT NULL,
        maNhanVien    NVARCHAR(20)  NULL,
        thoiGianSua   DATETIME      NOT NULL DEFAULT GETDATE(),
        hanhDong      NVARCHAR(10)  NOT NULL,  -- 'SUA' | 'XOA'
        giaTriCu      NVARCHAR(50)  NOT NULL,
        giaTriMoi     NVARCHAR(50)  NOT NULL
    );
    PRINT 'Tao bang ChiTietDichVuLog thanh cong.';
END
ELSE
    PRINT 'Bang ChiTietDichVuLog da ton tai, bo qua.';
