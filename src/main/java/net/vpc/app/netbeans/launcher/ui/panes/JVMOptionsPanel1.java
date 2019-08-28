/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.netbeans.launcher.ui.panes;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import net.vpc.app.netbeans.launcher.ui.utils.SwingToolkit;
import net.vpc.app.nuts.NutsWorkspace;

/**
 *
 * @author vpc
 */
public class JVMOptionsPanel1 extends JPanel {

    private JTable list = new JTable();
    private JComboBox text = new JComboBox();
    private JComponent remove;
    private JComponent add;
    private JComponent up;
    private JComponent down;
    private NutsWorkspace ws;
    private Component parent;
    private SwingToolkit toolkit;

    public JVMOptionsPanel1(NutsWorkspace ws, Component parent, SwingToolkit toolkit) {
        super(new BorderLayout());
        this.ws = ws;
        this.toolkit = toolkit;
        this.parent = parent;
        text.setEditable(true);
        DefaultComboBoxModel defaultComboBoxModel = new DefaultComboBoxModel();
        text.setModel(defaultComboBoxModel);
        defaultComboBoxModel.addElement("-Dawt.useSystemAAFontSettings=on");
        defaultComboBoxModel.addElement("-Dswing.aatext=true");
        defaultComboBoxModel.addElement("-J-Xms1g");
        defaultComboBoxModel.addElement("-J-Xmx4g");
        text.setSelectedItem("");
        add = toolkit.createIconButton("add", "App.Action.Add", () -> onAdd(), true);
        remove = toolkit.createIconButton("remove", "App.Action.Remove", () -> onRemove(), true);
        up = toolkit.createIconButton("up", "App.Action.Up", () -> onUp(), true);
        down = toolkit.createIconButton("down", "App.Action.Down", () -> onDown(), true);
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("");
        list.setModel(model);
        Box buttons = Box.createVerticalBox();
        buttons.add(up);
        buttons.add(down);
        buttons.add(remove);
        buttons.add(add);
        add(buttons, BorderLayout.EAST);
        add(text, BorderLayout.SOUTH);
        add(new JScrollPane(list), BorderLayout.CENTER);
    }

    private void onAdd() {
        String txt = (String) text.getSelectedItem();
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
        text.setSelectedItem("");
        text.requestFocus();
    }

    private void onRemove() {
        int[] ii = list.getSelectedRows();
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

    public boolean showDialog() {
        if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(parent, this, "JVM Options", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
            return true;
        }
        return false;
    }

    public boolean ok(int i) {
        return i >= 0 && i < getListModel().getRowCount();
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
        list.getSelectionModel().clearSelection();
        for (int i = 0; i < aa.length; i++) {
            list.getSelectionModel().addSelectionInterval(aa[i], aa[i]);
        }
        if (aa.length > 0) {
            list.scrollRectToVisible(
                    list.getCellRect(
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

    public void onUp() {
        swapIndices(list.getSelectedRows(), -1);
    }

    public void onDown() {
        swapIndices(list.getSelectedRows(), +1);
    }

    public void setArguments(String args) {
        DefaultTableModel m = getListModel();
        while (m.getRowCount() > 0) {
            m.removeRow(0);
        }
        for (String a : ws.commandLine().parse(args).toArray()) {
            m.addRow(new Object[]{a});
        }
    }

    public String getArguments() {
        DefaultTableModel m = getListModel();
        List<String> a = new ArrayList<>();
        for (int i = 0; i < m.getRowCount(); i++) {
            a.add("" + m.getValueAt(i, 0));

        }
        return ws.commandLine().create(a).toString();
    }

    private DefaultTableModel getListModel() {
        return (DefaultTableModel) list.getModel();
    }

}
