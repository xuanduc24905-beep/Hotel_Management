package com.lotuslaverne.fx.views;

import com.lotuslaverne.dao.BangGiaDAO;
import com.lotuslaverne.dao.LoaiPhongDAO;
import com.lotuslaverne.entity.BangGia;
import com.lotuslaverne.entity.LoaiPhong;
import com.lotuslaverne.util.ConnectDB;
import java.sql.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import java.text.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class BangGiaView {
    private static final LinkedHashMap<String,String> KG = new LinkedHashMap<>();
    static { KG.put("NgayThuong","Ngày Thường"); KG.put("CuoiTuan","Cuối Tuần"); KG.put("LeTet","Lễ / Tết"); KG.put("CaoDiem","Cao Điểm"); }
    private static final LinkedHashMap<String,String> SM = new LinkedHashMap<>();
    static { SM.put("maBangGia","Mã BG"); SM.put("maLoaiPhong","Loại Phòng"); SM.put("loaiThue","Loại Thuê"); SM.put("kyGia","Kỳ Giá"); SM.put("donGia","Đơn Giá"); SM.put("ngayBatDau","Ngày BĐ"); SM.put("ngayKetThuc","Ngày KT"); }

    private ObservableList<Object[]> items;
    private TableView<Object[]> table;
    private String[] lpArr = {"LP01","LP02","LP03","LP04"};
    private String fKG=null, fLP=null, fLT=null, sCol="maBangGia";
    private boolean sAsc=true;
    private double kmPct=0; private String kmMa=null, kmTen=null;
    private Button btnClear;

    private static final String CB_STYLE="-fx-background-color:#FFFFFF;-fx-border-color:#D9D9D9;-fx-border-radius:5;-fx-background-radius:5;-fx-border-width:1;-fx-padding:5 8;-fx-pref-height:32;-fx-min-width:120;";
    private static final String CB_LABEL="-fx-font-size:11px;-fx-text-fill:#8C8C8C;-fx-padding:0 0 2 2;";

    private void loadKM(){
        kmPct=0;kmMa=null;kmTen=null;
        Connection c=ConnectDB.getInstance().getConnection(); if(c==null)return;
        try(PreparedStatement p=c.prepareStatement("SELECT TOP 1 maKhuyenMai,tenKhuyenMai,phanTramGiam FROM KhuyenMai WHERE CAST(GETDATE() AS DATE) BETWEEN ngayApDung AND ngayKetThuc ORDER BY phanTramGiam DESC");ResultSet r=p.executeQuery()){
            if(r.next()){kmMa=r.getString(1);kmTen=r.getString(2);kmPct=r.getDouble(3);}
        }catch(Exception ignored){}
    }

    public Node build(){
        try{List<LoaiPhong> l=new LoaiPhongDAO().getAll();if(!l.isEmpty())lpArr=l.stream().map(LoaiPhong::getMaLoaiPhong).toArray(String[]::new);}catch(Exception ignored){}
        loadKM();

        VBox root=new VBox(0); root.setStyle("-fx-background-color:#F0F2F5;");
        VBox content=new VBox(14); content.setPadding(new Insets(28)); content.setStyle("-fx-background-color:#F0F2F5;"); VBox.setVgrow(content,Priority.ALWAYS);

        VBox hdr=new VBox(4);
        Label t=new Label("Bảng Giá Phòng"); t.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:#1A1A2E;");
        Label s=new Label("Quản lý đơn giá theo loại phòng, loại thuê và kỳ giá"); s.setStyle("-fx-font-size:13px;-fx-text-fill:#8C8C8C;");
        hdr.getChildren().addAll(t,s);
        if(kmPct>0){Label km=new Label(String.format("🎁  KM: %s (%s) — Giảm %.0f%%",kmTen,kmMa,kmPct));km.setStyle("-fx-background-color:#FFF7E6;-fx-text-fill:#D48806;-fx-padding:8 14;-fx-background-radius:8;-fx-border-color:#FFD591;-fx-border-width:1;-fx-border-radius:8;-fx-font-size:12px;-fx-font-weight:bold;");hdr.getChildren().add(km);}

        // ── FLAT FILTER/SORT BAR ──
        HBox bar=new HBox(); bar.setAlignment(Pos.CENTER_LEFT); bar.setPadding(new Insets(12,20,12,20));
        bar.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:10;-fx-border-color:#E8E8E8;-fx-border-radius:10;-fx-border-width:1;");

        HBox filterGrp=new HBox(12); filterGrp.setAlignment(Pos.BOTTOM_LEFT);
        Label fIcon=new Label("⏳ Bộ lọc"); fIcon.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1A1A2E;-fx-padding:0 4 6 0;");
        VBox vKG=labeledCB("Kỳ giá",makeCB("Tất cả",KG.values(),e->{fKG=rKey(KG,((ComboBox<String>)e.getSource()).getValue());updClear();refresh();}));
        VBox vLP=labeledCB("Loại phòng",makeLPCB());
        VBox vLT=labeledCB("Loại thuê",makeCB("Tất cả",Arrays.asList("QuaDem","TheoNgay","TheoGio"),e->{fLT=((ComboBox<String>)e.getSource()).getValue();if("Tất cả".equals(fLT))fLT=null;updClear();refresh();}));

        btnClear=new Button("✕ Xóa lọc"); btnClear.setVisible(false); btnClear.setManaged(false);
        btnClear.setStyle("-fx-background-color:transparent;-fx-text-fill:#FF4D4F;-fx-border-color:#FF4D4F;-fx-border-radius:5;-fx-background-radius:5;-fx-padding:5 10;-fx-cursor:hand;-fx-font-size:11px;");
        btnClear.setOnAction(e->{fKG=null;fLP=null;fLT=null;
            ((ComboBox<String>)((VBox)vKG).getChildren().get(1)).setValue("Tất cả");
            ((ComboBox<String>)((VBox)vLP).getChildren().get(1)).setValue("Tất cả");
            ((ComboBox<String>)((VBox)vLT).getChildren().get(1)).setValue("Tất cả");
            updClear();refresh();});

        filterGrp.getChildren().addAll(fIcon,vKG,vLP,vLT,btnClear);

        Region spacer=new Region(); HBox.setHgrow(spacer,Priority.ALWAYS);

        HBox sortGrp=new HBox(10); sortGrp.setAlignment(Pos.BOTTOM_RIGHT);
        Label sIcon=new Label("⇅ Sắp xếp"); sIcon.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1A1A2E;-fx-padding:0 4 6 0;");

        ComboBox<String> cbSort=new ComboBox<>(); SM.values().forEach(v->cbSort.getItems().add(v)); cbSort.setValue("Mã BG"); cbSort.setStyle(CB_STYLE);
        cbSort.setOnAction(e->{for(Map.Entry<String,String> en:SM.entrySet())if(en.getValue().equals(cbSort.getValue())){sCol=en.getKey();break;}refresh();});
        VBox vSort=labeledCB("Tiêu chí",cbSort);

        ComboBox<String> cbDir=new ComboBox<>(); cbDir.getItems().addAll("Tăng dần (A→Z)","Giảm dần (Z→A)"); cbDir.setValue("Tăng dần (A→Z)"); cbDir.setStyle(CB_STYLE);
        cbDir.setOnAction(e->{sAsc="Tăng dần (A→Z)".equals(cbDir.getValue());refresh();});
        VBox vDir=labeledCB("Chiều",cbDir);

        sortGrp.getChildren().addAll(sIcon,vSort,vDir);
        bar.getChildren().addAll(filterGrp,spacer,sortGrp);

        HBox actRow=new HBox(10); actRow.setAlignment(Pos.CENTER_LEFT); actRow.setPadding(new Insets(4,0,0,0));
        Label hint=new Label("💡 Double-click dòng để sửa"); hint.setStyle("-fx-font-size:12px;-fx-text-fill:#8C8C8C;-fx-font-style:italic;");
        Region sp2=new Region(); HBox.setHgrow(sp2,Priority.ALWAYS);
        Button bR=actBtn("↻ Làm Mới","#8C8C8C"),bT=actBtn("＋ Thêm Mới","#52C41A"),bX=actBtn("🗑 Xóa","#FF4D4F");
        actRow.getChildren().addAll(hint,sp2,bR,bT,bX);

        items=FXCollections.observableArrayList(loadData()); table=buildTbl(); VBox.setVgrow(table,Priority.ALWAYS);
        table.setOnMouseClicked(e->{if(e.getClickCount()==2&&table.getSelectionModel().getSelectedItem()!=null)openDlg(table.getSelectionModel().getSelectedItem(),false);});
        bT.setOnAction(e->openDlg(null,true));
        bX.setOnAction(e->handleXoa());
        bR.setOnAction(e->{loadKM();refresh();com.lotuslaverne.fx.UiUtils.flashButton(bR,"✓ OK");});

        content.getChildren().addAll(hdr,bar,actRow,table);
        root.getChildren().add(content); VBox.setVgrow(root,Priority.ALWAYS);
        return root;
    }

    private void updClear(){boolean show=fKG!=null||fLP!=null||fLT!=null;btnClear.setVisible(show);btnClear.setManaged(show);}

    private TableView<Object[]> buildTbl(){
        TableView<Object[]> tb=new TableView<>(); tb.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tb.setStyle("-fx-background-color:#FFF;-fx-background-radius:10;-fx-border-radius:10;-fx-border-color:#E8E8E8;-fx-border-width:1;");
        String[] h={"Mã BG","Loại Phòng","Loại Thuê","Kỳ Giá","Đơn Giá","% Giảm KM","Giá Sau KM","Ngày BĐ","Ngày KT"};
        for(int i=0;i<h.length;i++){final int x=i;TableColumn<Object[],String> c=new TableColumn<>(h[i]);
            c.setCellValueFactory(p->new SimpleStringProperty(x<p.getValue().length&&p.getValue()[x]!=null?p.getValue()[x].toString():""));
            if(i==3)c.setCellFactory(tc->kgCell());if(i==4)c.setCellFactory(tc->prCell());if(i==5)c.setCellFactory(tc->kmPCell());if(i==6)c.setCellFactory(tc->kmSCell());
            tb.getColumns().add(c);}
        tb.setItems(items);tb.setPlaceholder(new Label("Không có dữ liệu."));return tb;
    }
    private TableCell<Object[],String> kgCell(){return new TableCell<>(){@Override protected void updateItem(String s,boolean e){super.updateItem(s,e);if(e||s==null){setText(null);setStyle("");return;}setText(s);String c;switch(s){case"Cuối Tuần":c="#E6F7FF;-fx-text-fill:#1890FF";break;case"Lễ / Tết":c="#FFF1F0;-fx-text-fill:#FF4D4F";break;case"Cao Điểm":c="#FFF7E6;-fx-text-fill:#D48806";break;default:c="#F6FFED;-fx-text-fill:#52C41A";}setStyle("-fx-background-color:"+c+";-fx-font-weight:bold;-fx-alignment:center;");}};}
    private TableCell<Object[],String> prCell(){return new TableCell<>(){@Override protected void updateItem(String s,boolean e){super.updateItem(s,e);setText(e?null:s);setStyle(e?"":"-fx-text-fill:#8C8C8C;"+(kmPct>0?"-fx-strikethrough:true;":""));}};}
    private TableCell<Object[],String> kmPCell(){return new TableCell<>(){@Override protected void updateItem(String s,boolean e){super.updateItem(s,e);setText(e?null:s);setStyle(e||s==null||"—".equals(s)?"":"-fx-text-fill:#D48806;-fx-font-weight:bold;");}};}
    private TableCell<Object[],String> kmSCell(){return new TableCell<>(){@Override protected void updateItem(String s,boolean e){super.updateItem(s,e);setText(e?null:s);setStyle(e?"":"-fx-font-weight:bold;-fx-text-fill:#FF4D4F;");}};}

    private void openDlg(Object[] row,boolean isNew){
        Stage d=new Stage();d.initModality(Modality.APPLICATION_MODAL);d.setTitle(isNew?"Thêm Bảng Giá":"Sửa Bảng Giá");d.setResizable(false);
        GridPane f=new GridPane();f.setHgap(12);f.setVgap(12);f.setPadding(new Insets(20));
        ColumnConstraints c1=new ColumnConstraints();c1.setPrefWidth(150);ColumnConstraints c2=new ColumnConstraints();c2.setPrefWidth(200);f.getColumnConstraints().addAll(c1,c2);
        TextField tMa=new TextField(row!=null?row[0].toString():"");tMa.setEditable(isNew);tMa.setStyle(fldS());
        ComboBox<String> cLP=new ComboBox<>();cLP.getItems().addAll(lpArr);cLP.setValue(row!=null?row[1].toString():lpArr[0]);cLP.setMaxWidth(1e9);
        ComboBox<String> cLT=new ComboBox<>();cLT.getItems().addAll("QuaDem","TheoNgay","TheoGio");cLT.setValue(row!=null?row[2].toString():"QuaDem");cLT.setMaxWidth(1e9);
        ComboBox<String> cKG=new ComboBox<>();KG.values().forEach(v->cKG.getItems().add(v));cKG.setValue(row!=null?row[3].toString():"Ngày Thường");cKG.setMaxWidth(1e9);
        TextField tG=new TextField(row!=null?row[4].toString().replaceAll("[^0-9.]",""):"");tG.setStyle(fldS());
        DatePicker dB=new DatePicker(LocalDate.now()),dK=new DatePicker(LocalDate.of(LocalDate.now().getYear(),12,31));dB.setMaxWidth(1e9);dK.setMaxWidth(1e9);
        int r=0;f.add(lbl("Mã bảng giá *:"),0,r);f.add(tMa,1,r++);f.add(lbl("Loại phòng *:"),0,r);f.add(cLP,1,r++);f.add(lbl("Loại thuê *:"),0,r);f.add(cLT,1,r++);
        f.add(lbl("Kỳ giá *:"),0,r);f.add(cKG,1,r++);f.add(lbl("Đơn giá (VNĐ) *:"),0,r);f.add(tG,1,r++);f.add(lbl("Ngày bắt đầu *:"),0,r);f.add(dB,1,r++);f.add(lbl("Ngày kết thúc *:"),0,r);f.add(dK,1,r++);

        Button bS=new Button(isNew?"Thêm":"Lưu");bS.setStyle("-fx-background-color:"+(isNew?"#52C41A":"#1890FF")+";-fx-text-fill:white;-fx-background-radius:6;-fx-padding:8 20;-fx-cursor:hand;");
        Button bC=new Button("Hủy");bC.setStyle("-fx-background-color:#F5F5F5;-fx-text-fill:#595959;-fx-background-radius:6;-fx-padding:8 20;-fx-cursor:hand;");bC.setOnAction(e->d.close());
        bS.setOnAction(e->{
            if(tMa.getText().trim().isEmpty()||tG.getText().trim().isEmpty()){new Alert(Alert.AlertType.WARNING,"Điền đầy đủ (*)").showAndWait();return;}
            double g;try{g=Double.parseDouble(tG.getText().trim());if(g<=0)throw new Exception();}catch(Exception x){new Alert(Alert.AlertType.ERROR,"Đơn giá phải > 0!").showAndWait();return;}
            if(dK.getValue()==null||dB.getValue()==null||!dK.getValue().isAfter(dB.getValue())){new Alert(Alert.AlertType.WARNING,"Ngày KT phải sau ngày BĐ!").showAndWait();return;}
            String kg=rKey(KG,cKG.getValue());if(kg==null)kg="NgayThuong";
            try{BangGia b=new BangGia(tMa.getText().trim(),cLP.getValue(),cLT.getValue(),kg,g,java.sql.Date.valueOf(dB.getValue()),java.sql.Date.valueOf(dK.getValue()));
                boolean ok=isNew?new BangGiaDAO().them(b):new BangGiaDAO().sua(b);if(ok){d.close();refresh();}else new Alert(Alert.AlertType.ERROR,"Lỗi dữ liệu.").showAndWait();
            }catch(Exception x){new Alert(Alert.AlertType.ERROR,"Lỗi DB: "+x.getMessage()).showAndWait();}});
        HBox br=new HBox(10,bC,bS);br.setAlignment(Pos.CENTER_RIGHT);br.setPadding(new Insets(0,20,16,20));
        VBox vr=new VBox(f,br);vr.setStyle("-fx-background-color:#FFF;");d.setScene(new Scene(vr,420,430));d.showAndWait();
    }

    private void handleXoa(){Object[] sel=table.getSelectionModel().getSelectedItem();if(sel==null){new Alert(Alert.AlertType.WARNING,"Chọn dòng cần xóa!").showAndWait();return;}
        new Alert(Alert.AlertType.CONFIRMATION,"Xóa \""+sel[0]+"\"?").showAndWait().ifPresent(b->{if(b==ButtonType.OK){try{if(new BangGiaDAO().xoa(sel[0].toString()))refresh();else new Alert(Alert.AlertType.ERROR,"Không thể xóa.").showAndWait();}catch(Exception x){new Alert(Alert.AlertType.ERROR,"Lỗi DB.").showAndWait();}}});}

    private void refresh(){items.setAll(loadData());}
    private List<Object[]> loadData(){
        List<Object[]> res=new ArrayList<>();DecimalFormat df=new DecimalFormat("#,### VNĐ");SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy");
        try{List<BangGia> list=new BangGiaDAO().getAllSorted(sCol,sAsc);
            if(fKG!=null)list=list.stream().filter(b->fKG.equals(b.getKyGia())).collect(Collectors.toList());
            if(fLP!=null)list=list.stream().filter(b->fLP.equals(b.getMaLoaiPhong())).collect(Collectors.toList());
            if(fLT!=null)list=list.stream().filter(b->fLT.equals(b.getLoaiThue())).collect(Collectors.toList());
            if(!list.isEmpty()){for(BangGia b:list){String tk=KG.getOrDefault(b.getKyGia(),b.getKyGia());String gm=kmPct>0?String.format("-%.0f%%",kmPct):"—";double sau=b.getDonGia()*(1-kmPct/100);
                res.add(new Object[]{b.getMaBangGia(),b.getMaLoaiPhong(),b.getLoaiThue(),tk,df.format(b.getDonGia()),gm,kmPct>0?df.format(sau):df.format(b.getDonGia()),b.getNgayBatDau()!=null?sdf.format(b.getNgayBatDau()):"",b.getNgayKetThuc()!=null?sdf.format(b.getNgayKetThuc()):""});}return res;}
        }catch(Exception ignored){}
        res.add(new Object[]{"BG001","LP01","QuaDem","Ngày Thường","500,000 VNĐ","—","500,000 VNĐ","01/01/2026","31/12/2026"});return res;
    }

    private VBox labeledCB(String label,ComboBox<String> cb){Label l=new Label(label);l.setStyle(CB_LABEL);VBox v=new VBox(2,l,cb);return v;}
    @SuppressWarnings("unchecked")
    private ComboBox<String> makeCB(String ph,Collection<String> vals,javafx.event.EventHandler<javafx.event.ActionEvent> handler){
        ComboBox<String> cb=new ComboBox<>();cb.getItems().add(ph);cb.getItems().addAll(vals);cb.setValue(ph);cb.setStyle(CB_STYLE);cb.setOnAction(handler);return cb;}
    private ComboBox<String> makeLPCB(){ComboBox<String> cb=new ComboBox<>();cb.getItems().add("Tất cả");cb.getItems().addAll(lpArr);cb.setValue("Tất cả");cb.setStyle(CB_STYLE);
        cb.setOnAction(e->{fLP=cb.getValue().equals("Tất cả")?null:cb.getValue();updClear();refresh();});return cb;}
    private String rKey(Map<String,String> m,String v){if(v==null||v.startsWith("Tất"))return null;for(Map.Entry<String,String> e:m.entrySet())if(e.getValue().equals(v))return e.getKey();return null;}
    private Button actBtn(String t,String c){Button b=new Button(t);b.setStyle("-fx-background-color:"+c+";-fx-text-fill:white;-fx-background-radius:8;-fx-padding:8 16;-fx-cursor:hand;-fx-font-size:12px;");return b;}
    private Label lbl(String t){Label l=new Label(t);l.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#595959;");return l;}
    private String fldS(){return"-fx-background-color:#FFF;-fx-border-color:#D9D9D9;-fx-border-radius:5;-fx-background-radius:5;-fx-border-width:1;-fx-padding:7 10;";}
}
