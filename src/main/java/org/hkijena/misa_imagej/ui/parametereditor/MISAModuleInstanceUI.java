package org.hkijena.misa_imagej.ui.parametereditor;

import org.hkijena.misa_imagej.api.MISAModuleInstance;
import org.hkijena.misa_imagej.api.MISAValidityReport;
import org.hkijena.misa_imagej.api.MISASample;
import org.hkijena.misa_imagej.ui.MISAValidityReportStatusUI;
import org.hkijena.misa_imagej.ui.repository.MISAModuleRepositoryUI;
import org.hkijena.misa_imagej.ui.workbench.MISAWorkbenchUI;
import org.hkijena.misa_imagej.utils.FilesystemUtils;
import org.hkijena.misa_imagej.utils.ProcessStreamToStringGobbler;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.jdesktop.swingx.JXStatusBar;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MISAModuleInstanceUI extends JFrame {



    private MISAModuleInstance moduleInstance;

    private MISAValidityReportStatusUI validityReportStatusUI;
    private JComboBox<MISASample> sampleList;

    /**
     * Create the dialog.
     */
    public MISAModuleInstanceUI(MISAModuleInstance moduleInstance, boolean editOnlyMode) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.moduleInstance = moduleInstance;
        initialize(editOnlyMode);

        // Create a new sample if necessary
        if(moduleInstance.getSamples().size() == 0)
            this.moduleInstance.addSample("New Sample");
        else
            refreshSampleList();
    }

    private void install(Path parameterSchema, Path importedDirectory, Path exportedDirectory, boolean forceCopy, boolean relativeDirectories) {
        setEnabled(false);
        MISAModuleRepositoryUI.getInstance().getCommand().getUiService().getDefaultUI().getConsolePane().show();
        this.moduleInstance.install(parameterSchema, importedDirectory, exportedDirectory, forceCopy, relativeDirectories);
        setEnabled(true);
    }

    private boolean parametersAreValid() {
        MISAValidityReport report = moduleInstance.getValidityReport();
        validityReportStatusUI.setReport(report);
        return report.isValid();
    }

    /**
     * Exports the current settings into a user-selected folder
     */
    private void exportMISARun() {

       if(!parametersAreValid())
           return;

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Open folder");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Path exportRunPath = chooser.getSelectedFile().toPath();

                if (!FilesystemUtils.directoryIsEmpty(exportRunPath)) {
                    JOptionPane.showMessageDialog(this,
                            "The directory " + exportRunPath.toString() + " must be empty!",
                            "Export run",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Path importedPath = exportRunPath.resolve("imported");
                Path exportedPath = exportRunPath.resolve("exported");
                Files.createDirectories(exportRunPath);

                // Write the parameter schema
                install(exportRunPath.resolve("parameters.json"),
                        importedPath,
                        exportedPath,
                        true,
                        true);

                if (JOptionPane.showConfirmDialog(this,
                        "Export successful. Do you want to open the output directory?",
                        "Export run", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    Desktop.getDesktop().open(exportRunPath.toFile());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void runMISA() {

        if(!parametersAreValid())
            return;

        MISAModuleRepositoryUI.getInstance().getCommand().getUiService().getDefaultUI().getConsolePane().show();

        MISARunModuleDialogUI dialog = new MISARunModuleDialogUI(this);
        dialog.setLocationRelativeTo(this);
        if (dialog.showDialog() == MISARunModuleDialogUI.ACCEPT_OPTION) {
            try {
                Files.createDirectories(dialog.getImportedPath());
                Files.createDirectories(dialog.getExportedPath());
                Files.createDirectories(dialog.getParameterFilePath().getParent());

                // Write the parameter schema
                install(dialog.getParameterFilePath(), dialog.getImportedPath(), dialog.getExportedPath(), false, false);

                // Run the executable
                MISAModuleRepositoryUI.getInstance().getCommand().getLogService().info("Starting worker process ...");
                ProcessBuilder pb = new ProcessBuilder(getModuleInstance().getModule().getExecutablePath(), "--parameters", dialog.getParameterFilePath().toString());
                Process p = pb.start();
                new ProcessStreamToStringGobbler(p.getInputStream(), s -> MISAModuleRepositoryUI.getInstance().getCommand().getLogService().info(s)).start();
                new ProcessStreamToStringGobbler(p.getErrorStream(), s -> MISAModuleRepositoryUI.getInstance().getCommand().getLogService().error(s)).start();

                CancelableProcessUI processUI = new CancelableProcessUI(p);
                processUI.setLocationRelativeTo(this);

                // React to changes in status
                processUI.addPropertyChangeListener(propertyChangeEvent -> {
                    if(processUI.getStatus() == CancelableProcessUI.Status.Done) {
                        setEnabled(true);
                        if(JOptionPane.showConfirmDialog(this, "The calculated finished. Do you want to analyze the results?",
                                "Calulcation finished", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                            MISAWorkbenchUI ui = new MISAWorkbenchUI();
                            ui.setVisible(true);
                            ui.open(dialog.getExportedPath());
                        }
                    }
                    else if(processUI.getStatus() == CancelableProcessUI.Status.Failed ||
                            processUI.getStatus() == CancelableProcessUI.Status.Canceled) {
                        setEnabled(true);
                        if(processUI.getStatus() == CancelableProcessUI.Status.Failed) {
                            JOptionPane.showMessageDialog(this, "There was an error during calculation. Please check the console to see the cause of this error.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });

                setEnabled(false);
                processUI.showDialog();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initialize(boolean editOnlyMode) {
        setSize(800, 600);
        getContentPane().setLayout(new BorderLayout(8, 8));
        setTitle("MISA++ for ImageJ - " + getModuleInstance().getModuleInfo().toString());
        setIconImage(UIUtils.getIconFromResources("misaxx.png").getImage());
        SampleParametersEditorUI sampleParametersEditorUI = new SampleParametersEditorUI(this);
        AlgorithmParametersEditorUI algorithmParametersEditorUI = new AlgorithmParametersEditorUI(this);
        RuntimeParametersEditorUI runtimeParametersEditorUI = new RuntimeParametersEditorUI(this);
        SampleDataEditorUI sampleDataEditorUI = new SampleDataEditorUI(this);

        // Menu bar
        initializeMenuBar();

        // Tabs with settings
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Data", sampleDataEditorUI);
        tabbedPane.addTab("Sample parameters", sampleParametersEditorUI);
        tabbedPane.addTab("Algorithm parameters", algorithmParametersEditorUI);
        tabbedPane.addTab("Runtime", runtimeParametersEditorUI);
        add(tabbedPane, BorderLayout.CENTER);

        // Toolbar
        JToolBar toolBar = new JToolBar();

        initalizeSampleManagerUI(toolBar);

        toolBar.add(Box.createHorizontalGlue());

        JButton validateButton = new JButton("Check parameters", UIUtils.getIconFromResources("checkmark.png"));
        validateButton.addActionListener(actionEvent -> parametersAreValid());
        toolBar.add(validateButton);

        if(!editOnlyMode) {
            JButton exportButton = new JButton("Export", UIUtils.getIconFromResources("export.png"));
            exportButton.setToolTipText("Instead of running the MISA++ module, export all necessary files into a folder. This folder for example can be put onto a server.");
            exportButton.addActionListener(actionEvent -> exportMISARun());
            toolBar.add(exportButton);

            JButton runButton = new JButton("Run", UIUtils.getIconFromResources("run.png"));
            runButton.setToolTipText("Runs the MISA++ module.");
            runButton.addActionListener(actionEvent -> runMISA());
            toolBar.add(runButton);
        }

        add(toolBar, BorderLayout.NORTH);

        // Status bar
        JXStatusBar statusBar = new JXStatusBar();
        validityReportStatusUI = new MISAValidityReportStatusUI();
        statusBar.add(validityReportStatusUI);
        add(statusBar, BorderLayout.SOUTH);
    }

    private void initializeMenuBar() {
//        JMenuBar menuBar = new JMenuBar();
//        JMenu samplesMenu = new JMenu("Samples");
//        menuBar.add(samplesMenu);
//        setJMenuBar(menuBar);
    }

    private void initalizeSampleManagerUI(JToolBar toolBar) {
        // Add sample button
        JButton addSampleButton = new JButton("Add sample", UIUtils.getIconFromResources("add.png"));
        addSampleButton.addActionListener(actionEvent -> addSample());
        toolBar.add(addSampleButton);

        {
            sampleList = new JComboBox<>();
            moduleInstance.addPropertyChangeListener(propertyChangeEvent -> {
                if(propertyChangeEvent.getPropertyName().equals("samples")) {
                    refreshSampleList();
                }
                else if(propertyChangeEvent.getPropertyName().equals("currentSample")) {
                    refreshSampleList();
                }
            });
            sampleList.addItemListener(itemEvent -> {
                if(moduleInstance.getSamples().size() > 0 && sampleList.getSelectedItem() != null) {
                    moduleInstance.setCurrentSample(((MISASample)sampleList.getSelectedItem()).name);
                }
            });
            toolBar.add(sampleList);
        }

        // Remove sample button
        JButton removeSampleButton = new JButton(UIUtils.getIconFromResources("delete.png"));
        removeSampleButton.setToolTipText("Remove current sample");
        removeSampleButton.addActionListener(actionEvent -> removeSample());
        toolBar.add(removeSampleButton);
    }

    private void refreshSampleList() {
        DefaultComboBoxModel<MISASample> model = new DefaultComboBoxModel<>();
        for(MISASample sample : moduleInstance.getSamples()) {
            model.addElement(sample);
        }
        sampleList.setModel(model);
        if(moduleInstance.getCurrentSample() != null) {
            sampleList.setSelectedItem(moduleInstance.getCurrentSample());
            sampleList.setEnabled(true);
        }
        else {
            sampleList.setEnabled(false);
        }
    }

    private void addSample() {
        String result = JOptionPane.showInputDialog(this, "Sample name", "Add sample", JOptionPane.PLAIN_MESSAGE);
        if(result != null && !result.isEmpty()) {
            moduleInstance.addSample(result);
        }
    }

    private void removeSample() {
        if(JOptionPane.showConfirmDialog(this,
                "Do you really want to remove this sample?",
                "Remove sample", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            moduleInstance.removeSample(moduleInstance.getCurrentSample().name);
        }
    }

    public MISAModuleInstance getModuleInstance() {
        return moduleInstance;
    }

}
