package net.thevpc.netbeans.launcher.ui.panes;

import java.util.ArrayList;

import net.thevpc.netbeans.launcher.model.NetbeansBinaryLink;
import net.thevpc.netbeans.launcher.model.NetbeansInstallation;
import net.thevpc.netbeans.launcher.model.NetbeansLocation;
import net.thevpc.netbeans.launcher.model.SortType;
import net.thevpc.netbeans.launcher.ui.utils.CatalogComponent;
import net.thevpc.netbeans.launcher.ui.utils.Equalizer;
import net.thevpc.netbeans.launcher.ui.utils.SwingToolkit;
import net.thevpc.netbeans.launcher.util.SwingWorker;
import net.thevpc.netbeans.launcher.util.Workers;
import net.thevpc.netbeans.launcher.ui.AppPaneType;
import net.thevpc.netbeans.launcher.ui.MainWindowSwing;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public abstract class NetbeansInstallationListComponent {

    protected MainWindowSwing win;
    protected SwingToolkit toolkit;
    protected CatalogComponent table;
    protected Runnable _onRequiredUpdateButtonStatuses;
    private static final Set<String> downloading = new HashSet<>();

    public NetbeansInstallationListComponent(MainWindowSwing win, Runnable _onRequiredUpdateButtonStatuses) {
        this.win = win;
        this.toolkit = win.getToolkit();
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
//        if (v instanceof NetbeansInstallation) {
//            NetbeansBinaryLink ni = win.getConfigService().searchNetbeansBinaryLinkForInstallation((NetbeansInstallation) v);
//            if (ni != null) {
//                v = ni;
//            }
//        }
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
                                    NetbeansInstallation n = win.getConfigService().ins().addNetbeansInstallationByLink(w.load("i"));
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
                                        NetbeansWorkspaceListPane ws = (NetbeansWorkspaceListPane) win.getPane(AppPaneType.LIST_WS);
                                        ws.setSelectedWorkspace(i);
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
            NetbeansWorkspaceListPane ws = (NetbeansWorkspaceListPane) win.getPane(AppPaneType.LIST_WS);
            ws.setSelectedWorkspace((NetbeansInstallation) v);
        }
    }

    private String extractUniformVersion(String version) {
        if (version == null) {
            return null;
        }
        int e = version.indexOf('-');
        if (e >= 0) {
            return version.substring(0, e);
        }
        return version;
    }

    public NetbeansLocation[] load(boolean withRemote, boolean cached) {
        win.getConfigService().conf().waitForConfigLoaded();
        java.util.List<NetbeansLocation> rr = new ArrayList<>();
        Set<String> versions = new TreeSet<>();
        for (NetbeansInstallation netbeansInstallation : win.getConfigService().ins().findNetbeansInstallations(SortType.LATEST_FIRST)) {
            rr.add(netbeansInstallation);
            versions.add(extractUniformVersion(netbeansInstallation.getVersion()));
        }
        if (withRemote) {
            for (NetbeansBinaryLink netbeansBinaryLink : win.getConfigService().ins().searchRemoteInstallableNbBinariesWithCache(cached)) {
                if (!versions.contains(extractUniformVersion(netbeansBinaryLink.getVersion()))) {
                    rr.add(netbeansBinaryLink);
                }
            }
        }
        return rr.stream().sorted(win.getConfigService().ins().comparator(SortType.LATEST_FIRST)).toArray(NetbeansLocation[]::new);
    }

    public void refresh() {
        refresh(true);
    }

    public void refresh(boolean cached) {
        new Thread(() -> {
            win.getToolkit().updateTable(table, load(false, cached), new NetbeansInstallOrBinaryEqualizer(), null, null);
            win.getToolkit().updateTable(table, load(true, cached), new NetbeansInstallOrBinaryEqualizer(), null, null);
        }).start();
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

    private static class NetbeansInstallOrBinaryEqualizer implements Equalizer {

        @Override
        public boolean equals(Object a, Object b) {
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
        }
    }
}
