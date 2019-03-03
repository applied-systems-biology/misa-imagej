package org.hkijena.misa_imagej.api;

import org.hkijena.misa_imagej.MISAImageJService;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

@Plugin(type = MISAImageJService.class)
public class MISAImageJServiceImpl extends AbstractService implements MISAImageJService {
}
