package com.lotuslaverne.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PdfExporter {
    private static final DecimalFormat DF = new DecimalFormat("#,###");
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static final BaseColor PRIMARY = new BaseColor(24, 144, 255);
    private static final BaseColor DARK = new BaseColor(26, 26, 46);
    private static final BaseColor GRAY = new BaseColor(140, 140, 140);
    private static final BaseColor LIGHT_BG = new BaseColor(240, 242, 245);
    private static final BaseColor RED = new BaseColor(255, 77, 79);
    private static final BaseColor GREEN = new BaseColor(82, 196, 26);

    private static Font titleFont()  { return new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, PRIMARY); }
    private static Font headerFont() { return new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE); }
    private static Font labelFont()  { return new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, DARK); }
    private static Font valueFont()  { return new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, DARK); }
    private static Font smallFont()  { return new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, GRAY); }
    private static Font totalFont()  { return new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, RED); }
    private static Font greenFont()  { return new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, GREEN); }

    public static void xuatHoaDon(String filePath, String maHD, String maPDP,
            String tenKH, String maPhong, String tenPhong, String loaiPhong,
            String tgNhan, String tgTra, long soNgay, double donGiaPhong, double tamTinhPhong,
            List<Object[]> dichVuItems, double phatSinhDV, double khuyenMai, double tongTien,
            String phuongThuc, String tenNV) throws Exception {

        Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(filePath));
        doc.open();

        // Header
        PdfPTable header = new PdfPTable(2); header.setWidthPercentage(100); header.setWidths(new float[]{60, 40});
        PdfPCell left = new PdfPCell(); left.setBorder(Rectangle.NO_BORDER); left.setPaddingBottom(10);
        left.addElement(new Paragraph("LOTUS LAVERNE HOTEL", titleFont()));
        left.addElement(new Paragraph("123 Nguyen Hue, Q.1, TP.HCM", smallFont()));
        left.addElement(new Paragraph("Tel: (028) 3822 1234  |  info@lotuslaverne.vn", smallFont()));
        header.addCell(left);
        PdfPCell right = new PdfPCell(); right.setBorder(Rectangle.NO_BORDER); right.setPaddingBottom(10);
        Paragraph p1 = new Paragraph("HOA DON THANH TOAN", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, DARK)); p1.setAlignment(Element.ALIGN_RIGHT); right.addElement(p1);
        Paragraph p2 = new Paragraph("So: " + maHD, labelFont()); p2.setAlignment(Element.ALIGN_RIGHT); right.addElement(p2);
        Paragraph p3 = new Paragraph("Ngay: " + SDF.format(new Date()), smallFont()); p3.setAlignment(Element.ALIGN_RIGHT); right.addElement(p3);
        header.addCell(right);
        doc.add(header);

        // Blue line
        PdfPTable sep = new PdfPTable(1); sep.setWidthPercentage(100);
        PdfPCell sc = new PdfPCell(); sc.setBorder(Rectangle.NO_BORDER); sc.setFixedHeight(2); sc.setBackgroundColor(PRIMARY); sep.addCell(sc); sep.setSpacingAfter(15);
        doc.add(sep);

        // Info grid
        PdfPTable info = new PdfPTable(4); info.setWidthPercentage(100); info.setWidths(new float[]{25,25,25,25}); info.setSpacingAfter(15);
        addInfo(info, "Khach hang:", tenKH, "Ma phieu:", maPDP);
        addInfo(info, "Phong:", maPhong + " - " + tenPhong, "Loai phong:", loaiPhong);
        addInfo(info, "Nhan phong:", tgNhan, "Tra phong:", tgTra);
        addInfo(info, "So dem:", String.valueOf(soNgay), "Don gia/dem:", DF.format(donGiaPhong) + " VND");
        doc.add(info);

        // Room table
        PdfPTable rt = new PdfPTable(4); rt.setWidthPercentage(100); rt.setWidths(new float[]{40,15,20,25}); rt.setSpacingAfter(5);
        addTH(rt, new String[]{"Hang muc", "So luong", "Don gia (VND)", "Thanh tien (VND)"});
        addTR(rt, new String[]{"Tien phong - " + loaiPhong, soNgay + " dem", DF.format(donGiaPhong), DF.format(tamTinhPhong)}, false);
        doc.add(rt);

        // Services
        if (dichVuItems != null && !dichVuItems.isEmpty()) {
            Paragraph dvT = new Paragraph("Dich vu phat sinh:", labelFont()); dvT.setSpacingBefore(10); dvT.setSpacingAfter(5); doc.add(dvT);
            PdfPTable dt = new PdfPTable(4); dt.setWidthPercentage(100); dt.setWidths(new float[]{40,15,20,25}); dt.setSpacingAfter(5);
            addTH(dt, new String[]{"Dich vu", "SL", "Don gia (VND)", "Thanh tien (VND)"});
            boolean alt = false;
            for (Object[] item : dichVuItems) { addTR(dt, new String[]{s(item,0),s(item,1),s(item,2),s(item,3)}, alt); alt = !alt; }
            doc.add(dt);
        }

        // Summary
        PdfPTable sum = new PdfPTable(2); sum.setWidthPercentage(50); sum.setHorizontalAlignment(Element.ALIGN_RIGHT); sum.setWidths(new float[]{55,45}); sum.setSpacingBefore(15);
        addSR(sum, "Tam tinh phong:", DF.format(tamTinhPhong) + " VND", labelFont(), valueFont());
        addSR(sum, "Dich vu phat sinh:", DF.format(phatSinhDV) + " VND", labelFont(), valueFont());
        if (khuyenMai > 0) addSR(sum, "Khuyen mai:", "- " + DF.format(khuyenMai) + " VND", labelFont(), greenFont());

        PdfPCell tl = new PdfPCell(new Phrase("TONG TIEN:", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, DARK)));
        tl.setBorder(Rectangle.TOP); tl.setBorderColorTop(DARK); tl.setBorderWidthTop(1.5f); tl.setPadding(8); sum.addCell(tl);
        PdfPCell tv = new PdfPCell(new Phrase(DF.format(tongTien) + " VND", totalFont()));
        tv.setBorder(Rectangle.TOP); tv.setBorderColorTop(DARK); tv.setBorderWidthTop(1.5f); tv.setPadding(8); tv.setHorizontalAlignment(Element.ALIGN_RIGHT); sum.addCell(tv);
        doc.add(sum);

        // Footer
        Paragraph ft = new Paragraph(); ft.setSpacingBefore(30);
        ft.add(new Chunk("Phuong thuc: ", labelFont())); ft.add(new Chunk(phuongThuc + "\n", valueFont()));
        ft.add(new Chunk("Nhan vien lap: ", labelFont())); ft.add(new Chunk(tenNV + "\n\n", valueFont()));
        ft.add(new Chunk("Cam on quy khach da su dung dich vu!\n", new Font(Font.FontFamily.HELVETICA, 11, Font.ITALIC, PRIMARY)));
        ft.add(new Chunk("LOTUS LAVERNE HOTEL - www.lotuslaverne.vn", smallFont()));
        doc.add(ft);

        doc.close(); writer.close();
    }

    private static void addInfo(PdfPTable t, String l1, String v1, String l2, String v2) {
        t.addCell(ic(l1, labelFont())); t.addCell(ic(v1, valueFont())); t.addCell(ic(l2, labelFont())); t.addCell(ic(v2, valueFont()));
    }
    private static PdfPCell ic(String txt, Font f) { PdfPCell c = new PdfPCell(new Phrase(txt, f)); c.setBorder(Rectangle.NO_BORDER); c.setPadding(4); return c; }
    private static void addTH(PdfPTable t, String[] h) { for (String s : h) { PdfPCell c = new PdfPCell(new Phrase(s, headerFont())); c.setBackgroundColor(PRIMARY); c.setPadding(6); c.setHorizontalAlignment(Element.ALIGN_CENTER); c.setBorder(Rectangle.NO_BORDER); t.addCell(c); } }
    private static void addTR(PdfPTable t, String[] v, boolean alt) { BaseColor bg = alt ? LIGHT_BG : BaseColor.WHITE; for (int i = 0; i < v.length; i++) { PdfPCell c = new PdfPCell(new Phrase(v[i], valueFont())); c.setBackgroundColor(bg); c.setPadding(5); c.setHorizontalAlignment(i == 0 ? Element.ALIGN_LEFT : Element.ALIGN_CENTER); c.setBorderColor(new BaseColor(230,230,230)); c.setBorderWidth(0.5f); t.addCell(c); } }
    private static void addSR(PdfPTable t, String l, String v, Font lf, Font vf) { PdfPCell c1 = new PdfPCell(new Phrase(l, lf)); c1.setBorder(Rectangle.NO_BORDER); c1.setPadding(4); t.addCell(c1); PdfPCell c2 = new PdfPCell(new Phrase(v, vf)); c2.setBorder(Rectangle.NO_BORDER); c2.setPadding(4); c2.setHorizontalAlignment(Element.ALIGN_RIGHT); t.addCell(c2); }
    private static String s(Object[] a, int i) { return i < a.length && a[i] != null ? a[i].toString() : ""; }
}
