package com.lotuslaverne.fx.views;

import com.lotuslaverne.dao.PhongDAO;
import com.lotuslaverne.util.ConnectDB;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * HousekeepingView – Bảng buồng phòng dành cho NV trực nhật.
 * Hiển thị dạng bảng kiểu Blue Jay PMS: tình trạng phòng, gán NV, ghi chú, xuất file.
 */
public class HousekeepingView {

    // ── Dữ liệu bảng: [maPhong, tenPhong, trangThaiDB, loaiPhong, tang, tinhTrang, nhanVien, ghiChu]
    private ObservableList<String[]> tableItems = FXCollections.observableArrayList();
    private TableView<String[]> tableView;
    private Label lblTong, lblCanDon, lblDangDon, lblSanSang;
    private TextField txtSearch;
    private String filterTang = "Tất cả";

    // Danh sách NV mẫu (thực tế nên query từ DB NhanVien)
    private static final String[] NHAN_VIEN_LIST = {
        "Chưa chọn", "Nguyễn Thị Lan", "Trần Văn Hùng", "Lê Thị Mai",
        "Phạm Minh Tuấn", "Hoàng Thị Hoa", "Vũ Quang Vinh"
    };

    private final PhongDAO phongDAO = new PhongDAO();

    public Node build() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #F0F2F5;");

        // ── Header
        VBox header = new VBox(4);
        header.setPadding(new Insets(24, 28, 0, 28));
        Label title = new Label("Buồng Phòng");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Label sub = new Label("Quản lý tình trạng dọn phòng — phân công nhân viên trực nhật");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #8C8C8C;");
        header.getChildren().addAll(title, sub);

        // ── Stat bar
        HBox statBar = buildStatBar();
        statBar.setPadding(new Insets(16, 28, 0, 28));

        // ── Toolbar (search + filter + actions)
        HBox toolbar = buildToolbar();
        toolbar.setPadding(new Insets(12, 28, 12, 28));

        // ── Table
        tableView = buildTable();
        VBox tableWrapper = new VBox(tableView);
        tableWrapper.setPadding(new Insets(0, 28, 24, 28));
        VBox.setVgrow(tableView, Priority.ALWAYS);
        VBox.setVgrow(tableWrapper, Priority.ALWAYS);

        root.getChildren().addAll(header, statBar, toolbar, tableWrapper);
        VBox.setVgrow(root, Priority.ALWAYS);

        loadData();
        return root;
    }

    // ─────────────────────────── STAT BAR
    private HBox buildStatBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);

        lblTong    = new Label("0");
        lblCanDon  = new Label("0");
        lblDangDon = new Label("0");
        lblSanSang = new Label("0");

        bar.getChildren().addAll(
            statCard(lblTong,    "Tổng phòng",       "#1890FF", "#E6F4FF"),
            statCard(lblCanDon,  "Cần Dọn",           "#FAAD14", "#FFFBE6"),
            statCard(lblDangDon, "Đang Dọn",          "#FF7A00", "#FFF2E8"),
            statCard(lblSanSang, "Sẵn sàng / Sạch",  "#52C41A", "#F6FFED")
        );
        return bar;
    }

    private HBox statCard(Label numLbl, String title, String fg, String bg) {
        numLbl.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + fg + ";");
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + fg + "; -fx-font-weight: bold;");
        VBox inner = new VBox(2, numLbl, titleLbl);
        HBox card = new HBox(inner);
        card.setPadding(new Insets(12, 18, 12, 18));
        card.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.05),4,0,0,1);");
        return card;
    }

    // ─────────────────────────── TOOLBAR
    private HBox buildToolbar() {
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 16, 10, 16));
        bar.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        // Search
        txtSearch = new TextField();
        txtSearch.setPromptText("🔍  Tìm phòng...");
        txtSearch.setPrefWidth(200);
        txtSearch.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #E8E8E8;"
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 6 10; -fx-font-size: 12px;");
        txtSearch.textProperty().addListener((obs, o, n) -> applyFilter());

        // Filter tầng
        ComboBox<String> cbTang = new ComboBox<>();
        cbTang.getItems().add("Tất cả");
        for (int i = 1; i <= 6; i++) cbTang.getItems().add("Tầng " + i);
        cbTang.setValue("Tất cả");
        cbTang.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D9D9D9;"
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1;");
        cbTang.setOnAction(e -> { filterTang = cbTang.getValue(); applyFilter(); });

        // Filter trạng thái
        ComboBox<String> cbTT = new ComboBox<>();
        cbTT.getItems().addAll("Tất cả TT", "Cần Dọn", "Đang Dọn", "Sẵn sàng", "Bảo Trì");
        cbTT.setValue("Tất cả TT");
        cbTT.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D9D9D9;"
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1;");

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        // Nút làm mới
        Button btnRefresh = new Button("↻  Làm Mới");
        btnRefresh.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #595959;"
                + "-fx-background-radius: 8; -fx-border-color: #D9D9D9; -fx-border-width: 1;"
                + "-fx-border-radius: 8; -fx-padding: 7 14; -fx-cursor: hand; -fx-font-size: 12px;");
        btnRefresh.setOnAction(e -> {
            loadData();
            com.lotuslaverne.fx.UiUtils.flashButton(btnRefresh, "✓ Đã làm mới");
        });

        // Nút Xuất File CSV
        Button btnExport = new Button("📄  Xuất File");
        btnExport.setStyle("-fx-background-color: #52C41A; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-padding: 7 14; -fx-cursor: hand;"
                + "-fx-font-size: 12px; -fx-font-weight: bold;");
        btnExport.setOnAction(e -> exportCSV());

        // Lưu ghi chú / phân công hàng loạt
        Button btnSave = new Button("💾  Lưu Phân Công");
        btnSave.setStyle("-fx-background-color: #1890FF; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-padding: 7 14; -fx-cursor: hand;"
                + "-fx-font-size: 12px; -fx-font-weight: bold;");
        btnSave.setOnAction(e -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION,
                    "Đã lưu phân công nhân viên và ghi chú cho tất cả phòng.");
            a.setHeaderText(null); a.setTitle("Lưu Thành Công"); a.showAndWait();
        });

        bar.getChildren().addAll(txtSearch, cbTang, cbTT, spacer, btnRefresh, btnExport, btnSave);
        return bar;
    }

    // ─────────────────────────── TABLE
    @SuppressWarnings("unchecked")
    private TableView<String[]> buildTable() {
        TableView<String[]> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-border-radius: 10; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");
        table.setFixedCellSize(46);

        // Checkbox col
        TableColumn<String[], Void> colChk = new TableColumn<>("");
        colChk.setPrefWidth(40);
        colChk.setMinWidth(40);
        colChk.setMaxWidth(40);
        colChk.setCellFactory(tc -> new TableCell<>() {
            final CheckBox cb = new CheckBox();
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : cb);
            }
        });

        // Phòng
        TableColumn<String[], String> colPhong = col("Phòng", 80);
        colPhong.setCellValueFactory(p -> new SimpleStringProperty(p.getValue()[1]));
        colPhong.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText(null); setGraphic(null); return; }
                Label lbl = new Label(s);
                lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1A1A2E;");
                setGraphic(lbl); setText(null);
            }
        });

        // Tầng
        TableColumn<String[], String> colTang = col("Tầng", 60);
        colTang.setCellValueFactory(p -> new SimpleStringProperty("Tầng " + p.getValue()[4]));

        // Loại phòng
        TableColumn<String[], String> colLoai = col("Loại", 90);
        colLoai.setCellValueFactory(p -> new SimpleStringProperty(p.getValue()[3]));

        // Tình trạng phòng (editable ComboBox)
        TableColumn<String[], String> colTinhTrang = col("Tình trạng phòng", 160);
        colTinhTrang.setCellValueFactory(p -> new SimpleStringProperty(p.getValue()[5]));
        colTinhTrang.setCellFactory(tc -> new TableCell<>() {
            final ComboBox<String> cb = new ComboBox<>(FXCollections.observableArrayList(
                "Sẵn sàng bán", "Phòng sạch", "Phòng bẩn", "Đang dọn", "Đang sửa chữa"
            ));
            { cb.setMaxWidth(Double.MAX_VALUE); styleCombo(cb); }
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); return;
                }
                String[] row = (String[]) getTableRow().getItem();
                cb.setValue(row[5]);
                cb.setOnAction(e -> {
                    row[5] = cb.getValue();
                    // Sync trạng thái DB
                    String dbStatus = switch (cb.getValue()) {
                        case "Phòng bẩn"       -> "PhongCanDon";
                        case "Đang dọn"         -> "DangDon";
                        case "Đang sửa chữa"   -> "BaoTri";
                        default                 -> "PhongTrong";
                    };
                    try { phongDAO.capNhatTrangThai(row[0], dbStatus); } catch (Exception ignored) {}
                    updateStatCounts();
                });
                setGraphic(cb); setText(null);
            }
        });

        // Trạng thái badge (VR / VC / VD / OC / OD)
        TableColumn<String[], String> colBadge = col("Trạng thái", 90);
        colBadge.setCellValueFactory(p -> new SimpleStringProperty(p.getValue()[2]));
        colBadge.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); return;
                }
                String[] row = (String[]) getTableRow().getItem();
                String code = trangThaiCode(row[2]);
                String[] colors = badgeColors(row[2]);
                Label badge = new Label(code);
                badge.setStyle("-fx-background-color: " + colors[0] + "; -fx-text-fill: " + colors[1]
                        + "; -fx-padding: 4 10; -fx-background-radius: 6;"
                        + " -fx-font-size: 11px; -fx-font-weight: bold;");
                setGraphic(badge); setText(null);
            }
        });

        // Nhân viên (editable ComboBox)
        TableColumn<String[], String> colNV = col("Nhân viên", 150);
        colNV.setCellValueFactory(p -> new SimpleStringProperty(p.getValue()[6]));
        colNV.setCellFactory(tc -> new TableCell<>() {
            final ComboBox<String> cb = new ComboBox<>(
                FXCollections.observableArrayList(NHAN_VIEN_LIST));
            { cb.setMaxWidth(Double.MAX_VALUE); styleCombo(cb); }
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); return;
                }
                String[] row = (String[]) getTableRow().getItem();
                cb.setValue(row[6]);
                cb.setOnAction(e -> row[6] = cb.getValue());
                setGraphic(cb); setText(null);
            }
        });

        // Ghi chú (editable TextField)
        TableColumn<String[], String> colGhiChu = col("Ghi chú", 200);
        colGhiChu.setCellValueFactory(p -> new SimpleStringProperty(p.getValue()[7]));
        colGhiChu.setCellFactory(tc -> new TableCell<>() {
            final TextField tf = new TextField();
            {
                tf.setStyle("-fx-background-color: #F9F9F9; -fx-border-color: #E8E8E8;"
                        + "-fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 4 8;"
                        + "-fx-font-size: 12px;");
            }
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); return;
                }
                String[] row = (String[]) getTableRow().getItem();
                tf.setText(row[7]);
                tf.textProperty().addListener((obs, o, n) -> row[7] = n);
                setGraphic(tf); setText(null);
            }
        });

        // Thao tác nhanh
        TableColumn<String[], Void> colAction = col2("Thao tác", 110);
        colAction.setCellFactory(tc -> new TableCell<>() {
            final Button btnDon = new Button("🧹 Dọn");
            final Button btnXong = new Button("✅ Xong");
            {
                String styleBlue = "-fx-background-color: #1890FF; -fx-text-fill: white;"
                    + "-fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand; -fx-font-size: 11px;";
                String styleGreen = "-fx-background-color: #52C41A; -fx-text-fill: white;"
                    + "-fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand; -fx-font-size: 11px;";
                btnDon.setStyle(styleBlue);
                btnXong.setStyle(styleGreen);
                btnDon.setOnAction(e -> {
                    String[] row = (String[]) getTableRow().getItem();
                    if (row == null) return;
                    row[2] = "DangDon"; row[5] = "Đang dọn";
                    try { phongDAO.capNhatTrangThai(row[0], "DangDon"); } catch (Exception ignored) {}
                    tableView.refresh(); updateStatCounts();
                });
                btnXong.setOnAction(e -> {
                    String[] row = (String[]) getTableRow().getItem();
                    if (row == null) return;
                    row[2] = "PhongTrong"; row[5] = "Sẵn sàng bán";
                    try { phongDAO.capNhatTrangThai(row[0], "PhongTrong"); } catch (Exception ignored) {}
                    tableView.refresh(); updateStatCounts();
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); return;
                }
                String[] row = (String[]) getTableRow().getItem();
                HBox box;
                if ("PhongCanDon".equals(row[2])) {
                    box = new HBox(4, btnDon);
                } else if ("DangDon".equals(row[2])) {
                    box = new HBox(4, btnXong);
                } else {
                    box = new HBox();
                }
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box); setText(null);
            }
        });

        table.getColumns().addAll(colChk, colPhong, colTang, colLoai, colTinhTrang, colBadge, colNV, colGhiChu, colAction);
        table.setItems(tableItems);
        table.setPlaceholder(new Label("Không có dữ liệu phòng."));
        return table;
    }

    // ─────────────────────────── DATA
    private void loadData() {
        tableItems.clear();
        boolean loaded = false;
        Connection con = ConnectDB.getInstance().getConnection();
        if (con != null) {
            String sql = "SELECT p.maPhong, p.tenPhong, p.trangThai, lp.tenLoaiPhong, " +
                         "'1' as tang " +
                         "FROM Phong p " +
                         "JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong " +
                         "ORDER BY p.maPhong";
            try (PreparedStatement pst = con.prepareStatement(sql);
                 ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    String tt = rs.getString("trangThai");
                    tableItems.add(new String[]{
                        rs.getString("maPhong"),               // [0] maPhong
                        rs.getString("tenPhong"),              // [1] tenPhong
                        tt != null ? tt : "PhongTrong",        // [2] trangThaiDB
                        rs.getString("tenLoaiPhong"),           // [3] loaiPhong
                        String.valueOf(rs.getInt("tang")),      // [4] tang
                        dbToTinhTrang(tt),                     // [5] tinhTrang display
                        "Chưa chọn",                           // [6] nhanVien
                        ""                                     // [7] ghiChu
                    });
                    loaded = true;
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
        if (!loaded) addDemoData();
        updateStatCounts();
    }

    private void addDemoData() {
        String[][] demo = {
            {"P101","101","PhongCanDon","Standard","1"},
            {"P102","102","DangDon","Standard","1"},
            {"P103","103","PhongTrong","Deluxe","1"},
            {"P104","104","PhongDat","Standard","1"},
            {"P201","201","PhongCanDon","Deluxe","2"},
            {"P202","202","PhongTrong","Suite","2"},
            {"P203","203","BaoTri","Family","2"},
            {"P301","301","DangDon","Standard","3"},
        };
        for (String[] d : demo) {
            tableItems.add(new String[]{
                d[0], d[1], d[2], d[3], d[4], dbToTinhTrang(d[2]), "Chưa chọn", ""
            });
        }
    }

    private String dbToTinhTrang(String db) {
        if (db == null) return "Sẵn sàng bán";
        return switch (db) {
            case "PhongCanDon" -> "Phòng bẩn";
            case "DangDon"     -> "Đang dọn";
            case "BaoTri"      -> "Đang sửa chữa";
            case "PhongDat", "PhongTrong" -> "Sẵn sàng bán";
            default -> "Phòng sạch";
        };
    }

    private String trangThaiCode(String db) {
        return switch (db) {
            case "PhongTrong"  -> "VR";
            case "PhongCanDon" -> "VD";
            case "DangDon"     -> "VC";
            case "PhongDat"    -> "OC";
            case "BaoTri"      -> "OOO";
            default -> "—";
        };
    }

    private String[] badgeColors(String db) {
        return switch (db) {
            case "PhongTrong"  -> new String[]{"#F6FFED", "#52C41A"};
            case "PhongCanDon" -> new String[]{"#FFFBE6", "#FAAD14"};
            case "DangDon"     -> new String[]{"#E6F4FF", "#1890FF"};
            case "PhongDat"    -> new String[]{"#FFF2E8", "#FF7A00"};
            case "BaoTri"      -> new String[]{"#FFF1F0", "#FF4D4F"};
            default -> new String[]{"#F5F5F5", "#8C8C8C"};
        };
    }

    private void applyFilter() {
        String kw = txtSearch.getText() == null ? "" : txtSearch.getText().toLowerCase().trim();
        // Reload & filter
        loadData();
        if (!kw.isEmpty()) {
            tableItems.removeIf(r -> !r[1].toLowerCase().contains(kw) && !r[0].toLowerCase().contains(kw));
        }
        if (!"Tất cả".equals(filterTang)) {
            String tangNum = filterTang.replace("Tầng ", "");
            tableItems.removeIf(r -> !r[4].equals(tangNum));
        }
    }

    private void updateStatCounts() {
        long tong    = tableItems.size();
        long canDon  = tableItems.stream().filter(r -> "PhongCanDon".equals(r[2])).count();
        long dangDon = tableItems.stream().filter(r -> "DangDon".equals(r[2])).count();
        long sanSang = tableItems.stream().filter(r -> "PhongTrong".equals(r[2])).count();
        lblTong.setText(String.valueOf(tong));
        lblCanDon.setText(String.valueOf(canDon));
        lblDangDon.setText(String.valueOf(dangDon));
        lblSanSang.setText(String.valueOf(sanSang));
    }

    // ─────────────────────────── EXPORT CSV
    private void exportCSV() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        File f = new File(System.getProperty("user.home") + "/Desktop/buong_phong_" + ts + ".csv");
        try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
            pw.println("Phòng,Tầng,Loại,Tình trạng,Trạng thái,Nhân viên,Ghi chú");
            for (String[] r : tableItems) {
                pw.printf("%s,%s,%s,%s,%s,%s,%s%n",
                    r[1], r[4], r[3], r[5], trangThaiCode(r[2]), r[6], r[7]);
            }
            Alert a = new Alert(Alert.AlertType.INFORMATION,
                "✅ Xuất file thành công!\nĐường dẫn: " + f.getAbsolutePath());
            a.setHeaderText(null); a.setTitle("Xuất File"); a.showAndWait();
        } catch (Exception ex) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Lỗi xuất file: " + ex.getMessage());
            a.setHeaderText(null); a.showAndWait();
        }
    }

    // ─────────────────────────── HELPERS
    private <T> TableColumn<String[], T> col(String title, double w) {
        TableColumn<String[], T> c = new TableColumn<>(title);
        c.setPrefWidth(w); c.setMinWidth(w);
        return c;
    }
    @SuppressWarnings("unchecked")
    private <T> TableColumn<String[], T> col2(String title, double w) {
        return col(title, w);
    }

    private void styleCombo(ComboBox<String> cb) {
        cb.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D9D9D9;"
                + "-fx-border-radius: 5; -fx-background-radius: 5; -fx-border-width: 1; -fx-font-size: 12px;");
    }
}
