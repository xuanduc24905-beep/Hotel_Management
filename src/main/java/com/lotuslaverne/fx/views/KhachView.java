package com.lotuslaverne.fx.views;

import com.lotuslaverne.dao.KhachHangDAO;
import com.lotuslaverne.entity.KhachHang;
import com.lotuslaverne.fx.UiUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

public class KhachView {

    private static final Object[][] DEMO_KHACH = {
        {"KH001", "Nguyễn Văn An",   "101", "0901234567", "012345678901", "22/04/2026", "26/04/2026", "Đang Lưu Trú"},
        {"KH002", "Trần Thị Bình",   "205", "0912345678", "098765432100", "20/04/2026", "25/04/2026", "Đang Lưu Trú"},
        {"KH003", "Lê Hoàng Cường",  "312", "0923456789", "011223344556", "23/04/2026", "27/04/2026", "Đang Lưu Trú"},
        {"KH004", "Phạm Thị Dung",   "408", "0934567890", "034567890123", "21/04/2026", "28/04/2026", "Đang Lưu Trú"},
        {"KH005", "Hoàng Minh Đức",  "103", "0945678901", "045678901234", "19/04/2026", "24/04/2026", "Đã Trả Phòng"},
        {"KH006", "Vũ Thị Lan",      "",    "0956789012", "056789012345", "25/04/2026", "30/04/2026", "Đặt Trước"},
        {"KH007", "Đặng Quốc Hùng",  "202", "0967890123", "067890123456", "22/04/2026", "26/04/2026", "Đang Lưu Trú"},
        {"KH008", "Bùi Thị Mai",     "304", "0978901234", "078901234567", "18/04/2026", "23/04/2026", "Đã Trả Phòng"},
    };

    public Node build() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #F0F2F5;");

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #F0F2F5; -fx-border-color: transparent;");

        VBox content = new VBox(20);
        content.setPadding(new Insets(28, 28, 28, 28));
        content.setStyle("-fx-background-color: #F0F2F5;");

        // Page header
        VBox header = new VBox(4);
        Label title = new Label("Quản Lý Khách");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        long dangLuuTru = countByStatus("Đang Lưu Trú");
        long datTruoc   = countByStatus("Đặt Trước");
        Label sub = new Label(dangLuuTru + " khách đang lưu trú • " + datTruoc + " đặt trước");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #8C8C8C;");
        header.getChildren().addAll(title, sub);

        // Add guest form card
        Node formCard = buildFormCard();

        // Search bar
        HBox searchBar = buildSearchBar();

        // Table
        TableView<Object[]> table = buildTable();
        table.setPrefHeight(400);

        content.getChildren().addAll(header, formCard, searchBar, table);
        scroll.setContent(content);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        root.getChildren().add(scroll);
        return root;
    }

    private Node buildFormCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 1);");

        Label cardTitle = new Label("+ Thêm Khách Mới");
        cardTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;"
                + "-fx-padding: 0 0 10 0; -fx-border-color: transparent transparent #F0F2F5 transparent;"
                + "-fx-border-width: 0 0 1 0;");

        GridPane form = new GridPane();
        form.setHgap(16);
        form.setVgap(10);
        for (int i = 0; i < 4; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(25);
            form.getColumnConstraints().add(cc);
        }

        String[] labels = {"Họ Và Tên", "Phòng", "SĐT", "CMND/CCCD",
                           "Ngày Nhận", "Ngày Trả", "Email", "Trạng Thái"};
        for (int i = 0; i < labels.length; i++) {
            int col = i % 4;
            int row = (i / 4) * 2;
            Label lbl = new Label(labels[i]);
            lbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #595959;");
            form.add(lbl, col, row);

            Node input;
            if (labels[i].equals("Trạng Thái")) {
                ComboBox<String> cb = new ComboBox<>();
                cb.getItems().addAll("Đang Lưu Trú", "Đặt Trước", "Đã Trả Phòng");
                cb.setValue("Đang Lưu Trú");
                cb.setMaxWidth(Double.MAX_VALUE);
                cb.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D9D9D9;"
                        + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1;");
                input = cb;
            } else {
                TextField tf = new TextField();
                tf.setMaxWidth(Double.MAX_VALUE);
                tf.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D9D9D9;"
                        + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1;"
                        + "-fx-padding: 7 10 7 10;");
                input = tf;
            }
            form.add(input, col, row + 1);
        }

        Button addBtn = new Button("Thêm Khách");
        addBtn.setStyle("-fx-background-color: #1890FF; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-padding: 8 20; -fx-font-weight: bold; -fx-cursor: hand;");

        card.getChildren().addAll(cardTitle, form, addBtn);
        return card;
    }

    private HBox buildSearchBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12, 16, 12, 16));
        bar.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 1);");

        TextField search = new TextField();
        search.setPromptText("🔍  Tìm theo tên, phòng, CMND...");
        search.setPrefWidth(300);
        search.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #E8E8E8;"
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 1;"
                + "-fx-padding: 8 12 8 12; -fx-font-size: 13px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label countLbl = new Label(DEMO_KHACH.length + " kết quả");
        countLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #8C8C8C;");

        Button addBtn = new Button("+ Thêm Mới");
        addBtn.setStyle("-fx-background-color: #1890FF; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-padding: 8 16; -fx-font-weight: bold; -fx-cursor: hand;");

        bar.getChildren().addAll(search, spacer, countLbl, addBtn);
        return bar;
    }

    @SuppressWarnings("unchecked")
    private TableView<Object[]> buildTable() {
        TableView<Object[]> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 1);");

        // STT
        TableColumn<Object[], String> colStt = new TableColumn<>("STT");
        colStt.setCellValueFactory(p -> new SimpleStringProperty(
                String.valueOf(table.getItems().indexOf(p.getValue()) + 1)));
        colStt.setPrefWidth(45);

        // Họ Tên with avatar
        TableColumn<Object[], String> colTen = new TableColumn<>("Họ Tên");
        colTen.setCellValueFactory(p -> new SimpleStringProperty((String) p.getValue()[1]));
        colTen.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                HBox box = new HBox(8);
                box.setAlignment(Pos.CENTER_LEFT);
                Node avatar = UiUtils.makeAvatarCircle(item, 14);
                Label lbl = new Label(item);
                lbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
                box.getChildren().addAll(avatar, lbl);
                setGraphic(box); setText(null);
            }
        });

        // Phòng badge
        TableColumn<Object[], String> colPhong = new TableColumn<>("Phòng");
        colPhong.setCellValueFactory(p -> new SimpleStringProperty((String) p.getValue()[2]));
        colPhong.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isEmpty()) { setGraphic(null); setText(null); return; }
                Label badge = new Label(item);
                badge.setStyle("-fx-background-color: #E6F4FF; -fx-text-fill: #1890FF;"
                        + "-fx-padding: 2 8 2 8; -fx-background-radius: 10;"
                        + "-fx-font-size: 11px; -fx-font-weight: bold;");
                setGraphic(badge); setText(null);
            }
        });
        colPhong.setPrefWidth(60);

        TableColumn<Object[], String> colSDT    = simpleCol("Điện Thoại", 3);
        TableColumn<Object[], String> colCMND   = simpleCol("CMND/CCCD", 4);
        TableColumn<Object[], String> colNhan   = simpleCol("Nhận Phòng", 5);
        TableColumn<Object[], String> colTra    = simpleCol("Trả Phòng",  6);

        // Trạng thái badge
        TableColumn<Object[], String> colTT = new TableColumn<>("Trạng Thái");
        colTT.setCellValueFactory(p -> new SimpleStringProperty((String) p.getValue()[7]));
        colTT.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(item);
                String bg, fg;
                switch (item) {
                    case "Đang Lưu Trú" -> { bg = "#FFF1F0"; fg = "#FF4D4F"; }
                    case "Đặt Trước"    -> { bg = "#FFFBE6"; fg = "#FAAD14"; }
                    default             -> { bg = "#F6FFED"; fg = "#52C41A"; }
                }
                badge.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";"
                        + "-fx-padding: 2 8 2 8; -fx-background-radius: 10;"
                        + "-fx-font-size: 11px; -fx-font-weight: bold;");
                setGraphic(badge); setText(null);
            }
        });

        // Thao tác
        TableColumn<Object[], String> colAction = new TableColumn<>("Thao Tác");
        colAction.setCellFactory(tc -> new TableCell<>() {
            private final Button editBtn = new Button("Sửa");
            private final Button delBtn  = new Button("Xóa");
            {
                editBtn.setStyle("-fx-background-color: #E6F4FF; -fx-text-fill: #1890FF;"
                        + "-fx-background-radius: 6; -fx-border-radius: 6;"
                        + "-fx-font-size: 11px; -fx-padding: 3 8; -fx-cursor: hand;");
                delBtn.setStyle("-fx-background-color: #FFF1F0; -fx-text-fill: #FF4D4F;"
                        + "-fx-background-radius: 6; -fx-border-radius: 6;"
                        + "-fx-font-size: 11px; -fx-padding: 3 8; -fx-cursor: hand;");
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                HBox box = new HBox(6);
                box.setAlignment(Pos.CENTER_LEFT);
                box.getChildren().addAll(editBtn, delBtn);
                setGraphic(box);
            }
        });
        colAction.setPrefWidth(100);

        table.getColumns().addAll(colStt, colTen, colPhong, colSDT, colCMND, colNhan, colTra, colTT, colAction);
        table.setItems(FXCollections.observableArrayList(loadKhach()));
        return table;
    }

    private TableColumn<Object[], String> simpleCol(String title, int idx) {
        TableColumn<Object[], String> col = new TableColumn<>(title);
        col.setCellValueFactory(p -> new SimpleStringProperty((String) p.getValue()[idx]));
        return col;
    }

    private List<Object[]> loadKhach() {
        List<Object[]> result = new ArrayList<>();
        try {
            KhachHangDAO dao = new KhachHangDAO();
            List<KhachHang> list = dao.getAll();
            if (!list.isEmpty()) {
                String[] statuses = {"Đang Lưu Trú", "Đặt Trước", "Đã Trả Phòng"};
                for (int i = 0; i < list.size(); i++) {
                    KhachHang kh = list.get(i);
                    result.add(new Object[]{
                        kh.getMaKH(), kh.getHoTenKH(), "",
                        kh.getSoDienThoai(), kh.getCmnd(),
                        "N/A", "N/A",
                        statuses[i % statuses.length]
                    });
                }
                return result;
            }
        } catch (Exception ignored) {}
        for (Object[] r : DEMO_KHACH) result.add(r);
        return result;
    }

    private long countByStatus(String status) {
        int count = 0;
        for (Object[] r : DEMO_KHACH) {
            if (status.equals(r[7])) count++;
        }
        return count;
    }
}
