package org.hkijena.misa_imagej.extension.plotbuilder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.table.DefaultTableModel;

public class StackedBarCategoryPlot extends CategoryPlot {
    public StackedBarCategoryPlot(DefaultTableModel tableModel) {
        super(tableModel);
        setTitle("Bar plot");
    }

    @Override
    protected JFreeChart createPlotFromDataset(DefaultCategoryDataset dataset) {
        JFreeChart chart = ChartFactory.createStackedBarChart(getTitle(), getCategoryAxisLabel(), getValueAxisLabel(), dataset);
        chart.setTitle(getTitle());
        ((BarRenderer)chart.getCategoryPlot().getRenderer()).setBarPainter(new StandardBarPainter());
        return chart;
    }
}
