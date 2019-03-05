package org.hkijena.misa_imagej.extension.attachmentfilters;

import com.google.common.eventbus.EventBus;
import org.hkijena.misa_imagej.api.MISASample;
import org.hkijena.misa_imagej.api.workbench.MISAAttachmentDatabase;
import org.hkijena.misa_imagej.api.workbench.PreparedStatementValuesBuilder;
import org.hkijena.misa_imagej.api.workbench.filters.MISAAttachmentFilter;
import org.hkijena.misa_imagej.api.workbench.filters.MISAAttachmentFilterChangedEvent;

import java.sql.SQLException;
import java.util.*;

public class MISAAttachmentSampleFilter implements MISAAttachmentFilter {

    private MISAAttachmentDatabase database;
    private EventBus eventBus = new EventBus();
    private Set<MISASample> samples = new HashSet<>();

    public MISAAttachmentSampleFilter(MISAAttachmentDatabase database) {
        this.database = database;
    }

    public Collection<MISASample> getSamples() {
        return samples;
    }

    public void addSample(MISASample sample) {
        samples.add(sample);
        getEventBus().post(new MISAAttachmentFilterChangedEvent(this));
    }

    public void removeSample(MISASample sample) {
        samples.remove(sample);
        getEventBus().post(new MISAAttachmentFilterChangedEvent(this));
    }

    @Override
    public String toSQLStatement() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");
        for(MISASample sample : samples) {
            if(stringBuilder.length() > 0)
                stringBuilder.append(" or ");
            stringBuilder.append(" sample is ?");
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    @Override
    public void setSQLStatementVariables(PreparedStatementValuesBuilder builder) throws SQLException {
        for(MISASample sample : samples) {
            builder.addString(sample.getName());
        }
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public MISAAttachmentDatabase getDatabase() {
        return database;
    }
}
