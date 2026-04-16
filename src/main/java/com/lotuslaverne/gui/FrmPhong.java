package com.lotuslaverne.gui;

import com.lotuslaverne.dao.PhongDAO;
import com.lotuslaverne.entity.Phong;
import com.lotuslaverne.util.UIFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
        setLayout(new BorderLayout(20, 20));
        UIFactory.styleMainPanel(this);

        JLabel lblTitle = new JLabel("QUẢN LÝ DANH MỤC PHÒNG KHÁCH SẠN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        add(lblTitle, BorderLayout.NORTH);

        // FORM NHẬP
        JPanel topPanel = new JPanel(new GridLayout(2, 4, 20, 15));
        UIFactory.styleFormPanel(topPanel);
        
        // Thêm Title cho form nhập
        topPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)), 
                "Cấu hình Phòng", 
                0, 0, new Font("Arial", Font.BOLD, 12), new Color(100, 100, 100)
        ));
        
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
        UIFactory.styleTable(table);

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

        JPanel pnCenter = new JPanel(new BorderLayout(0, 20)); // Margin giữa topPanel và table
        pnCenter.setBackground(new Color(245, 246, 250));
        pnCenter.add(topPanel, BorderLayout.NORTH);
        
        // Wrap table inside a white card
        JPanel pnlTableWrapper = new JPanel(new BorderLayout());
        UIFactory.styleFormPanel(pnlTableWrapper);
        // Xóa border do Titled trên này không cần
        pnlTableWrapper.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        pnlTableWrapper.add(new JScrollPane(table), BorderLayout.CENTER);
        
        pnCenter.add(pnlTableWrapper, BorderLayout.CENTER);
        add(pnCenter, BorderLayout.CENTER);

        // BUTTONS & LOGIC UC
        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panelBottom.setBackground(new Color(245, 246, 250));
        
        JButton btnLamMoi = UIFactory.createActionButton("Bỏ chọn", new Color(240, 240, 240), Color.BLACK);
        JButton btnThem = UIFactory.createActionButton("Thiết lập Phòng", new Color(40, 167, 69), Color.WHITE);
        JButton btnSua = UIFactory.createActionButton("Đổi Trạng Thái", new Color(24, 144, 255), Color.WHITE);
        JButton btnXoa = UIFactory.createActionButton("Gỡ Phòng", new Color(220, 53, 69), Color.WHITE);

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
            if (txtMaPhong.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn hoặc nhập Mã Phòng cần sửa!");
                return;
            }
            if (dao.capNhatTrangThai(txtMaPhong.getText(), cbTrangThai.getSelectedItem().toString())) {
                JOptionPane.showMessageDialog(this, "Trạng thái phòng đã thay đổi.");
                loadDataToTable();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể cập nhật! Kiểm tra lại mã phòng.");
            }
        });

        btnXoa.addActionListener(e -> { // UC: XÓA PHÒNG
            if (txtMaPhong.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn Phòng cần xóa!");
                return;
            }
            if (dao.xoaPhong(txtMaPhong.getText())) {
                JOptionPane.showMessageDialog(this, "Phòng đã bị gỡ.");
                loadDataToTable();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xóa! Phòng này đang có lịch sử Đặt Phòng (ràng buộc khóa ngoại).");
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
