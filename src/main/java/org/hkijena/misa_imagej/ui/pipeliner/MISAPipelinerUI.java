package org.hkijena.misa_imagej.ui.pipeliner;

import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.MISAModuleInstance;
import org.hkijena.misa_imagej.api.pipelining.MISAPipeline;
import org.hkijena.misa_imagej.api.repository.MISAModule;
import org.hkijena.misa_imagej.api.repository.MISAModuleRepository;
import org.hkijena.misa_imagej.ui.parametereditor.CacheListTree;
import org.hkijena.misa_imagej.ui.repository.MISAModuleListCellRenderer;
import org.hkijena.misa_imagej.utils.UIUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MISAPipelinerUI extends JFrame {

    private MISAPipeline pipeline = new MISAPipeline();
    private MISAModuleRepository repository;
    private JList<MISAModule> moduleList;
    private CacheListTree cacheTree;
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

        JPanel toolboxPanel = createToolbox();
        pipelineEditor = new MISAPipelineUI();
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(pipelineEditor) {
            {
                setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            }
        }, toolboxPanel);
        splitPane.setResizeWeight(1);
        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createToolbox() {
        JPanel  toolboxPanel = new JPanel(new BorderLayout());
        moduleList = new JList<>();
        moduleList.setCellRenderer(new MISAModuleListCellRenderer());
        moduleList.addListSelectionListener(listSelectionEvent -> updateCacheTree());

        cacheTree = new CacheListTree() {
            @Override
            protected Entry createRootEntry() {
                Entry result = super.createRootEntry();
                result.name = "Input and output";
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
        toolboxToolbar.add(instantiateButton);

        toolboxPanel.add(toolboxToolbar, BorderLayout.SOUTH);


        return toolboxPanel;
    }

    private void open() {

    }

    private void save() {

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

    private void updateCacheTree() {
        if(moduleList.getSelectedValue() != null) {
            cacheTree.setSample(uiParameterSchemata.get(moduleList.getSelectedValue()).getCurrentSample());
        }
    }

}
