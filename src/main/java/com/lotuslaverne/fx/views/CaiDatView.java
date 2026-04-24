package com.lotuslaverne.fx.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

public class CaiDatView {

    public Node build() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #F0F2F5; -fx-border-color: transparent;");

        VBox content = new VBox(24);
        content.setPadding(new Insets(28, 28, 28, 28));
        content.setStyle("-fx-background-color: #F0F2F5;");

        // Page header
        VBox header = new VBox(4);
        Label title = new Label("Cài Đặt");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Label sub = new Label("Cấu hình hệ thống quản lý khách sạn");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #8C8C8C;");
        header.getChildren().addAll(title, sub);

        // Cards grid
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        ColumnConstraints cc1 = new ColumnConstraints();
        cc1.setPercentWidth(50);
        ColumnConstraints cc2 = new ColumnConstraints();
        cc2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(cc1, cc2);

        grid.add(buildHotelInfoCard(), 0, 0);
        grid.add(buildNotificationCard(), 1, 0);
        grid.add(buildSecurityCard(), 0, 1);
        grid.add(buildSystemCard(), 1, 1);

        content.getChildren().addAll(header, grid);
        scroll.setContent(content);
        return scroll;
    }

    private VBox buildHotelInfoCard() {
        VBox card = card("Thông Tin Khách Sạn", "🏨");

        String[][] rows = {
            {"Tên Khách Sạn",  "Lotus Laverne Hotel"},
            {"Địa Chỉ",        "123 Nguyễn Huệ, Q.1, TP.HCM"},
            {"Điện Thoại",     "(028) 3822 1234"},
            {"Email",          "info@lotuslaverne.vn"},
            {"Website",        "www.lotuslaverne.vn"},
            {"Số Phòng",       "30 phòng"},
        };

        for (String[] r : rows) {
            card.getChildren().add(settingRow(r[0], r[1]));
        }
        card.getChildren().add(editButton());
        return card;
    }

    private VBox buildNotificationCard() {
        VBox card = card("Thông Báo", "🔔");

        Object[][] rows = {
            {"Nhận Phòng Mới",      true},
            {"Trả Phòng",           true},
            {"Thanh Toán",          true},
            {"Cảnh Báo Hệ Thống",   false},
            {"Báo Cáo Hàng Ngày",   true},
            {"Email Tổng Kết Tuần", false},
        };

        for (Object[] r : rows) {
            card.getChildren().add(notifRow((String) r[0], (Boolean) r[1]));
        }
        card.getChildren().add(editButton());
        return card;
    }

    private VBox buildSecurityCard() {
        VBox card = card("Bảo Mật", "🔒");

        Object[][] rows = {
            {"Xác Thực 2 Bước",      false},
            {"Thời Gian Hết Phiên",  "30 phút"},
            {"Nhật Ký Truy Cập",     true},
            {"Mã Hoá Dữ Liệu",       true},
            {"Đổi Mật Khẩu Định Kỳ", "90 ngày"},
        };

        for (Object[] r : rows) {
            String val;
            if (r[1] instanceof Boolean) {
                val = (Boolean) r[1] ? "Bật" : "Tắt";
            } else {
                val = (String) r[1];
            }
            card.getChildren().add(settingRow((String) r[0], val));
        }
        card.getChildren().add(editButton());
        return card;
    }

    private VBox buildSystemCard() {
        VBox card = card("Hệ Thống", "⚙");

        String[][] rows = {
            {"Phiên Bản",          "v1.0.0"},
            {"Ngôn Ngữ",           "Tiếng Việt"},
            {"Múi Giờ",            "GMT+7 (Hà Nội)"},
            {"Định Dạng Ngày",     "dd/MM/yyyy"},
            {"Đơn Vị Tiền Tệ",    "VND (₫)"},
            {"Tự Động Sao Lưu",    "Hàng Ngày 02:00"},
        };

        for (String[] r : rows) {
            card.getChildren().add(settingRow(r[0], r[1]));
        }
        card.getChildren().add(editButton());
        return card;
    }

    // ---------------------------------------------------------------- HELPERS
    private VBox card(String title, String icon) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 1);");

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        titleRow.setPadding(new Insets(0, 0, 10, 0));
        titleRow.setStyle("-fx-border-color: transparent transparent #F0F2F5 transparent;"
                + "-fx-border-width: 0 0 1 0;");
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 16px;");
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        titleRow.getChildren().addAll(iconLbl, titleLbl);
        card.getChildren().add(titleRow);
        return card;
    }

    private HBox settingRow(String label, String value) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(5, 0, 5, 0));

        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #595959;");
        lbl.setPrefWidth(180);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label val = new Label(value);
        val.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");

        row.getChildren().addAll(lbl, spacer, val);
        return row;
    }

    private HBox notifRow(String label, boolean enabled) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(5, 0, 5, 0));

        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #595959;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badge = new Label(enabled ? "Bật" : "Tắt");
        String bg = enabled ? "#F6FFED" : "#F5F5F5";
        String fg = enabled ? "#52C41A" : "#8C8C8C";
        badge.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";"
                + "-fx-padding: 2 10 2 10; -fx-background-radius: 10;"
                + "-fx-font-size: 11px; -fx-font-weight: bold;");

        row.getChildren().addAll(lbl, spacer, badge);
        return row;
    }

    private Button editButton() {
        Button btn = new Button("Chỉnh Sửa");
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #1890FF;"
                + "-fx-border-color: #1890FF; -fx-border-width: 1; -fx-border-radius: 8;"
                + "-fx-background-radius: 8; -fx-font-size: 12px; -fx-padding: 6 16;"
                + "-fx-cursor: hand; -fx-font-weight: bold;");
        btn.setMaxWidth(Double.MAX_VALUE);
        return btn;
    }
}
