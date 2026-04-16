package com.lotuslaverne.entity;

public class KhachHang {
    private String maKH;
    private String hoTenKH;
    private String soDienThoai;
    private String cmnd;

    public KhachHang() {
    }

    public KhachHang(String maKH, String hoTenKH, String soDienThoai, String cmnd) {
        this.maKH = maKH;
        this.hoTenKH = hoTenKH;
        this.soDienThoai = soDienThoai;
        this.cmnd = cmnd;
    }

    public String getMaKH() { return maKH; }
    public void setMaKH(String maKH) { this.maKH = maKH; }
    public String getHoTenKH() { return hoTenKH; }
    public void setHoTenKH(String hoTenKH) { this.hoTenKH = hoTenKH; }
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public String getCmnd() { return cmnd; }
    public void setCmnd(String cmnd) { this.cmnd = cmnd; }
}
