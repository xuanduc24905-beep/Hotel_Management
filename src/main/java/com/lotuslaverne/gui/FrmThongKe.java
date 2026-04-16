package com.lotuslaverne.gui;

import com.lotuslaverne.dao.ThongKeDAO;
import com.lotuslaverne.util.UIFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.DecimalFormat;

public class FrmThongKe extends JPanel {

    private final ThongKeDAO dao = new ThongKeDAO();
    private JPanel cardStatPanel;

    public FrmThongKe() {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 20));
        UIFactory.styleMainPanel(this);

        JLabel lblTitle = new JLabel("BẢNG ĐIỀU KHIỂN & BÁO CÁO DOANH THU (LIVE-SYNC)", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 26));
        lblTitle.setForeground(new Color(44, 62, 80));
        add(lblTitle, BorderLayout.NORTH);

        // Fetch Live Data
        int phongCoKhach = dao.demSoPhongTheoTrangThai("Đang Thuê");
        int phongTrong = dao.demSoPhongTheoTrangThai("Trống (Chưa Dọn)");
        int nhanSu = dao.demTongNhanSu();
        double doanhThu = dao.layDoanhThuHomNay();
        
        DecimalFormat df = new DecimalFormat("#,###.## VNĐ");

        // Grid Thống kê dạng Card
        cardStatPanel = new JPanel(new GridLayout(1, 4, 15, 15));
        cardStatPanel.add(createStatCard("Phòng Có Khách", phongCoKhach + " Phòng", new Color(41, 128, 185)));
        cardStatPanel.add(createStatCard("Phòng Đang Trống", phongTrong + " Phòng", new Color(39, 174, 96)));
        cardStatPanel.add(createStatCard("Nhân Sự Đang Xài", nhanSu + " người", new Color(142, 68, 173)));
        cardStatPanel.add(createStatCard("Doanh Thu Hôm Nay", df.format(doanhThu), new Color(231, 76, 60)));

        // Khu vực dưới (Biểu đồ Data)
        JPanel chartArea = new JPanel(new BorderLayout());
        UIFactory.styleFormPanel(chartArea);
        chartArea.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)), 
                "Mô phỏng Biểu Đồ Tăng Trưởng Doanh Thu Khách Sạn",
                0, 0, new Font("Arial", Font.BOLD, 12), new Color(100, 100, 100)
        ));
        JLabel lblChartPlot = new JLabel("<html><center><h2>Hệ thống Thống kê SQL đã Live Data đếm tiền thành công!</h2><br><i>(Doanh thu và trạng thái phòng đã móc thẳng từ CSDL thực tế. Bạn có thể Test bằng cách Thêm phiếu Checkout, con số Doanh thu này sẽ nảy số tự động!)</i></center></html>", SwingConstants.CENTER);
        lblChartPlot.setOpaque(true);
        lblChartPlot.setBackground(new Color(236, 240, 241));
        chartArea.add(lblChartPlot, BorderLayout.CENTER);

        // Nút Reload nhanh
        JButton btnReload = UIFactory.createActionButton("Tải Lại Giỏ Thống Kê Mới Nhất", new Color(24, 144, 255), Color.WHITE);
        btnReload.setPreferredSize(new Dimension(300, 45));
        btnReload.addActionListener(e -> {
            removeAll();
            initUI();
            revalidate();
            repaint();
            JOptionPane.showMessageDialog(this, "Đã quét lại dữ liệu SQL mới nhất!");
        });
        chartArea.add(btnReload, BorderLayout.SOUTH);

        add(cardStatPanel, BorderLayout.CENTER);
        add(chartArea, BorderLayout.SOUTH);
        chartArea.setPreferredSize(new Dimension(800, 350));
    }

    private JPanel createStatCard(String title, String data, Color color) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(color);
        panel.putClientProperty("FlatLaf.style", "arc: 15"); 
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Arial", Font.PLAIN, 14));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblData = new JLabel(data, SwingConstants.CENTER);
        lblData.setForeground(Color.WHITE);
        lblData.setFont(new Font("Arial", Font.BOLD, 32));
        lblData.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(lblTitle);
        panel.add(Box.createVerticalStrut(10));
        panel.add(lblData);
        return panel;
    }
}
