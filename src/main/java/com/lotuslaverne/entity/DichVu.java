package com.lotuslaverne.entity;

public class DichVu {
    private String maDichVu;
    private String tenDichVu;
    private String maLoaiDichVu;
    private double donGia;
    private String trangThai;

    public DichVu() {
    }

    public DichVu(String maDichVu, String tenDichVu, String maLoaiDichVu, double donGia, String trangThai) {
        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.maLoaiDichVu = maLoaiDichVu;
        this.donGia = donGia;
        this.trangThai = trangThai;
    }

    public String getMaDichVu() { return maDichVu; }
    public void setMaDichVu(String maDichVu) { this.maDichVu = maDichVu; }
    public String getTenDichVu() { return tenDichVu; }
    public void setTenDichVu(String tenDichVu) { this.tenDichVu = tenDichVu; }
    public String getMaLoaiDichVu() { return maLoaiDichVu; }
    public void setMaLoaiDichVu(String maLoaiDichVu) { this.maLoaiDichVu = maLoaiDichVu; }
    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
