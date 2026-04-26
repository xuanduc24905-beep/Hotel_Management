package com.lotuslaverne.fx.views;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * NhanSuView – Gộp Nhân Viên, Tài Khoản, Hóa Đơn, Khuyến Mãi vào TabPane.
 */
public class NhanSuView {
    public Node build() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle("-fx-background-color: #F0F2F5;");

        Tab t1 = new Tab("Nhan Vien",   new NhanVienView().build());
        Tab t2 = new Tab("Tai Khoan",   new TaiKhoanView("").build());
        Tab t3 = new Tab("Hoa Don",     new HoaDonView().build());
        Tab t4 = new Tab("Khuyen Mai",  new KhuyenMaiView().build());

        tabs.getTabs().addAll(t1, t2, t3, t4);
        return tabs;
    }
}
