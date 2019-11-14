/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.netbeans.launcher.ui.utils;

import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.JComponent;

/**
 *
 * @author vpc
 */
public interface CatalogComponent {

    JComponent toComponent();

    void setEqualizer(Equalizer e);

    Equalizer getEqualizer();

    Object getSelectedValue();

    int getSelectedIndex();

    void setSelectedValue(Object i);

    void setSelectedIndex(int i);

    List<Object> getValues();

    void clearValues();

    void removeValue(Object a);

    void addValue(Object a);

    int indexOf(Object a);

    void setValues(List<Object> a);

    void addListSelectionListener(ObjectSelectionListener d);

    void addMouseSelection(ObjectSelectionListener d);

    void addEnterSelection(ObjectSelectionListener a);

    void setElementHeight(int h);

    void repaint();

    interface ObjectSelectionListener {

        void onObjectSelected(ObjectSelectionEvent event);
    }

    class ObjectSelectionEvent {

        private CatalogComponent component;
        private Object value;
        private int index;
        private MouseEvent mouseEvent;

        public ObjectSelectionEvent(CatalogComponent component, Object value, int index, MouseEvent event) {
            this.component = component;
            this.value = value;
            this.index = index;
            this.mouseEvent = event;
        }

        public MouseEvent getMouseEvent() {
            return mouseEvent;
        }

        public CatalogComponent getComponent() {
            return component;
        }

        public void setComponent(CatalogComponent component) {
            this.component = component;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

    }
}
