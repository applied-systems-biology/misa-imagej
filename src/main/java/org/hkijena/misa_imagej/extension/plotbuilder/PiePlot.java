package org.hkijena.misa_imagej.extension.plotbuilder;

import org.hkijena.misa_imagej.ui.workbench.plotbuilder.MISANumericPlotSeriesColumn;
import org.hkijena.misa_imagej.ui.workbench.plotbuilder.MISAPlot;
import org.hkijena.misa_imagej.ui.workbench.plotbuilder.MISAPlotSeries;
import org.hkijena.misa_imagej.ui.workbench.plotbuilder.MISAStringPlotSeriesColumn;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.table.DefaultTableModel;
import java.util.List;

public abstract class PiePlot extends MISAPlot {

    private DefaultPieDataset dataset = new DefaultPieDataset();

    public PiePlot(DefaultTableModel tableModel) {
        super(tableModel);
        addSeries();
    }

    @Override
    public boolean canRemoveSeries() {
        return false;
    }

    @Override
    public boolean canAddSeries() {
        return getSeries().size() == 0;
    }

    @Override
    protected MISAPlotSeries createSeries() {
        MISAPlotSeries series = new MISAPlotSeries();
        series.addColumn("Category", new MISAStringPlotSeriesColumn(getTableModel(), x -> "Category " + x));
        series.addColumn("Amount", new MISANumericPlotSeriesColumn(getTableModel(), x -> 1.0));
        return series;
    }

    public DefaultPieDataset getDataset() {
        return dataset;
    }

    protected void updateDataset() {
        dataset.clear();
        MISAPlotSeries series = getSeries().get(0);
        List<String> categories = ((MISAStringPlotSeriesColumn)series.getColumns().get("Category")).getValues();
        List<Double> values = ((MISANumericPlotSeriesColumn)series.getColumns().get("Amount")).getValues();

        for(int i = 0; i < categories.size(); ++i) {
            dataset.setValue(categories.get(i), 0);
        }
        for(int i = 0; i < categories.size(); ++i) {
            dataset.setValue(categories.get(i), dataset.getValue(categories.get(i)).doubleValue() + values.get(i));
        }
    }

    protected abstract JFreeChart createPlotFromDataset(DefaultPieDataset dataset);

    @Override
    public final JFreeChart createPlot() {
        updateDataset();
        return createPlotFromDataset(dataset);
    }
}
