package org.hkijena.misa_imagej.api.workbench.filters;

public class MISAAttachmentFilterChangedEvent {
    private MISAAttachmentFilter filter;

    public MISAAttachmentFilterChangedEvent(MISAAttachmentFilter filter) {
        this.filter = filter;
    }

    public MISAAttachmentFilter getFilter() {
        return filter;
    }
}
