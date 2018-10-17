package org.hkijena.misa_imagej;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import javax.swing.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.scif.services.DatasetIOService;

import org.hkijena.misa_imagej.data.MISAExportedData;
import org.hkijena.misa_imagej.json_schema.JSONSchemaObject;
import org.scijava.app.StatusService;
import org.scijava.command.CommandService;
import org.scijava.display.DisplayService;
import org.scijava.log.LogService;
import org.scijava.thread.ThreadService;
import org.scijava.ui.UIService;

public class MISADialog extends JFrame {

    private LogService log;
    private StatusService status;
    private ThreadService thread;
    private UIService ui;
    private DatasetIOService datasetIO;
    private DisplayService display;

    private AlgorithmParametersEditor algorithmParametersEditor;
    private RuntimeParametersEditor runtimeParametersEditor;
    private ObjectParametersEditor objectParametersEditor;
    private FilesystemParametersEditor filesystemParametersEditor;

    private MISAParameterSchema parameterSchema;


    /**
     * Create the dialog.
     */
    public MISADialog() {
        initialize();
        loadSchema(MISADialog.class.getResourceAsStream("/param_schema.json"));
    }

    /**
     * Loads the schema from a stream
     *
     * @param stream
     */
    private void loadSchema(InputStream stream) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        try (InputStreamReader r = new InputStreamReader(stream)) {

            JSONSchemaObject schema = gson.fromJson(r, JSONSchemaObject.class);
            schema.id = "parameters";
            schema.update();
            parameterSchema = new MISAParameterSchema(schema);
            setTitle("MISA++ Plugin");

        } catch (IOException e) {
            e.printStackTrace();
        }

        parameterSchema.addObject("object1");

        algorithmParametersEditor.setSchema(parameterSchema);
        runtimeParametersEditor.setSchema(parameterSchema);
        objectParametersEditor.setSchema(parameterSchema);
        filesystemParametersEditor.setSchema(parameterSchema);
    }

    private void writeParameterSchema(Path parameterSchema, Path importedDirectory, Path exportedDirectory, boolean forceCopy, boolean relativeDirectories) {
        setEnabled(false);
        getUi().getDefaultUI().getConsolePane().show();
        this.parameterSchema.writeParameterJSON(this, parameterSchema, importedDirectory, exportedDirectory, forceCopy, relativeDirectories);
        setEnabled(true);
    }


    /**
     * Exports the current settings into a user-selected folder
     */
    private void exportMISARun() {

        if (!parameterSchema.canWriteParameterJSON(this))
            return;

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Open folder");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Path exportRunPath = chooser.getSelectedFile().toPath();

                if (!IOHelper.directoryIsEmpty(exportRunPath)) {
                    JOptionPane.showMessageDialog(this,
                            "The directory " + exportRunPath.toString() + " must be empty!",
                            "Export run",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Path importedPath = exportRunPath.resolve("imported");
                Path exportedPath = exportRunPath.resolve("exported");
                Files.createDirectories(exportRunPath);

                // Export all available executables
                for (Map.Entry<OSHelper.OperatingSystem, MISAExecutable> entry : MISAExecutable.getAvailableExecutables().entrySet()) {
                    entry.getValue().writeToFile(exportRunPath.resolve(entry.getValue().getName()));
                }

                // Write the parameter schema
                writeParameterSchema(exportRunPath.resolve("parameters.json"),
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

        getUi().getDefaultUI().getConsolePane().show();

        if (!parameterSchema.canWriteParameterJSON(this))
            return;

        MISARunDialog dialog = new MISARunDialog(this);
        dialog.setLocationRelativeTo(this);
        if (dialog.showDialog() == MISARunDialog.ACCEPT_OPTION) {
            try {
                Files.createDirectories(dialog.getImportedPath());
                Files.createDirectories(dialog.getExportedPath());
                Files.createDirectories(dialog.getParameterFilePath().getParent());
                Files.createDirectories(dialog.getExecutablePath().getParent());

                if (!IOHelper.directoryIsEmpty(dialog.getImportedPath())) {
                    JOptionPane.showMessageDialog(this, "The directory " + dialog.getImportedPath().toString() + " must be empty!", "Run", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!IOHelper.directoryIsEmpty(dialog.getExportedPath())) {
                    JOptionPane.showMessageDialog(this, "The directory " + dialog.getExportedPath().toString() + " must be empty!", "Run", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Export only the matching executable
                MISAExecutable.getBestMatchingExecutable().writeToFile(dialog.getExecutablePath());

                // Write the parameter schema
                writeParameterSchema(dialog.getParameterFilePath(), dialog.getImportedPath(), dialog.getExportedPath(), false, false);

                // Run the executable
                getLog().info("Starting worker process ...");
                ProcessBuilder pb = new ProcessBuilder(dialog.getExecutablePath().toString(), "--parameters", dialog.getParameterFilePath().toString());
                Process p = pb.start();
                new ProcessStreamToStringGobbler(p.getInputStream(), s -> getLog().info(s)).start();
                new ProcessStreamToStringGobbler(p.getErrorStream(), s -> getLog().error(s)).start();

                CancelableProcessUI processUI = new CancelableProcessUI(p);
                processUI.setLocationRelativeTo(this);
                processUI.showDialog();

                if(processUI.getStatus() == CancelableProcessUI.Status.Failed) {
                    JOptionPane.showMessageDialog(this, "There was an error during calculation. Please check the console to see the cause of this error.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                getLog().info("Importing results back into ImageJ ...");
                for(JSONSchemaObject obj : parameterSchema.getExportedFilesystemSchema().flatten()) {
                    if(obj.filesystemData != null)
                        ((MISAExportedData)obj.filesystemData).applyImportImageJAction(this, dialog.getExportedPath());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initialize() {
        setSize(800, 600);
        getContentPane().setLayout(new BorderLayout(8, 8));
        objectParametersEditor = new ObjectParametersEditor();
        algorithmParametersEditor = new AlgorithmParametersEditor();
        runtimeParametersEditor = new RuntimeParametersEditor();
        filesystemParametersEditor = new FilesystemParametersEditor();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Data", filesystemParametersEditor);
        tabbedPane.addTab("Object parameters", objectParametersEditor);
        tabbedPane.addTab("Algorithm parameters", algorithmParametersEditor);
        tabbedPane.addTab("Runtime", runtimeParametersEditor);
        tabbedPane.addTab("Info", new InfoPanel());
        add(tabbedPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        add(buttonPanel, BorderLayout.SOUTH);

        JButton exportButton = new JButton("Export");
        exportButton.setToolTipText("Instead of running the MISA++ module, export all necessary files into a folder. This folder for example can be put onto a server.");
        exportButton.addActionListener(actionEvent -> exportMISARun());
        buttonPanel.add(exportButton);

        JButton runButton = new JButton("Run");
        runButton.setToolTipText("Runs the MISA++ module.");
        runButton.addActionListener(actionEvent -> runMISA());
        buttonPanel.add(runButton);
    }

    public LogService getLog() {
        return log;
    }

    public void setLog(final LogService log) {
        this.log = log;
    }

    public StatusService getStatus() {
        return status;
    }

    public void setStatus(final StatusService status) {
        this.status = status;
    }

    public ThreadService getThread() {
        return thread;
    }

    public void setThread(final ThreadService thread) {
        this.thread = thread;
    }

    public UIService getUi() {
        return ui;
    }

    public void setUi(final UIService ui) {
        this.ui = ui;
    }

    public DatasetIOService getDatasetIO() {
        return datasetIO;
    }

    public void setDatasetIO(DatasetIOService datasetIO) {
        this.datasetIO = datasetIO;
    }

    public DisplayService getDisplay() {
        return display;
    }

    public void setDisplay(DisplayService display) {
        this.display = display;
    }
}
