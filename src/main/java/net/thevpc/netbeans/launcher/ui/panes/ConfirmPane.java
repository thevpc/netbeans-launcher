/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.netbeans.launcher.ui.panes;

import net.thevpc.netbeans.launcher.model.ConfirmResult;
import net.thevpc.netbeans.launcher.ui.*;
import net.thevpc.netbeans.launcher.ui.utils.Direction;
import net.thevpc.netbeans.launcher.ui.utils.PaintablePanel;
import net.thevpc.netbeans.launcher.ui.utils.SwingToolkit;
import net.thevpc.netbeans.launcher.ui.utils.SwingUtils2;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author thevpc
 */
public class ConfirmPane extends AppPane {

    private AppPaneType lastPane;
    private SwingToolkit.Message title;
    private SwingToolkit.Message desc;
    private Consumer<ConfirmResult> supp;
    private boolean yesNoCancel = false;
    private boolean regularButtons = true;

    private static class Comps1 {

        JComponent buttonOk;
        JComponent buttonYes;
        JComponent buttonNo;
        JLabel title;
        JLabel desc;
        JComponent buttonCancel;
        JComponent[] buttons;
        JComponent main;
    }
    protected Map<FrameInfo, Comps1> cachedComps1=new HashMap<>();

    private Comps1 getComps1() {
        return cachedComps1.computeIfAbsent(toolkit.getFrameInfo(),k->createComps1(k));
    }

    public ConfirmPane(MainWindowSwing win) {
        super(AppPaneType.CONFIRM, new AppPanePos(10, 10), win);
        build();
    }

    private Comps1 createComps1(FrameInfo compact) {
        Comps1 c = new Comps1();
        c.buttonOk = toolkit.createIconButton("ok", "App.Action.Ok", () -> onOk());
        c.buttonYes = toolkit.createIconButton("yes", "App.Action.Yes", () -> onYes());
        c.buttonNo = toolkit.createIconButton("no", "App.Action.No", () -> onNo());
        c.buttonCancel = toolkit.createIconButton("close", "App.Action.Cancel", () -> onCancel());
        if(regularButtons) {
            c.buttons = new JComponent[]{};
        }else{
            c.buttons = new JComponent[]{c.buttonOk, c.buttonCancel};
        }
        c.title = toolkit.createLabel();
        c.title.setText("Title");
        c.desc = toolkit.createLabel();
        c.desc.setText("Desc");
        c.title.setHorizontalAlignment(SwingConstants.CENTER);
        c.title.setFont(new Font("Arial", Font.BOLD,(int) toolkit.fontSize(16)));
        c.title.setOpaque(false);

        c.desc.setHorizontalAlignment(SwingConstants.CENTER);
        c.desc.setVerticalAlignment(SwingConstants.CENTER);
        c.desc.setOpaque(false);
//        c.desc.setBorder(BorderFactory.createEtchedBorder());
        PaintablePanel p = new PaintablePanel(new BorderLayout());
        p.add(c.title, BorderLayout.NORTH);
        p.add(c.desc, BorderLayout.CENTER);
        if(regularButtons) {
            Box hb = Box.createHorizontalBox();
            hb.add(Box.createHorizontalGlue());
            hb.add(c.buttonOk);
            hb.add(Box.createRigidArea(new Dimension(10, 20)));
            hb.add(c.buttonYes);
            hb.add(Box.createRigidArea(new Dimension(10, 20)));
            hb.add(c.buttonNo);
            hb.add(Box.createRigidArea(new Dimension(10, 20)));
            hb.add(c.buttonCancel);
            hb.add(Box.createHorizontalGlue());
            p.add(hb, BorderLayout.SOUTH);
        }
        p.setBackgroundPaint(SwingUtils2.componentGradientPaint("c4c7cc", "d9dce1", Direction.BOTTOM));
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        p.setOpaque(true);
        c.main = new JScrollPane(p);
        c.main.setBackground(Color.WHITE);
        return c;
    }

    public void initOkCancel(AppPaneType lastType, SwingToolkit.Message title, SwingToolkit.Message desc, Consumer<ConfirmResult> supp) {
        this.lastPane = lastType;
        this.title = title;
        this.desc = desc;
        this.supp = supp;
        this.yesNoCancel = false;
        updateAll();
    }

    public void initYesNoCancel(AppPaneType lastType, SwingToolkit.Message title, SwingToolkit.Message desc, Consumer<ConfirmResult> supp) {
        this.lastPane = lastType;
        this.title = title;
        this.desc = desc;
        this.supp = supp;
        this.yesNoCancel = true;
        updateAll();
    }

    private void onOk() {
        win.setSelectedPane(lastPane);
        supp.accept(ConfirmResult.OK);
    }

    private void onYes() {
        win.setSelectedPane(lastPane);
        supp.accept(ConfirmResult.YES);
    }

    private void onNo() {
        win.setSelectedPane(lastPane);
        supp.accept(ConfirmResult.NO);
    }

    private void onCancel() {
        win.setSelectedPane(lastPane);
        supp.accept(ConfirmResult.CANCEL);
    }

    @Override
    public JComponent[] createButtons(FrameInfo compact) {
        return getComps1().buttons;
    }

    @Override
    public JComponent createMain(FrameInfo compact) {
        return getComps1().main;
    }

    @Override
    public void onRefreshHeader() {
        onRequiredUpdateButtonStatuses();
    }

    protected void onRequiredUpdateButtonStatuses() {
        Comps1 c = getComps1();
        toolkit.setControlVisible(c.buttonOk, !yesNoCancel);
        toolkit.setControlVisible(c.buttonYes, yesNoCancel);
        toolkit.setControlVisible(c.buttonNo, yesNoCancel);
        toolkit.setControlVisible(c.buttonCancel, true);
        c.main.invalidate();
        c.main.revalidate();
        c.main.repaint();
    }

    @Override
    public void onInit() {
        onRequiredUpdateButtonStatuses();
    }

    @Override
    public void updateAll() {
        onRequiredUpdateButtonStatuses();
        getComps1().title.setText(title == null ? "Attention" : title.getText());
        String t = desc == null ? "" : desc.getText();
        if (t.indexOf("\n") >= 0) {
            if (t.toLowerCase().indexOf("<html>") < 0) {
                t = t.replaceAll("<", "&lt;");
                t = t.replaceAll(">", "&gt;");
                t = t.replaceAll("\n", "<br>");
                t = "<html>" + t + "</html>";
            }
        }
        getComps1().desc.setText(t);
    }
}
