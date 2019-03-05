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
    private boolean enabled = true;

    public MISAAttachmentSampleFilter(MISAAttachmentDatabase database) {
        this.database = database;
        samples.addAll(database.getMisaOutput().getModuleInstance().getSamples().values());
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
        if(samples.isEmpty())
            return "false";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");
        boolean first = true;
        for(MISASample sample : samples) {
            if(!first) {
                stringBuilder.append(" or ");
            }
            stringBuilder.append(" sample is ?");
            first = false;
        }
        stringBuilder.append(" )");
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
