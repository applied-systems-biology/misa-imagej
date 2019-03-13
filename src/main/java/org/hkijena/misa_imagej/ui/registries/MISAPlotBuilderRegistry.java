package org.hkijena.misa_imagej.ui.registries;

import org.hkijena.misa_imagej.ui.workbench.plotbuilder.MISAPlot;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class MISAPlotBuilderRegistry {

    private Map<Class<? extends MISAPlot>, Entry> entries = new HashMap<>();

    public void register(Class<? extends MISAPlot> plotType, String name, Icon icon) {
        entries.put(plotType, new Entry(plotType, name, icon));
    }

    public Collection<Entry> getEntries() {
        return entries.values();
    }

    public String getNameOf(MISAPlot plot) {
        return entries.get(plot.getClass()).getName();
    }

    public Icon getIconOf(MISAPlot plot) {
        return entries.get(plot.getClass()).getIcon();
    }

    public List<MISAPlot> createAllPlots() {
        List<MISAPlot> plots = new ArrayList<>();
        for(Entry entry : entries.values()) {
            try {
                plots.add(entry.getPlotType().getConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return plots;
    }

    public static class Entry {
        private Class<? extends MISAPlot> plotType;
        private String name;
        private Icon icon;

        public Entry(Class<? extends MISAPlot> plotType, String name, Icon icon) {
            this.plotType = plotType;
            this.name = name;
            this.icon = icon;
        }

        public Class<? extends MISAPlot> getPlotType() {
            return plotType;
        }

        public String getName() {
            return name;
        }

        public Icon getIcon() {
            return icon;
        }
    }
}
