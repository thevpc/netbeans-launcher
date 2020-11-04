package net.thevpc.netbeans.launcher.ui.panes;

import net.thevpc.netbeans.launcher.model.NetbeansBinaryLink;
import net.thevpc.netbeans.launcher.model.NetbeansInstallation;
import net.thevpc.netbeans.launcher.model.NetbeansInstallationStore;
import net.thevpc.netbeans.launcher.ui.utils.CatalogComponent;
import net.thevpc.netbeans.launcher.ui.utils.ListComponent;
import net.thevpc.netbeans.launcher.util.JlistToStringer;
import net.thevpc.netbeans.launcher.ui.MainWindowSwing;

public class NbListComponentAsList extends NbListComponent {

    private ListComponent table;
    protected JlistToStringer nbLinkStringer = new JlistToStringer(2) {
        @Override
        public String toString(Object value, int level) {
            if (value instanceof NetbeansBinaryLink) {
                NetbeansBinaryLink i = (NetbeansBinaryLink) value;
                boolean _downloading = isDownloadingVersion(i.getVersion());
                return "[Available] Netbeans " + i.getVersion() + (_downloading ? " (downloading...)" : "");
            }
            if (value instanceof NetbeansInstallation) {
                NetbeansInstallation i = (NetbeansInstallation) value;
                boolean _downloading = false;
                if (i.getStore() == NetbeansInstallationStore.DEFAULT) {
                    _downloading = NbListComponent.isDownloadingVersion(i.getVersion());
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

    public NbListComponentAsList(MainWindowSwing win, Runnable _onRequiredUpdateButtonStatuses) {
        super(win,_onRequiredUpdateButtonStatuses);
    }

    @Override
    protected CatalogComponent createCatalog() {
        return new ListComponent().setStringer(nbLinkStringer);
    }

}
