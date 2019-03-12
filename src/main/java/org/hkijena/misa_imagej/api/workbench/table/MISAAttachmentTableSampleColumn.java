package org.hkijena.misa_imagej.api.workbench.table;

import org.hkijena.misa_imagej.api.MISAAttachment;

import java.sql.SQLException;

public class MISAAttachmentTableSampleColumn implements MISAAttachmentTableColumn {
    @Override
    public Object getValue(MISAAttachmentTable table, int id, String sample, String cache, String property, String serializationId, MISAAttachment attachment) throws SQLException {
        return sample;
    }

    @Override
    public String getName() {
        return "Sample";
    }
}