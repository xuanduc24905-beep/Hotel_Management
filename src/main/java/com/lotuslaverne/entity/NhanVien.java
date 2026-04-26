package com.lotuslaverne.entity;

public class NhanVien {
    private String maNhanVien;
    private String tenNhanVien;
    private String soDienThoai;
    private String vaiTro;
    private String caLamViec;  // Sang | Chieu | Dem | HanhChinh

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
}
