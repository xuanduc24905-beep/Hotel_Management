package com.lotuslaverne.gui;

import com.lotuslaverne.dao.DichVuDAO;
import com.lotuslaverne.entity.DichVu;
import com.lotuslaverne.util.UIFactory;

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
        setLayout(new BorderLayout(20, 20));
        UIFactory.styleMainPanel(this);

        JLabel lblTitle = new JLabel("QUẢN LÝ DỊCH VỤ PHÁT SINH", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        add(lblTitle, BorderLayout.NORTH);

        // FORM NHẬP
        JPanel topPanel = new JPanel(new GridLayout(2, 5, 15, 15));
        UIFactory.styleFormPanel(topPanel);
        topPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)), 
                "Cập nhật thẻ món ăn/dịch vụ", 
                0, 0, new Font("Arial", Font.BOLD, 12), new Color(100, 100, 100)
        ));
        
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
        UIFactory.styleTable(table);

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
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) moFormSuaDichVu();
            }
        });

        JPanel pnCenter = new JPanel(new BorderLayout(0, 20));
        pnCenter.setBackground(new Color(245, 246, 250));
        pnCenter.add(topPanel, BorderLayout.NORTH);
        
        JPanel pnlTableWrapper = new JPanel(new BorderLayout());
        UIFactory.styleFormPanel(pnlTableWrapper);
        pnlTableWrapper.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        pnlTableWrapper.add(new JScrollPane(table), BorderLayout.CENTER);
        
        pnCenter.add(pnlTableWrapper, BorderLayout.CENTER);
        add(pnCenter, BorderLayout.CENTER);

        // BUTTONS
        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panelBottom.setBackground(new Color(245, 246, 250));
        
        JButton btnLamMoi = UIFactory.createActionButton("Reset Form", new Color(240, 240, 240), Color.BLACK);
        JButton btnThem = UIFactory.createActionButton("Khởi tạo Món Mới", new Color(40, 167, 69), Color.WHITE);
        JButton btnSua = UIFactory.createActionButton("Đổi Tên/Đổi Giá", new Color(24, 144, 255), Color.WHITE);
        JButton btnNgungBD = UIFactory.createActionButton("Ngừng Kinh Doanh", new Color(220, 53, 69), Color.WHITE);

        btnLamMoi.addActionListener(e -> {
            txtMaDV.setText(""); txtMaDV.setEditable(true);
            txtTenDV.setText(""); txtMaLoaiDV.setText(""); txtDonGia.setText("0.0");
            loadDataToTable();
        });

        btnThem.addActionListener(e -> { // TẠO MENU
            if(txtMaDV.getText().isEmpty() || txtDonGia.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập Mã DV và Đơn giá!");
                return;
            }
            try {
                DichVu dv = new DichVu(txtMaDV.getText(), txtTenDV.getText(), txtMaLoaiDV.getText(), Double.parseDouble(txtDonGia.getText()), cbTrangThai.getSelectedItem().toString());
                if (dao.themDichVu(dv)) {
                    JOptionPane.showMessageDialog(this, "Đã bán Dịch vụ mới.");
                    loadDataToTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Bị vướng khóa phụ (Sai Mã Loại) hoặc Trùng Mã DV.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Đơn giá phải là số hợp lệ!");
            }
        });

        btnSua.addActionListener(e -> moFormSuaDichVu());

        btnNgungBD.addActionListener(e -> { // NGỪNG BÁN
            if(txtMaDV.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn Dịch vụ để huỷ!");
                return;
            }
            if (dao.dungBanDichVu(txtMaDV.getText())) {
                JOptionPane.showMessageDialog(this, "Món này đã bị treo biển Hết Hàng (Soft Delete).");
                loadDataToTable();
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy Dịch Vụ thao tác.");
            }
        });

        panelBottom.add(btnLamMoi); panelBottom.add(btnThem);
        panelBottom.add(btnSua); panelBottom.add(btnNgungBD);
        add(panelBottom, BorderLayout.SOUTH);
    }

    private void moFormSuaDichVu() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dịch vụ cần cập nhật!");
            return;
        }
        String maDV    = tableModel.getValueAt(row, 0).toString();
        String tenDV   = tableModel.getValueAt(row, 1).toString();
        String maLoai  = tableModel.getValueAt(row, 2).toString();
        String donGia  = tableModel.getValueAt(row, 3).toString();
        String tthai   = tableModel.getValueAt(row, 4).toString();

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Sửa Dịch Vụ", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(450, 260);
        dialog.setLocationRelativeTo(this);

        JPanel pnlForm = new JPanel(new GridLayout(5, 2, 10, 10));
        pnlForm.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

        JTextField fldTen   = new JTextField(tenDV);
        JTextField fldLoai  = new JTextField(maLoai);
        JTextField fldGia   = new JTextField(donGia);
        JComboBox<String> fldTT = new JComboBox<>(new String[]{"DangKinhDoanh", "NgungKinhDoanh"});
        fldTT.setSelectedItem(tthai);

        pnlForm.add(new JLabel("Mã Dịch Vụ:"));      pnlForm.add(new JLabel(maDV));
        pnlForm.add(new JLabel("Tên Dịch Vụ:"));      pnlForm.add(fldTen);
        pnlForm.add(new JLabel("Mã Loại:"));           pnlForm.add(fldLoai);
        pnlForm.add(new JLabel("Đơn Giá (VNĐ):"));    pnlForm.add(fldGia);
        pnlForm.add(new JLabel("Trạng Thái:"));        pnlForm.add(fldTT);

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLuu = UIFactory.createActionButton("Lưu", new Color(24, 144, 255), Color.WHITE);
        JButton btnHuy = UIFactory.createActionButton("Hủy", new Color(240, 240, 240), Color.BLACK);
        pnlBtn.add(btnHuy); pnlBtn.add(btnLuu);

        btnLuu.addActionListener(ev -> {
            try {
                DichVu dv = new DichVu(maDV, fldTen.getText(), fldLoai.getText(),
                        Double.parseDouble(fldGia.getText()), fldTT.getSelectedItem().toString());
                if (dao.capNhatDichVu(dv)) {
                    JOptionPane.showMessageDialog(dialog, "Đã lưu lại điều chỉnh!");
                    loadDataToTable();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Lỗi kết nối hoặc sai mã dịch vụ.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Đơn giá phải là số hợp lệ!");
            }
        });
        btnHuy.addActionListener(ev -> dialog.dispose());

        dialog.add(pnlForm, BorderLayout.CENTER);
        dialog.add(pnlBtn, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void loadDataToTable() {
        tableModel.setRowCount(0);
        List<DichVu> list = dao.getAll();
        for (DichVu dv : list) {
            tableModel.addRow(new Object[]{dv.getMaDichVu(), dv.getTenDichVu(), dv.getMaLoaiDichVu(), dv.getDonGia(), dv.getTrangThai()});
        }
    }
}
