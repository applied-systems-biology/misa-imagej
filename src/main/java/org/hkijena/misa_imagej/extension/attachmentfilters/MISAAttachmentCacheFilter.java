package org.hkijena.misa_imagej.extension.attachmentfilters;

import com.google.common.eventbus.EventBus;
import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.MISASample;
import org.hkijena.misa_imagej.api.workbench.MISAAttachmentDatabase;
import org.hkijena.misa_imagej.api.workbench.PreparedStatementValuesBuilder;
import org.hkijena.misa_imagej.api.workbench.filters.MISAAttachmentFilter;
import org.hkijena.misa_imagej.api.workbench.filters.MISAAttachmentFilterChangedEvent;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MISAAttachmentCacheFilter implements MISAAttachmentFilter {

    private MISAAttachmentDatabase database;
    private EventBus eventBus = new EventBus();
    private boolean enabled = true;
    private Set<String> caches = new HashSet<>();

    public MISAAttachmentCacheFilter(MISAAttachmentDatabase database) {
        this.database = database;
        MISASample sample = database.getMisaOutput().getModuleInstance().getSamples().values().stream().findFirst().get();
        for(MISACache cache : sample.getImportedCaches()) {
            String cacheName = "imported/" + cache.getRelativePath();
            caches.add(cacheName);
        }
        for(MISACache cache : sample.getExportedCaches()) {
            String cacheName = "exported/" + cache.getRelativePath();
            caches.add(cacheName);
        }
    }

    public Collection<String> getCaches() {
        return caches;
    }

    public void addCache(String cache) {
        caches.add(cache);
        getEventBus().post(new MISAAttachmentFilterChangedEvent(this));
    }

    public void removeCache(String cache) {
        caches.remove(cache);
        getEventBus().post(new MISAAttachmentFilterChangedEvent(this));
    }

    @Override
    public MISAAttachmentDatabase getDatabase() {
        return database;
    }

    @Override
    public String toSQLStatement() {
        if(caches.isEmpty())
            return "false";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");
        boolean first = true;
        for(String cache : caches) {
            if(!first) {
                stringBuilder.append(" or ");
            }
            stringBuilder.append(" cache like ?");
            first = false;
        }
        stringBuilder.append(" )");
        return stringBuilder.toString();
    }

    @Override
    public void setSQLStatementVariables(PreparedStatementValuesBuilder builder) throws SQLException {
        for(String cache : caches) {
            builder.addString(cache + "%");
        }
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
