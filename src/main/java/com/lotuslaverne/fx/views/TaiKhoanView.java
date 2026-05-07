package com.lotuslaverne.fx.views;

import com.lotuslaverne.dao.TaiKhoanDAO;
import com.lotuslaverne.entity.TaiKhoan;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

public class TaiKhoanView {

    private final String activeUsername;
    private final String currentVaiTro;
    private final boolean isQuanLy;
    private ObservableList<Object[]> items;
    private TableView<Object[]> table;
    private String activeMaTK = "";

    public TaiKhoanView(String username, String vaiTro) {
        this.activeUsername = username;
        this.currentVaiTro = vaiTro;
        this.isQuanLy = "Quản Lý".equals(vaiTro) || "QuanLy".equals(vaiTro);
    }

    public TaiKhoanView(String username) { this(username, "QuanLy"); }

    public Node build() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #F0F2F5;");

        VBox content = new VBox(20);
        content.setPadding(new Insets(28));
        content.setStyle("-fx-background-color: #F0F2F5;");

        // Header
        VBox header = new VBox(4);
        Label title = new Label("Tài Khoản Hệ Thống");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Label sub = new Label("Quản lý tài khoản đăng nhập và phân quyền");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #8C8C8C;");
        header.getChildren().addAll(title, sub);

        // TabPane — mỗi tab 1 chức năng
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: transparent;");

        Tab tabList = new Tab("📋  Danh Sách Tài Khoản");
        tabList.setContent(buildTableTab());

        Tab tabCreate = new Tab("➕  Tạo Tài Khoản");
        tabCreate.setContent(buildCreateTab());

        Tab tabPass = new Tab("🔑  Đổi Mật Khẩu");
        tabPass.setContent(buildChangePassTab());

        tabPane.getTabs().addAll(tabList, tabCreate, tabPass);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        content.getChildren().addAll(header, tabPane);
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #F0F2F5; -fx-border-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        root.getChildren().add(scroll);
        return root;
    }

    // ═══════════════════════════════════════════ TAB 1: DANH SÁCH
    private Node buildTableTab() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        HBox cardHeader = new HBox(8);
        cardHeader.setAlignment(Pos.CENTER_LEFT);
        Label cardTitle = new Label("Danh Sách Tài Khoản");
        cardTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        Button btnRefresh = new Button("↻ Làm Mới");
        btnRefresh.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #595959;"
                + "-fx-background-radius: 8; -fx-border-color: #D9D9D9; -fx-border-width: 1;"
                + "-fx-border-radius: 8; -fx-padding: 7 16; -fx-cursor: hand;");
        btnRefresh.setOnAction(e -> { refresh(); com.lotuslaverne.fx.UiUtils.flashButton(btnRefresh, "✓ OK"); });

        Button btnXoa = new Button("🗑 Xóa Tài Khoản Đã Chọn");
        btnXoa.setStyle("-fx-background-color: #FF4D4F; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-padding: 7 16; -fx-cursor: hand; -fx-font-size: 12px;");
        btnXoa.setOnAction(e -> handleXoa());
        cardHeader.getChildren().addAll(cardTitle, sp, btnRefresh, btnXoa);

        items = FXCollections.observableArrayList(loadData());
        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        card.getChildren().addAll(cardHeader, table);
        VBox wrapper = new VBox(card);
        wrapper.setPadding(new Insets(10, 0, 0, 0));
        VBox.setVgrow(card, Priority.ALWAYS);
        return wrapper;
    }

    // ═══════════════════════════════════════════ TAB 2: TẠO TÀI KHOẢN
    private Node buildCreateTab() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(24));
        card.setMaxWidth(500);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        Label cardTitle = new Label("Tạo Tài Khoản Mới");
        cardTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;"
                + "-fx-border-color: transparent transparent #F0F2F5 transparent;"
                + "-fx-border-width: 0 0 1 0; -fx-padding: 0 0 10 0;");

        GridPane form = new GridPane();
        form.setHgap(14); form.setVgap(12);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPrefWidth(160);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(c1, c2);

        TextField txtMaTK = field(""); txtMaTK.setPromptText("Tự sinh nếu để trống");
        TextField txtMaNV = field("");
        TextField txtTenDN = field("");
        PasswordField txtMK = new PasswordField(); txtMK.setMaxWidth(Double.MAX_VALUE); txtMK.setStyle(fieldStyle());
        ComboBox<String> cbVT = new ComboBox<>();
        if (isQuanLy) cbVT.getItems().addAll("LeTan", "QuanLy");
        else cbVT.getItems().add("LeTan");
        cbVT.setValue("LeTan"); cbVT.setMaxWidth(Double.MAX_VALUE);
        cbVT.setStyle("-fx-background-color:#FFF;-fx-border-color:#D9D9D9;-fx-border-radius:6;-fx-background-radius:6;-fx-border-width:1;");

        int r = 0;
        form.add(lbl("Mã tài khoản:"), 0, r); form.add(txtMaTK, 1, r++);
        form.add(lbl("Mã nhân viên *:"), 0, r); form.add(txtMaNV, 1, r++);
        form.add(lbl("Tên đăng nhập *:"), 0, r); form.add(txtTenDN, 1, r++);
        form.add(lbl("Mật khẩu *:"), 0, r); form.add(txtMK, 1, r++);
        form.add(lbl("Vai trò:"), 0, r); form.add(cbVT, 1, r++);

        Label errLbl = errLabel();
        Button btnTao = new Button("Tạo Tài Khoản");
        btnTao.setStyle("-fx-background-color: #52C41A; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-padding: 10 24; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 13px;");
        btnTao.setOnAction(e -> {
            String tenDN = txtTenDN.getText().trim(), mk = txtMK.getText(), maNV = txtMaNV.getText().trim();
            if (tenDN.isEmpty() || mk.isEmpty()) { showErr(errLbl, "Tên đăng nhập và mật khẩu không được trống!"); return; }
            if (maNV.isEmpty()) { showErr(errLbl, "Vui lòng nhập Mã Nhân Viên!"); return; }
            String vaiTroChon = cbVT.getValue();
            try {
                java.sql.Connection con = com.lotuslaverne.util.ConnectDB.getInstance().getConnection();
                if (con == null) { showErr(errLbl, "Không kết nối được CSDL!"); return; }
                try (java.sql.PreparedStatement pst = con.prepareStatement("SELECT vaiTro FROM NhanVien WHERE maNhanVien = ?")) {
                    pst.setString(1, maNV);
                    try (java.sql.ResultSet rs = pst.executeQuery()) {
                        if (!rs.next()) { showErr(errLbl, "⛔ Mã nhân viên \"" + maNV + "\" không tồn tại!"); return; }
                        String vaiTroNV = rs.getString("vaiTro");
                        if (!vaiTroChon.equals(vaiTroNV)) {
                            showErr(errLbl, "⛔ NV " + maNV + " vai trò \"" + ("QuanLy".equals(vaiTroNV)?"Quản Lý":"Lễ Tân")
                                    + "\" → không thể tạo TK \"" + vaiTroChon + "\"!");
                            return;
                        }
                    }
                }
                try (java.sql.PreparedStatement pst2 = con.prepareStatement("SELECT maTaiKhoan FROM TaiKhoan WHERE maNhanVien = ?")) {
                    pst2.setString(1, maNV);
                    try (java.sql.ResultSet rs2 = pst2.executeQuery()) {
                        if (rs2.next()) { showErr(errLbl, "⛔ NV " + maNV + " đã có TK \"" + rs2.getString(1) + "\"!"); return; }
                    }
                }
            } catch (Exception ex) { showErr(errLbl, "Lỗi DB: " + ex.getMessage()); return; }

            String maTK = txtMaTK.getText().trim();
            if (maTK.isEmpty()) maTK = "TK" + (System.currentTimeMillis() % 100000);
            try {
                if (new TaiKhoanDAO().taoTaiKhoan(new TaiKhoan(maTK, maNV, vaiTroChon, tenDN, mk))) {
                    errLbl.setVisible(false); errLbl.setManaged(false);
                    alert(Alert.AlertType.INFORMATION, "Thành công", "Tạo tài khoản thành công!\nMã TK: " + maTK);
                    txtMaTK.clear(); txtMaNV.clear(); txtTenDN.clear(); txtMK.clear();
                    refresh();
                } else { showErr(errLbl, "Mã TK hoặc tên đăng nhập đã tồn tại!"); }
            } catch (Exception ex) { showErr(errLbl, "Lỗi DB: " + ex.getMessage()); }
        });

        card.getChildren().addAll(cardTitle, form, errLbl, btnTao);
        VBox wrapper = new VBox(card);
        wrapper.setPadding(new Insets(10, 0, 0, 0));
        wrapper.setAlignment(Pos.TOP_CENTER);
        return wrapper;
    }

    // ═══════════════════════════════════════════ TAB 3: ĐỔI MẬT KHẨU
    private Node buildChangePassTab() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(24));
        card.setMaxWidth(500);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        Label cardTitle = new Label("Đổi Mật Khẩu");
        cardTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;"
                + "-fx-border-color: transparent transparent #F0F2F5 transparent;"
                + "-fx-border-width: 0 0 1 0; -fx-padding: 0 0 10 0;");

        Label lblChon = lbl("Chọn tài khoản *:");
        java.util.LinkedHashMap<String, String> tkMap = new java.util.LinkedHashMap<>();
        String defaultDisplay = activeUsername + " (đang đăng nhập)";
        try {
            for (TaiKhoan tk : new TaiKhoanDAO().getAll()) {
                String display;
                if (tk.getTenDangNhap().equalsIgnoreCase(activeUsername)) {
                    display = tk.getTenDangNhap() + " — " + tk.getVaiTro() + "  ★ đang đăng nhập";
                    activeMaTK = tk.getMaTaiKhoan(); defaultDisplay = display;
                } else {
                    display = tk.getTenDangNhap() + " — " + tk.getVaiTro();
                }
                tkMap.put(display, tk.getMaTaiKhoan());
            }
        } catch (Exception ignored) {}

        ComboBox<String> cbTaiKhoan = new ComboBox<>();
        cbTaiKhoan.getItems().addAll(tkMap.keySet());
        cbTaiKhoan.setValue(defaultDisplay);
        cbTaiKhoan.setMaxWidth(Double.MAX_VALUE);
        cbTaiKhoan.setStyle("-fx-background-color:#FFF;-fx-border-color:#D9D9D9;-fx-border-radius:6;-fx-background-radius:6;-fx-border-width:1;");

        final String[] selectedMaTK = { activeMaTK };
        cbTaiKhoan.setOnAction(e -> {
            String sel = cbTaiKhoan.getValue();
            if (sel != null && tkMap.containsKey(sel)) selectedMaTK[0] = tkMap.get(sel);
        });

        GridPane form = new GridPane();
        form.setHgap(14); form.setVgap(12);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPrefWidth(180);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(c1, c2);

        PasswordField txtCu = new PasswordField(), txtMoi = new PasswordField(), txtXN = new PasswordField();
        for (PasswordField pf : new PasswordField[]{txtCu, txtMoi, txtXN}) {
            pf.setMaxWidth(Double.MAX_VALUE); pf.setStyle(fieldStyle());
        }
        form.add(lbl("Mật khẩu hiện tại *:"), 0, 0); form.add(txtCu, 1, 0);
        form.add(lbl("Mật khẩu mới *:"), 0, 1); form.add(txtMoi, 1, 1);
        form.add(lbl("Xác nhận mật khẩu *:"), 0, 2); form.add(txtXN, 1, 2);

        Label errLbl = errLabel();
        Button btnDoi = new Button("Đổi Mật Khẩu");
        btnDoi.setStyle("-fx-background-color: #1890FF; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-padding: 10 24; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 13px;");
        btnDoi.setOnAction(e -> {
            String mkCu = txtCu.getText(), mkMoi = txtMoi.getText(), mkXN = txtXN.getText();
            if (mkCu.isEmpty()) { showErr(errLbl, "Vui lòng nhập mật khẩu hiện tại!"); return; }
            if (!mkMoi.equals(mkXN)) { showErr(errLbl, "Mật khẩu xác nhận không khớp!"); return; }
            if (mkMoi.length() < 4) { showErr(errLbl, "Mật khẩu mới phải có ít nhất 4 ký tự!"); return; }
            if (selectedMaTK[0] == null || selectedMaTK[0].isEmpty()) { showErr(errLbl, "Vui lòng chọn tài khoản!"); return; }
            try {
                if (new TaiKhoanDAO().doiMatKhau(selectedMaTK[0], mkCu, mkMoi)) {
                    errLbl.setVisible(false); errLbl.setManaged(false);
                    alert(Alert.AlertType.INFORMATION, "Thành công", "Đổi mật khẩu thành công!");
                    txtCu.clear(); txtMoi.clear(); txtXN.clear();
                } else { showErr(errLbl, "Mật khẩu hiện tại không đúng hoặc lỗi hệ thống!"); }
            } catch (Exception ex) { showErr(errLbl, "Lỗi DB: " + ex.getMessage()); }
        });

        card.getChildren().addAll(cardTitle, lblChon, cbTaiKhoan, form, errLbl, btnDoi);
        VBox wrapper = new VBox(card);
        wrapper.setPadding(new Insets(10, 0, 0, 0));
        wrapper.setAlignment(Pos.TOP_CENTER);
        return wrapper;
    }

    // ═══════════════════════════════════════════ TABLE
    private TableView<Object[]> buildTable() {
        TableView<Object[]> tbl = new TableView<>();
        tbl.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tbl.setStyle("-fx-background-color: #F9F9F9; -fx-border-color: #E8E8E8;"
                + "-fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");
        String[] heads = {"Mã TK", "Mã NV", "Vai Trò", "Tên Đăng Nhập"};
        for (int i = 0; i < heads.length; i++) {
            final int idx = i;
            TableColumn<Object[], String> col = new TableColumn<>(heads[i]);
            col.setCellValueFactory(p -> {
                Object v = idx < p.getValue().length ? p.getValue()[idx] : "";
                return new SimpleStringProperty(v != null ? v.toString() : "");
            });
            if (i == 2) {
                col.setCellFactory(tc -> new TableCell<>() {
                    @Override protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) { setGraphic(null); setText(null); return; }
                        Label badge = new Label(item);
                        boolean isQL = "QuanLy".equals(item);
                        badge.setStyle("-fx-background-color: " + (isQL ? "#E6F4FF" : "#F6FFED")
                                + "; -fx-text-fill: " + (isQL ? "#1890FF" : "#52C41A")
                                + "; -fx-padding: 2 8 2 8; -fx-background-radius: 10;"
                                + " -fx-font-size: 11px; -fx-font-weight: bold;");
                        setGraphic(badge); setText(null);
                    }
                });
            }
            tbl.getColumns().add(col);
        }
        tbl.setItems(items);
        tbl.setPlaceholder(new Label("Không có tài khoản nào."));
        return tbl;
    }

    // ═══════════════════════════════════════════ ACTIONS
    private void handleXoa() {
        Object[] sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { alert(Alert.AlertType.WARNING, "Chưa chọn", "Chọn tài khoản cần xóa!"); return; }
        String maTK = sel[0].toString();
        if (maTK.equals(activeMaTK)) {
            alert(Alert.AlertType.WARNING, "Không thể xóa", "Không thể xóa tài khoản đang đăng nhập!"); return;
        }
        new Alert(Alert.AlertType.CONFIRMATION, "Xóa tài khoản \"" + sel[3] + "\"?").showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    if (new TaiKhoanDAO().xoa(maTK)) refresh();
                    else alert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa.");
                } catch (Exception ex) { alert(Alert.AlertType.ERROR, "Lỗi DB", ex.getMessage()); }
            }
        });
    }

    private void refresh() { items.setAll(loadData()); }

    private List<Object[]> loadData() {
        List<Object[]> result = new ArrayList<>();
        try {
            for (TaiKhoan tk : new TaiKhoanDAO().getAll()) {
                if (tk.getTenDangNhap().equalsIgnoreCase(activeUsername)) activeMaTK = tk.getMaTaiKhoan();
                result.add(new Object[]{tk.getMaTaiKhoan(), tk.getMaNhanVien(), tk.getVaiTro(), tk.getTenDangNhap()});
            }
            if (!result.isEmpty()) return result;
        } catch (Exception ignored) {}
        result.add(new Object[]{"TK001", "NV001", "QuanLy", activeUsername});
        return result;
    }

    // ═══════════════════════════════════════════ HELPERS
    private TextField field(String val) { TextField tf = new TextField(val); tf.setMaxWidth(Double.MAX_VALUE); tf.setStyle(fieldStyle()); return tf; }
    private String fieldStyle() { return "-fx-background-color:#FFF;-fx-border-color:#D9D9D9;-fx-border-radius:6;-fx-background-radius:6;-fx-border-width:1;-fx-padding:7 10;"; }
    private Label lbl(String t) { Label l = new Label(t); l.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #595959;"); return l; }
    private Label errLabel() { Label l = new Label(); l.setStyle("-fx-text-fill: #FF4D4F; -fx-font-size: 12px;"); l.setVisible(false); l.setManaged(false); l.setWrapText(true); return l; }
    private void showErr(Label lbl, String msg) { lbl.setText(msg); lbl.setVisible(true); lbl.setManaged(true); }
    private void alert(Alert.AlertType type, String title, String msg) { Alert a = new Alert(type, msg); a.setHeaderText(null); a.setTitle(title); a.showAndWait(); }
}
