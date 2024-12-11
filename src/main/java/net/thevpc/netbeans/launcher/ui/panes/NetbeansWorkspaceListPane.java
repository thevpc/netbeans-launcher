/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.netbeans.launcher.ui.panes;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import net.thevpc.netbeans.launcher.model.NetbeansInstallation;

import net.thevpc.netbeans.launcher.model.NetbeansLocation;
import net.thevpc.netbeans.launcher.model.NetbeansWorkspace;
import net.thevpc.netbeans.launcher.ui.*;
import net.thevpc.netbeans.launcher.util.LocalDateTimePeriod;
import net.thevpc.netbeans.launcher.util.NbTheme;
import net.thevpc.netbeans.launcher.util.NbUtils;
import net.thevpc.netbeans.launcher.ui.utils.CatalogComponent;
import net.thevpc.netbeans.launcher.ui.utils.ListComponent;
import net.thevpc.netbeans.launcher.ui.utils.SwingUtils2;
import net.thevpc.netbeans.launcher.ui.utils.Equalizer;
import net.thevpc.netbeans.launcher.ui.utils.ObjectTableModel;
import net.thevpc.netbeans.launcher.ui.utils.TableComponent;
import net.thevpc.nuts.util.NStringUtils;

/**
 * @author thevpc
 */
public class NetbeansWorkspaceListPane extends AppPane {

    protected final static Set<String> running = new HashSet<String>();

    public static boolean isStarted(NetbeansWorkspace w) {
        String name = w.getName();
        synchronized (running) {
            if (running.contains(name)) {
                return true;
            }
        }
        return NbUtils.isRunningWithCache(w);
    }

    public static boolean setStopped(NetbeansWorkspace w) {
        String name = w.getName();
        synchronized (running) {
            if (running.remove(name)) {
                return true;
            }
        }
        return false;
    }

    public static boolean setStarted(NetbeansWorkspace w) {
        synchronized (running) {
            if (isStarted(w)) {
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
        FrameInfo compact;
    }

    protected Map<FrameInfo, Comps1> cachedComps1=new HashMap<>();

    public NetbeansWorkspaceListPane(MainWindowSwing win) {
        super(AppPaneType.LIST_WS, new AppPanePos(0, 0), win);
        build();
    }

    private Comps1 createComps1(FrameInfo compact) {
        Comps1 c = new Comps1();
        c.compact = compact;
        c.buttonAdd = toolkit.createIconButton("add", "App.Action.Add", () -> win.onAddWorkspace(null));
        c.buttonRemove = toolkit.createIconButton("remove", "App.Action.Remove", () -> onRemoveWorkspace());
        c.buttonCopy = toolkit.createIconButton("copy", "App.Action.Copy", () -> onCopyWorkspace());
        c.buttonEdit = toolkit.createIconButton("edit", "App.Action.Edit", () -> win.onEditWorkspace(getSelectedWorkspace()));
        c.buttonStart = toolkit.createIconButton("start", "App.Action.Start", () -> win.startWorkspace(getSelectedWorkspace()));
        c.buttonSearchLocal = toolkit.createIconButton("settings", "App.Action.Settings", () -> win.setSelectedPane(AppPaneType.SETTINGS));
        c.buttons = new JComponent[]{c.buttonStart, c.buttonAdd, c.buttonRemove, c.buttonEdit, c.buttonCopy, c.buttonSearchLocal};

        c.workspacesListView = new TableComponent();
        c.workspacesListView.setElementHeight(win.isCompact() ? 30 : 50);
        for (NetbeansWorkspace workspace : configService.ws().findNetbeansWorkspaces()) {
            c.workspacesListView.addValue(workspace);
        }
        c.workspacesListView.addEnterSelection((e) -> win.startWorkspace(getSelectedWorkspace()));
        if (c.workspacesListView instanceof ListComponent) {
            JList li = ((ListComponent) c.workspacesListView).getList();
            Font initialFont = li.getFont();
            li.setCellRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    NetbeansWorkspace ws = (value instanceof NetbeansWorkspace) ? (NetbeansWorkspace) value : null;
                    String name = ws == null ? "" : ws.getName()
                            + (" (" + evalInstantRelative(ws.getLastLaunchDate(), ws.getExecutionCount()) + ")");
                    super.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
                    if (ws != null && isStarted(ws)) {
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
                        setIcon(SwingUtils2.loadIcon("anb.png", toolkit.iconSize()));
                    } else {
                        if (!isSelected) {
                            setBackground(index % 2 == 0 ? Color.WHITE : SwingUtils2.color("f9f9f9"));
                        } else {
                            setBackground(SwingUtils2.color("0096c9"));
                        }
                        setIcon(SwingUtils2.loadIcon("anbg.png", toolkit.iconSize()));
                    }
                    setFont(toolkit.deriveFont(initialFont));
                    return this;
                }
            });
        }
        if (c.workspacesListView instanceof TableComponent) {
            TableComponent a = (TableComponent) c.workspacesListView;
            NbUtils.onRunningNbProcessesChanged(() -> SwingUtilities.invokeLater(() -> this.updateList(null)));
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
            Font initialFont = a.getTable().getFont();
            a.getTable().getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    NetbeansWorkspace ws = (NetbeansWorkspace) a.getValue(row);
                    if (ws != null && isStarted(ws)) {
//                        if (!isSelected) {
//                            setBackground(index % 2 == 0 ? Color.WHITE : SwingUtils2.color("f9f9f9"));
//                        } else {
//                            setBackground(SwingUtils2.color("0096c9"));
//                        }
                        setIcon(SwingUtils2.loadIcon("anb.png", toolkit.iconSize()));
                    } else {
//                        if (!isSelected) {
//                            setBackground(index % 2 == 0 ? Color.WHITE : SwingUtils2.color("f9f9f9"));
//                        } else {
//                            setBackground(SwingUtils2.color("0096c9"));
//                        }
                        setIcon(SwingUtils2.loadIcon("anbg.png", toolkit.iconSize()));
                    }
                    setFont(toolkit.deriveFont(initialFont));
                    return this;
                }

            });
            a.getTable().getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    setFont(toolkit.deriveFont(initialFont));
                    return this;
                }
            });
            a.getTable().getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    setFont(toolkit.deriveFont(initialFont));
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
        return cachedComps1.computeIfAbsent(toolkit.getFrameInfo(),k->createComps1(k));
    }

    public void setSelectedWorkspace(NetbeansWorkspace w) {
        getComps1().workspacesListView.setSelectedValue(w);
    }

    public void setSelectedWorkspace(NetbeansLocation w) {
        setSelectedWorkspace(w, true);
    }

    public boolean setSelectedWorkspace(NetbeansLocation w, boolean create) {
        if (w != null) {
            if (w instanceof NetbeansInstallation) {
                NetbeansInstallation ii = (NetbeansInstallation) w;
                final NetbeansWorkspace[] v = getWorkspaces();
                for (int i = 0; i < v.length; i++) {
                    NetbeansWorkspace ws = v[i];
                    String p = ws.getPath();
                    final String pp = ii.getPath();
                    if (Objects.equals(pp, p)) {
                        int index = getComps1().workspacesListView.indexOf(a
                                -> Objects.equals(((NetbeansWorkspace) a).getPath(),
                                        ii.getPath())
                        );
                        if (index >= 0) {
                            getComps1().workspacesListView.setSelectedIndex(index);
                            return true;
                        } else {
                            index = getComps1().workspacesListView.indexOf(a
                                    -> Objects.equals(((NetbeansWorkspace) a).getName(),
                                            ii.getName())
                            );
                            if (index >= 0) {
                                getComps1().workspacesListView.setSelectedIndex(index);
                                return true;
                            }
                        }
                        return false;
                    }
                }
                if (create) {
                    configService.ws().addNetbeansWorkspace(ii);
                    updateList(() -> {
                        setSelectedWorkspace(w, false);
                    });
                    return false;
                }
            }
        }
        //TODO
        //getComps1().workspacesListView.setSelectedValue(w);
        return false;
    }

    public NetbeansWorkspace[] getWorkspaces() {
        return getComps1().workspacesListView.getValues().toArray(new NetbeansWorkspace[0]);
    }

    public NetbeansWorkspace getSelectedWorkspace() {
        NetbeansWorkspace w = (NetbeansWorkspace) getComps1().workspacesListView.getSelectedValue();
        if (w != null) {
            return w;
        }
        return null;
    }

    public void updateList(Runnable onFinish) {
        Equalizer name = (a, b) -> a != null && b != null && NStringUtils.trim(((NetbeansWorkspace) a).getName())
                .equals(NStringUtils.trim(((NetbeansWorkspace) b).getName()));
        toolkit.updateTable(
                getComps1().workspacesListView, configService.ws().findNetbeansWorkspaces(),
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
        }, onFinish);
    }

    private void onRemoveWorkspace() {
        NetbeansWorkspace w = getSelectedWorkspace();
        win.showConfirmOkCancel(
                toolkit.msg("App.RemoveConfiguration.Confirm.Title"),
                toolkit.msg("App.RemoveConfiguration.Confirm.Message"),
                () -> {
                    try {
                        configService.ws().removeNetbeansWorkspace(w);
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
        NetbeansWorkspace w = getSelectedWorkspace();
        Comps1 c = getComps1();
        toolkit.setControlVisible(c.buttonStart, !(w == null || isStarted(w)));
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
        updateList(null);
    }

    private int cached_i;

    @Override
    public void onPreChangeCompatStatus(FrameInfo compact) {
        super.onPreChangeCompatStatus(compact);
        cached_i = getComps1().workspacesListView.getSelectedIndex();
    }

    @Override
    public void onChangeCompatStatus(FrameInfo compact) {
        super.onChangeCompatStatus(compact);
        getComps1().workspacesListView.setSelectedIndex(cached_i);
    }
}
