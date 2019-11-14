/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.netbeans.launcher.ui.panes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.io.File;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;

import net.vpc.app.netbeans.launcher.model.NetbeansBinaryLink;
import net.vpc.app.netbeans.launcher.model.NetbeansInstallation;
import net.vpc.app.netbeans.launcher.model.NetbeansInstallationStore;
import net.vpc.app.netbeans.launcher.model.NetbeansLocation;
import net.vpc.app.netbeans.launcher.ui.*;
import net.vpc.app.netbeans.launcher.ui.utils.CatalogComponent;
import net.vpc.app.netbeans.launcher.ui.utils.JdkJlistToStringer;
import net.vpc.app.netbeans.launcher.ui.utils.ListComponent;
import net.vpc.app.netbeans.launcher.ui.utils.ObjectTableModel;
import net.vpc.app.netbeans.launcher.ui.utils.TableComponent;
import net.vpc.app.netbeans.launcher.util.JlistToStringer;
import net.vpc.app.netbeans.launcher.util.NbTheme;
import net.vpc.app.netbeans.launcher.util.Workers;
import net.vpc.app.nuts.NutsSdkLocation;

/**
 * @author vpc
 */
public class SettingsPane extends AppPane {

    private static class Comp2 {

        private CatalogComponent jdkListView;
        private NbListComponent nbListView;
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
    private JlistToStringer jdkStringer = new JdkJlistToStringer();

    public SettingsPane(MainWindowSwing win) {
        super(AppPaneType.SETTINGS, new AppPanePos(2, 0), win);
        build();
    }

    @Override
    public JComponent createMain(boolean compact) {
        return getComps2().main;
    }

    public CatalogComponent createJdkList() {
        CatalogComponent jdkListView = new TableComponent();
        jdkListView.setElementHeight(win.isCompact() ? 30 : 50);
        if (jdkListView instanceof ListComponent) {
            ((ListComponent) jdkListView).setStringer(jdkStringer);
        } else if (jdkListView instanceof TableComponent) {
            ((TableComponent) jdkListView).setColumns(new ObjectTableModel.NamedColumns<NutsSdkLocation>(new String[]{"Name", "Location"}) {
                @Override
                public Object getValueAt(int row, String column, NutsSdkLocation t) {
                    switch (column) {
                        case "Name":
                            return t.getName();
                        case "Type":
                            return t.getPackaging();
                        case "Location":
                            return t.getPath();
                    }
                    return "";
                }
            }.setColumnSizes(new float[]{2, 5}));
        }
        jdkListView.addListSelectionListener((e) -> {
            onRequiredUpdateButtonStatuses();
        });
        return jdkListView;
    }

    public NbListComponent createRemoteNbList() {
        return new NbListComponentAsTable(win, () -> onRequiredUpdateButtonStatuses());
    }

    protected SettingType getSettingType() {
        switch (getComps2().tabbedPane.getSelectedIndex()) {
            case 0: {
                return SettingType.NB_INSTALLATION;
            }
            case 1: {
                return SettingType.JDK_INSTALLATION;
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
                toolkit.setControlVisible(getComps2().buttonDownload, getComps2().nbListView.getSelectedValue() instanceof NetbeansBinaryLink);
                toolkit.setControlVisible(getComps2().buttonRemove, getComps2().nbListView.getSelectedValue() instanceof NetbeansInstallation);
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
        }
//        NetbeansWorkspace w = getSelectedWorkspace();
    }

    public void setSettingType(SettingType r) {
        switch (r) {
            case NB_INSTALLATION: {
                getComps2().tabbedPane.setSelectedIndex(0);
                break;
            }
            case JDK_INSTALLATION: {
                getComps2().tabbedPane.setSelectedIndex(1);
                break;
            }
        }
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
                    NetbeansLocation g = (NetbeansLocation) getComps2().nbListView.getSelectedValue();
                    if (g instanceof NetbeansInstallation) {
                        NetbeansInstallation loc = (NetbeansInstallation) g;
                        if (loc != null) {
                            configService.removeNb(loc.getPath());
                            updateNbList();
                        }
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
                toolkit.msg("App.SearchLocal.Confirm.Message"),
                () -> {
                    try {
                        configService.configureDefaults();
                        win.updateList();
                    } catch (Exception ex) {
                        toolkit.showError(toolkit.msg("App.SearchLocal.Error"), ex);
                    }
                });

    }

    public void onSearchRemote() {
        win.showConfirmOkCancel(
                toolkit.msg("App.SearchRemote.Confirm.Title"),
                toolkit.msg("App.SearchRemote.Confirm.Message"),
                ()
                -> Workers.richWorker()
                        .run(() -> configService.configureDefaults())
                        .onSuccess(() -> win.updateList())
                        .onError((ex) -> toolkit.showError(toolkit.msg("App.SearchLocal.Error"), ex))
                        .start()
        );
    }

    public void updateJdkList() {
        toolkit.updateTable(getComps2().jdkListView, configService.getAllJdk(), (a, b) -> a != null && b != null && ((NutsSdkLocation) a).getName().equals(((NutsSdkLocation) b).getName()), null);
    }

    public void updateNbList() {
        getComps2().nbListView.refresh();
    }

    @Override
    public void updateAll() {
        updateJdkList();
        updateNbList();
    }

    public enum SettingType {
        NB_INSTALLATION,
        JDK_INSTALLATION,
    }

//    public NetbeansInstallation getSelectedNbInstallation() {
//        NetbeansInstallation w = (NetbeansInstallation) getComps2().localNbListView.getSelectedValue();
//        if (w != null) {
//            return w;
//        }
//        return null;
//    }
//
//    public NetbeansBinaryLink getSelectedRemoteNbInstallation() {
//        NetbeansBinaryLink w = (NetbeansBinaryLink) getComps2().remoteNbListView.getSelectedValue();
//        if (w != null) {
//            return w;
//        }
//        return null;
//    }
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
        c.nbListView = createRemoteNbList();
        c.jdkListView = createJdkList();
        c.buttonAdd = toolkit.createIconButton("add", "App.Action.Add", () -> onAdd(), win.isCompact());
        c.buttonRemove = toolkit.createIconButton("remove", "App.Action.Remove", () -> onRemove(), win.isCompact());
        c.buttonSearchLocal = toolkit.createIconButton("search-local", "App.Action.SearchLocal", () -> onSearchLocal(), win.isCompact());
        c.buttonSearchRemote = toolkit.createIconButton("search-remote", "App.Action.SearchRemote", () -> onSearchRemote(), win.isCompact());
        c.buttonDownload = toolkit.createIconButton("download", "App.Action.Download", () -> c.nbListView.onDownload(), win.isCompact());
        c.buttonClose = toolkit.createIconButton("close", "App.Action.Close", () -> win.setSelectedPane(AppPaneType.LIST_WS), win.isCompact());
        c.buttons = new JComponent[]{c.buttonAdd, c.buttonRemove, c.buttonSearchLocal, c.buttonSearchRemote, c.buttonDownload, c.buttonClose};
        c.tabbedPane = NbTheme.prepare(new JTabbedPane() {
            Font font = getFont();
            Font font2 = font.deriveFont(Font.BOLD | Font.ITALIC, 16);
            Color darker = new Color(37, 73, 110);

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                Dimension s = getSize();
                g.setFont(font2);
//                g.setColor(darker);
//                g.drawString("Settings...", (int) (s.getWidth() - 80+1), 18);
                g.setColor(darker);
                g.drawString("Settings...", (int) (s.getWidth() - 80), 18);
            }

        });
        c.tabbedPane.add(toolkit.msg("App.Label.Netbeans").getText(), NbTheme.prepare(new JScrollPane(c.nbListView.toComponent())));
        c.tabbedPane.add(toolkit.msg("App.Label.JavaSDK").getText(), NbTheme.prepare(new JScrollPane(c.jdkListView.toComponent())));
        c.tabbedPane.addChangeListener((ChangeEvent e) -> {
            onRequiredUpdateButtonStatuses();
        });
        c.main = c.tabbedPane;
        return c;
    }

    private int cached_i;
    private int cached_k;

    @Override
    public void onPreChangeCompatStatus(boolean compact) {
        cached_i = getComps2().jdkListView.getSelectedIndex();
        cached_k = getComps2().nbListView.getSelectedIndex();
    }

    @Override
    public void onChangeCompatStatus(boolean compact) {
        super.onChangeCompatStatus(compact);
        getComps2().jdkListView.setSelectedIndex(cached_i);
        getComps2().nbListView.setSelectedIndex(cached_k);
    }
}
