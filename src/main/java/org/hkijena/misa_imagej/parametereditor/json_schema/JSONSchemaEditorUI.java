package org.hkijena.misa_imagej.parametereditor.json_schema;

import org.hkijena.misa_imagej.utils.UIUtils;
import org.jdesktop.swingx.JXTextField;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;

import static org.hkijena.misa_imagej.utils.UIUtils.UI_PADDING;

/**
 * Editor widget for a JSONSchema. Has a node tree on the left side.
 */
public class JSONSchemaEditorUI extends JPanel {

    private JTree jsonTree;
    private JPanel objectEditor;
    private JComponent topPanel;
    private JSONSchemaObject currentObject = null;

    private int objectEditorRows = 0;
    private JSONSchemaObject lastObjectEditorSchemaParent = null;

    private JToggleButton enableObjectsButton;
    private JToggleButton showAllObjects;
    private JXTextField objectFilter;

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
        updateEditor();
    }

    private void updateEditor() {
        objectEditor.removeAll();
        objectEditorRows = 0;
        lastObjectEditorSchemaParent = null;
        objectEditor.revalidate();
        objectEditor.repaint();

        if(currentObject != null) {
            JSONSchemaEditorRegistry.getEditorFor(currentObject).populate(this);
            objectEditor.add(new JPanel(), new GridBagConstraints() {
                {
                    anchor = GridBagConstraints.PAGE_START;
                    gridx = 2;
                    gridy = objectEditorRows++;
                    fill = GridBagConstraints.HORIZONTAL | GridBagConstraints.VERTICAL;
                    weightx = 0;
                    weighty = 1;
                }
            });
        }
    }

    /**
     * Inserts an object editor UI into this schema editor
     * @param ui
     */
    public void insertObjectEditorUI(JSONSchemaObjectEditorUI ui, boolean withLabel) {
        // Filtering
        if(!enableObjectsButton.isSelected() && ui.getJsonSchemaObject().parent != currentObject)
            return;
        if(objectFilter.getText() != null && !objectFilter.getText().isEmpty()) {
            String searchText = ui.getJsonSchemaObject().getName().toLowerCase();
            if(!searchText.contains(objectFilter.getText().toLowerCase())) {
                return;
            }
        }

        JSONSchemaObject parent = ui.getJsonSchemaObject().parent;

        if(parent != null && parent != lastObjectEditorSchemaParent && parent.getDepth() >= getCurrentObject().getDepth()) {
            String parentPath = getCurrentObject().id + parent.getValuePath().substring(getCurrentObject().getValuePath().length());

            // Announce the object
            final boolean first = lastObjectEditorSchemaParent == null;
            lastObjectEditorSchemaParent = parent;
            JLabel description = new JLabel(parentPath);
            description.setIcon(parent.type.getIcon());
            description.setFont(description.getFont().deriveFont(14.0f));
            objectEditor.add(description, new GridBagConstraints() {
                {
                    anchor = GridBagConstraints.WEST;
                    gridx = 0;
                    gridy = objectEditorRows++;
                    gridwidth = 2;
                    weightx = 0;
                    insets = new Insets(first ? 8 : 24,4,8,4);
                }
            });
        }
        if(withLabel) {
            JLabel description = new JLabel(ui.getJsonSchemaObject().getName());
            description.setIcon(ui.getJsonSchemaObject().type.getIcon());
            objectEditor.add(description, new GridBagConstraints() {
                {
                    anchor = GridBagConstraints.WEST;
                    gridx = 0;
                    gridy = objectEditorRows;
                    weightx = 0;
                    insets = UI_PADDING;
                }
            });
        }
        objectEditor.add(ui, new GridBagConstraints() {
            {
                anchor = GridBagConstraints.WEST;
                gridx = 1;
                gridy = objectEditorRows;
                insets = UI_PADDING;
                weightx = 1;
                fill = GridBagConstraints.HORIZONTAL;
            }
        });
        ++objectEditorRows;
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
            jsonTree.setMinimumSize(new Dimension(128, 0));
            jsonTree.setCellRenderer(new JSONSchemaObjectTreeCellRenderer());
            jsonTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            treePanel.add(jsonTree, BorderLayout.CENTER);
        }

        JPanel editPanel = new JPanel(new BorderLayout());
        {
            // Create a toolbar with view options
            JToolBar toolBar = new JToolBar();

            enableObjectsButton = new JToggleButton("Objects", UIUtils.getIconFromResources("group.png"), true);
            enableObjectsButton.setToolTipText("If enabled, object parameters are shown in the editor.");
            enableObjectsButton.addActionListener(actionEvent -> updateEditor());
            toolBar.add(enableObjectsButton);

            showAllObjects = new JToggleButton("Whole tree", UIUtils.getIconFromResources("tree.png"), false);
            showAllObjects.setToolTipText("If enabled, the whole subtree of settings is shown.");
            showAllObjects.addActionListener(actionEvent -> updateEditor());
            toolBar.add(showAllObjects);

            toolBar.add(Box.createHorizontalGlue());

            objectFilter = new JXTextField("Filter ...");
            objectFilter.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent documentEvent) {
                    refreshEditor();
                }

                @Override
                public void removeUpdate(DocumentEvent documentEvent) {
                    refreshEditor();
                }

                @Override
                public void changedUpdate(DocumentEvent documentEvent) {
                    refreshEditor();
                }
            });
            toolBar.add(objectFilter);

            JButton clearFilterButton = new JButton(UIUtils.getIconFromResources("clear.png"));
            clearFilterButton.addActionListener(actionEvent -> objectFilter.setText(""));
            toolBar.add(clearFilterButton);

            editPanel.add(toolBar, BorderLayout.NORTH);

            // Add the scroll layout here
            objectEditor = new JPanel(new GridBagLayout());
            editPanel.add(new JScrollPane(objectEditor), BorderLayout.CENTER);
        }

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treePanel, editPanel);
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
                if(jsonSchema == getCurrentObject()) {
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
        if(getCurrentObject() != null) {
            setSchema(getCurrentObject());
        }
    }

    /**
     * Returns the currently selected object
     * @return
     */
    public JSONSchemaObject getCurrentObject() {
        return currentObject;
    }

    /**
     * If true, the populated objects should be limited
     * @return
     */
    public boolean getObjectLimitEnabled() {
        return !showAllObjects.isSelected();
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
                setIcon(entry.type.getIcon());
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
