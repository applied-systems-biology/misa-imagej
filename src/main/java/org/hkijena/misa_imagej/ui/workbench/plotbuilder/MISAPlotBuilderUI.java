package org.hkijena.misa_imagej.ui.workbench.plotbuilder;

import org.hkijena.misa_imagej.ui.components.PlotReader;
import org.hkijena.misa_imagej.ui.workbench.MISAWorkbenchUI;
import org.hkijena.misa_imagej.ui.workbench.tableanalyzer.MISATableAnalyzerUI;
import org.hkijena.misa_imagej.utils.TableUtils;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.hkijena.misa_imagej.utils.ui.DocumentTabPane;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MISAPlotBuilderUI extends JPanel {

    private MISAWorkbenchUI workbench;
    private DefaultTableModel tableModel;

    private PlotReader plotReader;

    public MISAPlotBuilderUI(MISAWorkbenchUI workbench, DefaultTableModel tableModel) {
        this.workbench = workbench;
        this.tableModel = tableModel;
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());

        plotReader = new PlotReader();
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createBuilderPanel(), plotReader);
        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createBuilderPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JToolBar toolBar = new JToolBar();
        JButton openTableButton = new JButton("Open table", UIUtils.getIconFromResources("table.png"));
        openTableButton.addActionListener(e -> openTable());
        toolBar.add(openTableButton);

        panel.add(toolBar, BorderLayout.NORTH);

        return panel;
    }

    private void openTable() {
        workbench.addTab("Table",
                UIUtils.getIconFromResources("table.png"),
                new MISATableAnalyzerUI(workbench, TableUtils.cloneTableModel(tableModel)),
                DocumentTabPane.CloseMode.withAskOnCloseButton, true);
        workbench.setSelectedTab(workbench.getTabCount() - 1);
    }
}
