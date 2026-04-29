package com.lotuslaverne.fx.views;

import com.lotuslaverne.dao.NhanVienDAO;
import com.lotuslaverne.entity.NhanVien;
import com.lotuslaverne.fx.UiUtils;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;



public class NhanVienView {

    private static final String[] CA_LAM = {"Sáng", "Chiều", "Đêm", "Hành Chính"};
    private static final String[] VAI_TRO_LIST = {"LeTan", "QuanLy"};

    private ObservableList<Object[]> items;
    private TableView<Object[]> table;
    private Label countLbl;

    /** DB → hiển thị */
    private static String caToDisplay(String db) {
        if (db == null) return "—";
        return switch (db) {
            case "Sang"      -> "Sáng";
            case "Chieu"     -> "Chiều";
            case "Dem"       -> "Đêm";
            case "HanhChinh" -> "Hành Chính";
            default -> db;
        };
    }

    /** Hiển thị → DB */
    private static String caToDB(String display) {
        if (display == null) return "Sang";
        return switch (display) {
            case "Sáng"         -> "Sang";
            case "Chiều"        -> "Chieu";
            case "Đêm"          -> "Dem";
            case "Hành Chính"  -> "HanhChinh";
            default -> display;
        };
    }

    private static String vaiTroDisplay(String db) {
        return "QuanLy".equals(db) ? "Quản Lý" : "Lễ Tân";
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

        VBox header = new VBox(4);
        Label title = new Label("Quản Lý Nhân Viên");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        countLbl = new Label("Đang tải...");
        countLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #8C8C8C;");
        header.getChildren().addAll(title, countLbl);

        items = FXCollections.observableArrayList(loadNhanVien());
        updateCountLabel();

        content.getChildren().addAll(header, buildFormCard(), buildFilterToolbar(), buildTable());
        scroll.setContent(content);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        root.getChildren().add(scroll);
        return root;
    }

    // ─── Form Thêm Mới ───────────────────────────────────────────────────
    private Node buildFormCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 1);");

        Label cardTitle = new Label("+ Thêm Nhân Viên Mới");
        cardTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;"
                + "-fx-padding: 0 0 10 0; -fx-border-color: transparent transparent #F0F2F5 transparent;"
                + "-fx-border-width: 0 0 1 0;");

        GridPane form = new GridPane();
        form.setHgap(16); form.setVgap(10);
        for (int i = 0; i < 4; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(25);
            form.getColumnConstraints().add(cc);
        }

        TextField tfTen  = formField("Họ và tên *");
        TextField tfSDT  = formField("Số điện thoại *");
        TextField tfCCCD = formField("Số CCCD");
        TextField tfEmail= formField("Email");

        ComboBox<String> cbVaiTro = new ComboBox<>();
        cbVaiTro.getItems().addAll("Lễ Tân", "Quản Lý");
        cbVaiTro.setValue("Lễ Tân");
        cbVaiTro.setMaxWidth(Double.MAX_VALUE);
        cbVaiTro.setStyle(comboStyle());

        ComboBox<String> cbCa = new ComboBox<>();
        cbCa.getItems().addAll(CA_LAM);
        cbCa.setValue("Sáng");
        cbCa.setMaxWidth(Double.MAX_VALUE);
        cbCa.setStyle(comboStyle());

        form.add(formLabel("Họ Và Tên *"),  0, 0); form.add(tfTen,   0, 1);
        form.add(formLabel("SĐT *"),         1, 0); form.add(tfSDT,   1, 1);
        form.add(formLabel("Số CCCD"),       2, 0); form.add(tfCCCD,  2, 1);
        form.add(formLabel("Email"),         3, 0); form.add(tfEmail, 3, 1);
        form.add(formLabel("Vai Trò *"),     0, 2); form.add(cbVaiTro,0, 3);
        form.add(formLabel("Ca Làm Việc"),   1, 2); form.add(cbCa,    1, 3);

        Label errLbl = new Label();
        errLbl.setStyle("-fx-text-fill: #FF4D4F; -fx-font-size: 12px;");
        errLbl.setVisible(false); errLbl.setManaged(false);

        Button addBtn = new Button("+ Thêm Nhân Viên");
        addBtn.setStyle("-fx-background-color: #1890FF; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-padding: 8 20; -fx-font-weight: bold; -fx-cursor: hand;");

        addBtn.setOnAction(e -> {
            String ten = tfTen.getText().trim();
            String sdt = tfSDT.getText().trim();
            if (ten.isEmpty() || sdt.isEmpty()) {
                errLbl.setText("Vui lòng nhập Họ Tên và Số Điện Thoại!");
                errLbl.setVisible(true); errLbl.setManaged(true); return;
            }
            String maNV  = "NV" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            String vaiTro = "Quản Lý".equals(cbVaiTro.getValue()) ? "QuanLy" : "LeTan";
            String caDB   = caToDB(cbCa.getValue());
            NhanVien nv = new NhanVien(maNV, ten, sdt, vaiTro);
            nv.setCaLamViec(caDB);
            // Truyền CCCD và Email vào entity
            String cccd = tfCCCD.getText().trim();
            nv.setCccd(cccd.isEmpty() ? null : cccd);
            String email = tfEmail.getText().trim();
            nv.setEmail(email.isEmpty() ? null : email);
            try {
                boolean ok = new NhanVienDAO().themNhanVien(nv);
                if (ok) {
                    items.add(new Object[]{maNV, ten, vaiTroDisplay(vaiTro), sdt,
                            email.isEmpty() ? "—" : email, "N/A", "Đang Làm", caToDisplay(caDB)});
                    updateCountLabel();
                    tfTen.clear(); tfSDT.clear(); tfCCCD.clear(); tfEmail.clear();
                    errLbl.setVisible(false); errLbl.setManaged(false);
                    alert(Alert.AlertType.INFORMATION, "Thành Công", "Thêm nhân viên thành công!\nMã NV: " + maNV);
                } else {
                    errLbl.setText("Lỗi thêm vào DB! Kiểm tra SĐT hoặc CCCD trùng.");
                    errLbl.setVisible(true); errLbl.setManaged(true);
                }
            } catch (Exception ex) {
                errLbl.setText("Lỗi: " + ex.getMessage());
                errLbl.setVisible(true); errLbl.setManaged(true);
            }
        });

        HBox btnRow = new HBox(addBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(8, 0, 0, 0));
        card.getChildren().addAll(cardTitle, form, errLbl, btnRow);
        return card;
    }

    // ─── Toolbar tìm kiếm + lọc ─────────────────────────────────────────
    private HBox buildFilterToolbar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12, 16, 12, 16));
        bar.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 1);");

        TextField search = new TextField();
        search.setPromptText("🔍  Tìm theo tên, vai trò, SĐT...");
        search.setPrefWidth(260);
        search.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #E8E8E8;"
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 1;"
                + "-fx-padding: 8 12; -fx-font-size: 13px;");

        search.textProperty().addListener((obs, o, n) -> applyFilter(n));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnRefresh = new Button("↻  Làm Mới");
        btnRefresh.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #595959;"
                + "-fx-background-radius: 8; -fx-border-color: #D9D9D9; -fx-border-width: 1;"
                + "-fx-border-radius: 8; -fx-padding: 8 14; -fx-cursor: hand;");
        btnRefresh.setOnAction(e -> {
            items.setAll(loadNhanVien());
            table.setItems(items);
            updateCountLabel();
            UiUtils.flashButton(btnRefresh, "✓ Đã làm mới");
        });

        bar.getChildren().addAll(search, spacer, btnRefresh);
        return bar;
    }

    private void applyFilter(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            table.setItems(items); return;
        }
        String kw = keyword.toLowerCase();
        ObservableList<Object[]> filtered = FXCollections.observableArrayList();
        for (Object[] r : items) {
            for (Object cell : r) {
                if (cell != null && cell.toString().toLowerCase().contains(kw)) {
                    filtered.add(r); break;
                }
            }
        }
        table.setItems(filtered);
    }

    // ─── Table ──────────────────────────────────────────────────────────
    private TableView<Object[]> buildTable() {
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-border-radius: 10; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        TableColumn<Object[], String> colMa = col("Mã NV", 0);
        colMa.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? null : s);
                setStyle(empty ? "" : "-fx-font-weight: bold; -fx-text-fill: #1890FF;");
            }
        });

        TableColumn<Object[], String> colTen = col("Họ Tên", 1);
        colTen.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                HBox box = new HBox(8); box.setAlignment(Pos.CENTER_LEFT);
                Label name = new Label(item);
                name.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
                box.getChildren().addAll(UiUtils.makeAvatarCircle(item, 14), name);
                setGraphic(box); setText(null);
            }
        });

        TableColumn<Object[], String> colVT  = col("Vai Trò", 2);
        TableColumn<Object[], String> colSDT = col("SĐT", 3);
        TableColumn<Object[], String> colNgay= col("Ngày Vào Làm", 5);

        TableColumn<Object[], String> colTT = new TableColumn<>("Trạng Thái");
        colTT.setCellValueFactory(p -> new SimpleStringProperty((String) p.getValue()[6]));
        colTT.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label b = new Label(item);
                String bg = "Đang Làm".equals(item) ? "#F6FFED" : "#FFFBE6";
                String fg = "Đang Làm".equals(item) ? "#52C41A" : "#FAAD14";
                b.setStyle("-fx-background-color:" + bg + ";-fx-text-fill:" + fg
                        + ";-fx-padding:2 8;-fx-background-radius:10;-fx-font-size:11px;-fx-font-weight:bold;");
                setGraphic(b); setText(null);
            }
        });

        TableColumn<Object[], String> colCa = col("Ca Làm", 7);

        // ── Cột Thao Tác với sự kiện đầy đủ ──
        TableColumn<Object[], Void> colAct = new TableColumn<>("Thao Tác");
        colAct.setPrefWidth(130);
        colAct.setCellFactory(tc -> new TableCell<>() {
            private final Button btnSua = styledBtn("✏ Sửa", "#E6F4FF", "#1890FF");
            private final Button btnXoa = styledBtn("🗑 Xóa", "#FFF1F0", "#FF4D4F");
            {
                btnSua.setOnAction(e -> {
                    Object[] row = getTableView().getItems().get(getIndex());
                    openEditDialog(row);
                });
                btnXoa.setOnAction(e -> {
                    Object[] row = getTableView().getItems().get(getIndex());
                    String ma = (String) row[0];
                    Alert c = new Alert(Alert.AlertType.CONFIRMATION,
                            "Xóa nhân viên " + row[1] + " (" + ma + ")?\nLưu ý: không thể xóa nếu còn phiếu đặt phòng liên quan.");
                    c.setHeaderText("Xác Nhận Xóa");
                    c.showAndWait().ifPresent(btn -> {
                        if (btn == ButtonType.OK) {
                            try {
                                boolean ok = new NhanVienDAO().xoaNhanVien(ma);
                                if (ok) {
                                    items.remove(row);
                                    table.setItems(items);
                                    updateCountLabel();
                                } else {
                                    alert(Alert.AlertType.ERROR, "Không Thể Xóa",
                                            "Không thể xóa nhân viên này!\nCó thể còn dữ liệu liên quan (phiếu đặt phòng, hóa đơn).");
                                }
                            } catch (Exception ex) {
                                alert(Alert.AlertType.ERROR, "Lỗi DB", ex.getMessage());
                            }
                        }
                    });
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                HBox box = new HBox(6, btnSua, btnXoa);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
            }
        });

        table.getColumns().addAll(colMa, colTen, colVT, colSDT, colNgay, colTT, colCa, colAct);
        table.setItems(items);
        table.setPlaceholder(new Label("Không có nhân viên nào."));
        return table;
    }

    // ─── Dialog Sửa ─────────────────────────────────────────────────────
    private void openEditDialog(Object[] row) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Sửa Nhân Viên - " + row[0]);
        dialog.setResizable(false);

        GridPane form = new GridPane();
        form.setHgap(12); form.setVgap(12);
        form.setPadding(new Insets(20));
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPrefWidth(130);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPrefWidth(220);
        form.getColumnConstraints().addAll(c1, c2);

        Label maLbl = new Label((String) row[0]);
        maLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #1890FF;");
        TextField tfTen = editField(row[1].toString());
        TextField tfSDT = editField(row[3].toString());

        ComboBox<String> cbVaiTro = new ComboBox<>();
        cbVaiTro.getItems().addAll("Lễ Tân", "Quản Lý");
        cbVaiTro.setValue(row[2].toString());
        cbVaiTro.setMaxWidth(Double.MAX_VALUE);
        cbVaiTro.setStyle(comboStyle());

        ComboBox<String> cbCa = new ComboBox<>();
        cbCa.getItems().addAll(CA_LAM);
        cbCa.setValue(row.length > 7 && row[7] != null ? row[7].toString() : "Sáng");
        cbCa.setMaxWidth(Double.MAX_VALUE);
        cbCa.setStyle(comboStyle());

        form.add(formLabel("Mã NV:"),       0, 0); form.add(maLbl,    1, 0);
        form.add(formLabel("Họ Tên *:"),    0, 1); form.add(tfTen,    1, 1);
        form.add(formLabel("SĐT *:"),       0, 2); form.add(tfSDT,    1, 2);
        form.add(formLabel("Vai Trò:"),     0, 3); form.add(cbVaiTro, 1, 3);
        form.add(formLabel("Ca Làm:"),      0, 4); form.add(cbCa,     1, 4);

        Button btnSave   = new Button("Lưu Thay Đổi");
        btnSave.setStyle("-fx-background-color: #1890FF; -fx-text-fill: white;"
                + "-fx-background-radius: 6; -fx-padding: 8 20; -fx-cursor: hand;");
        Button btnCancel = new Button("Hủy");
        btnCancel.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #595959;"
                + "-fx-background-radius: 6; -fx-padding: 8 20; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> dialog.close());

        btnSave.setOnAction(e -> {
            String ten = tfTen.getText().trim();
            String sdt = tfSDT.getText().trim();
            if (ten.isEmpty() || sdt.isEmpty()) {
                alert(Alert.AlertType.WARNING, "Thiếu Thông Tin", "Họ tên và SĐT không được để trống!"); return;
            }
            String vaiTro = "Quản Lý".equals(cbVaiTro.getValue()) ? "QuanLy" : "LeTan";
            String caDB   = caToDB(cbCa.getValue());
            NhanVien nv = new NhanVien((String) row[0], ten, sdt, vaiTro);
            nv.setCaLamViec(caDB);
            try {
                boolean ok = new NhanVienDAO().suaNhanVien(nv);
                if (ok) {
                    // Cập nhật trực tiếp trong observable list
                    row[1] = ten; row[2] = cbVaiTro.getValue(); row[3] = sdt; row[7] = cbCa.getValue();
                    table.refresh();
                    dialog.close();
                } else {
                    alert(Alert.AlertType.ERROR, "Lỗi", "Cập nhật thất bại! Kiểm tra cơ sở dữ liệu.");
                }
            } catch (Exception ex) {
                alert(Alert.AlertType.ERROR, "Lỗi DB", ex.getMessage());
            }
        });

        HBox btnRow = new HBox(10, btnCancel, btnSave);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(0, 20, 16, 20));

        VBox root = new VBox(form, btnRow);
        root.setStyle("-fx-background-color: #FFFFFF;");
        dialog.setScene(new Scene(root, 400, 300));
        dialog.showAndWait();
    }

    // ─── Load data ──────────────────────────────────────────────────────
    private List<Object[]> loadNhanVien() {
        List<Object[]> result = new ArrayList<>();
        try {
            NhanVienDAO dao = new NhanVienDAO();
            List<NhanVien> list = dao.getAll();
            if (!list.isEmpty()) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                for (NhanVien nv : list) {
                    String email = nv.getEmail() != null ? nv.getEmail() : "—";
                    String ngayVao = nv.getNgayBatDauLam() != null ? sdf.format(nv.getNgayBatDauLam()) : "N/A";
                    result.add(new Object[]{
                        nv.getMaNhanVien(),
                        nv.getTenNhanVien(),
                        vaiTroDisplay(nv.getVaiTro()),
                        nv.getSoDienThoai() != null ? nv.getSoDienThoai() : "—",
                        email,
                        ngayVao,
                        "Đang Làm",
                        caToDisplay(nv.getCaLamViec())
                    });
                }
                return result;
            }
        } catch (Exception ignored) {}
        // Demo khi offline
        result.add(new Object[]{"NV001","Nguyễn Văn Anh","Quản Lý","0912345678","anh.nv@lotus.vn","01/01/2022","Đang Làm","Hành Chính"});
        result.add(new Object[]{"NV002","Trần Thị Bình","Lễ Tân","0923456789","binh.tt@lotus.vn","01/03/2023","Đang Làm","Sáng"});
        result.add(new Object[]{"NV003","Lê Văn Cường","Lễ Tân","0934567890","cuong.lv@lotus.vn","15/01/2024","Đang Làm","Chiều"});
        return result;
    }



    private void updateCountLabel() {
        if (countLbl != null) {
            countLbl.setText("Tổng cộng " + items.size() + " nhân viên trong hệ thống");
        }
    }

    // ─── Helpers ────────────────────────────────────────────────────────
    private TableColumn<Object[], String> col(String title, int idx) {
        TableColumn<Object[], String> c = new TableColumn<>(title);
        c.setCellValueFactory(p -> {
            Object v = idx < p.getValue().length ? p.getValue()[idx] : "";
            return new SimpleStringProperty(v != null ? v.toString() : "—");
        });
        return c;
    }

    private Label formLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #595959;");
        return l;
    }

    private TextField formField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setMaxWidth(Double.MAX_VALUE);
        tf.setStyle("-fx-background-color:#FFFFFF;-fx-border-color:#D9D9D9;"
                + "-fx-border-radius:6;-fx-background-radius:6;-fx-border-width:1;-fx-padding:7 10;");
        return tf;
    }

    private TextField editField(String val) {
        TextField tf = new TextField(val);
        tf.setMaxWidth(Double.MAX_VALUE);
        tf.setStyle("-fx-background-color:#FFFFFF;-fx-border-color:#D9D9D9;"
                + "-fx-border-radius:6;-fx-background-radius:6;-fx-border-width:1;-fx-padding:7 10;");
        return tf;
    }

    private String comboStyle() {
        return "-fx-background-color:#FFFFFF;-fx-border-color:#D9D9D9;"
                + "-fx-border-radius:6;-fx-background-radius:6;-fx-border-width:1;";
    }

    private Button styledBtn(String text, String bg, String fg) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + bg + ";-fx-text-fill:" + fg + ";"
                + "-fx-background-radius:6;-fx-border-radius:6;"
                + "-fx-font-size:11px;-fx-padding:3 8;-fx-cursor:hand;");
        return b;
    }

    private void alert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type, msg);
        a.setHeaderText(null); a.setTitle(title); a.showAndWait();
    }
}
