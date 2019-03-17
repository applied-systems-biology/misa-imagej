package org.hkijena.misa_imagej.extension.plotbuilder;

import org.hkijena.misa_imagej.ui.workbench.plotbuilder.*;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;
import org.jfree.data.statistics.StatisticalCategoryDataset;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class StatisticalCategoryPlot extends MISAPlot {

    private String categoryAxisLabel;
    private String valueAxisLabel;
    private DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();

    protected StatisticalCategoryPlot(List<MISAPlotSeriesData> seriesDataList) {
        super(seriesDataList);
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
        series.addColumn("X", new MISAStringPlotSeriesColumn(getSeriesDataList(),
                new MISAPlotSeriesGenerator<>("No category", x -> "No category"),
                new MISAPlotSeriesGenerator<>("Row number", x -> "x" + x)));
        series.addColumn("Category", new MISAStringPlotSeriesColumn(getSeriesDataList(),
                new MISAPlotSeriesGenerator<>("No category", x -> "No category")));
        series.addColumn("Value", new MISANumericPlotSeriesColumn(getSeriesDataList(),
                new MISAPlotSeriesGenerator<>("Row index", x -> (double)x)));
        return series;
    }

    public DefaultStatisticalCategoryDataset getDataset() {
        return dataset;
    }

    protected void updateDataset() {
        dataset.clear();
        MISAPlotSeries series = getSeries().get(0);
        int rowCount =  series.getAsNumericColumn("Value").getRequiredRowCount();
        List<String> xvalues = series.getAsStringColumn("X").getValues(rowCount);
        List<String> categories = series.getAsStringColumn("Category").getValues(rowCount);
        List<Double> values = series.getAsNumericColumn("Value").getValues(rowCount);

        Map<String, Map<String, List<Double>>> splitValues = new HashMap<>();

        for(int i = 0; i < values.size(); ++i) {
            String x = xvalues.get(i);
            String category = categories.get(i);
            double value = values.get(i);

            if(!splitValues.containsKey(x))
                splitValues.put(x, new HashMap<>());
            if(!splitValues.get(x).containsKey(category))
                splitValues.get(x).put(category, new ArrayList<>());

            splitValues.get(x).get(category).add(value);
        }

        for(Map.Entry<String, Map<String, List<Double>>> xentry : splitValues.entrySet()) {
            for(Map.Entry<String, List<Double>> categoryEntry : xentry.getValue().entrySet()) {
                double sum = 0;
                double sumSq = 0;
                for(double v : categoryEntry.getValue()) {
                    sum += v;
                    sumSq += v * v;
                }

                double mean = sum / categoryEntry.getValue().size();
                double var = (sumSq / categoryEntry.getValue().size()) - mean * mean;
                dataset.add(mean, Math.sqrt(var), xentry.getKey(), categoryEntry.getKey());
            }
        }
    }

    protected abstract JFreeChart createPlotFromDataset(DefaultStatisticalCategoryDataset dataset);

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
