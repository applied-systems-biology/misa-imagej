package org.hkijena.misa_imagej.extension.plotbuilder;

import org.hkijena.misa_imagej.ui.workbench.plotbuilder.DefaultMISAPlotSettingsUI;
import org.hkijena.misa_imagej.ui.workbench.plotbuilder.MISAPlot;

public class HistogramPlotSettingsUI extends DefaultMISAPlotSettingsUI {

    public HistogramPlotSettingsUI(MISAPlot plot) {
        super(plot);
        initialize();
    }

    private void initialize() {
        addStringEditorComponent("X axis label", () -> getNativePlot().getxAxisLabel(), s -> getNativePlot().setxAxisLabel(s));
        addStringEditorComponent("Y axis label", () -> getNativePlot().getyAxisLabel(), s -> getNativePlot().setyAxisLabel(s));
    }

    private HistogramPlot getNativePlot() {
        return (HistogramPlot)getPlot();
    }
}
