package com.lotuslaverne.fx.views;

import com.lotuslaverne.dao.NhanVienDAO;
import com.lotuslaverne.entity.NhanVien;
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

public class NhanVienView {

    private static final String[] PHONG_BAN = {
        "Lễ Tân", "Quản Lý", "Buồng Phòng", "An Ninh", "Kỹ Thuật", "Ẩm Thực", "Tài Chính"
    };

    private static final Object[][] DEMO_NV = {
        {"NV001", "Nguyễn Thị Thu",   "Lễ Tân",     "Lễ Tân",     "0901234567", "thu.nguyen@lotus.vn",    "01/03/2023", "Đang Làm"},
        {"NV002", "Trần Văn Minh",    "Quản Lý",     "Quản Lý",    "0912345678", "minh.tran@lotus.vn",     "15/01/2022", "Đang Làm"},
        {"NV003", "Lê Thị Hoa",       "Nhân Viên",   "Buồng Phòng","0923456789", "hoa.le@lotus.vn",        "10/06/2023", "Đang Làm"},
        {"NV004", "Phạm Quốc Bảo",    "Bảo Vệ",      "An Ninh",    "0934567890", "bao.pham@lotus.vn",      "20/09/2022", "Đang Làm"},
        {"NV005", "Hoàng Văn Kỹ",     "Kỹ Thuật Viên","Kỹ Thuật",  "0945678901", "ky.hoang@lotus.vn",      "05/02/2024", "Đang Làm"},
        {"NV006", "Vũ Thị Bếp",       "Đầu Bếp",     "Ẩm Thực",   "0956789012", "bep.vu@lotus.vn",        "12/11/2021", "Đang Làm"},
        {"NV007", "Đặng Thị Kế",      "Kế Toán",     "Tài Chính",  "0967890123", "ke.dang@lotus.vn",       "07/07/2023", "Đang Làm"},
        {"NV008", "Bùi Văn Phục",     "Nhân Viên",   "Buồng Phòng","0978901234", "phuc.bui@lotus.vn",      "03/04/2024", "Nghỉ Phép"},
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
        Label title = new Label("Quản Lý Nhân Viên");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Label sub = new Label("Tổng cộng " + DEMO_NV.length + " nhân viên • Quản lý thông tin và phân công");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #8C8C8C;");
        header.getChildren().addAll(title, sub);

        content.getChildren().addAll(header, buildFormCard(), buildFilterBar(), buildTable());
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

        Label cardTitle = new Label("+ Thêm Nhân Viên Mới");
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

        // Row 0 labels
        String[] row0Labels = {"Họ Và Tên", "Chức Vụ", "Phòng Ban", "SĐT"};
        for (int i = 0; i < row0Labels.length; i++) {
            Label lbl = formLabel(row0Labels[i]);
            form.add(lbl, i, 0);
        }

        // Row 0 inputs
        TextField tfTen   = formField(); form.add(tfTen, 0, 1);
        TextField tfChucVu= formField(); form.add(tfChucVu, 1, 1);
        ComboBox<String> cbPhongBan = new ComboBox<>();
        cbPhongBan.getItems().addAll(PHONG_BAN);
        cbPhongBan.setValue("Lễ Tân");
        cbPhongBan.setMaxWidth(Double.MAX_VALUE);
        cbPhongBan.setStyle(comboStyle());
        form.add(cbPhongBan, 2, 1);
        TextField tfSDT = formField(); form.add(tfSDT, 3, 1);

        // Row 1 labels
        String[] row1Labels = {"Email", "Ngày Vào Làm", "Trạng Thái", ""};
        for (int i = 0; i < row1Labels.length; i++) {
            if (!row1Labels[i].isEmpty()) {
                form.add(formLabel(row1Labels[i]), i, 2);
            }
        }

        TextField tfEmail  = formField(); form.add(tfEmail, 0, 3);
        TextField tfNgay   = formField(); form.add(tfNgay, 1, 3);
        ComboBox<String> cbTT = new ComboBox<>();
        cbTT.getItems().addAll("Đang Làm", "Nghỉ Phép", "Đã Nghỉ Việc");
        cbTT.setValue("Đang Làm");
        cbTT.setMaxWidth(Double.MAX_VALUE);
        cbTT.setStyle(comboStyle());
        form.add(cbTT, 2, 3);

        Button addBtn = new Button("+ Thêm");
        addBtn.setStyle("-fx-background-color: #1890FF; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-padding: 8 20; -fx-font-weight: bold; -fx-cursor: hand;");
        form.add(addBtn, 3, 3);

        card.getChildren().addAll(cardTitle, form);
        return card;
    }

    private HBox buildFilterBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12, 16, 12, 16));
        bar.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 1);");

        TextField search = new TextField();
        search.setPromptText("🔍  Tìm theo tên, chức vụ...");
        search.setPrefWidth(260);
        search.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #E8E8E8;"
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-border-width: 1;"
                + "-fx-padding: 8 12 8 12; -fx-font-size: 13px;");

        HBox chips = new HBox(6);
        chips.setAlignment(Pos.CENTER_LEFT);
        String[] all = {"Tất Cả", "Lễ Tân", "Quản Lý", "Buồng Phòng", "An Ninh", "Kỹ Thuật", "Ẩm Thực", "Tài Chính"};
        List<Button> chipBtns = new ArrayList<>();
        for (String s : all) {
            Button btn = chipButton(s, s.equals("Tất Cả"));
            chipBtns.add(btn);
            chips.getChildren().add(btn);
        }
        for (Button btn : chipBtns) {
            String lbl = btn.getText();
            btn.setOnAction(e -> chipBtns.forEach(b -> applyChip(b, b.getText().equals(lbl))));
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label count = new Label(DEMO_NV.length + " nhân viên");
        count.setStyle("-fx-font-size: 12px; -fx-text-fill: #8C8C8C;");

        bar.getChildren().addAll(search, chips, spacer, count);
        return bar;
    }

    @SuppressWarnings("unchecked")
    private TableView<Object[]> buildTable() {
        TableView<Object[]> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(380);
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
                box.getChildren().addAll(UiUtils.makeAvatarCircle(item, 14), labelBold(item));
                setGraphic(box); setText(null);
            }
        });

        TableColumn<Object[], String> colChucVu = simple("Chức Vụ", 2);

        // Phòng ban badge
        TableColumn<Object[], String> colPB = new TableColumn<>("Phòng Ban");
        colPB.setCellValueFactory(p -> new SimpleStringProperty((String) p.getValue()[3]));
        colPB.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label b = new Label(item);
                b.setStyle("-fx-background-color: #F9F0FF; -fx-text-fill: #722ED1;"
                        + "-fx-padding: 2 8 2 8; -fx-background-radius: 10;"
                        + "-fx-font-size: 11px; -fx-font-weight: bold;");
                setGraphic(b); setText(null);
            }
        });

        TableColumn<Object[], String> colSDT   = simple("Điện Thoại", 4);
        TableColumn<Object[], String> colEmail = simple("Email",       5);
        TableColumn<Object[], String> colNgay  = simple("Ngày Vào Làm",6);

        // Trạng thái badge
        TableColumn<Object[], String> colTT = new TableColumn<>("Trạng Thái");
        colTT.setCellValueFactory(p -> new SimpleStringProperty((String) p.getValue()[7]));
        colTT.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label b = new Label(item);
                String bg = item.equals("Đang Làm") ? "#F6FFED" : "#FFFBE6";
                String fg = item.equals("Đang Làm") ? "#52C41A" : "#FAAD14";
                b.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";"
                        + "-fx-padding: 2 8 2 8; -fx-background-radius: 10;"
                        + "-fx-font-size: 11px; -fx-font-weight: bold;");
                setGraphic(b); setText(null);
            }
        });

        // Thao tác
        TableColumn<Object[], String> colAction = new TableColumn<>("Thao Tác");
        colAction.setCellFactory(tc -> new TableCell<>() {
            private final Button edit = styledBtn("Sửa", "#E6F4FF", "#1890FF");
            private final Button del  = styledBtn("Xóa", "#FFF1F0", "#FF4D4F");
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                HBox box = new HBox(6, edit, del);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
            }
        });
        colAction.setPrefWidth(100);

        table.getColumns().addAll(colStt, colTen, colChucVu, colPB, colSDT, colEmail, colNgay, colTT, colAction);
        table.setItems(FXCollections.observableArrayList(loadNhanVien()));
        return table;
    }

    private List<Object[]> loadNhanVien() {
        List<Object[]> result = new ArrayList<>();
        try {
            NhanVienDAO dao = new NhanVienDAO();
            List<NhanVien> list = dao.getAll();
            if (!list.isEmpty()) {
                for (NhanVien nv : list) {
                    result.add(new Object[]{
                        nv.getMaNhanVien(), nv.getTenNhanVien(),
                        nv.getVaiTro(), nv.getVaiTro(),
                        nv.getSoDienThoai(), "", "N/A", "Đang Làm"
                    });
                }
                return result;
            }
        } catch (Exception ignored) {}
        for (Object[] r : DEMO_NV) result.add(r);
        return result;
    }

    // Helpers
    private Label formLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #595959;");
        return l;
    }

    private TextField formField() {
        TextField tf = new TextField();
        tf.setMaxWidth(Double.MAX_VALUE);
        tf.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D9D9D9;"
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1;"
                + "-fx-padding: 7 10 7 10;");
        return tf;
    }

    private String comboStyle() {
        return "-fx-background-color: #FFFFFF; -fx-border-color: #D9D9D9;"
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1;";
    }

    private Button chipButton(String text, boolean active) {
        Button btn = new Button(text);
        applyChip(btn, active);
        return btn;
    }

    private void applyChip(Button btn, boolean active) {
        if (active) {
            btn.setStyle("-fx-background-color: #1890FF; -fx-text-fill: white;"
                    + "-fx-background-radius: 20; -fx-border-radius: 20;"
                    + "-fx-border-color: #1890FF; -fx-border-width: 1;"
                    + "-fx-padding: 4 14 4 14; -fx-font-size: 12px; -fx-cursor: hand;");
        } else {
            btn.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #333333;"
                    + "-fx-background-radius: 20; -fx-border-radius: 20;"
                    + "-fx-border-color: #D9D9D9; -fx-border-width: 1;"
                    + "-fx-padding: 4 14 4 14; -fx-font-size: 12px; -fx-cursor: hand;");
        }
    }

    private TableColumn<Object[], String> simple(String title, int idx) {
        TableColumn<Object[], String> col = new TableColumn<>(title);
        col.setCellValueFactory(p -> new SimpleStringProperty((String) p.getValue()[idx]));
        return col;
    }

    private Label labelBold(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        return l;
    }

    private Button styledBtn(String text, String bg, String fg) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";"
                + "-fx-background-radius: 6; -fx-border-radius: 6;"
                + "-fx-font-size: 11px; -fx-padding: 3 8; -fx-cursor: hand;");
        return b;
    }
}
