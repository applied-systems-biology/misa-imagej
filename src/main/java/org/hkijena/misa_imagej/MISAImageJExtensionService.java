package org.hkijena.misa_imagej;

import org.scijava.service.Service;

public interface MISAImageJExtensionService extends Service {
    /**
     * Registers custom types into MISA-ImageJ
     * @param registryService
     */
    void register(MISAImageJRegistryService registryService);
}
