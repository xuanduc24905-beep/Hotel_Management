package com.lotuslaverne.entity;

import java.sql.Timestamp;

public class KhuyenMai {
    private String maKhuyenMai;
    private String tenKhuyenMai;
    private double phanTramGiam;
    private double soTienGiam;
    private Timestamp ngayKetThuc;

    public KhuyenMai() {}

    public KhuyenMai(String maKhuyenMai, String tenKhuyenMai, double phanTramGiam, double soTienGiam, Timestamp ngayKetThuc) {
        this.maKhuyenMai = maKhuyenMai;
        this.tenKhuyenMai = tenKhuyenMai;
        this.phanTramGiam = phanTramGiam;
        this.soTienGiam = soTienGiam;
        this.ngayKetThuc = ngayKetThuc;
    }

    // Getters / Setters
    public String getMaKhuyenMai() { return maKhuyenMai; }
    public void setMaKhuyenMai(String maKhuyenMai) { this.maKhuyenMai = maKhuyenMai; }
    public String getTenKhuyenMai() { return tenKhuyenMai; }
    public void setTenKhuyenMai(String tenKhuyenMai) { this.tenKhuyenMai = tenKhuyenMai; }
    public double getPhanTramGiam() { return phanTramGiam; }
    public void setPhanTramGiam(double phanTramGiam) { this.phanTramGiam = phanTramGiam; }
    public double getSoTienGiam() { return soTienGiam; }
    public void setSoTienGiam(double soTienGiam) { this.soTienGiam = soTienGiam; }
    public Timestamp getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(Timestamp ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }
}
