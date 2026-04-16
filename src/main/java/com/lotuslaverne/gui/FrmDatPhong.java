package com.lotuslaverne.gui;

import com.lotuslaverne.dao.PhongDAO;
import com.lotuslaverne.dao.PhieuDatPhongDAO;
import com.lotuslaverne.entity.PhieuDatPhong;

import javax.swing.*;
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
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("HỆ THỐNG XỬ LÝ LỄ TÂN (ĐẶT PHÒNG)", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 26));
        lblTitle.setForeground(new Color(22, 160, 133));
        add(lblTitle, BorderLayout.NORTH);

        JPanel panelForm = new JPanel(new GridLayout(6, 2, 10, 20));
        panelForm.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Nhập thông tin Đặt phòng"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
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

        add(panelForm, BorderLayout.CENTER);

        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnLapPhieu = new JButton("GHI NHẬN ĐẶT PHÒNG");
        btnLapPhieu.setPreferredSize(new Dimension(300, 50));
        btnLapPhieu.setFont(new Font("Arial", Font.BOLD, 18));
        btnLapPhieu.setBackground(new Color(41, 128, 185));
        btnLapPhieu.setForeground(Color.WHITE);

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
