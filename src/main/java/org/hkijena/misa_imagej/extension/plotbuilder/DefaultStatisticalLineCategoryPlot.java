package org.hkijena.misa_imagej.extension.plotbuilder;

import org.hkijena.misa_imagej.ui.workbench.plotbuilder.MISAPlotSeriesData;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.category.StatisticalLineAndShapeRenderer;

import java.util.List;

public class DefaultStatisticalLineCategoryPlot extends DefaultStatisticalCategoryPlot {

    public DefaultStatisticalLineCategoryPlot(List<MISAPlotSeriesData> seriesDataList) {
        super(seriesDataList);
        setTitle("Line category plot");
    }

    @Override
    protected JFreeChart createPlotFromDataset() {
        JFreeChart chart = ChartFactory.createLineChart(getTitle(), getCategoryAxisLabel(), getValueAxisLabel(), getDataset());
        chart.getCategoryPlot().setRenderer(new StatisticalLineAndShapeRenderer());
        return chart;
    }
}
