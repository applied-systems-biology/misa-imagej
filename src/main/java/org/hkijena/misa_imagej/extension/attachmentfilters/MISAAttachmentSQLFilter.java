package org.hkijena.misa_imagej.extension.attachmentfilters;

import org.hkijena.misa_imagej.api.workbench.MISAAttachmentDatabase;
import org.hkijena.misa_imagej.api.workbench.PreparedStatementValuesBuilder;
import org.hkijena.misa_imagej.api.workbench.filters.MISAAttachmentFilter;
import org.hkijena.misa_imagej.api.workbench.filters.MISAAttachmentFilterChangedEvent;

public class MISAAttachmentSQLFilter extends MISAAttachmentFilter {

    private String sql = "true";

    public MISAAttachmentSQLFilter(MISAAttachmentDatabase database) {
        super(database);
    }

    @Override
    public String toSQLStatement() {
        return sql;
    }

    @Override
    public void setSQLStatementVariables(PreparedStatementValuesBuilder builder) {

    }

    public void setSql(String sql) {
        this.sql = sql;
        getEventBus().post(new MISAAttachmentFilterChangedEvent(this));
    }
}
