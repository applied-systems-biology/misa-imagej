package org.hkijena.misa_imagej.ui.pipeliner;

import org.hkijena.misa_imagej.api.pipelining.MISAPipeline;
import org.hkijena.misa_imagej.api.pipelining.MISAPipelineNode;
import org.hkijena.misa_imagej.ui.workbench.MISAWorkbenchUI;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.hkijena.misa_imagej.utils.ui.MonochromeColorIcon;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hkijena.misa_imagej.utils.UIUtils.UI_PADDING;

public class MISAPipelineOutputUI extends JFrame {

    private MISAPipeline pipeline;
    private Path exportDirectory;

    public MISAPipelineOutputUI(MISAPipeline pipeline, Path exportDirectory) {
        this.pipeline = pipeline;
        this.exportDirectory = exportDirectory;
        initialize();
    }

    private void initialize() {
        setSize(800, 600);
        getContentPane().setLayout(new BorderLayout(8,8));
        setTitle("Pipeline calculation finished");
        setIconImage(UIUtils.getIconFromResources("misaxx.png").getImage());

        // Allow the user to open the result directory
        initializeToolbar();

        // Create a list of all nodes
        JPanel nodePanel = new JPanel(new GridBagLayout());
        int row = 0;
        for(MISAPipelineNode node : pipeline.traverse()) {

            JLabel icon = new JLabel(new MonochromeColorIcon(UIUtils.getIconFromResources("module-template.png"),
                    node.getModuleInstance().getModule().getModuleInfo().toColor()));
            JLabel type = new JLabel(node.getModuleInstance().getModuleInfo().getId());
            JTextField name = new JTextField(node.getName());
            name.setToolTipText(node.getDescription());
            name.setEditable(false);

            JButton analyzeButton = new JButton("Analyze result", UIUtils.getIconFromResources("graph.png"));
            analyzeButton.addActionListener(actionEvent -> {
                MISAWorkbenchUI ui = new MISAWorkbenchUI();
                ui.open(exportDirectory.resolve(node.getId()).resolve("exported"));
                ui.pack();
                ui.setSize(800, 600);
                ui.setVisible(true);
            });

            Path checkPath = exportDirectory.resolve(node.getId()).resolve("exported").resolve("runtime-log.json");
            if(!Files.exists(checkPath)) {
                analyzeButton.setIcon(UIUtils.getIconFromResources("error.png"));;
                analyzeButton.setText("Calculation error");
                analyzeButton.setEnabled(false);
            }

            final int r = row;
            nodePanel.add(icon, new GridBagConstraints() {
                {
                    gridx = 0;
                    gridy = r;
                    anchor = GridBagConstraints.WEST;
                    insets = UI_PADDING;
                }
            });
            nodePanel.add(type, new GridBagConstraints() {
                {
                    gridx = 1;
                    gridy = r;
                    anchor = GridBagConstraints.WEST;
                    insets = UI_PADDING;
                }
            });
            nodePanel.add(name, new GridBagConstraints() {
                {
                    gridx = 2;
                    gridy = r;
                    weightx = 1;
                    fill = GridBagConstraints.HORIZONTAL;
                    insets = UI_PADDING;
                }
            });
            nodePanel.add(analyzeButton, new GridBagConstraints() {
                {
                    gridx = 3;
                    gridy = r;
                    anchor = GridBagConstraints.WEST;
                    insets = UI_PADDING;
                }
            });

            ++row;
        }

        {
            final int r = row;
            nodePanel.add(new JPanel(), new GridBagConstraints() {
                {
                    anchor = GridBagConstraints.PAGE_START;
                    gridx = 2;
                    gridy = r;
                    fill = GridBagConstraints.HORIZONTAL | GridBagConstraints.VERTICAL;
                    weightx = 0;
                    weighty = 1;
                }
            });
        }

        add(new JScrollPane(nodePanel), BorderLayout.CENTER);
    }

    private void initializeToolbar() {
        JToolBar toolBar = new JToolBar();
        JButton openButton = new JButton("Open result directory", UIUtils.getIconFromResources("open.png"));
        openButton.addActionListener(actionEvent -> {
            try {
                Desktop.getDesktop().open(exportDirectory.toFile());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        toolBar.add(openButton);
        add(toolBar, BorderLayout.NORTH);
    }
}
