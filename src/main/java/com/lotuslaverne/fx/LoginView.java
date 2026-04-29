package com.lotuslaverne.fx;

import com.lotuslaverne.dao.TaiKhoanDAO;
import com.lotuslaverne.entity.TaiKhoan;
import com.lotuslaverne.util.ConnectDB;
import com.lotuslaverne.util.PasswordUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
        TextField userField = new TextField();
        userField.setPromptText("Nhập tên đăng nhập...");
        styleField(userField);
        Region sp1 = new Region(); sp1.setPrefHeight(14);

        // Password field
        Label passLbl = new Label("Mật khẩu");
        passLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #595959;");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Nhập mật khẩu...");
        styleField(passField);
        Region sp2 = new Region(); sp2.setPrefHeight(20);

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
        forgotLbl.setOnMouseClicked(e -> openForgotPasswordDialog());

        // Xử lý đăng nhập
        loginBtn.setOnAction(e ->
            handleLogin(userField.getText().trim(), passField.getText(), errorLbl));
        passField.setOnAction(e -> loginBtn.fire());
        userField.setOnAction(e -> passField.requestFocus());

        form.getChildren().addAll(
            logoRow, subLbl, spacer1,
            userLbl, userField, sp1,
            passLbl, passField, sp2,
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

    private void handleLogin(String username, String password, Label errorLbl) {
        if (username.isEmpty() || password.isEmpty()) {
            show(errorLbl, "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.");
            return;
        }

        TaiKhoan tk = null;
        try { tk = new TaiKhoanDAO().checkLogin(username, password); }
        catch (Exception ex) {
            show(errorLbl, "Không thể kết nối cơ sở dữ liệu! Kiểm tra kết nối SQL Server.");
            return;
        }

        if (tk == null) {
            show(errorLbl, "Sai tên đăng nhập hoặc mật khẩu. Vui lòng thử lại.");
            return;
        }

        // Vai trò xác định từ DB, KHÔNG cho user tự chọn
        String resolvedRole = tk.getVaiTro().equalsIgnoreCase("QuanLy") ? "Quản Lý" : "Lễ Tân";
        new MainLayout(stage, username, resolvedRole).show();
    }

    private void show(Label lbl, String msg) {
        lbl.setText(msg);
        lbl.setVisible(true);
        lbl.setManaged(true);
    }

    /** Dialog quên mật khẩu: verify username + CCCD nhân viên → đặt mật khẩu mới */
    private void openForgotPasswordDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Quên Mật Khẩu");
        dialog.setResizable(false);

        VBox root = new VBox(14);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #FFFFFF;");

        Label title = new Label("🔑  Lấy Lại Mật Khẩu");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1890FF;");
        Label hint = new Label("Nhập tên đăng nhập và CCCD của nhân viên để xác minh.");
        hint.setStyle("-fx-font-size: 12px; -fx-text-fill: #8C8C8C;");
        hint.setWrapText(true);

        TextField txtUser = new TextField();
        txtUser.setPromptText("Tên đăng nhập");
        styleField(txtUser);
        TextField txtCCCD = new TextField();
        txtCCCD.setPromptText("CCCD/CMND nhân viên (12 số)");
        styleField(txtCCCD);

        Label step1Lbl = new Label("Bước 1: Xác minh danh tính");
        step1Lbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #595959;");

        // Phần đặt mật khẩu mới — ban đầu ẩn
        VBox newPassBox = new VBox(8);
        newPassBox.setVisible(false);
        newPassBox.setManaged(false);
        Label step2Lbl = new Label("Bước 2: Nhập mật khẩu mới");
        step2Lbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #52C41A;");
        PasswordField txtNewPass = new PasswordField();
        txtNewPass.setPromptText("Mật khẩu mới (tối thiểu 6 ký tự)");
        styleField(txtNewPass);
        PasswordField txtConfirm = new PasswordField();
        txtConfirm.setPromptText("Xác nhận mật khẩu mới");
        styleField(txtConfirm);
        newPassBox.getChildren().addAll(step2Lbl, txtNewPass, txtConfirm);

        Label statusLbl = new Label();
        statusLbl.setStyle("-fx-font-size: 12px;");
        statusLbl.setVisible(false);
        statusLbl.setManaged(false);
        statusLbl.setWrapText(true);

        Button btnVerify = new Button("Xác Minh");
        btnVerify.setStyle("-fx-background-color: #1890FF; -fx-text-fill: white;"
                + "-fx-background-radius: 6; -fx-padding: 8 20; -fx-cursor: hand; -fx-font-weight: bold;");

        Button btnReset = new Button("Đặt Lại Mật Khẩu");
        btnReset.setStyle("-fx-background-color: #52C41A; -fx-text-fill: white;"
                + "-fx-background-radius: 6; -fx-padding: 8 20; -fx-cursor: hand; -fx-font-weight: bold;");
        btnReset.setVisible(false);
        btnReset.setManaged(false);

        Button btnClose = new Button("Đóng");
        btnClose.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #595959;"
                + "-fx-background-radius: 6; -fx-padding: 8 20; -fx-cursor: hand;");
        btnClose.setOnAction(e -> dialog.close());

        // State: lưu maTaiKhoan sau khi xác minh OK
        String[] verifiedMaTK = {null};

        btnVerify.setOnAction(e -> {
            String user = txtUser.getText().trim();
            String cccd = txtCCCD.getText().trim();
            if (user.isEmpty() || cccd.isEmpty()) {
                showStatus(statusLbl, "Vui lòng điền đầy đủ thông tin!", "#FF4D4F");
                return;
            }
            String maTK = verifyUserAndCCCD(user, cccd);
            if (maTK == null) {
                showStatus(statusLbl, "Tên đăng nhập hoặc CCCD không khớp. Hãy liên hệ quản lý nếu bạn quên CCCD.", "#FF4D4F");
                return;
            }
            verifiedMaTK[0] = maTK;
            showStatus(statusLbl, "✓ Xác minh thành công! Mời nhập mật khẩu mới.", "#52C41A");
            txtUser.setEditable(false);
            txtCCCD.setEditable(false);
            btnVerify.setDisable(true);
            newPassBox.setVisible(true);
            newPassBox.setManaged(true);
            btnReset.setVisible(true);
            btnReset.setManaged(true);
        });

        btnReset.setOnAction(e -> {
            String np  = txtNewPass.getText();
            String np2 = txtConfirm.getText();
            if (np.length() < 6) {
                showStatus(statusLbl, "Mật khẩu mới phải có ít nhất 6 ký tự!", "#FF4D4F"); return;
            }
            if (!np.equals(np2)) {
                showStatus(statusLbl, "Hai mật khẩu không khớp!", "#FF4D4F"); return;
            }
            if (resetPassword(verifiedMaTK[0], np)) {
                Alert ok = new Alert(Alert.AlertType.INFORMATION,
                        "Đặt lại mật khẩu thành công!\nĐăng nhập lại bằng mật khẩu mới.");
                ok.setHeaderText(null); ok.setTitle("Hoàn Tất"); ok.showAndWait();
                dialog.close();
            } else {
                showStatus(statusLbl, "Lỗi cập nhật DB. Vui lòng thử lại.", "#FF4D4F");
            }
        });

        HBox btnRow = new HBox(10, btnClose, btnVerify, btnReset);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(title, hint, step1Lbl, txtUser, txtCCCD, newPassBox, statusLbl, btnRow);
        dialog.setScene(new Scene(root, 420, 460));
        dialog.showAndWait();
    }

    /** Verify username + CCCD bằng JOIN TaiKhoan ↔ NhanVien. Trả về maTaiKhoan nếu khớp. */
    private String verifyUserAndCCCD(String username, String cccd) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return null;
        String sql =
            "SELECT tk.maTaiKhoan FROM TaiKhoan tk " +
            "JOIN NhanVien nv ON nv.maNhanVien = tk.maNhanVien " +
            "WHERE tk.tenDangNhap = ? AND nv.cccd = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, username);
            pst.setString(2, cccd);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getString("maTaiKhoan");
            }
        } catch (Exception ignored) {}
        return null;
    }

    private boolean resetPassword(String maTaiKhoan, String newPassword) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try (PreparedStatement pst = con.prepareStatement(
                "UPDATE TaiKhoan SET matKhau = ? WHERE maTaiKhoan = ?")) {
            pst.setString(1, PasswordUtil.hash(newPassword));
            pst.setString(2, maTaiKhoan);
            return pst.executeUpdate() > 0;
        } catch (Exception ignored) { return false; }
    }

    private void showStatus(Label lbl, String msg, String color) {
        lbl.setText(msg);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + color + ";");
        lbl.setVisible(true);
        lbl.setManaged(true);
    }
}
