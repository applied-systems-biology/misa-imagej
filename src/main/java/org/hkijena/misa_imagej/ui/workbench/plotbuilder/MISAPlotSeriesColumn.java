package org.hkijena.misa_imagej.ui.workbench.plotbuilder;

import com.google.common.eventbus.EventBus;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class MISAPlotSeriesColumn<T> {
    private DefaultTableModel tableModel;
    private Function<Integer, T> generatorFunction;
    private int columnIndex = -1;
    private EventBus eventBus = new EventBus();

    public MISAPlotSeriesColumn(DefaultTableModel tableModel, Function<Integer, T> generatorFunction) {
        this.tableModel = tableModel;
        this.generatorFunction = generatorFunction;
    }

    public List<T> getValues() {
        if(columnIndex < 0) {
            List<T> result = new ArrayList<>(tableModel.getRowCount());
            for(int row = 0; row < tableModel.getRowCount(); ++row) {
                result.add(generatorFunction.apply(row));
            }
            return result;
        }
        else {
            return getValuesFromTable();
        }
    }

    protected abstract List<T> getValuesFromTable();

    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
        eventBus.post(new DataChangedEvent(this));
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public static class DataChangedEvent {
        private MISAPlotSeriesColumn seriesColumn;

        public DataChangedEvent(MISAPlotSeriesColumn seriesColumn) {
            this.seriesColumn = seriesColumn;
        }

        public MISAPlotSeriesColumn getSeriesColumn() {
            return seriesColumn;
        }
    }
}
