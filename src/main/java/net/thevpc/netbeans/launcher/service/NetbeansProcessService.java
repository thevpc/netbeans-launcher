/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.netbeans.launcher.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.thevpc.netbeans.launcher.model.NetbeansWorkspace;
import net.thevpc.netbeans.launcher.util.NbUtils;
import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.command.NExecutionType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NBlankable;

/**
 *
 * @author vpc
 */
public class NetbeansProcessService {

    private final NetbeansLauncherModule module;

    public NetbeansProcessService(NetbeansLauncherModule module) {
        this.module = module;
    }

    public String[] createNetbeansRunCommand(NetbeansWorkspace w) {
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

    public NExec launchNetbeans(NetbeansWorkspace w) throws IOException {
        String[] cmd = createNetbeansRunCommand(w);
        return NExec.of()
                .setExecutionType(NExecutionType.SYSTEM)
                .setDirectory(
                        NBlankable.isBlank(w.getPath()) ? null
                        : NPath.of(w.getPath())
                )
                .addCommand(cmd)
                .redirectErr()
                .setFailFast(true)
                .run();
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

}
