package net.vpc.app.netbeans.launcher.ui.swing.utils;

import net.vpc.app.netbeans.launcher.ui.swing.MainWindowSwingHelper;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * thank you
 * https://stackoverflow.com/questions/46797579/how-can-i-control-the-brightness-of-an-image
 */
public class SwingUtils2 {
    public static Image newBrightness( Image source, float brightnessPercentage ) {

        BufferedImage bi = new BufferedImage(
                source.getWidth( null ),
                source.getHeight( null ),
                BufferedImage.TYPE_INT_ARGB );

        int[] pixel = { 0, 0, 0, 0 };
        float[] hsbvals = { 0, 0, 0 };

        bi.getGraphics().drawImage( source, 0, 0, null );

        // recalculare every pixel, changing the brightness
        for ( int i = 0; i < bi.getHeight(); i++ ) {
            for ( int j = 0; j < bi.getWidth(); j++ ) {

                // get the pixel data
                bi.getRaster().getPixel( j, i, pixel );

                // converts its data to hsb to change brightness
                Color.RGBtoHSB( pixel[0], pixel[1], pixel[2], hsbvals );

                // create a new color with the changed brightness
                Color c = new Color( Color.HSBtoRGB( hsbvals[0], hsbvals[1], hsbvals[2] * brightnessPercentage ) );

                // set the new pixel
                bi.getRaster().setPixel( j, i, new int[]{ c.getRed(), c.getGreen(), c.getBlue(), pixel[3] } );

            }

        }

        return bi;

    }

    public static Image grayScaleImage( Image image) {
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

    public static Image grayScaleImage2( Image image) {
        BufferedImage result = new BufferedImage(
                image.getWidth(null),
                image.getHeight(null),
                BufferedImage.TYPE_INT_RGB);

        Graphics2D graphic = result.createGraphics();
        Color cc = MainWindowSwingHelper.color("0095c9");//Color.WHITE;
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
    private static BufferedImage imageToBufferedImage(final Image image)
    {
        final BufferedImage bufferedImage =
                new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2 = bufferedImage.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        return bufferedImage;
    }

    public static Image newBrightness2( Image source, float brightnessPercentage ) {

        BufferedImage bi = new BufferedImage(
                source.getWidth( null ),
                source.getHeight( null ),
                BufferedImage.TYPE_INT_ARGB );

        int[] pixel = { 0, 0, 0, 0 };
        float[] hsbvals = { 0, 0, 0 };

        bi.getGraphics().drawImage( source, 0, 0, null );

        // recalculare every pixel, changing the brightness
        for ( int i = 0; i < bi.getHeight(); i++ ) {
            for ( int j = 0; j < bi.getWidth(); j++ ) {

                // get the pixel data
                bi.getRaster().getPixel( j, i, pixel );

                // converts its data to hsb to change brightness
                Color.RGBtoHSB( pixel[0], pixel[1], pixel[2], hsbvals );

                // calculates the brightness component.
                float newBrightness = hsbvals[2] * brightnessPercentage;
                if ( newBrightness > 1f ) {
                    newBrightness = 1f;
                }

                // create a new color with the new brightness
                Color c = new Color( Color.HSBtoRGB( hsbvals[0], hsbvals[1], newBrightness ) );

                // set the new pixel
                bi.getRaster().setPixel( j, i, new int[]{ c.getRed(), c.getGreen(), c.getBlue(), pixel[3] } );

            }

        }

        return bi;

    }
}
