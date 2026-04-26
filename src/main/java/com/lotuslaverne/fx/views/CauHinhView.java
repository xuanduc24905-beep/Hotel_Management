package com.lotuslaverne.fx.views;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * CauHinhView – Gộp Bảng Giá, Dịch Vụ, Thiết Bị vào TabPane.
 */
public class CauHinhView {
    public Node build() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle("-fx-background-color: #F0F2F5;");

        Tab t1 = new Tab("Bang Gia",  new BangGiaView().build());
        Tab t2 = new Tab("Dich Vu",   new DichVuView().build());
        Tab t3 = new Tab("Thiet Bi",  new ThietBiView().build());

        tabs.getTabs().addAll(t1, t2, t3);
        return tabs;
    }
}
