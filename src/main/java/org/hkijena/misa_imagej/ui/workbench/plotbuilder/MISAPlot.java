package org.hkijena.misa_imagej.ui.workbench.plotbuilder;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.jfree.chart.JFreeChart;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class MISAPlot {

    protected List<MISAPlotSeries> series = new ArrayList<>();
    private EventBus eventBus = new EventBus();
    private DefaultTableModel tableModel;
    private String title = "Plot";

    public MISAPlot(DefaultTableModel tableModel) {
        this.tableModel = tableModel;
    }

    public boolean canRemoveSeries() {
        return true;
    }

    public boolean canAddSeries() {
        return true;
    }

    public void addSeries() {
        if(canAddSeries()) {
            MISAPlotSeries s = createSeries();
            s.getEventBus().register(this);
            series.add(s);
            eventBus.post(new PlotSeriesListChangedEvent(this));
            eventBus.post(new PlotChangedEvent(this));
        }
    }

    public void removeSeries(MISAPlotSeries series) {
        if(canRemoveSeries()) {
            this.series.remove(series);
            eventBus.post(new PlotSeriesListChangedEvent(this));
            eventBus.post(new PlotChangedEvent(this));
        }
    }

    @Subscribe
    public void handleSeriesDataChangedEvent(MISAPlotSeries.DataChangedEvent event) {
        eventBus.post(new PlotChangedEvent(this));
    }

    protected abstract MISAPlotSeries createSeries();

    public abstract JFreeChart createPlot();

    public EventBus getEventBus() {
        return eventBus;
    }

    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        eventBus.post(new PlotChangedEvent(this));
    }

    public List<MISAPlotSeries> getSeries() {
        return Collections.unmodifiableList(series);
    }

    public static class PlotChangedEvent {
        private MISAPlot plot;

        public PlotChangedEvent(MISAPlot plot) {
            this.plot = plot;
        }

        public MISAPlot getPlot() {
            return plot;
        }
    }

    public static class PlotSeriesListChangedEvent {
        private MISAPlot plot;

        public PlotSeriesListChangedEvent(MISAPlot plot) {
            this.plot = plot;
        }

        public MISAPlot getPlot() {
            return plot;
        }
    }
}
