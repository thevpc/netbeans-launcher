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
    private final NSession session;
    private final NbOptions options;

    public static void launch(NSession appContext, NbOptions options) {
        MainWindowCLI cli = new MainWindowCLI(appContext, options);
        cli.run();
    }

    private void run() {
        session.out().println("CLI mode is not yet supported. Ignoring command");
    }

    public MainWindowCLI(NSession session, NbOptions options) {
        this.configService = new NetbeansLauncherModule(session);
        this.session = session;
        this.options = options;
    }


}
