package com.lotuslaverne.fx.views;

import com.lotuslaverne.dao.LoaiPhongDAO;
import com.lotuslaverne.dao.PhongDAO;
import com.lotuslaverne.entity.LoaiPhong;
import com.lotuslaverne.entity.Phong;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;
import java.util.stream.Collectors;

public class PhongView {

    private static final String[] LOAI_FILTER = {"Tất Cả", "Standard", "Deluxe", "Suite", "Family"};
    private static final String[] TRANG_THAI_FILTER = {"Tất Cả", "Trống", "Đang Thuê", "Cần Dọn", "Đang Dọn"};

    // Demo data used when DB is offline
    private static final Object[][] DEMO_ROOMS = {
        // maPhong, tenPhong, loai, loaiPhong, gia, trangThai, tenKhach, ngayTra, tang
        {"P101", "101", "Standard", "Phòng Đơn",  550_000, "Trống",    "",               "",           1},
        {"P102", "102", "Standard", "Phòng Đôi",  750_000, "Đang Thuê","Nguyễn Văn An",  "25/04/2026", 1},
        {"P103", "103", "Deluxe",   "Phòng Đôi",  950_000, "Cần Dọn",  "",               "",           1},
        {"P104", "104", "Suite",    "Phòng Suite",1_500_000,"Đang Dọn", "",               "",           1},
        {"P201", "201", "Standard", "Phòng Đơn",  550_000, "Trống",    "",               "",           2},
        {"P202", "202", "Deluxe",   "Phòng Đôi",  950_000, "Đang Thuê","Trần Thị Bình",  "26/04/2026", 2},
        {"P203", "203", "Family",   "Phòng Gia Đình",1_200_000,"Trống", "",               "",           2},
        {"P204", "204", "Suite",    "Phòng Suite",1_500_000,"Đang Thuê","Lê Hoàng Cường", "27/04/2026", 2},
        {"P301", "301", "Standard", "Phòng Đơn",  550_000, "Cần Dọn",  "",               "",           3},
        {"P302", "302", "Deluxe",   "Phòng Đôi",  950_000, "Trống",    "",               "",           3},
        {"P303", "303", "Family",   "Phòng Gia Đình",1_200_000,"Đang Thuê","Phạm Thị Dung","28/04/2026",3},
        {"P304", "304", "Suite",    "Phòng Suite",1_500_000,"Trống",    "",               "",           3},
    };

    public Node build() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #F0F2F5;");

        // Page header
        VBox header = new VBox(4);
        header.setPadding(new Insets(28, 28, 16, 28));
        Label title = new Label("Quản Lý Phòng");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Label sub = new Label("Tổng số " + DEMO_ROOMS.length + " phòng • Quản lý đặt phòng và trạng thái");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #8C8C8C;");
        header.getChildren().addAll(title, sub);

        // Tab pane
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle("-fx-background-color: #F0F2F5;");

        Tab tabPhong = new Tab("Phòng");
        tabPhong.setContent(buildPhongTab());

        Tab tabDanhSach = new Tab("Danh Sách");
        tabDanhSach.setContent(buildDanhSachTab());

        tabs.getTabs().addAll(tabPhong, tabDanhSach);
        VBox.setVgrow(tabs, Priority.ALWAYS);

        root.getChildren().addAll(header, tabs);
        return root;
    }

    // ---------------------------------------------------------------- TAB PHÒNG
    private Node buildPhongTab() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(16, 28, 28, 28));
        container.setStyle("-fx-background-color: #F0F2F5;");

        // Filter bar
        HBox filterBar = buildFilterBar(container);
        container.getChildren().add(filterBar);

        // Room grid
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        VBox roomsBox = buildRoomsByFloor(null, null);
        scroll.setContent(roomsBox);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        container.getChildren().add(scroll);

        return container;
    }

    private HBox buildFilterBar(VBox container) {
        HBox bar = new HBox(16);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12, 16, 12, 16));
        bar.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 1);");

        // Loại filter
        Label loaiLbl = new Label("Loại:");
        loaiLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #595959;");
        HBox loaiChips = new HBox(6);
        loaiChips.setAlignment(Pos.CENTER_LEFT);
        final String[] activeLoai = {"Tất Cả"};
        List<Button> loaiButtons = new ArrayList<>();
        for (String f : LOAI_FILTER) {
            Button btn = makeChip(f, f.equals("Tất Cả"));
            loaiButtons.add(btn);
            loaiChips.getChildren().add(btn);
        }
        for (Button btn : loaiButtons) {
            String label = btn.getText();
            btn.setOnAction(e -> {
                activeLoai[0] = label;
                loaiButtons.forEach(b -> applyChipStyle(b, b.getText().equals(label)));
            });
        }

        // Separator
        Region sep = new Region();
        sep.setPrefWidth(1);
        sep.setPrefHeight(24);
        sep.setStyle("-fx-background-color: #E8E8E8;");

        // Trạng thái filter
        Label ttLbl = new Label("Trạng thái:");
        ttLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #595959;");
        HBox ttChips = new HBox(6);
        ttChips.setAlignment(Pos.CENTER_LEFT);
        final String[] activeTT = {"Tất Cả"};
        List<Button> ttButtons = new ArrayList<>();
        for (String f : TRANG_THAI_FILTER) {
            Button btn = makeChip(f, f.equals("Tất Cả"));
            ttButtons.add(btn);
            ttChips.getChildren().add(btn);
        }
        for (Button btn : ttButtons) {
            String label = btn.getText();
            btn.setOnAction(e -> {
                activeTT[0] = label;
                ttButtons.forEach(b -> applyChipStyle(b, b.getText().equals(label)));
            });
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label countLbl = new Label(DEMO_ROOMS.length + " phòng");
        countLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #8C8C8C;");

        bar.getChildren().addAll(loaiLbl, loaiChips, sep, ttLbl, ttChips, spacer, countLbl);
        return bar;
    }

    private VBox buildRoomsByFloor(String filterLoai, String filterTT) {
        VBox box = new VBox(20);
        box.setPadding(new Insets(8, 0, 8, 0));
        box.setStyle("-fx-background-color: transparent;");

        // Load rooms — try DB, fall back to demo
        List<Object[]> rooms = loadRooms();

        // Group by floor
        Map<Integer, List<Object[]>> byFloor = new LinkedHashMap<>();
        for (Object[] r : rooms) {
            int floor = (int) r[8];
            byFloor.computeIfAbsent(floor, k -> new ArrayList<>()).add(r);
        }

        for (Map.Entry<Integer, List<Object[]>> entry : byFloor.entrySet()) {
            int floor = entry.getKey();
            List<Object[]> floorRooms = entry.getValue();

            // Floor header
            HBox floorHeader = new HBox(8);
            floorHeader.setAlignment(Pos.CENTER_LEFT);
            floorHeader.setPadding(new Insets(0, 0, 8, 0));
            Label floorLbl = new Label("Lầu " + floor);
            floorLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #C0392B;");
            Label floorCount = new Label("• " + floorRooms.size() + " phòng");
            floorCount.setStyle("-fx-font-size: 12px; -fx-text-fill: #8C8C8C;");
            floorHeader.getChildren().addAll(floorLbl, floorCount);

            // Room cards grid
            int cols = 4;
            GridPane grid = new GridPane();
            grid.setHgap(12);
            grid.setVgap(12);
            for (int i = 0; i < cols; i++) {
                ColumnConstraints cc = new ColumnConstraints();
                cc.setPercentWidth(25);
                cc.setHgrow(Priority.ALWAYS);
                grid.getColumnConstraints().add(cc);
            }
            for (int i = 0; i < floorRooms.size(); i++) {
                Node card = buildRoomCard(floorRooms.get(i));
                grid.add(card, i % cols, i / cols);
            }

            VBox section = new VBox(8);
            section.getChildren().addAll(floorHeader, grid);
            box.getChildren().add(section);
        }

        return box;
    }

    private Node buildRoomCard(Object[] r) {
        String tenPhong  = (String) r[1];
        String loai      = (String) r[2];
        String loaiPhong = (String) r[3];
        long gia         = ((Number) r[4]).longValue();
        String trangThai = (String) r[5];
        String tenKhach  = (String) r[6];
        String ngayTra   = (String) r[7];

        String borderColor  = statusBorderColor(trangThai);
        String badgeBg      = statusBadgeBg(trangThai);
        String badgeText    = statusBadgeText(trangThai);

        VBox card = new VBox(6);
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
                + "-fx-border-color: " + borderColor + " transparent transparent transparent;"
                + "-fx-border-width: 0 0 0 4; -fx-border-radius: 12;");

        // Header row: name + badge
        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.CENTER_LEFT);
        Label nameLbl = new Label("Phòng " + tenPhong);
        nameLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        HBox.setHgrow(nameLbl, Priority.ALWAYS);
        Label badge = new Label(trangThai);
        badge.setStyle("-fx-background-color: " + badgeBg + "; -fx-text-fill: " + badgeText + ";"
                + "-fx-padding: 2 8 2 8; -fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: bold;");
        headerRow.getChildren().addAll(nameLbl, badge);

        Label categoryLbl = new Label(loai);
        categoryLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #8C8C8C;");

        Label typeLbl = new Label(loaiPhong);
        typeLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #595959;");

        Label priceLbl = new Label(String.format("%,dđ / đêm", gia));
        priceLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #8C8C8C;");

        card.getChildren().addAll(headerRow, categoryLbl, typeLbl, priceLbl);

        // Guest row if occupied
        if (!tenKhach.isEmpty()) {
            HBox guestRow = new HBox(6);
            guestRow.setAlignment(Pos.CENTER_LEFT);
            Label icon = new Label("👤");
            icon.setStyle("-fx-font-size: 11px;");
            Label guestLbl = new Label(tenKhach + " · Ra: " + ngayTra);
            guestLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #595959;");
            guestLbl.setWrapText(true);
            guestRow.getChildren().addAll(icon, guestLbl);
            card.getChildren().add(guestRow);
        }

        // Action row
        HBox actionRow = new HBox(6);
        actionRow.setAlignment(Pos.CENTER_LEFT);
        Button actionBtn = new Button(actionLabel(trangThai));
        actionBtn.setStyle(actionStyle(trangThai));
        actionBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(actionBtn, Priority.ALWAYS);

        Button editBtn = new Button("✏");
        editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #8C8C8C;"
                + "-fx-border-color: #D9D9D9; -fx-border-width: 1; -fx-border-radius: 6;"
                + "-fx-background-radius: 6; -fx-padding: 5 8 5 8; -fx-cursor: hand;");

        actionRow.getChildren().addAll(actionBtn, editBtn);
        card.getChildren().add(actionRow);

        return card;
    }

    // ---------------------------------------------------------------- TAB DANH SÁCH
    private Node buildDanhSachTab() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(16, 24, 24, 24));
        container.setStyle("-fx-background-color: #F0F2F5;");

        // ── Toolbar: search + chips + count + thêm phòng ──
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(12, 16, 12, 16));
        toolbar.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 1);");

        // Search
        TextField search = new TextField();
        search.setPromptText("Tìm phòng, khách...");
        search.setPrefWidth(180);
        search.setPrefHeight(34);
        search.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #E8E8E8;"
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1;"
                + "-fx-padding: 4 10 4 10; -fx-font-size: 12px;");

        // Loại chips
        Label loaiLbl = new Label("Loại:");
        loaiLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #595959;");
        HBox loaiBox = new HBox(5);
        loaiBox.setAlignment(Pos.CENTER_LEFT);
        List<Button> loaiBtns = new ArrayList<>();
        for (String f : LOAI_FILTER) {
            Button b = makeChip(f, f.equals("Tất Cả"));
            loaiBtns.add(b);
            loaiBox.getChildren().add(b);
        }
        for (Button b : loaiBtns) {
            b.setOnAction(e -> loaiBtns.forEach(x -> applyChipStyle(x, x == b)));
        }

        Region sep1 = new Region();
        sep1.setPrefSize(1, 24);
        sep1.setStyle("-fx-background-color: #E8E8E8;");

        // Trạng thái chips
        Label ttLbl = new Label("Trạng thái:");
        ttLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #595959;");
        HBox ttBox = new HBox(5);
        ttBox.setAlignment(Pos.CENTER_LEFT);
        List<Button> ttBtns = new ArrayList<>();
        for (String f : TRANG_THAI_FILTER) {
            Button b = makeChip(f, f.equals("Tất Cả"));
            ttBtns.add(b);
            ttBox.getChildren().add(b);
        }
        for (Button b : ttBtns) {
            b.setOnAction(e -> ttBtns.forEach(x -> applyChipStyle(x, x == b)));
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        List<Object[]> allRooms = loadRooms();
        Label countLbl = new Label(allRooms.size() + " kết quả");
        countLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #8C8C8C;");

        Button addBtn = new Button("+ Thêm Phòng");
        addBtn.setStyle("-fx-background-color: #1890FF; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-padding: 8 16; -fx-font-size: 12px;"
                + "-fx-font-weight: bold; -fx-cursor: hand;");

        toolbar.getChildren().addAll(
                search, loaiLbl, loaiBox, sep1, ttLbl, ttBox, spacer, countLbl, addBtn);

        // ── Table ──
        TableView<Object[]> table = buildPhongTable(allRooms);
        table.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 1);");
        VBox.setVgrow(table, Priority.ALWAYS);

        container.getChildren().addAll(toolbar, table);
        return container;
    }

    @SuppressWarnings("unchecked")
    private TableView<Object[]> buildPhongTable(List<Object[]> rooms) {
        TableView<Object[]> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(48);
        table.setRowFactory(tv -> {
            javafx.scene.control.TableRow<Object[]> row = new javafx.scene.control.TableRow<>();
            row.setStyle("-fx-border-color: transparent transparent #F5F5F5 transparent;"
                    + "-fx-border-width: 0 0 1 0;");
            return row;
        });

        // Phòng (bold number)
        TableColumn<Object[], String> colPhong = new TableColumn<>("Phòng");
        colPhong.setPrefWidth(70);
        colPhong.setCellValueFactory(p -> new SimpleStringProperty((String) p.getValue()[1]));
        colPhong.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText(null); return; }
                setText(s);
                setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1A1A2E;");
            }
        });

        // Lầu
        TableColumn<Object[], String> colLau = new TableColumn<>("Lầu");
        colLau.setPrefWidth(65);
        colLau.setCellValueFactory(p ->
                new SimpleStringProperty("Lầu " + p.getValue()[8]));

        // Loại Phòng
        TableColumn<Object[], String> colLoai = new TableColumn<>("Loại Phòng");
        colLoai.setCellValueFactory(p -> new SimpleStringProperty((String) p.getValue()[2]));

        // Hạng Phòng
        TableColumn<Object[], String> colHang = new TableColumn<>("Hạng Phòng");
        colHang.setCellValueFactory(p -> new SimpleStringProperty((String) p.getValue()[3]));

        // Trạng Thái (badge)
        TableColumn<Object[], String> colTT = new TableColumn<>("Trạng Thái");
        colTT.setPrefWidth(110);
        colTT.setCellValueFactory(p -> new SimpleStringProperty((String) p.getValue()[5]));
        colTT.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty); setText(null);
                if (empty || item == null) { setGraphic(null); return; }
                Label badge = new Label(item);
                badge.setStyle("-fx-background-color: " + statusBadgeBg(item) + ";"
                        + "-fx-text-fill: " + statusBadgeText(item) + ";"
                        + "-fx-padding: 3 10 3 10; -fx-background-radius: 12;"
                        + "-fx-font-size: 11px; -fx-font-weight: bold;");
                setGraphic(badge);
            }
        });

        // Giá/Đêm
        TableColumn<Object[], String> colGia = new TableColumn<>("Giá/Đêm");
        colGia.setPrefWidth(100);
        colGia.setCellValueFactory(p -> new SimpleStringProperty(
                String.format("%,dđ", ((Number) p.getValue()[4]).longValue())));

        // Khách Hiện Tại
        TableColumn<Object[], String> colKhach = new TableColumn<>("Khách Hiện Tại");
        colKhach.setCellValueFactory(p -> {
            String khach = (String) p.getValue()[6];
            return new SimpleStringProperty(khach == null || khach.isEmpty() ? "—" : khach);
        });

        // Thao Tác (Sửa / Xóa)
        TableColumn<Object[], Void> colAction = new TableColumn<>("Thao Tác");
        colAction.setPrefWidth(120);
        colAction.setCellFactory(tc -> new TableCell<>() {
            private final Button btnSua = new Button("✏ Sửa");
            private final Button btnXoa = new Button("🗑 Xóa");
            {
                btnSua.setStyle("-fx-background-color: #E6F4FF; -fx-text-fill: #1890FF;"
                        + "-fx-background-radius: 6; -fx-padding: 4 10; -fx-font-size: 11px;"
                        + "-fx-cursor: hand;");
                btnXoa.setStyle("-fx-background-color: #FFF1F0; -fx-text-fill: #FF4D4F;"
                        + "-fx-background-radius: 6; -fx-padding: 4 10; -fx-font-size: 11px;"
                        + "-fx-cursor: hand;");
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                HBox box = new HBox(6, btnSua, btnXoa);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
            }
        });

        table.getColumns().addAll(
                colPhong, colLau, colLoai, colHang, colTT, colGia, colKhach, colAction);
        table.setItems(FXCollections.observableArrayList(rooms));
        table.setPlaceholder(new Label("Không có dữ liệu phòng."));
        return table;
    }

    // ---------------------------------------------------------------- DATA
    private List<Object[]> loadRooms() {
        List<Object[]> result = new ArrayList<>();
        try {
            PhongDAO phongDAO = new PhongDAO();
            LoaiPhongDAO loaiDAO = new LoaiPhongDAO();
            List<Phong> phongs = phongDAO.getAll();
            List<LoaiPhong> loais = loaiDAO.getAll();
            Map<String, String> loaiMap = new HashMap<>();
            for (LoaiPhong lp : loais) loaiMap.put(lp.getMaLoaiPhong(), lp.getTenLoaiPhong());

            if (!phongs.isEmpty()) {
                for (int i = 0; i < phongs.size(); i++) {
                    Phong p = phongs.get(i);
                    String loaiName = loaiMap.getOrDefault(p.getMaLoaiPhong(), "Standard");
                    int floor = (i / 4) + 1;
                    result.add(new Object[]{
                        p.getMaPhong(), p.getTenPhong(), loaiName, "Phòng Đôi",
                        750_000L, p.getTrangThai(), "", "", floor
                    });
                }
                return result;
            }
        } catch (Exception ignored) {}

        // Fallback demo
        for (Object[] r : DEMO_ROOMS) result.add(r);
        return result;
    }

    // ---------------------------------------------------------------- HELPERS
    @SuppressWarnings("unchecked")
    private <S, T> TableColumn<S, T> col(String title, int idx, boolean isPrice) {
        TableColumn<S, T> c = new TableColumn<>(title);
        c.setStyle("-fx-alignment: CENTER-LEFT;");
        return c;
    }

    private Button makeChip(String text, boolean active) {
        Button btn = new Button(text);
        applyChipStyle(btn, active);
        return btn;
    }

    private void applyChipStyle(Button btn, boolean active) {
        if (active) {
            btn.setStyle("-fx-background-color: #1890FF; -fx-text-fill: white;"
                    + "-fx-background-radius: 20; -fx-border-radius: 20;"
                    + "-fx-border-color: #1890FF; -fx-border-width: 1;"
                    + "-fx-padding: 4 14 4 14; -fx-font-size: 12px; -fx-cursor: hand;");
        } else {
            btn.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #333333;"
                    + "-fx-background-radius: 20; -fx-border-radius: 20;"
                    + "-fx-border-color: #D9D9D9; -fx-border-width: 1;"
                    + "-fx-padding: 4 14 4 14; -fx-font-size: 12px; -fx-cursor: hand;");
        }
    }

    private String statusBorderColor(String tt) {
        return switch (tt) {
            case "Đang Thuê" -> "#FF4D4F";
            case "Cần Dọn"   -> "#FAAD14";
            case "Đang Dọn"  -> "#1890FF";
            default          -> "#52C41A";
        };
    }

    private String statusBadgeBg(String tt) {
        return switch (tt) {
            case "Đang Thuê" -> "#FFF1F0";
            case "Cần Dọn"   -> "#FFFBE6";
            case "Đang Dọn"  -> "#E6F4FF";
            default          -> "#F6FFED";
        };
    }

    private String statusBadgeText(String tt) {
        return switch (tt) {
            case "Đang Thuê" -> "#FF4D4F";
            case "Cần Dọn"   -> "#FAAD14";
            case "Đang Dọn"  -> "#1890FF";
            default          -> "#52C41A";
        };
    }

    private String actionLabel(String tt) {
        return switch (tt) {
            case "Đang Thuê" -> "Trả Phòng";
            case "Cần Dọn"   -> "Giao Dọn";
            case "Đang Dọn"  -> "Hoàn Thành";
            default          -> "Nhận Phòng";
        };
    }

    private String actionStyle(String tt) {
        String bg = switch (tt) {
            case "Đang Thuê" -> "#FF4D4F";
            case "Cần Dọn"   -> "#FAAD14";
            case "Đang Dọn"  -> "#1890FF";
            default          -> "#52C41A";
        };
        return "-fx-background-color: " + bg + "; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-border-radius: 8;"
                + "-fx-font-size: 12px; -fx-font-weight: bold;"
                + "-fx-padding: 6 10 6 10; -fx-cursor: hand;";
    }
}
