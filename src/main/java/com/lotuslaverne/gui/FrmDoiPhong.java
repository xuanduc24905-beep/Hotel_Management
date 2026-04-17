package com.lotuslaverne.gui;

import com.lotuslaverne.dao.PhongDAO;
import com.lotuslaverne.entity.Phong;
import com.lotuslaverne.util.UIFactory;
import com.lotuslaverne.util.ConnectDB;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.List;

public class FrmDoiPhong extends JPanel {

    private final PhongDAO phongDAO = new PhongDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtMaPhieuDP, txtDonGiaMoi;
    private JLabel lblPhongHienTai, lblChenhLech;

    public FrmDoiPhong() {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 20));
        UIFactory.styleMainPanel(this);

        JLabel lblTitle = new JLabel("ĐỔI PHÒNG CHO KHÁCH (UC020)", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(new Color(44, 62, 80));
        add(lblTitle, BorderLayout.NORTH);

        // LEFT - nhập thông tin
        JPanel leftPanel = new JPanel(new GridLayout(6, 2, 12, 12));
        UIFactory.styleFormPanel(leftPanel);
        leftPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                "Thông tin đổi phòng", 0, 0, new Font("Arial", Font.BOLD, 12), new Color(100, 100, 100)));

        txtMaPhieuDP = new JTextField();
        txtDonGiaMoi = new JTextField("0");
        lblPhongHienTai = new JLabel("(chưa kiểm tra)");
        lblPhongHienTai.setForeground(new Color(41, 128, 185));
        lblChenhLech = new JLabel("Chênh lệch: --");
        lblChenhLech.setFont(new Font("Arial", Font.BOLD, 13));
        lblChenhLech.setForeground(new Color(231, 76, 60));

        JButton btnKiemTra = UIFactory.createActionButton("Kiểm tra phiếu", new Color(41, 128, 185), Color.WHITE);
        btnKiemTra.addActionListener(e -> kiemTraPhieu());

        leftPanel.add(new JLabel("Mã phiếu đặt phòng:")); leftPanel.add(txtMaPhieuDP);
        leftPanel.add(new JLabel("")); leftPanel.add(btnKiemTra);
        leftPanel.add(new JLabel("Phòng hiện tại:")); leftPanel.add(lblPhongHienTai);
        leftPanel.add(new JLabel("Đơn giá phòng mới (VNĐ):")); leftPanel.add(txtDonGiaMoi);
        leftPanel.add(new JLabel("")); leftPanel.add(lblChenhLech);
        leftPanel.add(new JLabel("Chọn phòng mới bên phải →")); leftPanel.add(new JLabel(""));

        // RIGHT - danh sách phòng trống
        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        UIFactory.styleFormPanel(rightPanel);
        rightPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                "Danh sách phòng TRỐNG", 0, 0, new Font("Arial", Font.BOLD, 12), new Color(100, 100, 100)));

        String[] cols = {"Mã phòng", "Tên phòng", "Loại phòng", "Trạng thái"};
        tableModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(tableModel);
        UIFactory.styleTable(table);
        JButton btnTaiPhong = UIFactory.createActionButton("Tải phòng trống", new Color(240, 240, 240), Color.BLACK);
        btnTaiPhong.addActionListener(e -> loadPhongTrong());
        rightPanel.add(btnTaiPhong, BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        mainPanel.setBackground(new Color(245, 246, 250));
        mainPanel.add(leftPanel); mainPanel.add(rightPanel);
        add(mainPanel, BorderLayout.CENTER);

        // BUTTONS
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBackground(new Color(245, 246, 250));
        JButton btnDoiPhong = UIFactory.createActionButton("🔄  XÁC NHẬN ĐỔI PHÒNG", new Color(231, 76, 60), Color.WHITE);
        btnDoiPhong.setPreferredSize(new Dimension(300, 45));
        btnDoiPhong.addActionListener(e -> doiPhong());
        btnPanel.add(btnDoiPhong);
        add(btnPanel, BorderLayout.SOUTH);

        loadPhongTrong();
    }

    private void kiemTraPhieu() {
        String maPDP = txtMaPhieuDP.getText().trim();
        if (maPDP.isEmpty()) { JOptionPane.showMessageDialog(this, "Nhập mã phiếu đặt phòng!"); return; }
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return;
        try {
            PreparedStatement pst = con.prepareStatement(
                "SELECT ct.maPhong, bg.donGia FROM ChiTietPhieuDatPhong ct " +
                "JOIN PhieuDatPhong p ON ct.maPhieuDatPhong=p.maPhieuDatPhong " +
                "JOIN Phong ph ON ct.maPhong=ph.maPhong " +
                "LEFT JOIN BangGia bg ON ph.maLoaiPhong=bg.maLoaiPhong AND bg.loaiThue='QuaDem' AND GETDATE() BETWEEN bg.ngayBatDau AND bg.ngayKetThuc " +
                "WHERE ct.maPhieuDatPhong=? AND p.thoiGianNhanThucTe IS NOT NULL AND p.thoiGianTraThucTe IS NULL");
            pst.setString(1, maPDP);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                lblPhongHienTai.setText(rs.getString("maPhong") + "  (giá hiện tại: " +
                    new DecimalFormat("#,###").format(rs.getDouble("donGia")) + " VNĐ/đêm)");
            } else {
                lblPhongHienTai.setText("Không tìm thấy (phiếu chưa check-in hoặc đã trả)");
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void loadPhongTrong() {
        tableModel.setRowCount(0);
        List<Phong> list = phongDAO.getAll();
        for (Phong p : list) {
            if ("PhongTrong".equalsIgnoreCase(p.getTrangThai())) {
                tableModel.addRow(new Object[]{p.getMaPhong(), p.getTenPhong(), p.getMaLoaiPhong(), p.getTrangThai()});
            }
        }
    }

    private void doiPhong() {
        String maPDP = txtMaPhieuDP.getText().trim();
        int row = table.getSelectedRow();
        if (maPDP.isEmpty()) { JOptionPane.showMessageDialog(this, "Nhập mã phiếu đặt phòng!"); return; }
        if (row < 0) { JOptionPane.showMessageDialog(this, "Chọn phòng mới từ danh sách!"); return; }
        String maPhongMoi = tableModel.getValueAt(row, 0).toString();
        double donGiaMoi;
        try { donGiaMoi = Double.parseDouble(txtDonGiaMoi.getText().trim()); }
        catch (Exception e) { JOptionPane.showMessageDialog(this, "Đơn giá không hợp lệ!"); return; }

        int ok = JOptionPane.showConfirmDialog(this,
                "Xác nhận đổi sang phòng " + maPhongMoi + "\nĐơn giá mới: " +
                new DecimalFormat("#,###").format(donGiaMoi) + " VNĐ?",
                "Xác nhận đổi phòng", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return;
        try {
            CallableStatement cs = con.prepareCall("{call SP_DoiPhong(?,?,?)}");
            cs.setString(1, maPDP);
            cs.setString(2, maPhongMoi);
            cs.setDouble(3, donGiaMoi);
            cs.execute();
            JOptionPane.showMessageDialog(this, "✅ Đổi phòng thành công!\nPhòng mới: " + maPhongMoi);
            loadPhongTrong();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ " + ex.getMessage(), "Lỗi đổi phòng", JOptionPane.ERROR_MESSAGE);
        }
    }
}
