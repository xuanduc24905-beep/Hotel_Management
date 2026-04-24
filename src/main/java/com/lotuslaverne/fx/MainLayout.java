package com.lotuslaverne.fx;

import com.lotuslaverne.fx.views.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

    private StackPane contentArea;
    private final List<HBox> navItems = new ArrayList<>();
    private int activeIndex = 0;

    private static final String[][] NAV = {
        {"⊞",  "Tổng Quan"},
        {"🚪", "Quản Lý Phòng"},
        {"👤", "Quản Lý Khách"},
        {"👥", "Quản Lý Nhân Viên"},
        {"📊", "Báo Cáo"},
        {"⚙",  "Cài Đặt"}
    };

    public MainLayout(Stage stage, String username, String vaiTro) {
        this.stage = stage;
        this.username = username;
        this.vaiTro = vaiTro;
    }

    public void show() {
        BorderPane root = new BorderPane();

        // Build parts
        VBox sidebar = buildSidebar();
        HBox header  = buildHeader();
        contentArea  = new StackPane();
        contentArea.getStyleClass().add("content-area");
        contentArea.setStyle("-fx-background-color: #F0F2F5;");

        root.setLeft(sidebar);
        root.setTop(header);
        root.setCenter(contentArea);

        // Show dashboard by default
        navigate(0);

        Scene scene = new Scene(root, 1280, 780);
        scene.getStylesheets().add(
                getClass().getResource("/com/lotuslaverne/fx/style.css").toExternalForm());

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
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");
        sidebar.setStyle("-fx-background-color: #FFFFFF;"
                + "-fx-border-color: transparent #E8E8E8 transparent transparent;"
                + "-fx-border-width: 0 1 0 0;");
        sidebar.setPrefWidth(190);
        sidebar.setMinWidth(190);
        sidebar.setMaxWidth(190);

        // Logo section
        HBox logoSection = new HBox(10);
        logoSection.setAlignment(Pos.CENTER_LEFT);
        logoSection.setPadding(new Insets(18, 16, 14, 16));
        logoSection.setStyle("-fx-border-color: transparent transparent #F0F2F5 transparent;"
                + "-fx-border-width: 0 0 1 0;");

        StackPane logoCircle = new StackPane();
        logoCircle.setStyle("-fx-background-color: #FF69B4; -fx-background-radius: 18;"
                + "-fx-min-width: 36px; -fx-min-height: 36px;"
                + "-fx-max-width: 36px; -fx-max-height: 36px;");
        Text logoTxt = new Text("L");
        logoTxt.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: white;");
        logoCircle.getChildren().add(logoTxt);

        VBox logoText = new VBox(2);
        Label hotelName = new Label("Lotus Laverne");
        hotelName.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Label hotelSub = new Label("Hotel Management");
        hotelSub.setStyle("-fx-font-size: 10px; -fx-text-fill: #8C8C8C;");
        logoText.getChildren().addAll(hotelName, hotelSub);

        logoSection.getChildren().addAll(logoCircle, logoText);

        // Section label
        Label menuLabel = new Label("MENU");
        menuLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #BFBFBF; -fx-font-weight: bold;"
                + "-fx-padding: 14 16 4 16;");

        // Nav items
        VBox navBox = new VBox(0);
        for (int i = 0; i < NAV.length; i++) {
            HBox item = buildNavItem(i, NAV[i][0], NAV[i][1]);
            navItems.add(item);
            navBox.getChildren().add(item);
        }

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // User info at bottom
        VBox userSection = buildUserSection();

        sidebar.getChildren().addAll(logoSection, menuLabel, navBox, spacer, userSection);
        return sidebar;
    }

    private HBox buildNavItem(int index, String icon, String label) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(10, 16, 10, 16));
        item.setPrefWidth(190);
        item.setStyle("-fx-cursor: hand; -fx-background-color: transparent;");

        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 15px; -fx-text-fill: #595959; -fx-min-width: 20px;");
        Label textLbl = new Label(label);
        textLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #595959;");

        item.getChildren().addAll(iconLbl, textLbl);

        item.setOnMouseClicked(e -> navigate(index));
        item.setOnMouseEntered(e -> {
            if (navItems.indexOf(item) != activeIndex) {
                item.setStyle("-fx-cursor: hand; -fx-background-color: #F5F5F5;");
            }
        });
        item.setOnMouseExited(e -> {
            if (navItems.indexOf(item) != activeIndex) {
                item.setStyle("-fx-cursor: hand; -fx-background-color: transparent;");
            }
        });

        return item;
    }

    private VBox buildUserSection() {
        VBox section = new VBox(4);
        section.setPadding(new Insets(12, 16, 12, 16));
        section.setStyle("-fx-border-color: #F0F2F5 transparent transparent transparent;"
                + "-fx-border-width: 1 0 0 0;");

        // Avatar + name row
        HBox userRow = new HBox(8);
        userRow.setAlignment(Pos.CENTER_LEFT);

        StackPane avatar = makeAvatarCircle(username, 28);

        VBox nameBox = new VBox(1);
        Label nameLabel = new Label(username);
        nameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Label roleLabel = new Label(vaiTro);
        roleLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #8C8C8C;");
        nameBox.getChildren().addAll(nameLabel, roleLabel);

        userRow.getChildren().addAll(avatar, nameBox);

        // Logout button
        Button logoutBtn = new Button("Đăng Xuất");
        logoutBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #FF4D4F;"
                + "-fx-border-color: #FF4D4F; -fx-border-width: 1; -fx-border-radius: 6;"
                + "-fx-background-radius: 6; -fx-font-size: 11px; -fx-padding: 4 10 4 10;"
                + "-fx-cursor: hand;");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> {
            LoginView login = new LoginView(stage);
            login.show();
        });

        section.getChildren().addAll(userRow, logoutBtn);
        return section;
    }

    // ------------------------------------------------------------------ HEADER
    private HBox buildHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 24, 0, 24));
        header.setPrefHeight(56);
        header.setMinHeight(56);
        header.setMaxHeight(56);
        header.setStyle("-fx-background-color: #FFFFFF;"
                + "-fx-border-color: transparent transparent #E8E8E8 transparent;"
                + "-fx-border-width: 0 0 1 0;");

        // Left: title + date
        VBox leftBox = new VBox(2);
        leftBox.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label("Lotus Laverne Hotel Management");
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #C0392B;");
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Label dateLabel = new Label("Hôm nay: " + dateStr);
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #8C8C8C;");
        leftBox.getChildren().addAll(titleLabel, dateLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Right: bell + user
        Label bell = new Label("🔔");
        bell.setStyle("-fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 6;");

        StackPane avatar = makeAvatarCircle(username, 32);

        Label userNameLbl = new Label(username);
        userNameLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");

        Label roleLbl = new Label("[" + vaiTro + "]");
        roleLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #8C8C8C;");

        Label chevron = new Label("▾");
        chevron.setStyle("-fx-font-size: 10px; -fx-text-fill: #8C8C8C;");

        HBox rightBox = new HBox(8);
        rightBox.setAlignment(Pos.CENTER);
        rightBox.getChildren().addAll(bell, avatar, userNameLbl, roleLbl, chevron);

        header.getChildren().addAll(leftBox, spacer, rightBox);
        return header;
    }

    // ------------------------------------------------------------------ NAVIGATION
    private void navigate(int index) {
        activeIndex = index;
        updateNavStyles();

        Node view;
        switch (index) {
            case 0 -> view = new DashboardView().build();
            case 1 -> view = new PhongView().build();
            case 2 -> view = new KhachView().build();
            case 3 -> view = new NhanVienView().build();
            case 4 -> view = new BaoCaoView().build();
            case 5 -> view = new CaiDatView().build();
            default -> view = new DashboardView().build();
        }

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
                iconLbl.setStyle("-fx-font-size: 15px; -fx-text-fill: #1890FF; -fx-min-width: 20px;");
                textLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #1890FF; -fx-font-weight: bold;");
            } else {
                item.setStyle("-fx-cursor: hand; -fx-background-color: transparent;");
                iconLbl.setStyle("-fx-font-size: 15px; -fx-text-fill: #595959; -fx-min-width: 20px;");
                textLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #595959;");
            }
        }
    }

    // ------------------------------------------------------------------ HELPERS
    static StackPane makeAvatarCircle(String name, double radius) {
        return UiUtils.makeAvatarCircle(name, radius);
    }

    static String pickColor(String name) {
        return UiUtils.pickColor(name);
    }
}
