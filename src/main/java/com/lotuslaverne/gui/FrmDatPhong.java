package com.lotuslaverne.gui;

import com.lotuslaverne.dao.PhieuDatPhongDAO;
import com.lotuslaverne.dao.PhongDAO;
import com.lotuslaverne.entity.PhieuDatPhong;
import com.lotuslaverne.util.UIFactory;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class FrmDatPhong extends JPanel {

    private final PhieuDatPhongDAO pdpDAO = new PhieuDatPhongDAO();
    private final PhongDAO phongDAO = new PhongDAO();

    private JTextField txtMaKH, txtMaPhong, txtSoNguoi, txtGhiChu;
    private JSpinner spinNgayNhan, spinNgayTra;
    private JComboBox<String> cbLoaiHinh;

    public FrmDatPhong() {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 20));
        UIFactory.styleMainPanel(this);

        JLabel lblTitle = new JLabel("HỆ THỐNG XỬ LÝ LỄ TÂN — ĐẶT PHÒNG", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(new Color(44, 62, 80));
        add(lblTitle, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(8, 2, 15, 15));
        UIFactory.styleFormPanel(formPanel);
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                "Nhập thông tin đặt phòng",
                0, 0, new Font("Arial", Font.BOLD, 12), new Color(100, 100, 100)));

        txtMaKH    = new JTextField("KH001");
        txtMaPhong = new JTextField("P101");
        txtSoNguoi = new JTextField("2");
        txtGhiChu  = new JTextField();
        cbLoaiHinh = new JComboBox<>(new String[]{"Trực tiếp", "Qua điện thoại", "Online Booking"});

        spinNgayNhan = createDateTimeSpinner(new Date());
        spinNgayTra  = createDateTimeSpinner(ngayMai());

        formPanel.add(requiredLabel("Mã Khách Hàng:"));  formPanel.add(txtMaKH);
        formPanel.add(requiredLabel("Mã Phòng:"));        formPanel.add(txtMaPhong);
        formPanel.add(requiredLabel("Số lượng khách:"));  formPanel.add(txtSoNguoi);
        formPanel.add(requiredLabel("Ngày nhận phòng:")); formPanel.add(spinNgayNhan);
        formPanel.add(requiredLabel("Ngày trả phòng:"));  formPanel.add(spinNgayTra);
        formPanel.add(new JLabel("Hình thức đặt:"));      formPanel.add(cbLoaiHinh);
        formPanel.add(new JLabel("Ghi chú:"));             formPanel.add(txtGhiChu);
        formPanel.add(new JLabel(""));                     formPanel.add(new JLabel(""));

        JPanel pnlCenter = new JPanel(new BorderLayout());
        pnlCenter.setBackground(new Color(245, 246, 250));
        pnlCenter.add(formPanel, BorderLayout.NORTH);
        add(pnlCenter, BorderLayout.CENTER);

        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelBottom.setBackground(new Color(245, 246, 250));

        JButton btnLapPhieu = UIFactory.createActionButton("GHI NHẬN ĐẶT PHÒNG", new Color(41, 128, 185), Color.WHITE);
        btnLapPhieu.setPreferredSize(new Dimension(300, 45));

        btnLapPhieu.addActionListener(e -> {
            // --- Validate bắt buộc ---
            boolean ok = true;
            ok &= validateField(txtMaKH, !txtMaKH.getText().trim().isEmpty());
            ok &= validateField(txtMaPhong, !txtMaPhong.getText().trim().isEmpty());
            ok &= validateField(txtSoNguoi, !txtSoNguoi.getText().trim().isEmpty());
            if (!ok) {
                JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ các trường bắt buộc (*)!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int soNguoi;
            try { soNguoi = Integer.parseInt(txtSoNguoi.getText().trim()); if (soNguoi <= 0) throw new Exception(); }
            catch (Exception ex) {
                validateField(txtSoNguoi, false);
                JOptionPane.showMessageDialog(this, "Số lượng khách phải là số nguyên dương!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Timestamp tNhan = new Timestamp(((Date) spinNgayNhan.getValue()).getTime());
            Timestamp tTra  = new Timestamp(((Date) spinNgayTra.getValue()).getTime());

            if (!tTra.after(tNhan)) {
                JOptionPane.showMessageDialog(this, "Ngày trả phòng phải sau ngày nhận phòng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String maPDP = "PDP" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
            PhieuDatPhong pdp = new PhieuDatPhong(maPDP, txtMaKH.getText().trim(), "NV001",
                    soNguoi, tNhan, tTra, txtGhiChu.getText().trim());

            if (pdpDAO.lapPhieuDat(pdp)) {
                phongDAO.capNhatTrangThai(txtMaPhong.getText().trim(), "PhongDat");
                JOptionPane.showMessageDialog(this, "✅ Đặt phòng thành công!\nMã phiếu: " + maPDP);
                resetForm();
            } else {
                JOptionPane.showMessageDialog(this, "❌ Lỗi! Kiểm tra mã khách hàng, mã phòng hoặc lịch trùng.", "Lỗi DB", JOptionPane.ERROR_MESSAGE);
            }
        });

        panelBottom.add(btnLapPhieu);
        add(panelBottom, BorderLayout.SOUTH);
    }

    private void resetForm() {
        txtMaKH.setText("KH001");   resetBorder(txtMaKH);
        txtMaPhong.setText("P101"); resetBorder(txtMaPhong);
        txtSoNguoi.setText("2");    resetBorder(txtSoNguoi);
        txtGhiChu.setText("");
        spinNgayNhan.setValue(new Date());
        spinNgayTra.setValue(ngayMai());
    }

    // ── Helpers ──

    private JSpinner createDateTimeSpinner(Date initial) {
        SpinnerDateModel model = new SpinnerDateModel(initial, null, null, Calendar.HOUR_OF_DAY);
        JSpinner spinner = new JSpinner(model);
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy HH:mm"));
        spinner.setFont(new Font("Arial", Font.PLAIN, 13));
        return spinner;
    }

    private Date ngayMai() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 1);
        return c.getTime();
    }

    private JLabel requiredLabel(String text) {
        JLabel lbl = new JLabel("<html>" + text + " <font color='red'>*</font></html>");
        return lbl;
    }

    private boolean validateField(JTextField field, boolean condition) {
        if (condition) {
            resetBorder(field);
            return true;
        } else {
            field.setBorder(new LineBorder(Color.RED, 1));
            return false;
        }
    }

    private void resetBorder(JTextField field) {
        field.setBorder(UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border"));
    }
}
