package com.lotuslaverne.fx.views;

import com.lotuslaverne.dao.BangGiaDAO;
import com.lotuslaverne.dao.HoaDonDAO;
import com.lotuslaverne.dao.KhuyenMaiDAO;
import com.lotuslaverne.dao.PhieuDatPhongDAO;
import com.lotuslaverne.dao.PhongDAO;
import com.lotuslaverne.entity.HoaDon;
import com.lotuslaverne.entity.KhuyenMai;
import com.lotuslaverne.entity.PhieuDatPhong;
import com.lotuslaverne.util.ConnectDB;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;

public class ThanhToanView {

    private TextField txtMaPhieuDP;
    private TextField txtKhuyenMai;
    private Label lblTongTien;
    private Label lblTamTinhPhong, lblPhatSinhDV, lblKhuyenMaiVal;
    private ComboBox<String> cbPhuongThuc;
    private TableView<Object[]> dvTable;
    private javafx.collections.ObservableList<Object[]> dvItems;
    private double currentTotal = 0;
    private double discountAmount = 0;
    private double tamTinhPhong = 0;
    private double phatSinhDV   = 0;

    public Node build() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #F0F2F5;");

        VBox content = new VBox(20);
        content.setPadding(new Insets(28));
        content.setStyle("-fx-background-color: #F0F2F5;");
        VBox.setVgrow(content, Priority.ALWAYS);

        // Header
        VBox header = new VBox(4);
        Label title = new Label("Thanh Toán / Check-out");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Label sub = new Label("Tính tiền và xuất hóa đơn cho khách trả phòng");
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

        // Checkout button
        Button btnCheckOut = new Button("XUẤT HÓA ĐƠN & TRẢ PHÒNG");
        btnCheckOut.setStyle("-fx-background-color: #FF4D4F; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-padding: 12 32; -fx-font-size: 14px;"
                + "-fx-font-weight: bold; -fx-cursor: hand;");
        btnCheckOut.setOnAction(e -> handleCheckOut());

        HBox bottomBar = new HBox(btnCheckOut);
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

        Label cardTitle = new Label("Thông Tin Tính Tiền");
        cardTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;"
                + "-fx-border-color: transparent transparent #F0F2F5 transparent;"
                + "-fx-border-width: 0 0 1 0; -fx-padding: 0 0 10 0;");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(42);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(58);
        form.getColumnConstraints().addAll(c1, c2);

        txtMaPhieuDP = field("PDP001");

        // Khuyến mãi row
        txtKhuyenMai = field("");
        Button btnApKM = new Button("Áp Dụng");
        btnApKM.setStyle("-fx-background-color: #1890FF; -fx-text-fill: white;"
                + "-fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand; -fx-font-size: 12px;");
        btnApKM.setOnAction(e -> apDungKhuyenMai());
        HBox kmRow = new HBox(6, txtKhuyenMai, btnApKM);
        HBox.setHgrow(txtKhuyenMai, Priority.ALWAYS);
        kmRow.setAlignment(Pos.CENTER_LEFT);

        cbPhuongThuc = new ComboBox<>();
        cbPhuongThuc.getItems().addAll("TienMat", "ChuyenKhoan");
        cbPhuongThuc.setValue("TienMat");
        cbPhuongThuc.setMaxWidth(Double.MAX_VALUE);
        cbPhuongThuc.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D9D9D9;"
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1;");

        Button btnTinh = new Button("TÍNH TOÁN TIỀN");
        btnTinh.setStyle("-fx-background-color: #52C41A; -fx-text-fill: white;"
                + "-fx-background-radius: 6; -fx-padding: 8 16; -fx-cursor: hand;"
                + "-fx-font-size: 12px; -fx-font-weight: bold;");
        btnTinh.setMaxWidth(Double.MAX_VALUE);
        btnTinh.setOnAction(e -> tinhTien());

        lblTongTien = new Label("0 VNĐ");
        lblTongTien.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #FF4D4F;");

        // Breakdown chi tiết
        lblTamTinhPhong = new Label("—");
        lblTamTinhPhong.setStyle("-fx-font-size: 13px; -fx-text-fill: #1A1A2E; -fx-font-weight: bold;");
        lblPhatSinhDV = new Label("—");
        lblPhatSinhDV.setStyle("-fx-font-size: 13px; -fx-text-fill: #1A1A2E; -fx-font-weight: bold;");
        lblKhuyenMaiVal = new Label("—");
        lblKhuyenMaiVal.setStyle("-fx-font-size: 13px; -fx-text-fill: #52C41A; -fx-font-weight: bold;");

        form.add(lbl("Mã phiếu đặt phòng:"),    0, 0); form.add(txtMaPhieuDP,  1, 0);
        form.add(lbl("Mã Voucher khuyến mãi:"),  0, 1); form.add(kmRow,         1, 1);
        form.add(lbl("Phương thức thanh toán:"), 0, 2); form.add(cbPhuongThuc,  1, 2);
        form.add(new Label(""),                   0, 3); form.add(btnTinh,       1, 3);

        // Separator + breakdown
        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color: #F0F2F5;");
        form.add(sep, 0, 4);
        GridPane.setColumnSpan(sep, 2);

        form.add(lbl("Tạm tính tiền phòng:"),  0, 5); form.add(lblTamTinhPhong, 1, 5);
        form.add(lbl("Phát sinh dịch vụ:"),    0, 6); form.add(lblPhatSinhDV,   1, 6);
        form.add(lbl("Khuyến mãi:"),           0, 7); form.add(lblKhuyenMaiVal, 1, 7);
        form.add(lbl("TỔNG TIỀN THANH TOÁN:"), 0, 8); form.add(lblTongTien,     1, 8);

        card.getChildren().addAll(cardTitle, form);
        return card;
    }

    private VBox buildRightCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        Label cardTitle = new Label("Dịch Vụ Phát Sinh");
        cardTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;"
                + "-fx-border-color: transparent transparent #F0F2F5 transparent;"
                + "-fx-border-width: 0 0 1 0; -fx-padding: 0 0 10 0;");

        dvTable = new TableView<>();
        dvTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        dvTable.setStyle("-fx-background-color: #F9F9F9; -fx-border-color: #E8E8E8;"
                + "-fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");
        VBox.setVgrow(dvTable, Priority.ALWAYS);

        String[] heads = {"Dịch Vụ", "Số Lượng", "Đơn Giá (VNĐ)", "Thành Tiền (VNĐ)"};
        for (int i = 0; i < heads.length; i++) {
            final int idx = i;
            TableColumn<Object[], String> col = new TableColumn<>(heads[i]);
            col.setCellValueFactory(p -> {
                Object v = idx < p.getValue().length ? p.getValue()[idx] : "";
                return new SimpleStringProperty(v != null ? v.toString() : "");
            });
            dvTable.getColumns().add(col);
        }
        dvItems = FXCollections.observableArrayList();
        dvTable.setItems(dvItems);
        dvTable.setPlaceholder(new Label("Nhập mã phiếu và bấm 'Tính Tiền' để xem dịch vụ phát sinh."));

        card.getChildren().addAll(cardTitle, dvTable);
        VBox.setVgrow(card, Priority.ALWAYS);
        return card;
    }

    /** Load dịch vụ phát sinh của 1 phiếu đặt phòng + tổng tiền dịch vụ */
    private double loadDichVuPhatSinh(String maPDP) {
        dvItems.clear();
        double total = 0;
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return 0;
        String sql =
            "SELECT dv.tenDichVu, ctdv.soLuong, dv.donGia, (ctdv.soLuong * dv.donGia) AS thanhTien " +
            "FROM ChiTietDichVu ctdv " +
            "JOIN DichVu dv ON dv.maDichVu = ctdv.maDichVu " +
            "WHERE ctdv.maPhieuDatPhong = ?";
        DecimalFormat df = new DecimalFormat("#,###");
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maPDP);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    int sl = rs.getInt("soLuong");
                    double gia = rs.getDouble("donGia");
                    double tt  = rs.getDouble("thanhTien");
                    total += tt;
                    dvItems.add(new Object[]{
                        rs.getString("tenDichVu"),
                        String.valueOf(sl),
                        df.format(gia),
                        df.format(tt)
                    });
                }
            }
        } catch (Exception ignored) {}
        return total;
    }

    private void tinhTien() {
        String maPDP = txtMaPhieuDP.getText().trim();
        if (maPDP.isEmpty()) {
            alert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập mã phiếu đặt phòng!"); return;
        }
        try {
            List<PhieuDatPhong> list = new PhieuDatPhongDAO().getDangSuDung();
            PhieuDatPhong pdp = null;
            for (PhieuDatPhong p : list) {
                if (p.getMaPhieuDatPhong().equalsIgnoreCase(maPDP)) { pdp = p; break; }
            }
            if (pdp == null) {
                alert(Alert.AlertType.WARNING, "Không tìm thấy",
                        "Không tìm thấy phiếu đang sử dụng!\nKiểm tra mã phiếu hoặc khách chưa check-in.");
                return;
            }

            java.util.Date tRef = pdp.getThoiGianNhanThucTe() != null
                    ? pdp.getThoiGianNhanThucTe() : pdp.getThoiGianNhanDuKien();
            if (tRef == null) {
                alert(Alert.AlertType.ERROR, "Lỗi", "Không xác định được thời gian nhận phòng!"); return;
            }
            long soGio  = Math.max(1, (System.currentTimeMillis() - tRef.getTime()) / 3600000);
            long soNgay = Math.max(1, soGio / 24);

            double donGia = 0;
            try {
                Connection con = ConnectDB.getInstance().getConnection();
                if (con != null) {
                    try (PreparedStatement pst = con.prepareStatement(
                            "SELECT ph.maLoaiPhong FROM ChiTietPhieuDatPhong ct "
                            + "JOIN Phong ph ON ct.maPhong=ph.maPhong WHERE ct.maPhieuDatPhong=?")) {
                        pst.setString(1, maPDP);
                        try (ResultSet rs = pst.executeQuery()) {
                            if (rs.next()) {
                                String maLP = rs.getString("maLoaiPhong");
                                BangGiaDAO bgDAO = new BangGiaDAO();
                                donGia = bgDAO.getGiaHienTai(maLP, "QuaDem");
                                if (donGia == 0) donGia = bgDAO.getGiaHienTai(maLP, "TheoNgay");
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}
            if (donGia == 0) donGia = 200_000;

            tamTinhPhong  = soNgay * donGia;
            phatSinhDV    = loadDichVuPhatSinh(maPDP);
            double base   = tamTinhPhong + phatSinhDV;

            // Khuyến mãi tính lại trên (phòng + DV)
            if (discountAmount > 0 && currentTotal > 0) {
                // Nếu user đã apply KM trước, tính lại tỉ lệ theo base mới
                double oldBase = currentTotal + discountAmount;
                if (oldBase > 0) discountAmount = base * discountAmount / oldBase;
            }
            currentTotal = Math.max(0, base - discountAmount);

            DecimalFormat df = new DecimalFormat("#,### VNĐ");
            lblTamTinhPhong.setText(df.format(tamTinhPhong) + "  (" + soNgay + " đêm × "
                    + new DecimalFormat("#,###").format(donGia) + ")");
            lblPhatSinhDV.setText(df.format(phatSinhDV) + "  (" + dvItems.size() + " mục)");
            lblKhuyenMaiVal.setText(discountAmount > 0 ? "- " + df.format(discountAmount) : "—");
            lblTongTien.setText(df.format(currentTotal));
        } catch (Exception ex) {
            alert(Alert.AlertType.ERROR, "Lỗi", "Lỗi kết nối DB: " + ex.getMessage());
        }
    }

    private void apDungKhuyenMai() {
        String maKM = txtKhuyenMai.getText().trim();
        if (maKM.isEmpty()) return;
        try {
            List<KhuyenMai> list = new KhuyenMaiDAO().layTatCaKhuyenMai();
            for (KhuyenMai km : list) {
                if (km.getMaKhuyenMai().equalsIgnoreCase(maKM)) {
                    discountAmount = currentTotal > 0 ? currentTotal * km.getPhanTramGiam() / 100 : 0;
                    alert(Alert.AlertType.INFORMATION, "Khuyến Mãi",
                            "Áp dụng mã KM thành công!\nGiảm " + km.getPhanTramGiam() + "%");
                    if (currentTotal > 0) tinhTien();
                    return;
                }
            }
        } catch (Exception ignored) {}
        discountAmount = 0;
        alert(Alert.AlertType.ERROR, "Lỗi", "Mã KM không hợp lệ hoặc đã hết hạn!");
    }

    private void handleCheckOut() {
        if (currentTotal == 0) {
            alert(Alert.AlertType.WARNING, "Chưa tính tiền", "Vui lòng nhấn 'Tính Toán Tiền' trước!"); return;
        }
        String maPDP     = txtMaPhieuDP.getText().trim();
        String maHD      = "HD" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String phuongThuc = cbPhuongThuc.getValue();

        HoaDon hd = new HoaDon(maHD, "NV001", maPDP, discountAmount, currentTotal,
                phuongThuc, "Checkout " + phuongThuc);
        try {
            if (new HoaDonDAO().taoHoaDon(hd)) {
                String maPhong = layMaPhong(maPDP);
                if (!maPhong.isEmpty()) new PhongDAO().capNhatTrangThai(maPhong, "PhongCanDon");
                alert(Alert.AlertType.INFORMATION, "Thanh Toán Hoàn Tất",
                        "Thanh toán thành công!\nMã hóa đơn: " + maHD
                        + "\nTổng tiền: " + new DecimalFormat("#,### VNĐ").format(currentTotal)
                        + "\nTrạng thái phòng → Cần dọn.");
                currentTotal = 0; discountAmount = 0;
                tamTinhPhong = 0; phatSinhDV = 0;
                lblTongTien.setText("0 VNĐ");
                lblTamTinhPhong.setText("—");
                lblPhatSinhDV.setText("—");
                lblKhuyenMaiVal.setText("—");
                if (dvItems != null) dvItems.clear();
                txtMaPhieuDP.clear();
                txtKhuyenMai.clear();
            } else {
                alert(Alert.AlertType.ERROR, "Lỗi", "Lỗi tạo hóa đơn. Kiểm tra Database.");
            }
        } catch (Exception ex) {
            alert(Alert.AlertType.ERROR, "Lỗi", "Lỗi kết nối DB: " + ex.getMessage());
        }
    }

    private String layMaPhong(String maPDP) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            if (con == null) return "";
            try (PreparedStatement pst = con.prepareStatement(
                    "SELECT maPhong FROM ChiTietPhieuDatPhong WHERE maPhieuDatPhong=?")) {
                pst.setString(1, maPDP);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) return rs.getString("maPhong");
                }
            }
        } catch (Exception ignored) {}
        return "";
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

    private void alert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type, msg);
        a.setHeaderText(null);
        a.setTitle(title);
        a.showAndWait();
    }
}
