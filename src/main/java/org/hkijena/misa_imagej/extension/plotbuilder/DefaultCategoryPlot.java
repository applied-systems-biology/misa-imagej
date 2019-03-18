package org.hkijena.misa_imagej.extension.plotbuilder;

import org.hkijena.misa_imagej.ui.workbench.plotbuilder.MISAPlotSeries;
import org.hkijena.misa_imagej.ui.workbench.plotbuilder.MISAPlotSeriesData;
import org.jfree.data.category.DefaultCategoryDataset;

import java.util.List;

public abstract class DefaultCategoryPlot extends CategoryPlot {

    private DefaultCategoryDataset dataset = new DefaultCategoryDataset();

    public DefaultCategoryPlot(List<MISAPlotSeriesData> seriesDataList) {
        super(seriesDataList);
    }

    @Override
    protected void updateDataset() {
        dataset.clear();
        MISAPlotSeries series = getSeries().get(0);
        int rowCount = series.getMaximumRequiredRowCount();
        List<String> xvalues = series.getAsStringColumn("X").getValues(rowCount);
        List<String> categories = series.getAsStringColumn("Category").getValues(rowCount);
        List<Double> values = series.getAsNumericColumn("Value").getValues(rowCount);
        for(int i = 0; i < xvalues.size(); ++i) {
            dataset.addValue(values.get(i), categories.get(i), xvalues.get(i));
        }
    }

    @Override
    public DefaultCategoryDataset getDataset() {
        return dataset;
    }
}
