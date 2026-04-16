package com.lotuslaverne.gui;

import com.lotuslaverne.dao.HoaDonDAO;
import com.lotuslaverne.dao.PhongDAO;
import com.lotuslaverne.entity.HoaDon;
import com.lotuslaverne.util.UIFactory;

import com.lotuslaverne.gui.FrmMain;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.UUID;
import java.util.List;
import com.lotuslaverne.entity.Phong;
import com.lotuslaverne.dao.KhuyenMaiDAO;
import com.lotuslaverne.entity.KhuyenMai;

public class FrmThanhToan extends JPanel {

    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final PhongDAO phongDAO = new PhongDAO();
    private final KhuyenMaiDAO khuyenMaiDAO = new KhuyenMaiDAO();
    private JTextField txtMaPhong, txtSoGio, txtKhuyenMai, txtTienPhong;
    private JComboBox<String> cbPhuongThuc;
    private double currentTotal = 0;
    private double discountRate = 0;

    public FrmThanhToan() {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 20));
        UIFactory.styleMainPanel(this);

        JLabel lblTitle = new JLabel("HỆ THỐNG THANH TOÁN (CHECK-OUT)", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 26));
        lblTitle.setForeground(new Color(44, 62, 80));
        add(lblTitle, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        mainPanel.setBackground(new Color(245, 246, 250));

        // LEFT (Nhập liệu tính tiền)
        JPanel leftPanel = new JPanel(new GridLayout(8, 2, 10, 15));
        UIFactory.styleFormPanel(leftPanel);
        leftPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)), 
                "Nhập thông tin tính xuất Bill",
                0, 0, new Font("Arial", Font.BOLD, 12), new Color(100, 100, 100)
        ));
        
        leftPanel.add(new JLabel("Mã Phòng Khách đang ở:"));
        txtMaPhong = new JTextField("P101");
        leftPanel.add(txtMaPhong);

        leftPanel.add(new JLabel("Số giờ lưu trú thực tế:"));
        txtSoGio = new JTextField("24");
        leftPanel.add(txtSoGio);

        leftPanel.add(new JLabel("Mã Voucher Khuyến Mãi (nếu có):"));
        JPanel pnlKM = new JPanel(new BorderLayout(5, 0));
        pnlKM.setBackground(Color.WHITE);
        txtKhuyenMai = new JTextField();
        JButton btnApDungKM = UIFactory.createActionButton("Áp dụng", new Color(41, 128, 185), Color.WHITE);
        btnApDungKM.setPreferredSize(new Dimension(80, 30));
        pnlKM.add(txtKhuyenMai, BorderLayout.CENTER);
        pnlKM.add(btnApDungKM, BorderLayout.EAST);
        leftPanel.add(pnlKM);
        
        leftPanel.add(new JLabel("Phương thức Thu tiền:"));
        cbPhuongThuc = new JComboBox<>(new String[]{"Tiền Mặt", "Chuyển khoản / Quẹt Thẻ", "Ví điện tử (MoMo/ZaloPay)"});
        leftPanel.add(cbPhuongThuc);

        leftPanel.add(new JLabel("===============")); 
        JButton btnTinhThu = UIFactory.createActionButton("TÍNH TOÁN TIỀN", new Color(39, 174, 96), Color.WHITE);
        leftPanel.add(btnTinhThu);
        
        leftPanel.add(new JLabel("TỔNG TIỀN THANH TOÁN (*):"));
        txtTienPhong = new JTextField("0 VNĐ");
        txtTienPhong.setEditable(false); 
        txtTienPhong.setFont(new Font("Arial", Font.BOLD, 16));
        txtTienPhong.setForeground(new Color(231, 76, 60));
        leftPanel.add(txtTienPhong);

        mainPanel.add(leftPanel);

        // RIGHT panel
        JPanel rightPanel = new JPanel(new BorderLayout());
        UIFactory.styleFormPanel(rightPanel);
        rightPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)), 
                "Dịch vụ phát sinh (Ăn uống)",
                0, 0, new Font("Arial", Font.BOLD, 12), new Color(100, 100, 100)
        ));
        
        String[] cols = {"Dịch vụ", "Số lượng", "Đơn giá", "Thành tiền"};
        JTable table = new JTable(new DefaultTableModel(cols, 4));
        UIFactory.styleTable(table);
        
        rightPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        mainPanel.add(rightPanel);

        add(mainPanel, BorderLayout.CENTER);

        // BOTTOM
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomPanel.setBackground(new Color(245, 246, 250));
        
        JButton btnThanhToan = UIFactory.createActionButton("XUẤT HÓA ĐƠN & CLEAN TRẢ PHÒNG", new Color(231, 76, 60), Color.WHITE);
        btnThanhToan.setPreferredSize(new Dimension(400, 45));
        btnThanhToan.setMaximumSize(new Dimension(400, 45));
        
        btnTinhThu.addActionListener(e -> {
            tinhTienThanhToan();
        });
        
        btnApDungKM.addActionListener(e -> {
            apDungKhuyenMai();
        });

        btnThanhToan.addActionListener(e -> {
            if(currentTotal == 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng 'Tính Toán Tiền' trước khi xuất hóa đơn!");
                return;
            }
            String maHD = "HD" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            String maPh = txtMaPhong.getText();
            
            HoaDon hd = new HoaDon(maHD, "NV_CA1", "KH001", new Timestamp(System.currentTimeMillis()), currentTotal, "Thanh toán " + cbPhuongThuc.getSelectedItem());
            if (hoaDonDAO.taoHoaDon(hd)) {
                // Logic Reset Phòng dơ
                phongDAO.capNhatTrangThai(maPh, "Chưa Dọn");
                
                // MOCK UP TẠO PDF
                JOptionPane.showMessageDialog(this, "✅ Lập hóa đơn và Thu tiền thành công!\nTrạng thái phòng chuyển về Trống Dọn Dẹp.\nĐang kết nối máy in để in bill điện tử...", "Thanh toán hoàn tất", JOptionPane.INFORMATION_MESSAGE);
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setSelectedFile(new java.io.File(maHD + ".pdf"));
                int userSelection = fileChooser.showSaveDialog(this);
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    JOptionPane.showMessageDialog(this, "Đã lưu hóa đơn thành phần mềm PDF: " + fileChooser.getSelectedFile().getAbsolutePath());
                }
            } else {
                JOptionPane.showMessageDialog(this, "Báo lỗi tạo HĐ. Kiểm tra Database.", "Lỗi HĐ", JOptionPane.ERROR_MESSAGE);
            }
        });

        bottomPanel.add(btnThanhToan);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void tinhTienThanhToan() {
        try {
            int soGio = Integer.parseInt(txtSoGio.getText());
            String maPh = txtMaPhong.getText();
            double unitPrice = 100000; // default 100k
            
            List<Phong> list = phongDAO.getAll();
            for (Phong p : list) {
                if(p.getMaPhong().equalsIgnoreCase(maPh)) {
                    String mlp = p.getMaLoaiPhong();
                    if(mlp == null) break;
                    if(mlp.contains("VIP")) unitPrice = 500000;
                    else if(mlp.contains("Suite")) unitPrice = 1000000;
                    else if(mlp.contains("Deluxe")) unitPrice = 300000;
                    else unitPrice = 150000;
                    break;
                }
            }
            
            double baseTotal = soGio * unitPrice;
            if(discountRate > 0) {
                baseTotal = baseTotal - (baseTotal * discountRate / 100);
            }
            currentTotal = baseTotal;
            
            DecimalFormat df = new DecimalFormat("#,###.## VNĐ");
            txtTienPhong.setText(df.format(currentTotal));
        } catch(Exception e) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số giờ hợp lệ!");
        }
    }
    
    private void apDungKhuyenMai() {
        String maKM = txtKhuyenMai.getText().trim();
        if(maKM.isEmpty()) return;
        List<KhuyenMai> list = khuyenMaiDAO.layTatCaKhuyenMai();
        boolean found = false;
        for(KhuyenMai km : list) {
            if(km.getMaKhuyenMai().equalsIgnoreCase(maKM)) {
                discountRate = km.getPhanTramGiam();
                found = true;
                break;
            }
        }
        if(found) {
            JOptionPane.showMessageDialog(this, "Áp dụng mã KM thành công! Giảm " + discountRate + "%");
            tinhTienThanhToan();
        } else {
            discountRate = 0;
            JOptionPane.showMessageDialog(this, "Mã KM không hợp lệ hoặc đã hết hạn!");
            tinhTienThanhToan();
        }
    }
}
