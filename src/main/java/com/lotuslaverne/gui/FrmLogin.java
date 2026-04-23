package com.lotuslaverne.gui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.lotuslaverne.dao.TaiKhoanDAO;
import com.lotuslaverne.entity.TaiKhoan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

public class FrmLogin extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JComboBox<String> cbVaiTro;
    private JButton btnLogin;

    public FrmLogin() {
        // Thiết lập Giao diện FlatLaf hiện đại
        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }
        
        setTitle("Đăng nhập - Lotus Laverne Hotel");
        setSize(950, 550);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        mainPanel.setBackground(Color.WHITE);
        setContentPane(mainPanel);

        // --- Cột Trái (Hình ảnh minh hoạ Isometric) ---
        JPanel leftPanel = new JPanel() {
            Image img;
            {
                try {
                    java.net.URL url = getClass().getResource("/images/bg_login.png");
                    if(url != null) {
                        img = new ImageIcon(url).getImage();
                    } else {
                        img = new ImageIcon("src/main/resources/images/bg_login.png").getImage();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                
                if (img != null && img.getWidth(null) > 0) {
                    // Vẽ ảnh phủ kín màn hình trái
                    g2d.drawImage(img, 0, 0, getWidth(), getHeight(), this);
                } else {
                    // Fallback gradient tĩnh nếu không load được ảnh
                    GradientPaint gp = new GradientPaint(0, 0, new Color(42, 196, 234), getWidth(), getHeight(), new Color(24, 144, 255));
                    g2d.setPaint(gp);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Arial", Font.BOLD, 30));
                    g2d.drawString("HOTEL RECEPTION", 50, getHeight() / 2);
                }
                g2d.dispose();
            }
        };
        mainPanel.add(leftPanel);

        // --- Cột Phải (Form đăng nhập) ---
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(20, 40, 20, 40));
        
        // Logo LotusLarverne
        JLabel lblLogo = new JLabel("<html><h1 style='color:#1890ff; font-family:Arial, sans-serif; font-size:38px; margin:0;'><span style='font-size:38px;'>Lotus</span><span style='color:#0dcaf0;'>Larverne</span></h1></html>");
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(lblLogo);
        formPanel.add(Box.createVerticalStrut(10));

        // Subtitle
        JLabel lblSub = new JLabel("Hệ thống quản lý khách sạn trên nền tảng điện toán đám mây");
        lblSub.setFont(new Font("Arial", Font.PLAIN, 12));
        lblSub.setForeground(new Color(150, 150, 150));
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(lblSub);
        formPanel.add(Box.createVerticalStrut(40));

        // Tên đăng nhập
        txtUsername = new JTextField("admin");
        styleTextField(txtUsername, "Tên đăng nhập", "👤 ");
        formPanel.add(txtUsername);
        formPanel.add(Box.createVerticalStrut(20));

        // Mật khẩu
        txtPassword = new JPasswordField("123456");
        styleTextField(txtPassword, "Mật khẩu", "🔒 ");
        formPanel.add(txtPassword);
        formPanel.add(Box.createVerticalStrut(20));

        // Vai trò
        cbVaiTro = new JComboBox<>(new String[]{"Lễ Tân", "Quản Lý"});
        cbVaiTro.setMaximumSize(new Dimension(320, 45));
        cbVaiTro.setPreferredSize(new Dimension(320, 45));
        cbVaiTro.setFont(new Font("Arial", Font.PLAIN, 14));
        cbVaiTro.putClientProperty("JComboBox.buttonType", "roundRect");
        cbVaiTro.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel lblVaiTro = new JLabel("Đăng nhập với vai trò:");
        lblVaiTro.setFont(new Font("Arial", Font.PLAIN, 12));
        lblVaiTro.setForeground(new Color(100, 100, 100));
        lblVaiTro.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(lblVaiTro);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(cbVaiTro);
        formPanel.add(Box.createVerticalStrut(20));

        // Nút Đăng nhập
        btnLogin = new JButton("Đăng nhập");
        btnLogin.setFont(new Font("Arial", Font.BOLD, 15));
        btnLogin.setBackground(new Color(24, 144, 255)); 
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.putClientProperty("JButton.buttonType", "roundRect");
        btnLogin.setMaximumSize(new Dimension(320, 45));
        btnLogin.setPreferredSize(new Dimension(320, 45));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect bị bỏ đi để Swing và FlatLaf tự xử lý hiệu ứng Click chuẩn xác hơn, 
        // vì custom MouseListener có thể lỗi hiển thị chìm nút.
        
        btnLogin.addActionListener(e -> xulyDangNhap());
        // Cho phím Enter tự động ăn vào nút Đăng nhập
        SwingUtilities.invokeLater(() -> getRootPane().setDefaultButton(btnLogin));
        
        formPanel.add(btnLogin);
        formPanel.add(Box.createVerticalStrut(20));
        
        // Quên mật khẩu
        JLabel lblForgot = new JLabel("<html><u>Quên mật khẩu?</u></html>");
        lblForgot.setFont(new Font("Arial", Font.PLAIN, 13));
        lblForgot.setForeground(new Color(120, 120, 120));
        lblForgot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblForgot.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        lblForgot.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { lblForgot.setForeground(new Color(24, 144, 255)); }
            public void mouseExited(MouseEvent e) { lblForgot.setForeground(new Color(120, 120, 120)); }
            public void mouseClicked(MouseEvent e) { 
                JOptionPane.showMessageDialog(FrmLogin.this, "Tính năng lấy lại mật khẩu đang được phát triển!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        formPanel.add(lblForgot);

        rightPanel.add(formPanel);
        mainPanel.add(rightPanel);
    }
    
    private void styleTextField(JTextField tf, String placeholder, String prefixChar) {
        tf.setMaximumSize(new Dimension(320, 45));
        tf.setPreferredSize(new Dimension(320, 45));
        tf.setFont(new Font("Arial", Font.PLAIN, 14));
        tf.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        tf.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        tf.putClientProperty("JComponent.roundRect", true);
        tf.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Khởi tạo Prefix (Text Icon)
        JLabel iconLbl = new JLabel(prefixChar);
        iconLbl.setForeground(new Color(150,150,150));
        iconLbl.setBorder(new EmptyBorder(0, 5, 0, 0));
        tf.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_COMPONENT, iconLbl);
    }



    private void xulyDangNhap() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin Tên đăng nhập và Mật khẩu!");
            return;
        }

        // Map display text → DB value
        String vaiTroChon = cbVaiTro.getSelectedIndex() == 0 ? "LeTan" : "QuanLy";

        TaiKhoanDAO dao = new TaiKhoanDAO();
        TaiKhoan tk = dao.checkLogin(username, password);

        if (tk == null) {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập hoặc mật khẩu không đúng!", "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!vaiTroChon.equals(tk.getVaiTro())) {
            String tenVaiTroChon = cbVaiTro.getSelectedItem().toString();
            JOptionPane.showMessageDialog(this,
                    "Tài khoản này không có quyền \"" + tenVaiTroChon + "\"!\nVui lòng chọn đúng vai trò.",
                    "Không đủ quyền", JOptionPane.WARNING_MESSAGE);
            return;
        }

        this.dispose();
        new FrmMain(tk).setVisible(true);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FrmLogin().setVisible(true));
    }
}
