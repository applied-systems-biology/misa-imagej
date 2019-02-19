package org.hkijena.misa_imagej.ui.parametereditor;

import org.hkijena.misa_imagej.api.MISAModuleInstance;
import org.hkijena.misa_imagej.api.MISAValidityReport;
import org.hkijena.misa_imagej.ui.components.MISAValidityReportStatusUI;
import org.hkijena.misa_imagej.ui.components.CancelableProcessUI;
import org.hkijena.misa_imagej.ui.repository.MISAModuleRepositoryUI;
import org.hkijena.misa_imagej.ui.workbench.MISAWorkbenchUI;
import org.hkijena.misa_imagej.utils.FilesystemUtils;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.jdesktop.swingx.JXStatusBar;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class MISAModuleInstanceUI extends JFrame {



    private org.hkijena.misa_imagej.api.MISAModuleInstance moduleInstance;

    private MISAValidityReportStatusUI validityReportStatusUI;
//    private JComboBox<MISASample> sampleList;

    /**
     * Create the dialog.
     */
    public MISAModuleInstanceUI(org.hkijena.misa_imagej.api.MISAModuleInstance moduleInstance, boolean editOnlyMode, boolean addDefaultSample) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.moduleInstance = moduleInstance;
        initialize(editOnlyMode);

        // Create a new sample if necessary
        if(moduleInstance.getSamples().size() == 0 && addDefaultSample)
            this.moduleInstance.addSample("New Sample");
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
                CancelableProcessUI processUI = new CancelableProcessUI(Arrays.asList(getModuleInstance().getModule().run(dialog.getParameterFilePath())));
                processUI.setLocationRelativeTo(this);

                // React to changes in status
                processUI.addPropertyChangeListener(propertyChangeEvent -> {
                    if(processUI.getStatus() == CancelableProcessUI.Status.Done) {
                        setEnabled(true);
                        if(JOptionPane.showConfirmDialog(this, "The calculated finished. Do you want to analyze the results?",
                                "Calculation finished", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                            MISAWorkbenchUI ui = new MISAWorkbenchUI();
                            ui.setVisible(true);
                            ui.open(dialog.getExportedPath());
                        }
                    }
                    else if(processUI.getStatus() == CancelableProcessUI.Status.Failed ||
                            processUI.getStatus() == CancelableProcessUI.Status.Canceled) {
                        setEnabled(true);
                        if(processUI.getStatus() == CancelableProcessUI.Status.Failed) {
                            JOptionPane.showMessageDialog(this,
                                    "There was an error during calculation. Please check the console to see the cause of this error.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });

                setEnabled(false);
                processUI.start();

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
        if(!editOnlyMode)
            UIUtils.setToAskOnClose(this, "Do you really want to close this parameter editor?", "Close window");

        MISASampleManagerUI sampleManagerUI = new MISASampleManagerUI(getModuleInstance());
        MISASampleParametersUI sampleParametersEditorUI = new MISASampleParametersUI(getModuleInstance());
        AlgorithmParametersEditorUI algorithmParametersEditorUI = new AlgorithmParametersEditorUI(getModuleInstance());
        MISARuntimeParametersUI MISARuntimeParametersUI = new MISARuntimeParametersUI(getModuleInstance());
        MISASampleCachesUI MISASampleCachesUI = new MISASampleCachesUI(getModuleInstance());

        // Tabs with settings
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Samples", sampleManagerUI);
        tabbedPane.addTab("Data", MISASampleCachesUI);
        tabbedPane.addTab("Sample parameters", sampleParametersEditorUI);
        tabbedPane.addTab("Algorithm parameters", algorithmParametersEditorUI);
        tabbedPane.addTab("Runtime", MISARuntimeParametersUI);
        add(tabbedPane, BorderLayout.CENTER);

        // Toolbar
        JToolBar toolBar = new JToolBar();

        JButton importParametersButton = new JButton("Import parameters", UIUtils.getIconFromResources("open.png"));
        importParametersButton.addActionListener(e -> importParameters());
        toolBar.add(importParametersButton);

        JButton importFolderButton = new JButton("Import folder", UIUtils.getIconFromResources("open.png"));
        importFolderButton.addActionListener(e -> importFolder());
        toolBar.add(importFolderButton);

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

    private void importFolder() {
    }

    private void importParameters() {
    }

    public MISAModuleInstance getModuleInstance() {
        return moduleInstance;
    }

}
