package org.hkijena.misa_imagej.ui.workbench.plotbuilder;

import java.util.List;
import java.util.Map;

public interface MISAPlot {
    int getDefaultSeriesCount();
    int getMinSeriesCount();
    int getMaxSeriesCount();
    Map<String, Class<? extends MISAPlotSeries>> getSeriesDescription();
}
