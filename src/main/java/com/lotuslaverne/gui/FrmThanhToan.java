package com.lotuslaverne.gui;

import com.lotuslaverne.dao.HoaDonDAO;
import com.lotuslaverne.dao.PhongDAO;
import com.lotuslaverne.entity.HoaDon;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Timestamp;
import java.util.UUID;

public class FrmThanhToan extends JPanel {

    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final PhongDAO phongDAO = new PhongDAO();
    private JTextField txtMaPhong;

    public FrmThanhToan() {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("HỆ THỐNG THANH TOÁN (CHECK-OUT)", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 26));
        lblTitle.setForeground(new Color(41, 128, 185));
        add(lblTitle, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 20, 0));

        // LEFT (Nhập liệu tính tiền)
        JPanel leftPanel = new JPanel(new GridLayout(8, 2, 10, 20));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Nhập thông tin tính xuất Bill"));
        
        leftPanel.add(new JLabel("Nhập Phòng cần trả:"));
        txtMaPhong = new JTextField("P101");
        leftPanel.add(txtMaPhong);

        leftPanel.add(new JLabel("Tổng số giờ lưu trú thật:"));
        JTextField txtSoGio = new JTextField("24");
        leftPanel.add(txtSoGio);

        leftPanel.add(new JLabel("Khuyến Mãi (%):"));
        JTextField txtKhuyenMai = new JTextField("10");
        leftPanel.add(txtKhuyenMai);

        leftPanel.add(new JLabel("===============")); leftPanel.add(new JLabel("==============="));
        leftPanel.add(new JLabel("TỔNG TIỀN PHÒNG (*):"));
        JTextField txtTienPhong = new JTextField("960,000 VNĐ");
        txtTienPhong.setEditable(false); leftPanel.add(txtTienPhong);

        mainPanel.add(leftPanel);

        // RIGHT panel
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Dịch vụ phát sinh (Ăn uống)"));
        String[] cols = {"Dịch vụ", "Số lượng", "Đơn giá", "Thành tiền"};
        JTable table = new JTable(new DefaultTableModel(cols, 4));
        table.setRowHeight(30);
        rightPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        mainPanel.add(rightPanel);

        add(mainPanel, BorderLayout.CENTER);

        // BOTTOM
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnThanhToan = new JButton("XUẤT HÓA ĐƠN & CLEAN TRẢ PHÒNG");
        btnThanhToan.setFont(new Font("Arial", Font.BOLD, 18));
        btnThanhToan.setPreferredSize(new Dimension(450, 50));
        btnThanhToan.setBackground(new Color(231, 76, 60));
        btnThanhToan.setForeground(Color.WHITE);

        btnThanhToan.addActionListener(e -> {
            String maHD = "HD" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            String maPh = txtMaPhong.getText();
            
            HoaDon hd = new HoaDon(maHD, "NV_CA1", "KH001", new Timestamp(System.currentTimeMillis()), 960000.0, "Thu tiền checkout");
            if (hoaDonDAO.taoHoaDon(hd)) {
                // Logic Reset Phòng dơ
                phongDAO.capNhatTrangThai(maPh, "Trống (Chưa Dọn)");
                JOptionPane.showMessageDialog(this, "✅ Lập hóa đơn và Thu tiền thành công!\nTrạng thái phòng chuyển về Trống Dọn Dẹp.");
            } else {
                JOptionPane.showMessageDialog(this, "Báo lỗi tạo HĐ. Kiểm tra Database.", "Lỗi HĐ", JOptionPane.ERROR_MESSAGE);
            }
        });

        bottomPanel.add(btnThanhToan);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}
