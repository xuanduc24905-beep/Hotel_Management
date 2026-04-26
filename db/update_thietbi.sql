-- SQL Update Script: Add ThietBi table

USE [QuanLyKhachSan]
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'ThietBi')
BEGIN
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

    INSERT INTO ThietBi (maThietBi, tenThietBi, loaiThietBi, soLuong, donGia, trangThai) VALUES
        ('TB001', N'Tivi Samsung 43 inch', N'Điện tử', 20, 5000000, N'Tot'),
        ('TB002', N'Điều hòa Panasonic 1HP', N'Điện lạnh', 25, 8000000, N'Tot'),
        ('TB003', N'Tủ lạnh Mini', N'Điện lạnh', 20, 2500000, N'Tot'),
        ('TB004', N'Giường đôi', N'Nội thất', 15, 3000000, N'Tot'),
        ('TB005', N'Tủ quần áo gỗ', N'Nội thất', 15, 2000000, N'Tot'),
        ('TB006', N'Máy sấy tóc Panasonic', N'Điện tử', 30, 400000, N'Tot');
END
GO
