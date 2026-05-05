package com.lotuslaverne.fx.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.*;
import java.util.*;

/**
 * CaiDatView – Màn hình Cài Đặt hệ thống.
 * Cho phép chỉnh sửa inline từng card với nút Chỉnh Sửa / Lưu / Hủy.
 * Giá trị được lưu vào file caidat.properties trong thư mục làm việc.
 */
public class CaiDatView {

    private static final String PROPS_FILE = "caidat.properties";

    // ── Default values ──────────────────────────────────────────────
    private static final String[][] HOTEL_DEFAULTS = {
        {"hotel.name",    "Lotus Laverne Hotel"},
        {"hotel.address", "123 Nguyễn Huệ, Q.1, TP.HCM"},
        {"hotel.phone",   "(028) 3822 1234"},
        {"hotel.email",   "info@lotuslaverne.vn"},
        {"hotel.website", "www.lotuslaverne.vn"},
        {"hotel.rooms",   "30 phòng"},
    };
    private static final String[] HOTEL_LABELS = {
        "Tên Khách Sạn", "Địa Chỉ", "Điện Thoại", "Email", "Website", "Số Phòng"
    };

    private static final String[][] NOTIF_DEFAULTS = {
        {"notif.checkin",  "true"},
        {"notif.checkout", "true"},
        {"notif.payment",  "true"},
        {"notif.sysalert", "false"},
        {"notif.daily",    "true"},
        {"notif.weekly",   "false"},
    };
    private static final String[] NOTIF_LABELS = {
        "Nhận Phòng Mới", "Trả Phòng", "Thanh Toán",
        "Cảnh Báo Hệ Thống", "Báo Cáo Hàng Ngày", "Email Tổng Kết Tuần"
    };

    private static final String[][] SECURITY_ITEMS = {
        {"sec.2fa",        "false",   "toggle"},
        {"sec.session",    "30 phút", "text"},
        {"sec.accesslog",  "true",    "toggle"},
        {"sec.encrypt",    "true",    "toggle"},
        {"sec.pwdchange",  "90 ngày", "text"},
    };
    private static final String[] SECURITY_LABELS = {
        "Xác Thực 2 Bước", "Thời Gian Hết Phiên", "Nhật Ký Truy Cập",
        "Mã Hoá Dữ Liệu", "Đổi Mật Khẩu Định Kỳ"
    };

    private static final String[][] SYSTEM_DEFAULTS = {
        {"sys.version",  "v1.0.0"},
        {"sys.language", "Tiếng Việt"},
        {"sys.timezone", "GMT+7 (Hà Nội)"},
        {"sys.dateformat","dd/MM/yyyy"},
        {"sys.currency", "VND (₫)"},
        {"sys.backup",   "Hàng Ngày 02:00"},
    };
    private static final String[] SYSTEM_LABELS = {
        "Phiên Bản", "Ngôn Ngữ", "Múi Giờ", "Định Dạng Ngày",
        "Đơn Vị Tiền Tệ", "Tự Động Sao Lưu"
    };

    private final Properties props = new Properties();

    public CaiDatView() {
        loadProps();
    }

    // ── Build ────────────────────────────────────────────────────────
    public Node build() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #F0F2F5; -fx-border-color: transparent;");

        VBox content = new VBox(24);
        content.setPadding(new Insets(28));
        content.setStyle("-fx-background-color: #F0F2F5;");

        // Page header
        VBox header = new VBox(4);
        Label title = new Label("Cài Đặt");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        Label sub = new Label("Cấu hình hệ thống quản lý khách sạn");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #8C8C8C;");
        header.getChildren().addAll(title, sub);

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        ColumnConstraints cc1 = new ColumnConstraints(); cc1.setPercentWidth(50);
        ColumnConstraints cc2 = new ColumnConstraints(); cc2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(cc1, cc2);

        grid.add(buildHotelInfoCard(), 0, 0);
        grid.add(buildNotificationCard(), 1, 0);
        grid.add(buildSecurityCard(), 0, 1);
        grid.add(buildSystemCard(), 1, 1);

        content.getChildren().addAll(header, grid);
        scroll.setContent(content);
        return scroll;
    }

    // ── Hotel Info Card ──────────────────────────────────────────────
    private VBox buildHotelInfoCard() {
        return buildTextCard("Thông Tin Khách Sạn", "🏨",
                HOTEL_LABELS, HOTEL_DEFAULTS);
    }

    // ── Notification Card (toggles) ─────────────────────────────────
    private VBox buildNotificationCard() {
        return buildToggleCard("Thông Báo", "🔔",
                NOTIF_LABELS, NOTIF_DEFAULTS);
    }

    // ── Security Card (mixed) ───────────────────────────────────────
    private VBox buildSecurityCard() {
        return buildMixedCard("Bảo Mật", "🔒",
                SECURITY_LABELS, SECURITY_ITEMS);
    }

    // ── System Card ─────────────────────────────────────────────────
    private VBox buildSystemCard() {
        return buildTextCard("Hệ Thống", "⚙",
                SYSTEM_LABELS, SYSTEM_DEFAULTS);
    }

    // ================================================================
    //  CARD BUILDERS
    // ================================================================

    /**
     * Card with all text-editable rows.
     */
    private VBox buildTextCard(String title, String icon,
                               String[] labels, String[][] defs) {
        VBox card = card(title, icon);

        // Containers for display vs edit mode
        VBox displayBox = new VBox(0);
        VBox editBox = new VBox(0);
        editBox.setVisible(false);
        editBox.setManaged(false);

        List<TextField> fields = new ArrayList<>();

        for (int i = 0; i < labels.length; i++) {
            String key = defs[i][0];
            String val = props.getProperty(key, defs[i][1]);

            displayBox.getChildren().add(settingRow(labels[i], val));

            // Edit row
            TextField tf = editTextField(val);
            fields.add(tf);
            editBox.getChildren().add(editRow(labels[i], tf));
        }

        // Status label for feedback
        Label statusLbl = statusLabel();

        // Buttons
        Button btnEdit = editButton();
        Button btnSave = saveButton();
        Button btnCancel = cancelButton();
        HBox btnRow = new HBox(8, btnCancel, btnSave);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setVisible(false);
        btnRow.setManaged(false);

        btnEdit.setOnAction(e -> {
            // Refresh field values from props
            for (int i = 0; i < fields.size(); i++) {
                fields.get(i).setText(props.getProperty(defs[i][0], defs[i][1]));
            }
            displayBox.setVisible(false); displayBox.setManaged(false);
            editBox.setVisible(true); editBox.setManaged(true);
            btnEdit.setVisible(false); btnEdit.setManaged(false);
            btnRow.setVisible(true); btnRow.setManaged(true);
            statusLbl.setVisible(false); statusLbl.setManaged(false);
        });

        btnCancel.setOnAction(e -> {
            editBox.setVisible(false); editBox.setManaged(false);
            displayBox.setVisible(true); displayBox.setManaged(true);
            btnRow.setVisible(false); btnRow.setManaged(false);
            btnEdit.setVisible(true); btnEdit.setManaged(true);
        });

        btnSave.setOnAction(e -> {
            for (int i = 0; i < fields.size(); i++) {
                String newVal = fields.get(i).getText().trim();
                if (newVal.isEmpty()) newVal = defs[i][1];
                props.setProperty(defs[i][0], newVal);
            }
            saveProps();

            // Rebuild display rows
            displayBox.getChildren().clear();
            for (int i = 0; i < labels.length; i++) {
                displayBox.getChildren().add(
                    settingRow(labels[i], props.getProperty(defs[i][0], defs[i][1])));
            }

            editBox.setVisible(false); editBox.setManaged(false);
            displayBox.setVisible(true); displayBox.setManaged(true);
            btnRow.setVisible(false); btnRow.setManaged(false);
            btnEdit.setVisible(true); btnEdit.setManaged(true);
            showStatus(statusLbl, true, "✓ Đã lưu thành công!");
        });

        card.getChildren().addAll(displayBox, editBox, statusLbl, btnEdit, btnRow);
        return card;
    }

    /**
     * Card with all toggle (boolean) rows.
     */
    private VBox buildToggleCard(String title, String icon,
                                 String[] labels, String[][] defs) {
        VBox card = card(title, icon);

        VBox displayBox = new VBox(0);
        VBox editBox = new VBox(0);
        editBox.setVisible(false);
        editBox.setManaged(false);

        List<CheckBox> toggles = new ArrayList<>();

        for (int i = 0; i < labels.length; i++) {
            String key = defs[i][0];
            boolean val = "true".equals(props.getProperty(key, defs[i][1]));

            displayBox.getChildren().add(notifRow(labels[i], val));

            CheckBox cb = toggleSwitch(val);
            toggles.add(cb);
            editBox.getChildren().add(toggleEditRow(labels[i], cb));
        }

        Label statusLbl = statusLabel();

        Button btnEdit = editButton();
        Button btnSave = saveButton();
        Button btnCancel = cancelButton();
        HBox btnRow = new HBox(8, btnCancel, btnSave);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setVisible(false); btnRow.setManaged(false);

        btnEdit.setOnAction(e -> {
            for (int i = 0; i < toggles.size(); i++) {
                toggles.get(i).setSelected(
                    "true".equals(props.getProperty(defs[i][0], defs[i][1])));
            }
            displayBox.setVisible(false); displayBox.setManaged(false);
            editBox.setVisible(true); editBox.setManaged(true);
            btnEdit.setVisible(false); btnEdit.setManaged(false);
            btnRow.setVisible(true); btnRow.setManaged(true);
            statusLbl.setVisible(false); statusLbl.setManaged(false);
        });

        btnCancel.setOnAction(e -> {
            editBox.setVisible(false); editBox.setManaged(false);
            displayBox.setVisible(true); displayBox.setManaged(true);
            btnRow.setVisible(false); btnRow.setManaged(false);
            btnEdit.setVisible(true); btnEdit.setManaged(true);
        });

        btnSave.setOnAction(e -> {
            for (int i = 0; i < toggles.size(); i++) {
                props.setProperty(defs[i][0], String.valueOf(toggles.get(i).isSelected()));
            }
            saveProps();

            displayBox.getChildren().clear();
            for (int i = 0; i < labels.length; i++) {
                boolean v = "true".equals(props.getProperty(defs[i][0], defs[i][1]));
                displayBox.getChildren().add(notifRow(labels[i], v));
            }

            editBox.setVisible(false); editBox.setManaged(false);
            displayBox.setVisible(true); displayBox.setManaged(true);
            btnRow.setVisible(false); btnRow.setManaged(false);
            btnEdit.setVisible(true); btnEdit.setManaged(true);
            showStatus(statusLbl, true, "✓ Đã lưu thành công!");
        });

        card.getChildren().addAll(displayBox, editBox, statusLbl, btnEdit, btnRow);
        return card;
    }

    /**
     * Card with mixed text + toggle rows (Security card).
     */
    private VBox buildMixedCard(String title, String icon,
                                String[] labels, String[][] items) {
        VBox card = card(title, icon);

        VBox displayBox = new VBox(0);
        VBox editBox = new VBox(0);
        editBox.setVisible(false); editBox.setManaged(false);

        // Store edit controls — either TextField or CheckBox
        List<Node> controls = new ArrayList<>();

        for (int i = 0; i < labels.length; i++) {
            String key = items[i][0];
            String def = items[i][1];
            String type = items[i][2];
            String val = props.getProperty(key, def);

            if ("toggle".equals(type)) {
                boolean bv = "true".equals(val);
                displayBox.getChildren().add(settingRow(labels[i], bv ? "Bật" : "Tắt"));
                CheckBox cb = toggleSwitch(bv);
                controls.add(cb);
                editBox.getChildren().add(toggleEditRow(labels[i], cb));
            } else {
                displayBox.getChildren().add(settingRow(labels[i], val));
                TextField tf = editTextField(val);
                controls.add(tf);
                editBox.getChildren().add(editRow(labels[i], tf));
            }
        }

        Label statusLbl = statusLabel();

        Button btnEdit = editButton();
        Button btnSave = saveButton();
        Button btnCancel = cancelButton();
        HBox btnRow = new HBox(8, btnCancel, btnSave);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setVisible(false); btnRow.setManaged(false);

        btnEdit.setOnAction(e -> {
            for (int i = 0; i < controls.size(); i++) {
                String key = items[i][0];
                String def = items[i][1];
                String val = props.getProperty(key, def);
                Node ctrl = controls.get(i);
                if (ctrl instanceof CheckBox cb) cb.setSelected("true".equals(val));
                else if (ctrl instanceof TextField tf) tf.setText(val);
            }
            displayBox.setVisible(false); displayBox.setManaged(false);
            editBox.setVisible(true); editBox.setManaged(true);
            btnEdit.setVisible(false); btnEdit.setManaged(false);
            btnRow.setVisible(true); btnRow.setManaged(true);
            statusLbl.setVisible(false); statusLbl.setManaged(false);
        });

        btnCancel.setOnAction(e -> {
            editBox.setVisible(false); editBox.setManaged(false);
            displayBox.setVisible(true); displayBox.setManaged(true);
            btnRow.setVisible(false); btnRow.setManaged(false);
            btnEdit.setVisible(true); btnEdit.setManaged(true);
        });

        btnSave.setOnAction(e -> {
            for (int i = 0; i < controls.size(); i++) {
                Node ctrl = controls.get(i);
                if (ctrl instanceof CheckBox cb) {
                    props.setProperty(items[i][0], String.valueOf(cb.isSelected()));
                } else if (ctrl instanceof TextField tf) {
                    String nv = tf.getText().trim();
                    if (nv.isEmpty()) nv = items[i][1];
                    props.setProperty(items[i][0], nv);
                }
            }
            saveProps();

            displayBox.getChildren().clear();
            for (int i = 0; i < labels.length; i++) {
                String key = items[i][0];
                String def = items[i][1];
                String type = items[i][2];
                String val = props.getProperty(key, def);
                if ("toggle".equals(type)) {
                    displayBox.getChildren().add(settingRow(labels[i], "true".equals(val) ? "Bật" : "Tắt"));
                } else {
                    displayBox.getChildren().add(settingRow(labels[i], val));
                }
            }

            editBox.setVisible(false); editBox.setManaged(false);
            displayBox.setVisible(true); displayBox.setManaged(true);
            btnRow.setVisible(false); btnRow.setManaged(false);
            btnEdit.setVisible(true); btnEdit.setManaged(true);
            showStatus(statusLbl, true, "✓ Đã lưu thành công!");
        });

        card.getChildren().addAll(displayBox, editBox, statusLbl, btnEdit, btnRow);
        return card;
    }

    // ================================================================
    //  PERSISTENCE
    // ================================================================

    private void loadProps() {
        File f = new File(PROPS_FILE);
        if (f.exists()) {
            try (FileInputStream fis = new FileInputStream(f)) {
                props.load(fis);
                return;
            } catch (IOException ignored) {}
        }
        // Load defaults
        for (String[] d : HOTEL_DEFAULTS)    props.setProperty(d[0], d[1]);
        for (String[] d : NOTIF_DEFAULTS)    props.setProperty(d[0], d[1]);
        for (String[] d : SECURITY_ITEMS)    props.setProperty(d[0], d[1]);
        for (String[] d : SYSTEM_DEFAULTS)   props.setProperty(d[0], d[1]);
    }

    private void saveProps() {
        try (FileOutputStream fos = new FileOutputStream(PROPS_FILE)) {
            props.store(fos, "Lotus Laverne Hotel - Cài đặt hệ thống");
        } catch (IOException ex) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Lỗi lưu cài đặt: " + ex.getMessage());
            a.setHeaderText(null);
            a.showAndWait();
        }
    }

    // ================================================================
    //  UI HELPERS
    // ================================================================

    private VBox card(String title, String icon) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 1);");

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        titleRow.setPadding(new Insets(0, 0, 10, 0));
        titleRow.setStyle("-fx-border-color: transparent transparent #F0F2F5 transparent;"
                + "-fx-border-width: 0 0 1 0;");
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 16px;");
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        titleRow.getChildren().addAll(iconLbl, titleLbl);
        card.getChildren().add(titleRow);
        return card;
    }

    /** Display-mode row: label ... value */
    private HBox settingRow(String label, String value) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(5, 0, 5, 0));

        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #595959;");
        lbl.setPrefWidth(180);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label val = new Label(value);
        val.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");

        row.getChildren().addAll(lbl, spacer, val);
        return row;
    }

    /** Display-mode row for notifications (Bật/Tắt badge) */
    private HBox notifRow(String label, boolean enabled) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(5, 0, 5, 0));

        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #595959;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badge = new Label(enabled ? "Bật" : "Tắt");
        String bg = enabled ? "#F6FFED" : "#F5F5F5";
        String fg = enabled ? "#52C41A" : "#8C8C8C";
        badge.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";"
                + "-fx-padding: 2 10 2 10; -fx-background-radius: 10;"
                + "-fx-font-size: 11px; -fx-font-weight: bold;");

        row.getChildren().addAll(lbl, spacer, badge);
        return row;
    }

    /** Edit-mode row with a text field */
    private HBox editRow(String label, TextField tf) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(4, 0, 4, 0));

        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #595959; -fx-font-weight: bold;");
        lbl.setPrefWidth(150);
        lbl.setMinWidth(150);

        HBox.setHgrow(tf, Priority.ALWAYS);
        row.getChildren().addAll(lbl, tf);
        return row;
    }

    /** Edit-mode row with a toggle checkbox */
    private HBox toggleEditRow(String label, CheckBox cb) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(4, 0, 4, 0));

        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #595959; -fx-font-weight: bold;");
        lbl.setPrefWidth(150);
        lbl.setMinWidth(150);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(lbl, spacer, cb);
        return row;
    }

    private TextField editTextField(String value) {
        TextField tf = new TextField(value);
        tf.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D9D9D9;"
                + "-fx-border-radius: 6; -fx-background-radius: 6;"
                + "-fx-border-width: 1; -fx-padding: 6 10; -fx-font-size: 12px;");
        return tf;
    }

    private CheckBox toggleSwitch(boolean selected) {
        CheckBox cb = new CheckBox(selected ? "Bật" : "Tắt");
        cb.setSelected(selected);
        cb.setStyle("-fx-font-size: 12px; -fx-cursor: hand;");
        cb.selectedProperty().addListener((obs, o, n) ->
                cb.setText(n ? "Bật" : "Tắt"));
        return cb;
    }

    private Button editButton() {
        Button btn = new Button("✏  Chỉnh Sửa");
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #1890FF;"
                + "-fx-border-color: #1890FF; -fx-border-width: 1; -fx-border-radius: 8;"
                + "-fx-background-radius: 8; -fx-font-size: 12px; -fx-padding: 6 16;"
                + "-fx-cursor: hand; -fx-font-weight: bold;");
        btn.setMaxWidth(Double.MAX_VALUE);
        return btn;
    }

    private Button saveButton() {
        Button btn = new Button("💾  Lưu");
        btn.setStyle("-fx-background-color: #1890FF; -fx-text-fill: white;"
                + "-fx-background-radius: 8; -fx-padding: 6 20;"
                + "-fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand;");
        return btn;
    }

    private Button cancelButton() {
        Button btn = new Button("✕  Hủy");
        btn.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #595959;"
                + "-fx-border-color: #D9D9D9; -fx-border-width: 1; -fx-border-radius: 8;"
                + "-fx-background-radius: 8; -fx-font-size: 12px; -fx-padding: 6 16;"
                + "-fx-cursor: hand;");
        return btn;
    }

    private Label statusLabel() {
        Label lbl = new Label();
        lbl.setVisible(false);
        lbl.setManaged(false);
        return lbl;
    }

    private void showStatus(Label lbl, boolean success, String msg) {
        lbl.setText(msg);
        lbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: "
                + (success ? "#52C41A" : "#FF4D4F") + "; -fx-padding: 4 0 0 0;");
        lbl.setVisible(true);
        lbl.setManaged(true);

        // Auto-hide after 3 seconds
        new Thread(() -> {
            try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
            javafx.application.Platform.runLater(() -> {
                lbl.setVisible(false);
                lbl.setManaged(false);
            });
        }).start();
    }
}
