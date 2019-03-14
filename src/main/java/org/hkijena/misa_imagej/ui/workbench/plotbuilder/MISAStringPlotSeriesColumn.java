package org.hkijena.misa_imagej.ui.workbench.plotbuilder;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MISAStringPlotSeriesColumn extends MISAPlotSeriesColumn<String> {

    public MISAStringPlotSeriesColumn(DefaultTableModel tableModel, Function<Integer, String> generatorFunction) {
        super(tableModel, generatorFunction);
    }

    @Override
    protected List<String> getValuesFromTable() {
        List<String> result = new ArrayList<>(getTableModel().getRowCount());
        for(int row = 0; row < getTableModel().getRowCount(); ++row) {
            Object value = getTableModel().getValueAt(row, getColumnIndex());
            result.add("" + value);
        }
        return result;
    }
}
