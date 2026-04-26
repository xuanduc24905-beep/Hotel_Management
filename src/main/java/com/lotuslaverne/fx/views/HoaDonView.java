package com.lotuslaverne.fx.views;

import com.lotuslaverne.dao.HoaDonDAO;
import com.lotuslaverne.entity.HoaDon;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class HoaDonView {

    private static final Object[][] DEMO_HD = {
        {"HD001", "24/04/2026 08:00", "NV001", "PDP001", "24/04/2026 10:00", "100,000",   "850,000",   "TienMat",    "Checkout"},
        {"HD002", "23/04/2026 11:30", "NV002", "PDP002", "23/04/2026 12:00", "200,000", "1,500,000",   "ChuyenKhoan","Checkout"},
        {"HD003", "22/04/2026 09:00", "NV001", "PDP003", "22/04/2026 09:30", "0",         "750,000",   "TienMat",    ""},
        {"HD004", "21/04/2026 14:00", "NV003", "PDP004", "21/04/2026 15:00", "300,000", "2,700,000",   "ChuyenKhoan","Checkout"},
        {"HD005", "20/04/2026 10:00", "NV002", "PDP005", "20/04/2026 10:30", "0",         "600,000",   "TienMat",    ""},
    };

    public Node build() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #F0F2F5;");

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #F0F2F5; -fx-border-color: transparent;");

        VBox content = new VBox(20);
        content.setPadding(new Insets(28));
        content.setStyle("-fx-background-color: #F0F2F5;");

        VBox header = new VBox(4);
        Label title = new Label("Quản Lý Hóa Đơn");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Label sub = new Label("Tra cứu và quản lý hóa đơn thanh toán");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #8C8C8C;");
        header.getChildren().addAll(title, sub);

        List<Object[]> data = loadData();

        HBox searchBar = buildSearchBar(data);
        TableView<Object[]> table = buildTable(data);
        table.setPrefHeight(480);

        content.getChildren().addAll(header, searchBar, table);
        scroll.setContent(content);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        root.getChildren().add(scroll);
        return root;
    }

    private HBox buildSearchBar(List<Object[]> data) {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(14, 16, 14, 16));
        bar.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        TextField search = new TextField();
        search.setPromptText("🔍  Tìm mã HD, mã phiếu, nhân viên...");
        search.setPrefWidth(300);
        search.setStyle(fieldStyle());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        double total = 0;
        for (Object[] r : data) total += parseMoney(r[6].toString());
        Label revLbl = new Label(String.format("Tổng doanh thu: %,.0f VNĐ", total));
        revLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #FF4D4F;");

        bar.getChildren().addAll(search, spacer, revLbl);
        return bar;
    }

    @SuppressWarnings("unchecked")
    private TableView<Object[]> buildTable(List<Object[]> data) {
        TableView<Object[]> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-border-radius: 10; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        String[] headers = {
            "Mã Hóa Đơn", "Ngày Lập", "NV Lập", "Mã Phiếu ĐP",
            "Ngày TT", "Tiền KM (VNĐ)", "Thành Tiền (VNĐ)", "Phương Thức", "Ghi Chú"
        };
        for (int i = 0; i < headers.length; i++) {
            final int idx = i;
            TableColumn<Object[], String> col = new TableColumn<>(headers[i]);
            col.setCellValueFactory(p -> {
                Object v = idx < p.getValue().length ? p.getValue()[idx] : "";
                return new SimpleStringProperty(v != null ? v.toString() : "");
            });
            if (i == 0) {
                col.setCellFactory(tc -> new TableCell<>() {
                    @Override protected void updateItem(String s, boolean empty) {
                        super.updateItem(s, empty);
                        setText(empty || s == null ? null : s);
                        setStyle(empty ? "" : "-fx-font-weight: bold; -fx-text-fill: #1890FF;");
                    }
                });
            }
            if (i == 7) {
                col.setCellFactory(tc -> new TableCell<>() {
                    @Override protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) { setGraphic(null); setText(null); return; }
                        Label badge = new Label(item);
                        String bg = "TienMat".equals(item) ? "#F6FFED" : "#E6F4FF";
                        String fg = "TienMat".equals(item) ? "#52C41A" : "#1890FF";
                        badge.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";"
                                + "-fx-padding: 2 8 2 8; -fx-background-radius: 10; -fx-font-size: 11px; -fx-font-weight: bold;");
                        setGraphic(badge); setText(null);
                    }
                });
            }
            table.getColumns().add(col);
        }

        table.setItems(FXCollections.observableArrayList(data));
        table.setPlaceholder(new Label("Không có dữ liệu hóa đơn."));
        return table;
    }

    private List<Object[]> loadData() {
        List<Object[]> result = new ArrayList<>();
        try {
            HoaDonDAO dao = new HoaDonDAO();
            List<HoaDon> list = dao.getAll();
            if (!list.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                DecimalFormat df = new DecimalFormat("#,###");
                for (HoaDon hd : list) {
                    result.add(new Object[]{
                        hd.getMaHoaDon(),
                        hd.getNgayLap() != null ? sdf.format(hd.getNgayLap()) : "",
                        hd.getMaNhanVienLap(),
                        hd.getMaPhieuDatPhong(),
                        hd.getNgayThanhToan() != null ? sdf.format(hd.getNgayThanhToan()) : "",
                        df.format(hd.getTienKhuyenMai()),
                        df.format(hd.getTienThanhToan()),
                        hd.getPhuongThucThanhToan(),
                        hd.getGhiChu()
                    });
                }
                return result;
            }
        } catch (Exception ignored) {}
        for (Object[] r : DEMO_HD) result.add(r);
        return result;
    }

    private double parseMoney(String s) {
        try { return Double.parseDouble(s.replaceAll("[^0-9.]", "")); } catch (Exception e) { return 0; }
    }

    private String fieldStyle() {
        return "-fx-background-color: #F5F5F5; -fx-border-color: #E8E8E8;"
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 1;"
                + "-fx-padding: 8 12 8 12; -fx-font-size: 13px;";
    }
}
