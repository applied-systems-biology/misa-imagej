package org.hkijena.misa_imagej.extension.attachmentfilters;

import org.hkijena.misa_imagej.api.workbench.MISAAttachmentDatabase;
import org.hkijena.misa_imagej.api.workbench.PreparedStatementValuesBuilder;
import org.hkijena.misa_imagej.api.workbench.filters.MISAAttachmentFilter;

import java.sql.SQLException;

public class MISAAttachmentRootTypeFilter extends MISAAttachmentFilter {

    public MISAAttachmentRootTypeFilter(MISAAttachmentDatabase database) {
        super(database);
    }

    @Override
    public String toSQLStatement() {
        return "\"serialization-id\" is property";
    }

    @Override
    public void setSQLStatementVariables(PreparedStatementValuesBuilder builder) throws SQLException {

    }
}
