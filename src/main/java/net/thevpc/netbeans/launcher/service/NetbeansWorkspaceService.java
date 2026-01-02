/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.netbeans.launcher.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.thevpc.netbeans.launcher.model.*;
import net.thevpc.netbeans.launcher.util.NbStringUtils;
import net.thevpc.netbeans.launcher.util.NbUtils;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;

/**
 * @author vpc
 */
public class NetbeansWorkspaceService {

    private static final Logger LOG = Logger.getLogger(NetbeansWorkspaceService.class.getName());
    public static final NetbeansGroup NETBEANS_NO_GROUP = new NetbeansGroup("--no-group", "--no-group");
    public static final NetbeansGroup NETBEANS_CLOSE_GROUP = new NetbeansGroup("--close-group", "--close-group");

    private static String[] prefix = {"Workspace", "WS", "NB", "Netbeans"};
    private static String[] suffix = {"-Perso", "-Work", "-Research", "-Edu", "-Fun", "-Test", "-Release", "-Test 1", "-Test 2", "-A", "-B", "-C", "-D", "-E"};

    private final NetbeansLauncherModule module;

    public NetbeansWorkspaceService(NetbeansLauncherModule module) {
        this.module = module;
    }

    private NetbeansWorkspace detectNetbeansWorkspace(File userdir, File cachedir) {
        if (!userdir.isDirectory()) {
            return null;
        }
        if (!new File(userdir, "config").isDirectory()) {
        }
        File[] cc = new File(userdir, "config").listFiles(f -> f.isDirectory() && f.getName().startsWith("org-netbeans"));
        if (cc == null || cc.length == 0) {
            return null;
        }
        String preferredName = null;
        NetbeansInstallation installation = null;
        if (new File(".lastUsedVersion").exists()) {
            String lastUsedVersion = null;
            try {
                lastUsedVersion = new String(Files.readAllBytes(new File(".lastUsedVersion").toPath())).trim();
            } catch (Exception ex) {
                //ignore
            }
            if (lastUsedVersion != null && !lastUsedVersion.isEmpty()) {
                installation = findBestNetbeansInstallationOrLatest(lastUsedVersion).orNull();
                if (installation == null) {
                    return null;
                }
                preferredName = userdir.getName();
            }
        }
        if (preferredName == null) {
            String vv = NbStringUtils.extract(userdir.getName().toUpperCase(), "NETBEANS (IDE )?(?<n>[0-9]+([.][0-9]+)?)", "n");
            if (vv != null) {
                installation = findBestNetbeansInstallationOrLatest(vv).orNull();
                if (installation == null) {
                    return null;
                }
                preferredName = userdir.getName();
            }
        }
        if (preferredName == null) {
            if (userdir.getName().matches("[0-9]+([.][0-9]+)?")) {
                installation = findBestNetbeansInstallationOrLatest(userdir.getName()).orNull();
                if (installation == null) {
                    return null;
                }
                preferredName = "Netbeans " + userdir.getName();
            }
        }
        if (preferredName == null) {
            installation = findBestNetbeansInstallationOrLatest(null).orNull();
            if (installation == null) {
                return null;
            }
            preferredName = userdir.getName();
        }

        NetbeansWorkspace w = new NetbeansWorkspace();
        w.setUserdir(userdir.getAbsolutePath());
        w.setCachedir(cachedir.getAbsolutePath());
        w.setName(preferredName);
        w.setPath(installation.getPath());
        return w;
    }

    public NOptional<NetbeansInstallation> findBestNetbeansInstallationOrLatest(String version) {
        return findBestNetbeansInstallationOr(version).orElseGetOptionalFrom(() -> {
            NetbeansInstallation[] all = module.ins().findNetbeansInstallations(SortType.LATEST_FIRST);
            if (all.length > 0) {
                return NOptional.of(all[0]);
            }
            return NOptional.ofNamedEmpty("installation");
        });
    }

    public NOptional<NetbeansInstallation> findBestNetbeansInstallationOr(String version) {
        if (!NBlankable.isBlank(version)) {
            NetbeansInstallation[] best = module.ins().findNetbeansInstallationsByVersion(version, SortType.OLDEST_FIRST);
            if (best.length > 0) {
                //return the oldest
                return NOptional.of(best[0]);
            }
        }
        return NOptional.ofNamedEmpty("version " + version);
    }

    public NetbeansWorkspace[] detectNetbeansWorkspaces(boolean autoAdd) {
        List<NetbeansWorkspace> found = new ArrayList<>();
        NetbeansInstallation[] all = module.ins().findNetbeansInstallations(SortType.LATEST_FIRST);
        if (all.length == 0) {
            return new NetbeansWorkspace[0];
        }
        // default config
        File[] nbConfs = new File(System.getProperty("user.home"), ".netbeans").listFiles();
        if (nbConfs != null) {
            for (File file : nbConfs) {
                NetbeansWorkspace d = detectNetbeansWorkspace(file, new File(System.getProperty("user.home"), ".cache/netbeans/" + file.getName()));
                if (d != null) {
                    found.add(d);
                }
            }
        }
        found.sort(new Comparator<NetbeansWorkspace>() {
            @Override
            public int compare(NetbeansWorkspace o1, NetbeansWorkspace o2) {
                NetbeansInstallation i1 = module.ins().findNetbeansInstallation(o1.getPath());
                NetbeansInstallation i2 = module.ins().findNetbeansInstallation(o2.getPath());
                if (i1 != null && i2 != null) {
                    int i = module.ins().comparator(SortType.LATEST_FIRST).compare(i1, i2);
                    if (i != 0) {
                        return i;
                    }
                }
                String n1 = o1.getName();
                String n2 = o2.getName();
                int i = Comparator.nullsLast(Comparator.<String>naturalOrder()).compare(n1, n2);
                if (i != 0) {
                    return i;
                }
                return 0;
            }
        });
        if (autoAdd) {
            //add oldest first
            for (int i = found.size() - 1; i >= 0; i--) {
                addNetbeansWorkspace(found.get(i), NetbeansInstallationStore.DEFAULT);
            }
        }
        return found.toArray(
                new NetbeansWorkspace[0]);
    }

    public NetbeansWorkspace[] findNetbeansWorkspaces() {
        final NetbeansWorkspace[] w = module.conf().getWorkspaces().toArray(new NetbeansWorkspace[0]);
        Arrays.sort(w, (a, b) -> {
            int i = 0;//a.getVersion().compareTo(b.getVersion());
            if (i != 0) {
                return i;
            }
            String n1 = a.getName();
            String n2 = b.getName();
            if (n1 == null) {
                n1 = "";
            }
            if (n2 == null) {
                n2 = "";
            }
            return n1.compareTo(n2);
        });
        return w;
    }

    public NetbeansWorkspace[] findNetbeansWorkspacesByInstallationPath(String path) {
        return module.conf().getWorkspaces().stream().filter(x -> NbUtils.equalsStr(x.getPath(), path)).toArray(NetbeansWorkspace[]::new);
    }

    public NetbeansWorkspace[] findNetbeansWorkspacesWorkspacesByJdkPath(String path) {
        return module.conf().getWorkspaces().stream().filter(x -> NbUtils.equalsStr(x.getJdkhome(), path)).toArray(NetbeansWorkspace[]::new);
    }

    public NetbeansWorkspace convertToNetbeansWorkspace(NetbeansInstallation i) {
        NetbeansWorkspace nw = new NetbeansWorkspace();
        nw.setPath(i.getPath());
        nw.setName(i.getName());
        nw.setUserdir(i.getUserdir());
        nw.setCachedir(i.getCachedir());
        nw.setJdkhome(i.getJdkhome());
        nw.setOptions(i.getOptions());
        return nw;
    }

    public boolean addNetbeansWorkspace(NetbeansInstallation object) {
        return addDefaultNetbeansWorkspace(object.getPath(), object.getStore());
    }

    public boolean addDefaultNetbeansWorkspace(String path, NetbeansInstallationStore store) {
        return addNetbeansWorkspace(convertToNetbeansWorkspace(module.ins().findOrAddNetbeansInstallationOrError(path, store)), store);
    }

    public NetbeansWorkspace findNetbeansWorkspace(NetbeansWorkspace w) {
        for (NetbeansWorkspace workspace : module.conf().getWorkspaces()) {
            if (NbUtils.equalsStr(workspace.getName(), w.getName())) {
                return workspace;
            }
        }
        for (NetbeansWorkspace workspace : module.conf().getWorkspaces()) {
            if (NbUtils.equalsStr(workspace.getPath(), w.getPath())
                    && NbUtils.equalsStr(workspace.getUserdir(), w.getUserdir())
                    && NbUtils.equalsStr(workspace.getCachedir(), w.getCachedir())) {
                return workspace;
            }
        }
        return null;
    }

    public NetbeansWorkspace findNetbeansWorkspace(String w) {
        for (NetbeansWorkspace workspace : module.conf().getWorkspaces()) {
            if (NbUtils.equalsStr(workspace.getName(), w)) {
                return workspace;
            }
        }
        return null;
    }

    public boolean saveNetbeansWorkspace(NetbeansWorkspace w) {
        NetbeansWorkspace old = NetbeansWorkspaceService.this.findNetbeansWorkspace(w);
        if (old != null) {
            old.copyFrom(w);
            module.conf().saveConfig();
            return false;
        } else {
            module.conf().getWorkspaces().add(w);
            module.conf().saveConfig();
            return true;
        }
    }

    public boolean addNetbeansWorkspace(NetbeansWorkspace o, NetbeansInstallationStore store) {
        NetbeansInstallation i = module.ins().findOrAddNetbeansInstallationOrError(o.getPath(), store);
        NetbeansWorkspace ws = findNetbeansWorkspace(o.getPath(), o.getUserdir(), o.getCachedir());
        if (ws == null) {
            NetbeansWorkspace w = new NetbeansWorkspace();
            w.copyFrom(o);
            w.setPath(i.getPath());
            w.setName(NStringUtils.firstNonBlankTrimmed(o.getName(), i.getName(), "Netbeans").trim());
            w.setCreationDate(Instant.now());
            module.conf().getWorkspaces().add(w);
            module.conf().saveConfig();
            return true;
        }
        return false;
    }

    public String extractBaseName(String name) {
        boolean someUpdates = true;
        while (someUpdates) {
            someUpdates = false;
            if (name.toLowerCase().endsWith(" copy")) {
                name = name.substring(0, name.length() - " copy".length());
                someUpdates = true;
            }
            for (String string : suffix) {
                if (name.toLowerCase().endsWith(string)) {
                    name = name.substring(0, name.length() - string.length());
                    someUpdates = true;
                }
            }
        }
        return name;
    }

    public String[] getNewNameProposals(String baseName) {
        List<String> all = new ArrayList<>();
        HashSet<String> base = new HashSet<>();
        base.add(baseName);
        List<String> pref = new ArrayList<>();
        pref.addAll(base);
        pref.addAll(Arrays.asList(prefix));
        for (String p : pref) {
            String m = (p + "").trim();
            all.add(m);
            for (String extra : suffix) {
                m = (p + extra).trim();
                all.add(m);
            }
        }
        return all.toArray(new String[0]);
    }

    public String[] getNewNameProposals() {
        List<String> all = new ArrayList<>();
        HashSet<String> old = new HashSet<>();
        HashSet<String> base = new HashSet<>();
        for (NetbeansWorkspace workspace : findNetbeansWorkspaces()) {
            old.add(workspace.getName());
            base.add(extractBaseName(workspace.getName()));
        }
        List<String> pref = new ArrayList<>();
        pref.addAll(base);
        pref.addAll(Arrays.asList(prefix));
        for (String p : pref) {
            String m = (p + "").trim();
            if (!old.contains(m)) {
                all.add(m);
            }
            for (String extra : suffix) {
                m = (p + extra).trim();
                if (!old.contains(m)) {
                    all.add(m);
                }
            }
        }
        return all.toArray(new String[0]);
    }

    public String getUserdirProposal(NetbeansWorkspace w) {
        String n = NStringUtils.trim(w.getName());
        if (NbUtils.isEmpty(n)) {
            n = "noname";
        }
        String configRoot = NbUtils.getNbOsConfig().getConfigRoot();
        return NbUtils.toOsPath(configRoot + File.separatorChar + n);
    }

    public String[] getUserdirProposals(NetbeansWorkspace w) {
        String n = NStringUtils.trim(w.getName());
        if (NbUtils.isEmpty(n)) {
            n = "noname";
        }
        String configRoot = NbUtils.getNbOsConfig().getConfigRoot();
        List<String> all = new ArrayList<>();
        for (String extra : new String[]{"", " 1", " 2", " 3", " 4", " 5"}) {
            all.add(NbUtils.toOsPath(configRoot + File.separatorChar + n + extra));
        }
        return all.toArray(new String[0]);
    }

    public String[] getCachedirProposals(NetbeansWorkspace w) {
        String n = NStringUtils.trim(w.getName());
        if (NbUtils.isEmpty(n)) {
            n = "noname";
        }
        String cacheRoot = NbUtils.getNbOsConfig().getCacheRoot();
        List<String> all = new ArrayList<>();
        for (String extra : new String[]{"", " 1", " 2", " 3", " 4", " 5"}) {
            all.add(NbUtils.toOsPath(cacheRoot + File.separatorChar + n + extra));
        }
        return all.toArray(new String[0]);
    }

    public String getCachedirProposal(NetbeansWorkspace w) {
        String n = NStringUtils.trim(w.getName());
        if (NbUtils.isEmpty(n)) {
            n = "noname";
        }
        String cacheRoot = NbUtils.getNbOsConfig().getCacheRoot();
        return NbUtils.toOsPath(cacheRoot + File.separatorChar + n);
    }

    public void removeNetbeansWorkspacesByInstallationPath(String path) {
        for (NetbeansWorkspace w : module.ws().findNetbeansWorkspacesByInstallationPath(path)) {
            module.ws().removeNetbeansWorkspace(w);
        }

    }

    public void removeNetbeansWorkspace(NetbeansWorkspace w) {
        NetbeansWorkspace old = NetbeansWorkspaceService.this.findNetbeansWorkspace(w);
        if (old != null) {
            module.conf().getWorkspaces().remove(old);
            module.conf().saveConfig();
        }
    }

    public NetbeansWorkspace findNetbeansWorkspace(String path, String userdir, String cachedir) {
        NetbeansInstallation i = module.ins().findOrAddNetbeansInstallationOrError(path, NetbeansInstallationStore.USER);
        NetbeansWorkspace[] ws = module.conf().getWorkspaces().stream().filter(x -> NbUtils.equalsStr(x.getPath(), path) && NbUtils.equalsStr(x.getUserdir(), userdir) && NbUtils.equalsStr(x.getCachedir(), cachedir)
        ).toArray(NetbeansWorkspace[]::new);
        if (ws.length > 0) {
            return ws[0];
        }
        return null;
    }

    public NetbeansGroup[] detectNetbeansGroups(NetbeansWorkspace w) {
        try {
            if (w.getPath() == null) {
                return null;
            }
            List<String> cmd = new ArrayList<>();
            cmd.add(NbUtils.toOsPath(w.getPath() + "/bin/" + NbUtils.getNbOsConfig().getNetbeansExe()));//linux
            if (w.getUserdir() != null) {
                cmd.add("--userdir");
                cmd.add(NbUtils.resolveFile(w.getUserdir()).getPath());
            }
            if (w.getCachedir() != null) {
                cmd.add("--cachedir");
                cmd.add(NbUtils.resolveFile(w.getCachedir()).getPath());
            }
            if (w.getGroup() != null) {
                cmd.add("--open-group");
                cmd.add(w.getGroup());
            }
            if (w.getJdkhome() != null) {
                cmd.add("--jdkhome");
                cmd.add(w.getJdkhome());
            }
            cmd.add("--nosplash");
            cmd.add("--list-groups");

            String s = NbUtils.response(cmd);
            List<NetbeansGroup> all = new ArrayList<>();
            if (s != null) {
                all.add(NETBEANS_NO_GROUP);
                all.add(NETBEANS_CLOSE_GROUP);
                String[] split = s.split("\n");
                if (split.length > 0) {
                    if (split[0].startsWith("Shortened Name")) {
                        for (int i = 1; i < split.length; i++) {
                            String string = split[i];
                            int x = string.indexOf(' ');
                            NetbeansGroup g = new NetbeansGroup(string.substring(0, x).trim(), string.substring(x).trim());
                            all.add(g);
                        }
                    }
                }
                return all.toArray(new NetbeansGroup[0]);
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void configureDefaultNetbeansWorkspaces() {
        if (findNetbeansWorkspaces().length == 0) {
            detectNetbeansWorkspaces(true);
        }
    }

    public void removeNetbeansWorkspacesByJdkPath(String path) {
        for (NetbeansWorkspace w : module.ws().findNetbeansWorkspacesWorkspacesByJdkPath(path)) {
            removeNetbeansWorkspace(w);
        }
    }

}
