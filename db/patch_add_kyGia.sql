-- ============================================================
-- PATCH: Thêm cột kyGia vào bảng BangGia
-- Chạy trên DB QuanLyKhachSan đã có sẵn (không cần drop/recreate)
-- ============================================================

USE [QuanLyKhachSan]
GO

-- 1. Thêm cột kyGia nếu chưa tồn tại
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='BangGia' AND COLUMN_NAME='kyGia')
BEGIN
    ALTER TABLE BangGia ADD kyGia NVARCHAR(15) NOT NULL DEFAULT N'NgayThuong';
    ALTER TABLE BangGia ADD CONSTRAINT CHK_BangGia_KyGia CHECK (kyGia IN (N'NgayThuong', N'CuoiTuan', N'LeTet', N'CaoDiem'));
    PRINT N'Đã thêm cột kyGia vào BangGia.';
END
ELSE
    PRINT N'Cột kyGia đã tồn tại, bỏ qua.';
GO

-- 2. Xóa dữ liệu cũ và chèn dữ liệu mẫu mới (32 bản ghi)
DELETE FROM BangGia;
GO

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
GO

PRINT N'✅ Patch hoàn tất: 32 bản ghi bảng giá với 4 kỳ giá.';
GO
