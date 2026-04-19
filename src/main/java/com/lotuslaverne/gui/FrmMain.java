package com.lotuslaverne.gui;

import com.lotuslaverne.entity.TaiKhoan;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
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

        // --- Sidebar (scrollable) ---
        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(11, 26, 44));

        JLabel lblLogo = new JLabel("<html><h1 style='color:#1890ff; font-family:Arial; font-size:26px; margin:0;'>Lotus<span style='color:#0dcaf0;'>Larverne</span></h1></html>");
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblLogo.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        sidebar.add(lblLogo);

        boolean isQuanLy = tkActive != null && "QuanLy".equals(tkActive.getVaiTro());

        // Trang chủ (standalone)
        JButton btnTrangChu = createMenuButton("Trang chủ", true, "home");
        currentActiveButton = btnTrangChu;
        sidebar.add(btnTrangChu);
        sidebar.add(Box.createRigidArea(new Dimension(0, 4)));

        // --- Nhóm dropdown (phân quyền) ---
        sidebar.add(createMenuGroup("Quản lý Phòng", "room",
                new String[]{"Danh sách Phòng", "Bảng Giá Phòng", "Quản lý Dịch Vụ"},
                new String[]{"Phong", "BangGia", "DichVu"}));
        sidebar.add(Box.createRigidArea(new Dimension(0, 4)));

        sidebar.add(createMenuGroup("Lưu trú", "stay",
                new String[]{"Đặt Phòng", "Check-in Khách", "Đổi Phòng", "Check-out / Thanh toán"},
                new String[]{"DatPhong", "CheckIn", "DoiPhong", "ThanhToan"}));
        sidebar.add(Box.createRigidArea(new Dimension(0, 4)));

        // Chỉ QuanLy mới thấy Tài chính
        if (isQuanLy) {
            sidebar.add(createMenuGroup("Tài chính", "finance",
                    new String[]{"Quản lý Hóa Đơn", "Khuyến Mãi"},
                    new String[]{"HoaDon", "KhuyenMai"}));
            sidebar.add(Box.createRigidArea(new Dimension(0, 4)));
        }

        // LeTan chỉ thấy Khách Hàng, QuanLy thấy cả Nhân Viên
        if (isQuanLy) {
            sidebar.add(createMenuGroup("Khách hàng & Nhân viên", "people",
                    new String[]{"Quản lý Khách Hàng", "Quản lý Nhân Viên"},
                    new String[]{"KhachHang", "NhanVien"}));
        } else {
            sidebar.add(createMenuGroup("Khách hàng", "people",
                    new String[]{"Quản lý Khách Hàng"},
                    new String[]{"KhachHang"}));
        }
        sidebar.add(Box.createRigidArea(new Dimension(0, 4)));

        // Chỉ QuanLy mới thấy Hệ thống
        if (isQuanLy) {
            sidebar.add(createMenuGroup("Hệ thống", "system",
                    new String[]{"Báo Cáo Thống Kê", "Tài Khoản Hệ Thống"},
                    new String[]{"ThongKe", "TaiKhoan"}));
        }

        sidebar.add(Box.createVerticalGlue());

        JButton btnLogout = createMenuButton("Đăng xuất", false, "logout");
        sidebar.add(btnLogout);
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));

        JScrollPane sidebarScroll = new JScrollPane(sidebar);
        sidebarScroll.setPreferredSize(new Dimension(248, getHeight()));
        sidebarScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sidebarScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sidebarScroll.setBorder(null);
        sidebarScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(sidebarScroll, BorderLayout.WEST);
        final JScrollPane finalSidebarScroll = sidebarScroll;

        // --- Top Header ---
        JPanel rightWrapper = new JPanel(new BorderLayout());
        rightWrapper.setBackground(new Color(245, 246, 250));

        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(Color.WHITE);
        topHeader.setPreferredSize(new Dimension(0, 60));
        topHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        JLabel lblMenuIcon = new JLabel("  \u2630  ");
        lblMenuIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 22));
        lblMenuIcon.setForeground(Color.DARK_GRAY);
        lblMenuIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblMenuIcon.setToolTipText("Ẩn/hiện thanh menu");
        lblMenuIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                boolean visible = finalSidebarScroll.isVisible();
                finalSidebarScroll.setVisible(!visible);
                revalidate();
                repaint();
            }
            public void mouseEntered(java.awt.event.MouseEvent e) {
                lblMenuIcon.setForeground(new Color(24, 144, 255));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                lblMenuIcon.setForeground(Color.DARK_GRAY);
            }
        });
        topHeader.add(lblMenuIcon, BorderLayout.WEST);

        JPanel pnlUser = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        pnlUser.setBackground(Color.WHITE);
        JLabel lblPhone = new JLabel("<html><font face='Segoe UI Symbol'>\u260E</font> 024 35 683727</html>");
        lblPhone.setForeground(new Color(220, 53, 69));
        lblPhone.setFont(new Font("Arial", Font.BOLD, 14));
        lblPhone.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblPhone.setToolTipText("Click để sao chép số điện thoại");
        lblPhone.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new StringSelection("024 35 683727"), null);
                JOptionPane.showMessageDialog(null, "Đã sao chép số điện thoại: 024 35 683727", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JLabel lblHelp = new JLabel("<html><font face='Segoe UI Symbol'>\u2753</font> Trợ giúp</html>");
        lblHelp.setForeground(Color.GRAY);
        lblHelp.setFont(new Font("Arial", Font.PLAIN, 14));
        lblHelp.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblHelp.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                JOptionPane.showMessageDialog(null,
                        "<html><b>Hướng dẫn sử dụng</b><br><br>"
                        + "- <b>Quản Lý</b>: Toàn quyền truy cập hệ thống<br>"
                        + "- <b>Lễ Tân</b>: Đặt phòng, check-in/out, khách hàng<br><br>"
                        + "Hotline hỗ trợ: <b>024 35 683727</b><br>"
                        + "Email: support@lotuslaverne.vn</html>",
                        "Trợ giúp", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        String vaiTro = tkActive != null ? tkActive.getVaiTro() : "";
        Color badgeColor = "QuanLy".equals(vaiTro) ? new Color(24, 144, 255) : new Color(40, 167, 69);
        String badgeText = "QuanLy".equals(vaiTro) ? "Quản Lý" : "Lễ Tân";

        JLabel lblBadge = new JLabel("  " + badgeText + "  ");
        lblBadge.setFont(new Font("Arial", Font.BOLD, 11));
        lblBadge.setForeground(Color.WHITE);
        lblBadge.setBackground(badgeColor);
        lblBadge.setOpaque(true);
        lblBadge.putClientProperty("FlatLaf.style", "arc: 8");

        JLabel lblUser = new JLabel("<html><font face='Segoe UI Symbol' color='#1890ff'>\u25CF</font> " + (tkActive != null ? tkActive.getTenDangNhap().toUpperCase() : "ADMIN") + "</html>");
        lblUser.setFont(new Font("Arial", Font.BOLD, 14));
        lblUser.setForeground(new Color(44, 62, 80));
        pnlUser.add(lblBadge);
        pnlUser.add(lblPhone);
        pnlUser.add(lblHelp);
        pnlUser.add(lblUser);
        topHeader.add(pnlUser, BorderLayout.EAST);
        rightWrapper.add(topHeader, BorderLayout.NORTH);

        // --- Content Area (CardLayout) ---
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(new Color(245, 246, 250));

        mainContentPanel.add(new FrmTrangChu(), "TrangChu");
        mainContentPanel.add(new FrmPhong(), "Phong");
        try { mainContentPanel.add(new FrmDatPhong(), "DatPhong"); } catch (Exception e) {}
        mainContentPanel.add(new FrmKhachHang(), "KhachHang");
        mainContentPanel.add(new FrmDichVu(), "DichVu");
        try { mainContentPanel.add(new FrmThanhToan(), "ThanhToan"); } catch (Exception e) {}
        try { mainContentPanel.add(new FrmNhanVien(), "NhanVien"); } catch (Exception e) {}
        try { mainContentPanel.add(new FrmThongKe(), "ThongKe"); } catch (Exception e) {}
        try { mainContentPanel.add(new FrmCheckIn(), "CheckIn"); } catch (Exception e) {}
        try { mainContentPanel.add(new FrmDoiPhong(), "DoiPhong"); } catch (Exception e) {}
        try { mainContentPanel.add(new FrmHoaDon(), "HoaDon"); } catch (Exception e) {}
        try { mainContentPanel.add(new FrmKhuyenMai(), "KhuyenMai"); } catch (Exception e) {}
        try { mainContentPanel.add(new FrmBangGia(), "BangGia"); } catch (Exception e) {}
        try { mainContentPanel.add(new FrmTaiKhoan(tkActive), "TaiKhoan"); } catch (Exception e) {}

        rightWrapper.add(mainContentPanel, BorderLayout.CENTER);
        add(rightWrapper, BorderLayout.CENTER);

        // Actions
        setupMenuAction(btnTrangChu, "TrangChu");
        btnLogout.addActionListener((ActionEvent e) -> {
            this.dispose();
            new FrmLogin().setVisible(true);
        });
    }

    private String arrowText(String title, boolean open) {
        // Dùng "Segoe UI Symbol" vì Arial không có glyph ▶/▼ → hiện thành □
        String arrow = open ? "▼" : "▶";
        return "<html><table width='155'><tr>"
            + "<td>" + title + "</td>"
            + "<td align='right'><font face='Segoe UI Symbol' color='#8ab4d4'>" + arrow + "</font></td>"
            + "</tr></table></html>";
    }

    private Icon loadIcon(String name, int size) {
        try {
            FlatSVGIcon icon = new FlatSVGIcon("icons/" + name + ".svg", size, size);
            icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> new Color(180, 200, 220)));
            return icon;
        } catch (Exception e) {
            return null;
        }
    }

    /** Tạo panel nhóm accordion có thể mở/đóng */
    private JPanel createMenuGroup(String headerTitle, String iconName, String[] subTitles, String[] cardNames) {
        JPanel groupPanel = new JPanel();
        groupPanel.setLayout(new BoxLayout(groupPanel, BoxLayout.Y_AXIS));
        groupPanel.setBackground(new Color(11, 26, 44));
        groupPanel.setMaximumSize(new Dimension(240, 45));
        groupPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnHeader = createMenuButton(arrowText(headerTitle, false), false, iconName);

        JPanel subPanel = new JPanel();
        subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.Y_AXIS));
        subPanel.setBackground(new Color(11, 26, 44));
        subPanel.setVisible(false);

        for (int i = 0; i < subTitles.length; i++) {
            JButton btn = createSubMenuButton(subTitles[i]);
            setupMenuAction(btn, cardNames[i]);
            subPanel.add(btn);
            subPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        }

        int subHeight = subTitles.length * (38 + 3);
        btnHeader.addActionListener(e -> {
            boolean wasOpen = subPanel.isVisible();
            subPanel.setVisible(!wasOpen);
            btnHeader.setText(arrowText(headerTitle, !wasOpen));
            groupPanel.setMaximumSize(new Dimension(240, wasOpen ? 45 : 45 + subHeight));
            sidebar.revalidate();
            sidebar.repaint();
        });

        groupPanel.add(btnHeader);
        groupPanel.add(subPanel);
        return groupPanel;
    }

    private JButton createMenuButton(String text, boolean isActive, String iconName) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(220, 45));
        btn.setPreferredSize(new Dimension(220, 45));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.setFont(new Font("Arial", isActive ? Font.BOLD : Font.PLAIN, 14));
        btn.setForeground(isActive ? Color.WHITE : new Color(180, 190, 200));
        btn.setBackground(isActive ? new Color(24, 144, 255) : new Color(11, 26, 44));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        if (iconName != null) {
            Icon icon = loadIcon(iconName, 18);
            if (icon != null) {
                btn.setIcon(icon);
                btn.setIconTextGap(8);
            }
        }
        return btn;
    }

    private JButton createSubMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(220, 38));
        btn.setPreferredSize(new Dimension(220, 38));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.putClientProperty("subMenu", Boolean.TRUE);
        btn.setFont(new Font("Arial", Font.PLAIN, 13));
        btn.setForeground(new Color(150, 168, 185));
        btn.setBackground(new Color(16, 34, 57));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 42, 0, 0));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }

    private void setupMenuAction(JButton btn, String cardName) {
        btn.addActionListener(e -> {
            if (currentActiveButton != null) {
                resetButtonStyle(currentActiveButton);
            }
            btn.setBackground(new Color(24, 144, 255));
            btn.setForeground(Color.WHITE);
            boolean isSub = Boolean.TRUE.equals(btn.getClientProperty("subMenu"));
            btn.setFont(new Font("Arial", Font.BOLD, isSub ? 13 : 14));
            currentActiveButton = btn;
            cardLayout.show(mainContentPanel, cardName);
        });
    }

    private void resetButtonStyle(JButton btn) {
        boolean isSub = Boolean.TRUE.equals(btn.getClientProperty("subMenu"));
        btn.setBackground(isSub ? new Color(16, 34, 57) : new Color(11, 26, 44));
        btn.setForeground(isSub ? new Color(150, 168, 185) : new Color(180, 190, 200));
        btn.setFont(new Font("Arial", Font.PLAIN, isSub ? 13 : 14));
    }
}
