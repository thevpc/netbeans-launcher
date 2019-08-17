/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.netbeans.launcher.ui.swing.panes;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import net.vpc.app.netbeans.launcher.model.NetbeansWorkspace;
import net.vpc.app.netbeans.launcher.ui.PaneType;
import net.vpc.app.netbeans.launcher.ui.swing.AppPane;
import net.vpc.app.netbeans.launcher.ui.swing.MainWindowSwing;
import net.vpc.app.netbeans.launcher.ui.swing.MainWindowSwingHelper;
import net.vpc.app.netbeans.launcher.ui.swing.SwingToolkit;
import net.vpc.app.netbeans.launcher.util.LocalDateTimePeriod;

/**
 *
 * @author vpc
 */
public class NbListPane extends AppPane {

    private static class Comps1 {

        JList workspacesListView;
        JComponent buttonStart;
        JComponent buttonRemove;
        JComponent buttonCopy;
        JComponent buttonEdit;
        JComponent buttonReconfigure;
        JComponent buttonAdd;
        JComponent[] buttons;
        JComponent main;
        DefaultListModel<NetbeansWorkspace> workspacesModel;
    }
    Comps1 compact;
    Comps1 nonCompact;

    public NbListPane(MainWindowSwing win) {
        super(PaneType.LIST_WS, win);
        build();
    }

    private Comps1 createComps1(boolean compact) {
        Comps1 c = new Comps1();
        c.buttonAdd = toolkit.createIconButton("add", "App.Action.Add", () -> win.onAddWorkspace(null), compact);
        c.buttonRemove = toolkit.createIconButton("remove", "App.Action.Remove", () -> onRemoveWorkspace(), compact);
        c.buttonCopy = toolkit.createIconButton("copy", "App.Action.Copy", () -> onCopyWorkspace(), compact);
        c.buttonEdit = toolkit.createIconButton("edit", "App.Action.Edit", () -> win.onEditWorkspace(getSelectedWorkspace()), compact);
        c.buttonStart = toolkit.createIconButton("start", "App.Action.Start", () -> win.startWorkspace(getSelectedWorkspace()), compact);
        c.buttonReconfigure = toolkit.createIconButton("settings", "App.Action.Settings", () -> win.setSelectedPane(PaneType.SETTINGS), compact);
        c.buttons = new JComponent[]{c.buttonStart, c.buttonAdd, c.buttonRemove, c.buttonEdit, c.buttonCopy, c.buttonReconfigure};

        c.workspacesModel = new DefaultListModel();
        for (NetbeansWorkspace workspace : configService.getAllNbWorkspaces()) {
            c.workspacesModel.addElement(workspace);
        }
        c.workspacesListView = new JList(c.workspacesModel);
        c.workspacesListView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        c.workspacesListView.setBorder(new EmptyBorder(2, 2, 2, 2));
        c.workspacesListView.setFixedCellHeight(compact ? 30 : 50);
        c.workspacesListView.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                NetbeansWorkspace ws = (value instanceof NetbeansWorkspace) ? (NetbeansWorkspace) value : null;
                String name = ws == null ? "" : ws.getName()
                        + (" (" + evalInstantRelative(ws.getLastLaunchDate(), ws.getExecutionCount()) + ")");
                super.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
                if (ws != null && win.isRunningWorkspace(ws.getName())) {
                    if (!isSelected) {
                        setBackground(index % 2 == 0 ? Color.WHITE : MainWindowSwingHelper.color("f9f9f9"));
                    } else {
                        setBackground(MainWindowSwingHelper.color("0096c9"));
                    }
//                    if (!isSelected) {
//                        setBackground(index % 2 == 0 ? MainWindowSwingHelper.color("ffe4b4") : MainWindowSwingHelper.color("ffbd86"));
//                    } else {
//                        setBackground(MainWindowSwingHelper.color("ffdb9d"));
//                    }
                    setIcon(MainWindowSwingHelper.loadIcon("running.png", win.isCompact() ? 16 : 32));
                } else {
                    if (!isSelected) {
                        setBackground(index % 2 == 0 ? Color.WHITE : MainWindowSwingHelper.color("f9f9f9"));
                    } else {
                        setBackground(MainWindowSwingHelper.color("0096c9"));
                    }
                    setIcon(MainWindowSwingHelper.loadIcon("not-running.png", win.isCompact() ? 16 : 32));
                }
                return this;
            }
        });
        c.workspacesListView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == 1) {
                    if (mouseEvent.getClickCount() == 2) {
                        win.startWorkspace(getSelectedWorkspace());
                    }
                }
            }
        });
        c.workspacesListView.addListSelectionListener((ListSelectionEvent e) -> {
            onRequiredUpdateButtonStatuses();
        });
        c.main = new javax.swing.JScrollPane(c.workspacesListView);
        return c;
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

    public NetbeansWorkspace getSelectedWorkspace() {
        NetbeansWorkspace w = (NetbeansWorkspace) getComps1().workspacesListView.getSelectedValue();
        if (w != null) {
            return w;
        }
        return null;
    }

    public void updateList() {
        SwingToolkit.Equilizer name = (a, b) -> a != null && b != null && ((NetbeansWorkspace) a).getName().equals(((NetbeansWorkspace) b).getName());
        toolkit.updateList(
                getComps1().workspacesListView, configService.getAllNbWorkspaces(),
                name,
                new Comparator<NetbeansWorkspace>() {
            @Override
            public int compare(NetbeansWorkspace a, NetbeansWorkspace b) {
                return instantFor(b).compareTo(instantFor(a));
            }

            public Instant instantFor(NetbeansWorkspace a) {
                if (a == null) {
                    return Instant.MIN;
                }
                if (a.getLastLaunchDate() != null) {
                    return a.getLastLaunchDate();
                }
                if (a.getCreationDate() != null) {
                    return a.getCreationDate();
                }
                return Instant.MIN;
            }
        });
    }

    private void onRemoveWorkspace() {
        try {
            NetbeansWorkspace w = getSelectedWorkspace();
            if (toolkit.showConfirm(
                    toolkit.msg("App.RemoveConfiguration.Confirm.Title"),
                    toolkit.msg("App.RemoveConfiguration.Confirm.Message")
            )) {
                configService.removeNbWorkspace(w);
                getComps1().workspacesModel.removeElement(w);
            }
        } catch (Exception ex) {
            toolkit.showError(toolkit.msg("App.RemoveWorkspace.Error"), ex);
        }
    }

    private String evalInstantRelative(Instant i, long count) {
        if (i == null) {
            return "never executed";
        }
        Duration duration = Duration.between(i, Instant.now());
        if (duration.getSeconds() <= 60) {
            return "now, " + (count == 1 ? "one time" : ("" + count + " times"));
        }
        LocalDateTimePeriod period = LocalDateTimePeriod.between(LocalDateTime.ofInstant(i, ZoneId.systemDefault()), 
                LocalDateTime.now())
                .setSeconds(0).setMilliseconds(0);
        return period.toString()
                + " ago, " + (count == 1 ? "one time" : ("" + count + " times"));
    }

    private void onCopyWorkspace() {
        try {
            NetbeansWorkspace w = getSelectedWorkspace();
            if (w != null) {
                win.setSelectedPane(PaneType.EDIT_WS);
                w = w.copy();
                w.setName(w.getName() + " Copy");
                win.onAddWorkspace(w);
            }
        } catch (Exception ex) {
            toolkit.showError(toolkit.msg("App.CopyWorkspace.Error"), ex);
        }
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
        NetbeansWorkspace w = getSelectedWorkspace();
        Comps1 c = getComps1();
        toolkit.setControlVisible(c.buttonStart, !(w == null || win.isRunningWorkspace(w.getName())));
        toolkit.setControlVisible(c.buttonCopy, !(w == null));
        toolkit.setControlVisible(c.buttonEdit, !(w == null));
        toolkit.setControlVisible(c.buttonRemove, !(w == null));
        c.workspacesListView.invalidate();
        c.workspacesListView.revalidate();
        c.workspacesListView.repaint();
    }

    @Override
    public void onInit() {
        onRequiredUpdateButtonStatuses();
    }

    @Override
    public void updateAll() {
        updateList();
    }

    private int cached_i;

    @Override
    public void onPreChangeCompatStatus(boolean compact) {
        super.onPreChangeCompatStatus(compact);
        cached_i = getComps1().workspacesListView.getSelectedIndex();
    }

    @Override
    public void onChangeCompatStatus(boolean compact) {
        super.onChangeCompatStatus(compact);
        getComps1().workspacesListView.setSelectedIndex(cached_i);
    }
}
