/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.netbeans.launcher.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import net.thevpc.netbeans.launcher.model.*;
import net.thevpc.netbeans.launcher.util.OpNInputStreamProgressMonitor;
import net.thevpc.netbeans.launcher.util.NbStringUtils;

import static net.thevpc.netbeans.launcher.util.NbStringUtils.match;

import net.thevpc.netbeans.launcher.util.NbUtils;
import net.thevpc.nuts.app.NApp;
import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.concurrent.NLock;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.elem.NElementParser;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.io.NUncompress;
import net.thevpc.nuts.platform.NStoreType;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.text.NMsg;

/**
 * @author vpc
 */
public class NetbeansInstallationService {

    private List<NetbeansBinaryLink> cachedNetbeansBinaryLink = null;
    private static final Logger LOG = Logger.getLogger(NetbeansInstallationService.class.getName());

    private final NetbeansLauncherModule module;

    public NetbeansInstallationService(NetbeansLauncherModule module) {
        this.module = module;
    }

    public NetbeansInstallation[] findNetbeansInstallationsByVersion(String version, SortType sortType) {
        NetbeansInstallation[] list = module.conf().getInstallations()
                .stream().filter(x -> {
                    return NbUtils.compareVersions(x.getVersion(), version) >= 0;
                })
                .toArray(NetbeansInstallation[]::new);
        Arrays.sort(list, comparator(sortType));
        return list;
    }

    public NetbeansInstallation[] findNetbeansInstallations(SortType sortType) {
        NetbeansInstallation[] list = module.conf().getInstallations().toArray(new NetbeansInstallation[0]);
        Arrays.sort(list, comparator(sortType));
        return list;
    }

    public NetbeansInstallation findOrAddNetbeansInstallationOrError(String path, NetbeansInstallationStore store) {
        NetbeansInstallation o = NetbeansInstallationService.this.findNetbeansInstallation(path);
        if (o == null) {
            o = detectNetbeansInstallations(path, store);
            if (o == null) {
                throw new NIllegalArgumentException(NMsg.ofC("invalid Netbeans installation directory %s", path));
            }
            NetbeansInstallationService.this.addNetbeansInstallation(o);
        }
        return o;
    }

    public NetbeansInstallation findOrAddNetbeansInstallation(String path, NetbeansInstallationStore store) {
        NetbeansInstallation o = NetbeansInstallationService.this.findNetbeansInstallation(path);
        if (o == null) {
            o = detectNetbeansInstallations(path, store);
            if (o != null) {
                NetbeansInstallationService.this.addNetbeansInstallation(o);
            }
        }
        return o;
    }

    public NetbeansInstallation addNetbeansInstallationByLink(NetbeansBinaryLink i) {
        NPath zipTo = NApp.of().getSharedFolder(NStoreType.BIN)
                .resolve("org")
                .resolve("netbeans")
                .resolve("netbeans-" + i.getVersion() + ".zip");
        NPath folderTo = NApp.of().getSharedFolder(NStoreType.BIN)
                .resolve("org")
                .resolve("netbeans")
                .resolve("netbeans-" + i.getVersion());
        //if (!Files.exists(zipTo)) {
        NCp.of().from(NPath.of(i.getUrl())).to(zipTo).addOptions(NPathOption.LOG, NPathOption.TRACE)
                .setProgressMonitor(new OpNInputStreamProgressMonitor(module.rt().addOperation("Downloading " + i)))
                .run();
        //}
        NLock.of(zipTo).runWith(() -> {
            if (folderTo.resolve("bin").resolve("netbeans").exists()) {
                //already unzipped!!
            } else {
                NUncompress.of().from(zipTo).to(folderTo).setSkipRoot(true)
                        .progressMonitor(new OpNInputStreamProgressMonitor(module.rt().addOperation("Unzipping " + i)))
                        .run();
            }
        });
        NetbeansInstallation o = detectNetbeansInstallations(folderTo.toString(), NetbeansInstallationStore.DEFAULT);
        if (o != null) {
            switch (NWorkspace.of().getOsFamily()) {
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
            addNetbeansInstallation(o);
        }
        return o;
    }

    public void removeNetbeansInstallation(String path) {
        NetbeansInstallation o = findNetbeansInstallation(path);
        if (o != null) {
            module.ws().removeNetbeansWorkspacesByInstallationPath(o.getPath());
            module.conf().getInstallations().remove(o);
            module.conf().saveConfig();
        }
    }

    public boolean addNetbeansInstallation(NetbeansInstallation netbeansInstallation) {
        for (NetbeansInstallation installation : module.conf().getInstallations()) {
            if (NbUtils.equalsStr(netbeansInstallation.getPath(), installation.getPath())) {
                return false;
            }
        }
        module.conf().getInstallations().add(netbeansInstallation);
        module.conf().saveConfig();
        //try to add existing workspaces (under ~/.netbeans)
        if (module.conf().getInstallations().size() == 1) {
            //this is the first one!
            module.ws().detectNetbeansWorkspaces(true);
        }
        //create default workspace for this installation if none defined
        NetbeansWorkspace[] old = module.ws().findNetbeansWorkspacesByInstallationPath(netbeansInstallation.getPath());
        if (old.length == 0) {
            module.ws().addNetbeansWorkspace(netbeansInstallation);
        }
        return true;
    }

    public NetbeansInstallation findNetbeansInstallation(String path) {
        for (NetbeansInstallation installation : module.conf().getInstallations()) {
            if (NbUtils.equalsStr(path, installation.getPath())) {
                return installation;
            }
        }
        if (!NbUtils.isPath(path)) {
            for (NetbeansInstallation installation : module.conf().getInstallations()) {
                if (NbUtils.equalsStr(path, installation.getName())) {
                    return installation;
                }
            }
            for (NetbeansInstallation installation : module.conf().getInstallations()) {
                if (NbUtils.equalsStr(path, installation.getVersion())) {
                    return installation;
                }
            }
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

    public NetbeansInstallation[] detectNetbeansInstallations(String path, boolean autoAdd, NetbeansInstallationStore store) {
        NetbeansInstallation[] found = detectNetbeansInstallations(path, store, 4);
        if (autoAdd) {
            Arrays.sort(found, comparator(SortType.LATEST_FIRST));
            for (NetbeansInstallation a : found) {
                addNetbeansInstallation(a);
            }
            return found;
        } else {
            return found;
        }
    }

    public Comparator<NetbeansLocation> comparator(SortType sortType) {
        if (sortType == null) {
            sortType = SortType.LATEST_FIRST;
        }
        switch (sortType) {
            case LATEST_FIRST:
                return (o1, o2) -> {
                    int i = -NVersion.of(o1.getVersion()).compareTo(o2.getVersion());
                    if (i != 0) {
                        return i;
                    }
                    i = -Comparator.nullsLast(Instant::compareTo).compare(o1.getReleaseDate(), o2.getReleaseDate());
                    if (i != 0) {
                        return i;
                    }
                    String name1 = (o1 instanceof NetbeansInstallation) ? ((NetbeansInstallation) o1).getName() : null;
                    String name2 = (o2 instanceof NetbeansInstallation) ? ((NetbeansInstallation) o2).getName() : null;
                    i = Comparator.nullsLast(String::compareTo).compare(name1, name2);
                    if (i != 0) {
                        return i;
                    }
                    name1 = (o1 instanceof NetbeansInstallation) ? "1" : "2";
                    name2 = (o2 instanceof NetbeansInstallation) ? "1" : "2";
                    return name1.compareTo(name2);
                };
            case OLDEST_FIRST: {
                return (o1, o2) -> {
                    int i = NVersion.of(o1.getVersion()).compareTo(o2.getVersion());
                    if (i != 0) {
                        return i;
                    }
                    i = Comparator.nullsLast(Instant::compareTo).compare(o1.getReleaseDate(), o2.getReleaseDate());
                    if (i != 0) {
                        return i;
                    }
                    String name1 = (o1 instanceof NetbeansInstallation) ? ((NetbeansInstallation) o1).getName() : null;
                    String name2 = (o2 instanceof NetbeansInstallation) ? ((NetbeansInstallation) o2).getName() : null;
                    i = Comparator.nullsLast(String::compareTo).compare(name1, name2);
                    if (i != 0) {
                        return i;
                    }
                    name1 = (o1 instanceof NetbeansInstallation) ? "1" : "2";
                    name2 = (o2 instanceof NetbeansInstallation) ? "1" : "2";
                    return name1.compareTo(name2);
                };
            }
        }
        throw new IllegalArgumentException("unsupported");
    }

    public NetbeansInstallation[] detectNetbeansInstallations(String path, NetbeansInstallationStore store, int levels) {
        if (path == null) {
            return new NetbeansInstallation[0];
        }
        NetbeansInstallation a = detectNetbeansInstallations(path, store);
        if (a != null) {
            return new NetbeansInstallation[]{a};
        }
        if (levels > 0) {
            List<NetbeansInstallation> subNetbeans = new ArrayList<>();
            File f = NbUtils.resolveFile(path);
            if (f.isDirectory()) {
                File[] pp = f.listFiles(x -> x.isDirectory());
                if (pp != null) {
                    for (File file : pp) {
                        subNetbeans.addAll(Arrays.asList(detectNetbeansInstallations(file.getPath(), store, levels - 1)));
                    }
                }
            }
            return subNetbeans.toArray(new NetbeansInstallation[0]);
        }
        return new NetbeansInstallation[0];
    }

    public NetbeansInstallation detectNetbeansInstallations(String path, NetbeansInstallationStore store) {
        if (path == null) {
            return null;
        }
        File f = NbUtils.resolveFile(path);
        if (!new File(f, NbUtils.toOsPath("bin/" + NbUtils.getNbOsConfig().getNetbeansExe())).exists()) {
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
            netbeans_default_userdir = netbeans_default_userdir.replace("${DEFAULT_USERDIR_ROOT}", NbUtils.getNbOsConfig().getConfigRoot());
        }
        String netbeans_default_cachedir = nbconf.getProperty("netbeans_default_cachedir");
        if (netbeans_default_cachedir != null) {
            netbeans_default_cachedir = netbeans_default_cachedir.replace("${DEFAULT_CACHEDIR_ROOT}", NbUtils.getNbOsConfig().getCacheRoot());
        }
        String netbeans_default_options = nbconf.getProperty("netbeans_default_options");
        if (netbeans_default_options != null) {
            netbeans_default_options = netbeans_default_options.replace("${DEFAULT_CACHEDIR_ROOT}", NbUtils.getNbOsConfig().getCacheRoot());
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

    public static VersionData parseVersionData(String version, Instant releaseDate) {
        if (version == null) {
            version = "";
        }
        version = version.trim();
        Matcher m;
        if (version.startsWith("20160930")) {
            return new VersionData("8.2", version, Instant.parse("2016-09-30T01:01:00Z"));
        } else if ((m = NbStringUtils.match("(incubator-)?netbeans-release-(?<bn>[0-9]+)-on-(?<bd>[0-9]+)", version)) != null) {
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

    public ConfigResult configureDefaultNetbeansInstallations(File baseFolder, NetbeansInstallationStore store) {
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
                NetbeansInstallation o = findNetbeansInstallation(file.getPath());
                if (o == null) {
                    o = detectNetbeansInstallations(file.getPath(), store);
                    if (o != null) {
                        r.setFound(r.getFound() + 1);
                        if (addNetbeansInstallation(o)) {
                            r.setInstalled(r.getInstalled() + 1);
                        }
                    }
                }
            }
        }
        return r;
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
                Arrays.asList(NElementParser.ofJson().parse(getClass().getResource("/net/thevpc/netbeans/launcher/binaries.json"), NetbeansBinaryLink[].class))
        );

        //nuts supports out of the box navigating apache website using htmlfs
        for (NPath p : NPath.of("htmlfs:https://archive.apache.org/dist/netbeans/netbeans/").stream()) {
            if (p.isDirectory()) {
                ///12.0/netbeans-12.0-bin.zip
                String version = p.getName();
                NPath b = NPath.of("https://archive.apache.org/dist/netbeans/netbeans/" + version + "/netbeans-" + version + "-bin.zip");
                if (b.exists()) {
                    all.add(new NetbeansBinaryLink()
                            .setPackaging("zip")
                            .setVersion(version)
                            .setUrl(b.toString())
                            .setReleaseDate(b.lastModifiedInstant())
                    );
                }
            }
        }
        for (NPath p : NPath.of("htmlfs:https://downloads.apache.org/netbeans/netbeans/").stream()) {
            if (p.isDirectory()) {
                ///12.0/netbeans-12.0-bin.zip
                String version = p.getName();
                NPath b = NPath.of("https://downloads.apache.org/netbeans/netbeans/" + version + "/netbeans-" + version + "-bin.zip");
                if (b.exists()) {
                    all.add(new NetbeansBinaryLink()
                            .setPackaging("zip")
                            .setVersion(version)
                            .setUrl(b.toString())
                            .setReleaseDate(b.lastModifiedInstant())
                    );
                }
            }
        }

        Set<String> locallyAvailable = Arrays.stream(findNetbeansInstallations(SortType.LATEST_FIRST)).map(NetbeansInstallation::getVersion).collect(Collectors.toSet());
        return all.stream().filter(x -> !locallyAvailable.contains(x.getVersion()))
                .sorted(comparator(SortType.LATEST_FIRST))
                .toArray(NetbeansBinaryLink[]::new);
    }

    public ConfigResult configureDefaultInstallations() {
        ConfigResult r0 = new ConfigResult();
        for (String programFolder : NbUtils.getNbOsConfig().getProgramFolders()) {
            File level1Folder = NbUtils.resolveFile(programFolder);
            ConfigResult r = configureDefaultNetbeansInstallations(level1Folder, NetbeansInstallationStore.SYSTEM);
            r0.add(r);
            if (r.getFound() > 0) {
                //
            } else {
                //Level 2
                if (level1Folder.isDirectory()) {
                    for (File level2Folder : level1Folder.listFiles()) {
                        r = configureDefaultNetbeansInstallations(level2Folder, NetbeansInstallationStore.SYSTEM);
                        r0.add(r);
                        if (r.getFound() > 0) {
                            //
                        } else {
                            //Level 3
                            if (level2Folder.isDirectory()) {
                                for (File level3Folder : level2Folder.listFiles()) {
                                    r = configureDefaultNetbeansInstallations(level3Folder, NetbeansInstallationStore.SYSTEM);
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

}
