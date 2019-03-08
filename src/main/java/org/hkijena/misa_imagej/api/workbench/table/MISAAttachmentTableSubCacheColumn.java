package org.hkijena.misa_imagej.api.workbench.table;

import com.google.gson.JsonObject;
import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.MISASample;
import org.hkijena.misa_imagej.api.workbench.MISAAttachmentDatabase;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MISAAttachmentTableSubCacheColumn implements MISAAttachmentTableColumn {

    private MISAAttachmentDatabase database;

    public MISAAttachmentTableSubCacheColumn(MISAAttachmentDatabase database) {
        this.database = database;
    }

    @Override
    public String getSelectionStatement() {
        return "cache";
    }

    @Override
    public Object getValue(ResultSet resultSet, int columnIndex, JsonObject json) throws SQLException {
        String value = resultSet.getString(columnIndex + 1);
        MISASample sample = database.getMisaOutput().getModuleInstance().getOrCreateAnySample();
        MISACache cache = sample.findMatchingCache(value);
        return value.substring(cache.getFullRelativePath().length());
    }
}
