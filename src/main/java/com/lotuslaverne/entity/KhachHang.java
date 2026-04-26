package com.lotuslaverne.entity;

public class KhachHang {
    private String maKH;
    private String hoTenKH;
    private String soDienThoai;
    private String cmnd;
    // Trường mở rộng (đã có trong DB)
    private String email;
    private String ngaySinh;      // format dd/MM/yyyy
    private String diaChi;
    private String quocTich;
    private boolean gioiTinh;     // true = Nam

    public KhachHang() {}

    /** Constructor tối giản dùng cho tra cứu nhanh */
    public KhachHang(String maKH, String hoTenKH, String soDienThoai, String cmnd) {
        this.maKH        = maKH;
        this.hoTenKH     = hoTenKH;
        this.soDienThoai = soDienThoai;
        this.cmnd        = cmnd;
        this.quocTich    = "Việt Nam";
        this.gioiTinh    = true;
    }

    /** Constructor đầy đủ */
    public KhachHang(String maKH, String hoTenKH, String soDienThoai, String cmnd,
                     String email, String ngaySinh, String diaChi, String quocTich, boolean gioiTinh) {
        this.maKH        = maKH;
        this.hoTenKH     = hoTenKH;
        this.soDienThoai = soDienThoai;
        this.cmnd        = cmnd;
        this.email       = email;
        this.ngaySinh    = ngaySinh;
        this.diaChi      = diaChi;
        this.quocTich    = quocTich != null ? quocTich : "Việt Nam";
        this.gioiTinh    = gioiTinh;
    }

    // ── Getters & Setters ──
    public String getMaKH()              { return maKH; }
    public void   setMaKH(String v)      { this.maKH = v; }

    public String getHoTenKH()           { return hoTenKH; }
    public void   setHoTenKH(String v)   { this.hoTenKH = v; }

    public String getSoDienThoai()           { return soDienThoai; }
    public void   setSoDienThoai(String v)   { this.soDienThoai = v; }

    public String getCmnd()             { return cmnd; }
    public void   setCmnd(String v)     { this.cmnd = v; }

    public String getEmail()            { return email; }
    public void   setEmail(String v)    { this.email = v; }

    public String getNgaySinh()           { return ngaySinh; }
    public void   setNgaySinh(String v)   { this.ngaySinh = v; }

    public String getDiaChi()           { return diaChi; }
    public void   setDiaChi(String v)   { this.diaChi = v; }

    public String getQuocTich()           { return quocTich; }
    public void   setQuocTich(String v)   { this.quocTich = v; }

    public boolean isGioiTinh()         { return gioiTinh; }
    public void    setGioiTinh(boolean v){ this.gioiTinh = v; }
}
