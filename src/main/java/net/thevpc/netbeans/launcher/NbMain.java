/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.netbeans.launcher;

import javax.swing.JOptionPane;

import net.thevpc.netbeans.launcher.cli.MainWindowCLI;
import net.thevpc.netbeans.launcher.ui.MainWindowSwing;
import net.thevpc.netbeans.launcher.util.NbUtils;
import net.thevpc.nuts.*;
import net.thevpc.nuts.*;

import java.io.PrintStream;

/**
 *
 * @author thevpc
 */
public class NbMain extends NutsApplication {

    public static void main(String[] args) {
        // just create an instance and call runAndExit in the main method
        new NbMain().runAndExit(args);
    }

    private NutsWorkspaceCommandAlias findDefaultAlias(NutsApplicationContext applicationContext) {
        String preferredAlias = "nb";
        NutsWorkspace ws = applicationContext.getWorkspace();
        NutsSession session = applicationContext.getSession();
        NutsId appId = applicationContext.getAppId();
        return ws.aliases().find(preferredAlias, appId,appId,session);
    }

    @Override
    protected void onUninstallApplication(NutsApplicationContext applicationContext) {
        String preferredAlias = "nb";
        NutsWorkspace ws = applicationContext.getWorkspace();
        NutsSession session = applicationContext.getSession();
        NutsWorkspaceCommandAlias a = findDefaultAlias(applicationContext);
        if (a != null) {
            ws.aliases().remove(preferredAlias, new NutsRemoveOptions().setSession(session));
        }
    }

    @Override
    protected void onUpdateApplication(NutsApplicationContext applicationContext) {
        onInstallApplication(applicationContext);
    }

    @Override
    protected void onInstallApplication(NutsApplicationContext applicationContext) {
        String preferredAlias = "nb";
        NutsWorkspace ws = applicationContext.getWorkspace();
        NutsSession session = applicationContext.getSession();
        NutsWorkspaceCommandAlias a = findDefaultAlias(applicationContext);
        boolean update = false;
        boolean add = false;
        if (a != null) {
            update = true;
        } else if (ws.aliases().find(preferredAlias, session) == null) {
            add = true;
        }
        if (update || add) {
            ws.aliases().add(new NutsCommandAliasConfig()
                    .setName(preferredAlias)
                    .setOwner(applicationContext.getAppId())
                    .setCommand(applicationContext.getAppId().getShortName()),
                    new NutsAddOptions().setSession(update ? session.copy().setConfirm(NutsConfirmationMode.YES) : session));
        }
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        PrintStream out = appContext.getSession().out();
        PrintStream err = appContext.getSession().err();
        if (!NbUtils.isPlatformSupported()) {
            err.println("platform not supported");
            if (System.console() == null) {
                JOptionPane.showInputDialog("Platform not supported");
            }
            return;
        }
        NbOptions options = new NbOptions();
        NutsCommandLine cmdLine = appContext.getCommandLine();
        while (cmdLine.hasNext()) {
            if (appContext.configureFirst(cmdLine)) {
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
            } else {
                cmdLine.unexpectedArgument();
            }
        }
        if (options.version) {
            out.println(appContext.getAppId().getVersion());
        } else if (options.cli && !options.swing_arg) {
            MainWindowCLI.launch(appContext, options);
        } else if (options.swing_arg) {
            MainWindowSwing.launch(appContext, options, true);
        } else {
            //will default to swing!!
            MainWindowSwing.launch(appContext, options, true);
        }
    }
}
