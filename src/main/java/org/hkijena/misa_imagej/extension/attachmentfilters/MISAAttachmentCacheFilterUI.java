package org.hkijena.misa_imagej.extension.attachmentfilters;

import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.MISACacheIOType;
import org.hkijena.misa_imagej.api.MISASample;
import org.hkijena.misa_imagej.api.workbench.filters.MISAAttachmentFilter;
import org.hkijena.misa_imagej.ui.workbench.MISAAttachmentFilterUI;
import org.hkijena.misa_imagej.utils.ui.ColorIcon;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class MISAAttachmentCacheFilterUI extends MISAAttachmentFilterUI {
    public MISAAttachmentCacheFilterUI(MISAAttachmentFilter filter) {
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
                    return MISACache.class;
                return super.getColumnClass(columnIndex);
            }
        };

        model.setColumnCount(2);
        MISASample sample = getFilter().getDatabase().getMisaOutput().getModuleInstance().getSamples().values().stream().findFirst().get();
        for(MISACache cache : sample.getImportedCaches()) {
            String cacheName = "imported/" + cache.getRelativePath();
            model.addRow(new Object[]{ getNativeFilter().getCaches().contains(cacheName), cache });
        }
        for(MISACache cache : sample.getExportedCaches()) {
            String cacheName = "exported/" + cache.getRelativePath();
            model.addRow(new Object[]{ getNativeFilter().getCaches().contains(cacheName), cache });
        }

        JTable selectionTable = new JTable(model);
        selectionTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        selectionTable.setDefaultRenderer(MISACache.class, new CacheCellRenderer());
        selectionTable.getColumnModel().getColumn(0).setMaxWidth(20);
        selectionTable.setShowGrid(false);
        selectionTable.setOpaque(false);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(selectionTable, BorderLayout.CENTER);

        model.addTableModelListener(e -> {
            if(e.getType() == TableModelEvent.UPDATE && e.getColumn() == 0) {
                for(int i = e.getFirstRow(); i <= e.getLastRow(); ++i) {
                    MISACache cache = (MISACache) model.getValueAt(i, 1);
                    String cacheName;
                    if(cache.getIOType() == MISACacheIOType.Imported)
                        cacheName = "imported/" + cache.getRelativePath();
                    else
                        cacheName = "exported/" + cache.getRelativePath();
                    boolean isChecked = (boolean)model.getValueAt(i, 0);
                    if(isChecked != getNativeFilter().getCaches().contains(cacheName)) {
                        if(isChecked) {
                            getNativeFilter().addCache(cacheName);
                        }
                        else {
                            getNativeFilter().removeCache(cacheName);
                        }
                    }
                }
            }
        });
    }

    public MISAAttachmentCacheFilter getNativeFilter() {
        return (MISAAttachmentCacheFilter)getFilter();
    }

    public static class CacheCellRenderer extends JLabel implements TableCellRenderer {

        private ColorIcon icon = new ColorIcon(16,16);

        public CacheCellRenderer() {
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if(value instanceof MISACache) {
                MISACache cache = (MISACache)value;
                String cacheName;
                if(cache.getIOType() == MISACacheIOType.Imported)
                    cacheName = "imported/" + cache.getRelativePath();
                else
                    cacheName = "exported/" + cache.getRelativePath();
                setText(cacheName);
                icon.setColor(cache.toColor());
                setIcon(icon);

                if(isSelected || hasFocus) {
                    setBackground(new Color(184, 207, 229));
                }
                else {
                    setBackground(new Color(255,255,255));
                }

                setToolTipText("Data of type " + cache.getCacheTypeName());
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
