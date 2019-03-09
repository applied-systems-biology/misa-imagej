package org.hkijena.misa_imagej.api.workbench.table;

import org.hkijena.misa_imagej.api.MISAAttachment;

import java.sql.SQLException;

public class MISAAttachmentTableCacheAndSubCacheColumn implements MISAAttachmentTableColumn {

    @Override
    public Object getValue(int id, String sample, String cache, String property, String serializationId, MISAAttachment attachment) throws SQLException {
        return cache;
    }
}
