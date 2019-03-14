package org.hkijena.misa_imagej.extension.plotbuilder;

import org.hkijena.misa_imagej.ui.workbench.plotbuilder.MISANumericPlotSeriesColumn;
import org.hkijena.misa_imagej.ui.workbench.plotbuilder.MISAPlot;
import org.hkijena.misa_imagej.ui.workbench.plotbuilder.MISAPlotSeries;
import org.hkijena.misa_imagej.ui.workbench.plotbuilder.MISAStringPlotSeriesColumn;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.table.DefaultTableModel;
import java.util.List;

public abstract class CategoryPlot extends MISAPlot {

    private String categoryAxisLabel;
    private String valueAxisLabel;
    private DefaultCategoryDataset dataset = new DefaultCategoryDataset();

    public CategoryPlot(DefaultTableModel tableModel) {
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
        series.addColumn("X Axis", new MISAStringPlotSeriesColumn(getTableModel(), x -> "x" + x));
        series.addColumn("Category", new MISAStringPlotSeriesColumn(getTableModel(), x -> "No category"));
        series.addColumn("Value", new MISANumericPlotSeriesColumn(getTableModel(), x -> (double)x));
        return series;
    }

    public DefaultCategoryDataset getDataset() {
        return dataset;
    }

    protected void updateDataset() {
        dataset.clear();
        MISAPlotSeries series = getSeries().get(0);
        List<String> xvalues = ((MISAStringPlotSeriesColumn)series.getColumns().get("X Axis")).getValues();
        List<String> categories = ((MISAStringPlotSeriesColumn)series.getColumns().get("Category")).getValues();
        List<Double> values = ((MISANumericPlotSeriesColumn)series.getColumns().get("Value")).getValues();
        for(int i = 0; i < xvalues.size(); ++i) {
            dataset.addValue(values.get(i), categories.get(i), xvalues.get(i));
        }
    }

    protected abstract JFreeChart createPlotFromDataset(DefaultCategoryDataset dataset);

    @Override
    public final JFreeChart createPlot() {
        updateDataset();
        return createPlotFromDataset(dataset);
    }

    public String getCategoryAxisLabel() {
        return categoryAxisLabel;
    }

    public void setCategoryAxisLabel(String categoryAxisLabel) {
        this.categoryAxisLabel = categoryAxisLabel;
        getEventBus().post(new PlotChangedEvent(this));
    }

    public String getValueAxisLabel() {
        return valueAxisLabel;
    }

    public void setValueAxisLabel(String valueAxisLabel) {
        this.valueAxisLabel = valueAxisLabel;
        getEventBus().post(new PlotChangedEvent(this));
    }
}
