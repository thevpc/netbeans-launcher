package net.vpc.app.netbeans.launcher.ui.utils;

import net.vpc.app.netbeans.launcher.util.JlistToStringer;
import net.vpc.app.nuts.NutsSdkLocation;

public class JdkJlistToStringer extends JlistToStringer {
    public JdkJlistToStringer() {
        super(2);
    }

    @Override
    public String toString(Object value, int level) {
        if (value instanceof NutsSdkLocation) {
            NutsSdkLocation i = (NutsSdkLocation) value;
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
