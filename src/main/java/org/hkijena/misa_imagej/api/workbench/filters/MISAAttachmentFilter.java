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

    /**
     * Gets the event bus
     * @return
     */
    EventBus getEventBus();

    /**
     * Returns true if the filter is enabled
     * @return
     */
    boolean isEnabled();

    /**
     * Enables or disables the filter
     * @param enabled
     */
    void setEnabled(boolean enabled);
}
