package com.lotuslaverne.entity;

public class NhanVien {
    private String maNhanVien;
    private String tenNhanVien;
    private String soDienThoai;
    private String vaiTro;
    private String caLamViec;  // Sang | Chieu | Dem | HanhChinh
    private String cccd;
    private String email;
    private String diaChi;
    private java.sql.Date ngaySinh;
    private java.sql.Date ngayBatDauLam;
    private java.sql.Date ngayKetThucHopDong;

    public NhanVien() {}

    public NhanVien(String maNhanVien, String tenNhanVien, String soDienThoai, String vaiTro) {
        this.maNhanVien = maNhanVien;
        this.tenNhanVien = tenNhanVien;
        this.soDienThoai = soDienThoai;
        this.vaiTro = vaiTro;
    }

    public NhanVien(String maNhanVien, String tenNhanVien, String soDienThoai, String vaiTro, String caLamViec) {
        this(maNhanVien, tenNhanVien, soDienThoai, vaiTro);
        this.caLamViec = caLamViec;
    }

    public String getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(String maNhanVien) { this.maNhanVien = maNhanVien; }
    public String getTenNhanVien() { return tenNhanVien; }
    public void setTenNhanVien(String tenNhanVien) { this.tenNhanVien = tenNhanVien; }
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }
    public String getCaLamViec() { return caLamViec; }
    public void setCaLamViec(String caLamViec) { this.caLamViec = caLamViec; }
    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    public java.sql.Date getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(java.sql.Date ngaySinh) { this.ngaySinh = ngaySinh; }
    public java.sql.Date getNgayBatDauLam() { return ngayBatDauLam; }
    public void setNgayBatDauLam(java.sql.Date ngayBatDauLam) { this.ngayBatDauLam = ngayBatDauLam; }
    public java.sql.Date getNgayKetThucHopDong() { return ngayKetThucHopDong; }
    public void setNgayKetThucHopDong(java.sql.Date ngayKetThucHopDong) { this.ngayKetThucHopDong = ngayKetThucHopDong; }
}
