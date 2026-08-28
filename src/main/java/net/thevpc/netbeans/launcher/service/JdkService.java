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
import net.thevpc.nuts.platform.NRuntimeDistributionFamily;
import net.thevpc.nuts.platform.NRuntimeDistributionManager;
import net.thevpc.nuts.platform.NRuntimeDistribution;
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

    public NRuntimeDistribution detectJdk(String path) {
        return detectJdk(toPath(path));
    }

    public NRuntimeDistribution detectJdk(NPath path) {
        return NRuntimeDistributionManager.of().resolveRuntimeDistribution(NRuntimeDistributionFamily.JAVA, path, null)
                .filter(x -> NRuntimeDistribution.JAVA_PRODUCT_JDK.equalsIgnoreCase(x.product()))
                .orNull();
    }

    public List<NRuntimeDistribution> configureJdks(NPath[] baseFolders, boolean autoAdd) {
        ArrayList<NRuntimeDistribution> all = new ArrayList<>();
        for (NPath baseFolder : baseFolders) {
            if (baseFolder.isDirectory()) {
                for (NPath file : baseFolder.list().stream().filter(x -> x.isDirectory()).collect(Collectors.toList())) {
                    NRuntimeDistribution o = findJdk(file);
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

    public NRuntimeDistribution findJdk(NPath path) {
        if (path == null) {
            return null;
        }
        for (NRuntimeDistribution loc : module.conf().getJdkLocations()) {
            if (NbUtils.equalsStr(path.toString(), toPath(loc.path()).toString())) {
                return loc;
            }
        }
        for (NRuntimeDistribution loc : module.conf().getJdkLocations()) {
            if (NbUtils.equalsStr(path.toString(), toPath(loc.name()).toString())) {
                return loc;
            }
        }
        for (NRuntimeDistribution loc : module.conf().getJdkLocations()) {
            if (NbUtils.equalsStr(path.toString(), toPath(loc.version()).toString())) {
                return loc;
            }
        }
        return null;
    }

    public NRuntimeDistribution findOrAddJdk(String path) {
        if (path == null) {
            return null;
        }
        NRuntimeDistribution o = findJdk(toPath(path));
        if (o == null) {
            o = detectJdk(toPath(path));
            if (o != null) {
                addJdk(o);
            }
        }
        return o;
    }

    public boolean addJdk(NRuntimeDistribution netbeansInstallation) {
        for (NRuntimeDistribution installation : module.conf().getJdkLocations()) {
            if (NbUtils.equalsStr(netbeansInstallation.path(), installation.path())) {
                return false;
            }
        }
        module.conf().getJdkLocations().add(netbeansInstallation);
        module.conf().saveConfig();
        return true;
    }

    public NRuntimeDistribution[] findAllJdks() {
        List<NRuntimeDistribution> list = module.conf().getJdkLocations().list();
        list.sort((a, b) -> {
            int i = NbUtils.compareVersions(a.version(), b.version());
            if (i != 0) {
                return i;
            }
            return a.name().compareTo(b.name());
        });
        return list.toArray(new NRuntimeDistribution[0]);
    }

    public void addDefaultJdks() {
        List<NRuntimeDistribution> all =
                configureJdks(
                        Arrays.stream(NbUtils.getNbOsConfig().getJdkFolders()).map(x -> toPath(x)).toArray(NPath[]::new)
                        , false);
        Map<String, List<NRuntimeDistribution>> mapped = all.stream().collect(
                Collectors.groupingBy(x -> {
                    File file = new File(x.path());
                    try {
                        return file.getCanonicalPath();
                    } catch (IOException e) {
                        return file.getAbsolutePath();
                    }
                })
        );
        for (Map.Entry<String, List<NRuntimeDistribution>> e : mapped.entrySet()) {
            List<NRuntimeDistribution> li = e.getValue();
            if (li.size() > 1) {
                //remove if have link pointed to it!
                li.removeIf(x -> x.path().equals(e.getKey()));
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
        List<NRuntimeDistribution> res = new ArrayList<>();
        for (Map.Entry<String, List<NRuntimeDistribution>> e : mapped.entrySet()) {
            res.addAll(e.getValue());
        }
        for (NRuntimeDistribution r : res) {
            addJdk(r);
        }
    }

    static interface ToRemove {
        boolean accept(String toRemoveName, String baseName);
    }

    private void removeOthersIfFound(List<NRuntimeDistribution> li, Predicate<String> name, ToRemove toRemove) {
        NRuntimeDistribution javaOpenJdk = li.stream().filter(x -> name.test(new File(x.path()).getName())).findFirst().orElse(null);
        if (javaOpenJdk != null) {
            li.removeIf(x -> toRemove.accept(new File(x.path()).getName(), new File(javaOpenJdk.path()).getName()));
        }
    }

    private void removeOtherIfFound(List<NRuntimeDistribution> li, Predicate<String> name) {
        if (
                li.stream().anyMatch(x -> name.test(new File(x.path()).getName()))
                        && li.stream().anyMatch(x -> !name.test(new File(x.path()).getName()))
        ) {
            li.removeIf(x -> !name.test(new File(x.path()).getName()));
        }
    }

    private void removeThisIfFound(List<NRuntimeDistribution> li, Predicate<String> name) {
        if (
                li.stream().anyMatch(x -> name.test(new File(x.path()).getName()))
                        && li.stream().anyMatch(x -> !name.test(new File(x.path()).getName()))
        ) {
            li.removeIf(x -> name.test(new File(x.path()).getName()));
        }
    }

    public void removeJdk(String path) {
        NRuntimeDistribution o = findJdk(toPath(path));
        if (o != null) {
            module.ws().removeNetbeansWorkspacesByJdkPath(o.path());
            module.conf().getJdkLocations().remove(o);
            module.conf().saveConfig();
        }
    }
}
