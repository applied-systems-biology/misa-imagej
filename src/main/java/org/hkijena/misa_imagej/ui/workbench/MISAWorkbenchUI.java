package org.hkijena.misa_imagej.ui.workbench;

import org.hkijena.misa_imagej.api.workbench.MISAOutput;
import org.hkijena.misa_imagej.ui.perfanalysis.MISARuntimeLogUI;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.jdesktop.swingx.JXStatusBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Path;

public class MISAWorkbenchUI extends JFrame{

    private JLabel statusLabel;
    private JButton showRuntimeLogButton;
    private MISAOutput misaOutput;

    public MISAWorkbenchUI() {
        initialize();
    }

    private void initialize() {
        setSize(800, 600);
        getContentPane().setLayout(new BorderLayout(8, 8));
        setTitle("MISA++ Workbench for ImageJ");
        setIconImage(UIUtils.getIconFromResources("misaxx.png").getImage());
        UIUtils.setToAskOnClose(this, "Do you really want to close this analysis tool?", "Close window");

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

        showRuntimeLogButton = new JButton("Analyze runtime", UIUtils.getIconFromResources("clock.png"));
        showRuntimeLogButton.addActionListener(actionEvent -> showRuntimeLog());
        toolBar.add(showRuntimeLogButton);

        add(toolBar, BorderLayout.NORTH);
    }

    private void initializeStatusBar() {
        JXStatusBar statusBar = new JXStatusBar();
        statusLabel = new JLabel("Ready");
        statusBar.add(statusLabel);
        add(statusBar, BorderLayout.SOUTH);
    }

    private void showRuntimeLog() {
        if(misaOutput != null && misaOutput.getRuntimeLogPath().toFile().isFile()) {
            MISARuntimeLogUI ui = new MISARuntimeLogUI();
            ui.setHideOpenButton(true);
            ui.open(misaOutput.getRuntimeLogPath());
            ui.setTitle(misaOutput.getRootPath().toString() + " - MISA++ runtime analysis");
            ui.pack();
            ui.setSize(new Dimension(800,600));
            ui.setVisible(true);
        }
    }

    public void open(Path path) {
        setTitle("MISA++ Workbench for ImageJ");
        misaOutput = null;
        try {
            misaOutput = new MISAOutput(path);
            setTitle(misaOutput.getRootPath().toString() + " - MISA++ Workbench for ImageJ");
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
        showRuntimeLogButton.setEnabled(misaOutput != null && misaOutput.getRuntimeLogPath().toFile().isFile());
    }
}
