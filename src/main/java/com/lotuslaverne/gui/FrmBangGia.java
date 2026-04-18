package com.lotuslaverne.gui;

import com.lotuslaverne.dao.BangGiaDAO;
import com.lotuslaverne.dao.LoaiPhongDAO;
import com.lotuslaverne.entity.BangGia;
import com.lotuslaverne.entity.LoaiPhong;
import com.lotuslaverne.util.UIFactory;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class FrmBangGia extends JPanel {

    private final BangGiaDAO dao = new BangGiaDAO();
    private final LoaiPhongDAO loaiPhongDAO = new LoaiPhongDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private String[] loaiPhongArr;

    public FrmBangGia() {
        List<LoaiPhong> lp = loaiPhongDAO.getAll();
        loaiPhongArr = lp.stream().map(LoaiPhong::getMaLoaiPhong).toArray(String[]::new);
        if (loaiPhongArr.length == 0) loaiPhongArr = new String[]{"(chưa có loại phòng)"};
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(16, 16));
        UIFactory.styleMainPanel(this);

        // ── Header ──
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(new Color(245, 246, 250));
        JLabel lblTitle = new JLabel("QUẢN LÝ BẢNG GIÁ PHÒNG");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(new Color(44, 62, 80));
        pnlHeader.add(lblTitle, BorderLayout.WEST);
        add(pnlHeader, BorderLayout.NORTH);

        // ── Table ──
        String[] cols = {"Mã BG", "Loại phòng", "Loại thuê", "Đơn giá", "Ngày bắt đầu", "Ngày kết thúc"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIFactory.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setToolTipText("Double-click để chỉnh sửa");

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    openDialog(rowToEntity(table.getSelectedRow()), false);
                }
            }
        });

        JPanel pnlTable = new JPanel(new BorderLayout());
        UIFactory.styleFormPanel(pnlTable);
        pnlTable.add(new JScrollPane(table), BorderLayout.CENTER);

        JLabel lblHint = new JLabel("  💡 Double-click vào dòng để chỉnh sửa");
        lblHint.setFont(new Font("Arial", Font.ITALIC, 12));
        lblHint.setForeground(Color.GRAY);
        pnlTable.add(lblHint, BorderLayout.SOUTH);
        add(pnlTable, BorderLayout.CENTER);

        // ── Buttons ──
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        btnPanel.setBackground(new Color(245, 246, 250));
        JButton btnThem    = UIFactory.createActionButton("＋  Thêm mới", new Color(40, 167, 69),  Color.WHITE);
        JButton btnXoa     = UIFactory.createActionButton("🗑  Xóa",       new Color(220, 53, 69),   Color.WHITE);
        JButton btnRefresh = UIFactory.createActionButton("↻  Làm mới",   new Color(108, 117, 125), Color.WHITE);

        btnThem.addActionListener(e -> openDialog(null, true));
        btnXoa.addActionListener(e -> xoaSelected());
        btnRefresh.addActionListener(e -> loadData());

        btnPanel.add(btnRefresh); btnPanel.add(btnThem); btnPanel.add(btnXoa);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void openDialog(BangGia bg, boolean isNew) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                isNew ? "Thêm bảng giá" : "Sửa bảng giá", true);
        dlg.setSize(460, 340);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(12, 12));

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(16, 20, 8, 20));
        form.setBackground(Color.WHITE);

        JTextField txtMa    = new JTextField(bg != null ? bg.getMaBangGia() : "");
        JTextField txtDonGia = new JTextField(bg != null ? String.valueOf(bg.getDonGia()) : "");
        txtMa.setEditable(isNew);

        JComboBox<String> cbLoaiPhong = new JComboBox<>(loaiPhongArr);
        JComboBox<String> cbLoaiThue  = new JComboBox<>(new String[]{"QuaDem", "TheoNgay", "TheoGio"});
        if (bg != null) {
            cbLoaiPhong.setSelectedItem(bg.getMaLoaiPhong());
            cbLoaiThue.setSelectedItem(bg.getLoaiThue());
        }

        JSpinner spinBD = createDateSpinner(bg != null && bg.getNgayBatDau() != null
                ? new java.util.Date(bg.getNgayBatDau().getTime()) : new java.util.Date());
        JSpinner spinKT = createDateSpinner(bg != null && bg.getNgayKetThuc() != null
                ? new java.util.Date(bg.getNgayKetThuc().getTime()) : ngayCuoiNam());

        form.add(requiredLabel("Mã bảng giá:"));   form.add(txtMa);
        form.add(requiredLabel("Loại phòng:"));     form.add(cbLoaiPhong);
        form.add(requiredLabel("Loại thuê:"));      form.add(cbLoaiThue);
        form.add(requiredLabel("Đơn giá (VNĐ):")); form.add(txtDonGia);
        form.add(requiredLabel("Ngày bắt đầu:"));  form.add(spinBD);
        form.add(requiredLabel("Ngày kết thúc:")); form.add(spinKT);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnRow.setBackground(Color.WHITE);
        JButton btnCancel = new JButton("Hủy");
        JButton btnSave = UIFactory.createActionButton(isNew ? "Thêm" : "Lưu",
                isNew ? new Color(40, 167, 69) : new Color(24, 144, 255), Color.WHITE);

        btnCancel.addActionListener(e -> dlg.dispose());
        btnSave.addActionListener(e -> {
            boolean ok = true;
            if (isNew) ok &= validateField(txtMa, !txtMa.getText().trim().isEmpty());
            ok &= validateField(txtDonGia, !txtDonGia.getText().trim().isEmpty());
            if (!ok) { JOptionPane.showMessageDialog(dlg, "Vui lòng điền đầy đủ các trường bắt buộc (*)!"); return; }

            double donGia;
            try {
                donGia = Double.parseDouble(txtDonGia.getText().trim());
                if (donGia <= 0) throw new Exception();
            } catch (Exception ex) {
                validateField(txtDonGia, false);
                JOptionPane.showMessageDialog(dlg, "Đơn giá phải là số dương!"); return;
            }

            Date ngayBD = new Date(((java.util.Date) spinBD.getValue()).getTime());
            Date ngayKT = new Date(((java.util.Date) spinKT.getValue()).getTime());
            if (!ngayKT.after(ngayBD)) {
                JOptionPane.showMessageDialog(dlg, "Ngày kết thúc phải sau ngày bắt đầu!"); return;
            }

            BangGia entity = new BangGia(txtMa.getText().trim(),
                    cbLoaiPhong.getSelectedItem().toString(),
                    cbLoaiThue.getSelectedItem().toString(),
                    donGia, ngayBD, ngayKT);

            boolean success = isNew ? dao.them(entity) : dao.sua(entity);
            if (success) {
                JOptionPane.showMessageDialog(dlg, isNew ? "✅ Thêm thành công!" : "✅ Cập nhật thành công!");
                dlg.dispose(); loadData();
            } else {
                JOptionPane.showMessageDialog(dlg, "❌ Lỗi! Kiểm tra lại dữ liệu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRow.add(btnCancel); btnRow.add(btnSave);
        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btnRow, BorderLayout.SOUTH);
        dlg.getRootPane().setDefaultButton(btnSave);
        dlg.setVisible(true);
    }

    private void xoaSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Chọn bảng giá cần xóa!"); return; }
        String ma = tableModel.getValueAt(row, 0).toString();
        int ok = JOptionPane.showConfirmDialog(this, "Xóa bảng giá \"" + ma + "\"?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            if (dao.xoa(ma)) { JOptionPane.showMessageDialog(this, "✅ Xóa thành công!"); loadData(); }
            else JOptionPane.showMessageDialog(this, "❌ Không thể xóa.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private BangGia rowToEntity(int row) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return new BangGia(
                tableModel.getValueAt(row, 0).toString(),
                tableModel.getValueAt(row, 1).toString(),
                tableModel.getValueAt(row, 2).toString(),
                Double.parseDouble(tableModel.getValueAt(row, 3).toString().replaceAll("[^0-9.]", "")),
                new Date(sdf.parse(tableModel.getValueAt(row, 4).toString()).getTime()),
                new Date(sdf.parse(tableModel.getValueAt(row, 5).toString()).getTime())
            );
        } catch (Exception e) { return null; }
    }

    private void loadData() {
        tableModel.setRowCount(0);
        DecimalFormat df = new DecimalFormat("#,### VNĐ");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (BangGia bg : dao.getAll()) {
            tableModel.addRow(new Object[]{
                bg.getMaBangGia(), bg.getMaLoaiPhong(), bg.getLoaiThue(),
                df.format(bg.getDonGia()),
                bg.getNgayBatDau()  != null ? sdf.format(bg.getNgayBatDau())  : "",
                bg.getNgayKetThuc() != null ? sdf.format(bg.getNgayKetThuc()) : ""
            });
        }
    }

    // ── Helpers ──
    private JSpinner createDateSpinner(java.util.Date initial) {
        SpinnerDateModel m = new SpinnerDateModel(initial, null, null, Calendar.DAY_OF_MONTH);
        JSpinner s = new JSpinner(m);
        s.setEditor(new JSpinner.DateEditor(s, "dd/MM/yyyy"));
        return s;
    }

    private java.util.Date ngayCuoiNam() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MONTH, Calendar.DECEMBER);
        c.set(Calendar.DAY_OF_MONTH, 31);
        return c.getTime();
    }

    private JLabel requiredLabel(String text) {
        return new JLabel("<html>" + text + " <font color='red'>*</font></html>");
    }

    private boolean validateField(JTextField f, boolean ok) {
        if (ok) f.setBorder(UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border"));
        else    f.setBorder(new LineBorder(Color.RED, 1));
        return ok;
    }
}
