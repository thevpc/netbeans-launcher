package net.vpc.app.netbeans.launcher.ui.utils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BoxH {

    boolean opaque = true;
    boolean rightGlue;
    boolean leftGlue;
    int vgap;
    int hgap;
    ComponentPaint background;
    String name;

    private java.util.List<JComponent> children = new ArrayList<JComponent>();

    public String getName() {
        return name;
    }

    public BoxH setName(String name) {
        this.name = name;
        return this;
    }

    public int getVgap() {
        return vgap;
    }

    public BoxH setVgap(int vgap) {
        this.vgap = vgap;
        return this;
    }

    public BoxH setHgap(int hgap) {
        this.hgap = hgap;
        return this;
    }

    public BoxH setBackground(ComponentPaint c) {
        background=c;
        return this;
    }

    public BoxH setOpaque(boolean c) {
        opaque = c;
        return this;
    }

    public void addAll(JComponent... all) {
        for (JComponent jComponent : all) {
            add(jComponent);
        }
    }

    public void addGlueH() {
        add(new Glue(true, false));
    }

    public List<JComponent> getChildren() {
        return children;
    }

    public void add(JComponent c) {
        children.add(c);
    }

    public BoxH setLeftAligned() {
        rightGlue = true;
        return this;
    }

    public BoxH setRightAligned() {
        leftGlue = true;
        return this;
    }

    public JComponent toComponent() {
        PaintablePanel p = new PaintablePanel(new GridBagLayout());
        int index = 0;
        if (leftGlue) {
            p.add(new JLabel(), Grid.at(index++, 0).expandH());
        }
        for (JComponent cc : children) {
            if (cc instanceof Glue) {
                Grid g = Grid.at(index++, 0);
                if (((Glue) cc).horizontal) {
                    g = g.expandH();
                }
                if (((Glue) cc).vertical) {
                    g = g.expandV();
                }
                p.add(cc, g);
            } else {
                p.add(cc, Grid.at(index++, 0).fillH().fillV().insets(vgap, hgap));
            }
        }
        if (rightGlue) {
            p.add(new JLabel(), Grid.at(index++, 0).weight(100, 0).expandH());
        }
        p.setOpaque(opaque);
        if (background != null) {
            p.setBackgroundPaint(background);
        }
        p.setName(getName());
        return p;
    }
}
