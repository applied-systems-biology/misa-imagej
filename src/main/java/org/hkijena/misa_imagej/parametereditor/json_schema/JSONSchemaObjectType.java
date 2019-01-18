package org.hkijena.misa_imagej.parametereditor.json_schema;

import org.hkijena.misa_imagej.utils.UIUtils;

import javax.swing.*;

public enum JSONSchemaObjectType {
    jsonObject,
    jsonString,
    jsonNumber,
    jsonBoolean,
    jsonArray;

    public Icon getIcon() {
        switch (this) {
            case jsonObject:
                return UIUtils.getIconFromResources("group.png");
            case jsonString:
                return UIUtils.getIconFromResources("text.png");
            case jsonBoolean:
                return UIUtils.getIconFromResources("checkbox.png");
            case jsonNumber:
                return UIUtils.getIconFromResources("pi.png");
            default:
                return null;
        }
    }
}
