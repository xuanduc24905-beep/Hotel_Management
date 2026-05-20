package com.lotuslaverne.fx.views;

import com.lotuslaverne.dao.PhieuDatPhongDAO;
import com.lotuslaverne.service.CheckInService;
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

    private ObservableList<Object[]> items;
    private TableView<Object[]> table;

    public Node build() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #F0F2F5;");

        VBox content = new VBox(20);
        content.setPadding(new Insets(28));
        content.setStyle("-fx-background-color: #F0F2F5;");
        VBox.setVgrow(content, Priority.ALWAYS);

        // ── Header ──
        VBox header = new VBox(4);
        Label title = new Label("Tiếp Nhận Khách (Check-in)");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Label sub = new Label("Danh sách phiếu đặt phòng đang chờ check-in hôm nay");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #8C8C8C;");
        header.getChildren().addAll(title, sub);

        // ── Banner hướng dẫn ──
        HBox banner = new HBox(8);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setPadding(new Insets(12, 16, 12, 16));
        banner.setStyle("-fx-background-color: #E6F4FF; -fx-background-radius: 8;"
                + "-fx-border-color: #1890FF; -fx-border-width: 1; -fx-border-radius: 8;");
        Label bannerLbl = new Label("ℹ  Chọn phiếu trong bảng rồi nhấn [Xác Nhận Check-in] — hệ thống sẽ ghi thời gian nhận phòng thực tế và đổi trạng thái phòng sang 'Đang có khách'.");
        bannerLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #0050B3;");
        bannerLbl.setWrapText(true);
        banner.getChildren().add(bannerLbl);

        // ── Toolbar ──
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(12, 16, 12, 16));
        toolbar.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        TextField search = new TextField();
        search.setPromptText("🔍  Tìm mã phiếu, tên khách, ghi chú...");
        search.setPrefWidth(280);
        search.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #E8E8E8;"
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 1;"
                + "-fx-padding: 8 12; -fx-font-size: 13px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnRefresh = new Button("↻  Làm Mới");
        btnRefresh.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #595959;"
                + "-fx-background-radius: 8; -fx-border-color: #D9D9D9; -fx-border-width: 1;"
                + "-fx-border-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");

        toolbar.getChildren().addAll(search, spacer, btnRefresh);

        // ── Table ──
        items = FXCollections.observableArrayList(loadData());
        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        // Sự kiện Làm Mới
        btnRefresh.setOnAction(e -> {
            items.setAll(loadData());
            table.setItems(items);
            com.lotuslaverne.fx.UiUtils.flashButton(btnRefresh, "✓ Đã làm mới");
        });

        // Sự kiện Tìm kiếm live
        search.textProperty().addListener((obs, o, n) -> {
            if (n == null || n.trim().isEmpty()) {
                table.setItems(items);
            } else {
                String kw = n.toLowerCase();
                ObservableList<Object[]> filtered = FXCollections.observableArrayList();
                for (Object[] r : items) {
                    for (Object cell : r) {
                        if (cell != null && cell.toString().toLowerCase().contains(kw)) {
                            filtered.add(r); break;
                        }
                    }
                }
                table.setItems(filtered);
            }
        });

        // ── Nút Check-in ──
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

        // Cột Mã Phiếu (in đậm xanh)
        TableColumn<Object[], String> colMa = new TableColumn<>("Mã Phiếu");
        colMa.setCellValueFactory(p -> new SimpleStringProperty(p.getValue()[0].toString()));
        colMa.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? null : s);
                refresh();
            }
            @Override public void updateSelected(boolean sel) {
                super.updateSelected(sel);
                if (!isEmpty()) refresh();
            }
            private void refresh() {
                setStyle(isEmpty() ? "" : (isSelected()
                    ? "-fx-font-weight: bold; -fx-text-fill: white;"
                    : "-fx-font-weight: bold; -fx-text-fill: #1890FF;"));
            }
        });

        // Các cột thông thường
        String[] heads = {"Tên Khách Hàng", "Nhân Viên", "Số Khách", "Giờ Nhận Dự Kiến", "Giờ Trả Dự Kiến", "Ghi Chú", "Phòng"};
        for (int i = 0; i < heads.length; i++) {
            final int idx = i + 1;   // offset +1 vì cột 0 đã xử lý riêng
            TableColumn<Object[], String> col = new TableColumn<>(heads[i]);
            col.setCellValueFactory(p -> {
                Object v = idx < p.getValue().length ? p.getValue()[idx] : "";
                return new SimpleStringProperty(v != null ? v.toString() : "");
            });
            tbl.getColumns().add(col);
        }
        tbl.getColumns().add(0, colMa);  // thêm cột Mã vào đầu

        tbl.setItems(items);
        tbl.setPlaceholder(new Label("Không có phiếu nào đang chờ check-in."));
        return tbl;
    }

    private void handleCheckIn() {
        Object[] selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn một phiếu đặt phòng trong bảng!");
            return;
        }
        String maPDP   = selected[0].toString();
        String tenKhach = selected[1].toString();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Xác nhận check-in phiếu: " + maPDP + "\nKhách hàng: " + tenKhach + "\n\nHệ thống sẽ ghi nhận thời gian nhận phòng ngay bây giờ.");
        confirm.setHeaderText("Xác Nhận Check-in");
        confirm.setTitle("Tiếp Nhận Khách");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    new CheckInService().checkIn(maPDP);
                    alert(Alert.AlertType.INFORMATION, "Check-in Thành Công",
                            "✅ Check-in thành công!\nKhách: " + tenKhach
                            + "\nTrạng thái phòng → Đang có khách.\nPhiếu: " + maPDP);
                    items.setAll(loadData());
                    table.setItems(items);
                } catch (IllegalStateException ex) {
                    alert(Alert.AlertType.ERROR, "Lỗi Check-in", ex.getMessage());
                } catch (Exception ex) {
                    alert(Alert.AlertType.ERROR, "Lỗi Kết Nối", "Không thể kết nối cơ sở dữ liệu!\n" + ex.getMessage());
                }
            }
        });
    }

    private List<Object[]> loadData() {
        List<Object[]> result = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (Object[] row : new PhieuDatPhongDAO().getChuaCheckInJoined()) {
            java.sql.Timestamp tNhan = (java.sql.Timestamp) row[4];
            java.sql.Timestamp tTra  = (java.sql.Timestamp) row[5];
            result.add(new Object[]{
                row[0],
                row[1] != null ? row[1] : "—",
                row[2],
                String.valueOf(row[3]),
                tNhan != null ? sdf.format(tNhan) : "—",
                tTra  != null ? sdf.format(tTra)  : "—",
                row[6] != null ? row[6] : "",
                row.length > 7 && row[7] != null ? row[7] : "—"
            });
        }
        return result;
    }

    private void alert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type, msg);
        a.setHeaderText(null);
        a.setTitle(title);
        a.showAndWait();
    }
}
