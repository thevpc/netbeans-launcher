package net.vpc.app.netbeans.launcher.ui.panes;

import net.vpc.app.netbeans.launcher.model.NetbeansBinaryLink;
import net.vpc.app.netbeans.launcher.model.NetbeansInstallation;
import net.vpc.app.netbeans.launcher.model.NetbeansInstallationStore;
import net.vpc.app.netbeans.launcher.model.NetbeansLocation;
import net.vpc.app.netbeans.launcher.ui.MainWindowSwing;
import net.vpc.app.netbeans.launcher.ui.utils.CatalogComponent;
import net.vpc.app.netbeans.launcher.ui.utils.ObjectTableModel;
import net.vpc.app.netbeans.launcher.ui.utils.TableComponent;
import net.vpc.app.netbeans.launcher.util.JlistToStringer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class NbListComponentAsTable extends NbListComponent {

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

    public NbListComponentAsTable(MainWindowSwing win, Runnable _onRequiredUpdateButtonStatuses) {
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
                                _downloading = NbListComponent.isDownloadingVersion(i.getVersion());
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
        tab.getTable().getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String s = String.valueOf(value);
                if ("Not Installed".equals(s)) {
                    label.setToolTipText("Double click to install");
                } else {
                    label.setToolTipText(null);
                }
                return label;
            }
        });
        tab.getTable().getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                Object oo = null;
                try {
                    oo = NbListComponentAsTable.this.table.getValues().get(row);
                }catch (Exception e){
                    //
                }
                if(oo instanceof NetbeansBinaryLink) {
                    setIcon(win.getToolkit().createIcon("download", win.isCompact()));
                }else{
                    setIcon(win.getToolkit().createIcon("anb", win.isCompact()));
                }
                return label;
            }
        });
        return tab;

    }


}
