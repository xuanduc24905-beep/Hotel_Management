package com.lotuslaverne.fx.views;

import com.lotuslaverne.dao.DichVuDAO;
import com.lotuslaverne.dao.PhieuDatPhongDAO;
import com.lotuslaverne.entity.DichVu;
import com.lotuslaverne.entity.PhieuDatPhong;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * DichVuPhongView – Ghi nhận dịch vụ phát sinh (mini-bar, giặt ủi, room service...)
 * cho một phiếu đặt phòng đang ở (DaCheckIn).
 */
public class DichVuPhongView {

    private static final DecimalFormat MONEY = new DecimalFormat("#,###");

    private ComboBox<String> cbPhieu;
    private ComboBox<DichVu> cbDichVu;
    private Spinner<Integer> spSoLuong;
    private TextField txtGhiChu;
    private Label lblDonGia, lblThanhTien, lblTongPhatSinh;
    private TableView<Object[]> tblChiTiet;
    private ObservableList<Object[]> chiTietItems;

    private final DichVuDAO dichVuDAO = new DichVuDAO();
    private final PhieuDatPhongDAO pdpDAO = new PhieuDatPhongDAO();

    public Node build() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #F0F2F5;");

        VBox content = new VBox(20);
        content.setPadding(new Insets(28));
        VBox.setVgrow(content, Priority.ALWAYS);

        // ── Header
        VBox header = new VBox(4);
        Label title = new Label("Dịch Vụ Phòng");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Label sub = new Label("Ghi nhận dịch vụ phát sinh (mini-bar, giặt ủi, room service...) cho phòng đang ở");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #8C8C8C;");
        header.getChildren().addAll(title, sub);

        HBox mainRow = new HBox(20);
        mainRow.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(mainRow, Priority.ALWAYS);

        VBox leftCard = buildInputCard();
        VBox rightCard = buildHistoryCard();
        HBox.setHgrow(leftCard, Priority.ALWAYS);
        HBox.setHgrow(rightCard, Priority.ALWAYS);
        mainRow.getChildren().addAll(leftCard, rightCard);

        content.getChildren().addAll(header, mainRow);
        root.getChildren().add(content);
        VBox.setVgrow(root, Priority.ALWAYS);

        // Load initial data
        loadPhieuDangO();
        loadDichVu();

        return root;
    }

    // ─────────────────────────── LEFT CARD: Input form
    private VBox buildInputCard() {
        VBox card = new VBox(16);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        Label cardTitle = new Label("Thêm Dịch Vụ Cho Phòng");
        cardTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;"
                + "-fx-border-color: transparent transparent #F0F2F5 transparent;"
                + "-fx-border-width: 0 0 1 0; -fx-padding: 0 0 10 0;");

        GridPane form = new GridPane();
        form.setHgap(12); form.setVgap(14);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(38);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(62);
        form.getColumnConstraints().addAll(c1, c2);

        // Phiếu đặt phòng
        cbPhieu = new ComboBox<>();
        cbPhieu.setMaxWidth(Double.MAX_VALUE);
        cbPhieu.setPromptText("Chọn phòng đang ở...");
        cbPhieu.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D9D9D9;"
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1;");
        cbPhieu.setOnAction(e -> loadChiTietByPhieu());

        // Dịch vụ
        cbDichVu = new ComboBox<>();
        cbDichVu.setMaxWidth(Double.MAX_VALUE);
        cbDichVu.setPromptText("Chọn dịch vụ...");
        cbDichVu.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D9D9D9;"
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1;");
        cbDichVu.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(DichVu dv, boolean empty) {
                super.updateItem(dv, empty);
                setText(empty || dv == null ? null
                        : dv.getTenDichVu() + "  —  " + MONEY.format(dv.getDonGia()) + "đ");
            }
        });
        cbDichVu.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(DichVu dv, boolean empty) {
                super.updateItem(dv, empty);
                setText(empty || dv == null ? null
                        : dv.getTenDichVu() + "  —  " + MONEY.format(dv.getDonGia()) + "đ");
            }
        });
        cbDichVu.setOnAction(e -> updatePricePreview());

        // Số lượng
        spSoLuong = new Spinner<>(1, 99, 1);
        spSoLuong.setMaxWidth(Double.MAX_VALUE);
        spSoLuong.setEditable(true);
        spSoLuong.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D9D9D9;"
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1;");
        spSoLuong.valueProperty().addListener((obs, o, n) -> updatePricePreview());

        // Ghi chú
        txtGhiChu = new TextField();
        txtGhiChu.setPromptText("VD: Khách yêu cầu tại 22:30...");
        txtGhiChu.setMaxWidth(Double.MAX_VALUE);
        txtGhiChu.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D9D9D9;"
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1; -fx-padding: 7 10;");

        // Price preview
        lblDonGia   = new Label("—");
        lblThanhTien = new Label("—");
        lblDonGia.setStyle("-fx-font-size: 13px; -fx-text-fill: #595959; -fx-font-weight: bold;");
        lblThanhTien.setStyle("-fx-font-size: 14px; -fx-text-fill: #1890FF; -fx-font-weight: bold;");

        // Nút thêm
        Button btnThem = new Button("✚  Ghi Nhận Dịch Vụ");
        btnThem.setStyle("-fx-background-color: #1890FF; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-size: 13px;"
                + "-fx-font-weight: bold; -fx-cursor: hand;");
        btnThem.setMaxWidth(Double.MAX_VALUE);
        btnThem.setOnAction(e -> handleThem());

        Button btnRefresh = new Button("↻ Làm Mới");
        btnRefresh.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #595959;"
                + "-fx-background-radius: 8; -fx-border-color: #D9D9D9; -fx-border-width: 1;"
                + "-fx-border-radius: 8; -fx-padding: 8 14; -fx-cursor: hand; -fx-font-size: 12px;");
        btnRefresh.setOnAction(e -> { loadPhieuDangO(); loadDichVu(); loadChiTietByPhieu(); });

        form.add(lbl("Phòng đang ở:"),   0, 0); form.add(cbPhieu,     1, 0);
        form.add(lbl("Dịch vụ:"),        0, 1); form.add(cbDichVu,    1, 1);
        form.add(lbl("Số lượng:"),       0, 2); form.add(spSoLuong,   1, 2);
        form.add(lbl("Đơn giá:"),        0, 3); form.add(lblDonGia,   1, 3);
        form.add(lbl("Thành tiền:"),     0, 4); form.add(lblThanhTien,1, 4);
        form.add(lbl("Ghi chú:"),        0, 5); form.add(txtGhiChu,   1, 5);

        card.getChildren().addAll(cardTitle, form, btnThem, btnRefresh);
        return card;
    }

    // ─────────────────────────── RIGHT CARD: History table
    private VBox buildHistoryCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");
        VBox.setVgrow(card, Priority.ALWAYS);

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label cardTitle = new Label("Dịch Vụ Đã Ghi Nhận");
        cardTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        lblTongPhatSinh = new Label("Tổng: 0đ");
        lblTongPhatSinh.setStyle("-fx-font-size: 13px; -fx-text-fill: #FF4D4F; -fx-font-weight: bold;");
        titleRow.getChildren().addAll(cardTitle, sp, lblTongPhatSinh);

        tblChiTiet = new TableView<>();
        tblChiTiet.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblChiTiet.setStyle("-fx-background-color: #F9F9F9; -fx-border-color: #E8E8E8;"
                + "-fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");
        tblChiTiet.setPlaceholder(new Label("Chọn phòng để xem dịch vụ đã dùng."));
        VBox.setVgrow(tblChiTiet, Priority.ALWAYS);

        String[] heads = {"Dịch Vụ", "SL", "Đơn Giá", "Thành Tiền", "Thời Gian", "Ghi Chú"};
        for (int i = 0; i < heads.length; i++) {
            final int idx = i;
            TableColumn<Object[], String> col = new TableColumn<>(heads[i]);
            col.setCellValueFactory(p -> {
                Object v = idx < p.getValue().length ? p.getValue()[idx + 1] : "";
                return new SimpleStringProperty(v != null ? v.toString() : "");
            });
            tblChiTiet.getColumns().add(col);
        }
        chiTietItems = FXCollections.observableArrayList();
        tblChiTiet.setItems(chiTietItems);

        card.getChildren().addAll(titleRow, tblChiTiet);
        return card;
    }

    // ─────────────────────────── DATA LOGIC
    private void loadPhieuDangO() {
        try {
            List<PhieuDatPhong> list = pdpDAO.getDangSuDung();
            cbPhieu.getItems().clear();
            for (PhieuDatPhong p : list) {
                cbPhieu.getItems().add(p.getMaPhieuDatPhong());
            }
        } catch (Exception ignored) {}
    }

    private void loadDichVu() {
        try {
            List<DichVu> list = dichVuDAO.getAll();
            cbDichVu.getItems().clear();
            for (DichVu dv : list) {
                if ("DangKinhDoanh".equals(dv.getTrangThai())) cbDichVu.getItems().add(dv);
            }
        } catch (Exception ignored) {}
    }

    private void loadChiTietByPhieu() {
        String maPDP = cbPhieu.getValue();
        chiTietItems.clear();
        if (maPDP == null || maPDP.isBlank()) {
            lblTongPhatSinh.setText("Tổng: 0đ");
            return;
        }
        try {
            List<Object[]> rows = dichVuDAO.getChiTietDichVuByPhieu(maPDP);
            chiTietItems.addAll(rows);
            // Tính tổng
            double tong = rows.stream().mapToDouble(r -> {
                try { return Double.parseDouble(r[4].toString().replace(",", "")); }
                catch (Exception ex) { return 0; }
            }).sum();
            lblTongPhatSinh.setText("Tổng phát sinh: " + MONEY.format(tong) + "đ");
        } catch (Exception ignored) {}
    }

    private void updatePricePreview() {
        DichVu dv = cbDichVu.getValue();
        if (dv == null) { lblDonGia.setText("—"); lblThanhTien.setText("—"); return; }
        int qty = spSoLuong.getValue();
        double donGia = dv.getDonGia();
        lblDonGia.setText(MONEY.format(donGia) + "đ");
        lblThanhTien.setText(MONEY.format(donGia * qty) + "đ");
    }

    private void handleThem() {
        String maPDP = cbPhieu.getValue();
        DichVu dv   = cbDichVu.getValue();
        if (maPDP == null || maPDP.isBlank()) { alert("Chọn phòng đang ở!"); return; }
        if (dv == null) { alert("Chọn dịch vụ!"); return; }
        int qty = spSoLuong.getValue();
        String ghiChu = txtGhiChu.getText().trim();

        boolean ok = dichVuDAO.themChiTietDichVu(dv.getMaDichVu(), maPDP, qty, ghiChu);
        if (ok) {
            loadChiTietByPhieu();
            txtGhiChu.clear();
            spSoLuong.getValueFactory().setValue(1);
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setHeaderText(null);
            a.setTitle("Thành Công");
            a.setContentText("✅ Đã ghi nhận: " + dv.getTenDichVu()
                    + " × " + qty + "  →  " + MONEY.format(dv.getDonGia() * qty) + "đ");
            a.showAndWait();
        } else {
            alert("Lỗi ghi nhận dịch vụ. Kiểm tra kết nối DB.");
        }
    }

    // ─────────────────────────── HELPERS
    private Label lbl(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #595959;");
        return l;
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg);
        a.setHeaderText(null); a.setTitle("Thông báo"); a.showAndWait();
    }
}
