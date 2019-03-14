package org.hkijena.misa_imagej.extension.plotbuilder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.table.DefaultTableModel;

public class Pie2DPlot extends PiePlot {
    public Pie2DPlot(DefaultTableModel tableModel) {
        super(tableModel);
        setTitle("2D Pie plot");
    }

    @Override
    protected JFreeChart createPlotFromDataset(DefaultPieDataset dataset) {
        return ChartFactory.createPieChart(getTitle(), dataset, true, true, false);
    }
}
