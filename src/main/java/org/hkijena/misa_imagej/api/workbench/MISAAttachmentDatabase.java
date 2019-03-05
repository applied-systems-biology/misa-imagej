package org.hkijena.misa_imagej.api.workbench;

import com.google.common.eventbus.EventBus;
import org.hkijena.misa_imagej.api.workbench.filters.MISAAttachmentFilter;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MISAAttachmentDatabase {

    private MISAOutput misaOutput;
    private Connection databaseConnection;
    private List<MISAAttachmentFilter> filters = new ArrayList<>();
    private EventBus eventBus = new EventBus();

    public MISAAttachmentDatabase(MISAOutput misaOutput) {
        this.misaOutput = misaOutput;
        try {
            initialize();
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void initialize() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        databaseConnection = DriverManager.getConnection("jdbc:sqlite:" + misaOutput.getRootPath().resolve("attachment-index.sqlite"));
    }

    public MISAOutput getMisaOutput() {
        return misaOutput;
    }

    public List<MISAAttachmentFilter> getFilters() {
        return Collections.unmodifiableList(filters);
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public void addFilter(MISAAttachmentFilter filter) {
        if(!filters.contains(filter)) {
            filters.add(filter);
            getEventBus().post(new AddedFilterEvent(this, filter));
        }
    }

    public void removeFilter(MISAAttachmentFilter filter) {
        filters.remove(filter);
        getEventBus().post(new RemovedFilterEvent(this, filter));
    }

    public ResultSet query(String selectionStatement) {
        StringBuilder template = new StringBuilder();
        template.append("select ").append(selectionStatement).append(" from attachments");
        if(!filters.isEmpty()) {
            template.append(" where");
            boolean first = true;
            for(MISAAttachmentFilter filter : filters) {
                if(!first) {
                    template.append(" and ");
                }
                else {
                    template.append(" ");
                    first = false;
                }
                template.append(filter.toSQLStatement());
            }
        }

        try {
            PreparedStatement statement = databaseConnection.prepareStatement(template.toString());
            PreparedStatementValuesBuilder builder = new PreparedStatementValuesBuilder(statement);
            for(MISAAttachmentFilter filter : filters) {
                filter.setSQLStatementVariables(builder);
            }
            return statement.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getDatasetCount() {
        ResultSet resultSet = query("count()");
        try {
            return resultSet.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static class AddedFilterEvent {
        private MISAAttachmentDatabase database;
        private MISAAttachmentFilter filter;

        public AddedFilterEvent(MISAAttachmentDatabase database, MISAAttachmentFilter filter) {
            this.database = database;
            this.filter = filter;
        }

        public MISAAttachmentDatabase getDatabase() {
            return database;
        }

        public MISAAttachmentFilter getFilter() {
            return filter;
        }
    }

    public static class RemovedFilterEvent {
        private MISAAttachmentDatabase database;
        private MISAAttachmentFilter filter;

        public RemovedFilterEvent(MISAAttachmentDatabase database, MISAAttachmentFilter filter) {
            this.database = database;
            this.filter = filter;
        }

        public MISAAttachmentDatabase getDatabase() {
            return database;
        }

        public MISAAttachmentFilter getFilter() {
            return filter;
        }
    }

    public static class UpdatedFiltersEvent {
        private MISAAttachmentDatabase database;

        public UpdatedFiltersEvent(MISAAttachmentDatabase database) {
            this.database = database;
        }

        public MISAAttachmentDatabase getDatabase() {
            return database;
        }
    }
}
