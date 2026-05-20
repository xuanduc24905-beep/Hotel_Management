package com.lotuslaverne.fx.views;

import com.lotuslaverne.service.BaoCaoService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class BaoCaoView {

    private static final DecimalFormat MONEY = new DecimalFormat("#,###");

    /** Phần nội dung động — được rebuild mỗi khi thay đổi bộ lọc ngày. */
    private VBox dataSection;

    public Node build() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #F0F2F5; -fx-border-color: transparent;");

        VBox content = new VBox(20);
        content.setPadding(new Insets(28, 28, 28, 28));
        content.setStyle("-fx-background-color: #F0F2F5;");

        dataSection = new VBox(20);

        // Mặc định: tháng hiện tại
        LocalDate from = LocalDate.now().withDayOfMonth(1);
        LocalDate to   = LocalDate.now();

        content.getChildren().addAll(
                buildPageHeader(),
                buildFilterBar(from, to),
                dataSection
        );
        rebuildData(from, to);

        scroll.setContent(content);
        return scroll;
    }

    // ─── HEADER ──────────────────────────────────────────────────────────────

    private VBox buildPageHeader() {
        VBox box = new VBox(4);
        Label title = new Label("Báo Cáo");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Label sub = new Label("Thống kê doanh thu và hiệu suất hoạt động — chọn khoảng thời gian bên dưới");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #8C8C8C;");
        box.getChildren().addAll(title, sub);
        return box;
    }

    // ─── FILTER BAR ──────────────────────────────────────────────────────────

    private HBox buildFilterBar(LocalDate initFrom, LocalDate initTo) {
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(14, 18, 14, 18));
        bar.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        // Preset buttons
        String[][] presets = {
            {"7 ngày",      "7d"},
            {"Tháng này",   "thisMonth"},
            {"Tháng trước", "lastMonth"},
            {"Năm nay",     "thisYear"}
        };
        String[] activePreset = {"thisMonth"};
        Button[] presetBtns   = new Button[presets.length];

        DatePicker dpFrom = new DatePicker(initFrom);
        dpFrom.setPrefWidth(130);
        DatePicker dpTo   = new DatePicker(initTo);
        dpTo.setPrefWidth(130);

        Button btnApply = new Button("Áp Dụng");
        btnApply.setStyle("-fx-background-color:#1890FF;-fx-text-fill:white;"
                + "-fx-background-radius:7;-fx-padding:7 18;-fx-font-weight:bold;-fx-cursor:hand;");

        HBox presetRow = new HBox(6);
        for (int i = 0; i < presets.length; i++) {
            final String code  = presets[i][1];
            final String label = presets[i][0];
            Button b = new Button(label);
            presetBtns[i] = b;
            b.setStyle(code.equals(activePreset[0]) ? presetActiveStyle() : presetStyle());
            b.setOnAction(e -> {
                LocalDate[] range = resolvePreset(code);
                dpFrom.setValue(range[0]);
                dpTo.setValue(range[1]);
                activePreset[0] = code;
                for (Button pb : presetBtns) pb.setStyle(presetStyle());
                b.setStyle(presetActiveStyle());
                rebuildData(range[0], range[1]);
            });
            presetRow.getChildren().add(b);
        }

        btnApply.setOnAction(e -> {
            LocalDate f = dpFrom.getValue(), t = dpTo.getValue();
            if (f == null || t == null || t.isBefore(f)) return;
            for (Button pb : presetBtns) pb.setStyle(presetStyle());
            rebuildData(f, t);
        });

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblFrom = new Label("Từ:");
        lblFrom.setStyle("-fx-font-size:12px;-fx-text-fill:#595959;");
        Label lblTo = new Label("Đến:");
        lblTo.setStyle("-fx-font-size:12px;-fx-text-fill:#595959;");

        bar.getChildren().addAll(presetRow, spacer, lblFrom, dpFrom, lblTo, dpTo, btnApply);
        return bar;
    }

    private LocalDate[] resolvePreset(String code) {
        LocalDate now = LocalDate.now();
        return switch (code) {
            case "7d"        -> new LocalDate[]{now.minusDays(6), now};
            case "thisMonth" -> new LocalDate[]{now.withDayOfMonth(1), now};
            case "lastMonth" -> {
                LocalDate first = now.minusMonths(1).withDayOfMonth(1);
                yield new LocalDate[]{first, first.withDayOfMonth(first.lengthOfMonth())};
            }
            case "thisYear"  -> new LocalDate[]{now.withDayOfYear(1), now};
            default          -> new LocalDate[]{now.withDayOfMonth(1), now};
        };
    }

    // ─── REBUILD ─────────────────────────────────────────────────────────────

    private void rebuildData(LocalDate from, LocalDate to) {
        dataSection.getChildren().clear();
        dataSection.getChildren().addAll(
                buildStatCards(from, to),
                buildChartsRow(from, to),
                buildPaymentBreakdown(from, to)
        );
    }

    // ─── STAT CARDS ──────────────────────────────────────────────────────────

    private HBox buildStatCards(LocalDate from, LocalDate to) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.TOP_LEFT);

        double[] stats = new BaoCaoService().getStatCards(from, to);
        double doanhThu      = stats[0];
        double doanhThuTruoc = stats[1];
        int    tongPhieu     = (int) stats[2];
        int    soHuy         = (int) stats[3];
        int    luotKhach     = (int) stats[4];
        double avgStay       = stats[5];

        String dtDelta  = pctChange(doanhThu, doanhThuTruoc);
        String huyRate  = tongPhieu == 0 ? "0 hủy" :
            soHuy + " hủy (" + String.format("%.0f%%", soHuy * 100.0 / tongPhieu) + ")";

        row.getChildren().addAll(
            makeStatCard("Doanh Thu Kỳ",   MONEY.format((long) doanhThu) + "đ",
                                            dtDelta,                              "#1890FF", "#E6F4FF", "💰"),
            makeStatCard("Lượt Khách",      String.valueOf(luotKhach),
                                            "đã check-in trong kỳ",              "#722ED1", "#F9F0FF", "👤"),
            makeStatCard("Tổng Phiếu Đặt", String.valueOf(tongPhieu),
                                            huyRate,                              "#52C41A", "#F6FFED", "📋"),
            makeStatCard("Lưu Trú TB",      String.format("%.1f đêm", avgStay),
                                            "trung bình mỗi lần ở",              "#FA8C16", "#FFF7E6", "🌙")
        );
        for (Node n : row.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);
        return row;
    }

    // ─── CHARTS ROW ──────────────────────────────────────────────────────────

    private HBox buildChartsRow(LocalDate from, LocalDate to) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.TOP_LEFT);
        Node revenueChart  = buildRevenueChart(from, to);
        Node roomTypeChart = buildRoomTypeChart(from, to);
        HBox.setHgrow(revenueChart,  Priority.ALWAYS);
        HBox.setHgrow(roomTypeChart, Priority.ALWAYS);
        row.getChildren().addAll(revenueChart, roomTypeChart);
        return row;
    }

    /** Biểu đồ doanh thu theo ngày (≤31 ngày) hoặc theo tháng (>31 ngày). */
    private Node buildRevenueChart(LocalDate from, LocalDate to) {
        VBox card = buildCard();
        long days    = ChronoUnit.DAYS.between(from, to) + 1;
        boolean byMonth = days > 31;

        Label title = new Label(byMonth ? "Doanh Thu Theo Tháng" : "Doanh Thu Theo Ngày");
        title.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#1A1A2E;");

        BaoCaoService svc = new BaoCaoService();
        Map<String, Double> data = byMonth
                ? svc.getRevenueByMonth(from, to)
                : svc.getRevenueByDay(from, to);

        double max = data.values().stream().mapToDouble(Double::doubleValue).max().orElse(1_000_000);
        double upperBound = Math.max(1, Math.ceil(max / 1_000_000.0 / 5) * 5);

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis   yAxis = new NumberAxis(0, upperBound, Math.max(1, upperBound / 5));
        yAxis.setLabel("Triệu VNĐ");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setStyle("-fx-background-color: transparent;");
        chart.setPrefHeight(240);
        chart.setAnimated(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, Double> e : data.entrySet())
            series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue() / 1_000_000.0));
        chart.getData().add(series);

        card.getChildren().addAll(title, chart);
        return card;
    }

    /** Biểu đồ lượt đặt theo loại phòng trong kỳ. */
    private Node buildRoomTypeChart(LocalDate from, LocalDate to) {
        VBox card = buildCard();
        Label title = new Label("Lượt Đặt Theo Loại Phòng");
        title.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#1A1A2E;");

        Map<String, Integer> data = new BaoCaoService().getTopLoaiPhong(from, to);
        if (data.isEmpty()) {
            Label empty = new Label("Chưa có dữ liệu đặt phòng trong kỳ");
            empty.setStyle("-fx-text-fill:#8C8C8C;-fx-padding:40;");
            card.getChildren().addAll(title, empty);
            return card;
        }

        int max = data.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis   yAxis = new NumberAxis(0, Math.max(5, Math.ceil(max * 1.2)), 1);
        yAxis.setLabel("Số lượt đặt");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setStyle("-fx-background-color: transparent;");
        chart.setPrefHeight(240);
        chart.setAnimated(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, Integer> e : data.entrySet())
            series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        chart.getData().add(series);

        card.getChildren().addAll(title, chart);
        return card;
    }

    // ─── PAYMENT BREAKDOWN ───────────────────────────────────────────────────

    /** Phân tích doanh thu theo hình thức thanh toán — dạng progress bar ngang. */
    private Node buildPaymentBreakdown(LocalDate from, LocalDate to) {
        VBox card = buildCard();
        Label title = new Label("Doanh Thu Theo Hình Thức Thanh Toán");
        title.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#1A1A2E;");

        Map<String, Double> data = new BaoCaoService().getRevenueByPayment(from, to);
        if (data.isEmpty()) {
            Label empty = new Label("Chưa có hóa đơn nào trong kỳ này");
            empty.setStyle("-fx-text-fill:#8C8C8C;-fx-padding:20;");
            card.getChildren().addAll(title, empty);
            return card;
        }

        double total = data.values().stream().mapToDouble(Double::doubleValue).sum();
        Map<String, String> htDisplay = new LinkedHashMap<>();
        htDisplay.put("TienMat",     "Tiền Mặt");
        htDisplay.put("MaQR",        "QR Code");
        htDisplay.put("MOMO",        "MOMO");
        htDisplay.put("ChuyenKhoan", "Chuyển Khoản");
        String[] colors = {"#1890FF", "#52C41A", "#722ED1", "#FA8C16", "#FF4D4F"};

        VBox rows = new VBox(12);
        int ci = 0;
        for (Map.Entry<String, Double> e : data.entrySet()) {
            String display = htDisplay.getOrDefault(e.getKey(), e.getKey());
            double pct     = total == 0 ? 0 : e.getValue() / total;
            String color   = colors[ci % colors.length];

            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);

            Label nameLbl = new Label(display);
            nameLbl.setMinWidth(130);
            nameLbl.setStyle("-fx-font-size:13px;-fx-text-fill:#595959;");

            ProgressBar bar = new ProgressBar(pct);
            bar.setMaxWidth(Double.MAX_VALUE);
            bar.setPrefHeight(16);
            bar.setStyle("-fx-accent:" + color + ";");
            HBox.setHgrow(bar, Priority.ALWAYS);

            Label valLbl = new Label(MONEY.format(e.getValue().longValue()) + "đ"
                    + "  (" + String.format("%.0f%%", pct * 100) + ")");
            valLbl.setMinWidth(190);
            valLbl.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");

            row.getChildren().addAll(nameLbl, bar, valLbl);
            rows.getChildren().add(row);
            ci++;
        }

        // Tổng cộng
        HBox totalRow = new HBox();
        totalRow.setAlignment(Pos.CENTER_RIGHT);
        totalRow.setPadding(new Insets(8, 0, 0, 0));
        totalRow.setStyle("-fx-border-color:#F0F2F5 transparent transparent transparent;-fx-border-width:1 0 0 0;");
        Label totalLbl = new Label("Tổng:  " + MONEY.format((long) total) + "đ");
        totalLbl.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#1A1A2E;");
        totalRow.getChildren().add(totalLbl);

        card.getChildren().addAll(title, rows, totalRow);
        return card;
    }

    // ─── STYLE HELPERS ───────────────────────────────────────────────────────

    private String pctChange(double now, double prev) {
        if (prev == 0) return now > 0 ? "▲ mới có doanh thu" : "Chưa có dữ liệu kỳ trước";
        double pct = (now - prev) * 100.0 / prev;
        return String.format("%s%.1f%% so kỳ trước", pct >= 0 ? "▲ +" : "▼ ", pct);
    }

    private VBox buildCard() {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:10;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),8,0,0,2);-fx-padding:20;");
        return card;
    }

    private VBox makeStatCard(String title, String value, String sub,
                               String fg, String bg, String icon) {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:10;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),8,0,0,2);-fx-padding:20;");
        HBox inner = new HBox(14);
        inner.setAlignment(Pos.CENTER_LEFT);
        VBox textBox = new VBox(4);
        HBox.setHgrow(textBox, Priority.ALWAYS);
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#8C8C8C;");
        Label valueLbl = new Label(value);
        valueLbl.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:#1A1A2E;");
        Label subLbl = new Label(sub);
        subLbl.setStyle("-fx-font-size:11px;-fx-text-fill:" + fg + ";");
        textBox.getChildren().addAll(titleLbl, valueLbl, subLbl);

        StackPane iconPane = new StackPane();
        iconPane.setStyle("-fx-background-color:" + bg + ";-fx-background-radius:24;"
                + "-fx-min-width:48px;-fx-min-height:48px;"
                + "-fx-max-width:48px;-fx-max-height:48px;");
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size:20px;");
        iconPane.getChildren().add(iconLbl);

        inner.getChildren().addAll(textBox, iconPane);
        card.getChildren().add(inner);
        return card;
    }

    private String presetStyle() {
        return "-fx-background-color:#F5F5F5;-fx-text-fill:#595959;"
             + "-fx-background-radius:6;-fx-border-color:#D9D9D9;-fx-border-width:1;"
             + "-fx-border-radius:6;-fx-padding:6 14;-fx-cursor:hand;-fx-font-size:12px;";
    }

    private String presetActiveStyle() {
        return "-fx-background-color:#E6F4FF;-fx-text-fill:#1890FF;"
             + "-fx-background-radius:6;-fx-border-color:#1890FF;-fx-border-width:1.5;"
             + "-fx-border-radius:6;-fx-padding:6 14;-fx-cursor:hand;"
             + "-fx-font-size:12px;-fx-font-weight:bold;";
    }
}
