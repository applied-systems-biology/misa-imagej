package org.hkijena.misa_imagej.ui.perfanalysis;

import com.google.common.base.CharMatcher;
import com.google.gson.Gson;
import org.hkijena.misa_imagej.api.perfanalysis.MISARuntimeLog;
import org.hkijena.misa_imagej.utils.GsonUtils;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.util.StringUtils;
import org.jfree.data.category.DefaultIntervalCategoryDataset;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.List;

public class MISARuntimeLogUI extends JFrame {

    private MISARuntimeLog runtimeLog;
    private ChartPanel chartPanel;

    public MISARuntimeLogUI() {
        initialize();
    }

    private void initialize() {
        setSize(800, 600);
        getContentPane().setLayout(new BorderLayout(8, 8));
        setTitle("MISA++ for ImageJ - Analyze runtime log");

        JToolBar toolBar = new JToolBar();

        JButton openButton = new JButton("Open ...", UIUtils.getIconFromResources("open.png"));
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

    }

    public void open(Path path) {
        Gson gson = GsonUtils.getGson();
        try {
            runtimeLog = gson.fromJson(new String(Files.readAllBytes(path)), MISARuntimeLog.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        rebuildChart();
    }

    public void rebuildChart() {
        if (chartPanel != null) {
            this.remove(chartPanel);
        }
        if (runtimeLog == null)
            return;

        // Transform the dataset into something that is compatible with JFreeChart
        Map<String, List<MISARuntimeLog.Entry>> entriesByName = new HashMap<>();
        Map<MISARuntimeLog.Entry, String> entryThreads = new HashMap<>();

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
                else {
                    name = "Module dispatcher";
                }
                if (!entriesByName.containsKey(name))
                    entriesByName.put(name, new ArrayList<>());
                entriesByName.get(name).add(entry);
                entryThreads.put(entry, kv.getKey());
            }
        }

        // Create the dataset
        TaskSeriesCollection dataset = new TaskSeriesCollection();
        for (Map.Entry<String, List<MISARuntimeLog.Entry>> kv : entriesByName.entrySet()) {
            TaskSeries series = new TaskSeries(kv.getKey());
            for (MISARuntimeLog.Entry entry : kv.getValue()) {
                Task task = new Task(entryThreads.get(entry),
                        Date.from(Instant.ofEpochMilli((long) entry.startTime)),
                        Date.from(Instant.ofEpochMilli((long) entry.endTime)));
                series.add(task);
            }
            dataset.add(series);
        }

        // Create chart
        JFreeChart chart = ChartFactory.createGanttChart("MISA++ runtime",
                "Thread",
                "Time (ms)",
                dataset,
                true,
                false,
                false);
        CategoryPlot plot = chart.getCategoryPlot();
        DateAxis axis = (DateAxis)plot.getRangeAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("SSS"));
        chartPanel = new ChartPanel(chart);
        add(chartPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
}
