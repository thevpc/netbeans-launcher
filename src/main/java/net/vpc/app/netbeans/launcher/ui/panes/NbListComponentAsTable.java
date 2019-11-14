package net.vpc.app.netbeans.launcher.ui.panes;

import net.vpc.app.netbeans.launcher.model.NetbeansBinaryLink;
import net.vpc.app.netbeans.launcher.model.NetbeansInstallation;
import net.vpc.app.netbeans.launcher.model.NetbeansInstallationStore;
import net.vpc.app.netbeans.launcher.model.NetbeansLocation;
import net.vpc.app.netbeans.launcher.ui.MainWindowSwing;
import net.vpc.app.netbeans.launcher.ui.utils.ObjectTableModel;
import net.vpc.app.netbeans.launcher.util.JlistToStringer;

import java.util.List;
import net.vpc.app.netbeans.launcher.ui.utils.CatalogComponent;
import net.vpc.app.netbeans.launcher.ui.utils.TableComponent;

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
        super(win,_onRequiredUpdateButtonStatuses);
    }

    protected CatalogComponent createCatalog() {
        return new TableComponent().setColumns(new ObjectTableModel.NamedColumns<NetbeansLocation>(
                new String[]{"Name", "Status"}
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
                }
                return nbLinkStringer.toString(m, value);
            }
        }.setColumnSizes(new float[]{1,2}));

    }

    
}
