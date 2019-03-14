package org.hkijena.misa_imagej.extension.plotbuilder;

import org.hkijena.misa_imagej.ui.workbench.plotbuilder.DefaultMISAPlotSettingsUI;
import org.hkijena.misa_imagej.ui.workbench.plotbuilder.MISAPlot;

public class CategoryPlotSettingsUI extends DefaultMISAPlotSettingsUI {

    public CategoryPlotSettingsUI(MISAPlot plot) {
        super(plot);
        initialize();
    }

    private void initialize() {
        addStringEditorComponent("X axis label", () -> getNativePlot().getCategoryAxisLabel(), s -> getNativePlot().setCategoryAxisLabel(s));
        addStringEditorComponent("Value axis label", () -> getNativePlot().getValueAxisLabel(), s -> getNativePlot().setValueAxisLabel(s));
    }

    private CategoryPlot getNativePlot() {
        return (CategoryPlot)getPlot();
    }
}
