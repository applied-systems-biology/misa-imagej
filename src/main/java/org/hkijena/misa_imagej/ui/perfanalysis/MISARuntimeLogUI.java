package org.hkijena.misa_imagej.ui.perfanalysis;

import com.google.common.base.CharMatcher;
import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import org.hkijena.misa_imagej.api.MISARuntimeLog;
import org.hkijena.misa_imagej.utils.GsonUtils;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.jdesktop.swingx.JXStatusBar;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RectangularShape;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MISARuntimeLogUI extends JPanel {

    private MISARuntimeLog runtimeLog;
    private ChartPanel ganttChartPanel;
    private JTable summaryTable;
    private EventBus eventBus = new EventBus();

    private JButton openButton;

    private JToggleButton ganttWithBorderToggle;

    public MISARuntimeLogUI() {
        initialize();
    }

    private void initialize() {
        JToolBar toolBar = new JToolBar();

        setLayout(new BorderLayout());

        openButton = new JButton("Open ...", UIUtils.getIconFromResources("open.png"));
        openButton.addActionListener(actionEvent -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Open runtime log");
            chooser.setMultiSelectionEnabled(false);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                open(chooser.getSelectedFile().toPath());
            }
        });
        toolBar.add(openButton);
        add(toolBar, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
        tabbedPane.addTab("Timeline", initializeGantt());
        tabbedPane.addTab("Summary", initializeSummaryPanel());
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel initializeGantt() {
        JPanel result = new JPanel(new BorderLayout());

        JToolBar toolBar = new JToolBar();
        ganttWithBorderToggle = new JToggleButton("With border", UIUtils.getIconFromResources("border.png"));
        ganttWithBorderToggle.addActionListener(actionEvent -> {
            JFreeChart chart = ganttChartPanel.getChart();
            ganttChartPanel.setChart(null);
            ganttChartPanel.revalidate(); ganttChartPanel.repaint();
            ganttChartPanel.setChart(chart);
            ganttChartPanel.revalidate(); ganttChartPanel.repaint();
        });
        ganttWithBorderToggle.setSelected(true);
        toolBar.add(ganttWithBorderToggle);
        result.add(toolBar, BorderLayout.NORTH);

        ganttChartPanel = new ChartPanel(null);
        result.add(ganttChartPanel, BorderLayout.CENTER);

        return result;
    }

    private JPanel initializeSummaryPanel() {
        JPanel result = new JPanel(new BorderLayout());
        summaryTable = new JTable() {
            @Override
            public boolean isCellEditable(int i, int i1) {
                return false;
            }
        };
        result.add(summaryTable, BorderLayout.CENTER);
        return result;
    }

    public void open(MISARuntimeLog log) {
        this.runtimeLog = log;
        eventBus.post(new RuntimeLogChangedEvent(runtimeLog, null));
        rebuildCharts();
    }

    public void open(Path path) {
        Gson gson = GsonUtils.getGson();
        try {
            runtimeLog = gson.fromJson(new String(Files.readAllBytes(path)), MISARuntimeLog.class);
            eventBus.post(new RuntimeLogChangedEvent(runtimeLog, path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        rebuildCharts();
    }

    public void rebuildCharts() {
        rebuildGanttChart();
        rebuildSummary();
    }

    private void rebuildSummary() {
        DefaultTableModel model = new DefaultTableModel();
        if(runtimeLog != null) {
            model.addColumn("");
            model.addColumn("Value");
            model.addRow(new Object[] { "Total runtime (s)", runtimeLog.getTotalRuntime() / 1000.0 });
            model.addRow(new Object[] { "Estimated single-threaded runtime (s)", runtimeLog.getUnparallelizedRuntime() / 1000.0 });
            model.addRow(new Object[] { "Multithreading speedup", runtimeLog.getParallelizationSpeedup() });
        }
        summaryTable.setModel(model);
    }

    private void rebuildGanttChart() {
        if (ganttChartPanel != null) {
            this.remove(ganttChartPanel);
        }
        if (runtimeLog == null)
            return;

        // Transform the dataset into something that is compatible with JFreeChart
        Map<String, List<MISARuntimeLog.Entry>> entriesByName = new HashMap<>();
        Map<MISARuntimeLog.Entry, String> entryThreads = new HashMap<>();
        ArrayList<String> threadList = new ArrayList<>(runtimeLog.entries.keySet());

        for (Map.Entry<String, List<MISARuntimeLog.Entry>> kv : runtimeLog.entries.entrySet()) {
            for (MISARuntimeLog.Entry entry : kv.getValue()) {
                String name = entry.name;
                if (name.startsWith("Postprocessing"))
                    name = "Postprocessing";
                else if (name.startsWith("Attachment"))
                    name = "Attachment";
                else if (name.contains("__OBJECT__"))
                    name = "Parameter schema";
                else if (CharMatcher.is('/').countIn(name) >= 2) {
                    name = name.substring(name.indexOf('/') + 1);
                    name = name.substring(name.indexOf('/') + 1);
                }
                else if(name.equals("Undefined workload")) {
                    // Do nothing
                }
                else {
                    name = "Module dispatcher";
                }
                if (!entriesByName.containsKey(name))
                    entriesByName.put(name, new ArrayList<>());
                entriesByName.get(name).add(entry);
                entryThreads.put(entry, kv.getKey());
            }
        }

        XYIntervalSeriesCollection dataset = new XYIntervalSeriesCollection();

        //Create series. Start and end times are used as y intervals, and the room is represented by the x value
        for(Map.Entry<String, List<MISARuntimeLog.Entry>> kv : entriesByName.entrySet()){
            XYIntervalSeries series = new XYIntervalSeries(kv.getKey());
            for(MISARuntimeLog.Entry entry : kv.getValue()) {
                int threadId = threadList.indexOf(entryThreads.get(entry));
                series.add(threadId, threadId - 0.3, threadId + 0.3, entry.startTime / 1000.0, entry.startTime / 1000.0, entry.endTime / 1000.0);
            }

            dataset.addSeries(series);
        }

        XYBarRenderer renderer = new XYBarRenderer();
        renderer.setBarPainter(new StandardXYBarPainter() {
            @Override
            public void paintBarShadow(Graphics2D g2, XYBarRenderer renderer, int row, int column, RectangularShape bar, RectangleEdge base, boolean pegShadow) {
            }

            @Override
            public void paintBar(Graphics2D g2, XYBarRenderer renderer, int row, int column, RectangularShape bar, RectangleEdge base) {
                super.paintBar(g2, renderer, row, column, bar, base);
                if(ganttWithBorderToggle.isSelected()) {
                    g2.setColor(Color.DARK_GRAY);
                    g2.setStroke(new BasicStroke(1));
                    g2.draw(bar);
                }
            }
        });
        renderer.setUseYInterval(true);
        XYPlot plot = new XYPlot(dataset, new SymbolAxis("Threads", threadList.toArray(new String[0])), new NumberAxis("Time (s)"), renderer);
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        JFreeChart chart = new JFreeChart(plot);

        // Setup panel
        ganttChartPanel.setChart(chart);
        ganttChartPanel.setMaximumDrawWidth(Integer.MAX_VALUE);
        ganttChartPanel.setMaximumDrawHeight(Integer.MAX_VALUE);
        revalidate();
        repaint();
    }

    public void setHideOpenButton(boolean hide) {
        if(hide)
            openButton.setVisible(false);
        else
            openButton.setVisible(true);
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public static class RuntimeLogChangedEvent {
        private MISARuntimeLog runtimeLog;
        private Path path;

        public RuntimeLogChangedEvent(MISARuntimeLog runtimeLog, Path path) {
            this.runtimeLog = runtimeLog;
            this.path = path;
        }

        public MISARuntimeLog getRuntimeLog() {
            return runtimeLog;
        }

        public Path getPath() {
            return path;
        }
    }
}
