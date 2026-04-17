package com.lotuslaverne.entity;

import java.sql.Date;

public class BangGia {
    private String maBangGia;
    private String maLoaiPhong;
    private String loaiThue;
    private double donGia;
    private Date ngayBatDau;
    private Date ngayKetThuc;

    public BangGia() {}

    public BangGia(String maBangGia, String maLoaiPhong, String loaiThue,
                   double donGia, Date ngayBatDau, Date ngayKetThuc) {
        this.maBangGia = maBangGia;
        this.maLoaiPhong = maLoaiPhong;
        this.loaiThue = loaiThue;
        this.donGia = donGia;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
    }

    public String getMaBangGia() { return maBangGia; }
    public void setMaBangGia(String v) { this.maBangGia = v; }
    public String getMaLoaiPhong() { return maLoaiPhong; }
    public void setMaLoaiPhong(String v) { this.maLoaiPhong = v; }
    public String getLoaiThue() { return loaiThue; }
    public void setLoaiThue(String v) { this.loaiThue = v; }
    public double getDonGia() { return donGia; }
    public void setDonGia(double v) { this.donGia = v; }
    public Date getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(Date v) { this.ngayBatDau = v; }
    public Date getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(Date v) { this.ngayKetThuc = v; }
}
