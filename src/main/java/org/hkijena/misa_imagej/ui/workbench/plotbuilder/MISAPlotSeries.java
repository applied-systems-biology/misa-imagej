package org.hkijena.misa_imagej.ui.workbench.plotbuilder;

import javax.swing.table.DefaultTableModel;
import java.util.List;

public abstract class MISAPlotSeries<T> {
    private DefaultTableModel tableModel;
    private int columnIndex;

    public MISAPlotSeries(DefaultTableModel tableModel, int columnIndex) {
        this.tableModel = tableModel;
        this.columnIndex = columnIndex;
    }

    abstract List<T> getValues();

    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    public int getColumnIndex() {
        return columnIndex;
    }
}
