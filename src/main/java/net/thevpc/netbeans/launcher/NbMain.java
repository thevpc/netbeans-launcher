/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.netbeans.launcher;

import net.thevpc.netbeans.launcher.cli.MainWindowCLI;
import net.thevpc.netbeans.launcher.model.NbOptions;
import net.thevpc.netbeans.launcher.ui.MainWindowSwing;
import net.thevpc.netbeans.launcher.util.NbUtils;
import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.env.NPlatformFamily;
import net.thevpc.nuts.io.NPrintStream;

import javax.swing.*;

import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NSupportMode;

/**
 * @author thevpc
 */
public class NbMain implements NApplication {

    String PREFERRED_ALIAS = "nbl";

    public static void main(String[] args) {
        // just create an instance and call runAndExit in the main method
        new NbMain().runAndExit(args);
    }

    @Override
    public void onInstallApplication(NSession session) {
        addDesktopIntegration(session);
    }

    @Override
    public void onUpdateApplication(NSession session) {
        onInstallApplication(session);
    }

    @Override
    public void onUninstallApplication(NSession session) {
        NCommands.of(session).removeCommandIfExists(PREFERRED_ALIAS);
    }

    @Override
    public void run(NSession session) {
        NPrintStream out = session.out();
        NPrintStream err = session.err();
        if (!NbUtils.isPlatformSupported()) {
            err.println("platform not supported");
            if (System.console() == null) {
                JOptionPane.showInputDialog("platform not supported");
            }
            return;
        }
        NbOptions options = new NbOptions();
        NCmdLine cmdLine = session.getAppCmdLine();
        while (cmdLine.hasNext()) {
            if (session.configureFirst(cmdLine)) {
                //do nothing
            } else if (cmdLine.next("--swing") != null) {
                options.plaf = "nimbus";
                options.swing_arg = true;
            } else if (cmdLine.next("--metal") != null) {
                options.plaf = "metal";
                options.swing_arg = true;
            } else if (cmdLine.next("--system") != null) {
                options.plaf = "system";
                options.swing_arg = true;
            } else if (cmdLine.next("--gtk") != null) {
                options.plaf = "gtk";
                options.swing_arg = true;
            } else if (cmdLine.next("--motif") != null) {
                options.plaf = "motif";
                options.swing_arg = true;
            } else if (cmdLine.next("--cli") != null) {
                options.cli = true;
            } else if (cmdLine.next("--version") != null) {
                options.version = true;
            } else if (cmdLine.next("--install") != null) {
                options.install = true;
            } else {
                cmdLine.throwUnexpectedArgument();
            }
        }

        if (options.install) {
            addDesktopIntegration(session);
        }

        if (options.version) {
            out.println(session.getAppId().getVersion());
        } else if (options.cli && !options.swing_arg) {
            MainWindowCLI.launch(session, options);
        } else if (options.swing_arg) {
            MainWindowSwing.launch(session, options, true);
        } else {
            //will default to swing!!
            MainWindowSwing.launch(session, options, true);
        }
    }

    protected void addDesktopIntegration(NSession session) {
        NEnvs.of(session).addLauncher(new NLauncherOptions()
                .setId(session.getAppId())
                .setAlias(PREFERRED_ALIAS)
                .setCreateAlias(true)
                .setCreateMenuLauncher(NSupportMode.PREFERRED)
                .setCreateDesktopLauncher(NSupportMode.PREFERRED)
        );
    }
}
