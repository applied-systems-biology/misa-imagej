package org.hkijena.misa_imagej.ui.workbench;

import com.google.common.base.Joiner;
import com.google.common.primitives.Ints;
import org.hkijena.misa_imagej.api.json.JSONSchemaObject;
import org.hkijena.misa_imagej.api.workbench.MISAAttachmentDatabase;
import org.hkijena.misa_imagej.api.workbench.MISAOutput;
import org.hkijena.misa_imagej.api.workbench.table.MISAAttachmentTable;
import org.hkijena.misa_imagej.api.workbench.table.MISAAttachmentTableSampleColumn;
import org.hkijena.misa_imagej.api.workbench.table.MISAAttachmentTableTypeColumn;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.hkijena.misa_imagej.utils.ui.MonochromeColorIcon;

import javax.swing.*;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MISAAttachmentTableBuilderUI extends JPanel {
    private MISAAttachmentDatabase database;
    private List<Integer> databaseIds;
    private MISAAttachmentTableUI tableUI;

    private JComboBox<String> objectSelection;
    private JToggleButton toggleAutosyncFilters;

    public MISAAttachmentTableBuilderUI(MISAAttachmentDatabase database) {
        this.database = database;
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());

        JToolBar toolBar = new JToolBar();

        toggleAutosyncFilters = new JToggleButton(UIUtils.getIconFromResources("cog.png"));
        toggleAutosyncFilters.setSelected(true);
        toggleAutosyncFilters.setToolTipText("Automatically update object browser");
        toolBar.add(toggleAutosyncFilters);

        JButton syncFilters = new JButton("Update", UIUtils.getIconFromResources("refresh.png"));
        syncFilters.addActionListener(e -> updateTable());
        toolBar.add(syncFilters);

        toolBar.add(Box.createHorizontalGlue());

        objectSelection = new JComboBox<>();
        objectSelection.setRenderer(new ObjectTypeComboBoxRenderer(database.getMisaOutput()));
        objectSelection.addItemListener(e -> updateTable());
        toolBar.add(objectSelection);

        add(toolBar, BorderLayout.NORTH);

        tableUI = new MISAAttachmentTableUI();
        add(tableUI, BorderLayout.CENTER);
    }

    private void updateTable() {
        if(objectSelection.getSelectedItem() != null) {
            MISAAttachmentTable attachmentTable = new MISAAttachmentTable(database, databaseIds,
                    objectSelection.getSelectedItem().toString());
            attachmentTable.addColumn(new MISAAttachmentTableSampleColumn());
            attachmentTable.addColumn(new MISAAttachmentTableTypeColumn());
            tableUI.setTable(attachmentTable);
        }
    }

    public void setDatabaseIds(List<Integer> databaseIds) {
        this.databaseIds = databaseIds;
        updateObjectSelection();
        updateTable();
    }

    private void updateObjectSelection() {
        DefaultComboBoxModel<String> objectTypes = new DefaultComboBoxModel<>();
        ResultSet resultSet = database.query("distinct \"serialization-id\"",
                Arrays.asList("id in (" + Joiner.on(',').join(databaseIds) + ")"), "");
        try {
            while(resultSet.next()) {
                objectTypes.addElement(resultSet.getString(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        objectSelection.setModel(objectTypes);
    }

    public static class ObjectTypeComboBoxRenderer extends JLabel implements ListCellRenderer<String> {

        private MISAOutput misaOutput;
        private MonochromeColorIcon icon = new MonochromeColorIcon(UIUtils.getIconFromResources("object-template.png"), Color.WHITE);

        public ObjectTypeComboBoxRenderer(MISAOutput misaOutput) {
            this.misaOutput = misaOutput;
            setIcon(icon);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {

            JSONSchemaObject schemaObject = misaOutput.getAttachmentSchemas().getOrDefault(value, null);
            if(schemaObject == null || schemaObject.getDocumentationTitle() == null || schemaObject.getDocumentationTitle().isEmpty()) {
                setText(value);
                icon.setColor(Color.GRAY);
            }
            else {
                setText(schemaObject.getDocumentationTitle() + " (" + value + ")");
                icon.setColor(schemaObject.toColor());
            }

            if(isSelected) {
                setBackground(new Color(184, 207, 229));
            }
            else {
                setBackground(new Color(255,255,255));
            }

            return this;
        }
    }
}
