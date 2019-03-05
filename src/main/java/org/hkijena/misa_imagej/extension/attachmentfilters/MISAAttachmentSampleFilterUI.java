package org.hkijena.misa_imagej.extension.attachmentfilters;

import org.hkijena.misa_imagej.api.MISASample;
import org.hkijena.misa_imagej.api.workbench.filters.MISAAttachmentFilter;
import org.hkijena.misa_imagej.ui.components.renderers.MISASampleTableCellRender;
import org.hkijena.misa_imagej.ui.workbench.filters.MISAAttachmentFilterUI;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.Vector;

public class MISAAttachmentSampleFilterUI extends MISAAttachmentFilterUI {
    public MISAAttachmentSampleFilterUI(MISAAttachmentFilter filter) {
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
                    return MISASample.class;
                return super.getColumnClass(columnIndex);
            }
        };
//        DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
//        columnModel.addColumn(new TableColumn(0));
//        columnModel.addColumn(new TableColumn(1, 0, new DefaultTableCellRenderer(), new DefaultCellEditor(new JCheckBox())));

        model.setColumnCount(2);
        for(MISASample sample : getFilter().getDatabase().getMisaOutput().getModuleInstance().getSamples().values()) {
            model.addRow(new Object[]{ getNativeFilter().isSampleSelected(sample), sample });
        }

        JTable selectionTable = new JTable(model);
        selectionTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        selectionTable.setDefaultRenderer(MISASample.class, new MISASampleTableCellRender());
        selectionTable.getColumnModel().getColumn(0).setMaxWidth(20);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(selectionTable, BorderLayout.CENTER);

         model.addTableModelListener(e -> {
            if(e.getType() == TableModelEvent.UPDATE && e.getColumn() == 0) {
                for(int i = e.getFirstRow(); i <= e.getLastRow(); ++i) {
                    MISASample sample = (MISASample)model.getValueAt(i, 1);
                    boolean isChecked = (boolean)model.getValueAt(i, 0);
                    if(isChecked != getNativeFilter().isSampleSelected(sample)) {
                        if(isChecked) {
                            getNativeFilter().addSample(sample);
                        }
                        else {
                            getNativeFilter().removeSample(sample);
                        }
                    }
                }
            }
         });
    }

    public MISAAttachmentSampleFilter getNativeFilter() {
        return (MISAAttachmentSampleFilter)getFilter();
    }
}
