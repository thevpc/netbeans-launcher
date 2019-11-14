package net.vpc.app.netbeans.launcher.ui.panes;

import java.util.ArrayList;
import net.vpc.app.netbeans.launcher.model.NetbeansBinaryLink;
import net.vpc.app.netbeans.launcher.model.NetbeansInstallation;
import net.vpc.app.netbeans.launcher.model.NetbeansWorkspace;
import net.vpc.app.netbeans.launcher.ui.AppPaneType;
import net.vpc.app.netbeans.launcher.ui.MainWindowSwing;
import net.vpc.app.netbeans.launcher.util.SwingWorker;
import net.vpc.app.netbeans.launcher.util.Workers;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import net.vpc.app.netbeans.launcher.model.NetbeansLocation;
import net.vpc.app.netbeans.launcher.ui.utils.CatalogComponent;

public abstract class NbListComponent {

    protected MainWindowSwing win;
    protected CatalogComponent table;
    protected Runnable _onRequiredUpdateButtonStatuses;
    private static final Set<String> downloading = new HashSet<>();

    public NbListComponent(MainWindowSwing win,Runnable _onRequiredUpdateButtonStatuses) {
        this.win = win;
        this._onRequiredUpdateButtonStatuses = _onRequiredUpdateButtonStatuses;
        table = createCatalog();
        prepare();
    }

    protected abstract CatalogComponent createCatalog();

    public static boolean isDownloadingVersion(String v) {
        synchronized (downloading) {
            if (downloading.contains(v)) {
                return true;
            }
        }
        return false;
    }

    public NetbeansBinaryLink getSelectedRemoteNbInstallation() {
        Object v = getSelectedValue();
        if (v instanceof NetbeansBinaryLink) {
            return (NetbeansBinaryLink) v;
        }
        return null;
    }

    public boolean markForDownload(String v) {
        if (!downloading.contains(v)) {
            downloading.add(v);
            return true;
        } else {
            return false;
        }
    }

    public boolean unmarkForDownload(String v) {
        return downloading.remove(v);
    }

    public void onDownload() {
        SwingWorker w = Workers.richWorker();
        Object v = getSelectedValue();
        if (v instanceof NetbeansBinaryLink) {
            NetbeansBinaryLink i = (NetbeansBinaryLink) v;
            if (markForDownload(i.getVersion())) {
                w.store("i", i);
            }
            win.showConfirmOkCancel(
                    win.getToolkit().msg("App.Download.Confirm.Title"),
                    win.getToolkit().msg("App.Download.Confirm.Message").with("name", i.toString()),
                    () -> {
                        w.run(() -> {
                            NetbeansInstallation n = win.getConfigService().installNetbeansBinary(w.load("i"));
                            if (n != null) {
                                w.store("n", n);
                            }
                        })
                                .onError(ex -> win.getToolkit().showError(win.getToolkit().msg("App.Download.Error"), ex))
                                .onSuccess(() -> {
                                    win.updateList();
                                    win.setSelectedPane(AppPaneType.LIST_WS);
                                    NetbeansInstallation n = w.load("n");
                                    if (n != null) {
                                        NbListPane ws = (NbListPane) win.getPane(AppPaneType.LIST_WS);
                                        ws.setSelectedWorkspace((NetbeansWorkspace) v);
                                    }
                                })
                                .onFinally(() -> {
                                    NetbeansBinaryLink ii = w.load("i");
                                    if (ii != null) {
                                        unmarkForDownload(ii.getVersion());
                                    }
                                })
                                .start();
                    },
                    () -> {
                        NetbeansBinaryLink ii = w.load("i");
                        if (ii != null) {
                            unmarkForDownload(ii.getVersion());
                        }
                    }
            );
        } else if (v instanceof NetbeansInstallation) {
            win.setSelectedPane(AppPaneType.LIST_WS);
            NbListPane ws = (NbListPane) win.getPane(AppPaneType.LIST_WS);
            ws.setSelectedWorkspace((NetbeansWorkspace) v);
        }
    }

    public NetbeansLocation[] load() {
        java.util.List<NetbeansLocation> rr = new ArrayList<>();
        Set<String> versions = new TreeSet<>();
        for (NetbeansInstallation netbeansInstallation : win.getConfigService().getAllNb()) {
            rr.add(netbeansInstallation);
            versions.add(netbeansInstallation.getVersion());
        }
        for (NetbeansBinaryLink netbeansBinaryLink : win.getConfigService().searchRemoteInstallableNbBinaries()) {
            if (!versions.contains(netbeansBinaryLink.getVersion())) {
                rr.add(netbeansBinaryLink);
            }
        }
        return rr.toArray(new NetbeansLocation[0]);
    }

    public void refresh() {
        win.getToolkit().updateTable(table, load(),
                (a, b) -> {
                    if (a == null || b == null) {
                        return a == b;
                    }
                    if (a instanceof NetbeansBinaryLink && b instanceof NetbeansBinaryLink) {
                        return ((NetbeansBinaryLink) a).getVersion().equals(((NetbeansBinaryLink) b).getVersion());
                    }
                    if (a instanceof NetbeansInstallation && b instanceof NetbeansInstallation) {
                        return ((NetbeansInstallation) a).getName().equals(((NetbeansInstallation) b).getName());
                    }
                    return false;
                }, null);
        table.setElementHeight(win.isCompact() ? 30 : 50);
    }

    public JComponent toComponent() {
        return table.toComponent();
    }

    public Object getSelectedValue() {
        return table.getSelectedValue();
    }

    public int getSelectedIndex() {
        return table.getSelectedIndex();
    }

    public void setSelectedIndex(int i) {
        table.setSelectedIndex(i);
    }

    protected void prepare() {
        table.setElementHeight(win.isCompact() ? 30 : 50);
        table.addListSelectionListener(new CatalogComponent.ObjectSelectionListener() {
            @Override
            public void onObjectSelected(CatalogComponent.ObjectSelectionEvent event) {
                if (_onRequiredUpdateButtonStatuses != null) {
                    _onRequiredUpdateButtonStatuses.run();
                }
            }
        });
        table.addMouseSelection(new CatalogComponent.ObjectSelectionListener() {
            @Override
            public void onObjectSelected(CatalogComponent.ObjectSelectionEvent event) {
                if (event.getMouseEvent().getClickCount() == 2) {
                    onDownload();
                }
            }
        });
        table.addEnterSelection(new CatalogComponent.ObjectSelectionListener() {
            @Override
            public void onObjectSelected(CatalogComponent.ObjectSelectionEvent event) {
                onDownload();
            }
        });
    }

}
