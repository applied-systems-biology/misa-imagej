package org.hkijena.misa_imagej.parametereditor;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.scif.services.DatasetIOService;

import net.imagej.DatasetService;
import org.hkijena.misa_imagej.MISACommand;
import org.hkijena.misa_imagej.repository.MISAModule;
import org.hkijena.misa_imagej.utils.*;
import org.hkijena.misa_imagej.parametereditor.json_schema.JSONSchemaObject;
import org.scijava.app.StatusService;
import org.scijava.display.DisplayService;
import org.scijava.log.LogService;
import org.scijava.thread.ThreadService;
import org.scijava.ui.UIService;

public class MISAModuleParameterEditorUI extends JFrame {

    private MISACommand command;
    private MISAModule module;

    private AlgorithmParametersEditorUI algorithmParametersEditorUI;
    private RuntimeParametersEditorUI runtimeParametersEditorUI;
    private SampleParametersEditorUI sampleParametersEditorUI;
    private SampleDataEditorUI sampleDataEditorUI;

    private MISAParameterSchema parameterSchema;

    /**
     * Create the dialog.
     */
    public MISAModuleParameterEditorUI(MISACommand command, MISAModule module) {
        this.command = command;
        this.module = module;
        loadSchema();
        initialize();
        parameterSchema.addSample("New Sample");
    }

    /**
     * Loads the module schema into the editor
     */
    private void loadSchema() {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        JSONSchemaObject schema = gson.fromJson(module.getParameterSchema(), JSONSchemaObject.class);
        schema.id = "parameters";
        schema.update();
        parameterSchema = new MISAParameterSchema(schema);
        setTitle("MISA++ for ImageJ - " + module.getModuleInfo().toString());
    }

    private void install(Path parameterSchema, Path importedDirectory, Path exportedDirectory, boolean forceCopy, boolean relativeDirectories) {
        setEnabled(false);
        getUiService().getDefaultUI().getConsolePane().show();
        this.parameterSchema.install(this, parameterSchema, importedDirectory, exportedDirectory, forceCopy, relativeDirectories);
        setEnabled(true);
    }


    /**
     * Exports the current settings into a user-selected folder
     */
    private void exportMISARun() {

        ParameterSchemaValidityReport report = parameterSchema.isValidParameter();

        if (!report.isValid())
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

        getUiService().getDefaultUI().getConsolePane().show();

        ParameterSchemaValidityReport report = parameterSchema.isValidParameter();

        if (!report.isValid())
            return;

        MISARunDialog dialog = new MISARunDialog(this);
        dialog.setLocationRelativeTo(this);
        if (dialog.showDialog() == MISARunDialog.ACCEPT_OPTION) {
            try {
                Files.createDirectories(dialog.getImportedPath());
                Files.createDirectories(dialog.getExportedPath());
                Files.createDirectories(dialog.getParameterFilePath().getParent());
                Files.createDirectories(dialog.getExecutablePath().getParent());

                if (!FilesystemUtils.directoryIsEmpty(dialog.getImportedPath())) {
                    JOptionPane.showMessageDialog(this, "The directory " + dialog.getImportedPath().toString() + " must be empty!", "Run", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!FilesystemUtils.directoryIsEmpty(dialog.getExportedPath())) {
                    JOptionPane.showMessageDialog(this, "The directory " + dialog.getExportedPath().toString() + " must be empty!", "Run", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Write the parameter schema
                install(dialog.getParameterFilePath(), dialog.getImportedPath(), dialog.getExportedPath(), false, false);

                // Run the executable
                getLogService().info("Starting worker process ...");
                ProcessBuilder pb = new ProcessBuilder(module.executablePath, "--parameters", dialog.getParameterFilePath().toString());
                Process p = pb.start();
                new ProcessStreamToStringGobbler(p.getInputStream(), s -> getLogService().info(s)).start();
                new ProcessStreamToStringGobbler(p.getErrorStream(), s -> getLogService().error(s)).start();

                CancelableProcessUI processUI = new CancelableProcessUI(p);
                processUI.setLocationRelativeTo(this);

                // React to changes in status
                processUI.addPropertyChangeListener(propertyChangeEvent -> {
                    if(processUI.getStatus() == CancelableProcessUI.Status.Done ||
                            processUI.getStatus() == CancelableProcessUI.Status.Failed ||
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

    private void initialize() {
        setSize(800, 600);
        getContentPane().setLayout(new BorderLayout(8, 8));
        sampleParametersEditorUI = new SampleParametersEditorUI(this);
        algorithmParametersEditorUI = new AlgorithmParametersEditorUI(this);
        runtimeParametersEditorUI = new RuntimeParametersEditorUI(this);
        sampleDataEditorUI = new SampleDataEditorUI(this);

        // Tabs with settings
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Data", sampleDataEditorUI);
        tabbedPane.addTab("Sample parameters", sampleParametersEditorUI);
        tabbedPane.addTab("Algorithm parameters", algorithmParametersEditorUI);
        tabbedPane.addTab("Runtime", runtimeParametersEditorUI);
        tabbedPane.addTab("Info", new InfoPanelUI());
        add(tabbedPane, BorderLayout.CENTER);

        // Toolbar
        JToolBar toolBar = new JToolBar();

        initalizeSampleManagerUI(toolBar);

        toolBar.add(Box.createHorizontalGlue());

        JButton exportButton = new JButton("Export", UIUtils.getIconFromResources("export.png"));
        exportButton.setToolTipText("Instead of running the MISA++ module, export all necessary files into a folder. This folder for example can be put onto a server.");
        exportButton.addActionListener(actionEvent -> exportMISARun());
        toolBar.add(exportButton);

        JButton runButton = new JButton("Run", UIUtils.getIconFromResources("run.png"));
        runButton.setToolTipText("Runs the MISA++ module.");
        runButton.addActionListener(actionEvent -> runMISA());
        toolBar.add(runButton);

        add(toolBar, BorderLayout.NORTH);
    }

    private void initalizeSampleManagerUI(JToolBar toolBar) {
        // Add sample button
        JButton addSampleButton = new JButton("Add sample", UIUtils.getIconFromResources("add.png"));
        addSampleButton.addActionListener(actionEvent -> addSample());
        toolBar.add(addSampleButton);

        // Batch-add button
        JButton batchAddSampleButton = new JButton("Batch ...", UIUtils.getIconFromResources("batch-add.png"));
        toolBar.add(batchAddSampleButton);

        {
            JComboBox<MISASample> sampleList = new JComboBox<>();
            parameterSchema.addPropertyChangeListener(propertyChangeEvent -> {
                if(propertyChangeEvent.getPropertyName().equals("samples")) {
                    DefaultComboBoxModel<MISASample> model = new DefaultComboBoxModel<>();
                    for(MISASample sample : parameterSchema.getSamples()) {
                        model.addElement(sample);
                    }
                    sampleList.setModel(model);
                }
                else if(propertyChangeEvent.getPropertyName().equals("currentSample")) {
                    if(parameterSchema.getCurrentSample() != null) {
                        sampleList.setSelectedItem(parameterSchema.getCurrentSample());
                        sampleList.setEnabled(true);
                    }
                    else {
                        sampleList.setEnabled(false);
                    }
                }
            });
            sampleList.addItemListener(itemEvent -> {
                if(parameterSchema.getSamples().size() > 0 && sampleList.getSelectedItem() != null) {
                    parameterSchema.setCurrentSample(((MISASample)sampleList.getSelectedItem()).name);
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

    private void addSample() {
        String result = JOptionPane.showInputDialog(this, "Sample name", "Add sample", JOptionPane.PLAIN_MESSAGE);
        if(result != null && !result.isEmpty()) {
            parameterSchema.addSample(result);
        }
    }

    private void removeSample() {
        if(JOptionPane.showConfirmDialog(this,
                "Do you really want to remove this sample?",
                "Remove sample", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            parameterSchema.removeSample(parameterSchema.getCurrentSample().name);
        }
    }

    public MISAParameterSchema getParameterSchema() {
        return parameterSchema;
    }

    public LogService getLogService() {
        return command.getLogService();
    }

    public StatusService getStatusService() {
        return command.getStatusService();
    }

    public ThreadService getThreadService() {
        return command.getThreadService();
    }

    public UIService getUiService() {
        return command.getUiService();
    }

    public DatasetIOService getDatasetIOService() {
        return command.getDatasetIOService();
    }

    public DisplayService getDisplayService() {
        return command.getDisplayService();
    }

    public DatasetService getDatasetService() {
        return command.getDatasetService();
    }
}
