package com.lotuslaverne.fx.views;

import com.lotuslaverne.dao.ThongKeDAO;
import com.lotuslaverne.fx.UiUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.text.NumberFormat;
import java.util.Locale;

public class DashboardView {

    public Node build() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #F0F2F5; -fx-border-color: transparent;");
        scroll.getStyleClass().add("scroll-pane");

        VBox content = new VBox(24);
        content.setPadding(new Insets(28, 28, 28, 28));
        content.setStyle("-fx-background-color: #F0F2F5;");

        content.getChildren().addAll(
                buildPageHeader(),
                buildStatCards(),
                buildBottomSection()
        );

        scroll.setContent(content);
        return scroll;
    }

    // ---------------------------------------------------------------- HEADER
    private VBox buildPageHeader() {
        VBox box = new VBox(4);
        Label title = new Label("Tổng Quan");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Label sub = new Label("Xin chào! Đây là tổng quan hoạt động hôm nay.");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #8C8C8C;");
        box.getChildren().addAll(title, sub);
        return box;
    }

    // ---------------------------------------------------------------- STAT CARDS
    private GridPane buildStatCards() {
        // Load data
        int phongTrong = 0, phongDangThue = 0, phongCanDon = 0, tongNhanVien = 0, khachLuuTru = 0;
        double doanhThu = 0;
        boolean dbOnline = false;
        try {
            ThongKeDAO dao = new ThongKeDAO();
            // Truyền đúng giá trị trangThai lưu trong DB
            phongTrong    = dao.demSoPhongTheoTrangThai("PhongTrong");
            phongDangThue = dao.demSoPhongTheoTrangThai("PhongDat");
            phongCanDon   = dao.demSoPhongTheoTrangThai("PhongCanDon");
            tongNhanVien  = dao.demTongNhanSu();
            khachLuuTru   = dao.demKhachDangLuuTru();
            doanhThu      = dao.layDoanhThuHomNay();
            dbOnline      = true;
        } catch (Exception ignored) {}

        // Dữ liệu demo khi DB offline
        if (!dbOnline) {
            phongTrong    = 12;
            phongDangThue = 8;
            phongCanDon   = 3;
            tongNhanVien  = 24;
            khachLuuTru   = 9;
            doanhThu      = 15_500_000;
        }

        String doanhThuFmt = formatVND(doanhThu);

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);
        for (int i = 0; i < 3; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(33.33);
            grid.getColumnConstraints().add(cc);
        }

        grid.add(makeStatCard("Phòng Trống",       String.valueOf(phongTrong),    "phòng sẵn sàng",   "#52C41A", "🏠"), 0, 0);
        grid.add(makeStatCard("Phòng Đang Thuê",   String.valueOf(phongDangThue), "khách đang ở",     "#FF4D4F", "🔑"), 1, 0);
        grid.add(makeStatCard("Doanh Thu Hôm Nay", doanhThuFmt,                  "tổng thu hôm nay", "#1890FF", "💰"),  2, 0);
        grid.add(makeStatCard("Khách Lưu Trú",     String.valueOf(khachLuuTru),   "đang ở khách sạn", "#722ED1", "👤"), 0, 1);
        grid.add(makeStatCard("Tổng Nhân Viên",    String.valueOf(tongNhanVien),  "đang làm việc",    "#FA8C16", "👥"), 1, 1);
        grid.add(makeStatCard("Phòng Cần Dọn",     String.valueOf(phongCanDon),   "cần vệ sinh",      "#FAAD14", "🧹"), 2, 1);

        return grid;
    }

    private HBox makeStatCard(String title, String value, String sub, String color, String icon) {
        HBox card = new HBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
                + "-fx-padding: 20;");

        VBox textBox = new VBox(4);
        HBox.setHgrow(textBox, Priority.ALWAYS);
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #8C8C8C;");
        Label valueLbl = new Label(value);
        valueLbl.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Label subLbl = new Label(sub);
        subLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #8C8C8C;");
        textBox.getChildren().addAll(titleLbl, valueLbl, subLbl);

        StackPane iconCircle = new StackPane();
        iconCircle.setStyle("-fx-background-color: " + color + "20;"
                + "-fx-background-radius: 24;"
                + "-fx-min-width: 48px; -fx-min-height: 48px;"
                + "-fx-max-width: 48px; -fx-max-height: 48px;");
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 20px;");
        iconCircle.getChildren().add(iconLbl);

        // Colored left border via nested wrapper
        HBox wrapper = new HBox();
        wrapper.setStyle("-fx-background-color: " + color + ";"
                + "-fx-min-width: 4px; -fx-max-width: 4px;"
                + "-fx-background-radius: 10 0 0 10;");

        StackPane outer = new StackPane();
        outer.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        card.getChildren().addAll(textBox, iconCircle);
        return card;
    }

    // ---------------------------------------------------------------- BOTTOM SECTION
    private HBox buildBottomSection() {
        HBox box = new HBox(20);
        box.setAlignment(Pos.TOP_LEFT);

        Node barChart = buildRevenueChart();
        Node statusPanel = buildRoomStatusPanel();

        HBox.setHgrow(barChart, Priority.ALWAYS);
        HBox.setHgrow(statusPanel, Priority.SOMETIMES);

        box.getChildren().addAll(barChart, statusPanel);
        return box;
    }

    private Node buildRevenueChart() {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
                + "-fx-padding: 20;");
        HBox.setHgrow(card, Priority.ALWAYS);

        Label title = new Label("Doanh Thu 7 Ngày Qua");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Triệu VND");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setStyle("-fx-background-color: transparent;");
        chart.setPrefHeight(220);
        chart.setAnimated(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String[] days  = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        double[] vals  = {12.5, 15.2, 11.8, 18.4, 22.1, 25.6, 19.3};
        for (int i = 0; i < days.length; i++) {
            series.getData().add(new XYChart.Data<>(days[i], vals[i]));
        }
        chart.getData().add(series);

        // Style bars blue
        chart.lookupAll(".bar").forEach(n ->
                n.setStyle("-fx-bar-fill: #1890FF;"));
        chart.setOnMouseEntered(e ->
                chart.lookupAll(".bar").forEach(n ->
                        n.setStyle("-fx-bar-fill: #1890FF;")));

        card.getChildren().addAll(title, chart);
        return card;
    }

    private Node buildRoomStatusPanel() {
        VBox card = new VBox(14);
        card.setPrefWidth(300);
        card.setMinWidth(260);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
                + "-fx-padding: 20;");

        Label title = new Label("Trạng Thái Phòng");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");

        card.getChildren().add(title);
        card.getChildren().add(makeStatusRow("Phòng Trống",    0.40, "#52C41A", "12/30"));
        card.getChildren().add(makeStatusRow("Đang Thuê",      0.27, "#FF4D4F", "8/30"));
        card.getChildren().add(makeStatusRow("Cần Dọn",        0.10, "#FAAD14", "3/30"));
        card.getChildren().add(makeStatusRow("Đang Dọn",       0.23, "#1890FF", "7/30"));

        // Divider
        Region div = new Region();
        div.setPrefHeight(1);
        div.setStyle("-fx-background-color: #F0F2F5;");
        card.getChildren().add(div);

        Label guestTitle = new Label("Khách Đang Lưu Trú");
        guestTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        card.getChildren().add(guestTitle);

        String[][] guests = {
            {"Nguyễn Văn An",    "101", "#1890FF"},
            {"Trần Thị Bình",    "205", "#52C41A"},
            {"Lê Hoàng Cường",   "312", "#FF7A45"},
            {"Phạm Thị Dung",    "408", "#9254DE"},
        };
        for (String[] g : guests) {
            card.getChildren().add(makeGuestRow(g[0], g[1], g[2]));
        }

        return card;
    }

    private VBox makeStatusRow(String label, double progress, String color, String countStr) {
        VBox row = new VBox(4);
        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #595959;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label cnt = new Label(countStr);
        cnt.setStyle("-fx-font-size: 12px; -fx-text-fill: #8C8C8C;");
        top.getChildren().addAll(lbl, sp, cnt);

        ProgressBar bar = new ProgressBar(progress);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setPrefHeight(8);
        bar.setStyle("-fx-accent: " + color + ";");

        row.getChildren().addAll(top, bar);
        return row;
    }

    private HBox makeGuestRow(String name, String room, String color) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(4, 0, 4, 0));

        StackPane avatar = UiUtils.makeAvatarCircle(name, 16);

        VBox info = new VBox(1);
        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #1A1A2E;");
        Label roomLbl = new Label("Phòng " + room);
        roomLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #8C8C8C;");
        info.getChildren().addAll(nameLbl, roomLbl);

        row.getChildren().addAll(avatar, info);
        return row;
    }

    private String formatVND(double amount) {
        if (amount == 0) return "0đ";
        NumberFormat nf = NumberFormat.getInstance(Locale.of("vi", "VN"));
        return nf.format((long) amount) + "đ";
    }
}
