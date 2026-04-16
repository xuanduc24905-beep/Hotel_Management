package com.lotuslaverne.entity;

public class ChiTietPhieuDatPhong {
    private String maPhieuDatPhong;
    private String maPhong;
    private double donGia;

    public ChiTietPhieuDatPhong() {}

    public ChiTietPhieuDatPhong(String maPhieuDatPhong, String maPhong, double donGia) {
        this.maPhieuDatPhong = maPhieuDatPhong;
        this.maPhong = maPhong;
        this.donGia = donGia;
    }

    public String getMaPhieuDatPhong() { return maPhieuDatPhong; }
    public void setMaPhieuDatPhong(String maPhieuDatPhong) { this.maPhieuDatPhong = maPhieuDatPhong; }
    public String getMaPhong() { return maPhong; }
    public void setMaPhong(String maPhong) { this.maPhong = maPhong; }
    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }
}
