package org.hkijena.misa_imagej.extension.plotbuilder;

import org.hkijena.misa_imagej.ui.workbench.plotbuilder.MISAPlotSeriesData;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.table.DefaultTableModel;
import java.util.List;

public class LineCategoryPlot extends CategoryPlot {
    public LineCategoryPlot(List<MISAPlotSeriesData> seriesDataList) {
        super(seriesDataList);
        setTitle("Line category plot");
    }

    @Override
    protected JFreeChart createPlotFromDataset(DefaultCategoryDataset dataset) {
        JFreeChart chart = ChartFactory.createLineChart(getTitle(), getCategoryAxisLabel(), getValueAxisLabel(), dataset);
        chart.setTitle(getTitle());
        return chart;
    }
}
