package org.hkijena.misa_imagej.api.workbench.filters;

import com.google.common.eventbus.EventBus;
import org.hkijena.misa_imagej.api.workbench.MISAAttachmentDatabase;
import org.hkijena.misa_imagej.api.workbench.PreparedStatementValuesBuilder;

import java.sql.SQLException;

/**
 * A class that builds a SQL query to filter attachments
 * If the query changes, a {@link MISAAttachmentFilterChangedEvent} is triggered
 */
public interface MISAAttachmentFilter {

    /**
     * Returns the database
     * @return
     */
    MISAAttachmentDatabase getDatabase();

    /**
     * Used to build a prepared statement
     * @return
     */
    String toSQLStatement();

    /**
     * Adds the missing variables one after another
     * @param builder
     */
    void setSQLStatementVariables(PreparedStatementValuesBuilder builder) throws SQLException;

    EventBus getEventBus();
}
