package org.hkijena.misa_imagej.api.workbench.table;

import com.google.gson.JsonObject;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MISAAttachmentTableCacheAndSubCacheColumn implements MISAAttachmentTableColumn {

    @Override
    public Object getValue(int id, String sample, String cache, String property, String serializationId, JsonObject json) throws SQLException {
        return cache;
    }
}
