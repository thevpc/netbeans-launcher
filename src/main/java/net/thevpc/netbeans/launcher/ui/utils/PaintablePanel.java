package net.thevpc.netbeans.launcher.ui.utils;

import javax.swing.*;
import java.awt.*;

public class PaintablePanel extends JPanel {
    private ComponentPaint backgroundPaint;

    public PaintablePanel(LayoutManager2 layout) {
        super(layout);
    }

    public ComponentPaint getBackgroundPaint() {
        return backgroundPaint;
    }

    public void setBackgroundPaint(Paint backgroundPaint) {
        this.backgroundPaint = backgroundPaint==null?null: new FixedComponentPaint(backgroundPaint);
    }

    public void setBackgroundPaint(ComponentPaint backgroundPaint) {
        this.backgroundPaint = backgroundPaint;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(!isOpaque()){
            return;
        }
        Graphics2D g2d = (Graphics2D) g;
        ComponentPaint b = getBackgroundPaint();
        int w = getWidth();
        int h = getHeight();
        Paint bb = b==null?null:b.get(this, w, h);
        if(bb==null) {
            Color color1 = getBackground();
            Color color2 = color1.darker();
            GradientPaint gp = new GradientPaint(
                    0, 0, color1, 0, h, color2);
            g2d.setPaint(gp);
        }else{
            g2d.setPaint(bb);
        }
        g2d.fillRect(0, 0, w, h);
    }

}
