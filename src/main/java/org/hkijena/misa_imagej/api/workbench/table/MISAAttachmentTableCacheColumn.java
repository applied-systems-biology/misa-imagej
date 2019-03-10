package org.hkijena.misa_imagej.api.workbench.table;

import org.hkijena.misa_imagej.api.MISAAttachment;
import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.MISASample;
import org.hkijena.misa_imagej.api.workbench.MISAAttachmentDatabase;

import java.sql.SQLException;

public class MISAAttachmentTableCacheColumn implements MISAAttachmentTableColumn {

    @Override
    public Object getValue(MISAAttachmentTable table, int id, String sampleName, String cacheAndSubCache, String property, String serializationId, MISAAttachment attachment) throws SQLException {
        MISASample sample = table.getDatabase().getMisaOutput().getModuleInstance().getSample(sampleName);
        MISACache cache = sample.findMatchingCache(cacheAndSubCache);
        return cache.getFullRelativePath();
    }

    @Override
    public String getName() {
        return "Data";
    }
}
