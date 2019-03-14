package org.hkijena.misa_imagej.ui.workbench.plotbuilder;

import javax.swing.*;

public class MISAPlotSettingsUI extends JPanel {
    private MISAPlot plot;

    public MISAPlotSettingsUI(MISAPlot plot) {
        this.plot = plot;
    }

    public MISAPlot getPlot() {
        return plot;
    }
}
