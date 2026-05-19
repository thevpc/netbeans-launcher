package net.thevpc.netbeans.launcher.ui.utils;

import net.thevpc.netbeans.launcher.util.JlistToStringer;
import net.thevpc.nuts.platform.NExecutionEngineLocation;

public class JdkJlistToStringer extends JlistToStringer {
    public JdkJlistToStringer() {
        super(2);
    }

    @Override
    public String toString(Object value, int level) {
        if (value instanceof NExecutionEngineLocation) {
            NExecutionEngineLocation i = (NExecutionEngineLocation) value;
            switch (level) {
                case 0: {
                    return i.name();
                }
                case 1: {
                    return i.name() + " (" + i.path() + ")";
                }
            }
        }
        return String.valueOf(value);
    }
}
