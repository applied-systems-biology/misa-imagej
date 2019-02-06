package org.hkijena.misa_imagej.api.workbench;

import org.hkijena.misa_imagej.api.MISAAttachment;
import org.hkijena.misa_imagej.api.MISASerializable;

public class MISAAttachmentIndex {

    private MISAOutput misaOutput;

    /**
     * The index will only load one attachment per time to save memory
     */
    private MISAAttachment currentCachedAttachment;

    /**
     * The index will only load one attachment per time to save memory
     */
    private MISASerializable currentCachedAttachmentData;

    public MISAAttachmentIndex(MISAOutput output) {
        this.misaOutput = output;
    }

    /**
     * Gets the value of an attachment
     * @param attachment
     * @return
     */
    public MISASerializable getAttachment(MISAAttachment attachment) {
        if(currentCachedAttachment == attachment && currentCachedAttachmentData != null)
            return currentCachedAttachmentData;
        else {
            currentCachedAttachment = attachment;
            currentCachedAttachmentData = attachment.getValue();
            return currentCachedAttachmentData;
        }
    }

}
