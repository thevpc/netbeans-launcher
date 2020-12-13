/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.netbeans.launcher.ui.panes;

import java.awt.Color;
import java.awt.Component;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

import net.thevpc.netbeans.launcher.model.NetbeansLocation;
import net.thevpc.netbeans.launcher.model.NetbeansWorkspace;
import net.thevpc.netbeans.launcher.ui.AppPane;
import net.thevpc.netbeans.launcher.ui.AppPanePos;
import net.thevpc.netbeans.launcher.ui.AppPaneType;
import net.thevpc.netbeans.launcher.ui.MainWindowSwing;
import net.thevpc.netbeans.launcher.util.LocalDateTimePeriod;
import net.thevpc.netbeans.launcher.util.NbTheme;
import net.thevpc.netbeans.launcher.util.NbUtils;
import net.thevpc.netbeans.launcher.ui.*;
import net.thevpc.netbeans.launcher.ui.utils.CatalogComponent;
import net.thevpc.netbeans.launcher.ui.utils.ListComponent;
import net.thevpc.netbeans.launcher.ui.utils.SwingUtils2;
import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.netbeans.launcher.ui.utils.Equalizer;
import net.thevpc.netbeans.launcher.ui.utils.ObjectTableModel;
import net.thevpc.netbeans.launcher.ui.utils.TableComponent;

/**
 * @author thevpc
 */
public class NbListPane extends AppPane {

    protected final static Set<String> running = new HashSet<String>();

    public static boolean isStarted(NutsApplicationContext ctx, NetbeansWorkspace w) {
        String name = w.getName();
        synchronized (running) {
            if (running.contains(name)) {
                return true;
            }
        }
        return NbUtils.isRunningWithCache(ctx, w);
    }

    public static boolean setStopped(NutsApplicationContext ctx, NetbeansWorkspace w) {
        String name = w.getName();
        synchronized (running) {
            if (running.remove(name)) {
                return true;
            }
        }
        return false;
    }

    public static boolean setStarted(NutsApplicationContext ctx, NetbeansWorkspace w) {
        synchronized (running) {
            if (isStarted(ctx, w)) {
                String name = w.getName();
                running.add(name);
                return true;

            }
        }
        return false;
    }

    private static class Comps1 {

        CatalogComponent workspacesListView;
        JComponent buttonStart;
        JComponent buttonRemove;
        JComponent buttonCopy;
        JComponent buttonEdit;
        JComponent buttonSearchLocal;
        JComponent buttonAdd;
        JComponent[] buttons;
        JComponent main;
        boolean compact;
    }

    Comps1 compact;
    Comps1 nonCompact;

    public NbListPane(MainWindowSwing win) {
        super(AppPaneType.LIST_WS, new AppPanePos(0, 0), win);
        build();
    }

    private Comps1 createComps1(boolean compact) {
        Comps1 c = new Comps1();
        c.compact = compact;
        c.buttonAdd = toolkit.createIconButton("add", "App.Action.Add", () -> win.onAddWorkspace(null), compact);
        c.buttonRemove = toolkit.createIconButton("remove", "App.Action.Remove", () -> onRemoveWorkspace(), compact);
        c.buttonCopy = toolkit.createIconButton("copy", "App.Action.Copy", () -> onCopyWorkspace(), compact);
        c.buttonEdit = toolkit.createIconButton("edit", "App.Action.Edit", () -> win.onEditWorkspace(getSelectedWorkspace()), compact);
        c.buttonStart = toolkit.createIconButton("start", "App.Action.Start", () -> win.startWorkspace(getSelectedWorkspace()), compact);
        c.buttonSearchLocal = toolkit.createIconButton("settings", "App.Action.Settings", () -> win.setSelectedPane(AppPaneType.SETTINGS), compact);
        c.buttons = new JComponent[]{c.buttonStart, c.buttonAdd, c.buttonRemove, c.buttonEdit, c.buttonCopy, c.buttonSearchLocal};

        c.workspacesListView = new TableComponent();
        c.workspacesListView.setElementHeight(win.isCompact() ? 30 : 50);
        for (NetbeansWorkspace workspace : configService.getAllNbWorkspaces()) {
            c.workspacesListView.addValue(workspace);
        }
        c.workspacesListView.addEnterSelection((e) -> win.startWorkspace(getSelectedWorkspace()));
        if (c.workspacesListView instanceof ListComponent) {
            ((ListComponent) c.workspacesListView).getList().setCellRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    NetbeansWorkspace ws = (value instanceof NetbeansWorkspace) ? (NetbeansWorkspace) value : null;
                    String name = ws == null ? "" : ws.getName()
                            + (" (" + evalInstantRelative(ws.getLastLaunchDate(), ws.getExecutionCount()) + ")");
                    super.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
                    if (ws != null && isStarted(win.getAppContext(), ws)) {
                        if (!isSelected) {
                            setBackground(index % 2 == 0 ? Color.WHITE : SwingUtils2.color("f9f9f9"));
                        } else {
                            setBackground(SwingUtils2.color("0096c9"));
                        }
//                    if (!isSelected) {
//                        setBackground(index % 2 == 0 ? MainWindowSwingHelper.color("ffe4b4") : MainWindowSwingHelper.color("ffbd86"));
//                    } else {
//                        setBackground(MainWindowSwingHelper.color("ffdb9d"));
//                    }
                        setIcon(SwingUtils2.loadIcon("anb.png", win.isCompact() ? 16 : 32));
                    } else {
                        if (!isSelected) {
                            setBackground(index % 2 == 0 ? Color.WHITE : SwingUtils2.color("f9f9f9"));
                        } else {
                            setBackground(SwingUtils2.color("0096c9"));
                        }
                        setIcon(SwingUtils2.loadIcon("anbg.png", win.isCompact() ? 16 : 32));
                    }
                    return this;
                }
            });
        }
        if (c.workspacesListView instanceof TableComponent) {
            TableComponent a = (TableComponent) c.workspacesListView;
            NbUtils.onRunningNbProcessesChanged(() -> SwingUtilities.invokeLater(this::updateList));
            a.setColumns(new ObjectTableModel.NamedColumns<NetbeansWorkspace>(new String[]{"Workspace", "Since", "Times"}) {
                @Override
                public Object getValueAt(int row, String column, NetbeansWorkspace ws) {
                    switch (column) {
                        case "Workspace": {
                            return ws.getName();
                        }
                        case "Since": {
                            return evalInstantRelativeWhen(ws.getLastLaunchDate());
                        }
                        case "Times": {
                            long r = ws.getExecutionCount();
                            return r > 0 ? r : null;
                        }
                    }
                    return "";
                }

            }.setColumnSizes(new float[]{5, 2, 1}));
            a.getTable().getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    NetbeansWorkspace ws = (NetbeansWorkspace) a.getValue(row);
                    if (ws != null && isStarted(win.getAppContext(), ws)) {
//                        if (!isSelected) {
//                            setBackground(index % 2 == 0 ? Color.WHITE : SwingUtils2.color("f9f9f9"));
//                        } else {
//                            setBackground(SwingUtils2.color("0096c9"));
//                        }
                        setIcon(SwingUtils2.loadIcon("anb.png", win.isCompact() ? 24 : 48));
                    } else {
//                        if (!isSelected) {
//                            setBackground(index % 2 == 0 ? Color.WHITE : SwingUtils2.color("f9f9f9"));
//                        } else {
//                            setBackground(SwingUtils2.color("0096c9"));
//                        }
                        setIcon(SwingUtils2.loadIcon("anbg.png", win.isCompact() ? 24 : 48));
                    }
                    return this;
                }

            });
        }

        c.workspacesListView.addMouseSelection((e) -> {
            if (e.getMouseEvent().getButton() == 1) {
                if (e.getMouseEvent().getClickCount() == 2) {
                    win.startWorkspace(getSelectedWorkspace());
                }
            }
        });
        c.workspacesListView.addListSelectionListener((e) -> {
            onRequiredUpdateButtonStatuses();
        });
        c.main = NbTheme.prepare(new javax.swing.JScrollPane(c.workspacesListView.toComponent()));
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

    public void setSelectedWorkspace(NetbeansWorkspace w) {
        getComps1().workspacesListView.setSelectedValue(w);
    }

    public void setSelectedWorkspace(NetbeansLocation w) {
        //TODO
        //getComps1().workspacesListView.setSelectedValue(w);
    }

    public NetbeansWorkspace getSelectedWorkspace() {
        NetbeansWorkspace w = (NetbeansWorkspace) getComps1().workspacesListView.getSelectedValue();
        if (w != null) {
            return w;
        }
        return null;
    }

    public void updateList() {
        Equalizer name = (a, b) -> a != null && b != null && SwingUtils2.trim(((NetbeansWorkspace) a).getName())
                .equals(SwingUtils2.trim(((NetbeansWorkspace) b).getName()));
        toolkit.updateTable(
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
        NetbeansWorkspace w = getSelectedWorkspace();
        win.showConfirmOkCancel(
                toolkit.msg("App.RemoveConfiguration.Confirm.Title"),
                toolkit.msg("App.RemoveConfiguration.Confirm.Message"),
                () -> {
                    try {
                        configService.removeNbWorkspace(w);
                        getComps1().workspacesListView.removeValue(w);
                    } catch (Exception ex) {
                        toolkit.showError(toolkit.msg("App.RemoveWorkspace.Error"), ex);
                    }
                }
        );
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

    private String evalInstantRelativeWhen(Instant i) {
        if (i == null) {
            return "never executed";
        }
        Duration duration = Duration.between(i, Instant.now());
        if (duration.getSeconds() <= 60) {
            return "now";
        }
        LocalDateTimePeriod period = LocalDateTimePeriod.between(LocalDateTime.ofInstant(i, ZoneId.systemDefault()),
                LocalDateTime.now())
                .setSeconds(0).setMilliseconds(0);
        return period.toString()
                + " ago";
    }

    private String evalInstantRelativeHowMutch(long count) {
        return count == 1 ? "" : count == 1 ? "one time" : ("" + count + " times");
    }

    private void onCopyWorkspace() {
        try {
            NetbeansWorkspace w = getSelectedWorkspace();
            if (w != null) {
                win.setSelectedPane(AppPaneType.EDIT_WS);
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
        toolkit.setControlVisible(c.buttonStart, !(w == null || isStarted(win.getAppContext(), w)));
        toolkit.setControlVisible(c.buttonCopy, !(w == null));
        toolkit.setControlVisible(c.buttonEdit, !(w == null));
        toolkit.setControlVisible(c.buttonRemove, !(w == null));
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
