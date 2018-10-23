package misa_imagej_template.data.importing;

import misa_imagej_template.data.MISAImportedData;

import java.nio.file.Path;

public interface MISADataImportSource {

    MISAImportedData getData();

    void runImport(Path importedDirectory, boolean forceCopy);

}
