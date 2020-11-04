/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.netbeans.launcher.ui.panes;

import net.thevpc.netbeans.launcher.model.ConfirmResult;
import net.thevpc.netbeans.launcher.ui.AppPane;
import net.thevpc.netbeans.launcher.ui.AppPanePos;
import net.thevpc.netbeans.launcher.ui.AppPaneType;
import net.thevpc.netbeans.launcher.ui.MainWindowSwing;
import net.thevpc.netbeans.launcher.ui.utils.Direction;
import net.thevpc.netbeans.launcher.ui.utils.PaintablePanel;
import net.thevpc.netbeans.launcher.ui.utils.SwingToolkit;
import net.thevpc.netbeans.launcher.ui.utils.SwingUtils2;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * @author vpc
 */
public class ConfirmPane extends AppPane {
    private AppPaneType lastPane;
    private SwingToolkit.Message title;
    private SwingToolkit.Message desc;
    private Consumer<ConfirmResult> supp;
    private boolean yesNoCancel = false;

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

    Comps1 compact;
    Comps1 nonCompact;

    public ConfirmPane(MainWindowSwing win) {
        super(AppPaneType.CONFIRM, new AppPanePos(10, 10), win);
        build();
    }

    private Comps1 createComps1(boolean compact) {
        Comps1 c = new Comps1();
        c.buttonOk = toolkit.createIconButton("ok", "App.Action.Ok", () -> onOk(), compact);
        c.buttonYes = toolkit.createIconButton("yes", "App.Action.Yes", () -> onYes(), compact);
        c.buttonNo = toolkit.createIconButton("no", "App.Action.No", () -> onNo(), compact);
        c.buttonCancel = toolkit.createIconButton("close", "App.Action.Cancel", () -> onCancel(), compact);
        c.buttons = new JComponent[]{c.buttonOk, c.buttonCancel};
        c.title = new JLabel("Title");
        c.desc = new JLabel("Desc");
        c.title.setHorizontalAlignment(SwingConstants.CENTER);
        c.title.setFont(new Font("Arial", Font.BOLD, 16));
        c.title.setOpaque(false);

        c.desc.setHorizontalAlignment(SwingConstants.CENTER);
        c.desc.setVerticalAlignment(SwingConstants.CENTER);
        c.desc.setOpaque(false);
//        c.desc.setBorder(BorderFactory.createEtchedBorder());
        PaintablePanel p = new PaintablePanel(new BorderLayout());
        p.add(c.title, BorderLayout.NORTH);
        p.add(c.desc, BorderLayout.CENTER);
        p.setBackgroundPaint(SwingUtils2.componentGradientPaint("c4c7cc","d9dce1", Direction.BOTTOM));
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

    private Comps1 getComps1() {
        if (win.isCompact()) {
            if (compact == null) {
                compact = createComps1(true);
            }
            return compact;
        }
        if (nonCompact == null) {
            nonCompact = createComps1(false);
        }
        return nonCompact;
    }

    @Override
    public JComponent[] createButtons(boolean compact) {
        return getComps1().buttons;
    }

    @Override
    public JComponent createMain(boolean compact) {
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
        getComps1().desc.setText(desc == null ? "" : desc.getText());
    }
}
