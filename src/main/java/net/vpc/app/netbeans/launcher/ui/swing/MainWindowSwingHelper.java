/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.netbeans.launcher.ui.swing;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import net.vpc.app.netbeans.launcher.NbOptions;
import net.vpc.app.netbeans.launcher.ui.Grid;

/**
 *
 * @author vpc
 */
public class MainWindowSwingHelper {

    private static Map<String, Color> colorsCache = new HashMap<>();
    private static Map<String, ImageIcon> iconsCache = new HashMap<>();

    public static JComponent createIconButton(String icon, String tooltip,ButtonAction a) {
        final JButton b = new JButton("",loadIcon(icon, 16));
        b.setToolTipText(tooltip);
        b.addActionListener((ActionEvent e) -> a.action());
        return b;
    }
    public static ImageIcon loadIcon(String icon, int size) {
        final URL resource = Grid.class.getResource(icon);
        if(resource==null){
            throw new IllegalArgumentException("URL not found "+icon);
        }
        return loadIcon(resource, size);
    }

    public static ImageIcon loadIcon(String icon, int w,int h) {
        final URL resource = Grid.class.getResource(icon);
        if(resource==null){
            throw new IllegalArgumentException("URL not found "+icon);
        }
        return loadIcon(resource, w,h);
    }

    public static synchronized ImageIcon loadIcon(URL url, int size) {
            return loadIcon(url,size,size);
    }

    public static synchronized ImageIcon loadIcon(URL url, int w,int h) {
        final String k = ""+w+":"+h+":"+url;
        ImageIcon v = iconsCache.get(k);
        if (v == null) {
            ImageIcon imageDecline = new ImageIcon(url);
            Image image = imageDecline.getImage();
            Image newimg = image.getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH);
            v = new ImageIcon(newimg);
            iconsCache.put(k, v);
        }
        return v;
    }

    public static synchronized Color color(String s) {
        Color v = colorsCache.get(s);
        if (v == null) {
            v = new Color(Integer.parseInt(s, 16));
            colorsCache.put(s, v);
        }
        return v;
    }

    public static GridPane gridPane() {
        return new GridPane();
    }

    public static BoxH boxH() {
        return new BoxH();
    }

    public static class Glue extends JComponent {

        private boolean horizontal;
        private boolean vertical;

        public Glue(boolean horizontal, boolean vertical) {
            this.horizontal = horizontal;
            this.vertical = vertical;
        }
    }

    public static class GridPane {

        public GridPane insets(int vgap, int hgap) {
            this.vgap = vgap;
            this.hgap = hgap;
            return this;
        }

        static class CompAndConst {

            JComponent component;
            GridBagConstraints constrainst;

            public CompAndConst(JComponent component, GridBagConstraints constrainst) {
                this.component = component;
                this.constrainst = constrainst;
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
                p.add(compAndConst.component, compAndConst.constrainst);
                maxRow = Math.max(maxRow, compAndConst.constrainst.gridy + compAndConst.constrainst.gridheight - 1);
            }
            p.add(new JPanel(), Grid.at(0, maxRow + 1).weight(1, 100).fillReminder());
//            p.setBorder(BorderFactory.createLineBorder(Color.RED));
            return p;
        }
    }

    public static class BoxH {

        boolean opaque = true;
        boolean rightGlue;
        boolean leftGlue;
        int vgap;
        int hgap;
        Color background;

        private List<JComponent> children = new ArrayList<JComponent>();

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

        public BoxH setBackground(Color c) {
            background = c;
            return this;
        }

        public BoxH setOpaque(boolean c) {
            opaque = c;
            return this;
        }

        public void setBackground(String c) {
            background = (c == null) ? null : new java.awt.Color(Integer.parseInt(c, 16));
        }

        public void addAll(JComponent... all) {
            for (JComponent jComponent : all) {
                add(jComponent);
            }
        }

        public void addGlueH() {
            add(new Glue(true, false));
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
            JPanel p = new JPanel(new GridBagLayout());
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
                p.setBackground(background);
            }
            return p;
        }
    }

    public static void prepareLaunch(NbOptions options) {
        String plaf = options.plaf;
        try {
            if (plaf == null) {
                plaf = "nimbus";
            }

            if (plaf.equals("metal")) {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } else if (plaf.equals("gtk")) {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("GTK+".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } else if (plaf.equals("motif")) {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("CDE/Motif".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } else if (plaf.equals("nimbus")) {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            }
            // Set cross-platform Java L&F (also called "Metal")
//            UIManager.setLookAndFeel(
//                    UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException e) {
            // handle exception
        } catch (ClassNotFoundException e) {
            // handle exception
        } catch (InstantiationException e) {
            // handle exception
        } catch (IllegalAccessException e) {
            // handle exception
        }

    }

    public static boolean acceptCmdArg(String arg) {
        if (arg.equalsIgnoreCase("--swing")) {
            return true;
        } else if (arg.equalsIgnoreCase("--metal")) {
            return true;
        } else if (arg.equalsIgnoreCase("--numbus")) {
            return true;
        } else if (arg.equalsIgnoreCase("--system")) {
            return true;
        } else if (arg.equalsIgnoreCase("--gtk")) {
            return true;
        } else if (arg.equalsIgnoreCase("--motif")) {
            return true;
        }
        return false;
    }

}
