package com.lotuslaverne.fx.views;

import com.lotuslaverne.dao.KhachHangDAO;
import com.lotuslaverne.dao.PhieuDatPhongDAO;
import com.lotuslaverne.dao.PhongDAO;
import com.lotuslaverne.entity.KhachHang;
import com.lotuslaverne.entity.PhieuDatPhong;
import com.lotuslaverne.util.ConnectDB;
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
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatPhongView {

    /** Mã phòng được điền sẵn từ màn hình Quản Lý Phòng (null = mở bình thường). */
    private final String prefillMaPhong;

    public DatPhongView() { this.prefillMaPhong = null; }
    public DatPhongView(String maPhong) { this.prefillMaPhong = maPhong; }

    public Node build() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #F0F2F5;");

        // Header
        VBox header = new VBox(4);
        header.setPadding(new Insets(24, 28, 8, 28));
        Label title = new Label("Đặt Phòng");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Label sub = new Label("Lập phiếu đặt phòng & xem lịch đặt theo phòng");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #8C8C8C;");
        header.getChildren().addAll(title, sub);
        if (prefillMaPhong != null && !prefillMaPhong.isEmpty()) {
            HBox banner = new HBox(8);
            banner.setAlignment(Pos.CENTER_LEFT);
            banner.setPadding(new Insets(8, 12, 8, 12));
            banner.setStyle("-fx-background-color:#E6F4FF;-fx-background-radius:8;"
                +"-fx-border-color:#91CAFF;-fx-border-width:1;-fx-border-radius:8;");
            Label bi = new Label("🛎"); bi.setStyle("-fx-font-size:15px;");
            Label bt = new Label("Đang đặt phòng: " + prefillMaPhong + "  —  Nhập thông tin khách hàng để hoàn tất.");
            bt.setStyle("-fx-font-size:12px;-fx-text-fill:#1890FF;-fx-font-weight:bold;");
            banner.getChildren().addAll(bi, bt);
            header.getChildren().add(banner);
        }

        // TabPane
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle("-fx-background-color:#F0F2F5;");
        VBox.setVgrow(tabs, Priority.ALWAYS);

        // Tab 1: Form
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:#F0F2F5;-fx-border-color:transparent;");
        VBox fc = new VBox(20);
        fc.setPadding(new Insets(20, 28, 28, 28));
        fc.setStyle("-fx-background-color:#F0F2F5;");
        fc.getChildren().add(buildFormCard());
        scroll.setContent(fc);
        Tab tabForm = new Tab("📋  Đặt Phòng", scroll);

        // Tab 2: Calendar
        Tab tabCal = new Tab("📅  Lịch Đặt Phòng", buildCalendarTab());

        tabs.getTabs().addAll(tabForm, tabCal);
        root.getChildren().addAll(header, tabs);
        VBox.setVgrow(root, Priority.ALWAYS);
        return root;
    }

    // ── CALENDAR TAB ──────────────────────────────────────────────────────────
    private Node buildCalendarTab() {
        VBox container = new VBox(0);
        container.setStyle("-fx-background-color:#F0F2F5;");
        VBox.setVgrow(container, Priority.ALWAYS);

        LocalDate[] cur = {LocalDate.now()};
        Label lblRange = new Label();
        lblRange.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1A1A2E;");

        Button btnPrev = navBtn("◄ Tuần trước");
        Button btnNext = navBtn("Tuần sau ►");
        Button btnToday = new Button("Hôm nay");
        btnToday.setStyle("-fx-background-color:#1890FF;-fx-text-fill:white;"
            +"-fx-background-radius:6;-fx-padding:7 14;-fx-cursor:hand;-fx-font-weight:bold;");

        ScrollPane ganttScroll = new ScrollPane();
        ganttScroll.setFitToWidth(true);
        ganttScroll.setStyle("-fx-background-color:transparent;-fx-border-color:transparent;");
        VBox.setVgrow(ganttScroll, Priority.ALWAYS);

        Runnable render = () -> {
            ganttScroll.setContent(buildGantt(cur[0]));
            lblRange.setText(cur[0].format(DateTimeFormatter.ofPattern("'Tuần:' dd/MM"))
                + " – " + cur[0].plusDays(6).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        };
        btnPrev.setOnAction(e -> { cur[0] = cur[0].minusWeeks(1); render.run(); });
        btnNext.setOnAction(e -> { cur[0] = cur[0].plusWeeks(1); render.run(); });
        btnToday.setOnAction(e -> { cur[0] = LocalDate.now(); render.run(); });
        render.run();

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        HBox toolbar = new HBox(10, btnPrev, btnNext, btnToday, sp, lblRange);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(12, 28, 10, 28));
        toolbar.setStyle("-fx-background-color:#FFFFFF;"
            +"-fx-border-color:transparent transparent #E8E8E8 transparent;-fx-border-width:0 0 1 0;");

        container.getChildren().addAll(toolbar, ganttScroll);
        return container;
    }

    private Button navBtn(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:#FFFFFF;-fx-text-fill:#595959;"
            +"-fx-border-color:#D9D9D9;-fx-border-width:1;-fx-border-radius:6;"
            +"-fx-background-radius:6;-fx-padding:7 14;-fx-cursor:hand;");
        return b;
    }

    private Node buildGantt(LocalDate start) {
        int DAYS = 7;
        double ROOM_W = 90, DAY_W = 120;
        String[] DOW = {"CN","T2","T3","T4","T5","T6","T7"};

        List<String[]> rooms    = loadRoomsForCalendar();
        List<String[]> bookings = loadBookings(start, start.plusDays(DAYS));

        VBox gantt = new VBox(0);
        gantt.setStyle("-fx-background-color:#FFFFFF;");
        gantt.setPadding(new Insets(0, 28, 28, 28));

        // Header row
        HBox hdr = new HBox(0);
        hdr.setStyle("-fx-background-color:#F8F8F8;"
            +"-fx-border-color:transparent transparent #E0E0E0 transparent;-fx-border-width:0 0 1 0;");
        Label rh = cell("Phòng", ROOM_W, true, false);
        rh.setStyle(rh.getStyle()+"-fx-border-color:transparent #E0E0E0 transparent transparent;"
            +"-fx-border-width:0 1 0 0;");
        hdr.getChildren().add(rh);
        for (int d = 0; d < DAYS; d++) {
            LocalDate date = start.plusDays(d);
            boolean today = date.equals(LocalDate.now());
            String dow = DOW[date.getDayOfWeek().getValue() % 7];
            VBox dc = new VBox(2);
            dc.setPrefWidth(DAY_W); dc.setMinWidth(DAY_W);
            dc.setAlignment(Pos.CENTER);
            dc.setPadding(new Insets(8));
            dc.setStyle("-fx-background-color:"+(today?"#E6F4FF":"transparent")+";"
                +"-fx-border-color:transparent #E0E0E0 transparent transparent;-fx-border-width:0 1 0 0;");
            Label dl = new Label(dow+" "+date.format(DateTimeFormatter.ofPattern("dd/MM")));
            dl.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:"
                +(today?"#1890FF":"#1A1A2E")+";");
            dc.getChildren().add(dl);
            hdr.getChildren().add(dc);
        }
        gantt.getChildren().add(hdr);

        // Room rows
        for (String[] room : rooms) {
            HBox row = new HBox(0);
            row.setStyle("-fx-border-color:transparent transparent #F0F0F0 transparent;-fx-border-width:0 0 1 0;");
            Label rl = cell(room[1], ROOM_W, true, true);
            row.getChildren().add(rl);
            for (int d = 0; d < DAYS; d++) {
                LocalDate date = start.plusDays(d);
                boolean today = date.equals(LocalDate.now());
                StackPane cellPane = new StackPane();
                cellPane.setPrefWidth(DAY_W); cellPane.setMinWidth(DAY_W);
                cellPane.setPrefHeight(44); cellPane.setMinHeight(44);
                cellPane.setStyle("-fx-background-color:"+(today?"#F0F8FF":"transparent")+";"
                    +"-fx-border-color:transparent #E8E8E8 transparent transparent;-fx-border-width:0 1 0 0;");
                String[] bk = findBooking(bookings, room[0], date);
                if (bk != null) {
                    String name = bk[2] != null && !bk[2].isEmpty() ? bk[2] : bk[0];
                    Label bar = new Label("  "+name);
                    bar.setMaxWidth(Double.MAX_VALUE); bar.setMaxHeight(Double.MAX_VALUE);
                    String[] clr = bkColors(bk[5]);
                    bar.setStyle("-fx-background-color:"+clr[0]+";-fx-text-fill:"+clr[1]+";"
                        +"-fx-font-size:11px;-fx-font-weight:bold;"
                        +"-fx-background-radius:4;-fx-padding:4 8;");
                    bar.setTooltip(new Tooltip("Phiếu: "+bk[0]+"\nKhách: "+bk[2]
                        +"\nNhận: "+bk[3]+"\nTrả: "+bk[4]));
                    StackPane.setAlignment(bar, Pos.CENTER_LEFT);
                    StackPane.setMargin(bar, new Insets(4));
                    cellPane.getChildren().add(bar);
                } else {
                    cellPane.setOnMouseEntered(e ->
                        cellPane.setStyle("-fx-background-color:#F5F5F5;-fx-cursor:hand;"
                            +"-fx-border-color:transparent #E8E8E8 transparent transparent;-fx-border-width:0 1 0 0;"));
                    cellPane.setOnMouseExited(e ->
                        cellPane.setStyle("-fx-background-color:"+(today?"#F0F8FF":"transparent")+";"
                            +"-fx-border-color:transparent #E8E8E8 transparent transparent;-fx-border-width:0 1 0 0;"));
                }
                row.getChildren().add(cellPane);
            }
            gantt.getChildren().add(row);
        }
        if (rooms.isEmpty()) {
            Label e = new Label("Không có dữ liệu phòng.");
            e.setStyle("-fx-font-size:13px;-fx-text-fill:#8C8C8C;-fx-padding:30;");
            gantt.getChildren().add(e);
        }
        return gantt;
    }

    private Label cell(String text, double w, boolean bold, boolean border) {
        Label l = new Label(text);
        l.setPrefWidth(w); l.setMinWidth(w);
        l.setStyle("-fx-font-size:12px;-fx-padding:8;"
            +(bold?"-fx-font-weight:bold;":"")
            +(border?"-fx-border-color:transparent #E0E0E0 transparent transparent;-fx-border-width:0 1 0 0;":""));
        return l;
    }

    private List<String[]> loadRoomsForCalendar() {
        List<String[]> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con != null) {
            try (PreparedStatement p = con.prepareStatement(
                    "SELECT maPhong,tenPhong FROM Phong ORDER BY maPhong");
                 ResultSet rs = p.executeQuery()) {
                while (rs.next()) list.add(new String[]{rs.getString(1), rs.getString(2)});
            } catch (Exception ignored) {}
        }
        if (list.isEmpty()) {
            for (String[] d : new String[][]{{"P101","101"},{"P102","102"},{"P103","103"},
                {"P201","201"},{"P202","202"},{"P301","301"}}) list.add(d);
        }
        return list;
    }

    private List<String[]> loadBookings(LocalDate from, LocalDate to) {
        List<String[]> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;
        String sql = "SELECT pdp.maPhieuDatPhong,ct.maPhong,kh.hoTenKH,"
            +"pdp.thoiGianNhanDuKien,pdp.thoiGianTraDuKien,pdp.trangThai "
            +"FROM PhieuDatPhong pdp "
            +"JOIN ChiTietPhieuDatPhong ct ON ct.maPhieuDatPhong=pdp.maPhieuDatPhong "
            +"JOIN KhachHang kh ON kh.maKH=pdp.maKhachHang "
            +"WHERE pdp.trangThai NOT IN ('DaCheckOut','HuyDat') "
            +"AND pdp.thoiGianNhanDuKien<? AND pdp.thoiGianTraDuKien>?";
        try (PreparedStatement p = con.prepareStatement(sql)) {
            p.setTimestamp(1, Timestamp.valueOf(to.atStartOfDay()));
            p.setTimestamp(2, Timestamp.valueOf(from.atStartOfDay()));
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) list.add(new String[]{
                    rs.getString(1), rs.getString(2), rs.getString(3),
                    rs.getTimestamp(4)!=null?rs.getTimestamp(4).toString().substring(0,10):"",
                    rs.getTimestamp(5)!=null?rs.getTimestamp(5).toString().substring(0,10):"",
                    rs.getString(6)});
            }
        } catch (Exception ignored) {}
        return list;
    }

    private String[] findBooking(List<String[]> bks, String maPhong, LocalDate date) {
        for (String[] b : bks) {
            if (!maPhong.equals(b[1])) continue;
            try {
                if (!date.isBefore(LocalDate.parse(b[3])) && date.isBefore(LocalDate.parse(b[4]))) return b;
            } catch (Exception ignored) {}
        }
        return null;
    }

    private String[] bkColors(String tt) {
        if (tt == null) return new String[]{"#ADC6FF","#1D39C4"};
        return switch (tt) {
            case "DaCheckIn" -> new String[]{"#B7EB8F","#237804"};
            case "DaDat"    -> new String[]{"#ADC6FF","#1D39C4"};
            default         -> new String[]{"#D3ADF7","#531DAB"};
        };
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
        // Mã phòng: điền sẵn nếu được navigate từ Quản Lý Phòng
        TextField txtMaPhong = field(prefillMaPhong != null ? prefillMaPhong : "P101");
        // Nếu điền sẵn thì khóa trường mã phòng để tránh sử a nhầm
        if (prefillMaPhong != null && !prefillMaPhong.isEmpty()) {
            txtMaPhong.setEditable(false);
            txtMaPhong.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #1890FF;"
                    + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1.5;"
                    + "-fx-padding: 8 10 8 10; -fx-font-weight: bold; -fx-text-fill: #1890FF;");
        }
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
