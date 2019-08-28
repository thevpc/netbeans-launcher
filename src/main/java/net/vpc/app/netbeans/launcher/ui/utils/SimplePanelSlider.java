package net.vpc.app.netbeans.launcher.ui.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;

/**
 * credits goes to.
 * https://stackoverflow.com/questions/23785804/java-sliding-jpanels
 * I made some changes to enable usage of extra panels.
 */
public class SimplePanelSlider {
    private final JPanel basePanel = new JPanel();
    private final Container parent;
    private final Object lock = new Object();
    private boolean isSlideInProgress = false;
    private long slideTime = 10;
    private int slideMinStep = 10;
    private Component componentOld = new JPanel();
    private final JPanel glassPane;
    private final LinkedList<SlideEvent> toslide = new LinkedList<>();
    private final Timer timer = new Timer(1000, e -> popSlide());

    public SimplePanelSlider(final Container parent) {
        if (parent == null) {
            throw new RuntimeException("ProgramCheck: Parent can not be null.");
        }
        if ((parent instanceof JFrame) || (parent instanceof JDialog) || (parent instanceof JWindow) || (parent instanceof JPanel)) {
        } else {
            throw new RuntimeException("ProgramCheck: Parent type not supported. " + parent.getClass().getSimpleName());
        }
        this.parent = parent;
        glassPane = new JPanel();
        glassPane.setOpaque(false);
        glassPane.addMouseListener(new MouseAdapter() {
        });
        glassPane.addMouseMotionListener(new MouseMotionAdapter() {
        });
        glassPane.addKeyListener(new KeyAdapter() {
        });
        attach();
        basePanel.setSize(parent.getSize());
        basePanel.setLayout(new BorderLayout());
        enableTransparentOverlay();
        //timer.start();
    }

    public void setSlideTime(int slideTime) {
        synchronized (lock) {
            this.slideTime = slideTime;
        }
    }

    public void setSlideMinStep(int slideMinStep) {
        synchronized (lock) {
            this.slideMinStep = slideMinStep;
        }
    }

    public JPanel getBasePanel() {
        return basePanel;
    }

    private void pushSlide(final Direction slideType, Component componentNew) {
        synchronized (toslide){
            toslide.add(new SlideEvent(slideType, componentNew));
        }
    }

    private void popSlide() {
        if(SwingUtilities.isEventDispatchThread()){
            popSlide0();
        }else{
            SwingUtilities.invokeLater(()->popSlide0());
        }

    }
    private void popSlide0() {
        while (true) {
            if (isSlideInProgress) {
                return;
            }
            isSlideInProgress = true;
            try {
                SlideEvent s = null;
                synchronized (toslide) {
                    if (toslide.isEmpty()) {
                        return;
                    }
                    s = toslide.remove();
                }
                parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                disableUserInput(parent);
                slideImpl(s.slideType, s.next);
                enableUserInput(parent);
                parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            } finally {
                isSlideInProgress = false;
            }
        }
    }

    private void attach() {
        final Container w = this.parent;
        if (w instanceof JFrame) {
            final JFrame j = (JFrame) w;
            if (j.getContentPane().getComponents().length > 0) {
                throw new RuntimeException("ProgramCheck: Parent already contains content.");
            }
            j.getContentPane().add(basePanel);
        }
        if (w instanceof JDialog) {
            final JDialog j = (JDialog) w;
            if (j.getContentPane().getComponents().length > 0) {
                throw new RuntimeException("ProgramCheck: Parent already contains content.");
            }
            j.getContentPane().add(basePanel);
        }
        if (w instanceof JWindow) {
            final JWindow j = (JWindow) w;
            if (j.getContentPane().getComponents().length > 0) {
                throw new RuntimeException("ProgramCheck: Parent already contains content.");
            }
            j.getContentPane().add(basePanel);
        }
        if (w instanceof JPanel) {
            final JPanel j = (JPanel) w;
            if (j.getComponents().length > 0) {
                throw new RuntimeException("ProgramCheck: Parent already contains content.");
            }
            j.add(basePanel);
        }
    }

    private void enableUserInput(final Container w) {
        if (w instanceof JFrame) {
            ((JFrame) w).getGlassPane().setVisible(false);
        }
        if (w instanceof JDialog) {
            ((JDialog) w).getGlassPane().setVisible(false);
        }
        if (w instanceof JWindow) {
            ((JWindow) w).getGlassPane().setVisible(false);
        }
    }

    private void disableUserInput(final Container w) {
        if (w instanceof JFrame) {
            ((JFrame) w).setGlassPane(glassPane);
        }
        if (w instanceof JDialog) {
            ((JDialog) w).setGlassPane(glassPane);
        }
        if (w instanceof JWindow) {
            ((JWindow) w).setGlassPane(glassPane);
        }
        glassPane.setVisible(true);
    }

    private void enableTransparentOverlay() {
        Color c = Color.BLACK;
//        if (componentOld != null) {
//            c = componentOld.getBackground();
//        }
        if (parent instanceof JFrame) {
            ((JFrame) parent).getContentPane().setBackground(c);
            parent.remove(basePanel);
            parent.validate();
        }
        if (parent instanceof JDialog) {
            ((JDialog) parent).getContentPane().setBackground(c);
            parent.remove(basePanel);
            parent.validate();
        }
        if (parent instanceof JWindow) {
            ((JWindow) parent).getContentPane().setBackground(c);
            parent.remove(basePanel);
            parent.validate();
        }
    }

    private static class SlideEvent {
        Direction slideType;
        Component next;

        public SlideEvent(Direction slideType, Component next) {
            this.slideType = slideType;
            this.next = next;
        }
    }

    public void slide(final Direction slideType, Component next) {
        pushSlide(slideType, next);
        popSlide();
    }

    private void slideImpl(final Direction slideType, Component componentNew) {
        if (componentOld == componentNew) {
            return;
        }
        synchronized (lock) {
            basePanel.removeAll();
            basePanel.add(componentOld);
            final int w = componentOld.getWidth();
            final int h = componentOld.getHeight();
            final Point p1 = componentOld.getLocation();
            final Point p2 = new Point(0, 0);
            if (slideType == Direction.LEFT) {
                p2.x += w;
            }
            if (slideType == Direction.RIGHT) {
                p2.x -= w;
            }
            if (slideType == Direction.TOP) {
                p2.y += h;
            }
            if (slideType == Direction.BOTTOM) {
                p2.y -= h;
            }
            componentNew.setLocation(p2);
            int step = 0;
            if ((slideType == Direction.LEFT) || (slideType == Direction.RIGHT)) {
                step = (int) (((float) parent.getWidth() / (float) Toolkit.getDefaultToolkit().getScreenSize().width) * 40.f);
            } else {
                step = (int) (((float) parent.getHeight() / (float) Toolkit.getDefaultToolkit().getScreenSize().height) * 20.f);
            }
            step = step < slideMinStep ? slideMinStep : step;

            basePanel.add(componentNew);
            //basePanel.remove(componentOld);
            basePanel.revalidate();
            if (false) {
                final int max = (slideType == Direction.LEFT) || (slideType == Direction.RIGHT) ? w : h;
                final long t0 = System.currentTimeMillis();
                int maxI = max / step;
//                System.out.println("::"+ maxI);
                for (int i = 0; i != maxI; i++) {
                    switch (slideType) {
                        case LEFT: {
                            p1.x -= step;
                            componentOld.setLocation(p1);
                            p2.x -= step;
                            componentNew.setLocation(p2);
                            break;
                        }
                        case RIGHT: {
                            p1.x += step;
                            componentOld.setLocation(p1);
                            p2.x += step;
                            componentNew.setLocation(p2);
                            break;
                        }
                        case TOP: {
                            p1.y -= step;
                            componentOld.setLocation(p1);
                            p2.y -= step;
                            componentNew.setLocation(p2);
                            break;
                        }
                        case BOTTOM: {
                            p1.y += step;
                            componentOld.setLocation(p1);
                            p2.y += step;
                            componentNew.setLocation(p2);
                            break;
                        }
                        default:
                            new RuntimeException("ProgramCheck").printStackTrace();
                            break;
                    }

                    try {
                        long millis = slideTime;//slidTime / (max / step);
                        if (millis > 0) {
                            Thread.sleep(millis);
                        }
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                    basePanel.invalidate();
                    basePanel.revalidate();
                    basePanel.repaint();
                    componentOld.repaint();
                    componentNew.repaint();
//                    System.out.println(i+"/"+maxI+" :: "+componentOld.getLocation()+"_"+componentNew.getLocation());
                }
                final long t1 = System.currentTimeMillis();
            }
            componentOld.setLocation(-10000, -10000);
            componentNew.setLocation(0, 0);
            basePanel.remove(componentOld);
            componentOld = componentNew;
        }
    }

}