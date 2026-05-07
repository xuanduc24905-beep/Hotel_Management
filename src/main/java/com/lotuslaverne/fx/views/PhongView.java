package com.lotuslaverne.fx.views;

import com.lotuslaverne.dao.KhachHangDAO;
import com.lotuslaverne.dao.LoaiPhongDAO;
import com.lotuslaverne.dao.PhieuDatPhongDAO;
import com.lotuslaverne.dao.PhongDAO;
import com.lotuslaverne.entity.KhachHang;
import com.lotuslaverne.entity.LoaiPhong;
import com.lotuslaverne.entity.Phong;
import com.lotuslaverne.fx.MainLayout;
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

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class PhongView {

    /** Reference to MainLayout để điều hướng màn hình khi click card phòng. */
    private final MainLayout mainLayout;

    /** Constructor mặc định (dùng khi không cần điều hướng cross-screen). */
    public PhongView() { this.mainLayout = null; }

    /** Constructor với MainLayout reference — dùng bởi MainLayout.navigateToView(). */
    public PhongView(MainLayout mainLayout) { this.mainLayout = mainLayout; }

    private static final String[] LOAI_FILTER_FALLBACK = {"Standard", "Deluxe", "Suite", "Family"};
    private static final String[] TRANG_THAI_FILTER = {"Tất Cả", "Trống", "Chờ Thanh Toán", "Chờ Check-in", "Đang Thuê", "Cần Dọn", "Đang Dọn", "Bảo Trì"};
    private static final String[] TIEN_NGHI_FILTER  = {"Tất Cả", "WiFi", "TV", "Điều Hòa", "Bồn Tắm", "Ban Công", "Mini Bar"};

    /** Lấy danh sách loại phòng động từ DB (fallback về list cứng nếu DB offline). */
    private List<String> loadLoaiFilters() {
        List<String> list = new ArrayList<>();
        list.add("Tất Cả");
        try {
            for (LoaiPhong lp : new LoaiPhongDAO().getAll()) {
                String ten = lp.getTenLoaiPhong();
                if (ten != null && !list.contains(ten)) list.add(ten);
            }
        } catch (Exception ignored) {}
        if (list.size() == 1) {
            for (String s : LOAI_FILTER_FALLBACK) list.add(s);
        }
        return list;
    }

    // col[9] = tienNghi (String)
    private static final Object[][] DEMO_ROOMS = {
        {"P101","101","Standard","Phòng Đơn",    550_000,"Trống",    "",               "",          1,"WiFi,TV"},
        {"P102","102","Standard","Phòng Đôi",    750_000,"Đang Thuê","Nguyễn Văn An",  "25/04/2026",1,"WiFi,TV,Điều Hòa"},
        {"P103","103","Deluxe",  "Phòng Đôi",    950_000,"Cần Dọn",  "",               "",          1,"WiFi,TV,Điều Hòa,Bồn Tắm"},
        {"P104","104","Suite",   "Phòng Suite",1_500_000,"Đang Dọn", "",               "",          1,"WiFi,TV,Điều Hòa,Bồn Tắm,Ban Công,Mini Bar"},
        {"P201","201","Standard","Phòng Đơn",    550_000,"Trống",    "",               "",          2,"WiFi"},
        {"P202","202","Deluxe",  "Phòng Đôi",    950_000,"Đang Thuê","Trần Thị Bình",  "26/04/2026",2,"WiFi,TV,Điều Hòa"},
        {"P203","203","Family",  "Phòng Gia Đình",1_200_000,"Trống", "",               "",          2,"WiFi,TV,Bồn Tắm"},
        {"P204","204","Suite",   "Phòng Suite",1_500_000,"Đang Thuê","Lê Hoàng Cường", "27/04/2026",2,"WiFi,TV,Điều Hòa,Mini Bar"},
        {"P301","301","Standard","Phòng Đơn",    550_000,"Cần Dọn",  "",               "",          3,"WiFi"},
        {"P302","302","Deluxe",  "Phòng Đôi",    950_000,"Trống",    "",               "",          3,"WiFi,TV,Điều Hòa,Ban Công"},
        {"P303","303","Family",  "Phòng Gia Đình",1_200_000,"Đang Thuê","Phạm Thị Dung","28/04/2026",3,"WiFi,TV,Bồn Tắm"},
        {"P304","304","Suite",   "Phòng Suite",1_500_000,"Trống",    "",               "",          3,"WiFi,TV,Điều Hòa,Bồn Tắm,Ban Công,Mini Bar"},
    };

    // Instance fields for refresh
    private ScrollPane roomsScroll;
    private ObservableList<Object[]> tableItems;
    private TableView<Object[]> tableView;
    private Label headerSubtitle;

    // Filter state — tab Phòng (card grid) — multi-select, empty = all
    private final java.util.Set<String> selLoai      = new java.util.LinkedHashSet<>();
    private final java.util.Set<String> selTrangThai = new java.util.LinkedHashSet<>();
    private final java.util.Set<String> selTienNghi  = new java.util.LinkedHashSet<>();

    // Filter state — tab Danh Sách (table) — multi-select
    private final java.util.Set<String> selTableLoai      = new java.util.LinkedHashSet<>();
    private final java.util.Set<String> selTableTrangThai = new java.util.LinkedHashSet<>();
    private String currentSearchKeyword = "";
    private Label  tableCountLbl;

    public Node build() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #F0F2F5;");

        VBox header = new VBox(4);
        header.setPadding(new Insets(28, 28, 16, 28));
        Label title = new Label("Quản Lý Phòng");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        title.setMinHeight(Region.USE_PREF_SIZE);

        headerSubtitle = new Label();
        headerSubtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #8C8C8C;");
        headerSubtitle.setWrapText(true);
        updateHeaderSubtitle();
        header.getChildren().addAll(title, headerSubtitle);

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle("-fx-background-color: #F0F2F5;");

        Tab tabPhong     = new Tab("Phòng");     tabPhong.setContent(buildPhongTab());
        Tab tabDanhSach  = new Tab("Danh Sách"); tabDanhSach.setContent(buildDanhSachTab());
        tabs.getTabs().addAll(tabPhong, tabDanhSach);
        VBox.setVgrow(tabs, Priority.ALWAYS);

        root.getChildren().addAll(header, tabs);
        return root;
    }

    // ─────────────────────────────────────────── TAB PHÒNG (card grid)
    private Node buildPhongTab() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(16, 28, 28, 28));
        container.setStyle("-fx-background-color: #F0F2F5;");

        // Dùng buildFilterBarFull (hàng Loại + Trạng thái + Tiện nghi)
        container.getChildren().add(buildFilterBarFull(container));

        roomsScroll = new ScrollPane();
        roomsScroll.setFitToWidth(true);
        roomsScroll.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        roomsScroll.setContent(buildRoomsByFloor());
        VBox.setVgrow(roomsScroll, Priority.ALWAYS);
        container.getChildren().add(roomsScroll);

        return container;
    }

    private HBox buildFilterBar(VBox container) {
        HBox bar = new HBox(16);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12, 16, 12, 16));
        bar.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        Label loaiLbl = chipLabel("Loại:");
        HBox loaiChips = new HBox(6);
        loaiChips.setAlignment(Pos.CENTER_LEFT);
        List<Button> loaiBtns = new ArrayList<>();
        for (String f : loadLoaiFilters()) {
            Button b = makeChip(f, f.equals("Tất Cả"));
            loaiBtns.add(b);
            loaiChips.getChildren().add(b);
        }
        for (Button b : loaiBtns) {
            String[] currentLoaiFilter = {""};
            b.setOnAction(e -> {
                loaiBtns.forEach(x -> applyChipStyle(x, x == b));
                currentLoaiFilter[0] = b.getText();
                refreshRoomCards();
            });
        }

        Region sep = new Region(); sep.setPrefWidth(1); sep.setPrefHeight(24);
        sep.setStyle("-fx-background-color: #E8E8E8;");

        Label ttLbl = chipLabel("Trạng thái:");
        HBox ttChips = new HBox(6);
        ttChips.setAlignment(Pos.CENTER_LEFT);
        List<Button> ttBtns = new ArrayList<>();
        for (String f : TRANG_THAI_FILTER) {
            Button b = makeChip(f, f.equals("Tất Cả"));
            ttBtns.add(b);
            ttChips.getChildren().add(b);
        }
        for (Button b : ttBtns) {
            String[] currentTrangThaiFilter = {""};
            b.setOnAction(e -> {
                ttBtns.forEach(x -> applyChipStyle(x, x == b));
                currentTrangThaiFilter[0] = b.getText();
                refreshRoomCards();
            });
        }

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnRefresh = new Button("↻ Làm Mới");
        btnRefresh.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #595959;"
                + "-fx-background-radius: 8; -fx-border-color: #D9D9D9; -fx-border-width: 1;"
                + "-fx-border-radius: 8; -fx-padding: 5 14; -fx-cursor: hand; -fx-font-size: 12px;");
        btnRefresh.setOnAction(e -> {
            refreshRoomCards();
            refreshTable();
            com.lotuslaverne.fx.UiUtils.flashButton(btnRefresh, "✓ Đã làm mới");
        });

        // ── Tiện Nghi filter (hàng 2) ──
        HBox tnBar = new HBox(16);
        tnBar.setAlignment(Pos.CENTER_LEFT);
        tnBar.setPadding(new Insets(8, 16, 8, 16));
        tnBar.setStyle("-fx-background-color: #FAFAFA; -fx-background-radius: 0 0 10 10;"
                + "-fx-border-color: #F0F0F0; -fx-border-width: 1 0 0 0;");
        Label tnLbl = chipLabel("Tiện nghi:");
        HBox tnChips = new HBox(6);
        tnChips.setAlignment(Pos.CENTER_LEFT);
        List<Button> tnBtns = new ArrayList<>();
        for (String f : TIEN_NGHI_FILTER) {
            Button b = makeChip(f, f.equals("Tất Cả"));
            tnBtns.add(b);
            tnChips.getChildren().add(b);
        }
        for (Button b : tnBtns) {
            String[] currentTienNghiFilter = {""};
            b.setOnAction(e -> {
                tnBtns.forEach(x -> applyChipStyle(x, x == b));
                currentTienNghiFilter[0] = b.getText();
                refreshRoomCards();
            });
        }
        tnBar.getChildren().addAll(tnLbl, tnChips);

        VBox fullBar = new VBox(0, bar, tnBar);
        fullBar.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        // Thay thế bar bằng fullBar trong container
        bar.setStyle("-fx-background-color: #FFFFFF;");
        return (HBox) fullBar.getChildren().get(0); // trả về để tương thích; container dùng fullBar
    }

    // Ghi đè buildFilterBar để trả về VBox bọc cả 3 hàng — multi-select
    private Node buildFilterBarFull(VBox container) {
        // ── Hàng 1: Xóa bộ lọc + Làm mới
        Button btnClear = new Button("✕ Xóa Bộ Lọc");
        btnClear.setStyle("-fx-background-color:#FFF1F0;-fx-text-fill:#FF4D4F;"
                +"-fx-background-radius:8;-fx-border-color:#FF4D4F;-fx-border-width:1;"
                +"-fx-border-radius:8;-fx-padding:5 14;-fx-cursor:hand;-fx-font-size:12px;");
        Button btnRefresh2 = new Button("↻ Làm Mới");
        btnRefresh2.setStyle("-fx-background-color:#F5F5F5;-fx-text-fill:#595959;"
                +"-fx-background-radius:8;-fx-border-color:#D9D9D9;-fx-border-width:1;"
                +"-fx-border-radius:8;-fx-padding:5 14;-fx-cursor:hand;-fx-font-size:12px;");
        Label activeFilterLbl = new Label("Tất cả phòng");
        activeFilterLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#8C8C8C;");
        Region spacerTop = new Region(); HBox.setHgrow(spacerTop, Priority.ALWAYS);
        HBox topRow = new HBox(10, activeFilterLbl, spacerTop, btnClear, btnRefresh2);
        topRow.setAlignment(Pos.CENTER_LEFT);
        topRow.setPadding(new Insets(10, 16, 6, 16));
        topRow.setStyle("-fx-background-color: #FFFFFF;");

        // ── Hàng 2: FlowPane Loại (multi-select)
        javafx.scene.layout.FlowPane loaiRow = new javafx.scene.layout.FlowPane(8, 6);
        loaiRow.setAlignment(Pos.CENTER_LEFT);
        loaiRow.setPadding(new Insets(0, 16, 6, 16));
        loaiRow.setStyle("-fx-background-color: #FFFFFF;");

        Label loaiLbl2 = chipLabel("Loại:");
        List<Button> loaiBtns2 = new ArrayList<>();
        for (String f : loadLoaiFilters()) {
            if ("Tất Cả".equals(f)) continue; // bỏ "Tất Cả" — thay bằng nút Xóa
            Button b = makeChip(f, false); loaiBtns2.add(b);
            b.setOnAction(e -> {
                boolean nowSelected = !selLoai.contains(f);
                if (nowSelected) selLoai.add(f); else selLoai.remove(f);
                applyChipStyle(b, nowSelected);
                updateActiveLabel(activeFilterLbl);
                refreshRoomCards();
            });
        }
        loaiRow.getChildren().add(loaiLbl2);
        loaiRow.getChildren().addAll(loaiBtns2);

        Region sep2 = new Region(); sep2.setPrefSize(1, 24); sep2.setStyle("-fx-background-color:#E8E8E8;");

        // ── Trạng thái (multi-select, same row)
        Label ttLbl2 = chipLabel("Trạng thái:");
        List<Button> ttBtns2 = new ArrayList<>();
        for (String f : TRANG_THAI_FILTER) {
            if ("Tất Cả".equals(f)) continue;
            Button b = makeChip(f, false); ttBtns2.add(b);
            b.setOnAction(e -> {
                boolean nowSelected = !selTrangThai.contains(f);
                if (nowSelected) selTrangThai.add(f); else selTrangThai.remove(f);
                applyChipStyle(b, nowSelected);
                updateActiveLabel(activeFilterLbl);
                refreshRoomCards();
            });
        }
        loaiRow.getChildren().add(sep2);
        loaiRow.getChildren().add(ttLbl2);
        loaiRow.getChildren().addAll(ttBtns2);

        // ── Hàng 3: Tiện nghi (multi-select)
        javafx.scene.layout.FlowPane tnRow = new javafx.scene.layout.FlowPane(8, 6);
        tnRow.setAlignment(Pos.CENTER_LEFT);
        tnRow.setPadding(new Insets(4, 16, 10, 16));
        tnRow.setStyle("-fx-background-color:#FAFAFA;-fx-border-color:#F0F0F0;-fx-border-width:1 0 0 0;");
        Label tnLbl2 = chipLabel("Tiện nghi:");
        List<Button> tnBtns2 = new ArrayList<>();
        for (String f : TIEN_NGHI_FILTER) {
            if ("Tất Cả".equals(f)) continue;
            Button b = makeChip(f, false); tnBtns2.add(b);
            b.setOnAction(e -> {
                boolean nowSelected = !selTienNghi.contains(f);
                if (nowSelected) selTienNghi.add(f); else selTienNghi.remove(f);
                applyChipStyle(b, nowSelected);
                updateActiveLabel(activeFilterLbl);
                refreshRoomCards();
            });
        }
        tnRow.getChildren().add(tnLbl2);
        tnRow.getChildren().addAll(tnBtns2);

        // ── Clear action
        List<Button> allChips = new ArrayList<>();
        allChips.addAll(loaiBtns2); allChips.addAll(ttBtns2); allChips.addAll(tnBtns2);
        btnClear.setOnAction(e -> {
            selLoai.clear(); selTrangThai.clear(); selTienNghi.clear();
            allChips.forEach(b -> applyChipStyle(b, false));
            updateActiveLabel(activeFilterLbl);
            refreshRoomCards();
        });
        btnRefresh2.setOnAction(e -> { refreshRoomCards(); refreshTable(); com.lotuslaverne.fx.UiUtils.flashButton(btnRefresh2,"✓ Đã làm mới"); });

        VBox fullBar2 = new VBox(0, topRow, loaiRow, tnRow);
        fullBar2.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:10;"
                +"-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");
        return fullBar2;
    }

    private void updateActiveLabel(Label lbl) {
        int total = selLoai.size() + selTrangThai.size() + selTienNghi.size();
        if (total == 0) {
            lbl.setText("Tất cả phòng");
            lbl.setStyle("-fx-font-size:12px;-fx-text-fill:#8C8C8C;");
        } else {
            lbl.setText(total + " bộ lọc đang áp dụng");
            lbl.setStyle("-fx-font-size:12px;-fx-text-fill:#1890FF;-fx-font-weight:bold;");
        }
    }


    @SuppressWarnings("unused")
    private HBox _unused() {
        return null;
    }

    private void refreshRoomCards() {
        if (roomsScroll != null) roomsScroll.setContent(buildRoomsByFloor());
        updateHeaderSubtitle();
    }

    /** Cập nhật subtitle với count thật theo trạng thái. */
    private void updateHeaderSubtitle() {
        if (headerSubtitle == null) return;
        List<Object[]> rooms = loadRooms();
        long trong      = rooms.stream().filter(r -> "Trống".equals(r[5])).count();
        long choCheckin = rooms.stream().filter(r -> "Chờ Check-in".equals(r[5])).count();
        long thue       = rooms.stream().filter(r -> "Đang Thuê".equals(r[5])).count();
        long canDon     = rooms.stream().filter(r -> "Cần Dọn".equals(r[5])).count();
        long baoTri     = rooms.stream().filter(r -> "Bảo Trì".equals(r[5])).count();
        headerSubtitle.setText(String.format(
                "Tổng số %d phòng  •  Trống: %d  •  Chờ check-in: %d  •  Đang thuê: %d  •  Cần dọn: %d  •  Bảo trì: %d",
                rooms.size(), trong, choCheckin, thue, canDon, baoTri));
    }

    private VBox buildRoomsByFloor() {
        VBox box = new VBox(20);
        box.setPadding(new Insets(8, 0, 8, 0));
        box.setStyle("-fx-background-color: transparent;");

        List<Object[]> rooms = loadRooms().stream()
                .filter(r -> selLoai.isEmpty() || selLoai.contains(r[2]))
                .filter(r -> selTrangThai.isEmpty() || selTrangThai.contains(r[5]))
                .filter(r -> {
                    if (selTienNghi.isEmpty()) return true;
                    Object tn = r.length > 9 ? r[9] : null;
                    if (tn == null) return false;
                    String tnStr = tn.toString();
                    return selTienNghi.stream().anyMatch(tnStr::contains);
                })
                .collect(Collectors.toList());
        Map<Integer, List<Object[]>> byFloor = new LinkedHashMap<>();
        for (Object[] r : rooms) {
            int floor = (int) r[8];
            byFloor.computeIfAbsent(floor, k -> new ArrayList<>()).add(r);
        }

        if (rooms.isEmpty()) {
            Label empty = new Label("Không có phòng nào khớp bộ lọc.");
            empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #8C8C8C; -fx-padding: 20;");
            box.getChildren().add(empty);
            return box;
        }

        for (Map.Entry<Integer, List<Object[]>> entry : byFloor.entrySet()) {
            int floor = entry.getKey();
            List<Object[]> floorRooms = entry.getValue();

            HBox floorHeader = new HBox(8);
            floorHeader.setAlignment(Pos.CENTER_LEFT);
            floorHeader.setPadding(new Insets(0, 0, 8, 0));
            Label floorLbl   = new Label("Lầu " + floor);
            floorLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #C0392B;");
            Label floorCount = new Label("• " + floorRooms.size() + " phòng");
            floorCount.setStyle("-fx-font-size: 12px; -fx-text-fill: #8C8C8C;");
            floorHeader.getChildren().addAll(floorLbl, floorCount);

            int cols = 4;
            GridPane grid = new GridPane();
            grid.setHgap(12); grid.setVgap(12);
            for (int i = 0; i < cols; i++) {
                ColumnConstraints cc = new ColumnConstraints();
                cc.setPercentWidth(25);
                cc.setHgrow(Priority.ALWAYS);
                grid.getColumnConstraints().add(cc);
            }
            for (int i = 0; i < floorRooms.size(); i++) {
                grid.add(buildRoomCard(floorRooms.get(i)), i % cols, i / cols);
            }

            VBox section = new VBox(8, floorHeader, grid);
            box.getChildren().add(section);
        }
        return box;
    }

    private Node buildRoomCard(Object[] r) {
        String maPhong   = (String) r[0];
        String tenPhong  = (String) r[1];
        String loai      = (String) r[2];
        String loaiPhong = (String) r[3];
        long gia         = ((Number) r[4]).longValue();
        String trangThai = (String) r[5];
        String tenKhach  = (String) r[6];
        String ngayTra   = (String) r[7];

        VBox card = new VBox(6);
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.08),8,0,0,2);"
                + "-fx-border-color: " + statusBorderColor(trangThai) + " transparent transparent transparent;"
                + "-fx-border-width: 0 0 0 4; -fx-border-radius: 12;");

        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.CENTER_LEFT);
        String displayName = tenPhong.startsWith("Phòng ") ? tenPhong : "Phòng " + tenPhong;
        Label nameLbl = new Label(displayName);
        nameLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        HBox.setHgrow(nameLbl, Priority.ALWAYS);
        Label badge = new Label(trangThai);
        badge.setStyle("-fx-background-color: " + statusBadgeBg(trangThai)
                + "; -fx-text-fill: " + statusBadgeText(trangThai)
                + "; -fx-padding: 2 8 2 8; -fx-background-radius: 10;"
                + " -fx-font-size: 10px; -fx-font-weight: bold;");
        headerRow.getChildren().addAll(nameLbl, badge);

        Label categoryLbl = new Label(loai);
        categoryLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #8C8C8C;");
        Label typeLbl = new Label(loaiPhong);
        typeLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #595959;");
        Label priceLbl = new Label(String.format("%,dđ / đêm", gia));
        priceLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #8C8C8C;");

        // Tags tiện nghi (tối đa 3 + "+N")
        String tienNghi = r.length > 9 && r[9] != null ? r[9].toString() : "";
        if (!tienNghi.isEmpty()) {
            HBox tagsRow = new HBox(4); tagsRow.setAlignment(Pos.CENTER_LEFT);
            String[] tags = tienNghi.split(",");
            int show = Math.min(tags.length, 3);
            for (int i = 0; i < show; i++) {
                Label tag = new Label(tags[i].trim());
                tag.setStyle("-fx-background-color:#F0F8FF;-fx-text-fill:#1890FF;"
                        +"-fx-padding:1 6;-fx-background-radius:8;-fx-font-size:10px;");
                tagsRow.getChildren().add(tag);
            }
            if (tags.length > 3) {
                Label more = new Label("+" + (tags.length-3));
                more.setStyle("-fx-background-color:#F5F5F5;-fx-text-fill:#8C8C8C;"
                        +"-fx-padding:1 6;-fx-background-radius:8;-fx-font-size:10px;");
                tagsRow.getChildren().add(more);
            }
            card.getChildren().add(tagsRow);
        }

        card.getChildren().addAll(headerRow, categoryLbl, typeLbl, priceLbl);

        if (!tenKhach.isEmpty()) {
            HBox guestRow = new HBox(6);
            guestRow.setAlignment(Pos.CENTER_LEFT);
            Label icon = new Label("👤"); icon.setStyle("-fx-font-size: 11px;");
            Label guestLbl = new Label(tenKhach + " · Ra: " + ngayTra);
            guestLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #595959;");
            guestLbl.setWrapText(true);
            guestRow.getChildren().addAll(icon, guestLbl);
            card.getChildren().add(guestRow);
        }

        // ── Action buttons with REAL handlers ──
        HBox actionRow = new HBox(6);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        Button actionBtn = new Button(actionLabel(trangThai));
        actionBtn.setStyle(actionStyle(trangThai));
        actionBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(actionBtn, Priority.ALWAYS);
        actionBtn.setOnAction(e -> handleCardAction(maPhong, trangThai));

        Button editBtn = new Button("✏");
        editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #8C8C8C;"
                + "-fx-border-color: #D9D9D9; -fx-border-width: 1; -fx-border-radius: 6;"
                + "-fx-background-radius: 6; -fx-padding: 5 8; -fx-cursor: hand;");
        editBtn.setOnAction(e -> openEditDialog(r));

        actionRow.getChildren().addAll(actionBtn, editBtn);
        card.getChildren().add(actionRow);
        return card;
    }

    /** Xử lý nút hành động chính trên card phòng */
    private void handleCardAction(String maPhong, String trangThai) {
        PhongDAO phongDAO = new PhongDAO();
        switch (trangThai) {
            case "Trống" -> {
                if (mainLayout != null) {
                    openDatPhongPopup(maPhong);
                } else {
                    openNhanPhongDialog(maPhong);
                }
            }
            case "Chờ Thanh Toán" -> showXacNhanThanhToanDialog(maPhong);
            case "Chờ Check-in" -> showCheckInConfirmDialog(maPhong);
            case "Đang Thuê" -> {
                if (mainLayout != null) {
                    openCheckoutPopup(maPhong);
                } else {
                    Alert confirm = confirm("Trả Phòng",
                            "Xác nhận trả phòng " + maPhong + "?\nTrạng thái sẽ chuyển sang 'Cần Dọn'.");
                    confirm.showAndWait().ifPresent(btn -> {
                        if (btn == ButtonType.OK) {
                            try { phongDAO.capNhatTrangThai(maPhong, "PhongCanDon"); }
                            catch (Exception ignored) {}
                            refreshRoomCards();
                            refreshTable();
                        }
                    });
                }
            }
            case "Cần Dọn" -> {
                try { phongDAO.capNhatTrangThai(maPhong, "DangDon"); }
                catch (Exception ignored) {}
                refreshRoomCards();
                refreshTable();
            }
            case "Đang Dọn" -> {
                Alert confirm = confirm("Hoàn Thành Dọn Phòng",
                        "Phòng " + maPhong + " đã dọn xong?\nTrạng thái sẽ chuyển sang 'Trống'.");
                confirm.showAndWait().ifPresent(btn -> {
                    if (btn == ButtonType.OK) {
                        try { phongDAO.capNhatTrangThai(maPhong, "PhongTrong"); }
                        catch (Exception ignored) {}
                        refreshRoomCards();
                        refreshTable();
                    }
                });
            }
            case "Bảo Trì" -> {
                Alert confirm = confirm("Kết Thúc Bảo Trì",
                        "Phòng " + maPhong + " đã hoàn tất bảo trì?\nTrạng thái sẽ chuyển sang 'Trống'.");
                confirm.showAndWait().ifPresent(btn -> {
                    if (btn == ButtonType.OK) {
                        try { phongDAO.capNhatTrangThai(maPhong, "PhongTrong"); }
                        catch (Exception ignored) {}
                        refreshRoomCards();
                        refreshTable();
                    }
                });
            }
            default -> {
                try { phongDAO.capNhatTrangThai(maPhong, "PhongTrong"); }
                catch (Exception ignored) {}
                refreshRoomCards();
            }
        }
    }

    /** Dialog xác nhận check-in trực tiếp từ card phòng, hiển thị đầy đủ thông tin khách */
    private void showCheckInConfirmDialog(String maPhong) {
        String maPDP = null, tenKH = null, sdt = null, cmnd = null,
               soKhach = null, gioNhan = null, gioTra = null, ghiChu = null;

        Connection con = ConnectDB.getInstance().getConnection();
        if (con != null) {
            String sql = "SELECT TOP 1 pdp.maPhieuDatPhong, kh.hoTenKH, kh.soDienThoai,"
                    + " kh.soCMND, pdp.soLuongKhach,"
                    + " CONVERT(varchar,pdp.thoiGianNhanDuKien,120),"
                    + " CONVERT(varchar,pdp.thoiGianTraDuKien,120),"
                    + " ISNULL(pdp.ghiChu,N'')"
                    + " FROM ChiTietPhieuDatPhong ct"
                    + " JOIN PhieuDatPhong pdp ON pdp.maPhieuDatPhong=ct.maPhieuDatPhong"
                    + " JOIN KhachHang kh ON kh.maKH=pdp.maKhachHang"
                    + " WHERE ct.maPhong=?"
                    + " AND pdp.trangThai NOT IN (N'DaCheckOut',N'HuyDat',N'DaCheckIn')"
                    + " ORDER BY pdp.thoiGianNhanDuKien ASC";
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, maPhong);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        maPDP    = rs.getString(1);
                        tenKH    = rs.getString(2);
                        sdt      = rs.getString(3);
                        cmnd     = rs.getString(4);
                        soKhach  = rs.getString(5);
                        gioNhan  = rs.getString(6);
                        gioTra   = rs.getString(7);
                        ghiChu   = rs.getString(8);
                    }
                }
            } catch (Exception ignored) {}
        }

        if (maPDP == null) {
            Alert err = new Alert(Alert.AlertType.WARNING,
                    "Không tìm thấy phiếu đặt phòng hợp lệ cho phòng " + maPhong + ".");
            err.setHeaderText(null); err.setTitle("Không Tìm Thấy Phiếu"); err.showAndWait();
            return;
        }

        // ── Build info grid ──────────────────────────────────────────────────
        GridPane grid = new GridPane();
        grid.setHgap(20); grid.setVgap(9);
        grid.setPadding(new Insets(14, 4, 6, 4));

        String[][] rows = {
            {"Mã Phiếu:",      maPDP},
            {"Phòng:",          maPhong},
            {"Khách Hàng:",    tenKH   != null ? tenKH  : "—"},
            {"SĐT:",           sdt     != null ? sdt    : "—"},
            {"CMND / CCCD:",   cmnd    != null ? cmnd   : "—"},
            {"Số Khách:",      soKhach != null ? soKhach : "—"},
            {"Giờ Nhận DK:",   gioNhan != null ? gioNhan : "—"},
            {"Giờ Trả DK:",    gioTra  != null ? gioTra  : "—"},
        };
        for (int i = 0; i < rows.length; i++) {
            Label key = new Label(rows[i][0]);
            key.setStyle("-fx-font-size:12px;-fx-text-fill:#595959;-fx-font-weight:bold;");
            Label val = new Label(rows[i][1]);
            val.setStyle("-fx-font-size:13px;-fx-text-fill:#1A1A2E;"
                    + (i == 2 ? "-fx-font-weight:bold;" : ""));
            grid.add(key, 0, i); grid.add(val, 1, i);
        }
        if (ghiChu != null && !ghiChu.isBlank()) {
            Label kGhi = new Label("Ghi Chú:");
            kGhi.setStyle("-fx-font-size:12px;-fx-text-fill:#595959;-fx-font-weight:bold;");
            Label vGhi = new Label(ghiChu);
            vGhi.setStyle("-fx-font-size:12px;-fx-text-fill:#FA8C16;");
            vGhi.setWrapText(true);
            grid.add(kGhi, 0, rows.length); grid.add(vGhi, 1, rows.length);
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Tiếp Nhận Khách");
        confirm.setHeaderText("Xác Nhận Check-in — Phòng " + maPhong);
        confirm.getDialogPane().setContent(grid);
        confirm.getDialogPane().setPrefWidth(420);

        final String finalMaPDP = maPDP, finalTenKH = tenKH;
        confirm.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK) return;
            try {
                boolean ok = new PhieuDatPhongDAO().checkIn(finalMaPDP);
                if (ok) {
                    refreshRoomCards();
                    refreshTable();
                    Alert success = new Alert(Alert.AlertType.INFORMATION,
                            "✅ Check-in thành công!\nKhách: " + finalTenKH
                            + "\nPhiếu: " + finalMaPDP
                            + "\nTrạng thái phòng → Đang có khách.");
                    success.setHeaderText(null); success.setTitle("Check-in Thành Công");
                    success.showAndWait();
                } else {
                    Alert err = new Alert(Alert.AlertType.ERROR,
                            "Check-in thất bại!\nPhiếu có thể đã được xử lý hoặc không hợp lệ.");
                    err.setHeaderText(null); err.setTitle("Lỗi Check-in"); err.showAndWait();
                }
            } catch (Exception ex) {
                Alert err = new Alert(Alert.AlertType.ERROR,
                        "Lỗi kết nối: " + ex.getMessage());
                err.setHeaderText(null); err.setTitle("Lỗi Kết Nối"); err.showAndWait();
            }
        });
    }

    /**
     * Tra cứu mã phiếu đặt phòng đang active (PhongDat) cho phòng này.
     * Ưu tiên phiếu chưa checkout, lấy mới nhất.
     */
    private String layMaPDPDangActive(String maPhong) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return null;
        String sql = "SELECT TOP 1 ct.maPhieuDatPhong " +
                     "FROM ChiTietPhieuDatPhong ct " +
                     "JOIN PhieuDatPhong pdp ON pdp.maPhieuDatPhong = ct.maPhieuDatPhong " +
                     "WHERE ct.maPhong = ? " +
                     "  AND pdp.trangThai NOT IN ('DaCheckOut','HuyDat') " +
                     "ORDER BY pdp.thoiGianNhanDuKien DESC";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maPhong);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        } catch (Exception ignored) {}
        return null;
    }

    /** Dialog đặt phòng nhanh từ card "Nhận Phòng" */
    private void openNhanPhongDialog(String maPhong) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Nhận Phòng - " + maPhong);
        dialog.setResizable(false);

        GridPane form = new GridPane();
        form.setHgap(12); form.setVgap(12);
        form.setPadding(new Insets(20));
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPrefWidth(140);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPrefWidth(200);
        form.getColumnConstraints().addAll(c1, c2);

        TextField txtSDT     = field("");
        txtSDT.setPromptText("VD: 0912345678");
        TextField txtTenKH   = field("");
        txtTenKH.setPromptText("(điền nếu khách mới)");
        TextField txtSoNguoi = field("1");
        DatePicker dpNhan = new DatePicker(LocalDate.now());
        DatePicker dpTra  = new DatePicker(LocalDate.now().plusDays(1));
        dpNhan.setMaxWidth(Double.MAX_VALUE);
        dpTra.setMaxWidth(Double.MAX_VALUE);

        Label kh_status = new Label("⚪ Chưa tra cứu");
        kh_status.setStyle("-fx-font-size: 11px; -fx-text-fill: #8C8C8C;");
        Button btnTraCuu = new Button("🔍");
        btnTraCuu.setStyle("-fx-background-color: #1890FF; -fx-text-fill: white;"
                + "-fx-background-radius: 6; -fx-padding: 6 10; -fx-cursor: hand; -fx-font-size: 11px;");
        HBox sdtRow = new HBox(6, txtSDT, btnTraCuu);
        HBox.setHgrow(txtSDT, Priority.ALWAYS);

        String[] resolvedMaKH = {null};
        btnTraCuu.setOnAction(ev -> {
            String sdt = txtSDT.getText().trim();
            if (sdt.isEmpty()) { kh_status.setText("⚠ Nhập SĐT trước"); return; }
            KhachHang found = lookupKhachBySDT(sdt);
            if (found != null) {
                resolvedMaKH[0] = found.getMaKH();
                txtTenKH.setText(found.getHoTenKH());
                kh_status.setText("✓ " + found.getHoTenKH() + " (" + found.getMaKH() + ")");
                kh_status.setStyle("-fx-font-size: 11px; -fx-text-fill: #52C41A;");
            } else {
                resolvedMaKH[0] = null;
                kh_status.setText("⚠ Khách mới — tự tạo khi đặt");
                kh_status.setStyle("-fx-font-size: 11px; -fx-text-fill: #FAAD14;");
            }
        });
        txtSDT.setOnAction(ev -> btnTraCuu.fire());

        form.add(fLabel("SĐT Khách *:"),     0, 0); form.add(sdtRow,   1, 0);
        form.add(fLabel("Tên Khách:"),        0, 1); form.add(txtTenKH, 1, 1);
        form.add(fLabel("Mã Phòng:"),         0, 2);
        Label maPhongLbl = new Label(maPhong);
        maPhongLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #1890FF;");
        form.add(maPhongLbl, 1, 2);
        form.add(fLabel("Số Khách:"),         0, 3); form.add(txtSoNguoi, 1, 3);
        form.add(fLabel("Ngày Nhận:"),        0, 4); form.add(dpNhan,     1, 4);
        form.add(fLabel("Ngày Trả:"),         0, 5); form.add(dpTra,      1, 5);
        form.add(kh_status,                   0, 6);
        GridPane.setColumnSpan(kh_status, 2);

        Label errLbl = new Label();
        errLbl.setStyle("-fx-text-fill: #FF4D4F; -fx-font-size: 12px;");
        errLbl.setVisible(false); errLbl.setManaged(false);
        errLbl.setPadding(new Insets(0, 20, 0, 20));

        Button btnOK = new Button("Đặt Phòng");
        btnOK.setStyle("-fx-background-color: #1890FF; -fx-text-fill: white;"
                + "-fx-background-radius: 6; -fx-padding: 8 20; -fx-cursor: hand;");
        Button btnCancel = new Button("Hủy");
        btnCancel.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #595959;"
                + "-fx-background-radius: 6; -fx-padding: 8 20; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> dialog.close());

        btnOK.setOnAction(e -> {
            String sdt   = txtSDT.getText().trim();
            String tenKH = txtTenKH.getText().trim();
            if (sdt.isEmpty()) {
                errLbl.setText("Vui lòng nhập SĐT!"); errLbl.setVisible(true); errLbl.setManaged(true); return;
            }
            if (dpTra.getValue() == null || !dpTra.getValue().isAfter(dpNhan.getValue())) {
                errLbl.setText("Ngày trả phải sau ngày nhận!"); errLbl.setVisible(true); errLbl.setManaged(true); return;
            }
            int soNguoi;
            try { soNguoi = Integer.parseInt(txtSoNguoi.getText().trim()); if (soNguoi <= 0) throw new Exception(); }
            catch (Exception ex) { errLbl.setText("Số khách phải là số nguyên dương!"); errLbl.setVisible(true); errLbl.setManaged(true); return; }

            // Resolve maKH
            String maKH = resolvedMaKH[0];
            if (maKH == null) {
                KhachHang found = lookupKhachBySDT(sdt);
                if (found != null) {
                    maKH = found.getMaKH();
                } else {
                    if (tenKH.isEmpty()) {
                        errLbl.setText("Khách mới — vui lòng nhập tên khách!");
                        errLbl.setVisible(true); errLbl.setManaged(true); return;
                    }
                    KhachHang newKH = new KhachHang(null, tenKH, sdt, "");
                    KhachHangDAO khDao = new KhachHangDAO();
                    if (!khDao.themKhachHang(newKH)) {
                        String reason = khDao.getLastError();
                        errLbl.setText(reason != null ? reason : "Không tạo được khách mới. Kiểm tra DB.");
                        errLbl.setVisible(true); errLbl.setManaged(true); return;
                    }
                    maKH = newKH.getMaKH();
                }
            }

            try {
                String maPDP = "PDP" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
                com.lotuslaverne.entity.PhieuDatPhong pdp = new com.lotuslaverne.entity.PhieuDatPhong(
                    maPDP, maKH, "NV001", soNguoi,
                    Timestamp.valueOf(dpNhan.getValue().atStartOfDay()),
                    Timestamp.valueOf(dpTra.getValue().atStartOfDay()), ""
                );
                if (new PhieuDatPhongDAO().lapPhieuDat(pdp)) {
                    Connection ctCon = ConnectDB.getInstance().getConnection();
                    if (ctCon != null) {
                        double donGia = 0;
                        try (PreparedStatement pstGia = ctCon.prepareStatement(
                                "SELECT ISNULL(bg.donGia,0) FROM Phong p "
                                + "LEFT JOIN BangGia bg ON bg.maLoaiPhong=p.maLoaiPhong "
                                + "AND bg.loaiThue='QuaDem' AND GETDATE() BETWEEN bg.ngayBatDau AND bg.ngayKetThuc "
                                + "WHERE p.maPhong=?")) {
                            pstGia.setString(1, maPhong);
                            ResultSet rsGia = pstGia.executeQuery();
                            if (rsGia.next()) donGia = rsGia.getDouble(1);
                        } catch (Exception exGia) { exGia.printStackTrace(); }
                        try (PreparedStatement pstCT = ctCon.prepareStatement(
                                "INSERT INTO ChiTietPhieuDatPhong (maPhieuDatPhong, maPhong, donGia) VALUES (?,?,?)")) {
                            pstCT.setString(1, maPDP);
                            pstCT.setString(2, maPhong);
                            pstCT.setDouble(3, donGia);
                            pstCT.executeUpdate();
                        } catch (Exception exCT) { exCT.printStackTrace(); }
                    }
                    dialog.close();
                    refreshRoomCards();
                    refreshTable();
                    alert("Thành công", "Đặt phòng thành công!\nMã phiếu: " + maPDP + "\nKhách: " + maKH);
                } else {
                    errLbl.setText("Lỗi! Kiểm tra mã phòng hoặc lịch trùng.");
                    errLbl.setVisible(true); errLbl.setManaged(true);
                }
            } catch (Exception ex) {
                errLbl.setText("Lỗi kết nối DB. Kiểm tra máy chủ.");
                errLbl.setVisible(true); errLbl.setManaged(true);
            }
        });

        HBox btnRow = new HBox(10, btnCancel, btnOK);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(0, 20, 16, 20));

        VBox root = new VBox(form, errLbl, btnRow);
        root.setStyle("-fx-background-color: #FFFFFF;");
        dialog.setScene(new Scene(root, 420, 420));
        dialog.showAndWait();
    }

    /** Tra cứu khách theo SĐT từ DB */
    private KhachHang lookupKhachBySDT(String sdt) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null || sdt == null || sdt.isEmpty()) return null;
        try (PreparedStatement pst = con.prepareStatement(
                "SELECT maKH, hoTenKH, soDienThoai, cmnd FROM KhachHang WHERE soDienThoai = ?")) {
            pst.setString(1, sdt);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new KhachHang(rs.getString("maKH"), rs.getString("hoTenKH"),
                            rs.getString("soDienThoai"), rs.getString("cmnd"));
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    /** Dialog sửa thông tin phòng */
    private void openEditDialog(Object[] r) {
        String maPhong = (String) r[0];
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Sửa Phòng - " + maPhong);
        dialog.setResizable(false);

        GridPane form = new GridPane();
        form.setHgap(12); form.setVgap(12);
        form.setPadding(new Insets(20));
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPrefWidth(130);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPrefWidth(200);
        form.getColumnConstraints().addAll(c1, c2);

        TextField txtTen  = field(r[1].toString());
        TextField txtLoai = field(r[2].toString());
        ComboBox<String> cbTT = new ComboBox<>();
        cbTT.getItems().addAll("Trống", "Chờ Check-in", "Đang Thuê", "Cần Dọn", "Đang Dọn", "Bảo Trì");
        cbTT.setValue(r[5].toString());
        cbTT.setMaxWidth(Double.MAX_VALUE);

        form.add(fLabel("Mã Phòng:"),    0, 0);
        Label maLbl = new Label(maPhong);
        maLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #1890FF;");
        form.add(maLbl, 1, 0);
        form.add(fLabel("Tên Phòng:"),   0, 1); form.add(txtTen,  1, 1);
        form.add(fLabel("Loại Phòng:"),  0, 2); form.add(txtLoai, 1, 2);
        form.add(fLabel("Trạng Thái:"),  0, 3); form.add(cbTT,    1, 3);

        Button btnSave = new Button("Lưu");
        btnSave.setStyle("-fx-background-color: #1890FF; -fx-text-fill: white;"
                + "-fx-background-radius: 6; -fx-padding: 8 20; -fx-cursor: hand;");
        Button btnCancel = new Button("Hủy");
        btnCancel.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #595959;"
                + "-fx-background-radius: 6; -fx-padding: 8 20; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> dialog.close());

        btnSave.setOnAction(e -> {
            String dbStatus = switch (cbTT.getValue()) {
                case "Trống"        -> "PhongTrong";
                case "Chờ Check-in" -> "PhongTrong";
                case "Đang Thuê"    -> "PhongDat";
                case "Cần Dọn"     -> "PhongCanDon";
                case "Đang Dọn"    -> "DangDon";
                case "Bảo Trì"     -> "BaoTri";
                default             -> cbTT.getValue();
            };
            try {
                new PhongDAO().capNhatTrangThai(maPhong, dbStatus);
            } catch (Exception ignored) {}
            dialog.close();
            refreshRoomCards();
            refreshTable();
        });

        HBox btnRow = new HBox(10, btnCancel, btnSave);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(0, 20, 16, 20));

        VBox root = new VBox(form, btnRow);
        root.setStyle("-fx-background-color: #FFFFFF;");
        dialog.setScene(new Scene(root, 370, 260));
        dialog.showAndWait();
    }

    // ─────────────────────────────────────────── TAB DANH SÁCH (table)
    private Node buildDanhSachTab() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(16, 24, 24, 24));
        container.setStyle("-fx-background-color: #F0F2F5;");

        // ── Hàng 1: Search + Count + Nút hành động
        tableItems = FXCollections.observableArrayList(loadRooms());
        tableCountLbl = new Label(tableItems.size() + " kết quả");
        tableCountLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #8C8C8C;");

        TextField search = new TextField();
        search.setPromptText("Tìm phòng, khách...");
        search.setPrefWidth(200); search.setPrefHeight(34);
        search.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #E8E8E8;"
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1;"
                + "-fx-padding: 4 10; -fx-font-size: 12px;");

        Button refreshBtn = new Button("↻ Làm Mới");
        refreshBtn.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #595959;"
                + "-fx-background-radius: 8; -fx-border-color: #D9D9D9; -fx-border-width: 1;"
                + "-fx-border-radius: 8; -fx-padding: 6 12; -fx-cursor: hand; -fx-font-size: 12px;");
        refreshBtn.setOnAction(e -> {
            refreshTable();
            refreshRoomCards();
            com.lotuslaverne.fx.UiUtils.flashButton(refreshBtn, "✓ Đã làm mới");
        });

        Button addBtn = new Button("+ Thêm Phòng");
        addBtn.setStyle("-fx-background-color: #1890FF; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-padding: 8 16; -fx-font-size: 12px;"
                + "-fx-font-weight: bold; -fx-cursor: hand;");
        addBtn.setOnAction(e -> openAddPhongDialog());

        Region spacerTop = new Region(); HBox.setHgrow(spacerTop, Priority.ALWAYS);
        HBox topRow = new HBox(10, search, spacerTop, tableCountLbl, refreshBtn, addBtn);
        topRow.setAlignment(Pos.CENTER_LEFT);
        topRow.setPadding(new Insets(12, 16, 8, 16));
        topRow.setStyle("-fx-background-color: #FFFFFF;");

        // ── Hàng 2: Filter chips Loại + Trạng thái (multi-select, tự wrap)
        Label tableFilterLbl = new Label("Tất cả phòng");
        tableFilterLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#8C8C8C;");
        Button btnTableClear = new Button("✕ Xóa Bộ Lọc");
        btnTableClear.setStyle("-fx-background-color:#FFF1F0;-fx-text-fill:#FF4D4F;"
                +"-fx-background-radius:8;-fx-border-color:#FF4D4F;-fx-border-width:1;"
                +"-fx-border-radius:8;-fx-padding:4 12;-fx-cursor:hand;-fx-font-size:11px;");
        Region spacerClear = new Region(); HBox.setHgrow(spacerClear, Priority.ALWAYS);
        HBox clearRow = new HBox(8, tableFilterLbl, spacerClear, btnTableClear);
        clearRow.setAlignment(Pos.CENTER_LEFT);
        clearRow.setPadding(new Insets(6, 16, 4, 16));
        clearRow.setStyle("-fx-background-color: #FAFAFA; -fx-border-color: #F0F0F0; -fx-border-width: 1 0 0 0;");

        javafx.scene.layout.FlowPane filterRow = new javafx.scene.layout.FlowPane(8, 6);
        filterRow.setAlignment(Pos.CENTER_LEFT);
        filterRow.setPadding(new Insets(0, 16, 10, 16));
        filterRow.setStyle("-fx-background-color: #FAFAFA;");

        Label loaiLbl = chipLabel("Loại:");
        List<Button> loaiBtns = new ArrayList<>();
        for (String f : loadLoaiFilters()) {
            if ("Tất Cả".equals(f)) continue;
            Button b = makeChip(f, false); loaiBtns.add(b);
            b.setOnAction(e -> {
                boolean nowSel = !selTableLoai.contains(f);
                if (nowSel) selTableLoai.add(f); else selTableLoai.remove(f);
                applyChipStyle(b, nowSel);
                updateTableFilterLabel(tableFilterLbl);
                applyTableFilter();
            });
        }

        Region sep1 = new Region(); sep1.setPrefSize(1, 24);
        sep1.setStyle("-fx-background-color: #E8E8E8;");

        Label ttLbl = chipLabel("Trạng thái:");
        List<Button> ttBtns = new ArrayList<>();
        for (String f : TRANG_THAI_FILTER) {
            if ("Tất Cả".equals(f)) continue;
            Button b = makeChip(f, false); ttBtns.add(b);
            b.setOnAction(e -> {
                boolean nowSel = !selTableTrangThai.contains(f);
                if (nowSel) selTableTrangThai.add(f); else selTableTrangThai.remove(f);
                applyChipStyle(b, nowSel);
                updateTableFilterLabel(tableFilterLbl);
                applyTableFilter();
            });
        }

        List<Button> allTableChips = new ArrayList<>();
        allTableChips.addAll(loaiBtns); allTableChips.addAll(ttBtns);
        btnTableClear.setOnAction(e -> {
            selTableLoai.clear(); selTableTrangThai.clear();
            allTableChips.forEach(b -> applyChipStyle(b, false));
            updateTableFilterLabel(tableFilterLbl);
            applyTableFilter();
        });

        filterRow.getChildren().add(loaiLbl);
        filterRow.getChildren().addAll(loaiBtns);
        filterRow.getChildren().add(sep1);
        filterRow.getChildren().add(ttLbl);
        filterRow.getChildren().addAll(ttBtns);

        VBox toolbar = new VBox(0, topRow, clearRow, filterRow);
        toolbar.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");
;

        tableView = buildPhongTable();
        VBox.setVgrow(tableView, Priority.ALWAYS);

        // Live search kết hợp với chip filter
        search.textProperty().addListener((obs, o, n) -> {
            currentSearchKeyword = n == null ? "" : n.trim();
            applyTableFilter();
        });

        container.getChildren().addAll(toolbar, tableView);
        return container;
    }

    private void applyTableFilter() {
        if (tableItems == null || tableView == null) return;
        String kw = currentSearchKeyword.toLowerCase();
        ObservableList<Object[]> filtered = FXCollections.observableArrayList();
        for (Object[] r : tableItems) {
            if (!selTableLoai.isEmpty() && !selTableLoai.contains(r[2])) continue;
            if (!selTableTrangThai.isEmpty() && !selTableTrangThai.contains(r[5])) continue;
            if (!kw.isEmpty()) {
                boolean matched = false;
                for (Object cell : r) {
                    if (cell != null && cell.toString().toLowerCase().contains(kw)) { matched = true; break; }
                }
                if (!matched) continue;
            }
            filtered.add(r);
        }
        tableView.setItems(filtered);
        if (tableCountLbl != null) tableCountLbl.setText(filtered.size() + " kết quả");
    }

    private void updateTableFilterLabel(Label lbl) {
        int total = selTableLoai.size() + selTableTrangThai.size();
        if (total == 0) {
            lbl.setText("Tất cả phòng");
            lbl.setStyle("-fx-font-size:12px;-fx-text-fill:#8C8C8C;");
        } else {
            lbl.setText(total + " bộ lọc đang áp dụng");
            lbl.setStyle("-fx-font-size:12px;-fx-text-fill:#1890FF;-fx-font-weight:bold;");
        }
    }

    private void refreshTable() {
        if (tableItems != null) tableItems.setAll(loadRooms());
        applyTableFilter();
    }

    @SuppressWarnings("unchecked")
    private TableView<Object[]> buildPhongTable() {
        TableView<Object[]> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(48);
        table.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-border-radius: 10; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        // Phòng
        TableColumn<Object[], String> colPhong = new TableColumn<>("Phòng");
        colPhong.setPrefWidth(70);
        colPhong.setCellValueFactory(p -> new SimpleStringProperty((String) p.getValue()[1]));
        colPhong.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? null : s); refresh();
            }
            @Override public void updateSelected(boolean sel) {
                super.updateSelected(sel); if (!isEmpty()) refresh();
            }
            private void refresh() {
                setStyle(isEmpty() ? "" : (isSelected()
                    ? "-fx-font-weight:bold;-fx-font-size:13px;-fx-text-fill:white;"
                    : "-fx-font-weight:bold;-fx-font-size:13px;-fx-text-fill:#1A1A2E;"));
            }
        });

        // Lầu
        TableColumn<Object[], String> colLau = new TableColumn<>("Lầu");
        colLau.setPrefWidth(60);
        colLau.setCellValueFactory(p -> new SimpleStringProperty("Lầu " + p.getValue()[8]));

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
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); setStyle(""); return; }
                if (isSelected()) { setGraphic(null); setText(item); setStyle("-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:11px;"); }
                else { setBadge(item); }
            }
            @Override public void updateSelected(boolean sel) {
                super.updateSelected(sel);
                String item = getItem();
                if (item == null || isEmpty()) return;
                if (sel) { setGraphic(null); setText(item); setStyle("-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:11px;"); }
                else { setBadge(item); }
            }
            private void setBadge(String item) {
                Label badge = new Label(item);
                badge.setStyle("-fx-background-color:" + statusBadgeBg(item)
                        + ";-fx-text-fill:" + statusBadgeText(item)
                        + ";-fx-padding:3 10;-fx-background-radius:12;-fx-font-size:11px;-fx-font-weight:bold;");
                setGraphic(badge); setText(null); setStyle("");
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
            String k = (String) p.getValue()[6];
            return new SimpleStringProperty(k == null || k.isEmpty() ? "—" : k);
        });

        // Thao Tác
        TableColumn<Object[], Void> colAction = new TableColumn<>("Thao Tác");
        colAction.setPrefWidth(120);
        colAction.setCellFactory(tc -> new TableCell<>() {
            private final Button btnSua = btnCell("✏ Sửa", "#E6F4FF", "#1890FF");
            private final Button btnXoa = btnCell("🗑 Xóa", "#FFF1F0", "#FF4D4F");
            {
                btnSua.setOnAction(e -> {
                    Object[] row = getTableView().getItems().get(getIndex());
                    openEditDialog(row);
                });
                btnXoa.setOnAction(e -> {
                    Object[] row = getTableView().getItems().get(getIndex());
                    String ma = (String) row[0];
                    Alert c = confirm("Xóa Phòng", "Xóa phòng " + row[1] + " (" + ma + ")?");
                    c.showAndWait().ifPresent(btn -> {
                        if (btn == ButtonType.OK) {
                            try {
                                new PhongDAO().xoaPhong(ma);
                            } catch (Exception ignored) {}
                            refreshTable();
                            refreshRoomCards();
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

        table.getColumns().addAll(colPhong, colLau, colLoai, colHang, colTT, colGia, colKhach, colAction);
        table.setItems(tableItems);
        table.setPlaceholder(new Label("Không có dữ liệu phòng."));
        return table;
    }

    private void openAddPhongDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Thêm Phòng Mới");
        dialog.setResizable(false);

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:#FFFFFF;");

        VBox formRoot = new VBox(16);
        formRoot.setPadding(new Insets(20));
        formRoot.setStyle("-fx-background-color:#FFFFFF;");

        // ── Grid các trường cơ bản ──
        GridPane form = new GridPane();
        form.setHgap(12); form.setVgap(12);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(50);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(50);
        form.getColumnConstraints().addAll(c1, c2);

        TextField txtMa   = field(""); txtMa.setPromptText("VD: P305");
        TextField txtTen  = field(""); txtTen.setPromptText("VD: 305");
        TextField txtSuaChua = field("2"); txtSuaChua.setPromptText("Số người tối đa");
        TextField txtTang  = field(""); txtTang.setPromptText("VD: 3");

        ComboBox<String> cbLoai = new ComboBox<>();
        try { for (LoaiPhong lp : new LoaiPhongDAO().getAll()) cbLoai.getItems().add(lp.getTenLoaiPhong()); }
        catch (Exception ignored) {}
        if (cbLoai.getItems().isEmpty()) cbLoai.getItems().addAll("Standard","Deluxe","Suite","Family");
        cbLoai.setValue(cbLoai.getItems().get(0));
        cbLoai.setMaxWidth(Double.MAX_VALUE);
        cbLoai.setStyle("-fx-background-color:#FFF;-fx-border-color:#D9D9D9;-fx-border-radius:6;-fx-background-radius:6;-fx-border-width:1;");

        form.add(fLabel("Mã Phòng *:"),  0,0); form.add(txtMa,    0,1);
        form.add(fLabel("Tên Phòng *:"), 1,0); form.add(txtTen,   1,1);
        form.add(fLabel("Loại Phòng:"),  0,2); form.add(cbLoai,  0,3);
        form.add(fLabel("Sức Chứa:"),   1,2); form.add(txtSuaChua,1,3);

        // ── Tiện nghi (multi-select chips) ──
        Label tnLabel = fLabel("Tiện Nghi:");
        String[] tnList = {"WiFi","TV","Điều Hòa","Bồn Tắm","Ban Công","Mini Bar","Tủ Lạnh","Két An Toàn"};
        Map<String,Button> tnMap = new LinkedHashMap<>();
        HBox tnChips = new HBox(8); tnChips.setAlignment(Pos.CENTER_LEFT);
        FlowPane tnFlow = new FlowPane(8,8);
        for (String tn : tnList) {
            Button b = new Button(tn);
            b.setStyle("-fx-background-color:#F5F5F5;-fx-text-fill:#595959;"
                    +"-fx-background-radius:20;-fx-border-color:#D9D9D9;-fx-border-width:1;"
                    +"-fx-border-radius:20;-fx-padding:4 12;-fx-cursor:hand;-fx-font-size:12px;");
            b.setOnAction(e -> {
                boolean sel = "#1890FF".equals(b.getUserData());
                if (sel) {
                    b.setUserData(null);
                    b.setStyle("-fx-background-color:#F5F5F5;-fx-text-fill:#595959;"
                            +"-fx-background-radius:20;-fx-border-color:#D9D9D9;-fx-border-width:1;"
                            +"-fx-border-radius:20;-fx-padding:4 12;-fx-cursor:hand;-fx-font-size:12px;");
                } else {
                    b.setUserData("#1890FF");
                    b.setStyle("-fx-background-color:#1890FF;-fx-text-fill:white;"
                            +"-fx-background-radius:20;-fx-border-color:#1890FF;-fx-border-width:1;"
                            +"-fx-border-radius:20;-fx-padding:4 12;-fx-cursor:hand;-fx-font-size:12px;");
                }
            });
            tnMap.put(tn, b);
            tnFlow.getChildren().add(b);
        }

        // ── Mô tả ──
        Label moTaLabel = fLabel("Mô Tả:");
        TextArea taMotA = new TextArea();
        taMotA.setPromptText("Nhập mô tả phòng (tiện nghi nổi bật, view, đặc điểm...)");
        taMotA.setPrefRowCount(3);
        taMotA.setWrapText(true);
        taMotA.setStyle("-fx-background-color:#FFF;-fx-border-color:#D9D9D9;"
                +"-fx-border-radius:6;-fx-background-radius:6;-fx-border-width:1;-fx-font-size:13px;");

        Label errLbl = new Label();
        errLbl.setStyle("-fx-text-fill:#FF4D4F;-fx-font-size:12px;");
        errLbl.setVisible(false); errLbl.setManaged(false);

        Button btnSave = new Button("+ Thêm Phòng");
        btnSave.setStyle("-fx-background-color:#52C41A;-fx-text-fill:white;"
                +"-fx-background-radius:8;-fx-padding:10 24;-fx-font-weight:bold;-fx-cursor:hand;");
        Button btnCancel = new Button("Hủy");
        btnCancel.setStyle("-fx-background-color:#F5F5F5;-fx-text-fill:#595959;"
                +"-fx-background-radius:8;-fx-padding:10 20;-fx-cursor:hand;");
        btnCancel.setOnAction(e -> dialog.close());

        btnSave.setOnAction(e -> {
            if (txtMa.getText().trim().isEmpty() || txtTen.getText().trim().isEmpty()) {
                errLbl.setText("Mã phòng và tên phòng không được để trống!");
                errLbl.setVisible(true); errLbl.setManaged(true); return;
            }
            // Thu thập tiện nghi được chọn
            String tienNghi = tnMap.entrySet().stream()
                .filter(en -> "#1890FF".equals(en.getValue().getUserData()))
                .map(Map.Entry::getKey)
                .collect(Collectors.joining(","));
            int sucChua = 2;
            try { sucChua = Integer.parseInt(txtSuaChua.getText().trim()); } catch (Exception ignored) {}

            Phong phong = new Phong();
            phong.setMaPhong     (txtMa.getText().trim());
            phong.setTenPhong    (txtTen.getText().trim());
            phong.setMaLoaiPhong (cbLoai.getValue());
            phong.setTrangThai   ("PhongTrong");
            phong.setTienNghi    (tienNghi.isEmpty() ? null : tienNghi);
            phong.setSoNguoiToiDa(sucChua);
            phong.setMoTa        (taMotA.getText().trim());
            try {
                new PhongDAO().themPhong(phong);
            } catch (Exception ex) {
                errLbl.setText("Lỗi DB: " + ex.getMessage());
                errLbl.setVisible(true); errLbl.setManaged(true); return;
            }
            dialog.close();
            refreshTable();
            refreshRoomCards();
        });

        HBox btnRow = new HBox(10, btnCancel, btnSave);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        formRoot.getChildren().addAll(
            fLabel("Thêm Phòng Mới — Thông Tin Cơ Bản"), form,
            tnLabel, tnFlow,
            moTaLabel, taMotA,
            errLbl, btnRow
        );
        scroll.setContent(formRoot);
        dialog.setScene(new Scene(scroll, 520, 500));
        dialog.showAndWait();
    }

    // ─────────────────────────────────────────── DATA

    /** Trả về tập maPhong đang có phiếu đặt chờ check-in (trangThai='DaDat'). */
    /** Trả về map maPhong → trangThaiPDP cho các phiếu chờ check-in hoặc chờ thanh toán */
    private Map<String, String> loadPhongDaDat() {
        Map<String, String> map = new HashMap<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return map;
        String sql = "SELECT ct.maPhong, pdp.trangThai FROM ChiTietPhieuDatPhong ct "
                   + "JOIN PhieuDatPhong pdp ON pdp.maPhieuDatPhong=ct.maPhieuDatPhong "
                   + "WHERE pdp.trangThai IN (N'DaDat', N'ChoThanhToan')";
        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) map.put(rs.getString(1), rs.getString(2));
        } catch (Exception ignored) {}
        return map;
    }

    /** Trả về map maPhong → [tenKhach, ngayTraDK] cho phòng có booking đang hoạt động */
    private Map<String, String[]> loadGuestInfo() {
        Map<String, String[]> map = new HashMap<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return map;
        String sql = "SELECT ct.maPhong, kh.hoTenKH,"
                   + " CONVERT(varchar,pdp.thoiGianTraDuKien,103)"
                   + " FROM ChiTietPhieuDatPhong ct"
                   + " JOIN PhieuDatPhong pdp ON pdp.maPhieuDatPhong=ct.maPhieuDatPhong"
                   + " JOIN KhachHang kh ON kh.maKH=pdp.maKhachHang"
                   + " WHERE pdp.trangThai IN (N'DaDat', N'DaCheckIn')";
        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                String maPhong  = rs.getString(1);
                String tenKhach = rs.getString(2) != null ? rs.getString(2) : "";
                String ngayTra  = rs.getString(3) != null ? rs.getString(3) : "";
                map.put(maPhong, new String[]{tenKhach, ngayTra});
            }
        } catch (Exception ignored) {}
        return map;
    }

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
                Map<String, String> phongDaDat = loadPhongDaDat();
                Map<String, String[]> guestInfo = loadGuestInfo();
                for (int i = 0; i < phongs.size(); i++) {
                    Phong p = phongs.get(i);
                    String loaiName = loaiMap.getOrDefault(p.getMaLoaiPhong(), "Standard");
                    String pdpStatus = phongDaDat.get(p.getMaPhong());
                    String displayStatus = switch (p.getTrangThai() != null ? p.getTrangThai() : "") {
                        case "PhongTrong"  -> "DaDat".equals(pdpStatus) ? "Chờ Check-in"
                                           : "ChoThanhToan".equals(pdpStatus) ? "Chờ Thanh Toán"
                                           : "Trống";
                        case "PhongDat"    -> "Đang Thuê";
                        case "PhongCanDon" -> "Cần Dọn";
                        case "DangDon"     -> "Đang Dọn";
                        case "BaoTri"      -> "Bảo Trì";
                        default -> p.getTrangThai();
                    };
                    int floor = (i / 4) + 1;
                    String tienNghi = p.getTienNghi() != null ? p.getTienNghi() : "WiFi";
                    String[] guest  = guestInfo.getOrDefault(p.getMaPhong(), new String[]{"", ""});
                    result.add(new Object[]{
                        p.getMaPhong(), p.getTenPhong(), loaiName, "Phòng Đôi",
                        750_000L, displayStatus, guest[0], guest[1], floor, tienNghi
                    });
                }
                return result;
            }
        } catch (Exception ignored) {}
        for (Object[] r : DEMO_ROOMS) result.add(r);
        return result;
    }

    // ─────────────────────────────────────────── HELPERS
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
                    + "-fx-padding: 4 14; -fx-font-size: 12px; -fx-cursor: hand;");
        } else {
            btn.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #333333;"
                    + "-fx-background-radius: 20; -fx-border-radius: 20;"
                    + "-fx-border-color: #D9D9D9; -fx-border-width: 1;"
                    + "-fx-padding: 4 14; -fx-font-size: 12px; -fx-cursor: hand;");
        }
    }

    private Label chipLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #595959;");
        return l;
    }

    private TextField field(String val) {
        TextField tf = new TextField(val);
        tf.setMaxWidth(Double.MAX_VALUE);
        tf.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D9D9D9;"
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1; -fx-padding: 7 10;");
        return tf;
    }

    private Label fLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #595959;");
        return l;
    }

    private Button btnCell(String text, String bg, String fg) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";"
                + "-fx-background-radius: 6; -fx-padding: 4 10; -fx-font-size: 11px; -fx-cursor: hand;");
        return b;
    }

    private Alert confirm(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg);
        a.setHeaderText(null); a.setTitle(title);
        return a;
    }

    private void alert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg);
        a.setHeaderText(null); a.setTitle(title);
        a.showAndWait();
    }

    private String statusBorderColor(String tt) {
        return switch (tt) {
            case "Chờ Thanh Toán" -> "#FA8C16";
            case "Chờ Check-in" -> "#722ED1";
            case "Đang Thuê" -> "#FF4D4F";
            case "Cần Dọn"  -> "#FAAD14";
            case "Đang Dọn" -> "#1890FF";
            case "Bảo Trì"  -> "#8C8C8C";
            default          -> "#52C41A";
        };
    }

    private String statusBadgeBg(String tt) {
        return switch (tt) {
            case "Chờ Thanh Toán" -> "#FFF7E6";
            case "Chờ Check-in" -> "#F9F0FF";
            case "Đang Thuê" -> "#FFF1F0";
            case "Cần Dọn"  -> "#FFFBE6";
            case "Đang Dọn" -> "#E6F4FF";
            case "Bảo Trì"  -> "#F5F5F5";
            default          -> "#F6FFED";
        };
    }

    private String statusBadgeText(String tt) {
        return switch (tt) {
            case "Chờ Thanh Toán" -> "#FA8C16";
            case "Chờ Check-in" -> "#722ED1";
            case "Đang Thuê" -> "#FF4D4F";
            case "Cần Dọn"  -> "#FAAD14";
            case "Đang Dọn" -> "#1890FF";
            case "Bảo Trì"  -> "#595959";
            default          -> "#52C41A";
        };
    }

    private String actionLabel(String tt) {
        return switch (tt) {
            case "Chờ Thanh Toán" -> "Xác Nhận TT";
            case "Chờ Check-in" -> "Check In";
            case "Đang Thuê" -> "Trả Phòng";
            case "Cần Dọn"  -> "Giao Dọn";
            case "Đang Dọn" -> "Hoàn Thành";
            case "Bảo Trì"  -> "Kết Thúc Bảo Trì";
            default          -> "Đặt Phòng";
        };
    }

    /** Mở cửa sổ nổi Đặt Phòng cho phòng cụ thể, giữ nguyên màn Quản Lý Phòng ở sau */
    private void openDatPhongPopup(String maPhong) {
        Stage popup = new Stage();
        popup.setTitle("Đặt Phòng — Phòng " + maPhong);
        popup.setWidth(1150); popup.setHeight(760);
        popup.setMinWidth(860); popup.setMinHeight(580);
        popup.setResizable(true);

        javafx.scene.Node view = new DatPhongView(maPhong).build();
        ScrollPane scroll = new ScrollPane(view);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:#F0F2F5;-fx-border-color:transparent;");

        javafx.scene.Scene scene = new javafx.scene.Scene(scroll, 1150, 760);
        try { scene.getStylesheets().add(
                getClass().getResource("/com/lotuslaverne/fx/style.css").toExternalForm());
        } catch (Exception ignored) {}

        popup.setScene(scene);
        popup.setOnHidden(e -> { refreshRoomCards(); refreshTable(); });
        popup.show();
        popup.centerOnScreen();
    }

    /** Mở cửa sổ nổi Check-out / Trả Phòng cho phòng cụ thể */
    private void openCheckoutPopup(String maPhong) {
        Stage popup = new Stage();
        popup.setTitle("Trả Phòng — Phòng " + maPhong);
        popup.setWidth(1150); popup.setHeight(760);
        popup.setMinWidth(860); popup.setMinHeight(580);
        popup.setResizable(true);

        javafx.scene.Node view = new CheckoutView(maPhong).build();
        ScrollPane scroll = new ScrollPane(view);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:#F0F2F5;-fx-border-color:transparent;");

        javafx.scene.Scene scene = new javafx.scene.Scene(scroll, 1150, 760);
        try { scene.getStylesheets().add(
                getClass().getResource("/com/lotuslaverne/fx/style.css").toExternalForm());
        } catch (Exception ignored) {}

        popup.setScene(scene);
        popup.setOnHidden(e -> { refreshRoomCards(); refreshTable(); });
        popup.show();
        popup.centerOnScreen();
    }

    /** Xác nhận thanh toán cọc cho phòng đang ở trạng thái Chờ Thanh Toán */
    private void showXacNhanThanhToanDialog(String maPhong) {
        // Lấy thông tin phiếu ChoThanhToan của phòng
        String maPDP = null, tenKH = null, gioNhan = null, gioTra = null;
        Connection con = ConnectDB.getInstance().getConnection();
        if (con != null) {
            String sql = "SELECT TOP 1 pdp.maPhieuDatPhong, kh.hoTenKH,"
                    + " CONVERT(varchar,pdp.thoiGianNhanDuKien,120),"
                    + " CONVERT(varchar,pdp.thoiGianTraDuKien,120)"
                    + " FROM ChiTietPhieuDatPhong ct"
                    + " JOIN PhieuDatPhong pdp ON pdp.maPhieuDatPhong=ct.maPhieuDatPhong"
                    + " JOIN KhachHang kh ON kh.maKH=pdp.maKhachHang"
                    + " WHERE ct.maPhong=? AND pdp.trangThai=N'ChoThanhToan'"
                    + " ORDER BY pdp.thoiGianNhanDuKien ASC";
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, maPhong);
                try (java.sql.ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        maPDP   = rs.getString(1);
                        tenKH   = rs.getString(2);
                        gioNhan = rs.getString(3);
                        gioTra  = rs.getString(4);
                    }
                }
            } catch (Exception ignored) {}
        }
        if (maPDP == null) {
            Alert err = new Alert(Alert.AlertType.WARNING,
                    "Không tìm thấy phiếu chờ thanh toán cho phòng " + maPhong + ".");
            err.setHeaderText(null); err.setTitle("Không Tìm Thấy"); err.showAndWait(); return;
        }

        GridPane grid = new GridPane();
        grid.setHgap(20); grid.setVgap(9);
        grid.setPadding(new Insets(14, 4, 6, 4));
        String[][] rows = {
            {"Mã Phiếu:", maPDP}, {"Phòng:", maPhong},
            {"Khách Hàng:", tenKH  != null ? tenKH  : "—"},
            {"Giờ Nhận DK:", gioNhan != null ? gioNhan : "—"},
            {"Giờ Trả DK:",  gioTra  != null ? gioTra  : "—"},
        };
        for (int i = 0; i < rows.length; i++) {
            Label k = new Label(rows[i][0]);
            k.setStyle("-fx-font-size:12px;-fx-text-fill:#595959;-fx-font-weight:bold;");
            Label v = new Label(rows[i][1]);
            v.setStyle("-fx-font-size:13px;-fx-text-fill:#1A1A2E;"
                    + (i == 2 ? "-fx-font-weight:bold;" : ""));
            grid.add(k, 0, i); grid.add(v, 1, i);
        }

        ButtonType btnXacNhan = new ButtonType("✅ Xác Nhận Đã Nhận Tiền", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnHuy     = new ButtonType("🗑 Hủy Đặt Chỗ", ButtonBar.ButtonData.OTHER);
        ButtonType btnCancel  = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert dlg = new Alert(Alert.AlertType.CONFIRMATION);
        dlg.setTitle("Xác Nhận Thanh Toán Cọc");
        dlg.setHeaderText("Phòng " + maPhong + " — Chờ Chuyển Khoản");
        dlg.getDialogPane().setContent(grid);
        dlg.getDialogPane().setPrefWidth(420);
        dlg.getButtonTypes().setAll(btnXacNhan, btnHuy, btnCancel);

        final String fMaPDP = maPDP, fTenKH = tenKH;
        dlg.showAndWait().ifPresent(btn -> {
            if (btn == btnXacNhan) {
                Connection c = ConnectDB.getInstance().getConnection();
                if (c == null) { alert("Lỗi", "Không kết nối DB."); return; }
                try (PreparedStatement pst = c.prepareStatement(
                        "UPDATE PhieuDatPhong SET trangThai=N'DaDat' WHERE maPhieuDatPhong=?")) {
                    pst.setString(1, fMaPDP); pst.executeUpdate();
                } catch (Exception ex) { alert("Lỗi", ex.getMessage()); return; }
                refreshRoomCards(); refreshTable();
                Alert ok = new Alert(Alert.AlertType.INFORMATION,
                        "✅ Xác nhận thành công!\nKhách: " + fTenKH + "\nPhiếu: " + fMaPDP
                        + "\nPhòng → Chờ Check-in.");
                ok.setHeaderText(null); ok.setTitle("Đã Xác Nhận"); ok.showAndWait();
            } else if (btn == btnHuy) {
                Alert confirmHuy = confirm("Hủy Đặt Chỗ",
                        "Hủy giữ chỗ phòng " + maPhong + " cho khách " + fTenKH + "?\nPhòng sẽ trở về trạng thái Trống.");
                confirmHuy.showAndWait().ifPresent(b2 -> {
                    if (b2 != ButtonType.OK) return;
                    Connection c = ConnectDB.getInstance().getConnection();
                    if (c == null) { alert("Lỗi", "Không kết nối DB."); return; }
                    try (PreparedStatement pst = c.prepareStatement(
                            "UPDATE PhieuDatPhong SET trangThai=N'HuyDat' WHERE maPhieuDatPhong=?")) {
                        pst.setString(1, fMaPDP); pst.executeUpdate();
                    } catch (Exception ex) { alert("Lỗi", ex.getMessage()); return; }
                    refreshRoomCards(); refreshTable();
                });
            }
        });
    }

    private String actionStyle(String tt) {
        String bg = switch (tt) {
            case "Chờ Thanh Toán" -> "#FA8C16";
            case "Chờ Check-in" -> "#722ED1";
            case "Đang Thuê" -> "#FF4D4F";
            case "Cần Dọn"  -> "#FAAD14";
            case "Đang Dọn" -> "#1890FF";
            case "Bảo Trì"  -> "#8C8C8C";
            default          -> "#1890FF";
        };
        return "-fx-background-color: " + bg + "; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-border-radius: 8;"
                + "-fx-font-size: 12px; -fx-font-weight: bold;"
                + "-fx-padding: 6 10; -fx-cursor: hand;";
    }
}
