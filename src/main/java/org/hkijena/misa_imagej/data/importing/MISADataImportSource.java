package org.hkijena.misa_imagej.data.importing;

import org.hkijena.misa_imagej.data.MISAImportedData;

import java.nio.file.Path;

public interface MISADataImportSource {

    MISAImportedData getData();

    void runImport(Path importedDirectory, boolean forceCopy);

}
