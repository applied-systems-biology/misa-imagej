package org.hkijena.misa_imagej.parametereditor.json_schema;

import org.hkijena.misa_imagej.utils.UIUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;

/**
 * Editor widget for a JSONSchema. Has a node tree on the left side.
 */
public class JSONSchemaEditorUI extends JPanel {

    private JTree jsonTree;
    private JPanel objectEditor;
    private JComponent topPanel;
    private JSONSchemaObject currentObject = null;

    private int objectEditorRows = 0;

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
        objectEditor.removeAll();
        objectEditorRows = 0;
        objectEditor.revalidate();

        if(obj != null) {
            JSONSchemaEditorRegistry.getEditorFor(obj).populate(this);
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
            jsonTree.setCellRenderer(new JSONSchemaObjectTreeCellRenderer());
            jsonTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            treePanel.add(jsonTree, BorderLayout.CENTER);
        }

        objectEditor = new JPanel(new GridBagLayout());
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treePanel, new JScrollPane(objectEditor));
        splitPane.setResizeWeight(0);
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

    private class JSONSchemaObjectTreeCellRenderer extends JLabel implements TreeCellRenderer {
        public JSONSchemaObjectTreeCellRenderer() {
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        }

        @Override
        public Component getTreeCellRendererComponent(JTree jTree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

            if(jTree.getFont() != null) {
                setFont(jTree.getFont());
            }

            Object o = ((DefaultMutableTreeNode)value).getUserObject();
            if(o instanceof JSONSchemaObject) {
                setText(o.toString());
                JSONSchemaObject entry = (JSONSchemaObject)o;
                if("object".equals(entry.type)) {
                    setIcon(UIUtils.getIconFromResources("group.png"));
                }
                else if("string".equals(entry.type)) {
                    setIcon(UIUtils.getIconFromResources("text.png"));
                }
                else if("number".equals(entry.type)) {
                    setIcon(UIUtils.getIconFromResources("pi.png"));
                }
                else if("boolean".equals(entry.type)) {
                    setIcon(UIUtils.getIconFromResources("checkbox.png"));
                }
            }
            else {
                setText(o.toString());
                setIcon(null);
            }

            // Update status
            // Update status
            if(selected) {
                setBackground(new Color(184, 207, 229));
            }
            else {
                setBackground(new Color(255,255,255));
            }

            return this;
        }
    }

}
