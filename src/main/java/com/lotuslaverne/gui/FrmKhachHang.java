package com.lotuslaverne.gui;

import com.lotuslaverne.dao.KhachHangDAO;
import com.lotuslaverne.entity.KhachHang;
import com.lotuslaverne.util.UIFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class FrmKhachHang extends JPanel {

    private final KhachHangDAO dao = new KhachHangDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtMaKH, txtTenKH, txtSDT, txtCMND;

    public FrmKhachHang() {
        initUI();
        loadDataToTable();
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 20));
        UIFactory.styleMainPanel(this);

        JLabel lblTitle = new JLabel("CHĂM SÓC KHÁCH HÀNG & THÔNG TIN ĐỊNH DANH", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(new Color(44, 62, 80));
        add(lblTitle, BorderLayout.NORTH);

        // FORM NHẬP
        JPanel topPanel = new JPanel(new GridLayout(2, 4, 15, 15));
        UIFactory.styleFormPanel(topPanel);
        
        topPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)), 
                "Cập nhật Hồ sơ Khách hàng", 
                0, 0, new Font("Arial", Font.BOLD, 12), new Color(100, 100, 100)
        ));
        
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
        UIFactory.styleTable(table);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if(row >= 0) {
                int modelRow = table.convertRowIndexToModel(row);
                txtMaKH.setText(tableModel.getValueAt(modelRow, 0).toString());
                txtMaKH.setEditable(false);
                txtTenKH.setText(tableModel.getValueAt(modelRow, 1).toString());
                txtSDT.setText(tableModel.getValueAt(modelRow, 2).toString());
                txtCMND.setText(tableModel.getValueAt(modelRow, 3).toString());
            }
        });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) moFormSuaKhachHang();
            }
        });

        JPanel pnCenter = new JPanel(new BorderLayout(0, 20));
        pnCenter.setBackground(new Color(245, 246, 250));
        
        // Cụm Search filter
        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pnlSearch.setBackground(new Color(245, 246, 250));
        pnlSearch.add(new JLabel("Tìm hồ sơ theo Tên/SĐT:"));
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

        // BUTTONS
        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panelBottom.setBackground(new Color(245, 246, 250));
        
        JButton btnLamMoi = UIFactory.createActionButton("Làm Trống", new Color(240, 240, 240), Color.BLACK);
        JButton btnThem = UIFactory.createActionButton("Đăng ký Thẻ Cứng", new Color(40, 167, 69), Color.WHITE);
        JButton btnSua = UIFactory.createActionButton("Sửa Căn Cước/SĐT", new Color(24, 144, 255), Color.WHITE);

        btnLamMoi.addActionListener(e -> {
            txtMaKH.setText(""); txtMaKH.setEditable(true);
            txtTenKH.setText(""); txtSDT.setText(""); txtCMND.setText("");
            table.clearSelection();
        });

        btnThem.addActionListener(e -> { // UC: TẠO KHÁCH HÀNG
            String ma = txtMaKH.getText().trim();
            if(ma.isEmpty()) ma = "KH" + (System.currentTimeMillis() % 100000);
            KhachHang kh = new KhachHang(ma, txtTenKH.getText(), txtSDT.getText(), txtCMND.getText());
            if (dao.themKhachHang(kh)) {
                JOptionPane.showMessageDialog(this, "Đã lưu hồ sơ VIP.");
                loadDataToTable();
                btnLamMoi.doClick();
            } else {
                JOptionPane.showMessageDialog(this, "Trùng Mã hoặc SDT bị trống.");
            }
        });

        btnSua.addActionListener(e -> moFormSuaKhachHang());

        panelBottom.add(btnLamMoi); panelBottom.add(btnThem); panelBottom.add(btnSua);
        add(panelBottom, BorderLayout.SOUTH);
    }

    private void moFormSuaKhachHang() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần sửa!");
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        String maKH  = tableModel.getValueAt(modelRow, 0).toString();
        String tenKH = tableModel.getValueAt(modelRow, 1).toString();
        String sdt   = tableModel.getValueAt(modelRow, 2).toString();
        String cmnd  = tableModel.getValueAt(modelRow, 3).toString();

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Sửa thông tin Khách Hàng", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(420, 230);
        dialog.setLocationRelativeTo(this);

        JPanel pnlForm = new JPanel(new GridLayout(4, 2, 10, 10));
        pnlForm.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

        JTextField fldTen  = new JTextField(tenKH);
        JTextField fldSDT  = new JTextField(sdt);
        JTextField fldCMND = new JTextField(cmnd);

        pnlForm.add(new JLabel("Mã Khách Hàng:"));    pnlForm.add(new JLabel(maKH));
        pnlForm.add(new JLabel("Họ & Tên:"));          pnlForm.add(fldTen);
        pnlForm.add(new JLabel("Số Điện Thoại:"));     pnlForm.add(fldSDT);
        pnlForm.add(new JLabel("Số CMND / Căn cước:")); pnlForm.add(fldCMND);

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLuu = UIFactory.createActionButton("Lưu", new Color(24, 144, 255), Color.WHITE);
        JButton btnHuy = UIFactory.createActionButton("Hủy", new Color(240, 240, 240), Color.BLACK);
        pnlBtn.add(btnHuy); pnlBtn.add(btnLuu);

        btnLuu.addActionListener(ev -> {
            KhachHang kh = new KhachHang(maKH, fldTen.getText(), fldSDT.getText(), fldCMND.getText());
            if (dao.suaKhachHang(kh)) {
                JOptionPane.showMessageDialog(dialog, "Đã thay đổi thông tin định danh.");
                loadDataToTable();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Sửa thất bại! Không tìm thấy hoặc trùng dữ liệu.");
            }
        });
        btnHuy.addActionListener(ev -> dialog.dispose());

        dialog.add(pnlForm, BorderLayout.CENTER);
        dialog.add(pnlBtn, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void loadDataToTable() {
        tableModel.setRowCount(0);
        List<KhachHang> list = dao.getAll();
        for (KhachHang kh : list) {
            tableModel.addRow(new Object[]{kh.getMaKH(), kh.getHoTenKH(), kh.getSoDienThoai(), kh.getCmnd()});
        }
    }
}
