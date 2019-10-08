/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.netbeans.launcher.ui.panes;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;

import net.vpc.app.netbeans.launcher.model.NetbeansBinaryLink;
import net.vpc.app.netbeans.launcher.model.NetbeansInstallation;
import net.vpc.app.netbeans.launcher.model.NetbeansInstallationStore;
import net.vpc.app.netbeans.launcher.ui.*;
import net.vpc.app.netbeans.launcher.ui.utils.JdkJlistToStringer;
import net.vpc.app.netbeans.launcher.ui.utils.SwingUtils2;
import net.vpc.app.netbeans.launcher.util.JlistToStringer;
import net.vpc.app.netbeans.launcher.util.Workers;
import net.vpc.app.nuts.NutsSdkLocation;

/**
 * @author vpc
 */
public class SettingsPane extends AppPane {

    private static class Comp2 {

        private JList jdkListView;
        private JList localNbListView;
        private JList remoteNbListView;
        private JTabbedPane tabbedPane;
        private JComponent buttonAdd;
        private JComponent buttonRemove;
        private JComponent buttonSearchLocal;
        private JComponent buttonSearchRemote;
        private JComponent buttonDownload;
        private JComponent buttonClose;
        private JComponent[] buttons;
        private JComponent main;
    }

    private Comp2 compact;
    private Comp2 nonCompact;
    private JlistToStringer jdkStringer = new  JdkJlistToStringer();
    private static final Set<String> downloading = new HashSet<>();
    private JlistToStringer nbStringer = new JlistToStringer(2) {
        @Override
        public String toString(Object value, int level) {
            if (value instanceof NetbeansInstallation) {
                NetbeansInstallation i = (NetbeansInstallation) value;
                boolean _downloading = false;
                if (i.getStore() == NetbeansInstallationStore.DEFAULT) {
                    synchronized (downloading) {
                        if (downloading.contains(i.getVersion())) {
                            _downloading = true;
                        }
                    }
                }
                if (win.isCompact()) {
                    switch (level) {
                        case 0: {
                            return i.getName();
                        }
                        case 1: {
                            switch (i.getStore()) {
                                case USER: {
                                    return i.getName() + " (" + i.getPath() + ")";
                                }
                                case SYSTEM: {
                                    return i.getName() + " (system)";
                                }
                                case DEFAULT: {
                                    return i.getName() + (_downloading ? " (downloading...)" : "");
                                }
                            }
                            return i.getName() + " (" + i.getPath() + ")";
                        }
                    }
                } else {
                    switch (i.getStore()) {
                        case USER: {
                            return i.getName() + " (" + i.getPath() + ")";
                        }
                        case SYSTEM: {
                            return i.getName() + " (system)";
                        }
                        case DEFAULT: {
                            return i.getName() + (_downloading ? "(downloading...)" : "");
                        }
                    }
                    return i.getName() + " (" + i.getPath() + ")";
                }
            }
            return String.valueOf(value);
        }

    };
    private JlistToStringer nbLinkStringer = new JlistToStringer(2) {
        @Override
        public String toString(Object value, int level) {
            if (value instanceof NetbeansBinaryLink) {
                NetbeansBinaryLink i = (NetbeansBinaryLink) value;
                boolean _downloading = false;
                synchronized (downloading) {
                    if (downloading.contains(i.getVersion())) {
                        _downloading = true;
                    }
                }
                return "Netbeans " + i.getVersion() + (_downloading ? " (downloading...)" : "");
            }
            return String.valueOf(value);
        }

    };

    public SettingsPane(MainWindowSwing win) {
        super(AppPaneType.SETTINGS,new AppPanePos(2,0), win);
        build();
    }

    @Override
    public JComponent createMain(boolean compact) {
        return getComps2().main;
    }

    public JList createJdkList() {
        JList jdkListView = new JList(new DefaultListModel());
        jdkListView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jdkListView.setBorder(new EmptyBorder(2, 2, 2, 2));
        jdkListView.setFixedCellHeight(win.isCompact() ? 30 : 50);
        jdkListView.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, jdkStringer.toString(list, value), index, isSelected, cellHasFocus);
                if (!isSelected) {
                    setBackground(index % 2 == 0 ? Color.WHITE : SwingUtils2.color("f9f9f9"));
                } else {
                    setBackground(SwingUtils2.color("0096c9"));
                }
                return this;
            }
        });
        jdkListView.addListSelectionListener((ListSelectionEvent e) -> {
            onRequiredUpdateButtonStatuses();
        });
        return jdkListView;
    }

    public JList createLocalNbList() {
        JList nbListView = new JList(new DefaultListModel());
        nbListView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        nbListView.setBorder(new EmptyBorder(2, 2, 2, 2));
        nbListView.setFixedCellHeight(win.isCompact() ? 30 : 50);
        nbListView.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

                super.getListCellRendererComponent(list, nbStringer.toString(list, value), index, isSelected, cellHasFocus);
                if (!isSelected) {
                    setBackground(index % 2 == 0 ? Color.WHITE : SwingUtils2.color("f9f9f9"));
                } else {
                    setBackground(SwingUtils2.color("0096c9"));
                }
                return this;
            }
        });
        nbListView.addListSelectionListener((ListSelectionEvent e) -> {
            onRequiredUpdateButtonStatuses();
        });
        return nbListView;
    }

    public JList createRemoteNbList() {
        JList nbListView = new JList(new DefaultListModel());
        nbListView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        nbListView.setBorder(new EmptyBorder(2, 2, 2, 2));
        nbListView.setFixedCellHeight(win.isCompact() ? 30 : 50);
        nbListView.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

                super.getListCellRendererComponent(list, nbLinkStringer.toString(list, value), index, isSelected, cellHasFocus);
                if (!isSelected) {
                    setBackground(index % 2 == 0 ? Color.WHITE : SwingUtils2.color("f9f9f9"));
                } else {
                    setBackground(SwingUtils2.color("0096c9"));
                }
                return this;
            }
        });
        nbListView.addListSelectionListener((ListSelectionEvent e) -> {
            onRequiredUpdateButtonStatuses();
        });
        nbListView.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                if (evt.getClickCount() == 2) {
                    int index = list.locationToIndex(evt.getPoint());
                    if (index >= 0) {
                        onDownload();
                    }
                }
            }
        });
        SwingUtils2.addEnterAction(nbListView, () -> onDownload());
        return nbListView;
    }

    protected SettingType getSettingType() {
        switch (getComps2().tabbedPane.getSelectedIndex()) {
            case 0: {
                return SettingType.NB_INSTALLATION;
            }
            case 1: {
                return SettingType.JDK_INSTALLATION;
            }
            case 2: {
                return SettingType.NB_REMOTE;
            }
        }
        return SettingType.JDK_INSTALLATION;
    }

    protected void onRequiredUpdateButtonStatuses() {
        switch (getSettingType()) {
            case NB_INSTALLATION: {
                //netbeans installations
                toolkit.setControlVisible(getComps2().buttonSearchLocal, true);
                toolkit.setControlVisible(getComps2().buttonSearchRemote, false);
                toolkit.setControlVisible(getComps2().buttonDownload, false);
                toolkit.setControlVisible(getComps2().buttonAdd, true);
                toolkit.setControlVisible(getComps2().buttonRemove, getSelectedNbInstallation() != null);
                break;
            }
            case JDK_INSTALLATION: {
                //jdk installations
                toolkit.setControlVisible(getComps2().buttonAdd, true);
                toolkit.setControlVisible(getComps2().buttonRemove, getSelectedJdkLocation() != null);
                toolkit.setControlVisible(getComps2().buttonSearchLocal, true);
                toolkit.setControlVisible(getComps2().buttonSearchRemote, false);
                toolkit.setControlVisible(getComps2().buttonDownload, false);
                break;
            }
            case NB_REMOTE: {
                //remote nb installations
                toolkit.setControlVisible(getComps2().buttonAdd, false);
                toolkit.setControlVisible(getComps2().buttonRemove, false);
                toolkit.setControlVisible(getComps2().buttonSearchLocal, false);
                toolkit.setControlVisible(getComps2().buttonSearchRemote, false);
                toolkit.setControlVisible(getComps2().buttonDownload, getSelectedRemoteNbInstallation() != null);
                break;
            }
        }
//        NetbeansWorkspace w = getSelectedWorkspace();
    }

    @Override
    public JComponent[] createButtons(boolean compact) {
        return getComps2().buttons;
    }

    private void onAdd() {
        try {
            switch (getSettingType()) {
                case JDK_INSTALLATION: {
                    JFileChooser c = new JFileChooser();
                    c.setCurrentDirectory(configService.getCurrentDirectory());
                    c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    c.setAcceptAllFileFilterUsed(false);
                    if (JFileChooser.APPROVE_OPTION == c.showDialog(this, toolkit.msg("App.Workspace.SelectJDK").getText())) {
                        final File f = c.getSelectedFile();
                        if (f != null) {
                            configService.setCurrentDirectory(f);
                            NutsSdkLocation loc = configService.detectJdk(f.getPath());
                            if (loc != null) {
                                configService.addJdk(loc);
                                updateJdkList();
                            } else {
                                toolkit.showError(toolkit.msg("App.JdkInstalltion.Error"));
                            }
                        }
                    }
                    break;
                }

                case NB_INSTALLATION: {
                    JFileChooser c = new JFileChooser();
                    c.setCurrentDirectory(configService.getCurrentDirectory());
                    c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    c.setAcceptAllFileFilterUsed(false);
                    if (JFileChooser.APPROVE_OPTION == c.showDialog(this, toolkit.msg("App.Workspace.SelectNetbeans").getText())) {
                        final File f = c.getSelectedFile();
                        if (f != null) {
                            configService.setCurrentDirectory(f);
                            NetbeansInstallation ni = configService.findNb(f.getPath());
                            if (ni == null) {
                                ni = configService.detectNb(f.getPath(), NetbeansInstallationStore.USER);
                            }
                            NetbeansInstallation[] loc = configService.detectNbs(f.getPath(), true, ni == null ? NetbeansInstallationStore.USER : ni.getStore());
                            if (loc.length > 0) {
                                configService.saveFile();
                                for (AppPane pane : win.getPanes()) {
                                    pane.updateAll();
                                }
//                                updateNbList();
                            } else {
                                toolkit.showError(toolkit.msg("App.NbInstalltion.Error"));
                            }
                        }
                    }
                    break;
                }
            }

        } catch (Exception ex) {
            toolkit.showError(toolkit.msg("App.AddAny.Error"), ex);
        }
    }

    private void onRemove() {
        try {
            switch (getSettingType()) {
                case JDK_INSTALLATION: {
                    NutsSdkLocation loc = getSelectedJdkLocation();
                    if (loc != null) {
                        configService.removeJdk(loc.getPath());
                        updateJdkList();
                    }
                    break;
                }
                case NB_INSTALLATION: {
                    NetbeansInstallation loc = getSelectedNbInstallation();
                    if (loc != null) {
                        configService.removeNb(loc.getPath());
                        updateNbList();
                    }
                    break;
                }
            }
        } catch (Exception ex) {
            toolkit.showError(toolkit.msg("App.RemoveAny.Error"), ex);
        }
    }

    public void onSearchLocal() {
        win.showConfirmOkCancel(
                toolkit.msg("App.SearchLocal.Confirm.Title"),
                toolkit.msg("App.SearchLocal.Confirm.Message")
                , () -> {
                    try {
                        configService.configureDefaults();
                        win.updateList();
                    } catch (Exception ex) {
                        toolkit.showError(toolkit.msg("App.SearchLocal.Error"), ex);
                    }
                });

    }

    public void onDownload() {
        Workers.SwingWorker w = Workers.richWorker();
        NetbeansBinaryLink i = getSelectedRemoteNbInstallation();
        if (i != null) {
            synchronized (downloading) {
                if (!downloading.contains(i.getVersion())) {
                    w.store("i", i);
                    downloading.add(i.getVersion());
                } else {
                    return;
                }
            }
            win.showConfirmOkCancel(
                    toolkit.msg("App.Download.Confirm.Title"),
                    toolkit.msg("App.Download.Confirm.Message").with("name", i.toString())
                    ,
                    () -> {
                        w.run(() -> configService.installNetbeansBinary(w.load("i")))
                                .onError(ex -> toolkit.showError(toolkit.msg("App.Download.Error"), ex))
                                .onSuccess(() -> win.updateList())
                                .onFinally(() -> {
                                    synchronized (downloading) {
                                        NetbeansBinaryLink ii = w.load("i");
                                        if (ii != null) {
                                            downloading.remove(ii.getVersion());
                                        }
                                    }
                                })
                                .start();
                    }
            );
        }


    }


    public void onSearchRemote() {
        win.showConfirmOkCancel(
                toolkit.msg("App.SearchRemote.Confirm.Title"),
                toolkit.msg("App.SearchRemote.Confirm.Message")
                ,
                () ->
                        Workers.richWorker()
                                .run(() -> configService.configureDefaults())
                                .onSuccess(() -> win.updateList())
                                .onError((ex) -> toolkit.showError(toolkit.msg("App.SearchLocal.Error"), ex))
                                .start()
        );
    }

    public void updateJdkList() {
        toolkit.updateList(getComps2().jdkListView, configService.getAllJdk(), (a, b) -> a != null && b != null && ((NutsSdkLocation) a).getName().equals(((NutsSdkLocation) b).getName()), null);
    }

    public void updateNbList() {
        toolkit.updateList(getComps2().localNbListView, configService.getAllNb(), (a, b) -> a != null && b != null && ((NetbeansInstallation) a).getName().equals(((NetbeansInstallation) b).getName()), null);
        toolkit.updateList(getComps2().remoteNbListView, configService.searchRemoteInstallableNbBinaries(), (a, b) -> a != null && b != null && ((NetbeansBinaryLink) a).getVersion().equals(((NetbeansBinaryLink) b).getVersion()), null);
    }

    @Override
    public void updateAll() {
        updateJdkList();
        updateNbList();
    }

    public enum SettingType {
        NB_REMOTE,
        NB_INSTALLATION,
        JDK_INSTALLATION,
    }

    public NetbeansInstallation getSelectedNbInstallation() {
        NetbeansInstallation w = (NetbeansInstallation) getComps2().localNbListView.getSelectedValue();
        if (w != null) {
            return w;
        }
        return null;
    }

    public NetbeansBinaryLink getSelectedRemoteNbInstallation() {
        NetbeansBinaryLink w = (NetbeansBinaryLink) getComps2().remoteNbListView.getSelectedValue();
        if (w != null) {
            return w;
        }
        return null;
    }

    public NutsSdkLocation getSelectedJdkLocation() {
        NutsSdkLocation w = (NutsSdkLocation) getComps2().jdkListView.getSelectedValue();
        if (w != null) {
            return w;
        }
        return null;
    }

    private Comp2 getComps2() {
        if (win.isCompact()) {
            if (compact == null) {
                compact = createComp2(true);
            }
            return compact;
        }
        if (nonCompact == null) {
            nonCompact = createComp2(true);
        }
        return nonCompact;
    }

    private Comp2 createComp2(boolean b) {
        Comp2 c = new Comp2();
        c.buttonAdd = toolkit.createIconButton("add", "App.Action.Add", () -> onAdd(), win.isCompact());
        c.buttonRemove = toolkit.createIconButton("remove", "App.Action.Remove", () -> onRemove(), win.isCompact());
        c.buttonSearchLocal = toolkit.createIconButton("search-local", "App.Action.SearchLocal", () -> onSearchLocal(), win.isCompact());
        c.buttonSearchRemote = toolkit.createIconButton("search-remote", "App.Action.SearchRemote", () -> onSearchRemote(), win.isCompact());
        c.buttonDownload = toolkit.createIconButton("download", "App.Action.Download", () -> onDownload(), win.isCompact());
        c.buttonClose = toolkit.createIconButton("close", "App.Action.Close", () -> win.setSelectedPane(AppPaneType.LIST_WS), win.isCompact());
        c.buttons = new JComponent[]{c.buttonAdd, c.buttonRemove, c.buttonSearchLocal, c.buttonSearchRemote, c.buttonDownload, c.buttonClose};
        c.tabbedPane = new JTabbedPane();
        c.tabbedPane.add(toolkit.msg("App.Label.Netbeans").getText(), new JScrollPane(c.localNbListView = createLocalNbList()));
        c.tabbedPane.add(toolkit.msg("App.Label.JavaSDK").getText(), new JScrollPane(c.jdkListView = createJdkList()));
        c.tabbedPane.add(toolkit.msg("App.Label.RemoteNetbeans").getText(), new JScrollPane(c.remoteNbListView = createRemoteNbList()));
        c.tabbedPane.addChangeListener((ChangeEvent e) -> {
            onRequiredUpdateButtonStatuses();
        });
        c.main = c.tabbedPane;
        return c;
    }

    private int cached_i;
    private int cached_j;
    private int cached_k;

    @Override
    public void onPreChangeCompatStatus(boolean compact) {
        cached_i = getComps2().jdkListView.getSelectedIndex();
        cached_j = getComps2().localNbListView.getSelectedIndex();
        cached_k = getComps2().remoteNbListView.getSelectedIndex();
    }

    @Override
    public void onChangeCompatStatus(boolean compact) {
        super.onChangeCompatStatus(compact);
        getComps2().jdkListView.setSelectedIndex(cached_i);
        getComps2().localNbListView.setSelectedIndex(cached_j);
        getComps2().remoteNbListView.setSelectedIndex(cached_k);
    }
}
