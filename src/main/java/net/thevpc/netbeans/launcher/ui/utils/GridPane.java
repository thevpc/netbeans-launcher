package net.thevpc.netbeans.launcher.ui.utils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GridPane {

    private List<CompAndConst> list = new ArrayList<>();
    private int itop;
    private int ileft;
    private int ibottom;
    private int iright;
    private Set<Integer> expandH = new HashSet<>();
    private Map<String, GridPaneBuilderTemplate> templates = new HashMap<>();

    int row;
    int col;

    public GridPane insets(int top, int left, int bottom, int right) {
        this.itop = top;
        this.ileft = left;
        this.ibottom = bottom;
        this.iright = right;
        return this;
    }

    public GridPane insets(int hgap, int itop) {
        this.itop = itop;
        this.ileft = hgap;
        this.ibottom = itop;
        this.iright = hgap;
        return this;
    }

    public GridPane incRow(int c) {
        this.row++;
        this.col = 0;
        return this;
    }

    public GridPane incColum(int c) {
        this.col++;
        return this;
    }

    public GridPane newRow() {
        return incRow(1);
    }

    static class CompAndConst {

        JComponent component;
        GridBagConstraints constraints;

        public CompAndConst(JComponent component, GridBagConstraints constraints) {
            this.component = component;
            this.constraints = constraints;
        }

    }

    public GridPane expandHorizontallyColumn(int col) {
        expandH.add(col);
        return this;
    }

    public GridPaneBuilderTemplate template(String name) {
        GridPaneBuilderTemplate old = templates.get(name);
        if (old != null) {
            throw new IllegalArgumentException("already defined");
        }
        GridPaneBuilderTemplate t = new GridPaneBuilderTemplate(name);
        templates.put(name, t);
        return t;
    }

    public GridPaneBuilder b() {
        return new GridPaneBuilder(null);
    }

    public GridPaneBuilder b(String name) {
        GridPaneBuilderTemplate t = templates.get(name);
        if (t == null) {
            throw new IllegalArgumentException("missing template " + name);
        }
        return new GridPaneBuilder(t);
    }

    public Grid g() {
        return new Grid().insets(itop, ileft, ibottom, iright);
    }

    public GridPane add(JComponent comp, int c, int r) {
        return add(comp, c, r, 1, 1);
    }

    public GridPane add(JComponent comp, int c, int r, int h, int v) {
        Grid g = Grid.at(c, r).insets(itop, ileft, ibottom, iright).span(h, v);
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

    public class GridPaneBuilderTemplate {

        private int itop = -1;
        private int ileft = -1;
        private int ibottom = -1;
        private int iright = -1;
        private int expandH = -1;
        private String name;
        private int h = -1;
        private int v = -1;

        private GridPaneBuilderTemplate(String name) {
            this.name = name;
        }

        public GridPaneBuilderTemplate insets(int top, int left, int bottom, int right) {
            this.itop = top;
            this.ileft = left;
            this.ibottom = bottom;
            this.iright = right;
            return this;
        }

        public GridPaneBuilderTemplate insets(int hgap, int itop) {
            this.itop = itop;
            this.ileft = hgap;
            this.ibottom = itop;
            this.iright = hgap;
            return this;
        }

        public GridPaneBuilderTemplate span(int h, int v) {
            this.h = h;
            this.v = v;
            return this;
        }

        public GridPane build() {
            return GridPane.this;
        }

        public GridPaneBuilderTemplate expandH() {
            if (expandH < 0) {
                expandH = 1;
            } else {
                this.expandH++;
            }
            return this;
        }
    }

    public class GridPaneBuilder {

        private int itop;
        private int ileft;
        private int ibottom;
        private int iright;
        private int expandH = -1;
        private int h = -1;
        private int v = -1;

        public GridPaneBuilder(GridPaneBuilderTemplate t) {
            if (t == null) {
                this.itop = GridPane.this.itop;
                this.ileft = GridPane.this.ileft;
                this.ibottom = GridPane.this.ibottom;
                this.iright = GridPane.this.iright;
                this.expandH = 0;
            } else {
                this.itop = t.itop > 0 ? t.itop : GridPane.this.itop > 0 ? GridPane.this.itop : 0;
                this.ileft = t.ileft > 0 ? t.ileft : GridPane.this.ileft > 0 ? GridPane.this.ileft : 0;
                this.ibottom = t.ibottom > 0 ? t.ibottom : GridPane.this.ibottom > 0 ? GridPane.this.ibottom : 0;
                this.iright = t.iright > 0 ? t.iright : GridPane.this.iright > 0 ? GridPane.this.iright : 0;
                this.h = t.h > 0 ? t.h : -1;
                this.v = t.v > 0 ? t.v : -1;
                this.expandH = t.expandH >= 0 ? t.expandH : -1;
            }
        }

        public GridPaneBuilder insets(int top, int left, int bottom, int right) {
            this.itop = top;
            this.ileft = left;
            this.ibottom = bottom;
            this.iright = right;
            return this;
        }

        public GridPaneBuilder insets(int hgap, int itop) {
            this.itop = itop;
            this.ileft = hgap;
            this.ibottom = itop;
            this.iright = hgap;
            return this;
        }

        public GridPaneBuilder expandH() {
            if (expandH < 0) {
                expandH = 1;
            } else {
                this.expandH++;
            }
            return this;
        }

        public GridPane add(JComponent comp) {
            return add(comp, GridPane.this.col, GridPane.this.row, this.h, this.v);
        }

        public GridPane add(JComponent comp, int c, int r) {
            return add(comp, c, r, this.h, this.v);
        }

        public GridPane add(JComponent comp, int c, int r, int h, int v) {
            GridPane.this.col = c;
            GridPane.this.row = r;
            final int itop = this.itop < 0 ? 0 : this.itop;
            final int ileft = this.ileft < 0 ? 0 : this.ileft;
            final int ibottom = this.ileft < 0 ? 0 : this.ileft;
            final int iright = this.iright < 0 ? 0 : this.iright;
            Grid g = Grid.at(c, r).insets(itop, ileft, ibottom, iright)
                    .span(h > 0 ? h : 1, v > 0 ? v : 1);
            if (expandH > 0) {
                g.expandH();
            } else {
                if (GridPane.this.expandH.contains(c)) {
                    g.expandH();
                }
            }
            g.fillVH();
            if (r == 0) {
                g.anchorNoth();
            }
            list.add(new CompAndConst(comp, g));
            if (h > 0) {
                GridPane.this.col += h;
            } else {
                GridPane.this.col += 1;
            }
            if (v > 1) {
                GridPane.this.row += v - 1;
            }
            return GridPane.this;
        }
    }
}
