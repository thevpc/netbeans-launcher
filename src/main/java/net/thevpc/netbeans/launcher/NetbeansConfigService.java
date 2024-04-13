/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.netbeans.launcher;

import net.thevpc.netbeans.launcher.model.*;
import net.thevpc.netbeans.launcher.util.NbUtils;
import net.thevpc.nuts.*;
import net.thevpc.nuts.concurrent.NLocks;
import net.thevpc.nuts.concurrent.NScheduler;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.io.NUncompress;
import net.thevpc.nuts.util.*;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.thevpc.nuts.env.NPlatformFamily;
import net.thevpc.nuts.time.NProgressEvent;
import net.thevpc.nuts.time.NProgressListener;

/**
 * @author thevpc
 */
public class NetbeansConfigService {

    public static final NetbeansGroup NETBEANS_NO_GROUP = new NetbeansGroup("--no-group", "--no-group");
    public static final NetbeansGroup NETBEANS_CLOSE_GROUP = new NetbeansGroup("--close-group", "--close-group");
    private static final Logger LOG = Logger.getLogger(NetbeansConfigService.class.getName());
    private static String[] prefix = {"Workspace", "WS", "NB", "Netbeans"};
    private static String[] suffix = {"-Perso", "-Work", "-Research", "-Edu", "-Fun", "-Test", "-Release", "-Test 1", "-Test 2", "-A", "-B", "-C", "-D", "-E"};
    private final NSession session;
    private List<WritableLongOperation> operations = new ArrayList<>();
    private List<LongOperationListener> operationListeners = new ArrayList<>();
    private ObservableNetbeansConfig config = new ObservableNetbeansConfig();
    private File currentDirectory = new File(System.getProperty("user.home"));
    private List<ConfigListener> configListeners = new ArrayList<>();
    private List<NetbeansBinaryLink> cachedNetbeansBinaryLink = null;
    private boolean configLoaded;

    public NetbeansConfigService(NSession appContext) {
        this.session = appContext;
    }

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

    public void addOnceConfigListener(ConfigListener conf) {
        addConfigListener(new OnceConfigListener(conf));
    }

    public void addConfigListener(ConfigListener conf) {
        configListeners.add(conf);
    }

    public void removeConfigListener(ConfigListener conf) {
        configListeners.remove(conf);
    }

    private static Matcher match(String expr, String str) {
        Pattern p = Pattern.compile(expr);
        Matcher m = p.matcher(str);
        if (m.find()) {
            return m;
        }
        return null;
    }

    public static VersionData parseVersionData(String version, Instant releaseDate) {
        if (version == null) {
            version = "";
        }
        version = version.trim();
        Matcher m;
        if (version.startsWith("20160930")) {
            return new VersionData("8.2", version, Instant.parse("2016-09-30T01:01:00Z"));
        } else if ((m = match("(incubator-)?netbeans-release-(?<bn>[0-9]+)-on-(?<bd>[0-9]+)", version)) != null) {
            String bn = m.group("bn");
            String bd = m.group("bd");
            int ibn = Integer.parseInt(bn);
            if (ibn < 334) {
                return new VersionData("8.x", version, releaseDate);
            } else if (ibn == 334) {
                return new VersionData("9.0", version, releaseDate);
            } else if (ibn < 380) {
                return new VersionData("9.x", version, releaseDate);
            } else if (ibn == 380) {
                return new VersionData("10.0", version, releaseDate);
            } else if (ibn < 404) {
                return new VersionData("10.x", version, releaseDate);
            } else if (ibn == 404) {
                return new VersionData("11.0", version, releaseDate);
            } else if (ibn < 428) {
                return new VersionData("11.0.x", version, releaseDate);
            } else if (ibn == 428) {
                return new VersionData("11.1", version, releaseDate);
            } else {
                return new VersionData("11.1.x", version, releaseDate);
            }
        } else if ((m = match("(?<bn>[0-9]+[.][0-9]+)-(?<bd>.*)", version)) != null) {
            String v = m.group("bn");
            return new VersionData(v, version, releaseDate);
        } else if ((m = match("(?<bn>[0-9.]+)(?<bd>.*)", version)) != null) {
            String bn = m.group("bn");
            return new VersionData(bn, version, releaseDate);
        } else {
            return new VersionData(version, version, releaseDate);
        }
    }

    public File getCurrentDirectory() {
        return currentDirectory;
    }

    public void setCurrentDirectory(File currentDirectory) {
        this.currentDirectory = currentDirectory;
    }

    public ConfigResult configureDefaultNb(File baseFolder, NetbeansInstallationStore store) {
        if (!baseFolder.isDirectory()) {
            return new ConfigResult();
        }
        File[] files = baseFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        ConfigResult r = new ConfigResult();
        if (files != null) {
            for (File file : files) {
                NetbeansInstallation o = findNb(file.getPath());
                if (o == null) {
                    o = detectNb(file.getPath(), store);
                    if (o != null) {
                        r.setFound(r.getFound() + 1);
                        if (addNb(o)) {
                            r.setInstalled(r.getInstalled() + 1);
                        }
                    }
                }
            }
        }
        return r;
    }

    public void configureJdk(File baseFolder) {
        if (!baseFolder.isDirectory()) {
            return;
        }
        File[] files = baseFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        if (files != null) {
            for (File file : files) {
                NPlatformLocation o = findJdk(file.getPath());
                if (o == null) {
                    o = detectJdk(file.getPath());
                    if (o != null) {
                        addJdk(o);
                    }
                }
            }
        }
    }

    public NetbeansBinaryLink[] searchRemoteInstallableNbBinariesWithCache(boolean cached) {
        if (cached && cachedNetbeansBinaryLink != null) {
            return cachedNetbeansBinaryLink.toArray(new NetbeansBinaryLink[0]);
        }
        cachedNetbeansBinaryLink = new ArrayList<>(
                Arrays.asList(searchRemoteInstallableNbBinaries())
        );
        return cachedNetbeansBinaryLink.toArray(new NetbeansBinaryLink[0]);
    }

    public NetbeansBinaryLink[] searchRemoteInstallableNbBinaries() {
        List<NetbeansBinaryLink> all = new ArrayList<>(
                Arrays.asList(NElements.of(session).json().parse(getClass().getResource("/net/thevpc/netbeans/launcher/binaries.json"), NetbeansBinaryLink[].class))
        );

        //nuts supports out of the box navigating apache website using htmlfs
        for (NPath p : NPath.of("htmlfs:https://archive.apache.org/dist/netbeans/netbeans/", session).stream()) {
            if (p.isDirectory()) {
                ///12.0/netbeans-12.0-bin.zip
                String version = p.getName();
                NPath b = NPath.of("https://archive.apache.org/dist/netbeans/netbeans/" + version + "/netbeans-" + version + "-bin.zip", session);
                if (b.exists()) {
                    all.add(new NetbeansBinaryLink()
                            .setPackaging("zip")
                            .setVersion(version)
                            .setUrl(b.toString())
                            .setReleaseDate(b.getLastModifiedInstant())
                    );
                }
            }
        }

        Set<String> locallyAvailable = Arrays.stream(getAllNb()).map(NetbeansInstallation::getVersion).collect(Collectors.toSet());
        return all.stream().filter(x -> !locallyAvailable.contains(x.getVersion()))
                .sorted((o1, o2) -> -NVersion.of(o1.getVersion()).get().compareTo(o2.getVersion()))
                .toArray(NetbeansBinaryLink[]::new);
    }

    public void configureDefaults() {
        configureDefaultJdk();
        configureDefaultNb();
        configureDefaultNbWorkspaces();
    }

    public void configureDefaultNbWorkspaces() {
        for (NetbeansInstallation object : config.getInstallations()) {
            addNbWorkspace(object);
        }
    }

    public ConfigResult configureDefaultNb() {
        ConfigResult r0 = new ConfigResult();
        for (String programFolder : NbUtils.getNbOsConfig(session).getProgramFolders()) {
            File level1Folder = NbUtils.resolveFile(programFolder);
            ConfigResult r = configureDefaultNb(level1Folder, NetbeansInstallationStore.SYSTEM);
            r0.add(r);
            if (r.getFound() > 0) {
                //
            } else {
                //Level 2
                if (level1Folder.isDirectory()) {
                    for (File level2Folder : level1Folder.listFiles()) {
                        r = configureDefaultNb(level2Folder, NetbeansInstallationStore.SYSTEM);
                        r0.add(r);
                        if (r.getFound() > 0) {
                            //
                        } else {
                            //Level 3
                            if (level2Folder.isDirectory()) {
                                for (File level3Folder : level2Folder.listFiles()) {
                                    r = configureDefaultNb(level3Folder, NetbeansInstallationStore.SYSTEM);
                                    r0.add(r);
                                    if (r.getFound() > 0) {
                                        //
                                    } else {
                                        //nothing
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return r0;
    }

    public void configureDefaultJdk() {
        for (String jdkFolder : NbUtils.getNbOsConfig(session).getJdkFolders()) {
            configureJdk(NbUtils.resolveFile(jdkFolder));
        }
    }

    public NPlatformLocation detectJdk(String path) {
        return NPlatforms.of(session).resolvePlatform(NPlatformFamily.JAVA, path, null).orNull();
    }

    public NetbeansGroup[] detectNbGroups(NetbeansWorkspace w) {
        try {
            if (w.getPath() == null) {
                return null;
            }
            List<String> cmd = new ArrayList<>();
            cmd.add(NbUtils.toOsPath(w.getPath() + "/bin/" + NbUtils.getNbOsConfig(session).getNetbeansExe()));//linux
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

            String s = NbUtils.response(cmd, session);
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

    public VersionAndDate detectNbVersionFrom_build_info_file(String path) {
        File f = NbUtils.resolveFile(path);
        if (!new File(f, NbUtils.toOsPath("nb/build_info")).exists()) {
            return null;
        }
        try (BufferedReader r = new BufferedReader(new FileReader(new File(f, NbUtils.toOsPath("nb/build_info"))))) {
            String line = null;
            String version = null;
            Instant date = null;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0) {
                    if (line.toLowerCase().startsWith("number:")) {
                        version = line.substring("number:".length()).trim();
                    } else if (line.toLowerCase().startsWith("date:")) {
                        String dateStr = line.substring("date:".length()).trim();
                        if (!dateStr.startsWith("$")) {
                            try {
                                String[] split = dateStr.split(" +");
                                Calendar c = Calendar.getInstance();
                                c.set(Calendar.MILLISECOND, 0);
                                c.set(Calendar.SECOND, 0);
                                c.set(Calendar.HOUR_OF_DAY, 0);
                                c.set(Calendar.YEAR, Integer.parseInt(split[2]));
                                c.set(Calendar.MONTH, ((Function<String, Integer>) s -> {
                                    switch (s) {
                                        case "jan":
                                            return Calendar.JANUARY;
                                        case "feb":
                                            return Calendar.FEBRUARY;
                                        case "mar":
                                            return Calendar.MARCH;
                                        case "apr":
                                            return Calendar.APRIL;
                                        case "may":
                                            return Calendar.MAY;
                                        case "jun":
                                            return Calendar.JUNE;
                                        case "jul":
                                            return Calendar.JULY;
                                        case "aug":
                                            return Calendar.AUGUST;
                                        case "sept":
                                            return Calendar.SEPTEMBER;
                                        case "oct":
                                            return Calendar.OCTOBER;
                                        case "nov":
                                            return Calendar.NOVEMBER;
                                        case "dec":
                                            return Calendar.DECEMBER;
                                        default: {
                                            throw new IllegalArgumentException("invalid date");
                                        }
                                    }
                                }).apply(split[1].toLowerCase()));
                                date = c.toInstant();
                            } catch (Exception ex) {
                                //ignore
                            }
                        }
                    }
                }
            }
            if (version != null) {
                return new VersionAndDate(version, date);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String detectNbVersionFrom_VERSION_file(String path) {
        File f = NbUtils.resolveFile(path);
        if (!new File(f, NbUtils.toOsPath("nb/VERSION.txt")).exists()) {
            return null;
        }
        try (BufferedReader r = new BufferedReader(new FileReader(new File(f, NbUtils.toOsPath("nb/VERSION.txt"))))) {
            String line = null;
            String version = null;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0) {
                    if (line.startsWith("#")) {
                        //ignore
                    } else {
                        version = line;
                        return version;
                    }
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public NetbeansInstallation[] detectNbs(String path, boolean autoAdd, NetbeansInstallationStore store) {
        return detectNbs(path, autoAdd, store, 3);
    }

    public NetbeansInstallation[] detectNbs(String path, boolean autoAdd, NetbeansInstallationStore store, int levels) {
        if (path == null) {
            return new NetbeansInstallation[0];
        }
        NetbeansInstallation a = detectNb(path, store);
        if (a != null) {
            if (autoAdd) {
                addNb(a);
            }
            return new NetbeansInstallation[]{a};
        }
        if (levels > 0) {
            List<NetbeansInstallation> subNetbeans = new ArrayList<>();
            File f = NbUtils.resolveFile(path);
            if (f.isDirectory()) {
                File[] pp = f.listFiles(x -> x.isDirectory());
                if (pp != null) {
                    for (File file : pp) {
                        subNetbeans.addAll(Arrays.asList(detectNbs(file.getPath(), autoAdd, store, levels - 1)));
                    }
                }
            }
            return subNetbeans.toArray(new NetbeansInstallation[0]);
        }
        return new NetbeansInstallation[0];
    }

    public NetbeansInstallation detectNb(String path, NetbeansInstallationStore store) {
        if (path == null) {
            return null;
        }
        File f = NbUtils.resolveFile(path);
        if (!new File(f, NbUtils.toOsPath("bin/" + NbUtils.getNbOsConfig(session).getNetbeansExe())).exists()) {
            return null;
        }
        VersionAndDate versionAndDate = detectNbVersionFrom_build_info_file(path);
        if (versionAndDate == null) {
            return null;
        }
        VersionData vd = parseVersionData(versionAndDate.getVersion(), versionAndDate.getDate());
        NetbeansInstallation netbeansInstallation = new NetbeansInstallation();
        netbeansInstallation.setName("Netbeans IDE " + vd.getVersion());
        netbeansInstallation.setVersion(vd.getVersion());
        netbeansInstallation.setFullVersion(vd.getFullVersion());
        netbeansInstallation.setReleaseDate(vd.getReleaseDate());
        netbeansInstallation.setPath(path);
        Properties nbconf = NbUtils.loadProperties(new File(f, NbUtils.toOsPath("etc/netbeans.conf")));
        for (Map.Entry<Object, Object> entry : new HashSet<>(nbconf.entrySet())) {
            String s = (String) entry.getValue();
            if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
                s = s.substring(1, s.length() - 1);
                nbconf.setProperty((String) entry.getKey(), s);
            }
        }
        String netbeans_default_userdir = nbconf.getProperty("netbeans_default_userdir");
        if (netbeans_default_userdir != null) {
            netbeans_default_userdir = netbeans_default_userdir.replace("${DEFAULT_USERDIR_ROOT}", NbUtils.getNbOsConfig(session).getConfigRoot());
        }
        String netbeans_default_cachedir = nbconf.getProperty("netbeans_default_cachedir");
        if (netbeans_default_cachedir != null) {
            netbeans_default_cachedir = netbeans_default_cachedir.replace("${DEFAULT_CACHEDIR_ROOT}", NbUtils.getNbOsConfig(session).getCacheRoot());
        }
        String netbeans_default_options = nbconf.getProperty("netbeans_default_options");
        if (netbeans_default_options != null) {
            netbeans_default_options = netbeans_default_options.replace("${DEFAULT_CACHEDIR_ROOT}", NbUtils.getNbOsConfig(session).getCacheRoot());
        }
//                String netbeans_default_options=nbconf.getProperty("netbeans_default_options");
        String netbeans_jdkhome = nbconf.getProperty("netbeans_jdkhome");
        netbeansInstallation.setUserdir(netbeans_default_userdir);
        netbeansInstallation.setCachedir(netbeans_default_cachedir);
        netbeansInstallation.setJdkhome(netbeans_jdkhome);
        netbeansInstallation.setOptions(netbeans_default_options);
        netbeansInstallation.setStore(store);
        return netbeansInstallation;
    }

    public NetbeansWorkspace findNbWorkspace(String path, String userdir, String cachedir) {
        NetbeansInstallation i = getNbOrError(path, NetbeansInstallationStore.USER);
        NetbeansWorkspace[] ws = config.getWorkspaces().stream().filter(x -> NbUtils.equalsStr(x.getPath(), path) && NbUtils.equalsStr(x.getUserdir(), userdir) && NbUtils.equalsStr(x.getCachedir(), cachedir)
        ).toArray(NetbeansWorkspace[]::new);
        if (ws.length > 0) {
            return ws[0];
        }
        return null;
    }

    public NetbeansInstallation findNb(String path) {
        for (NetbeansInstallation installation : config.getInstallations()) {
            if (NbUtils.equalsStr(path, installation.getPath())) {
                return installation;
            }
        }
        if (!NbUtils.isPath(path)) {
            for (NetbeansInstallation installation : config.getInstallations()) {
                if (NbUtils.equalsStr(path, installation.getName())) {
                    return installation;
                }
            }
            for (NetbeansInstallation installation : config.getInstallations()) {
                if (NbUtils.equalsStr(path, installation.getVersion())) {
                    return installation;
                }
            }
        }
        return null;
    }

    public NPlatformLocation findJdk(String path) {
        if (path == null) {
            return null;
        }
        for (NPlatformLocation loc : config.getJdkLocations()) {
            if (NbUtils.equalsStr(path, loc.getPath())) {
                return loc;
            }
        }
        if (!NbUtils.isPath(path)) {
            for (NPlatformLocation loc : config.getJdkLocations()) {
                if (NbUtils.equalsStr(path, loc.getName())) {
                    return loc;
                }
            }
            for (NPlatformLocation loc : config.getJdkLocations()) {
                if (NbUtils.equalsStr(path, loc.getVersion())) {
                    return loc;
                }
            }
        }
        return null;
    }

    public boolean addNb(NetbeansInstallation netbeansInstallation) {
        for (NetbeansInstallation installation : config.getInstallations()) {
            if (NbUtils.equalsStr(netbeansInstallation.getPath(), installation.getPath())) {
                return false;
            }
        }
        config.getInstallations().add(netbeansInstallation);
        saveFile();
        addDefaultNbWorkspace(netbeansInstallation.getPath(), netbeansInstallation.getStore());
        return true;
    }

    public boolean setSumoMode(boolean b) {
        config.getSumoMode().set(b);
        saveFile();
        return true;
    }

    public boolean addJdk(NPlatformLocation netbeansInstallation) {
        for (NPlatformLocation installation : config.getJdkLocations()) {
            if (NbUtils.equalsStr(netbeansInstallation.getPath(), installation.getPath())) {
                return false;
            }
        }
        config.getJdkLocations().add(netbeansInstallation);
        saveFile();
        return true;
    }

    public NetbeansWorkspace[] getAllNbWorkspaces() {
        final NetbeansWorkspace[] w = config.getWorkspaces().toArray(new NetbeansWorkspace[0]);
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

    public NPlatformLocation[] getAllJdk() {
        List<NPlatformLocation> list = config.getJdkLocations().list();
        list.sort((a, b) -> {
            int i = NbUtils.compareVersions(a.getVersion(), b.getVersion());
            if (i != 0) {
                return i;
            }
            return a.getName().compareTo(b.getName());
        });
        return list.toArray(new NPlatformLocation[0]);
    }

    public NetbeansInstallation[] getAllNb() {
        NetbeansInstallation[] list = config.getInstallations().toArray(new NetbeansInstallation[0]);
        Arrays.sort(list, (a, b) -> {
            int i = NbUtils.compareVersions(a.getVersion(), b.getVersion());
            if (i != 0) {
                return i;
            }
            return a.getName().compareTo(b.getName());
        });
        return list;
    }

    public NetbeansWorkspace[] getWorkspacesByNb(String path) {
        return config.getWorkspaces().stream().filter(x -> NbUtils.equalsStr(x.getPath(), path)).toArray(NetbeansWorkspace[]::new);
    }

    public NetbeansWorkspace[] getWorkspacesByJdk(String path) {
        return config.getWorkspaces().stream().filter(x -> NbUtils.equalsStr(x.getJdkhome(), path)).toArray(NetbeansWorkspace[]::new);
    }

    public NetbeansInstallation getNbOrError(String path, NetbeansInstallationStore store) {
        NetbeansInstallation o = findNb(path);
        if (o == null) {
            o = detectNb(path, store);
            if (o == null) {
                throw new NIllegalArgumentException(session, NMsg.ofC("invalid Netbeans installation directory %s", path));
            }
            addNb(o);
        }
        return o;
    }

    public NetbeansInstallation getNb(String path, NetbeansInstallationStore store) {
        NetbeansInstallation o = findNb(path);
        if (o == null) {
            o = detectNb(path, store);
            if (o != null) {
                addNb(o);
            }
        }
        return o;
    }

    public NPlatformLocation getJdk(String path) {
        if (path == null) {
            return null;
        }
        NPlatformLocation o = findJdk(path);
        if (o == null) {
            o = detectJdk(path);
            if (o != null) {
                addJdk(o);
            }
        }
        return o;
    }

    public NetbeansWorkspace createNbWorkspace(NetbeansInstallation i) {
        NetbeansWorkspace nw = new NetbeansWorkspace();
        nw.setPath(i.getPath());
        nw.setName(i.getName());
        nw.setUserdir(i.getUserdir());
        nw.setCachedir(i.getCachedir());
        nw.setJdkhome(i.getJdkhome());
        nw.setOptions(i.getOptions());
        return nw;
    }

    public boolean addNbWorkspace(NetbeansInstallation object) {
        return addDefaultNbWorkspace(object.getPath(), object.getStore());
    }

    public boolean addDefaultNbWorkspace(String path, NetbeansInstallationStore store) {
        return addNbWorkspace(createNbWorkspace(getNbOrError(path, store)), store);
    }

    public NetbeansWorkspace findNbWorkspace(NetbeansWorkspace w) {
        for (NetbeansWorkspace workspace : config.getWorkspaces()) {
            if (NbUtils.equalsStr(workspace.getName(), w.getName())) {
                return workspace;
            }
        }
        for (NetbeansWorkspace workspace : config.getWorkspaces()) {
            if (NbUtils.equalsStr(workspace.getPath(), w.getPath())
                    && NbUtils.equalsStr(workspace.getUserdir(), w.getUserdir())
                    && NbUtils.equalsStr(workspace.getCachedir(), w.getCachedir())) {
                return workspace;
            }
        }
        return null;
    }

    public NetbeansWorkspace findNbWorkspace(String w) {
        for (NetbeansWorkspace workspace : config.getWorkspaces()) {
            if (NbUtils.equalsStr(workspace.getName(), w)) {
                return workspace;
            }
        }
        return null;
    }

    public boolean saveNbWorkspace(NetbeansWorkspace w) {
        NetbeansWorkspace old = findNbWorkspace(w);
        if (old != null) {
            old.copyFrom(w);
            saveFile();
            return false;
        } else {
            config.getWorkspaces().add(w);
            saveFile();
            return true;
        }
    }

    public boolean addNbWorkspace(NetbeansWorkspace o, NetbeansInstallationStore store) {
        NetbeansInstallation i = getNbOrError(o.getPath(), store);
        NetbeansWorkspace ws = findNbWorkspace(o.getPath(), o.getUserdir(), o.getCachedir());
        if (ws == null) {
            NetbeansWorkspace w = new NetbeansWorkspace();
            w.copyFrom(o);
            w.setPath(i.getPath());
            w.setName(i.getName());
            w.setCreationDate(Instant.now());
            config.getWorkspaces().add(w);
            saveFile();
            return true;
        }
        return false;
    }

    public void removeNb(String path) {
        NetbeansInstallation o = findNb(path);
        if (o != null) {
            for (NetbeansWorkspace w : getWorkspacesByNb(o.getPath())) {
                removeNbWorkspace(w);
            }
            config.getInstallations().remove(o);
            saveFile();
        }
    }

    public void removeJdk(String path) {
        NPlatformLocation o = findJdk(path);
        if (o != null) {
            for (NetbeansWorkspace w : getWorkspacesByJdk(o.getPath())) {
                removeNbWorkspace(w);
            }
            config.getJdkLocations().remove(o);
            saveFile();
        }
    }

    public void removeNbWorkspace(NetbeansWorkspace w) {
        NetbeansWorkspace old = findNbWorkspace(w);
        if (old != null) {
            config.getWorkspaces().remove(old);
            saveFile();
        }
    }

    private String resolveLafClass(String n) {
        if (n == null) {
            n = "";
        }
        String n0 = n.toLowerCase().trim();
        switch (n0) {
            case "default":
                return "";
            case "metal":
                return "javax.swing.plaf.metal.MetalLookAndFeel";
            case "windows":
                return "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
            case "mac":
                return "com.sun.java.swing.plaf.mac.MacLookAndFeel";
            case "motif":
                return "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
            case "nimbus":
                return "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
            case "gtk":
                return "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
            default:
                return n;
        }
    }

    public String[] createRunCommand(NetbeansWorkspace w) {
        List<String> cmd = new ArrayList<>();
        cmd.add(NbUtils.toOsPath(w.getPath() + "/bin/" + NbUtils.getNbOsConfig(session).getNetbeansExe()));//linux
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
        if (w.getLaf() != null) {
            String l = resolveLafClass(w.getLaf());
            if (l.length() > 0) {
                cmd.add("--laf");
                cmd.add(l);
            }
        }
        if (w.getFontSize() > 0) {
            cmd.add("--fontsize");//linux
            cmd.add(String.valueOf(w.getFontSize()));
        }
        if (w.getCpPrepend() != null) {
            String l = w.getCpPrepend().trim();
            if (l.length() > 0) {
                cmd.add("--cp:p");
                cmd.add(l);
            }
        }
        if (w.getCpAppend() != null) {
            String l = w.getCpAppend().trim();
            if (l.length() > 0) {
                cmd.add("--cp:a");
                cmd.add(l);
            }
        }
        if (w.getLocale() != null) {
            String l = w.getLocale().trim();
            if (l.length() > 0) {
                cmd.add("--locale");
                cmd.add(l);
            }
        }
        if (w.getOptions() != null) {
            String l = w.getOptions().trim();
            if (l.length() > 0) {
                for (String s : l.split(" ")) {
                    if (s.length() > 0) {
                        cmd.add(s);
                    }
                }
            }
        }
        return cmd.toArray(new String[0]);
    }

    public NExecCmd run(NetbeansWorkspace w) throws IOException {
        String[] cmd = createRunCommand(w);
        return NExecCmd.of(session)
                .setExecutionType(NExecutionType.SYSTEM)
                .setDirectory(
                        NBlankable.isBlank(w.getPath()) ? null
                        : NPath.of(w.getPath(), session)
                )
                .addCommand(cmd)
                .redirectErr()
                .setFailFast(true)
                .run();
    }

    public synchronized void saveFile() {
        NetbeansConfig c = config.getNetbeansConfig();
        NElements.of(session).json()
                .setValue(c).setNtf(false)
                .print(session.getAppConfFolder().resolve("config.json"));
    }

    public <T> void loadFile(ConfigListener onFinish) {
        NetbeansConfig config = null;
        boolean loaded = false;
        NPath validFile = session.getAppConfFolder().resolve("config.json");
        boolean foundCurrVersionFile = false;
        if (validFile.isRegularFile()) {
            try {
                config = (NetbeansConfig) NElements.of(session).json().parse(validFile, NetbeansConfig.class);
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
                saveFile();
            }
            loaded = true;
        }
        if (!foundCurrVersionFile) {
            List<NId> olderVersions = NSearchCmd.of(session).setInstallStatus(
                    NInstallStatusFilters.of(session).byInstalled(true)
            ).addId(session.getAppId().builder().setVersion("").build()).getResultIds().stream().sorted(
                    (a, b) -> b.getVersion().compareTo(a.getVersion())
            ).filter(x -> x.getVersion().compareTo(session.getAppId().getVersion()) < 0).collect(Collectors.toList());
            for (NId olderVersion : olderVersions) {
                NPath validFile2
                        = NLocations.of(session).getStoreLocation(olderVersion, NStoreType.CONF)
                                .resolve("config.json");
                if (validFile2.isRegularFile()) {
                    try {
                        config = (NetbeansConfig) NElements.of(session).json().parse(validFile2, NetbeansConfig.class);
                    } catch (Exception e) {
                        System.err.println("Unable to load config from " + validFile2.toString());
                        break;
                    }
                    if (config != null) {
                        saveFile();
                        loaded = true;
                        break;
                    }
                }
            }
        }
        if (config == null) {
            config = new NetbeansConfig();
        }
        if (config.getInstallations().isEmpty()) {
            saveFile();
            new Thread(() -> {
                configureDefaults();
                saveFile();
            }).start();
        }
        this.config.setNetbeansConfig(config);
        this.configLoaded = true;
        for (ConfigListener configListener : configListeners.toArray(new ConfigListener[0])) {
            configListener.onConfigLoaded();
        }
        if (onFinish != null) {
            onFinish.onConfigLoaded();
        }
    }

    public void loadAsync(ConfigListener onFinish) {
        NScheduler.of(session)
                .executorService().submit(() -> this.load(onFinish));
    }

    public void load(ConfigListener onFinish) {
        loadFile(onFinish);
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
        for (NetbeansWorkspace workspace : getAllNbWorkspaces()) {
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
        String configRoot = NbUtils.getNbOsConfig(session).getConfigRoot();
        return NbUtils.toOsPath(configRoot + File.separatorChar + n);
    }

    public String[] getUserdirProposals(NetbeansWorkspace w) {
        String n = NStringUtils.trim(w.getName());
        if (NbUtils.isEmpty(n)) {
            n = "noname";
        }
        String configRoot = NbUtils.getNbOsConfig(session).getConfigRoot();
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
        String cacheRoot = NbUtils.getNbOsConfig(session).getCacheRoot();
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
        String cacheRoot = NbUtils.getNbOsConfig(session).getCacheRoot();
        return NbUtils.toOsPath(cacheRoot + File.separatorChar + n);
    }

    public boolean isSumoMode() {
        return config.getSumoMode().get();
    }

    public NetbeansInstallation installNetbeansBinary(NetbeansBinaryLink i) {
        NPath zipTo = session.getAppSharedFolder(NStoreType.BIN)
                .resolve("org")
                .resolve("netbeans")
                .resolve("netbeans-" + i.getVersion() + ".zip");
        NPath folderTo = session.getAppSharedFolder(NStoreType.BIN)
                .resolve("org")
                .resolve("netbeans")
                .resolve("netbeans-" + i.getVersion());
        //if (!Files.exists(zipTo)) {
        NCp.of(session).from(NPath.of(i.getUrl(), session)).to(zipTo).addOptions(NPathOption.LOG, NPathOption.TRACE)
                .setProgressMonitor(new OpNInputStreamProgressMonitor(addOperation("Downloading " + i)))
                .run();
        //}
        NLocks.of(session).setSource(zipTo).run(() -> {
            if (folderTo.resolve("bin").resolve("netbeans").exists()) {
                //already unzipped!!
            } else {
                NUncompress.of(session).from(zipTo).to(folderTo).setSkipRoot(true)
                        .progressMonitor(new OpNInputStreamProgressMonitor(addOperation("Unzipping " + i)))
                        .run();
            }
        });
        NetbeansInstallation o = detectNb(folderTo.toString(), NetbeansInstallationStore.DEFAULT);
        if (o != null) {
            switch (NEnvs.of(session).getOsFamily()) {
                case LINUX:
                case UNIX:
                case MACOS: {
                    for (String s : new String[]{
                        "/bin/netbeans",
                        "/java/maven/bin/mvn",
                        "/java/maven/bin/mvnDebug",
                        "/java/maven/bin/mvnyjp",
                        "/extide/ant/bin/ant",
                        "/extide/ant/bin/antRun",
                        "/extide/ant/bin/antRun.pl",
                        "/extide/ant/bin/complete-ant-cmd.pl",
                        "/extide/ant/bin/runant.pl",
                        "/extide/ant/bin/runant.py"
                    }) {
                        Path n = Paths.get(o.getPath() + s);
                        if (Files.exists(n)) {
                            n.toFile().setExecutable(true);
                        }
                    }
                    try {
                        Path nativeexecution = Paths.get(o.getPath() + "/ide/bin/nativeexecution/");
                        if (Files.isDirectory(nativeexecution)) {
                            Files.walkFileTree(nativeexecution, new FileVisitor<Path>() {
                                @Override
                                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                                    return FileVisitResult.CONTINUE;
                                }

                                @Override
                                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                    String n = file.getFileName().toString();
                                    if (n.indexOf('.') < 0) {
                                        file.toFile().setExecutable(true);
                                    }
                                    return FileVisitResult.CONTINUE;
                                }

                                @Override
                                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                                    return FileVisitResult.CONTINUE;
                                }

                                @Override
                                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                                    return FileVisitResult.CONTINUE;
                                }
                            });
                        }
                    } catch (IOException e) {
                        LOG.log(Level.FINEST, "Unable to visit " + o.getPath());
                    }
                    break;
                }
            }
            addNb(o);
        }
        return o;
    }

    public void fire(WritableLongOperation w) {
        if (w.getStatus() == LongOperationStatus.ENDED) {
            operations.remove(w);
        }
        for (LongOperationListener operationListener : operationListeners) {
            operationListener.onLongOperationProgress(w);
        }
    }

    public void addOperationListener(LongOperationListener listener) {
        operationListeners.add(listener);
    }

    public void removeOperationListener(LongOperationListener listener) {
        operationListeners.add(listener);
    }

    public LongOperation[] getOperations() {
        return operations.toArray(new LongOperation[0]);
    }

    public WritableLongOperation addOperation(String name) {
        DefaultLongOperation d = new DefaultLongOperation(this);
        d.setName(name);
        d.setStatus(LongOperationStatus.INIT);
        operations.add(d);
        return d;
    }

    public ObservableNetbeansConfig getConfig() {
        return config;
    }

    public static class ConfigResult {

        private int found = 0;
        private int installed = 0;

        public int getFound() {
            return found;
        }

        public ConfigResult setFound(int found) {
            this.found = found;
            return this;
        }

        public int getInstalled() {
            return installed;
        }

        public ConfigResult setInstalled(int installed) {
            this.installed = installed;
            return this;
        }

        public ConfigResult add(ConfigResult y) {
            if (y != null) {
                this.found += y.found;
                this.installed += y.installed;
            }
            return this;
        }
    }

    public static final class VersionAndDate {

        private String version;
        private Instant date;

        public VersionAndDate(String version, Instant date) {
            this.version = version;
            this.date = date;
        }

        public String getVersion() {
            return version;
        }

        public Instant getDate() {
            return date;
        }
    }

    public static class VersionData {

        private String version;
        private String fullVersion;
        private Instant releaseDate;

        public VersionData(String version, String fullVersion, Instant releaseDate) {
            this.setVersion(version);
            this.setFullVersion(fullVersion);
            this.releaseDate = releaseDate;
        }

        public Instant getReleaseDate() {
            return releaseDate;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getFullVersion() {
            return fullVersion;
        }

        public void setFullVersion(String fullVersion) {
            this.fullVersion = fullVersion;
        }
    }

    private static class OpNInputStreamProgressMonitor implements NProgressListener {

        private final WritableLongOperation op;

        public OpNInputStreamProgressMonitor(WritableLongOperation op) {
            this.op = op;
        }

        @Override
        public boolean onProgress(NProgressEvent event) {
            switch (event.getState()) {
                case START: {
                    op.start(event.isIndeterminate());
                    break;
                }
                case COMPLETE: {
                    op.end();
                    break;
                }
                case PROGRESS: {
                    op.setPercent((float) (event.getProgress() * 100));
                    break;
                }
            }
            return true;
        }
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

}
