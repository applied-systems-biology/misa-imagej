package org.hkijena.misa_imagej.api;

import org.hkijena.misa_imagej.api.registries.MISACacheRegistry;
import org.hkijena.misa_imagej.api.registries.MISASerializableRegistry;
import org.scijava.service.Service;

public interface MISAImageJAPIService extends Service {

    MISACacheRegistry getCacheRegistry();
    MISASerializableRegistry getSerializableRegistry();

}
