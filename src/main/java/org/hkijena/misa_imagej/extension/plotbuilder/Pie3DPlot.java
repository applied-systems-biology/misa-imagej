package org.hkijena.misa_imagej.extension.plotbuilder;

import org.hkijena.misa_imagej.ui.workbench.plotbuilder.MISAPlotSeriesData;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import java.util.List;

public class Pie3DPlot extends PiePlot {
    public Pie3DPlot(List<MISAPlotSeriesData> seriesDataList) {
        super(seriesDataList);
        setTitle("3D Pie plot");
    }

    @Override
    protected JFreeChart createPlotFromDataset(DefaultPieDataset dataset) {
        return ChartFactory.createPieChart3D(getTitle(), dataset, true, true, false);
    }
}
