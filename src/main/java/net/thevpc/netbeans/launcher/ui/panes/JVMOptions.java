/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.netbeans.launcher.ui.panes;

import net.thevpc.netbeans.launcher.ui.AppPane;
import net.thevpc.netbeans.launcher.ui.AppPanePos;
import net.thevpc.netbeans.launcher.ui.AppPaneType;
import net.thevpc.netbeans.launcher.ui.MainWindowSwing;
import net.thevpc.netbeans.launcher.ui.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author thevpc
 */
public class JVMOptions extends AppPane {
    private AppPaneType lastPane;
    private Consumer<Boolean> supp;

    private static class Comps1 {
        JComponent buttonOk;
        JComponent buttonCancel;
        JComponent buttonUp;
        JComponent buttonDown;
        JComponent buttonAdd;
        JComponent buttonRemove;
        JComponent[] buttons;
        JComponent main;
        JTable list = new JTable();
        JComboBox text = new JComboBox();
    }

    Comps1 compact;
    Comps1 nonCompact;

    public JVMOptions(MainWindowSwing win) {
        super(AppPaneType.JVM_OPTIONS,new AppPanePos(0,2), win);
        build();
    }

    private Comps1 createComps1(boolean compact) {
        Comps1 c = new Comps1();

        c.text.setEditable(true);
        DefaultComboBoxModel defaultComboBoxModel = new DefaultComboBoxModel();
        c.text.setModel(defaultComboBoxModel);
        defaultComboBoxModel.addElement("-Dawt.useSystemAAFontSettings=on");
        defaultComboBoxModel.addElement("-Dswing.aatext=true");
        defaultComboBoxModel.addElement("-J-Xms1g");
        defaultComboBoxModel.addElement("-J-Xmx4g");
        c.text.setSelectedItem("");

        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("");
        c.list.setModel(model);

        c.buttonUp = toolkit.createIconButton("up", "App.Action.Up", () -> onUp(), compact);
        c.buttonDown = toolkit.createIconButton("down", "App.Action.Down", () -> onDown(), compact);
        c.buttonAdd = toolkit.createIconButton("add", "App.Action.Add", () -> onAdd(), compact);
        c.buttonRemove = toolkit.createIconButton("remove", "App.Action.Remove", () -> onRemove(), compact);
        c.buttonOk = toolkit.createIconButton("ok", "App.Action.Ok", () -> onOk(), compact);
        c.buttonCancel = toolkit.createIconButton("close", "App.Action.Cancel", () -> onCancel(), compact);
        c.buttons = new JComponent[]{c.buttonAdd,c.buttonRemove,c.buttonUp,c.buttonDown,c.buttonOk,c.buttonCancel};
        JPanel p = new JPanel(new BorderLayout());
        p.add(c.text, BorderLayout.NORTH);
        p.add(new JScrollPane(c.list), BorderLayout.CENTER);
        c.main = p;
        return c;
    }


    public void init(AppPaneType lastType, Consumer<Boolean> supp) {
        this.lastPane = lastType;
        this.supp = supp;
        updateAll();
    }

    private void onUp() {
        swapIndices(getComps1().list.getSelectedRows(), -1);
    }

    private void onDown() {
        swapIndices(getComps1().list.getSelectedRows(), +1);
    }
    private void onAdd() {
        String txt = (String) getComps1().text.getSelectedItem();
        if (txt.length() > 0) {
            if (txt.equals("\"\"")) {
                txt = "";
            } else {
                //
            }
        } else {
            return;
        }
        getListModel().addRow(new Object[]{txt});
        updatedSelection(new int[]{getListModel().getRowCount() - 1});
        getComps1().text.setSelectedItem("");
        getComps1().text.requestFocus();
    }

    private void onRemove() {
        int[] ii = getComps1().list.getSelectedRows();
        int nextSel = -1;
        for (int i = ii.length - 1; i >= 0; i--) {
            if (nextSel != -1 && ii[i] - 1 >= 0) {
                nextSel = ii[i] - 1;
            }
            getListModel().removeRow(ii[i]);
        }
        if (nextSel == -1 && getListModel().getRowCount() > 0) {
            nextSel = 0;
        }
        if (nextSel != -1) {
            updatedSelection(new int[]{nextSel});
        }
    }
    private void onOk() {
        win.setSelectedPane(lastPane);
        supp.accept(true);
    }

    private void onCancel() {
        win.setSelectedPane(lastPane);
        supp.accept(false);
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
        toolkit.setControlVisible(c.buttonOk, true);
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
    }

    public void swapIndices(int[] ii, int delta) {
        List<Integer> next = new ArrayList<>();
        if (delta > 0) {
            for (int i = ii.length - 1; i >= 0; i--) {
                if (!swapIndices(ii[i], ii[i] + delta)) {
                    break;
                }
                if (ok(ii[i]) && ok(ii[i] + delta)) {
                    next.add(ii[i] + delta);
                }
            }
        } else {
            for (int i = 0; i < ii.length; i++) {
                if (!swapIndices(ii[i], ii[i] + delta)) {
                    break;
                }
                if (ok(ii[i]) && ok(ii[i] + delta)) {
                    next.add(ii[i] + delta);
                }
            }
        }
        updatedSelection(next.stream().mapToInt(i -> i).toArray());
    }
    protected void updatedSelection(int[] aa) {
        getComps1().list.getSelectionModel().clearSelection();
        for (int i = 0; i < aa.length; i++) {
            getComps1().list.getSelectionModel().addSelectionInterval(aa[i], aa[i]);
        }
        if (aa.length > 0) {
            getComps1().list.scrollRectToVisible(
                    getComps1().list.getCellRect(
                            aa[0],
                            0, true
                    )
            );
        }
    }

    public boolean swapIndices(int i, int j) {
        if (ok(i) && ok(j)
                && i != j) {
            String s = (String) getListModel().getValueAt(i, 0);
            getListModel().setValueAt(getListModel().getValueAt(j, 0), i, 0);
            getListModel().setValueAt(s, j, 0);
            updatedSelection(new int[]{j});
            return true;
        }
        return false;
    }
    public boolean ok(int i) {
        return i >= 0 && i < getListModel().getRowCount();
    }

    private DefaultTableModel getListModel() {
        return (DefaultTableModel) getComps1().list.getModel();
    }

    public void setArguments(String args) {
        DefaultTableModel m = getListModel();
        while (m.getRowCount() > 0) {
            m.removeRow(0);
        }
        for (String a : win.getAppContext().getWorkspace().commandLine().parse(args).toStringArray()) {
            m.addRow(new Object[]{a});
        }
    }

    public String getArguments() {
        DefaultTableModel m = getListModel();
        List<String> a = new ArrayList<>();
        for (int i = 0; i < m.getRowCount(); i++) {
            a.add("" + m.getValueAt(i, 0));

        }
        return win.getAppContext().getWorkspace().commandLine().create(a).toString();
    }
}
