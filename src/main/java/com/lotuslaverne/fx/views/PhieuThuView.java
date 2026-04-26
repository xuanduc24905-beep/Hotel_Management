package com.lotuslaverne.fx.views;

import com.lotuslaverne.dao.PhieuThuDAO;
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
import com.lotuslaverne.util.ConnectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;

/**
 * PhieuThuView – Quản lý phiếu đặt cọc (Deposit Management).
 * Lễ tân thu tiền cọc khi khách đặt phòng và ghi vào bảng PhieuThu.
 */
public class PhieuThuView {

    private static final DecimalFormat MONEY = new DecimalFormat("#,###");

    private ComboBox<String> cbPhieu;
    private TextField txtSoTienCoc, txtGhiChu;
    private ComboBox<String> cbPhuongThuc;
    private Label lblKhach, lblPhong, lblNgayDat;
    private TableView<Object[]> tblPhieuThu;
    private ObservableList<Object[]> tblItems;

    private final PhieuThuDAO phieuThuDAO = new PhieuThuDAO();
    private final PhieuDatPhongDAO pdpDAO  = new PhieuDatPhongDAO();

    public Node build() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #F0F2F5;");

        VBox content = new VBox(20);
        content.setPadding(new Insets(28));
        VBox.setVgrow(content, Priority.ALWAYS);

        // ── Header
        VBox header = new VBox(4);
        Label title = new Label("Phiếu Thu Đặt Cọc");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Label sub = new Label("Thu tiền đặt cọc từ khách khi đặt phòng — lưu vào sổ cọc để đối chiếu khi checkout");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #8C8C8C;");
        header.getChildren().addAll(title, sub);

        HBox mainRow = new HBox(20);
        mainRow.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(mainRow, Priority.ALWAYS);

        VBox leftCard  = buildFormCard();
        VBox rightCard = buildListCard();
        HBox.setHgrow(leftCard,  Priority.ALWAYS);
        HBox.setHgrow(rightCard, Priority.ALWAYS);
        mainRow.getChildren().addAll(leftCard, rightCard);

        content.getChildren().addAll(header, mainRow);
        root.getChildren().add(content);
        VBox.setVgrow(root, Priority.ALWAYS);

        loadPhieuChuaCheckIn();
        loadAllPhieuThu();

        return root;
    }

    // ─────────────────────────── LEFT: Form thu cọc
    private VBox buildFormCard() {
        VBox card = new VBox(16);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        Label cardTitle = new Label("Tạo Phiếu Thu Cọc");
        cardTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;"
                + "-fx-border-color: transparent transparent #F0F2F5 transparent;"
                + "-fx-border-width: 0 0 1 0; -fx-padding: 0 0 10 0;");

        GridPane form = new GridPane();
        form.setHgap(12); form.setVgap(14);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(40);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(60);
        form.getColumnConstraints().addAll(c1, c2);

        // Phiếu đặt phòng
        cbPhieu = new ComboBox<>();
        cbPhieu.setMaxWidth(Double.MAX_VALUE);
        cbPhieu.setPromptText("Chọn phiếu đặt phòng...");
        cbPhieu.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D9D9D9;"
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1;");
        cbPhieu.setOnAction(e -> fillPhieuInfo());

        // Info labels (auto-fill khi chọn phiếu)
        lblKhach   = infoLabel("—");
        lblPhong   = infoLabel("—");
        lblNgayDat = infoLabel("—");

        // Số tiền cọc
        txtSoTienCoc = new TextField();
        txtSoTienCoc.setPromptText("VD: 500000");
        txtSoTienCoc.setMaxWidth(Double.MAX_VALUE);
        txtSoTienCoc.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D9D9D9;"
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1; -fx-padding: 7 10;");

        // Phương thức
        cbPhuongThuc = new ComboBox<>();
        cbPhuongThuc.getItems().addAll("Tiền Mặt", "Chuyển Khoản");
        cbPhuongThuc.setValue("Tiền Mặt");
        cbPhuongThuc.setMaxWidth(Double.MAX_VALUE);
        cbPhuongThuc.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D9D9D9;"
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1;");

        // Ghi chú
        txtGhiChu = new TextField();
        txtGhiChu.setPromptText("Ghi chú tùy chọn...");
        txtGhiChu.setMaxWidth(Double.MAX_VALUE);
        txtGhiChu.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D9D9D9;"
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1; -fx-padding: 7 10;");

        form.add(lbl("Phiếu đặt phòng:"), 0, 0); form.add(cbPhieu,       1, 0);
        form.add(lbl("Khách hàng:"),      0, 1); form.add(lblKhach,       1, 1);
        form.add(lbl("Phòng:"),           0, 2); form.add(lblPhong,       1, 2);
        form.add(lbl("Ngày đặt:"),        0, 3); form.add(lblNgayDat,     1, 3);
        form.add(lbl("Số tiền cọc (đ):"), 0, 4); form.add(txtSoTienCoc,   1, 4);
        form.add(lbl("Phương thức:"),     0, 5); form.add(cbPhuongThuc,   1, 5);
        form.add(lbl("Ghi chú:"),         0, 6); form.add(txtGhiChu,      1, 6);

        Button btnThu = new Button("💳  Thu Tiền Cọc");
        btnThu.setStyle("-fx-background-color: #1890FF; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-size: 13px;"
                + "-fx-font-weight: bold; -fx-cursor: hand;");
        btnThu.setMaxWidth(Double.MAX_VALUE);
        btnThu.setOnAction(e -> handleThuCoc());

        card.getChildren().addAll(cardTitle, form, btnThu);
        return card;
    }

    // ─────────────────────────── RIGHT: List all phiếu thu
    private VBox buildListCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");
        VBox.setVgrow(card, Priority.ALWAYS);

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label cardTitle = new Label("Danh Sách Phiếu Thu Cọc");
        cardTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button btnReload = new Button("↻");
        btnReload.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #D9D9D9;"
                + "-fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6;"
                + "-fx-padding: 4 10; -fx-cursor: hand;");
        btnReload.setOnAction(e -> loadAllPhieuThu());
        titleRow.getChildren().addAll(cardTitle, sp, btnReload);

        tblPhieuThu = new TableView<>();
        tblPhieuThu.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblPhieuThu.setStyle("-fx-background-color: #F9F9F9; -fx-border-color: #E8E8E8;"
                + "-fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");
        tblPhieuThu.setPlaceholder(new Label("Chưa có phiếu thu cọc nào."));
        VBox.setVgrow(tblPhieuThu, Priority.ALWAYS);

        String[] heads = {"Mã Phiếu Thu", "Mã Phiếu ĐP", "Tiền Cọc", "Phương Thức", "Ngày Thu", "Ghi Chú"};
        for (int i = 0; i < heads.length; i++) {
            final int idx = i;
            TableColumn<Object[], String> col = new TableColumn<>(heads[i]);
            col.setCellValueFactory(p -> {
                Object v = idx < p.getValue().length ? p.getValue()[idx] : "";
                return new SimpleStringProperty(v != null ? v.toString() : "");
            });
            tblPhieuThu.getColumns().add(col);
        }
        tblItems = FXCollections.observableArrayList();
        tblPhieuThu.setItems(tblItems);

        card.getChildren().addAll(titleRow, tblPhieuThu);
        return card;
    }

    // ─────────────────────────── DATA LOGIC
    private void loadPhieuChuaCheckIn() {
        cbPhieu.getItems().clear();
        // PhieuDatPhong entity has no getTrangThai() — query DB directly
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return;
        String sql = "SELECT maPhieuDatPhong FROM PhieuDatPhong " +
                     "WHERE trangThai IN ('DaDat','DaCheckIn') ORDER BY ngayDat DESC";
        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                cbPhieu.getItems().add(rs.getString("maPhieuDatPhong"));
            }
        } catch (Exception ignored) {}
    }

    private void fillPhieuInfo() {
        String maPDP = cbPhieu.getValue();
        if (maPDP == null) return;
        try {
            List<PhieuDatPhong> all = pdpDAO.getAll();
            for (PhieuDatPhong p : all) {
                if (p.getMaPhieuDatPhong().equals(maPDP)) {
                    lblKhach.setText(p.getMaKhachHang() != null ? p.getMaKhachHang() : "—");
                    lblNgayDat.setText(p.getNgayDat() != null
                            ? p.getNgayDat().toString().substring(0, 16) : "—");
                    break;
                }
            }
            // Lấy thông tin phòng
            Connection con = ConnectDB.getInstance().getConnection();
            if (con != null) {
                try (PreparedStatement pst = con.prepareStatement(
                        "SELECT STRING_AGG(maPhong, ', ') AS phong FROM ChiTietPhieuDatPhong WHERE maPhieuDatPhong=?")) {
                    pst.setString(1, maPDP);
                    ResultSet rs = pst.executeQuery();
                    if (rs.next()) lblPhong.setText(rs.getString("phong") != null ? rs.getString("phong") : "—");
                } catch (Exception ex) {
                    // STRING_AGG not supported in older SQL Server — fallback
                    try (PreparedStatement pst = con.prepareStatement(
                            "SELECT maPhong FROM ChiTietPhieuDatPhong WHERE maPhieuDatPhong=?")) {
                        pst.setString(1, maPDP);
                        ResultSet rs = pst.executeQuery();
                        StringBuilder sb = new StringBuilder();
                        while (rs.next()) { if (sb.length() > 0) sb.append(", "); sb.append(rs.getString("maPhong")); }
                        lblPhong.setText(sb.length() > 0 ? sb.toString() : "—");
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private void loadAllPhieuThu() {
        tblItems.clear();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return;
        String sql = "SELECT maPhieuThu, maPhieuDatPhong, soTienCoc, phuongThucThanhToan, ngayThu, ghiChu " +
                     "FROM PhieuThu ORDER BY ngayThu DESC";
        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                tblItems.add(new Object[]{
                    rs.getString("maPhieuThu"),
                    rs.getString("maPhieuDatPhong"),
                    MONEY.format(rs.getDouble("soTienCoc")) + "đ",
                    rs.getString("phuongThucThanhToan"),
                    rs.getTimestamp("ngayThu") != null
                            ? rs.getTimestamp("ngayThu").toString().substring(0, 16) : "",
                    rs.getString("ghiChu") != null ? rs.getString("ghiChu") : ""
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleThuCoc() {
        String maPDP = cbPhieu.getValue();
        if (maPDP == null || maPDP.isBlank()) { alert("Chọn phiếu đặt phòng!"); return; }

        double soTienCoc;
        try {
            soTienCoc = Double.parseDouble(txtSoTienCoc.getText().trim().replace(",", ""));
            if (soTienCoc <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            alert("Số tiền cọc không hợp lệ! Nhập số nguyên dương."); return;
        }

        String phuongThucDB = "Chuyển Khoản".equals(cbPhuongThuc.getValue()) ? "ChuyenKhoan" : "TienMat";
        String maPT = "PT" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Xác nhận thu cọc:\n"
                + "Phiếu đặt phòng: " + maPDP + "\n"
                + "Số tiền cọc: " + MONEY.format(soTienCoc) + "đ\n"
                + "Phương thức: " + cbPhuongThuc.getValue());
        confirm.setHeaderText("Xác Nhận Thu Tiền Cọc");
        confirm.setTitle("Thu Cọc");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK) return;
            boolean ok = phieuThuDAO.taoPhieuThu(maPT, maPDP, "NV001",
                    soTienCoc, phuongThucDB, txtGhiChu.getText().trim());
            if (ok) {
                loadAllPhieuThu();
                txtSoTienCoc.clear(); txtGhiChu.clear();
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setHeaderText(null); a.setTitle("Thành Công");
                a.setContentText("✅ Thu cọc thành công!\nMã phiếu thu: " + maPT
                        + "\nSố tiền: " + MONEY.format(soTienCoc) + "đ");
                a.showAndWait();
            } else {
                alert("Lỗi tạo phiếu thu. Kiểm tra kết nối DB.");
            }
        });
    }

    // ─────────────────────────── HELPERS
    private Label lbl(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #595959;");
        return l;
    }

    private Label infoLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: #1890FF; -fx-font-weight: bold;");
        return l;
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg);
        a.setHeaderText(null); a.setTitle("Thông báo"); a.showAndWait();
    }
}
