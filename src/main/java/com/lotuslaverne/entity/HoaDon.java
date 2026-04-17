package com.lotuslaverne.entity;

import java.sql.Timestamp;

public class HoaDon {
    private String maHoaDon;
    private Timestamp ngayLap;
    private String maNhanVienLap;
    private String maPhieuDatPhong;
    private Timestamp ngayThanhToan;
    private double tienKhuyenMai;
    private double tienThanhToan;
    private String phuongThucThanhToan;
    private String ghiChu;

    public HoaDon() {}

    public HoaDon(String maHoaDon, String maNhanVienLap, String maPhieuDatPhong,
                  double tienKhuyenMai, double tienThanhToan,
                  String phuongThucThanhToan, String ghiChu) {
        this.maHoaDon = maHoaDon;
        this.ngayLap = new Timestamp(System.currentTimeMillis());
        this.ngayThanhToan = this.ngayLap;
        this.maNhanVienLap = maNhanVienLap;
        this.maPhieuDatPhong = maPhieuDatPhong;
        this.tienKhuyenMai = tienKhuyenMai;
        this.tienThanhToan = tienThanhToan;
        this.phuongThucThanhToan = phuongThucThanhToan;
        this.ghiChu = ghiChu;
    }

    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String v) { this.maHoaDon = v; }
    public Timestamp getNgayLap() { return ngayLap; }
    public void setNgayLap(Timestamp v) { this.ngayLap = v; }
    public String getMaNhanVienLap() { return maNhanVienLap; }
    public void setMaNhanVienLap(String v) { this.maNhanVienLap = v; }
    public String getMaPhieuDatPhong() { return maPhieuDatPhong; }
    public void setMaPhieuDatPhong(String v) { this.maPhieuDatPhong = v; }
    public Timestamp getNgayThanhToan() { return ngayThanhToan; }
    public void setNgayThanhToan(Timestamp v) { this.ngayThanhToan = v; }
    public double getTienKhuyenMai() { return tienKhuyenMai; }
    public void setTienKhuyenMai(double v) { this.tienKhuyenMai = v; }
    public double getTienThanhToan() { return tienThanhToan; }
    public void setTienThanhToan(double v) { this.tienThanhToan = v; }
    public String getPhuongThucThanhToan() { return phuongThucThanhToan; }
    public void setPhuongThucThanhToan(String v) { this.phuongThucThanhToan = v; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String v) { this.ghiChu = v; }
}
