package org.hkijena.misa_imagej.extension.attachmentfilters;

import org.hkijena.misa_imagej.api.MISASample;
import org.hkijena.misa_imagej.api.workbench.filters.MISAAttachmentFilter;
import org.hkijena.misa_imagej.ui.components.renderers.MISASampleTableCellRender;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class MISAAttachmentSampleFilterUI extends DefaultTabularMISAAttachmentFilterUI {

    public MISAAttachmentSampleFilterUI(MISAAttachmentFilter filter) {
        super(filter);
        initialize();
    }

    private void initialize() {
        TableModel model = getSelectionTable().getModel();
        model.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 0) {
                for (int i = e.getFirstRow(); i <= e.getLastRow(); ++i) {
                    MISASample sample = (MISASample) model.getValueAt(i, 1);
                    boolean isChecked = (boolean) model.getValueAt(i, 0);
                    if (isChecked != getNativeFilter().getSamples().contains(sample)) {
                        if (isChecked) {
                            getNativeFilter().addSample(sample);
                        } else {
                            getNativeFilter().removeSample(sample);
                        }
                    }
                }
            }
        });

        getSelectionTable().setDefaultRenderer(MISASample.class, new MISASampleTableCellRender());
    }

    @Override
    protected Class getTableContentClass() {
        return MISASample.class;
    }

    @Override
    protected void initializeTableModel(DefaultTableModel model) {
        for (MISASample sample : getFilter().getDatabase().getMisaOutput().getModuleInstance().getSamples().values()) {
            model.addRow(new Object[]{getNativeFilter().getSamples().contains(sample), sample});
        }
    }

    public MISAAttachmentSampleFilter getNativeFilter() {
        return (MISAAttachmentSampleFilter) getFilter();
    }
}
