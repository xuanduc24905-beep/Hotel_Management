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
    private ObservableList<Object[]> items;
    private TableView<Object[]> table;
    private String activeMaTK = "";

    public TaiKhoanView(String username) {
        this.activeUsername = username;
    }

    public Node build() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #F0F2F5;");

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #F0F2F5; -fx-border-color: transparent;");

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

        // Top row: create + change password
        HBox topRow = new HBox(20);
        topRow.setAlignment(Pos.TOP_LEFT);
        VBox createCard = buildCreateCard();
        VBox changePassCard = buildChangePassCard();
        HBox.setHgrow(createCard,     Priority.ALWAYS);
        HBox.setHgrow(changePassCard, Priority.ALWAYS);
        topRow.getChildren().addAll(createCard, changePassCard);

        // Account list table
        VBox tableCard = buildTableCard();

        content.getChildren().addAll(header, topRow, tableCard);
        scroll.setContent(content);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        root.getChildren().add(scroll);
        return root;
    }

    private VBox buildCreateCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        Label cardTitle = new Label("Tạo Tài Khoản Mới");
        cardTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;"
                + "-fx-border-color: transparent transparent #F0F2F5 transparent;"
                + "-fx-border-width: 0 0 1 0; -fx-padding: 0 0 10 0;");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(40);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(60);
        form.getColumnConstraints().addAll(c1, c2);

        TextField txtMaTK  = field("");  txtMaTK.setPromptText("Tự sinh nếu để trống");
        TextField txtMaNV  = field("");
        TextField txtTenDN = field("");
        PasswordField txtMK = new PasswordField();
        txtMK.setMaxWidth(Double.MAX_VALUE);
        txtMK.setStyle(fieldStyle());
        ComboBox<String> cbVT = new ComboBox<>();
        cbVT.getItems().addAll("LeTan", "QuanLy");
        cbVT.setValue("LeTan");
        cbVT.setMaxWidth(Double.MAX_VALUE);
        cbVT.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D9D9D9;"
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1;");

        form.add(lbl("Mã tài khoản:"),  0, 0); form.add(txtMaTK,  1, 0);
        form.add(lbl("Mã nhân viên:"),  0, 1); form.add(txtMaNV,  1, 1);
        form.add(lbl("Tên đăng nhập *:"),0,2); form.add(txtTenDN,1, 2);
        form.add(lbl("Mật khẩu *:"),    0, 3); form.add(txtMK,    1, 3);
        form.add(lbl("Vai trò:"),        0, 4); form.add(cbVT,     1, 4);

        Label errLbl = errLabel();

        Button btnTao = new Button("Tạo Tài Khoản");
        btnTao.setStyle("-fx-background-color: #52C41A; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-padding: 9 20; -fx-font-weight: bold; -fx-cursor: hand;");
        btnTao.setOnAction(e -> {
            String tenDN = txtTenDN.getText().trim();
            String mk    = txtMK.getText();
            if (tenDN.isEmpty() || mk.isEmpty()) {
                showErr(errLbl, "Tên đăng nhập và mật khẩu không được trống!"); return;
            }
            String maTK = txtMaTK.getText().trim();
            if (maTK.isEmpty()) maTK = "TK" + (System.currentTimeMillis() % 100000);
            TaiKhoan tk = new TaiKhoan(maTK, txtMaNV.getText().trim(),
                    cbVT.getValue(), tenDN, mk);
            try {
                if (new TaiKhoanDAO().taoTaiKhoan(tk)) {
                    errLbl.setVisible(false); errLbl.setManaged(false);
                    alert(Alert.AlertType.INFORMATION, "Thành công", "Tạo tài khoản thành công!");
                    txtMaTK.clear(); txtMaNV.clear(); txtTenDN.clear(); txtMK.clear();
                    refresh();
                } else {
                    showErr(errLbl, "Mã hoặc tên đăng nhập đã tồn tại!");
                }
            } catch (Exception ex) {
                showErr(errLbl, "Lỗi DB: " + ex.getMessage());
            }
        });

        card.getChildren().addAll(cardTitle, form, errLbl, btnTao);
        return card;
    }

    private VBox buildChangePassCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        Label cardTitle = new Label("Đổi Mật Khẩu");
        cardTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;"
                + "-fx-border-color: transparent transparent #F0F2F5 transparent;"
                + "-fx-border-width: 0 0 1 0; -fx-padding: 0 0 10 0;");

        Label lblAccount = new Label("Tài khoản: " + activeUsername);
        lblAccount.setStyle("-fx-font-size: 12px; -fx-text-fill: #1890FF; -fx-font-weight: bold;");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(45);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(55);
        form.getColumnConstraints().addAll(c1, c2);

        PasswordField txtCu = new PasswordField();
        PasswordField txtMoi = new PasswordField();
        PasswordField txtXN = new PasswordField();
        for (PasswordField pf : new PasswordField[]{txtCu, txtMoi, txtXN}) {
            pf.setMaxWidth(Double.MAX_VALUE);
            pf.setStyle(fieldStyle());
        }

        form.add(lbl("Mật khẩu hiện tại *:"), 0, 0); form.add(txtCu,  1, 0);
        form.add(lbl("Mật khẩu mới *:"),       0, 1); form.add(txtMoi, 1, 1);
        form.add(lbl("Xác nhận mật khẩu *:"),  0, 2); form.add(txtXN,  1, 2);

        Label errLbl = errLabel();

        Button btnDoi = new Button("Đổi Mật Khẩu");
        btnDoi.setStyle("-fx-background-color: #1890FF; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-padding: 9 20; -fx-font-weight: bold; -fx-cursor: hand;");
        btnDoi.setOnAction(e -> {
            String mkCu  = txtCu.getText();
            String mkMoi = txtMoi.getText();
            String mkXN  = txtXN.getText();
            if (!mkMoi.equals(mkXN)) { showErr(errLbl, "Mật khẩu xác nhận không khớp!"); return; }
            if (mkMoi.length() < 4)  { showErr(errLbl, "Mật khẩu mới phải có ít nhất 4 ký tự!"); return; }
            if (activeMaTK.isEmpty()) { showErr(errLbl, "Không xác định được tài khoản đang đăng nhập!"); return; }
            try {
                if (new TaiKhoanDAO().doiMatKhau(activeMaTK, mkCu, mkMoi)) {
                    errLbl.setVisible(false); errLbl.setManaged(false);
                    alert(Alert.AlertType.INFORMATION, "Thành công", "Đổi mật khẩu thành công!");
                    txtCu.clear(); txtMoi.clear(); txtXN.clear();
                } else {
                    showErr(errLbl, "Mật khẩu hiện tại không đúng hoặc lỗi hệ thống!");
                }
            } catch (Exception ex) {
                showErr(errLbl, "Lỗi DB: " + ex.getMessage());
            }
        });

        card.getChildren().addAll(cardTitle, lblAccount, form, errLbl, btnDoi);
        return card;
    }

    private VBox buildTableCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        HBox cardHeader = new HBox(8);
        cardHeader.setAlignment(Pos.CENTER_LEFT);
        Label cardTitle = new Label("Danh Sách Tài Khoản");
        cardTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        Button btnXoa = new Button("Xóa Tài Khoản Đã Chọn");
        btnXoa.setStyle("-fx-background-color: #FF4D4F; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-padding: 7 16; -fx-cursor: hand; -fx-font-size: 12px;");
        btnXoa.setOnAction(e -> handleXoa());
        cardHeader.getChildren().addAll(cardTitle, sp, btnXoa);

        items = FXCollections.observableArrayList(loadData());
        table = buildTable();
        table.setPrefHeight(220);

        card.getChildren().addAll(cardHeader, table);
        return card;
    }

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

    private void handleXoa() {
        Object[] sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { alert(Alert.AlertType.WARNING, "Chưa chọn", "Chọn tài khoản cần xóa!"); return; }
        String maTK = sel[0].toString();
        if (maTK.equals(activeMaTK)) {
            alert(Alert.AlertType.WARNING, "Không thể xóa", "Không thể xóa tài khoản đang đăng nhập!"); return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Xóa tài khoản \"" + sel[3] + "\"?");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    if (new TaiKhoanDAO().xoa(maTK)) refresh();
                    else alert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa.");
                } catch (Exception ex) {
                    alert(Alert.AlertType.ERROR, "Lỗi DB", ex.getMessage());
                }
            }
        });
    }

    private void refresh() {
        items.setAll(loadData());
        table.setItems(items);
    }

    private List<Object[]> loadData() {
        List<Object[]> result = new ArrayList<>();
        try {
            TaiKhoanDAO dao = new TaiKhoanDAO();
            List<TaiKhoan> list = dao.getAll();
            for (TaiKhoan tk : list) {
                if (tk.getTenDangNhap().equalsIgnoreCase(activeUsername)) {
                    activeMaTK = tk.getMaTaiKhoan();
                }
                result.add(new Object[]{
                    tk.getMaTaiKhoan(), tk.getMaNhanVien(), tk.getVaiTro(), tk.getTenDangNhap()
                });
            }
            if (!result.isEmpty()) return result;
        } catch (Exception ignored) {}
        result.add(new Object[]{"TK001", "NV001", "QuanLy", activeUsername});
        return result;
    }

    private TextField field(String val) {
        TextField tf = new TextField(val);
        tf.setMaxWidth(Double.MAX_VALUE);
        tf.setStyle(fieldStyle());
        return tf;
    }

    private String fieldStyle() {
        return "-fx-background-color: #FFFFFF; -fx-border-color: #D9D9D9;"
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1; -fx-padding: 7 10;";
    }

    private Label lbl(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #595959;");
        return l;
    }

    private Label errLabel() {
        Label l = new Label();
        l.setStyle("-fx-text-fill: #FF4D4F; -fx-font-size: 12px;");
        l.setVisible(false);
        l.setManaged(false);
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
