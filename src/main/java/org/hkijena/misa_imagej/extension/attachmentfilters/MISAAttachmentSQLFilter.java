package org.hkijena.misa_imagej.extension.attachmentfilters;

import com.google.common.eventbus.EventBus;
import org.hkijena.misa_imagej.api.workbench.MISAAttachmentDatabase;
import org.hkijena.misa_imagej.api.workbench.PreparedStatementValuesBuilder;
import org.hkijena.misa_imagej.api.workbench.filters.MISAAttachmentFilter;
import org.hkijena.misa_imagej.api.workbench.filters.MISAAttachmentFilterChangedEvent;

public class MISAAttachmentSQLFilter implements MISAAttachmentFilter {

    private MISAAttachmentDatabase database;
    private String sql = "true";
    private EventBus eventBus = new EventBus();
    private boolean enabled = true;

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

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        getEventBus().post(new MISAAttachmentFilterChangedEvent(this));
    }
}
