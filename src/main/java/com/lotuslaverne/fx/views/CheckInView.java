package com.lotuslaverne.fx.views;

import com.lotuslaverne.dao.PhieuDatPhongDAO;
import com.lotuslaverne.entity.PhieuDatPhong;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class CheckInView {

    private static final Object[][] DEMO_DATA = {
        {"PDP001", "KH001", "NV001", "2", "24/04/2026 14:00", "27/04/2026 12:00", ""},
        {"PDP002", "KH002", "NV002", "1", "25/04/2026 13:00", "28/04/2026 12:00", "Phòng nhìn ra biển"},
        {"PDP003", "KH003", "NV001", "3", "25/04/2026 15:00", "30/04/2026 12:00", "Thêm giường phụ"},
    };

    private ObservableList<Object[]> items;
    private TableView<Object[]> table;

    public Node build() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #F0F2F5;");

        VBox content = new VBox(20);
        content.setPadding(new Insets(28));
        content.setStyle("-fx-background-color: #F0F2F5;");
        VBox.setVgrow(content, Priority.ALWAYS);

        // Header
        VBox header = new VBox(4);
        Label title = new Label("Check-in Khách");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Label sub = new Label("Danh sách phiếu đặt phòng chưa check-in");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #8C8C8C;");
        header.getChildren().addAll(title, sub);

        // Info banner
        HBox banner = new HBox(8);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setPadding(new Insets(12, 16, 12, 16));
        banner.setStyle("-fx-background-color: #E6F4FF; -fx-background-radius: 8;"
                + "-fx-border-color: #1890FF; -fx-border-width: 1; -fx-border-radius: 8;");
        Label bannerLbl = new Label("ℹ  Chọn phiếu đặt phòng trong bảng và nhấn [Xác Nhận Check-in] để nhận phòng cho khách.");
        bannerLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #0050B3;");
        banner.getChildren().add(bannerLbl);

        // Toolbar
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(12, 16, 12, 16));
        toolbar.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        TextField search = new TextField();
        search.setPromptText("🔍  Tìm mã phiếu, mã khách...");
        search.setPrefWidth(260);
        search.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #E8E8E8;"
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 1;"
                + "-fx-padding: 8 12; -fx-font-size: 13px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnRefresh = new Button("↻ Tải Lại");
        btnRefresh.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #595959;"
                + "-fx-background-radius: 8; -fx-border-color: #D9D9D9; -fx-border-width: 1;"
                + "-fx-border-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");

        toolbar.getChildren().addAll(search, spacer, btnRefresh);

        // Table
        items = FXCollections.observableArrayList(loadData());
        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        btnRefresh.setOnAction(e -> {
            items.setAll(loadData());
            table.setItems(items);
            com.lotuslaverne.fx.UiUtils.flashButton(btnRefresh, "✓ Đã làm mới");
        });

        search.textProperty().addListener((obs, o, n) -> {
            if (n.trim().isEmpty()) {
                table.setItems(items);
            } else {
                String kw = n.toLowerCase();
                ObservableList<Object[]> filtered = FXCollections.observableArrayList();
                for (Object[] r : items) {
                    for (Object cell : r) {
                        if (cell != null && cell.toString().toLowerCase().contains(kw)) {
                            filtered.add(r);
                            break;
                        }
                    }
                }
                table.setItems(filtered);
            }
        });

        // Bottom button
        Button btnCheckIn = new Button("✅  XÁC NHẬN CHECK-IN");
        btnCheckIn.setStyle("-fx-background-color: #52C41A; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-padding: 12 32; -fx-font-size: 14px;"
                + "-fx-font-weight: bold; -fx-cursor: hand;");
        btnCheckIn.setOnAction(e -> handleCheckIn());

        HBox bottomBar = new HBox(btnCheckIn);
        bottomBar.setAlignment(Pos.CENTER_RIGHT);
        bottomBar.setPadding(new Insets(8, 0, 0, 0));

        content.getChildren().addAll(header, banner, toolbar, table, bottomBar);
        root.getChildren().add(content);
        VBox.setVgrow(root, Priority.ALWAYS);
        return root;
    }

    private TableView<Object[]> buildTable() {
        TableView<Object[]> tbl = new TableView<>();
        tbl.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tbl.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-border-radius: 10; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        String[] heads = {"Mã Phiếu", "Mã Khách", "Mã NV", "Số Người", "Giờ Nhận DK", "Giờ Trả DK", "Ghi Chú"};
        for (int i = 0; i < heads.length; i++) {
            final int idx = i;
            TableColumn<Object[], String> col = new TableColumn<>(heads[i]);
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
            tbl.getColumns().add(col);
        }

        tbl.setItems(items);
        tbl.setPlaceholder(new Label("Không có phiếu chờ check-in."));
        return tbl;
    }

    private void handleCheckIn() {
        Object[] selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn phiếu đặt phòng cần check-in!");
            return;
        }
        String maPDP = selected[0].toString();
        String maKH  = selected[1].toString();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Xác nhận check-in phiếu:\n" + maPDP + "\nKhách hàng: " + maKH);
        confirm.setHeaderText(null);
        confirm.setTitle("Xác Nhận Check-in");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    if (new PhieuDatPhongDAO().checkIn(maPDP)) {
                        alert(Alert.AlertType.INFORMATION, "Thành công",
                                "Check-in thành công!\nTrạng thái phòng → Đang sử dụng.");
                        items.setAll(loadData());
                        table.setItems(items);
                    } else {
                        alert(Alert.AlertType.ERROR, "Lỗi",
                                "Check-in thất bại! Phiếu có thể đã check-in hoặc không hợp lệ.");
                    }
                } catch (Exception ex) {
                    alert(Alert.AlertType.ERROR, "Lỗi DB", "Lỗi kết nối cơ sở dữ liệu!");
                }
            }
        });
    }

    private List<Object[]> loadData() {
        List<Object[]> result = new ArrayList<>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            List<PhieuDatPhong> list = new PhieuDatPhongDAO().getChuaCheckIn();
            if (!list.isEmpty()) {
                for (PhieuDatPhong p : list) {
                    result.add(new Object[]{
                        p.getMaPhieuDatPhong(), p.getMaKhachHang(), p.getMaNhanVien(),
                        String.valueOf(p.getSoNguoi()),
                        p.getThoiGianNhanDuKien() != null ? sdf.format(p.getThoiGianNhanDuKien()) : "",
                        p.getThoiGianTraDuKien()  != null ? sdf.format(p.getThoiGianTraDuKien())  : "",
                        p.getGhiChu()
                    });
                }
                return result;
            }
        } catch (Exception ignored) {}
        for (Object[] r : DEMO_DATA) result.add(r);
        return result;
    }

    private void alert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type, msg);
        a.setHeaderText(null);
        a.setTitle(title);
        a.showAndWait();
    }
}
