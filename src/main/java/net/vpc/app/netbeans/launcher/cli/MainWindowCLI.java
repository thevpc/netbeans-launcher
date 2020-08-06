/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.netbeans.launcher.cli;

import net.vpc.app.netbeans.launcher.NbOptions;
import net.vpc.app.netbeans.launcher.NetbeansConfigService;
import net.vpc.app.nuts.NutsApplicationContext;

/**
 * @author vpc
 */
public class MainWindowCLI {
    private final NetbeansConfigService configService;
    private final NutsApplicationContext appContext;
    private final NbOptions options;

    public static void launch(NutsApplicationContext appContext, NbOptions options) {
        MainWindowCLI cli = new MainWindowCLI(appContext, options);
        cli.run();
    }

    private void run() {
        appContext.getSession().out().println("CLI mode is not yet supported. Ignoring command");
    }

    public MainWindowCLI(NutsApplicationContext appContext, NbOptions options) {
        this.configService = new NetbeansConfigService(appContext);
        this.appContext = appContext;
        this.options = options;
    }


}
