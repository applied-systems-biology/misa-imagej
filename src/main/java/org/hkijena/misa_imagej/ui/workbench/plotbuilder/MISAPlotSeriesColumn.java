package org.hkijena.misa_imagej.ui.workbench.plotbuilder;

import com.google.common.eventbus.EventBus;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class MISAPlotSeriesColumn<T> {
    private DefaultTableModel tableModel;
    private List<MISAPlotSeriesGenerator<T>> generators;
    private int columnIndex = -1;
    private EventBus eventBus = new EventBus();

    @SafeVarargs
    public MISAPlotSeriesColumn(DefaultTableModel tableModel, MISAPlotSeriesGenerator<T> defaultGenerator, MISAPlotSeriesGenerator<T>... additionalGenerators) {
        this.tableModel = tableModel;
        this.generators = new ArrayList<>();
        this.generators.add(defaultGenerator);
        this.generators.addAll(Arrays.asList(additionalGenerators));
    }

    public List<T> getValues() {
        if(columnIndex < 0) {
            MISAPlotSeriesGenerator<T> generator = generators.get(-columnIndex - 1);
            List<T> result = new ArrayList<>(tableModel.getRowCount());
            for(int row = 0; row < tableModel.getRowCount(); ++row) {
                result.add(generator.getGeneratorFunction().apply(row));
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

    public List<MISAPlotSeriesGenerator<T>> getGenerators() {
        return Collections.unmodifiableList(generators);
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
