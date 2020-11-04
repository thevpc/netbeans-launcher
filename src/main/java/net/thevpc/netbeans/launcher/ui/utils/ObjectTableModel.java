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

    /**
     * The <code>Vector</code> of <code>Vectors</code> of <code>Object</code>
     * values.
     */
    protected List<T> dataVector = new ArrayList<>();

    /**
     * The <code>Vector</code> of column identifiers.
     */
    protected Columns<T> columnIdentifiers;

//
// Constructors
//
    /**
     * Constructs a default <code>DefaultTableModel</code> which is a table of
     * zero columns and zero rows.
     */
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

    /**
     * Returns the <code>Vector</code> of <code>Vectors</code> that contains the
     * table's data values. The vectors contained in the outer vector are each a
     * single row of values. In other words, to get to the cell at row 1, column
     * 5:
     * <p>
     *
     * <code>((Vector)getDataVector().elementAt(1)).elementAt(5);</code>
     *
     * @return the vector of vectors containing the tables data values
     * @see #newDataAvailable
     * @see #newRowsAdded
     * @see #setDataVector
     */
    public List<T> getDataVector() {
        return dataVector;
    }

    public void setDataVector(List<T> dataVector) {
        this.dataVector = dataVector == null ? new ArrayList<>() : new ArrayList<>(dataVector);
        fireTableStructureChanged();
    }

    /**
     * Equivalent to <code>fireTableChanged</code>.
     *
     * @param event the change event
     */
    public void newDataAvailable(TableModelEvent event) {
        fireTableChanged(event);
    }

    /**
     * Ensures that the new rows have the correct number of columns. This is
     * accomplished by using the <code>setSize</code> method in
     * <code>Vector</code> which truncates vectors which are too long, and
     * appends <code>null</code>s if they are too short. This method also sends
     * out a <code>tableChanged</code> notification message to all the
     * listeners.
     *
     * @param e this <code>TableModelEvent</code> describes where the rows were
     * added. If <code>null</code> it assumes all the rows were newly added
     * @see #getDataVector
     */
    public void newRowsAdded(TableModelEvent e) {
        fireTableChanged(e);
    }

    /**
     * Equivalent to <code>fireTableChanged</code>.
     *
     * @param event the change event
     */
    public void rowsRemoved(TableModelEvent event) {
        fireTableChanged(event);
    }

    /**
     * Obsolete as of Java 2 platform v1.3. Please use <code>setRowCount</code>
     * instead.
     */
    /*
     *  Sets the number of rows in the model.  If the new size is greater
     *  than the current size, new rows are added to the end of the model
     *  If the new size is less than the current size, all
     *  rows at index <code>rowCount</code> and greater are discarded.
     *
     * @param   rowCount   the new number of rows
     * @see #setRowCount
     */
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

    /**
     * Sets the number of rows in the model. If the new size is greater than the
     * current size, new rows are added to the end of the model If the new size
     * is less than the current size, all rows at index <code>rowCount</code>
     * and greater are discarded.
     *
     * @since 1.3
     */
    public void setRowCount(int rowCount) {
        setNumRows(rowCount);
    }

    /**
     * Adds a row to the end of the model. The new row will contain
     * <code>null</code> values unless <code>rowData</code> is specified.
     * Notification of the row being added will be generated.
     *
     * @param rowData optional data of the row being added
     */
    public void addRow(T rowData) {
        insertRow(getRowCount(), rowData);
    }

    /**
     * Inserts a row at <code>row</code> in the model. The new row will contain
     * <code>null</code> values unless <code>rowData</code> is specified.
     * Notification of the row being added will be generated.
     *
     * @param row the row index of the row to be inserted
     * @param rowData optional data of the row being added
     * @throws ArrayIndexOutOfBoundsException if the row was invalid
     */
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

    /**
     * Moves one or more rows from the inclusive range <code>start</code> to
     * <code>end</code> to the <code>to</code> position in the model. After the
     * move, the row that was at index <code>start</code> will be at index
     * <code>to</code>. This method will send a <code>tableChanged</code>
     * notification message to all the listeners.
     *
     * <pre>
     *  Examples of moves:
     *
     *  1. moveRow(1,3,5);
     *          a|B|C|D|e|f|g|h|i|j|k   - before
     *          a|e|f|g|h|B|C|D|i|j|k   - after
     *
     *  2. moveRow(6,7,1);
     *          a|b|c|d|e|f|G|H|i|j|k   - before
     *          a|G|H|b|c|d|e|f|i|j|k   - after
     * </pre>
     *
     * @param start the starting row index to be moved
     * @param end the ending row index to be moved
     * @param to the destination of the rows to be moved
     * @throws ArrayIndexOutOfBoundsException if any of the elements would be
     * moved out of the table's range
     */
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

    /**
     * Removes the row at <code>row</code> from the model. Notification of the
     * row being removed will be sent to all the listeners.
     *
     * @param row the row index of the row to be removed
     * @throws ArrayIndexOutOfBoundsException if the row was invalid
     */
    public void removeRow(int row) {
        dataVector.remove(row);
        fireTableRowsDeleted(row, row);
    }

//
// Manipulating columns
//
    /**
     * Replaces the column identifiers in the model. If the number of
     * <code>newIdentifier</code>s is greater than the current number of
     * columns, new columns are added to the end of each row in the model. If
     * the number of <code>newIdentifier</code>s is less than the current number
     * of columns, all the extra columns at the end of a row are discarded.
     *
     * @param columnIdentifiers vector of column identifiers. If
     * <code>null</code>, set the model to zero columns
     * @see #setNumRows
     */
    public void setColumnIdentifiers(Columns<T> columnIdentifiers) {
        this.columnIdentifiers = columnIdentifiers;
        fireTableStructureChanged();
    }

    public Columns<T> getColumnIdentifiers() {
        return columnIdentifiers;
    }
    

    /**
     * Returns the number of rows in this data table.
     *
     * @return the number of rows in the model
     */
    public final int getRowCount() {
        return dataVector.size();
    }

    /**
     * Returns the number of columns in this data table.
     *
     * @return the number of columns in the model
     */
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

    /**
     * Returns the column name.
     *
     * @return a name for this column using the string value of the appropriate
     * member in <code>columnIdentifiers</code>. If
     * <code>columnIdentifiers</code> does not have an entry for this index,
     * returns the default name provided by the superclass.
     */
    public String getColumnName(int column) {
        Columns<T> c = desc();
        String n = c.getColumnName(column);
        if (n == null) {
            n = super.getColumnName(column);
        }
        return n;
    }

    /**
     * Returns true regardless of parameter values.
     *
     * @param row the row whose value is to be queried
     * @param column the column whose value is to be queried
     * @return true
     * @see #setValueAt
     */
    public boolean isCellEditable(int row, int column) {
        return desc().isCellEditable(row, column, getObject(row));
    }

    /**
     * Returns an attribute value for the cell at <code>row</code> and
     * <code>column</code>.
     *
     * @param row the row whose value is to be queried
     * @param column the column whose value is to be queried
     * @return the value Object at the specified cell
     * @throws ArrayIndexOutOfBoundsException if an invalid row or column was
     * given
     */
    public Object getValueAt(int row, int column) {
        return desc().getValueAt(row, column, getObject(row));
    }

    public T getObject(int row) {
        return row<dataVector.size()? dataVector.get(row):null;
    }

    /**
     * Sets the object value for the cell at <code>column</code> and
     * <code>row</code>.  <code>aValue</code> is the new value. This method will
     * generate a <code>tableChanged</code> notification.
     *
     * @param aValue the new value; this can be null
     * @param row the row whose value is to be changed
     * @param column the column whose value is to be changed
     * @throws ArrayIndexOutOfBoundsException if an invalid row or column was
     * given
     */
    public void setValueAt(Object aValue, int row, int column) {
        desc().setValueAt(row, column, dataVector.get(row), aValue);
        fireTableCellUpdated(row, column);
    }

} // End of class DefaultTableModel
