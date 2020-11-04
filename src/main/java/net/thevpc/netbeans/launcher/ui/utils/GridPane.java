package net.thevpc.netbeans.launcher.ui.utils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GridPane {

    public GridPane insets(int vgap, int hgap) {
        this.vgap = vgap;
        this.hgap = hgap;
        return this;
    }

    static class CompAndConst {

        JComponent component;
        GridBagConstraints constraints;

        public CompAndConst(JComponent component, GridBagConstraints constraints) {
            this.component = component;
            this.constraints = constraints;
        }

    }
    private List<CompAndConst> list = new ArrayList<>();
    private int vgap = 0;
    private int hgap = 0;
    private Set<Integer> expandH = new HashSet<>();

    public GridPane expandHorizontallyColumn(int col) {
        expandH.add(col);
        return this;
    }

    public Grid g() {
        return new Grid().insets(vgap, hgap);
    }

    public GridPane add(JComponent comp, int c, int r) {
        return add(comp, c, r, 1, 1);
    }

    public GridPane add(JComponent comp, int c, int r, int h, int v) {
        Grid g = Grid.at(c, r).insets(vgap, hgap).span(h, v);
        if (expandH.contains(c)) {
            g.expandH();
        }
        g.fillVH();
        if (r == 0) {
            g.anchorNoth();
        }
        list.add(new CompAndConst(comp, g));
        return this;
    }

    public JComponent toComponent() {
        JPanel p = new JPanel(new GridBagLayout());
        int maxRow = 0;
        for (CompAndConst compAndConst : list) {
            p.add(compAndConst.component, compAndConst.constraints);
            maxRow = Math.max(maxRow, compAndConst.constraints.gridy + compAndConst.constraints.gridheight - 1);
        }
        p.add(new JPanel(), Grid.at(0, maxRow + 1).weight(1, 100).fillReminder());
//            p.setBorder(BorderFactory.createLineBorder(Color.RED));
        return p;
    }
}
