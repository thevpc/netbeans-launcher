/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.netbeans.launcher.ui;

import java.awt.*;

import net.thevpc.netbeans.launcher.service.NetbeansLauncherModule;
import net.thevpc.netbeans.launcher.model.ConfirmResult;
import net.thevpc.netbeans.launcher.model.NetbeansWorkspace;
import net.thevpc.netbeans.launcher.ui.panes.*;
import net.thevpc.netbeans.launcher.ui.utils.*;
import net.thevpc.netbeans.launcher.util.NbUtils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

import net.thevpc.netbeans.launcher.model.NbOptions;
import net.thevpc.netbeans.launcher.util.RefreshContext;
import net.thevpc.nuts.app.NApp;

/**
 * @author thevpc
 */
public class MainWindowSwing {

    private static final Logger LOG = Logger.getLogger(MainWindowSwing.class.getName());
    protected NetbeansLauncherModule configService;
    protected SwingToolkit toolkit;
    private WorkspacePane workspacePane;
    private NetbeansWorkspaceListPane workspaceListPane;
    private SettingsPane settingsPane;
    private ConfirmPane confirmPane;
    private JVMOptions jvmOptions;
    protected JFrame frame;
    private AppPaneContainer appPaneContainer;
    private JComponent minimizeButton;
    private JComponent noZoomButton;
    private JComponent zoomInButton;
    private JComponent zoomOutButton;
    private JComponent enlargeButton;
    private JComponent compactButton;
    private JComponent exitButton;
    private JPanel headerButtons;
    private JLabel logo;
    private AppPane currentPane;
    private JComponent winHeader;

    public static void launch(NbOptions options, boolean wait) {
        SwingUtils2.prepareLaunch(options);
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        MainWindowSwing w = new MainWindowSwing();
        w.start(frame);

        if (SystemTray.isSupported()) {

            SystemTray tray = SystemTray.getSystemTray();
            Image image = SwingUtils2.loadIcon("nb.png", 32).getImage();

//            MouseListener mouseListener = new MouseListener() {
//
//                public void mouseClicked(MouseEvent e) {
//                    System.out.println("Tray Icon - Mouse clicked!");
//                }
//
//                public void mouseEntered(MouseEvent e) {
//                    System.out.println("Tray Icon - Mouse entered!");
//                }
//
//                public void mouseExited(MouseEvent e) {
//                    System.out.println("Tray Icon - Mouse exited!");
//                }
//
//                public void mousePressed(MouseEvent e) {
//                    System.out.println("Tray Icon - Mouse pressed!");
//                }
//
//                public void mouseReleased(MouseEvent e) {
//                    System.out.println("Tray Icon - Mouse released!");
//                }
//            };
//            ActionListener exitListener = new ActionListener() {
//                public void actionPerformed(ActionEvent e) {
//                    System.out.println("Exiting...");
//                    System.exit(0);
//                }
//            };
//            PopupMenu popup = new PopupMenu();
//            MenuItem defaultItem = new MenuItem("Exit");
//            defaultItem.addActionListener(exitListener);
//            popup.add(defaultItem);
            TrayIcon trayIcon = new TrayIcon(image, "Netbeans Launcher", null);
//            trayIcon.setPopupMenu(new JPopupMenu());
            ActionListener actionListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    switch (frame.getState()) {
                        case Frame.NORMAL: {
                            if (frame.isActive()) {
                                frame.setState(Frame.ICONIFIED);
                            } else {
                                frame.setVisible(false);
                                frame.setState(Frame.NORMAL);
                                frame.setVisible(true);
                                frame.toFront();
                                frame.repaint();
                            }
                            break;
                        }
                        case Frame.ICONIFIED: {
                            frame.setState(Frame.NORMAL);
                            frame.toFront();
                            frame.repaint();
                            break;
                        }
                    }
                }
            };

            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(actionListener);
//            trayIcon.addMouseListener(mouseListener);

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.err.println("TrayIcon could not be added.");
            }

        } else {

            //  System Tray is not supported
        }
        if (wait) {
            final Object lock = new Object();
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    LOG.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }
    }

    public MainWindowSwing() {
        this.configService = new NetbeansLauncherModule();
    }

    public SwingToolkit getToolkit() {
        return toolkit;
    }

    public void start(JFrame primaryStage) { //int2troadmin
        toolkit = new SwingToolkit(primaryStage);
        configService.conf().getSumoMode().addListener(t -> setFrameInfo(this.toolkit.getFrameInfo().setCompact(!t.getNewValue())));
        configService.conf().getInstallations().addListener(e -> updateList());
        configService.conf().getJdkLocations().addListener(e -> updateList());
        configService.conf().getWorkspaces().addListener(e -> updateList());
        toolkit.setFrameInfo(new FrameInfo(
                !configService.conf().getSumoMode().get(),
                configService.conf().getZoom().get()
        ));
        this.frame = primaryStage;
        primaryStage.setTitle("Netbeans Launcher " + NApp.of().getId().get().getVersion());
        primaryStage.setIconImage(new ImageIcon(MainWindowSwing.class.getResource("nb.png")).getImage());
        primaryStage.setResizable(false);
        JPanel basePanel = new JPanel(new BorderLayout());
        winHeader = createHeader();
        basePanel.add(winHeader, BorderLayout.NORTH);
        JPanel container = new JPanel(new BorderLayout());
        basePanel.add(container, BorderLayout.CENTER);
        appPaneContainer = new SlideAppPaneContainer(container);

        workspacePane = new WorkspacePane(this);
        workspaceListPane = new NetbeansWorkspaceListPane(this);
        settingsPane = new SettingsPane(this);
        confirmPane = new ConfirmPane(this);
        jvmOptions = new JVMOptions(this);

        addPane(workspacePane);
        addPane(workspaceListPane);
        addPane(settingsPane);
        addPane(confirmPane);
        addPane(jvmOptions);

        container.add(appPaneContainer.toPanel());
        primaryStage.getContentPane().add(basePanel);
        for (AppPane pane : getPanes()) {
            pane.onInit();
        }
        setSelectedPane(AppPaneType.LIST_WS);
        onChangeCompatStatus();
        frame.setUndecorated(true);
        primaryStage.setLocationRelativeTo(null);
        primaryStage.setVisible(true);
        if (configService.ws().findNetbeansWorkspaces().length == 0) {
            setSelectedPane(AppPaneType.SETTINGS);
            SettingsPane s = (SettingsPane) getPane(AppPaneType.SETTINGS);
            s.setSettingType(SettingsPane.SettingType.NB_INSTALLATION);
        }
        configService.conf().loadAsync(() -> {
            if (configService.ws().findNetbeansWorkspaces().length == 0) {
                setSelectedPane(AppPaneType.SETTINGS);
                SettingsPane s = (SettingsPane) getPane(AppPaneType.SETTINGS);
                s.setSettingType(SettingsPane.SettingType.NB_INSTALLATION);
            } else {
                setSelectedPane(AppPaneType.LIST_WS);
            }
        });
    }

    private JComponent createHeader() {
        int vgap = 1;//compact ? 1 : 13;
        int hgap = 1;//compact ? 1 : 5;
        BoxH winHeader = SwingUtils2.boxH().setVgap(vgap).setHgap(hgap).setOpaque(true).setName("global-header");
        Color cc = new Color(Integer.parseInt("336699", 16));
        winHeader.setBackground(
                SwingUtils2.componentGradientPaint(
                        new GradientPaint(0, 0, cc, 0, 32, cc.darker())
                )
        );
//        header.addAll(new JLabel("Netbeans Launcher"));
        JLabel icon = new JLabel(SwingUtils2.loadIcon("nb.png", 32));
        winHeader.add(icon);
        logo = new JLabel(SwingUtils2.loadIcon("logo.png", 63, 28));
        toolkit.prepareComponent(logo, new RefreshContext.Refresher() {
            @Override
            public void onRefresh(RefreshContext context) {
                int is = toolkit.iconSize();
                logo.setIcon(SwingUtils2.loadIcon("logo.png", 63+is-16, 28+is-16));
            }
        });
        winHeader.add(logo);
        headerButtons = new JPanel(new BorderLayout());
        headerButtons.setOpaque(false);
        winHeader.add(headerButtons);
        winHeader.addGlueH();

        zoomInButton = getToolkit().createIconButton("zoom-in", "App.Action.ZoomIn", () -> setFrameInfo(toolkit.getFrameInfo().zoomIn()));
        winHeader.add(zoomInButton);

        noZoomButton = getToolkit().createIconButton("zoom-out", "App.Action.NoZoom", () -> setFrameInfo(toolkit.getFrameInfo().zoomOut()));
        winHeader.add(noZoomButton);

        zoomOutButton = getToolkit().createIconButton("no-zoom", "App.Action.ZoomOut", () -> setFrameInfo(toolkit.getFrameInfo().zoomNone()));
        winHeader.add(zoomOutButton);

        minimizeButton = getToolkit().createIconButton("minimize-window", "App.Action.MinimizeWindow", () -> frame.setState(Frame.ICONIFIED));
        winHeader.add(minimizeButton);

        enlargeButton = getToolkit().createIconButton("enlarge", "App.Action.NonCompactMode", () -> setFrameInfo(toolkit.getFrameInfo().setCompact(false)));
        winHeader.add(enlargeButton);
        compactButton = getToolkit().createIconButton("compress", "App.Action.CompactMode", () -> setFrameInfo(toolkit.getFrameInfo().setCompact(true)));
        winHeader.add(compactButton);
        exitButton = getToolkit().createIconButton("exit", "App.Action.Exit", () -> confirmAndExit());
        winHeader.add(exitButton);
        setFrameInfo(toolkit.getFrameInfo());
        JComponent c = winHeader.toComponent();
        NbUtils.installMoveWin(c, frame);
        return c;
    }

    public AppPane[] getPanes() {
        return appPaneContainer.getAppPanes();
    }

    private void addPane(AppPane p) {
        appPaneContainer.addAppPane(p);
    }

    public void onAddWorkspace(NetbeansWorkspace w) {
        setSelectedPane(AppPaneType.EDIT_WS);
        workspacePane.onAddWorkspace(w);
    }

    public void onEditWorkspace(NetbeansWorkspace w) {
        try {
            if (w != null) {
                setSelectedPane(AppPaneType.EDIT_WS);
                workspacePane.onEditWorkspace(w);
            }
        } catch (Exception ex) {
            toolkit.showError(toolkit.msg("App.EditWorkspace.Error"), ex);
        }

    }

    public AppPane getPane(AppPaneType type) {
        for (AppPane appPane : appPaneContainer.getAppPanes()) {
            if (appPane.getPaneType() == type) {
                return appPane;
            }
        }
        return null;
    }

    public void setSelectedPane(AppPaneType type) {
        AppPane good = getPane(type);
        if (good != null) {
            this.currentPane = good;
            good.onSelectedPane();
            appPaneContainer.setAppPane(good);
            updateGlobalHeader();
        }
//        frame.pack();
    }

    public void updateGlobalHeader() {
        headerButtons.removeAll();
        headerButtons.add(currentPane.getHeader().toComponent(), BorderLayout.CENTER);
        currentPane.onRefreshHeader();
        int visible = 0;
        for (JComponent child : currentPane.getHeader().getChildren()) {
            if (child.isVisible()) {
                visible++;
            }
            toolkit.refreshComponent(child);
        }
        winHeader.invalidate();
        winHeader.revalidate();
        winHeader.repaint();
    }

    public void onRefreshHeader() {
        for (AppPane pane : getPanes()) {
            pane.onRefreshHeader();
        }
    }

    public void startWorkspace(NetbeansWorkspace w) {
        if (w != null) {
            if (NetbeansWorkspaceListPane.isStarted(w)) {
                return;
            }
            NetbeansWorkspaceListPane.setStarted(w);
            try {
                w.setLastLaunchDate(Instant.now());
                w.setExecutionCount(w.getExecutionCount() + 1);
                configService.ws().saveNetbeansWorkspace(w);
                frame.invalidate();
                frame.revalidate();
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            NbUtils.setTempRunning(w, true);
                            configService.ps().launchNetbeans(w);
                        } catch (Exception ex) {
                            toolkit.showError(toolkit.msg("App.RunWorkspace.Error"), ex);
                        } finally {
                            NbUtils.setTempRunning(w, false);
                            NetbeansWorkspaceListPane.setStopped(w);
                            onRefreshHeader();
                            updateList();
                        }
                    }

                }.start();
            } catch (Exception ex) {
                toolkit.showError(toolkit.msg("App.RunWorkspace.Error"), ex);
            } finally {
                onRefreshHeader();
                updateList();
            }
        }
    }

    public void updateList() {
        for (AppPane pane : getPanes()) {
            pane.updateAll();
        }
    }

    public boolean isCompact() {
        return toolkit.getFrameInfo().isCompact();
    }

    public void setFrameInfo(FrameInfo frameInfo) {
        if (!Objects.equals(toolkit.getFrameInfo(), frameInfo)) {
            toolkit.setFrameInfo(frameInfo);
            toolkit.refreshComponent(zoomInButton);
            toolkit.refreshComponent(zoomOutButton);
            toolkit.refreshComponent(noZoomButton);
            toolkit.refreshComponent(enlargeButton);
            toolkit.refreshComponent(compactButton);
            toolkit.refreshComponent(exitButton);
            toolkit.refreshComponent(minimizeButton);
            toolkit.refreshComponent(logo);
            onPreChangeCompatStatus();
            onChangeCompatStatus();
            updateGlobalHeader();
            configService.conf().setSumoMode(!frameInfo.isCompact());
            frame.invalidate();
            frame.revalidate();
            frame.repaint();
        }
        enlargeButton.setVisible(frameInfo.isCompact());
        compactButton.setVisible(!frameInfo.isCompact());
        if (appPaneContainer != null) {
            ((SlideAppPaneContainer) appPaneContainer).setSlideTime(
                    //                    compact ? 50 : 20
                    frameInfo.isCompact() ? 500 : 200
            );
        }
    }


    public void onPreChangeCompatStatus() {
        for (AppPane pane : getPanes()) {
            pane.onPreChangeCompatStatus(toolkit.getFrameInfo());
        }
    }

    public void onChangeCompatStatus() {
        for (AppPane pane : getPanes()) {
            pane.onChangeCompatStatus(toolkit.getFrameInfo());
        }
        int extraX=toolkit.getFrameInfo().getZoom()*16;
        int extraY=toolkit.getFrameInfo().getZoom()*16;
        if(extraX<0){
            extraX=0;
        }
        if(extraY<0){
            extraY=0;
        }
        Dimension dimension = toolkit.getFrameInfo().isCompact() ? new Dimension(410+extraX, 300+extraY) : new Dimension(800+extraX, 600+extraY);
        frame.setPreferredSize(dimension);
        frame.setSize(dimension);
    }

    public void confirmAndExit() {
        showConfirmOkCancel(
                toolkit.msg("App.Exit.Confirm.Title"),
                toolkit.msg("App.Exit.Confirm.Message"),
                () -> System.exit(0)
        );
    }

    interface AppPaneContainer {

        void addAppPane(AppPane pane);

        void setAppPane(AppPane pane);

        AppPane[] getAppPanes();

        Container toPanel();
    }

    public static class SlideAppPaneContainer implements AppPaneContainer {

        SimplePanelSlider root;
        List<AppPane> all = new ArrayList<>();
        private AppPanePos pos = new AppPanePos(-1000, -1000);

        public SlideAppPaneContainer(Container parent) {
            this.root = new SimplePanelSlider(parent);
        }

        public void setSlideTime(int slideTime) {
            root.setSlideTime(slideTime);
        }

        public void setSlideMinStep(int slideMinStep) {
            root.setSlideMinStep(slideMinStep);
        }

        @Override
        public void addAppPane(AppPane pane) {
            all.add(pane);
        }

        @Override
        public void setAppPane(AppPane pane) {
            AppPanePos newPos = pane.getPos();
            AppPanePos c = newPos.comp(this.pos);
            this.pos = newPos;
            if (c.getY() > 0) {
                root.slide(Direction.TOP, pane);
            } else if (c.getY() < 0) {
                root.slide(Direction.BOTTOM, pane);
            } else if (c.getX() > 0) {
                root.slide(Direction.LEFT, pane);
            } else if (c.getX() < 0) {
                root.slide(Direction.RIGHT, pane);
            } else {
                root.slide(Direction.RIGHT, pane);
            }
        }

        @Override
        public AppPane[] getAppPanes() {
            return all.toArray(new AppPane[0]);
        }

        @Override
        public Container toPanel() {
            return root.getBasePanel();
        }
    }

    private static class CardAppPaneContainer implements AppPaneContainer {

        private JPanel root;

        public CardAppPaneContainer() {
            this.root = new JPanel(new CardLayout());
        }

        public AppPane[] getAppPanes() {
            List<AppPane> all = new ArrayList<>();
            for (Component component : root.getComponents()) {
                if (component instanceof AppPane) {
                    all.add((AppPane) component);
                }
            }
            return all.toArray(new AppPane[0]);
        }

        @Override
        public void addAppPane(AppPane pane) {
            root.add(pane);
        }

        @Override
        public JPanel toPanel() {
            return root;
        }

        @Override
        public void setAppPane(AppPane pane0) {
            for (AppPane pane : getAppPanes()) {
                pane.setVisible(pane == pane0);
            }
        }
    }

    public void showConfirmOkCancel(SwingToolkit.Message title, SwingToolkit.Message message, Runnable todo) {
        showConfirmOkCancel(title, message, todo, null);
    }

    public void showConfirmOkCancel(SwingToolkit.Message title, SwingToolkit.Message message, Runnable todo, Runnable todoElse) {
        if (currentPane.paneType != AppPaneType.CONFIRM) {
            confirmPane.initOkCancel(currentPane.paneType, title, message, ok -> {
                if (ok == ConfirmResult.OK) {
                    if (todo != null) {
                        todo.run();
                    }
                } else {
                    if (todoElse != null) {
                        todoElse.run();
                    }
                }
            });
            setSelectedPane(AppPaneType.CONFIRM);
        }
    }

    public void showConfirmYesNoCancel(SwingToolkit.Message title, SwingToolkit.Message message, Runnable yes, Runnable no, Runnable cancel) {
        if (currentPane.paneType != AppPaneType.CONFIRM) {
            confirmPane.initYesNoCancel(currentPane.paneType, title, message, ok -> {
                if (ok == ConfirmResult.YES) {
                    if (yes != null) {
                        yes.run();
                    }
                } else if (ok == ConfirmResult.NO) {
                    if (no != null) {
                        no.run();
                    }
                } else {
                    if (cancel != null) {
                        cancel.run();
                    }
                }
            });
            setSelectedPane(AppPaneType.CONFIRM);
        }
    }

    public NetbeansLauncherModule getConfigService() {
        return configService;
    }
}
