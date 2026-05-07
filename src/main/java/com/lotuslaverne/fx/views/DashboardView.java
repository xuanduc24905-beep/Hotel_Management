package com.lotuslaverne.fx.views;

import com.lotuslaverne.dao.ThongKeDAO;
import com.lotuslaverne.fx.UiUtils;
import com.lotuslaverne.util.ConnectDB;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
        int phongTrong = 0, phongDangThue = 0, phongCanDon = 0, tongNhanVien = 0, khachLuuTru = 0;
        double doanhThu = 0;
        boolean dbOnline = false;
        try {
            ThongKeDAO dao = new ThongKeDAO();
            phongTrong    = dao.demSoPhongTheoTrangThai("PhongTrong");
            phongDangThue = dao.demSoPhongTheoTrangThai("PhongDat");
            phongCanDon   = dao.demSoPhongTheoTrangThai("PhongCanDon");
            tongNhanVien  = dao.demTongNhanSu();
            khachLuuTru   = dao.demKhachDangLuuTru();
            doanhThu      = dao.layDoanhThuHomNay();
            dbOnline      = true;
        } catch (Exception ignored) {}

        if (!dbOnline) {
            phongTrong = 12; phongDangThue = 8; phongCanDon = 3;
            tongNhanVien = 24; khachLuuTru = 9; doanhThu = 15_500_000;
        }

        GridPane grid = new GridPane();
        grid.setHgap(16); grid.setVgap(16);
        for (int i = 0; i < 3; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(33.33);
            grid.getColumnConstraints().add(cc);
        }

        grid.add(makeStatCard("Phòng Trống",       String.valueOf(phongTrong),    "phòng sẵn sàng",   "#52C41A", "🏠"), 0, 0);
        grid.add(makeStatCard("Phòng Đang Thuê",   String.valueOf(phongDangThue), "khách đang ở",     "#FF4D4F", "🔑"), 1, 0);
        grid.add(makeStatCard("Doanh Thu Hôm Nay", formatVND(doanhThu),           "tổng thu hôm nay", "#1890FF", "💰"), 2, 0);
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

        card.getChildren().addAll(textBox, iconCircle);
        return card;
    }

    // ---------------------------------------------------------------- BOTTOM SECTION
    private HBox buildBottomSection() {
        HBox box = new HBox(20);
        box.setAlignment(Pos.TOP_LEFT);
        Node barChart    = buildRevenueChart();
        Node statusPanel = buildRoomStatusPanel();
        HBox.setHgrow(barChart,    Priority.ALWAYS);
        HBox.setHgrow(statusPanel, Priority.SOMETIMES);
        box.getChildren().addAll(barChart, statusPanel);
        return box;
    }

    // ── Biểu đồ doanh thu 7 ngày — query thật từ DB ──────────────────────────
    private Node buildRevenueChart() {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
                + "-fx-padding: 20;");
        HBox.setHgrow(card, Priority.ALWAYS);

        Label title = new Label("Doanh Thu 7 Ngày Qua");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");

        Map<String, Double> data = queryRevenueLast7Days();
        double max = data.values().stream().mapToDouble(Double::doubleValue).max().orElse(1_000_000);
        double upperBound = Math.max(1, Math.ceil(max / 1_000_000.0 / 5) * 5);

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(0, upperBound, Math.max(1, upperBound / 5));
        yAxis.setLabel("Triệu VNĐ");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setStyle("-fx-background-color: transparent;");
        chart.setPrefHeight(220);
        chart.setAnimated(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, Double> e : data.entrySet())
            series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue() / 1_000_000.0));
        chart.getData().add(series);

        card.getChildren().addAll(title, chart);
        return card;
    }

    // ── Panel trạng thái phòng + khách đang ở — query thật từ DB ─────────────
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

        // Query tổng hợp trạng thái phòng
        int[] stats = queryRoomStats(); // [total, trong, dangThue, canDon, dangDon, baoTri]
        int total = Math.max(1, stats[0]);
        card.getChildren().add(makeStatusRow("Phòng Trống",  (double) stats[1] / total, "#52C41A", stats[1] + "/" + total));
        card.getChildren().add(makeStatusRow("Đang Thuê",    (double) stats[2] / total, "#FF4D4F", stats[2] + "/" + total));
        card.getChildren().add(makeStatusRow("Cần Dọn",      (double) stats[3] / total, "#FAAD14", stats[3] + "/" + total));
        card.getChildren().add(makeStatusRow("Đang Dọn",     (double) stats[4] / total, "#1890FF", stats[4] + "/" + total));
        if (stats[5] > 0)
            card.getChildren().add(makeStatusRow("Bảo Trì", (double) stats[5] / total, "#8C8C8C", stats[5] + "/" + total));

        Region div = new Region();
        div.setPrefHeight(1);
        div.setStyle("-fx-background-color: #F0F2F5;");
        card.getChildren().add(div);

        Label guestTitle = new Label("Khách Đang Lưu Trú");
        guestTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        card.getChildren().add(guestTitle);

        List<String[]> guests = queryCurrentGuests();
        String[] colors = {"#1890FF", "#52C41A", "#FF7A45", "#9254DE", "#FA8C16"};
        if (guests.isEmpty()) {
            Label empty = new Label("Không có khách đang lưu trú");
            empty.setStyle("-fx-font-size: 12px; -fx-text-fill: #8C8C8C; -fx-padding: 8 0;");
            card.getChildren().add(empty);
        } else {
            for (int i = 0; i < guests.size(); i++)
                card.getChildren().add(makeGuestRow(guests.get(i)[0], guests.get(i)[1], colors[i % colors.length]));
        }
        return card;
    }

    private VBox makeStatusRow(String label, double progress, String color, String countStr) {
        VBox row = new VBox(4);
        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #595959;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
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

    // ---------------------------------------------------------------- DB QUERIES

    /** Doanh thu từng ngày trong 7 ngày qua. */
    private Map<String, Double> queryRevenueLast7Days() {
        Map<String, Double> result = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
        for (int i = 6; i >= 0; i--)
            result.put(today.minusDays(i).format(fmt), 0.0);

        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return result;
        String sql = "SELECT CAST(ngayLap AS DATE) AS ngay, ISNULL(SUM(tienThanhToan),0) "
                   + "FROM HoaDon "
                   + "WHERE ngayLap >= DATEADD(DAY, -6, CAST(GETDATE() AS DATE)) "
                   + "GROUP BY CAST(ngayLap AS DATE) "
                   + "ORDER BY ngay";
        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next())
                result.put(rs.getDate(1).toLocalDate().format(fmt), rs.getDouble(2));
        } catch (Exception ignored) {}
        return result;
    }

    /**
     * Đếm số phòng theo từng trạng thái trong một lần query.
     * @return [total, trong, dangThue, canDon, dangDon, baoTri]
     */
    private int[] queryRoomStats() {
        int[] r = new int[6];
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return r;
        String sql = "SELECT trangThai, COUNT(*) FROM Phong GROUP BY trangThai";
        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                String tt = rs.getString(1);
                int cnt   = rs.getInt(2);
                r[0] += cnt;
                switch (tt) {
                    case "PhongTrong"   -> r[1] += cnt;
                    case "PhongDat"     -> r[2] += cnt;
                    case "PhongCanDon"  -> r[3] += cnt;
                    case "DangDon"      -> r[4] += cnt;
                    case "BaoTri"       -> r[5] += cnt;
                }
            }
        } catch (Exception ignored) {}
        return r;
    }

    /** Danh sách tối đa 5 khách đang check-in. */
    private List<String[]> queryCurrentGuests() {
        List<String[]> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;
        String sql = "SELECT TOP 5 kh.hoTenKH, ct.maPhong "
                   + "FROM PhieuDatPhong pdp "
                   + "JOIN KhachHang kh ON kh.maKH = pdp.maKhachHang "
                   + "JOIN ChiTietPhieuDatPhong ct ON ct.maPhieuDatPhong = pdp.maPhieuDatPhong "
                   + "WHERE pdp.trangThai = N'DaCheckIn'";
        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next())
                list.add(new String[]{rs.getString(1), rs.getString(2)});
        } catch (Exception ignored) {}
        return list;
    }

    // ---------------------------------------------------------------- HELPERS
    private String formatVND(double amount) {
        if (amount == 0) return "0đ";
        NumberFormat nf = NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        return nf.format((long) amount) + "đ";
    }
}
