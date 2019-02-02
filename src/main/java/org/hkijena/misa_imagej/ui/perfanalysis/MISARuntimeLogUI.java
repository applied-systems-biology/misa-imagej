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
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.util.StringUtils;
import org.jfree.data.category.DefaultIntervalCategoryDataset;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;

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
                series.add(threadId, threadId - 0.3, threadId + 0.3, entry.startTime, entry.startTime + 0.1, entry.endTime - 0.1);
            }

            dataset.addSeries(series);
        }

//        for(int k = 0; k < totalCourseCount; k++){
//            //get a random room
//            int currentRoom = r.nextInt(runtimeLog.entries.size());
//            //get a random course
//            int currentCourse = r.nextInt(courseTypes);
//            //get a random course duration (1-3 h)
//            int time = r.nextInt(3) + 1;
//            //Encode the room as x value. The width of the bar is only 0.6 to leave a small gap. The course starts 0.1 h/6 min after the end of the preceding course.
//            series[currentCourse].add(currentRoom, currentRoom - 0.3, currentRoom + 0.3, startTimes[currentRoom], startTimes[currentRoom] +0.1, startTimes[currentRoom] + time - 0.1);
//            //Increase start time for the current room
//            startTimes[currentRoom] += time;
//        }
        XYBarRenderer renderer = new XYBarRenderer();
        renderer.setUseYInterval(true);
        XYPlot plot = new XYPlot(dataset, new SymbolAxis("Threads", threadList.toArray(new String[0])), new NumberAxis(), renderer);
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        JFreeChart chart = new JFreeChart(plot);

        // Setup panel
        chartPanel = new ChartPanel(chart);
        chartPanel.setMaximumDrawWidth(Integer.MAX_VALUE);
        chartPanel.setMaximumDrawHeight(Integer.MAX_VALUE);
        add(chartPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
}
