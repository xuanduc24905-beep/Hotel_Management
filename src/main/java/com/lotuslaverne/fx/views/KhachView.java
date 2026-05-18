package com.lotuslaverne.fx.views;

import com.lotuslaverne.dao.KhachHangDAO;
import com.lotuslaverne.dao.PhieuDatPhongDAO;
import com.lotuslaverne.entity.KhachHang;
import com.lotuslaverne.entity.PhieuDatPhong;
import com.lotuslaverne.fx.UiUtils;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KhachView {

    private static final Object[][] DEMO = {
        {"KH001","Nguyễn Văn An","0912345678","012345678901","Nam","01/01/1990","123 Lê Lợi, Q1, HCM","Việt Nam"},
        {"KH002","Trần Thị Bình","0923456789","023456789012","Nữ","15/05/1992","456 Hai Bà Trưng, HN","Việt Nam"},
        {"KH003","Robert Smith","0934567890","US12345678","Nam","20/03/1985","New York, USA","Mỹ"},
    };

    private ObservableList<Object[]> items;
    private FlowPane cardsPane;
    private Label countLbl;

    // ── Form sửa inline ──
    private Object[] selectedRow;
    private TextField tfTen, tfSDT, tfCCCD, tfDiaChi, tfNgaySinh, tfQuocTich;
    private ComboBox<String> cbGioiTinh;
    private Label formTitle;
    private Button btnSave, btnCancel;
    private VBox formCard;

    public Node build() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #F0F2F5;");

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #F0F2F5; -fx-border-color: transparent;");

        VBox content = new VBox(20);
        content.setPadding(new Insets(28));
        content.setStyle("-fx-background-color: #F0F2F5;");

        // Header
        VBox header = new VBox(4);
        Label title = new Label("Quản Lý Liên Lạc Khách Hàng");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        countLbl = new Label("Đang tải...");
        countLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #8C8C8C;");
        header.getChildren().addAll(title, countLbl);

        items = FXCollections.observableArrayList(loadData());
        updateCount();

        content.getChildren().addAll(header, buildFormCard(), buildToolbar(), buildCards());
        scroll.setContent(content);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        root.getChildren().add(scroll);
        return root;
    }

    // ── Form Thêm / Sửa (Inline) ──────────────────────────────────────
    private Node buildFormCard() {
        formCard = new VBox(14);
        formCard.setPadding(new Insets(20));
        formCard.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        formTitle = new Label("+ Thêm Khách Hàng Mới");
        formTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");

        GridPane form = new GridPane();
        form.setHgap(16); form.setVgap(10);
        for (int i = 0; i < 4; i++) {
            ColumnConstraints cc = new ColumnConstraints(); cc.setPercentWidth(25);
            form.getColumnConstraints().add(cc);
        }

        tfTen      = field("Họ và tên *");
        tfSDT      = field("Số điện thoại *");
        tfCCCD     = field("CCCD / Hộ chiếu *");
        tfNgaySinh = field("dd/MM/yyyy");
        tfDiaChi   = field("Địa chỉ");
        tfQuocTich = field("Quốc tịch");
        tfQuocTich.setText("Việt Nam");

        cbGioiTinh = new ComboBox<>();
        cbGioiTinh.getItems().addAll("Nam","Nữ");
        cbGioiTinh.setValue("Nam");
        cbGioiTinh.setMaxWidth(Double.MAX_VALUE);
        cbGioiTinh.setStyle("-fx-background-color:#FFF;-fx-border-color:#D9D9D9;"
                + "-fx-border-radius:6;-fx-background-radius:6;-fx-border-width:1;");

        form.add(lbl("Họ Và Tên *"),   0,0); form.add(tfTen,      0,1);
        form.add(lbl("SĐT *"),         1,0); form.add(tfSDT,      1,1);
        form.add(lbl("CCCD *"),        2,0); form.add(tfCCCD,     2,1);
        form.add(lbl("Giới Tính"),     3,0); form.add(cbGioiTinh, 3,1);
        form.add(lbl("Ngày Sinh"),     0,2); form.add(tfNgaySinh, 0,3);
        form.add(lbl("Địa Chỉ"),      1,2); form.add(tfDiaChi,   1,3);
        form.add(lbl("Quốc Tịch"),    2,2); form.add(tfQuocTich, 2,3);

        Label errLbl = new Label();
        errLbl.setStyle("-fx-text-fill:#FF4D4F;-fx-font-size:12px;");
        errLbl.setVisible(false); errLbl.setManaged(false);

        btnSave = new Button("+ Thêm Khách Hàng");
        btnSave.setStyle("-fx-background-color:#1890FF;-fx-text-fill:white;"
                + "-fx-background-radius:8;-fx-padding:8 20;-fx-font-weight:bold;-fx-cursor:hand;");

        btnCancel = new Button("✕ Hủy Sửa");
        btnCancel.setStyle("-fx-background-color:#F5F5F5;-fx-text-fill:#595959;"
                + "-fx-background-radius:8;-fx-padding:8 14;-fx-cursor:hand;");
        btnCancel.setVisible(false); btnCancel.setManaged(false);
        btnCancel.setOnAction(e -> resetForm());

        btnSave.setOnAction(e -> {
            String ten = tfTen.getText().trim();
            String sdt = tfSDT.getText().trim();
            String cccd = tfCCCD.getText().trim();
            if (ten.isEmpty() || sdt.isEmpty() || cccd.isEmpty()) {
                errLbl.setText("Vui lòng nhập Họ Tên, SĐT và CCCD!"); errLbl.setVisible(true); errLbl.setManaged(true); return;
            }
            errLbl.setVisible(false); errLbl.setManaged(false);
            if (selectedRow != null) {
                // Chế độ SỬA
                KhachHang kh = buildFromForm(selectedRow[0].toString());
                try {
                    boolean ok = new KhachHangDAO().suaKhachHang(kh);
                    if (ok) {
                        selectedRow[1] = ten; selectedRow[2] = sdt; selectedRow[3] = cccd;
                        selectedRow[4] = cbGioiTinh.getValue();
                        selectedRow[5] = tfNgaySinh.getText().trim();
                        selectedRow[6] = tfDiaChi.getText().trim();
                        selectedRow[7] = tfQuocTich.getText().trim();
                        refreshCards(); resetForm();
                    } else { errLbl.setText("Cập nhật thất bại! Kiểm tra DB."); errLbl.setVisible(true); errLbl.setManaged(true); }
                } catch (Exception ex) { errLbl.setText("Lỗi: " + ex.getMessage()); errLbl.setVisible(true); errLbl.setManaged(true); }
            } else {
                // Chế độ THÊM
                String maKH = "KH" + UUID.randomUUID().toString().substring(0,4).toUpperCase();
                KhachHang kh = buildFromForm(maKH);
                System.out.println("[DEBUG] Thêm KH: ma=" + kh.getMaKH()
                    + ", ten=" + kh.getHoTenKH()
                    + ", cmnd=" + kh.getCmnd()
                    + ", sdt=" + kh.getSoDienThoai());
                KhachHangDAO dao = new KhachHangDAO();
                try {
                    boolean ok = dao.themKhachHang(kh);
                    if (ok) {
                        Object[] row = {maKH, ten, sdt, cccd, cbGioiTinh.getValue(),
                            tfNgaySinh.getText().trim(), tfDiaChi.getText().trim(), tfQuocTich.getText().trim()};
                        items.add(row);
                        refreshCards(); resetForm(); updateCount();
                        new Alert(Alert.AlertType.INFORMATION, "Thêm khách hàng thành công!\nMã: " + maKH).showAndWait();
                    } else {
                        String errMsg = dao.getLastError() != null ? dao.getLastError() : "Thêm thất bại! Kiểm tra dữ liệu.";
                        errLbl.setText(errMsg); errLbl.setVisible(true); errLbl.setManaged(true);
                        new Alert(Alert.AlertType.ERROR, errMsg).showAndWait();
                    }
                } catch (Exception ex) {
                    String errMsg = "Lỗi: " + ex.getMessage();
                    errLbl.setText(errMsg); errLbl.setVisible(true); errLbl.setManaged(true);
                    new Alert(Alert.AlertType.ERROR, errMsg).showAndWait();
                }
            }
        });

        HBox btnRow = new HBox(10, btnCancel, btnSave);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        formCard.getChildren().addAll(formTitle, form, errLbl, btnRow);
        return formCard;
    }

    // ── Toolbar tìm kiếm ──────────────────────────────────────────────
    private HBox buildToolbar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12,16,12,16));
        bar.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        TextField search = new TextField();
        search.setPromptText("🔍  Tìm theo tên, SĐT, CCCD, địa chỉ...");
        search.setPrefWidth(300);
        search.setStyle("-fx-background-color:#F5F5F5;-fx-border-color:#E8E8E8;"
                + "-fx-border-radius:8;-fx-background-radius:8;-fx-border-width:1;"
                + "-fx-padding:8 12;-fx-font-size:13px;");

        search.textProperty().addListener((obs,o,n) -> {
            if (n == null || n.trim().isEmpty()) { renderCards(items); return; }
            String kw = n.toLowerCase();
            List<Object[]> f = new ArrayList<>();
            for (Object[] r : items) {
                for (Object c : r) if (c != null && c.toString().toLowerCase().contains(kw)) { f.add(r); break; }
            }
            renderCards(FXCollections.observableArrayList(f));
        });

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnRefresh = new Button("↻  Làm Mới");
        btnRefresh.setStyle("-fx-background-color:#F5F5F5;-fx-text-fill:#595959;"
                + "-fx-background-radius:8;-fx-border-color:#D9D9D9;-fx-border-width:1;"
                + "-fx-border-radius:8;-fx-padding:8 14;-fx-cursor:hand;");
        btnRefresh.setOnAction(e -> {
            items.setAll(loadData()); refreshCards(); updateCount();
            UiUtils.flashButton(btnRefresh, "✓ Đã làm mới");
        });

        bar.getChildren().addAll(search, spacer, btnRefresh);
        return bar;
    }

    // ── Cards ──────────────────────────────────────────────────────────
    private FlowPane buildCards() {
        cardsPane = new FlowPane(16, 16);
        cardsPane.setPadding(new Insets(4, 0, 0, 0));
        renderCards(items);
        return cardsPane;
    }

    private void renderCards(ObservableList<Object[]> rows) {
        cardsPane.getChildren().clear();
        for (Object[] row : rows) {
            cardsPane.getChildren().add(makeCard(row));
        }
        if (rows.isEmpty()) {
            Label empty = new Label("Không tìm thấy khách hàng.");
            empty.setStyle("-fx-text-fill:#8C8C8C;-fx-font-size:14px;");
            cardsPane.getChildren().add(empty);
        }
    }

    private Node makeCard(Object[] row) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setPrefWidth(320);
        card.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:12;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.07),8,0,0,2);");

        // Header: avatar + tên + mã
        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);
        String ten = row[1].toString();
        Node avatar = UiUtils.makeAvatarCircle(ten, 18);
        VBox nameBox = new VBox(2);
        Label nameLbl = new Label(ten);
        nameLbl.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#1A1A2E;");
        Label maLbl = new Label(row[0].toString());
        maLbl.setStyle("-fx-font-size:11px;-fx-text-fill:#1890FF;");
        nameBox.getChildren().addAll(nameLbl, maLbl);
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        // Badge quốc tịch
        Label nationBadge = new Label(row[7] != null ? row[7].toString() : "Việt Nam");
        nationBadge.setStyle("-fx-background-color:#E6F4FF;-fx-text-fill:#1890FF;"
                + "-fx-padding:2 8;-fx-background-radius:10;-fx-font-size:11px;");
        topRow.getChildren().addAll(avatar, nameBox, sp, nationBadge);

        Separator sep = new Separator();

        // Thông tin liên lạc
        VBox infoBox = new VBox(6);
        infoBox.getChildren().addAll(
            infoRow("📞", row[2].toString()),
            infoRow("🪪", "CCCD: " + row[3].toString()),
            infoRow(row[4] != null && "Nữ".equals(row[4].toString()) ? "👩" : "👨",
                (row[4] != null ? row[4].toString() : "Nam")
                + (row[5] != null && !row[5].toString().isEmpty() ? "  •  Sinh: " + row[5] : "")),
            infoRow("🏠", row[6] != null && !row[6].toString().isEmpty() ? row[6].toString() : "Chưa cập nhật")
        );

        // Nút hành động
        Button btnSua = new Button("✏  Sửa");
        btnSua.setStyle("-fx-background-color:#E6F4FF;-fx-text-fill:#1890FF;"
                + "-fx-background-radius:8;-fx-font-size:12px;-fx-padding:5 14;-fx-cursor:hand;");
        Button btnLichSu = new Button("📋  Lịch Sử");
        btnLichSu.setStyle("-fx-background-color:#F6FFED;-fx-text-fill:#52C41A;"
                + "-fx-background-radius:8;-fx-font-size:12px;-fx-padding:5 14;-fx-cursor:hand;");

        btnSua.setOnAction(e -> populateForm(row));
        btnLichSu.setOnAction(e -> showLichSu(row[0].toString(), ten));

        HBox actions = new HBox(8, btnSua, btnLichSu);
        card.getChildren().addAll(topRow, sep, infoBox, actions);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color:#FAFAFA;-fx-background-radius:12;"
                + "-fx-effect: dropshadow(gaussian,rgba(24,144,255,0.15),12,0,0,3);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:12;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.07),8,0,0,2);"));
        return card;
    }

    private HBox infoRow(String icon, String text) {
        HBox row = new HBox(8); row.setAlignment(Pos.CENTER_LEFT);
        Label ico = new Label(icon);
        ico.setStyle("-fx-font-size:13px;");
        Label txt = new Label(text);
        txt.setStyle("-fx-font-size:12px;-fx-text-fill:#595959;");
        txt.setWrapText(true); txt.setMaxWidth(270);
        row.getChildren().addAll(ico, txt);
        return row;
    }

    // ── Lịch Sử Đặt Phòng ────────────────────────────────────────────
    private void showLichSu(String maKH, String tenKH) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Lịch Sử Đặt Phòng — " + tenKH);
        dialog.setResizable(true);

        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color:#F0F2F5;");

        Label hdr = new Label("📋  Lịch sử đặt phòng của: " + tenKH);
        hdr.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#1A1A2E;");

        ListView<String> listView = new ListView<>();
        listView.setStyle("-fx-background-color:#FFFFFF;-fx-border-color:#E8E8E8;");

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            List<PhieuDatPhong> pdps = new PhieuDatPhongDAO().getAll();
            for (PhieuDatPhong p : pdps) {
                if (maKH.equals(p.getMaKhachHang())) {
                    String entry = String.format("[%s]  Nhận: %s  →  Trả: %s  |  Khách: %d  |  %s",
                        p.getMaPhieuDatPhong(),
                        p.getThoiGianNhanDuKien() != null ? sdf.format(p.getThoiGianNhanDuKien()) : "—",
                        p.getThoiGianTraDuKien()  != null ? sdf.format(p.getThoiGianTraDuKien())  : "—",
                        p.getSoNguoi(),
                        p.getGhiChu() != null ? p.getGhiChu() : "");
                    listView.getItems().add(entry);
                }
            }
        } catch (Exception ignored) {}

        if (listView.getItems().isEmpty())
            listView.getItems().add("Chưa có lịch sử đặt phòng.");

        Button btnClose = new Button("Đóng");
        btnClose.setStyle("-fx-background-color:#1890FF;-fx-text-fill:white;"
                + "-fx-background-radius:8;-fx-padding:8 20;-fx-cursor:hand;");
        btnClose.setOnAction(e -> dialog.close());
        HBox btnRow = new HBox(btnClose); btnRow.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(hdr, listView, btnRow);
        dialog.setScene(new Scene(root, 600, 400));
        dialog.showAndWait();
    }

    // ── Helpers ───────────────────────────────────────────────────────
    private void populateForm(Object[] row) {
        selectedRow = row;
        formTitle.setText("✏  Sửa Thông Tin: " + row[1]);
        tfTen.setText      (row[1].toString());
        tfSDT.setText      (row[2].toString());
        tfCCCD.setText     (row[3].toString());
        cbGioiTinh.setValue(row[4] != null ? row[4].toString() : "Nam");
        tfNgaySinh.setText (row[5] != null ? row[5].toString() : "");
        tfDiaChi.setText   (row[6] != null ? row[6].toString() : "");
        tfQuocTich.setText (row[7] != null ? row[7].toString() : "Việt Nam");
        btnSave.setText("💾  Lưu Thay Đổi");
        btnSave.setStyle("-fx-background-color:#FA8C16;-fx-text-fill:white;"
                + "-fx-background-radius:8;-fx-padding:8 20;-fx-font-weight:bold;-fx-cursor:hand;");
        btnCancel.setVisible(true); btnCancel.setManaged(true);
        formCard.setStyle("-fx-background-color:#FFFBE6;-fx-background-radius:10;"
                + "-fx-border-color:#FAAD14;-fx-border-width:1;-fx-border-radius:10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.08),8,0,0,2);");
    }

    private void resetForm() {
        selectedRow = null;
        formTitle.setText("+ Thêm Khách Hàng Mới");
        tfTen.clear(); tfSDT.clear(); tfCCCD.clear();
        tfNgaySinh.clear(); tfDiaChi.clear();
        tfQuocTich.setText("Việt Nam"); cbGioiTinh.setValue("Nam");
        btnSave.setText("+ Thêm Khách Hàng");
        btnSave.setStyle("-fx-background-color:#1890FF;-fx-text-fill:white;"
                + "-fx-background-radius:8;-fx-padding:8 20;-fx-font-weight:bold;-fx-cursor:hand;");
        btnCancel.setVisible(false); btnCancel.setManaged(false);
        formCard.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");
    }

    private KhachHang buildFromForm(String maKH) {
        KhachHang kh = new KhachHang();
        kh.setMaKH       (maKH);
        kh.setHoTenKH    (tfTen.getText().trim());
        kh.setSoDienThoai(tfSDT.getText().trim());
        kh.setCmnd       (tfCCCD.getText().trim());
        kh.setGioiTinh   ("Nam".equals(cbGioiTinh.getValue()));
        kh.setNgaySinh   (tfNgaySinh.getText().trim());
        kh.setDiaChi     (tfDiaChi.getText().trim());
        kh.setQuocTich   (tfQuocTich.getText().trim());
        return kh;
    }

    private void refreshCards() { renderCards(items); }

    private void updateCount() {
        if (countLbl != null) countLbl.setText("Tổng cộng " + items.size() + " khách hàng trong hệ thống");
    }

    private List<Object[]> loadData() {
        List<Object[]> result = new ArrayList<>();
        try {
            List<KhachHang> list = new KhachHangDAO().getAll();
            if (!list.isEmpty()) {
                for (KhachHang kh : list) {
                    result.add(new Object[]{
                        kh.getMaKH(), kh.getHoTenKH(), kh.getSoDienThoai(), kh.getCmnd(),
                        kh.isGioiTinh() ? "Nam" : "Nữ",
                        kh.getNgaySinh() != null ? kh.getNgaySinh() : "",
                        kh.getDiaChi()   != null ? kh.getDiaChi()   : "",
                        kh.getQuocTich() != null ? kh.getQuocTich() : "Việt Nam"
                    });
                }
                return result;
            }
        } catch (Exception ignored) {}
        for (Object[] r : DEMO) result.add(r);
        return result;
    }

    private Label lbl(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#595959;");
        return l;
    }

    private TextField field(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt); tf.setMaxWidth(Double.MAX_VALUE);
        tf.setStyle("-fx-background-color:#FFF;-fx-border-color:#D9D9D9;"
                + "-fx-border-radius:6;-fx-background-radius:6;-fx-border-width:1;-fx-padding:7 10;");
        return tf;
    }
}
