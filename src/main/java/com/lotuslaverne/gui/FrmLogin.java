package com.lotuslaverne.gui;

import com.lotuslaverne.dao.TaiKhoanDAO;
import com.lotuslaverne.entity.TaiKhoan;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FrmLogin extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    public FrmLogin() {
        setTitle("Hệ thống Quản lý Khách sạn - Lotus Laverne");
        setSize(400, 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel panelMain = new JPanel(new BorderLayout(10, 10));
        panelMain.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JLabel lblTitle = new JLabel("ĐĂNG NHẬP HỆ THỐNG", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        panelMain.add(lblTitle, BorderLayout.NORTH);

        // Center form
        JPanel panelForm = new JPanel(new GridLayout(2, 2, 10, 20));
        panelForm.add(new JLabel("Tên đăng nhập:"));
        txtUsername = new JTextField("admin"); // Default mẫu
        panelForm.add(txtUsername);

        panelForm.add(new JLabel("Mật khẩu:"));
        txtPassword = new JPasswordField("123456");
        panelForm.add(txtPassword);
        panelMain.add(panelForm, BorderLayout.CENTER);

        // Bottom
        JPanel panelBottom = new JPanel();
        btnLogin = new JButton("Đăng Nhập");
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
        
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                xulyDangNhap();
            }
        });
        
        panelBottom.add(btnLogin);
        panelMain.add(panelBottom, BorderLayout.SOUTH);

        add(panelMain);
    }

    private void xulyDangNhap() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        if(username.isEmpty() || password.isEmpty()){
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        // Gọi DAO lấy CSDL
        TaiKhoanDAO dao = new TaiKhoanDAO();
        TaiKhoan tk = dao.checkLogin(username, password);

        if (tk != null) {
            JOptionPane.showMessageDialog(this, "Đăng nhập thành công! Xin chào " + tk.getTenDangNhap());
            // Mở thư mục chính
            this.dispose(); 
            new FrmMain(tk).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập hoặc mật khẩu không đúng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
