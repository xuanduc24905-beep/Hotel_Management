package com.lotuslaverne.entity;

public class Phong {
    private String maPhong;
    private String tenPhong;
    private String maLoaiPhong;
    private String trangThai;
    // Các trường mới
    private String tienNghi;      // "WiFi,TV,Điều Hòa,Bồn Tắm"
    private int    soNguoiToiDa;  // Sức chứa tối đa
    private String moTa;          // Mô tả phòng

    public Phong() {}

    public Phong(String maPhong, String tenPhong, String maLoaiPhong, String trangThai) {
        this.maPhong      = maPhong;
        this.tenPhong     = tenPhong;
        this.maLoaiPhong  = maLoaiPhong;
        this.trangThai    = trangThai;
        this.soNguoiToiDa = 2;
    }

    // ── Getters & Setters ──
    public String getMaPhong()          { return maPhong; }
    public void   setMaPhong(String v)  { this.maPhong = v; }

    public String getTenPhong()          { return tenPhong; }
    public void   setTenPhong(String v)  { this.tenPhong = v; }

    public String getMaLoaiPhong()          { return maLoaiPhong; }
    public void   setMaLoaiPhong(String v)  { this.maLoaiPhong = v; }

    public String getTrangThai()          { return trangThai; }
    public void   setTrangThai(String v)  { this.trangThai = v; }

    public String getTienNghi()          { return tienNghi; }
    public void   setTienNghi(String v)  { this.tienNghi = v; }

    public int  getSoNguoiToiDa()       { return soNguoiToiDa; }
    public void setSoNguoiToiDa(int v)  { this.soNguoiToiDa = v; }

    public String getMoTa()          { return moTa; }
    public void   setMoTa(String v)  { this.moTa = v; }
}
