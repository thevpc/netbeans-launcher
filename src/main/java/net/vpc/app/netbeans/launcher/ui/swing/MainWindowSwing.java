/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.netbeans.launcher.ui.swing;

import java.awt.*;

import net.vpc.app.netbeans.launcher.ui.swing.utils.GridBagLayout2;
import net.vpc.app.netbeans.launcher.ui.swing.utils.SimplePanelSlider;
import net.vpc.app.netbeans.launcher.util.NbUtils;
import net.vpc.app.netbeans.launcher.model.NetbeansWorkspace;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.*;

import net.vpc.app.netbeans.launcher.NbOptions;
import net.vpc.app.netbeans.launcher.NetbeansConfigService;
import net.vpc.app.netbeans.launcher.ui.Grid;
import net.vpc.app.netbeans.launcher.ui.PaneType;
import net.vpc.app.netbeans.launcher.ui.swing.panes.SettingsPane;
import net.vpc.app.netbeans.launcher.ui.swing.panes.NbListPane;
import net.vpc.app.netbeans.launcher.ui.swing.panes.WorkspacePane;
import net.vpc.app.nuts.NutsApplicationContext;

/**
 * @author vpc
 */
public class MainWindowSwing {

    protected NetbeansConfigService configService;
    protected SwingToolkit toolkit;
    protected final Set<String> running = new HashSet<String>();
    private WorkspacePane workspacePane;
    private NbListPane workspaceListPane;
    private SettingsPane settingsPane;
    protected JFrame frame;
    private boolean compact = false;
    private NutsApplicationContext appContext;
    private AppPaneContainer appPaneContainer;
    private JComponent minimizeButton;
    private JComponent enlargeButton;
    private JComponent compressButton;
    private JComponent exitButton;

    public static void launch(NutsApplicationContext appContext, NbOptions options) {
        MainWindowSwingHelper.prepareLaunch(options);
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        MainWindowSwing w = new MainWindowSwing(appContext);
        w.start(frame);

        if (SystemTray.isSupported()) {

            SystemTray tray = SystemTray.getSystemTray();
            Image image = MainWindowSwingHelper.loadIcon("nb.png", 32).getImage();

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

            ActionListener exitListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("Exiting...");
                    System.exit(0);
                }
            };

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
        configService.load();
        compact = !configService.isSumoMode();
        this.frame = primaryStage;
        primaryStage.setTitle("Netbeans Launcher " + NbUtils.getArtifactVersionOrDev());
        primaryStage.setIconImage(new ImageIcon(Grid.class.getResource("nb.png")).getImage());
        primaryStage.setResizable(false);
        JPanel basePanel = new JPanel(new BorderLayout());
        basePanel.add(createHeader(), BorderLayout.NORTH);
        JPanel container = new JPanel(new BorderLayout());
        basePanel.add(container, BorderLayout.CENTER);
        appPaneContainer = new SlideAppPaneContainer(container);

        workspacePane = new WorkspacePane(this);
        workspaceListPane = new NbListPane(this);
        settingsPane = new SettingsPane(this);

        addPane(workspacePane);
        addPane(workspaceListPane);
        addPane(settingsPane);

        container.add(appPaneContainer.toPanel());
        primaryStage.getContentPane().add(basePanel);
        for (AppPane pane : getPanes()) {
            pane.onInit();
        }
        setSelectedPane(PaneType.LIST_WS);
        onChangeCompatStatus(compact);
        frame.setUndecorated(true);
        primaryStage.setLocationRelativeTo(null);
        primaryStage.setVisible(true);
    }

    private JComponent createHeader() {
        int vgap = 1;//compact ? 1 : 13;
        int hgap = 1;//compact ? 1 : 5;
        MainWindowSwingHelper.BoxH header = MainWindowSwingHelper.boxH().setVgap(vgap).setHgap(hgap);
        header.setBackground("336699");
//        header.addAll(new JLabel("Netbeans Launcher"));
        JLabel icon = new JLabel(MainWindowSwingHelper.loadIcon("nb.png", 32));
        header.add(icon);
        JLabel logo = new JLabel(MainWindowSwingHelper.loadIcon("logo.png", 63, 28));
        header.add(logo);
        header.addGlueH();

        minimizeButton = getToolkit().createIconButton("minimize-window", "App.Action.MinimizeWindow", () -> frame.setState(Frame.ICONIFIED), compact);        header.add(minimizeButton);
        enlargeButton = getToolkit().createIconButton("enlarge", "App.Action.NonCompactMode", () -> setCompact(false), compact);
        header.add(enlargeButton);
        compressButton = getToolkit().createIconButton("compress", "App.Action.CompactMode", () -> setCompact(true), compact);
        header.add(compressButton);
        exitButton = getToolkit().createIconButton("exit", "App.Action.Exit", () -> confirmAndExit(), compact);
        header.add(exitButton);
        setCompact(compact);

//        Font verdana = new Font("Verdana", Font.BOLD, 12);
//        Font courrier = new Font(Font.MONOSPACED, Font.BOLD, 12);
//        icon.setOpaque(false);
//        JLabel nbLabel=new JLabel("Netbeans");
//        nbLabel.setOpaque(false);
//        nbLabel.setForeground(Color.white);
//        JLabel launcherLabel=new JLabel("Launcher");
//        launcherLabel.setForeground(Color.white);
//        launcherLabel.setOpaque(false);
//        launcherLabel.setFont(courrier .deriveFont(Font.BOLD,20));
//        GridBagLayout2 g=new GridBagLayout2();
//        g.addLine("[_A>][C*]");
//        g.addLine("[^B>]");
//        JPanel pp=new JPanel(g);
//        pp.setOpaque(false);
//        pp.add(nbLabel,g.getConstraints("A"));
//        pp.add(launcherLabel,g.getConstraints("B"));
//        pp.add(icon,g.getConstraints("C"));
//        header.add(pp);
        JComponent c = header.toComponent();
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
        setSelectedPane(PaneType.EDIT_WS);
        workspacePane.onAddWorkspace(w);
    }

    public void onEditWorkspace(NetbeansWorkspace w) {
        try {
            if (w != null) {
                setSelectedPane(PaneType.EDIT_WS);
                workspacePane.onEditWorkspace(w);
            }
        } catch (Exception ex) {
            toolkit.showError(toolkit.msg("App.EditWorkspace.Error"), ex);
        }

    }

    public void setSelectedPane(PaneType type) {
        AppPane good = null;
        for (AppPane appPane : appPaneContainer.getAppPanes()) {
            if (appPane.getPaneType() == type) {
                good = appPane;
            }
        }
        if (good != null) {
            good.onSelectedPane();
            appPaneContainer.setAppPane(good);
        }
//        frame.pack();
    }

    public void onRefreshHeader() {
        for (AppPane pane : getPanes()) {
            pane.onRefreshHeader();
        }
    }

    public void startWorkspace(NetbeansWorkspace w) {
        if (w != null) {
            String name = w.getName();
            synchronized (running) {
                if (running.contains(name)) {
                    return;
                }
            }
            synchronized (running) {
                running.add(name);
            }
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
                            configService.run(w);
                        } catch (Exception ex) {
                            toolkit.showError(toolkit.msg("App.RunWorkspace.Error"), ex);
                        } finally {
                            synchronized (running) {
                                running.remove(name);
                            }
                            onRefreshHeader();
                        }
                    }

                }.start();
            } catch (Exception ex) {
                toolkit.showError(toolkit.msg("App.RunWorkspace.Error"), ex);
            } finally {
                onRefreshHeader();
            }
        }
    }

    public boolean isRunningWorkspace(String n) {
        synchronized (running) {
            return running.contains(n);
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
        compressButton.setVisible(!compact);
        if(appPaneContainer!=null) {
            ((SlideAppPaneContainer) appPaneContainer).setSlideTime(
                    compact ? 50 : 20
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
        Dimension dimension = compact ? new Dimension(350, 300) : new Dimension(700, 500);
        frame.setPreferredSize(dimension);
        frame.setSize(dimension);
    }

    public void confirmAndExit() {
        if (toolkit.showConfirm(
                toolkit.msg("App.Exit.Confirm.Title"),
                toolkit.msg("App.Exit.Confirm.Message")
        )) {
            System.exit(0);
        }
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
            switch (pane.getPaneType()) {
                case SETTINGS: {
                    root.slide(SimplePanelSlider.Slide.RIGHT, pane);
                    break;
                }
                case LIST_WS: {
                    root.slide(SimplePanelSlider.Slide.LEFT, pane);
                    break;
                }
                case EDIT_WS: {
                    root.slide(SimplePanelSlider.Slide.TOP, pane);
                    break;
                }
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
}
