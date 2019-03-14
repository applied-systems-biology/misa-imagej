package org.hkijena.misa_imagej.extension.plotbuilder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.table.DefaultTableModel;

public class ScatterXYPlot extends XYPlot {
    public ScatterXYPlot(DefaultTableModel tableModel) {
        super(tableModel);
        setTitle("XY Scatter Plot");
    }

    @Override
    protected JFreeChart createPlotFromDataset(XYSeriesCollection dataset) {
        return ChartFactory.createScatterPlot(getTitle(), getxAxisLabel(), getyAxisLabel(), dataset);
    }
}
