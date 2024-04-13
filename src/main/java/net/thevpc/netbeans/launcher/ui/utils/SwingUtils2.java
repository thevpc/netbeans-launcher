package net.thevpc.netbeans.launcher.ui.utils;

import net.thevpc.netbeans.launcher.NbOptions;
import net.thevpc.netbeans.launcher.ui.MainWindowSwing;
import net.thevpc.swing.plaf.UIPlafManager;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import net.thevpc.nuts.util.NBlankable;

/**
 * thank you
 * https://stackoverflow.com/questions/46797579/how-can-i-control-the-brightness-of-an-image
 */
public class SwingUtils2 {

    private static Map<String, Color> colorsCache = new HashMap<>();
    private static Map<String, ImageIcon> iconsCache = new HashMap<>();

    public static Image newBrightness(Image source, float brightnessPercentage) {

        BufferedImage bi = new BufferedImage(
                source.getWidth(null),
                source.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);

        int[] pixel = {0, 0, 0, 0};
        float[] hsbvals = {0, 0, 0};

        bi.getGraphics().drawImage(source, 0, 0, null);

        // recalculare every pixel, changing the brightness
        for (int i = 0; i < bi.getHeight(); i++) {
            for (int j = 0; j < bi.getWidth(); j++) {

                // get the pixel data
                bi.getRaster().getPixel(j, i, pixel);

                // converts its data to hsb to change brightness
                Color.RGBtoHSB(pixel[0], pixel[1], pixel[2], hsbvals);

                // create a new color with the changed brightness
                Color c = new Color(Color.HSBtoRGB(hsbvals[0], hsbvals[1], hsbvals[2] * brightnessPercentage));

                // set the new pixel
                bi.getRaster().setPixel(j, i, new int[]{c.getRed(), c.getGreen(), c.getBlue(), pixel[3]});

            }

        }

        return bi;

    }

    public static Image grayScaleImage(Image image) {
        BufferedImage result = new BufferedImage(
                image.getWidth(null),
                image.getHeight(null),
                BufferedImage.TYPE_INT_RGB);

        Graphics2D graphic = result.createGraphics();
        graphic.drawImage(image, 0, 0, Color.WHITE, null);

        for (int i = 0; i < result.getHeight(); i++) {
            for (int j = 0; j < result.getWidth(); j++) {
                Color c = new Color(result.getRGB(j, i));
                int red = (int) (c.getRed() * 0.299);
                int green = (int) (c.getGreen() * 0.587);
                int blue = (int) (c.getBlue() * 0.114);
                Color newColor = new Color(
                        red + green + blue,
                        red + green + blue,
                        red + green + blue);
                result.setRGB(j, i, newColor.getRGB());
            }
        }
        return result;
    }

    public static Image grayScaleImage2(Image image) {
        BufferedImage result = new BufferedImage(
                image.getWidth(null),
                image.getHeight(null),
                BufferedImage.TYPE_INT_RGB);

        Graphics2D graphic = result.createGraphics();
        Color cc = color("0095c9");//Color.WHITE;
        graphic.drawImage(image, 0, 0, cc, null);

        for (int i = 0; i < result.getHeight(); i++) {
            for (int j = 0; j < result.getWidth(); j++) {
                Color c = new Color(result.getRGB(j, i));
                int red = (int) (c.getRed() * 0.299);
                int green = (int) (c.getGreen() * 0.587);
                int blue = (int) (c.getBlue() * 0.114);
                Color newColor = new Color(
                        red + green + blue,
                        red + green + blue,
                        red + green + blue);
                result.setRGB(j, i, newColor.getRGB());
            }
        }
        return result;
    }

    /**
     * Convert Image to BufferedImage.
     *
     * @param image Image to be converted to BufferedImage.
     * @return BufferedImage corresponding to provided Image.
     */
    private static BufferedImage imageToBufferedImage(final Image image) {
        final BufferedImage bufferedImage
                = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2 = bufferedImage.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        return bufferedImage;
    }

    public static Image newBrightness2(Image source, float brightnessPercentage) {

        BufferedImage bi = new BufferedImage(
                source.getWidth(null),
                source.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);

        int[] pixel = {0, 0, 0, 0};
        float[] hsbvals = {0, 0, 0};

        bi.getGraphics().drawImage(source, 0, 0, null);

        // recalculare every pixel, changing the brightness
        for (int i = 0; i < bi.getHeight(); i++) {
            for (int j = 0; j < bi.getWidth(); j++) {

                // get the pixel data
                bi.getRaster().getPixel(j, i, pixel);

                // converts its data to hsb to change brightness
                Color.RGBtoHSB(pixel[0], pixel[1], pixel[2], hsbvals);

                // calculates the brightness component.
                float newBrightness = hsbvals[2] * brightnessPercentage;
                if (newBrightness > 1f) {
                    newBrightness = 1f;
                }

                // create a new color with the new brightness
                Color c = new Color(Color.HSBtoRGB(hsbvals[0], hsbvals[1], newBrightness));

                // set the new pixel
                bi.getRaster().setPixel(j, i, new int[]{c.getRed(), c.getGreen(), c.getBlue(), pixel[3]});

            }

        }

        return bi;

    }

    public static JComponent createIconButton(String icon, String tooltip, ButtonAction a) {
        final JButton b = new JButton("", loadIcon(icon, 16));
        b.setToolTipText(tooltip);
        b.addActionListener((ActionEvent e) -> a.action());
        return b;
    }

    public static ImageIcon loadIcon(String icon, int size) {
        final URL resource = MainWindowSwing.class.getResource(icon);
        if (resource == null) {
            throw new IllegalArgumentException("URL not found " + icon);
        }
        return loadIcon(resource, size);
    }

    public static ImageIcon loadIcon(String icon, int w, int h) {
        final URL resource = MainWindowSwing.class.getResource(icon);
        if (resource == null) {
            throw new IllegalArgumentException("URL not found " + icon);
        }
        return loadIcon(resource, w, h);
    }

    public static synchronized ImageIcon loadIcon(URL url, int size) {
        return loadIcon(url, size, size);
    }

    public static synchronized ImageIcon loadIcon(URL url, int w, int h) {
        final String k = "" + w + ":" + h + ":" + url;
        ImageIcon v = iconsCache.get(k);
        if (v == null) {
            ImageIcon imageDecline = new ImageIcon(url);
            Image image = imageDecline.getImage();
            Image newimg = image.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            v = new ImageIcon(newimg);
            iconsCache.put(k, v);
        }
        return v;
    }

    public static synchronized ComponentPaint componentGradientPaint(String a, int darkening, Direction dir) {
        return componentGradientPaint(color(a), darkening, dir);
    }

    public static synchronized ComponentPaint componentGradientPaint(Color a, int darkening, Direction dir) {
        Color b = a;
        while (darkening > 0) {
            b = b.darker();
            darkening--;
        }
        while (darkening < 0) {
            b = b.brighter();
            darkening++;
        }
        return componentGradientPaint(a, b, dir);
    }

    public static synchronized ComponentPaint componentGradientPaint(String a, String b, Direction dir) {
        return componentGradientPaint(color(a), color(b), dir);
    }

    public static synchronized ComponentPaint componentGradientPaint(Color a, Color b, Direction dir) {
        if (dir == null) {
            dir = Direction.BOTTOM;
        }
        switch (dir) {
            case BOTTOM:
                return new ComponentPaint() {
                    @Override
                    public Paint get(Component c, int w, int h) {
                        return new GradientPaint(0, 0, a, 0, h, b);
                    }
                };
            case TOP:
                return new ComponentPaint() {
                    @Override
                    public Paint get(Component c, int w, int h) {
                        return new GradientPaint(0, h, a, 0, 0, b);
                    }
                };
            case LEFT:
                return new ComponentPaint() {
                    @Override
                    public Paint get(Component c, int w, int h) {
                        return new GradientPaint(w, 0, a, 0, 0, b);
                    }
                };
            case RIGHT:
                return new ComponentPaint() {
                    @Override
                    public Paint get(Component c, int w, int h) {
                        return new GradientPaint(0, 0, a, w, 0, b);
                    }
                };
        }
        throw new IllegalArgumentException("Unsupported");
    }

    public static synchronized ComponentPaint componentGradientPaint(Paint s) {
        return s == null ? null : new FixedComponentPaint(s);
    }

    public static synchronized ComponentPaint componentPaint(Paint s) {
        return s == null ? null : new FixedComponentPaint(s);
    }

    public static synchronized ComponentPaint componentPaint(String s) {
        return s == null ? null : new FixedComponentPaint(color(s));
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

    public static void prepareLaunch(NbOptions options) {
        String plaf = options.plaf;
        try {
            if (NBlankable.isBlank(plaf)) {
//                plaf = "FlatDark";
                plaf = "FlatLight";
            }
            UIPlafManager.INSTANCE.apply(plaf);
            // Set cross-platform Java L&F (also called "Metal")
//            UIManager.setLookAndFeel(
//                    UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            // handle exception
        }

    }

    public static boolean acceptCmdArg(String arg) {
        if (arg.equalsIgnoreCase("--swing")) {
            return true;
        } else if (arg.equalsIgnoreCase("--metal")) {
            return true;
        } else if (arg.equalsIgnoreCase("--nimbus")) {
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

    public static void addEnterAction(JList list, ButtonAction action) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        InputMap im = list.getInputMap();
        im.put(keyStroke, keyStroke);
        list.getActionMap().put(keyStroke, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.action();
            }
        });
    }

    public static void addEnterAction(JTable list, ButtonAction action) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        InputMap im = list.getInputMap();
        im.put(keyStroke, keyStroke);
        list.getActionMap().put(keyStroke, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.action();
            }
        });
    }

    public static void addEnterAction(JList list, Action action) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        InputMap im = list.getInputMap();
        im.put(keyStroke, keyStroke);
        list.getActionMap().put(keyStroke, action);
    }

    public static void setWidthAsPercentages(JTable table, float... percentages) {
        Dimension size = table.getSize();

        TableColumnModel model = table.getColumnModel();
        float[] allColumns = new float[model.getColumnCount()];
        float t = 0;
        for (int i = 0; i < percentages.length; i++) {
            t += percentages[i];
        }
        for (int i = 0; i < allColumns.length; i++) {
            float f = 0;
            if (i < percentages.length) {
                f = percentages[i] / t;
            } else {
                f = percentages[percentages.length - 1] / t;
            }
            allColumns[i] = f;
        }
        for (int columnIndex = 0; columnIndex < allColumns.length; columnIndex++) {
            TableColumn column = model.getColumn(columnIndex);
            column.setPreferredWidth((int) (allColumns[columnIndex] * size.getWidth()));
        }
    }
}
