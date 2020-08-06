/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.netbeans.launcher;

import javax.swing.JOptionPane;

import net.vpc.app.netbeans.launcher.ui.MainWindowSwing;
import net.vpc.app.netbeans.launcher.util.NbUtils;
import net.vpc.app.nuts.*;

import java.io.PrintStream;

/**
 *
 * @author vpc
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
        NutsWorkspaceCommandAlias a = ws.config().findCommandAlias(preferredAlias, session);
        if (a != null && a.getCommand() != null && a.getCommand().length > 0) {
            NutsId i = ws.id().parse(a.getCommand()[0]);
            if (i != null
                    && (i.getShortName().equals(appId.getArtifactId())
                    || (i.getShortName().equals(appId.getShortName())))
                    && a.getOwner() != null && a.getOwner().getShortName().equals(appId.getShortName())) {
                return a;
            }
        }
        return null;
    }

    @Override
    protected void onUninstallApplication(NutsApplicationContext applicationContext) {
        String preferredAlias = "nb";
        NutsWorkspace ws = applicationContext.getWorkspace();
        NutsSession session = applicationContext.getSession();
        NutsWorkspaceCommandAlias a = findDefaultAlias(applicationContext);
        if (a != null) {
            ws.config().removeCommandAlias(preferredAlias, new NutsRemoveOptions().setSession(session));
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
        } else if (ws.config().findCommandAlias(preferredAlias, session) == null) {
            add = true;
        }
        if (update || add) {
            ws.config().addCommandAlias(new NutsCommandAliasConfig()
                    .setName(preferredAlias)
                    .setOwner(applicationContext.getAppId())
                    .setCommand(applicationContext.getAppId().getShortName()),
                    new NutsAddOptions().setSession(update ? session.copy().setYes(true) : session));
        }
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        PrintStream out = appContext.getSession().out();
        PrintStream err = appContext.getSession().err();
        if (!NbUtils.isPlatformSupported()) {
            err.println("Platform not supported");
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
            net.vpc.app.netbeans.launcher.cli.MainWindowCLI.launch(appContext, options);
        } else if (options.swing_arg) {
            MainWindowSwing.launch(appContext, options, true);
        } else {
            //will default to swing!!
            MainWindowSwing.launch(appContext, options, true);
        }
    }
}
