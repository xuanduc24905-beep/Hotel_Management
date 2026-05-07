package com.lotuslaverne.util;

public class SessionContext {

    private static final SessionContext INSTANCE = new SessionContext();

    private String maNhanVien = "NV001";
    private String tenDangNhap = "";
    private String vaiTro = "";

    private SessionContext() {}

    public static SessionContext getInstance() { return INSTANCE; }

    public void init(String maNhanVien, String tenDangNhap, String vaiTro) {
        this.maNhanVien = maNhanVien != null ? maNhanVien : "NV001";
        this.tenDangNhap = tenDangNhap;
        this.vaiTro = vaiTro;
    }

    public String getMaNhanVien() { return maNhanVien; }
    public String getTenDangNhap() { return tenDangNhap; }
    public String getVaiTro()      { return vaiTro; }
}
