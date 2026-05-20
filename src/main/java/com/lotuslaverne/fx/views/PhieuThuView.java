package com.lotuslaverne.fx.views;

import com.lotuslaverne.dao.BangGiaDAO;
import com.lotuslaverne.dao.KhachHangDAO;
import com.lotuslaverne.dao.PhieuThuDAO;
import com.lotuslaverne.dao.PhieuDatPhongDAO;
import com.lotuslaverne.service.PhieuThuService;
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

/**
 * PhieuThuView – Quản lý phiếu đặt cọc (Deposit Management).
 * Lễ tân thu tiền cọc khi khách đặt phòng và ghi vào bảng PhieuThu.
 */
public class PhieuThuView {

    private static final DecimalFormat MONEY = new DecimalFormat("#,###");

    private ComboBox<String> cbPhieu;
    private TextField txtSoTienCoc, txtGhiChu;
    private ComboBox<String> cbPhuongThuc;
    private Label lblKhach, lblPhong, lblNgayDat;
    private Button btnThu;
    private TableView<Object[]> tblPhieuThu;
    private ObservableList<Object[]> tblItems;

    private final PhieuThuDAO phieuThuDAO     = new PhieuThuDAO();
    private final PhieuDatPhongDAO pdpDAO      = new PhieuDatPhongDAO();
    private final PhieuThuService phieuThuSvc  = new PhieuThuService();

    public Node build() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #F0F2F5;");

        VBox content = new VBox(20);
        content.setPadding(new Insets(28));
        VBox.setVgrow(content, Priority.ALWAYS);

        // ── Header
        VBox header = new VBox(4);
        Label title = new Label("Phiếu Thu Đặt Cọc");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Label sub = new Label("Thu tiền đặt cọc từ khách khi đặt phòng — lưu vào sổ cọc để đối chiếu khi checkout");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #8C8C8C;");
        header.getChildren().addAll(title, sub);

        // TabPane — mỗi tab 1 chức năng
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: transparent;");

        Tab tabList = new Tab("📋  Danh Sách Phiếu Thu");
        VBox listWrapper = new VBox(buildListCard());
        listWrapper.setPadding(new Insets(10, 0, 0, 0));
        VBox.setVgrow(listWrapper, Priority.ALWAYS);
        tabList.setContent(listWrapper);

        Tab tabCreate = new Tab("➕  Tạo Phiếu Thu Cọc");
        VBox formWrapper = new VBox(buildFormCard());
        formWrapper.setPadding(new Insets(10, 0, 0, 0));
        formWrapper.setAlignment(Pos.TOP_CENTER);
        tabCreate.setContent(formWrapper);

        tabPane.getTabs().addAll(tabList, tabCreate);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        content.getChildren().addAll(header, tabPane);
        root.getChildren().add(content);
        VBox.setVgrow(root, Priority.ALWAYS);

        loadPhieuChuaCheckIn();
        loadAllPhieuThu();

        return root;
    }

    // ─────────────────────────── LEFT: Form thu cọc
    private VBox buildFormCard() {
        VBox card = new VBox(16);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        Label cardTitle = new Label("Tạo Phiếu Thu Cọc");
        cardTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;"
                + "-fx-border-color: transparent transparent #F0F2F5 transparent;"
                + "-fx-border-width: 0 0 1 0; -fx-padding: 0 0 10 0;");

        GridPane form = new GridPane();
        form.setHgap(12); form.setVgap(14);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(40);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(60);
        form.getColumnConstraints().addAll(c1, c2);

        // Phiếu đặt phòng
        cbPhieu = new ComboBox<>();
        cbPhieu.setMaxWidth(Double.MAX_VALUE);
        cbPhieu.setPromptText("Chọn phiếu đặt phòng...");
        cbPhieu.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D9D9D9;"
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1;");
        cbPhieu.setOnAction(e -> fillPhieuInfo());

        // Info labels (auto-fill khi chọn phiếu)
        lblKhach   = infoLabel("—");
        lblPhong   = infoLabel("—");
        lblNgayDat = infoLabel("—");

        // Số tiền cọc — tự động tính 50%, không cho nhập tay
        txtSoTienCoc = new TextField();
        txtSoTienCoc.setPromptText("Chọn phiếu để tự động tính...");
        txtSoTienCoc.setMaxWidth(Double.MAX_VALUE);
        txtSoTienCoc.setEditable(false);
        txtSoTienCoc.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #D9D9D9;"
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1; -fx-padding: 7 10;"
                + "-fx-text-fill: #1A1A2E; -fx-font-weight: bold;");

        // Phương thức
        cbPhuongThuc = new ComboBox<>();
        cbPhuongThuc.getItems().addAll("Tiền Mặt", "Chuyển Khoản");
        cbPhuongThuc.setValue("Tiền Mặt");
        cbPhuongThuc.setMaxWidth(Double.MAX_VALUE);
        cbPhuongThuc.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D9D9D9;"
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1;");

        // Ghi chú
        txtGhiChu = new TextField();
        txtGhiChu.setPromptText("Ghi chú tùy chọn...");
        txtGhiChu.setMaxWidth(Double.MAX_VALUE);
        txtGhiChu.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D9D9D9;"
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1; -fx-padding: 7 10;");

        form.add(lbl("Phiếu đặt phòng:"), 0, 0); form.add(cbPhieu,       1, 0);
        form.add(lbl("Khách hàng:"),      0, 1); form.add(lblKhach,       1, 1);
        form.add(lbl("Phòng:"),           0, 2); form.add(lblPhong,       1, 2);
        form.add(lbl("Ngày đặt:"),        0, 3); form.add(lblNgayDat,     1, 3);
        form.add(lbl("Số tiền cọc (đ):"), 0, 4); form.add(txtSoTienCoc,   1, 4);
        form.add(lbl("Phương thức:"),     0, 5); form.add(cbPhuongThuc,   1, 5);
        form.add(lbl("Ghi chú:"),         0, 6); form.add(txtGhiChu,      1, 6);

        btnThu = new Button("💳  Thu Tiền Cọc");
        btnThu.setStyle("-fx-background-color: #1890FF; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-size: 13px;"
                + "-fx-font-weight: bold; -fx-cursor: hand;");
        btnThu.setMaxWidth(Double.MAX_VALUE);
        btnThu.setDisable(true);
        btnThu.setOnAction(e -> handleThuCoc());

        card.getChildren().addAll(cardTitle, form, btnThu);
        return card;
    }

    // ─────────────────────────── RIGHT: List all phiếu thu
    private VBox buildListCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");
        VBox.setVgrow(card, Priority.ALWAYS);

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label cardTitle = new Label("Danh Sách Phiếu Thu Cọc");
        cardTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button btnReload = new Button("↻");
        btnReload.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #D9D9D9;"
                + "-fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6;"
                + "-fx-padding: 4 10; -fx-cursor: hand;");
        btnReload.setOnAction(e -> loadAllPhieuThu());
        titleRow.getChildren().addAll(cardTitle, sp, btnReload);

        tblPhieuThu = new TableView<>();
        tblPhieuThu.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblPhieuThu.setStyle("-fx-background-color: #F9F9F9; -fx-border-color: #E8E8E8;"
                + "-fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");
        tblPhieuThu.setPlaceholder(new Label("Chưa có phiếu thu cọc nào."));
        VBox.setVgrow(tblPhieuThu, Priority.ALWAYS);

        String[] heads = {"Mã Phiếu Thu", "Mã Phiếu ĐP", "Tiền Cọc", "Phương Thức", "Ngày Thu", "Ghi Chú"};
        for (int i = 0; i < heads.length; i++) {
            final int idx = i;
            TableColumn<Object[], String> col = new TableColumn<>(heads[i]);
            col.setCellValueFactory(p -> {
                Object v = idx < p.getValue().length ? p.getValue()[idx] : "";
                return new SimpleStringProperty(v != null ? v.toString() : "");
            });
            tblPhieuThu.getColumns().add(col);
        }
        tblItems = FXCollections.observableArrayList();
        tblPhieuThu.setItems(tblItems);

        card.getChildren().addAll(titleRow, tblPhieuThu);
        return card;
    }

    // ─────────────────────────── DATA LOGIC
    private void loadPhieuChuaCheckIn() {
        cbPhieu.getItems().clear();
        cbPhieu.getItems().addAll(phieuThuSvc.loadPhieuChuaCheckIn());
    }

    private void fillPhieuInfo() {
        String maPDP = cbPhieu.getValue();
        if (maPDP == null) return;
        try {
            PhieuDatPhong found = null;
            for (PhieuDatPhong p : pdpDAO.getAll()) {
                if (p.getMaPhieuDatPhong().equals(maPDP)) { found = p; break; }
            }
            if (found == null) return;

            // Fix: tên KH thay vì mã KH
            String maKH = found.getMaKhachHang() != null ? found.getMaKhachHang() : "—";
            lblKhach.setText(new KhachHangDAO().getTenKhach(maKH));
            lblNgayDat.setText(found.getNgayDat() != null
                    ? found.getNgayDat().toString().substring(0, 16) : "—");
            lblPhong.setText(phieuThuDAO.getPhongsByPhieu(maPDP));

            // Guard: phiếu đã thu cọc
            if (phieuThuDAO.daDuocThuCoc(maPDP)) {
                txtSoTienCoc.setText("Đã thu cọc");
                btnThu.setText("⚠  Phiếu đã được thu cọc");
                btnThu.setStyle("-fx-background-color: #D9D9D9; -fx-text-fill: #595959;"
                        + "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-size: 13px;"
                        + "-fx-font-weight: bold;");
                btnThu.setDisable(true);
                return;
            }

            // Reset nút về trạng thái bình thường
            btnThu.setText("💳  Thu Tiền Cọc");
            btnThu.setStyle("-fx-background-color: #1890FF; -fx-text-fill: white;"
                    + "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-size: 13px;"
                    + "-fx-font-weight: bold; -fx-cursor: hand;");

            // Auto-tính 50% cọc = 50% × donGia × số đêm
            String maPhong = pdpDAO.getMaPhong(maPDP);
            double donGia = maPhong.isEmpty() ? 0 : new BangGiaDAO().getDonGiaQuaDem(maPhong);
            long soNgay = 1;
            if (found.getThoiGianNhanDuKien() != null && found.getThoiGianTraDuKien() != null) {
                soNgay = Math.max(1, (found.getThoiGianTraDuKien().getTime()
                        - found.getThoiGianNhanDuKien().getTime()) / (24L * 3_600_000));
            }
            if (donGia > 0) {
                long coc = (long) (0.5 * donGia * soNgay);
                txtSoTienCoc.setText(String.valueOf(coc));
                btnThu.setDisable(false);
            } else {
                txtSoTienCoc.setText("0");
                btnThu.setDisable(true);
            }
        } catch (Exception ignored) {}
    }

    private void loadAllPhieuThu() {
        tblItems.clear();
        tblItems.addAll(phieuThuDAO.loadAllPhieuThu());
    }

    private void handleThuCoc() {
        String maPDP = cbPhieu.getValue();
        if (maPDP == null || maPDP.isBlank()) { alert("Chọn phiếu đặt phòng!"); return; }

        double soTienCoc;
        try {
            soTienCoc = Double.parseDouble(txtSoTienCoc.getText().trim().replace(",", ""));
            if (soTienCoc <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            alert("Số tiền cọc không hợp lệ! Nhập số nguyên dương."); return;
        }

        String phuongThucDB = "Chuyển Khoản".equals(cbPhuongThuc.getValue()) ? "ChuyenKhoan" : "TienMat";
        String ghiChuVal = txtGhiChu.getText().trim();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Xác nhận thu cọc:\n"
                + "Phiếu đặt phòng: " + maPDP + "\n"
                + "Số tiền cọc: " + MONEY.format(soTienCoc) + "đ\n"
                + "Phương thức: " + cbPhuongThuc.getValue());
        confirm.setHeaderText("Xác Nhận Thu Tiền Cọc");
        confirm.setTitle("Thu Cọc");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK) return;
            try {
                String maPT = phieuThuSvc.thuCoc(maPDP, soTienCoc, phuongThucDB, ghiChuVal);
                loadAllPhieuThu();
                txtSoTienCoc.clear(); txtGhiChu.clear();
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setHeaderText(null); a.setTitle("Thành Công");
                a.setContentText("✅ Thu cọc thành công!\nMã phiếu thu: " + maPT
                        + "\nSố tiền: " + MONEY.format(soTienCoc) + "đ");
                a.showAndWait();
            } catch (IllegalStateException ex) {
                alert("Lỗi: " + ex.getMessage());
            } catch (Exception ex) {
                alert("Lỗi tạo phiếu thu: " + ex.getMessage());
            }
        });
    }

    // ─────────────────────────── HELPERS
    private Label lbl(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #595959;");
        return l;
    }

    private Label infoLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: #1890FF; -fx-font-weight: bold;");
        return l;
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg);
        a.setHeaderText(null); a.setTitle("Thông báo"); a.showAndWait();
    }
}
