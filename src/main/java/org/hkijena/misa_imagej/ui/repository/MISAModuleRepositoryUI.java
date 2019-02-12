package org.hkijena.misa_imagej.ui.repository;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import org.hkijena.misa_imagej.MISACommand;
import org.hkijena.misa_imagej.api.repository.MISAModule;
import org.hkijena.misa_imagej.api.repository.MISAModuleRepository;
import org.hkijena.misa_imagej.ui.parametereditor.MISAModuleInstanceUI;
import org.hkijena.misa_imagej.ui.perfanalysis.MISARuntimeLogUI;
import org.hkijena.misa_imagej.ui.pipeliner.MISAPipelinerUI;
import org.hkijena.misa_imagej.utils.GsonUtils;
import org.hkijena.misa_imagej.utils.OSUtils;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.hkijena.misa_imagej.ui.workbench.MISAWorkbenchUI;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * User interface that allows the user to manage and select MISA++ modules
 */
public class MISAModuleRepositoryUI extends JFrame {

    private MISACommand command;
    private static MISAModuleRepositoryUI instance;
    private JList<MISAModule> misaModuleJList;
    private JPanel detailPanel;

    public static MISAModuleRepositoryUI getInstance(MISACommand command) {
        if(instance == null)
            instance = new MISAModuleRepositoryUI(command);
        return instance;
    }

    public static MISAModuleRepositoryUI getInstance() {
        return instance;
    }

    private MISAModuleRepositoryUI(MISACommand command) {
        instance = this;
        this.command = command;
        initialize();
        refreshModuleList();
    }

    private void initialize() {
        setSize(800, 600);
        getContentPane().setLayout(new BorderLayout(8, 8));
        setTitle("MISA++ for ImageJ - Module manager");
        setIconImage(UIUtils.getIconFromResources("misaxx.png").getImage());

        // Toolbar
        JToolBar toolBar = new JToolBar();

        JButton refreshButton = new JButton("Refresh", UIUtils.getIconFromResources("refresh.png"));
        refreshButton.addActionListener(actionEvent -> refreshModuleList());
        toolBar.add(refreshButton);

        JButton addButton = new JButton("Add module ...", UIUtils.getIconFromResources("add.png"));
        addButton.addActionListener(actionEvent -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select the module exectuable");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setMultiSelectionEnabled(false);

            if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                MISAModule module = new MISAModule();
                module.setExecutablePath(fileChooser.getSelectedFile().getAbsolutePath());
                module.setOperatingSystem(OSUtils.detectOperatingSystem());
                module.setOperatingSystemArchitecture(OSUtils.detectArchitecture());
                if(module.getModuleInfo() != null) {
                    Gson gson = GsonUtils.getGson();
                    try(JsonWriter writer = new JsonWriter(new FileWriter(MISAModuleRepository.USER_MODULE_PATH.resolve(module.getGeneratedFileName() + ".json").toString()))) {
                        gson.toJson(module, MISAModule.class, writer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    refreshModuleList();
                }
                else {
                    JOptionPane.showMessageDialog(this, fileChooser.getSelectedFile().getAbsolutePath() + " seems not to be a valid module.\nModule information could not be retrieved.", "Add module", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        toolBar.add(addButton);

        toolBar.add(Box.createHorizontalGlue());

        JButton launcherPipeliner = new JButton("Connect modules together ...", UIUtils.getIconFromResources("connect.png"));
        launcherPipeliner.addActionListener(actionEvent -> {
            MISAPipelinerUI pipelinerUI = new MISAPipelinerUI();
            pipelinerUI.setVisible(true);
        });
        toolBar.add(launcherPipeliner);

        JButton launchAnalyzer = new JButton("Analyze result ...", UIUtils.getIconFromResources("graph.png"));
        launchAnalyzer.addActionListener(actionEvent -> {
            MISAWorkbenchUI workbench = new MISAWorkbenchUI();
            workbench.setVisible(true);
        });
        toolBar.add(launchAnalyzer);

        JButton launchRuntimeLogUI = new JButton(UIUtils.getIconFromResources("clock.png"));
        launchRuntimeLogUI.setToolTipText("Analyze runtime log");
        launchRuntimeLogUI.addActionListener(actionEvent -> {
            MISARuntimeLogUI runtimeLogUI = new MISARuntimeLogUI();
            runtimeLogUI.setVisible(true);
        });
        toolBar.add(launchRuntimeLogUI);

        add(toolBar, BorderLayout.NORTH);

        // Detail panel
        JLabel descriptionTitle;
        JLabel descriptionVersionId;
        JLabel descriptionSourceFile;
        JButton removeModuleButton;
        detailPanel = new JPanel(new GridBagLayout());
        {
            descriptionTitle = UIUtils.createDescriptionLabelUI(detailPanel, "<Title>", 0, 0);
            descriptionTitle.setFont(descriptionTitle.getFont().deriveFont(18f));
            descriptionVersionId = UIUtils.createDescriptionLabelUI(detailPanel, "<VersionID>", 1, 0);
            descriptionSourceFile = UIUtils.createDescriptionLabelUI(detailPanel, "<SourceFile>", 2, 0);
            UIUtils.addFillerGridBagComponent(detailPanel, 3);

            removeModuleButton = new JButton("Remove", UIUtils.getIconFromResources("delete.png"));
            removeModuleButton.addActionListener(actionEvent -> {
                MISAModule selectedModule = misaModuleJList.getSelectedValue();
                if(selectedModule != null) {
                    if(JOptionPane.showConfirmDialog(this, "Do you really want to remove the selected module from this list?",
                            "Remove module", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        try {
                            Files.delete(Paths.get(selectedModule.getLinkPath()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        refreshModuleList();
                    }
                }
            });
            UIUtils.addToGridBag(detailPanel, removeModuleButton, 4, 0);

            JButton launchButton = new JButton("Launch", UIUtils.getIconFromResources("run.png"));
            launchButton.addActionListener(actionEvent -> {
                MISAModuleInstanceUI launcher = new MISAModuleInstanceUI(misaModuleJList.getSelectedValue().instantiate(), false);
                launcher.setVisible(true);
            });
            UIUtils.addToGridBag(detailPanel, launchButton, 5, 0);
        }

        // List of modules
        misaModuleJList = new JList<>(new DefaultListModel<>());
        misaModuleJList.setCellRenderer(new MISAModuleListCellRenderer());
        misaModuleJList.addListSelectionListener(listSelectionEvent -> {
            MISAModule selectedModule = misaModuleJList.getSelectedValue();
            if(selectedModule != null && selectedModule.getModuleInfo() != null) {
                descriptionTitle.setText(selectedModule.getModuleInfo().getDescription());
                descriptionVersionId.setText(selectedModule.getModuleInfo().getName() + " version " + misaModuleJList.getSelectedValue().getModuleInfo().version);
                descriptionSourceFile.setText(selectedModule.getLinkPath());

                File linkLocation = new File(selectedModule.getLinkPath());
                if(linkLocation.getParentFile() != null && linkLocation.getParentFile().canWrite()) {
                    removeModuleButton.setEnabled(true);
                }
                else {
                    removeModuleButton.setEnabled(false);
                }
            }
        });

        // Arrange in split panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, misaModuleJList, detailPanel);
        splitPane.setResizeWeight(1.0);
        add(splitPane, BorderLayout.CENTER);
    }

    private void refreshModuleList() {
        MISAModuleRepository.getInstance().refresh();
        DefaultListModel<MISAModule> model = (DefaultListModel<MISAModule>)misaModuleJList.getModel();
        model.clear();
        for(MISAModule module : MISAModuleRepository.getInstance().getModules()) {
            model.addElement(module);
        }
        if(MISAModuleRepository.getInstance().getModules().size() > 0) {
            detailPanel.setVisible(true);
            misaModuleJList.setSelectedIndex(0);
        }
        else {
            detailPanel.setVisible(false);
        }
    }

    public MISACommand getCommand() {
        return command;
    }
}
