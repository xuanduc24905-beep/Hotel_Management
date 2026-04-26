package com.lotuslaverne.fx.views;

import com.lotuslaverne.dao.ThietBiDAO;
import com.lotuslaverne.entity.ThietBi;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ThietBiView {

    private static final Object[][] DEMO_TB = {
        {"TB001", "Tivi Samsung 43 inch",   "Điện tử",  "20", "5,000,000", "Tot"},
        {"TB002", "Điều hòa Panasonic 1HP", "Điện lạnh", "25", "8,000,000", "Tot"},
        {"TB003", "Giường đôi cao cấp",     "Nội thất",  "15", "3,000,000", "Tot"},
        {"TB004", "Tủ lạnh Mini",           "Điện lạnh", "10", "2,500,000", "CanBaoTri"},
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
        Label title = new Label("Quản Lý Thiết Bị");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Label sub = new Label("Danh mục trang thiết bị, nội thất trong khách sạn");
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
        search.setPromptText("🔍  Tìm theo tên, mã TB...");
        search.setPrefWidth(260);
        search.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #E8E8E8;"
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 1;"
                + "-fx-padding: 8 12; -fx-font-size: 13px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnEdit    = actionBtn("✏ Sửa",            "#1890FF");
        Button btnDelete  = actionBtn("✖ Xóa",            "#FF4D4F");
        Button btnRefresh = actionBtn("↻ Làm Mới",         "#8C8C8C");
        toolbar.getChildren().addAll(search, spacer, btnRefresh, btnEdit, btnDelete);

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
            else new Alert(Alert.AlertType.WARNING, "Vui lòng chọn thiết bị cần sửa!").showAndWait();
        });
        btnDelete.setOnAction(e -> handleDelete());
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

        Label cardTitle = new Label("+ Thêm Thiết Bị Mới");
        cardTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;"
                + "-fx-border-color: transparent transparent #F0F2F5 transparent;"
                + "-fx-border-width: 0 0 1 0; -fx-padding: 0 0 10 0;");

        GridPane form = new GridPane();
        form.setHgap(16);
        form.setVgap(10);
        for (int i = 0; i < 6; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(16.66);
            form.getColumnConstraints().add(cc);
        }

        TextField txtMa    = field(""); txtMa.setPromptText("VD: TB001");
        TextField txtTen   = field(""); txtTen.setPromptText("Tên thiết bị");
        
        ComboBox<String> cbLoai = new ComboBox<>();
        cbLoai.getItems().addAll("Điện tử", "Điện lạnh", "Nội thất", "Khác");
        cbLoai.setValue("Điện tử");
        cbLoai.setMaxWidth(Double.MAX_VALUE);
        cbLoai.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D9D9D9;"
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1;");

        TextField txtSL    = field("1"); txtSL.setPromptText("Số lượng");
        TextField txtGia   = field("0"); txtGia.setPromptText("Đơn giá");

        ComboBox<String> cbTT = new ComboBox<>();
        cbTT.getItems().addAll("Tot", "CanBaoTri", "HuHong");
        cbTT.setValue("Tot");
        cbTT.setMaxWidth(Double.MAX_VALUE);
        cbTT.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D9D9D9;"
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1;");

        form.add(lbl("Mã TB *"),        0, 0); form.add(txtMa,   0, 1);
        form.add(lbl("Tên Thiết Bị *"), 1, 0); form.add(txtTen,  1, 1);
        form.add(lbl("Loại Thiết Bị"),  2, 0); form.add(cbLoai,  2, 1);
        form.add(lbl("Số Lượng"),       3, 0); form.add(txtSL,   3, 1);
        form.add(lbl("Đơn Giá (VNĐ)"),  4, 0); form.add(txtGia,  4, 1);
        form.add(lbl("Trạng Thái"),     5, 0); form.add(cbTT,    5, 1);

        Button btnAdd = new Button("+ Thêm Mới");
        btnAdd.setStyle("-fx-background-color: #1890FF; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-padding: 8 20; -fx-font-weight: bold; -fx-cursor: hand;");
        btnAdd.setOnAction(e -> {
            if (txtMa.getText().trim().isEmpty() || txtTen.getText().trim().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Vui lòng nhập mã và tên thiết bị!").showAndWait(); return;
            }
            try {
                ThietBi tb = new ThietBi(
                    txtMa.getText().trim(),
                    txtTen.getText().trim(),
                    cbLoai.getValue(),
                    Integer.parseInt(txtSL.getText().trim()),
                    Double.parseDouble(txtGia.getText().trim()),
                    cbTT.getValue()
                );
                if (new ThietBiDAO().themThietBi(tb)) {
                    refresh();
                    txtMa.clear(); txtTen.clear(); txtGia.setText("0"); txtSL.setText("1");
                } else {
                    new Alert(Alert.AlertType.ERROR, "Lỗi thêm thiết bị, có thể mã bị trùng!").showAndWait();
                }
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Số lượng và đơn giá phải là số hợp lệ!").showAndWait();
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
        dialog.setTitle("Sửa Thiết Bị");
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
        
        ComboBox<String> cbLoai = new ComboBox<>();
        cbLoai.getItems().addAll("Điện tử", "Điện lạnh", "Nội thất", "Khác");
        cbLoai.setValue(row[2].toString());
        cbLoai.setMaxWidth(Double.MAX_VALUE);
        
        TextField txtSL   = field(row[3].toString());
        TextField txtGia  = field(row[4].toString().replaceAll("[^0-9.]", ""));
        
        ComboBox<String> cbTT = new ComboBox<>();
        cbTT.getItems().addAll("Tot", "CanBaoTri", "HuHong");
        cbTT.setValue(row[5].toString());
        cbTT.setMaxWidth(Double.MAX_VALUE);

        form.add(lbl("Mã Thiết Bị:"),   0, 0); form.add(lblMa,  1, 0);
        form.add(lbl("Tên Thiết Bị *:"),0, 1); form.add(txtTen, 1, 1);
        form.add(lbl("Loại:"),          0, 2); form.add(cbLoai, 1, 2);
        form.add(lbl("Số Lượng:"),      0, 3); form.add(txtSL,  1, 3);
        form.add(lbl("Đơn Giá (VNĐ):"), 0, 4); form.add(txtGia, 1, 4);
        form.add(lbl("Trạng Thái:"),    0, 5); form.add(cbTT,   1, 5);

        Button btnSave   = new Button("Lưu");
        btnSave.setStyle("-fx-background-color: #1890FF; -fx-text-fill: white;"
                + "-fx-background-radius: 6; -fx-padding: 8 20; -fx-cursor: hand;");
        Button btnCancel = new Button("Hủy");
        btnCancel.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #595959;"
                + "-fx-background-radius: 6; -fx-padding: 8 20; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> dialog.close());

        btnSave.setOnAction(e -> {
            try {
                ThietBi tb = new ThietBi(
                    row[0].toString(),
                    txtTen.getText().trim(),
                    cbLoai.getValue(),
                    Integer.parseInt(txtSL.getText().trim()),
                    Double.parseDouble(txtGia.getText().trim()),
                    cbTT.getValue()
                );
                if (new ThietBiDAO().capNhatThietBi(tb)) { dialog.close(); refresh(); }
                else new Alert(Alert.AlertType.ERROR, "Lỗi cập nhật thiết bị!").showAndWait();
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Số lượng và đơn giá phải là số hợp lệ!").showAndWait();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Lỗi DB: " + ex.getMessage()).showAndWait();
            }
        });

        HBox btnRow = new HBox(10, btnCancel, btnSave);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(0, 20, 16, 20));

        VBox root = new VBox(form, btnRow);
        root.setStyle("-fx-background-color: #FFFFFF;");
        dialog.setScene(new Scene(root, 380, 350));
        dialog.showAndWait();
    }

    private void handleDelete() {
        Object[] sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { new Alert(Alert.AlertType.WARNING, "Chọn thiết bị cần xóa!").showAndWait(); return; }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắn chắn muốn xóa thiết bị " + sel[1] + "?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        
        if (confirm.getResult() == ButtonType.YES) {
            try {
                if (new ThietBiDAO().xoaThietBi(sel[0].toString())) refresh();
                else new Alert(Alert.AlertType.ERROR, "Không thể xóa! (Có thể do thiết bị đang được sử dụng ở đâu đó)").showAndWait();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Lỗi DB: " + ex.getMessage()).showAndWait();
            }
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

        String[] heads = {"Mã TB", "Tên Thiết Bị", "Loại", "Số Lượng", "Đơn Giá (VNĐ)", "Trạng Thái"};
        for (int i = 0; i < heads.length; i++) {
            final int idx = i;
            TableColumn<Object[], String> col = new TableColumn<>(heads[i]);
            col.setCellValueFactory(p -> {
                Object v = idx < p.getValue().length ? p.getValue()[idx] : "";
                return new SimpleStringProperty(v != null ? v.toString() : "");
            });
            if (i == 5) {
                col.setCellFactory(tc -> new TableCell<>() {
                    @Override protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) { setGraphic(null); setText(null); return; }
                        Label badge = new Label(item);
                        boolean ok = "Tot".equals(item);
                        boolean hu = "HuHong".equals(item);
                        badge.setStyle("-fx-background-color: " + (ok ? "#F6FFED" : (hu ? "#FFF1F0" : "#FFF7E6"))
                                + "; -fx-text-fill: " + (ok ? "#52C41A" : (hu ? "#FF4D4F" : "#FAAD14"))
                                + "; -fx-padding: 2 8 2 8; -fx-background-radius: 10;"
                                + " -fx-font-size: 11px; -fx-font-weight: bold;");
                        setGraphic(badge); setText(null);
                    }
                });
            }
            tbl.getColumns().add(col);
        }
        tbl.setItems(items);
        tbl.setPlaceholder(new Label("Không có thiết bị nào."));
        return tbl;
    }

    private List<Object[]> loadData() {
        List<Object[]> result = new ArrayList<>();
        try {
            DecimalFormat df = new DecimalFormat("#,###");
            List<ThietBi> list = new ThietBiDAO().getAll();
            if (list != null && !list.isEmpty()) {
                for (ThietBi tb : list) {
                    result.add(new Object[]{
                        tb.getMaThietBi(), tb.getTenThietBi(), tb.getLoaiThietBi(),
                        tb.getSoLuong(), df.format(tb.getDonGia()), tb.getTrangThai()
                    });
                }
                return result;
            }
        } catch (Exception ignored) {}
        for (Object[] r : DEMO_TB) result.add(r);
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
