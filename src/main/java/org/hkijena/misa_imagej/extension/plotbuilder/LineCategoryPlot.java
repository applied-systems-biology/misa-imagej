package org.hkijena.misa_imagej.extension.plotbuilder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.table.DefaultTableModel;

public class LineCategoryPlot extends CategoryPlot {
    public LineCategoryPlot(DefaultTableModel tableModel) {
        super(tableModel);
        setTitle("Line category plot");
    }

    @Override
    protected JFreeChart createPlotFromDataset(DefaultCategoryDataset dataset) {
        JFreeChart chart = ChartFactory.createLineChart(getTitle(), getCategoryAxisLabel(), getValueAxisLabel(), dataset);
        chart.setTitle(getTitle());
        return chart;
    }
}
