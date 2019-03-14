package org.hkijena.misa_imagej.extension.plotbuilder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.table.DefaultTableModel;

public class LineXYPlot extends XYPlot {
    public LineXYPlot(DefaultTableModel tableModel) {
        super(tableModel);
        setTitle("XY Line Plot");
    }

    @Override
    protected JFreeChart createPlotFromDataset(XYSeriesCollection dataset) {
        return ChartFactory.createXYLineChart(getTitle(), getxAxisLabel(), getyAxisLabel(), dataset);
    }
}
