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

public class GhepPhongView {

    private static final Object[][] DEMO_PHONG_TRONG = {
        {"P103", "103", "Deluxe",   "Trống"},
        {"P201", "201", "Standard", "Trống"},
        {"P304", "304", "Suite",    "Trống"},
    };

    private ObservableList<Object[]> phongTrongItems;
    private ObservableList<Object[]> phongDangCoItems;
    private TableView<Object[]> phongTrongTable;
    private TableView<Object[]> phongDangCoTable;

    private TextField txtSDT;
    private Label lblTenKH, lblCCCD, lblMaPDP, lblSoNguoi;
    private Label lblNgayNhan, lblNgayTra, lblDemConLai;
    private Label lblPhongChon, lblGiaPhong, lblTongCong;

    private String resolvedMaPDP = null;
    private long soDemConLai = 0;

    public Node build() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #F0F2F5;");

        VBox content = new VBox(20);
        content.setPadding(new Insets(28));
        content.setStyle("-fx-background-color: #F0F2F5;");
        VBox.setVgrow(content, Priority.ALWAYS);

        VBox header = new VBox(4);
        Label title = new Label("Ghép Phòng");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Label sub = new Label("Thêm phòng vào phiếu đang ở (cho nhóm khách thuê nhiều phòng cùng phiếu)");
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

        Button btnGhep = new Button("➕  XÁC NHẬN GHÉP PHÒNG");
        btnGhep.setStyle("-fx-background-color: #52C41A; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-padding: 12 32; -fx-font-size: 14px;"
                + "-fx-font-weight: bold; -fx-cursor: hand;");
        btnGhep.setOnAction(e -> handleGhepPhong());

        HBox bottomBar = new HBox(btnGhep);
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

        Label cardTitle = new Label("Thông Tin Phiếu Hiện Tại");
        cardTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;"
                + "-fx-border-color: transparent transparent #F0F2F5 transparent;"
                + "-fx-border-width: 0 0 1 0; -fx-padding: 0 0 10 0;");

        // Tra cứu SĐT
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

        // Customer info
        GridPane info = new GridPane();
        info.setHgap(10); info.setVgap(8);
        info.setPadding(new Insets(10, 0, 0, 0));
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(40);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(60);
        info.getColumnConstraints().addAll(c1, c2);

        lblTenKH    = infoValue();
        lblCCCD     = infoValue();
        lblMaPDP    = infoValue();
        lblSoNguoi  = infoValue();
        lblNgayNhan = infoValue();
        lblNgayTra  = infoValue();
        lblDemConLai = infoValue();

        int row = 0;
        info.add(lbl("Tên khách:"),        0, row); info.add(lblTenKH,     1, row++);
        info.add(lbl("CCCD:"),             0, row); info.add(lblCCCD,      1, row++);
        info.add(lbl("Mã phiếu đặt:"),     0, row); info.add(lblMaPDP,     1, row++);
        info.add(lbl("Số khách:"),         0, row); info.add(lblSoNguoi,   1, row++);
        info.add(lbl("Ngày nhận:"),        0, row); info.add(lblNgayNhan,  1, row++);
        info.add(lbl("Ngày dự kiến trả:"), 0, row); info.add(lblNgayTra,   1, row++);
        info.add(lbl("Số đêm còn lại:"),   0, row); info.add(lblDemConLai, 1, row++);

        // Phòng đang có trong phiếu
        Label sectionPhong = new Label("Phòng đang có trong phiếu này:");
        sectionPhong.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #595959; -fx-padding: 8 0 4 0;");

        phongDangCoItems = FXCollections.observableArrayList();
        phongDangCoTable = buildSimpleTable(phongDangCoItems,
                new String[]{"Mã Phòng", "Tên Phòng", "Loại", "Đơn Giá"});
        phongDangCoTable.setPrefHeight(120);
        phongDangCoTable.setPlaceholder(new Label("(chưa tra cứu)"));

        // Cộng thêm panel
        VBox costBox = new VBox(6);
        costBox.setPadding(new Insets(12));
        costBox.setStyle("-fx-background-color: #F6FFED; -fx-background-radius: 8;"
                + "-fx-border-color: #B7EB8F; -fx-border-width: 1; -fx-border-radius: 8;");
        Label costTitle = new Label("➕  Tiền Cộng Thêm (cho phòng được ghép)");
        costTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #389E0D;");
        lblPhongChon = infoValue();
        lblGiaPhong  = infoValue();
        lblTongCong  = new Label("—");
        lblTongCong.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #389E0D;");

        GridPane costGrid = new GridPane();
        costGrid.setHgap(10); costGrid.setVgap(6);
        ColumnConstraints cc1 = new ColumnConstraints(); cc1.setPercentWidth(45);
        ColumnConstraints cc2 = new ColumnConstraints(); cc2.setPercentWidth(55);
        costGrid.getColumnConstraints().addAll(cc1, cc2);
        int cr = 0;
        costGrid.add(lbl("Phòng ghép:"),    0, cr); costGrid.add(lblPhongChon, 1, cr++);
        costGrid.add(lbl("Giá/đêm:"),       0, cr); costGrid.add(lblGiaPhong,  1, cr++);
        costGrid.add(lbl("Tổng cộng thêm:"),0, cr); costGrid.add(lblTongCong,  1, cr++);

        costBox.getChildren().addAll(costTitle, costGrid);

        Label hint = new Label("→ Chọn phòng trống từ bảng bên phải để ghép");
        hint.setStyle("-fx-font-size: 12px; -fx-text-fill: #8C8C8C; -fx-font-style: italic;");

        card.getChildren().addAll(cardTitle, searchRow, info, sectionPhong, phongDangCoTable, costBox, hint);
        return card;
    }

    private VBox buildRightCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        HBox cardHeader = new HBox(8);
        cardHeader.setAlignment(Pos.CENTER_LEFT);
        Label cardTitle = new Label("Danh Sách Phòng Trống (chọn để ghép)");
        cardTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;"
                + "-fx-border-color: transparent transparent #F0F2F5 transparent;"
                + "-fx-border-width: 0 0 1 0; -fx-padding: 0 0 10 0;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button btnLoad = new Button("↻ Tải Lại");
        btnLoad.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #595959;"
                + "-fx-background-radius: 6; -fx-border-color: #D9D9D9; -fx-border-width: 1;"
                + "-fx-border-radius: 6; -fx-padding: 5 12; -fx-cursor: hand; -fx-font-size: 12px;");
        btnLoad.setOnAction(e -> {
            phongTrongItems.setAll(loadPhongTrong());
            com.lotuslaverne.fx.UiUtils.flashButton(btnLoad, "✓ Đã tải");
        });
        cardHeader.getChildren().addAll(cardTitle, sp, btnLoad);

        phongTrongItems = FXCollections.observableArrayList(loadPhongTrong());
        phongTrongTable = buildSimpleTable(phongTrongItems,
                new String[]{"Mã Phòng", "Tên Phòng", "Loại", "Trạng Thái"});
        VBox.setVgrow(phongTrongTable, Priority.ALWAYS);

        phongTrongTable.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) tinhTienCongThem(n[0].toString());
        });

        card.getChildren().addAll(cardHeader, phongTrongTable);
        VBox.setVgrow(card, Priority.ALWAYS);
        return card;
    }

    private TableView<Object[]> buildSimpleTable(ObservableList<Object[]> items, String[] heads) {
        TableView<Object[]> tbl = new TableView<>();
        tbl.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tbl.setStyle("-fx-background-color: #F9F9F9; -fx-background-radius: 8;"
                + "-fx-border-color: #E8E8E8; -fx-border-width: 1; -fx-border-radius: 8;");
        for (int i = 0; i < heads.length; i++) {
            final int idx = i;
            TableColumn<Object[], String> col = new TableColumn<>(heads[i]);
            col.setCellValueFactory(p -> {
                Object v = idx < p.getValue().length ? p.getValue()[idx] : "";
                return new SimpleStringProperty(v != null ? v.toString() : "");
            });
            tbl.getColumns().add(col);
        }
        tbl.setItems(items);
        return tbl;
    }

    private void traCuuTheoSDT() {
        resetInfo();
        String sdt = txtSDT.getText().trim();
        if (sdt.isEmpty()) {
            alert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập số điện thoại khách!");
            return;
        }
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) {
            alert(Alert.AlertType.ERROR, "Offline", "Không có kết nối DB.");
            return;
        }
        // Lấy phiếu mới nhất đang ở của khách có SĐT này
        String sqlPhieu =
            "SELECT TOP 1 kh.maKH, kh.hoTenKH, kh.cmnd, " +
            "       pdp.maPhieuDatPhong, pdp.soNguoi, " +
            "       pdp.thoiGianNhanThucTe, pdp.thoiGianTraDuKien " +
            "FROM KhachHang kh " +
            "JOIN PhieuDatPhong pdp ON pdp.maKhachHang = kh.maKH " +
            "WHERE kh.soDienThoai = ? " +
            "  AND pdp.thoiGianNhanThucTe IS NOT NULL " +
            "  AND pdp.thoiGianTraThucTe IS NULL " +
            "ORDER BY pdp.thoiGianNhanThucTe DESC";

        try (PreparedStatement pst = con.prepareStatement(sqlPhieu)) {
            pst.setString(1, sdt);
            try (ResultSet rs = pst.executeQuery()) {
                if (!rs.next()) {
                    alert(Alert.AlertType.WARNING, "Không tìm thấy",
                            "Không có khách nào với SĐT này đang lưu trú.");
                    return;
                }
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                resolvedMaPDP = rs.getString("maPhieuDatPhong");
                Timestamp tsNhan = rs.getTimestamp("thoiGianNhanThucTe");
                Timestamp tsTra  = rs.getTimestamp("thoiGianTraDuKien");

                lblTenKH.setText(rs.getString("hoTenKH"));
                String cccd = rs.getString("cmnd");
                lblCCCD.setText(cccd != null ? cccd : "—");
                lblMaPDP.setText(resolvedMaPDP);
                lblSoNguoi.setText(rs.getInt("soNguoi") + " người");

                LocalDateTime ldtNhan = tsNhan.toLocalDateTime();
                LocalDateTime ldtTra  = tsTra.toLocalDateTime();
                LocalDate today = LocalDate.now();
                soDemConLai = Math.max(1, ChronoUnit.DAYS.between(today, ldtTra.toLocalDate()));

                lblNgayNhan.setText(ldtNhan.format(fmt));
                lblNgayTra.setText(ldtTra.format(fmt));
                lblDemConLai.setText(soDemConLai + " đêm");
            }
        } catch (Exception ex) {
            alert(Alert.AlertType.ERROR, "Lỗi", "Tra cứu thất bại: " + ex.getMessage());
            return;
        }

        // Lấy danh sách phòng đang có trong phiếu
        loadPhongDangCoTrongPhieu();

        // Reset selection bên phải
        phongTrongTable.getSelectionModel().clearSelection();
    }

    private void loadPhongDangCoTrongPhieu() {
        phongDangCoItems.clear();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null || resolvedMaPDP == null) return;
        String sql =
            "SELECT ct.maPhong, p.tenPhong, lp.tenLoaiPhong, ct.donGia " +
            "FROM ChiTietPhieuDatPhong ct " +
            "JOIN Phong p     ON p.maPhong = ct.maPhong " +
            "JOIN LoaiPhong lp ON lp.maLoaiPhong = p.maLoaiPhong " +
            "WHERE ct.maPhieuDatPhong = ?";
        DecimalFormat money = new DecimalFormat("#,###");
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, resolvedMaPDP);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    phongDangCoItems.add(new Object[]{
                        rs.getString("maPhong"),
                        rs.getString("tenPhong"),
                        rs.getString("tenLoaiPhong"),
                        money.format(rs.getDouble("donGia")) + "đ"
                    });
                }
            }
        } catch (Exception ignored) {}
    }

    private void tinhTienCongThem(String maPhongMoi) {
        if (resolvedMaPDP == null) {
            lblPhongChon.setText(maPhongMoi);
            lblGiaPhong.setText("— (hãy tra cứu khách trước)");
            lblTongCong.setText("—");
            return;
        }
        double giaMoi = loadDonGiaPhong(maPhongMoi);
        double tong   = giaMoi * soDemConLai;
        DecimalFormat money = new DecimalFormat("#,###");

        lblPhongChon.setText(maPhongMoi);
        lblGiaPhong.setText(money.format(giaMoi) + " VNĐ / đêm");
        lblTongCong.setText(money.format(tong) + " VNĐ (" + soDemConLai + " đêm)");
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

    private void handleGhepPhong() {
        Object[] selected = phongTrongTable.getSelectionModel().getSelectedItem();
        if (resolvedMaPDP == null) {
            alert(Alert.AlertType.WARNING, "Chưa tra cứu", "Nhập SĐT và bấm Tra Cứu trước."); return;
        }
        if (selected == null) {
            alert(Alert.AlertType.WARNING, "Chưa chọn phòng", "Vui lòng chọn phòng để ghép!"); return;
        }
        String maPhongMoi = selected[0].toString();

        // Kiểm tra phòng đã có trong phiếu chưa
        for (Object[] r : phongDangCoItems) {
            if (maPhongMoi.equals(r[0])) {
                alert(Alert.AlertType.WARNING, "Trùng phòng",
                        "Phòng " + maPhongMoi + " đã có trong phiếu này!"); return;
            }
        }

        double giaMoi = loadDonGiaPhong(maPhongMoi);
        double tongThem = giaMoi * soDemConLai;
        DecimalFormat money = new DecimalFormat("#,###");

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Ghép phòng " + maPhongMoi + " vào phiếu " + resolvedMaPDP + "?\n"
                + "Giá: " + money.format(giaMoi) + " VNĐ/đêm × " + soDemConLai + " đêm\n"
                + "Tổng cộng thêm: " + money.format(tongThem) + " VNĐ");
        confirm.setHeaderText(null);
        confirm.setTitle("Xác Nhận Ghép Phòng");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK) return;
            Connection con = ConnectDB.getInstance().getConnection();
            if (con == null) {
                alert(Alert.AlertType.ERROR, "Offline", "Không có kết nối DB."); return;
            }
            try {
                con.setAutoCommit(false);
                try (PreparedStatement insCT = con.prepareStatement(
                        "INSERT INTO ChiTietPhieuDatPhong (maPhieuDatPhong, maPhong, donGia) VALUES (?, ?, ?)")) {
                    insCT.setString(1, resolvedMaPDP);
                    insCT.setString(2, maPhongMoi);
                    insCT.setDouble(3, giaMoi);
                    insCT.executeUpdate();
                }
                try (PreparedStatement updPhong = con.prepareStatement(
                        "UPDATE Phong SET trangThai = N'PhongDat' WHERE maPhong = ?")) {
                    updPhong.setString(1, maPhongMoi);
                    updPhong.executeUpdate();
                }
                con.commit();
                alert(Alert.AlertType.INFORMATION, "Thành công",
                        "Ghép phòng " + maPhongMoi + " thành công!\n"
                        + "Cộng thêm vào hóa đơn: " + money.format(tongThem) + " VNĐ");

                phongTrongItems.setAll(loadPhongTrong());
                loadPhongDangCoTrongPhieu();
                phongTrongTable.getSelectionModel().clearSelection();
                lblPhongChon.setText("—");
                lblGiaPhong.setText("—");
                lblTongCong.setText("—");
            } catch (Exception ex) {
                try { con.rollback(); } catch (Exception ignored) {}
                alert(Alert.AlertType.ERROR, "Lỗi ghép phòng", ex.getMessage());
            } finally {
                try { con.setAutoCommit(true); } catch (Exception ignored) {}
            }
        });
    }

    private void resetInfo() {
        resolvedMaPDP = null;
        soDemConLai = 0;
        for (Label l : new Label[]{lblTenKH, lblCCCD, lblMaPDP, lblSoNguoi,
                                    lblNgayNhan, lblNgayTra, lblDemConLai,
                                    lblPhongChon, lblGiaPhong}) {
            if (l != null) l.setText("—");
        }
        if (lblTongCong != null) lblTongCong.setText("—");
        if (phongDangCoItems != null) phongDangCoItems.clear();
    }

    private List<Object[]> loadPhongTrong() {
        List<Object[]> result = new ArrayList<>();
        try {
            List<Phong> list = new PhongDAO().getAll();
            for (Phong p : list) {
                if ("PhongTrong".equalsIgnoreCase(p.getTrangThai()) || "Trống".equalsIgnoreCase(p.getTrangThai())) {
                    result.add(new Object[]{p.getMaPhong(), p.getTenPhong(), p.getMaLoaiPhong(), "Trống"});
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

    private void alert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type, msg);
        a.setHeaderText(null);
        a.setTitle(title);
        a.showAndWait();
    }
}
