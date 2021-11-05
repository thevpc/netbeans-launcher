/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.netbeans.launcher.ui.panes;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;

/**
 *
 * @author thevpc
 */
public class JVMOptionsPanel2 extends JPanel {

    private JList list = new JList();
    private JButton remove = new JButton("-");
    private JButton add = new JButton("+");
    private JButton up = new JButton("up");
    private JButton down = new JButton("down");
    private JTextField text = new JTextField("");
    private NutsSession ws;
    private Component parent;

    public JVMOptionsPanel2(NutsSession ws, Component parent) {
        super(new BorderLayout());
        this.ws = ws;
        this.parent = parent;
        list.setModel(new DefaultListModel<>());
        remove.addActionListener((e) -> {
            int[] ii = list.getSelectedIndices();
            int nextSel = -1;
            for (int i = ii.length - 1; i >= 0; i--) {
                if (nextSel != -1 && ii[i] - 1 >= 0) {
                    nextSel = ii[i] - 1;
                }
                getListModel().remove(ii[i]);
            }
            if (nextSel == -1 && getListModel().size() > 0) {
                nextSel = 0;
            }
            if (nextSel != -1) {
                updatedSelection(new int[]{nextSel});
            }
        });
        add.addActionListener((e) -> {
            String txt = text.getText();
            if (txt.length() > 0) {
                if (txt.equals("\"\"")) {
                    txt = "";
                } else {
                    //
                }
            } else {
                return;
            }
            getListModel().addElement(txt);
            updatedSelection(new int[]{getListModel().size() - 1});
            text.setText("");
            text.requestFocus();
        });
        up.addActionListener((e) -> moveUpIndex());
        down.addActionListener((e) -> moveDownIndex());
        Box buttons = Box.createVerticalBox();
        buttons.add(up);
        buttons.add(down);
        buttons.add(remove);
        buttons.add(add);
        add(buttons, BorderLayout.EAST);
        add(text, BorderLayout.SOUTH);
        add(new JScrollPane(list), BorderLayout.CENTER);
    }

    public boolean showDialog() {
        if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(parent, this)) {
            return true;
        }
        return false;
    }

    public boolean ok(int i) {
        return i >= 0 && i < getListModel().size();
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
        list.setSelectedIndices(aa);
        if (aa.length > 0) {
            list.scrollRectToVisible(
                    list.getCellBounds(
                            aa[0],
                            aa[aa.length - 1]
                    )
            );
        }
    }

    public boolean swapIndices(int i, int j) {
        if (ok(i) && ok(j)
                && i != j) {
            String s = getListModel().get(i);
            getListModel().set(i, getListModel().get(j));
            getListModel().set(j, s);
            updatedSelection(new int[]{j});
            return true;
        }
        return false;
    }

    public void moveUpIndex() {
        swapIndices(list.getSelectedIndices(), -1);
    }

    public void moveDownIndex() {
        swapIndices(list.getSelectedIndices(), +1);
    }

    public void setArguments(String args) {
        DefaultListModel<String> m = getListModel();
        m.clear();
        for (String a : NutsCommandLine.parse(args,ws).toStringArray()) {
            m.addElement(a);
        }
    }

    public String getArguments() {
        DefaultListModel<String> m = getListModel();
        List<String> a = new ArrayList<>();
        for (Object object : m.toArray()) {
            a.add(String.valueOf(object));
        }
        return NutsCommandLine.of(a,ws).toString();
    }

    private DefaultListModel<String> getListModel() {
        return (DefaultListModel<String>) list.getModel();
    }

}
