package com.lotuslaverne.gui;

import com.lotuslaverne.entity.TaiKhoan;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class FrmMain extends JFrame {
    private TaiKhoan tkActive;
    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    private JPanel sidebar;
    private JButton currentActiveButton;

    public FrmMain(TaiKhoan tk) {
        this.tkActive = tk;
        setTitle("Phần mềm Quản lý Khách Sạn Lotus Laverne - " + tk.getVaiTro());
        setSize(1280, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // --- Sidebar ---
        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(240, getHeight()));
        sidebar.setBackground(new Color(11, 26, 44)); // Dark Navy background
        
        // Logo
        JLabel lblLogo = new JLabel("<html><h1 style='color:#1890ff; font-family:Arial; font-size:26px; margin:0;'>Lotus<span style='color:#0dcaf0;'>Larverne</span></h1></html>");
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblLogo.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));
        sidebar.add(lblLogo);

        // Buttons
        JButton btnTrangChu = createMenuButton("Trang chủ", true);
        currentActiveButton = btnTrangChu; // Mặc định chọn Trang chủ
        
        JButton btnPhong = createMenuButton("Quản lý Phòng", false);
        JButton btnDatPhong = createMenuButton("Hệ thống Đặt Phòng", false);
        JButton btnKhachHang = createMenuButton("Quản lý Khách Hàng", false);
        JButton btnDichVu = createMenuButton("Quản lý Dịch Vụ", false);
        JButton btnThanhToan = createMenuButton("Thanh toán (Check-out)", false);
        JButton btnNhanVien = createMenuButton("Quản lý Nhân Viên", false);
        JButton btnThongKe = createMenuButton("Báo Cáo Thống Kê", false);
        JButton btnLogout = createMenuButton("Đăng xuất", false);

        sidebar.add(btnTrangChu);
        sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
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

        add(sidebar, BorderLayout.WEST);

        // --- Main Area (Bên phải) ---
        JPanel rightWrapper = new JPanel(new BorderLayout());
        rightWrapper.setBackground(new Color(245, 246, 250));

        // Top Header
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(Color.WHITE);
        topHeader.setPreferredSize(new Dimension(0, 60));
        topHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        JLabel lblMenuIcon = new JLabel("  ≡  ");
        lblMenuIcon.setFont(new Font("Arial", Font.BOLD, 24));
        lblMenuIcon.setForeground(Color.DARK_GRAY);
        lblMenuIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        topHeader.add(lblMenuIcon, BorderLayout.WEST);

        JPanel pnlUser = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        pnlUser.setBackground(Color.WHITE);
        
        JLabel lblPhone = new JLabel("📞 024 35 683727");
        lblPhone.setForeground(new Color(220, 53, 69)); // Red color
        lblPhone.setFont(new Font("Arial", Font.BOLD, 14));
        
        JLabel lblHelp = new JLabel("❓ Trợ giúp");
        lblHelp.setForeground(Color.GRAY);
        lblHelp.setFont(new Font("Arial", Font.PLAIN, 14));
        
        // Avatar + Name giả lập
        JLabel lblUser = new JLabel("👨 " + (tkActive != null ? tkActive.getTenDangNhap().toUpperCase() : "ADMIN"));
        lblUser.setFont(new Font("Arial", Font.BOLD, 14));
        lblUser.setForeground(new Color(44, 62, 80));

        pnlUser.add(lblPhone);
        pnlUser.add(lblHelp);
        pnlUser.add(lblUser);
        topHeader.add(pnlUser, BorderLayout.EAST);

        rightWrapper.add(topHeader, BorderLayout.NORTH);

        // Content Area (CardLayout)
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(new Color(245, 246, 250));

        // Chèn các form
        mainContentPanel.add(new FrmTrangChu(), "TrangChu");
        mainContentPanel.add(new FrmPhong(), "Phong");
        try { mainContentPanel.add(new FrmDatPhong(), "DatPhong"); } catch(Exception e){}
        mainContentPanel.add(new FrmKhachHang(), "KhachHang");
        mainContentPanel.add(new FrmDichVu(), "DichVu");
        try { mainContentPanel.add(new FrmThanhToan(), "ThanhToan"); } catch(Exception e){}
        try { mainContentPanel.add(new FrmNhanVien(), "NhanVien"); } catch(Exception e){}
        try { mainContentPanel.add(new FrmThongKe(), "ThongKe"); } catch(Exception e){}

        rightWrapper.add(mainContentPanel, BorderLayout.CENTER);
        add(rightWrapper, BorderLayout.CENTER);

        // Xử lý sự kiện click menu
        setupMenuAction(btnTrangChu, "TrangChu");
        setupMenuAction(btnPhong, "Phong");
        setupMenuAction(btnDatPhong, "DatPhong");
        setupMenuAction(btnKhachHang, "KhachHang");
        setupMenuAction(btnDichVu, "DichVu");
        setupMenuAction(btnThanhToan, "ThanhToan");
        setupMenuAction(btnNhanVien, "NhanVien");
        setupMenuAction(btnThongKe, "ThongKe");

        btnLogout.addActionListener((ActionEvent e) -> {
            this.dispose();
            new FrmLogin().setVisible(true);
        });
    }

    private JButton createMenuButton(String text, boolean isActive) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(220, 45));
        btn.setPreferredSize(new Dimension(220, 45));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.setFont(new Font("Arial", isActive ? Font.BOLD : Font.PLAIN, 15));
        btn.setForeground(isActive ? Color.WHITE : new Color(180, 190, 200));
        btn.setBackground(isActive ? new Color(24, 144, 255) : new Color(11, 26, 44));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }

    private void setupMenuAction(JButton btn, String cardName) {
        btn.addActionListener(e -> {
            // Đổi style nút dang active thành bình thường
            if(currentActiveButton != null) {
                currentActiveButton.setBackground(new Color(11, 26, 44));
                currentActiveButton.setForeground(new Color(180, 190, 200));
                currentActiveButton.setFont(new Font("Arial", Font.PLAIN, 15));
            }
            // Đổi style nút được click thành active
            btn.setBackground(new Color(24, 144, 255));
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Arial", Font.BOLD, 15));
            currentActiveButton = btn;

            // Chuyển card
            cardLayout.show(mainContentPanel, cardName);
        });
    }
}
