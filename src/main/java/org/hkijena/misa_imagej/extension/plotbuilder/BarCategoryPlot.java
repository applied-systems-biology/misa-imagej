package org.hkijena.misa_imagej.extension.plotbuilder;

import org.hkijena.misa_imagej.ui.workbench.plotbuilder.MISAPlotSeriesData;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.table.DefaultTableModel;
import java.util.List;

public class BarCategoryPlot extends CategoryPlot {
    public BarCategoryPlot(List<MISAPlotSeriesData> seriesDataList) {
        super(seriesDataList);
        setTitle("Bar plot");
    }

    @Override
    protected JFreeChart createPlotFromDataset(DefaultCategoryDataset dataset) {
        JFreeChart chart = ChartFactory.createBarChart(getTitle(), getCategoryAxisLabel(), getValueAxisLabel(), dataset);
        ((BarRenderer)chart.getCategoryPlot().getRenderer()).setBarPainter(new StandardBarPainter());
        return chart;
    }
}
