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

    public TaiKhoanView(String username) {
        this(username, "QuanLy");
    }

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
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button btnRefresh = new Button("↻ Làm Mới");
        btnRefresh.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #595959;"
                + "-fx-background-radius: 8; -fx-border-color: #D9D9D9; -fx-border-width: 1;"
                + "-fx-border-radius: 8; -fx-padding: 7 16; -fx-cursor: hand;");
        btnRefresh.setOnAction(e -> {
            refresh();
            com.lotuslaverne.fx.UiUtils.flashButton(btnRefresh, "✓ OK");
        });

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
        VBox card = new VBox(18);
        card.setPadding(new Insets(28));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),8,0,0,2);");

        // ── Card title with icon ──
        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label iconTitle = new Label("👤");
        iconTitle.setStyle("-fx-font-size: 20px;");
        Label cardTitle = new Label("Tạo Tài Khoản Mới");
        cardTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        titleRow.getChildren().addAll(iconTitle, cardTitle);

        // Separator
        Region sep = new Region();
        sep.setMinHeight(1);
        sep.setMaxHeight(1);
        sep.setStyle("-fx-background-color: #F0F2F5;");

        // ── Form grid: 2 columns, labels ON TOP of fields ──
        GridPane form = new GridPane();
        form.setHgap(20);
        form.setVgap(16);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        form.getColumnConstraints().addAll(col1, col2);

        // --- Fields ---
        TextField txtMaTK = field("");
        txtMaTK.setPromptText("👤  Tự sinh nếu để trống");

        // Mã nhân viên → ComboBox dropdown (load from DB)
        ComboBox<String> cbMaNV = new ComboBox<>();
        cbMaNV.setEditable(true);
        cbMaNV.setPromptText("Chọn nhân viên");
        cbMaNV.setMaxWidth(Double.MAX_VALUE);
        cbMaNV.setStyle(
                "-fx-background-color:#FFF;-fx-border-color:#D9D9D9;-fx-border-radius:8;-fx-background-radius:8;"
                        + "-fx-border-width:1;-fx-padding:4 6;-fx-font-size:13px;");
        try {
            for (com.lotuslaverne.entity.NhanVien nv : new com.lotuslaverne.dao.NhanVienDAO().getAll()) {
                cbMaNV.getItems().add(nv.getMaNhanVien() + " — " + nv.getTenNhanVien());
            }
        } catch (Exception ignored) {
        }

        TextField txtTenDN = field("");
        txtTenDN.setPromptText("👤  Nhập tên đăng nhập");

        // Mật khẩu with toggle visibility
        PasswordField txtMK = new PasswordField();
        txtMK.setPromptText("🔒  Nhập mật khẩu");
        txtMK.setMaxWidth(Double.MAX_VALUE);
        txtMK.setStyle(fieldStyle());
        TextField txtMKVisible = new TextField();
        txtMKVisible.setPromptText("🔒  Nhập mật khẩu");
        txtMKVisible.setMaxWidth(Double.MAX_VALUE);
        txtMKVisible.setStyle(fieldStyle());
        txtMKVisible.setVisible(false);
        txtMKVisible.setManaged(false);
        txtMK.textProperty().bindBidirectional(txtMKVisible.textProperty());

        Button btnEye = new Button("👁");
        btnEye.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 14px; -fx-padding: 4 8;");
        btnEye.setOnAction(ev -> {
            boolean showing = txtMKVisible.isVisible();
            txtMK.setVisible(showing);
            txtMK.setManaged(showing);
            txtMKVisible.setVisible(!showing);
            txtMKVisible.setManaged(!showing);
            btnEye.setText(showing ? "👁" : "🙈");
        });
        StackPane passStack = new StackPane(txtMK, txtMKVisible);
        HBox passBox = new HBox(0, passStack, btnEye);
        passBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(passStack, Priority.ALWAYS);

        ComboBox<String> cbVT = new ComboBox<>();
        if (isQuanLy)
            cbVT.getItems().addAll("LeTan", "QuanLy");
        else
            cbVT.getItems().add("LeTan");
        cbVT.setPromptText("Chọn vai trò");
        cbVT.setMaxWidth(Double.MAX_VALUE);
        cbVT.setStyle("-fx-background-color:#FFF;-fx-border-color:#D9D9D9;-fx-border-radius:8;-fx-background-radius:8;"
                + "-fx-border-width:1;-fx-padding:4 6;-fx-font-size:13px;");

        // Row 0: Labels
        Label lblMaTK = formLabel("Mã tài khoản");
        Label lblMaNV = formLabel("Mã nhân viên *");
        form.add(lblMaTK, 0, 0);
        form.add(lblMaNV, 1, 0);

        // Row 1: Mã tài khoản | Mã nhân viên (ComboBox)
        form.add(txtMaTK, 0, 1);
        form.add(cbMaNV, 1, 1);

        // Row 2: Labels
        Label lblTenDN = formLabel("Tên đăng nhập *");
        Label lblMK = formLabel("Mật khẩu *");
        form.add(lblTenDN, 0, 2);
        form.add(lblMK, 1, 2);

        // Row 3: Tên đăng nhập | Mật khẩu
        form.add(txtTenDN, 0, 3);
        form.add(passBox, 1, 3);

        // Row 4: Vai trò label (full width)
        Label lblVT = formLabel("Vai trò *");
        form.add(lblVT, 0, 4, 2, 1);

        // Row 5: Vai trò ComboBox (full width)
        form.add(cbVT, 0, 5, 2, 1);

        // ── Info note ──
        HBox infoNote = new HBox(8);
        infoNote.setAlignment(Pos.CENTER_LEFT);
        infoNote.setPadding(new Insets(10, 16, 10, 16));
        infoNote.setStyle(
                "-fx-background-color: #F0F5FF; -fx-background-radius: 8; -fx-border-color: #D6E4FF; -fx-border-radius: 8; -fx-border-width: 1;");
        Label infoIcon = new Label("⚠");
        infoIcon.setStyle("-fx-text-fill: #1890FF; -fx-font-size: 15px; -fx-font-weight: bold;");
        Label infoText = new Label("Lưu ý: Các trường có dấu (*) là bắt buộc.");
        infoText.setStyle("-fx-text-fill: #595959; -fx-font-size: 12.5px;");
        infoNote.getChildren().addAll(infoIcon, infoText);

        // ── Error label ──
        Label errLbl = errLabel();

        // ── Buttons row: Hủy + Tạo tài khoản ──
        Region btnSpacer = new Region();
        HBox.setHgrow(btnSpacer, Priority.ALWAYS);

        Button btnHuy = new Button("✕  Hủy");
        btnHuy.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #595959;"
                + "-fx-background-radius: 8; -fx-border-color: #D9D9D9; -fx-border-width: 1;"
                + "-fx-border-radius: 8; -fx-padding: 10 28; -fx-cursor: hand; -fx-font-size: 13px;");
        btnHuy.setOnAction(e -> {
            txtMaTK.clear();
            cbMaNV.setValue(null);
            cbMaNV.getEditor().clear();
            txtTenDN.clear();
            txtMK.clear();
            txtMKVisible.clear();
            cbVT.setValue(null);
            errLbl.setVisible(false);
            errLbl.setManaged(false);
        });

        Button btnTao = new Button("👤  Tạo tài khoản");
        btnTao.setStyle("-fx-background-color: #1890FF; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-padding: 10 28; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 13px;");
        btnTao.setOnAction(e -> {
            // Extract maNV from ComboBox selection
            String selectedNV = cbMaNV.getValue();
            String maNV = "";
            if (selectedNV != null && !selectedNV.trim().isEmpty()) {
                maNV = selectedNV.contains(" — ") ? selectedNV.split(" — ")[0].trim() : selectedNV.trim();
            }
            String tenDN = txtTenDN.getText().trim();
            String mk = txtMK.getText();
            if (tenDN.isEmpty() || mk.isEmpty()) {
                showErr(errLbl, "Tên đăng nhập và mật khẩu không được trống!");
                return;
            }
            if (maNV.isEmpty()) {
                showErr(errLbl, "Vui lòng chọn Nhân Viên!");
                return;
            }
            String vaiTroChon = cbVT.getValue();
            if (vaiTroChon == null || vaiTroChon.isEmpty()) {
                showErr(errLbl, "Vui lòng chọn Vai Trò!");
                return;
            }
            try {
                java.sql.Connection con = com.lotuslaverne.util.ConnectDB.getInstance().getConnection();
                if (con == null) {
                    showErr(errLbl, "Không kết nối được CSDL!");
                    return;
                }
                try (java.sql.PreparedStatement pst = con
                        .prepareStatement("SELECT vaiTro FROM NhanVien WHERE maNhanVien = ?")) {
                    pst.setString(1, maNV);
                    try (java.sql.ResultSet rs = pst.executeQuery()) {
                        if (!rs.next()) {
                            showErr(errLbl, "⛔ Mã nhân viên \"" + maNV + "\" không tồn tại!");
                            return;
                        }
                        String vaiTroNV = rs.getString("vaiTro");
                        if (!vaiTroChon.equals(vaiTroNV)) {
                            showErr(errLbl,
                                    "⛔ NV " + maNV + " vai trò \"" + ("QuanLy".equals(vaiTroNV) ? "Quản Lý" : "Lễ Tân")
                                            + "\" → không thể tạo TK \"" + vaiTroChon + "\"!");
                            return;
                        }
                    }
                }
                try (java.sql.PreparedStatement pst2 = con
                        .prepareStatement("SELECT maTaiKhoan FROM TaiKhoan WHERE maNhanVien = ?")) {
                    pst2.setString(1, maNV);
                    try (java.sql.ResultSet rs2 = pst2.executeQuery()) {
                        if (rs2.next()) {
                            showErr(errLbl, "⛔ NV " + maNV + " đã có TK \"" + rs2.getString(1) + "\"!");
                            return;
                        }
                    }
                }
            } catch (Exception ex) {
                showErr(errLbl, "Lỗi DB: " + ex.getMessage());
                return;
            }

            String maTK = txtMaTK.getText().trim();
            if (maTK.isEmpty())
                maTK = "TK" + (System.currentTimeMillis() % 100000);
            try {
                if (new TaiKhoanDAO().taoTaiKhoan(new TaiKhoan(maTK, maNV, vaiTroChon, tenDN, mk))) {
                    errLbl.setVisible(false);
                    errLbl.setManaged(false);
                    alert(Alert.AlertType.INFORMATION, "Thành công", "Tạo tài khoản thành công!\nMã TK: " + maTK);
                    txtMaTK.clear();
                    cbMaNV.setValue(null);
                    cbMaNV.getEditor().clear();
                    txtTenDN.clear();
                    txtMK.clear();
                    txtMKVisible.clear();
                    cbVT.setValue(null);
                    refresh();
                } else {
                    showErr(errLbl, "Mã TK hoặc tên đăng nhập đã tồn tại!");
                }
            } catch (Exception ex) {
                showErr(errLbl, "Lỗi DB: " + ex.getMessage());
            }
        });

        HBox btnRow = new HBox(12, btnSpacer, btnHuy, btnTao);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(8, 0, 0, 0));

        card.getChildren().addAll(titleRow, sep, form, infoNote, errLbl, btnRow);
        VBox wrapper = new VBox(card);
        wrapper.setPadding(new Insets(10, 0, 0, 0));
        return wrapper;
    }

    // ═══════════════════════════════════════════ TAB 3: ĐỔI MẬT KHẨU
    private Node buildChangePassTab() {
        VBox card = new VBox(18);
        card.setPadding(new Insets(28));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),8,0,0,2);");

        // ── Card title with icon ──
        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label iconTitle = new Label("🔑");
        iconTitle.setStyle("-fx-font-size: 20px;");
        Label cardTitle = new Label("Đổi Mật Khẩu");
        cardTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        titleRow.getChildren().addAll(iconTitle, cardTitle);

        // Separator
        Region sep = new Region();
        sep.setMinHeight(1);
        sep.setMaxHeight(1);
        sep.setStyle("-fx-background-color: #F0F2F5;");

        // ── Chọn tài khoản ──
        java.util.LinkedHashMap<String, String> tkMap = new java.util.LinkedHashMap<>();
        String defaultDisplay = activeUsername + " (đang đăng nhập)";
        try {
            for (TaiKhoan tk : new TaiKhoanDAO().getAll()) {
                String display;
                if (tk.getTenDangNhap().equalsIgnoreCase(activeUsername)) {
                    display = tk.getTenDangNhap() + " — " + tk.getVaiTro() + "  ★ đang đăng nhập";
                    activeMaTK = tk.getMaTaiKhoan();
                    defaultDisplay = display;
                } else {
                    display = tk.getTenDangNhap() + " — " + tk.getVaiTro();
                }
                tkMap.put(display, tk.getMaTaiKhoan());
            }
        } catch (Exception ignored) {
        }

        ComboBox<String> cbTaiKhoan = new ComboBox<>();
        cbTaiKhoan.getItems().addAll(tkMap.keySet());
        cbTaiKhoan.setValue(defaultDisplay);
        cbTaiKhoan.setMaxWidth(Double.MAX_VALUE);
        cbTaiKhoan.setStyle(
                "-fx-background-color:#FFF;-fx-border-color:#D9D9D9;-fx-border-radius:8;-fx-background-radius:8;"
                        + "-fx-border-width:1;-fx-padding:4 6;-fx-font-size:13px;");

        final String[] selectedMaTK = { activeMaTK };
        cbTaiKhoan.setOnAction(e -> {
            String sel = cbTaiKhoan.getValue();
            if (sel != null && tkMap.containsKey(sel))
                selectedMaTK[0] = tkMap.get(sel);
        });

        // ── Form grid: 2 columns, labels ON TOP of fields ──
        GridPane form = new GridPane();
        form.setHgap(20);
        form.setVgap(16);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        form.getColumnConstraints().addAll(col1, col2);

        // --- Chọn tài khoản (full width row 0-1) ---
        Label lblChon = formLabel("Chọn tài khoản *");
        form.add(lblChon, 0, 0, 2, 1);
        form.add(cbTaiKhoan, 0, 1, 2, 1);

        // --- Mật khẩu hiện tại (full width row 2-3) ---
        PasswordField txtCu = new PasswordField();
        txtCu.setPromptText("🔒  Nhập mật khẩu hiện tại");
        txtCu.setMaxWidth(Double.MAX_VALUE);
        txtCu.setStyle(fieldStyle());
        TextField txtCuVisible = new TextField();
        txtCuVisible.setPromptText("🔒  Nhập mật khẩu hiện tại");
        txtCuVisible.setMaxWidth(Double.MAX_VALUE);
        txtCuVisible.setStyle(fieldStyle());
        txtCuVisible.setVisible(false);
        txtCuVisible.setManaged(false);
        txtCu.textProperty().bindBidirectional(txtCuVisible.textProperty());
        Button btnEyeCu = new Button("👁");
        btnEyeCu.setStyle(
                "-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 14px; -fx-padding: 4 8;");
        btnEyeCu.setOnAction(ev -> {
            boolean showing = txtCuVisible.isVisible();
            txtCu.setVisible(showing);
            txtCu.setManaged(showing);
            txtCuVisible.setVisible(!showing);
            txtCuVisible.setManaged(!showing);
            btnEyeCu.setText(showing ? "👁" : "🙈");
        });
        StackPane passCuStack = new StackPane(txtCu, txtCuVisible);
        HBox passCuBox = new HBox(0, passCuStack, btnEyeCu);
        passCuBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(passCuStack, Priority.ALWAYS);

        Label lblCu = formLabel("Mật khẩu hiện tại *");
        form.add(lblCu, 0, 2, 2, 1);
        form.add(passCuBox, 0, 3, 2, 1);

        // --- Mật khẩu mới + Xác nhận (2 columns, row 4-5) ---
        PasswordField txtMoi = new PasswordField();
        txtMoi.setPromptText("🔒  Nhập mật khẩu mới");
        txtMoi.setMaxWidth(Double.MAX_VALUE);
        txtMoi.setStyle(fieldStyle());
        TextField txtMoiVisible = new TextField();
        txtMoiVisible.setPromptText("🔒  Nhập mật khẩu mới");
        txtMoiVisible.setMaxWidth(Double.MAX_VALUE);
        txtMoiVisible.setStyle(fieldStyle());
        txtMoiVisible.setVisible(false);
        txtMoiVisible.setManaged(false);
        txtMoi.textProperty().bindBidirectional(txtMoiVisible.textProperty());
        Button btnEyeMoi = new Button("👁");
        btnEyeMoi.setStyle(
                "-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 14px; -fx-padding: 4 8;");
        btnEyeMoi.setOnAction(ev -> {
            boolean showing = txtMoiVisible.isVisible();
            txtMoi.setVisible(showing);
            txtMoi.setManaged(showing);
            txtMoiVisible.setVisible(!showing);
            txtMoiVisible.setManaged(!showing);
            btnEyeMoi.setText(showing ? "👁" : "🙈");
        });
        StackPane passMoiStack = new StackPane(txtMoi, txtMoiVisible);
        HBox passMoiBox = new HBox(0, passMoiStack, btnEyeMoi);
        passMoiBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(passMoiStack, Priority.ALWAYS);

        PasswordField txtXN = new PasswordField();
        txtXN.setPromptText("🔒  Xác nhận mật khẩu mới");
        txtXN.setMaxWidth(Double.MAX_VALUE);
        txtXN.setStyle(fieldStyle());
        TextField txtXNVisible = new TextField();
        txtXNVisible.setPromptText("🔒  Xác nhận mật khẩu mới");
        txtXNVisible.setMaxWidth(Double.MAX_VALUE);
        txtXNVisible.setStyle(fieldStyle());
        txtXNVisible.setVisible(false);
        txtXNVisible.setManaged(false);
        txtXN.textProperty().bindBidirectional(txtXNVisible.textProperty());
        Button btnEyeXN = new Button("👁");
        btnEyeXN.setStyle(
                "-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 14px; -fx-padding: 4 8;");
        btnEyeXN.setOnAction(ev -> {
            boolean showing = txtXNVisible.isVisible();
            txtXN.setVisible(showing);
            txtXN.setManaged(showing);
            txtXNVisible.setVisible(!showing);
            txtXNVisible.setManaged(!showing);
            btnEyeXN.setText(showing ? "👁" : "🙈");
        });
        StackPane passXNStack = new StackPane(txtXN, txtXNVisible);
        HBox passXNBox = new HBox(0, passXNStack, btnEyeXN);
        passXNBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(passXNStack, Priority.ALWAYS);

        Label lblMoi = formLabel("Mật khẩu mới *");
        Label lblXN = formLabel("Xác nhận mật khẩu *");
        form.add(lblMoi, 0, 4);
        form.add(lblXN, 1, 4);
        form.add(passMoiBox, 0, 5);
        form.add(passXNBox, 1, 5);

        // ── Info note ──
        HBox infoNote = new HBox(8);
        infoNote.setAlignment(Pos.CENTER_LEFT);
        infoNote.setPadding(new Insets(10, 16, 10, 16));
        infoNote.setStyle(
                "-fx-background-color: #FFF7E6; -fx-background-radius: 8; -fx-border-color: #FFE58F; -fx-border-radius: 8; -fx-border-width: 1;");
        Label infoIcon = new Label("⚠");
        infoIcon.setStyle("-fx-text-fill: #FA8C16; -fx-font-size: 15px; -fx-font-weight: bold;");
        Label infoText = new Label("Lưu ý: Mật khẩu mới phải có ít nhất 6 ký tự.");
        infoText.setStyle("-fx-text-fill: #595959; -fx-font-size: 12.5px;");
        infoText.setWrapText(true);
        infoNote.getChildren().addAll(infoIcon, infoText);

        // ── Error label ──
        Label errLbl = errLabel();

        // ── Buttons row: Hủy + Đổi mật khẩu ──
        Region btnSpacer = new Region();
        HBox.setHgrow(btnSpacer, Priority.ALWAYS);

        Button btnHuy = new Button("✕  Hủy");
        btnHuy.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #595959;"
                + "-fx-background-radius: 8; -fx-border-color: #D9D9D9; -fx-border-width: 1;"
                + "-fx-border-radius: 8; -fx-padding: 10 28; -fx-cursor: hand; -fx-font-size: 13px;");
        btnHuy.setOnAction(e -> {
            txtCu.clear();
            txtCuVisible.clear();
            txtMoi.clear();
            txtMoiVisible.clear();
            txtXN.clear();
            txtXNVisible.clear();
            errLbl.setVisible(false);
            errLbl.setManaged(false);
        });

        Button btnDoi = new Button("🔑  Đổi mật khẩu");
        btnDoi.setStyle("-fx-background-color: #1890FF; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-padding: 10 28; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 13px;");
        btnDoi.setOnAction(e -> {
            String mkCu = txtCu.getText(), mkMoi = txtMoi.getText(), mkXN = txtXN.getText();
            if (mkCu.isEmpty()) {
                showErr(errLbl, "Vui lòng nhập mật khẩu hiện tại!");
                return;
            }
            if (mkMoi.isEmpty()) {
                showErr(errLbl, "Vui lòng nhập mật khẩu mới!");
                return;
            }
            if (mkMoi.length() < 6) {
                showErr(errLbl, "Mật khẩu mới phải có ít nhất 6 ký tự!");
                return;
            }
            if (!mkMoi.equals(mkXN)) {
                showErr(errLbl, "Mật khẩu xác nhận không khớp!");
                return;
            }
            if (selectedMaTK[0] == null || selectedMaTK[0].isEmpty()) {
                showErr(errLbl, "Vui lòng chọn tài khoản!");
                return;
            }
            try {
                if (new TaiKhoanDAO().doiMatKhau(selectedMaTK[0], mkCu, mkMoi)) {
                    errLbl.setVisible(false);
                    errLbl.setManaged(false);
                    alert(Alert.AlertType.INFORMATION, "Thành công", "Đổi mật khẩu thành công!");
                    txtCu.clear();
                    txtCuVisible.clear();
                    txtMoi.clear();
                    txtMoiVisible.clear();
                    txtXN.clear();
                    txtXNVisible.clear();
                } else {
                    showErr(errLbl, "Mật khẩu hiện tại không đúng hoặc lỗi hệ thống!");
                }
            } catch (Exception ex) {
                showErr(errLbl, "Lỗi DB: " + ex.getMessage());
            }
        });

        HBox btnRow = new HBox(12, btnSpacer, btnHuy, btnDoi);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(8, 0, 0, 0));

        card.getChildren().addAll(titleRow, sep, form, infoNote, errLbl, btnRow);
        VBox wrapper = new VBox(card);
        wrapper.setPadding(new Insets(10, 0, 0, 0));
        return wrapper;
    }

    // ═══════════════════════════════════════════ TABLE
    private TableView<Object[]> buildTable() {
        TableView<Object[]> tbl = new TableView<>();
        tbl.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tbl.setStyle("-fx-background-color: #F9F9F9; -fx-border-color: #E8E8E8;"
                + "-fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");
        String[] heads = { "Mã TK", "Mã NV", "Vai Trò", "Tên Đăng Nhập" };
        for (int i = 0; i < heads.length; i++) {
            final int idx = i;
            TableColumn<Object[], String> col = new TableColumn<>(heads[i]);
            col.setCellValueFactory(p -> {
                Object v = idx < p.getValue().length ? p.getValue()[idx] : "";
                return new SimpleStringProperty(v != null ? v.toString() : "");
            });
            if (i == 2) {
                col.setCellFactory(tc -> new TableCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setGraphic(null);
                            setText(null);
                            return;
                        }
                        Label badge = new Label(item);
                        boolean isQL = "QuanLy".equals(item);
                        badge.setStyle("-fx-background-color: " + (isQL ? "#E6F4FF" : "#F6FFED")
                                + "; -fx-text-fill: " + (isQL ? "#1890FF" : "#52C41A")
                                + "; -fx-padding: 2 8 2 8; -fx-background-radius: 10;"
                                + " -fx-font-size: 11px; -fx-font-weight: bold;");
                        setGraphic(badge);
                        setText(null);
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
        if (sel == null) {
            alert(Alert.AlertType.WARNING, "Chưa chọn", "Chọn tài khoản cần xóa!");
            return;
        }
        String maTK = sel[0].toString();
        if (maTK.equals(activeMaTK)) {
            alert(Alert.AlertType.WARNING, "Không thể xóa", "Không thể xóa tài khoản đang đăng nhập!");
            return;
        }
        new Alert(Alert.AlertType.CONFIRMATION, "Xóa tài khoản \"" + sel[3] + "\"?").showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    if (new TaiKhoanDAO().xoa(maTK))
                        refresh();
                    else
                        alert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa.");
                } catch (Exception ex) {
                    alert(Alert.AlertType.ERROR, "Lỗi DB", ex.getMessage());
                }
            }
        });
    }

    private void refresh() {
        items.setAll(loadData());
    }

    private List<Object[]> loadData() {
        List<Object[]> result = new ArrayList<>();
        try {
            for (TaiKhoan tk : new TaiKhoanDAO().getAll()) {
                if (tk.getTenDangNhap().equalsIgnoreCase(activeUsername))
                    activeMaTK = tk.getMaTaiKhoan();
                result.add(
                        new Object[] { tk.getMaTaiKhoan(), tk.getMaNhanVien(), tk.getVaiTro(), tk.getTenDangNhap() });
            }
            if (!result.isEmpty())
                return result;
        } catch (Exception ignored) {
        }
        result.add(new Object[] { "TK001", "NV001", "QuanLy", activeUsername });
        return result;
    }

    // ═══════════════════════════════════════════ HELPERS
    private TextField field(String val) {
        TextField tf = new TextField(val);
        tf.setMaxWidth(Double.MAX_VALUE);
        tf.setStyle(fieldStyle());
        return tf;
    }

    private String fieldStyle() {
        return "-fx-background-color:#FFF;-fx-border-color:#D9D9D9;-fx-border-radius:8;-fx-background-radius:8;-fx-border-width:1;-fx-padding:8 12;-fx-font-size:13px;";
    }

    private Label lbl(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #595959;");
        return l;
    }

    private Label formLabel(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2C2C2C;");
        return l;
    }

    private Label errLabel() {
        Label l = new Label();
        l.setStyle("-fx-text-fill: #FF4D4F; -fx-font-size: 12px;");
        l.setVisible(false);
        l.setManaged(false);
        l.setWrapText(true);
        return l;
    }

    private void showErr(Label lbl, String msg) {
        lbl.setText(msg);
        lbl.setVisible(true);
        lbl.setManaged(true);
    }

    private void alert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type, msg);
        a.setHeaderText(null);
        a.setTitle(title);
        a.showAndWait();
    }
}
