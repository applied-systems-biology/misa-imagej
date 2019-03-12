package org.hkijena.misa_imagej.ui.components;

import org.hkijena.misa_imagej.utils.UIUtils;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import java.awt.*;

public class PlotReader extends JPanel {

    private ChartPanel chartPanel;
    private JToolBar toolBar;

    public PlotReader() {
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());

        toolBar = new JToolBar();

        JButton exportButton = new JButton("Export", UIUtils.getIconFromResources("save.png"));
        exportButton.addActionListener(e -> exportPlot());
        toolBar.add(exportButton);

        add(toolBar, BorderLayout.NORTH);

        chartPanel = new ChartPanel(null);
        add(chartPanel, BorderLayout.CENTER);
    }

    private void exportPlot() {
        if(chartPanel.getChart() != null) {
            PlotExporterDialog dialog = new PlotExporterDialog(chartPanel.getChart());
            dialog.pack();
            dialog.setSize(400,300);
            dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
            dialog.setModal(true);
            dialog.setVisible(true);
        }
    }

    public JToolBar getToolBar() {
        return toolBar;
    }

    public ChartPanel getChartPanel() {
        return chartPanel;
    }

    public void redrawPlot() {
        JFreeChart chart = chartPanel.getChart();
        chartPanel.setChart(null);
        chartPanel.revalidate();
        chartPanel.repaint();
        chartPanel.setChart(chart);
        chartPanel.revalidate();
        chartPanel.repaint();
    }
}
