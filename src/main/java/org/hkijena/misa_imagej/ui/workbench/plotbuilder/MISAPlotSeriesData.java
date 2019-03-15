package org.hkijena.misa_imagej.ui.workbench.plotbuilder;

import java.util.ArrayList;
import java.util.List;

public class MISAPlotSeriesData {
    private String name;
    private List<Object> data = new ArrayList<>();

    public MISAPlotSeriesData(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Object> getData() {
        return data;
    }

    public int getSize() {
        return data.size();
    }
}
