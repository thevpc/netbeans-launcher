package net.thevpc.netbeans.launcher.ui;

import java.util.Objects;

public class FrameInfo {
    private boolean compact;
    private int zoom;

    public FrameInfo() {
    }

    public FrameInfo(boolean compact, int zoom) {
        this.compact = compact;
        this.zoom = zoom;
    }

    public boolean isCompact() {
        return compact;
    }

    public FrameInfo setCompact(boolean compact) {
        return new FrameInfo(compact, zoom);
    }

    public int getZoom() {
        return zoom;
    }

    public FrameInfo zoomIn() {
        return setZoom(zoom + 1);
    }
    public FrameInfo zoomOut() {
        return setZoom(zoom - 1);
    }
    public FrameInfo zoomNone() {
        return setZoom(0);
    }
    public FrameInfo setZoom(int zoom) {
        return new FrameInfo(compact, zoom);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FrameInfo frameInfo = (FrameInfo) o;
        return compact == frameInfo.compact && zoom == frameInfo.zoom;
    }

    @Override
    public int hashCode() {
        return Objects.hash(compact, zoom);
    }
}
