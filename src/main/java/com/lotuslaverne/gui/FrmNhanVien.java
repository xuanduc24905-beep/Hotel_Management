package com.lotuslaverne.gui;

import com.lotuslaverne.dao.NhanVienDAO;
import com.lotuslaverne.entity.NhanVien;
import com.lotuslaverne.util.UIFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class FrmNhanVien extends JPanel {

    private final NhanVienDAO dao = new NhanVienDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtMaNV, txtTenNV, txtSDT;
    private JComboBox<String> cbVaiTro;

    public FrmNhanVien() {
        initUI();
        loadDataToTable();
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 20));
        UIFactory.styleMainPanel(this);

        JLabel lblTitle = new JLabel("HỆ THỐNG QUẢN LÝ NHÂN SỰ TOÀN DIỆN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        add(lblTitle, BorderLayout.NORTH);

        // FORM NHẬP LIỆU (TOP PANEL)
        JPanel topPanel = new JPanel(new GridLayout(2, 4, 15, 15));
        UIFactory.styleFormPanel(topPanel);
        topPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)), 
                "Nhập liệu Hồ sơ", 
                0, 0, new Font("Arial", Font.BOLD, 12), new Color(100, 100, 100)
        ));
        
        txtMaNV = new JTextField();
        txtTenNV = new JTextField();
        txtSDT = new JTextField();
        cbVaiTro = new JComboBox<>(new String[]{"LeTan", "QuanLy"});

        topPanel.add(new JLabel("Mã Nhân Viên:")); topPanel.add(txtMaNV);
        topPanel.add(new JLabel("Tên Nhân Viên:")); topPanel.add(txtTenNV);
        topPanel.add(new JLabel("Số Điện Thoại:")); topPanel.add(txtSDT);
        topPanel.add(new JLabel("Vai Trò CV:")); topPanel.add(cbVaiTro);
        
        // TABLE (CENTER_PANEL)
        String[] cols = {"Mã Nhân Viên", "Tên Nhân Viên", "Số Điện Thoại", "Vai Trò CV"};
        tableModel = new DefaultTableModel(cols, 0);
        table = new JTable(tableModel);
        UIFactory.styleTable(table);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // MAP EVENT CLICK TABLE
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if(row >= 0) {
                int modelRow = table.convertRowIndexToModel(row);
                txtMaNV.setText(tableModel.getValueAt(modelRow, 0).toString());
                txtMaNV.setEditable(false); // Ko cho sửa Mã
                txtTenNV.setText(tableModel.getValueAt(modelRow, 1).toString());
                txtSDT.setText(tableModel.getValueAt(modelRow, 2).toString());
                cbVaiTro.setSelectedItem(tableModel.getValueAt(modelRow, 3).toString());
            }
        });

        JPanel pnCenter = new JPanel(new BorderLayout(0, 20));
        pnCenter.setBackground(new Color(245, 246, 250));
        
        // Cụm Search
        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pnlSearch.setBackground(new Color(245, 246, 250));
        pnlSearch.add(new JLabel("Tìm kiếm NV:"));
        JTextField txtSearch = new JTextField(20);
        pnlSearch.add(txtSearch);
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = txtSearch.getText();
                if (text.trim().length() == 0) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });

        JPanel pnlNorthCenter = new JPanel(new BorderLayout());
        pnlNorthCenter.setBackground(new Color(245, 246, 250));
        pnlNorthCenter.add(topPanel, BorderLayout.NORTH);
        pnlNorthCenter.add(pnlSearch, BorderLayout.SOUTH);
        pnCenter.add(pnlNorthCenter, BorderLayout.NORTH);
        
        JPanel pnlTableWrapper = new JPanel(new BorderLayout());
        UIFactory.styleFormPanel(pnlTableWrapper);
        pnlTableWrapper.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        pnlTableWrapper.add(new JScrollPane(table), BorderLayout.CENTER);
        
        pnCenter.add(pnlTableWrapper, BorderLayout.CENTER);
        add(pnCenter, BorderLayout.CENTER);

        // BOTTOM BUTTONS & LOGIC UC
        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panelBottom.setBackground(new Color(245, 246, 250));
        
        JButton btnLamMoi = UIFactory.createActionButton("Làm mới Tool", new Color(240, 240, 240), Color.BLACK);
        JButton btnThem = UIFactory.createActionButton("Thêm Mới", new Color(40, 167, 69), Color.WHITE);
        JButton btnSua = UIFactory.createActionButton("Cập Nhật", new Color(24, 144, 255), Color.WHITE);
        JButton btnXoa = UIFactory.createActionButton("Đuổi Việc (Xóa)", new Color(220, 53, 69), Color.WHITE);

        btnLamMoi.addActionListener(e -> {
            txtMaNV.setText(""); txtMaNV.setEditable(true);
            txtTenNV.setText(""); txtSDT.setText("");
            cbVaiTro.setSelectedIndex(0);
            table.clearSelection();
        });

        btnThem.addActionListener(e -> { // UC: THÊM NHÂN VIÊN
            String ma = txtMaNV.getText().trim();
            if(ma.isEmpty()) ma = "NV" + (System.currentTimeMillis() % 100000);
            NhanVien nv = new NhanVien(ma, txtTenNV.getText(), txtSDT.getText(), cbVaiTro.getSelectedItem().toString());
            if (dao.themNhanVien(nv)) {
                JOptionPane.showMessageDialog(this, "Đã thêm tân binh!");
                loadDataToTable();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi thêm! Kiểm tra lại mã trùng.");
            }
        });

        btnSua.addActionListener(e -> { // UC: CẬP NHẬT
            if (txtMaNV.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần sửa!");
                return;
            }
            NhanVien nv = new NhanVien(txtMaNV.getText(), txtTenNV.getText(), txtSDT.getText(), cbVaiTro.getSelectedItem().toString());
            if (dao.suaNhanVien(nv)) {
                JOptionPane.showMessageDialog(this, "Đã cập nhật thông tin!");
                loadDataToTable();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể cập nhật nhân viên.");
            }
        });

        btnXoa.addActionListener(e -> { // UC: XÓA NHÂN VIÊN
            if (txtMaNV.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần xóa!");
                return;
            }
            if (dao.xoaNhanVien(txtMaNV.getText())) {
                JOptionPane.showMessageDialog(this, "Xóa cứng NV khỏi CSDL thành công!");
                loadDataToTable();
                btnLamMoi.doClick();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xóa nhân viên này.");
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
            tableModel.addRow(new Object[]{nv.getMaNhanVien(), nv.getTenNhanVien(), nv.getSoDienThoai(), nv.getVaiTro()});
        }
    }
}
