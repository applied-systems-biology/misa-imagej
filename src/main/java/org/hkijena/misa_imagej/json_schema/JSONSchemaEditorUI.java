package org.hkijena.misa_imagej.json_schema;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;

/**
 * Editor widget for a JSONSchema. Has a node tree on the left side.
 */
public class JSONSchemaEditorUI extends JPanel {

    private JTree jsonTree;
    private JPanel objectEditor;
    private JComponent topPanel;
    private JSONSchemaObjectEditorUI objectEditorInstance = null;
    private JSONSchemaObject currentObject = null;

    /**
     * Creates a JSON schema editor with a panel on top of the tree
     * @param topPanel if null, no panel will be created
     */
    public JSONSchemaEditorUI(JComponent topPanel) {
        this.topPanel = topPanel;
        initialize();
        setSchema(null);
    }

    public JSONSchemaEditorUI() {
        this(null);
    }

    private void setCurrentSchema(JSONSchemaObject obj) {
        currentObject = obj;
        if(objectEditorInstance != null)
            objectEditor.remove(objectEditorInstance);
        if(obj != null) {
            objectEditorInstance = JSONSchemaEditorRegistry.getEditorFor(obj);
            objectEditor.add(objectEditorInstance, BorderLayout.CENTER);
            objectEditor.revalidate();
        }
        else {
            objectEditorInstance = null;
            objectEditor.revalidate();
        }
    }

    private void initialize() {
        setLayout(new BorderLayout());

        JPanel treePanel = new JPanel(new BorderLayout(8, 8));
        {
            // If enabled, add panel
            if(topPanel != null) {
                treePanel.add(topPanel, BorderLayout.NORTH);
            }

            // Create tree
            jsonTree = new JTree();
            jsonTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            treePanel.add(jsonTree, BorderLayout.CENTER);
        }

        objectEditor = new JPanel(new BorderLayout());
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treePanel, new JScrollPane(objectEditor));
        add(splitPane, BorderLayout.CENTER);

        jsonTree.addTreeSelectionListener(e -> {
            if(jsonTree.getLastSelectedPathComponent() != null) {
                DefaultMutableTreeNode nd = (DefaultMutableTreeNode)jsonTree.getLastSelectedPathComponent();
                setCurrentSchema((JSONSchemaObject)nd.getUserObject());
            }
        });

    }

    public void setSchema(JSONSchemaObject jsonSchema) {
        if(jsonSchema != null && jsonSchema.properties != null && jsonSchema.properties.size() > 0) {
            jsonTree.setModel(new DefaultTreeModel(jsonSchema.toTreeNode()));
            setCurrentSchema(jsonSchema);
            jsonSchema.addPropertyChangeListener(propertyChangeEvent -> {
                if(jsonSchema == currentObject) {
                    jsonTree.setModel(new DefaultTreeModel(jsonSchema.toTreeNode()));
                    refreshEditor();
                }
            });

            jsonTree.setEnabled(true);
            objectEditor.setEnabled(true);
        }
        else {
            jsonTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("No properties to edit")));
            setCurrentSchema(null);
            jsonTree.setEnabled(false);
            objectEditor.setEnabled(false);
        }
    }

    public void refreshEditor() {
        if(currentObject != null) {
            setSchema(currentObject);
        }
    }

}
