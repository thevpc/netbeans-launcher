/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.netbeans.launcher.ui.swing;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.vpc.app.netbeans.launcher.NetbeansConfigService;
import net.vpc.app.netbeans.launcher.ui.PaneType;
import net.vpc.app.netbeans.launcher.util.NbUtils;

/**
 *
 * @author vpc
 */
public abstract class AppPane extends JPanel {

    private static class Comps {

        JComponent headerComponent;
        JComponent footerComponent;
        JComponent mainComponent;
        JComponent[] buttons;
        protected MainWindowSwingHelper.BoxH header;
        protected MainWindowSwingHelper.BoxH footer;
    }

    protected Comps compactComp;
    protected Comps nonCompactComp;
    protected MainWindowSwing win;
    protected SwingToolkit toolkit;
    protected NetbeansConfigService configService;
    protected PaneType paneType;

    public AppPane(PaneType paneType, MainWindowSwing win) {
        super(new BorderLayout());
        this.win = win;
        this.paneType = paneType;
        this.toolkit = win.toolkit;
        this.configService = win.configService;
    }

    public PaneType getPaneType() {
        return paneType;
    }

    private JComponent createNbIconView() {
        JLabel icon = new JLabel(MainWindowSwingHelper.loadIcon("nb.png", 32));
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
        this.add(c.headerComponent, BorderLayout.NORTH);
        this.add(c.mainComponent, BorderLayout.CENTER);
        this.add(c.footerComponent, BorderLayout.SOUTH);
        updateAll();
        this.invalidate();
        this.revalidate();
    }

    private Comps build0(boolean compact) {
        Comps c = new Comps();
        int vgap = 1;//compact ? 1 : 10;
        int hgap = 1;//compact ? 1 : 5;
        c.header = MainWindowSwingHelper.boxH().setVgap(vgap).setHgap(hgap);
        c.header.setBackground("336699");
        c.header.addAll(c.buttons = createButtons(compact));
        c.header.addGlueH();
//        c.header.add(createNbIconView());

        vgap = 1;//compact ? 1 : 4;
        hgap = 1;//compact ? 1 : 3;
        c.footer = MainWindowSwingHelper.boxH().setVgap(vgap).setHgap(hgap);
        c.footer.addGlueH();
        c.footer.setBackground("DDDDDD");
        //            JPänel stack = new JPänel();
        JLabel link = new JLabel();
        link.setText("v"+win.getAppContext().getAppId().getVersion());
        link.setForeground(MainWindowSwingHelper.color("0095c9"));
        link.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(new URI("http://github.com/thevpc"));
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
//        c.footer.add(
//                toolkit.createIconButton("minimize-window", toolkit.msg("App.Action.MinimizeWindow").getText(), () -> win.frame.setState(Frame.ICONIFIED), compact)
//        );
//        if (compact) {
//            c.footer.add(
//                    toolkit.createIconButton("enlarge", toolkit.msg("App.Action.NonCompactMode").getText(), () -> win.setCompact(false), compact)
//            );
//        } else {
//            c.footer.add(
//                    toolkit.createIconButton("compress", toolkit.msg("App.Action.CompactMode").getText(), () -> win.setCompact(true), compact)
//            );
//        }
//        c.footer.add(
//                toolkit.createIconButton("exit", toolkit.msg("App.Action.Exit").getText(), () -> win.confirmAndExit(), compact)
//        );
        final JComponent hc = c.header.toComponent();
        final JComponent fc = c.footer.toComponent();
        NbUtils.installMoveWin(hc, win.frame);
        NbUtils.installMoveWin(fc, win.frame);
        c.headerComponent = hc;
        c.footerComponent = fc;
        c.mainComponent = createMain(compact);
        return c;
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
