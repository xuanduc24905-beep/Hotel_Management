package com.lotuslaverne.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class UIFactory {

    // Style for the main container panel of a module
    public static void styleMainPanel(JPanel panel) {
        panel.setBackground(new Color(245, 246, 250));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
    }

    // Style for input form wrapper panel (White Card)
    public static void styleFormPanel(JPanel panel) {
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(15, 15, 15, 15)
        ));
    }

    // Style for Data Tables
    public static void styleTable(JTable table) {
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(240, 240, 240));
        table.setFont(new Font("Arial", Font.BOLD, 12));
        table.setForeground(new Color(60, 60, 60));
        
        // Header style
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(250, 250, 250));
        header.setForeground(Color.GRAY);
        header.setFont(new Font("Arial", Font.BOLD, 11));
        header.setPreferredSize(new Dimension(100, 40));
        ((DefaultTableCellRenderer)header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        // Center align text in all columns by default
        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, centerRender);
        table.setDefaultRenderer(String.class, centerRender);
    }
    
    // Style for Action Buttons
    public static JButton createActionButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(150, 38));
        btn.setMaximumSize(new Dimension(200, 38));
        return btn;
    }
}
