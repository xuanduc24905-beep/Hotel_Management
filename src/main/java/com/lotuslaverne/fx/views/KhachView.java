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

    // Form thêm mới (inline at top)
    private TextField tfTen, tfSDT, tfCCCD, tfNgaySinh, tfDiaChi, tfQuocTich;
    private ComboBox<String> cbGioiTinh;

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
        Label title = new Label("Quản Lý Liên Lạc Khách Hàng");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        countLbl = new Label("Đang tải...");
        countLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #8C8C8C;");
        header.getChildren().addAll(title, countLbl);

        items = FXCollections.observableArrayList(loadData());
        updateCount();

        content.getChildren().addAll(header, buildAddForm(), buildToolbar(), buildCards());
        scroll.setContent(content);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        root.getChildren().add(scroll);
        return root;
    }

    // ── Form Thêm Khách Mới (inline, chỉ dùng cho thêm) ─────────────────
    private Node buildAddForm() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        Label formTitle = new Label("+ Thêm Khách Hàng Mới");
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

        Button btnAdd = new Button("+ Thêm Khách Hàng");
        btnAdd.setStyle("-fx-background-color:#1890FF;-fx-text-fill:white;"
                + "-fx-background-radius:8;-fx-padding:8 20;-fx-font-weight:bold;-fx-cursor:hand;");

        btnAdd.setOnAction(e -> {
            String ten = tfTen.getText().trim();
            String sdt = tfSDT.getText().trim();
            String cccd = tfCCCD.getText().trim();
            if (ten.isEmpty() || sdt.isEmpty() || cccd.isEmpty()) {
                errLbl.setText("Vui lòng nhập Họ Tên, SĐT và CCCD!");
                errLbl.setVisible(true); errLbl.setManaged(true); return;
            }
            errLbl.setVisible(false); errLbl.setManaged(false);
            String maKH = "KH" + UUID.randomUUID().toString().substring(0,4).toUpperCase();
            KhachHang kh = buildKhachHang(maKH, ten, sdt, cccd,
                    cbGioiTinh.getValue(), tfNgaySinh.getText().trim(),
                    tfDiaChi.getText().trim(), tfQuocTich.getText().trim());
            KhachHangDAO dao = new KhachHangDAO();
            try {
                boolean ok = dao.themKhachHang(kh);
                if (ok) {
                    Object[] row = {maKH, ten, sdt, cccd, cbGioiTinh.getValue(),
                        tfNgaySinh.getText().trim(), tfDiaChi.getText().trim(), tfQuocTich.getText().trim()};
                    items.add(row);
                    refreshCards(); updateCount();
                    tfTen.clear(); tfSDT.clear(); tfCCCD.clear();
                    tfNgaySinh.clear(); tfDiaChi.clear(); tfQuocTich.setText("Việt Nam"); cbGioiTinh.setValue("Nam");
                    new Alert(Alert.AlertType.INFORMATION, "Thêm khách hàng thành công!\nMã: " + maKH).showAndWait();
                } else {
                    String msg = dao.getLastError() != null ? dao.getLastError() : "Thêm thất bại! Kiểm tra dữ liệu.";
                    errLbl.setText(msg); errLbl.setVisible(true); errLbl.setManaged(true);
                }
            } catch (Exception ex) {
                errLbl.setText("Lỗi: " + ex.getMessage()); errLbl.setVisible(true); errLbl.setManaged(true);
            }
        });

        HBox btnRow = new HBox(btnAdd);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        card.getChildren().addAll(formTitle, form, errLbl, btnRow);
        return card;
    }

    // ── Dialog sửa thông tin (popup modal) ───────────────────────────────
    private void openEditDialog(Object[] row) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Sửa Thông Tin Khách — " + row[1]);
        dialog.setResizable(false);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color:#F0F2F5;");

        // Header dialog
        VBox hdr = new VBox(3);
        hdr.setPadding(new Insets(20, 24, 12, 24));
        hdr.setStyle("-fx-background-color:#FFFFFF;"
                + "-fx-border-color:transparent transparent #F0F2F5 transparent;-fx-border-width:0 0 1 0;");
        Label hdrTitle = new Label("✏  Sửa Thông Tin Khách Hàng");
        hdrTitle.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#1A1A2E;");
        Label hdrSub = new Label("Mã khách: " + row[0]);
        hdrSub.setStyle("-fx-font-size:12px;-fx-text-fill:#8C8C8C;");
        hdr.getChildren().addAll(hdrTitle, hdrSub);

        // Form fields
        VBox formWrap = new VBox(14);
        formWrap.setPadding(new Insets(20, 24, 4, 24));
        formWrap.setStyle("-fx-background-color:#F0F2F5;");

        GridPane form = new GridPane();
        form.setHgap(16); form.setVgap(10);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:10;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");
        for (int i = 0; i < 2; i++) {
            ColumnConstraints cc = new ColumnConstraints(); cc.setPercentWidth(50);
            form.getColumnConstraints().add(cc);
        }

        TextField dTen      = fieldVal(row[1].toString());
        TextField dSDT      = fieldVal(row[2].toString());
        TextField dCCCD     = fieldVal(row[3].toString());
        TextField dNgaySinh = fieldVal(row[5] != null ? row[5].toString() : "");
        TextField dDiaChi   = fieldVal(row[6] != null ? row[6].toString() : "");
        TextField dQuocTich = fieldVal(row[7] != null ? row[7].toString() : "Việt Nam");
        ComboBox<String> dGioiTinh = new ComboBox<>();
        dGioiTinh.getItems().addAll("Nam","Nữ");
        dGioiTinh.setValue(row[4] != null ? row[4].toString() : "Nam");
        dGioiTinh.setMaxWidth(Double.MAX_VALUE);
        dGioiTinh.setStyle("-fx-background-color:#FFF;-fx-border-color:#D9D9D9;"
                + "-fx-border-radius:6;-fx-background-radius:6;-fx-border-width:1;");

        form.add(lbl("Họ Và Tên *"),  0,0); form.add(dTen,      0,1);
        form.add(lbl("SĐT *"),        1,0); form.add(dSDT,      1,1);
        form.add(lbl("CCCD *"),       0,2); form.add(dCCCD,     0,3);
        form.add(lbl("Giới Tính"),    1,2); form.add(dGioiTinh, 1,3);
        form.add(lbl("Ngày Sinh"),    0,4); form.add(dNgaySinh, 0,5);
        form.add(lbl("Địa Chỉ"),     0,6); form.add(dDiaChi,   0,7);
        form.add(lbl("Quốc Tịch"),   1,4); form.add(dQuocTich, 1,5);

        Label errLbl = new Label();
        errLbl.setStyle("-fx-text-fill:#FF4D4F;-fx-font-size:12px;");
        errLbl.setVisible(false); errLbl.setManaged(false);

        formWrap.getChildren().addAll(form, errLbl);

        // Footer buttons
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(16, 24, 20, 24));
        footer.setStyle("-fx-background-color:#FFFFFF;"
                + "-fx-border-color:#F0F2F5 transparent transparent transparent;-fx-border-width:1 0 0 0;");

        Button btnCancel = new Button("Hủy");
        btnCancel.setStyle("-fx-background-color:#F5F5F5;-fx-text-fill:#595959;"
                + "-fx-background-radius:8;-fx-border-color:#D9D9D9;-fx-border-width:1;"
                + "-fx-border-radius:8;-fx-padding:9 20;-fx-cursor:hand;");
        btnCancel.setOnAction(e -> dialog.close());

        Button btnSave = new Button("💾  Lưu Thay Đổi");
        btnSave.setStyle("-fx-background-color:#FA8C16;-fx-text-fill:white;"
                + "-fx-background-radius:8;-fx-padding:9 24;-fx-font-weight:bold;-fx-cursor:hand;");
        btnSave.setDefaultButton(true);

        btnSave.setOnAction(e -> {
            String ten  = dTen.getText().trim();
            String sdt  = dSDT.getText().trim();
            String cccd = dCCCD.getText().trim();
            if (ten.isEmpty() || sdt.isEmpty() || cccd.isEmpty()) {
                errLbl.setText("Vui lòng nhập Họ Tên, SĐT và CCCD!");
                errLbl.setVisible(true); errLbl.setManaged(true); return;
            }
            errLbl.setVisible(false); errLbl.setManaged(false);
            KhachHang kh = buildKhachHang(row[0].toString(), ten, sdt, cccd,
                    dGioiTinh.getValue(), dNgaySinh.getText().trim(),
                    dDiaChi.getText().trim(), dQuocTich.getText().trim());
            try {
                boolean ok = new KhachHangDAO().suaKhachHang(kh);
                if (ok) {
                    row[1] = ten;  row[2] = sdt;  row[3] = cccd;
                    row[4] = dGioiTinh.getValue();
                    row[5] = dNgaySinh.getText().trim();
                    row[6] = dDiaChi.getText().trim();
                    row[7] = dQuocTich.getText().trim();
                    refreshCards();
                    dialog.close();
                } else {
                    errLbl.setText("Cập nhật thất bại! Kiểm tra kết nối DB.");
                    errLbl.setVisible(true); errLbl.setManaged(true);
                }
            } catch (Exception ex) {
                errLbl.setText("Lỗi: " + ex.getMessage());
                errLbl.setVisible(true); errLbl.setManaged(true);
            }
        });

        footer.getChildren().addAll(btnCancel, btnSave);
        root.getChildren().addAll(hdr, formWrap, footer);

        dialog.setScene(new Scene(root, 480, 460));
        dialog.showAndWait();
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
            for (Object[] r : items)
                for (Object c : r) if (c != null && c.toString().toLowerCase().contains(kw)) { f.add(r); break; }
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
        for (Object[] row : rows) cardsPane.getChildren().add(makeCard(row));
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

        String ten = row[1].toString();
        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);
        Node avatar = UiUtils.makeAvatarCircle(ten, 18);
        VBox nameBox = new VBox(2);
        Label nameLbl = new Label(ten);
        nameLbl.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#1A1A2E;");
        Label maLbl = new Label(row[0].toString());
        maLbl.setStyle("-fx-font-size:11px;-fx-text-fill:#1890FF;");
        nameBox.getChildren().addAll(nameLbl, maLbl);
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label nationBadge = new Label(row[7] != null ? row[7].toString() : "Việt Nam");
        nationBadge.setStyle("-fx-background-color:#E6F4FF;-fx-text-fill:#1890FF;"
                + "-fx-padding:2 8;-fx-background-radius:10;-fx-font-size:11px;");
        topRow.getChildren().addAll(avatar, nameBox, sp, nationBadge);

        Separator sep = new Separator();

        VBox infoBox = new VBox(6);
        infoBox.getChildren().addAll(
            infoRow("📞", row[2].toString()),
            infoRow("🪪", "CCCD: " + row[3].toString()),
            infoRow(row[4] != null && "Nữ".equals(row[4].toString()) ? "👩" : "👨",
                (row[4] != null ? row[4].toString() : "Nam")
                + (row[5] != null && !row[5].toString().isEmpty() ? "  •  Sinh: " + row[5] : "")),
            infoRow("🏠", row[6] != null && !row[6].toString().isEmpty() ? row[6].toString() : "Chưa cập nhật")
        );

        Button btnSua = new Button("✏  Sửa");
        btnSua.setStyle("-fx-background-color:#E6F4FF;-fx-text-fill:#1890FF;"
                + "-fx-background-radius:8;-fx-font-size:12px;-fx-padding:5 14;-fx-cursor:hand;");
        Button btnLichSu = new Button("📋  Lịch Sử");
        btnLichSu.setStyle("-fx-background-color:#F6FFED;-fx-text-fill:#52C41A;"
                + "-fx-background-radius:8;-fx-font-size:12px;-fx-padding:5 14;-fx-cursor:hand;");

        btnSua.setOnAction(e -> openEditDialog(row));
        btnLichSu.setOnAction(e -> showLichSu(row[0].toString(), ten));

        HBox actions = new HBox(8, btnSua, btnLichSu);
        card.getChildren().addAll(topRow, sep, infoBox, actions);

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color:#FAFAFA;-fx-background-radius:12;"
                + "-fx-effect: dropshadow(gaussian,rgba(24,144,255,0.15),12,0,0,3);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:12;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.07),8,0,0,2);"));
        return card;
    }

    private HBox infoRow(String icon, String text) {
        HBox row = new HBox(8); row.setAlignment(Pos.CENTER_LEFT);
        Label ico = new Label(icon); ico.setStyle("-fx-font-size:13px;");
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
    private KhachHang buildKhachHang(String maKH, String ten, String sdt, String cccd,
                                      String gioiTinh, String ngaySinh, String diaChi, String quocTich) {
        KhachHang kh = new KhachHang();
        kh.setMaKH(maKH);
        kh.setHoTenKH(ten);
        kh.setSoDienThoai(sdt);
        kh.setCmnd(cccd);
        kh.setGioiTinh("Nam".equals(gioiTinh));
        kh.setNgaySinh(ngaySinh);
        kh.setDiaChi(diaChi);
        kh.setQuocTich(quocTich);
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

    private TextField fieldVal(String value) {
        TextField tf = field("");
        tf.setText(value);
        return tf;
    }
}
