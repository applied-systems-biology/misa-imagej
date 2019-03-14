package org.hkijena.misa_imagej.ui.workbench.plotbuilder;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import java.util.*;
import java.util.stream.Collectors;

public class MISAPlotSeries {
    private Map<String, MISAPlotSeriesColumn> columns = new HashMap<>();
    private Map<String, Object> parameters = new HashMap<>();
    private Map<String, Class> parameterTypes = new HashMap<>();
    private EventBus eventBus = new EventBus();

    public MISAPlotSeries() {

    }

    public void addColumn(String name, MISAPlotSeriesColumn column) {
        columns.put(name, column);
        column.getEventBus().register(this);
    }

    @Subscribe
    public void handleColumnDataChangedEvent(MISAPlotSeriesColumn.DataChangedEvent event) {
        eventBus.post(new DataChangedEvent(this));
    }

    public Map<String, MISAPlotSeriesColumn> getColumns() {
        return Collections.unmodifiableMap(columns);
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public void addParameter(String key, Object defaultValue) {
        parameters.put(key, Objects.requireNonNull(defaultValue));
        parameterTypes.put(key, defaultValue.getClass());
    }

    public Class getParameterType(String key) {
        return parameterTypes.get(key);
    }

    public Object getParameterValue(String key) {
        return parameters.get(key);
    }

    public void setParameterValue(String key, Object value) {
        parameters.put(key, Objects.requireNonNull(value));
        getEventBus().post(new DataChangedEvent(this));
    }

    public List<String> getParameterNames() {
        return parameters.keySet().stream().sorted().collect(Collectors.toList());
    }

    public MISANumericPlotSeriesColumn getAsNumericColumn(String name) {
        return (MISANumericPlotSeriesColumn)columns.get(name);
    }

    public MISAStringPlotSeriesColumn getAsStringColumn(String name) {
        return (MISAStringPlotSeriesColumn)columns.get(name);
    }

    public static class DataChangedEvent {
        private MISAPlotSeries series;

        public DataChangedEvent(MISAPlotSeries series) {
            this.series = series;
        }

        public MISAPlotSeries getSeries() {
            return series;
        }
    }
}
