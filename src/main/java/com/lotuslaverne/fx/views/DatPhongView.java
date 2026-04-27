package com.lotuslaverne.fx.views;

import com.lotuslaverne.dao.KhachHangDAO;
import com.lotuslaverne.dao.PhieuDatPhongDAO;
import com.lotuslaverne.dao.PhongDAO;
import com.lotuslaverne.entity.KhachHang;
import com.lotuslaverne.entity.PhieuDatPhong;
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
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatPhongView {

    /** Observable list cho bảng lịch sử — được refresh sau mỗi lần đặt thành công */
    private ObservableList<Object[]> historyItems;
    private TableView<Object[]> historyTable;

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

        // Tab 1: Form + Lịch Sử
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:#F0F2F5;-fx-border-color:transparent;");
        formScrollPane = scroll; // lưu để scrollToStep dùng
        VBox fc = new VBox(24);
        fc.setPadding(new Insets(20, 28, 28, 28));
        fc.setStyle("-fx-background-color:#F0F2F5;");
        fc.getChildren().addAll(buildFormCard(), buildHistoryCard());
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

    /** Mã phòng đã chọn sau bước tìm kiếm */
    private String[] selectedMaPhong = {null};
    /** ScrollPane chứa outer (3 bước) — dùng để auto-scroll khi bấm */
    private ScrollPane formScrollPane = null;

    private VBox buildFormCard() {
        VBox outer = new VBox(20);

        // ══════════════════════════════════════════════
        // BƯỚC 1 — Nhập yêu cầu tìm phòng
        // ══════════════════════════════════════════════
        VBox step1 = new VBox(14);
        step1.setPadding(new Insets(22));
        step1.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:10;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        Label t1 = new Label("🔍  Bước 1 — Tìm Phòng Phù Hợp");
        t1.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#1A1A2E;"
                + "-fx-border-color:transparent transparent #F0F2F5 transparent;"
                + "-fx-border-width:0 0 1 0;-fx-padding:0 0 10 0;");

        GridPane g1 = new GridPane(); g1.setHgap(16); g1.setVgap(10);
        for (int i = 0; i < 3; i++) {
            ColumnConstraints c = new ColumnConstraints(); c.setPercentWidth(33.33);
            g1.getColumnConstraints().add(c);
        }

        DatePicker dpNhan = datePicker(LocalDate.now());
        DatePicker dpTra  = datePicker(LocalDate.now().plusDays(1));
        TextField txtSoKhach = field("2");

        ComboBox<String> cbLoai = new ComboBox<>();
        cbLoai.getItems().addAll("Tất cả", "Standard", "Deluxe", "Superior", "Suite");
        cbLoai.setValue("Tất cả"); cbLoai.setMaxWidth(Double.MAX_VALUE); cbLoai.setStyle(comboStyle());

        ComboBox<String> cbHinhThuc = new ComboBox<>();
        cbHinhThuc.getItems().addAll("Trực tiếp", "Qua điện thoại", "Online Booking");
        cbHinhThuc.setValue("Trực tiếp"); cbHinhThuc.setMaxWidth(Double.MAX_VALUE); cbHinhThuc.setStyle(comboStyle());

        // Tiện nghi checkbox row
        CheckBox cbWifi = amenity("WiFi"); CheckBox cbTv = amenity("TV");
        CheckBox cbDh   = amenity("Điều Hòa"); CheckBox cbBt = amenity("Bồn Tắm");
        CheckBox cbBc   = amenity("Ban Công");  CheckBox cbMb = amenity("Mini Bar");
        HBox amenRow = new HBox(10, cbWifi, cbTv, cbDh, cbBt, cbBc, cbMb);
        amenRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        g1.add(lbl("Ngày Nhận *"),      0,0); g1.add(dpNhan,    0,1);
        g1.add(lbl("Ngày Trả *"),       1,0); g1.add(dpTra,     1,1);
        g1.add(lbl("Số Khách *"),       2,0); g1.add(txtSoKhach,2,1);
        g1.add(lbl("Loại Phòng"),       0,2); g1.add(cbLoai,    0,3);
        g1.add(lbl("Hình Thức Đặt"),   1,2); g1.add(cbHinhThuc,1,3);
        g1.add(lbl("Tiện Nghi Yêu Cầu"),0,4);
        GridPane.setColumnSpan(amenRow, 3); g1.add(amenRow, 0, 5);

        Button btnSearch = new Button("🔍  TÌM PHÒNG PHÙ HỢP");
        btnSearch.setStyle("-fx-background-color:#1890FF;-fx-text-fill:white;"
                + "-fx-background-radius:8;-fx-padding:11 28;-fx-font-size:14px;"
                + "-fx-font-weight:bold;-fx-cursor:hand;");
        HBox searchRow = new HBox(btnSearch);
        searchRow.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        step1.getChildren().addAll(t1, g1, searchRow);

        // ══════════════════════════════════════════════
        // BƯỚC 2 — Kết quả phòng trống phù hợp
        // ══════════════════════════════════════════════
        VBox step2 = new VBox(12);
        step2.setPadding(new Insets(22));
        step2.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:10;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");
        
        Label t2hdr = new Label("🏨  Bước 2 — Chọn Phòng");
        t2hdr.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#1A1A2E;"
                + "-fx-border-color:transparent transparent #F0F2F5 transparent;"
                + "-fx-border-width:0 0 1 0;-fx-padding:0 0 10 0;");
        Label t2sub = new Label();
        t2sub.setStyle("-fx-font-size:12px;-fx-text-fill:#8C8C8C;");

        javafx.scene.layout.FlowPane roomGrid = new javafx.scene.layout.FlowPane();
        roomGrid.setHgap(14); roomGrid.setVgap(14);
        roomGrid.setPrefWrapLength(800);

        step2.getChildren().addAll(t2hdr, t2sub, roomGrid);

        // ══════════════════════════════════════════════
        // BƯỚC 3 — Thông tin khách + xác nhận
        // ══════════════════════════════════════════════
        VBox step3 = new VBox(14);
        step3.setPadding(new Insets(22));
        step3.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:10;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");
        
        Label t3 = new Label("👤  Bước 3 — Thông Tin Khách Hàng");
        t3.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#1A1A2E;"
                + "-fx-border-color:transparent transparent #F0F2F5 transparent;"
                + "-fx-border-width:0 0 1 0;-fx-padding:0 0 10 0;");

        // Banner phòng đã chọn
        Label selectedBanner = new Label();
        selectedBanner.setStyle("-fx-background-color:#E6F4FF;-fx-text-fill:#1890FF;"
                + "-fx-background-radius:8;-fx-border-color:#91CAFF;-fx-border-width:1;"
                + "-fx-border-radius:8;-fx-padding:10 16;-fx-font-size:13px;-fx-font-weight:bold;");
        selectedBanner.setMaxWidth(Double.MAX_VALUE);

        GridPane g3 = new GridPane(); g3.setHgap(16); g3.setVgap(10);
        for (int i = 0; i < 2; i++) {
            ColumnConstraints c = new ColumnConstraints(); c.setPercentWidth(50);
            g3.getColumnConstraints().add(c);
        }
        TextField txtSDT   = field(""); txtSDT.setPromptText("VD: 0912345678");
        TextField txtTenKH = field(""); txtTenKH.setPromptText("(tự điền nếu khách cũ)");
        TextField txtGhiChu= field(""); txtGhiChu.setPromptText("Yêu cầu đặc biệt...");

        Button btnTraCuu = new Button("Tra Cứu");
        btnTraCuu.setStyle("-fx-background-color:#1890FF;-fx-text-fill:white;"
                + "-fx-background-radius:6;-fx-padding:7 12;-fx-cursor:hand;");
        HBox sdtRow = new HBox(6, txtSDT, btnTraCuu); HBox.setHgrow(txtSDT, Priority.ALWAYS);
        Label lblKH = new Label("Nhập SĐT rồi bấm Tra Cứu");
        lblKH.setStyle("-fx-font-size:11px;-fx-text-fill:#8C8C8C;");

        g3.add(lbl("SĐT Khách *"), 0,0); g3.add(sdtRow,   0,1);
        g3.add(lbl("Tên Khách"),   1,0); g3.add(txtTenKH, 1,1);
        g3.add(lbl("Ghi Chú"),     0,2); GridPane.setColumnSpan(txtGhiChu,2); g3.add(txtGhiChu,0,3);
        g3.add(lblKH,              0,4); GridPane.setColumnSpan(lblKH,2);

        // ── Mã khuyến mãi ──
        double[] discountPct = {0.0}; // % giảm (0.0–1.0)
        String[] appliedKM   = {null};

        HBox kmRow = new HBox(8);
        kmRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        kmRow.setPadding(new Insets(2, 0, 2, 0));
        Label lblKMIcon = new Label("🏷");
        lblKMIcon.setStyle("-fx-font-size:14px;");
        TextField txtKM = field("");
        txtKM.setPromptText("Nhập mã khuyến mãi (nếu có)...");
        txtKM.setMaxWidth(220);
        Button btnKM = new Button("Áp Dụng");
        btnKM.setStyle("-fx-background-color:#722ED1;-fx-text-fill:white;"
                + "-fx-background-radius:6;-fx-padding:8 14;-fx-cursor:hand;-fx-font-weight:bold;");
        Label lblKMStatus = new Label();
        lblKMStatus.setStyle("-fx-font-size:11px;-fx-text-fill:#8C8C8C;");
        Button btnRemoveKM = new Button("✕");
        btnRemoveKM.setStyle("-fx-background-color:#FFF1F0;-fx-text-fill:#FF4D4F;"
                + "-fx-background-radius:6;-fx-border-color:#FFA39E;-fx-border-width:1;"
                + "-fx-border-radius:6;-fx-padding:7 10;-fx-cursor:hand;");
        btnRemoveKM.setVisible(false); btnRemoveKM.setManaged(false);
        kmRow.getChildren().addAll(lblKMIcon, txtKM, btnKM, lblKMStatus, btnRemoveKM);

        // ── Chi phí ước tính (có dòng giảm giá) ──
        VBox uocTinhBox = new VBox(5);
        uocTinhBox.setPadding(new Insets(12));
        uocTinhBox.setStyle("-fx-background-color:#F0F5FF;-fx-background-radius:8;"
                + "-fx-border-color:#ADC6FF;-fx-border-width:1;-fx-border-radius:8;");
        Label uocTitle = new Label("💰  Chi Phí Ước Tính");
        uocTitle.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1890FF;");
        Label lblGia  = new Label("Đơn giá: —");
        lblGia.setStyle("-fx-font-size:12px;-fx-text-fill:#595959;");
        Label lblDem  = new Label("Số đêm: —");
        lblDem.setStyle("-fx-font-size:12px;-fx-text-fill:#595959;");
        Label lblGoc  = new Label();
        lblGoc.setStyle("-fx-font-size:12px;-fx-text-fill:#595959;");
        lblGoc.setVisible(false); lblGoc.setManaged(false);
        Label lblGiam = new Label();
        lblGiam.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#52C41A;");
        lblGiam.setVisible(false); lblGiam.setManaged(false);
        Label lblTong = new Label("Tổng: —");
        lblTong.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#FF4D4F;");
        uocTinhBox.getChildren().addAll(uocTitle, lblGia, lblDem, lblGoc, lblGiam, lblTong);

        // Hàm tính lại tổng khi áp/xóa KM
        double[] baseTotal = {0};
        Runnable recalcTotal = () -> {
            if (baseTotal[0] == 0) return;
            java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
            if (discountPct[0] > 0) {
                double giam = baseTotal[0] * discountPct[0];
                double sau  = baseTotal[0] - giam;
                lblGoc.setText("Giá gốc: " + df.format(baseTotal[0]) + " đ");
                lblGiam.setText("Giảm " + (int)(discountPct[0]*100) + "%: -" + df.format(giam) + " đ");
                lblTong.setText("Tổng: " + df.format(sau) + " đ");
                lblGoc.setVisible(true);  lblGoc.setManaged(true);
                lblGiam.setVisible(true); lblGiam.setManaged(true);
            } else {
                lblGoc.setVisible(false);  lblGoc.setManaged(false);
                lblGiam.setVisible(false); lblGiam.setManaged(false);
                lblTong.setText("Tổng: " + df.format(baseTotal[0]) + " đ");
            }
        };

        // Áp mã KM
        btnKM.setOnAction(e -> {
            String ma = txtKM.getText().trim().toUpperCase();
            if (ma.isEmpty()) return;
            double[] pct = lookupKhuyenMai(ma);
            if (pct[0] < 0) {
                lblKMStatus.setText("❌ Mã không tồn tại hoặc hết hạn");
                lblKMStatus.setStyle("-fx-font-size:11px;-fx-text-fill:#FF4D4F;");
            } else {
                discountPct[0] = pct[0];
                appliedKM[0]   = ma;
                lblKMStatus.setText("✓ Giảm " + (int)(pct[0]*100) + "% — " + ma);
                lblKMStatus.setStyle("-fx-font-size:11px;-fx-text-fill:#52C41A;-fx-font-weight:bold;");
                btnRemoveKM.setVisible(true); btnRemoveKM.setManaged(true);
                btnKM.setDisable(true); txtKM.setDisable(true);
                recalcTotal.run();
            }
        });
        txtKM.setOnAction(e -> btnKM.fire());

        // Xóa mã KM
        btnRemoveKM.setOnAction(e -> {
            discountPct[0] = 0; appliedKM[0] = null;
            txtKM.clear(); txtKM.setDisable(false);
            btnKM.setDisable(false);
            btnRemoveKM.setVisible(false); btnRemoveKM.setManaged(false);
            lblKMStatus.setText("");
            recalcTotal.run();
        });

        Label errLbl = new Label();
        errLbl.setStyle("-fx-text-fill:#FF4D4F;-fx-font-size:12px;");
        errLbl.setVisible(false); errLbl.setManaged(false);

        Button btnSubmit = new Button("✅  XÁC NHẬN ĐẶT PHÒNG");
        btnSubmit.setStyle("-fx-background-color:#52C41A;-fx-text-fill:white;"
                + "-fx-background-radius:8;-fx-padding:12 32;-fx-font-size:14px;"
                + "-fx-font-weight:bold;-fx-cursor:hand;");
        Button btnBack = new Button("← Chọn Lại Phòng");
        btnBack.setStyle("-fx-background-color:#F5F5F5;-fx-text-fill:#595959;"
                + "-fx-background-radius:8;-fx-border-color:#D9D9D9;-fx-border-width:1;"
                + "-fx-border-radius:8;-fx-padding:12 20;-fx-cursor:hand;");
        HBox btnRow3 = new HBox(12, btnBack, btnSubmit);
        btnRow3.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        step3.getChildren().addAll(t3, selectedBanner, g3, kmRow, uocTinhBox, errLbl, btnRow3);

        // ══════════════════════════════════════════════
        // STATE — cache KH
        // ══════════════════════════════════════════════
        String[] resolvedMaKH = {null};

        // ── Tra cứu khách ──
        btnTraCuu.setOnAction(e -> {
            String sdt = txtSDT.getText().trim();
            if (sdt.isEmpty()) { lblKH.setText("Nhập SĐT trước!"); return; }
            KhachHang kh = lookupKhachBySDT(sdt);
            if (kh != null) {
                resolvedMaKH[0] = kh.getMaKH();
                txtTenKH.setText(kh.getHoTenKH());
                lblKH.setText("✓ Khách: " + kh.getHoTenKH() + " (" + kh.getMaKH() + ")");
                lblKH.setStyle("-fx-font-size:11px;-fx-text-fill:#52C41A;");
            } else {
                resolvedMaKH[0] = null;
                lblKH.setText("⚠ Khách mới — điền tên để tạo");
                lblKH.setStyle("-fx-font-size:11px;-fx-text-fill:#FAAD14;");
            }
        });
        txtSDT.setOnAction(e -> btnTraCuu.fire());

        // ── Quay lại ──
        btnBack.setOnAction(e -> {
                        javafx.application.Platform.runLater(() -> scrollToStep(outer, step2));
            selectedMaPhong[0] = null;
        });

        // ── Tìm phòng ──
        btnSearch.setOnAction(e -> {
            LocalDate nd = dpNhan.getValue(), nt = dpTra.getValue();
            if (nd == null || nt == null || !nt.isAfter(nd)) {
                alert(Alert.AlertType.WARNING, "Lỗi ngày", "Ngày trả phải sau ngày nhận!");
                return;
            }
            int soK;
            try { soK = Integer.parseInt(txtSoKhach.getText().trim()); }
            catch (Exception ex) { soK = 1; }

            List<String[]> rooms = searchAvailableRooms(
                cbLoai.getValue(), soK, nd, nt,
                cbWifi.isSelected(), cbTv.isSelected(), cbDh.isSelected(),
                cbBt.isSelected(), cbBc.isSelected(), cbMb.isSelected());

            t2sub.setText("Tìm thấy " + rooms.size() + " phòng trống phù hợp   •  "
                    + nd.format(DateTimeFormatter.ofPattern("dd/MM")) + " → "
                    + nt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            roomGrid.getChildren().clear();

            if (rooms.isEmpty()) {
                Label empty = new Label("😔  Không có phòng trống phù hợp với yêu cầu.");
                empty.setStyle("-fx-font-size:13px;-fx-text-fill:#8C8C8C;-fx-padding:20;");
                roomGrid.getChildren().add(empty);
            } else {
                for (String[] r : rooms) {
                    // r = {maPhong, tenPhong, loaiPhong, donGia, tienNghi}
                    VBox card2 = new VBox(8);
                    card2.setPadding(new Insets(14));
                    card2.setPrefWidth(210);
                    card2.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:10;"
                            + "-fx-border-color:#E8E8E8;-fx-border-width:1;-fx-border-radius:10;"
                            + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.05),4,0,0,1);"
                            + "-fx-cursor:hand;");

                    Label rName = new Label(r[1]);
                    rName.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#1A1A2E;");
                    Label rLoai = new Label(r[2]);
                    rLoai.setStyle("-fx-font-size:11px;-fx-text-fill:#8C8C8C;");
                    Label rGia  = new Label(r[3] + " đ/đêm");
                    rGia.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#FF4D4F;");
                    Label rAmen = new Label(r[4]);
                    rAmen.setStyle("-fx-font-size:10px;-fx-text-fill:#52C41A;");
                    rAmen.setWrapText(true);

                    Button btnChon = new Button("✔ Chọn Phòng Này");
                    btnChon.setMaxWidth(Double.MAX_VALUE);
                    btnChon.setStyle("-fx-background-color:#1890FF;-fx-text-fill:white;"
                            + "-fx-background-radius:6;-fx-padding:7 0;-fx-cursor:hand;"
                            + "-fx-font-weight:bold;");

                    btnChon.setOnAction(ev -> {
                        selectedMaPhong[0] = r[0];
                        selectedBanner.setText("🏷  Phòng đã chọn: " + r[1] + " (" + r[0] + ")  —  " + r[2] + "  —  " + r[3] + " đ/đêm");
                        tinhUocTinh(r[0], dpNhan.getValue(), dpTra.getValue(), lblGia, lblDem, lblTong);
                        // Cập nhật baseTotal để logic giảm giá dùng được
                        try {
                            String raw = lblTong.getText().replaceAll("[^\\d]", "");
                            baseTotal[0] = raw.isEmpty() ? 0 : Double.parseDouble(raw);
                        } catch (Exception ex) { baseTotal[0] = 0; }
                        discountPct[0] = 0; appliedKM[0] = null; // reset KM khi đổi phòng
                        txtKM.clear(); txtKM.setDisable(false); btnKM.setDisable(false);
                        btnRemoveKM.setVisible(false); btnRemoveKM.setManaged(false);
                        lblKMStatus.setText("");
                                                javafx.application.Platform.runLater(() -> scrollToStep(outer, step3));
                    });

                    card2.getChildren().addAll(rName, rLoai, rGia, rAmen, btnChon);
                    // Hover effect
                    card2.setOnMouseEntered(ev -> card2.setStyle("-fx-background-color:#F0F8FF;"
                            + "-fx-background-radius:10;-fx-border-color:#1890FF;"
                            + "-fx-border-width:1.5;-fx-border-radius:10;"
                            + "-fx-effect:dropshadow(gaussian,rgba(24,144,255,0.15),8,0,0,2);"
                            + "-fx-cursor:hand;"));
                    card2.setOnMouseExited(ev -> card2.setStyle("-fx-background-color:#FFFFFF;"
                            + "-fx-background-radius:10;-fx-border-color:#E8E8E8;"
                            + "-fx-border-width:1;-fx-border-radius:10;"
                            + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.05),4,0,0,1);"
                            + "-fx-cursor:hand;"));
                    roomGrid.getChildren().add(card2);
                }
            }
            javafx.application.Platform.runLater(() -> scrollToStep(outer, step2));
        });

        // ── Submit ──
        btnSubmit.setOnAction(e -> {
            String maPhong = selectedMaPhong[0];
            String sdt     = txtSDT.getText().trim();
            String tenKH   = txtTenKH.getText().trim();
            if (maPhong == null) { showError(errLbl, "Chưa chọn phòng!"); return; }
            if (sdt.isEmpty())   { showError(errLbl, "Vui lòng nhập SĐT khách hàng!"); return; }
            if (dpNhan.getValue() == null || dpTra.getValue() == null
                    || !dpTra.getValue().isAfter(dpNhan.getValue())) {
                showError(errLbl, "Ngày trả phải sau ngày nhận!"); return;
            }
            int soNguoi;
            try { soNguoi = Math.max(1, Integer.parseInt(txtSoKhach.getText().trim())); }
            catch (Exception ex) { soNguoi = 1; }

            String maKH = resolvedMaKH[0];
            if (maKH == null) {
                KhachHang found = lookupKhachBySDT(sdt);
                if (found != null) { maKH = found.getMaKH(); }
                else {
                    if (tenKH.isEmpty()) { showError(errLbl, "Khách mới — điền tên khách!"); return; }
                    KhachHang nk = new KhachHang(null, tenKH, sdt, "");
                    if (!new KhachHangDAO().themKhachHang(nk)) { showError(errLbl, "Không tạo được khách mới!"); return; }
                    maKH = nk.getMaKH();
                }
            }
            errLbl.setVisible(false); errLbl.setManaged(false);
            try {
                Timestamp tNhan = Timestamp.valueOf(dpNhan.getValue().atStartOfDay());
                Timestamp tTra  = Timestamp.valueOf(dpTra.getValue().atStartOfDay());
                String maPDP = "PDP" + UUID.randomUUID().toString().substring(0,5).toUpperCase();
                PhieuDatPhong pdp = new PhieuDatPhong(maPDP, maKH, "NV001",
                        soNguoi, tNhan, tTra, txtGhiChu.getText().trim());
                if (new PhieuDatPhongDAO().lapPhieuDat(pdp)) {
                    new PhongDAO().capNhatTrangThai(maPhong, "PhongDat");
                    alert(Alert.AlertType.INFORMATION, "Thành công",
                            "✅ Đặt phòng thành công!\nMã phiếu: " + maPDP
                            + "\nPhòng: " + maPhong + "\nKhách: " + maKH);
                    // Reset về bước 1
                    selectedMaPhong[0] = null; resolvedMaKH[0] = null;
                    txtSDT.clear(); txtTenKH.clear(); txtGhiChu.clear();
                    txtSoKhach.setText("2"); cbLoai.setValue("Tất cả");
                    dpNhan.setValue(LocalDate.now()); dpTra.setValue(LocalDate.now().plusDays(1));
                                                            if (historyItems != null) historyItems.setAll(loadHistory());
                } else {
                    showError(errLbl, "Lỗi! Kiểm tra DB hoặc phòng đã bị đặt.");
                }
            } catch (Exception ex) { showError(errLbl, "Lỗi kết nối DB: " + ex.getMessage()); }
        });

        outer.getChildren().addAll(step1, step2, step3);
        return outer;
    }

    /**
     * Scroll đến vị trí của targetStep bên trong outer (bên trong formScrollPane).
     * Phải gọi trong Platform.runLater để layout kịp tính toạ độ.
     */
    private void scrollToStep(VBox outer, VBox targetStep) {
        if (formScrollPane == null) return;
        double outerH = outer.getBoundsInLocal().getHeight();
        double viewH  = formScrollPane.getViewportBounds().getHeight();
        if (outerH <= viewH) return; // không cần scroll
        double stepY  = targetStep.getBoundsInParent().getMinY();
        double maxScroll = outerH - viewH;
        double vval = Math.min(1.0, stepY / maxScroll);
        formScrollPane.setVvalue(vval);
    }

    /** Tìm phòng trống theo yêu cầu */
    private List<String[]> searchAvailableRooms(String loai, int soKhach,
            LocalDate nd, LocalDate nt,
            boolean wifi, boolean tv, boolean dh, boolean bt, boolean bc, boolean mb) {
        List<String[]> result = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con != null) {
            StringBuilder sql = new StringBuilder(
                "SELECT p.maPhong, ISNULL(p.tenPhong,p.maPhong), lp.tenLoaiPhong, "
                + "ISNULL(bg.donGia,0), "
                + "ISNULL(p.tienNghi,'') "
                + "FROM Phong p "
                + "LEFT JOIN LoaiPhong lp ON lp.maLoaiPhong=p.maLoaiPhong "
                + "LEFT JOIN BangGia bg ON bg.maLoaiPhong=p.maLoaiPhong "
                + "  AND bg.loaiThue='QuaDem' AND GETDATE() BETWEEN bg.ngayBatDau AND bg.ngayKetThuc "
                + "WHERE p.trangThai='Trong' "
                + "AND p.maPhong NOT IN ("
                + "  SELECT ct.maPhong FROM ChiTietPhieuDatPhong ct "
                + "  JOIN PhieuDatPhong pdp ON pdp.maPhieuDatPhong=ct.maPhieuDatPhong "
                + "  WHERE pdp.trangThai NOT IN ('DaCheckOut','HuyDat') "
                + "  AND pdp.thoiGianNhanDuKien<? AND pdp.thoiGianTraDuKien>?) ");
            if (loai != null && !loai.equals("Tất cả"))
                sql.append("AND lp.tenLoaiPhong=? ");
            sql.append("ORDER BY bg.donGia ASC");
            try (PreparedStatement pst = con.prepareStatement(sql.toString())) {
                pst.setTimestamp(1, Timestamp.valueOf(nt.atStartOfDay()));
                pst.setTimestamp(2, Timestamp.valueOf(nd.atStartOfDay()));
                if (loai != null && !loai.equals("Tất cả")) pst.setString(3, loai);
                try (ResultSet rs = pst.executeQuery()) {
                    DecimalFormat df = new DecimalFormat("#,###");
                    while (rs.next()) {
                        double gia = rs.getDouble(4);
                        result.add(new String[]{
                            rs.getString(1), rs.getString(2), rs.getString(3),
                            df.format(gia), rs.getString(5) != null ? rs.getString(5) : ""
                        });
                    }
                }
            } catch (Exception ignored) {}
        }
        // Demo offline
        if (result.isEmpty()) {
            result.add(new String[]{"P101","Phòng 101","Standard","500,000","WiFi, TV"});
            result.add(new String[]{"P201","Phòng 201","Deluxe","750,000","WiFi, TV, Bồn Tắm"});
            result.add(new String[]{"P301","Suite Hoàng Gia","Suite","1,500,000","WiFi, TV, Bồn Tắm, Ban Công"});
        }
        return result;
    }

    private CheckBox amenity(String label) {
        CheckBox cb = new CheckBox(label);
        cb.setStyle("-fx-font-size:12px;");
        return cb;
    }



    /** Tra cứu mã khuyến mãi. Return [pct] >= 0 nếu hợp lệ, [-1] nếu không tìm thấy/hết hạn. */
    private double[] lookupKhuyenMai(String ma) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con != null) {
            String sql = "SELECT phanTramGiam FROM KhuyenMai "
                    + "WHERE maKhuyenMai=? AND GETDATE() BETWEEN ngayBatDau AND ngayKetThuc "
                    + "AND trangThai='HoatDong'";
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, ma);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) return new double[]{rs.getDouble(1) / 100.0};
            } catch (Exception ignored) {}
        }
        // Demo offline: các mã thử
        return switch (ma) {
            case "SUMMER10" -> new double[]{0.10};
            case "LOTUS20"  -> new double[]{0.20};
            case "VIP30"    -> new double[]{0.30};
            default         -> new double[]{-1};
        };
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

    // ── Bảng lịch sử đặt phòng ──────────────────────────────────────────────
    private Node buildHistoryCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        // ── Header card ──
        HBox cardHeader = new HBox(12);
        cardHeader.setAlignment(Pos.CENTER_LEFT);
        cardHeader.setPadding(new Insets(0, 0, 12, 0));
        cardHeader.setStyle("-fx-border-color: transparent transparent #F0F2F5 transparent;"
                + "-fx-border-width: 0 0 1 0;");
        Label cardTitle = new Label("📜  Lịch Sử Đặt Phòng");
        cardTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        // Search
        TextField histSearch = new TextField();
        histSearch.setPromptText("🔍 Tìm mã phiếu, tên khách, phòng...");
        histSearch.setPrefWidth(240);
        histSearch.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #E8E8E8;"
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 1;"
                + "-fx-padding: 6 10; -fx-font-size: 12px;");

        // Filter trạng thái
        ComboBox<String> cbFilter = new ComboBox<>();
        cbFilter.getItems().addAll("Tất cả", "DaDat", "DaCheckIn", "DaCheckOut", "HuyDat");
        cbFilter.setValue("Tất cả");
        cbFilter.setStyle("-fx-background-color:#FFFFFF;-fx-border-color:#D9D9D9;"
                + "-fx-border-radius:6;-fx-background-radius:6;-fx-border-width:1;"
                + "-fx-font-size:12px;");

        Button btnRefresh = new Button("↻");
        btnRefresh.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #595959;"
                + "-fx-background-radius: 6; -fx-border-color: #D9D9D9; -fx-border-width: 1;"
                + "-fx-border-radius: 6; -fx-padding: 6 10; -fx-cursor: hand; -fx-font-size: 13px;");

        cardHeader.getChildren().addAll(cardTitle, sp, histSearch, cbFilter, btnRefresh);

        // ── Table ──
        historyItems = FXCollections.observableArrayList(loadHistory());
        historyTable = buildHistoryTable();
        historyTable.setPrefHeight(280);
        historyTable.setMinHeight(200);

        // Sự kiện filter
        Runnable applyFilter = () -> {
            String kw  = histSearch.getText() == null ? "" : histSearch.getText().toLowerCase().trim();
            String tts = cbFilter.getValue();
            ObservableList<Object[]> filtered = FXCollections.observableArrayList();
            for (Object[] r : historyItems) {
                boolean matchTt = "Tất cả".equals(tts) || tts.equals(r[5] != null ? r[5].toString() : "");
                boolean matchKw = kw.isEmpty();
                if (!matchKw) {
                    for (Object c : r) {
                        if (c != null && c.toString().toLowerCase().contains(kw)) { matchKw = true; break; }
                    }
                }
                if (matchTt && matchKw) filtered.add(r);
            }
            historyTable.setItems(filtered);
        };
        histSearch.textProperty().addListener((obs, o, n) -> applyFilter.run());
        cbFilter.valueProperty().addListener((obs, o, n) -> applyFilter.run());
        btnRefresh.setOnAction(e -> {
            historyItems.setAll(loadHistory());
            historyTable.setItems(historyItems);
            com.lotuslaverne.fx.UiUtils.flashButton(btnRefresh, "✓");
        });

        card.getChildren().addAll(cardHeader, historyTable);
        return card;
    }

    private TableView<Object[]> buildHistoryTable() {
        TableView<Object[]> tbl = new TableView<>();
        tbl.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tbl.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 8;"
                + "-fx-border-color: #F0F2F5; -fx-border-width: 1; -fx-border-radius: 8;");

        // Cột Mã Phiếu
        TableColumn<Object[], String> colMa = new TableColumn<>("Mã Phiếu");
        colMa.setCellValueFactory(p -> new SimpleStringProperty(p.getValue()[0] != null ? p.getValue()[0].toString() : ""));
        colMa.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? null : s);
                setStyle(empty ? "" : "-fx-font-weight: bold; -fx-text-fill: #1890FF;");
            }
        });
        colMa.setPrefWidth(100);

        // Các cột thông thường
        String[] heads = {"Tên Khách", "Phòng", "Ngày Nhận", "Ngày Trả", "Trạng Thái", "Ghi Chú"};
        int[] idxs    = {1, 2, 3, 4, 5, 6};
        List<TableColumn<Object[], String>> cols = new ArrayList<>();
        cols.add(colMa);
        for (int i = 0; i < heads.length; i++) {
            final int idx = idxs[i];
            TableColumn<Object[], String> col = new TableColumn<>(heads[i]);
            col.setCellValueFactory(p -> {
                Object v = idx < p.getValue().length ? p.getValue()[idx] : "";
                return new SimpleStringProperty(v != null ? v.toString() : "—");
            });
            if (heads[i].equals("Trạng Thái")) {
                col.setCellFactory(tc -> new TableCell<>() {
                    @Override protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) { setGraphic(null); setText(null); return; }
                        Label b = new Label(trangThaiDisplay(item));
                        String[] clr = ttColors(item);
                        b.setStyle("-fx-background-color:" + clr[0] + ";-fx-text-fill:" + clr[1]
                                + ";-fx-padding:2 8;-fx-background-radius:10;"
                                + "-fx-font-size:11px;-fx-font-weight:bold;");
                        setGraphic(b); setText(null);
                    }
                });
            }
            cols.add(col);
        }
        tbl.getColumns().addAll(cols);
        tbl.setItems(historyItems);
        tbl.setPlaceholder(new Label("Chưa có lịch sử đặt phòng."));
        return tbl;
    }

    private List<Object[]> loadHistory() {
        List<Object[]> result = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con != null) {
            String sql = "SELECT TOP 100 pdp.maPhieuDatPhong, kh.hoTenKH, "
                    + "ct.maPhong, "
                    + "CONVERT(varchar,pdp.thoiGianNhanDuKien,103) AS ngayNhan, "
                    + "CONVERT(varchar,pdp.thoiGianTraDuKien,103) AS ngayTra, "
                    + "pdp.trangThai, pdp.ghiChu "
                    + "FROM PhieuDatPhong pdp "
                    + "LEFT JOIN KhachHang kh ON kh.maKH = pdp.maKhachHang "
                    + "LEFT JOIN ChiTietPhieuDatPhong ct ON ct.maPhieuDatPhong = pdp.maPhieuDatPhong "
                    + "ORDER BY pdp.thoiGianNhanDuKien DESC";
            try (PreparedStatement pst = con.prepareStatement(sql);
                 ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    result.add(new Object[]{
                        rs.getString(1),
                        rs.getString(2) != null ? rs.getString(2) : "—",
                        rs.getString(3) != null ? rs.getString(3) : "—",
                        rs.getString(4) != null ? rs.getString(4) : "—",
                        rs.getString(5) != null ? rs.getString(5) : "—",
                        rs.getString(6) != null ? rs.getString(6) : "—",
                        rs.getString(7) != null ? rs.getString(7) : ""
                    });
                }
            } catch (Exception ignored) {}
        }
        // Demo khi DB offline
        if (result.isEmpty()) {
            result.add(new Object[]{"PDP001", "Nguyễn Văn A", "P101", "24/04/2026", "27/04/2026", "DaCheckIn", ""});
            result.add(new Object[]{"PDP002", "Trần Thị B",   "P102", "25/04/2026", "28/04/2026", "DaDat",     "Phòng view biển"});
            result.add(new Object[]{"PDP003", "Lê Văn C",     "P201", "20/04/2026", "22/04/2026", "DaCheckOut", ""});
            result.add(new Object[]{"PDP004", "Phạm Thị D",   "P301", "10/04/2026", "12/04/2026", "HuyDat",    "Khách hủy"});
        }
        return result;
    }

    private String trangThaiDisplay(String tt) {
        if (tt == null) return "—";
        return switch (tt) {
            case "DaDat"      -> "Đã Đặt";
            case "DaCheckIn"  -> "Đang Ở";
            case "DaCheckOut" -> "Đã Trả Phòng";
            case "HuyDat"     -> "Đã Hủy";
            default -> tt;
        };
    }

    private String[] ttColors(String tt) {
        if (tt == null) return new String[]{"#F5F5F5", "#595959"};
        return switch (tt) {
            case "DaDat"      -> new String[]{"#E6F4FF", "#1890FF"};
            case "DaCheckIn"  -> new String[]{"#F6FFED", "#52C41A"};
            case "DaCheckOut" -> new String[]{"#F5F5F5", "#8C8C8C"};
            case "HuyDat"     -> new String[]{"#FFF1F0", "#FF4D4F"};
            default           -> new String[]{"#FFFBE6", "#FAAD14"};
        };
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
