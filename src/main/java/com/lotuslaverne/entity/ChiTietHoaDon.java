package com.lotuslaverne.entity;

public class ChiTietHoaDon {
    private String maHoaDon;
    private String maNoiDung; // Có thể là Phòng hoặc Dịch Vụ
    private int soLuong;
    private double thanhTien;

    public ChiTietHoaDon() {}

    public ChiTietHoaDon(String maHoaDon, String maNoiDung, int soLuong, double thanhTien) {
        this.maHoaDon = maHoaDon;
        this.maNoiDung = maNoiDung;
        this.soLuong = soLuong;
        this.thanhTien = thanhTien;
    }

    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }
    public String getMaNoiDung() { return maNoiDung; }
    public void setMaNoiDung(String maNoiDung) { this.maNoiDung = maNoiDung; }
    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
    public double getThanhTien() { return thanhTien; }
    public void setThanhTien(double thanhTien) { this.thanhTien = thanhTien; }
}
