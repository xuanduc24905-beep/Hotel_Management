package com.lotuslaverne.entity;

public class PhuThu {
    private String maPhuThu;
    private String tenQuyDinh;
    private int soGioLich; // Số giờ vào sớm hoặc ra trễ
    private double phanTramPhuThu;

    public PhuThu() {}

    public PhuThu(String maPhuThu, String tenQuyDinh, int soGioLich, double phanTramPhuThu) {
        this.maPhuThu = maPhuThu;
        this.tenQuyDinh = tenQuyDinh;
        this.soGioLich = soGioLich;
        this.phanTramPhuThu = phanTramPhuThu;
    }

    public String getMaPhuThu() { return maPhuThu; }
    public void setMaPhuThu(String maPhuThu) { this.maPhuThu = maPhuThu; }
    public String getTenQuyDinh() { return tenQuyDinh; }
    public void setTenQuyDinh(String tenQuyDinh) { this.tenQuyDinh = tenQuyDinh; }
    public int getSoGioLich() { return soGioLich; }
    public void setSoGioLich(int soGioLich) { this.soGioLich = soGioLich; }
    public double getPhanTramPhuThu() { return phanTramPhuThu; }
    public void setPhanTramPhuThu(double phanTramPhuThu) { this.phanTramPhuThu = phanTramPhuThu; }
}
