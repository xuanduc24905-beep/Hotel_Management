package com.lotuslaverne.fx.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

public class BaoCaoView {

    public Node build() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #F0F2F5; -fx-border-color: transparent;");

        VBox content = new VBox(24);
        content.setPadding(new Insets(28, 28, 28, 28));
        content.setStyle("-fx-background-color: #F0F2F5;");

        content.getChildren().addAll(
                buildPageHeader(),
                buildStatCards(),
                buildCharts()
        );

        scroll.setContent(content);
        return scroll;
    }

    private VBox buildPageHeader() {
        VBox box = new VBox(4);
        Label title = new Label("Báo Cáo");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Label sub = new Label("Thống kê doanh thu và hiệu suất hoạt động khách sạn");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #8C8C8C;");
        box.getChildren().addAll(title, sub);
        return box;
    }

    private HBox buildStatCards() {
        HBox row = new HBox(16);
        row.setAlignment(Pos.TOP_LEFT);

        row.getChildren().addAll(
            makeStatCard("Doanh Thu Tuần",     "124,500,000đ", "+12.5% so tuần trước", "#1890FF", "#E6F4FF", "💰"),
            makeStatCard("Công Suất Phòng",    "78.3%",        "30 phòng tổng cộng",   "#52C41A", "#F6FFED", "📊"),
            makeStatCard("Lượt Khách Tháng",   "142",          "+8 so tháng trước",    "#722ED1", "#F9F0FF", "👤"),
            makeStatCard("Đánh Giá Trung Bình","4.7 ★",        "Dựa trên 89 đánh giá", "#FA8C16", "#FFF7E6", "⭐")
        );

        for (Node n : row.getChildren()) {
            HBox.setHgrow(n, Priority.ALWAYS);
        }
        return row;
    }

    private VBox makeStatCard(String title, String value, String sub, String fg, String bg, String icon) {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
                + "-fx-padding: 20;");

        HBox inner = new HBox(14);
        inner.setAlignment(Pos.CENTER_LEFT);

        VBox textBox = new VBox(4);
        HBox.setHgrow(textBox, Priority.ALWAYS);
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #8C8C8C;");
        Label valueLbl = new Label(value);
        valueLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Label subLbl = new Label(sub);
        subLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + fg + ";");
        textBox.getChildren().addAll(titleLbl, valueLbl, subLbl);

        StackPane iconPane = new StackPane();
        iconPane.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 24;"
                + "-fx-min-width: 48px; -fx-min-height: 48px;"
                + "-fx-max-width: 48px; -fx-max-height: 48px;");
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 20px;");
        iconPane.getChildren().add(iconLbl);

        inner.getChildren().addAll(textBox, iconPane);
        card.getChildren().add(inner);
        return card;
    }

    private HBox buildCharts() {
        HBox row = new HBox(20);
        row.setAlignment(Pos.TOP_LEFT);

        Node barChart  = buildRevenueBarChart();
        Node lineChart = buildOccupancyLineChart();
        HBox.setHgrow(barChart,  Priority.ALWAYS);
        HBox.setHgrow(lineChart, Priority.ALWAYS);

        row.getChildren().addAll(barChart, lineChart);
        return row;
    }

    private Node buildRevenueBarChart() {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
                + "-fx-padding: 20;");

        Label title = new Label("Doanh Thu 7 Ngày");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(0, 30, 5);
        yAxis.setLabel("Triệu VND");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setStyle("-fx-background-color: transparent;");
        chart.setPrefHeight(240);
        chart.setAnimated(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String[] days = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        double[] vals = {12.5, 15.2, 11.8, 18.4, 22.1, 25.6, 19.3};
        for (int i = 0; i < days.length; i++) {
            series.getData().add(new XYChart.Data<>(days[i], vals[i]));
        }
        chart.getData().add(series);

        card.getChildren().addAll(title, chart);
        return card;
    }

    private Node buildOccupancyLineChart() {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
                + "-fx-padding: 20;");

        Label title = new Label("Công Suất Phòng (%) theo Tháng");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(50, 100, 10);
        yAxis.setLabel("%");

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setStyle("-fx-background-color: transparent;");
        chart.setPrefHeight(240);
        chart.setAnimated(false);
        chart.setCreateSymbols(true);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String[] months = {"T1","T2","T3","T4","T5","T6","T7","T8","T9","T10","T11","T12"};
        double[] vals   = {70,  72,  75,  78,  82,  88,  92,  95,  91,  85,   80,   76};
        for (int i = 0; i < months.length; i++) {
            series.getData().add(new XYChart.Data<>(months[i], vals[i]));
        }
        chart.getData().add(series);

        card.getChildren().addAll(title, chart);
        return card;
    }
}
