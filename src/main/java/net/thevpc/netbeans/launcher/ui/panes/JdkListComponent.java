/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.netbeans.launcher.ui.panes;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import net.thevpc.netbeans.launcher.model.NetbeansWorkspace;
import net.thevpc.netbeans.launcher.ui.MainWindowSwing;
import net.thevpc.netbeans.launcher.ui.utils.ObjectTableModel;
import net.thevpc.netbeans.launcher.ui.utils.TableComponent;
import net.thevpc.netbeans.launcher.util.ObservableList;
import net.thevpc.netbeans.launcher.util.ObservableListEvent;
import net.thevpc.nuts.platform.NExecutionEngineLocation;

/**
 *
 * @author vpc
 */
public class JdkListComponent extends TableComponent {

    private SettingsPane parent;
    private MainWindowSwing win;

    public JdkListComponent(MainWindowSwing win, SettingsPane parent) {
        this.win = win;
        this.parent = parent;
        this.setElementHeight(win.isCompact() ? 30 : 50);
        /*if (this instanceof ListComponent) {
            ((ListComponent) this).setStringer(jdkStringer);
        } else */
        if (this instanceof TableComponent) {
            setColumns(new ObjectTableModel.NamedColumns<NExecutionEngineLocation>(
                    win.isCompact() ? new String[]{"Name"}
                    : new String[]{"Name", "Type", "Version", "Location"}
            ) {
                @Override
                public Object getValueAt(int row, String column, NExecutionEngineLocation t) {
                    switch (column) {
                        case "Name":
                            return t == null ? "<null>" : t.getName();
                        case "Type":
                            return t == null ? "<null>" : t.getPackaging();
                        case "Version":
                            return t == null ? "<null>" : t.getVersion();
                        case "Location":
                            return t == null ? "<null>" : t.getPath();
                    }
                    return "";
                }
            }.setColumnSizes(new float[]{3, 1, 1, 5}));

            setColumnRenderer("Name", new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    setIcon(win.getToolkit().createIcon("java"));
                    label.setHorizontalAlignment(JLabel.LEFT);
                    return label;
                }
            });
            setColumnRenderer("Type", new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    label.setHorizontalAlignment(JLabel.CENTER);
                    return label;
                }
            });
            setColumnRenderer("Version", new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    label.setHorizontalAlignment(JLabel.CENTER);
                    return label;
                }
            });
            setColumnRenderer("Location", new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    label.setHorizontalAlignment(JLabel.LEFT);
                    return label;
                }
            });
        }
        this.addListSelectionListener((e) -> {
            parent.onRequiredUpdateButtonStatuses();
        });

        win.getConfigService().conf()
                .getJdkLocations().addListener(new ObservableList.ObservableListListener<NExecutionEngineLocation>() {
                    @Override
                    public void onAdd(ObservableListEvent<NExecutionEngineLocation> event) {
                        updateJdkList();
                    }

                    @Override
                    public void onRemove(ObservableListEvent<NExecutionEngineLocation> event) {
                        updateJdkList();
                    }

                    @Override
                    public void onUpdate(ObservableListEvent<NExecutionEngineLocation> event) {
                        updateJdkList();
                    }
                });

        win.getConfigService().conf()
                .getWorkspaces().addListener(new ObservableList.ObservableListListener<NetbeansWorkspace>() {
                    @Override
                    public void onAdd(ObservableListEvent<NetbeansWorkspace> event) {
                        updateJdkList();
                    }

                    @Override
                    public void onRemove(ObservableListEvent<NetbeansWorkspace> event) {
                        updateJdkList();
                    }

                    @Override
                    public void onUpdate(ObservableListEvent<NetbeansWorkspace> event) {
                        updateJdkList();
                    }
                });
    }

    public void updateJdkList() {
        parent.getNbToolkit().updateTable(this, win.getConfigService().jdk().findAllJdks(), (a, b) -> a != null && b != null && ((NExecutionEngineLocation) a).getName().equals(((NExecutionEngineLocation) b).getName()), null, null);
    }

}
