package com.lotuslaverne.gui;

import com.lotuslaverne.dao.TaiKhoanDAO;
import com.lotuslaverne.entity.TaiKhoan;
import com.lotuslaverne.util.UIFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FrmTaiKhoan extends JPanel {

    private final TaiKhoanDAO tkDAO = new TaiKhoanDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtMaTK, txtMaNV, txtTenDangNhap;
    private JComboBox<String> cbVaiTro;
    private TaiKhoan tkActive;

    public FrmTaiKhoan(TaiKhoan tkActive) {
        this.tkActive = tkActive;
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 20));
        UIFactory.styleMainPanel(this);

        JLabel lblTitle = new JLabel("QUẢN LÝ TÀI KHOẢN HỆ THỐNG (UC003/004/005)", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(new Color(44, 62, 80));
        add(lblTitle, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        mainPanel.setBackground(new Color(245, 246, 250));

        // LEFT: tạo tài khoản mới (UC004)
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setBackground(new Color(245, 246, 250));

        JPanel formTao = new JPanel(new GridLayout(5, 2, 10, 12));
        UIFactory.styleFormPanel(formTao);
        formTao.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                "Tạo tài khoản mới (UC004)", 0, 0, new Font("Arial", Font.BOLD, 12), new Color(100, 100, 100)));

        txtMaTK = new JTextField();
        txtMaNV = new JTextField();
        txtTenDangNhap = new JTextField();
        JTextField txtMatKhau = new JTextField();
        cbVaiTro = new JComboBox<>(new String[]{"LeTan", "QuanLy"});

        formTao.add(new JLabel("Mã tài khoản:")); formTao.add(txtMaTK);
        formTao.add(new JLabel("Mã nhân viên:")); formTao.add(txtMaNV);
        formTao.add(new JLabel("Tên đăng nhập:")); formTao.add(txtTenDangNhap);
        formTao.add(new JLabel("Mật khẩu:")); formTao.add(txtMatKhau);
        formTao.add(new JLabel("Vai trò:")); formTao.add(cbVaiTro);

        JButton btnTao = UIFactory.createActionButton("Tạo tài khoản", new Color(40, 167, 69), Color.WHITE);
        btnTao.addActionListener(e -> {
            String maTK = txtMaTK.getText().trim();
            if (maTK.isEmpty()) maTK = "TK" + System.currentTimeMillis() % 100000;
            TaiKhoan tk = new TaiKhoan(maTK, txtMaNV.getText().trim(),
                    cbVaiTro.getSelectedItem().toString(), txtTenDangNhap.getText().trim(), txtMatKhau.getText().trim());
            if (tk.getTenDangNhap().isEmpty() || txtMatKhau.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tên đăng nhập và mật khẩu không được trống!"); return;
            }
            if (tkDAO.taoTaiKhoan(tk)) {
                JOptionPane.showMessageDialog(this, "✅ Tạo tài khoản thành công!");
                loadData();
                txtMaTK.setText(""); txtMaNV.setText(""); txtTenDangNhap.setText(""); txtMatKhau.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "❌ Lỗi! Mã hoặc tên đăng nhập đã tồn tại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel pnBtnTao = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnBtnTao.setBackground(new Color(245, 246, 250));
        pnBtnTao.add(btnTao);
        leftPanel.add(formTao, BorderLayout.NORTH);
        leftPanel.add(pnBtnTao, BorderLayout.CENTER);

        // RIGHT: đổi mật khẩu (UC003)
        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.setBackground(new Color(245, 246, 250));

        JPanel formDoiMK = new JPanel(new GridLayout(3, 2, 10, 12));
        UIFactory.styleFormPanel(formDoiMK);
        formDoiMK.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                "Đổi mật khẩu (UC003) — Tài khoản: " +
                (tkActive != null ? tkActive.getTenDangNhap() : "?"),
                0, 0, new Font("Arial", Font.BOLD, 12), new Color(100, 100, 100)));

        JPasswordField txtMKCu = new JPasswordField();
        JPasswordField txtMKMoi = new JPasswordField();
        JPasswordField txtMKXacNhan = new JPasswordField();
        formDoiMK.add(new JLabel("Mật khẩu hiện tại:")); formDoiMK.add(txtMKCu);
        formDoiMK.add(new JLabel("Mật khẩu mới:")); formDoiMK.add(txtMKMoi);
        formDoiMK.add(new JLabel("Xác nhận mật khẩu:")); formDoiMK.add(txtMKXacNhan);

        JButton btnDoiMK = UIFactory.createActionButton("Đổi mật khẩu", new Color(41, 128, 185), Color.WHITE);
        btnDoiMK.addActionListener(e -> {
            String mkCu = new String(txtMKCu.getPassword()).trim();
            String mkMoi = new String(txtMKMoi.getPassword()).trim();
            String mkXN = new String(txtMKXacNhan.getPassword()).trim();
            if (!mkMoi.equals(mkXN)) { JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp!"); return; }
            if (mkMoi.length() < 4) { JOptionPane.showMessageDialog(this, "Mật khẩu mới phải có ít nhất 4 ký tự!"); return; }
            if (tkActive != null && tkDAO.doiMatKhau(tkActive.getMaTaiKhoan(), mkCu, mkMoi)) {
                JOptionPane.showMessageDialog(this, "✅ Đổi mật khẩu thành công!");
                txtMKCu.setText(""); txtMKMoi.setText(""); txtMKXacNhan.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "❌ Mật khẩu hiện tại không đúng hoặc lỗi hệ thống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel pnBtnDoiMK = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnBtnDoiMK.setBackground(new Color(245, 246, 250));
        pnBtnDoiMK.add(btnDoiMK);
        rightPanel.add(formDoiMK, BorderLayout.NORTH);
        rightPanel.add(pnBtnDoiMK, BorderLayout.CENTER);

        mainPanel.add(leftPanel); mainPanel.add(rightPanel);
        add(mainPanel, BorderLayout.CENTER);

        // TABLE: danh sách tài khoản
        String[] cols = {"Mã TK", "Mã NV", "Vai trò", "Tên đăng nhập"};
        tableModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(tableModel);
        UIFactory.styleTable(table);

        JPanel pnTable = new JPanel(new BorderLayout());
        UIFactory.styleFormPanel(pnTable);
        pnTable.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                "Danh sách tài khoản hiện có", 0, 0, new Font("Arial", Font.BOLD, 12), new Color(100, 100, 100)));
        pnTable.setPreferredSize(new Dimension(0, 180));
        pnTable.add(new JScrollPane(table));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        btnPanel.setBackground(new Color(245, 246, 250));
        JButton btnXoa = UIFactory.createActionButton("Xóa tài khoản đã chọn", new Color(220, 53, 69), Color.WHITE);
        btnXoa.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Chọn tài khoản cần xóa!"); return; }
            String maTK = tableModel.getValueAt(row, 0).toString();
            if (tkActive != null && maTK.equals(tkActive.getMaTaiKhoan())) {
                JOptionPane.showMessageDialog(this, "Không thể xóa tài khoản đang đăng nhập!"); return;
            }
            if (tkDAO.xoa(maTK)) { JOptionPane.showMessageDialog(this, "✅ Xóa thành công!"); loadData(); }
            else JOptionPane.showMessageDialog(this, "❌ Không thể xóa.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        });
        btnPanel.add(btnXoa);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(new Color(245, 246, 250));
        southPanel.add(pnTable, BorderLayout.CENTER);
        southPanel.add(btnPanel, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<TaiKhoan> list = tkDAO.getAll();
        for (TaiKhoan tk : list) {
            tableModel.addRow(new Object[]{tk.getMaTaiKhoan(), tk.getMaNhanVien(), tk.getVaiTro(), tk.getTenDangNhap()});
        }
    }
}
