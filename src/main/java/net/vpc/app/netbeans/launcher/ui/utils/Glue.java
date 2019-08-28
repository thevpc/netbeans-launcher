package net.vpc.app.netbeans.launcher.ui.utils;

import javax.swing.*;

public class Glue extends JComponent {

    boolean horizontal;
    boolean vertical;

    public Glue(boolean horizontal, boolean vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }
}
