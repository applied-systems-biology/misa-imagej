package org.hkijena.misa_imagej.extension.plotbuilder;

import org.hkijena.misa_imagej.ui.workbench.plotbuilder.MISANumericPlotSeries;
import org.hkijena.misa_imagej.ui.workbench.plotbuilder.MISAPlot;
import org.hkijena.misa_imagej.ui.workbench.plotbuilder.MISAPlotSeries;
import org.hkijena.misa_imagej.ui.workbench.plotbuilder.MISAStringPlotSeries;
import org.jfree.data.category.DefaultCategoryDataset;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LineCategoryPlot implements MISAPlot {

    private static final Map<String, Class<? extends MISAPlotSeries>> DESCRIPTION = createDescription();

    @Override
    public int getDefaultSeriesCount() {
        return 1;
    }

    @Override
    public int getMinSeriesCount() {
        return 1;
    }

    @Override
    public int getMaxSeriesCount() {
        return 1;
    }

    @Override
    public Map<String, Class<? extends MISAPlotSeries>> getSeriesDescription() {
        return Collections.unmodifiableMap(DESCRIPTION);
    }

    private static Map<String, Class<? extends MISAPlotSeries>> createDescription() {
        Map<String, Class<? extends MISAPlotSeries>> result = new HashMap<>();
        result.put("Category", MISAStringPlotSeries.class);
        result.put("Value", MISANumericPlotSeries.class);
        return result;
    }
}
