package com.lotuslaverne.entity;

import java.sql.Timestamp;

public class KhuyenMai {
    private String maKhuyenMai;
    private String tenKhuyenMai;
    private Timestamp ngayApDung;
    private Timestamp ngayKetThuc;
    private double phanTramGiam;
    private String dieuKienApDung;

    public KhuyenMai() {}

    public KhuyenMai(String maKhuyenMai, String tenKhuyenMai, Timestamp ngayApDung,
                     Timestamp ngayKetThuc, double phanTramGiam, String dieuKienApDung) {
        this.maKhuyenMai = maKhuyenMai;
        this.tenKhuyenMai = tenKhuyenMai;
        this.ngayApDung = ngayApDung;
        this.ngayKetThuc = ngayKetThuc;
        this.phanTramGiam = phanTramGiam;
        this.dieuKienApDung = dieuKienApDung;
    }

    public String getMaKhuyenMai() { return maKhuyenMai; }
    public void setMaKhuyenMai(String v) { this.maKhuyenMai = v; }
    public String getTenKhuyenMai() { return tenKhuyenMai; }
    public void setTenKhuyenMai(String v) { this.tenKhuyenMai = v; }
    public Timestamp getNgayApDung() { return ngayApDung; }
    public void setNgayApDung(Timestamp v) { this.ngayApDung = v; }
    public Timestamp getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(Timestamp v) { this.ngayKetThuc = v; }
    public double getPhanTramGiam() { return phanTramGiam; }
    public void setPhanTramGiam(double v) { this.phanTramGiam = v; }
    public String getDieuKienApDung() { return dieuKienApDung; }
    public void setDieuKienApDung(String v) { this.dieuKienApDung = v; }
}
