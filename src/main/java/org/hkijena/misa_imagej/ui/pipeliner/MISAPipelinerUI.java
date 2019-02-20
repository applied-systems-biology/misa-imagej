package org.hkijena.misa_imagej.ui.pipeliner;

import com.google.gson.Gson;
import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.MISAModuleInstance;
import org.hkijena.misa_imagej.api.MISAValidityReport;
import org.hkijena.misa_imagej.api.pipelining.MISAPipeline;
import org.hkijena.misa_imagej.api.pipelining.MISAPipelineNode;
import org.hkijena.misa_imagej.api.repository.MISAModule;
import org.hkijena.misa_imagej.api.repository.MISAModuleRepository;
import org.hkijena.misa_imagej.ui.components.MISAValidityReportStatusUI;
import org.hkijena.misa_imagej.ui.components.CancelableProcessUI;
import org.hkijena.misa_imagej.ui.components.MISACacheTreeUI;
import org.hkijena.misa_imagej.ui.components.renderers.MISAModuleListCellRenderer;
import org.hkijena.misa_imagej.ui.repository.MISAModuleRepositoryUI;
import org.hkijena.misa_imagej.utils.GsonUtils;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.jdesktop.swingx.JXStatusBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class MISAPipelinerUI extends JFrame {

    private MISAPipeline pipeline = new MISAPipeline();
    private JList<MISAModule> moduleList;
    private MISACacheTreeUI cacheTree;
    private MISAPipelineUI pipelineEditor;

    /**
     * We use this to give users an easy overview of a module
     */
    private Map<MISAModule, MISAModuleInstance> uiParameterSchemata = new HashMap<>();
    private MISAValidityReportStatusUI validityReportStatusUI;

    public MISAPipelinerUI()
    {
        initialize();
        refresh();
    }

    private void initialize() {
        setSize(800, 600);
        getContentPane().setLayout(new BorderLayout());
        setTitle("MISA++ for ImageJ - Pipeline tool");
        setIconImage(UIUtils.getIconFromResources("misaxx.png").getImage());
        UIUtils.setToAskOnClose(this, "Do you really want to close this pipeline builder?", "Close window");

        JToolBar toolBar = new JToolBar();

        JButton openButton = new JButton("Open", UIUtils.getIconFromResources("open.png"));
        openButton.addActionListener(actionEvent -> open());
        toolBar.add(openButton);

        JButton saveButton = new JButton("Save", UIUtils.getIconFromResources("save.png"));
        saveButton.addActionListener(actionEvent -> save());
        toolBar.add(saveButton);

        toolBar.add(Box.createHorizontalGlue());

        JButton validateButton = new JButton("Check parameters", UIUtils.getIconFromResources("checkmark.png"));
        validateButton.addActionListener(actionEvent -> validityReportStatusUI.setReport(pipeline.getValidityReport()));
        toolBar.add(validateButton);

        JButton exportButton = new JButton("Export", UIUtils.getIconFromResources("export.png"));
        exportButton.addActionListener(actionEvent -> export());
        toolBar.add(exportButton);

        JButton runButton = new JButton("Run", UIUtils.getIconFromResources("run.png"));
        runButton.addActionListener(actionEvent -> runPipeline());
        toolBar.add(runButton);

        add(toolBar, BorderLayout.NORTH);
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.addTab("Available modules", createModuleList());

        pipelineEditor = new MISAPipelineUI();
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(pipelineEditor) {
            {
                setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            }
        }, tabbedPane);
        splitPane.setResizeWeight(1);
        add(splitPane, BorderLayout.CENTER);

        // Status bar
        JXStatusBar statusBar = new JXStatusBar();
        validityReportStatusUI = new MISAValidityReportStatusUI();
        statusBar.add(validityReportStatusUI);
        add(statusBar, BorderLayout.SOUTH);
    }

    private JPanel createModuleList() {
        JPanel  toolboxPanel = new JPanel(new BorderLayout());
        moduleList = new JList<>();
        moduleList.setCellRenderer(new MISAModuleListCellRenderer());
        moduleList.addListSelectionListener(listSelectionEvent -> updateCacheTree());
        moduleList.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if(mouseEvent.getClickCount() == 2) {
                    addInstance();
                }
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {}

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {}

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {}

            @Override
            public void mouseExited(MouseEvent mouseEvent) {}
        });

        cacheTree = new MISACacheTreeUI() {
            @Override
            protected Entry createRootEntry() {
                Entry result = super.createRootEntry();
                result.name = "Input and output preview";
                return result;
            }

            @Override
            protected Entry createEntry(MISACache cache) {
                Entry result = super.createEntry(cache);
                result.name = cache.getCacheTypeName() + ": " + result.name;
                return result;
            }
        };

        toolboxPanel.add(new JSplitPane(JSplitPane.VERTICAL_SPLIT, moduleList, cacheTree), BorderLayout.CENTER);

        JToolBar toolboxToolbar = new JToolBar();

        JButton instantiateButton = new JButton("Add to pipeline", UIUtils.getIconFromResources("add.png"));
        instantiateButton.addActionListener(actionEvent -> addInstance());
        toolboxToolbar.add(instantiateButton);

        toolboxToolbar.add(Box.createHorizontalGlue());

        JButton refreshButton = new JButton(UIUtils.getIconFromResources("refresh.png"));
        refreshButton.setToolTipText("Refresh list of available modules");
        refreshButton.addActionListener(actionEvent -> refresh());
        toolboxToolbar.add(refreshButton);

        toolboxPanel.add(toolboxToolbar, BorderLayout.SOUTH);


        return toolboxPanel;
    }

    private void export() {
        MISAValidityReport report = pipeline.getValidityReport();
        validityReportStatusUI.setReport(report);
        if(!report.isValid())
            return;

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Export pipeline");
        if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
               pipeline.export(fileChooser.getSelectedFile().toPath(), true, true, false);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void runPipeline() {
        MISAValidityReport report = pipeline.getValidityReport();
        validityReportStatusUI.setReport(report);
        if(!report.isValid())
            return;
        MISARunPipelineDialogUI dialogUI = new MISARunPipelineDialogUI(this);
        if(dialogUI.showDialog() == MISARunPipelineDialogUI.ACCEPT_OPTION) {
            try {
                pipeline.export(dialogUI.getExportPath(), false, false, true);
                List<ProcessBuilder> processes = new ArrayList<>();
                for(MISAPipelineNode node : pipeline.traverse()) {
                    processes.add(node.getModuleInstance().getModule().run(dialogUI.getExportPath().resolve(node.getId()).resolve("parameters.json")));
                }

                // Run the executable
                MISAModuleRepositoryUI.getInstance().getCommand().getLogService().info("Starting worker process ...");
                CancelableProcessUI processUI = new CancelableProcessUI(processes);
                processUI.setLocationRelativeTo(this);

                // React to changes in status
                processUI.addPropertyChangeListener(propertyChangeEvent -> {
                    if(processUI.getStatus() == CancelableProcessUI.Status.Done ||
                            processUI.getStatus() == CancelableProcessUI.Status.Canceled ||
                            processUI.getStatus() == CancelableProcessUI.Status.Failed) {
                        setEnabled(true);
                        if(processUI.getStatus() == CancelableProcessUI.Status.Failed) {
                            JOptionPane.showMessageDialog(this, "There was an error during calculation. Please check the console to see the cause of this error.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                        MISAPipelineOutputUI ui = new MISAPipelineOutputUI(pipeline, dialogUI.getExportPath());
                        ui.setLocationRelativeTo(this);
                        ui.setVisible(true);
                    }
                });

                setEnabled(false);
                processUI.start();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void save() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if(fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
               pipeline.save(fileChooser.getSelectedFile().toPath());
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void open() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Gson gson = GsonUtils.getGson();
                pipeline = GsonUtils.fromJsonFile(gson, fileChooser.getSelectedFile().toPath(), MISAPipeline.class);
                refresh();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void refresh() {
        // Refresh the list of available modules
        uiParameterSchemata.clear();
        MISAModuleRepository.getInstance().refresh();
        DefaultListModel<MISAModule> model = new DefaultListModel<>();
        for(MISAModule module : MISAModuleRepository.getInstance().getModules()) {
            model.addElement(module);
            MISAModuleInstance instance = module.instantiate();
            instance.addSample("Preview");
            uiParameterSchemata.put(module, instance);
        }
        moduleList.setModel(model);

        // Update the editor UI
        pipelineEditor.setPipeline(pipeline);
    }

    private void addInstance() {
        if(moduleList.getSelectedValue() != null) {
            pipeline.addNode(moduleList.getSelectedValue());
        }
    }

    private void updateCacheTree() {
        if(moduleList.getSelectedValue() != null) {
            cacheTree.setSample(uiParameterSchemata.get(moduleList.getSelectedValue()).getSample("Preview"));
        }
    }

}
