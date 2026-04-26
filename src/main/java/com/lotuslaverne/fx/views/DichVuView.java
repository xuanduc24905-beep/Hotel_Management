package com.lotuslaverne.fx.views;

import com.lotuslaverne.dao.DichVuDAO;
import com.lotuslaverne.entity.DichVu;
import com.lotuslaverne.util.ConnectDB;
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DichVuView {

    private static final Object[][] DEMO_DV = {
        {"NU001", "Nước suối",       "LDV02",  "15,000",   "DangKinhDoanh"},
        {"NU002", "Cà phê",          "LDV02",  "30,000",   "DangKinhDoanh"},
        {"AU001", "Phở bò",          "LDV01",  "60,000",   "DangKinhDoanh"},
        {"GU001", "Giặt ủi (1 kg)",  "LDV03",  "25,000",   "DangKinhDoanh"},
        {"TI001", "Massage 60 phút", "LDV03",  "250,000",  "DangKinhDoanh"},
        {"TI002", "Thuê xe đạp",     "LDV03",  "50,000",   "NgungKinhDoanh"},
    };

    /** Parse "Đồ Uống (LDV02) → NU" → "LDV02" */
    private static String parseMaLoai(String comboValue) {
        if (comboValue == null) return null;
        int open  = comboValue.indexOf('(');
        int close = comboValue.indexOf(')');
        if (open < 0 || close < 0 || close <= open) return null;
        return comboValue.substring(open + 1, close).trim();
    }

    /** Map mã loại DV → prefix mã dịch vụ tương ứng */
    private static String prefixForLoai(String maLoai) {
        if (maLoai == null) return "DV";
        return switch (maLoai.toUpperCase()) {
            case "LDV01" -> "AU";  // Đồ Ăn
            case "LDV02" -> "NU";  // Đồ Uống / Nước Uống
            case "LDV03" -> "TI";  // Tiện Ích
            default       -> "DV";
        };
    }

    /** Sinh mã DV tiếp theo theo prefix: query MAX(mã) trong DB rồi +1, padded 3 chữ số */
    private String generateNextMaDV(String maLoai) {
        String prefix = prefixForLoai(maLoai);
        Connection con = ConnectDB.getInstance().getConnection();
        int next = 1;
        if (con != null) {
            try (PreparedStatement pst = con.prepareStatement(
                    "SELECT MAX(maDichVu) FROM DichVu WHERE maDichVu LIKE ?")) {
                pst.setString(1, prefix + "%");
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        String maxMa = rs.getString(1);
                        if (maxMa != null && maxMa.length() > prefix.length()) {
                            try {
                                next = Integer.parseInt(maxMa.substring(prefix.length())) + 1;
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }
            } catch (Exception ignored) {}
        } else {
            // Offline: đếm trong DEMO_DV
            for (Object[] r : DEMO_DV) {
                String ma = r[0].toString();
                if (ma.startsWith(prefix) && ma.length() > prefix.length()) {
                    try {
                        int n = Integer.parseInt(ma.substring(prefix.length()));
                        if (n >= next) next = n + 1;
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return String.format("%s%03d", prefix, next);
    }

    /** Load các loại DV từ DB → Map<maLoai, tenLoai> */
    private Map<String, String> loadLoaiDichVu() {
        Map<String, String> map = new LinkedHashMap<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) {
            // Fallback hardcode khớp seed SQL
            map.put("LDV01", "Đồ Ăn");
            map.put("LDV02", "Đồ Uống");
            map.put("LDV03", "Tiện Ích");
            return map;
        }
        try (PreparedStatement pst = con.prepareStatement(
                "SELECT maLoaiDichVu, tenLoaiDichVu FROM LoaiDichVu ORDER BY maLoaiDichVu");
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString(1), rs.getString(2));
            }
        } catch (Exception ignored) {
            map.put("LDV01", "Đồ Ăn");
            map.put("LDV02", "Đồ Uống");
            map.put("LDV03", "Tiện Ích");
        }
        return map;
    }

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
        Label title = new Label("Quản Lý Dịch Vụ");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Label sub = new Label("Danh mục dịch vụ và món ăn cung cấp cho khách");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #8C8C8C;");
        header.getChildren().addAll(title, sub);

        // Add form card
        VBox formCard = buildAddCard();

        // Toolbar
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(12, 16, 12, 16));
        toolbar.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        TextField search = new TextField();
        search.setPromptText("🔍  Tìm theo tên, mã dịch vụ...");
        search.setPrefWidth(260);
        search.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #E8E8E8;"
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 1;"
                + "-fx-padding: 8 12; -fx-font-size: 13px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnEdit    = actionBtn("✏ Sửa",            "#1890FF");
        Button btnStop    = actionBtn("⛔ Ngừng KD",       "#FAAD14");
        Button btnRefresh = actionBtn("↻ Làm Mới",         "#8C8C8C");
        toolbar.getChildren().addAll(search, spacer, btnRefresh, btnEdit, btnStop);

        // Table
        items = FXCollections.observableArrayList(loadData());
        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        search.textProperty().addListener((obs, o, n) -> filterTable(n));
        table.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
                openEditDialog(table.getSelectionModel().getSelectedItem());
            }
        });
        btnEdit.setOnAction(e -> {
            Object[] sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) openEditDialog(sel);
            else new Alert(Alert.AlertType.WARNING, "Vui lòng chọn dịch vụ cần sửa!").showAndWait();
        });
        btnStop.setOnAction(e -> handleNgungKD());
        btnRefresh.setOnAction(e -> {
            refresh();
            com.lotuslaverne.fx.UiUtils.flashButton(btnRefresh, "✓ Đã làm mới");
        });

        content.getChildren().addAll(header, formCard, toolbar, table);
        root.getChildren().add(content);
        VBox.setVgrow(root, Priority.ALWAYS);
        return root;
    }

    private VBox buildAddCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        Label cardTitle = new Label("+ Thêm Dịch Vụ / Món Mới");
        cardTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;"
                + "-fx-border-color: transparent transparent #F0F2F5 transparent;"
                + "-fx-border-width: 0 0 1 0; -fx-padding: 0 0 10 0;");

        GridPane form = new GridPane();
        form.setHgap(16);
        form.setVgap(10);
        for (int i = 0; i < 5; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(20);
            form.getColumnConstraints().add(cc);
        }

        // Mã DV: read-only, auto-sinh theo loại
        Label lblMaDV = new Label("(chọn loại trước)");
        lblMaDV.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1890FF;"
                + "-fx-background-color: #F0F5FF; -fx-padding: 8 12; -fx-background-radius: 6;"
                + "-fx-border-color: #ADC6FF; -fx-border-width: 1; -fx-border-radius: 6;");
        lblMaDV.setMaxWidth(Double.MAX_VALUE);

        TextField txtTen   = field(""); txtTen.setPromptText("Tên dịch vụ");
        TextField txtGia   = field("0"); txtGia.setPromptText("Đơn giá");

        // Loại dịch vụ: ComboBox load từ DB
        Map<String, String> loaiMap = loadLoaiDichVu();
        ComboBox<String> cbLoai = new ComboBox<>();
        for (Map.Entry<String, String> e : loaiMap.entrySet()) {
            cbLoai.getItems().add(e.getValue() + " (" + e.getKey() + ") → " + prefixForLoai(e.getKey()));
        }
        cbLoai.setMaxWidth(Double.MAX_VALUE);
        cbLoai.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D9D9D9;"
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1;");
        cbLoai.setPromptText("Chọn loại...");

        // Khi chọn loại → cập nhật mã DV preview
        cbLoai.valueProperty().addListener((obs, o, n) -> {
            String maLoai = parseMaLoai(n);
            if (maLoai != null) lblMaDV.setText(generateNextMaDV(maLoai));
        });

        ComboBox<String> cbTT = new ComboBox<>();
        cbTT.getItems().addAll("Đang Kinh Doanh", "Ngưng Kinh Doanh");
        cbTT.setValue("Đang Kinh Doanh");
        cbTT.setMaxWidth(Double.MAX_VALUE);
        cbTT.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D9D9D9;"
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1;");

        form.add(lbl("Mã DV (auto)"),    0, 0); form.add(lblMaDV, 0, 1);
        form.add(lbl("Tên Dịch Vụ *"),   1, 0); form.add(txtTen,  1, 1);
        form.add(lbl("Loại Dịch Vụ *"), 2, 0); form.add(cbLoai,  2, 1);
        form.add(lbl("Đơn Giá (VNĐ)"),  3, 0); form.add(txtGia,  3, 1);
        form.add(lbl("Trạng Thái"),      4, 0); form.add(cbTT,    4, 1);

        Button btnAdd = new Button("+ Thêm Mới");
        btnAdd.setStyle("-fx-background-color: #1890FF; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-padding: 8 20; -fx-font-weight: bold; -fx-cursor: hand;");
        btnAdd.setOnAction(e -> {
            String maLoai = parseMaLoai(cbLoai.getValue());
            if (maLoai == null) {
                new Alert(Alert.AlertType.WARNING, "Vui lòng chọn loại dịch vụ!").showAndWait(); return;
            }
            if (txtTen.getText().trim().isEmpty() || txtGia.getText().trim().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Vui lòng nhập tên và đơn giá!").showAndWait(); return;
            }
            String maDV = generateNextMaDV(maLoai);
            try {
                DichVu dv = new DichVu(maDV, txtTen.getText().trim(),
                        maLoai,
                        Double.parseDouble(txtGia.getText().trim()),
                        // Map hiển thị → DB
                        "Đang Kinh Doanh".equals(cbTT.getValue()) ? "DangKinhDoanh" : "NgungKinhDoanh");
                if (new DichVuDAO().themDichVu(dv)) {
                    refresh();
                    txtTen.clear(); txtGia.setText("0");
                    lblMaDV.setText(generateNextMaDV(maLoai));  // preview kế tiếp
                } else {
                    new Alert(Alert.AlertType.ERROR, "Trùng mã DV hoặc sai mã loại!").showAndWait();
                }
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Đơn giá phải là số hợp lệ!").showAndWait();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Lỗi DB: " + ex.getMessage()).showAndWait();
            }
        });

        HBox btnRow = new HBox(btnAdd);
        btnRow.setPadding(new Insets(4, 0, 0, 0));
        card.getChildren().addAll(cardTitle, form, btnRow);
        return card;
    }

    private void openEditDialog(Object[] row) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Sửa Dịch Vụ");
        dialog.setResizable(false);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(20));
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPrefWidth(140);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPrefWidth(200);
        form.getColumnConstraints().addAll(c1, c2);

        Label lblMa  = new Label(row[0].toString());
        lblMa.setStyle("-fx-font-weight: bold; -fx-text-fill: #1890FF;");
        TextField txtTen  = field(row[1].toString());
        TextField txtLoai = field(row[2].toString());
        TextField txtGia  = field(row[3].toString().replaceAll("[^0-9.]", ""));
        ComboBox<String> cbTT = new ComboBox<>();
        cbTT.getItems().addAll("Đang Kinh Doanh", "Ngưng Kinh Doanh");
        // Map DB enum → hiển thị
        String displayTT = "DangKinhDoanh".equals(row[4].toString()) ? "Đang Kinh Doanh" : "Ngưng Kinh Doanh";
        cbTT.setValue(displayTT);
        cbTT.setMaxWidth(Double.MAX_VALUE);

        form.add(lbl("Mã Dịch Vụ:"),     0, 0); form.add(lblMa,  1, 0);
        form.add(lbl("Tên Dịch Vụ *:"),  0, 1); form.add(txtTen, 1, 1);
        form.add(lbl("Mã Loại:"),         0, 2); form.add(txtLoai,1, 2);
        form.add(lbl("Đơn Giá (VNĐ) *:"),0, 3); form.add(txtGia, 1, 3);
        form.add(lbl("Trạng Thái:"),      0, 4); form.add(cbTT,   1, 4);

        Button btnSave   = new Button("Lưu");
        btnSave.setStyle("-fx-background-color: #1890FF; -fx-text-fill: white;"
                + "-fx-background-radius: 6; -fx-padding: 8 20; -fx-cursor: hand;");
        Button btnCancel = new Button("Hủy");
        btnCancel.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #595959;"
                + "-fx-background-radius: 6; -fx-padding: 8 20; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> dialog.close());

        btnSave.setOnAction(e -> {
            try {
                // Map hiển thị → DB
                String trangThaiDB = "Đang Kinh Doanh".equals(cbTT.getValue())
                        ? "DangKinhDoanh" : "NgungKinhDoanh";
                DichVu dv = new DichVu(row[0].toString(), txtTen.getText().trim(),
                        txtLoai.getText().trim(), Double.parseDouble(txtGia.getText().trim()),
                        trangThaiDB);
                if (new DichVuDAO().capNhatDichVu(dv)) { dialog.close(); refresh(); }
                else new Alert(Alert.AlertType.ERROR, "Lỗi cập nhật dịch vụ!").showAndWait();
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Đơn giá phải là số hợp lệ!").showAndWait();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Lỗi DB: " + ex.getMessage()).showAndWait();
            }
        });

        HBox btnRow = new HBox(10, btnCancel, btnSave);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(0, 20, 16, 20));

        VBox root = new VBox(form, btnRow);
        root.setStyle("-fx-background-color: #FFFFFF;");
        dialog.setScene(new Scene(root, 380, 300));
        dialog.showAndWait();
    }

    private void handleNgungKD() {
        Object[] sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { new Alert(Alert.AlertType.WARNING, "Chọn dịch vụ cần ngừng!").showAndWait(); return; }
        try {
            if (new DichVuDAO().dungBanDichVu(sel[0].toString())) refresh();
            else new Alert(Alert.AlertType.ERROR, "Không tìm thấy dịch vụ!").showAndWait();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Lỗi DB.").showAndWait();
        }
    }

    private void filterTable(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            table.setItems(items);
            return;
        }
        String kw = keyword.toLowerCase();
        ObservableList<Object[]> filtered = FXCollections.observableArrayList();
        for (Object[] r : items) {
            for (Object cell : r) {
                if (cell != null && cell.toString().toLowerCase().contains(kw)) {
                    filtered.add(r);
                    break;
                }
            }
        }
        table.setItems(filtered);
    }

    private void refresh() {
        items.setAll(loadData());
        table.setItems(items);
    }

    private TableView<Object[]> buildTable() {
        TableView<Object[]> tbl = new TableView<>();
        tbl.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tbl.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-border-radius: 10; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        String[] heads = {"Mã DV", "Tên Dịch Vụ", "Loại", "Đơn Giá (VNĐ)", "Trạng Thái"};
        for (int i = 0; i < heads.length; i++) {
            final int idx = i;
            TableColumn<Object[], String> col = new TableColumn<>(heads[i]);
            col.setCellValueFactory(p -> {
                Object v = idx < p.getValue().length ? p.getValue()[idx] : "";
                return new SimpleStringProperty(v != null ? v.toString() : "");
            });
            if (i == 4) {
                col.setCellFactory(tc -> new TableCell<>() {
                    @Override protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) { setGraphic(null); setText(null); return; }
                        boolean active = "DangKinhDoanh".equals(item);
                        String display = active ? "Đang Kinh Doanh" : "Ngưng Kinh Doanh";
                        Label badge = new Label(display);
                        badge.setStyle("-fx-background-color: " + (active ? "#F6FFED" : "#FFF1F0")
                                + "; -fx-text-fill: " + (active ? "#52C41A" : "#FF4D4F")
                                + "; -fx-padding: 2 8 2 8; -fx-background-radius: 10;"
                                + " -fx-font-size: 11px; -fx-font-weight: bold;");
                        setGraphic(badge); setText(null);
                    }
                });
            }
            tbl.getColumns().add(col);
        }
        tbl.setItems(items);
        tbl.setPlaceholder(new Label("Không có dịch vụ nào."));
        return tbl;
    }

    private List<Object[]> loadData() {
        List<Object[]> result = new ArrayList<>();
        try {
            DecimalFormat df = new DecimalFormat("#,###");
            List<DichVu> list = new DichVuDAO().getAll();
            if (!list.isEmpty()) {
                for (DichVu dv : list) {
                    result.add(new Object[]{
                        dv.getMaDichVu(), dv.getTenDichVu(), dv.getMaLoaiDichVu(),
                        df.format(dv.getDonGia()), dv.getTrangThai()
                    });
                }
                return result;
            }
        } catch (Exception ignored) {}
        for (Object[] r : DEMO_DV) result.add(r);
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
