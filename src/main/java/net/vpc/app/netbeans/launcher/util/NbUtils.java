/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.netbeans.launcher.util;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JComponent;
import javax.swing.JFrame;
import net.vpc.app.netbeans.launcher.model.NbOsConfig;

/**
 *
 * @author vpc
 */
public class NbUtils {
    private static final Logger LOG = Logger.getLogger(NbUtils.class.getName());

    public static final NbOsConfig LINUX_CONFIG = new NbOsConfig(
            new String[]{
                "/usr/local",
                "~/bin",
                "~/programs",
                "~/Programs",},
            new String[]{
                "/usr/java",
                "/usr/lib64/jvm",
                "/usr/lib/jvm"
            },
            "~/.netbeans",
            "~/.cache/netbeans",
            "netbeans",
            "java"
    );
    public static final NbOsConfig WINDOWS_CONFIG = new NbOsConfig(
            new String[]{
                NbUtils.coalesce(System.getenv("ProgramFiles"), "C:\\Program Files"),
                NbUtils.coalesce(System.getenv("ProgramFiles(x86)"), "C:\\Program Files (x86)"),
                "~/programs",
                "~/Programs",},
            new String[]{
                NbUtils.coalesce(System.getenv("ProgramFiles"), "C:\\Program Files") + "\\Java",
                NbUtils.coalesce(System.getenv("ProgramFiles(x86)"), "C:\\Program Files (x86)") + "\\Java",},
            "~\\AppData\\Roaming/Netbeans",
            "~\\AppData\\Local\\Netbeans\\Cache",
            "netbeans.exe",
            "java.exe"
    );
    public static final NbOsConfig MAC_CONFIG = new NbOsConfig(
            new String[]{
                "/Library/",
                "~/programs",
                "~/Programs",},
            new String[]{
                "/Library/Java/JavaVirtualMachines",
                "/System/Library/Frameworks/JavaVM.framework"
            },
            "~/Library/Application Support/",
            "~/Library/Caches/NetBeans",
            "netbeans",
            "java"
    );

//    public static void main(String[] args) {
//        try {
////            String s = response(new String[]{"/usr/java/jdk1.5.0_22/bin/java", "-version"});
//            String s = response(new String[]{"/usr/java/jdk-10s/bin/java", "-version"});
//            System.out.println(s);
//        } catch (IOException ex) {
//            Logger.getLogger(NbUtils.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
    public static boolean isPlatformSupported() {
        return true;//isOsWindows() || isOsLinux() || isOsMac();
    }

    public static boolean isOsWindows() {
        String osname = System.getProperty("os.name");
        return osname.toLowerCase().startsWith("win");
    }

    public static boolean isOsLinux() {
        String osname = System.getProperty("os.name");
        return osname.toLowerCase().startsWith("linux");
    }

    public static boolean isOsMac() {
        String osname = System.getProperty("os.name");
        return osname.toLowerCase().startsWith("mac");
    }

    public static String trim(String cmd) {
        return cmd == null ? "" : cmd.trim();
    }

    public static String coalesce(String... cmd) {
        for (String string : cmd) {
            if (!isEmpty(string)) {
                return string;
            }
        }
        return null;
    }

    public static boolean isEmpty(String cmd) {
        return cmd == null || cmd.trim().isEmpty();
    }

    public static String response(List<String> cmd) throws IOException {
        return response(cmd.toArray(new String[cmd.size()]));
    }

    public static String response(String[] cmd) throws IOException {
//        for (String string : cmd) {
//            System.out.print(" \"" + string + "\"");
//        }
//        System.out.println();
        ProcessBuilder b = new ProcessBuilder(cmd);
        b.redirectErrorStream(true);
        Process proc = b.start();
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(NbUtils.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (proc.isAlive()) {
                    proc.destroy();
                }
            }
        }.start();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[512];
        int r = 0;
        InputStream outTream = proc.getInputStream();
        while ((r = outTream.read(buffer)) > 0) {
            out.write(buffer, 0, r);
        }
        return new String(out.toByteArray());
    }

    public static boolean equalsStr(String s1, String s2) {
        if (s1 == null) {
            s1 = "";
        }
        if (s2 == null) {
            s2 = "";
        }
        return s1.equals(s2);
    }

    public static String getArtifactVersionOrDev() {
        String groupId = "net.vpc.app";
        String artifactId = "netbeans-launcher";
        return getArtifactVersionOrDev(groupId, artifactId);
    }

    public static Properties loadProperties(File file) {
        Properties p = new Properties();
        if (file.isFile()) {
            try (FileInputStream is = new FileInputStream(file)) {
                p.load(is);
            } catch (IOException e) {
                //
            }
        }
        return p;
    }

    public static String getArtifactVersionOrDev(String groupId, String artifactId) {
        URL url = Thread.currentThread().getContextClassLoader().getResource("META-INF/maven/" + groupId + "/" + artifactId + "/pom.properties");

        if (url != null) {
            Properties p = new Properties();
            try {
                p.load(url.openStream());
            } catch (IOException e) {
                //
            }
            String version = p.getProperty("version");
            if (!isEmpty(version)) {
                return version;
            }
        }
        return "DEV";
    }

    public static String toOsPath(String s) {
        StringBuilder sb = new StringBuilder();
        boolean wasSlash = false;
        for (char c : s.toCharArray()) {
            if ((c == '\\' || c == '/') && c != File.separatorChar) {
                c = File.separatorChar;
            }
            if (c == File.separatorChar) {
                if (!wasSlash) {
                    sb.append(c);
                }
                wasSlash = true;
            } else {
                wasSlash = false;
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static final NbOsConfig getNbOsConfig() {
        if (NbUtils.isOsLinux()) {
            return NbUtils.LINUX_CONFIG;
        }
        if (NbUtils.isOsWindows()) {
            return NbUtils.WINDOWS_CONFIG;
        }
        if (NbUtils.isOsMac()) {
            return NbUtils.MAC_CONFIG;
        }
        // all others are supposed to be unixes!!
        return NbUtils.LINUX_CONFIG;
        //throw new IllegalArgumentException("Unsupported OS " + System.getProperty("os.name"));
    }

    public static File resolveFile(String path) {
        String p2 = path.replace('\\', '/');
        if (p2.equals("~")) {
            return new File(System.getProperty("user.home"));
        }
        if (p2.equals("~/")) {
            return new File(System.getProperty("user.home"));
        }
        if (p2.startsWith("~/")) {
            return new File(System.getProperty("user.home"), toOsPath(path.substring(2)));
        }
        return new File(path);
    }

    public static boolean isPath(String path) {
        if (path == null) {
            return false;
        }
        if (path.indexOf('/') >= 0) {
            return true;
        }
        if (path.indexOf('\\') >= 0) {
            return true;
        }
        if (path.equals('~')) {
            return true;
        }
        if (path.startsWith("~/")) {
            return true;
        }
        if (path.startsWith("~\\")) {
            return true;
        }
        return false;
    }

    public static int compareVersions(String v1, String v2) {
        v1 = trim(v1);
        v2 = trim(v2);
        if (v1.equals(v2)) {
            return 0;
        }
        if ("LATEST".equals(v1)) {
            return 1;
        }
        if ("LATEST".equals(v2)) {
            return -1;
        }
        if ("RELEASE".equals(v1)) {
            return 1;
        }
        if ("RELEASE".equals(v2)) {
            return -1;
        }
        String[] v1arr = splitVersionParts(v1);
        String[] v2arr = splitVersionParts(v2);
        for (int i = 0; i < Math.max(v1arr.length, v2arr.length); i++) {
            if (i >= v1arr.length) {
                if (v2arr[i].equalsIgnoreCase("SNAPSHOT")) {
                    return 1;
                }
                return -1;
            }
            if (i >= v2arr.length) {
                if (v1arr[i].equalsIgnoreCase("SNAPSHOT")) {
                    return -1;
                }
                return 1;
            }
            int x = compareVersionItem(v1arr[i], v2arr[i]);
            if (x != 0) {
                return x;
            }
        }
        return 0;
    }

    private static String[] splitVersionParts(String v1) {
        v1 = trim(v1);
        List<String> parts = new ArrayList<>();
        StringBuilder last = new StringBuilder();
        for (char c : v1.toCharArray()) {
            if (last.length() == 0) {
                last.append(c);
            } else if (Character.isDigit(last.charAt(0)) == Character.isDigit(c)) {
                last.append(c);
            } else {
                parts.add(last.toString());
                last.delete(0, last.length());
            }
        }
        if (last.length() > 0) {
            parts.add(last.toString());
        }
        return parts.toArray(new String[0]);
    }

    private static int compareVersionItem(String v1, String v2) {
        Integer i1 = null;
        Integer i2 = null;

        if (v1.equals(v2)) {
            return 0;
        } else if ((i1 = toInteger(v1)) != null && (i2 = toInteger(v2)) != null) {
            return i1 - i2;
        } else if ("SNAPSHOT".equalsIgnoreCase(v1)) {
            return -1;
        } else if ("SNAPSHOT".equalsIgnoreCase(v2)) {
            return 1;
        } else {
            int a = getStartingInt(v1);
            int b = getStartingInt(v2);
            if (a != -1 && b != -1 && a != b) {
                return a - b;
            } else {
                return v1.compareTo(v2);
            }
        }
    }

    public static Integer toInteger(String v1) {
        try {
            return Integer.parseInt(v1);
        } catch (Exception ex) {
            return null;
        }
    }

    public static int getStartingInt(String v1) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < v1.length(); i++) {
            char c = v1.charAt(i);
            if (c >= '0' && c <= '9') {
                sb.append(c);
            }
        }
        if (sb.length() > 0) {
            return Integer.parseInt(sb.toString());
        }
        return -1;
    }

    private static String commandLineItemToString(String arg) {
        arg = String.valueOf(arg);
        int h1 = arg.indexOf(' ');
        int h2 = arg.indexOf('\"');
        int h3 = arg.indexOf('\'');
        if (h1 >= 0 || h2 >= 0 || h3 >= 0) {
            arg = arg.replace("\"", "\\\"");
            return "\"" + arg + "\"";
        }
        return arg;
    }

    public static void installMoveWin(JComponent c, JFrame parent) {
        c.setCursor(new Cursor(Cursor.HAND_CURSOR));
        c.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                c.putClientProperty("initialClick", e.getPoint());
                c.getComponentAt((Point) c.getClientProperty("initialClick"));
            }
        });

        c.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point initialClick = (Point) c.getClientProperty("initialClick");
                // get location of Window
                int thisX = parent.getLocation().x;
                int thisY = parent.getLocation().y;

                // Determine how much the mouse moved since the initial click
                int xMoved = e.getX() - initialClick.x;
                int yMoved = e.getY() - initialClick.y;

                // Move window to this position
                int X = thisX + xMoved;
                int Y = thisY + yMoved;
                parent.setLocation(X, Y);
            }
        });
    }

    public static String commandLineToString(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            String arg = commandLineItemToString(args[i]);
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(arg);
        }
        return sb.toString();
    }
    /**
     * Unzip it
     *
     * @param zipFile input zip file
     * @param outputFolder zip file output folder
     */
    public static void unzip(String zipFile, String outputFolder, UnzipOptions options) throws IOException {
        if (options == null) {
            options = new UnzipOptions();
        }
        byte[] buffer = new byte[1024];

        //create output directory is not exists
        File folder = new File(outputFolder);
        if (!folder.exists()) {
            folder.mkdir();
        }

        //get the zip file content
        try(ZipInputStream zis
                    = new ZipInputStream(new FileInputStream(new File(zipFile)))) {
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();
            String root = null;
            while (ze != null) {

                String fileName = ze.getName();
                if (options.isSkipRoot()) {
                    if (root == null) {
                        if (fileName.endsWith("/")) {
                            root = fileName;
                            ze = zis.getNextEntry();
                            continue;
                        } else {
                            throw new IOException("tot a single root zip");
                        }
                    }
                    if (fileName.startsWith(root)) {
                        fileName = fileName.substring(root.length());
                    } else {
                        throw new IOException("tot a single root zip");
                    }
                }
                if (fileName.endsWith("/")) {
                    File newFile = new File(outputFolder + File.separator + fileName);
                    newFile.mkdirs();
                } else {
                    File newFile = new File(outputFolder + File.separator + fileName);
                    LOG.log(Level.FINEST, "file unzip : " + newFile.getAbsoluteFile());
                    //create all non exists folders
                    //else you will hit FileNotFoundException for compressed folder
                    newFile.getParentFile().mkdirs();

                    FileOutputStream fos = new FileOutputStream(newFile);

                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }

                    fos.close();
                }
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
        }
    }


    public static class UnzipOptions {

        private boolean skipRoot = false;

        public UnzipOptions() {
        }

        public boolean isSkipRoot() {
            return skipRoot;
        }

        public UnzipOptions setSkipRoot(boolean skipRoot) {
            this.skipRoot = skipRoot;
            return this;
        }

    }

}
