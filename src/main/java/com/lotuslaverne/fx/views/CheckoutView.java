package com.lotuslaverne.fx.views;

import com.lotuslaverne.util.ConnectDB;
import com.lotuslaverne.util.SessionContext;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CheckoutView {

    private static final DecimalFormat FMT = new DecimalFormat("#,###");
    private static final DateTimeFormatter DT  = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private String[] selectedPhieu = null;
    private double tongTienFinal = 0;
    private double tienPhongFinal = 0;
    private double tienDVFinal    = 0;
    private long   soNgayFinal    = 1;
    private Label lblTienThua;
    private Label lblConThuVal;
    private Label lblHTSummaryRow;

    private final String prefillMaPhong;

    public CheckoutView() { this.prefillMaPhong = null; }
    public CheckoutView(String maPhong) { this.prefillMaPhong = maPhong; }

    public Node build() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color:#F0F2F5;");

        VBox hdr = new VBox(4);
        hdr.setPadding(new Insets(24, 28, 10, 28));
        Label title = new Label("Check-out / Thanh Toán");
        title.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:#1A1A2E;");
        Label sub = new Label("Trả phòng và thanh toán toàn bộ chi phí cho khách");
        sub.setStyle("-fx-font-size:13px;-fx-text-fill:#8C8C8C;");
        hdr.getChildren().addAll(title, sub);

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:#F0F2F5;-fx-border-color:transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox content = new VBox(20);
        content.setPadding(new Insets(12, 28, 28, 28));
        content.setStyle("-fx-background-color:#F0F2F5;");

        // ── BƯỚC 1 ──────────────────────────────────────────────────────────
        VBox step1 = buildStep("🛎  Bước 1 — Chọn Phòng Đang Có Khách");
        ObservableList<Object[]> checkinItems = FXCollections.observableArrayList(loadCheckedIn());
        TableView<Object[]> tbl1 = buildCheckinTable(checkinItems);
        tbl1.setPrefHeight(220);

        HBox tb1 = new HBox(12);
        tb1.setAlignment(Pos.CENTER_LEFT);
        TextField s1 = searchField("🔍 Tìm mã phiếu, tên khách, phòng...");
        s1.textProperty().addListener((obs, o, n) -> {
            if (n == null || n.trim().isEmpty()) { tbl1.setItems(checkinItems); return; }
            String kw = n.toLowerCase();
            ObservableList<Object[]> f = FXCollections.observableArrayList();
            for (Object[] r : checkinItems)
                for (Object c : r) if (c != null && c.toString().toLowerCase().contains(kw)) { f.add(r); break; }
            tbl1.setItems(f);
        });
        Button btnRefresh1 = btnSecondary("↻ Làm Mới");
        btnRefresh1.setOnAction(e -> { checkinItems.setAll(loadCheckedIn()); tbl1.setItems(checkinItems); });
        Region sp1 = new Region(); HBox.setHgrow(sp1, Priority.ALWAYS);
        tb1.getChildren().addAll(s1, sp1, btnRefresh1);
        step1.getChildren().addAll(tb1, tbl1);

        if (prefillMaPhong != null) {
            javafx.application.Platform.runLater(() -> {
                for (int i = 0; i < checkinItems.size(); i++) {
                    Object[] r = checkinItems.get(i);
                    if (r.length > 1 && prefillMaPhong.equals(r[1].toString())) {
                        tbl1.getSelectionModel().select(i);
                        tbl1.scrollTo(i);
                        break;
                    }
                }
            });
        }

        // ── BƯỚC 2 ──────────────────────────────────────────────────────────
        VBox step2 = buildStep("💰  Bước 2 — Chi Tiết Hóa Đơn");
        step2.setVisible(false); step2.setManaged(false);

        Label lblPhieu = new Label();
        lblPhieu.setStyle("-fx-background-color:#E6F4FF;-fx-text-fill:#1890FF;-fx-padding:10 16;"
                + "-fx-background-radius:8;-fx-border-color:#91CAFF;-fx-border-width:1;-fx-border-radius:8;"
                + "-fx-font-size:13px;-fx-font-weight:bold;");
        lblPhieu.setMaxWidth(Double.MAX_VALUE);

        TableView<Object[]> tblDv = buildDVTable();
        tblDv.setPrefHeight(160);
        Label lblDvTitle = new Label("📋 Dịch vụ đã sử dụng:");
        lblDvTitle.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#595959;");

        VBox panelTong = new VBox(8);
        panelTong.setPadding(new Insets(16));
        panelTong.setStyle("-fx-background-color:#F8F9FA;-fx-background-radius:10;"
                + "-fx-border-color:#E8E8E8;-fx-border-width:1;-fx-border-radius:10;");
        Label[] lblRows = new Label[5];
        String[] rowNames = {"Tiền phòng:", "Dịch vụ:", "Phụ thu:", "Giảm giá:", "TỔNG THANH TOÁN:"};
        String[] rowVals  = {"—", "—", "—", "0 đ", "—"};
        for (int i = 0; i < 5; i++) {
            HBox row = new HBox();
            Label name = new Label(rowNames[i]);
            name.setStyle("-fx-font-size:" + (i == 4 ? "15" : "13") + "px;"
                    + (i == 4 ? "-fx-font-weight:bold;" : "") + "-fx-text-fill:#595959;");
            lblRows[i] = new Label(rowVals[i]);
            lblRows[i].setStyle("-fx-font-size:" + (i == 4 ? "16" : "13") + "px;"
                    + (i == 4 ? "-fx-font-weight:bold;-fx-text-fill:#FF4D4F;" : "-fx-text-fill:#1A1A2E;"));
            Region gap = new Region(); HBox.setHgrow(gap, Priority.ALWAYS);
            row.getChildren().addAll(name, gap, lblRows[i]);
            if (i == 4) row.setStyle("-fx-border-color:#E8E8E8 transparent transparent transparent;"
                    + "-fx-border-width:1 0 0 0;-fx-padding:8 0 0 0;");
            panelTong.getChildren().add(row);
        }

        step2.getChildren().addAll(lblPhieu, lblDvTitle, tblDv, panelTong);

        // ── BƯỚC 3 — layout 2 cột kiểu MISA ────────────────────────────────
        VBox step3 = buildStep("💳  Bước 3 — Thanh Toán & Xác Nhận");
        step3.setVisible(false); step3.setManaged(false);

        // Trạng thái thanh toán
        String[] selectedHT = {"TienMat"};
        double[] soTienNhap = {0};

        // ── Cột trái: nhập tiền ─────────────────────────────────────────────
        VBox leftCol = new VBox(14);
        leftCol.setPadding(new Insets(0, 12, 0, 0));
        HBox.setHgrow(leftCol, Priority.ALWAYS);

        // Hiển thị số tiền khách đưa
        Label lblNhapTitle = new Label("Số tiền khách đưa");
        lblNhapTitle.setStyle("-fx-font-size:12px;-fx-text-fill:#8C8C8C;");
        Label lblSoTienNhap = new Label("0");
        lblSoTienNhap.setMaxWidth(Double.MAX_VALUE);
        lblSoTienNhap.setAlignment(Pos.CENTER_RIGHT);
        lblSoTienNhap.setStyle("-fx-font-size:28px;-fx-font-weight:bold;-fx-text-fill:#1A1A2E;"
                + "-fx-background-color:#F5F5F5;-fx-background-radius:8;"
                + "-fx-padding:10 16;-fx-border-color:#E8E8E8;-fx-border-width:1;-fx-border-radius:8;");

        boolean[] programmatic = {false};
        Label lblNhapTayTitle = new Label("Hoặc nhập tay:");
        lblNhapTayTitle.setStyle("-fx-font-size:12px;-fx-text-fill:#595959;");
        TextField tfManual = new TextField();
        tfManual.setPromptText("Nhập số tiền khách đưa...");
        tfManual.setStyle("-fx-background-color:#FFFFFF;-fx-border-color:#D9D9D9;"
                + "-fx-border-radius:6;-fx-background-radius:6;-fx-border-width:1;"
                + "-fx-padding:8 12;-fx-font-size:14px;");
        tfManual.textProperty().addListener((obs2, o2, n2) -> {
            if (programmatic[0]) return;
            String digits = (n2 == null ? "" : n2.replaceAll("[^0-9]", ""));
            if (digits.isEmpty()) {
                soTienNhap[0] = 0;
            } else {
                try { soTienNhap[0] = Double.parseDouble(digits); } catch (NumberFormatException ignored) { return; }
            }
            programmatic[0] = true;
            lblSoTienNhap.setText(digits.isEmpty() ? "0" : FMT.format((long) soTienNhap[0]));
            programmatic[0] = false;
            updateTienThua(soTienNhap, lblRows, step3);
        });

        // Nút mệnh giá
        Label lblMenhGia = new Label("Nhập số tiền theo mệnh giá");
        lblMenhGia.setStyle("-fx-font-size:12px;-fx-text-fill:#595959;");
        long[][] denominations = {{500_000, 200_000, 100_000}, {50_000, 20_000, 10_000}, {5_000, 2_000, 1_000}};
        GridPane gridDenom = new GridPane();
        gridDenom.setHgap(8); gridDenom.setVgap(8);
        for (int r = 0; r < denominations.length; r++) {
            for (int c = 0; c < denominations[r].length; c++) {
                long val = denominations[r][c];
                Button btn = new Button(FMT.format(val));
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setStyle("-fx-background-color:#FFFFFF;-fx-text-fill:#1A1A2E;"
                        + "-fx-background-radius:6;-fx-border-color:#D9D9D9;-fx-border-width:1;"
                        + "-fx-border-radius:6;-fx-padding:8 0;-fx-font-size:12px;-fx-cursor:hand;");
                btn.setOnMouseEntered(ev -> btn.setStyle("-fx-background-color:#E6F4FF;-fx-text-fill:#1890FF;"
                        + "-fx-background-radius:6;-fx-border-color:#1890FF;-fx-border-width:1;"
                        + "-fx-border-radius:6;-fx-padding:8 0;-fx-font-size:12px;-fx-cursor:hand;"));
                btn.setOnMouseExited(ev -> btn.setStyle("-fx-background-color:#FFFFFF;-fx-text-fill:#1A1A2E;"
                        + "-fx-background-radius:6;-fx-border-color:#D9D9D9;-fx-border-width:1;"
                        + "-fx-border-radius:6;-fx-padding:8 0;-fx-font-size:12px;-fx-cursor:hand;"));
                btn.setOnAction(ev -> {
                    soTienNhap[0] += val;
                    programmatic[0] = true;
                    tfManual.setText(FMT.format((long) soTienNhap[0]));
                    programmatic[0] = false;
                    lblSoTienNhap.setText(FMT.format((long) soTienNhap[0]));
                    updateTienThua(soTienNhap, lblRows, step3);
                });
                GridPane.setHgrow(btn, Priority.ALWAYS);
                gridDenom.add(btn, c, r);
                ColumnConstraints cc = new ColumnConstraints();
                cc.setPercentWidth(33.33);
                if (gridDenom.getColumnConstraints().size() <= c)
                    gridDenom.getColumnConstraints().add(cc);
            }
        }

        // Nút xóa
        Button btnXoa = new Button("⌫ Xóa");
        btnXoa.setStyle("-fx-background-color:#FFF1F0;-fx-text-fill:#FF4D4F;"
                + "-fx-background-radius:6;-fx-border-color:#FFA39E;-fx-border-width:1;"
                + "-fx-border-radius:6;-fx-padding:8 20;-fx-cursor:hand;");
        btnXoa.setOnAction(ev -> {
            soTienNhap[0] = 0;
            programmatic[0] = true;
            tfManual.setText("");
            programmatic[0] = false;
            lblSoTienNhap.setText("0");
            updateTienThua(soTienNhap, lblRows, step3);
        });

        // Hình thức thanh toán
        Label lblHTTitle = new Label("Hình thức thanh toán");
        lblHTTitle.setStyle("-fx-font-size:12px;-fx-text-fill:#8C8C8C;");

        String[][] htOptions = {{"💵", "Tiền Mặt", "TienMat"}, {"📷", "Mã QR", "MaQR"},
                {"📱", "MOMO", "MOMO"}, {"🏦", "Chuyển Khoản", "ChuyenKhoan"}};
        Button[] htBtns = new Button[htOptions.length];
        HBox htRow = new HBox(8);
        for (int i = 0; i < htOptions.length; i++) {
            final int idx = i;
            final String htCode = htOptions[i][2];
            Button btn = new Button(htOptions[i][0] + "  " + htOptions[i][1]);
            btn.setStyle(htCode.equals("TienMat") ? htBtnActiveStyle() : htBtnStyle());
            btn.setOnAction(ev -> {
                selectedHT[0] = htCode;
                for (Button b : htBtns) b.setStyle(htBtnStyle());
                btn.setStyle(htBtnActiveStyle());
                boolean isCash = "TienMat".equals(htCode);
                gridDenom.setVisible(isCash); gridDenom.setManaged(isCash);
                lblMenhGia.setVisible(isCash); lblMenhGia.setManaged(isCash);
                btnXoa.setVisible(isCash); btnXoa.setManaged(isCash);
                lblNhapTitle.setVisible(isCash); lblNhapTitle.setManaged(isCash);
                lblSoTienNhap.setVisible(isCash); lblSoTienNhap.setManaged(isCash);
                lblNhapTayTitle.setVisible(isCash); lblNhapTayTitle.setManaged(isCash);
                tfManual.setVisible(isCash); tfManual.setManaged(isCash);
                if (!isCash) { soTienNhap[0] = tongTienFinal; updateTienThua(soTienNhap, lblRows, step3); }
            });
            htBtns[idx] = btn;
            htRow.getChildren().add(btn);
        }

        // Gợi ý tiền mặt
        Label lblGoiY = new Label("Gợi ý tiền mặt");
        lblGoiY.setStyle("-fx-font-size:12px;-fx-text-fill:#8C8C8C;");
        HBox goiYRow = new HBox(8);

        leftCol.getChildren().addAll(lblHTTitle, htRow, lblNhapTitle, lblSoTienNhap,
                lblNhapTayTitle, tfManual, lblMenhGia, gridDenom, btnXoa, lblGoiY, goiYRow);

        // ── Cột phải: tóm tắt ───────────────────────────────────────────────
        VBox rightCol = new VBox(12);
        rightCol.setPadding(new Insets(0, 0, 0, 12));
        rightCol.setMinWidth(260);
        rightCol.setMaxWidth(300);
        rightCol.setStyle("-fx-background-color:#F8F9FA;-fx-background-radius:10;"
                + "-fx-border-color:#E8E8E8;-fx-border-width:1;-fx-border-radius:10;-fx-padding:16;");

        Label lblConThu = new Label("Số tiền còn phải thu");
        lblConThu.setStyle("-fx-font-size:12px;-fx-text-fill:#8C8C8C;");
        lblConThuVal = new Label("—");
        lblConThuVal.setStyle("-fx-font-size:26px;-fx-font-weight:bold;-fx-text-fill:#FF4D4F;");

        Separator sep1 = new Separator();

        // Bảng hình thức thanh toán đã chọn
        Label lblHTSummary = new Label("Hình thức thanh toán");
        lblHTSummary.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#595959;");

        VBox htSummaryBox = new VBox(6);
        lblHTSummaryRow = new Label("Tiền mặt:  —");
        lblHTSummaryRow.setStyle("-fx-font-size:13px;-fx-text-fill:#1A1A2E;");
        htSummaryBox.getChildren().add(lblHTSummaryRow);

        Separator sep2 = new Separator();

        Label lblTraLaiTitle = new Label("Tiền trả lại cho khách");
        lblTraLaiTitle.setStyle("-fx-font-size:12px;-fx-text-fill:#8C8C8C;");
        lblTienThua = new Label("0");
        lblTienThua.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:#52C41A;");

        rightCol.getChildren().addAll(lblConThu, lblConThuVal, sep1,
                lblHTSummary, htSummaryBox, sep2,
                lblTraLaiTitle, lblTienThua);

        HBox twoCol = new HBox(0, leftCol, rightCol);
        twoCol.setAlignment(Pos.TOP_LEFT);

        // Nút cuối
        Label errLbl3 = new Label();
        errLbl3.setStyle("-fx-text-fill:#FF4D4F;-fx-font-size:12px;");
        errLbl3.setVisible(false); errLbl3.setManaged(false);

        Button btnBack3    = btnSecondary("← Quay Lại");
        Button btnDong     = btnSecondary("✓  Đóng");
        Button btnInDong   = btnPrimary("🖨  IN & Đóng");
        Button btnConfirm  = new Button("✅  XÁC NHẬN CHECKOUT");
        btnConfirm.setStyle("-fx-background-color:#FF4D4F;-fx-text-fill:white;"
                + "-fx-background-radius:8;-fx-padding:11 24;-fx-font-size:13px;"
                + "-fx-font-weight:bold;-fx-cursor:hand;");

        HBox btnRow3 = new HBox(10, btnBack3, new Region(), btnDong, btnInDong, btnConfirm);
        HBox.setHgrow(btnRow3.getChildren().get(1), Priority.ALWAYS);
        btnRow3.setAlignment(Pos.CENTER_RIGHT);

        step3.getChildren().addAll(twoCol, errLbl3, btnRow3);

        // ── Sự kiện bước 1 → 2 ─────────────────────────────────────────────
        tbl1.getSelectionModel().selectedItemProperty().addListener((obs, old, row) -> {
            if (row == null) return;
            selectedPhieu = new String[]{
                row[0].toString(), row[1].toString(), row[2].toString(),
                row[3].toString(), row[4].toString(), row[5].toString()
            };
            lblPhieu.setText("🏷  Phòng: " + selectedPhieu[1]
                    + "   |   Khách: " + selectedPhieu[2]
                    + "   |   Nhận: " + selectedPhieu[3]
                    + "   |   Trả DK: " + selectedPhieu[4]
                    + "   |   " + selectedPhieu[5] + " đêm");
            long soNgay = 1;
            try { soNgay = Long.parseLong(selectedPhieu[5]); } catch (Exception ex) { soNgay = 1; }
            double tienPhong = queryTienPhong(selectedPhieu[1]) * soNgay;
            double tienDV    = queryTienDichVu(selectedPhieu[0]);
            double phuThu    = 0;
            tongTienFinal    = tienPhong + tienDV + phuThu;
            soNgayFinal    = soNgay;
            tienPhongFinal = tienPhong;
            tienDVFinal    = tienDV;
            lblRows[0].setText(FMT.format(tienPhong) + " đ");
            lblRows[1].setText(FMT.format(tienDV) + " đ");
            lblRows[2].setText(FMT.format(phuThu) + " đ");
            lblRows[3].setText("0 đ");
            lblRows[4].setText(FMT.format(tongTienFinal) + " đ");
            tblDv.setItems(FXCollections.observableArrayList(loadDichVuByPhieu(selectedPhieu[0])));

            // Cập nhật cột phải bước 3
            lblConThuVal.setText(FMT.format(tongTienFinal) + " đ");
            lblHTSummaryRow.setText("Tiền mặt:  " + FMT.format(tongTienFinal) + " đ");

            // Gợi ý tiền mặt: exact + 2 mức làm tròn lên
            goiYRow.getChildren().clear();
            soTienNhap[0] = 0;
            programmatic[0] = true; tfManual.setText(""); programmatic[0] = false;
            lblSoTienNhap.setText("0");
            long exact = (long) tongTienFinal;
            long r1 = roundUp(exact, 10_000);
            long r2 = roundUp(exact, 50_000);
            for (long hint : new long[]{exact, r1, r2}) {
                if (hint <= 0) continue;
                Button b = new Button(FMT.format(hint));
                b.setStyle("-fx-background-color:#52C41A;-fx-text-fill:white;"
                        + "-fx-background-radius:20;-fx-padding:6 14;-fx-font-size:12px;"
                        + "-fx-font-weight:bold;-fx-cursor:hand;");
                b.setOnAction(ev -> {
                    soTienNhap[0] = hint;
                    programmatic[0] = true; tfManual.setText(FMT.format(hint)); programmatic[0] = false;
                    lblSoTienNhap.setText(FMT.format(hint));
                    long thua = hint - exact;
                    lblTienThua.setText(FMT.format(Math.max(0, thua)));
                    lblConThuVal.setText(thua >= 0 ? "0 đ" : FMT.format(-thua) + " đ");
                    lblHTSummaryRow.setText("Tiền mặt:  " + FMT.format(hint) + " đ");
                });
                goiYRow.getChildren().add(b);
            }

            step2.setVisible(true); step2.setManaged(true);
        });


        Button btnNext2 = btnPrimary("Tiếp Theo →");
        btnNext2.setOnAction(e -> { step3.setVisible(true); step3.setManaged(true); });
        HBox btnRow2 = new HBox(btnNext2); btnRow2.setAlignment(Pos.CENTER_RIGHT);
        step2.getChildren().add(btnRow2);

        btnBack3.setOnAction(e -> { step3.setVisible(false); step3.setManaged(false); });
        btnDong.setOnAction(e -> { step3.setVisible(false); step3.setManaged(false); });

        btnConfirm.setOnAction(e -> {
            if (selectedPhieu == null) return;
            if ("TienMat".equals(selectedHT[0]) && soTienNhap[0] < tongTienFinal) {
                errLbl3.setText("⚠ Số tiền khách đưa chưa đủ!");
                errLbl3.setVisible(true); errLbl3.setManaged(true);
                return;
            }
            errLbl3.setVisible(false); errLbl3.setManaged(false);
            doCheckout(selectedPhieu[0], selectedHT[0]);
            alert(Alert.AlertType.INFORMATION, "Checkout Thành Công",
                    "✅ Checkout thành công!\nPhòng: " + selectedPhieu[1]
                    + "\nKhách: " + selectedPhieu[2]
                    + "\nTổng: " + FMT.format(tongTienFinal) + " đ"
                    + "\n\nTrạng thái phòng → Cần Dọn.");
            selectedPhieu = null; tongTienFinal = 0;
            checkinItems.setAll(loadCheckedIn());
            tbl1.setItems(checkinItems);
            step2.setVisible(false); step2.setManaged(false);
            step3.setVisible(false); step3.setManaged(false);
        });

        btnInDong.setOnAction(e -> btnConfirm.fire());

        content.getChildren().addAll(step1, step2, step3);
        scroll.setContent(content);
        root.getChildren().addAll(hdr, scroll);
        VBox.setVgrow(root, Priority.ALWAYS);
        return root;
    }

    private void updateTienThua(double[] soTienNhap, Label[] lblRows, VBox step3) {
        if (lblTienThua == null || lblConThuVal == null) return;
        long thua = (long) soTienNhap[0] - (long) tongTienFinal;
        lblTienThua.setText(FMT.format(Math.max(0, thua)));
        lblConThuVal.setText(thua >= 0 ? "0 đ" : FMT.format(-thua) + " đ");
        if (lblHTSummaryRow != null)
            lblHTSummaryRow.setText("Tiền mặt:  " + FMT.format((long) soTienNhap[0]) + " đ");
    }

    // ── Table bước 1 ────────────────────────────────────────────────────────
    private TableView<Object[]> buildCheckinTable(ObservableList<Object[]> items) {
        TableView<Object[]> tbl = new TableView<>(items);
        tbl.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tbl.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:10;"
                + "-fx-border-radius:10;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");
        tbl.setPlaceholder(new Label("Không có phòng nào đang có khách."));
        String[] heads = {"Mã Phiếu", "Phòng", "Tên Khách", "Giờ Nhận", "Giờ Trả DK", "Số Đêm", "Trạng Thái"};
        for (int i = 0; i < heads.length; i++) {
            final int idx = i;
            TableColumn<Object[], String> col = new TableColumn<>(heads[i]);
            col.setCellValueFactory(p -> new SimpleStringProperty(
                    idx < p.getValue().length && p.getValue()[idx] != null ? p.getValue()[idx].toString() : "—"));
            if (i == 0) col.setCellFactory(tc -> new TableCell<>() {
                @Override protected void updateItem(String s, boolean empty) {
                    super.updateItem(s, empty);
                    setText(empty ? null : s);
                    refresh();
                }
                @Override public void updateSelected(boolean sel) {
                    super.updateSelected(sel);
                    if (!isEmpty()) refresh();
                }
                private void refresh() {
                    setStyle(isEmpty() ? "" : (isSelected()
                        ? "-fx-font-weight:bold;-fx-text-fill:white;"
                        : "-fx-font-weight:bold;-fx-text-fill:#1890FF;"));
                }
            });
            if (i == 6) col.setCellFactory(tc -> new TableCell<>() {
                @Override protected void updateItem(String s, boolean empty) {
                    super.updateItem(s, empty);
                    setGraphic(null);
                    if (empty || s == null) { setText(null); setStyle(""); return; }
                    setText("Đang Ở"); refresh();
                }
                @Override public void updateSelected(boolean sel) {
                    super.updateSelected(sel);
                    if (!isEmpty() && getItem() != null) refresh();
                }
                private void refresh() {
                    setStyle(isSelected()
                        ? "-fx-text-fill:white;-fx-font-weight:bold;"
                        : "-fx-text-fill:#52C41A;-fx-font-weight:bold;");
                }
            });
            tbl.getColumns().add(col);
        }
        return tbl;
    }

    private TableView<Object[]> buildDVTable() {
        TableView<Object[]> tbl = new TableView<>();
        tbl.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tbl.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:8;"
                + "-fx-border-color:#F0F2F5;-fx-border-width:1;-fx-border-radius:8;");
        tbl.setPlaceholder(new Label("Không có dịch vụ nào."));
        String[] heads = {"Tên Dịch Vụ", "Số Lượng", "Đơn Giá", "Thành Tiền", "Ngày Dùng"};
        for (int i = 0; i < heads.length; i++) {
            final int idx = i;
            TableColumn<Object[], String> col = new TableColumn<>(heads[i]);
            col.setCellValueFactory(p -> new SimpleStringProperty(
                    idx < p.getValue().length && p.getValue()[idx] != null ? p.getValue()[idx].toString() : "—"));
            tbl.getColumns().add(col);
        }
        return tbl;
    }

    // ── DB queries ───────────────────────────────────────────────────────────
    private List<Object[]> loadCheckedIn() {
        List<Object[]> result = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con != null) {
            // [0]=maPDP [1]=maPhong [2]=tenKH [3]=gioNhan [4]=gioTraDK [5]=soDem [6]=trangThai
            String sql = "SELECT pdp.maPhieuDatPhong, ct.maPhong, kh.hoTenKH,"
                    + " CONVERT(varchar,ISNULL(pdp.thoiGianNhanThucTe,pdp.thoiGianNhanDuKien),120),"
                    + " CONVERT(varchar,ISNULL(pdp.thoiGianTraThucTe,pdp.thoiGianTraDuKien),120),"
                    + " DATEDIFF(day, ISNULL(pdp.thoiGianNhanThucTe,pdp.thoiGianNhanDuKien), GETDATE()), pdp.trangThai"
                    + " FROM PhieuDatPhong pdp"
                    + " JOIN KhachHang kh ON kh.maKH=pdp.maKhachHang"
                    + " JOIN ChiTietPhieuDatPhong ct ON ct.maPhieuDatPhong=pdp.maPhieuDatPhong"
                    + " WHERE pdp.trangThai='DaCheckIn'";
            try (PreparedStatement pst = con.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {
                while (rs.next())
                    result.add(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3),
                            rs.getString(4), rs.getString(5),
                            String.valueOf(Math.max(1, rs.getInt(6))), "DaCheckIn"});
            } catch (Exception ignored) {}
        }
        if (result.isEmpty()) {
            result.add(new Object[]{"PDP001","P102","Hoàng Thị Em",  "25/04/2026 13:00","27/04/2026 12:00","2","DaCheckIn"});
            result.add(new Object[]{"PDP002","P201","Vũ Quốc Phong", "25/04/2026 15:00","27/04/2026 12:00","2","DaCheckIn"});
        }
        return result;
    }

    private List<Object[]> loadDichVuByPhieu(String maPDP) {
        List<Object[]> result = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con != null) {
            String sql = "SELECT dv.tenDichVu, ctdv.soLuong, dv.donGia,"
                    + " (ctdv.soLuong * dv.donGia),"
                    + " CONVERT(varchar, ctdv.thoiDiemSuDung, 103)"
                    + " FROM ChiTietDichVu ctdv"
                    + " JOIN DichVu dv ON dv.maDichVu = ctdv.maDichVu"
                    + " WHERE ctdv.maPhieuDatPhong = ?";
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, maPDP);
                ResultSet rs = pst.executeQuery();
                while (rs.next())
                    result.add(new Object[]{
                        rs.getString(1),
                        rs.getString(2),
                        FMT.format(rs.getLong(3)) + " đ",
                        FMT.format(rs.getLong(4)) + " đ",
                        rs.getString(5)
                    });
            } catch (Exception ignored) {}
        }
        return result;
    }

    private double queryTienPhong(String maPhong) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con != null) {
            String sql = "SELECT ISNULL(bg.donGia,0) FROM Phong p"
                    + " LEFT JOIN BangGia bg ON bg.maLoaiPhong=p.maLoaiPhong"
                    + " AND bg.loaiThue='QuaDem' AND GETDATE() BETWEEN bg.ngayBatDau AND bg.ngayKetThuc"
                    + " WHERE p.maPhong=?";
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, maPhong);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) return rs.getDouble(1);
            } catch (Exception ignored) {}
        }
        return 750_000;
    }

    private double queryTienDichVu(String maPDP) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con != null) {
            String sql = "SELECT ISNULL(SUM(ctdv.soLuong * dv.donGia), 0)"
                    + " FROM ChiTietDichVu ctdv JOIN DichVu dv ON dv.maDichVu=ctdv.maDichVu"
                    + " WHERE ctdv.maPhieuDatPhong=?";
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, maPDP);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) return rs.getDouble(1);
            } catch (Exception ignored) {}
        }
        return 0;
    }

    private void doCheckout(String maPDP, String hinhThuc) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return;
        try {
            con.setAutoCommit(false);

            // 1. Load tiền cọc đã thu
            double datCoc = 0;
            try (PreparedStatement pst = con.prepareStatement(
                    "SELECT ISNULL(SUM(soTienCoc),0) FROM PhieuThu WHERE maPhieuDatPhong=?")) {
                pst.setString(1, maPDP);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) datCoc = rs.getDouble(1);
            }

            String maNV = SessionContext.getInstance().getMaNhanVien();
            double tienThanhToan = Math.max(0, tongTienFinal - datCoc);
            String maHD = "HD" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();

            // 2. Cập nhật phiếu đặt phòng
            try (PreparedStatement pst = con.prepareStatement(
                    "UPDATE PhieuDatPhong SET trangThai=N'DaCheckOut', thoiGianTraThucTe=GETDATE() WHERE maPhieuDatPhong=?")) {
                pst.setString(1, maPDP);
                pst.executeUpdate();
            }

            // 3. Cập nhật trạng thái phòng
            try (PreparedStatement pst = con.prepareStatement(
                    "UPDATE Phong SET trangThai=N'PhongCanDon' WHERE maPhong IN "
                    + "(SELECT maPhong FROM ChiTietPhieuDatPhong WHERE maPhieuDatPhong=?)")) {
                pst.setString(1, maPDP);
                pst.executeUpdate();
            }

            // 4. Tạo hóa đơn
            try (PreparedStatement pst = con.prepareStatement(
                    "INSERT INTO HoaDon (maHoaDon,ngayLap,maNhanVienLap,maPhieuDatPhong,"
                    + "ngayThanhToan,tienKhuyenMai,tienThanhToan,phuongThucThanhToan,ghiChu) "
                    + "VALUES (?,GETDATE(),?,?,GETDATE(),0,?,?,N'')")) {
                pst.setString(1, maHD);
                pst.setString(2, maNV);
                pst.setString(3, maPDP);
                pst.setDouble(4, tienThanhToan);
                pst.setString(5, hinhThuc);
                pst.executeUpdate();
            }

            // 5. Chi tiết hóa đơn — tiền phòng
            if (tienPhongFinal > 0) {
                try (PreparedStatement pst = con.prepareStatement(
                        "INSERT INTO ChiTietHoaDon (maHoaDon,loaiTien,moTa,donGia,soLuong,thanhTien) VALUES (?,N'TienPhong',?,?,?,?)")) {
                    double donGia = soNgayFinal > 0 ? tienPhongFinal / soNgayFinal : tienPhongFinal;
                    pst.setString(1, maHD);
                    pst.setString(2, "Tiền phòng (" + soNgayFinal + " đêm)");
                    pst.setDouble(3, donGia);
                    pst.setInt(4, (int) soNgayFinal);
                    pst.setDouble(5, tienPhongFinal);
                    pst.executeUpdate();
                }
            }

            // 6. Chi tiết hóa đơn — từng dịch vụ
            try (PreparedStatement qry = con.prepareStatement(
                    "SELECT dv.tenDichVu, ctdv.soLuong, dv.donGia, (ctdv.soLuong*dv.donGia) "
                    + "FROM ChiTietDichVu ctdv JOIN DichVu dv ON dv.maDichVu=ctdv.maDichVu "
                    + "WHERE ctdv.maPhieuDatPhong=?")) {
                qry.setString(1, maPDP);
                ResultSet rs = qry.executeQuery();
                while (rs.next()) {
                    try (PreparedStatement ins = con.prepareStatement(
                            "INSERT INTO ChiTietHoaDon (maHoaDon,loaiTien,moTa,donGia,soLuong,thanhTien) VALUES (?,N'TienDichVu',?,?,?,?)")) {
                        ins.setString(1, maHD);
                        ins.setString(2, rs.getString(1));
                        ins.setDouble(3, rs.getDouble(3));
                        ins.setInt(4, rs.getInt(2));
                        ins.setDouble(5, rs.getDouble(4));
                        ins.executeUpdate();
                    }
                }
            }

            // 7. Chi tiết hóa đơn — tiền cọc (ghi âm để trừ)
            if (datCoc > 0) {
                try (PreparedStatement pst = con.prepareStatement(
                        "INSERT INTO ChiTietHoaDon (maHoaDon,loaiTien,moTa,donGia,soLuong,thanhTien) VALUES (?,N'TienCoc',N'Tiền cọc đã thu',?,1,?)")) {
                    pst.setString(1, maHD);
                    pst.setDouble(2, -datCoc);
                    pst.setDouble(3, -datCoc);
                    pst.executeUpdate();
                }
            }

            // 8. Liên kết PhieuThu với HoaDon vừa tạo
            try (PreparedStatement pst = con.prepareStatement(
                    "UPDATE PhieuThu SET maHoaDon=? WHERE maPhieuDatPhong=? AND maHoaDon IS NULL")) {
                pst.setString(1, maHD);
                pst.setString(2, maPDP);
                pst.executeUpdate();
            }

            con.commit();
        } catch (Exception e) {
            try { con.rollback(); } catch (Exception ignored) {}
            e.printStackTrace();
        } finally {
            try { con.setAutoCommit(true); } catch (Exception ignored) {}
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private long roundUp(long amount, long unit) {
        return (long) (Math.ceil((double) amount / unit) * unit);
    }

    private VBox buildStep(String title) {
        VBox box = new VBox(14);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:10;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");
        Label lbl = new Label(title);
        lbl.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#1A1A2E;"
                + "-fx-border-color:transparent transparent #F0F2F5 transparent;"
                + "-fx-border-width:0 0 1 0;-fx-padding:0 0 10 0;");
        lbl.setMaxWidth(Double.MAX_VALUE);
        box.getChildren().add(lbl);
        return box;
    }

    private TextField searchField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle("-fx-background-color:#F5F5F5;-fx-border-color:#E8E8E8;"
                + "-fx-border-radius:8;-fx-background-radius:8;-fx-border-width:1;"
                + "-fx-padding:8 12;-fx-font-size:12px;");
        HBox.setHgrow(tf, Priority.ALWAYS);
        return tf;
    }

    private Button btnPrimary(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:#1890FF;-fx-text-fill:white;"
                + "-fx-background-radius:8;-fx-padding:9 20;-fx-font-weight:bold;-fx-cursor:hand;");
        return b;
    }

    private Button btnSecondary(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:#F5F5F5;-fx-text-fill:#595959;"
                + "-fx-background-radius:8;-fx-border-color:#D9D9D9;-fx-border-width:1;"
                + "-fx-border-radius:8;-fx-padding:9 20;-fx-cursor:hand;");
        return b;
    }

    private String htBtnStyle() {
        return "-fx-background-color:#FFFFFF;-fx-text-fill:#595959;"
                + "-fx-background-radius:8;-fx-border-color:#D9D9D9;-fx-border-width:1;"
                + "-fx-border-radius:8;-fx-padding:8 14;-fx-cursor:hand;-fx-font-size:12px;";
    }

    private String htBtnActiveStyle() {
        return "-fx-background-color:#E6F4FF;-fx-text-fill:#1890FF;"
                + "-fx-background-radius:8;-fx-border-color:#1890FF;-fx-border-width:2;"
                + "-fx-border-radius:8;-fx-padding:8 14;-fx-cursor:hand;-fx-font-size:12px;-fx-font-weight:bold;";
    }

    private void alert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type, msg);
        a.setHeaderText(null); a.setTitle(title); a.showAndWait();
    }
}
