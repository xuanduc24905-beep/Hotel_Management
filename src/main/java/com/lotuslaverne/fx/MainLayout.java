package com.lotuslaverne.fx;

import com.lotuslaverne.fx.views.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainLayout {

    private final Stage stage;
    private final String username;
    private final String vaiTro;
    private final boolean isQuanLy;

    private StackPane contentArea;
    private final List<HBox> navItems = new ArrayList<>();
    private int activeIndex = 0;

    private final Map<Integer, Boolean> sectionExpanded = new HashMap<>();
    private final Map<Integer, VBox> sectionContentMap = new HashMap<>();
    private final Map<Integer, Label> sectionArrows = new HashMap<>();

    private record NavEntry(String icon, String label, String key, boolean quanLyOnly) {}

    private final NavEntry[] ALL_NAV = {
            new NavEntry("⊖", "Tổng Quan",              "dashboard",   false), // 0
            new NavEntry("🚪", "Quản Lý Phòng",         "phong",       false), // 1
            new NavEntry("🧹", "Phòng",                  "housekeeping",false), // 2
            new NavEntry("🔧", "Thiết Bị",               "thietbi",     false), // 3
            new NavEntry("📋", "Đặt Phòng",              "datphong",    false), // 4
            new NavEntry("✅", "Check-in",               "checkin",     false), // 5
            new NavEntry("🔄", "Đổi Phòng",              "doiphong",    false), // 6
            new NavEntry("🏁", "Check-out / Trả Phòng", "checkout",    false), // 7
            new NavEntry("🛎", "Dịch Vụ Phòng",         "dichvuphong", false), // 8
            new NavEntry("🍽", "Danh Mục Dịch Vụ",      "dichvu",      false), // 9
            new NavEntry("📝", "Yêu Cầu Khách",         "yeucau",      false), // 10
            new NavEntry("👤", "Quản Lý Khách",         "khach",       false), // 11
            new NavEntry("📑", "Lịch Sử Hóa Đơn",      "hoadon",      false), // 12
            new NavEntry("🧾", "Phiếu Thu",              "phieuthu",    false), // 13
            new NavEntry("🏷", "Khuyến Mãi",             "khuyenmai",   false), // 14
            new NavEntry("💰", "Bảng Giá",               "banggia",     false), // 15
            new NavEntry("📊", "Báo Cáo",                "baocao",      false), // 16
            new NavEntry("👥", "Nhân Viên",              "nhanvien",    true),  // 17
            new NavEntry("⏱", "Chấm Công",              "chamcong",    true),  // 18
            new NavEntry("🔑", "Tài Khoản",              "taikhoan",    true),  // 19
            new NavEntry("⚙", "Cài Đặt",                "caidat",      true),  // 20
    };

    private static final int[] SECTION_BREAKS = { 0, 1, 4, 8, 11, 12, 16, 17 };
    private static final String[] SECTION_LABELS = {
            "TỔNG QUAN", "QUẢN LÝ PHÒNG", "LƯU TRÚ", "DỊCH VỤ",
            "KHÁCH HÀNG", "TÀI CHÍNH", "BÁO CÁO", "NHÂN SỰ & HỆ THỐNG"
    };

    public MainLayout(Stage stage, String username, String vaiTro) {
        this.stage = stage;
        this.username = username;
        this.vaiTro = vaiTro;
        this.isQuanLy = "Quản Lý".equals(vaiTro) || "QuanLy".equals(vaiTro);
    }

    public void show() {
        BorderPane root = new BorderPane();

        VBox sidebar = buildSidebar();
        HBox header = buildHeader();
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: #F0F2F5;");

        root.setLeft(sidebar);
        root.setTop(header);
        root.setCenter(contentArea);

        navigate(0);

        Scene scene = new Scene(root, 1280, 780);
        try {
            scene.getStylesheets().add(
                    getClass().getResource("/com/lotuslaverne/fx/style.css").toExternalForm());
        } catch (Exception ignored) {}

        stage.setTitle("Lotus Laverne Hotel Management");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(960);
        stage.setMinHeight(640);
        stage.centerOnScreen();
        stage.show();
    }

    // ------------------------------------------------------------------ SIDEBAR
    private VBox buildSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setStyle("-fx-background-color: #FFFFFF;"
                + "-fx-border-color: transparent #E8E8E8 transparent transparent;"
                + "-fx-border-width: 0 1 0 0;");
        sidebar.setPrefWidth(200);
        sidebar.setMinWidth(200);
        sidebar.setMaxWidth(200);

        // Logo
        HBox logoSection = new HBox(8);
        logoSection.setAlignment(Pos.CENTER_LEFT);
        logoSection.setPadding(new Insets(12, 14, 10, 14));
        logoSection.setStyle("-fx-border-color: transparent transparent #F0F2F5 transparent;"
                + "-fx-border-width: 0 0 1 0;");
        StackPane logoCircle = makeCircle("L", 30, "#FF69B4");
        VBox logoText = new VBox(1);
        Label hotelName = new Label("Lotus Laverne");
        hotelName.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Label hotelSub = new Label("Hotel Management");
        hotelSub.setStyle("-fx-font-size: 9px; -fx-text-fill: #8C8C8C;");
        logoText.getChildren().addAll(hotelName, hotelSub);
        logoSection.getChildren().addAll(logoCircle, logoText);

        // Nav list
        VBox navBox = new VBox(0);
        navBox.setPadding(new Insets(4, 0, 4, 0));

        int navIdx = 0;
        int sectionPtr = 0;
        VBox currentSectionContent = null;

        for (int i = 0; i < ALL_NAV.length; i++) {
            NavEntry entry = ALL_NAV[i];
            if (entry.quanLyOnly() && !isQuanLy) continue;

            while (sectionPtr < SECTION_BREAKS.length && SECTION_BREAKS[sectionPtr] == i) {
                int sp = sectionPtr;
                String sectionLabel = SECTION_LABELS[sectionPtr];
                String borderStyle = sectionPtr > 0
                        ? "-fx-border-color: #F0F0F0 transparent transparent transparent; -fx-border-width: 1 0 0 0;"
                        : "";

                HBox sectionHeader = new HBox(6);
                sectionHeader.setAlignment(Pos.CENTER_LEFT);
                sectionHeader.setPadding(new Insets(8, 12, 8, 14));
                sectionHeader.setStyle("-fx-cursor: hand; -fx-background-color: transparent;" + borderStyle);

                Label sectionLbl = new Label(sectionLabel);
                sectionLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #595959; -fx-font-weight: bold;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label arrowLbl = new Label("▾");
                arrowLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #AAAAAA;");
                sectionArrows.put(sp, arrowLbl);
                sectionExpanded.put(sp, true);

                sectionHeader.getChildren().addAll(sectionLbl, spacer, arrowLbl);

                VBox sectionContent = new VBox(0);
                sectionContentMap.put(sp, sectionContent);
                currentSectionContent = sectionContent;

                sectionHeader.setOnMouseEntered(e ->
                        sectionHeader.setStyle("-fx-cursor: hand; -fx-background-color: #F5F5F5;" + borderStyle));
                sectionHeader.setOnMouseExited(e ->
                        sectionHeader.setStyle("-fx-cursor: hand; -fx-background-color: transparent;" + borderStyle));
                sectionHeader.setOnMouseClicked(e -> toggleSection(sp));

                navBox.getChildren().addAll(sectionHeader, sectionContent);
                sectionPtr++;
            }

            HBox item = buildNavItem(navIdx, i, entry.label());
            navItems.add(item);
            if (currentSectionContent != null) {
                currentSectionContent.getChildren().add(item);
            } else {
                navBox.getChildren().add(item);
            }
            navIdx++;
        }

        ScrollPane navScroll = new ScrollPane(navBox);
        navScroll.setFitToWidth(true);
        navScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        navScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        navScroll.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(navScroll, Priority.ALWAYS);

        VBox userSection = buildUserSection();
        sidebar.getChildren().addAll(logoSection, navScroll, userSection);
        return sidebar;
    }

    private HBox buildNavItem(int navIdx, int allNavIdx, String label) {
        HBox item = new HBox(8);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(7, 16, 7, 24));
        item.setPrefWidth(200);
        item.setStyle("-fx-cursor: hand; -fx-background-color: transparent;");

        Label bulletLbl = new Label("○");
        bulletLbl.setStyle("-fx-font-size: 8px; -fx-text-fill: #CCCCCC; -fx-min-width: 12px;");
        Label textLbl = new Label(label);
        textLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #595959;");
        textLbl.setWrapText(false);

        item.getChildren().addAll(bulletLbl, textLbl);
        item.setUserData(allNavIdx);
        item.setOnMouseClicked(e -> navigate(navIdx));
        item.setOnMouseEntered(e -> {
            if (navItems.indexOf(item) != activeIndex)
                item.setStyle("-fx-cursor: hand; -fx-background-color: #F5F5F5;");
        });
        item.setOnMouseExited(e -> {
            if (navItems.indexOf(item) != activeIndex)
                item.setStyle("-fx-cursor: hand; -fx-background-color: transparent;");
        });

        return item;
    }

    private void toggleSection(int sp) {
        boolean newExpanded = !sectionExpanded.getOrDefault(sp, true);
        sectionExpanded.put(sp, newExpanded);

        VBox content = sectionContentMap.get(sp);
        if (content != null) {
            content.setVisible(newExpanded);
            content.setManaged(newExpanded);
        }
        Label arrow = sectionArrows.get(sp);
        if (arrow != null) {
            arrow.setText(newExpanded ? "▾" : "▸");
        }
    }

    private void expandSectionForIndex(int navIdx) {
        if (navIdx < 0 || navIdx >= navItems.size()) return;
        HBox targetItem = navItems.get(navIdx);
        for (Map.Entry<Integer, VBox> e : sectionContentMap.entrySet()) {
            if (e.getValue().getChildren().contains(targetItem)) {
                int sp = e.getKey();
                if (!sectionExpanded.getOrDefault(sp, true)) toggleSection(sp);
                return;
            }
        }
    }

    private VBox buildUserSection() {
        VBox section = new VBox(5);
        section.setPadding(new Insets(10, 14, 10, 14));
        section.setStyle("-fx-border-color: #F0F2F5 transparent transparent transparent;"
                + "-fx-border-width: 1 0 0 0;");

        HBox userRow = new HBox(8);
        userRow.setAlignment(Pos.CENTER_LEFT);
        StackPane avatar = UiUtils.makeAvatarCircle(username, 26);
        VBox nameBox = new VBox(1);
        Label nameLabel = new Label(username);
        nameLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Label roleLabel = new Label(vaiTro);
        roleLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #8C8C8C;");
        nameBox.getChildren().addAll(nameLabel, roleLabel);
        userRow.getChildren().addAll(avatar, nameBox);

        Button logoutBtn = new Button("Đăng Xuất");
        logoutBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #FF4D4F;"
                + "-fx-border-color: #FF4D4F; -fx-border-width: 1; -fx-border-radius: 6;"
                + "-fx-background-radius: 6; -fx-font-size: 11px; -fx-padding: 3 10;"
                + "-fx-cursor: hand;");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> new LoginView(stage).show());

        section.getChildren().addAll(userRow, logoutBtn);
        return section;
    }

    // ------------------------------------------------------------------ HEADER
    private HBox buildHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 24, 0, 24));
        header.setPrefHeight(54);
        header.setMinHeight(54);
        header.setMaxHeight(54);
        header.setStyle("-fx-background-color: #FFFFFF;"
                + "-fx-border-color: transparent transparent #E8E8E8 transparent;"
                + "-fx-border-width: 0 0 1 0;");

        VBox leftBox = new VBox(2);
        leftBox.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label("Lotus Laverne Hotel Management");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #C0392B;");
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Label dateLabel = new Label("Hôm nay: " + dateStr);
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #8C8C8C;");
        leftBox.getChildren().addAll(titleLabel, dateLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label bell = new Label("🔔");
        bell.setStyle("-fx-font-size: 15px; -fx-cursor: hand; -fx-padding: 6;");

        StackPane avatar = UiUtils.makeAvatarCircle(username, 30);

        Label userNameLbl = new Label(username);
        userNameLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");

        Label roleBadge = new Label(isQuanLy ? "Quản Lý" : "Lễ Tân");
        roleBadge.setStyle("-fx-background-color: " + (isQuanLy ? "#E6F4FF" : "#F6FFED")
                + "; -fx-text-fill: " + (isQuanLy ? "#1890FF" : "#52C41A")
                + "; -fx-padding: 2 8 2 8; -fx-background-radius: 10;"
                + " -fx-font-size: 11px; -fx-font-weight: bold;");

        HBox rightBox = new HBox(8);
        rightBox.setAlignment(Pos.CENTER);
        rightBox.getChildren().addAll(bell, avatar, userNameLbl, roleBadge);

        header.getChildren().addAll(leftBox, spacer, rightBox);
        return header;
    }

    // ------------------------------------------------------------------ NAVIGATION
    private void navigate(int navIdx) {
        navigateToView(resolveKey(navIdx), null);
        activeIndex = navIdx;
        updateNavStyles();
    }

    private String resolveKey(int navIdx) {
        if (navIdx < 0 || navIdx >= navItems.size()) navIdx = 0;
        int allIdx = (int) navItems.get(navIdx).getUserData();
        return ALL_NAV[allIdx].key();
    }

    public void navigateToView(String key, String prefill) {
        for (int i = 0; i < navItems.size(); i++) {
            int allIdx = (int) navItems.get(i).getUserData();
            if (ALL_NAV[allIdx].key().equals(key)) {
                activeIndex = i;
                break;
            }
        }
        expandSectionForIndex(activeIndex);
        updateNavStyles();

        Node view = switch (key) {
            case "dashboard"   -> new DashboardView().build();
            case "phong"       -> new PhongView(this).build();
            case "housekeeping"-> new HousekeepingView().build();
            case "datphong"    -> prefill != null ? new DatPhongView(prefill).build() : new DatPhongView().build();
            case "checkin"     -> new CheckInView().build();
            case "checkout"    -> prefill != null ? new CheckoutView(prefill).build() : new CheckoutView().build();
            case "dichvuphong" -> new DichVuPhongView().build();
            case "yeucau"      -> new YeuCauKhachView().build();
            case "khach"       -> new KhachView().build();
            case "baocao"      -> new BaoCaoView().build();
            case "nhanvien"    -> new NhanVienView().build();
            case "chamcong"    -> new ChamCongView().build();
            case "taikhoan"    -> new TaiKhoanView(username, vaiTro).build();
            case "caidat"      -> new CaiDatView().build();
            case "doiphong"    -> new DoiPhongView().build();
            case "luutru"      -> new LuuTruView().build();
            case "hoadon"      -> new HoaDonView().build();
            case "phieuthu"    -> new PhieuThuView().build();
            case "khuyenmai"   -> new KhuyenMaiView().build();
            case "banggia"     -> new BangGiaView().build();
            case "dichvu"      -> new DichVuView().build();
            case "thietbi"     -> new ThietBiView().build();
            case "nhansu"      -> new NhanSuView().build();
            case "cauhinh"     -> new CauHinhView().build();
            default            -> new DashboardView().build();
        };

        contentArea.getChildren().setAll(view);
    }

    private void updateNavStyles() {
        for (int i = 0; i < navItems.size(); i++) {
            HBox item = navItems.get(i);
            Label bulletLbl = (Label) item.getChildren().get(0);
            Label textLbl   = (Label) item.getChildren().get(1);
            if (i == activeIndex) {
                item.setStyle("-fx-cursor: hand;"
                        + "-fx-background-color: #E6F4FF;"
                        + "-fx-border-color: transparent transparent transparent #1890FF;"
                        + "-fx-border-width: 0 0 0 3;");
                bulletLbl.setStyle("-fx-font-size: 8px; -fx-text-fill: #1890FF; -fx-min-width: 12px;");
                textLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #1890FF; -fx-font-weight: bold;");
            } else {
                item.setStyle("-fx-cursor: hand; -fx-background-color: transparent;");
                bulletLbl.setStyle("-fx-font-size: 8px; -fx-text-fill: #CCCCCC; -fx-min-width: 12px;");
                textLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #595959;");
            }
        }
    }

    // ------------------------------------------------------------------ HELPERS
    private static StackPane makeCircle(String letter, double size, String color) {
        StackPane circle = new StackPane();
        circle.setStyle("-fx-background-color: " + color + "; -fx-background-radius: " + (size / 2) + ";"
                + "-fx-min-width: " + size + "px; -fx-min-height: " + size + "px;"
                + "-fx-max-width: " + size + "px; -fx-max-height: " + size + "px;");
        Text t = new Text(letter);
        t.setStyle("-fx-font-size: " + (size * 0.5) + "px; -fx-font-weight: bold; -fx-fill: white;");
        circle.getChildren().add(t);
        return circle;
    }

    static StackPane makeAvatarCircle(String name, double radius) {
        return UiUtils.makeAvatarCircle(name, radius);
    }

    static String pickColor(String name) {
        return UiUtils.pickColor(name);
    }
}
