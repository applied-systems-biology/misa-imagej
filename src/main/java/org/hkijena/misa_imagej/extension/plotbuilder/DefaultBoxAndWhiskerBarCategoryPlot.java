package org.hkijena.misa_imagej.extension.plotbuilder;

import org.hkijena.misa_imagej.ui.workbench.plotbuilder.MISAPlotSeriesData;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;

import java.util.List;

public class DefaultBoxAndWhiskerBarCategoryPlot extends DefaultBoxAndWhiskerCategoryPlot {

    public DefaultBoxAndWhiskerBarCategoryPlot(List<MISAPlotSeriesData> seriesDataList) {
        super(seriesDataList);
    }

    @Override
    protected JFreeChart createPlotFromDataset() {
        return ChartFactory.createBoxAndWhiskerChart(getTitle(), getCategoryAxisLabel(), getValueAxisLabel(), (BoxAndWhiskerCategoryDataset)getDataset(), true);
    }
}
