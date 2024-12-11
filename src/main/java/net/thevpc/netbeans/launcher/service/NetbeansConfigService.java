/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.netbeans.launcher.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import net.thevpc.netbeans.launcher.model.ConfigListener;
import net.thevpc.netbeans.launcher.model.NetbeansConfig;
import net.thevpc.netbeans.launcher.model.NetbeansInstallation;
import net.thevpc.netbeans.launcher.model.NetbeansInstallationStore;
import net.thevpc.netbeans.launcher.model.NetbeansWorkspace;
import net.thevpc.netbeans.launcher.model.ObservableNetbeansConfig;
import net.thevpc.netbeans.launcher.util.ObservableList;
import net.thevpc.netbeans.launcher.util.ObservableValue;
import net.thevpc.nuts.*;
import net.thevpc.nuts.concurrent.NScheduler;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NPath;

/**
 *
 * @author vpc
 */
public class NetbeansConfigService {

    private List<ConfigListener> configListeners = new ArrayList<>();
    private ObservableNetbeansConfig config = new ObservableNetbeansConfig();
    private boolean configLoaded;

    private final NetbeansLauncherModule module;

    public NetbeansConfigService(NetbeansLauncherModule module) {
        this.module = module;
    }

    public ObservableList<NetbeansInstallation> getInstallations() {
        return config.getInstallations();
    }

    public ObservableList<NetbeansWorkspace> getWorkspaces() {
        return config.getWorkspaces();
    }

    public ObservableList<NPlatformLocation> getJdkLocations() {
        return config.getJdkLocations();
    }

    public ObservableValue<Boolean> getSumoMode() {
        return config.getSumoMode();
    }

    public ObservableValue<Integer> getZoom() {
        return config.getZoom();
    }

    public ObservableNetbeansConfig config() {
        return config;
    }

    public void addOnceConfigListener(ConfigListener conf) {
        addConfigListener(new OnceConfigListener(conf));
    }

    public void addConfigListener(ConfigListener conf) {
        configListeners.add(conf);
    }

    public void removeConfigListener(ConfigListener conf) {
        configListeners.remove(conf);
    }

    public synchronized void saveConfig() {
        NetbeansConfig c = config.getNetbeansConfig();
        NElements.of().json()
                .setValue(c).setNtf(false)
                .print(NApp.of().getConfFolder().resolve("config.json"));
    }

    public <T> void loadFile(ConfigListener onFinish) {
        NetbeansConfig config = null;
        boolean loaded = false;
        NPath validFile = NApp.of().getConfFolder().resolve("config.json");
        boolean foundCurrVersionFile = false;
        if (validFile.isRegularFile()) {
            try {
                config = (NetbeansConfig) NElements.of().json().parse(validFile, NetbeansConfig.class);
                foundCurrVersionFile = config != null;
            } catch (Exception e) {
                System.err.println("Unable to load config from " + validFile.toString());
                int i = 2;
                while (true) {
                    NPath f2 = validFile.resolveSibling(validFile.getName() + "." + i + ".save");
                    if (!f2.exists()) {
                        validFile.moveTo(f2);
                        break;
                    }
                    i++;
                }
                config = new NetbeansConfig();
            }
            if (config == null) {
                config = new NetbeansConfig();
            }
            boolean needSave = false;
            for (NetbeansInstallation installation : config.getInstallations()) {
                if (installation.getStore() == null) {
                    installation.setStore(NetbeansInstallationStore.USER);
                    needSave = true;
                }
            }
            if (needSave) {
                saveConfig();
            }
            loaded = true;
        }
        if (!foundCurrVersionFile) {
            List<NId> olderVersions = NSearchCmd.of().setInstallStatus(
                    NInstallStatusFilters.of().byInstalled(true)
            ).addId(NApp.of().getId().get().builder().setVersion("").build()).getResultIds().stream().sorted(
                    (a, b) -> b.getVersion().compareTo(a.getVersion())
            ).filter(x -> x.getVersion().compareTo(NApp.of().getVersion().get()) < 0).collect(Collectors.toList());
            for (NId olderVersion : olderVersions) {
                NPath validFile2
                        = NWorkspace.of().getStoreLocation(olderVersion, NStoreType.CONF)
                                .resolve("config.json");
                if (validFile2.isRegularFile()) {
                    try {
                        config = (NetbeansConfig) NElements.of().json().parse(validFile2, NetbeansConfig.class);
                    } catch (Exception e) {
                        System.err.println("Unable to load config from " + validFile2.toString());
                        break;
                    }
                    if (config != null) {
                        saveConfig();
                        loaded = true;
                        break;
                    }
                }
            }
        }
        if (config == null) {
            config = new NetbeansConfig();
        }
        this.config.setNetbeansConfig(config);
        saveConfig();
        new Thread(() -> {
            prepareConfig();
        }).start();
        this.configLoaded = true;
        for (ConfigListener configListener : configListeners.toArray(new ConfigListener[0])) {
            configListener.onConfigLoaded();
        }
        if (onFinish != null) {
            onFinish.onConfigLoaded();
        }
    }

    public void prepareConfig() {
        boolean someUpdates = false;
        if (config.getJdkLocations().isEmpty()) {
            module.jdk().addDefaultJdks();
            someUpdates = true;
        }
        if (config.getInstallations().isEmpty()) {
            module.ins().configureDefaultInstallations();
            someUpdates = true;
        }
        if (config.getWorkspaces().isEmpty()) {
            module.ws().configureDefaultNetbeansWorkspaces();
            someUpdates = true;
        }
        if (someUpdates) {
            saveConfig();
        }
    }

    public void loadAsync(ConfigListener onFinish) {
        NScheduler.of()
                .executorService().submit(() -> this.load(onFinish));
    }

    public void load(ConfigListener onFinish) {
        loadFile(onFinish);
    }

    public boolean setSumoMode(boolean b) {
        config.getSumoMode().set(b);
        saveConfig();
        return true;
    }

    public boolean isSumoMode() {
        return config.getSumoMode().get();
    }

    public boolean setZoom(int b) {
        config.getZoom().set(b);
        saveConfig();
        return true;
    }


    public void waitForConfigLoaded() {
        if (configLoaded) {
            return;
        }
        CountDownLatch countDownLatch = new CountDownLatch(1);
        addOnceConfigListener(new ConfigListener() {
            @Override
            public void onConfigLoaded() {
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /////////////
    private class OnceConfigListener implements ConfigListener {

        ConfigListener c;

        public OnceConfigListener(ConfigListener c) {
            this.c = c;
        }

        @Override
        public void onConfigLoaded() {
            c.onConfigLoaded();
            NetbeansConfigService.this.removeConfigListener(this);
        }
    }

}
