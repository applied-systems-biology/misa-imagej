package org.hkijena.misa_imagej.extension.plotbuilder;

import org.hkijena.misa_imagej.ui.workbench.plotbuilder.MISAPlotSeries;
import org.hkijena.misa_imagej.ui.workbench.plotbuilder.MISAPlotSeriesData;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DefaultBoxAndWhiskerCategoryPlot extends CategoryPlot {
    private DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();

    protected DefaultBoxAndWhiskerCategoryPlot(List<MISAPlotSeriesData> seriesDataList) {
        super(seriesDataList);
        addSeries();
    }

    @Override
    public CategoryDataset getDataset() {
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
            for (Map.Entry<String, List<Double>> categoryEntry : xentry.getValue().entrySet()) {
                dataset.add(categoryEntry.getValue(), categoryEntry.getKey(), xentry.getKey());
            }
        }
    }
}
