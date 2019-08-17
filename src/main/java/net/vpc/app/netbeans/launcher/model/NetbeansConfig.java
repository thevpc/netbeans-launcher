/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.netbeans.launcher.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author vpc
 */
public class NetbeansConfig implements Serializable {

    public static final long serialVersionUID = 1;
    private final List<NetbeansInstallation> installations = new ArrayList<>();
    private final List<NetbeansWorkspace> workspaces = new ArrayList<>();
    private final List<JdkInstallation> jdkLocations = new ArrayList<>();
    private boolean sumoMode = false;

    public List<NetbeansInstallation> getInstallations() {
        return installations;
    }

    public List<NetbeansWorkspace> getWorkspaces() {
        return workspaces;
    }

    public List<JdkInstallation> getJdkLocations() {
        return jdkLocations;
    }

    public boolean isSumoMode() {
        return sumoMode;
    }

    public void setSumoMode(boolean sumoMode) {
        this.sumoMode = sumoMode;
    }

}
