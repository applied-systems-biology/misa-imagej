package org.hkijena.misa_imagej.api.parameterschema;

import java.util.*;

public class ParameterSchemaValidityReport {

    public static class Entry {
        private Object object;
        private List<String> categories;
        private boolean valid;
        private String message;

        public Entry(Object object, List<String> categories, boolean valid, String message) {
            this.object = object;
            this.categories = categories;
            this.valid = valid;
            this.message = message;
        }

        public Object getObject() {
            return object;
        }

        public List<String> getCategories() {
            return Collections.unmodifiableList(categories);
        }

        public void addCategories(List<String> categories) {
            this.categories.addAll(categories);
        }

        public void addCategory(String category) {
            if(category != null)
                this.categories.add(category);
        }

        public String getMessage() {
            return message;
        }

        public boolean isValid() {
            return valid;
        }

        public void markAsInvalid(String message) {
            this.valid = false;
            this.message = message;
        }
    }

    private Map<Object, Entry> entries = new HashMap<>();

    public ParameterSchemaValidityReport() {

    }

    public ParameterSchemaValidityReport(Object target, String category, boolean valid, String message) {
        this.report(target, category, valid, message);
    }

    /**
     * Reports the target object to the validity report
     * @param target
     * @param category
     * @param valid
     */
    public void report(Object target, String category, boolean valid, String message) {
        Entry e = entries.getOrDefault(target, null);
        if(e == null) {
            List<String> c = new ArrayList<>();
            if(category != null)
                c.add(category);
            e = new Entry(target, c, valid, message);
            entries.put(target, e);
        }
        else {
            e.addCategory(category);
            if(!valid)
                e.markAsInvalid(message);
        }
    }

    /**
     * Merges another report into this report under the list of specified categories
     * @param subreport
     * @param category
     */
    public void merge(ParameterSchemaValidityReport subreport, String... category) {
        for(Object key : subreport.entries.keySet()) {
            if(entries.containsKey(key)) {
                Entry src = subreport.entries.get(key);
                Entry dst = entries.get(key);
                dst.addCategories(src.getCategories());
                if(!src.isValid())
                    dst.markAsInvalid(src.getMessage());
            }
            else {
                entries.put(key, subreport.entries.get(key));
            }
        }
    }

    /**
     * Returns true if all reports are positive
     * @return
     */
    public boolean isValid() {
        for(Entry entry : entries.values()) {
            if(!entry.isValid()) {
                return false;
            }
        }
        return true;
    }

    public Map<Object, Entry> getEntries() {
        return Collections.unmodifiableMap(entries);
    }

    public Map<Object, Entry> getInvalidEntries() {
        HashMap<Object, Entry> result = new HashMap<>();
        for(Map.Entry<Object, Entry> kv : entries.entrySet()) {
            if(!kv.getValue().isValid())
                result.put(kv.getKey(), kv.getValue());
        }
        return Collections.unmodifiableMap(result);
    }

}
