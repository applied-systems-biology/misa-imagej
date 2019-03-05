package org.hkijena.misa_imagej.extension.attachmentfilters;

import com.google.common.eventbus.EventBus;
import org.hkijena.misa_imagej.api.workbench.MISAAttachmentDatabase;
import org.hkijena.misa_imagej.api.workbench.PreparedStatementValuesBuilder;
import org.hkijena.misa_imagej.api.workbench.filters.MISAAttachmentFilter;
import org.hkijena.misa_imagej.api.workbench.filters.MISAAttachmentFilterChangedEvent;

public class MISAAttachmentSQLFilter implements MISAAttachmentFilter {

    private MISAAttachmentDatabase database;
    private String sql;
    private EventBus eventBus = new EventBus();

    public MISAAttachmentSQLFilter(MISAAttachmentDatabase database) {
        this.database = database;
    }

    @Override
    public MISAAttachmentDatabase getDatabase() {
        return database;
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

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }
}
