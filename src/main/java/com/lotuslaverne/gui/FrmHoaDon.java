package com.lotuslaverne.gui;

import com.lotuslaverne.dao.HoaDonDAO;
import com.lotuslaverne.entity.HoaDon;
import com.lotuslaverne.util.UIFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class FrmHoaDon extends JPanel {

    private final HoaDonDAO dao = new HoaDonDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel lblTongDoanhThu;

    public FrmHoaDon() {
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 20));
        UIFactory.styleMainPanel(this);

        JLabel lblTitle = new JLabel("QUẢN LÝ HÓA ĐƠN THANH TOÁN (UC025)", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(new Color(44, 62, 80));
        add(lblTitle, BorderLayout.NORTH);

        // SEARCH BAR
        JPanel pnSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pnSearch.setBackground(Color.WHITE);
        pnSearch.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        JTextField txtSearch = new JTextField(25);
        JButton btnSearch = UIFactory.createActionButton("Tìm kiếm", new Color(41, 128, 185), Color.WHITE);
        JButton btnAll = UIFactory.createActionButton("Tất cả", new Color(240, 240, 240), Color.BLACK);
        lblTongDoanhThu = new JLabel("Tổng doanh thu: 0 VNĐ");
        lblTongDoanhThu.setFont(new Font("Arial", Font.BOLD, 14));
        lblTongDoanhThu.setForeground(new Color(231, 76, 60));

        pnSearch.add(new JLabel("Tìm theo mã HD / mã phiếu / nhân viên:"));
        pnSearch.add(txtSearch); pnSearch.add(btnSearch); pnSearch.add(btnAll);
        pnSearch.add(Box.createHorizontalStrut(30)); pnSearch.add(lblTongDoanhThu);

        btnSearch.addActionListener(e -> {
            String kw = txtSearch.getText().trim();
            if (!kw.isEmpty()) loadData(dao.timKiem(kw));
        });
        btnAll.addActionListener(e -> { txtSearch.setText(""); loadData(); });
        txtSearch.addActionListener(e -> btnSearch.doClick());

        // TABLE
        String[] cols = {"Mã hóa đơn", "Ngày lập", "NV lập", "Mã phiếu ĐP",
                "Ngày thanh toán", "Tiền KM (VNĐ)", "Thành tiền (VNĐ)", "Phương thức", "Ghi chú"};
        tableModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(tableModel);
        UIFactory.styleTable(table);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        JPanel pnCenter = new JPanel(new BorderLayout(0, 10));
        pnCenter.setBackground(new Color(245, 246, 250));
        pnCenter.add(pnSearch, BorderLayout.NORTH);
        JPanel pnTable = new JPanel(new BorderLayout());
        UIFactory.styleFormPanel(pnTable);
        pnTable.add(new JScrollPane(table));
        pnCenter.add(pnTable, BorderLayout.CENTER);
        add(pnCenter, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        btnPanel.setBackground(new Color(245, 246, 250));
        JButton btnRefresh = UIFactory.createActionButton("Tải lại danh sách", new Color(240, 240, 240), Color.BLACK);
        btnRefresh.addActionListener(e -> { txtSearch.setText(""); loadData(); });
        btnPanel.add(btnRefresh);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        loadData(dao.getAll());
    }

    private void loadData(List<HoaDon> list) {
        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        DecimalFormat df = new DecimalFormat("#,###");
        double tongDoanhThu = 0;
        for (HoaDon hd : list) {
            tableModel.addRow(new Object[]{
                hd.getMaHoaDon(),
                hd.getNgayLap() != null ? sdf.format(hd.getNgayLap()) : "",
                hd.getMaNhanVienLap(),
                hd.getMaPhieuDatPhong(),
                hd.getNgayThanhToan() != null ? sdf.format(hd.getNgayThanhToan()) : "",
                df.format(hd.getTienKhuyenMai()),
                df.format(hd.getTienThanhToan()),
                hd.getPhuongThucThanhToan(),
                hd.getGhiChu()
            });
            tongDoanhThu += hd.getTienThanhToan();
        }
        DecimalFormat dfTong = new DecimalFormat("#,### VNĐ");
        lblTongDoanhThu.setText("Tổng doanh thu: " + dfTong.format(tongDoanhThu));
    }
}
