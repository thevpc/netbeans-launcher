package net.vpc.app.netbeans.launcher.util;

import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.common.io.JpsResult;

public class NbProcess {

    private final String pid;
    private final String className;
    private final String userdir;
    private final String cachedir;

    public NbProcess(NutsApplicationContext ctx, JpsResult jpsResult) {
        NutsCommandLine cmd = ctx.commandLine().parseLine(jpsResult.getArgsLine());
        NutsArgument a;
        pid = jpsResult.getPid();
        className = jpsResult.getClassName();
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
