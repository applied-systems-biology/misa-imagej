package org.hkijena.misa_imagej.api.workbench.table;

import org.hkijena.misa_imagej.api.MISAAttachment;

import java.sql.SQLException;

public class MISAAttachmentTableJsonValueColumn implements MISAAttachmentTableColumn {

    private String propertyName;

    public MISAAttachmentTableJsonValueColumn(String propertyName) {
        this.propertyName = propertyName;
    }

    @Override
    public Object getValue(MISAAttachmentTable table, int id, String sample, String cache, String property, String serializationId, MISAAttachment attachment) throws SQLException {
        return attachment.getProperty(propertyName);
    }

    @Override
    public String getName() {
        return propertyName;
    }

    /**
     * The propertyName that is queried from the attachment
     * @return
     */
    public String getPropertyName() {
        return propertyName;
    }
}
