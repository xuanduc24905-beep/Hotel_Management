package com.lotuslaverne.gui;

import com.lotuslaverne.dao.BangGiaDAO;
import com.lotuslaverne.dao.HoaDonDAO;
import com.lotuslaverne.dao.PhieuDatPhongDAO;
import com.lotuslaverne.dao.PhongDAO;
import com.lotuslaverne.entity.HoaDon;
import com.lotuslaverne.entity.KhuyenMai;
import com.lotuslaverne.entity.PhieuDatPhong;
import com.lotuslaverne.dao.KhuyenMaiDAO;
import com.lotuslaverne.util.UIFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;

public class FrmThanhToan extends JPanel {

    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final PhongDAO phongDAO = new PhongDAO();
    private final KhuyenMaiDAO khuyenMaiDAO = new KhuyenMaiDAO();
    private final BangGiaDAO bangGiaDAO = new BangGiaDAO();
    private final PhieuDatPhongDAO pdpDAO = new PhieuDatPhongDAO();

    private JTextField txtMaPhieuDP, txtKhuyenMai, txtTienPhong;
    private JComboBox<String> cbPhuongThuc;
    private double currentTotal = 0;
    private double discountAmount = 0;

    public FrmThanhToan() {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 20));
        UIFactory.styleMainPanel(this);

        JLabel lblTitle = new JLabel("HỆ THỐNG THANH TOÁN — CHECK-OUT (UC022)", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(new Color(44, 62, 80));
        add(lblTitle, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        mainPanel.setBackground(new Color(245, 246, 250));

        // LEFT
        JPanel leftPanel = new JPanel(new GridLayout(7, 2, 10, 15));
        UIFactory.styleFormPanel(leftPanel);
        leftPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                "Nhập thông tin tính tiền",
                0, 0, new Font("Arial", Font.BOLD, 12), new Color(100, 100, 100)));

        txtMaPhieuDP = new JTextField("PDP001");
        txtTienPhong = new JTextField("0 VNĐ");
        txtTienPhong.setEditable(false);
        txtTienPhong.setFont(new Font("Arial", Font.BOLD, 16));
        txtTienPhong.setForeground(new Color(231, 76, 60));

        JPanel pnlKM = new JPanel(new BorderLayout(5, 0));
        pnlKM.setBackground(Color.WHITE);
        txtKhuyenMai = new JTextField();
        JButton btnApKM = UIFactory.createActionButton("Áp dụng", new Color(41, 128, 185), Color.WHITE);
        btnApKM.setPreferredSize(new Dimension(80, 30));
        pnlKM.add(txtKhuyenMai, BorderLayout.CENTER);
        pnlKM.add(btnApKM, BorderLayout.EAST);

        cbPhuongThuc = new JComboBox<>(new String[]{"TienMat", "ChuyenKhoan"});

        JButton btnTinh = UIFactory.createActionButton("TÍNH TOÁN TIỀN", new Color(39, 174, 96), Color.WHITE);

        leftPanel.add(new JLabel("Mã phiếu đặt phòng:")); leftPanel.add(txtMaPhieuDP);
        leftPanel.add(new JLabel("Mã Voucher khuyến mãi:")); leftPanel.add(pnlKM);
        leftPanel.add(new JLabel("Phương thức thanh toán:")); leftPanel.add(cbPhuongThuc);
        leftPanel.add(new JLabel("=== Nhấn để tính ===")); leftPanel.add(btnTinh);
        leftPanel.add(new JLabel("TỔNG TIỀN THANH TOÁN:")); leftPanel.add(txtTienPhong);
        leftPanel.add(new JLabel("")); leftPanel.add(new JLabel(""));
        leftPanel.add(new JLabel("")); leftPanel.add(new JLabel(""));

        mainPanel.add(leftPanel);

        // RIGHT
        JPanel rightPanel = new JPanel(new BorderLayout());
        UIFactory.styleFormPanel(rightPanel);
        rightPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                "Dịch vụ phát sinh (UC021)",
                0, 0, new Font("Arial", Font.BOLD, 12), new Color(100, 100, 100)));
        String[] cols = {"Dịch vụ", "Số lượng", "Đơn giá", "Thành tiền"};
        JTable tableDV = new JTable(new DefaultTableModel(cols, 4));
        UIFactory.styleTable(tableDV);
        rightPanel.add(new JScrollPane(tableDV), BorderLayout.CENTER);
        mainPanel.add(rightPanel);

        add(mainPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomPanel.setBackground(new Color(245, 246, 250));
        JButton btnCheckOut = UIFactory.createActionButton("XUẤT HÓA ĐƠN & TRẢ PHÒNG", new Color(231, 76, 60), Color.WHITE);
        btnCheckOut.setPreferredSize(new Dimension(380, 45));

        btnTinh.addActionListener(e -> tinhTien());
        btnApKM.addActionListener(e -> apDungKhuyenMai());
        btnCheckOut.addActionListener(e -> checkOut());

        bottomPanel.add(btnCheckOut);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void tinhTien() {
        String maPDP = txtMaPhieuDP.getText().trim();
        if (maPDP.isEmpty()) { JOptionPane.showMessageDialog(this, "Nhập mã phiếu đặt phòng!"); return; }

        List<PhieuDatPhong> list = pdpDAO.getDangSuDung();
        PhieuDatPhong pdp = null;
        for (PhieuDatPhong p : list) {
            if (p.getMaPhieuDatPhong().equalsIgnoreCase(maPDP)) { pdp = p; break; }
        }
        if (pdp == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy phiếu đang sử dụng!\nKiểm tra mã phiếu hoặc khách chưa check-in.");
            return;
        }

        long tNhan = pdp.getThoiGianNhanThucTe() != null
                ? pdp.getThoiGianNhanThucTe().getTime()
                : pdp.getThoiGianNhanDuKien().getTime();
        long soGio = Math.max(1, (System.currentTimeMillis() - tNhan) / 3600000);
        long soNgay = Math.max(1, soGio / 24);

        double donGia = 0;
        try {
            java.sql.Connection con = com.lotuslaverne.util.ConnectDB.getInstance().getConnection();
            java.sql.PreparedStatement pst = con.prepareStatement(
                "SELECT ph.maLoaiPhong FROM ChiTietPhieuDatPhong ct JOIN Phong ph ON ct.maPhong=ph.maPhong WHERE ct.maPhieuDatPhong=?");
            pst.setString(1, maPDP);
            java.sql.ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                String maLP = rs.getString("maLoaiPhong");
                donGia = bangGiaDAO.getGiaHienTai(maLP, "QuaDem");
                if (donGia == 0) donGia = bangGiaDAO.getGiaHienTai(maLP, "TheoNgay");
                if (donGia == 0) donGia = 200000; // fallback
            }
        } catch (Exception ex) { donGia = 200000; }

        double base = soNgay * donGia;
        currentTotal = Math.max(0, base - discountAmount);

        DecimalFormat df = new DecimalFormat("#,### VNĐ");
        txtTienPhong.setText(df.format(currentTotal));
        JOptionPane.showMessageDialog(this,
                "Số ngày lưu trú: " + soNgay + " ngày\n" +
                "Đơn giá/đêm: " + df.format(donGia) + "\n" +
                "Giảm giá: " + df.format(discountAmount) + "\n" +
                "Tổng: " + df.format(currentTotal));
    }

    private void apDungKhuyenMai() {
        String maKM = txtKhuyenMai.getText().trim();
        if (maKM.isEmpty()) return;
        List<KhuyenMai> list = khuyenMaiDAO.layTatCaKhuyenMai();
        for (KhuyenMai km : list) {
            if (km.getMaKhuyenMai().equalsIgnoreCase(maKM)) {
                discountAmount = currentTotal > 0 ? currentTotal * km.getPhanTramGiam() / 100 : 0;
                JOptionPane.showMessageDialog(this, "✅ Áp dụng mã KM: Giảm " + km.getPhanTramGiam() + "%");
                if (currentTotal > 0) tinhTien();
                return;
            }
        }
        discountAmount = 0;
        JOptionPane.showMessageDialog(this, "❌ Mã KM không hợp lệ hoặc đã hết hạn!");
    }

    private void checkOut() {
        if (currentTotal == 0) { JOptionPane.showMessageDialog(this, "Vui lòng 'Tính Toán Tiền' trước!"); return; }
        String maPDP = txtMaPhieuDP.getText().trim();
        String maHD = "HD" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String phuongThuc = cbPhuongThuc.getSelectedItem().toString();

        HoaDon hd = new HoaDon(maHD, "NV001", maPDP, discountAmount, currentTotal, phuongThuc, "Checkout " + phuongThuc);
        if (hoaDonDAO.taoHoaDon(hd)) {
            phongDAO.capNhatTrangThai(layMaPhong(maPDP), "PhongCanDon");
            JOptionPane.showMessageDialog(this,
                    "✅ Thanh toán thành công!\nMã hóa đơn: " + maHD +
                    "\nTổng tiền: " + new DecimalFormat("#,### VNĐ").format(currentTotal) +
                    "\nTrạng thái phòng → Cần dọn.",
                    "Thanh toán hoàn tất", JOptionPane.INFORMATION_MESSAGE);
            currentTotal = 0; discountAmount = 0;
            txtTienPhong.setText("0 VNĐ"); txtMaPhieuDP.setText(""); txtKhuyenMai.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "❌ Lỗi tạo hóa đơn. Kiểm tra Database.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String layMaPhong(String maPDP) {
        try {
            java.sql.Connection con = com.lotuslaverne.util.ConnectDB.getInstance().getConnection();
            java.sql.PreparedStatement pst = con.prepareStatement(
                "SELECT maPhong FROM ChiTietPhieuDatPhong WHERE maPhieuDatPhong=?");
            pst.setString(1, maPDP);
            java.sql.ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getString("maPhong");
        } catch (Exception ex) { ex.printStackTrace(); }
        return "";
    }
}
