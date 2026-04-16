package com.lotuslaverne.gui;

import com.lotuslaverne.dao.LoaiPhongDAO;
import com.lotuslaverne.dao.PhongDAO;
import com.lotuslaverne.entity.LoaiPhong;
import com.lotuslaverne.entity.Phong;
import com.lotuslaverne.util.UIFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class FrmPhong extends JPanel {

    private final PhongDAO dao = new PhongDAO();
    private final LoaiPhongDAO loaiPhongDAO = new LoaiPhongDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtMaPhong, txtTenPhong;
    private JComboBox<String> cbTrangThai, cbLoaiPhong;

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
        cbLoaiPhong = new JComboBox<>(); 
        loadLoaiPhong();
        
        String[] trangThaiList = {"Trống", "Đang Thuê", "Chưa Dọn"};
        cbTrangThai = new JComboBox<>(trangThaiList);

        topPanel.add(new JLabel("Mã Phòng (Số P):")); topPanel.add(txtMaPhong);
        topPanel.add(new JLabel("Tên Phòng:")); topPanel.add(txtTenPhong);
        topPanel.add(new JLabel("Loại Phòng:")); topPanel.add(cbLoaiPhong);
        topPanel.add(new JLabel("Trạng Thái:")); topPanel.add(cbTrangThai);
        
        // TABLE
        String[] cols = {"Mã Phòng", "Tên Phòng", "Loại Phòng", "Trạng Thái Phòng"};
        tableModel = new DefaultTableModel(cols, 0);
        table = new JTable(tableModel);
        UIFactory.styleTable(table);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if(row >= 0) {
                int modelRow = table.convertRowIndexToModel(row);
                txtMaPhong.setText(tableModel.getValueAt(modelRow, 0).toString());
                txtMaPhong.setEditable(false);
                txtTenPhong.setText(tableModel.getValueAt(modelRow, 1).toString());
                
                String maLoaiPhong = tableModel.getValueAt(modelRow, 2).toString();
                for (int i = 0; i < cbLoaiPhong.getItemCount(); i++) {
                    if (cbLoaiPhong.getItemAt(i).startsWith(maLoaiPhong)) {
                        cbLoaiPhong.setSelectedIndex(i);
                        break;
                    }
                }
                cbTrangThai.setSelectedItem(tableModel.getValueAt(modelRow, 3).toString());
            }
        });

        JPanel pnCenter = new JPanel(new BorderLayout(0, 20)); 
        pnCenter.setBackground(new Color(245, 246, 250));
        
        // Cụm Search filter
        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pnlSearch.setBackground(new Color(245, 246, 250));
        pnlSearch.add(new JLabel("Tìm kiếm nhanh:"));
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
        
        // Wrap table inside a white card
        JPanel pnlTableWrapper = new JPanel(new BorderLayout());
        UIFactory.styleFormPanel(pnlTableWrapper);
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
            txtTenPhong.setText(""); 
            if(cbLoaiPhong.getItemCount() > 0) cbLoaiPhong.setSelectedIndex(0); 
            cbTrangThai.setSelectedIndex(0);
            table.clearSelection();
        });

        btnThem.addActionListener(e -> { // UC: TẠO PHÒNG
            String selLP = (String) cbLoaiPhong.getSelectedItem();
            String maLP = selLP != null ? selLP.split(" - ")[0] : "";
            Phong p = new Phong(txtMaPhong.getText(), txtTenPhong.getText(), maLP, cbTrangThai.getSelectedItem().toString());
            if (dao.themPhong(p)) {
                JOptionPane.showMessageDialog(this, "Đã thêm mới phòng.");
                loadDataToTable();
            } else {
                JOptionPane.showMessageDialog(this, "Phòng đã tồn tại hoặc mã lỗi bảo mật!");
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

    private void loadLoaiPhong() {
        cbLoaiPhong.removeAllItems();
        List<LoaiPhong> dslp = loaiPhongDAO.getAll();
        for (LoaiPhong lp : dslp) {
            cbLoaiPhong.addItem(lp.getMaLoaiPhong() + " - " + lp.getTenLoaiPhong());
        }
    }

    private void loadDataToTable() {
        tableModel.setRowCount(0);
        List<Phong> list = dao.getAll();
        for (Phong p : list) {
            tableModel.addRow(new Object[]{p.getMaPhong(), p.getTenPhong(), p.getMaLoaiPhong(), p.getTrangThai()});
        }
    }
}
