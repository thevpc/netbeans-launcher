/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.netbeans.launcher.ui.swing.panes;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import net.vpc.app.netbeans.launcher.NetbeansConfigService;
import net.vpc.app.netbeans.launcher.model.JdkInstallation;
import net.vpc.app.netbeans.launcher.model.NetbeansGroup;
import net.vpc.app.netbeans.launcher.model.NetbeansInstallation;
import net.vpc.app.netbeans.launcher.model.NetbeansWorkspace;
import net.vpc.app.netbeans.launcher.ui.EditType;
import net.vpc.app.netbeans.launcher.ui.PaneType;
import net.vpc.app.netbeans.launcher.ui.swing.AppPane;
import net.vpc.app.netbeans.launcher.ui.swing.MainWindowSwing;
import net.vpc.app.netbeans.launcher.ui.swing.MainWindowSwingHelper;
import net.vpc.app.netbeans.launcher.util.NbUtils;

/**
 *
 * @author vpc
 */
public class WorkspacePane extends AppPane {

    private static class Comp3 {

        JComboBox path;
        private JTextField options;
        private JTextField locale;
        private JTextField cpAppend;
        private JTextField cpPrepend;
        private JComboBox userdir;
        private JComboBox cachedir;
        private JComboBox laf;
        private JComboBox name;
        private JComboBox group;
        //    private JButton buttonUpdateGroup = new JButton("load");
        //    private JButton buttonClearCachedir = new JButton("delete");
        private JComboBox jdkhome;
        private JSpinner fontSize;
        private JComponent[] buttons;
        private JComponent main;

    }

    private Comp3 compact;
    private Comp3 nonCompact;
    private EditType editMode = EditType.EDIT;
    private NetbeansWorkspace currWorkspace = new NetbeansWorkspace();
    private NetbeansWorkspace lastWorkspace = new NetbeansWorkspace();
    ItemListener updateGroupChangeListener = (ItemEvent e) -> {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            onRequiredUpdateGroupFast();
        }
    };
    ItemListener pathUpdatedChangeListener = (ItemEvent e) -> {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            onNbPathChanged();
        }
    };

    public WorkspacePane(MainWindowSwing win) {
        super(PaneType.EDIT_WS, win);
        build();
    }

    @Override
    public JComponent createMain(boolean compact) {
        return getComps3().main;
    }

    public void createMainCompact(final Comp3 c) {
        MainWindowSwingHelper.GridPane g = MainWindowSwingHelper.gridPane().insets(1, 1).expandHorizontallyColumn(1);
        //            g.setPadding(new Insets(5, 5, 5, 5));
        int row = 0;
        g.insets(1, 10).add(toolkit.createLabel("App.Workspace.InstallFolder"), 0, row);
        row++;
        g.insets(1, 1).add(c.path, 0, row, 2, 1);
        row++;
        g.insets(1, 10).add(toolkit.createLabel("App.Workspace.Name"), 0, row);
        row++;
        g.insets(1, 1).add(c.name, 0, row, 2, 1);
        row++;
        g.insets(1, 10).add(toolkit.createLabel("App.Workspace.ConfigFolder"), 0, row);
        Box b = Box.createHorizontalBox();
        b.add(toolkit.createIconButton("search", "App.Action.SelectFolder", () -> onSelectUserdir()));
        b.add(toolkit.createIconButton("folder", "App.Action.OpenSelectedFolder", () -> onOpenUserdir()));
        b.add(toolkit.createIconButton("trash", "App.Action.DeleteSelectedFolder", () -> onClearUserdir()));
        g.insets(1, 1).add(b, 1, row);
        row++;
        g.insets(1, 1).add(c.userdir, 0, row, 2, 1);
        row++;
        g.insets(1, 10).add(toolkit.createLabel("App.Workspace.CacheFolder"), 0, row);
        b = Box.createHorizontalBox();
        b.add(toolkit.createIconButton("search", "App.Action.SelectFolder", () -> onSelectCachedir()));
        b.add(toolkit.createIconButton("folder", "App.Action.OpenSelectedFolder", () -> onOpenCachedir()));
        b.add(toolkit.createIconButton("trash", "App.Action.DeleteSelectedFolder", () -> onClearCachedir()));
        g.insets(1, 1).add(b, 1, row);
        row++;
        g.insets(1, 1).add(c.cachedir, 0, row, 2, 1);
        row++;
        g.insets(1, 10).add(toolkit.createLabel("App.Workspace.Group"), 0, row);
        b = Box.createHorizontalBox();
        b.add(toolkit.createIconButton("search", "App.Action.LoadGroups", () -> onLoadGroup()));
//        b.add(MainWindowSwingHelper.createIconButton("folder.png", "Open Selected folder",()->{}));
//        b.add(MainWindowSwingHelper.createIconButton("trash.png", "Delete Selected folder",()->onClearCachedir()));
        g.insets(1, 1).add(b, 1, row);
        row++;
        g.insets(1, 1).add(c.group, 0, row, 2, 1);
//        g.insets(1, 1).add(buttonUpdateGroup, 3, row);
        row++;
        g.insets(1, 10).add(toolkit.createLabel("App.Workspace.JDK"), 0, row);
        row++;
        g.insets(1, 1).add(c.jdkhome, 0, row, 2, 1);
        //            GridPane.setHgrow(buttonUpdateGroup, Priority.NEVER);
        //            GridPane.setHgrow(group, Priority.ALWAYS);
        row++;
        g.insets(1, 10).add(toolkit.createLabel("App.Workspace.LAF"), 0, row);
        row++;
        g.insets(1, 1).add(c.laf, 0, row, 2, 1);

        row++;
        g.insets(1, 10).add(toolkit.createLabel("App.Workspace.FontSize"), 0, row);
        row++;
        g.insets(1, 1).add(c.fontSize, 0, row, 2, 1);

        row++;
        g.insets(1, 10).add(toolkit.createLabel("App.Workspace.Locale"), 0, row);
        row++;
        g.insets(1, 1).add(prepareTextField(c.locale, 3), 0, row, 2, 1);

        row++;
        g.insets(1, 10).add(toolkit.createLabel("App.Workspace.JVMOptions"), 0, row, 3, 1);
        b = Box.createHorizontalBox();
        b.add(toolkit.createIconButton("search", "App.Action.AddOption", () -> {
            onAddOption(c.options);
        }));
        b.add(toolkit.createIconButton("trash", "App.Action.Clear", () -> onClearOptions()));
        g.insets(1, 1).add(b, 1, row);
        row++;
        g.insets(1, 10).add(prepareTextField(c.options, 3), 0, row, 2, 1);

        row++;
        g.insets(1, 10).add(toolkit.createLabel("App.Workspace.ClassPathPrepend"), 0, row, 3, 1);
        row++;
        g.insets(1, 10).add(prepareTextField(c.cpPrepend, 1), 0, row, 2, 1);
        row++;

        row++;
        g.insets(1, 10).add(toolkit.createLabel("App.Workspace.ClassPathAppend"), 0, row, 3, 1);
        row++;
        g.insets(1, 10).add(prepareTextField(c.cpAppend, 1), 0, row, 2, 1);
        row++;

        final JComponent c2 = g.toComponent();
        final JScrollPane s = new JScrollPane(c2);
        c.main = s;
    }

    public void createMainNonCompact(final Comp3 c) {
        MainWindowSwingHelper.GridPane g = MainWindowSwingHelper.gridPane().insets(1, 1).expandHorizontallyColumn(1);
        //            g.setPadding(new Insets(5, 5, 5, 5));
        int row = 0;
        g.insets(1, 10).add(toolkit.createLabel("App.Workspace.InstallFolder"), 0, row);
        g.insets(1, 1).add(c.path, 1, row, 3, 1);
        row++;
        g.insets(1, 10).add(toolkit.createLabel("App.Workspace.Name"), 0, row);
        g.insets(1, 1).add(c.name, 1, row, 3, 1);
        row++;
        g.insets(1, 10).add(toolkit.createLabel("App.Workspace.ConfigFolder"), 0, row);
        g.insets(1, 1).add(c.userdir, 1, row, 2, 1);
        Box b = Box.createHorizontalBox();
        b.add(toolkit.createIconButton("search", "App.Action.SelectFolder", () -> onSelectUserdir()));
        b.add(toolkit.createIconButton("folder", "App.Action.OpenSelectedFolder", () -> onOpenUserdir()));
        b.add(toolkit.createIconButton("trash", "App.Action.DeleteSelectedFolder", () -> onClearUserdir()));
        g.insets(1, 1).add(b, 3, row);
        row++;
        g.insets(1, 10).add(toolkit.createLabel("App.Workspace.CacheFolder"), 0, row);
        g.insets(1, 1).add(c.cachedir, 1, row, 2, 1);
        b = Box.createHorizontalBox();
        b.add(toolkit.createIconButton("search", "App.Action.SelectFolder", () -> onSelectCachedir()));
        b.add(toolkit.createIconButton("folder", "App.Action.OpenSelectedFolder", () -> onOpenCachedir()));
        b.add(toolkit.createIconButton("trash", "App.Action.DeleteSelectedFolder", () -> onClearCachedir()));
        g.insets(1, 1).add(b, 3, row);
        row++;
        g.insets(1, 10).add(toolkit.createLabel("App.Workspace.Group"), 0, row);
        g.insets(1, 1).add(c.group, 1, row, 2, 1);
        b = Box.createHorizontalBox();
        b.add(toolkit.createIconButton("search", "App.Action.LoadGroups", () -> onLoadGroup()));
//        b.add(MainWindowSwingHelper.createIconButton("folder.png", "Open Selected folder",()->{}));
//        b.add(MainWindowSwingHelper.createIconButton("trash.png", "Delete Selected folder",()->onClearCachedir()));
        g.insets(1, 1).add(b, 3, row);
//        g.insets(1, 1).add(buttonUpdateGroup, 3, row);
        row++;
        g.insets(1, 10).add(toolkit.createLabel("App.Workspace.JDK"), 0, row);
        g.insets(1, 1).add(c.jdkhome, 1, row, 3, 1);
        //            GridPane.setHgrow(buttonUpdateGroup, Priority.NEVER);
        //            GridPane.setHgrow(group, Priority.ALWAYS);
        row++;
        g.insets(1, 10).add(toolkit.createLabel("App.Workspace.LAF"), 0, row);
        g.insets(1, 1).add(c.laf, 1, row, 3, 1);

        row++;
        g.insets(1, 10).add(toolkit.createLabel("App.Workspace.FontSize"), 0, row);
        g.insets(1, 1).add(c.fontSize, 1, row, 3, 1);

        row++;
        g.insets(1, 10).add(toolkit.createLabel("App.Workspace.Locale"), 0, row);
        g.insets(1, 1).add(prepareTextField(c.locale, 3), 1, row, 3, 1);

        row++;
        g.insets(1, 10).add(toolkit.createLabel("App.Workspace.JVMOptions"), 0, row, 3, 1);
        row++;
        g.insets(1, 10).add(prepareTextField(c.options, 3), 0, row, 3, 1);
        b = Box.createHorizontalBox();
        b.add(toolkit.createIconButton("search", "App.Action.AddOption", () -> {
            onAddOption(c.options);
        }));
        b.add(toolkit.createIconButton("trash", "App.Action.Clear", () -> onClearOptions()));
        g.insets(1, 1).add(b, 3, row);

        row++;
        g.insets(1, 10).add(toolkit.createLabel("App.Workspace.ClassPathPrepend"), 0, row, 3, 1);
        row++;
        g.insets(1, 10).add(prepareTextField(c.cpPrepend, 5), 0, row, 5, 2);
        row++;

        row++;
        g.insets(1, 10).add(toolkit.createLabel("App.Workspace.ClassPathAppend"), 0, row, 3, 1);
        row++;
        g.insets(1, 10).add(prepareTextField(c.cpAppend, 5), 0, row, 5, 2);
        row++;

        final JComponent cc = g.toComponent();
        final JScrollPane s = new JScrollPane(cc);
        c.main = s;
    }

    private void onClearOptions() {
        setEditOptions("");
    }

    private void clearNetbeansPath(String name, String path) {
        if (NbUtils.isEmpty(path)) {
            toolkit.showWarning(
                    toolkit.msg("App.DeleteWorkspaceFolder.Empty.Title"),
                    toolkit.msg("App.DeleteWorkspaceFolder.Empty.Message")
            );
            return;
        }
        String editName = getEditName();
        if (toolkit.showConfirm(
                toolkit.msg("App.DeleteFolder.Confirm.Title"),
                toolkit.msg("App.DeleteFolder.Confirm.Message")
        )) {
            if (win.isRunningWorkspace(editName)) {
                toolkit.showError(toolkit.msg("App.DeleteWorkspaceFolder.Error"));
            } else {
                try {
                    File resolveFile = NbUtils.resolveFile(path);
                    if (!resolveFile.isDirectory()) {
                        toolkit.showError(toolkit.msg("App.InvalidPath.Error"));
                        return;
                    }
                    Path directory = resolveFile.toPath();
                    Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            Files.delete(file);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        }
                    });
                    toolkit.showSucess(
                            toolkit.msg("App.DeleteFolder.Success.Title"),
                            toolkit.msg("App.DeleteFolder.Success.Message")
                    );
                } catch (IOException ex) {
                    toolkit.showError(toolkit.msg("App.DeleteWorkspaceFolder.Error"), ex);
                }
            }
        }
    }

    private void onClearUserdir() {
        String d = getEditUserdir();
        if (NbUtils.isEmpty(d)) {
            NetbeansInstallation ni = configService.detectNb(getEditPath());
            if (ni != null) {
                d = ni.getUserdir();
            }
        }
        clearNetbeansPath("Config Folder", d);
    }

    private void onClearCachedir() {
        String d = getEditCachedir();
        if (NbUtils.isEmpty(d)) {
            NetbeansInstallation ni = configService.detectNb(getEditPath());
            if (ni != null) {
                d = ni.getCachedir();
            }
        }
        clearNetbeansPath("Cache Folder", d);
    }

    @Override
    public JComponent[] createButtons(boolean compact) {
        return getComps3().buttons;
    }

    private void onStartWorkspace() {
        try {
            NetbeansWorkspace w = getWorkspace();
            win.startWorkspace(w);
        } catch (Exception ex) {
            toolkit.showError(toolkit.msg("App.RunWorkspace.Error"), ex);
        }
    }

    private void onSaveWorkspacePane() {
        try {
            NetbeansWorkspace w = getWorkspace();
            configService.saveNbWorkspace(w);
            win.updateList();
            win.setSelectedPane(PaneType.LIST_WS);
        } catch (Exception ex) {
            toolkit.showError(toolkit.msg("App.SaveWorkspace.Error"), ex);
        }
    }

    private void onCloseWorkspacePane() {
        try {
            if (isModified()) {
                if (toolkit.showConfirm(
                        toolkit.msg("App.DiscardConfigChanges.Confirm.Title"),
                        toolkit.msg("App.DiscardConfigChanges.Confirm.Message")
                )) {
                    win.setSelectedPane(PaneType.LIST_WS);
                }
            } else {
                win.setSelectedPane(PaneType.LIST_WS);
            }
        } catch (Exception ex) {
            toolkit.showError(toolkit.msg("App.CloseWorkspace.Error"), ex);
        }
    }

    @Override
    public void onInit() {
        DefaultComboBoxModel m = (DefaultComboBoxModel) getComps3().laf.getModel();
        m.removeAllElements();
        m.addElement("Default");
        m.addElement("GTK");
        m.addElement("Mac");
        m.addElement("Metal");
        m.addElement("Motif");
        m.addElement("Nimbus");
        m.addElement("Window");
    }

    public void onRequireUpdateDirProposals() {
        NetbeansWorkspace w = new NetbeansWorkspace();
        w.setName(getEditName());
        String oldValue = getEditUserdir();
        getComps3().userdir.setEditable(true);
        toolkit.setComboxValues(getComps3().userdir, configService.getUserdirProposals(w), oldValue);
        oldValue = getEditCachedir();
        getComps3().cachedir.setEditable(true);
        toolkit.setComboxValues(getComps3().cachedir, configService.getCachedirProposals(w), oldValue);
    }

    public void resetValues() {
        getComps3().path.setEditable(true);
        toolkit.setComboxValues(getComps3().path, configService.getAllNb(), null);
        getComps3().jdkhome.setEditable(true);
        toolkit.setComboxValues(getComps3().jdkhome, configService.getAllJdk(), null);
        getComps3().name.setEditable(true);
        toolkit.setComboxValues(getComps3().name, configService.getNewNameProposals(), null);
    }

    public EditType getEditMode() {
        return editMode;
    }

    public boolean isModified() {
        NetbeansWorkspace newVal = getWorkspace();
        boolean b = !lastWorkspace.equals(newVal);
        return b;
    }

    public void setEditMode(EditType editMode) {
        this.editMode = editMode;
        toolkit.setControlDisabled(getComps3().name, editMode == EditType.EDIT);
        toolkit.setControlDisabled(getComps3().path, editMode == EditType.EDIT);
    }

    protected void onLoadGroup() {
        NetbeansWorkspace w = getWorkspace();
        NetbeansGroup[] o = configService.detectNbGroups(w);
        NetbeansGroup gsel = null;
        if (o == null) {
            o = new NetbeansGroup[]{NetbeansConfigService.NETBEANS_NO_GROUP, NetbeansConfigService.NETBEANS_CLOSE_GROUP};
        }
        for (NetbeansGroup gg : o) {
            if (NbUtils.equalsStr(gg.getName(), w.getGroup())) {
                gsel = gg;
                break;
            }
        }
        if (gsel == null) {
            gsel = NetbeansConfigService.NETBEANS_NO_GROUP;
        }
        getComps3().group.setEditable(true);
        toolkit.setComboxValues(getComps3().group, o, gsel);
    }

    protected void onNbPathChanged() {
        NetbeansWorkspace w = getWorkspace();
        String p = w.getPath();
        NetbeansInstallation nb = configService.findNb(p);
        NetbeansInstallation[] existing = {};
        if (getEditMode() == EditType.ADD) {
            existing = configService.getAllNb();
        }
        if (nb != null) {
//            if (NbUtils.isEmpty(w.getJdkhome())) {
            setEditJdkhome(nb.getJdkhome());
//            }
//            if (NbUtils.isEmpty(w.getCachedir())) {
            setEditCachedir(nb.getCachedir());
            setEditOptions(nb.getOptions());
//            }
//            if (NbUtils.isEmpty(w.getUserdir())) {
            setEditUserdir(nb.getUserdir());
            String pname = nb.getName();
            if (getEditMode() == EditType.ADD) {
                HashSet<String> names = new HashSet<>();
                for (NetbeansInstallation netbeansInstallation : existing) {
                    names.add(netbeansInstallation.getName());
                }
                int x = 1;
                while (true) {
                    String n = (x == 1) ? pname : pname + " " + x;
                    if (!names.contains(n)) {
                        pname = n;
                        break;
                    }
                    x++;
                }
                setEditName(nb.getName());
            } else {
                setEditName(nb.getName());
            }
//            }
        }
        onRequiredUpdateGroupFast();
    }

    protected void onRequiredUpdateGroupFast() {
        NetbeansWorkspace w = getWorkspace();
        NetbeansGroup[] o = (NbUtils.isEmpty(w.getGroup())) ? new NetbeansGroup[]{NetbeansConfigService.NETBEANS_NO_GROUP, NetbeansConfigService.NETBEANS_CLOSE_GROUP} : new NetbeansGroup[]{NetbeansConfigService.NETBEANS_NO_GROUP, NetbeansConfigService.NETBEANS_CLOSE_GROUP, new NetbeansGroup(w.getGroup(), w.getGroup())};
        NetbeansGroup gsel = null;
        for (NetbeansGroup gg : o) {
            if (NbUtils.equalsStr(gg.getName(), w.getGroup())) {
                gsel = gg;
                break;
            }
        }
        if (gsel == null) {
            gsel = NetbeansConfigService.NETBEANS_NO_GROUP;
        }
        getComps3().group.setEditable(true);
        toolkit.setComboxValues(getComps3().group, o, gsel);
    }

    public void onAddWorkspace(NetbeansWorkspace w) {
        setEditMode(EditType.ADD);
        if (w == null) {
            w = new NetbeansWorkspace();
        }
        setWorkspace(w);
    }

    public void onEditWorkspace(NetbeansWorkspace w) {
        setEditMode(EditType.EDIT);
        setWorkspace(w);
    }

    public void setWorkspace(NetbeansWorkspace w) {
        if (w == null) {
            w = new NetbeansWorkspace();
        }
        lastWorkspace = w.copy();
        resetValues();
        setEditName(w.getName());
        setEditPath(w.getPath());
        setEditJdkhome(w.getJdkhome());
        setEditFontSize(w.getFontSize());
        onRequireUpdateDirProposals();
        setEditUserdir(w.getUserdir());
        setEditCachedir(w.getCachedir());
        setEditLaf(w.getLaf());
        setEditOptions(w.getOptions());
        setEditCpAppend(w.getCpAppend());
        setEditCpPrepend(w.getCpPrepend());
        setEditLocale(w.getLocale());
        onRequiredUpdateGroupFast();
    }

    public NetbeansWorkspace getWorkspace() {
        NetbeansWorkspace w = new NetbeansWorkspace();
        w.setName(getEditName());
        w.setPath(getEditPath());
        w.setGroup(getEditGroup());
        w.setJdkhome(getEditJdkHome());
        w.setFontSize(getEditFontSize());
        w.setUserdir(getEditUserdir());
        w.setCachedir(getEditCachedir());
        w.setLaf(getEditLaf());
        w.setOptions(getEditOptions());
        w.setCpAppend(getEditCpAppend());
        w.setCpPrepend(getEditCpPrepend());
        w.setLocale(getEditLocale());
        return w;
    }

    public void setEditPath(String p) {
        NetbeansInstallation pp = configService.getNb(p);
        if (pp != null) {
            getComps3().path.setSelectedItem(pp);
        } else {
            getComps3().path.setSelectedItem(p);
        }
    }

    public void setEditJdkhome(String p) {
        JdkInstallation pp2 = configService.getJdk(p);
        if (pp2 != null) {
            getComps3().jdkhome.setSelectedItem(pp2);
        } else {
            getComps3().jdkhome.setSelectedItem(p);
        }
    }

    public void setEditName(String n) {
        getComps3().name.setSelectedItem(n);
    }

    public void setEditFontSize(int n) {
        if (n > 0) {
            getComps3().fontSize.setValue(n);
        } else {
            getComps3().fontSize.setValue(0);
        }
    }

    public void setEditUserdir(String n) {
        getComps3().userdir.setSelectedItem(n);
    }

    public void setEditCachedir(String n) {
        getComps3().cachedir.setSelectedItem(n);
    }

    public void setEditLaf(String n) {
        getComps3().laf.setSelectedItem(n);
    }

    public void setEditOptions(String n) {
        getComps3().options.setText(n);
    }

    public void setEditLocale(String n) {
        getComps3().locale.setText(n);
    }

    public void setEditCpAppend(String n) {
        getComps3().cpAppend.setText(n);
    }

    public void setEditCpPrepend(String n) {
        getComps3().cpAppend.setText(n);
    }

    public String getEditUserdir() {
        return (String) getComps3().userdir.getSelectedItem();
    }

    public String getEditLaf() {
        return (String) getComps3().laf.getSelectedItem();
    }

    public String getEditCachedir() {
        return (String) getComps3().cachedir.getSelectedItem();
    }

    public String getEditName() {
        return (String) getComps3().name.getSelectedItem();
    }

    public String getEditOptions() {
        return (String) getComps3().options.getText();
    }

    public String getEditLocale() {
        return (String) getComps3().locale.getText();
    }

    public String getEditCpAppend() {
        return (String) getComps3().cpAppend.getText();
    }

    public String getEditCpPrepend() {
        return (String) getComps3().cpPrepend.getText();
    }

    public String getEditPath() {
        Object i = toolkit.getComboSelectedObject(getComps3().path);
        if (i instanceof NetbeansInstallation) {
            return ((NetbeansInstallation) i).getPath();
        } else {
            return i == null ? null : String.valueOf(i);
        }
    }

    public String getEditGroup() {
        Object g = toolkit.getComboSelectedObject(getComps3().group);
        return g == null ? null : (g instanceof NetbeansGroup) ? ((NetbeansGroup) g).getName() : String.valueOf(g);
    }

    public String getEditJdkHome() {
        Object i = toolkit.getComboSelectedObject(getComps3().jdkhome);
        if (i instanceof JdkInstallation) {
            return ((JdkInstallation) i).getPath();
        } else {
            return i == null ? null : String.valueOf(i);
        }
    }

    public int getEditFontSize() {
        try {
            int i = (Integer) getComps3().fontSize.getValue();
            if (i <= 0 || i > 72) {
                i = -1;
            }
            return i;
        } catch (Exception ex) {
            return -1;
        }
    }

    private void onOpenCachedir() {
        toolkit.openFolder(getEditCachedir());
    }

    private void onSelectCachedir() {
        JFileChooser c = new JFileChooser();
        c.setCurrentDirectory(configService.getCurrentDirectory());
        c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        c.setAcceptAllFileFilterUsed(false);
        if (JFileChooser.APPROVE_OPTION == c.showDialog(this, toolkit.msg("App.Workspace.SelectCacheDir").getText())) {
            File f = c.getSelectedFile();
            if (f != null) {
                configService.setCurrentDirectory(f);
                setEditCachedir(c.getSelectedFile().getPath());
            }
        }
    }

    private void onOpenUserdir() {
        toolkit.openFolder(getEditUserdir());
    }

    private void onSelectUserdir() {
        JFileChooser c = new JFileChooser();
        c.setCurrentDirectory(configService.getCurrentDirectory());
        c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        c.setAcceptAllFileFilterUsed(false);
        if (JFileChooser.APPROVE_OPTION == c.showDialog(this, toolkit.msg("App.Workspace.SelectUserDir").getText())) {
            File f = c.getSelectedFile();
            if (f != null) {
                configService.setCurrentDirectory(f);
                setEditUserdir(c.getSelectedFile().getPath());
            }
        }
    }

    private JComponent prepareTextField(JTextField options, int i) {
        options.setColumns(10);
        final Box b = Box.createHorizontalBox();
        b.add(options);
        options.setMaximumSize(new Dimension(550, 100));
//        b.setMaximumSize(new Dimension(600, 100));
//        b.setBorder(BorderFactory.createDashedBorder(Color.RED));
        return b;
    }
    private NetbeansWorkspace cached_lw;
    private NetbeansWorkspace cached_w;

    @Override
    public void onPreChangeCompatStatus(boolean compact) {
        cached_lw = lastWorkspace;
        cached_w = getWorkspace();
    }

    @Override
    public void onChangeCompatStatus(boolean compact) {
        super.onChangeCompatStatus(compact);
        setWorkspace(cached_w);
        lastWorkspace = cached_lw;
    }

    private Comp3 getComps3() {
        if (win.isCompact()) {
            if (compact == null) {
                compact = createComp3(true);
            }
            return compact;
        }
        if (nonCompact == null) {
            nonCompact = createComp3(false);
        }
        return nonCompact;
    }

    private Comp3 createComp3(boolean b) {
        Comp3 c = new Comp3();
        c.path = toolkit.createCombo();
        c.options = toolkit.createText();
        c.locale = toolkit.createText();
        c.cpAppend = toolkit.createText();
        c.cpPrepend = toolkit.createText();
        c.userdir = toolkit.createCombo();
        c.cachedir = toolkit.createCombo();
        c.laf = toolkit.createCombo();
        c.name = toolkit.createCombo();
        c.group = toolkit.createCombo();
        c.jdkhome = toolkit.createCombo();
        c.fontSize = new JSpinner();
        c.fontSize.setModel(new SpinnerNumberModel(0, 0, 72, 1));
        c.path.addItemListener(pathUpdatedChangeListener);
        c.userdir.addItemListener(updateGroupChangeListener);
        c.cachedir.addItemListener(updateGroupChangeListener);
        c.name.addItemListener((ItemEvent e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                onRequireUpdateDirProposals();
            }
        });
        JComponent buttonStart = toolkit.createIconButton("start", "App.Action.Start", () -> onStartWorkspace(), b);
        JComponent buttonSave = toolkit.createIconButton("save", "App.Action.Save", () -> onSaveWorkspacePane(), b);
        JComponent buttonClose = toolkit.createIconButton("close", "App.Action.Close", () -> onCloseWorkspacePane(), b);
        c.buttons = new JComponent[]{buttonStart, buttonSave, buttonClose};

        c.laf.setEditable(true);
        if (b) {
            createMainCompact(c);
        } else {
            createMainNonCompact(c);
        }
        return c;
    }

    private void onAddOption(JTextField options) {
        JVMOptionsPanel p = new JVMOptionsPanel(win.getAppContext().getWorkspace(), this,win.getToolkit());
        p.setArguments(options.getText());
        if (p.showDialog()) {
            options.setText(p.getArguments());
        }
    }
}
