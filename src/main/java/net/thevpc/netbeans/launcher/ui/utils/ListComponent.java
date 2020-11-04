/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.netbeans.launcher.ui.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.thevpc.netbeans.launcher.util.JlistToStringer;

/**
 *
 * @author vpc
 */
public class ListComponent implements CatalogComponent {

    private Equalizer equalizer;
    private JList list;
    private JlistToStringer stringer;

    public ListComponent() {
        list = new JList(new DefaultListModel());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setBorder(new EmptyBorder(2, 2, 2, 2));
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, stringer == null ? value : stringer.toString(list, value), index, isSelected, cellHasFocus);
                if (!isSelected) {
                    setBackground(index % 2 == 0 ? Color.WHITE : SwingUtils2.color("f9f9f9"));
                } else {
                    setBackground(SwingUtils2.color("0096c9"));
                }
                return this;
            }
        });
    }

    @Override
    public void setSelectedValue(Object i) {
        int o = indexOf(i);
        if (o >= 0) {
            setSelectedIndex(o);
        }
    }

    @Override
    public Equalizer getEqualizer() {
        return equalizer;
    }

    @Override
    public void setEqualizer(Equalizer equalizer) {
        this.equalizer = equalizer;
    }

    public JList getList() {
        return list;
    }

    @Override
    public void clearValues() {
        getModel().clear();
    }

    @Override
    public void addValue(Object a) {
        getModel().addElement(a);
    }

    @Override
    public void removeValue(Object a) {
        getModel().removeElement(a);
    }

    @Override
    public void repaint() {
        list.invalidate();
        list.revalidate();
        list.repaint();
    }

    @Override
    public void setElementHeight(int h) {
        list.setFixedCellHeight(h);
    }

    public ListComponent setStringer(JlistToStringer stringer) {
        this.stringer = stringer;
        return this;
    }

    @Override
    public void addEnterSelection(ObjectSelectionListener a) {
        SwingUtils2.addEnterAction(list, new ButtonAction() {
            @Override
            public void action() {
                int i = getSelectedIndex();
                Object v = getSelectedValue();
                if (i >= 0) {
                    a.onObjectSelected(new ObjectSelectionEvent(ListComponent.this, v, i, null));
                }
            }
        });
    }

    @Override
    public void addListSelectionListener(ObjectSelectionListener d) {
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int t = e.getFirstIndex();
                    if (t < 0) {

                    } else {
                        d.onObjectSelected(new ObjectSelectionEvent(ListComponent.this, getModel().get(t), t, null));
                    }
                }
            }
        });
    }

    @Override
    public List<Object> getValues() {
        return new ArrayList<>(Arrays.asList(getModel().toArray()));
    }

    @Override
    public void addMouseSelection(ObjectSelectionListener d) {
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                int index = list.locationToIndex(evt.getPoint());
                if (index >= 0) {
                    d.onObjectSelected(new ObjectSelectionEvent(ListComponent.this, getModel().get(index), index, evt));
                }
            }

        });
    }

    @Override
    public void setValues(List<Object> a) {
        DefaultListModel model = getModel();
        model.clear();
        for (Object object : a) {
            model.addElement(object);
        }
    }

    @Override
    public int indexOf(Object a) {
        Equalizer e = getEqualizer();
        DefaultListModel m = getModel();
        int s = getModel().size();
        if (e == null) {
            e = Objects::equals;
        }
        for (int i = 0; i < s; i++) {
            if (e.equals(m.get(i), a)) {
                return i;
            }
        }
        return -1;
    }

    protected DefaultListModel getModel() {
        return (DefaultListModel) list.getModel();
    }

    @Override
    public JComponent toComponent() {
        return list;
    }

    @Override
    public Object getSelectedValue() {
        return list.getSelectedValue();
    }

    @Override
    public int getSelectedIndex() {
        return list.getSelectedIndex();
    }

    @Override
    public void setSelectedIndex(int i) {
        list.setSelectedIndex(i);
        if (i >= 0) {
            list.ensureIndexIsVisible(i);
            list.repaint();
        }
    }
}
