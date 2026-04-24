package com.lotuslaverne.gui;

import com.lotuslaverne.dao.LoaiPhongDAO;
import com.lotuslaverne.dao.PhongDAO;
import com.lotuslaverne.entity.LoaiPhong;
import com.lotuslaverne.entity.Phong;
import com.lotuslaverne.util.UIFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class FrmPhong extends JPanel {

    private final PhongDAO dao = new PhongDAO();
    private final LoaiPhongDAO loaiPhongDAO = new LoaiPhongDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtMaPhong, txtTenPhong;
    private JComboBox<String> cbTrangThai, cbLoaiPhong;
    private JPanel cardPanel;

    // Filter state
    private String filterLoai   = null; // null = tất cả
    private String filterTrangThai = null;
    private List<Phong> allPhong = new ArrayList<>();
    private Map<String, String> loaiPhongMap = new HashMap<>();

    public FrmPhong() {
        initUI();
        loadDataToTable();
        loadCardView();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        UIFactory.styleMainPanel(this);

        JLabel lblTitle = new JLabel("QUẢN LÍ PHÒNG", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(new Color(200, 30, 30));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(12, 0, 8, 0));
        add(lblTitle, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.PLAIN, 14));
        tabs.addTab("Phòng", buildCardTab());
        tabs.addTab("Danh sách", buildTableTab());
        add(tabs, BorderLayout.CENTER);
    }

    // ─── TAB 1: CARD VIEW ────────────────────────────────────────────────────

    private JPanel buildCardTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // ── Bộ lọc ──
        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        pnlFilter.setBackground(new Color(245, 246, 250));
        pnlFilter.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));

        // Label
        JLabel lblFilter = new JLabel("Lọc:");
        lblFilter.setFont(new Font("Arial", Font.BOLD, 13));
        pnlFilter.add(lblFilter);

        // Nhóm lọc theo Loại phòng (load từ DB)
        JPanel grpLoai = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        grpLoai.setBackground(new Color(245, 246, 250));
        grpLoai.add(makeFilterBtn("Tất cả loại", null, "loai", pnlFilter));
        for (LoaiPhong lp : loaiPhongDAO.getAll())
            grpLoai.add(makeFilterBtn(lp.getTenLoaiPhong(), lp.getMaLoaiPhong(), "loai", pnlFilter));
        pnlFilter.add(grpLoai);

        // Divider
        JSeparator sep = new JSeparator(JSeparator.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 28));
        pnlFilter.add(sep);

        // Nhóm lọc theo Trạng thái
        JPanel grpTT = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        grpTT.setBackground(new Color(245, 246, 250));
        grpTT.add(makeFilterBtn("Tất cả trạng thái", null, "tt", pnlFilter));
        grpTT.add(makeFilterBtn("Trống",    "Trống",    "tt", pnlFilter));
        grpTT.add(makeFilterBtn("Đang Thuê","Đang Thuê","tt", pnlFilter));
        grpTT.add(makeFilterBtn("Chưa Dọn", "Chưa Dọn", "tt", pnlFilter));
        pnlFilter.add(grpTT);

        // ── Card panel ──
        cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(cardPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 8));
        pnlBottom.setBackground(Color.WHITE);
        JButton btnRefresh = UIFactory.createActionButton("Làm mới", new Color(24, 144, 255), Color.WHITE);
        btnRefresh.addActionListener(e -> loadCardView());
        pnlBottom.add(btnRefresh);

        panel.add(pnlFilter, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(pnlBottom, BorderLayout.SOUTH);
        return panel;
    }

    private JButton makeFilterBtn(String label, String value, String group, Container parent) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Arial", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        boolean isActive = (value == null);
        styleFilterBtn(btn, isActive);

        btn.addActionListener(e -> {
            if ("loai".equals(group)) filterLoai = value;
            else filterTrangThai = value;

            // Cập nhật màu tất cả button trong cùng nhóm
            for (Component c : ((JPanel) btn.getParent()).getComponents()) {
                if (c instanceof JButton b) {
                    boolean active = Objects.equals(b.getText(), label)
                            && Objects.equals(b.getActionListeners()[0], btn.getActionListeners()[0]);
                    styleFilterBtn(b, b == btn);
                }
            }
            applyCardFilter();
        });
        return btn;
    }

    private void styleFilterBtn(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(new Color(24, 144, 255));
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(new Color(235, 235, 235));
            btn.setForeground(Color.DARK_GRAY);
        }
    }

    private void loadCardView() {
        loaiPhongMap.clear();
        for (LoaiPhong lp : loaiPhongDAO.getAll())
            loaiPhongMap.put(lp.getMaLoaiPhong(), lp.getTenLoaiPhong());

        allPhong = dao.getAll();
        filterLoai = null;
        filterTrangThai = null;
        applyCardFilter();
    }

    private void applyCardFilter() {
        List<Phong> filtered = allPhong.stream()
            .filter(p -> filterLoai == null || filterLoai.equals(p.getMaLoaiPhong()))
            .filter(p -> filterTrangThai == null || filterTrangThai.equals(p.getTrangThai()))
            .collect(Collectors.toList());

        cardPanel.removeAll();

        if (filtered.isEmpty()) {
            JLabel lbl = new JLabel("Không có phòng nào phù hợp.", SwingConstants.CENTER);
            lbl.setFont(new Font("Arial", Font.ITALIC, 14));
            lbl.setForeground(Color.GRAY);
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            cardPanel.add(Box.createVerticalStrut(30));
            cardPanel.add(lbl);
        } else {
            Map<String, List<Phong>> byFloor = new LinkedHashMap<>();
            for (Phong p : filtered) {
                String floor = extractFloor(p.getMaPhong());
                byFloor.computeIfAbsent(floor, k -> new ArrayList<>()).add(p);
            }

            for (Map.Entry<String, List<Phong>> entry : byFloor.entrySet()) {
                JPanel floorRow = new JPanel(new BorderLayout(10, 0));
                floorRow.setBackground(Color.WHITE);
                floorRow.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));

                JLabel lblFloor = new JLabel("Lầu " + entry.getKey());
                lblFloor.setFont(new Font("Arial", Font.BOLD, 16));
                lblFloor.setForeground(new Color(0, 80, 200));
                lblFloor.setPreferredSize(new Dimension(70, 40));
                lblFloor.setVerticalAlignment(SwingConstants.TOP);

                JPanel grid = new JPanel(new GridLayout(0, 4, 10, 10));
                grid.setBackground(Color.WHITE);
                for (Phong p : entry.getValue())
                    grid.add(buildRoomCard(p));

                floorRow.add(lblFloor, BorderLayout.WEST);
                floorRow.add(grid, BorderLayout.CENTER);
                cardPanel.add(floorRow);
                cardPanel.add(Box.createVerticalStrut(4));
            }
        }

        cardPanel.revalidate();
        cardPanel.repaint();
    }

    private JPanel buildRoomCard(Phong p) {
        boolean occupied = "Đang Thuê".equals(p.getTrangThai());
        boolean dirty    = "Chưa Dọn".equals(p.getTrangThai());

        Color bg = occupied ? new Color(210, 45, 45)
                 : dirty    ? new Color(230, 140, 0)
                 :            new Color(34, 160, 60);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(bg);
        card.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JLabel lblMa = new JLabel(p.getMaPhong(), SwingConstants.CENTER);
        lblMa.setFont(new Font("Arial", Font.BOLD, 15));
        lblMa.setForeground(Color.WHITE);
        lblMa.setAlignmentX(Component.CENTER_ALIGNMENT);

        String tenLoai = loaiPhongMap.getOrDefault(p.getMaLoaiPhong(), p.getMaLoaiPhong());
        JLabel lblKieu = cardLabel("Kiểu phòng:  " + p.getTenPhong());
        JLabel lblLoai = cardLabel("Loại phòng:   " + tenLoai);
        JLabel lblTT   = cardLabel("Trạng thái:     " + p.getTrangThai());

        JButton btn;
        if (occupied) {
            btn = new JButton("Thanh toán");
            btn.setBackground(new Color(30, 70, 200));
            btn.setForeground(Color.WHITE);
        } else {
            btn = new JButton("Đặt phòng");
            btn.setBackground(Color.WHITE);
            btn.setForeground(Color.DARK_GRAY);
        }
        btn.setFont(new Font("Arial", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        card.add(lblMa);
        card.add(Box.createVerticalStrut(8));
        card.add(lblKieu);
        card.add(lblLoai);
        card.add(lblTT);
        card.add(Box.createVerticalStrut(8));
        card.add(btn);
        return card;
    }

    private JLabel cardLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.PLAIN, 12));
        lbl.setForeground(Color.WHITE);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private String extractFloor(String maPhong) {
        for (char c : maPhong.toCharArray())
            if (Character.isDigit(c)) return String.valueOf(c);
        return "?";
    }

    // ─── TAB 2: TABLE VIEW ───────────────────────────────────────────────────

    private JPanel buildTableTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        UIFactory.styleMainPanel(panel);

        JPanel topPanel = new JPanel(new GridLayout(2, 4, 20, 15));
        UIFactory.styleFormPanel(topPanel);
        topPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            "Cấu hình Phòng", 0, 0,
            new Font("Arial", Font.BOLD, 12), new Color(100, 100, 100)));

        txtMaPhong  = new JTextField();
        txtTenPhong = new JTextField();
        cbLoaiPhong = new JComboBox<>();
        loadLoaiPhong();
        cbTrangThai = new JComboBox<>(new String[]{"Trống", "Đang Thuê", "Chưa Dọn"});

        topPanel.add(new JLabel("Mã Phòng:")); topPanel.add(txtMaPhong);
        topPanel.add(new JLabel("Tên Phòng:")); topPanel.add(txtTenPhong);
        topPanel.add(new JLabel("Loại Phòng:")); topPanel.add(cbLoaiPhong);
        topPanel.add(new JLabel("Trạng Thái:")); topPanel.add(cbTrangThai);

        String[] cols = {"Mã Phòng", "Tên Phòng", "Loại Phòng", "Trạng Thái"};
        tableModel = new DefaultTableModel(cols, 0);
        table = new JTable(tableModel);
        UIFactory.styleTable(table);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int m = table.convertRowIndexToModel(row);
                txtMaPhong.setText(tableModel.getValueAt(m, 0).toString());
                txtMaPhong.setEditable(false);
                txtTenPhong.setText(tableModel.getValueAt(m, 1).toString());
                String maLoai = tableModel.getValueAt(m, 2).toString();
                for (int i = 0; i < cbLoaiPhong.getItemCount(); i++) {
                    if (cbLoaiPhong.getItemAt(i).startsWith(maLoai)) {
                        cbLoaiPhong.setSelectedIndex(i); break;
                    }
                }
                cbTrangThai.setSelectedItem(tableModel.getValueAt(m, 3).toString());
            }
        });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) moFormSuaPhong();
            }
        });

        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        pnlSearch.setBackground(new Color(245, 246, 250));
        JTextField txtSearch = new JTextField(20);
        pnlSearch.add(new JLabel("Tìm kiếm:"));
        pnlSearch.add(txtSearch);
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String t = txtSearch.getText();
                sorter.setRowFilter(t.isBlank() ? null : RowFilter.regexFilter("(?i)" + t));
            }
        });

        JPanel pnlNorth = new JPanel(new BorderLayout());
        pnlNorth.setBackground(new Color(245, 246, 250));
        pnlNorth.add(topPanel, BorderLayout.NORTH);
        pnlNorth.add(pnlSearch, BorderLayout.SOUTH);

        JPanel pnlTableWrap = new JPanel(new BorderLayout());
        UIFactory.styleFormPanel(pnlTableWrap);
        pnlTableWrap.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        pnlTableWrap.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel pnlCenter = new JPanel(new BorderLayout(0, 15));
        pnlCenter.setBackground(new Color(245, 246, 250));
        pnlCenter.add(pnlNorth, BorderLayout.NORTH);
        pnlCenter.add(pnlTableWrap, BorderLayout.CENTER);

        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panelBottom.setBackground(new Color(245, 246, 250));

        JButton btnLamMoi = UIFactory.createActionButton("Bỏ chọn",    new Color(240, 240, 240), Color.BLACK);
        JButton btnThem   = UIFactory.createActionButton("Thêm Phòng", new Color(40, 167, 69),   Color.WHITE);
        JButton btnSua    = UIFactory.createActionButton("Sửa Phòng",  new Color(24, 144, 255),  Color.WHITE);
        JButton btnXoa    = UIFactory.createActionButton("Xóa Phòng",  new Color(220, 53, 69),   Color.WHITE);

        btnLamMoi.addActionListener(e -> {
            txtMaPhong.setText(""); txtMaPhong.setEditable(true);
            txtTenPhong.setText("");
            if (cbLoaiPhong.getItemCount() > 0) cbLoaiPhong.setSelectedIndex(0);
            cbTrangThai.setSelectedIndex(0);
            table.clearSelection();
        });

        btnThem.addActionListener(e -> {
            String selLP = (String) cbLoaiPhong.getSelectedItem();
            String maLP  = selLP != null ? selLP.split(" - ")[0] : "";
            Phong p = new Phong(txtMaPhong.getText(), txtTenPhong.getText(), maLP,
                    cbTrangThai.getSelectedItem().toString());
            if (dao.themPhong(p)) {
                JOptionPane.showMessageDialog(panel, "Đã thêm mới phòng.");
                loadDataToTable(); loadCardView();
            } else {
                JOptionPane.showMessageDialog(panel, "Phòng đã tồn tại hoặc mã lỗi!");
            }
        });

        btnSua.addActionListener(e -> moFormSuaPhong());

        btnXoa.addActionListener(e -> {
            if (txtMaPhong.getText().isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Vui lòng chọn phòng cần xóa!");
                return;
            }
            if (dao.xoaPhong(txtMaPhong.getText())) {
                JOptionPane.showMessageDialog(panel, "Phòng đã bị xóa.");
                loadDataToTable(); loadCardView();
            } else {
                JOptionPane.showMessageDialog(panel, "Không thể xóa! Phòng này đang có lịch sử đặt phòng.");
            }
        });

        panelBottom.add(btnLamMoi); panelBottom.add(btnThem);
        panelBottom.add(btnSua); panelBottom.add(btnXoa);

        panel.add(pnlCenter, BorderLayout.CENTER);
        panel.add(panelBottom, BorderLayout.SOUTH);
        return panel;
    }

    private void moFormSuaPhong() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Vui lòng chọn phòng cần sửa!"); return; }
        int m = table.convertRowIndexToModel(row);
        String maPhong   = tableModel.getValueAt(m, 0).toString();
        String tenPhong  = tableModel.getValueAt(m, 1).toString();
        String maLoai    = tableModel.getValueAt(m, 2).toString();
        String trangThai = tableModel.getValueAt(m, 3).toString();

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
            "Sửa thông tin Phòng", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(450, 240);
        dialog.setLocationRelativeTo(this);

        JPanel pnlForm = new JPanel(new GridLayout(4, 2, 10, 10));
        pnlForm.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

        JTextField fldTen = new JTextField(tenPhong);
        JComboBox<String> fldLoai = new JComboBox<>();
        for (LoaiPhong lp : loaiPhongDAO.getAll())
            fldLoai.addItem(lp.getMaLoaiPhong() + " - " + lp.getTenLoaiPhong());
        for (int i = 0; i < fldLoai.getItemCount(); i++)
            if (fldLoai.getItemAt(i).startsWith(maLoai)) { fldLoai.setSelectedIndex(i); break; }

        JComboBox<String> fldTT = new JComboBox<>(new String[]{"Trống", "Đang Thuê", "Chưa Dọn"});
        fldTT.setSelectedItem(trangThai);

        pnlForm.add(new JLabel("Mã Phòng:"));  pnlForm.add(new JLabel(maPhong));
        pnlForm.add(new JLabel("Tên Phòng:"));  pnlForm.add(fldTen);
        pnlForm.add(new JLabel("Loại Phòng:")); pnlForm.add(fldLoai);
        pnlForm.add(new JLabel("Trạng Thái:")); pnlForm.add(fldTT);

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLuu = UIFactory.createActionButton("Lưu", new Color(24, 144, 255), Color.WHITE);
        JButton btnHuy = UIFactory.createActionButton("Hủy", new Color(240, 240, 240), Color.BLACK);
        pnlBtn.add(btnHuy); pnlBtn.add(btnLuu);

        btnLuu.addActionListener(ev -> {
            String selLP = (String) fldLoai.getSelectedItem();
            String maLP  = selLP != null ? selLP.split(" - ")[0] : "";
            Phong p = new Phong(maPhong, fldTen.getText(), maLP, fldTT.getSelectedItem().toString());
            if (dao.capNhatPhong(p)) {
                JOptionPane.showMessageDialog(dialog, "Đã cập nhật thông tin phòng.");
                loadDataToTable(); loadCardView();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Không thể cập nhật!");
            }
        });
        btnHuy.addActionListener(ev -> dialog.dispose());

        dialog.add(pnlForm, BorderLayout.CENTER);
        dialog.add(pnlBtn, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void loadLoaiPhong() {
        cbLoaiPhong.removeAllItems();
        for (LoaiPhong lp : loaiPhongDAO.getAll())
            cbLoaiPhong.addItem(lp.getMaLoaiPhong() + " - " + lp.getTenLoaiPhong());
    }

    private void loadDataToTable() {
        tableModel.setRowCount(0);
        for (Phong p : dao.getAll())
            tableModel.addRow(new Object[]{p.getMaPhong(), p.getTenPhong(), p.getMaLoaiPhong(), p.getTrangThai()});
    }
}
