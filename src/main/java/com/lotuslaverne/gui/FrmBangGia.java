package com.lotuslaverne.gui;

import com.lotuslaverne.dao.BangGiaDAO;
import com.lotuslaverne.dao.LoaiPhongDAO;
import com.lotuslaverne.entity.BangGia;
import com.lotuslaverne.entity.LoaiPhong;
import com.lotuslaverne.util.UIFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class FrmBangGia extends JPanel {

    private final BangGiaDAO dao = new BangGiaDAO();
    private final LoaiPhongDAO loaiPhongDAO = new LoaiPhongDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtMaBangGia, txtDonGia, txtNgayBatDau, txtNgayKetThuc;
    private JComboBox<String> cbLoaiPhong, cbLoaiThue;

    public FrmBangGia() {
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 20));
        UIFactory.styleMainPanel(this);

        JLabel lblTitle = new JLabel("QUẢN LÝ BẢNG GIÁ PHÒNG (UC009)", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(new Color(44, 62, 80));
        add(lblTitle, BorderLayout.NORTH);

        // FORM
        JPanel formPanel = new JPanel(new GridLayout(3, 4, 12, 12));
        UIFactory.styleFormPanel(formPanel);
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                "Thông tin bảng giá", 0, 0, new Font("Arial", Font.BOLD, 12), new Color(100, 100, 100)));

        txtMaBangGia = new JTextField();
        txtDonGia = new JTextField();
        txtNgayBatDau = new JTextField("2025-01-01");
        txtNgayKetThuc = new JTextField("2025-12-31");

        List<LoaiPhong> lpList = loaiPhongDAO.getAll();
        String[] lpArr = lpList.stream().map(LoaiPhong::getMaLoaiPhong).toArray(String[]::new);
        cbLoaiPhong = new JComboBox<>(lpArr.length > 0 ? lpArr : new String[]{"(chưa có loại phòng)"});
        cbLoaiThue = new JComboBox<>(new String[]{"QuaDem", "TheoNgay", "TheoGio"});

        formPanel.add(new JLabel("Mã bảng giá:")); formPanel.add(txtMaBangGia);
        formPanel.add(new JLabel("Loại phòng:")); formPanel.add(cbLoaiPhong);
        formPanel.add(new JLabel("Loại thuê:")); formPanel.add(cbLoaiThue);
        formPanel.add(new JLabel("Đơn giá (VNĐ):")); formPanel.add(txtDonGia);
        formPanel.add(new JLabel("Ngày bắt đầu (yyyy-MM-dd):")); formPanel.add(txtNgayBatDau);
        formPanel.add(new JLabel("Ngày kết thúc (yyyy-MM-dd):")); formPanel.add(txtNgayKetThuc);

        // TABLE
        String[] cols = {"Mã BG", "Loại phòng", "Loại thuê", "Đơn giá", "Ngày bắt đầu", "Ngày kết thúc"};
        tableModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(tableModel);
        UIFactory.styleTable(table);
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                txtMaBangGia.setText(tableModel.getValueAt(row, 0).toString());
                txtMaBangGia.setEditable(false);
                cbLoaiPhong.setSelectedItem(tableModel.getValueAt(row, 1).toString());
                cbLoaiThue.setSelectedItem(tableModel.getValueAt(row, 2).toString());
                txtDonGia.setText(tableModel.getValueAt(row, 3).toString().replaceAll("[^0-9.]", ""));
                txtNgayBatDau.setText(tableModel.getValueAt(row, 4).toString());
                txtNgayKetThuc.setText(tableModel.getValueAt(row, 5).toString());
            }
        });

        JPanel pnCenter = new JPanel(new BorderLayout(0, 10));
        pnCenter.setBackground(new Color(245, 246, 250));
        pnCenter.add(formPanel, BorderLayout.NORTH);
        JPanel pnTable = new JPanel(new BorderLayout());
        UIFactory.styleFormPanel(pnTable);
        pnTable.add(new JScrollPane(table));
        pnCenter.add(pnTable, BorderLayout.CENTER);
        add(pnCenter, BorderLayout.CENTER);

        // BUTTONS
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        btnPanel.setBackground(new Color(245, 246, 250));
        JButton btnLamMoi = UIFactory.createActionButton("Làm mới", new Color(240, 240, 240), Color.BLACK);
        JButton btnThem = UIFactory.createActionButton("Thêm mới", new Color(40, 167, 69), Color.WHITE);
        JButton btnSua = UIFactory.createActionButton("Cập nhật", new Color(24, 144, 255), Color.WHITE);
        JButton btnXoa = UIFactory.createActionButton("Xóa", new Color(220, 53, 69), Color.WHITE);

        btnLamMoi.addActionListener(e -> resetForm());
        btnThem.addActionListener(e -> {
            BangGia bg = buildFromForm(); if (bg == null) return;
            if (dao.them(bg)) { JOptionPane.showMessageDialog(this, "✅ Thêm bảng giá thành công!"); loadData(); resetForm(); }
            else JOptionPane.showMessageDialog(this, "❌ Lỗi thêm! Kiểm tra mã trùng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        });
        btnSua.addActionListener(e -> {
            if (txtMaBangGia.getText().isEmpty()) { JOptionPane.showMessageDialog(this, "Chọn bảng giá cần sửa!"); return; }
            BangGia bg = buildFromForm(); if (bg == null) return;
            if (dao.sua(bg)) { JOptionPane.showMessageDialog(this, "✅ Cập nhật thành công!"); loadData(); }
            else JOptionPane.showMessageDialog(this, "❌ Lỗi cập nhật.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        });
        btnXoa.addActionListener(e -> {
            if (txtMaBangGia.getText().isEmpty()) { JOptionPane.showMessageDialog(this, "Chọn bảng giá cần xóa!"); return; }
            if (dao.xoa(txtMaBangGia.getText())) { JOptionPane.showMessageDialog(this, "✅ Xóa thành công!"); loadData(); resetForm(); }
            else JOptionPane.showMessageDialog(this, "❌ Không thể xóa.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        });

        btnPanel.add(btnLamMoi); btnPanel.add(btnThem); btnPanel.add(btnSua); btnPanel.add(btnXoa);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private BangGia buildFromForm() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date ngayBD = new Date(sdf.parse(txtNgayBatDau.getText().trim()).getTime());
            Date ngayKT = new Date(sdf.parse(txtNgayKetThuc.getText().trim()).getTime());
            double donGia = Double.parseDouble(txtDonGia.getText().trim());
            return new BangGia(txtMaBangGia.getText().trim(),
                    cbLoaiPhong.getSelectedItem().toString(),
                    cbLoaiThue.getSelectedItem().toString(),
                    donGia, ngayBD, ngayKT);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Dữ liệu không hợp lệ!\nNgày: yyyy-MM-dd, Đơn giá: số.");
            return null;
        }
    }

    private void loadData() {
        tableModel.setRowCount(0);
        DecimalFormat df = new DecimalFormat("#,### VNĐ");
        List<BangGia> list = dao.getAll();
        for (BangGia bg : list) {
            tableModel.addRow(new Object[]{
                bg.getMaBangGia(), bg.getMaLoaiPhong(), bg.getLoaiThue(),
                df.format(bg.getDonGia()), bg.getNgayBatDau(), bg.getNgayKetThuc()
            });
        }
    }

    private void resetForm() {
        txtMaBangGia.setText(""); txtMaBangGia.setEditable(true);
        txtDonGia.setText(""); txtNgayBatDau.setText("2025-01-01"); txtNgayKetThuc.setText("2025-12-31");
        table.clearSelection();
    }
}
