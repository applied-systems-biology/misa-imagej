package org.hkijena.misa_imagej.ui.registries;

import org.hkijena.misa_imagej.ui.workbench.tableanalyzer.MISATableVectorOperation;
import org.hkijena.misa_imagej.ui.workbench.tableanalyzer.MISATableVectorOperationUI;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MISATableAnalyzerUIOperationRegistry {
    private Map<Class<? extends MISATableVectorOperation>, Entry> entries = new HashMap<>();

    public void register(Class<? extends MISATableVectorOperation> operationClass,
                         Class<? extends MISATableVectorOperationUI> uiClass,
                         String name,
                         String description,
                         Icon icon) {
        entries.put(operationClass, new Entry(operationClass,
                uiClass,
                name,
                description,
                icon));
    }

    public MISATableVectorOperationUI createUIFor(MISATableVectorOperation operation) {
        try {
            return entries.get(operation.getClass()).getUiClass().getConstructor(MISATableVectorOperation.class).newInstance(operation);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public Collection<Entry> getEntries() {
        return entries.values();
    }

    public static class Entry {
        private Class<? extends MISATableVectorOperation> operationClass;
        private Class<? extends MISATableVectorOperationUI> uiClass;
        private String name;
        private String description;
        private Icon icon;

        public Entry(Class<? extends MISATableVectorOperation> operationClass, Class<? extends MISATableVectorOperationUI> uiClass, String name, String description, Icon icon) {
            this.operationClass = operationClass;
            this.uiClass = uiClass;
            this.name = name;
            this.description = description;
            this.icon = icon;
        }

        public Class<? extends MISATableVectorOperationUI> getUiClass() {
            return uiClass;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public Icon getIcon() {
            return icon;
        }

        public Class<? extends MISATableVectorOperation> getOperationClass() {
            return operationClass;
        }

        public MISATableVectorOperation instantiateOperation() {
            try {
                return operationClass.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
