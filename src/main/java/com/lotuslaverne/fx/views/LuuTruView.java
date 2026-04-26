package com.lotuslaverne.fx.views;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * LuuTruView – Gộp 3 màn hình lưu trú vào TabPane.
 */
public class LuuTruView {
    public Node build() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle("-fx-background-color: #F0F2F5;");

        Tab t1 = new Tab("Check-in",       new CheckInView().build());
        Tab t2 = new Tab("Doi Phong",  new DoiPhongView().build());
        Tab t3 = new Tab("Dich Vu Phong", new DichVuPhongView().build());

        tabs.getTabs().addAll(t1, t2, t3);
        return tabs;
    }
}
