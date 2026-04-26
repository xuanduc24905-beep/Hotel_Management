package com.lotuslaverne.fx.views;

import com.lotuslaverne.dao.KhuyenMaiDAO;
import com.lotuslaverne.entity.KhuyenMai;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class KhuyenMaiView {

    private static final Object[][] DEMO_KM = {
        {"KM001", "Hè Vui",        "15", "01/04/2026", "30/06/2026", "Đặt trực tiếp"},
        {"KM002", "Cuối tuần",     "10", "01/01/2026", "31/12/2026", "Phòng Deluxe trở lên"},
        {"KM003", "Sinh nhật",     "20", "01/01/2026", "31/12/2026", "Khách lưu trú trên 3 ngày"},
        {"KM004", "Flash Sale",     "30", "01/05/2026", "31/05/2026", ""},
    };

    private ObservableList<Object[]> items;
    private TableView<Object[]> table;

    public Node build() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #F0F2F5;");

        VBox content = new VBox(20);
        content.setPadding(new Insets(28));
        content.setStyle("-fx-background-color: #F0F2F5;");
        VBox.setVgrow(content, Priority.ALWAYS);

        // Header
        VBox header = new VBox(4);
        Label title = new Label("Khuyến Mãi");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Label sub = new Label("Quản lý chương trình khuyến mãi và mã giảm giá");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #8C8C8C;");
        header.getChildren().addAll(title, sub);

        // Toolbar
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(12, 16, 12, 16));
        toolbar.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        TextField search = new TextField();
        search.setPromptText("🔍  Tìm mã KM, tên chương trình...");
        search.setPrefWidth(280);
        search.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #E8E8E8;"
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 1;"
                + "-fx-padding: 8 12; -fx-font-size: 13px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label hint = new Label("💡 Double-click để sửa");
        hint.setStyle("-fx-font-size: 11px; -fx-text-fill: #BFBFBF; -fx-font-style: italic;");

        Button btnThem    = actionBtn("＋ Thêm Mới", "#52C41A");
        Button btnXoa     = actionBtn("🗑 Xóa",       "#FF4D4F");
        Button btnRefresh = actionBtn("↻ Làm Mới",    "#8C8C8C");

        toolbar.getChildren().addAll(search, hint, spacer, btnRefresh, btnThem, btnXoa);

        // Table
        items = FXCollections.observableArrayList(loadData());
        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        search.textProperty().addListener((obs, o, n) -> filterTable(n));
        table.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
                openDialog(table.getSelectionModel().getSelectedItem(), false);
            }
        });
        btnThem.setOnAction(e -> openDialog(null, true));
        btnXoa.setOnAction(e -> handleXoa());
        btnRefresh.setOnAction(e -> {
            refresh();
            com.lotuslaverne.fx.UiUtils.flashButton(btnRefresh, "✓ Đã làm mới");
        });

        content.getChildren().addAll(header, toolbar, table);
        root.getChildren().add(content);
        VBox.setVgrow(root, Priority.ALWAYS);
        return root;
    }

    private TableView<Object[]> buildTable() {
        TableView<Object[]> tbl = new TableView<>();
        tbl.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tbl.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-border-radius: 10; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        String[] heads = {"Mã KM", "Tên Chương Trình", "% Giảm", "Ngày Áp Dụng", "Ngày Kết Thúc", "Điều Kiện"};
        for (int i = 0; i < heads.length; i++) {
            final int idx = i;
            TableColumn<Object[], String> col = new TableColumn<>(heads[i]);
            col.setCellValueFactory(p -> {
                Object v = idx < p.getValue().length ? p.getValue()[idx] : "";
                return new SimpleStringProperty(v != null ? v.toString() : "");
            });
            if (i == 2) {
                col.setPrefWidth(80);
                col.setCellFactory(tc -> new TableCell<>() {
                    @Override protected void updateItem(String s, boolean empty) {
                        super.updateItem(s, empty);
                        if (empty || s == null) { setGraphic(null); setText(null); return; }
                        Label badge = new Label(s + "%");
                        badge.setStyle("-fx-background-color: #FFF1F0; -fx-text-fill: #FF4D4F;"
                                + "-fx-padding: 2 8 2 8; -fx-background-radius: 10;"
                                + "-fx-font-size: 12px; -fx-font-weight: bold;");
                        setGraphic(badge); setText(null);
                    }
                });
            }
            tbl.getColumns().add(col);
        }
        tbl.setItems(items);
        tbl.setPlaceholder(new Label("Không có chương trình khuyến mãi."));
        return tbl;
    }

    private void openDialog(Object[] row, boolean isNew) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(isNew ? "Thêm Khuyến Mãi" : "Sửa Khuyến Mãi");
        dialog.setResizable(false);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(20));
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPrefWidth(150);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPrefWidth(210);
        form.getColumnConstraints().addAll(c1, c2);

        TextField txtMa        = field(row != null ? row[0].toString() : "");
        txtMa.setEditable(isNew);
        TextField txtTen       = field(row != null ? row[1].toString() : "");
        TextField txtPhanTram  = field(row != null ? row[2].toString() : "");
        TextField txtDieuKien  = field(row != null && row[5] != null ? row[5].toString() : "");
        DatePicker dpAD = new DatePicker(LocalDate.now());
        DatePicker dpKT = new DatePicker(LocalDate.of(LocalDate.now().getYear(), 12, 31));
        dpAD.setMaxWidth(Double.MAX_VALUE);
        dpKT.setMaxWidth(Double.MAX_VALUE);

        form.add(lbl("Mã khuyến mãi *:"),    0, 0); form.add(txtMa,       1, 0);
        form.add(lbl("Tên chương trình *:"),  0, 1); form.add(txtTen,      1, 1);
        form.add(lbl("% Giảm (0-100) *:"),    0, 2); form.add(txtPhanTram, 1, 2);
        form.add(lbl("Ngày áp dụng *:"),      0, 3); form.add(dpAD,        1, 3);
        form.add(lbl("Ngày kết thúc *:"),     0, 4); form.add(dpKT,        1, 4);
        form.add(lbl("Điều kiện:"),           0, 5); form.add(txtDieuKien, 1, 5);

        Button btnSave   = new Button(isNew ? "Thêm" : "Lưu");
        btnSave.setStyle("-fx-background-color: " + (isNew ? "#52C41A" : "#1890FF")
                + "; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 8 20; -fx-cursor: hand;");
        Button btnCancel = new Button("Hủy");
        btnCancel.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #595959;"
                + "-fx-background-radius: 6; -fx-padding: 8 20; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> dialog.close());

        btnSave.setOnAction(e -> {
            if (txtMa.getText().trim().isEmpty() || txtTen.getText().trim().isEmpty()
                    || txtPhanTram.getText().trim().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Vui lòng điền đầy đủ các trường bắt buộc (*).").showAndWait(); return;
            }
            double pct;
            try {
                pct = Double.parseDouble(txtPhanTram.getText().trim());
                if (pct <= 0 || pct > 100) throw new Exception();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "% Giảm phải là số từ 0 đến 100!").showAndWait(); return;
            }
            if (dpKT.getValue() == null || dpAD.getValue() == null || !dpKT.getValue().isAfter(dpAD.getValue())) {
                new Alert(Alert.AlertType.WARNING, "Ngày kết thúc phải sau ngày áp dụng!").showAndWait(); return;
            }
            try {
                KhuyenMai km = new KhuyenMai(
                    txtMa.getText().trim(), txtTen.getText().trim(),
                    Timestamp.valueOf(dpAD.getValue().atStartOfDay()),
                    Timestamp.valueOf(dpKT.getValue().atStartOfDay()),
                    pct, txtDieuKien.getText().trim()
                );
                boolean ok = isNew ? new KhuyenMaiDAO().them(km) : new KhuyenMaiDAO().sua(km);
                if (ok) { dialog.close(); refresh(); }
                else new Alert(Alert.AlertType.ERROR, "Lỗi! Kiểm tra lại dữ liệu.").showAndWait();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Lỗi DB: " + ex.getMessage()).showAndWait();
            }
        });

        HBox btnRow = new HBox(10, btnCancel, btnSave);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(0, 20, 16, 20));

        VBox root = new VBox(form, btnRow);
        root.setStyle("-fx-background-color: #FFFFFF;");
        dialog.setScene(new Scene(root, 410, 380));
        dialog.showAndWait();
    }

    private void handleXoa() {
        Object[] sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { new Alert(Alert.AlertType.WARNING, "Chọn khuyến mãi cần xóa!").showAndWait(); return; }
        String ma = sel[0].toString();
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Xóa khuyến mãi \"" + ma + "\"?");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    if (new KhuyenMaiDAO().xoa(ma)) refresh();
                    else new Alert(Alert.AlertType.ERROR, "Không thể xóa (đang được tham chiếu).").showAndWait();
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR, "Lỗi DB.").showAndWait();
                }
            }
        });
    }

    private void filterTable(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) { table.setItems(items); return; }
        String kw = keyword.toLowerCase();
        ObservableList<Object[]> filtered = FXCollections.observableArrayList();
        for (Object[] r : items) {
            for (Object cell : r) {
                if (cell != null && cell.toString().toLowerCase().contains(kw)) { filtered.add(r); break; }
            }
        }
        table.setItems(filtered);
    }

    private void refresh() {
        items.setAll(loadData());
        table.setItems(items);
    }

    private List<Object[]> loadData() {
        List<Object[]> result = new ArrayList<>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            List<KhuyenMai> list = new KhuyenMaiDAO().getAll();
            if (!list.isEmpty()) {
                for (KhuyenMai km : list) {
                    result.add(new Object[]{
                        km.getMaKhuyenMai(), km.getTenKhuyenMai(),
                        String.valueOf((int) km.getPhanTramGiam()),
                        km.getNgayApDung()  != null ? sdf.format(km.getNgayApDung())  : "",
                        km.getNgayKetThuc() != null ? sdf.format(km.getNgayKetThuc()) : "",
                        km.getDieuKienApDung() != null ? km.getDieuKienApDung() : ""
                    });
                }
                return result;
            }
        } catch (Exception ignored) {}
        for (Object[] r : DEMO_KM) result.add(r);
        return result;
    }

    private TextField field(String val) {
        TextField tf = new TextField(val);
        tf.setMaxWidth(Double.MAX_VALUE);
        tf.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D9D9D9;"
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1; -fx-padding: 7 10;");
        return tf;
    }

    private Label lbl(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #595959;");
        return l;
    }

    private Button actionBtn(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-padding: 8 14; -fx-cursor: hand; -fx-font-size: 12px;");
        return btn;
    }
}
