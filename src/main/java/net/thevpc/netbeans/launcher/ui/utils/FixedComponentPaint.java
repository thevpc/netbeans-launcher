package net.thevpc.netbeans.launcher.ui.utils;

import java.awt.*;

public class FixedComponentPaint implements ComponentPaint {
    private final Paint backgroundPaint;

    public FixedComponentPaint(Paint backgroundPaint) {
        this.backgroundPaint = backgroundPaint;
    }

    @Override
    public Paint get(Component c, int w, int h) {
        return backgroundPaint;
    }
}
