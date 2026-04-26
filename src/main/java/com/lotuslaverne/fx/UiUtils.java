package com.lotuslaverne.fx;

import javafx.animation.PauseTransition;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Duration;

public final class UiUtils {

    private UiUtils() {}

    /**
     * Flash button text/style ngắn để cho user thấy là click đã được xử lý.
     * Sau ~1.2 giây sẽ revert lại text + style cũ.
     */
    public static void flashButton(Button btn, String successText) {
        String oldText  = btn.getText();
        String oldStyle = btn.getStyle();
        btn.setText(successText);
        btn.setStyle(oldStyle
                + "-fx-background-color: #52C41A !important;"
                + "-fx-text-fill: white !important;"
                + "-fx-border-color: #52C41A;");
        btn.setDisable(true);
        PauseTransition pt = new PauseTransition(Duration.millis(900));
        pt.setOnFinished(e -> {
            btn.setText(oldText);
            btn.setStyle(oldStyle);
            btn.setDisable(false);
        });
        pt.play();
    }

    private static final String[] AVATAR_COLORS = {
        "#1890FF", "#52C41A", "#FF7A45", "#9254DE",
        "#13C2C2", "#FAAD14", "#F5222D", "#2F54EB"
    };

    public static String pickColor(String name) {
        if (name == null || name.isEmpty()) return AVATAR_COLORS[0];
        int idx = Math.abs(name.hashCode()) % AVATAR_COLORS.length;
        return AVATAR_COLORS[idx];
    }

    /**
     * Creates a circular avatar StackPane.
     *
     * @param name   Used for the initial letter and colour selection.
     * @param radius The radius in px (diameter = radius * 2).
     */
    public static StackPane makeAvatarCircle(String name, double radius) {
        String initial = (name != null && !name.isEmpty())
                ? String.valueOf(name.charAt(0)).toUpperCase() : "?";
        String color = pickColor(name);

        StackPane pane = new StackPane();
        double size = radius * 2;
        pane.setStyle("-fx-background-color: " + color + ";"
                + "-fx-background-radius: " + radius + ";"
                + "-fx-min-width: " + size + "px; -fx-min-height: " + size + "px;"
                + "-fx-max-width: " + size + "px; -fx-max-height: " + size + "px;");

        Text txt = new Text(initial);
        txt.setStyle("-fx-font-size: " + (radius * 0.75) + "px;"
                + "-fx-font-weight: bold; -fx-fill: white;");
        pane.getChildren().add(txt);
        return pane;
    }
}
