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
import net.thevpc.nuts.NPlatformLocation;
import net.thevpc.nuts.NPlatforms;
import net.thevpc.nuts.env.NPlatformFamily;
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

    public NPlatformLocation detectJdk(String path) {
        return detectJdk(toPath(path));
    }

    public NPlatformLocation detectJdk(NPath path) {
        return NPlatforms.of().resolvePlatform(NPlatformFamily.JAVA, path, null)
                .filter(x -> "jdk".equalsIgnoreCase(x.getPackaging()))
                .orNull();
    }

    public List<NPlatformLocation> configureJdks(NPath[] baseFolders, boolean autoAdd) {
        ArrayList<NPlatformLocation> all = new ArrayList<>();
        for (NPath baseFolder : baseFolders) {
            if (baseFolder.isDirectory()) {
                for (NPath file : baseFolder.list().stream().filter(x -> x.isDirectory()).collect(Collectors.toList())) {
                    NPlatformLocation o = findJdk(file);
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

    public NPlatformLocation findJdk(NPath path) {
        if (path == null) {
            return null;
        }
        for (NPlatformLocation loc : module.conf().getJdkLocations()) {
            if (NbUtils.equalsStr(path.toString(), toPath(loc.getPath()).toString())) {
                return loc;
            }
        }
        for (NPlatformLocation loc : module.conf().getJdkLocations()) {
            if (NbUtils.equalsStr(path.toString(), toPath(loc.getName()).toString())) {
                return loc;
            }
        }
        for (NPlatformLocation loc : module.conf().getJdkLocations()) {
            if (NbUtils.equalsStr(path.toString(), toPath(loc.getVersion()).toString())) {
                return loc;
            }
        }
        return null;
    }

    public NPlatformLocation findOrAddJdk(String path) {
        if (path == null) {
            return null;
        }
        NPlatformLocation o = findJdk(toPath(path));
        if (o == null) {
            o = detectJdk(toPath(path));
            if (o != null) {
                addJdk(o);
            }
        }
        return o;
    }

    public boolean addJdk(NPlatformLocation netbeansInstallation) {
        for (NPlatformLocation installation : module.conf().getJdkLocations()) {
            if (NbUtils.equalsStr(netbeansInstallation.getPath(), installation.getPath())) {
                return false;
            }
        }
        module.conf().getJdkLocations().add(netbeansInstallation);
        module.conf().saveConfig();
        return true;
    }

    public NPlatformLocation[] findAllJdks() {
        List<NPlatformLocation> list = module.conf().getJdkLocations().list();
        list.sort((a, b) -> {
            int i = NbUtils.compareVersions(a.getVersion(), b.getVersion());
            if (i != 0) {
                return i;
            }
            return a.getName().compareTo(b.getName());
        });
        return list.toArray(new NPlatformLocation[0]);
    }

    public void addDefaultJdks() {
        List<NPlatformLocation> all =
                configureJdks(
                        Arrays.stream(NbUtils.getNbOsConfig(module.session()).getJdkFolders()).map(x -> toPath(x)).toArray(NPath[]::new)
                        , false);
        Map<String, List<NPlatformLocation>> mapped = all.stream().collect(
                Collectors.groupingBy(x -> {
                    File file = new File(x.getPath());
                    try {
                        return file.getCanonicalPath();
                    } catch (IOException e) {
                        return file.getAbsolutePath();
                    }
                })
        );
        for (Map.Entry<String, List<NPlatformLocation>> e : mapped.entrySet()) {
            List<NPlatformLocation> li = e.getValue();
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
        List<NPlatformLocation> res = new ArrayList<>();
        for (Map.Entry<String, List<NPlatformLocation>> e : mapped.entrySet()) {
            res.addAll(e.getValue());
        }
        for (NPlatformLocation r : res) {
            addJdk(r);
        }
    }

    static interface ToRemove {
        boolean accept(String toRemoveName, String baseName);
    }

    private void removeOthersIfFound(List<NPlatformLocation> li, Predicate<String> name, ToRemove toRemove) {
        NPlatformLocation javaOpenJdk = li.stream().filter(x -> name.test(new File(x.getPath()).getName())).findFirst().orElse(null);
        if (javaOpenJdk != null) {
            li.removeIf(x -> toRemove.accept(new File(x.getPath()).getName(), new File(javaOpenJdk.getPath()).getName()));
        }
    }

    private void removeOtherIfFound(List<NPlatformLocation> li, Predicate<String> name) {
        if (
                li.stream().anyMatch(x -> name.test(new File(x.getPath()).getName()))
                        && li.stream().anyMatch(x -> !name.test(new File(x.getPath()).getName()))
        ) {
            li.removeIf(x -> !name.test(new File(x.getPath()).getName()));
        }
    }

    private void removeThisIfFound(List<NPlatformLocation> li, Predicate<String> name) {
        if (
                li.stream().anyMatch(x -> name.test(new File(x.getPath()).getName()))
                        && li.stream().anyMatch(x -> !name.test(new File(x.getPath()).getName()))
        ) {
            li.removeIf(x -> name.test(new File(x.getPath()).getName()));
        }
    }

    public void removeJdk(String path) {
        NPlatformLocation o = findJdk(toPath(path));
        if (o != null) {
            module.ws().removeNetbeansWorkspacesByJdkPath(o.getPath());
            module.conf().getJdkLocations().remove(o);
            module.conf().saveConfig();
        }
    }
}
