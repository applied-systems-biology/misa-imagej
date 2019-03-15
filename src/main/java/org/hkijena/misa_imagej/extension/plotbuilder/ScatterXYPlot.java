package org.hkijena.misa_imagej.extension.plotbuilder;

import org.hkijena.misa_imagej.ui.workbench.plotbuilder.MISAPlotSeriesData;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.table.DefaultTableModel;
import java.util.List;

public class ScatterXYPlot extends XYPlot {
    public ScatterXYPlot(List<MISAPlotSeriesData> seriesDataList) {
        super(seriesDataList);
        setTitle("XY Scatter Plot");
    }

    @Override
    protected JFreeChart createPlotFromDataset(XYSeriesCollection dataset) {
        return ChartFactory.createScatterPlot(getTitle(), getxAxisLabel(), getyAxisLabel(), dataset);
    }
}
