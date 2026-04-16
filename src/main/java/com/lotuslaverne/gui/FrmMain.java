package com.lotuslaverne.gui;

import com.lotuslaverne.entity.TaiKhoan;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class FrmMain extends JFrame {
    private TaiKhoan tkActive;

    private JPanel mainContentPanel;
    private CardLayout cardLayout;

    public FrmMain(TaiKhoan tk) {
        this.tkActive = tk;
        setTitle("Quản lý Khách Sạn - " + tk.getVaiTro());
        setSize(1024, 768);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Sidebar Navigation
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(200, getHeight()));
        sidebar.setBackground(new Color(44, 62, 80));

        JLabel lblLogo = new JLabel("LOTUS LAVERNE");
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setFont(new Font("Arial", Font.BOLD, 20));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblLogo.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        sidebar.add(lblLogo);

        // Buttons in sidebar
        JButton btnPhong = createMenuButton("Quản lý Phòng");
        JButton btnDatPhong = createMenuButton("Hệ thống Đặt Phòng");
        JButton btnKhachHang = createMenuButton("Quản lý Khách Hàng");
        JButton btnDichVu = createMenuButton("Quản lý Dịch Vụ");
        JButton btnThanhToan = createMenuButton("Thanh toán (Check-out)");
        JButton btnNhanVien = createMenuButton("Quản lý Nhân Viên");
        JButton btnThongKe = createMenuButton("Báo Cáo Thống Kê");
        JButton btnLogout = createMenuButton("Đăng xuất");

        // Gắn sự kiện chuyển đổi View
        btnPhong.addActionListener(e -> cardLayout.show(mainContentPanel, "Phong"));
        btnDatPhong.addActionListener(e -> cardLayout.show(mainContentPanel, "DatPhong"));
        btnKhachHang.addActionListener(e -> cardLayout.show(mainContentPanel, "KhachHang"));
        btnDichVu.addActionListener(e -> cardLayout.show(mainContentPanel, "DichVu"));
        btnThanhToan.addActionListener(e -> cardLayout.show(mainContentPanel, "ThanhToan"));
        btnNhanVien.addActionListener(e -> cardLayout.show(mainContentPanel, "NhanVien"));
        btnThongKe.addActionListener(e -> cardLayout.show(mainContentPanel, "ThongKe"));

        sidebar.add(btnPhong);
        sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        sidebar.add(btnDatPhong);
        sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        sidebar.add(btnKhachHang);
        sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        sidebar.add(btnDichVu);
        sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        sidebar.add(btnThanhToan);
        sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        sidebar.add(btnNhanVien);
        sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        sidebar.add(btnThongKe);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(btnLogout);

        btnLogout.addActionListener((ActionEvent e) -> {
            this.dispose();
            new FrmLogin().setVisible(true);
        });

        add(sidebar, BorderLayout.WEST);

        // Main Content Area
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);

        // Welcome Panel
        JPanel welcomePanel = new JPanel(new GridBagLayout());
        JLabel welcomeLabel = new JLabel("Chào mừng đến với Hệ thống Quản lý Khách sạn");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomePanel.add(welcomeLabel);

        mainContentPanel.add(welcomePanel, "Welcome");
        mainContentPanel.add(new FrmPhong(), "Phong"); // Thêm Module Quản lý Phòng
        mainContentPanel.add(new FrmDatPhong(), "DatPhong"); // Thêm Module Đặt phòng
        mainContentPanel.add(new FrmKhachHang(), "KhachHang"); // Tích hợp Form Khách Hàng
        mainContentPanel.add(new FrmDichVu(), "DichVu"); // Thêm Module Dịch vụ
        mainContentPanel.add(new FrmThanhToan(), "ThanhToan"); // Thêm Module Thanh Toán
        mainContentPanel.add(new FrmNhanVien(), "NhanVien"); // Thêm Module Nhân Việc
        mainContentPanel.add(new FrmThongKe(), "ThongKe"); // Thêm Module Báo cáo

        add(mainContentPanel, BorderLayout.CENTER);
    }

    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(200, 40));
        btn.setBackground(new Color(52, 73, 94));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.PLAIN, 16));
        btn.setBorderPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }
}
