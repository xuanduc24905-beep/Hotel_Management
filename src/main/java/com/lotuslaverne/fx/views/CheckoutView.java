package com.lotuslaverne.fx.views;

import com.lotuslaverne.util.ConnectDB;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CheckoutView {

    private static final DecimalFormat FMT = new DecimalFormat("#,###");
    private static final DateTimeFormatter DT  = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Phiếu đang check-in được chọn: [maPDP, maPhong, tenKhach, gioNhan, soNgay]
    private String[] selectedPhieu = null;

    public Node build() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color:#F0F2F5;");

        // ── Header ──
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

        // ── BƯỚC 1: Chọn phòng/phiếu đang ở ──
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
                for (Object c : r) if (c!=null && c.toString().toLowerCase().contains(kw)) { f.add(r); break; }
            tbl1.setItems(f);
        });
        Button btnRefresh1 = btnSecondary("↻ Làm Mới");
        btnRefresh1.setOnAction(e -> { checkinItems.setAll(loadCheckedIn()); tbl1.setItems(checkinItems); });
        Region sp1 = new Region(); HBox.setHgrow(sp1, Priority.ALWAYS);
        tb1.getChildren().addAll(s1, sp1, btnRefresh1);
        step1.getChildren().addAll(tb1, tbl1);

        // ── BƯỚC 2: Chi tiết hóa đơn ──
        VBox step2 = buildStep("💰  Bước 2 — Chi Tiết Hóa Đơn");
        step2.setVisible(false); step2.setManaged(false);

        Label lblPhieu = new Label();
        lblPhieu.setStyle("-fx-background-color:#E6F4FF;-fx-text-fill:#1890FF;-fx-padding:10 16;"
                + "-fx-background-radius:8;-fx-border-color:#91CAFF;-fx-border-width:1;-fx-border-radius:8;"
                + "-fx-font-size:13px;-fx-font-weight:bold;");
        lblPhieu.setMaxWidth(Double.MAX_VALUE);

        // Bảng dịch vụ đã dùng
        TableView<Object[]> tblDv = buildDVTable();
        tblDv.setPrefHeight(160);
        Label lblDvTitle = new Label("📋 Dịch vụ đã sử dụng:");
        lblDvTitle.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#595959;");

        // Panel tổng tiền
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
            name.setStyle("-fx-font-size:" + (i==4?"15":"13") + "px;"
                    + (i==4?"-fx-font-weight:bold;":"") + "-fx-text-fill:#595959;");
            lblRows[i] = new Label(rowVals[i]);
            lblRows[i].setStyle("-fx-font-size:" + (i==4?"16":"13") + "px;"
                    + (i==4?"-fx-font-weight:bold;-fx-text-fill:#FF4D4F;":"-fx-text-fill:#1A1A2E;"));
            Region gap = new Region(); HBox.setHgrow(gap, Priority.ALWAYS);
            row.getChildren().addAll(name, gap, lblRows[i]);
            if (i==4) row.setStyle("-fx-border-color:#E8E8E8 transparent transparent transparent;"
                    + "-fx-border-width:1 0 0 0;-fx-padding:8 0 0 0;");
            panelTong.getChildren().add(row);
        }

        step2.getChildren().addAll(lblPhieu, lblDvTitle, tblDv, panelTong);

        // ── BƯỚC 3: Thanh toán ──
        VBox step3 = buildStep("💳  Bước 3 — Thanh Toán & Xác Nhận");
        step3.setVisible(false); step3.setManaged(false);

        Label lblTongFinal = new Label("Tổng thanh toán: —");
        lblTongFinal.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:#FF4D4F;"
                + "-fx-background-color:#FFF1F0;-fx-padding:14 20;-fx-background-radius:8;"
                + "-fx-border-color:#FFA39E;-fx-border-width:1;-fx-border-radius:8;");
        lblTongFinal.setMaxWidth(Double.MAX_VALUE);

        Label lblHTTitle = new Label("Hình thức thanh toán:");
        lblHTTitle.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#595959;");

        ToggleGroup tgHT = new ToggleGroup();
        RadioButton rbTienMat = radio("💵  Tiền Mặt", tgHT);
        RadioButton rbThe     = radio("💳  Thẻ Ngân Hàng", tgHT);
        RadioButton rbCK      = radio("📱  Chuyển Khoản", tgHT);
        rbTienMat.setSelected(true);
        HBox htRow = new HBox(20, rbTienMat, rbThe, rbCK);
        htRow.setAlignment(Pos.CENTER_LEFT);

        TextField txtSoTien = searchField("Số tiền khách đưa (tiền mặt)...");
        Label lblTienThua = new Label();
        lblTienThua.setStyle("-fx-font-size:13px;-fx-text-fill:#52C41A;-fx-font-weight:bold;");
        HBox tienMatRow = new HBox(8, new Label("Tiền nhận:"), txtSoTien, lblTienThua);
        tienMatRow.setAlignment(Pos.CENTER_LEFT);
        rbTienMat.selectedProperty().addListener((obs,o,n) -> { tienMatRow.setVisible(n); tienMatRow.setManaged(n); });
        rbThe.selectedProperty().addListener((obs,o,n)  -> { tienMatRow.setVisible(!n); tienMatRow.setManaged(!n); });
        rbCK.selectedProperty().addListener((obs,o,n)   -> { tienMatRow.setVisible(!n); tienMatRow.setManaged(!n); });

        Label errLbl3 = new Label();
        errLbl3.setStyle("-fx-text-fill:#FF4D4F;-fx-font-size:12px;");
        errLbl3.setVisible(false); errLbl3.setManaged(false);

        Button btnBack3 = btnSecondary("← Quay Lại");
        Button btnConfirm = new Button("✅  XÁC NHẬN CHECKOUT & IN HÓA ĐƠN");
        btnConfirm.setStyle("-fx-background-color:#FF4D4F;-fx-text-fill:white;"
                + "-fx-background-radius:8;-fx-padding:13 28;-fx-font-size:14px;"
                + "-fx-font-weight:bold;-fx-cursor:hand;");
        HBox btnRow3 = new HBox(12, btnBack3, btnConfirm);
        btnRow3.setAlignment(Pos.CENTER_RIGHT);

        step3.getChildren().addAll(lblTongFinal, lblHTTitle, htRow, tienMatRow, errLbl3, btnRow3);

        // ── Sự kiện ──
        // Chọn phiếu ở bước 1 → chuyển bước 2
        tbl1.setOnMouseClicked(e -> {
            if (e.getClickCount() != 2) return;
            Object[] row = tbl1.getSelectionModel().getSelectedItem();
            if (row == null) return;
            selectedPhieu = new String[]{
                row[0].toString(), row[1].toString(), row[2].toString(),
                row[3].toString(), row[4].toString()
            };
            lblPhieu.setText("🏷  Phòng: " + selectedPhieu[1]
                    + "   |   Khách: " + selectedPhieu[2]
                    + "   |   Nhận: " + selectedPhieu[3]
                    + "   |   " + selectedPhieu[4] + " đêm");
            // Tính tiền (demo)
            long soNgay = 1;
            try { soNgay = Long.parseLong(selectedPhieu[4]); } catch (Exception ex) { soNgay = 1; }
            double tienPhong  = queryTienPhong(selectedPhieu[1]) * soNgay;
            double tienDV     = 0; // TODO
            double phuThu     = 0;
            double tong       = tienPhong + tienDV + phuThu;
            lblRows[0].setText(FMT.format(tienPhong) + " đ");
            lblRows[1].setText(FMT.format(tienDV) + " đ");
            lblRows[2].setText(FMT.format(phuThu) + " đ");
            lblRows[3].setText("0 đ");
            lblRows[4].setText(FMT.format(tong) + " đ");
            lblTongFinal.setText("Tổng thanh toán: " + FMT.format(tong) + " đ");
            // Load dịch vụ đã dùng
            tblDv.setItems(FXCollections.observableArrayList(loadDichVuByPhieu(selectedPhieu[0])));
            step2.setVisible(true); step2.setManaged(true);
        });

        Button btnNext2 = btnPrimary("Tiếp Theo →");
        btnNext2.setOnAction(e -> { step3.setVisible(true); step3.setManaged(true); });
        HBox btnRow2 = new HBox(btnNext2); btnRow2.setAlignment(Pos.CENTER_RIGHT);
        step2.getChildren().add(btnRow2);

        btnBack3.setOnAction(e -> { step3.setVisible(false); step3.setManaged(false); });

        btnConfirm.setOnAction(e -> {
            if (selectedPhieu == null) return;
            // TODO: gọi DAO để checkout, tạo HoaDon, cập nhật trạng thái phòng
            alert(Alert.AlertType.INFORMATION, "Checkout Thành Công",
                    "✅ Checkout thành công!\nPhòng: " + selectedPhieu[1]
                    + "\nKhách: " + selectedPhieu[2]
                    + "\n\nTrạng thái phòng → Cần Dọn.\nHóa đơn đã được ghi nhận.");
            selectedPhieu = null;
            checkinItems.setAll(loadCheckedIn());
            tbl1.setItems(checkinItems);
            step2.setVisible(false); step2.setManaged(false);
            step3.setVisible(false); step3.setManaged(false);
        });

        content.getChildren().addAll(step1, step2, step3);
        scroll.setContent(content);
        root.getChildren().addAll(hdr, scroll);
        VBox.setVgrow(root, Priority.ALWAYS);
        return root;
    }

    // ── Table phòng đang check-in ──
    private TableView<Object[]> buildCheckinTable(ObservableList<Object[]> items) {
        TableView<Object[]> tbl = new TableView<>(items);
        tbl.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tbl.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:10;"
                + "-fx-border-radius:10;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");
        tbl.setPlaceholder(new Label("Không có phòng nào đang có khách."));
        String[] heads = {"Mã Phiếu", "Phòng", "Tên Khách", "Giờ Nhận", "Số Đêm", "Trạng Thái"};
        for (int i = 0; i < heads.length; i++) {
            final int idx = i;
            TableColumn<Object[], String> col = new TableColumn<>(heads[i]);
            col.setCellValueFactory(p -> new SimpleStringProperty(
                    idx < p.getValue().length && p.getValue()[idx] != null ? p.getValue()[idx].toString() : "—"));
            if (i == 0) col.setCellFactory(tc -> new TableCell<>() {
                @Override protected void updateItem(String s, boolean empty) {
                    super.updateItem(s, empty);
                    setText(empty ? null : s);
                    setStyle(empty ? "" : "-fx-font-weight:bold;-fx-text-fill:#1890FF;");
                }
            });
            if (i == 5) col.setCellFactory(tc -> new TableCell<>() {
                @Override protected void updateItem(String s, boolean empty) {
                    super.updateItem(s, empty);
                    if (empty || s == null) { setGraphic(null); return; }
                    Label b = new Label("Đang Ở");
                    b.setStyle("-fx-background-color:#F6FFED;-fx-text-fill:#52C41A;-fx-padding:2 8;"
                            + "-fx-background-radius:10;-fx-font-size:11px;-fx-font-weight:bold;");
                    setGraphic(b); setText(null);
                }
            });
            tbl.getColumns().add(col);
        }
        Label hint = new Label("💡 Double-click vào phiếu để chọn và tiến hành checkout");
        hint.setStyle("-fx-font-size:11px;-fx-text-fill:#8C8C8C;-fx-padding:4 0 0 0;");
        return tbl;
    }

    // ── Table dịch vụ đã dùng ──
    private TableView<Object[]> buildDVTable() {
        TableView<Object[]> tbl = new TableView<>();
        tbl.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tbl.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:8;-fx-border-color:#F0F2F5;-fx-border-width:1;-fx-border-radius:8;");
        tbl.setPlaceholder(new Label("Không có dịch vụ nào được sử dụng."));
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

    // ── Load data ──
    private List<Object[]> loadCheckedIn() {
        List<Object[]> result = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con != null) {
            String sql = "SELECT pdp.maPhieuDatPhong, ct.maPhong, kh.hoTenKH, "
                    + "CONVERT(varchar,pdp.thoiGianNhanDuKien,120), "
                    + "DATEDIFF(day, pdp.thoiGianNhanDuKien, GETDATE()), pdp.trangThai "
                    + "FROM PhieuDatPhong pdp "
                    + "JOIN KhachHang kh ON kh.maKH=pdp.maKhachHang "
                    + "JOIN ChiTietPhieuDatPhong ct ON ct.maPhieuDatPhong=pdp.maPhieuDatPhong "
                    + "WHERE pdp.trangThai='DaCheckIn'";
            try (PreparedStatement pst = con.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {
                while (rs.next())
                    result.add(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3),
                            rs.getString(4), String.valueOf(Math.max(1, rs.getInt(5))), "DaCheckIn"});
            } catch (Exception ignored) {}
        }
        if (result.isEmpty()) {
            result.add(new Object[]{"PDP001", "P102", "Hoàng Thị Em", "25/04/2026 13:00", "2", "DaCheckIn"});
            result.add(new Object[]{"PDP002", "P201", "Vũ Quốc Phong", "25/04/2026 15:00", "2", "DaCheckIn"});
        }
        return result;
    }

    private List<Object[]> loadDichVuByPhieu(String maPDP) {
        List<Object[]> result = new ArrayList<>();
        // TODO: query ChiTietDichVu JOIN DichVu WHERE maPhieuDatPhong = maPDP
        result.add(new Object[]{"Nước khoáng", "2", "20,000", "40,000", "26/04/2026"});
        result.add(new Object[]{"Giặt ủi",     "1", "50,000", "50,000", "26/04/2026"});
        return result;
    }

    private double queryTienPhong(String maPhong) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con != null) {
            String sql = "SELECT ISNULL(bg.donGia,0) FROM Phong p "
                    + "LEFT JOIN BangGia bg ON bg.maLoaiPhong=p.maLoaiPhong "
                    + "AND bg.loaiThue='QuaDem' AND GETDATE() BETWEEN bg.ngayBatDau AND bg.ngayKetThuc "
                    + "WHERE p.maPhong=?";
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, maPhong);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) return rs.getDouble(1);
            } catch (Exception ignored) {}
        }
        return 750000;
    }

    // ── Helpers ──
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

    private RadioButton radio(String text, ToggleGroup tg) {
        RadioButton rb = new RadioButton(text);
        rb.setToggleGroup(tg);
        rb.setStyle("-fx-font-size:13px;");
        return rb;
    }

    private void alert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type, msg);
        a.setHeaderText(null); a.setTitle(title); a.showAndWait();
    }
}
