package org.hkijena.misa_imagej.ui.workbench.plotbuilder;

import com.google.common.eventbus.Subscribe;
import org.hkijena.misa_imagej.MISAImageJRegistryService;
import org.hkijena.misa_imagej.ui.components.PlotReader;
import org.hkijena.misa_imagej.ui.workbench.MISAWorkbenchUI;
import org.hkijena.misa_imagej.ui.workbench.tableanalyzer.MISATableAnalyzerUI;
import org.hkijena.misa_imagej.utils.TableUtils;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.hkijena.misa_imagej.utils.ui.DocumentTabPane;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.ScrollableSizeHint;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MISAPlotBuilderUI extends JPanel {

    private MISAWorkbenchUI workbench;
    private DefaultTableModel tableModel;
    private JToolBar plotSeriesEditorToolBar;
    private JXPanel plotSeriesListPanel;
    private JPanel plotSettingsPanel;
    private MISAPlot currentPlot;

    private JToggleButton toggleAutoUpdate;
    private PlotReader plotReader;

    public MISAPlotBuilderUI(MISAWorkbenchUI workbench, DefaultTableModel tableModel) {
        this.workbench = workbench;
        this.tableModel = tableModel;
        initialize();
        updatePlotSettings();
        updatePlot();
    }

    private void initialize() {
        setLayout(new BorderLayout());

        plotReader = new PlotReader();

        plotReader.getToolBar().add(Box.createHorizontalGlue());

        toggleAutoUpdate = new JToggleButton(UIUtils.getIconFromResources("cog.png"));
        toggleAutoUpdate.setSelected(true);
        toggleAutoUpdate.setToolTipText("Automatically update the plot");
        plotReader.getToolBar().add(toggleAutoUpdate);

        JButton updatePlotButton = new JButton("Update", UIUtils.getIconFromResources("refresh.png"));
        updatePlotButton.addActionListener(e -> updatePlot());
        plotReader.getToolBar().add(updatePlotButton);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createBuilderPanel(), plotReader);
        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createBuilderPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JToolBar toolBar = new JToolBar();

        JComboBox<MISAPlot> plotJComboBox = new JComboBox<>();
        plotJComboBox.setRenderer(new Renderer());
        for(MISAPlot plot : MISAImageJRegistryService.getInstance().getPlotBuilderRegistry().createAllPlots(tableModel)) {
            plot.getEventBus().register(this);
            plotJComboBox.addItem(plot);
        }
        plotJComboBox.addItemListener(e -> { if(e.getItem() != null) setCurrentPlot((MISAPlot)e.getItem()); });
        toolBar.add(plotJComboBox);

        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(Box.createHorizontalStrut(32));

        JButton openTableButton = new JButton("Open table", UIUtils.getIconFromResources("table.png"));
        openTableButton.addActionListener(e -> openTable());
        toolBar.add(openTableButton);

        panel.add(toolBar, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);

        JPanel seriesPanel = new JPanel(new BorderLayout());
        plotSeriesEditorToolBar = new JToolBar();

        JButton addSeriesButton = new JButton("Add series", UIUtils.getIconFromResources("add.png"));
        addSeriesButton.addActionListener(e -> addSeries());
        plotSeriesEditorToolBar.add(addSeriesButton);

        seriesPanel.add(plotSeriesEditorToolBar, BorderLayout.NORTH);

        plotSeriesListPanel = new JXPanel();
        plotSeriesListPanel.setScrollableWidthHint(ScrollableSizeHint.FIT);
        plotSeriesListPanel.setScrollableHeightHint(ScrollableSizeHint.NONE);
        plotSeriesListPanel.setLayout(new BoxLayout(plotSeriesListPanel, BoxLayout.PAGE_AXIS));
        seriesPanel.add(new JScrollPane(plotSeriesListPanel), BorderLayout.CENTER);
        tabbedPane.addTab("Data", UIUtils.getIconFromResources("table.png"), seriesPanel);

        plotSettingsPanel = new JPanel(new GridBagLayout());
        tabbedPane.addTab("Settings", UIUtils.getIconFromResources("wrench.png"), plotSettingsPanel);

        panel.add(tabbedPane, BorderLayout.CENTER);

        if(plotJComboBox.getSelectedItem() instanceof MISAPlot)
            currentPlot = (MISAPlot) plotJComboBox.getSelectedItem();

        return panel;
    }

    private void addSeries() {
        if(currentPlot != null)
            currentPlot.addSeries();
    }

    private void setCurrentPlot(MISAPlot plot) {
        this.currentPlot = plot;
        updatePlotSettings();
        updatePlot();
    }

    private void updatePlotSettings() {
        if(currentPlot != null) {
            // Update the settings
            plotSettingsPanel.removeAll();

            // Update the series list
            plotSeriesEditorToolBar.setVisible(currentPlot.canAddSeries());
            plotSeriesListPanel.removeAll();
            for (MISAPlotSeries series : currentPlot.getSeries()) {
                MISAPlotSeriesUI ui = new MISAPlotSeriesUI(currentPlot, series);
                ui.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8),
                        BorderFactory.createLineBorder(Color.BLACK)));
                plotSeriesListPanel.add(ui);
            }
            plotSeriesListPanel.add(Box.createVerticalGlue());
        }
    }

    private void updatePlot() {
        if(currentPlot != null) {
            // Rebuild the chart
            plotReader.getChartPanel().setChart(currentPlot.createPlot());
        }
    }

    @Subscribe
    public void handlePlotChangedEvent(MISAPlot.PlotChangedEvent event) {
        if(toggleAutoUpdate.isSelected())
            updatePlot();
    }

    private void openTable() {
        workbench.addTab("Table",
                UIUtils.getIconFromResources("table.png"),
                new MISATableAnalyzerUI(workbench, TableUtils.cloneTableModel(tableModel)),
                DocumentTabPane.CloseMode.withAskOnCloseButton, true);
        workbench.setSelectedTab(workbench.getTabCount() - 1);
    }

    private static class Renderer extends JLabel implements ListCellRenderer<MISAPlot> {

        public Renderer() {
            setOpaque(false);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends MISAPlot> list, MISAPlot value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {

            if(value != null) {
                setText(MISAImageJRegistryService.getInstance().getPlotBuilderRegistry().getNameOf(value));
                setIcon(MISAImageJRegistryService.getInstance().getPlotBuilderRegistry().getIconOf(value));
            }

            if(isSelected) {
                setBackground(new Color(184, 207, 229));
            }
            else {
                setBackground(new Color(255,255,255));
            }

            if(list.getSelectedValue() == value) {
                setBorder(null);
            }
            else {
                setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
            }

            return this;
        }
    }
}
