package org.hkijena.misa_imagej.api.workbench.table;

import com.google.gson.JsonObject;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface MISAAttachmentTableColumn {
    String getSelectionStatement();
    Object getValue(ResultSet resultSet, int columnIndex, JsonObject json) throws SQLException;
}
