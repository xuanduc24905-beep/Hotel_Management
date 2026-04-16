package com.lotuslaverne.entity;

public class BangGia {
    private String maBangGia;
    private String loaiPhong;
    private double giaTheoGio;
    private double giaTheoNgay;

    public BangGia() {}

    public BangGia(String maBangGia, String loaiPhong, double giaTheoGio, double giaTheoNgay) {
        this.maBangGia = maBangGia;
        this.loaiPhong = loaiPhong;
        this.giaTheoGio = giaTheoGio;
        this.giaTheoNgay = giaTheoNgay;
    }

    public String getMaBangGia() { return maBangGia; }
    public void setMaBangGia(String maBangGia) { this.maBangGia = maBangGia; }
    public String getLoaiPhong() { return loaiPhong; }
    public void setLoaiPhong(String loaiPhong) { this.loaiPhong = loaiPhong; }
    public double getGiaTheoGio() { return giaTheoGio; }
    public void setGiaTheoGio(double giaTheoGio) { this.giaTheoGio = giaTheoGio; }
    public double getGiaTheoNgay() { return giaTheoNgay; }
    public void setGiaTheoNgay(double giaTheoNgay) { this.giaTheoNgay = giaTheoNgay; }
}
