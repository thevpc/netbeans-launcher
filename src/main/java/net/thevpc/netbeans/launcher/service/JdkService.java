/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.netbeans.launcher.service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.thevpc.netbeans.launcher.util.NbUtils;
import net.thevpc.nuts.platform.NExecutionEngineFamily;
import net.thevpc.nuts.platform.NExecutionEngines;
import net.thevpc.nuts.platform.NExecutionEngineLocation;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NBlankable;

/**
 * @author vpc
 */
public class JdkService {

    private final NetbeansLauncherModule module;

    public JdkService(NetbeansLauncherModule module) {
        this.module = module;
    }

    public NPath toPath(String path) {
        if (NBlankable.isBlank(path)) {
            return null;
        }
        if (path.equals("~")) {
            return NPath.ofUserHome();
        }
        if (path.startsWith("~/") || path.startsWith("~\\")) {
            return NPath.ofUserHome().resolve(path.substring(2));
        }
        return NPath.of(path);
    }

    public NExecutionEngineLocation detectJdk(String path) {
        return detectJdk(toPath(path));
    }

    public NExecutionEngineLocation detectJdk(NPath path) {
        return NExecutionEngines.of().resolveExecutionEngine(NExecutionEngineFamily.JAVA, path, null)
                .filter(x -> NExecutionEngineLocation.JAVA_PRODUCT_JDK.equalsIgnoreCase(x.getProduct()))
                .orNull();
    }

    public List<NExecutionEngineLocation> configureJdks(NPath[] baseFolders, boolean autoAdd) {
        ArrayList<NExecutionEngineLocation> all = new ArrayList<>();
        for (NPath baseFolder : baseFolders) {
            if (baseFolder.isDirectory()) {
                for (NPath file : baseFolder.list().stream().filter(x -> x.isDirectory()).collect(Collectors.toList())) {
                    NExecutionEngineLocation o = findJdk(file);
                    if (o == null) {
                        o = detectJdk(file);
                        if (o != null) {
                            all.add(o);
                            if (autoAdd) {
                                addJdk(o);
                            }
                        }
                    }
                }
            }
        }
        return all;
    }

    public NExecutionEngineLocation findJdk(NPath path) {
        if (path == null) {
            return null;
        }
        for (NExecutionEngineLocation loc : module.conf().getJdkLocations()) {
            if (NbUtils.equalsStr(path.toString(), toPath(loc.getPath()).toString())) {
                return loc;
            }
        }
        for (NExecutionEngineLocation loc : module.conf().getJdkLocations()) {
            if (NbUtils.equalsStr(path.toString(), toPath(loc.getName()).toString())) {
                return loc;
            }
        }
        for (NExecutionEngineLocation loc : module.conf().getJdkLocations()) {
            if (NbUtils.equalsStr(path.toString(), toPath(loc.getVersion()).toString())) {
                return loc;
            }
        }
        return null;
    }

    public NExecutionEngineLocation findOrAddJdk(String path) {
        if (path == null) {
            return null;
        }
        NExecutionEngineLocation o = findJdk(toPath(path));
        if (o == null) {
            o = detectJdk(toPath(path));
            if (o != null) {
                addJdk(o);
            }
        }
        return o;
    }

    public boolean addJdk(NExecutionEngineLocation netbeansInstallation) {
        for (NExecutionEngineLocation installation : module.conf().getJdkLocations()) {
            if (NbUtils.equalsStr(netbeansInstallation.getPath(), installation.getPath())) {
                return false;
            }
        }
        module.conf().getJdkLocations().add(netbeansInstallation);
        module.conf().saveConfig();
        return true;
    }

    public NExecutionEngineLocation[] findAllJdks() {
        List<NExecutionEngineLocation> list = module.conf().getJdkLocations().list();
        list.sort((a, b) -> {
            int i = NbUtils.compareVersions(a.getVersion(), b.getVersion());
            if (i != 0) {
                return i;
            }
            return a.getName().compareTo(b.getName());
        });
        return list.toArray(new NExecutionEngineLocation[0]);
    }

    public void addDefaultJdks() {
        List<NExecutionEngineLocation> all =
                configureJdks(
                        Arrays.stream(NbUtils.getNbOsConfig().getJdkFolders()).map(x -> toPath(x)).toArray(NPath[]::new)
                        , false);
        Map<String, List<NExecutionEngineLocation>> mapped = all.stream().collect(
                Collectors.groupingBy(x -> {
                    File file = new File(x.getPath());
                    try {
                        return file.getCanonicalPath();
                    } catch (IOException e) {
                        return file.getAbsolutePath();
                    }
                })
        );
        for (Map.Entry<String, List<NExecutionEngineLocation>> e : mapped.entrySet()) {
            List<NExecutionEngineLocation> li = e.getValue();
            if (li.size() > 1) {
                //remove if have link pointed to it!
                li.removeIf(x -> x.getPath().equals(e.getKey()));
                removeThisIfFound(li, n -> n.startsWith("jre-"));
                for (String provider : new String[]{"openjdk", "openj9"}) {
                    removeOthersIfFound(li, n -> n.matches("^java-[0-9]+([.][0-9]+)*-" + provider + "$"),
                            (n,b) -> {
                                try {
                                    return n.equals(b.substring(0, b.length() - ("-" + provider).length()))
                                            || n.equals("java-" + provider)
                                            || n.startsWith("java-" + provider + "-");
                                } catch (Exception ex) {
                                    return false;
                                }
                            }
                    );
                }
                removeOtherIfFound(li, n -> n.equals("latest"));
                removeOtherIfFound(li, n -> n.equals("default"));
            }
        }
        List<NExecutionEngineLocation> res = new ArrayList<>();
        for (Map.Entry<String, List<NExecutionEngineLocation>> e : mapped.entrySet()) {
            res.addAll(e.getValue());
        }
        for (NExecutionEngineLocation r : res) {
            addJdk(r);
        }
    }

    static interface ToRemove {
        boolean accept(String toRemoveName, String baseName);
    }

    private void removeOthersIfFound(List<NExecutionEngineLocation> li, Predicate<String> name, ToRemove toRemove) {
        NExecutionEngineLocation javaOpenJdk = li.stream().filter(x -> name.test(new File(x.getPath()).getName())).findFirst().orElse(null);
        if (javaOpenJdk != null) {
            li.removeIf(x -> toRemove.accept(new File(x.getPath()).getName(), new File(javaOpenJdk.getPath()).getName()));
        }
    }

    private void removeOtherIfFound(List<NExecutionEngineLocation> li, Predicate<String> name) {
        if (
                li.stream().anyMatch(x -> name.test(new File(x.getPath()).getName()))
                        && li.stream().anyMatch(x -> !name.test(new File(x.getPath()).getName()))
        ) {
            li.removeIf(x -> !name.test(new File(x.getPath()).getName()));
        }
    }

    private void removeThisIfFound(List<NExecutionEngineLocation> li, Predicate<String> name) {
        if (
                li.stream().anyMatch(x -> name.test(new File(x.getPath()).getName()))
                        && li.stream().anyMatch(x -> !name.test(new File(x.getPath()).getName()))
        ) {
            li.removeIf(x -> name.test(new File(x.getPath()).getName()));
        }
    }

    public void removeJdk(String path) {
        NExecutionEngineLocation o = findJdk(toPath(path));
        if (o != null) {
            module.ws().removeNetbeansWorkspacesByJdkPath(o.getPath());
            module.conf().getJdkLocations().remove(o);
            module.conf().saveConfig();
        }
    }
}
