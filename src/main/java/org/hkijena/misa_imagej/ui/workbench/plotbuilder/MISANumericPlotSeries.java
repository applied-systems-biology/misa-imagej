package org.hkijena.misa_imagej.ui.workbench.plotbuilder;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

public class MISANumericPlotSeries extends MISAPlotSeries<Double> {
    public MISANumericPlotSeries(DefaultTableModel tableModel, int columnIndex) {
        super(tableModel, columnIndex);
    }

    @Override
    List<Double> getValues() {
        List<Double> result = new ArrayList<>(getTableModel().getRowCount());
        for(int row = 0; row < getTableModel().getRowCount(); ++row) {
            Object value = getTableModel().getValueAt(row, getColumnIndex());
            if(value instanceof Number) {
                result.add(((Number) value).doubleValue());
            }
            else {
                try {
                    result.add(Double.parseDouble("" + value));
                }
                catch (NumberFormatException e) {
                    result.add(0.0);
                }
            }
        }
        return result;
    }

    public static List<Double> getGeneratedValues(int rows) {
        List<Double> result = new ArrayList<>(rows);
        for(int row = 0; row < rows; ++row) {
            result.add((double) row);
        }
        return result;
    }
}
