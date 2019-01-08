package org.hkijena.misa_imagej;

import org.hkijena.misa_imagej.repository.MISAModule;
import org.hkijena.misa_imagej.repository.MISAModuleRepository;
import org.hkijena.misa_imagej.utils.UIUtils;

import javax.swing.*;
import java.awt.*;

/**
 * User interface that allows the user to manage and select MISA++ modules
 */
public class MISAModuleManagerUI extends JFrame {

    private MISACommand command;
    private MISAModuleRepository moduleRepository;
    private static MISAModuleManagerUI instance;
    private JList<MISAModule> misaModuleJList;

    public static MISAModuleManagerUI getInstance(MISACommand command) {
        if(instance == null)
            instance = new MISAModuleManagerUI(command);
        return instance;
    }

    private MISAModuleManagerUI(MISACommand command) {
        instance = this;
        moduleRepository = new MISAModuleRepository(command);
        this.command = command;
        initialize();
        refreshModuleList();
    }

    private void initialize() {
        setSize(800, 600);
        getContentPane().setLayout(new BorderLayout(8, 8));
        setTitle("MISA++ for ImageJ - Module manager");

        // Detail panel
        JLabel descriptionTitle;
        JLabel descriptionVersionId;
        JLabel descriptionSourceFile;
        JPanel detailPanel = new JPanel(new BorderLayout(8, 8));
        {
            JPanel descriptionPanel = new JPanel(new GridBagLayout());
            descriptionTitle = UIUtils.createDescriptionLabelUI(descriptionPanel, "<Title>", 0, 0);
            descriptionTitle.setFont(descriptionTitle.getFont().deriveFont(18f));
            descriptionVersionId = UIUtils.createDescriptionLabelUI(descriptionPanel, "<VersionID>", 1, 0);
            descriptionSourceFile = UIUtils.createDescriptionLabelUI(descriptionPanel, "<SourceFile>", 2, 0);
            UIUtils.addFillerGridBagComponent(descriptionPanel, 3);

            detailPanel.add(descriptionPanel, BorderLayout.CENTER);

            JButton launchButton = new JButton("Launch");
            launchButton.addActionListener(actionEvent -> {
                MISAModuleUI launcher = new MISAModuleUI(command, misaModuleJList.getSelectedValue());
                launcher.setVisible(true);
            });
            detailPanel.add(launchButton, BorderLayout.SOUTH);
        }

        // List of modules
        misaModuleJList = new JList<>(new DefaultListModel<>());
        misaModuleJList.addListSelectionListener(listSelectionEvent -> {
            descriptionTitle.setText(misaModuleJList.getSelectedValue().name);
            descriptionVersionId.setText(misaModuleJList.getSelectedValue().id + " version " + misaModuleJList.getSelectedValue().version);
            descriptionSourceFile.setText(misaModuleJList.getSelectedValue().definitionPath);
        });

        // Arrange in split panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, misaModuleJList, detailPanel);
        add(splitPane, BorderLayout.CENTER);
    }

    private void refreshModuleList() {
        moduleRepository.refresh();
        DefaultListModel<MISAModule> model = (DefaultListModel<MISAModule>)misaModuleJList.getModel();
        model.clear();
        for(MISAModule module : moduleRepository.getModules().values()) {
            model.addElement(module);
        }
        if(moduleRepository.getModules().size() > 0) {
            misaModuleJList.setSelectedIndex(0);
        }
    }

}
