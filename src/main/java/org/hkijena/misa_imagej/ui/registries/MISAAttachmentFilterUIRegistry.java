package org.hkijena.misa_imagej.ui.registries;

import org.hkijena.misa_imagej.api.workbench.MISAAttachmentDatabase;
import org.hkijena.misa_imagej.api.workbench.filters.MISAAttachmentFilter;
import org.hkijena.misa_imagej.ui.workbench.objectbrowser.MISAAttachmentFilterUI;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MISAAttachmentFilterUIRegistry {
    private Map<Class<? extends MISAAttachmentFilter>, Class<? extends MISAAttachmentFilterUI>> registeredFilters = new HashMap<>();
    private Map<Class<? extends MISAAttachmentFilter>, String> registeredFilterNames = new HashMap<>();
    private Map<Class<? extends MISAAttachmentFilter>, Icon> registeredFilterIcons = new HashMap<>();

    public void register(Class<? extends MISAAttachmentFilter> filterClass, Class<? extends MISAAttachmentFilterUI> uiClass,
                         String name, Icon icon) {
        registeredFilters.put(filterClass, uiClass);
        registeredFilterNames.put(filterClass, name);
        registeredFilterIcons.put(filterClass, icon);
    }

    public Collection<Class<? extends MISAAttachmentFilter>> getFilterTypes() {
        return registeredFilters.keySet();
    }

    /**
     * Creates a menu item that adds the filter class to the target database
     * @param filterClass
     * @param target
     * @return
     */
    public JMenuItem createMenuItem(Class<? extends MISAAttachmentFilter> filterClass, MISAAttachmentDatabase target) {
        JMenuItem item = new JMenuItem(registeredFilterNames.get(filterClass), registeredFilterIcons.get(filterClass));
        item.addActionListener(e -> {
            try {
                target.addFilter(filterClass.getConstructor(MISAAttachmentDatabase.class).newInstance(target));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e1) {
                throw new RuntimeException(e1);
            }
        });
        return item;
    }

    public MISAAttachmentFilterUI createUIFor(MISAAttachmentFilter filter) {
        try {
            return registeredFilters.get(filter.getClass()).getConstructor(MISAAttachmentFilter.class).newInstance(filter);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public String getNameFilterName(Class<? extends MISAAttachmentFilter> filterClass) {
        return registeredFilterNames.get(filterClass);
    }
}
