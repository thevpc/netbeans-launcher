/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.netbeans.launcher.cli;

import net.thevpc.netbeans.launcher.NetbeansConfigService;
import net.thevpc.netbeans.launcher.NbOptions;
import net.thevpc.nuts.NApplicationContext;

/**
 * @author thevpc
 */
public class MainWindowCLI {
    private final NetbeansConfigService configService;
    private final NApplicationContext appContext;
    private final NbOptions options;

    public static void launch(NApplicationContext appContext, NbOptions options) {
        MainWindowCLI cli = new MainWindowCLI(appContext, options);
        cli.run();
    }

    private void run() {
        appContext.getSession().out().println("CLI mode is not yet supported. Ignoring command");
    }

    public MainWindowCLI(NApplicationContext appContext, NbOptions options) {
        this.configService = new NetbeansConfigService(appContext);
        this.appContext = appContext;
        this.options = options;
    }


}
