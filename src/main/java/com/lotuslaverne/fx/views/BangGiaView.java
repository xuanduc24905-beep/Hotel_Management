package com.lotuslaverne.fx.views;

import com.lotuslaverne.dao.BangGiaDAO;
import com.lotuslaverne.dao.LoaiPhongDAO;
import com.lotuslaverne.entity.BangGia;
import com.lotuslaverne.entity.LoaiPhong;
import com.lotuslaverne.util.ConnectDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

import java.sql.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BangGiaView {

    private static final Object[][] DEMO_BG = {
        {"BG001", "Standard", "QuaDem",   "550,000 VNĐ", "01/01/2026", "31/12/2026"},
        {"BG002", "Deluxe",   "QuaDem",   "950,000 VNĐ", "01/01/2026", "31/12/2026"},
        {"BG003", "Suite",    "QuaDem", "1,500,000 VNĐ", "01/01/2026", "31/12/2026"},
        {"BG004", "Family",   "QuaDem", "1,200,000 VNĐ", "01/01/2026", "31/12/2026"},
        {"BG005", "Standard", "TheoGio",   "80,000 VNĐ", "01/01/2026", "31/12/2026"},
    };

    private ObservableList<Object[]> items;
    private TableView<Object[]> table;
    private String[] loaiPhongArr = {"Standard", "Deluxe", "Suite", "Family"};

    /** Cache KM đang active để tính giá sau giảm trên toàn table */
    private double activePhanTramGiam = 0;
    private String activeMaKM = null;
    private String activeTenKM = null;

    /** Query KM đang active hôm nay (% giảm cao nhất nếu có nhiều) */
    private void loadActiveKhuyenMai() {
        activePhanTramGiam = 0;
        activeMaKM = null;
        activeTenKM = null;
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return;
        String sql = "SELECT TOP 1 maKhuyenMai, tenKhuyenMai, phanTramGiam "
                + "FROM KhuyenMai "
                + "WHERE CAST(GETDATE() AS DATE) BETWEEN ngayApDung AND ngayKetThuc "
                + "ORDER BY phanTramGiam DESC";
        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                activeMaKM = rs.getString("maKhuyenMai");
                activeTenKM = rs.getString("tenKhuyenMai");
                activePhanTramGiam = rs.getDouble("phanTramGiam");
            }
        } catch (Exception ignored) {}
    }

    public Node build() {
        // Try to load loai phong from DB
        try {
            List<LoaiPhong> lps = new LoaiPhongDAO().getAll();
            if (!lps.isEmpty()) {
                loaiPhongArr = lps.stream().map(LoaiPhong::getMaLoaiPhong).toArray(String[]::new);
            }
        } catch (Exception ignored) {}

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #F0F2F5;");

        VBox content = new VBox(20);
        content.setPadding(new Insets(28));
        content.setStyle("-fx-background-color: #F0F2F5;");
        VBox.setVgrow(content, Priority.ALWAYS);

        // Tải KM active để tính giá sau giảm
        loadActiveKhuyenMai();

        // Header
        VBox header = new VBox(4);
        Label title = new Label("Bảng Giá Phòng");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Label sub = new Label("Quản lý đơn giá theo loại phòng và loại thuê");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #8C8C8C;");
        header.getChildren().addAll(title, sub);

        // Banner khuyến mãi nếu đang có KM active
        if (activePhanTramGiam > 0) {
            Label kmBanner = new Label(String.format(
                    "🎁  Khuyến mãi đang áp dụng: %s (%s) — Giảm %.0f%%   →   Cột \"Giá sau KM\" đã trừ sẵn",
                    activeTenKM, activeMaKM, activePhanTramGiam));
            kmBanner.setStyle("-fx-background-color: #FFF7E6; -fx-text-fill: #D48806;"
                    + "-fx-padding: 8 14; -fx-background-radius: 8;"
                    + "-fx-border-color: #FFD591; -fx-border-width: 1; -fx-border-radius: 8;"
                    + "-fx-font-size: 12px; -fx-font-weight: bold;");
            header.getChildren().add(kmBanner);
        }

        // Toolbar
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(12, 16, 12, 16));
        toolbar.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label hint = new Label("💡 Double-click vào dòng để chỉnh sửa");
        hint.setStyle("-fx-font-size: 12px; -fx-text-fill: #8C8C8C; -fx-font-style: italic;");

        Button btnThem    = actionBtn("＋ Thêm Mới", "#52C41A");
        Button btnXoa     = actionBtn("🗑 Xóa",       "#FF4D4F");
        Button btnRefresh = actionBtn("↻ Làm Mới",    "#8C8C8C");

        toolbar.getChildren().addAll(hint, spacer, btnRefresh, btnThem, btnXoa);

        // Table
        items = FXCollections.observableArrayList(loadData());
        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        table.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
                openDialog(table.getSelectionModel().getSelectedItem(), false);
            }
        });
        btnThem.setOnAction(e -> openDialog(null, true));
        btnXoa.setOnAction(e -> handleXoa());
        btnRefresh.setOnAction(e -> {
            loadActiveKhuyenMai();
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

        String[] heads = {"Mã BG", "Loại Phòng", "Loại Thuê", "Đơn Giá", "% Giảm KM", "Giá Sau KM", "Ngày Bắt Đầu", "Ngày Kết Thúc"};
        for (int i = 0; i < heads.length; i++) {
            final int idx = i;
            TableColumn<Object[], String> col = new TableColumn<>(heads[i]);
            col.setCellValueFactory(p -> {
                Object v = idx < p.getValue().length ? p.getValue()[idx] : "";
                return new SimpleStringProperty(v != null ? v.toString() : "");
            });
            if (i == 3) {
                col.setCellFactory(tc -> new TableCell<>() {
                    @Override protected void updateItem(String s, boolean empty) {
                        super.updateItem(s, empty);
                        setText(empty || s == null ? null : s);
                        setStyle(empty ? "" : "-fx-text-fill: #8C8C8C;"
                                + (s != null && activePhanTramGiam > 0 ? "-fx-strikethrough: true;" : ""));
                    }
                });
            }
            if (i == 4) {  // % Giảm
                col.setCellFactory(tc -> new TableCell<>() {
                    @Override protected void updateItem(String s, boolean empty) {
                        super.updateItem(s, empty);
                        if (empty || s == null || s.isEmpty() || s.equals("—")) { setText(s); setStyle(""); return; }
                        setText(s);
                        setStyle("-fx-text-fill: #D48806; -fx-font-weight: bold;");
                    }
                });
            }
            if (i == 5) {  // Giá Sau KM (highlight)
                col.setCellFactory(tc -> new TableCell<>() {
                    @Override protected void updateItem(String s, boolean empty) {
                        super.updateItem(s, empty);
                        setText(empty || s == null ? null : s);
                        setStyle(empty ? "" : "-fx-font-weight: bold; -fx-text-fill: #FF4D4F;");
                    }
                });
            }
            tbl.getColumns().add(col);
        }
        tbl.setItems(items);
        tbl.setPlaceholder(new Label("Không có dữ liệu bảng giá."));
        return tbl;
    }

    private void openDialog(Object[] row, boolean isNew) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(isNew ? "Thêm Bảng Giá" : "Sửa Bảng Giá");
        dialog.setResizable(false);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(20));
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPrefWidth(150);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPrefWidth(200);
        form.getColumnConstraints().addAll(c1, c2);

        TextField txtMa = new TextField(row != null ? row[0].toString() : "");
        txtMa.setEditable(isNew);
        txtMa.setStyle(fieldStyle());

        ComboBox<String> cbLoaiPhong = new ComboBox<>();
        cbLoaiPhong.getItems().addAll(loaiPhongArr);
        cbLoaiPhong.setValue(row != null ? row[1].toString() : loaiPhongArr[0]);
        cbLoaiPhong.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> cbLoaiThue = new ComboBox<>();
        cbLoaiThue.getItems().addAll("QuaDem", "TheoNgay", "TheoGio");
        cbLoaiThue.setValue(row != null ? row[2].toString() : "QuaDem");
        cbLoaiThue.setMaxWidth(Double.MAX_VALUE);

        TextField txtDonGia = new TextField(row != null ? row[3].toString().replaceAll("[^0-9.]", "") : "");
        txtDonGia.setStyle(fieldStyle());

        DatePicker dpBD = new DatePicker(LocalDate.now());
        DatePicker dpKT = new DatePicker(LocalDate.of(LocalDate.now().getYear(), 12, 31));
        dpBD.setMaxWidth(Double.MAX_VALUE);
        dpKT.setMaxWidth(Double.MAX_VALUE);

        form.add(lbl("Mã bảng giá *:"),   0, 0); form.add(txtMa,        1, 0);
        form.add(lbl("Loại phòng *:"),     0, 1); form.add(cbLoaiPhong,  1, 1);
        form.add(lbl("Loại thuê *:"),      0, 2); form.add(cbLoaiThue,   1, 2);
        form.add(lbl("Đơn giá (VNĐ) *:"), 0, 3); form.add(txtDonGia,    1, 3);
        form.add(lbl("Ngày bắt đầu *:"),  0, 4); form.add(dpBD,         1, 4);
        form.add(lbl("Ngày kết thúc *:"), 0, 5); form.add(dpKT,         1, 5);

        Button btnSave   = new Button(isNew ? "Thêm" : "Lưu");
        btnSave.setStyle("-fx-background-color: " + (isNew ? "#52C41A" : "#1890FF")
                + "; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 8 20; -fx-cursor: hand;");
        Button btnCancel = new Button("Hủy");
        btnCancel.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #595959;"
                + "-fx-background-radius: 6; -fx-padding: 8 20; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> dialog.close());

        btnSave.setOnAction(e -> {
            if (txtMa.getText().trim().isEmpty() || txtDonGia.getText().trim().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Vui lòng điền đầy đủ các trường bắt buộc (*).").showAndWait();
                return;
            }
            double donGia;
            try { donGia = Double.parseDouble(txtDonGia.getText().trim()); if (donGia <= 0) throw new Exception(); }
            catch (Exception ex) { new Alert(Alert.AlertType.ERROR, "Đơn giá phải là số dương!").showAndWait(); return; }
            if (dpKT.getValue() == null || dpBD.getValue() == null || !dpKT.getValue().isAfter(dpBD.getValue())) {
                new Alert(Alert.AlertType.WARNING, "Ngày kết thúc phải sau ngày bắt đầu!").showAndWait(); return;
            }
            try {
                BangGia bg = new BangGia(
                    txtMa.getText().trim(), cbLoaiPhong.getValue(), cbLoaiThue.getValue(), donGia,
                    Date.valueOf(dpBD.getValue()), Date.valueOf(dpKT.getValue())
                );
                boolean ok = isNew ? new BangGiaDAO().them(bg) : new BangGiaDAO().sua(bg);
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
        dialog.setScene(new Scene(root, 400, 360));
        dialog.showAndWait();
    }

    private void handleXoa() {
        Object[] selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng chọn bảng giá cần xóa!").showAndWait(); return;
        }
        String ma = selected[0].toString();
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Xóa bảng giá \"" + ma + "\"?");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    if (new BangGiaDAO().xoa(ma)) refresh();
                    else new Alert(Alert.AlertType.ERROR, "Không thể xóa.").showAndWait();
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR, "Lỗi DB.").showAndWait();
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
            DecimalFormat df  = new DecimalFormat("#,### VNĐ");
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            List<BangGia> list = new BangGiaDAO().getAll();
            if (!list.isEmpty()) {
                for (BangGia bg : list) {
                    String giamStr  = activePhanTramGiam > 0
                            ? String.format("-%.0f%%", activePhanTramGiam) : "—";
                    double sauGiam  = bg.getDonGia() * (1 - activePhanTramGiam / 100.0);
                    String sauStr   = activePhanTramGiam > 0
                            ? df.format(sauGiam) : df.format(bg.getDonGia());
                    result.add(new Object[]{
                        bg.getMaBangGia(), bg.getMaLoaiPhong(), bg.getLoaiThue(),
                        df.format(bg.getDonGia()),
                        giamStr, sauStr,
                        bg.getNgayBatDau()  != null ? sdf.format(bg.getNgayBatDau())  : "",
                        bg.getNgayKetThuc() != null ? sdf.format(bg.getNgayKetThuc()) : ""
                    });
                }
                return result;
            }
        } catch (Exception ignored) {}
        // Fallback DEMO — chèn giá trị placeholder cho 2 cột mới
        DecimalFormat df = new DecimalFormat("#,### VNĐ");
        for (Object[] r : DEMO_BG) {
            String giaStr = r[3].toString();
            double gia = 0;
            try { gia = Double.parseDouble(giaStr.replaceAll("[^0-9]", "")); } catch (Exception ignored) {}
            String giamStr = activePhanTramGiam > 0 ? String.format("-%.0f%%", activePhanTramGiam) : "—";
            String sauStr  = activePhanTramGiam > 0 ? df.format(gia * (1 - activePhanTramGiam / 100.0)) : giaStr;
            result.add(new Object[]{r[0], r[1], r[2], r[3], giamStr, sauStr, r[4], r[5]});
        }
        return result;
    }

    private Button actionBtn(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand; -fx-font-size: 12px;");
        return btn;
    }

    private Label lbl(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #595959;");
        return l;
    }

    private String fieldStyle() {
        return "-fx-background-color: #FFFFFF; -fx-border-color: #D9D9D9;"
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1; -fx-padding: 7 10;";
    }
}
