/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.netbeans.launcher.model;

import net.thevpc.nuts.NutsPlatformLocation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thevpc
 */
public class NetbeansConfig implements Serializable {

    public static final long serialVersionUID = 1;
    @SuppressWarnings("FieldMayBeFinal")
    private List<NetbeansInstallation> installations = new ArrayList<>();
    @SuppressWarnings("FieldMayBeFinal")
    private List<NetbeansWorkspace> workspaces = new ArrayList<>();
    @SuppressWarnings("FieldMayBeFinal")
    private List<NutsPlatformLocation> jdkLocations = new ArrayList<>();
    private boolean sumoMode = false;

    public List<NetbeansInstallation> getInstallations() {
        return installations;
    }

    public List<NetbeansWorkspace> getWorkspaces() {
        return workspaces;
    }

    public List<NutsPlatformLocation> getJdkLocations() {
        return jdkLocations;
    }

    public void setInstallations(List<NetbeansInstallation> installations) {
        this.installations = installations;
    }

    public void setWorkspaces(List<NetbeansWorkspace> workspaces) {
        this.workspaces = workspaces;
    }

    public void setJdkLocations(List<NutsPlatformLocation> jdkLocations) {
        this.jdkLocations = jdkLocations;
    }

    public boolean isSumoMode() {
        return sumoMode;
    }

    public void setSumoMode(boolean sumoMode) {
        this.sumoMode = sumoMode;
    }

}
