package org.hkijena.misa_imagej.api.workbench.keys;

import com.google.gson.JsonElement;
import org.hkijena.misa_imagej.api.MISAAttachment;
import org.hkijena.misa_imagej.api.json.JSONPath;

import java.util.Collection;

public class MISAJsonElementDataKey implements MISADataKey {

    private MISAAttachment attachment;
    private JSONPath path;

    public MISAJsonElementDataKey(MISAAttachment attachment, JSONPath path) {
        this.attachment = attachment;
        this.path = path;
    }

    public MISAAttachment getAttachment() {
        return attachment;
    }

    public JSONPath getPath() {
        return path;
    }

    @Override
    public void traverse(Collection<MISADataKey> result) {
        result.add(this);
        JsonElement element = path.getElement(attachment.getValue().rawData);
        if(element != null) {
            if(element.isJsonObject()) {
                for(String jkey : element.getAsJsonObject().keySet()) {
                    MISAJsonElementDataKey key = new MISAJsonElementDataKey(attachment, path.resolve(jkey));
                    key.traverse(result);
                }
            }
            else if(element.isJsonArray()) {
                for(int i = 0; i < element.getAsJsonArray().size(); ++i) {
                    MISAJsonElementDataKey key = new MISAJsonElementDataKey(attachment, path.resolve(i));
                    key.traverse(result);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "JSON element '" + path.toString() + "' @ " + attachment.toString();
    }
}
