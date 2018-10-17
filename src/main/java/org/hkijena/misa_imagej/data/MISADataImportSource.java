package org.hkijena.misa_imagej.data;

import java.nio.file.Path;

public interface MISADataImportSource {

    MISAImportedData getData();

    void runImport(Path importedDirectory, boolean forceCopy);

}
