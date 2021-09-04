/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.netbeans.launcher;

import net.thevpc.netbeans.launcher.cli.MainWindowCLI;
import net.thevpc.netbeans.launcher.ui.MainWindowSwing;
import net.thevpc.netbeans.launcher.util.NbUtils;
import net.thevpc.nuts.*;

import javax.swing.*;

/**
 * @author thevpc
 */
public class NbMain implements NutsApplication {

    String PREFERRED_ALIAS = "nbl";

    public static void main(String[] args) {
        // just create an instance and call runAndExit in the main method
        new NbMain().runAndExit(args);
    }

    @Override
    public void onInstallApplication(NutsApplicationContext applicationContext) {
        addDesktopIntegration(applicationContext);
    }

    @Override
    public void onUpdateApplication(NutsApplicationContext applicationContext) {
        onInstallApplication(applicationContext);
    }

    @Override
    public void onUninstallApplication(NutsApplicationContext applicationContext) {
        applicationContext.getWorkspace().commands().removeCommandIfExists(PREFERRED_ALIAS);
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        NutsPrintStream out = appContext.getSession().out();
        NutsPrintStream err = appContext.getSession().err();
        if (!NbUtils.isPlatformSupported()) {
            err.println("platform not supported");
            if (System.console() == null) {
                JOptionPane.showInputDialog("platform not supported");
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
            } else if (cmdLine.next("--install") != null) {
                options.install = true;
            } else {
                cmdLine.unexpectedArgument();
            }
        }

        if (options.install) {
            addDesktopIntegration(appContext);
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

    protected void addDesktopIntegration(NutsApplicationContext applicationContext) {
        NutsWorkspace ws = applicationContext.getWorkspace();
        ws.env().addLauncher(new NutsLauncherOptions()
                .setId(applicationContext.getAppId())
                .setAlias(PREFERRED_ALIAS)
                .setCreateAlias(true)
                .setCreateMenuShortcut(NutsSupportCondition.PREFERRED)
                .setCreateDesktopShortcut(NutsSupportCondition.PREFERRED)
        );
    }
}
