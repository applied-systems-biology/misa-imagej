package org.hkijena.misa_imagej.api.workbench.keys;

import org.hkijena.misa_imagej.api.MISAAttachment;
import org.hkijena.misa_imagej.api.MISACache;

import java.util.Collection;

public class MISACacheDataKey implements MISADataKey {

    private MISACache cache;

    public MISACacheDataKey(MISACache cache) {
        this.cache = cache;
    }

    public MISACache getCache() {
        return cache;
    }

    @Override
    public void traverse(Collection<MISADataKey> result) {
        result.add(this);
        for(MISAAttachment attachment : cache.getAttachments().values()) {
            MISAAttachmentDataKey key = new MISAAttachmentDataKey(attachment);
            key.traverse(result);
        }
    }

    @Override
    public String toString() {
        return "Cache " + cache.toString();
    }
}
