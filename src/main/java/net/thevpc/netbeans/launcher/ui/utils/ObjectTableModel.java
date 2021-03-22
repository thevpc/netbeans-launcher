package net.thevpc.netbeans.launcher.ui.utils;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ObjectTableModel<T> extends AbstractTableModel implements Serializable {

    public static abstract class NamedColumns<T> implements Columns<T> {

        private String[] columnIds;
        private String[] columnNames;
        private float[] columnSizes;
        private boolean editable;

        public NamedColumns(String[] columnIds) {
            this(false, columnIds, null);
        }

        public NamedColumns(boolean editable, String[] columnIds) {
            this(editable, columnIds, null);
        }

        public NamedColumns(boolean editable, String[] columnIds, String[] columnNames) {
            this.columnIds = columnIds;
            this.editable = editable;
            if (columnNames == null) {
                columnNames = columnIds;
            }
            this.columnNames = columnNames;
        }

        public float[] getColumnSizes() {
            return columnSizes;
        }

        public NamedColumns setColumnSizes(float[] columnSizes) {
            this.columnSizes = columnSizes;
            return this;
        }

        int columnIndex(String n) {
            for (int i = 0; i < columnIds.length; i++) {
                String columnId = columnIds[i];
                if (n.equals(columnId)) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public final int size() {
            return columnIds.length;
        }

        @Override
        public final String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public final boolean isCellEditable(int row, int column, T t) {
            return isCellEditable(row, columnIds[column], t);
        }

        @Override
        public final Object getValueAt(int row, int column, T t) {
            return getValueAt(row, columnIds[column], t);
        }

        @Override
        public final void setValueAt(int row, int column, T t, Object columnValue) {
            setValueAt(row, columnIds[column], t, columnValue);
        }

        public boolean isCellEditable(int row, String column, T t) {
            return editable;
        }

        public Object getValueAt(int row, String column, T t) {
            return t;
        }

        public void setValueAt(int row, String column, T t, Object columnValue) {

        }
    }

    public interface Columns<T> {

        int size();

        String getColumnName(int column);

        default boolean isCellEditable(int row, int column, T t) {
            return true;
        }

        default Object getValueAt(int row, int column, T t) {
            return t;
        }

        default void setValueAt(int row, int column, T t, Object columnValue) {

        }
        
        default float[] getColumnSizes() {
            return null;
        }
    }

    protected List<T> dataVector = new ArrayList<>();

    protected Columns<T> columnIdentifiers;

    public ObjectTableModel(Columns<T> columnIdentifiers) {
        this.columnIdentifiers = columnIdentifiers;
    }

    public void clear() {
        int size = dataVector.size();
        if (size > 0) {
            dataVector.clear();
            fireTableRowsDeleted(0, size - 1);
        }
    }

    public List<T> getDataVector() {
        return dataVector;
    }

    public void setDataVector(List<T> dataVector) {
        this.dataVector = dataVector == null ? new ArrayList<>() : new ArrayList<>(dataVector);
        fireTableStructureChanged();
    }

    public void newDataAvailable(TableModelEvent event) {
        fireTableChanged(event);
    }

    public void newRowsAdded(TableModelEvent e) {
        fireTableChanged(e);
    }

    public void rowsRemoved(TableModelEvent event) {
        fireTableChanged(event);
    }

    public void setNumRows(int rowCount) {
        int old = getRowCount();
        if (old == rowCount) {
            return;
        }
        if (rowCount < 0) {
            return;
        }
        while (dataVector.size() < rowCount) {
            dataVector.add(null);
        }
        while (dataVector.size() > rowCount) {
            dataVector.remove(dataVector.size() - 1);
        }
        if (rowCount <= old) {
            fireTableRowsDeleted(rowCount, old - 1);
        } else {
            fireTableRowsInserted(old, rowCount - 1);
        }
    }

    public void setRowCount(int rowCount) {
        setNumRows(rowCount);
    }

    public void addRow(T rowData) {
        insertRow(getRowCount(), rowData);
    }

    public void insertRow(int row, T rowData) {
        dataVector.add(row, rowData);
        fireTableRowsInserted(row, row);
    }

    private static int gcd(int i, int j) {
        return (j == 0) ? i : gcd(j, i % j);
    }

    private static <T> void rotate(List<T> v, int a, int b, int shift) {
        int size = b - a;
        int r = size - shift;
        int g = gcd(size, r);
        for (int i = 0; i < g; i++) {
            int to = i;
            T tmp = v.get(a + to);
            for (int from = (to + r) % size; from != i; from = (to + r) % size) {
                v.set(a + to, v.get(a + from));
                to = from;
            }
            v.set(a + to, tmp);
        }
    }

    public void moveRow(int start, int end, int to) {
        int shift = to - start;
        int first, last;
        if (shift < 0) {
            first = to;
            last = end;
        } else {
            first = start;
            last = to + end - start;
        }
        rotate(dataVector, first, last + 1, shift);

        fireTableRowsUpdated(first, last);
    }

    public void removeRow(int row) {
        dataVector.remove(row);
        fireTableRowsDeleted(row, row);
    }

    public void setColumnIdentifiers(Columns<T> columnIdentifiers) {
        this.columnIdentifiers = columnIdentifiers;
        fireTableStructureChanged();
    }

    public Columns<T> getColumnIdentifiers() {
        return columnIdentifiers;
    }
    

    @Override
    public final int getRowCount() {
        return dataVector.size();
    }

    @Override
    public int getColumnCount() {
        if (columnIdentifiers == null) {
            return 0;
        }
        return columnIdentifiers.size();
    }

    private Columns<T> desc() {
        Columns<T> c = columnIdentifiers;
        if (columnIdentifiers == null) {
            c = new Columns<T>() {
                @Override
                public int size() {
                    return 1;
                }

                @Override
                public String getColumnName(int column) {
                    return ObjectTableModel.this.getColumnName(column);
                }
            };
        }
        return c;
    }

    @Override
    public String getColumnName(int column) {
        Columns<T> c = desc();
        String n = c.getColumnName(column);
        if (n == null) {
            n = super.getColumnName(column);
        }
        return n;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return desc().isCellEditable(row, column, getObject(row));
    }

    @Override
    public Object getValueAt(int row, int column) {
        return desc().getValueAt(row, column, getObject(row));
    }

    public T getObject(int row) {
        return row<dataVector.size()? dataVector.get(row):null;
    }

    public void setValueAt(Object aValue, int row, int column) {
        desc().setValueAt(row, column, dataVector.get(row), aValue);
        fireTableCellUpdated(row, column);
    }

}
