package com.lotuslaverne.gui;

import com.lotuslaverne.dao.DichVuDAO;
import com.lotuslaverne.entity.DichVu;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FrmDichVu extends JPanel {

    private final DichVuDAO dao = new DichVuDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtMaDV, txtTenDV, txtMaLoaiDV, txtDonGia;
    private JComboBox<String> cbTrangThai;

    public FrmDichVu() {
        initUI();
        loadDataToTable();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("QUẢN LÝ DỊCH VỤ PHÁT SINH", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        add(lblTitle, BorderLayout.NORTH);

        // FORM NHẬP
        JPanel topPanel = new JPanel(new GridLayout(2, 5, 10, 10));
        topPanel.setBorder(BorderFactory.createTitledBorder("Cập nhật thẻ món ăn/dịch vụ"));
        
        txtMaDV = new JTextField();
        txtTenDV = new JTextField();
        txtMaLoaiDV = new JTextField(); 
        txtDonGia = new JTextField("0.0");
        
        cbTrangThai = new JComboBox<>(new String[]{"DangKinhDoanh", "NgungKinhDoanh"});

        topPanel.add(new JLabel("Mã Dịch Vụ:")); topPanel.add(txtMaDV);
        topPanel.add(new JLabel("Tên Dịch Vụ:")); topPanel.add(txtTenDV);
        topPanel.add(new JLabel("Mã Loại:")); topPanel.add(txtMaLoaiDV);
        topPanel.add(new JLabel("Đơn Giá (VNĐ):")); topPanel.add(txtDonGia);
        topPanel.add(new JLabel("Trạng Thái:")); topPanel.add(cbTrangThai);
        
        // TABLE
        String[] cols = {"Mã Dịch Vụ", "Tên Menu", "Loại", "Đơn Giá", "Trạng thái"};
        tableModel = new DefaultTableModel(cols, 0);
        table = new JTable(tableModel);
        table.setRowHeight(30);

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if(row >= 0) {
                txtMaDV.setText(tableModel.getValueAt(row, 0).toString());
                txtMaDV.setEditable(false);
                txtTenDV.setText(tableModel.getValueAt(row, 1).toString());
                txtMaLoaiDV.setText(tableModel.getValueAt(row, 2).toString());
                txtDonGia.setText(tableModel.getValueAt(row, 3).toString());
                cbTrangThai.setSelectedItem(tableModel.getValueAt(row, 4).toString());
            }
        });

        JPanel pnCenter = new JPanel(new BorderLayout());
        pnCenter.add(topPanel, BorderLayout.NORTH);
        pnCenter.add(new JScrollPane(table), BorderLayout.CENTER);
        add(pnCenter, BorderLayout.CENTER);

        // BUTTONS
        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnLamMoi = new JButton("Reset Form");
        JButton btnThem = new JButton("Khởi tạo Món Mới");
        JButton btnSua = new JButton("Đổi Tên/Đổi Giá");
        JButton btnNgungBD = new JButton("Ngừng Kinh Doanh");

        btnLamMoi.addActionListener(e -> {
            txtMaDV.setText(""); txtMaDV.setEditable(true);
            txtTenDV.setText(""); txtMaLoaiDV.setText(""); txtDonGia.setText("0.0");
            loadDataToTable();
        });

        btnThem.addActionListener(e -> { // TẠO MENU
            DichVu dv = new DichVu(txtMaDV.getText(), txtTenDV.getText(), txtMaLoaiDV.getText(), Double.parseDouble(txtDonGia.getText()), cbTrangThai.getSelectedItem().toString());
            if (dao.themDichVu(dv)) {
                JOptionPane.showMessageDialog(this, "Đã bán Dịch vụ mới.");
                loadDataToTable();
            } else {
                JOptionPane.showMessageDialog(this, "Bị vướng khóa phụ (Sai Mã Loại) hoặc Trùng Mã DV.");
            }
        });

        btnSua.addActionListener(e -> { // CẬP NHẬT MENU
            DichVu dv = new DichVu(txtMaDV.getText(), txtTenDV.getText(), txtMaLoaiDV.getText(), Double.parseDouble(txtDonGia.getText()), cbTrangThai.getSelectedItem().toString());
            if (dao.capNhatDichVu(dv)) {
                JOptionPane.showMessageDialog(this, "Đã lưu lại điều chỉnh giá cả!");
                loadDataToTable();
            }
        });

        btnNgungBD.addActionListener(e -> { // NGỪNG BÁN
            if (dao.dungBanDichVu(txtMaDV.getText())) {
                JOptionPane.showMessageDialog(this, "Món này đã bị treo biển Hết Hàng (Soft Delete).");
                loadDataToTable();
            }
        });

        panelBottom.add(btnLamMoi); panelBottom.add(btnThem);
        panelBottom.add(btnSua); panelBottom.add(btnNgungBD);
        add(panelBottom, BorderLayout.SOUTH);
    }

    private void loadDataToTable() {
        tableModel.setRowCount(0);
        List<DichVu> list = dao.getAll();
        for (DichVu dv : list) {
            tableModel.addRow(new Object[]{dv.getMaDichVu(), dv.getTenDichVu(), dv.getMaLoaiDichVu(), dv.getDonGia(), dv.getTrangThai()});
        }
    }
}
