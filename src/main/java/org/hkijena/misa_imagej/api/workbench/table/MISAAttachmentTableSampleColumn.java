package org.hkijena.misa_imagej.api.workbench.table;

import com.google.gson.JsonObject;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MISAAttachmentTableSampleColumn implements MISAAttachmentTableColumn {
    @Override
    public String getSelectionStatement() {
        return "sample";
    }

    @Override
    public Object getValue(ResultSet resultSet, int columnIndex, JsonObject json) throws SQLException {
        return resultSet.getString(columnIndex);
    }
}
