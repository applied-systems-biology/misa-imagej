package org.hkijena.misa_imagej.api.workbench.keys;

import org.hkijena.misa_imagej.api.MISAAttachment;
import org.hkijena.misa_imagej.api.json.JSONPath;

import java.util.Collection;

public class MISAAttachmentDataKey implements MISADataKey {

    private MISAAttachment attachment;

    public MISAAttachmentDataKey(MISAAttachment attachment) {
        this.attachment = attachment;
    }

    public MISAAttachment getAttachment() {
        return attachment;
    }

    @Override
    public void traverse(Collection<MISADataKey> result) {
        result.add(this);
        MISAJsonElementDataKey key = new MISAJsonElementDataKey(attachment, new JSONPath());
        key.traverse(result);
    }

    @Override
    public String toString() {
        return "Attachment " + attachment.getLocation().toString();
    }
}
