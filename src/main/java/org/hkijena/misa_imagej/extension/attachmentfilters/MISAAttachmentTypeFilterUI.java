package org.hkijena.misa_imagej.extension.attachmentfilters;

import org.hkijena.misa_imagej.api.json.JSONSchemaObject;
import org.hkijena.misa_imagej.api.workbench.filters.MISAAttachmentFilter;
import org.hkijena.misa_imagej.ui.workbench.MISAAttachmentFilterUI;
import org.hkijena.misa_imagej.utils.UIUtils;
import org.hkijena.misa_imagej.utils.ui.MonochromeColorIcon;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class MISAAttachmentTypeFilterUI extends MISAAttachmentFilterUI {
    public MISAAttachmentTypeFilterUI(MISAAttachmentFilter filter) {
        super(filter);
        initialize();
    }

    private void initialize() {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if(columnIndex == 0)
                    return Boolean.class;
                else if (columnIndex == 1)
                    return JSONSchemaObject.class;
                return super.getColumnClass(columnIndex);
            }
        };

        model.setColumnCount(2);
        for(JSONSchemaObject schema : getFilter().getDatabase().getMisaOutput().getAttachmentSchemas().values()) {
            model.addRow(new Object[]{ getNativeFilter().getSerializationIds().contains(schema.getSerializationId()), schema });
        }

        JTable selectionTable = new JTable(model);
        selectionTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        selectionTable.setDefaultRenderer(JSONSchemaObject.class, new SerializationSchemaCellRenderer(this));
        selectionTable.getColumnModel().getColumn(0).setMaxWidth(20);
        selectionTable.setShowGrid(false);
        selectionTable.setOpaque(false);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(selectionTable, BorderLayout.CENTER);

        model.addTableModelListener(e -> {
            if(e.getType() == TableModelEvent.UPDATE && e.getColumn() == 0) {
                for(int i = e.getFirstRow(); i <= e.getLastRow(); ++i) {
                    JSONSchemaObject cache = (JSONSchemaObject) model.getValueAt(i, 1);
                    boolean isChecked = (boolean)model.getValueAt(i, 0);
                    if(isChecked != getNativeFilter().getSerializationIds().contains(cache.getSerializationId())) {
                        if(isChecked) {
                            getNativeFilter().addSerializationId(cache.getSerializationId());
                        }
                        else {
                            getNativeFilter().removeSerializationId(cache.getSerializationId());
                        }
                    }
                }
            }
        });
    }

    private MISAAttachmentTypeFilter getNativeFilter() {
        return (MISAAttachmentTypeFilter)getFilter();
    }

    public static class SerializationSchemaCellRenderer extends JLabel implements TableCellRenderer {

        private MonochromeColorIcon icon = new MonochromeColorIcon(UIUtils.getIconFromResources("object-template.png"), Color.WHITE);
        private MISAAttachmentTypeFilterUI ui;

        public SerializationSchemaCellRenderer(MISAAttachmentTypeFilterUI ui) {
            this.ui = ui;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if(value instanceof JSONSchemaObject) {
                JSONSchemaObject schema = (JSONSchemaObject)value;
                String schemaName = schema.getName();
                if(schema.getDocumentationTitle() != null && !schema.getDocumentationTitle().isEmpty())
                    schemaName = schema.getDocumentationTitle();
                setText(schemaName);
                icon.setColor(schema.toColor());
                setIcon(icon);

                if(isSelected || hasFocus) {
                    setBackground(new Color(184, 207, 229));
                }
                else {
                    setBackground(new Color(255,255,255));
                }

                StringBuilder tooltipBuilder = new StringBuilder();
                tooltipBuilder.append("<html>");
                if(schema.getDocumentationTitle() != null && !schema.getDocumentationTitle().isEmpty())  {
                    tooltipBuilder.append("<i>").append(schema.getSerializationId()).append("</i><br/><br/>");
                }
                if(schema.getDocumentationDescription() != null && !schema.getDocumentationDescription().isEmpty()) {
                    tooltipBuilder.append(schema.getDocumentationDescription()).append("<br/><br/>");
                }
                tooltipBuilder.append("<b>Inherits from:</b><br/>");
                for(String id : schema.getSerializationHierarchy()) {
                    String inheritedSchemaName = id;
                    if(ui.getFilter().getDatabase().getMisaOutput().getAttachmentSchemas().containsKey(id)) {
                        JSONSchemaObject inheritedSchema = ui.getFilter().getDatabase().getMisaOutput().getAttachmentSchemas().get(id);
                        if(inheritedSchema.getDocumentationTitle() != null && !inheritedSchema.getDocumentationTitle().isEmpty()) {
                            inheritedSchemaName = inheritedSchema.getDocumentationTitle() + " <i>(" + inheritedSchemaName + ")</i>";
                        }
                    }
                    tooltipBuilder.append(inheritedSchemaName).append("<br/>");
                }
                tooltipBuilder.append("</html>");
                setToolTipText(tooltipBuilder.toString());
            }
            else {
                setText(null);
                setIcon(null);
                setToolTipText(null);
            }
            return this;
        }
    }
}
