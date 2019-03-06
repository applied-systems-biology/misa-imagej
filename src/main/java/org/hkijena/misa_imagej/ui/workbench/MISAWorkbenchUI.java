package org.hkijena.misa_imagej.ui.workbench;

import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.MISAModuleInstance;
import org.hkijena.misa_imagej.api.repository.MISAModule;
import org.hkijena.misa_imagej.api.repository.MISAModuleRepository;
import org.hkijena.misa_imagej.api.workbench.MISAOutput;
import org.hkijena.misa_imagej.extension.datasources.MISAFolderLinkDataSource;
import org.hkijena.misa_imagej.ui.components.CancelableProcessUI;
import org.hkijena.misa_imagej.ui.components.MarkdownReader;
import org.hkijena.misa_imagej.ui.perfanalysis.MISARuntimeLogUI;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.hkijena.misa_imagej.utils.ui.DocumentTabPane;
import org.jdesktop.swingx.JXStatusBar;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MISAWorkbenchUI extends JFrame{

    private JLabel statusLabel;
    private MISAOutput misaOutput;

    private DocumentTabPane documentTabPane;
    private MISACacheBrowserUI cacheBrowserUI;
    private MISARuntimeLogUI runtimeLogUI;
    private List<MISAAttachmentBrowserUI> attachmentBrowserUIList = new ArrayList<>();

    public MISAWorkbenchUI() {
        initialize();
    }

    private void initialize() {
        setSize(800, 600);
        getContentPane().setLayout(new BorderLayout(8, 8));
        setTitle("MISA++ Workbench for ImageJ");
        setIconImage(UIUtils.getIconFromResources("misaxx.png").getImage());
        UIUtils.setToAskOnClose(this, "Do you really want to close this analysis tool?", "Close window");

        cacheBrowserUI = new MISACacheBrowserUI();
        runtimeLogUI = new MISARuntimeLogUI();
        runtimeLogUI.setHideOpenButton(true);

        documentTabPane = new DocumentTabPane();
        documentTabPane.addSingletonTab("DATA_BROWSER", "Data browser", UIUtils.getIconFromResources("database.png"), cacheBrowserUI, false);
        documentTabPane.addSingletonTab("RUNTIME_LOG", "Runtime log",  UIUtils.getIconFromResources("clock.png"), runtimeLogUI, false);

        documentTabPane.addSingletonTab("HELP", "Documentation", UIUtils.getIconFromResources("help.png"),
                MarkdownReader.fromResource("documentation/workbench.md"), true);

        add(documentTabPane, BorderLayout.CENTER);

        initializeToolbar();

        initializeStatusBar();
        updateUI();
    }

    private void initializeToolbar() {
        JToolBar toolBar = new JToolBar();

        JButton openButton = new JButton("Open ...", UIUtils.getIconFromResources("open.png"));
        openButton.addActionListener(actionEvent -> open());
        toolBar.add(openButton);
        toolBar.addSeparator();

        JButton openDataBrowserButton = new JButton("Browse data", UIUtils.getIconFromResources("database.png"));
        openDataBrowserButton.addActionListener(e -> documentTabPane.selectSingletonTab("DATA_BROWSER"));
        toolBar.add(openDataBrowserButton);

        JButton openRuntimeLogButton = new JButton("Analyze runtime",  UIUtils.getIconFromResources("clock.png"));
        openRuntimeLogButton.addActionListener(e -> documentTabPane.selectSingletonTab("RUNTIME_LOG"));
        toolBar.add(openRuntimeLogButton);

        JButton createAttachmentBrowserButton = new JButton("Analyze quantification results", UIUtils.getIconFromResources("graph.png"));
        createAttachmentBrowserButton.addActionListener(e -> openAttachmentBrowserTab());
        toolBar.add(createAttachmentBrowserButton);

        toolBar.add(Box.createHorizontalGlue());

        JButton helpButton = new JButton(UIUtils.getIconFromResources("help.png"));
        helpButton.addActionListener(e -> documentTabPane.selectSingletonTab("HELP"));
        toolBar.add(helpButton);

        add(toolBar, BorderLayout.NORTH);
    }

    private void openAttachmentBrowserTab() {
        if(misaOutput == null)
            return;
        if(!misaOutput.hasAttachmentIndex()) {
            MISAModule module = MISAModuleRepository.getInstance().getModule("misaxx-analyzer");
            if(module == null) {
                JOptionPane.showMessageDialog(this, "Please make sure that the 'MISA++ Analysis Helper' module is installed.",
                        "Unable to analyze quantification results", JOptionPane.ERROR_MESSAGE);
                return;
            }

            setEnabled(false);

            // Generate temporary "output"
            try {
                Path tmp = Files.createTempDirectory("misaxx-workbench");
                Files.createDirectories(tmp.resolve("input"));
                MISAModuleInstance moduleInstance = module.instantiate();
                moduleInstance.addSample("misaxx-output");

                MISACache ioCache = moduleInstance.getSample("misaxx-output").getImportedCaches().get(0);
                MISAFolderLinkDataSource ioCacheDataSource = ioCache.getDataSourceByType(MISAFolderLinkDataSource.class);
                ioCacheDataSource.setSourceFolder(misaOutput.getRootPath());
                ioCache.setDataSource(ioCacheDataSource);

                moduleInstance.install(tmp.resolve("parameters.json"), tmp.resolve("input"), tmp.resolve("output"), false, false);

                CancelableProcessUI processUI = new CancelableProcessUI(Arrays.asList(module.run(tmp.resolve("parameters.json"))));
                processUI.setLocationRelativeTo(this);

                // React to changes in status
                processUI.addPropertyChangeListener(propertyChangeEvent -> {
                    if(processUI.getStatus() == CancelableProcessUI.Status.Done) {
                        setEnabled(true);
                        openAttachmentBrowserTab();
                    }
                    else if(processUI.getStatus() == CancelableProcessUI.Status.Failed ||
                            processUI.getStatus() == CancelableProcessUI.Status.Canceled) {
                        setEnabled(true);
                        if(processUI.getStatus() == CancelableProcessUI.Status.Failed) {
                            JOptionPane.showMessageDialog(this,
                                    "There was an error during preprocessing. Please check the console to see the cause of this error.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });

                processUI.start();
            } catch (IOException e) {
                setEnabled(true);
                throw new RuntimeException(e);
            }
        }
        else {
            MISAAttachmentBrowserUI browserUI = new MISAAttachmentBrowserUI(misaOutput);
            documentTabPane.addTab("Attachment browser", UIUtils.getIconFromResources("graph.png"), browserUI, DocumentTabPane.CloseMode.withAskOnCloseButton, true);
            documentTabPane.setSelectedIndex(documentTabPane.getTabCount() - 1);
        }
    }

    private void initializeStatusBar() {
        JXStatusBar statusBar = new JXStatusBar();
        statusLabel = new JLabel("Ready");
        statusBar.add(statusLabel);
        add(statusBar, BorderLayout.SOUTH);
    }

    public void open(Path path) {
        setTitle("MISA++ Workbench for ImageJ");
        misaOutput = null;
        try {
            misaOutput = new MISAOutput(path);
            setTitle(misaOutput.getRootPath().toString() + " - MISA++ Workbench for ImageJ");

            runtimeLogUI.open(misaOutput.getRuntimeLog());
            cacheBrowserUI.setMisaOutput(misaOutput);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        updateUI();
    }

    private void open() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setDialogTitle("Open MISA++ output");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
           open(fileChooser.getSelectedFile().toPath());
        }
    }

    private void updateUI() {
    }
}
