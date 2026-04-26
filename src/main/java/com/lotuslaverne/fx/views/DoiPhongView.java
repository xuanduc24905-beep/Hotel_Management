package com.lotuslaverne.fx.views;

import com.lotuslaverne.dao.PhongDAO;
import com.lotuslaverne.entity.Phong;
import com.lotuslaverne.util.ConnectDB;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class DoiPhongView {

    private static final Object[][] DEMO_PHONG_TRONG = {
        {"P103", "103", "Deluxe",   "Trống"},
        {"P201", "201", "Standard", "Trống"},
        {"P304", "304", "Suite",    "Trống"},
    };

    private ObservableList<Object[]> phongItems;
    private TableView<Object[]> phongTable;

    // Inputs / info labels
    private TextField txtSDT;
    private Label lblTenKH, lblCCCD, lblMaPDP, lblPhongCu, lblLoaiCu, lblGiaCu;
    private Label lblNgayNhan, lblNgayTra, lblDemDaO, lblDemConLai;

    // Bù trừ labels
    private Label lblPhongMoi, lblGiaMoi, lblChenhLech, lblTongBuTru;

    // Resolved state after tracuu
    private String resolvedMaPDP = null;
    private String resolvedMaPhongCu = null;
    private double resolvedGiaCu = 0;
    private long soDemConLai = 0;

    public Node build() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #F0F2F5;");

        VBox content = new VBox(20);
        content.setPadding(new Insets(28));
        content.setStyle("-fx-background-color: #F0F2F5;");
        VBox.setVgrow(content, Priority.ALWAYS);

        VBox header = new VBox(4);
        Label title = new Label("Đổi Phòng");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Label sub = new Label("Tra cứu theo SĐT khách, đổi phòng và tự động tính tiền bù trừ");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #8C8C8C;");
        header.getChildren().addAll(title, sub);

        HBox mainRow = new HBox(20);
        mainRow.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(mainRow, Priority.ALWAYS);

        VBox leftCard  = buildLeftCard();
        VBox rightCard = buildRightCard();
        HBox.setHgrow(leftCard,  Priority.ALWAYS);
        HBox.setHgrow(rightCard, Priority.ALWAYS);
        mainRow.getChildren().addAll(leftCard, rightCard);

        Button btnDoi = new Button("🔄  XÁC NHẬN ĐỔI PHÒNG");
        btnDoi.setStyle("-fx-background-color: #FF4D4F; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-padding: 12 32; -fx-font-size: 14px;"
                + "-fx-font-weight: bold; -fx-cursor: hand;");
        btnDoi.setOnAction(e -> handleDoiPhong());

        HBox bottomBar = new HBox(btnDoi);
        bottomBar.setAlignment(Pos.CENTER_RIGHT);
        bottomBar.setPadding(new Insets(4, 0, 0, 0));

        content.getChildren().addAll(header, mainRow, bottomBar);
        root.getChildren().add(content);
        VBox.setVgrow(root, Priority.ALWAYS);
        return root;
    }

    private VBox buildLeftCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        Label cardTitle = new Label("Thông Tin Khách & Phòng");
        cardTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;"
                + "-fx-border-color: transparent transparent #F0F2F5 transparent;"
                + "-fx-border-width: 0 0 1 0; -fx-padding: 0 0 10 0;");

        // ---- Tra cứu bằng SĐT ----
        HBox searchRow = new HBox(8);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        txtSDT = field("");
        txtSDT.setPromptText("VD: 0912345678");
        HBox.setHgrow(txtSDT, Priority.ALWAYS);
        Button btnTraCuu = new Button("🔍 Tra Cứu");
        btnTraCuu.setStyle("-fx-background-color: #1890FF; -fx-text-fill: white;"
                + "-fx-background-radius: 6; -fx-padding: 8 16; -fx-cursor: hand; -fx-font-size: 12px;");
        btnTraCuu.setOnAction(e -> traCuuTheoSDT());
        txtSDT.setOnAction(e -> traCuuTheoSDT());
        searchRow.getChildren().addAll(lbl("SĐT khách:"), txtSDT, btnTraCuu);

        // ---- Customer info panel ----
        GridPane info = new GridPane();
        info.setHgap(10);
        info.setVgap(8);
        info.setPadding(new Insets(10, 0, 0, 0));
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(40);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(60);
        info.getColumnConstraints().addAll(c1, c2);

        lblTenKH    = infoValue();
        lblCCCD     = infoValue();
        lblMaPDP    = infoValue();
        lblPhongCu  = infoValue();
        lblLoaiCu   = infoValue();
        lblGiaCu    = infoValue();
        lblNgayNhan = infoValue();
        lblNgayTra  = infoValue();
        lblDemDaO   = infoValue();
        lblDemConLai= infoValue();

        int row = 0;
        info.add(lbl("Tên khách:"),        0, row); info.add(lblTenKH,    1, row++);
        info.add(lbl("CCCD:"),             0, row); info.add(lblCCCD,     1, row++);
        info.add(lbl("Mã phiếu đặt:"),     0, row); info.add(lblMaPDP,    1, row++);
        info.add(lbl("Phòng hiện tại:"),   0, row); info.add(lblPhongCu,  1, row++);
        info.add(lbl("Loại phòng:"),       0, row); info.add(lblLoaiCu,   1, row++);
        info.add(lbl("Giá hiện tại:"),     0, row); info.add(lblGiaCu,    1, row++);
        info.add(lbl("Ngày nhận:"),        0, row); info.add(lblNgayNhan, 1, row++);
        info.add(lbl("Ngày dự kiến trả:"), 0, row); info.add(lblNgayTra,  1, row++);
        info.add(lbl("Đã ở:"),             0, row); info.add(lblDemDaO,   1, row++);
        info.add(lbl("Số đêm còn lại:"),   0, row); info.add(lblDemConLai,1, row++);

        // ---- Bù trừ panel ----
        VBox buTruBox = new VBox(6);
        buTruBox.setPadding(new Insets(12));
        buTruBox.setStyle("-fx-background-color: #FFFBE6; -fx-background-radius: 8;"
                + "-fx-border-color: #FFE58F; -fx-border-width: 1; -fx-border-radius: 8;");
        Label buTruTitle = new Label("💵  Tính Tiền Bù Trừ (áp cho số đêm còn lại)");
        buTruTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #D48806;");
        lblPhongMoi  = infoValue();
        lblGiaMoi    = infoValue();
        lblChenhLech = infoValue();
        lblTongBuTru = new Label("—");
        lblTongBuTru.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #FF4D4F;");

        GridPane buTruGrid = new GridPane();
        buTruGrid.setHgap(10); buTruGrid.setVgap(6);
        ColumnConstraints bc1 = new ColumnConstraints(); bc1.setPercentWidth(45);
        ColumnConstraints bc2 = new ColumnConstraints(); bc2.setPercentWidth(55);
        buTruGrid.getColumnConstraints().addAll(bc1, bc2);
        int br = 0;
        buTruGrid.add(lbl("Phòng mới:"),    0, br); buTruGrid.add(lblPhongMoi,  1, br++);
        buTruGrid.add(lbl("Giá mới/đêm:"),  0, br); buTruGrid.add(lblGiaMoi,    1, br++);
        buTruGrid.add(lbl("Chênh lệch/đêm:"),0,br); buTruGrid.add(lblChenhLech, 1, br++);
        buTruGrid.add(lbl("Tổng bù trừ:"),  0, br); buTruGrid.add(lblTongBuTru, 1, br++);

        buTruBox.getChildren().addAll(buTruTitle, buTruGrid);

        Label hint = new Label("→ Chọn phòng mới từ bảng bên phải để tự động tính");
        hint.setStyle("-fx-font-size: 12px; -fx-text-fill: #8C8C8C; -fx-font-style: italic;");

        card.getChildren().addAll(cardTitle, searchRow, info, buTruBox, hint);
        return card;
    }

    private VBox buildRightCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        HBox cardHeader = new HBox(8);
        cardHeader.setAlignment(Pos.CENTER_LEFT);
        Label cardTitle = new Label("Danh Sách Phòng Trống");
        cardTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;"
                + "-fx-border-color: transparent transparent #F0F2F5 transparent;"
                + "-fx-border-width: 0 0 1 0; -fx-padding: 0 0 10 0;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button btnLoad = new Button("↻ Tải Lại");
        btnLoad.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #595959;"
                + "-fx-background-radius: 6; -fx-border-color: #D9D9D9; -fx-border-width: 1;"
                + "-fx-border-radius: 6; -fx-padding: 5 12; -fx-cursor: hand; -fx-font-size: 12px;");
        btnLoad.setOnAction(e -> {
            phongItems.setAll(loadPhongTrong());
            phongTable.setItems(phongItems);
            com.lotuslaverne.fx.UiUtils.flashButton(btnLoad, "✓ Đã tải");
        });
        cardHeader.getChildren().addAll(cardTitle, sp, btnLoad);

        phongItems = FXCollections.observableArrayList(loadPhongTrong());
        phongTable = buildPhongTable();
        VBox.setVgrow(phongTable, Priority.ALWAYS);

        // Selection listener → auto tính bù trừ
        phongTable.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) tinhBuTru(n[0].toString());
        });

        card.getChildren().addAll(cardHeader, phongTable);
        VBox.setVgrow(card, Priority.ALWAYS);
        return card;
    }

    private TableView<Object[]> buildPhongTable() {
        TableView<Object[]> tbl = new TableView<>();
        tbl.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tbl.setStyle("-fx-background-color: #F9F9F9; -fx-background-radius: 8;"
                + "-fx-border-color: #E8E8E8; -fx-border-width: 1; -fx-border-radius: 8;");

        String[] heads = {"Mã Phòng", "Tên Phòng", "Loại Phòng", "Trạng Thái"};
        for (int i = 0; i < heads.length; i++) {
            final int idx = i;
            TableColumn<Object[], String> col = new TableColumn<>(heads[i]);
            col.setCellValueFactory(p -> {
                Object v = idx < p.getValue().length ? p.getValue()[idx] : "";
                return new SimpleStringProperty(v != null ? v.toString() : "");
            });
            tbl.getColumns().add(col);
        }
        tbl.setItems(phongItems);
        tbl.setPlaceholder(new Label("Không có phòng trống."));
        return tbl;
    }

    /** Tra cứu khách theo SĐT → load phiếu đang ở mới nhất */
    private void traCuuTheoSDT() {
        resetInfo();
        String sdt = txtSDT.getText().trim();
        if (sdt.isEmpty()) {
            alert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập số điện thoại khách!");
            return;
        }
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) {
            alert(Alert.AlertType.ERROR, "Offline", "Không có kết nối DB, không tra cứu được.");
            return;
        }
        String sql =
            "SELECT TOP 1 kh.hoTenKH, kh.cmnd, " +
            "       pdp.maPhieuDatPhong, pdp.thoiGianNhanThucTe, pdp.thoiGianTra, " +
            "       ct.maPhong, lp.tenLoaiPhong, bg.donGia " +
            "FROM KhachHang kh " +
            "JOIN PhieuDatPhong pdp ON pdp.maKH = kh.maKH " +
            "JOIN ChiTietPhieuDatPhong ct ON ct.maPhieuDatPhong = pdp.maPhieuDatPhong " +
            "JOIN Phong p ON p.maPhong = ct.maPhong " +
            "JOIN LoaiPhong lp ON lp.maLoaiPhong = p.maLoaiPhong " +
            "LEFT JOIN BangGia bg ON bg.maLoaiPhong = p.maLoaiPhong " +
            "   AND bg.loaiThue = 'QuaDem' " +
            "   AND GETDATE() BETWEEN bg.ngayBatDau AND bg.ngayKetThuc " +
            "WHERE kh.soDienThoai = ? " +
            "  AND pdp.thoiGianNhanThucTe IS NOT NULL " +
            "  AND pdp.thoiGianTraThucTe IS NULL " +
            "ORDER BY pdp.thoiGianNhanThucTe DESC";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, sdt);
            try (ResultSet rs = pst.executeQuery()) {
                if (!rs.next()) {
                    alert(Alert.AlertType.WARNING, "Không tìm thấy",
                            "Không có khách nào với SĐT này đang lưu trú.");
                    return;
                }
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

                String tenKH  = rs.getString("hoTenKH");
                String cccd   = rs.getString("cmnd");
                resolvedMaPDP = rs.getString("maPhieuDatPhong");
                Timestamp tsNhan = rs.getTimestamp("thoiGianNhanThucTe");
                Timestamp tsTra  = rs.getTimestamp("thoiGianTra");
                resolvedMaPhongCu = rs.getString("maPhong");
                String tenLoaiCu = rs.getString("tenLoaiPhong");
                resolvedGiaCu    = rs.getDouble("donGia");

                LocalDateTime ldtNhan = tsNhan.toLocalDateTime();
                LocalDateTime ldtTra  = tsTra.toLocalDateTime();
                LocalDate today = LocalDate.now();

                long demDaO = Math.max(0, ChronoUnit.DAYS.between(ldtNhan.toLocalDate(), today));
                soDemConLai = Math.max(1, ChronoUnit.DAYS.between(today, ldtTra.toLocalDate()));

                lblTenKH.setText(tenKH);
                lblCCCD.setText(cccd != null ? cccd : "—");
                lblMaPDP.setText(resolvedMaPDP);
                lblPhongCu.setText(resolvedMaPhongCu);
                lblLoaiCu.setText(tenLoaiCu);
                lblGiaCu.setText(formatVND(resolvedGiaCu) + " / đêm");
                lblNgayNhan.setText(ldtNhan.format(fmt));
                lblNgayTra.setText(ldtTra.format(fmt));
                lblDemDaO.setText(demDaO + " đêm");
                lblDemConLai.setText(soDemConLai + " đêm");

                // Nếu đang có phòng đang chọn ở bảng phải → tính luôn bù trừ
                Object[] sel = phongTable.getSelectionModel().getSelectedItem();
                if (sel != null) tinhBuTru(sel[0].toString());
            }
        } catch (Exception ex) {
            alert(Alert.AlertType.ERROR, "Lỗi", "Tra cứu thất bại: " + ex.getMessage());
        }
    }

    /** Tính tiền bù trừ theo cách A — chênh lệch × số đêm còn lại */
    private void tinhBuTru(String maPhongMoi) {
        if (resolvedMaPDP == null) {
            // Chưa tra cứu khách → chỉ show giá phòng mới
            lblPhongMoi.setText(maPhongMoi);
            lblGiaMoi.setText("— (hãy tra cứu khách trước)");
            lblChenhLech.setText("—");
            lblTongBuTru.setText("—");
            return;
        }
        double giaMoi = loadDonGiaPhong(maPhongMoi);
        double chenh  = giaMoi - resolvedGiaCu;
        double tong   = chenh * soDemConLai;

        lblPhongMoi.setText(maPhongMoi);
        lblGiaMoi.setText(formatVND(giaMoi) + " / đêm");
        lblChenhLech.setText((chenh >= 0 ? "+" : "") + formatVND(chenh));
        if (tong > 0) {
            lblTongBuTru.setText("Thu thêm: " + formatVND(tong));
            lblTongBuTru.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #FF4D4F;");
        } else if (tong < 0) {
            lblTongBuTru.setText("Hoàn lại: " + formatVND(-tong));
            lblTongBuTru.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #52C41A;");
        } else {
            lblTongBuTru.setText("Không chênh lệch");
            lblTongBuTru.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #8C8C8C;");
        }
    }

    private double loadDonGiaPhong(String maPhong) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return 0;
        String sql =
            "SELECT bg.donGia FROM Phong p " +
            "LEFT JOIN BangGia bg ON bg.maLoaiPhong = p.maLoaiPhong " +
            "  AND bg.loaiThue = 'QuaDem' " +
            "  AND GETDATE() BETWEEN bg.ngayBatDau AND bg.ngayKetThuc " +
            "WHERE p.maPhong = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maPhong);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getDouble("donGia");
            }
        } catch (Exception ignored) {}
        return 0;
    }

    private void handleDoiPhong() {
        Object[] selected = phongTable.getSelectionModel().getSelectedItem();
        if (resolvedMaPDP == null) {
            alert(Alert.AlertType.WARNING, "Chưa tra cứu", "Nhập SĐT và bấm Tra Cứu trước."); return;
        }
        if (selected == null) {
            alert(Alert.AlertType.WARNING, "Chưa chọn phòng", "Vui lòng chọn phòng mới từ danh sách!"); return;
        }
        String maPhongMoi = selected[0].toString();
        if (maPhongMoi.equals(resolvedMaPhongCu)) {
            alert(Alert.AlertType.WARNING, "Không hợp lệ", "Phòng mới trùng phòng hiện tại!"); return;
        }
        double giaMoi = loadDonGiaPhong(maPhongMoi);
        double tongBuTru = (giaMoi - resolvedGiaCu) * soDemConLai;

        String msg = "Đổi từ phòng " + resolvedMaPhongCu + " → " + maPhongMoi
                + "\nGiá mới: " + formatVND(giaMoi) + " / đêm"
                + "\nSố đêm còn lại: " + soDemConLai
                + "\n" + (tongBuTru > 0 ? "⚠ Thu thêm từ khách: " + formatVND(tongBuTru)
                        : tongBuTru < 0 ? "↩ Hoàn lại cho khách: " + formatVND(-tongBuTru)
                        : "Không phát sinh chênh lệch");

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, msg);
        confirm.setHeaderText(null);
        confirm.setTitle("Xác Nhận Đổi Phòng");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK) return;
            try {
                Connection con = ConnectDB.getInstance().getConnection();
                if (con == null) throw new Exception("Không có kết nối DB");
                try (CallableStatement cs = con.prepareCall("{call SP_DoiPhong(?,?,?)}")) {
                    cs.setString(1, resolvedMaPDP);
                    cs.setString(2, maPhongMoi);
                    cs.setDouble(3, giaMoi);
                    cs.execute();
                    alert(Alert.AlertType.INFORMATION, "Thành công",
                            "Đổi phòng thành công!\nPhòng mới: " + maPhongMoi
                            + (tongBuTru != 0 ? "\n(Nhớ ghi nhận bù trừ vào hóa đơn)" : ""));
                    phongItems.setAll(loadPhongTrong());
                    resetInfo();
                    txtSDT.clear();
                }
            } catch (Exception ex) {
                alert(Alert.AlertType.ERROR, "Lỗi đổi phòng", ex.getMessage());
            }
        });
    }

    private void resetInfo() {
        resolvedMaPDP = null;
        resolvedMaPhongCu = null;
        resolvedGiaCu = 0;
        soDemConLai = 0;
        for (Label l : new Label[]{lblTenKH, lblCCCD, lblMaPDP, lblPhongCu, lblLoaiCu,
                                    lblGiaCu, lblNgayNhan, lblNgayTra, lblDemDaO, lblDemConLai,
                                    lblPhongMoi, lblGiaMoi, lblChenhLech}) {
            if (l != null) l.setText("—");
        }
        if (lblTongBuTru != null) {
            lblTongBuTru.setText("—");
            lblTongBuTru.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #8C8C8C;");
        }
    }

    private List<Object[]> loadPhongTrong() {
        List<Object[]> result = new ArrayList<>();
        try {
            List<Phong> list = new PhongDAO().getAll();
            for (Phong p : list) {
                if ("PhongTrong".equalsIgnoreCase(p.getTrangThai()) || "Trống".equalsIgnoreCase(p.getTrangThai())) {
                    result.add(new Object[]{p.getMaPhong(), p.getTenPhong(), p.getMaLoaiPhong(), p.getTrangThai()});
                }
            }
            if (!result.isEmpty()) return result;
        } catch (Exception ignored) {}
        for (Object[] r : DEMO_PHONG_TRONG) result.add(r);
        return result;
    }

    private TextField field(String val) {
        TextField tf = new TextField(val);
        tf.setMaxWidth(Double.MAX_VALUE);
        tf.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D9D9D9;"
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1; -fx-padding: 7 10;");
        return tf;
    }

    private Label lbl(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #595959;");
        return l;
    }

    private Label infoValue() {
        Label l = new Label("—");
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: #1A1A2E;");
        return l;
    }

    private String formatVND(double v) {
        return new DecimalFormat("#,###").format(v) + " VNĐ";
    }

    private void alert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type, msg);
        a.setHeaderText(null);
        a.setTitle(title);
        a.showAndWait();
    }
}
