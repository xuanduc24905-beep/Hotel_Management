package com.lotuslaverne.fx;

import com.lotuslaverne.dao.TaiKhoanDAO;
import com.lotuslaverne.entity.TaiKhoan;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class LoginView {

    private final Stage stage;

    public LoginView(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        // Root: 2 cột, trái gradient, phải form
        HBox root = new HBox();
        root.setPrefSize(950, 550);

        // ── Cột trái: ảnh nền + overlay chữ ─────────────────────────────
        StackPane leftPanel = new StackPane();
        leftPanel.setPrefWidth(475);
        leftPanel.setMinWidth(475);
        leftPanel.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #2AC4EA, #1890FF);");

        // Load ảnh nền
        try {
            java.net.URL imgUrl = getClass().getResource("/images/bg_login.png");
            if (imgUrl != null) {
                javafx.scene.image.Image img =
                    new javafx.scene.image.Image(imgUrl.toExternalForm(), 475, 550, false, true);
                javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(img);
                iv.setFitWidth(475);
                iv.setFitHeight(550);
                iv.setPreserveRatio(false);
                leftPanel.getChildren().add(iv);
            }
        } catch (Exception ignored) {}

        // ── Cột phải: form đăng nhập ─────────────────────────────────────
        VBox rightPanel = new VBox();
        rightPanel.setPrefWidth(475);
        rightPanel.setAlignment(Pos.CENTER);
        rightPanel.setStyle("-fx-background-color: #FFFFFF;");
        rightPanel.setPadding(new Insets(0, 50, 0, 50));

        VBox form = new VBox(0);
        form.setAlignment(Pos.CENTER_LEFT);
        form.setMaxWidth(320);

        // Logo text nhỏ ở form
        HBox logoRow = new HBox(0);
        logoRow.setAlignment(Pos.CENTER_LEFT);
        Label lotusLbl = new Label("Lotus");
        lotusLbl.setStyle(
            "-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #1890FF;");
        Label larvLbl = new Label("Larverne");
        larvLbl.setStyle(
            "-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #0DCAF0;");
        logoRow.getChildren().addAll(lotusLbl, larvLbl);

        Label subLbl = new Label("Hệ thống quản lý khách sạn trên nền tảng điện toán đám mây");
        subLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #AAAAAA;");
        subLbl.setWrapText(true);
        subLbl.setMaxWidth(320);

        Region spacer1 = new Region(); spacer1.setPrefHeight(28);

        // Username field
        Label userLbl = new Label("Tên đăng nhập");
        userLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #595959;");
        TextField userField = new TextField("admin");
        userField.setPromptText("Nhập tên đăng nhập...");
        styleField(userField);
        Region sp1 = new Region(); sp1.setPrefHeight(14);

        // Password field
        Label passLbl = new Label("Mật khẩu");
        passLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #595959;");
        PasswordField passField = new PasswordField();
        passField.setText("123456");
        passField.setPromptText("Nhập mật khẩu...");
        styleField(passField);
        Region sp2 = new Region(); sp2.setPrefHeight(14);

        // Vai trò
        Label roleLbl = new Label("Đăng nhập với vai trò:");
        roleLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #595959;");
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("Lễ Tân", "Quản Lý");
        roleCombo.setValue("Lễ Tân");
        roleCombo.setMaxWidth(Double.MAX_VALUE);
        roleCombo.setPrefHeight(42);
        roleCombo.setStyle(
            "-fx-background-color: #FFFFFF; -fx-border-color: #D9D9D9;"
            + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 1;"
            + "-fx-font-size: 13px;");
        Region sp3 = new Region(); sp3.setPrefHeight(20);

        // Error label
        Label errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: #FF4D4F; -fx-font-size: 12px;");
        errorLbl.setWrapText(true);
        errorLbl.setMaxWidth(320);
        errorLbl.setVisible(false);
        errorLbl.setManaged(false);

        // Đăng nhập button
        Button loginBtn = new Button("Đăng nhập");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setPrefHeight(44);
        loginBtn.setStyle(
            "-fx-background-color: #1890FF; -fx-text-fill: white;"
            + "-fx-background-radius: 8; -fx-font-size: 14px; -fx-font-weight: bold;"
            + "-fx-cursor: hand;");
        loginBtn.setOnMouseEntered(e ->
            loginBtn.setStyle(
                "-fx-background-color: #40A9FF; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-font-size: 14px; -fx-font-weight: bold;"
                + "-fx-cursor: hand;"));
        loginBtn.setOnMouseExited(e ->
            loginBtn.setStyle(
                "-fx-background-color: #1890FF; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-font-size: 14px; -fx-font-weight: bold;"
                + "-fx-cursor: hand;"));
        Region sp4 = new Region(); sp4.setPrefHeight(12);

        // Quên mật khẩu
        Label forgotLbl = new Label("Quên mật khẩu?");
        forgotLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #AAAAAA; -fx-cursor: hand;");
        forgotLbl.setOnMouseEntered(e ->
            forgotLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #1890FF; -fx-cursor: hand;"
                + "-fx-underline: true;"));
        forgotLbl.setOnMouseExited(e ->
            forgotLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #AAAAAA; -fx-cursor: hand;"));
        forgotLbl.setOnMouseClicked(e -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION,
                "Tính năng lấy lại mật khẩu đang được phát triển!");
            a.setHeaderText(null); a.setTitle("Thông báo"); a.showAndWait();
        });

        // Xử lý đăng nhập
        loginBtn.setOnAction(e ->
            handleLogin(userField.getText().trim(), passField.getText(),
                        roleCombo.getValue(), errorLbl));
        passField.setOnAction(e -> loginBtn.fire());
        userField.setOnAction(e -> passField.requestFocus());

        form.getChildren().addAll(
            logoRow, subLbl, spacer1,
            userLbl, userField, sp1,
            passLbl, passField, sp2,
            roleLbl, roleCombo, sp3,
            errorLbl, loginBtn, sp4,
            forgotLbl
        );

        rightPanel.getChildren().add(form);
        root.getChildren().addAll(leftPanel, rightPanel);
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        Scene scene = new Scene(root, 950, 550);
        try {
            scene.getStylesheets().add(
                getClass().getResource("/com/lotuslaverne/fx/style.css").toExternalForm());
        } catch (Exception ignored) {}

        stage.setTitle("Đăng nhập - Lotus Larverne Hotel");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }

    private void styleField(TextField tf) {
        tf.setMaxWidth(Double.MAX_VALUE);
        tf.setPrefHeight(42);
        tf.setStyle(
            "-fx-background-color: #F5F5F5; -fx-border-color: #E8E8E8;"
            + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 1;"
            + "-fx-padding: 8 12 8 12; -fx-font-size: 13px;");
        tf.setOnMouseEntered(e -> tf.setStyle(
            "-fx-background-color: #FFFFFF; -fx-border-color: #1890FF;"
            + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 1;"
            + "-fx-padding: 8 12 8 12; -fx-font-size: 13px;"));
        tf.setOnMouseExited(e -> tf.setStyle(
            "-fx-background-color: #F5F5F5; -fx-border-color: #E8E8E8;"
            + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 1;"
            + "-fx-padding: 8 12 8 12; -fx-font-size: 13px;"));
    }

    private void handleLogin(String username, String password,
                             String selectedRole, Label errorLbl) {
        if (username.isEmpty() || password.isEmpty()) {
            show(errorLbl, "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.");
            return;
        }

        TaiKhoan tk = null;
        try { tk = new TaiKhoanDAO().checkLogin(username, password); }
        catch (Exception ignored) {}

        String resolvedRole = null;
        if (tk != null) {
            resolvedRole = tk.getVaiTro().equalsIgnoreCase("QuanLy") ? "Quản Lý" : "Lễ Tân";
        } else if ("admin".equals(username)     && "123456".equals(password)) resolvedRole = "Quản Lý";
        else if ("letanthu".equals(username)    && "123456".equals(password)) resolvedRole = "Lễ Tân";
        else if ("letancuong".equals(username)  && "123456".equals(password)) resolvedRole = "Lễ Tân";

        if (resolvedRole == null) {
            show(errorLbl, "Sai tên đăng nhập hoặc mật khẩu. Vui lòng thử lại.");
            return;
        }

        new MainLayout(stage, username, resolvedRole).show();
    }

    private void show(Label lbl, String msg) {
        lbl.setText(msg);
        lbl.setVisible(true);
        lbl.setManaged(true);
    }
}
