/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.netbeans.launcher.service;

import net.thevpc.nuts.*;
import java.util.logging.Logger;

/**
 * @author thevpc
 */
public class NetbeansLauncherModule {

    private static final Logger LOG = Logger.getLogger(NetbeansLauncherModule.class.getName());
    private final NSession session;

    private NetbeansWorkspaceService netbeansWorkspaceService;
    private JdkService jdKService;
    private NetbeansInstallationService installationService;
    private NetbeansConfigService configService;
    private NetbeansProcessService processService;
    private NetbeansRuntimeService runtimeService;

    public NetbeansLauncherModule(NSession appContext) {
        this.session = appContext;
        this.configService = new NetbeansConfigService(this);
        this.netbeansWorkspaceService = new NetbeansWorkspaceService(this);
        this.jdKService = new JdkService(this);
        this.processService = new NetbeansProcessService(this);
        this.runtimeService = new NetbeansRuntimeService(this);
        this.installationService = new NetbeansInstallationService(this);
    }

    public NSession session() {
        return session;
    }

    public NetbeansProcessService ps() {
        return processService;
    }

    public NetbeansWorkspaceService ws() {
        return netbeansWorkspaceService;
    }

    public NetbeansConfigService conf() {
        return configService;
    }

    public JdkService jdk() {
        return jdKService;
    }

    public NetbeansInstallationService ins() {
        return installationService;
    }

    public NetbeansRuntimeService rt() {
        return runtimeService;
    }

    public void configureDefaults() {
        jdk().addDefaultJdks();
        ins().configureDefaultInstallations();
        ws().configureDefaultNetbeansWorkspaces();
    }

}
