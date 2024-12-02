/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.netbeans.launcher.cli;

import net.thevpc.netbeans.launcher.service.NetbeansLauncherModule;
import net.thevpc.netbeans.launcher.model.NbOptions;
import net.thevpc.nuts.NSession;

/**
 * @author thevpc
 */
public class MainWindowCLI {
    private final NetbeansLauncherModule configService;
    private final NbOptions options;

    public static void launch(NbOptions options) {
        MainWindowCLI cli = new MainWindowCLI(options);
        cli.run();
    }

    private void run() {
        NSession.of().out().println("CLI mode is not yet supported. Ignoring command");
    }

    public MainWindowCLI(NbOptions options) {
        this.configService = new NetbeansLauncherModule();
        this.options = options;
    }


}
