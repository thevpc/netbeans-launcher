package net.vpc.app.netbeans.launcher.ui.utils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.reflect.Method;

public class SimpleBasicComboBoxEditor implements ComboBoxEditor, FocusListener {
    protected JTextField editor;
    private Object oldValue;
    private StringMapper mapper;

    public SimpleBasicComboBoxEditor(StringMapper mapper) {
        editor = createEditorComponent();
        this.mapper = mapper;
        if(mapper==null){
            throw new IllegalArgumentException("Mapper could not be null");
        }
    }

    public Component getEditorComponent() {
        return editor;
    }

    /**
     * Creates the internal editor component. Override this to provide
     * a custom implementation.
     *
     * @return a new editor component
     * @since 1.6
     */
    protected JTextField createEditorComponent() {
        JTextField editor = new BorderlessTextField("",9);
        editor.setBorder(null);
        return editor;
    }

    /**
     * Sets the item that should be edited.
     *
     * @param anObject the displayed value of the editor
     */
    public void setItem(Object anObject) {
        String text;

        if ( anObject != null )  {
            text = mapper.toString(anObject);
            if (text == null) {
                text = "";
            }
            oldValue = anObject;
        } else {
            text = "";
        }
        // workaround for 4530952
        if (! text.equals(editor.getText())) {
            editor.setText(text);
        }
    }

    public Object getItem() {
        Object newValue = editor.getText();

        if (oldValue != null && !(oldValue instanceof String))  {
            // The original value is not a string. Should return the value in it's
            // original type.
            if (newValue.equals(mapper.toString(oldValue))) {
                return oldValue;
            } else {
                newValue=mapper.fromString(editor.getText());
            }
        }else {
            newValue=mapper.fromString(editor.getText());
        }
        return newValue;
    }

    public void selectAll() {
        editor.selectAll();
        editor.requestFocus();
    }

    // This used to do something but now it doesn't.  It couldn't be
    // removed because it would be an API change to do so.
    public void focusGained(FocusEvent e) {}

    // This used to do something but now it doesn't.  It couldn't be
    // removed because it would be an API change to do so.
    public void focusLost(FocusEvent e) {}

    public void addActionListener(ActionListener l) {
        editor.addActionListener(l);
    }

    public void removeActionListener(ActionListener l) {
        editor.removeActionListener(l);
    }

    static class BorderlessTextField extends JTextField {
        public BorderlessTextField(String value,int n) {
            super(value,n);
        }

        // workaround for 4530952
        public void setText(String s) {
            if (getText().equals(s)) {
                return;
            }
            super.setText(s);
        }

        public void setBorder(Border b) {
            if (!(b instanceof BasicComboBoxEditor.UIResource)) {
                super.setBorder(b);
            }
        }
    }

    /**
     * A subclass of BasicComboBoxEditor that implements UIResource.
     * BasicComboBoxEditor doesn't implement UIResource
     * directly so that applications can safely override the
     * cellRenderer property with BasicListCellRenderer subclasses.
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases. The current serialization support is
     * appropriate for short term storage or RMI between applications running
     * the same version of Swing.  As of 1.4, support for long term storage
     * of all JavaBeans&trade;
     * has been added to the <code>java.beans</code> package.
     * Please see {@link java.beans.XMLEncoder}.
     */
    public static class UIResource extends BasicComboBoxEditor
            implements javax.swing.plaf.UIResource {
    }
}