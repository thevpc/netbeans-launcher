package net.thevpc.netbeans.launcher.ui.utils;

import net.thevpc.netbeans.launcher.util.JlistToStringer;
import net.thevpc.nuts.NutsPlatformLocation;

public class JdkJlistToStringer extends JlistToStringer {
    public JdkJlistToStringer() {
        super(2);
    }

    @Override
    public String toString(Object value, int level) {
        if (value instanceof NutsPlatformLocation) {
            NutsPlatformLocation i = (NutsPlatformLocation) value;
            switch (level) {
                case 0: {
                    return i.getName();
                }
                case 1: {
                    return i.getName() + " (" + i.getPath() + ")";
                }
            }
        }
        return String.valueOf(value);
    }
}
