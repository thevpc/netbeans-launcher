/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.netbeans.launcher.ui.swing.panes;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import net.vpc.app.netbeans.launcher.model.JdkInstallation;
import net.vpc.app.netbeans.launcher.model.NetbeansInstallation;
import net.vpc.app.netbeans.launcher.ui.PaneType;
import net.vpc.app.netbeans.launcher.ui.swing.AppPane;
import net.vpc.app.netbeans.launcher.ui.swing.MainWindowSwing;
import net.vpc.app.netbeans.launcher.ui.swing.MainWindowSwingHelper;
import net.vpc.app.netbeans.launcher.util.JlistToStringer;

/**
 *
 * @author vpc
 */
public class SettingsPane extends AppPane {

    private static class Comp2 {

        private JList jdkListView;
        private JList nbListView;
        private JTabbedPane tabbedPane;
        private JComponent buttonAdd;
        private JComponent buttonRemove;
        private JComponent buttonReconfigure;
        private JComponent buttonClose;
        private JComponent[] buttons;
        private JComponent main;
    }
    private Comp2 compact;
    private Comp2 nonCompact;
    private JlistToStringer jdkStringer = new JlistToStringer(2) {
        @Override
        public String toString(Object value, int level) {
            if (value instanceof JdkInstallation) {
                JdkInstallation i = (JdkInstallation) value;
                if (win.isCompact()) {
                    switch (level) {
                        case 0: {
                            return i.getName();
                        }
                        case 1: {
                            return i.getName() + " (" + i.getPath() + ")";
                        }
                    }
                } else {
                    switch (level) {
                        case 0: {
                            return i.getName() + " (" + i.getPath() + ")";
                        }
                        case 1: {
                            return i.getName() + " (" + i.getPath() + ")";
                        }
                    }
                }
            }
            return String.valueOf(value);
        }

    };
    private JlistToStringer nbStringer = new JlistToStringer(2) {
        @Override
        public String toString(Object value, int level) {
            if (value instanceof NetbeansInstallation) {
                NetbeansInstallation i = (NetbeansInstallation) value;
                if (win.isCompact()) {
                    switch (level) {
                        case 0: {
                            return i.getName();
                        }
                        case 1: {
                            return i.getName() + " (" + i.getPath() + ")";
                        }
                    }
                } else {
                    switch (level) {
                        case 0: {
                            return i.getName() + " (" + i.getPath() + ")";
                        }
                        case 1: {
                            return i.getName() + " (" + i.getPath() + ")";
                        }
                    }
                }
            }
            return String.valueOf(value);
        }

    };

    public SettingsPane(MainWindowSwing win) {
        super(PaneType.SETTINGS, win);
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
                    setBackground(index % 2 == 0 ? Color.WHITE : MainWindowSwingHelper.color("f9f9f9"));
                } else {
                    setBackground(MainWindowSwingHelper.color("0096c9"));
                }
                return this;
            }
        });
        jdkListView.addListSelectionListener((ListSelectionEvent e) -> {
            onRequiredUpdateButtonStatuses();
        });
        return jdkListView;
    }

    public JList createNbList() {
        JList nbListView = new JList(new DefaultListModel());
        nbListView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        nbListView.setBorder(new EmptyBorder(2, 2, 2, 2));
        nbListView.setFixedCellHeight(win.isCompact() ? 30 : 50);
        nbListView.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

                super.getListCellRendererComponent(list, nbStringer.toString(list, value), index, isSelected, cellHasFocus);
                if (!isSelected) {
                    setBackground(index % 2 == 0 ? Color.WHITE : MainWindowSwingHelper.color("f9f9f9"));
                } else {
                    setBackground(MainWindowSwingHelper.color("0096c9"));
                }
                return this;
            }
        });
        nbListView.addListSelectionListener((ListSelectionEvent e) -> {
            onRequiredUpdateButtonStatuses();
        });
        return nbListView;
    }

    protected SettingType getSettingType() {
        switch (getComps2().tabbedPane.getSelectedIndex()) {
            case 0: {
                return SettingType.NB_INSTLLATION;
            }
            case 1: {
                return SettingType.JDK_INSTLLATION;
            }
        }
        return SettingType.JDK_INSTLLATION;
    }

    protected void onRequiredUpdateButtonStatuses() {
        switch (getSettingType()) {
            case NB_INSTLLATION: {
                //netbeans instakllations
                toolkit.setControlVisible(getComps2().buttonRemove, getSelectedNbInstallation() != null);
                break;
            }
            case JDK_INSTLLATION: {
                //jdk instakllations
                toolkit.setControlVisible(getComps2().buttonRemove, getSelectedJdkLocation() != null);
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
                case JDK_INSTLLATION: {
                    JFileChooser c = new JFileChooser();
                    c.setCurrentDirectory(configService.getCurrentDirectory());
                    c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    c.setAcceptAllFileFilterUsed(false);
                    if (JFileChooser.APPROVE_OPTION == c.showDialog(this, toolkit.msg("App.Workspace.SelectJDK").getText())) {
                        final File f = c.getSelectedFile();
                        if (f != null) {
                            configService.setCurrentDirectory(f);
                            JdkInstallation loc = configService.detectJdk(f.getPath());
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

                case NB_INSTLLATION: {
                    JFileChooser c = new JFileChooser();
                    c.setCurrentDirectory(configService.getCurrentDirectory());
                    c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    c.setAcceptAllFileFilterUsed(false);
                    if (JFileChooser.APPROVE_OPTION == c.showDialog(this, toolkit.msg("App.Workspace.SelectNetbeans").getText())) {
                        final File f = c.getSelectedFile();
                        if (f != null) {
                            configService.setCurrentDirectory(f);
                            NetbeansInstallation[] loc = configService.detectNbs(f.getPath(), true);
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
                case JDK_INSTLLATION: {
                    JdkInstallation loc = getSelectedJdkLocation();
                    if (loc != null) {
                        configService.removeJdk(loc.getPath());
                        updateJdkList();
                    }
                    break;
                }
                case NB_INSTLLATION: {
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

    public void onConfigureDefaults() {
        try {
            if (toolkit.showConfirm(
                    toolkit.msg("App.ReConfiguration.Confirm.Title"),
                    toolkit.msg("App.ReConfiguration.Confirm.Message")
            )) {
                configService.configureDefaults();
                win.updateList();
            }
        } catch (Exception ex) {
            toolkit.showError(toolkit.msg("App.Reconfigure.Error"), ex);
        }
    }

    public void updateJdkList() {
        toolkit.updateList(getComps2().jdkListView, configService.getAllJdk(), (a, b) -> a != null && b != null && ((JdkInstallation) a).getName().equals(((JdkInstallation) b).getName()), null);
    }

    public void updateNbList() {
        toolkit.updateList(getComps2().nbListView, configService.getAllNb(), (a, b) -> a != null && b != null && ((NetbeansInstallation) a).getName().equals(((NetbeansInstallation) b).getName()), null);
    }

    @Override
    public void updateAll() {
        updateJdkList();
        updateNbList();
    }

    public static enum SettingType {
        NB_INSTLLATION,
        JDK_INSTLLATION,
    }

    public NetbeansInstallation getSelectedNbInstallation() {
        NetbeansInstallation w = (NetbeansInstallation) getComps2().nbListView.getSelectedValue();
        if (w != null) {
            return w;
        }
        return null;
    }

    public JdkInstallation getSelectedJdkLocation() {
        JdkInstallation w = (JdkInstallation) getComps2().jdkListView.getSelectedValue();
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
        c.buttonReconfigure = toolkit.createIconButton("re-configure", "App.Action.Reconfigure", () -> onConfigureDefaults(), win.isCompact());
        c.buttonClose = toolkit.createIconButton("close", "App.Action.Close", () -> win.setSelectedPane(PaneType.LIST_WS), win.isCompact());
        c.buttons = new JComponent[]{c.buttonAdd, c.buttonRemove, c.buttonReconfigure, c.buttonClose};
        c.tabbedPane = new JTabbedPane();
        c.tabbedPane.add(toolkit.msg("App.Label.Netbeans").getText(), new JScrollPane(c.nbListView = createNbList()));
        c.tabbedPane.add(toolkit.msg("App.Label.JavaSDK").getText(), new JScrollPane(c.jdkListView = createJdkList()));
        c.tabbedPane.addChangeListener((ChangeEvent e) -> {
            onRequiredUpdateButtonStatuses();
        });
        c.main = c.tabbedPane;
        return c;
    }

    private int cached_i;
    private int cached_j;

    @Override
    public void onPreChangeCompatStatus(boolean compact) {
        cached_i = getComps2().jdkListView.getSelectedIndex();
        cached_j = getComps2().nbListView.getSelectedIndex();
    }

    @Override
    public void onChangeCompatStatus(boolean compact) {
        super.onChangeCompatStatus(compact);
        getComps2().jdkListView.setSelectedIndex(cached_i);
        getComps2().nbListView.setSelectedIndex(cached_j);
    }
}
