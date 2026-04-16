package com.lotuslaverne.gui;

import com.lotuslaverne.dao.KhachHangDAO;
import com.lotuslaverne.entity.KhachHang;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FrmKhachHang extends JPanel {

    private final KhachHangDAO dao = new KhachHangDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtMaKH, txtTenKH, txtSDT, txtCMND;

    public FrmKhachHang() {
        initUI();
        loadDataToTable();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("CHĂM SÓC KHÁCH HÀNG & THÔNG TIN ĐỊNH DANH", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(new Color(44, 62, 80));
        add(lblTitle, BorderLayout.NORTH);

        // FORM NHẬP
        JPanel topPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        topPanel.setBorder(BorderFactory.createTitledBorder("Cập nhật Hồ sơ Khách hàng"));
        
        txtMaKH = new JTextField();
        txtTenKH = new JTextField();
        txtSDT = new JTextField();
        txtCMND = new JTextField();

        topPanel.add(new JLabel("Mã KH (Tạo tự động):")); topPanel.add(txtMaKH);
        topPanel.add(new JLabel("Họ & Tên:")); topPanel.add(txtTenKH);
        topPanel.add(new JLabel("Số Điện Thoại:")); topPanel.add(txtSDT);
        topPanel.add(new JLabel("Số CMND / Căn cước:")); topPanel.add(txtCMND);
        
        // TABLE
        String[] cols = {"Mã Khách Hàng", "Họ Tên", "SĐT", "Chứng Minh Thư"};
        tableModel = new DefaultTableModel(cols, 0);
        table = new JTable(tableModel);
        table.setRowHeight(30);

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if(row >= 0) {
                txtMaKH.setText(tableModel.getValueAt(row, 0).toString());
                txtMaKH.setEditable(false);
                txtTenKH.setText(tableModel.getValueAt(row, 1).toString());
                txtSDT.setText(tableModel.getValueAt(row, 2).toString());
                txtCMND.setText(tableModel.getValueAt(row, 3).toString());
            }
        });

        JPanel pnCenter = new JPanel(new BorderLayout());
        pnCenter.add(topPanel, BorderLayout.NORTH);
        pnCenter.add(new JScrollPane(table), BorderLayout.CENTER);
        add(pnCenter, BorderLayout.CENTER);

        // BUTTONS
        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnLamMoi = new JButton("Làm Trống");
        JButton btnThem = new JButton("Đăng ký Thẻ Cứng");
        JButton btnSua = new JButton("Sửa Căn Cước/SĐT");

        btnLamMoi.addActionListener(e -> {
            txtMaKH.setText(""); txtMaKH.setEditable(true);
            txtTenKH.setText(""); txtSDT.setText(""); txtCMND.setText("");
            loadDataToTable();
        });

        btnThem.addActionListener(e -> { // UC: TẠO KHÁCH HÀNG
            KhachHang kh = new KhachHang(txtMaKH.getText(), txtTenKH.getText(), txtSDT.getText(), txtCMND.getText());
            if (dao.themKhachHang(kh)) {
                JOptionPane.showMessageDialog(this, "Đã lưu hồ sơ VIP.");
                loadDataToTable();
            } else {
                JOptionPane.showMessageDialog(this, "Trùng Mã hoặc SDT bị trống.");
            }
        });

        btnSua.addActionListener(e -> { // UC: CẬP NHẬT
            KhachHang kh = new KhachHang(txtMaKH.getText(), txtTenKH.getText(), txtSDT.getText(), txtCMND.getText());
            if (dao.suaKhachHang(kh)) {
                JOptionPane.showMessageDialog(this, "Đã thay đổi thông tin định danh.");
                loadDataToTable();
            }
        });

        panelBottom.add(btnLamMoi); panelBottom.add(btnThem); panelBottom.add(btnSua);
        add(panelBottom, BorderLayout.SOUTH);
    }

    private void loadDataToTable() {
        tableModel.setRowCount(0);
        List<KhachHang> list = dao.getAll();
        for (KhachHang kh : list) {
            tableModel.addRow(new Object[]{kh.getMaKH(), kh.getHoTenKH(), kh.getSoDienThoai(), kh.getCmnd()});
        }
    }
}
