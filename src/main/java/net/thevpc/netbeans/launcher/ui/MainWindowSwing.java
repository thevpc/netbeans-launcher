/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.netbeans.launcher.ui;

import java.awt.*;

import net.thevpc.netbeans.launcher.NetbeansConfigService;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

import net.thevpc.netbeans.launcher.NbOptions;
import net.thevpc.nuts.NutsApplicationContext;

/**
 * @author thevpc
 */
public class MainWindowSwing {

    private static final Logger LOG = Logger.getLogger(MainWindowSwing.class.getName());
    protected NetbeansConfigService configService;
    protected SwingToolkit toolkit;
    private WorkspacePane workspacePane;
    private NbListPane workspaceListPane;
    private SettingsPane settingsPane;
    private ConfirmPane confirmPane;
    private JVMOptions jvmOptions;
    protected JFrame frame;
    private boolean compact = false;
    private NutsApplicationContext appContext;
    private AppPaneContainer appPaneContainer;
    private JComponent minimizeButton;
    private JComponent enlargeButton;
    private JComponent compactButton;
    private JComponent exitButton;
    private JPanel headerButtons;
    private AppPane currentPane;
    private JComponent winHeader;

    public static void launch(NutsApplicationContext appContext, NbOptions options, boolean wait) {
        SwingUtils2.prepareLaunch(options);
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        MainWindowSwing w = new MainWindowSwing(appContext);
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

    public NutsApplicationContext getAppContext() {
        return appContext;
    }

    public MainWindowSwing(NutsApplicationContext appContext) {
        this.appContext = appContext;
        this.configService = new NetbeansConfigService(appContext);
    }

    public SwingToolkit getToolkit() {
        return toolkit;
    }

    public void start(JFrame primaryStage) { //int2troadmin
        toolkit = new SwingToolkit(primaryStage);
        configService.getConfig().getSumoMode().addListener(t -> setCompact(!t.getNewValue()));
        configService.getConfig().getInstallations().addListener(e -> updateList());
        configService.getConfig().getJdkLocations().addListener(e -> updateList());
        configService.getConfig().getWorkspaces().addListener(e -> updateList());
        compact = !configService.getConfig().getSumoMode().get();
        this.frame = primaryStage;
        primaryStage.setTitle("Netbeans Launcher " + NbUtils.getArtifactVersionOrDev());
        primaryStage.setIconImage(new ImageIcon(MainWindowSwing.class.getResource("nb.png")).getImage());
        primaryStage.setResizable(false);
        JPanel basePanel = new JPanel(new BorderLayout());
        winHeader = createHeader();
        basePanel.add(winHeader, BorderLayout.NORTH);
        JPanel container = new JPanel(new BorderLayout());
        basePanel.add(container, BorderLayout.CENTER);
        appPaneContainer = new SlideAppPaneContainer(container);

        workspacePane = new WorkspacePane(this);
        workspaceListPane = new NbListPane(this);
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
        onChangeCompatStatus(compact);
        frame.setUndecorated(true);
        primaryStage.setLocationRelativeTo(null);
        primaryStage.setVisible(true);
        if (configService.getAllNbWorkspaces().length == 0) {
            setSelectedPane(AppPaneType.SETTINGS);
            SettingsPane s = (SettingsPane) getPane(AppPaneType.SETTINGS);
            s.setSettingType(SettingsPane.SettingType.NB_INSTALLATION);
        }
        configService.loadAsync();
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
        JLabel logo = new JLabel(SwingUtils2.loadIcon("logo.png", 63, 28));
        winHeader.add(logo);
        headerButtons = new JPanel(new BorderLayout());
        headerButtons.setOpaque(false);
        winHeader.add(headerButtons);
        winHeader.addGlueH();

        minimizeButton = getToolkit().createIconButton("minimize-window", "App.Action.MinimizeWindow", () -> frame.setState(Frame.ICONIFIED), compact);
        winHeader.add(minimizeButton);
        enlargeButton = getToolkit().createIconButton("enlarge", "App.Action.NonCompactMode", () -> setCompact(false), compact);
        winHeader.add(enlargeButton);
        compactButton = getToolkit().createIconButton("compress", "App.Action.CompactMode", () -> setCompact(true), compact);
        winHeader.add(compactButton);
        exitButton = getToolkit().createIconButton("exit", "App.Action.Exit", () -> confirmAndExit(), compact);
        winHeader.add(exitButton);
        setCompact(compact);
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
            if (NbListPane.isStarted(getAppContext(), w)) {
                return;
            }
            NbListPane.setStarted(getAppContext(), w);
            try {
                w.setLastLaunchDate(Instant.now());
                w.setExecutionCount(w.getExecutionCount() + 1);
                configService.saveNbWorkspace(w);
                frame.invalidate();
                frame.revalidate();
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            NbUtils.setTempRunning(w, true);
                            configService.run(w);
                        } catch (Exception ex) {
                            toolkit.showError(toolkit.msg("App.RunWorkspace.Error"), ex);
                        } finally {
                            NbUtils.setTempRunning(w, false);
                            NbListPane.setStopped(getAppContext(), w);
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
        return compact;
    }

    public void setCompact(boolean compact) {
        if (this.compact != compact) {
            onPreChangeCompatStatus(compact);
            this.compact = compact;
            onChangeCompatStatus(compact);
            configService.setSumoMode(!compact);
            frame.invalidate();
            frame.revalidate();
            frame.repaint();
        }
        enlargeButton.setVisible(compact);
        compactButton.setVisible(!compact);
        if (appPaneContainer != null) {
            ((SlideAppPaneContainer) appPaneContainer).setSlideTime(
                    //                    compact ? 50 : 20
                    compact ? 500 : 200
            );
        }
    }

    public void onPreChangeCompatStatus(boolean compact) {
        for (AppPane pane : getPanes()) {
            pane.onPreChangeCompatStatus(compact);
        }
    }

    public void onChangeCompatStatus(boolean compact) {
        this.toolkit.setCompact(compact);
        for (AppPane pane : getPanes()) {
            pane.onChangeCompatStatus(compact);
        }
        Dimension dimension = compact ? new Dimension(380, 300) : new Dimension(700, 500);
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

    public NetbeansConfigService getConfigService() {
        return configService;
    }
}
