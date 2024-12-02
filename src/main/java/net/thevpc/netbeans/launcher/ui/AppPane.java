/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.netbeans.launcher.ui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

import net.thevpc.netbeans.launcher.service.NetbeansLauncherModule;
import net.thevpc.netbeans.launcher.model.LongOperation;
import net.thevpc.netbeans.launcher.model.LongOperationListener;
import net.thevpc.netbeans.launcher.ui.utils.BoxH;
import net.thevpc.netbeans.launcher.ui.utils.Direction;
import net.thevpc.netbeans.launcher.ui.utils.SwingToolkit;
import net.thevpc.netbeans.launcher.ui.utils.SwingUtils2;
import net.thevpc.netbeans.launcher.util.NbUtils;
import net.thevpc.nuts.NApp;

/**
 * @author thevpc
 */
public abstract class AppPane extends JPanel {
    private static class Comps {
        JComponent headerComponent;
        JComponent footerComponent;
        JComponent mainComponent;
        JComponent[] buttons;
        protected BoxH header;
        protected BoxH footer;
        protected JProgressBar jpb;
    }

    protected Comps compactComp;
    protected Comps nonCompactComp;
    protected MainWindowSwing win;
    protected SwingToolkit toolkit;
    protected NetbeansLauncherModule configService;
    protected AppPaneType paneType;
    protected AppPanePos pos;

    public AppPane(AppPaneType paneType, AppPanePos pos, MainWindowSwing win) {
        super(new BorderLayout());
        this.win = win;
        this.pos =pos;
        this.paneType = paneType;
        this.toolkit = win.getToolkit();
        this.configService = win.configService;
    }

    public SwingToolkit getNbToolkit() {
        return toolkit;
    }

    public AppPanePos getPos() {
        return pos;
    }

    public AppPaneType getPaneType() {
        return paneType;
    }

    private JComponent createNbIconView() {
        JLabel icon = new JLabel(SwingUtils2.loadIcon("nb.png", 32));
        NbUtils.installMoveWin(icon, win.frame);
//        icon.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent mouseEvent) {
//                if (mouseEvent.getButton() == 1) {
//                    if (mouseEvent.getClickCount() == 2) {
//                        win.confirmAndExit();
//                    }
//                }
//            }
//        });
        return icon;
    }

    public void build() {
        Comps c = null;
        if (win.isCompact()) {
            if (compactComp == null) {
                compactComp = build0(true);
            }
            c = compactComp;
        } else {
            if (nonCompactComp == null) {
                nonCompactComp = build0(false);
            }
            c = nonCompactComp;
        }
        this.removeAll();
        if(c.headerComponent!=null) {
            this.add(c.headerComponent, BorderLayout.NORTH);
        }
        this.add(c.mainComponent, BorderLayout.CENTER);
        this.add(c.footerComponent, BorderLayout.SOUTH);
        updateAll();
        this.invalidate();
        this.revalidate();
    }

    public BoxH getHeader() {
        if (win.isCompact()) {
            return compactComp.header;
        }
        return nonCompactComp.header;
    }

    private Comps build0(boolean compact) {
        Comps c = new Comps();
        int vgap = 1;//compact ? 1 : 10;
        int hgap = 1;//compact ? 1 : 5;
        c.header = SwingUtils2.boxH().setVgap(vgap).setHgap(hgap).setName(getPaneType()+"-header");
//        c.header.setBackground("333333");
//        c.header.setBackground("336699");
        c.header.setOpaque(false);
        JComponent[] buttonComponents = c.buttons = createButtons(compact);
        c.header.addAll(buttonComponents);
//        c.header.addGlueH();
//        c.header.add(createNbIconView());

        vgap = 1;//compact ? 1 : 4;
        hgap = 1;//compact ? 1 : 3;
        c.footer = SwingUtils2.boxH().setVgap(vgap).setHgap(hgap).setName(getPaneType()+"-footer");
        c.jpb = new JProgressBar();
        configService.rt().addOperationListener(new LongOperationListener() {
            @Override
            public void onLongOperationProgress(LongOperation operation) {
                updateProgressbar();
            }
        });
        c.footer.addExpandH(c.jpb);
//        c.footer.addGlueH();
        c.footer.setBackground(SwingUtils2.componentGradientPaint("d6d9df","dfe2e8", Direction.BOTTOM));
        JLabel link = new JLabel();
        link.setText("v" + NApp.of().getVersion().get());
        link.setForeground(SwingUtils2.color("0095c9"));
        link.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(new URI("http://github.com/thevpc/netbeans-launcher"));
                    }
                } catch (Exception ex) {
                    Logger.getLogger(MainWindowSwing.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        //            stack.getChildren().addAll(link);
        //            stack.setAlignment(Pos.CENTER_RIGHT);     // Right-justify nodes in stack
        // Add to HBox from Example 1-2
        c.footer.add(link); // Add to HBox from Example 1-2

        //TODO
//        final JComponent hc = c.header.toComponent();
//        NbUtils.installMoveWin(hc, win.frame);
//        c.headerComponent = hc;

        final JComponent fc = c.footer.toComponent();
        NbUtils.installMoveWin(fc, win.frame);
        c.footerComponent = fc;
        c.mainComponent = createMain(compact);
        return c;
    }

    private void updateProgressbar() {
        if (compactComp != null && compactComp.jpb != null) {
            updateProgressbar(compactComp.jpb);
        }
        if (nonCompactComp != null && nonCompactComp.jpb != null) {
            updateProgressbar(nonCompactComp.jpb);
        }
    }

    private static class LongOperationTracker{
        private long lastTime;
        private int period=3;
        private LongOperation lastLongOperation;

        private LongOperation next(LongOperation[] operations){
            long now=System.currentTimeMillis();
            if(now-lastTime<period*1000){
                for (LongOperation operation : operations) {
                    if (lastLongOperation == operation) {
                        return lastLongOperation;
                    }
                }
            }
            int r=(int)(Math.random()*(operations.length));
            lastLongOperation=operations[r];
            lastTime=now;
            return lastLongOperation;
        };

    }
    private LongOperationTracker tracker=new LongOperationTracker();
    private void updateProgressbar(JProgressBar jpb) {
        LongOperation[] operations = configService.rt().getOperations();
        DecimalFormat df=new DecimalFormat("00.0");
        if (operations.length > 0) {
            if (operations.length == 1) {
                LongOperation o = operations[0];
                jpb.setIndeterminate(o.isIndeterminate());
                jpb.setValue((int) o.getPercent());
                jpb.setStringPainted(true);
                if(o.getName()==null) {
                    jpb.setString(df.format(o.getPercent()));
                }else{
                    jpb.setString(df.format(o.getPercent()) + "% " + o.getName());
                }
            } else {
                double percents=0;
                int determinateCount=0;
                for (LongOperation operation : operations) {
                    if(!operation.isIndeterminate()) {
                        percents += operation.getPercent();
                        determinateCount++;
                    }
                }
                if(determinateCount>1){
                    percents=percents/determinateCount;
                }
                jpb.setIndeterminate(determinateCount==0);
                jpb.setStringPainted(true);
                LongOperation o = tracker.next(operations);
                jpb.setValue((int) percents);
                if(o.getName()==null) {
                    jpb.setString(operations.length + " task(s) : "+df.format(o.getPercent()));
                }else{
                    jpb.setString(operations.length + " task(s) : "+df.format(o.getPercent()) + "% " + o.getName());
                }
            }

        } else {
            jpb.setValue(0);
            jpb.setIndeterminate(false);
            jpb.setStringPainted(false);
            jpb.setString("");
        }
    }

    public void onSelectedPane() {
        onRefreshHeader();
    }

    public void onRefreshHeader() {
    }

    public void onInit() {
    }

    public void updateAll() {

    }

    public void onPreChangeCompatStatus(boolean compact) {

    }

    public void onChangeCompatStatus(boolean compact) {
        build();
        onRefreshHeader();
    }

    public abstract JComponent[] createButtons(boolean compact);

    public abstract JComponent createMain(boolean compact);
}
