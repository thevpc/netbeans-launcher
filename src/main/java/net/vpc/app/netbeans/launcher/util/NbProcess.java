package net.vpc.app.netbeans.launcher.util;

import net.vpc.app.nuts.*;

public class NbProcess {

    private final String pid;
    private final String className;
    private final String userdir;
    private final String cachedir;

    public NbProcess(NutsWorkspace ws, NutsProcessInfo jpsResult) {
        NutsCommandLine cmd = ws.commandLine().parse(jpsResult.getCommandLine());
        NutsArgument a;
        pid = jpsResult.getPid();
        className = jpsResult.getName();
        String _userdir = null;
        String _cachedir = null;
        while (cmd.hasNext()) {
            if ((a = cmd.nextString("--userdir")) != null) {
                _userdir = a.getStringValue();
            } else if ((a = cmd.nextString("--cachedir")) != null) {
                _cachedir = a.getStringValue();
            } else {
                cmd.skip();
            }
        }
        userdir = _userdir;
        cachedir = _cachedir;
    }

    public String getPid() {
        return pid;
    }

    public String getClassName() {
        return className;
    }

    public String getUserdir() {
        return userdir;
    }

    public String getCachedir() {
        return cachedir;
    }
}
