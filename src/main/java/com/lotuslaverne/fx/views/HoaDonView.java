package com.lotuslaverne.fx.views;

import com.lotuslaverne.dao.HoaDonDAO;
import com.lotuslaverne.entity.HoaDon;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HoaDonView {

    private static final Object[][] DEMO_HD = {
        {"HD001","24/04/2026 08:00","NV001","PDP001","24/04/2026 10:00","100.000","850.000","Tiền Mặt","Checkout"},
        {"HD002","23/04/2026 11:30","NV002","PDP002","23/04/2026 12:00","200.000","1.500.000","Chuyển Khoản","Checkout"},
        {"HD003","22/04/2026 09:00","NV001","PDP003","22/04/2026 09:30","0","750.000","Tiền Mặt",""},
        {"HD004","21/04/2026 14:00","NV003","PDP004","21/04/2026 15:00","300.000","2.700.000","Chuyển Khoản","Checkout"},
    };

    private ObservableList<Object[]> items;
    private TableView<Object[]> table;
    private Label revLbl;

    public Node build() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #F0F2F5;");

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #F0F2F5; -fx-border-color: transparent;");

        VBox content = new VBox(20);
        content.setPadding(new Insets(28));
        content.setStyle("-fx-background-color: #F0F2F5;");

        VBox header = new VBox(4);
        Label title = new Label("Quản Lý Hóa Đơn");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Label sub = new Label("Tra cứu và quản lý hóa đơn thanh toán — dữ liệu thật từ cơ sở dữ liệu");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #8C8C8C;");
        header.getChildren().addAll(title, sub);

        items = FXCollections.observableArrayList(loadData());

        content.getChildren().addAll(header, buildToolbar(), buildTable());
        scroll.setContent(content);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        root.getChildren().add(scroll);
        return root;
    }

    private HBox buildToolbar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(14, 16, 14, 16));
        bar.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        TextField search = new TextField();
        search.setPromptText("🔍  Tìm mã HD, mã phiếu, phương thức...");
        search.setPrefWidth(300);
        search.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #E8E8E8;"
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 1;"
                + "-fx-padding: 8 12; -fx-font-size: 13px;");

        // Lọc live
        search.textProperty().addListener((obs, o, n) -> {
            if (n == null || n.trim().isEmpty()) {
                table.setItems(items);
            } else {
                String kw = n.toLowerCase();
                ObservableList<Object[]> filtered = FXCollections.observableArrayList();
                for (Object[] r : items) {
                    for (Object cell : r) {
                        if (cell != null && cell.toString().toLowerCase().contains(kw)) {
                            filtered.add(r); break;
                        }
                    }
                }
                table.setItems(filtered);
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        double total = 0;
        for (Object[] r : items) total += parseMoney(r[6].toString());
        revLbl = new Label(String.format("Tổng: %,.0f VNĐ", total));
        revLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #FF4D4F;");

        Button btnPrint = new Button("🖨  In Hóa Đơn");
        btnPrint.setStyle("-fx-background-color: #1890FF; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-border-radius: 8;"
                + "-fx-padding: 8 14; -fx-cursor: hand; -fx-font-weight: bold;");
        btnPrint.setOnAction(e -> handlePrint());

        Button btnExportHtml = new Button("📄  Xuất HTML");
        btnExportHtml.setStyle("-fx-background-color: #52C41A; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-border-radius: 8;"
                + "-fx-padding: 8 14; -fx-cursor: hand; -fx-font-weight: bold;");
        btnExportHtml.setOnAction(e -> handleExportHtml());

        Button btnRefresh = new Button("↻  Làm Mới");
        btnRefresh.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #595959;"
                + "-fx-background-radius: 8; -fx-border-color: #D9D9D9; -fx-border-width: 1;"
                + "-fx-border-radius: 8; -fx-padding: 8 14; -fx-cursor: hand;");
        btnRefresh.setOnAction(e -> {
            items.setAll(loadData());
            table.setItems(items);
            double t2 = 0;
            for (Object[] r : items) t2 += parseMoney(r[6].toString());
            revLbl.setText(String.format("Tổng: %,.0f VNĐ", t2));
            com.lotuslaverne.fx.UiUtils.flashButton(btnRefresh, "✓ Đã làm mới");
        });

        Button btnPdf = new Button("📄  Xuất PDF");
        btnPdf.setStyle("-fx-background-color: #FA541C; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-border-radius: 8;"
                + "-fx-padding: 8 14; -fx-cursor: hand; -fx-font-weight: bold;");
        btnPdf.setOnAction(e -> handleExportPdf());

        bar.getChildren().addAll(search, spacer, revLbl, btnPdf, btnPrint, btnExportHtml, btnRefresh);
        return bar;
    }

    // ─── In hóa đơn: xuất HTML rồi mở trình duyệt (Ctrl+P để in) ──
    private void handlePrint() {
        Object[] sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng chọn hóa đơn cần in!").showAndWait();
            return;
        }
        try {
            String html = buildInvoiceHtml(sel);
            String fileName = "Print_HoaDon_" + sel[0].toString() + ".html";
            File dir = new File(System.getProperty("user.home"), "LotusLaverne_HoaDon");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, fileName);
            try (FileWriter fw = new FileWriter(file, java.nio.charset.StandardCharsets.UTF_8)) {
                fw.write(html);
            }
            java.awt.Desktop.getDesktop().browse(file.toURI());
            new Alert(Alert.AlertType.INFORMATION,
                    "Hóa đơn đã mở trong trình duyệt!\nNhấn Ctrl+P để in.").showAndWait();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Lỗi: " + ex.getMessage()).showAndWait();
        }
    }

    // ─── Xuất HTML và mở trong trình duyệt ────────────────────────────
    private void handleExportHtml() {
        Object[] sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng chọn hóa đơn cần xuất!").showAndWait();
            return;
        }
        try {
            String html = buildInvoiceHtml(sel);
            String fileName = "HoaDon_" + sel[0].toString() + ".html";
            File dir = new File(System.getProperty("user.home"), "LotusLaverne_HoaDon");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, fileName);
            try (FileWriter fw = new FileWriter(file, java.nio.charset.StandardCharsets.UTF_8)) {
                fw.write(html);
            }
            // Mở file trong trình duyệt mặc định
            java.awt.Desktop.getDesktop().browse(file.toURI());
            new Alert(Alert.AlertType.INFORMATION,
                    "Đã xuất hóa đơn!\n📁 " + file.getAbsolutePath()
                    + "\n\nFile đã mở trong trình duyệt.\nDùng Ctrl+P để in.").showAndWait();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Lỗi xuất file: " + ex.getMessage()).showAndWait();
        }
    }

    // ─── Tạo nội dung HTML hóa đơn đẹp ────────────────────────────────
    private String buildInvoiceHtml(Object[] row) {
        String maHD       = row[0] != null ? row[0].toString() : "";
        String ngayLap    = row[1] != null ? row[1].toString() : "";
        String nvLap      = row[2] != null ? row[2].toString() : "";
        String maPDP      = row[3] != null ? row[3].toString() : "";
        String ngayTT     = row[4] != null ? row[4].toString() : "";
        String tienKM     = row[5] != null ? row[5].toString() : "0";
        String thanhTien  = row[6] != null ? row[6].toString() : "0";
        String phuongThuc = row[7] != null ? row[7].toString() : "";
        String ghiChu     = row.length > 8 && row[8] != null ? row[8].toString() : "";
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        return """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
            <meta charset="UTF-8">
            <title>Hóa Đơn %s</title>
            <style>
              @media print { body { margin: 0; } .no-print { display: none !important; } }
              * { margin: 0; padding: 0; box-sizing: border-box; }
              body { font-family: 'Segoe UI', Arial, sans-serif; background: #f5f5f5; padding: 20px; }
              .invoice { max-width: 700px; margin: 0 auto; background: #fff; border-radius: 12px;
                         box-shadow: 0 2px 12px rgba(0,0,0,0.1); overflow: hidden; }
              .header { background: linear-gradient(135deg, #1890FF, #0DCAF0); color: white;
                        padding: 30px 40px; text-align: center; }
              .header h1 { font-size: 26px; margin-bottom: 4px; }
              .header p { font-size: 13px; opacity: 0.9; }
              .badge { display: inline-block; background: rgba(255,255,255,0.25); padding: 4px 16px;
                       border-radius: 20px; font-size: 14px; font-weight: bold; margin-top: 10px; }
              .body { padding: 30px 40px; }
              .info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 24px; }
              .info-item label { display: block; font-size: 11px; color: #8c8c8c; text-transform: uppercase;
                                 letter-spacing: 0.5px; margin-bottom: 4px; }
              .info-item span { font-size: 14px; font-weight: 600; color: #1a1a2e; }
              .divider { border: none; border-top: 1px dashed #e8e8e8; margin: 20px 0; }
              .total-section { background: #f9f9f9; border-radius: 8px; padding: 20px; margin: 20px 0; }
              .total-row { display: flex; justify-content: space-between; padding: 8px 0;
                           font-size: 14px; color: #595959; }
              .total-row.grand { font-size: 20px; font-weight: bold; color: #FF4D4F;
                                 border-top: 2px solid #e8e8e8; padding-top: 12px; margin-top: 8px; }
              .footer { text-align: center; padding: 20px 40px 30px; color: #8c8c8c; font-size: 12px; }
              .print-btn { display: block; margin: 20px auto; padding: 12px 40px; background: #1890FF;
                           color: white; border: none; border-radius: 8px; font-size: 16px;
                           font-weight: bold; cursor: pointer; }
              .print-btn:hover { background: #40A9FF; }
            </style>
            </head>
            <body>
            <div class="invoice">
              <div class="header">
                <h1>LOTUS LAVERNE HOTEL</h1>
                <p>123 Nguyễn Huệ, Q.1, TP.HCM — ĐT: (028) 3822 1234</p>
                <div class="badge">HÓA ĐƠN THANH TOÁN</div>
              </div>
              <div class="body">
                <div class="info-grid">
                  <div class="info-item"><label>Mã hóa đơn</label><span>%s</span></div>
                  <div class="info-item"><label>Ngày lập</label><span>%s</span></div>
                  <div class="info-item"><label>Nhân viên lập</label><span>%s</span></div>
                  <div class="info-item"><label>Mã phiếu đặt phòng</label><span>%s</span></div>
                  <div class="info-item"><label>Ngày thanh toán</label><span>%s</span></div>
                  <div class="info-item"><label>Phương thức</label><span>%s</span></div>
                </div>
                <hr class="divider">
                <div class="total-section">
                  <div class="total-row"><span>Tiền khuyến mãi giảm:</span><span>- %s VNĐ</span></div>
                  <div class="total-row grand"><span>THÀNH TIỀN:</span><span>%s VNĐ</span></div>
                </div>
                %s
              </div>
              <div class="footer">
                <p>Cảm ơn quý khách đã sử dụng dịch vụ!</p>
                <p style="margin-top:6px;">In lúc: %s</p>
              </div>
            </div>
            <button class="print-btn no-print" onclick="window.print()">🖨  In Hóa Đơn (Ctrl+P)</button>
            </body>
            </html>
            """.formatted(maHD, maHD, ngayLap, nvLap, maPDP, ngayTT, phuongThuc,
                          tienKM, thanhTien,
                          ghiChu.isEmpty() ? "" : "<p style='color:#8c8c8c;font-size:13px;'>📝 Ghi chú: " + ghiChu + "</p>",
                          now);
    }

    private TableView<Object[]> buildTable() {
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-border-radius: 10; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        String[] headers = {
            "Mã Hóa Đơn", "Ngày Lập", "NV Lập", "Mã Phiếu ĐP",
            "Ngày TT", "Tiền KM (VNĐ)", "Thành Tiền (VNĐ)", "Phương Thức", "Ghi Chú"
        };
        for (int i = 0; i < headers.length; i++) {
            final int idx = i;
            TableColumn<Object[], String> col = new TableColumn<>(headers[i]);
            col.setCellValueFactory(p -> {
                Object v = idx < p.getValue().length ? p.getValue()[idx] : "";
                return new SimpleStringProperty(v != null ? v.toString() : "");
            });
            // Mã HD — in đậm xanh
            if (i == 0) {
                col.setCellFactory(tc -> new TableCell<>() {
                    @Override protected void updateItem(String s, boolean empty) {
                        super.updateItem(s, empty);
                        setText(empty || s == null ? null : s);
                        setStyle(empty ? "" : "-fx-font-weight: bold; -fx-text-fill: #1890FF;");
                    }
                });
            }
            // Phương thức TT — badge màu + tiếng Việt
            if (i == 7) {
                col.setCellFactory(tc -> new TableCell<>() {
                    @Override protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) { setGraphic(null); setText(null); return; }
                        // Hiển thị tiếng Việt đẹp
                        String display = "TienMat".equals(item) ? "Tiền Mặt"
                                       : "ChuyenKhoan".equals(item) ? "Chuyển Khoản"
                                       : item;
                        boolean isCash = "TienMat".equals(item) || "Tiền Mặt".equals(item);
                        Label badge = new Label(display);
                        badge.setStyle("-fx-background-color: " + (isCash ? "#F6FFED" : "#E6F4FF")
                                + "; -fx-text-fill: " + (isCash ? "#52C41A" : "#1890FF")
                                + "; -fx-padding: 2 8; -fx-background-radius: 10;"
                                + " -fx-font-size: 11px; -fx-font-weight: bold;");
                        setGraphic(badge); setText(null);
                    }
                });
            }
            table.getColumns().add(col);
        }

        table.setItems(items);
        table.setPlaceholder(new Label("Không có hóa đơn nào."));
        return table;
    }

    private List<Object[]> loadData() {
        List<Object[]> result = new ArrayList<>();
        try {
            HoaDonDAO dao = new HoaDonDAO();
            List<HoaDon> list = dao.getAll();
            if (!list.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                DecimalFormat df = new DecimalFormat("#,###");
                for (HoaDon hd : list) {
                    // Dịch phương thức sang tiếng Việt khi hiển thị
                    String phuongThuc = switch (hd.getPhuongThucThanhToan() != null
                            ? hd.getPhuongThucThanhToan() : "") {
                        case "TienMat"    -> "Tiền Mặt";
                        case "ChuyenKhoan"-> "Chuyển Khoản";
                        default -> hd.getPhuongThucThanhToan() != null
                                ? hd.getPhuongThucThanhToan() : "—";
                    };
                    result.add(new Object[]{
                        hd.getMaHoaDon(),
                        hd.getNgayLap()       != null ? sdf.format(hd.getNgayLap())       : "—",
                        hd.getMaNhanVienLap(),
                        hd.getMaPhieuDatPhong(),
                        hd.getNgayThanhToan() != null ? sdf.format(hd.getNgayThanhToan()) : "—",
                        df.format(hd.getTienKhuyenMai()),
                        df.format(hd.getTienThanhToan()),
                        phuongThuc,
                        hd.getGhiChu() != null ? hd.getGhiChu() : ""
                    });
                }
                return result;
            }
        } catch (Exception ignored) {}
        for (Object[] r : DEMO_HD) result.add(r);
        return result;
    }

    private double parseMoney(String s) {
        try { return Double.parseDouble(s.replaceAll("[^0-9.]", "")); }
        catch (Exception e) { return 0; }
    }

    // ─── Xuất PDF từ hóa đơn đã lưu trong DB ───────────────────────
    private void handleExportPdf() {
        Object[] sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng chọn hóa đơn cần xuất PDF!").showAndWait();
            return;
        }
        String maHD = sel[0].toString();
        try {
            Connection con = com.lotuslaverne.util.ConnectDB.getInstance().getConnection();
            if (con == null) {
                new Alert(Alert.AlertType.ERROR, "Không kết nối được CSDL!").showAndWait(); return;
            }

            // Query thông tin chi tiết hóa đơn
            String sql = "SELECT hd.maHoaDon, hd.maPhieuDatPhong, hd.tienKhuyenMai, hd.tienThanhToan, "
                + "hd.phuongThucThanhToan, hd.ghiChu, hd.maNhanVienLap, "
                + "kh.hoTenKH, ct.maPhong, p.tenPhong, lp.tenLoaiPhong, ct.donGia, "
                + "pdp.thoiGianNhanThucTe, pdp.thoiGianNhanDuKien, pdp.thoiGianTraThucTe "
                + "FROM HoaDon hd "
                + "JOIN PhieuDatPhong pdp ON hd.maPhieuDatPhong = pdp.maPhieuDatPhong "
                + "JOIN KhachHang kh ON pdp.maKhachHang = kh.maKH "
                + "JOIN ChiTietPhieuDatPhong ct ON pdp.maPhieuDatPhong = ct.maPhieuDatPhong "
                + "JOIN Phong p ON ct.maPhong = p.maPhong "
                + "JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong "
                + "WHERE hd.maHoaDon = ?";

            String maPDP = "", tenKH = "Khách lẻ", maPhong = "", tenPhong = "", loaiPhong = "";
            String tgNhan = "", tgTra = "", tenNV = "";
            long soNgay = 1; double donGia = 0, tamTinhPhong = 0, khuyenMai = 0, tongTien = 0;
            String phuongThuc = "", ghiChu = "";
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, maHD);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        maPDP = rs.getString("maPhieuDatPhong");
                        tenKH = rs.getString("hoTenKH");
                        maPhong = rs.getString("maPhong");
                        tenPhong = rs.getString("tenPhong");
                        loaiPhong = rs.getString("tenLoaiPhong");
                        donGia = rs.getDouble("donGia");
                        khuyenMai = rs.getDouble("tienKhuyenMai");
                        tongTien = rs.getDouble("tienThanhToan");
                        phuongThuc = rs.getString("phuongThucThanhToan");
                        ghiChu = rs.getString("ghiChu") != null ? rs.getString("ghiChu") : "";
                        tenNV = rs.getString("maNhanVienLap");

                        java.sql.Timestamp tsNhan = rs.getTimestamp("thoiGianNhanThucTe");
                        if (tsNhan == null) tsNhan = rs.getTimestamp("thoiGianNhanDuKien");
                        java.sql.Timestamp tsTra = rs.getTimestamp("thoiGianTraThucTe");
                        tgNhan = tsNhan != null ? sdf.format(tsNhan) : "N/A";
                        tgTra = tsTra != null ? sdf.format(tsTra) : "N/A";
                        if (tsNhan != null && tsTra != null) {
                            long ms = tsTra.getTime() - tsNhan.getTime();
                            soNgay = Math.max(1, ms / (24 * 3600000));
                        }
                        tamTinhPhong = donGia * soNgay;
                    }
                }
            }

            // Query dịch vụ phát sinh
            double phatSinhDV = 0;
            List<Object[]> dvList = new ArrayList<>();
            String sqlDV = "SELECT dv.tenDichVu, ctdv.soLuong, dv.donGia, (ctdv.soLuong * dv.donGia) AS thanhTien "
                + "FROM ChiTietDichVu ctdv JOIN DichVu dv ON ctdv.maDichVu = dv.maDichVu "
                + "WHERE ctdv.maPhieuDatPhong = ?";
            try (PreparedStatement pst2 = con.prepareStatement(sqlDV)) {
                pst2.setString(1, maPDP);
                try (ResultSet rs2 = pst2.executeQuery()) {
                    DecimalFormat df = new DecimalFormat("#,###");
                    while (rs2.next()) {
                        double tt = rs2.getDouble("thanhTien");
                        phatSinhDV += tt;
                        dvList.add(new Object[]{
                            rs2.getString("tenDichVu"),
                            String.valueOf(rs2.getInt("soLuong")),
                            df.format(rs2.getDouble("donGia")),
                            df.format(tt)
                        });
                    }
                }
            }

            // Hiển FileChooser để chọn nơi lưu
            javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
            fc.setTitle("Lưu Hóa Đơn PDF");
            fc.setInitialFileName(maHD + ".pdf");
            fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            fc.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
            File file = fc.showSaveDialog(null);
            if (file == null) return;

            // Map phương thức sang tiếng Việt
            String ptDisplay = "TienMat".equals(phuongThuc) ? "Tiền Mặt"
                             : "ChuyenKhoan".equals(phuongThuc) ? "Chuyển Khoản" : phuongThuc;

            com.lotuslaverne.util.PdfExporter.xuatHoaDon(
                file.getAbsolutePath(), maHD, maPDP,
                tenKH, maPhong, tenPhong, loaiPhong, tgNhan, tgTra, soNgay, donGia,
                tamTinhPhong, dvList, phatSinhDV, khuyenMai, tongTien, ptDisplay, tenNV);

            if (java.awt.Desktop.isDesktopSupported()) java.awt.Desktop.getDesktop().open(file);

            new Alert(Alert.AlertType.INFORMATION,
                    "✅ Xuất PDF thành công!\n📁 " + file.getAbsolutePath()).showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Lỗi xuất PDF: " + ex.getMessage()).showAndWait();
        }
    }
}
