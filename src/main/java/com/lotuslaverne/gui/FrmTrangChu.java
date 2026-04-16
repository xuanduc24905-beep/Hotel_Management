package com.lotuslaverne.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class FrmTrangChu extends JPanel {

    public FrmTrangChu() {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 246, 250));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- TOP CARDS ---
        JPanel pnlCards = new JPanel(new GridLayout(1, 4, 20, 0));
        pnlCards.setBackground(new Color(245, 246, 250));
        pnlCards.setPreferredSize(new Dimension(0, 120));

        pnlCards.add(createStatCard("Phòng đến trong ngày", "2.267", new Color(225, 238, 255), new Color(11, 26, 44)));
        pnlCards.add(createStatCard("Phòng đi trong ngày", "1.267", new Color(245, 236, 255), new Color(11, 26, 44)));
        pnlCards.add(createStatCard("Phòng đang sử dụng", "4.982", new Color(225, 250, 235), new Color(11, 26, 44)));
        pnlCards.add(createStatCard("Khách đang ở", "4.521", new Color(255, 236, 232), new Color(11, 26, 44)));

        add(pnlCards, BorderLayout.NORTH);

        // --- CENTER TABLE SECTION ---
        JPanel pnlTableWrapper = new JPanel(new BorderLayout());
        pnlTableWrapper.setBackground(Color.WHITE);
        pnlTableWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(0, 0, 0, 0)
        ));

        // Fake Tab Header
        JPanel pnlTabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 15));
        pnlTabs.setBackground(Color.WHITE);
        pnlTabs.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));

        JLabel lblTab1 = new JLabel("Phòng đến trong ngày");
        lblTab1.setFont(new Font("Arial", Font.BOLD, 14));
        lblTab1.setForeground(new Color(24, 144, 255)); // Active blue
        lblTab1.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(24, 144, 255)));

        JLabel lblTab2 = new JLabel("Phòng đi trong ngày");
        lblTab2.setFont(new Font("Arial", Font.PLAIN, 14));
        lblTab2.setForeground(Color.GRAY);

        JLabel lblTab3 = new JLabel("Phòng đang sử dụng");
        lblTab3.setFont(new Font("Arial", Font.PLAIN, 14));
        lblTab3.setForeground(Color.GRAY);

        pnlTabs.add(lblTab1);
        pnlTabs.add(lblTab2);
        pnlTabs.add(lblTab3);

        pnlTableWrapper.add(pnlTabs, BorderLayout.NORTH);

        // Table
        String[] cols = {"STT", "MÃ XÁC NHẬN", "MÃ BOOKING", "KHÁCH HÀNG", "LOẠI PHÒNG", "SỐ PHÒNG", "NGÀY ĐẾN", "NGÀY ĐI", "CÔNG TY"};
        Object[][] data = {
                {"01", "998866", "668899", "NGUYEN DUY HIEN", "Executive Double", "7003", "26/02/2021", "28/02/2021", "booking.com"},
                {"02", "998866", "668899", "TRAN QUANG MINH", "Executive Double", "7003", "26/02/2021", "28/02/2021", "booking.com"},
                {"03", "998866", "668899", "VU TO UYEN", "Executive Double", "7003", "26/02/2021", "28/02/2021", "booking.com"},
                {"04", "998866", "668899", "NGUYEN VAN TRUONG", "Executive Double", "7003", "26/02/2021", "28/02/2021", "booking.com"},
                {"05", "998866", "668899", "DO MINH HOA", "Executive Double", "7003", "26/02/2021", "28/02/2021", "booking.com"},
                {"06", "998866", "668899", "DINH VAN HIEU", "Executive Double", "7003", "26/02/2021", "28/02/2021", "booking.com"},
                {"07", "998866", "668899", "NGUYEN TUAN MINH", "Executive Double", "7003", "26/02/2021", "28/02/2021", "booking.com"},
                {"08", "998866", "668899", "TRAN MINH TUAN", "Executive Double", "7003", "26/02/2021", "28/02/2021", "booking.com"},
                {"09", "998866", "668899", "NGUYEN THI LUYEN", "Executive Double", "7003", "26/02/2021", "28/02/2021", "booking.com"}
        };

        DefaultTableModel model = new DefaultTableModel(data, cols) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
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

        // Center align text and color specific columns
        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(JLabel.CENTER);
        
        DefaultTableCellRenderer dateInRender = new DefaultTableCellRenderer();
        dateInRender.setHorizontalAlignment(JLabel.CENTER);
        dateInRender.setForeground(new Color(32, 201, 151)); // teal
        
        DefaultTableCellRenderer dateOutRender = new DefaultTableCellRenderer();
        dateOutRender.setHorizontalAlignment(JLabel.CENTER);
        dateOutRender.setForeground(new Color(253, 126, 20)); // orange

        table.getColumnModel().getColumn(0).setCellRenderer(centerRender);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRender);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRender);
        table.getColumnModel().getColumn(5).setCellRenderer(centerRender);
        table.getColumnModel().getColumn(6).setCellRenderer(dateInRender);  // Ngay den
        table.getColumnModel().getColumn(7).setCellRenderer(dateOutRender); // Ngay di
        table.getColumnModel().getColumn(8).setCellRenderer(centerRender);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        pnlTableWrapper.add(scrollPane, BorderLayout.CENTER);

        // Bottom pagination fake
        JPanel pnlBottom = new JPanel(new BorderLayout());
        pnlBottom.setBackground(Color.WHITE);
        pnlBottom.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        JLabel lblShowing = new JLabel("Hiển thị 1 - 10 trong số 50 mục");
        lblShowing.setForeground(Color.GRAY);
        lblShowing.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JPanel pnlPaging = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        pnlPaging.setBackground(Color.WHITE);
        pnlPaging.add(new JLabel(" < "));
        
        JLabel lblPage1 = new JLabel("  1  ");
        lblPage1.setOpaque(true);
        lblPage1.setBackground(new Color(225, 238, 255));
        lblPage1.setForeground(new Color(24, 144, 255));
        pnlPaging.add(lblPage1);
        
        pnlPaging.add(new JLabel("  2  "));
        pnlPaging.add(new JLabel("  3  "));
        pnlPaging.add(new JLabel("  4  "));
        pnlPaging.add(new JLabel("  5  "));
        pnlPaging.add(new JLabel(" ... "));
        pnlPaging.add(new JLabel("  12 "));
        pnlPaging.add(new JLabel(" > "));
        
        pnlBottom.add(lblShowing, BorderLayout.WEST);
        pnlBottom.add(pnlPaging, BorderLayout.EAST);
        
        pnlTableWrapper.add(pnlBottom, BorderLayout.SOUTH);

        add(pnlTableWrapper, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, String value, Color bgColor, Color textColor) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(bgColor);
        
        // Simulating rounded corners in Standard Swing 
        // using EmptyBorder will just show square, but with FlatLaf roundRect can be used.
        panel.putClientProperty("FlatLaf.style", "arc: 15"); 
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Arial", Font.PLAIN, 14));
        lblTitle.setForeground(new Color(80, 90, 100)); // Darker gray

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Arial", Font.BOLD, 42));
        lblValue.setForeground(textColor);

        panel.add(lblTitle);
        panel.add(Box.createVerticalStrut(10));
        panel.add(lblValue);

        return panel;
    }
}
