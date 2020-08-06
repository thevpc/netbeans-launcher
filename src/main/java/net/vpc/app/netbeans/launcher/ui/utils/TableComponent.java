/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.netbeans.launcher.ui.utils;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author vpc
 */
public class TableComponent implements CatalogComponent {

    private Equalizer equalizer;
    private JTable table;

    public TableComponent() {
        this.table = new JTable(new ObjectTableModel(null));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setBorder(new EmptyBorder(2, 2, 2, 2));
        table.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                repaint();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                repaint();
            }

            @Override
            public void componentShown(ComponentEvent e) {
                repaint();
            }

            @Override
            public void componentHidden(ComponentEvent e) {

            }
        });
    }

    @Override
    public void addEnterSelection(ObjectSelectionListener a) {
        SwingUtils2.addEnterAction(table, new ButtonAction() {
            @Override
            public void action() {
                int i = getSelectedIndex();
                Object v = getSelectedValue();
                if (i >= 0) {
                    a.onObjectSelected(new ObjectSelectionEvent(TableComponent.this, v, i, null));
                }
            }
        });
    }

    public JTable getTable() {
        return table;
    }

    @Override
    public Equalizer getEqualizer() {
        return equalizer;
    }

    @Override
    public void setEqualizer(Equalizer equalizer) {
        this.equalizer = equalizer;
    }

    @Override
    public void setElementHeight(int h) {
        table.setRowHeight(h);
    }

    @Override
    public void setSelectedValue(Object i) {
        int o = indexOf(i);
        if (o >= 0) {
            setSelectedIndex(o);
        }
    }

    @Override
    public void addListSelectionListener(ObjectSelectionListener d) {
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int t = e.getFirstIndex();
                    if (t < 0) {

                    } else {
                        d.onObjectSelected(new ObjectSelectionEvent(TableComponent.this, getModel().getObject(t), t, null));
                    }
                }
            }
        });
    }

    @Override
    public void addMouseSelection(ObjectSelectionListener d) {
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JTable list = (JTable) evt.getSource();
                int index = list.rowAtPoint(evt.getPoint());
                if (index >= 0) {
                    d.onObjectSelected(new ObjectSelectionEvent(TableComponent.this, getModel().getObject(index), index, evt));
                }
            }

        });
    }

    public TableComponent setColumns(ObjectTableModel.Columns d) {
        getModel().setColumnIdentifiers(d);
        repaint();
        return this;
    }

    @Override
    public List<Object> getValues() {
        return getModel().getDataVector();
    }

    @Override
    public void setValues(List<Object> a) {
        ObjectTableModel model = getModel();
        model.clear();
        for (Object object : a) {
            model.addRow(object);
        }
    }

    @Override
    public void clearValues() {
        getModel().clear();
    }

    @Override
    public int indexOf(Object a) {
        ObjectTableModel m = getModel();
        int s = getModel().getRowCount();
        Equalizer e = getEqualizer();
        if (e == null) {
            e = Objects::equals;
        }
        for (int i = 0; i < s; i++) {
            if (e.equals(m.getObject(i), a)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void removeValue(Object a) {
        int i = indexOf(a);
        if (i >= 0) {
            getModel().removeRow(i);
        }
    }

    @Override
    public void addValue(Object a) {
        getModel().addRow(a);
    }

    protected ObjectTableModel getModel() {
        return (ObjectTableModel) table.getModel();
    }

    @Override
    public Object getSelectedValue() {
        int i = getSelectedIndex();
        if (i >= 0) {
            ObjectTableModel model = (ObjectTableModel) table.getModel();
            return model.getObject(i);
        }
        return null;
    }

    @Override
    public int getSelectedIndex() {
        return table.getSelectedRow();
    }

    @Override
    public void setSelectedIndex(int i) {
        table.getSelectionModel().setSelectionInterval(i, i);
    }

    @Override
    public JComponent toComponent() {
        return table;
    }

    @Override
    public void repaint() {
        table.invalidate();
        table.revalidate();
        table.repaint();
        ObjectTableModel.Columns c = getModel().getColumnIdentifiers();
        if (c != null) {
            float[] s = c.getColumnSizes();
            if (s != null && s.length > 0) {
                SwingUtils2.setWidthAsPercentages(table, s);
            }
        }
    }

    public Object getValue(int row) {
        return getModel().getObject(row);
    }
}
