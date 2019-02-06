package org.hkijena.misa_imagej.api.workbench.keys;

import org.hkijena.misa_imagej.api.MISAParameterSchema;
import org.hkijena.misa_imagej.api.MISASample;
import org.hkijena.misa_imagej.api.workbench.MISAOutput;

import java.util.Collection;

public class MISARunDataKey implements MISADataKey {

    private MISAOutput output;

    public MISARunDataKey(MISAOutput output) {
        this.output = output;
    }

    @Override
    public void traverse(Collection<MISADataKey> result) {
        result.add(this);
        for(MISASample sample : output.getParameterSchema().getSamples()) {
            MISASampleDataKey key = new MISASampleDataKey(sample);
            key.traverse(result);
        }
    }

    @Override
    public String toString() {
        return "Run @ " + output.getRootPath();
    }
}
