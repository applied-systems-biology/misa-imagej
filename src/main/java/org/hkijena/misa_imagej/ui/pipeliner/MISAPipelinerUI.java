package org.hkijena.misa_imagej.ui.pipeliner;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.MISAModuleInstance;
import org.hkijena.misa_imagej.api.pipelining.MISAPipeline;
import org.hkijena.misa_imagej.api.repository.MISAModule;
import org.hkijena.misa_imagej.api.repository.MISAModuleRepository;
import org.hkijena.misa_imagej.ui.parametereditor.MISACacheTreeUI;
import org.hkijena.misa_imagej.ui.repository.MISAModuleListCellRenderer;
import org.hkijena.misa_imagej.utils.GsonUtils;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.jdesktop.swingx.JXTaskPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public class MISAPipelinerUI extends JFrame {

    private MISAPipeline pipeline = new MISAPipeline();
    private MISAModuleRepository repository;
    private JList<MISAModule> moduleList;
    private MISACacheTreeUI cacheTree;
    private MISAPipelineUI pipelineEditor;

    /**
     * We use this to give users an easy overview of a module
     */
    private Map<MISAModule, MISAModuleInstance> uiParameterSchemata = new HashMap<>();

    public MISAPipelinerUI(MISAModuleRepository repository)
    {
        this.repository = repository;
        initialize();
        refresh();
    }

    private void initialize() {
        setSize(800, 600);
        getContentPane().setLayout(new BorderLayout());
        setTitle("MISA++ for ImageJ - Pipeline tool");
        setIconImage(UIUtils.getIconFromResources("misaxx.png").getImage());

        JToolBar toolBar = new JToolBar();

        JButton openButton = new JButton("Open", UIUtils.getIconFromResources("open.png"));
        openButton.addActionListener(actionEvent -> open());
        toolBar.add(openButton);

        JButton saveButton = new JButton("Save", UIUtils.getIconFromResources("save.png"));
        saveButton.addActionListener(actionEvent -> save());
        toolBar.add(saveButton);

        toolBar.add(Box.createHorizontalGlue());

        JButton refreshButton = new JButton("Refresh", UIUtils.getIconFromResources("refresh.png"));
        refreshButton.addActionListener(actionEvent -> refresh());;
        toolBar.add(refreshButton);

        add(toolBar, BorderLayout.NORTH);
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.addTab("Available modules", createToolbox());

        pipelineEditor = new MISAPipelineUI();
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(pipelineEditor) {
            {
                setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            }
        }, tabbedPane);
        splitPane.setResizeWeight(1);
        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createToolbox() {
        JPanel  toolboxPanel = new JPanel(new BorderLayout());
        moduleList = new JList<>();
        moduleList.setCellRenderer(new MISAModuleListCellRenderer());
        moduleList.addListSelectionListener(listSelectionEvent -> updateCacheTree());
        moduleList.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if(mouseEvent.getClickCount() == 2) {
                    addInstance();
                }
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {}

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {}

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {}

            @Override
            public void mouseExited(MouseEvent mouseEvent) {}
        });

        cacheTree = new MISACacheTreeUI() {
            @Override
            protected Entry createRootEntry() {
                Entry result = super.createRootEntry();
                result.name = "Input and output preview";
                return result;
            }

            @Override
            protected Entry createEntry(MISACache cache) {
                Entry result = super.createEntry(cache);
                result.name = cache.getCacheTypeName() + ": " + result.name;
                return result;
            }
        };

        toolboxPanel.add(new JSplitPane(JSplitPane.VERTICAL_SPLIT, moduleList, cacheTree), BorderLayout.CENTER);

        JToolBar toolboxToolbar = new JToolBar();

        JButton instantiateButton = new JButton("Add to pipeline", UIUtils.getIconFromResources("add.png"));
        instantiateButton.addActionListener(actionEvent -> addInstance());
        toolboxToolbar.add(instantiateButton);

        toolboxPanel.add(toolboxToolbar, BorderLayout.SOUTH);


        return toolboxPanel;
    }

    private void open() {

    }

    private void save() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if(fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Gson gson = GsonUtils.getGson();
                String json = gson.toJson(pipeline);
                Files.write(fileChooser.getSelectedFile().toPath(), json.getBytes(Charsets.UTF_8));
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void refresh() {
        // Refresh the list of available modules
        uiParameterSchemata.clear();
        repository.refresh();
        DefaultListModel<MISAModule> model = new DefaultListModel<>();
        for(MISAModule module : repository.getModules()) {
            model.addElement(module);
            MISAModuleInstance instance = module.instantiate();
            instance.addSample("Preview");
            uiParameterSchemata.put(module, instance);
        }
        moduleList.setModel(model);

        // Update the editor UI
        pipelineEditor.setPipeline(pipeline);
    }

    private void addInstance() {
        if(moduleList.getSelectedValue() != null) {
            pipeline.addNode(moduleList.getSelectedValue());
        }
    }

    private void updateCacheTree() {
        if(moduleList.getSelectedValue() != null) {
            cacheTree.setSample(uiParameterSchemata.get(moduleList.getSelectedValue()).getCurrentSample());
        }
    }

}
