package org.hkijena.misa_imagej.json_schema;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;

/**
 * Editor widget for a JSONSchema. Has a node tree on the left side.
 */
public class JSONSchemaEditor extends JPanel {

    private JTree jsonTree;
    private JPanel objectEditor;
    private JSONSchemaObjectEditor objectEditorInstance = null;
    private JSONSchemaObject currentObject = null;

    public JSONSchemaEditor() {
        initialize();
    }

    private void editJSONSchema(JSONSchemaObject obj) {
        currentObject = obj;
        if(objectEditorInstance != null)
            objectEditor.remove(objectEditorInstance);
        objectEditorInstance = new JSONSchemaObjectEditor(obj);
        objectEditor.add(objectEditorInstance, BorderLayout.CENTER);
        objectEditor.revalidate();
    }

    private void initialize() {
        setLayout(new BorderLayout());
        jsonTree = new JTree();
        jsonTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        objectEditor = new JPanel(new BorderLayout());
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, jsonTree, new JScrollPane(objectEditor));
        add(splitPane, BorderLayout.CENTER);

        jsonTree.addTreeSelectionListener(e -> {
            if(jsonTree.getLastSelectedPathComponent() != null) {
                DefaultMutableTreeNode nd = (DefaultMutableTreeNode)jsonTree.getLastSelectedPathComponent();
                editJSONSchema((JSONSchemaObject)nd.getUserObject());
            }
        });

    }

    public void setSchema(JSONSchemaObject jsonSchema) {
        jsonTree.setModel(new DefaultTreeModel(jsonSchema.toTreeNode()));
        editJSONSchema(jsonSchema);
        jsonSchema.addPropertyChangeListener(propertyChangeEvent -> {
            if(jsonSchema == currentObject) {
                jsonTree.setModel(new DefaultTreeModel(jsonSchema.toTreeNode()));
                refreshEditor();
            }
        });
    }

    public void refreshEditor() {
        if(currentObject != null) {
            setSchema(currentObject);
        }
    }

}
