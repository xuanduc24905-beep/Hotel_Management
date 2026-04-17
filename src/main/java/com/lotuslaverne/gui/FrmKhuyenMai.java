package com.lotuslaverne.gui;

import com.lotuslaverne.dao.KhuyenMaiDAO;
import com.lotuslaverne.entity.KhuyenMai;
import com.lotuslaverne.util.UIFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

public class FrmKhuyenMai extends JPanel {

    private final KhuyenMaiDAO dao = new KhuyenMaiDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtMaKM, txtTenKM, txtPhanTram, txtNgayApDung, txtNgayKetThuc, txtDieuKien;

    public FrmKhuyenMai() {
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 20));
        UIFactory.styleMainPanel(this);

        JLabel lblTitle = new JLabel("QUẢN LÝ CHƯƠNG TRÌNH KHUYẾN MÃI", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(new Color(44, 62, 80));
        add(lblTitle, BorderLayout.NORTH);

        // FORM
        JPanel formPanel = new JPanel(new GridLayout(3, 4, 12, 12));
        UIFactory.styleFormPanel(formPanel);
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                "Thông tin khuyến mãi", 0, 0, new Font("Arial", Font.BOLD, 12), new Color(100, 100, 100)));

        txtMaKM = new JTextField();
        txtTenKM = new JTextField();
        txtPhanTram = new JTextField();
        txtNgayApDung = new JTextField("2025-01-01");
        txtNgayKetThuc = new JTextField("2025-12-31");
        txtDieuKien = new JTextField();

        formPanel.add(new JLabel("Mã khuyến mãi:")); formPanel.add(txtMaKM);
        formPanel.add(new JLabel("Tên chương trình:")); formPanel.add(txtTenKM);
        formPanel.add(new JLabel("% Giảm (0-100):")); formPanel.add(txtPhanTram);
        formPanel.add(new JLabel("Ngày áp dụng (yyyy-MM-dd):")); formPanel.add(txtNgayApDung);
        formPanel.add(new JLabel("Ngày kết thúc (yyyy-MM-dd):")); formPanel.add(txtNgayKetThuc);
        formPanel.add(new JLabel("Điều kiện áp dụng:")); formPanel.add(txtDieuKien);

        // SEARCH + TABLE
        JPanel pnSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnSearch.setBackground(new Color(245, 246, 250));
        JTextField txtSearch = new JTextField(20);
        pnSearch.add(new JLabel("Tìm kiếm:")); pnSearch.add(txtSearch);

        String[] cols = {"Mã KM", "Tên chương trình", "% Giảm", "Ngày áp dụng", "Ngày kết thúc", "Điều kiện"};
        tableModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(tableModel);
        UIFactory.styleTable(table);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(txtSearch.getText()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(txtSearch.getText()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(txtSearch.getText()); }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int m = table.convertRowIndexToModel(row);
                txtMaKM.setText(tableModel.getValueAt(m, 0).toString());
                txtMaKM.setEditable(false);
                txtTenKM.setText(tableModel.getValueAt(m, 1).toString());
                txtPhanTram.setText(tableModel.getValueAt(m, 2).toString());
                txtNgayApDung.setText(tableModel.getValueAt(m, 3).toString());
                txtNgayKetThuc.setText(tableModel.getValueAt(m, 4).toString());
                txtDieuKien.setText(tableModel.getValueAt(m, 5) != null ? tableModel.getValueAt(m, 5).toString() : "");
            }
        });

        JPanel pnCenter = new JPanel(new BorderLayout(0, 10));
        pnCenter.setBackground(new Color(245, 246, 250));
        JPanel pnNorth = new JPanel(new BorderLayout());
        pnNorth.setBackground(new Color(245, 246, 250));
        pnNorth.add(formPanel, BorderLayout.NORTH);
        pnNorth.add(pnSearch, BorderLayout.SOUTH);
        pnCenter.add(pnNorth, BorderLayout.NORTH);
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
            KhuyenMai km = buildFromForm();
            if (km == null) return;
            if (dao.them(km)) { JOptionPane.showMessageDialog(this, "✅ Thêm khuyến mãi thành công!"); loadData(); resetForm(); }
            else JOptionPane.showMessageDialog(this, "❌ Lỗi thêm! Kiểm tra mã trùng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        });
        btnSua.addActionListener(e -> {
            if (txtMaKM.getText().isEmpty()) { JOptionPane.showMessageDialog(this, "Chọn khuyến mãi cần sửa!"); return; }
            KhuyenMai km = buildFromForm();
            if (km == null) return;
            if (dao.sua(km)) { JOptionPane.showMessageDialog(this, "✅ Cập nhật thành công!"); loadData(); }
            else JOptionPane.showMessageDialog(this, "❌ Lỗi cập nhật.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        });
        btnXoa.addActionListener(e -> {
            if (txtMaKM.getText().isEmpty()) { JOptionPane.showMessageDialog(this, "Chọn khuyến mãi cần xóa!"); return; }
            int ok = JOptionPane.showConfirmDialog(this, "Xóa khuyến mãi " + txtMaKM.getText() + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                if (dao.xoa(txtMaKM.getText())) { JOptionPane.showMessageDialog(this, "✅ Xóa thành công!"); loadData(); resetForm(); }
                else JOptionPane.showMessageDialog(this, "❌ Không thể xóa (đang được tham chiếu).", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnLamMoi); btnPanel.add(btnThem); btnPanel.add(btnSua); btnPanel.add(btnXoa);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private KhuyenMai buildFromForm() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Timestamp ngayAD = new Timestamp(sdf.parse(txtNgayApDung.getText().trim()).getTime());
            Timestamp ngayKT = new Timestamp(sdf.parse(txtNgayKetThuc.getText().trim()).getTime());
            double phanTram = Double.parseDouble(txtPhanTram.getText().trim());
            return new KhuyenMai(txtMaKM.getText().trim(), txtTenKM.getText().trim(),
                    ngayAD, ngayKT, phanTram, txtDieuKien.getText().trim());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Dữ liệu không hợp lệ!\nNgày định dạng yyyy-MM-dd, % giảm là số.");
            return null;
        }
    }

    private void loadData() {
        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<KhuyenMai> list = dao.getAll();
        for (KhuyenMai km : list) {
            tableModel.addRow(new Object[]{
                km.getMaKhuyenMai(), km.getTenKhuyenMai(), km.getPhanTramGiam(),
                km.getNgayApDung() != null ? sdf.format(km.getNgayApDung()) : "",
                km.getNgayKetThuc() != null ? sdf.format(km.getNgayKetThuc()) : "",
                km.getDieuKienApDung()
            });
        }
    }

    private void resetForm() {
        txtMaKM.setText(""); txtMaKM.setEditable(true);
        txtTenKM.setText(""); txtPhanTram.setText("");
        txtNgayApDung.setText("2025-01-01"); txtNgayKetThuc.setText("2025-12-31");
        txtDieuKien.setText(""); table.clearSelection();
    }

    private void filter(String text) {
        if (text.trim().isEmpty()) sorter.setRowFilter(null);
        else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
    }
}
