/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.netbeans.launcher;

import javax.swing.JOptionPane;
import net.vpc.app.netbeans.launcher.util.NbUtils;
import net.vpc.app.nuts.NutsApplication;
import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsCommandLine;

/**
 *
 * @author vpc
 */
public class NbMain extends NutsApplication {

    public static void main(String[] args) {
        // just create an instance and call runAndExit in the main method
        new NbMain().run(args);
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        System.out.println("Netbeans Launcher " + NbUtils.getArtifactVersionOrDev());
        if (!NbUtils.isPlatformSupported()) {
            System.err.println("Platform not supported");
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
            } else if (cmdLine.accept("--swing")) {
                options.plaf = "nimbus";
                options.swing_arg = true;
            } else if (cmdLine.accept("--metal")) {
                options.plaf = "metal";
                options.swing_arg = true;
            } else if (cmdLine.accept("--system")) {
                options.plaf = "system";
                options.swing_arg = true;
            } else if (cmdLine.accept("--gtk")) {
                options.plaf = "gtk";
                options.swing_arg = true;
            } else if (cmdLine.accept("--motif")) {
                options.plaf = "motif";
                options.swing_arg = true;
            } else if (cmdLine.accept("--cli")) {
                options.cli = true;
            }
        }
        if (options.cli && !options.swing_arg) {
            net.vpc.app.netbeans.launcher.ui.cli.MainWindowCLI.main0(new String[0]);
        } else if (options.swing_arg) {
            net.vpc.app.netbeans.launcher.ui.swing.MainWindowSwing.launch(appContext,options);
        } else {
            //will default to swing!!
            net.vpc.app.netbeans.launcher.ui.swing.MainWindowSwing.launch(appContext,options);
        }
    }
}
