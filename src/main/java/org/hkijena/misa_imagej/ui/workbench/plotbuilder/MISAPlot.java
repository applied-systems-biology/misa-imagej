package org.hkijena.misa_imagej.ui.workbench.plotbuilder;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.jfree.chart.JFreeChart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class MISAPlot {

    protected List<MISAPlotSeries> series = new ArrayList<>();
    private EventBus eventBus = new EventBus();
    private List<MISAPlotSeriesData> seriesDataList;
    private String title = "Plot";

    protected MISAPlot(List<MISAPlotSeriesData> seriesDataList) {
        this.seriesDataList = seriesDataList;
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

    public List<MISAPlotSeriesData> getSeriesDataList() {
        return seriesDataList;
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
