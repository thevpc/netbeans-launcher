package net.thevpc.netbeans.launcher.ui;

public class AppPanePos {
    private int x;
    private int y;

    public AppPanePos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public AppPanePos min(AppPanePos other){
        return new AppPanePos(x-other.x,y-other.y);
    }

    public AppPanePos comp(AppPanePos other){
        return new AppPanePos(Integer.compare(x,other.x),Integer.compare(y,other.y));
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
