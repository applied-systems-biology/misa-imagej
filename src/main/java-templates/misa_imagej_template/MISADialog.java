package misa_imagej_template;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import javax.swing.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.scif.services.DatasetIOService;

import net.imagej.DatasetService;
import misa_imagej_template.data.MISAExportedData;
import misa_imagej_template.json_schema.JSONSchemaObject;
import org.scijava.app.StatusService;
import org.scijava.display.DisplayService;
import org.scijava.log.LogService;
import org.scijava.thread.ThreadService;
import org.scijava.ui.UIService;

public class MISADialog extends JFrame {

    private MISACommand command;

    private AlgorithmParametersEditor algorithmParametersEditor;
    private RuntimeParametersEditor runtimeParametersEditor;
    private ObjectParametersEditor objectParametersEditor;
    private FilesystemParametersEditor filesystemParametersEditor;

    private MISAParameterSchema parameterSchema;


    /**
     * Create the dialog.
     */
    public MISADialog(MISACommand command) {
        this.command = command;
        initialize();
        loadSchema(MISADialog.class.getResourceAsStream("/parameter-schema.json"));
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
            setTitle("${project.name}".startsWith("MISA++") ? "${project.name}" : "MISA++ ${project.name}");

        } catch (IOException e) {
            e.printStackTrace();
        }

        parameterSchema.addObject("sample1");

        algorithmParametersEditor.setSchema(parameterSchema);
        runtimeParametersEditor.setSchema(parameterSchema);
        objectParametersEditor.setSchema(parameterSchema);
        filesystemParametersEditor.setSchema(parameterSchema);
    }

    private void writeParameterSchema(Path parameterSchema, Path importedDirectory, Path exportedDirectory, boolean forceCopy, boolean relativeDirectories) {
        setEnabled(false);
        getUiService().getDefaultUI().getConsolePane().show();
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
                    entry.getValue().install(exportRunPath, true, "--parameters parameters.json");
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

    private void importMISAOutputData(Path exportedPath) {
        getLogService().info("Importing results back into ImageJ ...");
        for(JSONSchemaObject obj : parameterSchema.getExportedFilesystemSchema().flatten()) {
            if(obj.filesystemData != null) {
                try {
                    ((MISAExportedData)obj.filesystemData).applyImportImageJAction(this, exportedPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void runMISA() {

        getUiService().getDefaultUI().getConsolePane().show();

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
                Path executableFile = MISAExecutable.getBestMatchingExecutable().install(dialog.getExecutablePath(), false, null);

                // Write the parameter schema
                writeParameterSchema(dialog.getParameterFilePath(), dialog.getImportedPath(), dialog.getExportedPath(), false, false);

                // Run the executable
                getLogService().info("Starting worker process ...");
                ProcessBuilder pb = new ProcessBuilder(executableFile.toString(), "--parameters", dialog.getParameterFilePath().toString());
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
                            return;
                        }

                        importMISAOutputData(dialog.getExportedPath());
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
        objectParametersEditor = new ObjectParametersEditor();
        algorithmParametersEditor = new AlgorithmParametersEditor();
        runtimeParametersEditor = new RuntimeParametersEditor();
        filesystemParametersEditor = new FilesystemParametersEditor(this);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Data", filesystemParametersEditor);
        tabbedPane.addTab("Sample parameters", objectParametersEditor);
        tabbedPane.addTab("Algorithm parameters", algorithmParametersEditor);
        tabbedPane.addTab("Runtime", runtimeParametersEditor);
        tabbedPane.addTab("Info", new InfoPanel());
        add(tabbedPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));

        JButton addSampleButton = new JButton("Add sample ...");
        addSampleButton.setToolTipText("Adds a new sample.");
        addSampleButton.addActionListener(actionEvent -> addSample());
        buttonPanel.add(addSampleButton);

        JButton batchMenuButton = new JButton("Batch ...");
        {
            JPopupMenu popupMenu = UIHelper.addPopupMenuToComponent(batchMenuButton);
            JMenuItem itemBatchFromStructure = new JMenuItem("Import from folder structure");
            itemBatchFromStructure.setToolTipText("Imports objects from a folder structure that mirrors the structure as seen in the input data editor.");
            popupMenu.add(itemBatchFromStructure);

            JMenuItem itemMergeSingle = new JMenuItem("Import for data type");
            itemMergeSingle.setToolTipText("Import matching data from a selection of files and folders");
            popupMenu.add(itemMergeSingle);
        }
        buttonPanel.add(batchMenuButton);

        buttonPanel.add(Box.createHorizontalGlue());

        JButton exportButton = new JButton("Export");
        exportButton.setToolTipText("Instead of running the MISA++ module, export all necessary files into a folder. This folder for example can be put onto a server.");
        exportButton.addActionListener(actionEvent -> exportMISARun());
        buttonPanel.add(exportButton);

        JButton runButton = new JButton("Run");
        runButton.setToolTipText("Runs the MISA++ module.");
        runButton.addActionListener(actionEvent -> runMISA());
        buttonPanel.add(runButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addSample() {
        String result = JOptionPane.showInputDialog(this, "Sample name", "Add sample", JOptionPane.PLAIN_MESSAGE);
        if(result != null && !result.isEmpty()) {
            parameterSchema.addObject(result);
        }
    }

    public LogService getLogService() {
        return command.log;
    }

    public StatusService getStatusService() {
        return command.status;
    }

    public ThreadService getThreadService() {
        return command.thread;
    }

    public UIService getUiService() {
        return command.ui;
    }

    public DatasetIOService getDatasetIOService() {
        return command.datasetIO;
    }

    public DisplayService getDisplayService() {
        return command.display;
    }
    public DatasetService getDatasetService() {
        return command.datasetService;
    }
}
