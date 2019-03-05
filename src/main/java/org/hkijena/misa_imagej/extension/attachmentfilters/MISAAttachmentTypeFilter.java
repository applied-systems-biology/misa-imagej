package org.hkijena.misa_imagej.extension.attachmentfilters;

import com.google.common.eventbus.EventBus;
import org.hkijena.misa_imagej.api.MISASample;
import org.hkijena.misa_imagej.api.workbench.MISAAttachmentDatabase;
import org.hkijena.misa_imagej.api.workbench.PreparedStatementValuesBuilder;
import org.hkijena.misa_imagej.api.workbench.filters.MISAAttachmentFilter;
import org.hkijena.misa_imagej.api.workbench.filters.MISAAttachmentFilterChangedEvent;

import java.sql.SQLException;
import java.util.*;

public class MISAAttachmentTypeFilter extends MISAAttachmentFilter {

    private Set<String> serializationIds = new HashSet<>();


    public MISAAttachmentTypeFilter(MISAAttachmentDatabase database) {
        super(database);
        if(database.getMisaOutput().hasAttachmentSchemas()) {
            serializationIds.addAll(database.getMisaOutput().getAttachmentSchemas().keySet());
        }
    }

    @Override
    public String toSQLStatement() {
        if(serializationIds.isEmpty())
            return "false";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");
        boolean first = true;
        for(String id : serializationIds) {
            if(!first) {
                stringBuilder.append(" or ");
            }
            stringBuilder.append(" \"serialization-id\" is ?");
            first = false;
        }
        stringBuilder.append(" )");
        return stringBuilder.toString();
    }

    @Override
    public void setSQLStatementVariables(PreparedStatementValuesBuilder builder) throws SQLException {
        for(String id : serializationIds) {
            builder.addString(id);
        }
    }

    public void addSerializationId(String id) {
        serializationIds.add(id);
        getEventBus().post(new MISAAttachmentFilterChangedEvent(this));
    }

    public void removeSerializationId(String id) {
        serializationIds.remove(id);
        getEventBus().post(new MISAAttachmentFilterChangedEvent(this));
    }

    public Collection<String> getSerializationIds() {
        return Collections.unmodifiableCollection(serializationIds);
    }
}
