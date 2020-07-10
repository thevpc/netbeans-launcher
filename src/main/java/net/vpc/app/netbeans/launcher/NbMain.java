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

    @Override
    public void run(NutsApplicationContext appContext) {
        PrintStream out = appContext.session().out();
        PrintStream err = appContext.session().err();
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
            } else if (cmdLine.next("--swing")!=null) {
                options.plaf = "nimbus";
                options.swing_arg = true;
            } else if (cmdLine.next("--metal")!=null) {
                options.plaf = "metal";
                options.swing_arg = true;
            } else if (cmdLine.next("--system")!=null) {
                options.plaf = "system";
                options.swing_arg = true;
            } else if (cmdLine.next("--gtk")!=null) {
                options.plaf = "gtk";
                options.swing_arg = true;
            } else if (cmdLine.next("--motif")!=null) {
                options.plaf = "motif";
                options.swing_arg = true;
            } else if (cmdLine.next("--cli")!=null) {
                options.cli = true;
            } else if (cmdLine.next("--version")!=null) {
                options.version = true;
            }else{
                cmdLine.unexpectedArgument();
            }
        }
        if (options.version) {
            out.println(appContext.appId().getVersion());
        }else if (options.cli && !options.swing_arg) {
            net.vpc.app.netbeans.launcher.cli.MainWindowCLI.launch(appContext,options);
        } else if (options.swing_arg) {
            MainWindowSwing.launch(appContext,options,true);
        } else {
            //will default to swing!!
            MainWindowSwing.launch(appContext,options,true);
        }
    }
}
