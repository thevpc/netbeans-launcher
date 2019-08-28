/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.netbeans.launcher;

import net.vpc.app.netbeans.launcher.model.*;
import net.vpc.app.netbeans.launcher.util.NbUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import net.vpc.app.netbeans.launcher.compat.NetbeansConfigLoader11;
import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsExecCommand;
import net.vpc.app.nuts.NutsWorkspace;

/**
 * @author vpc
 */
public class NetbeansConfigService {

    private static final Logger LOG = Logger.getLogger(NetbeansConfigService.class.getName());
    private final NutsApplicationContext appContext;
    private NetbeansConfig config = new NetbeansConfig();
    private File currentDirectory = new File(System.getProperty("user.home"));
    private static String[] prefix = {"Workspace", "WS", "NB", "Netbeans"};
    private static String[] suffix = {" Perso", " Work", " Research", " Fun", " Test", " Release", " Test 1", " Test 2", " A", " B", " C", " D", " E"};
    private List<WritableLongOperation> operations = new ArrayList<>();
    private List<LongOperationListener> operationListeners = new ArrayList<>();

    public NetbeansConfigService(NutsApplicationContext appContext) {
        this.appContext = appContext;
    }

    public File getCurrentDirectory() {
        return currentDirectory;
    }

    public void setCurrentDirectory(File currentDirectory) {
        this.currentDirectory = currentDirectory;
    }

    public void configureDefaultNb(File baseFolder, NetbeansInstallationStore store) {
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
                NetbeansInstallation o = findNb(file.getPath());
                if (o == null) {
                    o = detectNb(file.getPath(), store);
                    if (o != null) {
                        addNb(o);
                    }
                }
            }
        }
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
                JdkInstallation o = findJdk(file.getPath());
                if (o == null) {
                    o = detectJdk(file.getPath());
                    if (o != null) {
                        addJdk(o);
                    }
                }
            }
        }
    }


    public NetbeansBinaryLink[] searchRemoteInstallableNbBinaries() {
        NetbeansBinaryLink[] all = appContext.workspace().json().parse(getClass().getResource("/net/vpc/app/netbeans/launcher/binaries.json"), NetbeansBinaryLink[].class);
        Set<String> locallyAvailable = Arrays.stream(getAllNb()).map(NetbeansInstallation::getVersion).collect(Collectors.toSet());
        return Arrays.stream(all).filter(x -> !locallyAvailable.contains(x.getVersion())).sorted(new Comparator<NetbeansBinaryLink>() {
            @Override
            public int compare(NetbeansBinaryLink o1, NetbeansBinaryLink o2) {
                return -appContext.workspace().version().parse(o1.getVersion())
                        .compareTo(o2.getVersion());
            }
        }).toArray(NetbeansBinaryLink[]::new);
    }

    public void configureDefaults() {
        configureDefaultJdk();
        configureDefaultNb();
        configureDefaultNbWorkspaces();
    }

    public void configureDefaultNbWorkspaces() {
        for (NetbeansInstallation object : config.getInstallations()) {
            addDefaultNbWorkspace(object.getPath(), object.getStore());
        }
    }

    public void configureDefaultNb() {
        for (String programFolder : NbUtils.getNbOsConfig().getProgramFolders()) {
            configureDefaultNb(new File(programFolder), NetbeansInstallationStore.SYSTEM);
        }
    }

    public void configureDefaultJdk() {
        for (String jdkFolder : NbUtils.getNbOsConfig().getJdkFolders()) {
            configureJdk(new File(jdkFolder));
        }
    }

    public JdkInstallation detectJdk(String path) {
        if (path == null) {
            return null;
        }
        File f = NbUtils.resolveFile(path);
        File javaExePath = new File(f, NbUtils.toOsPath("bin/" + NbUtils.getNbOsConfig().getJavaExe()));
        if (!javaExePath.exists()) {
            return null;
        }
        String type = null;
        String jdkVersion = null;
        try {
            String s = NbUtils.response(new String[]{javaExePath.getPath(), "-version"});
            if (s.length() > 0) {
                String prefix = "java version \"";
                int i = s.indexOf(prefix);
                if (i >= 0) {
                    i = i + prefix.length();
                    int j = s.indexOf("\"", i);
                    if (i >= 0) {
                        jdkVersion = s.substring(i, j);
                        type = "JDK";
                    }
                }
                if (jdkVersion == null) {

                    prefix = "openjdk version \"";
                    i = s.indexOf(prefix);
                    if (i >= 0) {
                        i = i + prefix.length();
                        int j = s.indexOf("\"", i);
                        if (i > 0) {
                            jdkVersion = s.substring(i, j);
                            type = "OpenJDK";
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(NetbeansConfigService.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (jdkVersion == null) {
            return null;
        }
        JdkInstallation loc = new JdkInstallation();
        loc.setName(type + " " + jdkVersion);
        loc.setVersion(jdkVersion);
        loc.setPath(f.getPath());
        return loc;
    }

    //    public JdkLocation addJdkLocation(String path, boolean registerNew) {
//        JdkLocation loc = resolveJdkLocation(path);
//        if (registerNew) {
//            addJdkLocation(loc);
//        }
//        return loc;
//    }
    public NetbeansGroup[] detectNbGroups(NetbeansWorkspace w) {
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
                return all.toArray(new NetbeansGroup[all.size()]);
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static final NetbeansGroup NETBEANS_NO_GROUP = new NetbeansGroup("--no-group", "--no-group");
    public static final NetbeansGroup NETBEANS_CLOSE_GROUP = new NetbeansGroup("--close-group", "--close-group");

    public String detectNbVersionFrom_buildinfo_file(String path) {
        File f = NbUtils.resolveFile(path);
        if (!new File(f, NbUtils.toOsPath("nb/build_info")).exists()) {
            return null;
        }
        try (BufferedReader r = new BufferedReader(new FileReader(new File(f, NbUtils.toOsPath("nb/build_info"))))) {
            String line = null;
            String version = null;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0) {
                    if (line.startsWith("#")) {
                        //ignore
                    } else if (line.startsWith("Number:")) {
                        version = line.substring("Number:".length()).trim();
                        return version;
                    }
                }
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
        List<NetbeansInstallation> subNetbeans = new ArrayList<>();
        File f = NbUtils.resolveFile(path);
        if (f.isDirectory()) {
            File[] pp = f.listFiles(x -> x.isDirectory());
            if (pp != null) {
                for (File file : pp) {
                    NetbeansInstallation o = detectNb(file.getPath(), store);
                    if (o != null) {
                        if (autoAdd) {
                            addNb(o);
                        }
                        subNetbeans.add(o);
                    }
                }
            }
        }
        return subNetbeans.toArray(new NetbeansInstallation[0]);
    }

    public NetbeansInstallation detectNb(String path, NetbeansInstallationStore store) {
        if (path == null) {
            return null;
        }
        File f = NbUtils.resolveFile(path);
        if (!new File(f, NbUtils.toOsPath("bin/" + NbUtils.getNbOsConfig().getNetbeansExe())).exists()) {
            return null;
        }
        String version = detectNbVersionFrom_VERSION_file(path);
        if ("9.0".equals(version)) {
            String v2 = detectNbVersionFrom_buildinfo_file(path);
            if (v2 != null) {
                Pattern p = Pattern.compile("(incubator-)?netbeans-release-(?<bn>[0-9]+)-on-(?<bd>[0-9]+)");
                Matcher m = p.matcher(v2.trim());
                if (m.find()) {
                    String bn = m.group("bn");
                    String bd = m.group("bd");
                    int ibn = Integer.parseInt(bn);
                    switch (ibn) {
                        case 334: {
                            version = "9.0";
                            break;
                        }
                        case 380: {
                            version = "10.0";
                            break;
                        }
                        case 404: {
                            version = "11.0";
                            break;
                        }
                        case 428: {
                            version = "11.1";
                            break;
                        }
                        default: {
                            if (ibn < 334) {
                                version = "8.x-" + bn + "-" + bd;
                            } else if (ibn < 380) {
                                version = "9.x-" + bn + "-" + bd;
                            } else if (ibn < 404) {
                                version = "10.x-" + bn + "-" + bd;
                            } else if (ibn < 428) {
                                version = "11.x-" + bn + "-" + bd;
                            } else {
                                version = "11.x-" + bn + "-" + bd;
                            }
                        }
                    }
                }

            }
        }

        if (version != null) {
            NetbeansInstallation netbeansInstallation = new NetbeansInstallation();
            netbeansInstallation.setName("Netbeans IDE " + version);
            netbeansInstallation.setVersion(version);
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
        return null;
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

    public JdkInstallation findJdk(String path) {
        if (path == null) {
            return null;
        }
        for (JdkInstallation loc : config.getJdkLocations()) {
            if (NbUtils.equalsStr(path, loc.getPath())) {
                return loc;
            }
        }
        if (!NbUtils.isPath(path)) {
            for (JdkInstallation loc : config.getJdkLocations()) {
                if (NbUtils.equalsStr(path, loc.getName())) {
                    return loc;
                }
            }
            for (JdkInstallation loc : config.getJdkLocations()) {
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
        config.setSumoMode(b);
        saveFile();
        return true;
    }

    public boolean addJdk(JdkInstallation netbeansInstallation) {
        for (JdkInstallation installation : config.getJdkLocations()) {
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
            if(n1==null){
                n1="";
            }
            if(n2==null){
                n2="";
            }
            return n1.compareTo(n2);
        });
        return w;
    }

    public JdkInstallation[] getAllJdk() {
        List<JdkInstallation> list = config.getJdkLocations();
        list.sort((a, b) -> {
            int i = NbUtils.compareVersions(a.getVersion(), b.getVersion());
            if (i != 0) {
                return i;
            }
            return a.getName().compareTo(b.getName());
        });
        return list.toArray(new JdkInstallation[0]);
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
                throw new NoSuchElementException("Invalid Netbeans installation directory " + path);
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

    public JdkInstallation getJdk(String path) {
        if (path == null) {
            return null;
        }
        JdkInstallation o = findJdk(path);
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
        JdkInstallation o = findJdk(path);
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

    public NutsExecCommand run(NetbeansWorkspace w) throws IOException {
        String[] cmd = createRunCommand(w);
        return appContext.workspace().exec()
                .syscall()
                .directory(w.getPath())
                .command(cmd)
                .redirectErrorStream()
                .grabOutputString()
                .failFast()
                .run();
    }

    public void saveFile() {
        appContext.workspace().json().value(config).print(appContext.getConfigFolder().resolve("config.json"));
    }

    public void loadFile() {
        Path oldFile = Paths.get(System.getProperty("user.home")).resolve(".java/apps/net/vpc/toolbox/netbeans-launcher.json");
        appContext.getConfigFolder().resolve("config.json");
        Path validFile = appContext.getConfigFolder().resolve("config.json");
        if (!Files.isRegularFile(validFile) && Files.isRegularFile(oldFile)) {
            try {
                config = NetbeansConfigLoader11.load(oldFile, appContext.workspace());
                saveFile();
            } catch (Exception e) {
                System.err.println("Unable to load config from " + oldFile);
                LOG.log(Level.SEVERE, "Unable to load config from " + oldFile, e);
                config = new NetbeansConfig();
            }
        } else if (Files.isRegularFile(validFile)) {
            try {
                config = (NetbeansConfig) appContext.workspace().json().parse(validFile, NetbeansConfig.class);
            } catch (Exception e) {
                System.err.println("Unable to load config from " + validFile.toString());
                LOG.log(Level.SEVERE, "Unable to load config from " + oldFile, e);
                int i = 2;
                while (true) {
                    Path f2 = Paths.get(validFile.toString() + "." + i + ".save");
                    if (!Files.exists(f2)) {
                        try {
                            Files.move(validFile, f2);
                        } catch (IOException ex) {
                            Logger.getLogger(NetbeansConfigService.class.getName()).log(Level.SEVERE, null, ex);
                            throw new UncheckedIOException(ex);
                        }
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
        } else {
            if (config == null) {
                config = new NetbeansConfig();
            }
        }
        if (config.getInstallations().isEmpty()) {
            configureDefaults();
            saveFile();
        }
    }

    public void load() {
        loadFile();
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

    public String[] getUserdirProposals(NetbeansWorkspace w) {
        String n = NbUtils.trim(w.getName());
        if (NbUtils.isEmpty(n)) {
            n = "noname";
        }
        String configRoot = NbUtils.getNbOsConfig().getConfigRoot();
        List<String> all = new ArrayList<>();
        for (String extra : new String[]{"", " 1", " 2", " 3", " 4", " 5"}) {
            all.add(NbUtils.toOsPath(configRoot + File.separatorChar + n + extra));
        }
        return all.toArray(new String[all.size()]);
    }

    public String[] getCachedirProposals(NetbeansWorkspace w) {
        String n = NbUtils.trim(w.getName());
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

    public boolean isSumoMode() {
        return config.isSumoMode();
    }

    public void installNetbeansBinary(NetbeansBinaryLink i) {
        WritableLongOperation op = addOperation("Installing " + i.toString());
        op.setDescription("Downloading " + i.toString());
        op.start(true);
        NutsWorkspace ws = appContext.workspace();
        Path zipTo = appContext.getSharedAppsFolder()
                .resolve("netbeans")
                .resolve("netbeans-" + i.getVersion() + ".zip");
        Path folderTo = appContext.getSharedAppsFolder()
                .resolve("netbeans")
                .resolve("netbeans-" + i.getVersion());
        if (!Files.exists(zipTo)) {
            ws.io().copy().from(i.getUrl()).to(zipTo).monitorable().run();
        }
        op.setDescription("Installing " + i.toString());
        op.inc(1);
        try {
            Files.createDirectories(folderTo);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        File running = new File(zipTo.toString() + ".unpacking");
        if (running.exists()) {
            throw new RuntimeException("Lock File exits " + running.getPath());
        }
        if (Files.exists(folderTo.resolve("bin").resolve("netbeans"))) {
            //ok !
        } else {
            try {
                new FileOutputStream(running).close();
                NbUtils.unzip(zipTo.toString(), folderTo.toString(), new NbUtils.UnzipOptions().setSkipRoot(true));
                running.delete();
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        NetbeansInstallation o = detectNb(folderTo.toString(), NetbeansInstallationStore.DEFAULT);
        if (o != null) {
            switch (appContext.getWorkspace().config().getOsFamily()) {
                case LINUX:
                case UNIX:
                case MACOS: {
                    Path n = Paths.get(o.getPath()).resolve("bin").resolve("netbeans");
                    n.toFile().setExecutable(true);
                    break;
                }
            }
            addNb(o);
        }
        op.setDescription("Installing " + i.toString());
        op.end();
    }

    void fire(WritableLongOperation w) {
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


}
