package com.lotuslaverne.entity;

import java.sql.Timestamp;

public class PhieuDatPhong {
    private String maPhieuDatPhong;
    private Timestamp ngayDat;
    private String maKhachHang;
    private String maNhanVien;
    private int soNguoi;
    private Timestamp thoiGianNhanDuKien;
    private Timestamp thoiGianNhanThucTe;
    private Timestamp thoiGianTraDuKien;
    private Timestamp thoiGianTraThucTe;
    private String ghiChu;

    public PhieuDatPhong() {}

    public PhieuDatPhong(String maPhieuDatPhong, String maKhachHang, String maNhanVien,
                         int soNguoi, Timestamp thoiGianNhanDuKien,
                         Timestamp thoiGianTraDuKien, String ghiChu) {
        this.maPhieuDatPhong = maPhieuDatPhong;
        this.ngayDat = new Timestamp(System.currentTimeMillis());
        this.maKhachHang = maKhachHang;
        this.maNhanVien = maNhanVien;
        this.soNguoi = soNguoi;
        this.thoiGianNhanDuKien = thoiGianNhanDuKien;
        this.thoiGianTraDuKien = thoiGianTraDuKien;
        this.ghiChu = ghiChu;
    }

    public String getMaPhieuDatPhong() { return maPhieuDatPhong; }
    public void setMaPhieuDatPhong(String v) { this.maPhieuDatPhong = v; }
    public Timestamp getNgayDat() { return ngayDat; }
    public void setNgayDat(Timestamp v) { this.ngayDat = v; }
    public String getMaKhachHang() { return maKhachHang; }
    public void setMaKhachHang(String v) { this.maKhachHang = v; }
    public String getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(String v) { this.maNhanVien = v; }
    public int getSoNguoi() { return soNguoi; }
    public void setSoNguoi(int v) { this.soNguoi = v; }
    public Timestamp getThoiGianNhanDuKien() { return thoiGianNhanDuKien; }
    public void setThoiGianNhanDuKien(Timestamp v) { this.thoiGianNhanDuKien = v; }
    public Timestamp getThoiGianNhanThucTe() { return thoiGianNhanThucTe; }
    public void setThoiGianNhanThucTe(Timestamp v) { this.thoiGianNhanThucTe = v; }
    public Timestamp getThoiGianTraDuKien() { return thoiGianTraDuKien; }
    public void setThoiGianTraDuKien(Timestamp v) { this.thoiGianTraDuKien = v; }
    public Timestamp getThoiGianTraThucTe() { return thoiGianTraThucTe; }
    public void setThoiGianTraThucTe(Timestamp v) { this.thoiGianTraThucTe = v; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String v) { this.ghiChu = v; }
}
