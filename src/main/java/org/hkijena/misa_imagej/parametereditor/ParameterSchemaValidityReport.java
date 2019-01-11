package org.hkijena.misa_imagej.parametereditor;

import java.util.*;

public class ParameterSchemaValidityReport {

    private Map<Object, Boolean> entries = new HashMap<>();
    private Map<Object, List<String>> categories = new HashMap<>();
    private Map<Object, String> messages = new HashMap<>();

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
        entries.put(target, valid);
        messages.put(target, message);
        if(category != null) {
            List<String> c = new ArrayList<>();
            c.add(category);
            categories.put(target, c);
        }
        else
            categories.put(target, new ArrayList<>());
    }

    /**
     * Merges another report into this report under the list of specified categories
     * @param subreport
     * @param category
     */
    public void merge(ParameterSchemaValidityReport subreport, String... category) {
        for(Object key : subreport.entries.keySet()) {
            entries.put(key, subreport.entries.get(key));
            messages.put(key, subreport.messages.get(key));
            List<String> cat = new ArrayList<>(Arrays.asList(category));
            cat.addAll(subreport.categories.get(key));
            categories.put(key, cat);
        }
    }

    /**
     * Returns true if all reports are positive
     * @return
     */
    public boolean isValid() {
        for(Boolean value : entries.values()) {
            if(!value) {
                return false;
            }
        }
        return true;
    }

}
