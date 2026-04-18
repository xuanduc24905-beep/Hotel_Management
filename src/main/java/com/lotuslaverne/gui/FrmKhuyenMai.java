package com.lotuslaverne.gui;

import com.lotuslaverne.dao.KhuyenMaiDAO;
import com.lotuslaverne.entity.KhuyenMai;
import com.lotuslaverne.util.UIFactory;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class FrmKhuyenMai extends JPanel {

    private final KhuyenMaiDAO dao = new KhuyenMaiDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;

    public FrmKhuyenMai() {
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(16, 16));
        UIFactory.styleMainPanel(this);

        // ── Header ──
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(new Color(245, 246, 250));

        JLabel lblTitle = new JLabel("QUẢN LÝ CHƯƠNG TRÌNH KHUYẾN MÃI");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(new Color(44, 62, 80));

        JTextField txtSearch = new JTextField(20);
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm kiếm...");
        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlSearch.setBackground(new Color(245, 246, 250));
        pnlSearch.add(new JLabel("🔍"));
        pnlSearch.add(txtSearch);

        pnlHeader.add(lblTitle,   BorderLayout.WEST);
        pnlHeader.add(pnlSearch,  BorderLayout.EAST);
        add(pnlHeader, BorderLayout.NORTH);

        // ── Table ──
        String[] cols = {"Mã KM", "Tên chương trình", "% Giảm", "Ngày áp dụng", "Ngày kết thúc", "Điều kiện"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIFactory.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setToolTipText("Double-click để chỉnh sửa");

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filter(txtSearch.getText()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filter(txtSearch.getText()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(txtSearch.getText()); }
        });

        // Double-click → mở dialog sửa
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    int m = table.convertRowIndexToModel(table.getSelectedRow());
                    KhuyenMai km = rowToEntity(m);
                    openDialog(km, false);
                }
            }
        });

        JPanel pnlTable = new JPanel(new BorderLayout());
        UIFactory.styleFormPanel(pnlTable);
        pnlTable.add(new JScrollPane(table), BorderLayout.CENTER);

        // Hint label
        JLabel lblHint = new JLabel("  💡 Double-click vào dòng để chỉnh sửa");
        lblHint.setFont(new Font("Arial", Font.ITALIC, 12));
        lblHint.setForeground(Color.GRAY);
        pnlTable.add(lblHint, BorderLayout.SOUTH);

        add(pnlTable, BorderLayout.CENTER);

        // ── Buttons ──
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        btnPanel.setBackground(new Color(245, 246, 250));

        JButton btnThem  = UIFactory.createActionButton("＋  Thêm mới", new Color(40, 167, 69),  Color.WHITE);
        JButton btnXoa   = UIFactory.createActionButton("🗑  Xóa",       new Color(220, 53, 69),   Color.WHITE);
        JButton btnRefresh = UIFactory.createActionButton("↻  Làm mới", new Color(108, 117, 125), Color.WHITE);

        btnThem.addActionListener(e -> openDialog(null, true));
        btnXoa.addActionListener(e -> xoaSelected());
        btnRefresh.addActionListener(e -> loadData());

        btnPanel.add(btnRefresh);
        btnPanel.add(btnThem);
        btnPanel.add(btnXoa);
        add(btnPanel, BorderLayout.SOUTH);
    }

    /** Mở dialog thêm (km=null) hoặc sửa (km có dữ liệu) */
    private void openDialog(KhuyenMai km, boolean isNew) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                isNew ? "Thêm khuyến mãi" : "Sửa khuyến mãi", true);
        dlg.setSize(480, 380);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(12, 12));

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(16, 20, 8, 20));
        form.setBackground(Color.WHITE);

        JTextField txtMa      = new JTextField(km != null ? km.getMaKhuyenMai() : "");
        JTextField txtTen     = new JTextField(km != null ? km.getTenKhuyenMai() : "");
        JTextField txtPhanTram = new JTextField(km != null ? String.valueOf(km.getPhanTramGiam()) : "");
        JTextField txtDieuKien = new JTextField(km != null && km.getDieuKienApDung() != null ? km.getDieuKienApDung() : "");

        txtMa.setEditable(isNew);

        JSpinner spinAD = createDateSpinner(km != null && km.getNgayApDung() != null
                ? new Date(km.getNgayApDung().getTime()) : new Date());
        JSpinner spinKT = createDateSpinner(km != null && km.getNgayKetThuc() != null
                ? new Date(km.getNgayKetThuc().getTime()) : ngayCuoiNam());

        form.add(requiredLabel("Mã khuyến mãi:"));    form.add(txtMa);
        form.add(requiredLabel("Tên chương trình:")); form.add(txtTen);
        form.add(requiredLabel("% Giảm (0-100):"));   form.add(txtPhanTram);
        form.add(requiredLabel("Ngày áp dụng:"));     form.add(spinAD);
        form.add(requiredLabel("Ngày kết thúc:"));    form.add(spinKT);
        form.add(new JLabel("Điều kiện:"));            form.add(txtDieuKien);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnRow.setBackground(Color.WHITE);
        JButton btnCancel = new JButton("Hủy");
        JButton btnSave   = UIFactory.createActionButton(isNew ? "Thêm" : "Lưu",
                isNew ? new Color(40, 167, 69) : new Color(24, 144, 255), Color.WHITE);

        btnCancel.addActionListener(e -> dlg.dispose());
        btnSave.addActionListener(e -> {
            boolean ok = true;
            if (isNew) ok &= validateField(txtMa, !txtMa.getText().trim().isEmpty());
            ok &= validateField(txtTen,      !txtTen.getText().trim().isEmpty());
            ok &= validateField(txtPhanTram, !txtPhanTram.getText().trim().isEmpty());
            if (!ok) { JOptionPane.showMessageDialog(dlg, "Vui lòng điền đầy đủ các trường bắt buộc (*)!"); return; }

            double phanTram;
            try {
                phanTram = Double.parseDouble(txtPhanTram.getText().trim());
                if (phanTram <= 0 || phanTram > 100) throw new Exception();
            } catch (Exception ex) {
                validateField(txtPhanTram, false);
                JOptionPane.showMessageDialog(dlg, "% Giảm phải là số từ 0 đến 100!"); return;
            }

            Timestamp ngayAD = new Timestamp(((Date) spinAD.getValue()).getTime());
            Timestamp ngayKT = new Timestamp(((Date) spinKT.getValue()).getTime());
            if (!ngayKT.after(ngayAD)) {
                JOptionPane.showMessageDialog(dlg, "Ngày kết thúc phải sau ngày áp dụng!"); return;
            }

            KhuyenMai entity = new KhuyenMai(txtMa.getText().trim(), txtTen.getText().trim(),
                    ngayAD, ngayKT, phanTram, txtDieuKien.getText().trim());

            boolean success = isNew ? dao.them(entity) : dao.sua(entity);
            if (success) {
                JOptionPane.showMessageDialog(dlg, isNew ? "✅ Thêm thành công!" : "✅ Cập nhật thành công!");
                dlg.dispose();
                loadData();
            } else {
                JOptionPane.showMessageDialog(dlg, "❌ Lỗi! Kiểm tra lại dữ liệu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRow.add(btnCancel);
        btnRow.add(btnSave);

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btnRow, BorderLayout.SOUTH);
        dlg.getRootPane().setDefaultButton(btnSave);
        dlg.setVisible(true);
    }

    private void xoaSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Chọn khuyến mãi cần xóa!"); return; }
        int m = table.convertRowIndexToModel(row);
        String ma = tableModel.getValueAt(m, 0).toString();
        int ok = JOptionPane.showConfirmDialog(this, "Xóa khuyến mãi \"" + ma + "\"?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            if (dao.xoa(ma)) { JOptionPane.showMessageDialog(this, "✅ Xóa thành công!"); loadData(); }
            else JOptionPane.showMessageDialog(this, "❌ Không thể xóa (đang được tham chiếu).", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private KhuyenMai rowToEntity(int modelRow) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return new KhuyenMai(
                tableModel.getValueAt(modelRow, 0).toString(),
                tableModel.getValueAt(modelRow, 1).toString(),
                new Timestamp(sdf.parse(tableModel.getValueAt(modelRow, 3).toString()).getTime()),
                new Timestamp(sdf.parse(tableModel.getValueAt(modelRow, 4).toString()).getTime()),
                Double.parseDouble(tableModel.getValueAt(modelRow, 2).toString()),
                tableModel.getValueAt(modelRow, 5) != null ? tableModel.getValueAt(modelRow, 5).toString() : ""
            );
        } catch (Exception e) { return null; }
    }

    private void loadData() {
        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (KhuyenMai km : dao.getAll()) {
            tableModel.addRow(new Object[]{
                km.getMaKhuyenMai(), km.getTenKhuyenMai(), km.getPhanTramGiam(),
                km.getNgayApDung()  != null ? sdf.format(km.getNgayApDung())  : "",
                km.getNgayKetThuc() != null ? sdf.format(km.getNgayKetThuc()) : "",
                km.getDieuKienApDung()
            });
        }
    }

    private void filter(String text) {
        sorter.setRowFilter(text.trim().isEmpty() ? null : RowFilter.regexFilter("(?i)" + text));
    }

    // ── Helpers ──
    private JSpinner createDateSpinner(Date initial) {
        SpinnerDateModel m = new SpinnerDateModel(initial, null, null, Calendar.DAY_OF_MONTH);
        JSpinner s = new JSpinner(m);
        s.setEditor(new JSpinner.DateEditor(s, "dd/MM/yyyy"));
        return s;
    }

    private Date ngayCuoiNam() {
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
