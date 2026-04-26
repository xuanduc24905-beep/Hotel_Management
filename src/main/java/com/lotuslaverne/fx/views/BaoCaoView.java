package com.lotuslaverne.fx.views;

import com.lotuslaverne.util.ConnectDB;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;

public class BaoCaoView {

    private static final DecimalFormat MONEY = new DecimalFormat("#,###");

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
        Label sub = new Label("Thống kê doanh thu và hiệu suất hoạt động khách sạn (dữ liệu thật từ DB)");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #8C8C8C;");
        box.getChildren().addAll(title, sub);
        return box;
    }

    private HBox buildStatCards() {
        HBox row = new HBox(16);
        row.setAlignment(Pos.TOP_LEFT);

        double doanhThuTuan = querySum(
                "SELECT ISNULL(SUM(tienThanhToan),0) FROM HoaDon " +
                "WHERE ngayLap >= DATEADD(DAY, -7, GETDATE())");
        double doanhThuThangTruoc = querySum(
                "SELECT ISNULL(SUM(tienThanhToan),0) FROM HoaDon " +
                "WHERE ngayLap >= DATEADD(DAY, -14, GETDATE()) AND ngayLap < DATEADD(DAY, -7, GETDATE())");
        String tuanDelta = pctChange(doanhThuTuan, doanhThuThangTruoc);

        double[] occ = queryOccupancy();
        String occText = String.format("%.1f%%", occ[0]);
        String occSub  = ((int) occ[1]) + " phòng tổng cộng";

        int luotKhachThang = (int) querySum(
                "SELECT COUNT(DISTINCT pdp.maKH) FROM PhieuDatPhong pdp " +
                "WHERE pdp.thoiGianNhanThucTe IS NOT NULL " +
                "  AND MONTH(pdp.thoiGianNhanThucTe) = MONTH(GETDATE()) " +
                "  AND YEAR(pdp.thoiGianNhanThucTe)  = YEAR(GETDATE())");
        int luotKhachThangTruoc = (int) querySum(
                "SELECT COUNT(DISTINCT pdp.maKH) FROM PhieuDatPhong pdp " +
                "WHERE pdp.thoiGianNhanThucTe IS NOT NULL " +
                "  AND MONTH(pdp.thoiGianNhanThucTe) = MONTH(DATEADD(MONTH, -1, GETDATE())) " +
                "  AND YEAR(pdp.thoiGianNhanThucTe)  = YEAR(DATEADD(MONTH, -1, GETDATE()))");
        String khachDelta = (luotKhachThang - luotKhachThangTruoc >= 0 ? "+" : "")
                + (luotKhachThang - luotKhachThangTruoc) + " so tháng trước";

        double doanhThuThang = querySum(
                "SELECT ISNULL(SUM(tienThanhToan),0) FROM HoaDon " +
                "WHERE MONTH(ngayLap) = MONTH(GETDATE()) AND YEAR(ngayLap) = YEAR(GETDATE())");

        row.getChildren().addAll(
            makeStatCard("Doanh Thu 7 Ngày",  MONEY.format(doanhThuTuan)  + "đ", tuanDelta,             "#1890FF", "#E6F4FF", "💰"),
            makeStatCard("Công Suất Phòng",   occText,                          occSub,                "#52C41A", "#F6FFED", "📊"),
            makeStatCard("Lượt Khách Tháng",  String.valueOf(luotKhachThang),   khachDelta,            "#722ED1", "#F9F0FF", "👤"),
            makeStatCard("Doanh Thu Tháng",   MONEY.format(doanhThuThang) + "đ", "Tổng tháng hiện tại", "#FA8C16", "#FFF7E6", "📈")
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
        Node topLoaiChart = buildTopLoaiPhongChart();
        HBox.setHgrow(barChart,  Priority.ALWAYS);
        HBox.setHgrow(topLoaiChart, Priority.ALWAYS);

        row.getChildren().addAll(barChart, topLoaiChart);
        return row;
    }

    private Node buildRevenueBarChart() {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
                + "-fx-padding: 20;");

        Label title = new Label("Doanh Thu 7 Ngày Gần Nhất");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");

        Map<String, Double> data = queryRevenueLast7Days();

        double max = data.values().stream().mapToDouble(Double::doubleValue).max().orElse(1);
        double upperBound = Math.max(5, Math.ceil(max / 1_000_000.0 / 5) * 5);

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(0, upperBound, Math.max(1, upperBound / 5));
        yAxis.setLabel("Triệu VNĐ");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setStyle("-fx-background-color: transparent;");
        chart.setPrefHeight(240);
        chart.setAnimated(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, Double> e : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue() / 1_000_000.0));
        }
        chart.getData().add(series);

        card.getChildren().addAll(title, chart);
        return card;
    }

    private Node buildTopLoaiPhongChart() {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
                + "-fx-padding: 20;");

        Label title = new Label("Top Loại Phòng Được Đặt Nhiều Nhất");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");

        Map<String, Integer> data = queryTopLoaiPhong();

        if (data.isEmpty()) {
            Label empty = new Label("Chưa có dữ liệu đặt phòng");
            empty.setStyle("-fx-text-fill: #8C8C8C; -fx-padding: 40;");
            card.getChildren().addAll(title, empty);
            return card;
        }

        int max = data.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(0, Math.max(5, Math.ceil(max * 1.2)), 1);
        yAxis.setLabel("Số lượt đặt");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setStyle("-fx-background-color: transparent;");
        chart.setPrefHeight(240);
        chart.setAnimated(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, Integer> e : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }
        chart.getData().add(series);

        card.getChildren().addAll(title, chart);
        return card;
    }

    // ───────────── DATA QUERIES ─────────────

    private double querySum(String sql) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return 0;
        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (Exception ignored) {}
        return 0;
    }

    /** @return [percentLapDay, totalPhong] */
    private double[] queryOccupancy() {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return new double[]{0, 0};
        String sql = "SELECT " +
                "  COUNT(*) AS total, " +
                "  SUM(CASE WHEN trangThai != 'PhongTrong' THEN 1 ELSE 0 END) AS daSuDung " +
                "FROM Phong";
        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                int total    = rs.getInt("total");
                int daSuDung = rs.getInt("daSuDung");
                double pct = total == 0 ? 0 : (daSuDung * 100.0 / total);
                return new double[]{pct, total};
            }
        } catch (Exception ignored) {}
        return new double[]{0, 0};
    }

    private Map<String, Double> queryRevenueLast7Days() {
        Map<String, Double> result = new LinkedHashMap<>();
        // Khởi tạo 7 ngày gần nhất với value = 0 để chart vẫn hiện đủ cột
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM");
        for (int i = 6; i >= 0; i--) {
            result.put(today.minusDays(i).format(fmt), 0.0);
        }

        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return result;
        String sql = "SELECT CAST(ngayLap AS DATE) AS ngay, SUM(tienThanhToan) AS doanhThu " +
                     "FROM HoaDon " +
                     "WHERE ngayLap >= DATEADD(DAY, -6, CAST(GETDATE() AS DATE)) " +
                     "GROUP BY CAST(ngayLap AS DATE) " +
                     "ORDER BY ngay";
        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                java.sql.Date d = rs.getDate("ngay");
                String key = d.toLocalDate().format(fmt);
                result.put(key, rs.getDouble("doanhThu"));
            }
        } catch (Exception ignored) {}
        return result;
    }

    private Map<String, Integer> queryTopLoaiPhong() {
        Map<String, Integer> result = new LinkedHashMap<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return result;
        String sql = "SELECT TOP 5 lp.tenLoaiPhong, COUNT(*) AS soLan " +
                     "FROM ChiTietPhieuDatPhong ct " +
                     "JOIN Phong p     ON p.maPhong = ct.maPhong " +
                     "JOIN LoaiPhong lp ON lp.maLoaiPhong = p.maLoaiPhong " +
                     "GROUP BY lp.tenLoaiPhong " +
                     "ORDER BY soLan DESC";
        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getString("tenLoaiPhong"), rs.getInt("soLan"));
            }
        } catch (Exception ignored) {}
        return result;
    }

    private String pctChange(double now, double prev) {
        if (prev == 0) return now > 0 ? "Mới có doanh thu" : "Chưa có dữ liệu kỳ trước";
        double pct = (now - prev) * 100.0 / prev;
        return String.format("%+.1f%% so kỳ trước", pct);
    }
}
