/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.netbeans.launcher.ui.utils;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;

import net.thevpc.netbeans.launcher.ui.FrameInfo;
import net.thevpc.netbeans.launcher.util.NbUtils;
import net.thevpc.netbeans.launcher.util.RefreshContext;
import net.thevpc.nuts.NExecutionException;
import net.thevpc.nuts.util.NStringUtils;

/**
 * @author thevpc
 */
public class SwingToolkit {

    private Component parent;
    private ResourceBundle lang;
    private FrameInfo frameInfo = new FrameInfo();

    public SwingToolkit(Component parent) {
        this.parent = parent;
        lang = ResourceBundle.getBundle("net.thevpc.netbeans.launcher.Messages");
    }


    public FrameInfo getFrameInfo() {
        return frameInfo;
    }

    public void setFrameInfo(FrameInfo frameInfo) {
        this.frameInfo = frameInfo;
    }

    private final static Pattern keyPattern = Pattern.compile("\\$\\{(?<name>[a-z]+)}");

    public Font deriveFont(Font initialFont) {
        return initialFont.deriveFont(fontSize(initialFont.getSize()));
    }

    private class ButtonLabelMouseAdapter extends MouseAdapter {

        private final ButtonAction action;
        JLabel label;
        ImageIcon normalIcon;
        ImageIcon selectedIcon;
        ImageIcon disabledIcon;
        boolean hover;
        String icon;

        public ButtonLabelMouseAdapter(String icon, ButtonAction action) {
            this.icon = icon;
            this.action = action;
            update();
        }

        public void update() {
            normalIcon = SwingUtils2.loadIcon(icon + ".png", iconSize());
            selectedIcon = new ImageIcon(SwingUtils2.newBrightness(normalIcon.getImage(), 0.8f));
//            selectedIcon = new ImageIcon(SwingUtils2.grayScaleImage2(normalIcon.getImage()));
            disabledIcon = new ImageIcon(SwingUtils2.grayScaleImage2(normalIcon.getImage()));
            revalidateIcon();
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            hover = true;
            revalidateIcon();
        }

        @Override
        public void mouseExited(MouseEvent e) {
            hover = false;
            revalidateIcon();
        }

        private void revalidateIcon() {
            if (label == null) {
                return;
            }
            if (label.isEnabled()) {
                if (hover) {
                    label.setIcon(selectedIcon);
                } else {
                    label.setIcon(normalIcon);
                }
            } else {
                label.setIcon(disabledIcon);
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1) {
                action.action();
            }
        }

        public void install(JLabel label) {
            this.label = label;
            label.setIcon(normalIcon);
            label.addPropertyChangeListener("enabled", new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    revalidateIcon();
                }
            });
            label.addMouseListener(this);
            revalidateIcon();
        }
    }

    public class Message {

        private String key;
        private Map<String, String> params = new HashMap<>();

        public Message(String key) {
            this.key = key;
        }

        public Message with(String k, String v) {
            params.put(k, v);
            return this;
        }

        public String getKey() {
            return key;
        }

        public Map<String, String> getParams() {
            return params;
        }

        public String getText() {
            String k = lang.getString(key);
            if (params.isEmpty()) {
                return k;
            }
            Matcher m = keyPattern.matcher(k);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                String v = params.get(m.group("name"));
                if (v == null) {
                    v = m.group();
                }
                m.appendReplacement(sb, Matcher.quoteReplacement(v));
            }
            m.appendTail(sb);
            return sb.toString();
        }

        @Override
        public String toString() {
            return getText();
        }

    }

    public Message msg(String m) {
        return new Message(m);
    }

    public void showSucess(Message title, Message message) {
        showSucess(title == null ? null : title.getText(), message == null ? null : message.getText());
    }

    private void showSucess(String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE, null);
    }

    public void showWarning(Message title, Message message) {
        showWarning(title == null ? null : title.getText(), message == null ? null : message.getText());
    }

    private void showWarning(String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE, null);
    }

    public void showError(Message title) {
        showError(null, title, null);
    }

    public void showError(Message title, Exception ex) {
        showError(null, title, ex);
    }

    public void showError(Message title, Message message, Exception ex) {
        showError(title == null ? null : title.getText(), message == null ? null : message.getText(), ex);
    }

    public boolean showConfirm(Message title, Message message) {
        return showConfirm(title == null ? null : title.getText(), message == null ? null : message.getText());
    }

    public static void runAndWait(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(r);
            } catch (InterruptedException ex) {
                throw new IllegalArgumentException(ex);
            } catch (InvocationTargetException ex) {
                throw new IllegalArgumentException(ex);
            }
        }
    }

    public static void runLater(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    private void showError(String title, String message, Exception ex) {
        if (NbUtils.isEmpty(title)) {
            title = msg("Toolkit.ShowError.Title").getText();
        }

        if (NbUtils.isEmpty(message)) {
            message = msg("Toolkit.ShowError.Message").getText();
        }
        if (ex != null) {
            message += " : " + ((ex instanceof NExecutionException) ? ex.getMessage() : ex.toString());
        }
//        JScrollPane scrollPane = new JScrollPane();
//        String messageOk=message;
//        runAndWait(() -> {
//            JTextArea textArea = new JTextArea();
//            textArea.setText(messageOk);
//            //textArea.setEditable(false);
//            textArea.setLineWrap(true);
//            textArea.setWrapStyleWord(true);
//            scrollPane.add(textArea);
//            scrollPane.setPreferredSize(new Dimension(600, 400));
//        });
//        JOptionPane.showMessageDialog(parent, scrollPane, title, JOptionPane.ERROR_MESSAGE, null);
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE, null);
    }

    private boolean showConfirm(String title, String message) {
        if (NbUtils.isEmpty(title)) {
            title = msg("Toolkit.ShowError.Title").getText();
        }

        if (NbUtils.isEmpty(message)) {
            message = msg("Toolkit.ShowError.Message").getText();
        }
        return JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null) == JOptionPane.OK_OPTION;
    }

    public void setComboxValues(JComboBox combo, Object[] values, Object selected) {
        DefaultComboBoxModel m = (DefaultComboBoxModel) combo.getModel();
        m.removeAllElements();
        for (Object value : values) {
            m.addElement(value);
        }
        combo.setSelectedItem(selected);
    }

    public JComboBox createCombo() {
        JComboBox combo = new JComboBox(new DefaultComboBoxModel());
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        return combo;
    }

    public JTextField createText() {
        return new JTextField();
    }

    public JTextArea createTextArea() {
        final JTextArea e = new JTextArea();
        e.setLineWrap(true);
        return e;
    }

    public Object getComboSelectedObject(JComboBox combo) {
        Object o = combo.getSelectedItem();
        if (o instanceof String) {
            for (int i = 0; i < combo.getItemCount(); i++) {
                Object item = combo.getItemAt(i);
                if (o.equals(String.valueOf(item))) {
                    return item;
                }
            }
        }
        return o;
    }

//    public void runLater(Runnable r) {
//        SwingUtilities.invokeLater(r);
//    }

    public void setControlDisabled(JComponent c, boolean disable) {
        c.setEnabled(!disable);
    }

    public void setControlVisible(JComponent c, boolean visible) {
        c.setVisible(visible);
    }

    public void updateList(JList list, Object[] values, Equalizer e, Comparator comp) {
        Equalizer e2 = new Equalizer() {
            @Override
            public boolean equals(Object a, Object b) {
                if (a == null && b == null) {
                    return true;
                } else if (a == null || b == null) {
                    return false;
                } else {
                    return a.equals(b);
                }
            }
        };
        if (comp != null) {
            Arrays.sort(values, comp);
        }
        Object old = (Object) list.getSelectedValue();
        Object ok = null;
        DefaultListModel model = (DefaultListModel) list.getModel();
        model.clear();
        for (Object loc : values) {
            model.addElement(loc);
            if (e != null ? e.equals(old, loc) : e2.equals(old, loc)) {
                ok = loc;
            }
        }
        list.setSelectedValue(ok, true);
    }

    public void updateTable(JTable table, Object[] values, Equalizer e, Comparator comp) {
        Equalizer e2 = new Equalizer() {
            @Override
            public boolean equals(Object a, Object b) {
                if (a == null && b == null) {
                    return true;
                } else if (a == null || b == null) {
                    return false;
                } else {
                    return a.equals(b);
                }
            }
        };
        if (comp != null) {
            Arrays.sort(values, comp);
        }
        int old = table.getSelectionModel().getLeadSelectionIndex();
        Object ok = null;
        int okIndex = -1;
        ObjectTableModel model = (ObjectTableModel) table.getModel();
        model.clear();
        for (int i = 0; i < values.length; i++) {
            Object loc = values[i];
            model.addRow(loc);
            if (e != null ? e.equals(old, loc) : e2.equals(old, loc)) {
                ok = loc;
                okIndex = i;
            }
        }
        if (okIndex > 0) {
            table.getSelectionModel().setSelectionInterval(okIndex, okIndex);
        }
    }

    public void updateTable(CatalogComponent table, Object[] values, Equalizer e, Comparator comp, Runnable onFinish) {
        SwingUtilities.invokeLater(() -> {
            Equalizer e2 = new Equalizer() {
                @Override
                public boolean equals(Object a, Object b) {
                    if (a == null && b == null) {
                        return true;
                    } else if (a == null || b == null) {
                        return false;
                    } else {
                        return a.equals(b);
                    }
                }
            };
            if (comp != null) {
                Arrays.sort(values, comp);
            }
            Object old = table.getSelectedValue();
            Object ok = null;
            int okIndex = -1;
            java.util.List<Object> newVals = new ArrayList<Object>();
            for (int i = 0; i < values.length; i++) {
                Object loc = values[i];
                newVals.add(loc);
                if (e != null ? e.equals(old, loc) : e2.equals(old, loc)) {
                    ok = loc;
                    okIndex = i;
                }
            }
            table.setValues(newVals);
            if (okIndex >= 0) {
                table.setSelectedIndex(okIndex);
            }
            if (onFinish != null) {
                onFinish.run();
            }
        });
    }

    public JComponent createIconButton0(String icon, String tooltip, ButtonAction action) {
        JLabel label = new JLabel();
        label.setToolTipText(msg(tooltip).getText());
        ButtonLabelMouseAdapter ad = new ButtonLabelMouseAdapter(icon, action);
        ad.install(label);
        prepareComponent(label, new DefaultJComponentRefresher() {
            @Override
            public void onRefresh(RefreshContext context) {
                super.onRefresh(context);
                ad.update();
            }
        });
        return label;
    }

    public float fontSize(float size) {
        int zoom = frameInfo.getZoom();
        size = size + zoom;
        if (size < 1) {
            size = 1;
        }
        if (size > 100) {
            size = size;
        }
        return size;
    }

    public JLabel createLabel(String textKey) {
        JLabel jLabel = new JLabel(msg(textKey).getText());
        Font oldFont = jLabel.getFont();
        jLabel.setFont(oldFont.deriveFont(Font.BOLD, fontSize(oldFont.getSize())));
        jLabel.setForeground(Color.GRAY.darker());
//        jLabel.setFont(new Font(Font.MONOSPACED,Font.BOLD,12));
        return jLabel;
    }

    public JLabel createLabel() {
        JLabel jLabel = new JLabel();
        Font oldFont = jLabel.getFont();
        jLabel.setFont(oldFont.deriveFont(Font.BOLD, fontSize(oldFont.getSize())));
        jLabel.setForeground(Color.GRAY.darker());
//        jLabel.setFont(new Font(Font.MONOSPACED,Font.BOLD,12));
        return jLabel;
    }


    public int iconSize() {
        boolean compact = frameInfo.isCompact();
        int size = compact ? 16 : 32;
        return iconSize(size);
    }

    public int iconSize(int size) {
        int zoom = frameInfo.getZoom();
        if (zoom > 100) {
            zoom = 100;
        }
        if (zoom < -32) {
            zoom = -32;
        }
        if (zoom > 0) {
            size += zoom * 2;
        } else {
            size += zoom;
        }
        if (size < 1) {
            size = 1;
        }
        return size;
    }

    public ImageIcon createIcon(String icon) {
        boolean compact = frameInfo.isCompact();
        int size = compact ? 16 : 32;
        int zoom = frameInfo.getZoom();
        if (zoom > 100) {
            zoom = 100;
        }
        if (zoom < -32) {
            zoom = -32;
        }
        if (zoom > 0) {
            size += zoom * 2;
        } else {
            size += zoom;
        }
        if (size < 1) {
            size = 1;
        }
        return SwingUtils2.loadIcon(icon + ".png", size);
    }

    public void prepareComponent(JComponent component) {
        prepareComponent(component, null);
    }


    public void prepareComponent(JComponent component, RefreshContext.Refresher r) {
        Object a = component.getClientProperty("SwingToolkit.refresh");
        RefreshContext rr;
        if (a instanceof RefreshContext) {
            rr = (RefreshContext) a;
        } else {
            rr = new RefreshContext();
            component.putClientProperty("SwingToolkit.refresh", rr);
        }
        rr.item = component;
        rr.initialFont = component.getFont();
        if (r == null) {
            r = new DefaultJComponentRefresher();
        }
        rr.refresher = r;
        rr.oldInfo = frameInfo;
    }

    public void refreshComponent(JComponent component) {
        Object u = component.getClientProperty("SwingToolkit.refresh");
        if (u instanceof RefreshContext) {
            RefreshContext uu = (RefreshContext) u;
            uu.newInfo = frameInfo;
            uu.onRefresh();
            uu.oldInfo = frameInfo;
        }
    }

    public JComponent createIconButton(String icon, String tooltip, ButtonAction action) {
        if (true) {
            return createIconButton0(icon, tooltip, action);
        } else {
            ImageIcon imageIcon = SwingUtils2.loadIcon(icon + ".png", iconSize());
            JButton button = new JButton(imageIcon);
            int m = frameInfo.isCompact() ? 1 : 4;
            button.setMargin(new Insets(1, m, 1, m));
            button.setToolTipText(msg(tooltip).getText());
            button.addActionListener((ActionEvent t) -> action.action());
            return button;
        }
    }

    public void openFolder(String path) {
        if (!NStringUtils.trim(path).isEmpty()) {
            try {
                final File f = NbUtils.resolveFile(path);
                if (f.isDirectory()) {
                    boolean overrideJavaDesktop = true;
                    if (overrideJavaDesktop) {
                        final String osName = System.getProperty("os.name").toLowerCase();
                        if (osName.contains("windows")) {
                            ProcessBuilder b = new ProcessBuilder("explorer", f.getPath()).inheritIO();
                            b.start();
                            return;
                        } else if (osName.contains("linux")) {
                            ProcessBuilder b = new ProcessBuilder("xdg-open", f.getPath()).inheritIO();
                            b.start();
                            return;
                        } else if (osName.contains("mac os")) {
                            ProcessBuilder b = new ProcessBuilder("open", f.getPath()).inheritIO();
                            b.start();
                            return;
                        } else {
                            //
                        }
                    }
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(f);
                    } else {
                        showError(msg("Toolkit.OpenFolder.Error.Unsupported"));
                    }
                    return;
                }
            } catch (IOException ex) {
                showError(msg("Toolkit.OpenFolder.Error.Unknown").with("error", ex.toString()), ex);
            }
        }
        showError(msg("Toolkit.OpenFolder.Error.Invalid"));
    }

    private class DefaultJComponentRefresher implements RefreshContext.Refresher {
        @Override
        public void onRefresh(RefreshContext context) {
            JComponent c = (JComponent) context.item;
            c.setFont(context.initialFont.deriveFont(fontSize(context.initialFont.getSize())));
        }
    }
}
