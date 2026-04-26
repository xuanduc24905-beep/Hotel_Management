package com.lotuslaverne.entity;

public class ThietBi {
    private String maThietBi;
    private String tenThietBi;
    private String loaiThietBi;
    private int soLuong;
    private double donGia;
    private String trangThai;

    public ThietBi() {
    }

    public ThietBi(String maThietBi, String tenThietBi, String loaiThietBi, int soLuong, double donGia, String trangThai) {
        this.maThietBi = maThietBi;
        this.tenThietBi = tenThietBi;
        this.loaiThietBi = loaiThietBi;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.trangThai = trangThai;
    }

    public String getMaThietBi() { return maThietBi; }
    public void setMaThietBi(String maThietBi) { this.maThietBi = maThietBi; }
    public String getTenThietBi() { return tenThietBi; }
    public void setTenThietBi(String tenThietBi) { this.tenThietBi = tenThietBi; }
    public String getLoaiThietBi() { return loaiThietBi; }
    public void setLoaiThietBi(String loaiThietBi) { this.loaiThietBi = loaiThietBi; }
    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
