package org.hkijena.misa_imagej.api.workbench.table;

import org.hkijena.misa_imagej.api.MISAAttachment;
import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.MISASample;
import org.hkijena.misa_imagej.api.workbench.MISAAttachmentDatabase;

import java.sql.SQLException;

public class MISAAttachmentTableSubCacheColumn implements MISAAttachmentTableColumn {

    private MISAAttachmentDatabase database;

    public MISAAttachmentTableSubCacheColumn(MISAAttachmentDatabase database) {
        this.database = database;
    }

    @Override
    public Object getValue(int id, String sampleName, String cacheAndSubCache, String property, String serializationId, MISAAttachment attachment) throws SQLException {
        MISASample sample = database.getMisaOutput().getModuleInstance().getSample(sampleName);
        MISACache cache = sample.findMatchingCache(cacheAndSubCache);
        return cacheAndSubCache.substring(cache.getFullRelativePath().length());
    }
}
