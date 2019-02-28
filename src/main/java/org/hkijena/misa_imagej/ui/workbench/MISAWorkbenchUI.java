package org.hkijena.misa_imagej.ui.workbench;

import org.hkijena.misa_imagej.api.workbench.MISAOutput;
import org.hkijena.misa_imagej.ui.perfanalysis.MISARuntimeLogUI;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.hkijena.misa_imagej.utils.ui.DocumentTabPane;
import org.jdesktop.swingx.JXStatusBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MISAWorkbenchUI extends JFrame{

    private JLabel statusLabel;
    private MISAOutput misaOutput;

    private DocumentTabPane tabbedPane;
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

        tabbedPane = new DocumentTabPane();
        tabbedPane.addTab("Data browser", UIUtils.getIconFromResources("database.png"), cacheBrowserUI, DocumentTabPane.CloseMode.withDisabledCloseButton);
        tabbedPane.addTab("Runtime log",  UIUtils.getIconFromResources("clock.png"), runtimeLogUI, DocumentTabPane.CloseMode.withDisabledCloseButton);

        add(tabbedPane, BorderLayout.CENTER);

        initializeToolbar();

        initializeStatusBar();
        updateUI();
    }

    private void initializeToolbar() {
        JToolBar toolBar = new JToolBar();

        JButton openButton = new JButton("Open ...", UIUtils.getIconFromResources("open.png"));
        openButton.addActionListener(actionEvent -> open());
        toolBar.add(openButton);

        toolBar.add(Box.createHorizontalGlue());

        add(toolBar, BorderLayout.NORTH);
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

            runtimeLogUI.open(misaOutput.getRuntimeLogPath());
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
