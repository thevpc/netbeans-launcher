package net.thevpc.netbeans.launcher.ui.panes;

import net.thevpc.netbeans.launcher.model.NetbeansBinaryLink;
import net.thevpc.netbeans.launcher.model.NetbeansInstallation;
import net.thevpc.netbeans.launcher.model.NetbeansInstallationStore;
import net.thevpc.netbeans.launcher.model.NetbeansLocation;
import net.thevpc.netbeans.launcher.ui.utils.CatalogComponent;
import net.thevpc.netbeans.launcher.ui.utils.ObjectTableModel;
import net.thevpc.netbeans.launcher.ui.utils.TableComponent;
import net.thevpc.netbeans.launcher.util.JlistToStringer;
import net.thevpc.netbeans.launcher.ui.MainWindowSwing;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NetbeansInstallationListComponentAsTable extends NetbeansInstallationListComponent {

    protected JlistToStringer nbLinkStringer = new JlistToStringer(2) {
        @Override
        public String toString(Object value, int level) {
            if (value instanceof NetbeansBinaryLink) {
                NetbeansBinaryLink i = (NetbeansBinaryLink) value;
                return "Netbeans IDE " + i.getVersion();
            }
            if (value instanceof NetbeansInstallation) {
                NetbeansInstallation i = (NetbeansInstallation) value;
                return i.getName();
            }
            return String.valueOf(value);
        }

    };

    public NetbeansInstallationListComponentAsTable(MainWindowSwing win, Runnable _onRequiredUpdateButtonStatuses) {
        super(win, _onRequiredUpdateButtonStatuses);
    }

    @Override
    protected CatalogComponent createCatalog() {
        TableComponent tab = new TableComponent().setColumns(new ObjectTableModel.NamedColumns<NetbeansLocation>(
                new String[]{"Name", "Status", "Release Date"}
        ) {
            @Override
            public boolean isCellEditable(int row, String column, NetbeansLocation netbeansLocation) {
                return false;
            }

            @Override
            public Object getValueAt(int row, String column, NetbeansLocation value) {
                List m = table.getValues();
                switch (column) {
                    case "Name":
                        nbLinkStringer.toString(m, value);
                        break;
                    case "Status": {
                        boolean _downloading = isDownloadingVersion(value.getVersion());
                        if (!_downloading) {
                            if (value instanceof NetbeansBinaryLink) {
                                return "Not Installed";
                            }
                            if (value instanceof NetbeansInstallation) {
                                NetbeansInstallation i = (NetbeansInstallation) value;
                                switch (i.getStore()) {
                                    case USER: {
                                        return i.getPath();
                                    }
                                    case SYSTEM: {
                                        return "(system)";
                                    }
                                    case DEFAULT: {
                                        return "Installed";
                                    }
                                }
                            }
                        }
                        if (value instanceof NetbeansInstallation) {
                            NetbeansInstallation i = (NetbeansInstallation) value;
                            if (i.getStore() == NetbeansInstallationStore.DEFAULT) {
                                _downloading = NetbeansInstallationListComponent.isDownloadingVersion(i.getVersion());
                            }
                        }
                        if (_downloading) {
                            return "downloading...";
                        }
                        return "";
                    }
                    case "Release Date": {
                        Instant r = value.getReleaseDate();
                        if (r == null) {
                            return "";
                        } else {
                            return DateTimeFormatter.ofPattern("yyyy MMM dd").format(LocalDateTime.ofInstant(r, ZoneOffset.UTC));
//                                    DateTimeFormatter.ISO_INSTANT.format(r.truncatedTo(ChronoUnit.MINUTES));
                        }
                    }
                }
                return nbLinkStringer.toString(m, value);
            }
        }.setColumnSizes(new float[]{1.5f, 1f, 0.75f}));
        Font initialFont = tab.getTable().getFont();
        tab.getTable().getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String s = String.valueOf(value);
                if ("Not Installed".equals(s)) {
                    setToolTipText("Double click to install");
                } else {
                    setToolTipText(null);
                }
                setFont(toolkit.deriveFont(initialFont));
                return this;
            }
        });
        tab.getTable().getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                Object oo = null;
                try {
                    oo = NetbeansInstallationListComponentAsTable.this.table.getValues().get(row);
                } catch (Exception e) {
                    //
                }
                if (oo instanceof NetbeansBinaryLink) {
                    setIcon(win.getToolkit().createIcon("download"));
                } else {
                    setIcon(win.getToolkit().createIcon("anb"));
                }
                setFont(toolkit.deriveFont(initialFont));
                return this;
            }
        });
        tab.getTable().getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setFont(toolkit.deriveFont(initialFont));
                return this;
            }
        });
        return tab;

    }


}
