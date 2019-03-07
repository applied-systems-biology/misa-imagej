package org.hkijena.misa_imagej.extension.attachmentfilters;

import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.MISASample;
import org.hkijena.misa_imagej.api.workbench.MISAAttachmentDatabase;
import org.hkijena.misa_imagej.api.workbench.PreparedStatementValuesBuilder;
import org.hkijena.misa_imagej.api.workbench.filters.MISAAttachmentFilter;
import org.hkijena.misa_imagej.api.workbench.filters.MISAAttachmentFilterChangedEvent;
import org.hkijena.misa_imagej.utils.SQLUtils;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MISAAttachmentCacheFilter extends MISAAttachmentFilter {

    private Set<String> caches = new HashSet<>();

    public MISAAttachmentCacheFilter(MISAAttachmentDatabase database) {
        super(database);
        MISASample sample = database.getMisaOutput().getModuleInstance().getOrCreateAnySample();
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
            stringBuilder.append(" cache like ? escape '\\'");
            first = false;
        }
        stringBuilder.append(" )");
        return stringBuilder.toString();
    }

    @Override
    public String toSQLQuery() {
        if(caches.isEmpty())
            return "false";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");
        boolean first = true;
        for(String cache : caches) {
            if(!first) {
                stringBuilder.append(" or ");
            }
            stringBuilder.append(" cache like ' escape '\\'").append(SQLUtils.escapeWildcardsForSQLite(cache)).append("%").append("'");
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
}
