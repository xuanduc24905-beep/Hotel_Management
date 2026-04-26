package com.lotuslaverne.fx.views;

import com.lotuslaverne.util.ConnectDB;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * YeuCauKhachView – Quan ly yeu cau tu phong cua khach.
 * Khach co the gui yeu cau (them khan, thuc an, sua chua...).
 * Nhan vien xu ly va cap nhat trang thai.
 */
public class YeuCauKhachView {

    private ObservableList<String[]> items = FXCollections.observableArrayList();
    private TableView<String[]> table;

    private static final String[] LOAI_YEU_CAU = {
        "Them khan tam", "Them goi nam", "Dong phong", "Sua chua thiet bi",
        "Thuc an & do uong", "Don dep phong", "Ho tro check-out", "Khac"
    };
    private static final String[] MUC_DO = {"Binh thuong", "Khan cap", "VIP"};

    public Node build() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #F0F2F5;");

        VBox header = new VBox(4);
        header.setPadding(new Insets(24, 28, 0, 28));
        Label title = new Label("Yeu Cau Khach Hang");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Label sub = new Label("Quan ly va xu ly yeu cau tu cac phong khach");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #8C8C8C;");
        header.getChildren().addAll(title, sub);

        HBox statBar = buildStatBar();
        statBar.setPadding(new Insets(14, 28, 0, 28));

        HBox mainRow = new HBox(16);
        mainRow.setPadding(new Insets(16, 28, 28, 28));
        VBox.setVgrow(mainRow, Priority.ALWAYS);

        VBox formCard = buildFormCard();
        VBox tableCard = buildTableCard();
        HBox.setHgrow(tableCard, Priority.ALWAYS);
        mainRow.getChildren().addAll(formCard, tableCard);

        root.getChildren().addAll(header, statBar, mainRow);
        VBox.setVgrow(root, Priority.ALWAYS);

        loadData();
        return root;
    }

    // ── STAT BAR
    private HBox buildStatBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);

        int[] counts = countByStatus();
        bar.getChildren().addAll(
            statCard("Cho xu ly",  counts[0], "#FAAD14", "#FFFBE6"),
            statCard("Dang xu ly", counts[1], "#1890FF", "#E6F4FF"),
            statCard("Hoan thanh", counts[2], "#52C41A", "#F6FFED")
        );
        return bar;
    }

    private HBox statCard(String title, int count, String fg, String bg) {
        Label num = new Label(String.valueOf(count));
        num.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + fg + ";");
        Label lbl = new Label(title);
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + fg + "; -fx-font-weight: bold;");
        VBox inner = new VBox(2, num, lbl);
        HBox card = new HBox(inner);
        card.setPadding(new Insets(12, 18, 12, 18));
        card.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 10;"
            + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.05),4,0,0,1);");
        return card;
    }

    // ── FORM TAO YEU CAU
    private VBox buildFormCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(20));
        card.setPrefWidth(280);
        card.setMinWidth(280);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
            + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        Label cardTitle = new Label("Tao Yeu Cau Moi");
        cardTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;"
            + "-fx-border-color: transparent transparent #F0F2F5 transparent;"
            + "-fx-border-width: 0 0 1 0; -fx-padding: 0 0 10 0;");

        TextField txtPhong = field("So phong (VD: 101)");
        TextField txtKhach = field("Ten khach");

        ComboBox<String> cbLoai = new ComboBox<>(
            FXCollections.observableArrayList(LOAI_YEU_CAU));
        cbLoai.setPromptText("Loai yeu cau...");
        cbLoai.setMaxWidth(Double.MAX_VALUE);
        cbLoai.setStyle(comboStyle());

        ComboBox<String> cbMucDo = new ComboBox<>(
            FXCollections.observableArrayList(MUC_DO));
        cbMucDo.setValue("Binh thuong");
        cbMucDo.setMaxWidth(Double.MAX_VALUE);
        cbMucDo.setStyle(comboStyle());

        TextArea txtGhiChu = new TextArea();
        txtGhiChu.setPromptText("Mo ta chi tiet yeu cau...");
        txtGhiChu.setPrefRowCount(3);
        txtGhiChu.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D9D9D9;"
            + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1;");

        Label errLbl = new Label();
        errLbl.setStyle("-fx-text-fill: #FF4D4F; -fx-font-size: 11px;");
        errLbl.setVisible(false);

        Button btnTao = new Button("Tao Yeu Cau");
        btnTao.setMaxWidth(Double.MAX_VALUE);
        btnTao.setStyle("-fx-background-color: #1890FF; -fx-text-fill: white;"
            + "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-size: 13px;"
            + "-fx-font-weight: bold; -fx-cursor: hand;");
        btnTao.setOnAction(e -> {
            String phong = txtPhong.getText().trim();
            String loai  = cbLoai.getValue();
            if (phong.isEmpty() || loai == null) {
                errLbl.setText("Vui long dien so phong va loai yeu cau!");
                errLbl.setVisible(true); return;
            }
            errLbl.setVisible(false);
            saveYeuCau(phong, txtKhach.getText().trim(), loai,
                cbMucDo.getValue(), txtGhiChu.getText().trim());
            txtPhong.clear(); txtKhach.clear();
            cbLoai.setValue(null); cbMucDo.setValue("Binh thuong");
            txtGhiChu.clear();
            loadData();
        });

        card.getChildren().addAll(
            cardTitle,
            lbl("So Phong"), txtPhong,
            lbl("Ten Khach"), txtKhach,
            lbl("Loai Yeu Cau"), cbLoai,
            lbl("Muc Do"), cbMucDo,
            lbl("Ghi Chu"), txtGhiChu,
            errLbl, btnTao
        );
        return card;
    }

    // ── TABLE DANH SACH YEU CAU
    @SuppressWarnings("unchecked")
    private VBox buildTableCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
            + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");
        VBox.setVgrow(card, Priority.ALWAYS);

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label cardTitle = new Label("Danh Sach Yeu Cau");
        cardTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button btnRefresh = new Button("Lam Moi");
        btnRefresh.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #D9D9D9;"
            + "-fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6;"
            + "-fx-padding: 5 12; -fx-cursor: hand; -fx-font-size: 12px;");
        btnRefresh.setOnAction(e -> loadData());
        titleRow.getChildren().addAll(cardTitle, sp, btnRefresh);

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-color: #F9F9F9; -fx-border-color: #E8E8E8;"
            + "-fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setPlaceholder(new Label("Chua co yeu cau nao."));

        // [maYC, maPhong, tenKhach, loaiYeuCau, mucDo, trangThai, thoiGian, ghiChu]
        String[] heads = {"Phong","Khach","Loai Yeu Cau","Muc Do","Trang Thai","Thoi Gian"};
        int[] idxs = {1, 2, 3, 4, 5, 6};
        for (int i = 0; i < heads.length; i++) {
            final int idx = idxs[i];
            TableColumn<String[], String> col = new TableColumn<>(heads[i]);
            col.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(
                idx < p.getValue().length && p.getValue()[idx] != null ? p.getValue()[idx] : ""));
            if (heads[i].equals("Trang Thai")) {
                col.setCellFactory(tc -> new TableCell<>() {
                    @Override protected void updateItem(String s, boolean empty) {
                        super.updateItem(s, empty);
                        if (empty || s == null) { setGraphic(null); return; }
                        String[] colors = statusColors(s);
                        Label badge = new Label(s);
                        badge.setStyle("-fx-background-color:" + colors[0]
                            + ";-fx-text-fill:" + colors[1]
                            + ";-fx-padding:3 10;-fx-background-radius:10;"
                            + "-fx-font-size:11px;-fx-font-weight:bold;");
                        setGraphic(badge); setText(null);
                    }
                });
            }
            table.getColumns().add(col);
        }

        // Cot xu ly
        TableColumn<String[], Void> colXuLy = new TableColumn<>("Xu Ly");
        colXuLy.setPrefWidth(130);
        colXuLy.setCellFactory(tc -> new TableCell<>() {
            final Button btnNhan = new Button("Nhan xu ly");
            final Button btnXong = new Button("Hoan thanh");
            {
                btnNhan.setStyle("-fx-background-color:#1890FF;-fx-text-fill:white;"
                    +"-fx-background-radius:6;-fx-padding:4 8;-fx-cursor:hand;-fx-font-size:11px;");
                btnXong.setStyle("-fx-background-color:#52C41A;-fx-text-fill:white;"
                    +"-fx-background-radius:6;-fx-padding:4 8;-fx-cursor:hand;-fx-font-size:11px;");
                btnNhan.setOnAction(e -> {
                    String[] row = getTableRow().getItem();
                    if (row != null) { updateStatus(row[0], "Dang xu ly"); loadData(); }
                });
                btnXong.setOnAction(e -> {
                    String[] row = getTableRow().getItem();
                    if (row != null) { updateStatus(row[0], "Hoan thanh"); loadData(); }
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); return; }
                String status = getTableRow().getItem()[5];
                HBox box;
                if ("Cho xu ly".equals(status))   box = new HBox(4, btnNhan);
                else if ("Dang xu ly".equals(status)) box = new HBox(4, btnXong);
                else box = new HBox();
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box); setText(null);
            }
        });
        table.getColumns().add(colXuLy);
        table.setItems(items);

        card.getChildren().addAll(titleRow, table);
        return card;
    }

    // ── DATA
    private void loadData() {
        items.clear();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con != null) {
            try {
                // Tao bang neu chua co
                con.prepareStatement(
                    "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='YeuCauKhach') " +
                    "CREATE TABLE YeuCauKhach (" +
                    "maYeuCau NVARCHAR(20) PRIMARY KEY," +
                    "maPhong NVARCHAR(20)," +
                    "tenKhach NVARCHAR(100)," +
                    "loaiYeuCau NVARCHAR(100)," +
                    "mucDo NVARCHAR(50)," +
                    "trangThai NVARCHAR(50) DEFAULT 'Cho xu ly'," +
                    "thoiGian DATETIME DEFAULT GETDATE()," +
                    "ghiChu NVARCHAR(500))").execute();

                try (PreparedStatement pst = con.prepareStatement(
                    "SELECT maYeuCau,maPhong,tenKhach,loaiYeuCau,mucDo,trangThai," +
                    "CONVERT(NVARCHAR(16),thoiGian,120) as thoiGian,ghiChu " +
                    "FROM YeuCauKhach ORDER BY thoiGian DESC");
                     ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        items.add(new String[]{
                            rs.getString(1), rs.getString(2), rs.getString(3),
                            rs.getString(4), rs.getString(5), rs.getString(6),
                            rs.getString(7), rs.getString(8)});
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
        if (items.isEmpty()) addDemoData();
    }

    private void addDemoData() {
        items.addAll(
            new String[]{"YC001","101","Nguyen Van A","Them khan tam","Binh thuong","Cho xu ly","2026-04-27 08:30",""},
            new String[]{"YC002","203","Tran Thi B","Sua chua thiet bi","Khan cap","Dang xu ly","2026-04-27 09:00","Dieu hoa hong"},
            new String[]{"YC003","305","Le Hoang C","Don dep phong","VIP","Hoan thanh","2026-04-27 07:45",""}
        );
    }

    private void saveYeuCau(String phong, String khach, String loai, String mucDo, String ghiChu) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) { items.add(new String[]{
            "YC" + System.currentTimeMillis(), phong, khach, loai, mucDo,
            "Cho xu ly", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), ghiChu});
            return; }
        String maYC = "YC" + System.currentTimeMillis();
        try (PreparedStatement pst = con.prepareStatement(
            "INSERT INTO YeuCauKhach(maYeuCau,maPhong,tenKhach,loaiYeuCau,mucDo,ghiChu) VALUES(?,?,?,?,?,?)")) {
            pst.setString(1, maYC); pst.setString(2, phong); pst.setString(3, khach);
            pst.setString(4, loai); pst.setString(5, mucDo); pst.setString(6, ghiChu);
            pst.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void updateStatus(String maYC, String status) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return;
        try (PreparedStatement pst = con.prepareStatement(
            "UPDATE YeuCauKhach SET trangThai=? WHERE maYeuCau=?")) {
            pst.setString(1, status); pst.setString(2, maYC);
            pst.executeUpdate();
        } catch (Exception ignored) {}
    }

    private int[] countByStatus() {
        int cho = 0, dang = 0, xong = 0;
        for (String[] r : items) {
            if ("Cho xu ly".equals(r[5])) cho++;
            else if ("Dang xu ly".equals(r[5])) dang++;
            else xong++;
        }
        return new int[]{cho, dang, xong};
    }

    private String[] statusColors(String s) {
        return switch (s) {
            case "Cho xu ly"   -> new String[]{"#FFFBE6", "#FAAD14"};
            case "Dang xu ly"  -> new String[]{"#E6F4FF", "#1890FF"};
            case "Hoan thanh"  -> new String[]{"#F6FFED", "#52C41A"};
            default -> new String[]{"#F5F5F5", "#8C8C8C"};
        };
    }

    private TextField field(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setMaxWidth(Double.MAX_VALUE);
        tf.setStyle("-fx-background-color:#FFFFFF;-fx-border-color:#D9D9D9;"
            + "-fx-border-radius:6;-fx-background-radius:6;-fx-border-width:1;-fx-padding:7 10;");
        return tf;
    }
    private Label lbl(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:#595959;");
        return l;
    }
    private String comboStyle() {
        return "-fx-background-color:#FFFFFF;-fx-border-color:#D9D9D9;"
            + "-fx-border-radius:6;-fx-background-radius:6;-fx-border-width:1;";
    }
}
