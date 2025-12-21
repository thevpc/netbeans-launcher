package net.thevpc.netbeans.launcher.model;

import net.thevpc.netbeans.launcher.util.ObservableList;
import net.thevpc.netbeans.launcher.util.ObservableValue;
import net.thevpc.nuts.platform.NExecutionEngineLocation;

public class ObservableNetbeansConfig {
    private final ObservableList<NetbeansInstallation> installations = new ObservableList<>();
    private final ObservableList<NetbeansWorkspace> workspaces = new ObservableList<>();
    private final ObservableList<NExecutionEngineLocation> jdkLocations = new ObservableList<>();
    private final ObservableValue<Boolean> sumoMode = new ObservableValue<>(false);
    private final ObservableValue<Integer> zoom = new ObservableValue<>(0);

    public void setNetbeansConfig(NetbeansConfig c){
        if(c==null){
            installations.clear();
            workspaces.clear();
            jdkLocations.clear();
            sumoMode.set(false);
            zoom.set(0);
        }else {
            installations.setAll(c.getInstallations());
            workspaces.setAll(c.getWorkspaces());
            jdkLocations.setAll(c.getJdkLocations());
            sumoMode.set(c.isSumoMode());
            zoom.set(c.getZoom());
        }
    }

    public NetbeansConfig getNetbeansConfig(){
        NetbeansConfig c=new NetbeansConfig();
        c.getInstallations().addAll(installations.list());
        c.getWorkspaces().addAll(workspaces.list());
        c.getJdkLocations().addAll(jdkLocations.list());
        c.setSumoMode(sumoMode.get());
        c.setZoom(zoom.get());
        return c;
    }

    public ObservableList<NetbeansInstallation> getInstallations() {
        return installations;
    }

    public ObservableList<NetbeansWorkspace> getWorkspaces() {
        return workspaces;
    }

    public ObservableList<NExecutionEngineLocation> getJdkLocations() {
        return jdkLocations;
    }

    public ObservableValue<Boolean> getSumoMode() {
        return sumoMode;
    }

    public ObservableValue<Integer> getZoom() {
        return zoom;
    }
}
