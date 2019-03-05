package org.hkijena.misa_imagej.extension.attachmentfilters;

import com.google.common.eventbus.EventBus;
import org.hkijena.misa_imagej.api.workbench.MISAAttachmentDatabase;
import org.hkijena.misa_imagej.api.workbench.PreparedStatementValuesBuilder;
import org.hkijena.misa_imagej.api.workbench.filters.MISAAttachmentFilter;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class MISAAttachmentTypeFilter implements MISAAttachmentFilter {

    private MISAAttachmentDatabase database;
    private EventBus eventBus = new EventBus();
    private Set<String> serializationIds = new HashSet<>();
    boolean enabled = true;

    public MISAAttachmentTypeFilter(MISAAttachmentDatabase database) {
        this.database = database;
    }

    @Override
    public MISAAttachmentDatabase getDatabase() {
        return database;
    }

    @Override
    public String toSQLStatement() {
        return null;
    }

    @Override
    public void setSQLStatementVariables(PreparedStatementValuesBuilder builder) throws SQLException {

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
    }
}
