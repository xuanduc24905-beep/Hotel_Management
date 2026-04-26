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
import java.util.List;

public class MainLayout {

    private final Stage stage;
    private final String username;
    private final String vaiTro;
    private final boolean isQuanLy;

    private StackPane contentArea;
    private final List<HBox> navItems = new ArrayList<>();
    private int activeIndex = 0;

    // Nav entry: { icon, label, viewKey, quanLyOnly }
    // viewKey used in navigate() switch
    private record NavEntry(String icon, String label, String key, boolean quanLyOnly) {}

    private static final String[][] SECTIONS = {
        {"TỔNG QUAN"},
        {"QUẢN LÝ PHÒNG"},
        {"LƯU TRÚ"},
        {"TÀI CHÍNH"},
        {"NHÂN SỰ"},
        {"HỆ THỐNG"},
    };

    // All nav items in display order
    private final NavEntry[] ALL_NAV = {
        new NavEntry("⊞",  "Tổng Quan",         "dashboard",    false),  // 0
        new NavEntry("🚪", "Quản Lý Phòng",      "phong",        false),  // 1
        new NavEntry("🧹", "Buồng Phòng",        "housekeeping", false),  // 2
        new NavEntry("💰", "Bảng Giá",           "banggia",      false),  // 3
        new NavEntry("🛎", "Dịch Vụ",            "dichvu",       false),  // 4
        new NavEntry("🔌", "Thiết Bị",           "thietbi",      false),  // 5
        new NavEntry("📋", "Đặt Phòng",          "datphong",     false),  // 6
        new NavEntry("✅", "Check-in",           "checkin",      false),  // 7
        new NavEntry("🔄", "Đổi Phòng",          "doiphong",     false),  // 8
        new NavEntry("🍽",  "Dịch Vụ Phòng",     "dichvuphong",  false),  // 9

        new NavEntry("💳", "Thanh Toán",         "thanhtoan",    false),  // 10
        new NavEntry("🧾", "Hóa Đơn",            "hoadon",       true),   // 11
        new NavEntry("💵", "Phiếu Thu Cọc",      "phieuthu",     false),  // 12
        new NavEntry("🎁", "Khuyến Mãi",         "khuyenmai",    true),   // 13
        new NavEntry("👤", "Quản Lý Khách",      "khach",        false),  // 14
        new NavEntry("👥", "Quản Lý Nhân Viên",  "nhanvien",     true),   // 15
        new NavEntry("📊", "Báo Cáo",            "baocao",       false),  // 16
        new NavEntry("🔑", "Tài Khoản",          "taikhoan",     true),   // 17
        new NavEntry("⚙",  "Cài Đặt",            "caidat",       false),  // 18
    };

    // Section break BEFORE which index (0-based in ALL_NAV)
    private static final int[] SECTION_BREAKS = {0, 1, 6, 10, 14, 16};
    private static final String[] SECTION_LABELS = {
        "TỔNG QUAN", "QUẢN LÝ PHÒNG", "LƯU TRÚ",
        "TÀI CHÍNH", "NHÂN SỰ", "HỆ THỐNG"
    };

    public MainLayout(Stage stage, String username, String vaiTro) {
        this.stage    = stage;
        this.username = username;
        this.vaiTro   = vaiTro;
        this.isQuanLy = "Quản Lý".equals(vaiTro) || "QuanLy".equals(vaiTro);
    }

    public void show() {
        BorderPane root = new BorderPane();

        VBox sidebar = buildSidebar();
        HBox header  = buildHeader();
        contentArea  = new StackPane();
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
        HBox logoSection = new HBox(10);
        logoSection.setAlignment(Pos.CENTER_LEFT);
        logoSection.setPadding(new Insets(16, 16, 12, 16));
        logoSection.setStyle("-fx-border-color: transparent transparent #F0F2F5 transparent;"
                + "-fx-border-width: 0 0 1 0;");
        StackPane logoCircle = makeCircle("L", 34, "#FF69B4");
        VBox logoText = new VBox(2);
        Label hotelName = new Label("Lotus Laverne");
        hotelName.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Label hotelSub = new Label("Hotel Management");
        hotelSub.setStyle("-fx-font-size: 10px; -fx-text-fill: #8C8C8C;");
        logoText.getChildren().addAll(hotelName, hotelSub);
        logoSection.getChildren().addAll(logoCircle, logoText);

        // Nav list — scrollable
        VBox navBox = new VBox(0);
        navBox.setPadding(new Insets(6, 0, 6, 0));

        int navIdx = 0;
        int sectionPtr = 0;
        for (int i = 0; i < ALL_NAV.length; i++) {
            NavEntry entry = ALL_NAV[i];
            if (entry.quanLyOnly() && !isQuanLy) continue;

            // Section header if needed
            while (sectionPtr < SECTION_BREAKS.length && SECTION_BREAKS[sectionPtr] == i) {
                Label sectionLbl = new Label(SECTION_LABELS[sectionPtr]);
                sectionLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #BFBFBF;"
                        + "-fx-font-weight: bold; -fx-padding: 10 16 2 16;");
                navBox.getChildren().add(sectionLbl);
                sectionPtr++;
            }

            HBox item = buildNavItem(navIdx, i, entry.icon(), entry.label());
            navItems.add(item);
            navBox.getChildren().add(item);
            navIdx++;
        }

        ScrollPane navScroll = new ScrollPane(navBox);
        navScroll.setFitToWidth(true);
        navScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        navScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        navScroll.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(navScroll, Priority.ALWAYS);

        // User section at bottom
        VBox userSection = buildUserSection();

        sidebar.getChildren().addAll(logoSection, navScroll, userSection);
        return sidebar;
    }

    private HBox buildNavItem(int navIdx, int allNavIdx, String icon, String label) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(9, 16, 9, 16));
        item.setPrefWidth(200);
        item.setStyle("-fx-cursor: hand; -fx-background-color: transparent;");

        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #595959; -fx-min-width: 20px;");
        Label textLbl = new Label(label);
        textLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #595959;");
        textLbl.setWrapText(false);

        item.getChildren().addAll(iconLbl, textLbl);

        // Store the allNavIdx so navigate() can use the key
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

    private VBox buildUserSection() {
        VBox section = new VBox(6);
        section.setPadding(new Insets(12, 16, 12, 16));
        section.setStyle("-fx-border-color: #F0F2F5 transparent transparent transparent;"
                + "-fx-border-width: 1 0 0 0;");

        HBox userRow = new HBox(8);
        userRow.setAlignment(Pos.CENTER_LEFT);
        StackPane avatar = UiUtils.makeAvatarCircle(username, 28);
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
                + "-fx-background-radius: 6; -fx-font-size: 11px; -fx-padding: 4 10;"
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

        // Role badge
        boolean ql = isQuanLy;
        Label roleBadge = new Label(ql ? "Quản Lý" : "Lễ Tân");
        roleBadge.setStyle("-fx-background-color: " + (ql ? "#E6F4FF" : "#F6FFED")
                + "; -fx-text-fill: " + (ql ? "#1890FF" : "#52C41A")
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

    /**
     * Navigate to a view by key, with optional pre-fill data.
     * Called from PhongView (and other views) to trigger cross-screen navigation.
     * @param key       the view key (e.g. "datphong", "thanhtoan")
     * @param prefill   pre-fill string passed to the view (e.g. maPhong or maPDP), or null
     */
    public void navigateToView(String key, String prefill) {
        // Sync sidebar highlight
        for (int i = 0; i < navItems.size(); i++) {
            int allIdx = (int) navItems.get(i).getUserData();
            if (ALL_NAV[allIdx].key().equals(key)) {
                activeIndex = i;
                break;
            }
        }
        updateNavStyles();

        Node view = switch (key) {
            case "dashboard"    -> new DashboardView().build();
            case "phong"        -> new PhongView(this).build();
            case "housekeeping" -> new HousekeepingView().build();
            case "banggia"      -> new BangGiaView().build();
            case "dichvu"       -> new DichVuView().build();
            case "thietbi"      -> new ThietBiView().build();
            case "datphong"     -> prefill != null
                                    ? new DatPhongView(prefill).build()
                                    : new DatPhongView().build();
            case "checkin"      -> new CheckInView().build();
            case "doiphong"     -> new DoiPhongView().build();
            case "dichvuphong"  -> new DichVuPhongView().build();
            case "thanhtoan"    -> prefill != null
                                    ? new ThanhToanView(prefill).build()
                                    : new ThanhToanView().build();
            case "hoadon"       -> new HoaDonView().build();
            case "phieuthu"     -> new PhieuThuView().build();
            case "khuyenmai"    -> new KhuyenMaiView().build();
            case "khach"        -> new KhachView().build();
            case "nhanvien"     -> new NhanVienView().build();
            case "baocao"       -> new BaoCaoView().build();
            case "taikhoan"     -> new TaiKhoanView(username).build();
            case "caidat"       -> new CaiDatView().build();
            default             -> new DashboardView().build();
        };

        contentArea.getChildren().setAll(view);
    }

    private void updateNavStyles() {
        for (int i = 0; i < navItems.size(); i++) {
            HBox item = navItems.get(i);
            Label iconLbl = (Label) item.getChildren().get(0);
            Label textLbl = (Label) item.getChildren().get(1);
            if (i == activeIndex) {
                item.setStyle("-fx-cursor: hand;"
                        + "-fx-background-color: #E6F4FF;"
                        + "-fx-border-color: transparent transparent transparent #1890FF;"
                        + "-fx-border-width: 0 0 0 3;");
                iconLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #1890FF; -fx-min-width: 20px;");
                textLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #1890FF; -fx-font-weight: bold;");
            } else {
                item.setStyle("-fx-cursor: hand; -fx-background-color: transparent;");
                iconLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #595959; -fx-min-width: 20px;");
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
