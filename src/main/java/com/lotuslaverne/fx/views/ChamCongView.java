package com.lotuslaverne.fx.views;

import com.lotuslaverne.fx.UiUtils;
import com.lotuslaverne.util.ConnectDB;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ChamCongView {

    private ObservableList<Object[]> items;
    private TableView<Object[]> table;
    private Label countLbl;

    public Node build() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color:#F0F2F5;");

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:#F0F2F5;-fx-border-color:transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox content = new VBox(20);
        content.setPadding(new Insets(28));
        content.setStyle("-fx-background-color:#F0F2F5;");

        // ── Header ──
        VBox hdr = new VBox(4);
        Label title = new Label("Chấm Công Nhân Viên");
        title.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:#1A1A2E;");
        countLbl = new Label("Đang tải...");
        countLbl.setStyle("-fx-font-size:13px;-fx-text-fill:#8C8C8C;");
        hdr.getChildren().addAll(title, countLbl);

        // ── Stats cards ──
        HBox stats = buildStatsRow();

        // ── Quick action: Check-in/out nhân viên ──
        VBox quickCard = buildQuickCheckCard();

        // ── Bảng lịch sử chấm công ──
        VBox tableCard = buildTableCard();

        content.getChildren().addAll(hdr, stats, quickCard, tableCard);
        scroll.setContent(content);
        root.getChildren().add(scroll);
        VBox.setVgrow(root, Priority.ALWAYS);
        return root;
    }

    // ── Stats row ──
    private HBox buildStatsRow() {
        HBox row = new HBox(16);
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        row.getChildren().addAll(
            statCard("🟢", "Đang Làm Việc", "0 nhân viên", "#F6FFED", "#52C41A"),
            statCard("🕐", "Đã Check-out", "0 nhân viên", "#FFF7E6", "#FAAD14"),
            statCard("❌", "Vắng Mặt",     "0 nhân viên", "#FFF1F0", "#FF4D4F"),
            statCard("📅", "Hôm Nay",       today,         "#F0F5FF", "#1890FF")
        );
        for (Node n : row.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);
        return row;
    }

    private VBox statCard(String icon, String label, String value, String bg, String fg) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color:" + bg + ";-fx-background-radius:10;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.05),4,0,0,1);");
        Label ico = new Label(icon + "  " + label);
        ico.setStyle("-fx-font-size:12px;-fx-text-fill:#595959;");
        Label val = new Label(value);
        val.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:" + fg + ";");
        card.getChildren().addAll(ico, val);
        return card;
    }

    // ── Quick check card ──
    private VBox buildQuickCheckCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:10;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        Label cardTitle = new Label("⚡  Ghi Nhanh Check-in / Check-out");
        cardTitle.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#1A1A2E;"
                + "-fx-border-color:transparent transparent #F0F2F5 transparent;"
                + "-fx-border-width:0 0 1 0;-fx-padding:0 0 10 0;");

        GridPane form = new GridPane();
        form.setHgap(16); form.setVgap(10);
        for (int i = 0; i < 3; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(33.33);
            form.getColumnConstraints().add(cc);
        }

        ComboBox<String> cbNV = new ComboBox<>();
        cbNV.setPromptText("Chọn nhân viên...");
        cbNV.setMaxWidth(Double.MAX_VALUE);
        cbNV.setStyle(comboStyle());
        loadNhanVienOptions(cbNV);

        ComboBox<String> cbCa = new ComboBox<>();
        cbCa.getItems().addAll("Ca Sáng (06:00-14:00)", "Ca Chiều (14:00-22:00)",
                "Ca Đêm (22:00-06:00)", "Hành Chính (08:00-17:00)");
        cbCa.setValue("Ca Sáng (06:00-14:00)");
        cbCa.setMaxWidth(Double.MAX_VALUE);
        cbCa.setStyle(comboStyle());

        TextField txtGhiChu = new TextField();
        txtGhiChu.setPromptText("Ghi chú...");
        txtGhiChu.setMaxWidth(Double.MAX_VALUE);
        txtGhiChu.setStyle("-fx-background-color:#FFFFFF;-fx-border-color:#D9D9D9;"
                + "-fx-border-radius:6;-fx-background-radius:6;-fx-border-width:1;-fx-padding:8 10;");

        form.add(lbl("Nhân Viên *"), 0, 0); form.add(cbNV,     0, 1);
        form.add(lbl("Ca Làm *"),   1, 0); form.add(cbCa,     1, 1);
        form.add(lbl("Ghi Chú"),    2, 0); form.add(txtGhiChu,2, 1);

        Label statusLbl = new Label();
        statusLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#8C8C8C;");

        Button btnCI = new Button("✅  CHECK-IN");
        btnCI.setStyle("-fx-background-color:#52C41A;-fx-text-fill:white;"
                + "-fx-background-radius:8;-fx-padding:10 24;-fx-font-weight:bold;-fx-cursor:hand;");

        Button btnCO = new Button("🚪  CHECK-OUT");
        btnCO.setStyle("-fx-background-color:#FF4D4F;-fx-text-fill:white;"
                + "-fx-background-radius:8;-fx-padding:10 24;-fx-font-weight:bold;-fx-cursor:hand;");

        String nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        btnCI.setOnAction(e -> {
            if (cbNV.getValue() == null) { statusLbl.setText("⚠ Chọn nhân viên trước!"); return; }
            // TODO: ghi vào DB ChamCong
            statusLbl.setText("✓ Check-in lúc " + nowStr + " — " + cbNV.getValue());
            statusLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#52C41A;-fx-font-weight:bold;");
            if (items != null) items.setAll(loadChamCong(null, null));
        });

        btnCO.setOnAction(e -> {
            if (cbNV.getValue() == null) { statusLbl.setText("⚠ Chọn nhân viên trước!"); return; }
            // TODO: cập nhật giờ out trong DB
            statusLbl.setText("✓ Check-out lúc " + nowStr + " — " + cbNV.getValue());
            statusLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#FF4D4F;-fx-font-weight:bold;");
            if (items != null) items.setAll(loadChamCong(null, null));
        });

        HBox btnRow = new HBox(12, btnCI, btnCO, statusLbl);
        btnRow.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().addAll(cardTitle, form, btnRow);
        return card;
    }

    // ── Bảng lịch sử ──
    private VBox buildTableCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:10;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        // Toolbar
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        TextField search = new TextField();
        search.setPromptText("🔍 Tìm theo tên nhân viên...");
        search.setPrefWidth(240);
        search.setStyle("-fx-background-color:#F5F5F5;-fx-border-color:#E8E8E8;"
                + "-fx-border-radius:8;-fx-background-radius:8;-fx-border-width:1;"
                + "-fx-padding:8 12;-fx-font-size:12px;");

        DatePicker dpFrom = new DatePicker(LocalDate.now().minusDays(6));
        dpFrom.setStyle(comboStyle()); dpFrom.setPrefWidth(140);
        DatePicker dpTo   = new DatePicker(LocalDate.now());
        dpTo.setStyle(comboStyle());   dpTo.setPrefWidth(140);

        Button btnFilter  = new Button("🔍 Lọc");
        btnFilter.setStyle("-fx-background-color:#1890FF;-fx-text-fill:white;"
                + "-fx-background-radius:8;-fx-padding:8 16;-fx-cursor:hand;-fx-font-weight:bold;");
        Button btnRefresh = new Button("↻ Làm Mới");
        btnRefresh.setStyle("-fx-background-color:#F5F5F5;-fx-text-fill:#595959;"
                + "-fx-background-radius:8;-fx-border-color:#D9D9D9;-fx-border-width:1;"
                + "-fx-border-radius:8;-fx-padding:8 14;-fx-cursor:hand;");

        Label titleTbl = new Label("📋  Lịch Sử Chấm Công");
        titleTbl.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#1A1A2E;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        toolbar.getChildren().addAll(titleTbl, sp, search,
                new Label("Từ:"), dpFrom, new Label("Đến:"), dpTo, btnFilter, btnRefresh);

        // Table
        items = FXCollections.observableArrayList(loadChamCong(null, null));
        table = buildTable();

        btnRefresh.setOnAction(e -> { items.setAll(loadChamCong(null, null)); table.setItems(items); });
        btnFilter.setOnAction(e  -> { items.setAll(loadChamCong(dpFrom.getValue(), dpTo.getValue())); table.setItems(items); });
        search.textProperty().addListener((obs, o, n) -> {
            if (n == null || n.trim().isEmpty()) { table.setItems(items); return; }
            String kw = n.toLowerCase();
            ObservableList<Object[]> f = FXCollections.observableArrayList();
            for (Object[] r : items)
                for (Object c : r) if (c!=null && c.toString().toLowerCase().contains(kw)) { f.add(r); break; }
            table.setItems(f);
        });

        if (countLbl != null) countLbl.setText("Tổng " + items.size() + " bản ghi chấm công");
        card.getChildren().addAll(toolbar, table);
        return card;
    }

    private TableView<Object[]> buildTable() {
        TableView<Object[]> tbl = new TableView<>();
        tbl.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tbl.setPrefHeight(380);
        tbl.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:10;"
                + "-fx-border-radius:10;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");
        tbl.setPlaceholder(new Label("Không có dữ liệu chấm công."));

        String[] heads = {"Mã CC", "Nhân Viên", "Ca Làm", "Ngày", "Giờ Vào", "Giờ Ra", "Tổng Giờ", "Trạng Thái"};
        for (int i = 0; i < heads.length; i++) {
            final int idx = i;
            TableColumn<Object[], String> col = new TableColumn<>(heads[i]);
            col.setCellValueFactory(p -> new SimpleStringProperty(
                    idx < p.getValue().length && p.getValue()[idx] != null ? p.getValue()[idx].toString() : "—"));
            if (heads[i].equals("Nhân Viên")) col.setCellFactory(tc -> new TableCell<>() {
                @Override protected void updateItem(String s, boolean empty) {
                    super.updateItem(s, empty);
                    if (empty || s == null) { setGraphic(null); return; }
                    HBox box = new HBox(8); box.setAlignment(Pos.CENTER_LEFT);
                    Label name = new Label(s); name.setStyle("-fx-font-weight:bold;-fx-text-fill:#1A1A2E;");
                    box.getChildren().addAll(UiUtils.makeAvatarCircle(s, 14), name);
                    setGraphic(box); setText(null);
                }
            });
            if (heads[i].equals("Trạng Thái")) col.setCellFactory(tc -> new TableCell<>() {
                @Override protected void updateItem(String s, boolean empty) {
                    super.updateItem(s, empty);
                    if (empty || s == null) { setGraphic(null); return; }
                    boolean dv = "Đang Làm".equals(s);
                    Label b = new Label(s);
                    b.setStyle("-fx-background-color:" + (dv?"#F6FFED":"#F5F5F5")
                            + ";-fx-text-fill:" + (dv?"#52C41A":"#8C8C8C")
                            + ";-fx-padding:2 8;-fx-background-radius:10;"
                            + "-fx-font-size:11px;-fx-font-weight:bold;");
                    setGraphic(b); setText(null);
                }
            });
            tbl.getColumns().add(col);
        }
        tbl.setItems(items);
        return tbl;
    }

    private List<Object[]> loadChamCong(LocalDate from, LocalDate to) {
        List<Object[]> result = new ArrayList<>();
        // TODO: query bảng ChamCong khi DB có
        // Demo data
        result.add(new Object[]{"CC001", "Nguyễn Văn Anh", "Hành Chính", "27/04/2026", "08:02", "17:05", "9.0h", "Đã Ra"});
        result.add(new Object[]{"CC002", "Trần Thị Bình",  "Ca Sáng",    "27/04/2026", "06:00", "—",     "—",    "Đang Làm"});
        result.add(new Object[]{"CC003", "Lê Văn Cường",   "Ca Chiều",   "27/04/2026", "14:00", "22:00", "8.0h", "Đã Ra"});
        result.add(new Object[]{"CC004", "Phạm Thị Dung",  "Ca Đêm",     "26/04/2026", "22:00", "06:00", "8.0h", "Đã Ra"});
        return result;
    }

    private void loadNhanVienOptions(ComboBox<String> cb) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con != null) {
            try (PreparedStatement pst = con.prepareStatement("SELECT tenNhanVien FROM NhanVien ORDER BY tenNhanVien");
                 ResultSet rs = pst.executeQuery()) {
                while (rs.next()) cb.getItems().add(rs.getString(1));
            } catch (Exception ignored) {}
        }
        if (cb.getItems().isEmpty()) {
            cb.getItems().addAll("Nguyễn Văn Anh", "Trần Thị Bình", "Lê Văn Cường", "Phạm Thị Dung");
        }
    }

    private Label lbl(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#595959;");
        return l;
    }

    private String comboStyle() {
        return "-fx-background-color:#FFFFFF;-fx-border-color:#D9D9D9;"
                + "-fx-border-radius:6;-fx-background-radius:6;-fx-border-width:1;";
    }
}
