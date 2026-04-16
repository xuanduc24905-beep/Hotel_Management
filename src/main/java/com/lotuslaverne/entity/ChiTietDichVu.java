package com.lotuslaverne.entity;

import java.sql.Timestamp;

public class ChiTietDichVu {
    private String maDichVu;
    private String maPhong;
    private int soLuong;
    private double donGia;
    private Timestamp thoiGianGoi;

    public ChiTietDichVu() {}

    public ChiTietDichVu(String maDichVu, String maPhong, int soLuong, double donGia, Timestamp thoiGianGoi) {
        this.maDichVu = maDichVu;
        this.maPhong = maPhong;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thoiGianGoi = thoiGianGoi;
    }

    public String getMaDichVu() { return maDichVu; }
    public void setMaDichVu(String maDichVu) { this.maDichVu = maDichVu; }
    public String getMaPhong() { return maPhong; }
    public void setMaPhong(String maPhong) { this.maPhong = maPhong; }
    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }
    public Timestamp getThoiGianGoi() { return thoiGianGoi; }
    public void setThoiGianGoi(Timestamp thoiGianGoi) { this.thoiGianGoi = thoiGianGoi; }
}
