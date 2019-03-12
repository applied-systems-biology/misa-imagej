package org.hkijena.misa_imagej.ui.workbench.tablebuilder;

import org.hkijena.misa_imagej.api.json.JSONSchemaObject;
import org.hkijena.misa_imagej.api.workbench.table.*;
import org.hkijena.misa_imagej.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MISAAttachmentTableColumnEditor extends JDialog {

    private MISAAttachmentTable table;
    private List<JToggleButton> columnToggles = new ArrayList<>();

    public MISAAttachmentTableColumnEditor(MISAAttachmentTable table) {
        this.table = table;
        initialize();
    }

    private void initialize() {
        setTitle("Edit columns");
        setIconImage(UIUtils.getIconFromResources("misaxx.png").getImage());
        setLayout(new BorderLayout());

        JToolBar toolBar = new JToolBar();

        JButton selectAllButton = new JButton("Select all", UIUtils.getIconFromResources("select-all.png"));
        selectAllButton.addActionListener(e -> {
            for(JToggleButton button : columnToggles) {
                button.setSelected(true);
            }
        });
        toolBar.add(selectAllButton);

        JButton selectNoneButton = new JButton("Clear selection", UIUtils.getIconFromResources("clear-brush.png"));
        selectNoneButton.addActionListener(e -> {
            for(JToggleButton button : columnToggles) {
                button.setSelected(false);
            }
        });
        toolBar.add(selectNoneButton);

        JButton invertSelectionButton = new JButton("Invert selection", UIUtils.getIconFromResources("invert.png"));
        invertSelectionButton.addActionListener(e -> {
            for(JToggleButton button : columnToggles) {
                button.setSelected(!button.isSelected());
            }
        });
        toolBar.add(invertSelectionButton);

        add(toolBar, BorderLayout.NORTH);

        add(new JScrollPane(initializeColumnsPanel()), BorderLayout.CENTER);
    }

    private void insertColumnUI(JPanel columnPanel, String name, Icon icon,
                                Class<? extends MISAAttachmentTableColumn> columnClass,
                                Predicate<MISAAttachmentTableColumn> existsCheck) {
        final int row = columnPanel.getComponentCount();
        JToggleButton toggleButton = createAddRemoveButton(aVoid -> {
            try {
               return columnClass.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e1) {
                throw new RuntimeException(e1);
            }
        }, existsCheck);
        columnPanel.add(toggleButton, new GridBagConstraints() {
            {
                gridx = 0;
                gridy = row;
                insets = UIUtils.UI_PADDING;
                anchor = GridBagConstraints.NORTHWEST;
            }
        });

        JLabel label = new JLabel(name, icon, JLabel.LEFT);
        columnPanel.add(label, new GridBagConstraints() {
            {
                gridx = 1;
                gridy = row;
                fill = GridBagConstraints.HORIZONTAL;
                weightx = 1;
                insets = UIUtils.UI_PADDING;
                anchor = GridBagConstraints.WEST;
            }
        });
    }

    private void insertColumnUIForProperty(JPanel columnPanel, String name, Icon icon, String property) {
        final int row = columnPanel.getComponentCount();
        JToggleButton toggleButton = createAddRemoveButton(aVoid -> new MISAAttachmentTableJsonValueColumn(property), e ->
                (e instanceof MISAAttachmentTableJsonValueColumn) && ((MISAAttachmentTableJsonValueColumn) e).getPropertyName().equals(property));
        columnPanel.add(toggleButton, new GridBagConstraints() {
            {
                gridx = 0;
                gridy = row;
                insets = UIUtils.UI_PADDING;
                anchor = GridBagConstraints.NORTHWEST;
            }
        });

        JLabel label = new JLabel(name, icon, JLabel.LEFT);
        columnPanel.add(label, new GridBagConstraints() {
            {
                gridx = 1;
                gridy = row;
                fill = GridBagConstraints.HORIZONTAL;
                weightx = 1;
                insets = UIUtils.UI_PADDING;
                anchor = GridBagConstraints.WEST;
            }
        });
    }

    private JToggleButton createAddRemoveButton(Function<Void, MISAAttachmentTableColumn> instantiator, Predicate<MISAAttachmentTableColumn> existsCheck) {
        JToggleButton button = new JToggleButton(UIUtils.getIconFromResources("table.png"));
        button.setToolTipText("Add as column");
        button.setSelected(columnsExists(existsCheck));
        button.addChangeListener(e -> {
            if(button.isSelected()) {
                if(table.getColumns().stream().noneMatch(existsCheck))
                    table.addColumn(instantiator.apply(null));
            }
            else {
                for(MISAAttachmentTableColumn column : table.getColumns().stream().filter(existsCheck).collect(Collectors.toList())) {
                    table.removeColumn(column);
                }
            }
        });
        columnToggles.add(button);
        return button;
    }

    private boolean columnsExists(Predicate<MISAAttachmentTableColumn> existsCheck) {
        return table.getColumns().stream().anyMatch(existsCheck);
    }

    private JPanel initializeColumnsPanel() {
        JPanel columnPanel = new JPanel(new GridBagLayout());

        insertColumnUI(columnPanel,
                "Sample",
                UIUtils.getIconFromResources("sample.png"),
                MISAAttachmentTableSampleColumn.class,
                misaAttachmentTableColumn -> misaAttachmentTableColumn instanceof MISAAttachmentTableSampleColumn);
        insertColumnUI(columnPanel,
                "Full data",
                UIUtils.getIconFromResources("database.png"),
                MISAAttachmentTableCacheAndSubCacheColumn.class,
                misaAttachmentTableColumn -> misaAttachmentTableColumn instanceof MISAAttachmentTableCacheAndSubCacheColumn);
        insertColumnUI(columnPanel,
                "Data",
                UIUtils.getIconFromResources("database.png"),
                MISAAttachmentTableCacheColumn.class,
                misaAttachmentTableColumn -> misaAttachmentTableColumn instanceof MISAAttachmentTableCacheColumn);
        insertColumnUI(columnPanel,
                "Sub-data",
                UIUtils.getIconFromResources("open.png"),
                MISAAttachmentTableSubCacheColumn.class,
                misaAttachmentTableColumn -> misaAttachmentTableColumn instanceof MISAAttachmentTableSubCacheColumn);
        insertColumnUI(columnPanel,
                "Property",
                UIUtils.getIconFromResources("object.png"),
                MISAAttachmentTablePropertyColumn.class,
                misaAttachmentTableColumn -> misaAttachmentTableColumn instanceof MISAAttachmentTablePropertyColumn);
        insertColumnUI(columnPanel,
                "Object type",
                UIUtils.getIconFromResources("object.png"),
                MISAAttachmentTableTypeColumn.class,
                misaAttachmentTableColumn -> misaAttachmentTableColumn instanceof MISAAttachmentTableTypeColumn);

        columnPanel.add(new JSeparator(), new GridBagConstraints() {
            {
                gridx = 0;
                gridy = columnPanel.getComponentCount();
                gridwidth = 2;
                fill = GridBagConstraints.HORIZONTAL;
                weightx = 1;
            }
        });

        // Create for all properties in schema
        {
            JSONSchemaObject schema = table.getDatabase().getMisaOutput().getAttachmentSchemas().getOrDefault(
                    table.getSerializationId(), null);
            if (schema != null) {
                Stack<JSONSchemaObject> stack = new Stack<>();
                Stack<String> paths = new Stack<>();
                stack.push(schema);
                paths.push("");

                while (!stack.isEmpty()) {
                    JSONSchemaObject object = stack.pop();
                    String path = paths.pop();
                    switch (object.getType()) {
                        case jsonString:
                            insertColumnUIForProperty(columnPanel,
                                    path,
                                    UIUtils.getIconFromResources("text.png"),
                                    path);
                            break;
                        case jsonNumber:
                            insertColumnUIForProperty(columnPanel,
                                    path,
                                    UIUtils.getIconFromResources("number.png"),
                                    path);
                            break;
                        case jsonBoolean:
                            insertColumnUIForProperty(columnPanel,
                                    path,
                                    UIUtils.getIconFromResources("checkbox.png"),
                                    path);
                            break;
                        case jsonObject:
                            for (Map.Entry<String, JSONSchemaObject> entry : object.getProperties().entrySet()) {
                                stack.push(entry.getValue());
                                paths.push(path + "/" + entry.getKey());
                            }
                            break;
                    }
                }
            }

            UIUtils.addFillerGridBagComponent(columnPanel, columnPanel.getComponentCount(), 1);
        }

        return columnPanel;
    }

}
