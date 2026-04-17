package com.lotuslaverne.gui;

import com.lotuslaverne.dao.PhieuDatPhongDAO;
import com.lotuslaverne.dao.PhongDAO;
import com.lotuslaverne.entity.PhieuDatPhong;
import com.lotuslaverne.util.UIFactory;

import javax.swing.*;
import java.awt.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.UUID;

public class FrmDatPhong extends JPanel {

    private final PhieuDatPhongDAO pdpDAO = new PhieuDatPhongDAO();
    private final PhongDAO phongDAO = new PhongDAO();

    private JTextField txtMaKH, txtMaPhong, txtSoNguoi, txtGhiChu;
    private JTextField txtNgayNhan, txtNgayTra;
    private JComboBox<String> cbLoaiHinh;

    public FrmDatPhong() {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 20));
        UIFactory.styleMainPanel(this);

        JLabel lblTitle = new JLabel("HỆ THỐNG XỬ LÝ LỄ TÂN — ĐẶT PHÒNG (UC016)", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(new Color(44, 62, 80));
        add(lblTitle, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(8, 2, 15, 15));
        UIFactory.styleFormPanel(formPanel);
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                "Nhập thông tin đặt phòng",
                0, 0, new Font("Arial", Font.BOLD, 12), new Color(100, 100, 100)));

        txtMaKH = new JTextField("KH001");
        txtMaPhong = new JTextField("P101");
        txtSoNguoi = new JTextField("2");
        txtGhiChu = new JTextField();
        cbLoaiHinh = new JComboBox<>(new String[]{"Trực tiếp", "Qua điện thoại", "Online Booking"});

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        long now = System.currentTimeMillis();
        txtNgayNhan = new JTextField(sdf.format(new java.util.Date(now)));
        txtNgayTra  = new JTextField(sdf.format(new java.util.Date(now + 86400000L)));

        formPanel.add(new JLabel("Mã Khách Hàng:")); formPanel.add(txtMaKH);
        formPanel.add(new JLabel("Mã Phòng:")); formPanel.add(txtMaPhong);
        formPanel.add(new JLabel("Số lượng khách:")); formPanel.add(txtSoNguoi);
        formPanel.add(new JLabel("Ngày nhận phòng (yyyy-MM-dd HH:mm):")); formPanel.add(txtNgayNhan);
        formPanel.add(new JLabel("Ngày trả phòng (yyyy-MM-dd HH:mm):")); formPanel.add(txtNgayTra);
        formPanel.add(new JLabel("Hình thức đặt:")); formPanel.add(cbLoaiHinh);
        formPanel.add(new JLabel("Ghi chú:")); formPanel.add(txtGhiChu);
        formPanel.add(new JLabel("")); formPanel.add(new JLabel(""));

        JPanel pnlCenter = new JPanel(new BorderLayout());
        pnlCenter.setBackground(new Color(245, 246, 250));
        pnlCenter.add(formPanel, BorderLayout.NORTH);
        add(pnlCenter, BorderLayout.CENTER);

        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelBottom.setBackground(new Color(245, 246, 250));

        JButton btnLapPhieu = UIFactory.createActionButton("GHI NHẬN ĐẶT PHÒNG", new Color(41, 128, 185), Color.WHITE);
        btnLapPhieu.setPreferredSize(new Dimension(300, 45));

        btnLapPhieu.addActionListener(e -> {
            try {
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Timestamp tNhan = new Timestamp(fmt.parse(txtNgayNhan.getText().trim()).getTime());
                Timestamp tTra  = new Timestamp(fmt.parse(txtNgayTra.getText().trim()).getTime());

                if (!tTra.after(tNhan)) {
                    JOptionPane.showMessageDialog(this, "Ngày trả phòng phải sau ngày nhận phòng!");
                    return;
                }

                int soNguoi;
                try { soNguoi = Integer.parseInt(txtSoNguoi.getText().trim()); }
                catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Số lượng khách phải là số nguyên!"); return;
                }

                String maPDP = "PDP" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
                String maKH  = txtMaKH.getText().trim();
                String maPh  = txtMaPhong.getText().trim();

                PhieuDatPhong pdp = new PhieuDatPhong(maPDP, maKH, "NV001",
                        soNguoi, tNhan, tTra, txtGhiChu.getText().trim());

                if (pdpDAO.lapPhieuDat(pdp)) {
                    phongDAO.capNhatTrangThai(maPh, "PhongDat");
                    JOptionPane.showMessageDialog(this,
                            "✅ Đặt phòng thành công!\nMã phiếu: " + maPDP +
                            "\nPhòng " + maPh + " → [Đã đặt]");
                    resetForm();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "❌ Lỗi tạo phiếu! Kiểm tra mã khách hàng, mã phòng hoặc lịch trùng.",
                            "Lỗi DB", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Dữ liệu không hợp lệ!\nNgày định dạng: yyyy-MM-dd HH:mm", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        panelBottom.add(btnLapPhieu);
        add(panelBottom, BorderLayout.SOUTH);
    }

    private void resetForm() {
        txtMaKH.setText("KH001");
        txtMaPhong.setText("P101");
        txtSoNguoi.setText("2");
        txtGhiChu.setText("");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        long now = System.currentTimeMillis();
        txtNgayNhan.setText(sdf.format(new java.util.Date(now)));
        txtNgayTra.setText(sdf.format(new java.util.Date(now + 86400000L)));
    }
}
