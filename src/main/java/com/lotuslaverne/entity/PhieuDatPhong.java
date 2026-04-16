package com.lotuslaverne.entity;

import java.sql.Timestamp;

public class PhieuDatPhong {
    private String maPhieuDatPhong;
    private String maNhanVien;
    private String maKhachHang;
    private Timestamp ngayLapPhieu;
    private int soNguoi;

    public PhieuDatPhong() {}

    public PhieuDatPhong(String maPhieuDatPhong, String maNhanVien, String maKhachHang, Timestamp ngayLapPhieu, int soNguoi) {
        this.maPhieuDatPhong = maPhieuDatPhong;
        this.maNhanVien = maNhanVien;
        this.maKhachHang = maKhachHang;
        this.ngayLapPhieu = ngayLapPhieu;
        this.soNguoi = soNguoi;
    }

    public String getMaPhieuDatPhong() { return maPhieuDatPhong; }
    public void setMaPhieuDatPhong(String maPhieuDatPhong) { this.maPhieuDatPhong = maPhieuDatPhong; }
    public String getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(String maNhanVien) { this.maNhanVien = maNhanVien; }
    public String getMaKhachHang() { return maKhachHang; }
    public void setMaKhachHang(String maKhachHang) { this.maKhachHang = maKhachHang; }
    public Timestamp getNgayLapPhieu() { return ngayLapPhieu; }
    public void setNgayLapPhieu(Timestamp ngayLapPhieu) { this.ngayLapPhieu = ngayLapPhieu; }
    public int getSoNguoi() { return soNguoi; }
    public void setSoNguoi(int soNguoi) { this.soNguoi = soNguoi; }
}
