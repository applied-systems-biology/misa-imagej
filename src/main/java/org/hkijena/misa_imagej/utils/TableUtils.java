package org.hkijena.misa_imagej.utils;

import javax.swing.table.DefaultTableModel;
import java.util.Vector;

public class TableUtils {
    private TableUtils() {

    }

    public static DefaultTableModel cloneTableModel(DefaultTableModel tableModel) {
        DefaultTableModel copy = new DefaultTableModel();
        copy.setColumnCount(tableModel.getColumnCount());
        {
            Object[] identifiers = new Object[tableModel.getColumnCount()];
            for (int i = 0; i < tableModel.getColumnCount(); ++i) {
                identifiers[i] = tableModel.getColumnName(i);
            }
            copy.setColumnIdentifiers(identifiers);
        }
        for(int row = 0; row < tableModel.getRowCount(); ++row) {
            Vector<Object> rowVector = new Vector<>(tableModel.getColumnCount());
            for(int column = 0; column < tableModel.getColumnCount(); ++column) {
                rowVector.add(tableModel.getValueAt(row, column));
            }
            copy.addRow(rowVector);
        }
        return copy;
    }

    public static Vector<String> getColumnIdentifiers(DefaultTableModel tableModel) {
        Vector<String> vector = new Vector<>(tableModel.getColumnCount());
        for(int i = 0; i < tableModel.getColumnCount(); ++i) {
            vector.add(tableModel.getColumnName(i));
        }
        return vector;
    }

    public static void addNameToColumnIdentifiers(DefaultTableModel tableModel, String name) {
        Vector<String> identifiers = getColumnIdentifiers(tableModel);
        for(int i = 0; i < identifiers.size(); ++i) {
            identifiers.set(i, name + "." + identifiers.get(i));
        }
        tableModel.setColumnIdentifiers(identifiers);
    }
}
