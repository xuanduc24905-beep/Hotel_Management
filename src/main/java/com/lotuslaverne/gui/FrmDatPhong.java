package com.lotuslaverne.gui;

import com.lotuslaverne.dao.PhieuDatPhongDAO;
import com.lotuslaverne.dao.PhongDAO;
import com.lotuslaverne.entity.PhieuDatPhong;
import com.lotuslaverne.util.UIFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Timestamp;
import java.util.UUID;

public class FrmDatPhong extends JPanel {

    private final PhieuDatPhongDAO pdpDAO = new PhieuDatPhongDAO();
    private final PhongDAO phongDAO = new PhongDAO();

    private JTextField txtKhachHang, txtSoDienThoai, txtMaPhong, txtSoNguoi;
    private JComboBox<String> cbLoaiHinh;

    public FrmDatPhong() {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 20));
        UIFactory.styleMainPanel(this);

        JLabel lblTitle = new JLabel("HỆ THỐNG XỬ LÝ LỄ TÂN (ĐẶT PHÒNG)", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 26));
        lblTitle.setForeground(new Color(44, 62, 80));
        add(lblTitle, BorderLayout.NORTH);
        
        JPanel pnlCenter = new JPanel(new BorderLayout());
        pnlCenter.setBackground(new Color(245, 246, 250));

        JPanel panelForm = new JPanel(new GridLayout(6, 2, 15, 20));
        UIFactory.styleFormPanel(panelForm);
        panelForm.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                "Nhập thông tin Đặt phòng",
                0, 0, new Font("Arial", Font.BOLD, 12), new Color(100, 100, 100)
        ));

        panelForm.add(new JLabel("Mã Khách Hàng (Tạo tự động trên nền):"));
        txtKhachHang = new JTextField("KH001");
        panelForm.add(txtKhachHang);

        panelForm.add(new JLabel("SĐT Khách Mới:"));
        txtSoDienThoai = new JTextField();
        panelForm.add(txtSoDienThoai);

        panelForm.add(new JLabel("Chọn Số Phòng (Mã Phòng):"));
        txtMaPhong = new JTextField("P101");
        panelForm.add(txtMaPhong);

        panelForm.add(new JLabel("Số Lượng Người:"));
        txtSoNguoi = new JTextField("2");
        panelForm.add(txtSoNguoi);

        panelForm.add(new JLabel("Hình Thức Đặt:"));
        cbLoaiHinh = new JComboBox<>(new String[]{"Trực tiếp ngay", "Qua điện thoại", "Online Booking"});
        panelForm.add(cbLoaiHinh);

        pnlCenter.add(panelForm, BorderLayout.NORTH);
        add(pnlCenter, BorderLayout.CENTER);

        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelBottom.setBackground(new Color(245, 246, 250));
        JButton btnLapPhieu = UIFactory.createActionButton("GHI NHẬN ĐẶT PHÒNG", new Color(41, 128, 185), Color.WHITE);
        btnLapPhieu.setPreferredSize(new Dimension(300, 45));
        btnLapPhieu.setMaximumSize(new Dimension(300, 45));

        btnLapPhieu.addActionListener(e -> {
            String maPDP = "PDP" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
            String maKH = txtKhachHang.getText();
            int soNguoi = Integer.parseInt(txtSoNguoi.getText());
            String maPh = txtMaPhong.getText();

            // Thực thi chèn xuống SQL DB 1: Phiếu, DB 2: Đổi trạng thái phòng
            PhieuDatPhong pdp = new PhieuDatPhong(maPDP, "NV_CA1", maKH, new Timestamp(System.currentTimeMillis()), soNguoi);
            
            if (pdpDAO.lapPhieuDat(pdp)) {
                // Đổi trạng thái phòng thành Đang Thuê
                phongDAO.capNhatTrangThai(maPh, "Đang Thuê");
                JOptionPane.showMessageDialog(this, "✅ Đặt phòng thành công!\nPhòng " + maPh + " đã chuyển sang [Đang Thuê].");
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi tạo phiếu! Thiếu KH hoặc trùng khóa.", "Lỗi DB", JOptionPane.ERROR_MESSAGE);
            }
        });

        panelBottom.add(btnLapPhieu);
        add(panelBottom, BorderLayout.SOUTH);
    }
}
