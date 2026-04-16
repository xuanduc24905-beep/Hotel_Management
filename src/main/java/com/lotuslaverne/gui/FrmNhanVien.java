package com.lotuslaverne.gui;

import com.lotuslaverne.dao.NhanVienDAO;
import com.lotuslaverne.entity.NhanVien;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FrmNhanVien extends JPanel {

    private final NhanVienDAO dao = new NhanVienDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtMaNV, txtTenNV, txtSDT, txtTrangThai;

    public FrmNhanVien() {
        initUI();
        loadDataToTable();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("HỆ THỐNG QUẢN LÝ NHÂN SỰ TOÀN DIỆN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        add(lblTitle, BorderLayout.NORTH);

        // FORM NHẬP LIỆU (TOP PANEL)
        JPanel topPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        topPanel.setBorder(BorderFactory.createTitledBorder("Nhập liệu Hồ sơ"));
        
        txtMaNV = new JTextField();
        txtTenNV = new JTextField();
        txtSDT = new JTextField();
        txtTrangThai = new JTextField("DangLamViec");

        topPanel.add(new JLabel("Mã Nhân Viên:")); topPanel.add(txtMaNV);
        topPanel.add(new JLabel("Tên Nhân Viên:")); topPanel.add(txtTenNV);
        topPanel.add(new JLabel("Số Điện Thoại:")); topPanel.add(txtSDT);
        topPanel.add(new JLabel("Trạng Thái:")); topPanel.add(txtTrangThai);
        
        // TABLE (CENTER_PANEL)
        String[] cols = {"Mã Nhân Viên", "Tên Nhân Viên", "Số Điện Thoại", "Trạng Thái"};
        tableModel = new DefaultTableModel(cols, 0);
        table = new JTable(tableModel);
        table.setRowHeight(30);

        // MAP EVENT CLICK TABLE
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if(row >= 0) {
                txtMaNV.setText(tableModel.getValueAt(row, 0).toString());
                txtMaNV.setEditable(false); // Ko cho sửa Mã
                txtTenNV.setText(tableModel.getValueAt(row, 1).toString());
                txtSDT.setText(tableModel.getValueAt(row, 2).toString());
                txtTrangThai.setText(tableModel.getValueAt(row, 3).toString());
            }
        });

        JPanel pnCenter = new JPanel(new BorderLayout());
        pnCenter.add(topPanel, BorderLayout.NORTH);
        pnCenter.add(new JScrollPane(table), BorderLayout.CENTER);
        add(pnCenter, BorderLayout.CENTER);

        // BOTTOM BUTTONS & LOGIC UC
        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnLamMoi = new JButton("Làm mới Tool");
        JButton btnThem = new JButton("Thêm Mới");
        JButton btnSua = new JButton("Cập Nhật");
        JButton btnXoa = new JButton("Đuổi Việc (Xóa)");

        btnLamMoi.addActionListener(e -> {
            txtMaNV.setText(""); txtMaNV.setEditable(true);
            txtTenNV.setText(""); txtSDT.setText("");
            loadDataToTable();
        });

        btnThem.addActionListener(e -> { // UC: THÊM NHÂN VIÊN
            NhanVien nv = new NhanVien(txtMaNV.getText(), txtTenNV.getText(), txtSDT.getText(), txtTrangThai.getText());
            if (dao.themNhanVien(nv)) {
                JOptionPane.showMessageDialog(this, "Đã thêm tân binh!");
                loadDataToTable();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi thêm! Kiểm tra lại mã trùng.");
            }
        });

        btnSua.addActionListener(e -> { // UC: CẬP NHẬT
            NhanVien nv = new NhanVien(txtMaNV.getText(), txtTenNV.getText(), txtSDT.getText(), txtTrangThai.getText());
            if (dao.suaNhanVien(nv)) {
                JOptionPane.showMessageDialog(this, "Đã cập nhật thông tin!");
                loadDataToTable();
            }
        });

        btnXoa.addActionListener(e -> { // UC: XÓA NHÂN VIÊN
            if (dao.xoaNhanVien(txtMaNV.getText())) {
                JOptionPane.showMessageDialog(this, "Đã đưa trạng thái thẻ thành Nghỉ việc!");
                loadDataToTable();
            }
        });

        panelBottom.add(btnLamMoi); panelBottom.add(btnThem);
        panelBottom.add(btnSua); panelBottom.add(btnXoa);
        add(panelBottom, BorderLayout.SOUTH);
    }

    private void loadDataToTable() {
        tableModel.setRowCount(0);
        List<NhanVien> list = dao.getAll();
        for (NhanVien nv : list) {
            tableModel.addRow(new Object[]{nv.getMaNhanVien(), nv.getTenNhanVien(), nv.getSoDienThoai(), nv.getTrangThai()});
        }
    }
}
