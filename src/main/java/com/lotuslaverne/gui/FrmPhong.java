package com.lotuslaverne.gui;

import com.lotuslaverne.dao.PhongDAO;
import com.lotuslaverne.entity.Phong;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FrmPhong extends JPanel {

    private final PhongDAO dao = new PhongDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtMaPhong, txtTenPhong, txtLoaiPhong;
    private JComboBox<String> cbTrangThai;

    public FrmPhong() {
        initUI();
        loadDataToTable();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("QUẢN LÝ DANH MỤC PHÒNG KHÁCH SẠN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        add(lblTitle, BorderLayout.NORTH);

        // FORM NHẬP
        JPanel topPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        topPanel.setBorder(BorderFactory.createTitledBorder("Cấu hình Phòng"));
        
        txtMaPhong = new JTextField();
        txtTenPhong = new JTextField();
        txtLoaiPhong = new JTextField(); // Thực tế sẽ load từ LoaiPhongDAO vào Combo
        
        String[] trangThaiList = {"Trống", "Đang Thuê", "Chưa Dọn"};
        cbTrangThai = new JComboBox<>(trangThaiList);

        topPanel.add(new JLabel("Mã Phòng (Số P):")); topPanel.add(txtMaPhong);
        topPanel.add(new JLabel("Tên Phòng:")); topPanel.add(txtTenPhong);
        topPanel.add(new JLabel("Mã Loại:")); topPanel.add(txtLoaiPhong);
        topPanel.add(new JLabel("Trạng Thái:")); topPanel.add(cbTrangThai);
        
        // TABLE
        String[] cols = {"Mã Phòng", "Tên Phòng", "Loại Phòng", "Trạng Thái Phòng"};
        tableModel = new DefaultTableModel(cols, 0);
        table = new JTable(tableModel);
        table.setRowHeight(30);

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if(row >= 0) {
                txtMaPhong.setText(tableModel.getValueAt(row, 0).toString());
                txtMaPhong.setEditable(false);
                txtTenPhong.setText(tableModel.getValueAt(row, 1).toString());
                txtLoaiPhong.setText(tableModel.getValueAt(row, 2).toString());
                cbTrangThai.setSelectedItem(tableModel.getValueAt(row, 3).toString());
            }
        });

        JPanel pnCenter = new JPanel(new BorderLayout());
        pnCenter.add(topPanel, BorderLayout.NORTH);
        pnCenter.add(new JScrollPane(table), BorderLayout.CENTER);
        add(pnCenter, BorderLayout.CENTER);

        // BUTTONS & LOGIC UC
        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnLamMoi = new JButton("Bỏ chọn");
        JButton btnThem = new JButton("Tạo Phòng Mới");
        JButton btnSua = new JButton("Đổi Loại/Trạng Thái");
        JButton btnXoa = new JButton("Xóa Phòng");

        btnLamMoi.addActionListener(e -> {
            txtMaPhong.setText(""); txtMaPhong.setEditable(true);
            txtTenPhong.setText(""); txtLoaiPhong.setText(""); 
            cbTrangThai.setSelectedIndex(0);
            loadDataToTable();
        });

        btnThem.addActionListener(e -> { // UC: TẠO PHÒNG
            Phong p = new Phong(txtMaPhong.getText(), txtTenPhong.getText(), txtLoaiPhong.getText(), cbTrangThai.getSelectedItem().toString());
            if (dao.themPhong(p)) {
                JOptionPane.showMessageDialog(this, "Đã thêm mới phòng.");
                loadDataToTable();
            } else {
                JOptionPane.showMessageDialog(this, "Phòng đã tồn tại hoặc mã loại phòng sai (ràng buộc khóa phụ).");
            }
        });

        btnSua.addActionListener(e -> { // UC: CẬP NHẬT TRẠNG THÁI
            if (dao.capNhatTrangThai(txtMaPhong.getText(), cbTrangThai.getSelectedItem().toString())) {
                JOptionPane.showMessageDialog(this, "Trạng thái phòng đã thay đổi.");
                loadDataToTable();
            }
        });

        btnXoa.addActionListener(e -> { // UC: XÓA PHÒNG
            if (dao.xoaPhong(txtMaPhong.getText())) {
                JOptionPane.showMessageDialog(this, "Phòng đã bị gỡ.");
                loadDataToTable();
            }
        });

        panelBottom.add(btnLamMoi); panelBottom.add(btnThem);
        panelBottom.add(btnSua); panelBottom.add(btnXoa);
        add(panelBottom, BorderLayout.SOUTH);
    }

    private void loadDataToTable() {
        tableModel.setRowCount(0);
        List<Phong> list = dao.getAll();
        for (Phong p : list) {
            tableModel.addRow(new Object[]{p.getMaPhong(), p.getTenPhong(), p.getMaLoaiPhong(), p.getTrangThai()});
        }
    }
}
