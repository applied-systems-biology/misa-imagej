package org.hkijena.misa_imagej.api.workbench.keys;

import org.hkijena.misa_imagej.api.MISACache;
import org.hkijena.misa_imagej.api.MISASample;

import java.util.Collection;

public class MISASampleDataKey implements MISADataKey {

    private MISASample sample;

    public MISASampleDataKey(MISASample sample) {
        this.sample = sample;
    }

    public MISASample getSample() {
        return sample;
    }

    @Override
    public void traverse(Collection<MISADataKey> result) {
        result.add(this);
        for(MISACache cache : sample.getImportedCaches()) {
            MISACacheDataKey key = new MISACacheDataKey(cache);
            key.traverse(result);
        }
        for(MISACache cache : sample.getExportedCaches()) {
            MISACacheDataKey key = new MISACacheDataKey(cache);
            key.traverse(result);
        }
    }

    @Override
    public String toString() {
        return "Sample " + sample.name;
    }
}
