package com.lotuslaverne.fx.views;

import com.lotuslaverne.dao.PhieuDatPhongDAO;
import com.lotuslaverne.dao.PhongDAO;
import com.lotuslaverne.service.CheckInService;
import com.lotuslaverne.service.WalkInService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
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

        Button btnWalkIn = new Button("🚶  Walk-in (Không Đặt Trước)");
        btnWalkIn.setStyle("-fx-background-color: #FA8C16; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand; -fx-font-weight: bold;");
        btnWalkIn.setOnAction(e -> openWalkInDialog());

        toolbar.getChildren().addAll(search, spacer, btnRefresh, btnWalkIn);

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

    // ── Walk-in dialog: tạo phiếu + thu cọc + check-in ngay (Rule 12)
    private void openWalkInDialog() {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Walk-in — Nhận Phòng Không Đặt Trước");
        dlg.setResizable(false);

        GridPane form = new GridPane();
        form.setHgap(12); form.setVgap(12);
        form.setPadding(new Insets(20));
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPrefWidth(130);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPrefWidth(220);
        form.getColumnConstraints().addAll(c1, c2);

        TextField txtHoTen = field(""); txtHoTen.setPromptText("Họ tên khách *");
        TextField txtSdt   = field(""); txtSdt.setPromptText("Số điện thoại *");
        TextField txtCmnd  = field(""); txtCmnd.setPromptText("CMND / CCCD");

        // Danh sách phòng trống
        ComboBox<String> cbPhong = new ComboBox<>();
        cbPhong.setMaxWidth(Double.MAX_VALUE);
        cbPhong.setPromptText("Chọn phòng trống...");
        try {
            for (var p : new PhongDAO().getPhongTrong()) cbPhong.getItems().add(p.getMaPhong());
        } catch (Exception ignored) {}

        DatePicker dpNgayTra = new DatePicker(LocalDate.now().plusDays(1));
        dpNgayTra.setMaxWidth(Double.MAX_VALUE);

        Label lblCoc = new Label("—");
        lblCoc.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #FA8C16;");

        // Preview cọc khi chọn phòng / ngày trả
        Runnable updateCoc = () -> {
            String mp = cbPhong.getValue();
            LocalDate tra = dpNgayTra.getValue();
            if (mp == null || tra == null) { lblCoc.setText("—"); return; }
            try {
                long nights = Math.max(1, LocalDate.now().until(tra).getDays());
                double gia  = new com.lotuslaverne.dao.BangGiaDAO().getDonGiaQuaDem(mp);
                lblCoc.setText(gia > 0
                        ? String.format("%,.0f đ  (50%% × %,.0f × %d đêm)", 0.5 * gia * nights, gia, nights)
                        : "Không có giá hiệu lực");
            } catch (Exception ex) { lblCoc.setText("—"); }
        };
        cbPhong.setOnAction(e -> updateCoc.run());
        dpNgayTra.setOnAction(e -> updateCoc.run());

        form.add(lbl("Họ Tên *:"),    0, 0); form.add(txtHoTen,  1, 0);
        form.add(lbl("SĐT *:"),       0, 1); form.add(txtSdt,    1, 1);
        form.add(lbl("CMND:"),        0, 2); form.add(txtCmnd,   1, 2);
        form.add(lbl("Phòng *:"),     0, 3); form.add(cbPhong,   1, 3);
        form.add(lbl("Ngày trả *:"),  0, 4); form.add(dpNgayTra, 1, 4);
        form.add(lbl("Tiền cọc:"),    0, 5); form.add(lblCoc,    1, 5);

        Button btnOk = new Button("✅  Nhận Phòng & Check-in");
        btnOk.setStyle("-fx-background-color: #52C41A; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: bold; -fx-cursor: hand;");
        Button btnCancel = new Button("Huỷ");
        btnCancel.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #595959;"
                + "-fx-background-radius: 8; -fx-border-color: #D9D9D9; -fx-border-width: 1;"
                + "-fx-border-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> dlg.close());

        btnOk.setOnAction(e -> {
            String hoTen = txtHoTen.getText().trim();
            String sdt   = txtSdt.getText().trim();
            String maPhong = cbPhong.getValue();
            LocalDate ngayTra = dpNgayTra.getValue();

            if (hoTen.isEmpty() || sdt.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Vui lòng nhập họ tên và số điện thoại!").showAndWait(); return;
            }
            if (maPhong == null) {
                new Alert(Alert.AlertType.WARNING, "Vui lòng chọn phòng!").showAndWait(); return;
            }
            if (ngayTra == null || !ngayTra.isAfter(LocalDate.now())) {
                new Alert(Alert.AlertType.WARNING, "Ngày trả phải sau hôm nay!").showAndWait(); return;
            }
            try {
                String maPDP = new WalkInService().walkIn(hoTen, sdt, txtCmnd.getText().trim(), maPhong, ngayTra);
                dlg.close();
                items.setAll(loadData());
                table.setItems(items);
                alert(Alert.AlertType.INFORMATION, "Walk-in Thành Công",
                        "Check-in thành công!\nPhòng: " + maPhong + "\nPhiếu: " + maPDP
                        + "\nCọc 50% đã ghi nhận. Vui lòng thu tiền mặt.");
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Lỗi: " + ex.getMessage()).showAndWait();
            }
        });

        HBox btnRow = new HBox(10, btnCancel, btnOk);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(0, 20, 16, 20));

        VBox root = new VBox(form, btnRow);
        root.setStyle("-fx-background-color: #FFFFFF;");
        dlg.setScene(new Scene(root, 400, 360));
        dlg.showAndWait();
    }

    private Label lbl(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #595959;");
        return l;
    }

    private TextField field(String val) {
        TextField tf = new TextField(val);
        tf.setMaxWidth(Double.MAX_VALUE);
        tf.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D9D9D9;"
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1; -fx-padding: 7 10;");
        return tf;
    }
}
