package org.hkijena.misa_imagej.ui.perfanalysis;

import com.google.common.base.CharMatcher;
import com.google.gson.Gson;
import org.hkijena.misa_imagej.api.perfanalysis.MISARuntimeLog;
import org.hkijena.misa_imagej.utils.GsonUtils;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.jdesktop.swingx.JXStatusBar;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.util.StringUtils;
import org.jfree.data.category.DefaultIntervalCategoryDataset;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.time.SimpleTimePeriod;

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
    private JLabel statusLabel;

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

        JXStatusBar statusBar = new JXStatusBar();
        statusLabel = new JLabel("Ready");
        statusBar.add(statusLabel);
        add(statusBar, BorderLayout.SOUTH);

    }

    public void open(Path path) {
        statusLabel.setText("Ready");
        Gson gson = GsonUtils.getGson();
        try {
            runtimeLog = gson.fromJson(new String(Files.readAllBytes(path)), MISARuntimeLog.class);
            statusLabel.setText(path.toString());
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

        // We need to have a map of entry name -> thread -> thread-representative task due to how this plot works
        Map<String, Map<String, Task>> taskTargets = new HashMap<>();
        for(String name : entriesByName.keySet()) {
            taskTargets.put(name, new HashMap<>());
            TaskSeries series = new TaskSeries(name);

            long minRuntime = Long.MAX_VALUE;
            long maxRuntime = 0;

            for(MISARuntimeLog.Entry entry : entriesByName.get(name)) {
                minRuntime = Math.min(minRuntime, (long)entry.startTime);
                maxRuntime = Math.max(maxRuntime, (long)entry.endTime);
            }

            for(String thread : runtimeLog.entries.keySet()) {
                Task taskRepresentative = new Task(thread, new SimpleTimePeriod(minRuntime, maxRuntime));
                taskTargets.get(name).put(thread, taskRepresentative);
                series.add(taskRepresentative);
            }

            dataset.add(series);
        }

        // Now go through all entries
        for(Map.Entry<String, List<MISARuntimeLog.Entry>> kv : entriesByName.entrySet()) {
            for(MISARuntimeLog.Entry entry : kv.getValue()) {
                String name = kv.getKey();
                String thread = entryThreads.get(entry);
                Task target = taskTargets.get(name).get(thread);
                target.addSubtask(new Task(entry.name + entry.startTime + "_" + entry.endTime, new SimpleTimePeriod((long)entry.startTime, (long)entry.endTime)));
            }
        }

//        {
//            TaskSeries s1 = new TaskSeries("A");
//            TaskSeries s2 = new TaskSeries("B");
//
//            Task s1t0 = new Task("thread0", new SimpleTimePeriod(0, 4000));
//            Task s1t1 = new Task("thread1", new SimpleTimePeriod(0, 4000));
//            Task s2t0 = new Task("thread0", new SimpleTimePeriod(0, 4000));
//
//            s1.add(s1t0);
//            s1.add(s1t1);
//            s2.add(s2t0);
//
//            s1t0.addSubtask(new Task("thread0", new SimpleTimePeriod(0, 1000)));
//            s1t0.addSubtask(new Task("thread0", new SimpleTimePeriod(1500, 3000)));
//            s1t1.addSubtask(new Task("thread1", new SimpleTimePeriod(1000, 4000)));
//            s2t0.addSubtask(new Task("thread1", new SimpleTimePeriod(100, 900)));
//            dataset.add(s1);
//            dataset.add(s2);
//        }

        // Create chart
        JFreeChart chart = ChartFactory.createGanttChart("MISA++ runtime",
                "Thread",
                "Time (ms)",
                dataset,
                true,
                false,
                false);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setRangeAxis(new NumberAxis("Time (ms)"));
        chartPanel = new ChartPanel(chart);
        chartPanel.setMaximumDrawWidth(Integer.MAX_VALUE);
        chartPanel.setMaximumDrawHeight(Integer.MAX_VALUE);
        add(chartPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
}
