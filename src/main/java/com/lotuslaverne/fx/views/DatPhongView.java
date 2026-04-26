package com.lotuslaverne.fx.views;

import com.lotuslaverne.dao.KhachHangDAO;
import com.lotuslaverne.dao.PhieuDatPhongDAO;
import com.lotuslaverne.dao.PhongDAO;
import com.lotuslaverne.entity.KhachHang;
import com.lotuslaverne.entity.PhieuDatPhong;
import com.lotuslaverne.util.ConnectDB;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class DatPhongView {

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
        Label title = new Label("Đặt Phòng");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Label sub = new Label("Lập phiếu đặt phòng cho khách hàng");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #8C8C8C;");
        header.getChildren().addAll(title, sub);

        VBox formCard = buildFormCard();
        content.getChildren().addAll(header, formCard);
        scroll.setContent(content);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        root.getChildren().add(scroll);
        return root;
    }

    private VBox buildFormCard() {
        VBox card = new VBox(16);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        Label cardTitle = new Label("Thông Tin Đặt Phòng");
        cardTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;"
                + "-fx-border-color: transparent transparent #F0F2F5 transparent;"
                + "-fx-border-width: 0 0 1 0; -fx-padding: 0 0 12 0;");

        GridPane form = new GridPane();
        form.setHgap(20);
        form.setVgap(12);
        for (int i = 0; i < 3; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(33.33);
            form.getColumnConstraints().add(cc);
        }

        TextField txtSDT     = field("");
        txtSDT.setPromptText("VD: 0912345678");
        TextField txtTenKH   = field("");
        txtTenKH.setPromptText("(điền nếu khách mới)");
        TextField txtMaPhong = field("P101");
        TextField txtSoNguoi = field("2");

        Label lblKHStatus = new Label("Nhập SĐT rồi bấm Tra Cứu");
        lblKHStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #8C8C8C;");
        Button btnTraCuu = new Button("🔍 Tra Cứu");
        btnTraCuu.setStyle("-fx-background-color: #1890FF; -fx-text-fill: white;"
                + "-fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand; -fx-font-size: 11px;");

        // State KH đã resolve (nếu null sẽ auto-tạo lúc submit)
        String[] resolvedMaKH = {null};

        btnTraCuu.setOnAction(e -> {
            String sdt = txtSDT.getText().trim();
            if (sdt.isEmpty()) {
                lblKHStatus.setText("Vui lòng nhập SĐT!");
                lblKHStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #FF4D4F;");
                return;
            }
            KhachHang kh = lookupKhachBySDT(sdt);
            if (kh != null) {
                resolvedMaKH[0] = kh.getMaKH();
                txtTenKH.setText(kh.getHoTenKH());
                lblKHStatus.setText("✓ Đã có khách: " + kh.getHoTenKH() + " (" + kh.getMaKH() + ")");
                lblKHStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #52C41A;");
            } else {
                resolvedMaKH[0] = null;
                lblKHStatus.setText("⚠ Khách mới — điền tên để tự tạo lúc đặt phòng");
                lblKHStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #FAAD14;");
            }
        });
        txtSDT.setOnAction(e -> btnTraCuu.fire());

        DatePicker dpNhan = datePicker(LocalDate.now());
        DatePicker dpTra  = datePicker(LocalDate.now().plusDays(1));

        ComboBox<String> cbHinhThuc = new ComboBox<>();
        cbHinhThuc.getItems().addAll("Trực tiếp", "Qua điện thoại", "Online Booking");
        cbHinhThuc.setValue("Trực tiếp");
        cbHinhThuc.setMaxWidth(Double.MAX_VALUE);
        cbHinhThuc.setStyle(comboStyle());

        TextField txtGhiChu = field("");

        HBox sdtRow = new HBox(6, txtSDT, btnTraCuu);
        HBox.setHgrow(txtSDT, Priority.ALWAYS);

        form.add(lbl("SĐT Khách Hàng *"), 0, 0); form.add(sdtRow,     0, 1);
        form.add(lbl("Tên Khách"),         1, 0); form.add(txtTenKH,   1, 1);
        form.add(lbl("Số Khách *"),        2, 0); form.add(txtSoNguoi, 2, 1);

        form.add(lbl("Mã Phòng *"),        0, 2); form.add(txtMaPhong, 0, 3);
        form.add(lbl("Ngày Nhận"),         1, 2); form.add(dpNhan,     1, 3);
        form.add(lbl("Ngày Trả"),          2, 2); form.add(dpTra,      2, 3);

        form.add(lbl("Hình Thức Đặt"),     0, 4); form.add(cbHinhThuc, 0, 5);
        form.add(lbl("Ghi Chú"),           1, 4); form.add(txtGhiChu,  1, 5);
        GridPane.setColumnSpan(txtGhiChu, 2);

        form.add(lblKHStatus, 0, 6);
        GridPane.setColumnSpan(lblKHStatus, 3);

        // ── Panel chi phí ước tính ──
        VBox uocTinhBox = new VBox(6);
        uocTinhBox.setPadding(new Insets(14));
        uocTinhBox.setStyle("-fx-background-color: #F0F5FF; -fx-background-radius: 8;"
                + "-fx-border-color: #ADC6FF; -fx-border-width: 1; -fx-border-radius: 8;");
        Label uocTinhTitle = new Label("💰  Chi Phí Ước Tính");
        uocTinhTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1890FF;");
        Label lblGiaPhong = new Label("Đơn giá phòng: —");
        lblGiaPhong.setStyle("-fx-font-size: 12px; -fx-text-fill: #595959;");
        Label lblSoDem    = new Label("Số đêm: —");
        lblSoDem.setStyle("-fx-font-size: 12px; -fx-text-fill: #595959;");
        Label lblTongUocTinh = new Label("Tổng: —");
        lblTongUocTinh.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #FF4D4F;");
        uocTinhBox.getChildren().addAll(uocTinhTitle, lblGiaPhong, lblSoDem, lblTongUocTinh);

        // Recompute estimate khi đổi phòng / ngày
        Runnable recompute = () -> tinhUocTinh(
                txtMaPhong.getText().trim(),
                dpNhan.getValue(), dpTra.getValue(),
                lblGiaPhong, lblSoDem, lblTongUocTinh);
        txtMaPhong.textProperty().addListener((obs, o, n) -> recompute.run());
        dpNhan.valueProperty().addListener((obs, o, n) -> recompute.run());
        dpTra.valueProperty().addListener((obs, o, n) -> recompute.run());
        recompute.run();  // tính lần đầu

        Label errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: #FF4D4F; -fx-font-size: 12px;");
        errorLbl.setVisible(false);
        errorLbl.setManaged(false);

        Button btnSubmit = new Button("GHI NHẬN ĐẶT PHÒNG");
        btnSubmit.setStyle("-fx-background-color: #1890FF; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-padding: 12 32; -fx-font-size: 14px;"
                + "-fx-font-weight: bold; -fx-cursor: hand;");

        btnSubmit.setOnAction(e -> {
            String sdt        = txtSDT.getText().trim();
            String tenKH      = txtTenKH.getText().trim();
            String maPhong    = txtMaPhong.getText().trim();
            String soNguoiStr = txtSoNguoi.getText().trim();

            if (sdt.isEmpty() || maPhong.isEmpty() || soNguoiStr.isEmpty()) {
                showError(errorLbl, "Vui lòng điền đầy đủ các trường bắt buộc (*)!");
                return;
            }
            int soNguoi;
            try {
                soNguoi = Integer.parseInt(soNguoiStr);
                if (soNguoi <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                showError(errorLbl, "Số khách phải là số nguyên dương!");
                return;
            }
            if (dpNhan.getValue() == null || dpTra.getValue() == null
                    || !dpTra.getValue().isAfter(dpNhan.getValue())) {
                showError(errorLbl, "Ngày trả phải sau ngày nhận phòng!");
                return;
            }

            // Resolve maKH: ưu tiên cache từ Tra Cứu, nếu chưa thì lookup lại
            String maKH = resolvedMaKH[0];
            if (maKH == null) {
                KhachHang found = lookupKhachBySDT(sdt);
                if (found != null) {
                    maKH = found.getMaKH();
                } else {
                    // Tự tạo KH mới
                    if (tenKH.isEmpty()) {
                        showError(errorLbl, "Khách chưa tồn tại. Nhập tên khách để tự tạo!");
                        return;
                    }
                    KhachHang newKH = new KhachHang(null, tenKH, sdt, "");
                    if (!new KhachHangDAO().themKhachHang(newKH)) {
                        showError(errorLbl, "Không tạo được khách mới. Kiểm tra DB.");
                        return;
                    }
                    maKH = newKH.getMaKH();
                }
            }

            errorLbl.setVisible(false);
            errorLbl.setManaged(false);

            try {
                Timestamp tNhan = Timestamp.valueOf(dpNhan.getValue().atStartOfDay());
                Timestamp tTra  = Timestamp.valueOf(dpTra.getValue().atStartOfDay());
                String maPDP = "PDP" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
                PhieuDatPhong pdp = new PhieuDatPhong(maPDP, maKH, "NV001",
                        soNguoi, tNhan, tTra, txtGhiChu.getText().trim());
                if (new PhieuDatPhongDAO().lapPhieuDat(pdp)) {
                    new PhongDAO().capNhatTrangThai(maPhong, "PhongDat");
                    alert(Alert.AlertType.INFORMATION, "Thành công",
                            "Đặt phòng thành công!\nMã phiếu: " + maPDP + "\nKhách: " + maKH);
                    txtSDT.clear();
                    txtTenKH.clear();
                    txtMaPhong.setText("P101");
                    txtSoNguoi.setText("2");
                    txtGhiChu.clear();
                    dpNhan.setValue(LocalDate.now());
                    dpTra.setValue(LocalDate.now().plusDays(1));
                    resolvedMaKH[0] = null;
                    lblKHStatus.setText("Nhập SĐT rồi bấm Tra Cứu");
                    lblKHStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #8C8C8C;");
                } else {
                    alert(Alert.AlertType.ERROR, "Lỗi",
                            "Lỗi! Kiểm tra mã phòng hoặc lịch trùng.");
                }
            } catch (Exception ex) {
                showError(errorLbl, "Lỗi kết nối cơ sở dữ liệu! Kiểm tra máy chủ.");
            }
        });

        HBox btnRow = new HBox(btnSubmit);
        btnRow.setPadding(new Insets(8, 0, 0, 0));

        card.getChildren().addAll(cardTitle, form, uocTinhBox, errorLbl, btnRow);
        return card;
    }

    /** Tra cứu khách hàng theo SĐT, trả về null nếu không tìm thấy. */
    private KhachHang lookupKhachBySDT(String sdt) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null || sdt == null || sdt.isEmpty()) return null;
        try (PreparedStatement pst = con.prepareStatement(
                "SELECT maKH, hoTenKH, soDienThoai, cmnd FROM KhachHang WHERE soDienThoai = ?")) {
            pst.setString(1, sdt);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new KhachHang(rs.getString("maKH"), rs.getString("hoTenKH"),
                            rs.getString("soDienThoai"), rs.getString("cmnd"));
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    /** Tính chi phí ước tính: query BangGia QuaDem theo loại của phòng × số đêm */
    private void tinhUocTinh(String maPhong, LocalDate ngayNhan, LocalDate ngayTra,
                              Label lblGia, Label lblDem, Label lblTong) {
        DecimalFormat money = new DecimalFormat("#,###");

        // Validate cơ bản
        if (maPhong == null || maPhong.isEmpty()
                || ngayNhan == null || ngayTra == null || !ngayTra.isAfter(ngayNhan)) {
            lblGia.setText("Đơn giá phòng: —");
            lblDem.setText("Số đêm: —");
            lblTong.setText("Tổng: —");
            return;
        }

        long soDem = Math.max(1, ChronoUnit.DAYS.between(ngayNhan, ngayTra));
        lblDem.setText("Số đêm: " + soDem);

        // Query đơn giá QuaDem từ BangGia theo loại phòng
        double donGia = 0;
        Connection con = ConnectDB.getInstance().getConnection();
        if (con != null) {
            String sql = "SELECT bg.donGia FROM Phong p "
                    + "LEFT JOIN BangGia bg ON bg.maLoaiPhong = p.maLoaiPhong "
                    + "  AND bg.loaiThue = 'QuaDem' "
                    + "  AND GETDATE() BETWEEN bg.ngayBatDau AND bg.ngayKetThuc "
                    + "WHERE p.maPhong = ?";
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, maPhong);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) donGia = rs.getDouble("donGia");
                }
            } catch (Exception ignored) {}
        }

        if (donGia == 0) {
            lblGia.setText("Đơn giá phòng: (không tìm thấy phòng " + maPhong + ")");
            lblTong.setText("Tổng: —");
            return;
        }

        double tong = donGia * soDem;
        lblGia.setText("Đơn giá phòng: " + money.format(donGia) + " VNĐ / đêm");
        lblTong.setText("Tổng ước tính: " + money.format(tong) + " VNĐ");
    }

    // ── Helpers ──
    private TextField field(String defaultVal) {
        TextField tf = new TextField(defaultVal);
        tf.setMaxWidth(Double.MAX_VALUE);
        tf.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D9D9D9;"
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1;"
                + "-fx-padding: 8 10 8 10;");
        return tf;
    }

    private DatePicker datePicker(LocalDate val) {
        DatePicker dp = new DatePicker(val);
        dp.setMaxWidth(Double.MAX_VALUE);
        dp.setStyle(comboStyle());
        return dp;
    }

    private Label lbl(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #595959;");
        return l;
    }

    private String comboStyle() {
        return "-fx-background-color: #FFFFFF; -fx-border-color: #D9D9D9;"
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1;";
    }

    private void showError(Label lbl, String msg) {
        lbl.setText(msg);
        lbl.setVisible(true);
        lbl.setManaged(true);
    }

    private void alert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type, msg);
        a.setHeaderText(null);
        a.setTitle(title);
        a.showAndWait();
    }
}
