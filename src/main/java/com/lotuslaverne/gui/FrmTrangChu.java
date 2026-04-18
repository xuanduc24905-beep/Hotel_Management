package com.lotuslaverne.gui;

import com.lotuslaverne.util.ConnectDB;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;

public class FrmTrangChu extends JPanel {

    private JLabel lblPhongTrong, lblPhongDung, lblPhongDat, lblPhongCanDon;
    private JLabel lblDoanhThu, lblKhachDangO, lblCheckinHom, lblCheckoutHom;
    private DefaultTableModel tableModel;

    public FrmTrangChu() {
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(16, 16));
        setBackground(new Color(245, 246, 250));
        setBorder(new EmptyBorder(24, 24, 24, 24));

        // ── Row 1: Stat cards ──
        JPanel pnlCards = new JPanel(new GridLayout(1, 4, 16, 0));
        pnlCards.setBackground(new Color(245, 246, 250));
        pnlCards.setPreferredSize(new Dimension(0, 110));

        lblPhongTrong  = new JLabel("...");
        lblPhongDung   = new JLabel("...");
        lblPhongDat    = new JLabel("...");
        lblPhongCanDon = new JLabel("...");

        pnlCards.add(createStatCard("Phòng Trống",    lblPhongTrong,  new Color(230, 244, 255), new Color(24, 144, 255),  "🟦"));
        pnlCards.add(createStatCard("Đang Sử Dụng",   lblPhongDung,   new Color(230, 255, 236), new Color(40, 167, 69),   "🟩"));
        pnlCards.add(createStatCard("Đã Đặt Trước",   lblPhongDat,    new Color(255, 243, 224), new Color(250, 140, 22),  "🟧"));
        pnlCards.add(createStatCard("Cần Dọn Phòng",  lblPhongCanDon, new Color(255, 232, 230), new Color(220, 53, 69),   "🟥"));

        add(pnlCards, BorderLayout.NORTH);

        // ── Row 2: Table (left) + Info panel (right) ──
        JPanel pnlCenter = new JPanel(new BorderLayout(16, 0));
        pnlCenter.setBackground(new Color(245, 246, 250));

        // Left: bảng check-in hôm nay
        JPanel pnlTable = new JPanel(new BorderLayout());
        pnlTable.setBackground(Color.WHITE);
        pnlTable.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            new EmptyBorder(0, 0, 0, 0)));

        JPanel pnlTableHeader = new JPanel(new BorderLayout());
        pnlTableHeader.setBackground(Color.WHITE);
        pnlTableHeader.setBorder(new EmptyBorder(14, 20, 14, 20));
        JLabel lblTableTitle = new JLabel("Check-in hôm nay");
        lblTableTitle.setFont(new Font("Arial", Font.BOLD, 15));
        lblTableTitle.setForeground(new Color(30, 40, 55));
        JButton btnRefresh = new JButton("↻ Làm mới");
        btnRefresh.setFont(new Font("Arial", Font.PLAIN, 12));
        btnRefresh.setForeground(new Color(24, 144, 255));
        btnRefresh.setBackground(Color.WHITE);
        btnRefresh.setBorder(BorderFactory.createLineBorder(new Color(24, 144, 255)));
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> loadData());
        pnlTableHeader.add(lblTableTitle, BorderLayout.WEST);
        pnlTableHeader.add(btnRefresh, BorderLayout.EAST);
        pnlTable.add(pnlTableHeader, BorderLayout.NORTH);

        String[] cols = {"Mã Phiếu", "Khách Hàng", "Phòng", "Nhận dự kiến", "Trả dự kiến", "Trạng thái"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(38);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(240, 240, 240));
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setForeground(new Color(50, 60, 70));
        table.setSelectionBackground(new Color(230, 244, 255));

        JTableHeader th = table.getTableHeader();
        th.setBackground(new Color(250, 251, 252));
        th.setForeground(new Color(130, 140, 155));
        th.setFont(new Font("Arial", Font.BOLD, 12));
        th.setPreferredSize(new Dimension(0, 38));
        ((DefaultTableCellRenderer) th.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);

        // Renderer màu cho cột trạng thái
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                String val = v == null ? "" : v.toString();
                lbl.setHorizontalAlignment(JLabel.CENTER);
                if (val.contains("Đang")) {
                    lbl.setForeground(new Color(40, 167, 69));
                } else if (val.contains("Đặt")) {
                    lbl.setForeground(new Color(250, 140, 22));
                } else {
                    lbl.setForeground(new Color(130, 140, 155));
                }
                return lbl;
            }
        });

        pnlTable.add(new JScrollPane(table), BorderLayout.CENTER);
        pnlCenter.add(pnlTable, BorderLayout.CENTER);

        // Right: quick stats panel
        JPanel pnlRight = new JPanel();
        pnlRight.setLayout(new BoxLayout(pnlRight, BoxLayout.Y_AXIS));
        pnlRight.setBackground(new Color(245, 246, 250));
        pnlRight.setPreferredSize(new Dimension(260, 0));

        lblDoanhThu    = new JLabel("...");
        lblKhachDangO  = new JLabel("...");
        lblCheckinHom  = new JLabel("...");
        lblCheckoutHom = new JLabel("...");

        pnlRight.add(createInfoCard("Doanh thu hôm nay",   lblDoanhThu,    new Color(24, 144, 255)));
        pnlRight.add(Box.createRigidArea(new Dimension(0, 14)));
        pnlRight.add(createInfoCard("Khách đang ở",        lblKhachDangO,  new Color(40, 167, 69)));
        pnlRight.add(Box.createRigidArea(new Dimension(0, 14)));
        pnlRight.add(createInfoCard("Check-in hôm nay",    lblCheckinHom,  new Color(250, 140, 22)));
        pnlRight.add(Box.createRigidArea(new Dimension(0, 14)));
        pnlRight.add(createInfoCard("Check-out hôm nay",   lblCheckoutHom, new Color(220, 53, 69)));

        pnlCenter.add(pnlRight, BorderLayout.EAST);
        add(pnlCenter, BorderLayout.CENTER);
    }

    private void loadData() {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return;
        try {
            // ── Stat cards ──
            ResultSet rs1 = con.prepareStatement(
                "SELECT trangThai, COUNT(*) AS so FROM Phong GROUP BY trangThai").executeQuery();
            int phongTrong = 0, phongDung = 0, phongDat = 0, phongCanDon = 0;
            while (rs1.next()) {
                switch (rs1.getString("trangThai")) {
                    case "PhongTrong"      -> phongTrong  = rs1.getInt("so");
                    case "PhongDangSuDung" -> phongDung   = rs1.getInt("so");
                    case "PhongDat"        -> phongDat    = rs1.getInt("so");
                    case "PhongCanDon"     -> phongCanDon = rs1.getInt("so");
                }
            }
            lblPhongTrong.setText(String.valueOf(phongTrong));
            lblPhongDung.setText(String.valueOf(phongDung));
            lblPhongDat.setText(String.valueOf(phongDat));
            lblPhongCanDon.setText(String.valueOf(phongCanDon));

            // ── Doanh thu hôm nay ──
            ResultSet rs2 = con.prepareStatement(
                "SELECT ISNULL(SUM(tienThanhToan),0) AS dt FROM HoaDon WHERE CAST(ngayThanhToan AS DATE)=CAST(GETDATE() AS DATE)").executeQuery();
            if (rs2.next()) {
                double dt = rs2.getDouble("dt");
                lblDoanhThu.setText(NumberFormat.getNumberInstance(new Locale("vi","VN")).format(dt) + " ₫");
            }

            // ── Khách đang ở ──
            ResultSet rs3 = con.prepareStatement(
                "SELECT COUNT(*) AS so FROM PhieuDatPhong WHERE thoiGianNhanThucTe IS NOT NULL AND thoiGianTraThucTe IS NULL").executeQuery();
            if (rs3.next()) lblKhachDangO.setText(String.valueOf(rs3.getInt("so")));

            // ── Check-in / Check-out hôm nay ──
            ResultSet rs4 = con.prepareStatement(
                "SELECT COUNT(*) AS so FROM PhieuDatPhong WHERE CAST(thoiGianNhanThucTe AS DATE)=CAST(GETDATE() AS DATE)").executeQuery();
            if (rs4.next()) lblCheckinHom.setText(String.valueOf(rs4.getInt("so")));

            ResultSet rs5 = con.prepareStatement(
                "SELECT COUNT(*) AS so FROM PhieuDatPhong WHERE CAST(thoiGianTraThucTe AS DATE)=CAST(GETDATE() AS DATE)").executeQuery();
            if (rs5.next()) lblCheckoutHom.setText(String.valueOf(rs5.getInt("so")));

            // ── Bảng check-in hôm nay ──
            tableModel.setRowCount(0);
            ResultSet rs6 = con.prepareStatement(
                "SELECT p.maPhieuDatPhong, kh.hoTenKH, ph.tenPhong, " +
                "       FORMAT(p.thoiGianNhanDuKien,'dd/MM/yyyy HH:mm'), " +
                "       FORMAT(p.thoiGianTraDuKien,'dd/MM/yyyy HH:mm'), " +
                "       ph.trangThai " +
                "FROM PhieuDatPhong p " +
                "JOIN KhachHang kh ON p.maKhachHang = kh.maKH " +
                "JOIN ChiTietPhieuDatPhong ct ON p.maPhieuDatPhong = ct.maPhieuDatPhong " +
                "JOIN Phong ph ON ct.maPhong = ph.maPhong " +
                "WHERE CAST(p.thoiGianNhanDuKien AS DATE) = CAST(GETDATE() AS DATE) " +
                "ORDER BY p.thoiGianNhanDuKien").executeQuery();
            while (rs6.next()) {
                String trangThai = switch (rs6.getString(6)) {
                    case "PhongDangSuDung" -> "Đang sử dụng";
                    case "PhongDat"        -> "Đã đặt";
                    case "PhongTrong"      -> "Phòng trống";
                    default                -> rs6.getString(6);
                };
                tableModel.addRow(new Object[]{
                    rs6.getString(1), rs6.getString(2), rs6.getString(3),
                    rs6.getString(4), rs6.getString(5), trangThai
                });
            }

        } catch (Exception e) { e.printStackTrace(); }
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color bg, Color accent, String icon) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(bg);
        panel.putClientProperty("FlatLaf.style", "arc: 14");
        panel.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        lblIcon.setVerticalAlignment(JLabel.CENTER);

        JPanel pnlText = new JPanel();
        pnlText.setLayout(new BoxLayout(pnlText, BoxLayout.Y_AXIS));
        pnlText.setBackground(bg);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Arial", Font.PLAIN, 12));
        lblTitle.setForeground(new Color(100, 110, 125));

        valueLabel.setFont(new Font("Arial", Font.BOLD, 36));
        valueLabel.setForeground(accent);

        pnlText.add(lblTitle);
        pnlText.add(Box.createVerticalStrut(4));
        pnlText.add(valueLabel);

        panel.add(lblIcon, BorderLayout.EAST);
        panel.add(pnlText, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createInfoCard(String title, JLabel valueLabel, Color accent) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.putClientProperty("FlatLaf.style", "arc: 14");
        panel.setBorder(new EmptyBorder(16, 18, 16, 18));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Arial", Font.PLAIN, 12));
        lblTitle.setForeground(new Color(130, 140, 155));

        valueLabel.setFont(new Font("Arial", Font.BOLD, 26));
        valueLabel.setForeground(accent);

        JPanel left = new JPanel(new BorderLayout(0, 4));
        left.setBackground(Color.WHITE);
        left.add(lblTitle, BorderLayout.NORTH);
        left.add(valueLabel, BorderLayout.CENTER);

        JPanel accent_bar = new JPanel();
        accent_bar.setPreferredSize(new Dimension(4, 0));
        accent_bar.setBackground(accent);

        panel.add(accent_bar, BorderLayout.WEST);
        panel.add(left, BorderLayout.CENTER);
        return panel;
    }
}
