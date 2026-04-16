package com.lotuslaverne.gui;

import com.lotuslaverne.dao.ThongKeDAO;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

public class FrmThongKe extends JPanel {

    private final ThongKeDAO dao = new ThongKeDAO();
    private JPanel cardStatPanel;

    public FrmThongKe() {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("BẢNG ĐIỀU KHIỂN & BÁO CÁO DOANH THU (LIVE-SYNC)", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 26));
        lblTitle.setForeground(new Color(211, 84, 0));
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
        chartArea.setBorder(BorderFactory.createTitledBorder("Mô phỏng Biểu Đồ Tăng Trưởng Doanh Thu Khách Sạn"));
        JLabel lblChartPlot = new JLabel("<html><center><h2>Hệ thống Thống kê SQL đã Live Data đếm tiền thành công!</h2><br><i>(Doanh thu và trạng thái phòng đã móc thẳng từ CSDL thực tế. Bạn có thể Test bằng cách Thêm phiếu Checkout, con số Doanh thu này sẽ nảy số tự động!)</i></center></html>", SwingConstants.CENTER);
        lblChartPlot.setOpaque(true);
        lblChartPlot.setBackground(new Color(236, 240, 241));
        chartArea.add(lblChartPlot, BorderLayout.CENTER);

        // Nút Reload nhanh
        JButton btnReload = new JButton("Tải Lại Giỏ Thống Kê Mới Nhất");
        btnReload.setFont(new Font("Arial", Font.BOLD, 18));
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
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.setBackground(color);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        
        JLabel lblData = new JLabel(data, SwingConstants.CENTER);
        lblData.setForeground(Color.WHITE);
        lblData.setFont(new Font("Arial", Font.BOLD, 24));

        panel.add(lblTitle);
        panel.add(lblData);
        return panel;
    }
}
