package org.hkijena.misa_imagej.api.workbench.table;

import com.google.gson.JsonObject;
import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.MISASample;
import org.hkijena.misa_imagej.api.workbench.MISAAttachmentDatabase;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MISAAttachmentTableCacheColumn implements MISAAttachmentTableColumn {

    private MISAAttachmentDatabase database;

    public MISAAttachmentTableCacheColumn(MISAAttachmentDatabase database) {
        this.database = database;
    }

    @Override
    public Object getValue(int id, String sampleName, String cacheAndSubCache, String property, String serializationId, JsonObject json) throws SQLException {
        MISASample sample = database.getMisaOutput().getModuleInstance().getSample(sampleName);
        MISACache cache = sample.findMatchingCache(cacheAndSubCache);
        return cache.getFullRelativePath();
    }
}
