package net.thevpc.netbeans.launcher.util;

import java.util.Objects;

import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.NutsProcessInfo;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.*;

public class NbProcess implements Comparable<NbProcess> {

    private final String pid;
    private final String className;
    private final String userdir;
    private final String cachedir;

    public NbProcess(NutsSession session, NutsProcessInfo jpsResult) {
        NutsCommandLine cmd = session.commandLine().parse(jpsResult.getCommandLine());
        NutsArgument a;
        pid = jpsResult.getPid();
        className = jpsResult.getName();
        String _userdir = null;
        String _cachedir = null;
        while (cmd.hasNext()) {
            if ((a = cmd.nextString("--userdir")) != null) {
                _userdir = a.getValue().getString();
            } else if ((a = cmd.nextString("--cachedir")) != null) {
                _cachedir = a.getValue().getString();
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

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NbProcess other = (NbProcess) obj;
        if (!Objects.equals(this.pid, other.pid)) {
            return false;
        }
        if (!Objects.equals(this.className, other.className)) {
            return false;
        }
        if (!Objects.equals(this.userdir, other.userdir)) {
            return false;
        }
        if (!Objects.equals(this.cachedir, other.cachedir)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(NbProcess other) {
        int x;
        if ((x = NbUtils.compare(this.pid, other.pid, null)) != 0) {
            return x;
        }
        if ((x = NbUtils.compare(this.className, other.className, null)) != 0) {
            return x;
        }
        if ((x = NbUtils.compare(this.userdir, other.userdir, null)) != 0) {
            return x;
        }
        if ((x = NbUtils.compare(this.cachedir, other.cachedir, null)) != 0) {
            return x;
        }
        return 0;
    }

}
