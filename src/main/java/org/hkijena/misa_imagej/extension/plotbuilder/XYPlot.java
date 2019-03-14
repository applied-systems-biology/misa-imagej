package org.hkijena.misa_imagej.extension.plotbuilder;

import org.hkijena.misa_imagej.ui.workbench.plotbuilder.MISANumericPlotSeriesColumn;
import org.hkijena.misa_imagej.ui.workbench.plotbuilder.MISAPlot;
import org.hkijena.misa_imagej.ui.workbench.plotbuilder.MISAPlotSeries;
import org.hkijena.misa_imagej.ui.workbench.plotbuilder.MISAPlotSeriesGenerator;
import org.hkijena.misa_imagej.utils.StringUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.table.DefaultTableModel;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class XYPlot extends MISAPlot {

    private String xAxisLabel = "X";
    private String yAxisLabel = "Y";
    private XYSeriesCollection dataset = new XYSeriesCollection();

    public XYPlot(DefaultTableModel tableModel) {
        super(tableModel);
        addSeries();
    }

    @Override
    public boolean canRemoveSeries() {
        return series.size() > 1;
    }

    @Override
    public boolean canAddSeries() {
        return true;
    }

    @Override
    protected MISAPlotSeries createSeries() {
        MISAPlotSeries series = new MISAPlotSeries();
        series.addParameter("Name", "Series");
        series.addColumn("X", new MISANumericPlotSeriesColumn(getTableModel(),
                new MISAPlotSeriesGenerator<>("Row number", x -> (double)x)));
        series.addColumn("Y", new MISANumericPlotSeriesColumn(getTableModel(),
                new MISAPlotSeriesGenerator<>("Row number", x -> (double)x)));
        return series;
    }

    protected abstract JFreeChart createPlotFromDataset(XYSeriesCollection dataset);

    protected void updateDataset() {
        dataset.removeAllSeries();
        Set<String> existingSeries = new HashSet<>();
        for(MISAPlotSeries seriesEntry : series) {
            String name = StringUtils.makeUniqueString(seriesEntry.getParameterValue("Name").toString(), existingSeries);
            XYSeries chartSeries = new XYSeries(name, true);

            List<Double> xValues = ((MISANumericPlotSeriesColumn)seriesEntry.getColumns().get("X")).getValues();
            List<Double> yValues = ((MISANumericPlotSeriesColumn)seriesEntry.getColumns().get("Y")).getValues();
            for(int i = 0; i < xValues.size(); ++i) {
                chartSeries.add(xValues.get(i), yValues.get(i));
            }
            dataset.addSeries(chartSeries);
            existingSeries.add(name);
        }
    }

    @Override
    public final JFreeChart createPlot() {
        updateDataset();
        return createPlotFromDataset(dataset);
    }

    public String getxAxisLabel() {
        return xAxisLabel;
    }

    public void setxAxisLabel(String xAxisLabel) {
        this.xAxisLabel = xAxisLabel;
        getEventBus().post(new PlotChangedEvent(this));
    }

    public String getyAxisLabel() {
        return yAxisLabel;
    }

    public void setyAxisLabel(String yAxisLabel) {
        this.yAxisLabel = yAxisLabel;
        getEventBus().post(new PlotChangedEvent(this));
    }

    public XYSeriesCollection getDataset() {
        return dataset;
    }
}
