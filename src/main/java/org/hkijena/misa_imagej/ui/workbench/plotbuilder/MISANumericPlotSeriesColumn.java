package org.hkijena.misa_imagej.ui.workbench.plotbuilder;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MISANumericPlotSeriesColumn extends MISAPlotSeriesColumn<Double> {

    public MISANumericPlotSeriesColumn(DefaultTableModel tableModel, Function<Integer, Double> generatorFunction) {
        super(tableModel, generatorFunction);
    }

    @Override
    protected List<Double> getValuesFromTable() {
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
}
