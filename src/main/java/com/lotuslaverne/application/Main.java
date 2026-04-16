package com.lotuslaverne.application;

import com.formdev.flatlaf.FlatLightLaf;
import com.lotuslaverne.gui.FrmLogin;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Thiết lập giao diện hiện đại FlatLaf
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            // Tuỳ chỉnh viền component
            UIManager.put( "Button.arc", 10 );
            UIManager.put( "Component.arc", 10 );
            UIManager.put( "TextComponent.arc", 10 );
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        // Chạy giao diện Đăng Nhập
        SwingUtilities.invokeLater(() -> {
            FrmLogin loginUI = new FrmLogin();
            loginUI.setVisible(true);
        });
    }
}
