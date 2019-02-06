package org.hkijena.misa_imagej.api.workbench.keys;

import java.util.Collection;

public interface MISADataKey {

    /**
     * Returns this and all nth-child data keys
     * @param result
     */
    void traverse(Collection<MISADataKey> result);

}
