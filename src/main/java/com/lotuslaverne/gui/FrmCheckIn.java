package com.lotuslaverne.gui;

import com.lotuslaverne.dao.PhieuDatPhongDAO;
import com.lotuslaverne.entity.PhieuDatPhong;
import com.lotuslaverne.util.UIFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class FrmCheckIn extends JPanel {

    private final PhieuDatPhongDAO dao = new PhieuDatPhongDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;

    public FrmCheckIn() {
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 20));
        UIFactory.styleMainPanel(this);

        JLabel lblTitle = new JLabel("QUẢN LÝ CHECK-IN (UC031)", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(new Color(44, 62, 80));
        add(lblTitle, BorderLayout.NORTH);

        // INFO PANEL
        JPanel pnInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pnInfo.setBackground(new Color(232, 244, 253));
        pnInfo.setBorder(BorderFactory.createLineBorder(new Color(41, 128, 185)));
        JLabel lblInfo = new JLabel("📋  Danh sách phiếu đặt phòng CHƯA CHECK-IN. Chọn dòng và nhấn [Xác nhận Check-in] để nhận phòng cho khách.");
        lblInfo.setFont(new Font("Arial", Font.PLAIN, 13));
        pnInfo.add(lblInfo);

        // SEARCH
        JPanel pnSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnSearch.setBackground(new Color(245, 246, 250));
        JTextField txtSearch = new JTextField(20);
        JButton btnRefresh = UIFactory.createActionButton("Tải lại", new Color(240, 240, 240), Color.BLACK);
        pnSearch.add(new JLabel("Tìm kiếm:")); pnSearch.add(txtSearch); pnSearch.add(btnRefresh);

        String[] cols = {"Mã phiếu", "Mã khách hàng", "Mã nhân viên", "Số người",
                "Giờ nhận dự kiến", "Giờ trả dự kiến", "Ghi chú"};
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
        btnRefresh.addActionListener(e -> loadData());

        JPanel pnCenter = new JPanel(new BorderLayout(0, 10));
        pnCenter.setBackground(new Color(245, 246, 250));
        JPanel pnNorth = new JPanel(new BorderLayout());
        pnNorth.setBackground(new Color(245, 246, 250));
        pnNorth.add(pnInfo, BorderLayout.NORTH);
        pnNorth.add(pnSearch, BorderLayout.SOUTH);
        pnCenter.add(pnNorth, BorderLayout.NORTH);
        JPanel pnTable = new JPanel(new BorderLayout());
        UIFactory.styleFormPanel(pnTable);
        pnTable.add(new JScrollPane(table));
        pnCenter.add(pnTable, BorderLayout.CENTER);
        add(pnCenter, BorderLayout.CENTER);

        // BUTTONS
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBackground(new Color(245, 246, 250));

        JButton btnCheckIn = UIFactory.createActionButton("✅  XÁC NHẬN CHECK-IN", new Color(40, 167, 69), Color.WHITE);
        btnCheckIn.setPreferredSize(new Dimension(280, 45));

        btnCheckIn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Vui lòng chọn phiếu đặt phòng cần check-in!"); return; }
            int m = table.convertRowIndexToModel(row);
            String maPDP = tableModel.getValueAt(m, 0).toString();
            int ok = JOptionPane.showConfirmDialog(this,
                    "Xác nhận check-in cho phiếu:\n" + maPDP + "\nKhách hàng: " + tableModel.getValueAt(m, 1),
                    "Xác nhận Check-in", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                if (dao.checkIn(maPDP)) {
                    JOptionPane.showMessageDialog(this, "✅ Check-in thành công!\nTrạng thái phòng → Đang sử dụng.");
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, "❌ Check-in thất bại! Phiếu có thể đã check-in hoặc không hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnPanel.add(btnCheckIn);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        List<PhieuDatPhong> list = dao.getChuaCheckIn();
        for (PhieuDatPhong p : list) {
            tableModel.addRow(new Object[]{
                p.getMaPhieuDatPhong(), p.getMaKhachHang(), p.getMaNhanVien(), p.getSoNguoi(),
                p.getThoiGianNhanDuKien() != null ? sdf.format(p.getThoiGianNhanDuKien()) : "",
                p.getThoiGianTraDuKien() != null ? sdf.format(p.getThoiGianTraDuKien()) : "",
                p.getGhiChu()
            });
        }
    }

    private void filter(String text) {
        if (text.trim().isEmpty()) sorter.setRowFilter(null);
        else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
    }
}
