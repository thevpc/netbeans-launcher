package net.thevpc.netbeans.launcher.util;

import net.thevpc.netbeans.launcher.ui.FrameInfo;

import java.awt.*;

public class RefreshContext {
    public Object item;
    public Font initialFont;
    public FrameInfo oldInfo;
    public FrameInfo newInfo;
    public Refresher refresher;

    public void onRefresh() {
        refresher.onRefresh(this);
    }

    public interface Refresher{
        void onRefresh(RefreshContext context);
    }
}
