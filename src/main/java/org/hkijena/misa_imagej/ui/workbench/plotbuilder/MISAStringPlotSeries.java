package org.hkijena.misa_imagej.ui.workbench.plotbuilder;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

public class MISAStringPlotSeries extends MISAPlotSeries<String> {
    public MISAStringPlotSeries(DefaultTableModel tableModel, int columnIndex) {
        super(tableModel, columnIndex);
    }

    @Override
    List<String> getValues() {
        List<String> result = new ArrayList<>(getTableModel().getRowCount());
        for(int row = 0; row < getTableModel().getRowCount(); ++row) {
            Object value = getTableModel().getValueAt(row, getColumnIndex());
            result.add("" + value);
        }
        return result;
    }

    public static List<String> getGeneratedValues(int rows) {
        List<String> result = new ArrayList<>(rows);
        for(int row = 0; row < rows; ++row) {
            result.add("Value " + row);
        }
        return result;
    }
}
